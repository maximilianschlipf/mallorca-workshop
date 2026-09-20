package de.nordwind.schulungsplaner.service;

import de.nordwind.schulungsplaner.domain.Benutzerkonto;
import de.nordwind.schulungsplaner.domain.Rolle;
import de.nordwind.schulungsplaner.katalog.Katalogschulung;
import de.nordwind.schulungsplaner.katalog.SchulungId;
import de.nordwind.schulungsplaner.katalog.ablage.KatalogRepository;
import de.nordwind.schulungsplaner.katalog.zustand.SchulungszustandRepository;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.net.URI;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
public class TerminService {
    private static final Set<String> ZUGANGSARTEN = Set.of("oeffentlich", "exklusiv");
    private static final Set<String> DURCHFUEHRUNGSARTEN =
            Set.of("remote", "vor_ort", "beim_kunden", "hybrid");
    private final JdbcTemplate jdbc;
    private final KontoService konten;
    private final KatalogRepository katalog;
    private final SchulungszustandRepository zustaende;
    private final Clock clock;
    private final BenachrichtigungService benachrichtigungen;
    private final VorgangService vorgaenge;

    public TerminService(JdbcTemplate jdbc, KontoService konten, KatalogRepository katalog,
                         SchulungszustandRepository zustaende, Clock clock,
                         BenachrichtigungService benachrichtigungen, VorgangService vorgaenge) {
        this.jdbc = jdbc;
        this.konten = konten;
        this.katalog = katalog;
        this.zustaende = zustaende;
        this.clock = clock;
        this.benachrichtigungen = benachrichtigungen;
        this.vorgaenge = vorgaenge;
    }

    @Transactional
    public LocalDate enddatumVorschlagen(String schulungId, LocalDate startdatum) {
        nachziehen();
        Katalogschulung schulung = schulung(schulungId, false);
        if (startdatum == null) throw fehler(HttpStatus.BAD_REQUEST, "STARTDATUM_FEHLT", "Das Startdatum fehlt.");
        LocalDate tag = startdatum;
        for (int rest = schulung.dauerInTagen() - 1; rest > 0;) {
            tag = tag.plusDays(1);
            if (istWerktag(tag)) rest--;
        }
        return tag;
    }

    @Transactional
    public TerminAnsicht anlegen(String administratorId, TerminEingabe eingabe) {
        nachziehen();
        pruefeAdministrator(administratorId);
        if (eingabe == null || eingabe.schulungId() == null) {
            throw fehler(HttpStatus.BAD_REQUEST, "SCHULUNG_FEHLT", "Die Schulung fehlt.");
        }
        schulung(eingabe.schulungId(), true);
        pruefeZeitraum(eingabe.startdatum(), eingabe.enddatum(), true);
        NormalisierteFelder felder = normalisiereFelder(eingabe, null);
        String id = naechsteId(eingabe.schulungId());
        jdbc.update("""
                INSERT INTO termin
                (termin_id, schulung_id, startdatum, enddatum, ort, format, status,
                 zugangsart, durchfuehrungsart, kundenfirma, online_zugang)
                VALUES (?, ?, ?, ?, ?, ?, 'geplant', ?, ?, ?, ?)
                """, id, eingabe.schulungId(), eingabe.startdatum(), eingabe.enddatum(),
                felder.ort(), formatText(felder.durchfuehrungsart()), felder.zugangsart(),
                felder.durchfuehrungsart(), felder.kundenfirma(), felder.onlineZugang());
        if (eingabe.trainerId() != null && !eingabe.trainerId().isBlank()) {
            trainerZuweisen(administratorId, id, eingabe.trainerId(), false);
        }
        return details(administratorId, id);
    }

    @Transactional
    public TerminAnsicht aendern(String administratorId, String terminId, TerminEingabe eingabe) {
        nachziehen();
        pruefeAdministrator(administratorId);
        TerminZeile alt = lade(terminId, true);
        verlangeGeplant(alt);
        if (eingabe.schulungId() != null && !eingabe.schulungId().equals(alt.schulungId())) {
            throw fehler(HttpStatus.CONFLICT, "SCHULUNG_UNVERAENDERLICH",
                    "Die zugeordnete Schulung kann nicht geändert werden.");
        }
        LocalDate neuStart = eingabe.startdatum() == null ? alt.startdatum() : eingabe.startdatum();
        LocalDate neuEnde = eingabe.enddatum() == null ? alt.enddatum() : eingabe.enddatum();
        pruefeAenderbarenZeitraum(alt, neuStart, neuEnde);
        if (!neuStart.equals(alt.startdatum()) || !neuEnde.equals(alt.enddatum())) {
            pruefeZuweisungen(terminId, neuStart, neuEnde);
        }
        NormalisierteFelder felder = normalisiereFelder(eingabe, alt);
        pruefeBuchungsfirmen(terminId, alt, felder, eingabe);
        List<String> nachrichten = aenderungsnachrichten(alt, neuStart, neuEnde, felder);
        jdbc.update("""
                UPDATE termin SET startdatum=?, enddatum=?, ort=?, format=?, zugangsart=?,
                    durchfuehrungsart=?, kundenfirma=?, online_zugang=?, version=version+1
                WHERE termin_id=?
                """, neuStart, neuEnde, felder.ort(), formatText(felder.durchfuehrungsart()),
                felder.zugangsart(), felder.durchfuehrungsart(), felder.kundenfirma(),
                felder.onlineZugang(), terminId);
        if ("exklusiv".equals(felder.zugangsart()) && felder.kundenfirma() != null
                && (!"exklusiv".equals(alt.zugangsart())
                    || !Objects.equals(alt.kundenfirma(), felder.kundenfirma()))) {
            jdbc.update("UPDATE teilnehmerbuchung SET firma=? WHERE termin_id=?",
                    felder.kundenfirma(), terminId);
        }
        nachrichten.forEach(text -> benachrichtigeBeteiligte(terminId, administratorId,
                Benachrichtigungsanlass.TERMIN_GEAENDERT, text));
        return details(administratorId, terminId);
    }

    @Transactional
    public void trainerZuweisen(String administratorId, String terminId, String trainerId,
                                boolean rollenwechselBestaetigt) {
        nachziehen();
        pruefeAdministrator(administratorId);
        TerminZeile termin = lade(terminId, true);
        verlangeGeplant(termin);
        pruefeTrainer(trainerId, termin.schulungId(), termin.startdatum(), termin.enddatum(), terminId);
        boolean istAssistent = existiert("""
                SELECT COUNT(*) FROM termin_assistent
                WHERE termin_id=? AND benutzerkonto_id=?
                """, terminId, trainerId);
        if (istAssistent && !rollenwechselBestaetigt) {
            throw fehler(HttpStatus.CONFLICT, "ROLLENWECHSEL_BESTAETIGEN",
                    "Der Wechsel von der Assistenz zur Trainerzuweisung muss bestätigt werden.");
        }
        String bisher = termin.trainerId();
        if (istAssistent) jdbc.update("DELETE FROM termin_assistent WHERE termin_id=? AND benutzerkonto_id=?",
                terminId, trainerId);
        jdbc.update("UPDATE termin SET trainer_id=?, trainer_name_snapshot=NULL, version=version+1 WHERE termin_id=?",
                trainerId, terminId);
        vorgaenge.trainerZugewiesen(terminId, null);
        if (bisher != null && !bisher.equals(trainerId)) benachrichtige(bisher, administratorId,
                Benachrichtigungsanlass.TRAINERZUWEISUNG_BEENDET,
                "Ihre Trainerzuweisung für " + terminId + " wurde beendet.", terminId);
        benachrichtige(trainerId, administratorId,
                istAssistent ? Benachrichtigungsanlass.ROLLENWECHSEL
                        : Benachrichtigungsanlass.TRAINERZUWEISUNG_GESETZT, istAssistent
                ? "Sie sind für " + terminId + " nun ausführender Trainer statt Assistent."
                : "Sie wurden " + terminId + " als Trainer zugewiesen.", terminId);
    }

    @Transactional
    public void trainerAbziehen(String administratorId, String terminId) {
        nachziehen();
        pruefeAdministrator(administratorId);
        TerminZeile termin = lade(terminId, true);
        verlangeGeplant(termin);
        jdbc.update("UPDATE termin SET trainer_id=NULL, version=version+1 WHERE termin_id=?", terminId);
        if (termin.trainerId() != null) benachrichtige(termin.trainerId(), administratorId,
                Benachrichtigungsanlass.TRAINERZUWEISUNG_BEENDET,
                "Ihre Trainerzuweisung für " + terminId + " wurde beendet.", terminId);
    }

    @Transactional
    public void assistentZuweisen(String administratorId, String terminId, String trainerId) {
        nachziehen();
        pruefeAdministrator(administratorId);
        TerminZeile termin = lade(terminId, true);
        verlangeGeplant(termin);
        pruefeAktivenTrainer(trainerId);
        if (trainerId.equals(termin.trainerId())) {
            throw fehler(HttpStatus.CONFLICT, "BEREITS_TRAINER",
                    "Der ausführende Trainer kann nicht zugleich Assistent sein.");
        }
        pruefeVerfuegbar(trainerId, termin.startdatum(), termin.enddatum(), terminId);
        List<Integer> belegt = jdbc.queryForList(
                "SELECT platz FROM termin_assistent WHERE termin_id=?", Integer.class, terminId);
        int platz = java.util.stream.IntStream.rangeClosed(1, 3).filter(i -> !belegt.contains(i))
                .findFirst().orElseThrow(() -> fehler(HttpStatus.CONFLICT, "KEIN_ASSISTENZPLATZ",
                        "Für diesen Termin sind bereits drei Assistenzplätze belegt."));
        try {
            jdbc.update("INSERT INTO termin_assistent (termin_id, benutzerkonto_id, platz) VALUES (?, ?, ?)",
                    terminId, trainerId, platz);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw fehler(HttpStatus.CONFLICT, "BEREITS_ZUGEWIESEN",
                    "Das Benutzerkonto ist diesem Termin bereits zugewiesen.");
        }
    }

    @Transactional
    public TerminAnsicht bestaetigen(String kontoId, String terminId) {
        nachziehen();
        TerminZeile termin = lade(terminId, true);
        verlangeGeplant(termin);
        Benutzerkonto konto = konten.laden(kontoId);
        boolean admin = konto.rollen().contains(Rolle.ADMINISTRATOR);
        if (termin.trainerId() == null) {
            throw fehler(HttpStatus.CONFLICT, "TRAINER_FEHLT",
                    "Für die manuelle Bestätigung muss ein Trainer zugewiesen sein.");
        }
        if (!existiert("SELECT COUNT(*) FROM trainer_qualifikation WHERE benutzerkonto_id=? AND schulung_id=?",
                termin.trainerId(), termin.schulungId())) {
            throw fehler(HttpStatus.CONFLICT, "QUALIFIKATION_ERFORDERLICH",
                    "Der zugewiesene Trainer ist für diese Schulung nicht qualifiziert.");
        }
        if (!admin && !kontoId.equals(termin.trainerId())) {
            throw fehler(HttpStatus.FORBIDDEN, "NICHT_AUSFUEHRENDER_TRAINER",
                    "Nur der ausführende Trainer oder ein Administrator darf bestätigen.");
        }
        if (LocalDate.now(clock).isBefore(termin.enddatum())) {
            throw fehler(HttpStatus.CONFLICT, "TERMIN_NICHT_BEENDET",
                    "Die Durchführung kann erst ab dem Enddatum bestätigt werden.");
        }
        if (existiert("SELECT COUNT(*) FROM teilnehmerbuchung WHERE termin_id=? AND teilnahmestatus='offen'", terminId)) {
            throw fehler(HttpStatus.CONFLICT, "TEILNAHME_OFFEN",
                    "Der Teilnahmestatus aller Buchungen muss geklärt sein.");
        }
        jdbc.update("""
                UPDATE termin SET status='abgeschlossen', abschlussart='manuell',
                    abgeschlossen_am=?, bestaetigt_von=?, online_zugang=NULL, version=version+1
                WHERE termin_id=?
                """, LocalDate.now(clock), kontoId, terminId);
        return details(kontoId, terminId);
    }

    @Transactional
    public TerminAnsicht absagen(String administratorId, String terminId, String grund) {
        nachziehen();
        pruefeAdministrator(administratorId);
        TerminZeile termin = lade(terminId, true);
        verlangeGeplant(termin);
        String sauber = leerZuNull(grund);
        jdbc.update("""
                UPDATE termin SET status='abgesagt', abgesagt_am=?, abgesagt_von=?,
                    absagegrund=?, online_zugang=NULL, version=version+1 WHERE termin_id=?
                """, LocalDate.now(clock), administratorId, sauber, terminId);
        benachrichtigeBeteiligte(terminId, administratorId, Benachrichtigungsanlass.TERMIN_ABGESAGT,
                "Der Termin " + terminId + " wurde abgesagt."
                        + (sauber == null ? "" : " Grund: " + sauber));
        vorgaenge.terminBeendet(terminId, termin.trainerId(), termin.startdatum(), termin.enddatum(),
                "Terminabsage" + (sauber == null ? "" : ": " + sauber));
        return details(administratorId, terminId);
    }

    @Transactional
    public void loeschen(String administratorId, String terminId) {
        nachziehen();
        pruefeAdministrator(administratorId);
        TerminZeile termin = lade(terminId, true);
        if ("abgeschlossen".equals(termin.status()) || !LocalDate.now(clock).isBefore(termin.startdatum())) {
            throw fehler(HttpStatus.CONFLICT, "TERMIN_NICHT_LOESCHBAR",
                    "Der Termin kann ab seinem Startdatum oder nach Abschluss nicht gelöscht werden.");
        }
        benachrichtigeBeteiligte(terminId, administratorId, Benachrichtigungsanlass.TERMIN_GELOESCHT,
                "Der Termin " + terminId + " wurde gelöscht.");
        jdbc.update("DELETE FROM termin WHERE termin_id=?", terminId);
        vorgaenge.terminBeendet(terminId, termin.trainerId(), termin.startdatum(), termin.enddatum(),
                "Terminlöschung");
    }

    @Transactional
    public Loeschwarnung warnung(String kontoId, String terminId) {
        nachziehen();
        konten.laden(kontoId);
        TerminZeile termin = lade(terminId, false);
        int buchungen = anzahl("SELECT COUNT(*) FROM teilnehmerbuchung WHERE termin_id=?", terminId);
        List<String> assistenten = beteiligteAssistenten(terminId);
        String trainerName = termin.trainerId() == null ? null : jdbc.queryForObject(
                "SELECT name FROM benutzerkonto WHERE id=?", String.class, termin.trainerId());
        return new Loeschwarnung(buchungen, trainerName, assistenten);
    }

    @Transactional
    public Teilnehmerbuchung buchungAnlegen(String kontoId, String terminId, BuchungEingabe eingabe) {
        nachziehen();
        TerminZeile termin = lade(terminId, true);
        pruefeTeilnehmerpflege(kontoId, termin);
        String firma = leerZuNull(eingabe.firma());
        if ("exklusiv".equals(termin.zugangsart())) {
            if (firma == null) firma = termin.kundenfirma();
            if (!gleichFirma(firma, termin.kundenfirma())) {
                throw fehler(HttpStatus.CONFLICT, "FALSCHE_FIRMA",
                        "Buchungen eines exklusiven Termins müssen zur Kundenfirma gehören.");
            }
            firma = termin.kundenfirma();
        }
        if (firma == null) throw fehler(HttpStatus.BAD_REQUEST, "FIRMA_FEHLT", "Die Firma fehlt.");
        String status = eingabe.teilnahmestatus() == null ? "offen" : eingabe.teilnahmestatus();
        if (!Set.of("offen", "teilgenommen", "nicht_teilgenommen").contains(status)) {
            throw fehler(HttpStatus.BAD_REQUEST, "STATUS_UNGUELTIG", "Der Teilnahmestatus ist ungültig.");
        }
        jdbc.update("""
                INSERT INTO teilnehmerbuchung (termin_id, name, firma, bemerkung, teilnahmestatus)
                VALUES (?, ?, ?, ?, ?)
                """, terminId, leerZuNull(eingabe.name()), firma, leerZuNull(eingabe.bemerkung()), status);
        Long id = jdbc.queryForObject("SELECT MAX(id) FROM teilnehmerbuchung WHERE termin_id=?", Long.class, terminId);
        return new Teilnehmerbuchung(id, leerZuNull(eingabe.name()), firma,
                leerZuNull(eingabe.bemerkung()), status);
    }

    @Transactional
    public void buchungStatus(String kontoId, String terminId, long buchungId, String status) {
        nachziehen();
        TerminZeile termin = lade(terminId, true);
        pruefeTeilnehmerpflege(kontoId, termin);
        if (!Set.of("teilgenommen", "nicht_teilgenommen").contains(status)) {
            throw fehler(HttpStatus.BAD_REQUEST, "STATUS_UNGUELTIG", "Der Teilnahmestatus ist ungültig.");
        }
        if (jdbc.update("UPDATE teilnehmerbuchung SET teilnahmestatus=? WHERE id=? AND termin_id=?",
                status, buchungId, terminId) == 0) {
            throw fehler(HttpStatus.NOT_FOUND, "BUCHUNG_NICHT_GEFUNDEN", "Die Buchung wurde nicht gefunden.");
        }
    }

    @Transactional
    public void buchungLoeschen(String kontoId, String terminId, long buchungId) {
        nachziehen();
        TerminZeile termin = lade(terminId, true);
        pruefeTeilnehmerpflege(kontoId, termin);
        if (jdbc.update("DELETE FROM teilnehmerbuchung WHERE id=? AND termin_id=?", buchungId, terminId) == 0) {
            throw fehler(HttpStatus.NOT_FOUND, "BUCHUNG_NICHT_GEFUNDEN", "Die Buchung wurde nicht gefunden.");
        }
    }

    @Transactional
    public List<TerminAnsicht> termine(String kontoId, YearMonth monat) {
        nachziehen();
        konten.laden(kontoId);
        LocalDate von = monat == null ? LocalDate.of(1900, 1, 1) : monat.atDay(1);
        LocalDate bis = monat == null ? LocalDate.of(2999, 12, 31) : monat.atEndOfMonth();
        return jdbc.query("""
                SELECT termin_id FROM termin WHERE startdatum<=? AND enddatum>=?
                ORDER BY startdatum, termin_id
                """, (rs, row) -> rs.getString(1), bis, von).stream()
                .map(id -> detailsOhneNachziehen(kontoId, id)).toList();
    }

    @Transactional
    public TerminAnsicht details(String kontoId, String terminId) {
        nachziehen();
        return detailsOhneNachziehen(kontoId, terminId);
    }

    private TerminAnsicht detailsOhneNachziehen(String kontoId, String terminId) {
        Benutzerkonto konto = konten.laden(kontoId);
        TerminZeile t = lade(terminId, false);
        boolean admin = konto.rollen().contains(Rolle.ADMINISTRATOR);
        boolean trainer = kontoId.equals(t.trainerId());
        boolean assistent = existiert("SELECT COUNT(*) FROM termin_assistent WHERE termin_id=? AND benutzerkonto_id=?",
                terminId, kontoId);
        List<Teilnehmerbuchung> buchungen = admin || trainer ? buchungen(terminId) : List.of();
        String online = admin || trainer || assistent ? t.onlineZugang() : null;
        Katalogschulung schulung = katalog.lade(SchulungId.von(t.schulungId())).orElse(null);
        int anzahl = buchungenAnzahl(terminId);
        List<String> warnungen = new ArrayList<>();
        if (schulung != null && "oeffentlich".equals(t.zugangsart()) && schulung.hatObergrenze()
                && anzahl > schulung.maxTeilnehmerOeffentlich()) warnungen.add("Höchstteilnehmerzahl überschritten");
        if (schulung != null && "exklusiv".equals(t.zugangsart()) && anzahl < schulung.mindestteilnehmerExklusiv())
            warnungen.add("Mindestteilnehmerzahl nicht erreicht");
        return new TerminAnsicht(t.terminId(), t.schulungId(),
                schulung == null ? t.schulungTitel() : schulung.titel(), t.startdatum(), t.enddatum(),
                schulungsTage(t.startdatum(), t.enddatum()), t.zugangsart(), t.durchfuehrungsart(), t.ort(),
                t.kundenfirma(), online, t.status(), t.trainerId(), name(t.trainerId()),
                assistenten(terminId), buchungen, anzahl, t.abschlussart(), t.abgeschlossenAm(),
                t.bestaetigtVon(), t.abgesagtAm(), t.abgesagtVon(), t.absagegrund(), warnungen);
    }

    @Transactional
    public List<DashboardEintrag> dashboard(String kontoId) {
        nachziehen();
        Benutzerkonto konto = konten.laden(kontoId);
        boolean admin = konto.rollen().contains(Rolle.ADMINISTRATOR);
        LocalDate heute = LocalDate.now(clock);
        List<String> ids = jdbc.query(admin ? """
                SELECT termin_id FROM termin WHERE status='geplant'
                ORDER BY CASE WHEN enddatum < ? THEN 0 ELSE 1 END, startdatum, termin_id
                """ : """
                SELECT termin_id FROM termin WHERE status='geplant' AND trainer_id=?
                ORDER BY CASE WHEN enddatum < ? THEN 0 ELSE 1 END, startdatum, termin_id
                """, (rs, row) -> rs.getString(1), admin ? new Object[]{heute} : new Object[]{kontoId, heute});
        return ids.stream().map(id -> {
            TerminZeile t = lade(id, false);
            Katalogschulung s = katalog.lade(SchulungId.von(t.schulungId())).orElse(null);
            boolean ueberfaellig = t.enddatum().isBefore(heute);
            boolean ohneTrainer = t.trainerId() == null;
            boolean zuWenig = s != null && "exklusiv".equals(t.zugangsart())
                    && buchungenAnzahl(id) < s.mindestteilnehmerExklusiv()
                    && t.startdatum().isBefore(heute.plusWeeks(4));
            boolean zuViele = s != null && "oeffentlich".equals(t.zugangsart())
                    && s.hatObergrenze() && buchungenAnzahl(id) > s.maxTeilnehmerOeffentlich();
            boolean dringend = (ohneTrainer && t.startdatum().isBefore(heute.plusWeeks(4))) || zuWenig;
            return new DashboardEintrag(id, s == null ? t.schulungTitel() : s.titel(), t.startdatum(), t.enddatum(), ueberfaellig,
                    ohneTrainer, dringend, zuWenig, zuViele);
        }).filter(e -> !admin || e.ueberfaellig() || e.ohneTrainer()
                || e.mindestteilnehmerUnterschritten() || e.hoechstteilnehmerUeberschritten()).toList();
    }

    @Transactional
    public List<TrainerOption> trainerOptionen(String kontoId, String terminId) {
        nachziehen();
        pruefeAdministrator(kontoId);
        TerminZeile termin = lade(terminId, false);
        return trainerOptionen(termin.schulungId(), termin.startdatum(), termin.enddatum(), terminId);
    }

    @Transactional
    public List<TrainerOption> trainerOptionen(String kontoId, String schulungId,
                                                LocalDate startdatum, LocalDate enddatum) {
        nachziehen();
        pruefeAdministrator(kontoId);
        schulung(schulungId, true);
        pruefeZeitraum(startdatum, enddatum, true);
        return trainerOptionen(schulungId, startdatum, enddatum, null);
    }

    private List<TrainerOption> trainerOptionen(String schulungId, LocalDate startdatum,
                                                 LocalDate enddatum, String ausnahmeTermin) {
        return jdbc.query("""
                SELECT k.id, k.name, k.email FROM benutzerkonto k
                JOIN benutzerkonto_rolle r ON r.benutzerkonto_id=k.id AND r.rolle='TRAINER'
                JOIN trainer_qualifikation q ON q.benutzerkonto_id=k.id
                WHERE k.aktiv=TRUE AND q.schulung_id=? ORDER BY LOWER(k.name), k.name
                """, (rs, row) -> {
            String id = rs.getString("id");
            String grund = nichtVerfuegbarGrund(id, startdatum, enddatum, ausnahmeTermin);
            return new TrainerOption(id, rs.getString("name"), rs.getString("email"), grund == null, grund,
                    belegungen(id, startdatum.minusWeeks(2), enddatum.plusWeeks(2)));
        }, schulungId);
    }

    @Transactional
    public Auswertung auswertung(String kontoId) {
        nachziehen();
        pruefeAdministrator(kontoId);
        Integer termine = jdbc.queryForObject("SELECT COUNT(*) FROM termin WHERE status='abgeschlossen' AND abschlussart='manuell'", Integer.class);
        Integer teilgenommen = jdbc.queryForObject("""
                SELECT COUNT(*) FROM teilnehmerbuchung b JOIN termin t ON t.termin_id=b.termin_id
                WHERE t.status='abgeschlossen' AND t.abschlussart='manuell' AND b.teilnahmestatus='teilgenommen'
                """, Integer.class);
        return new Auswertung(termine == null ? 0 : termine, teilgenommen == null ? 0 : teilgenommen);
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void beiStartNachziehen() {
        nachziehen();
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void taeglichNachziehen() {
        nachziehen();
    }

    @Transactional
    public void nachziehen() {
        LocalDate heute = LocalDate.now(clock);
        List<TerminZeile> faellig = jdbc.query("""
                SELECT * FROM termin WHERE status='geplant' AND DATEADD(MONTH, 2, enddatum) <= ?
                FOR UPDATE
                """, (rs, row) -> zeile(rs), heute);
        for (TerminZeile termin : faellig) {
            LocalDate stichtag = termin.enddatum().plusMonths(2);
            jdbc.update("""
                    UPDATE termin SET status='abgeschlossen', abschlussart='automatisch',
                        abgeschlossen_am=?, bestaetigt_von=NULL, online_zugang=NULL, version=version+1
                    WHERE termin_id=?
                    """, stichtag, termin.terminId());
            if (termin.trainerId() != null
                    && !konten.laden(termin.trainerId()).rollen().contains(Rolle.ADMINISTRATOR)) {
                benachrichtige(termin.trainerId(), null,
                        Benachrichtigungsanlass.TERMIN_AUTOMATISCH_ABGESCHLOSSEN,
                        "Der Termin " + termin.terminId()
                                + " wurde automatisch abgeschlossen und zählt nicht in Teilnehmerauswertungen.",
                        termin.terminId());
            }
        }
        jdbc.update("""
                UPDATE teilnehmerbuchung SET name=NULL, bemerkung=NULL WHERE termin_id IN (
                    SELECT termin_id FROM termin
                    WHERE (status='abgeschlossen' AND DATEADD(MONTH, 3, abgeschlossen_am) <= ?)
                       OR (status='abgesagt' AND DATEADD(MONTH, 3, abgesagt_am) <= ?)
                )
                """, heute, heute);
    }

    private String naechsteId(String schulungId) {
        List<Integer> werte = jdbc.query("SELECT naechste_nummer FROM termin_nummer WHERE schulung_id=? FOR UPDATE",
                (rs, row) -> rs.getInt(1), schulungId);
        int nummer;
        if (werte.isEmpty()) {
            Integer max = jdbc.queryForObject("""
                    SELECT COALESCE(MAX(CAST(SUBSTRING(termin_id, LENGTH(?) + 3) AS INT)), 0)
                    FROM termin WHERE schulung_id=? AND termin_id LIKE ?
                    """, Integer.class, schulungId, schulungId, schulungId + "-T____");
            nummer = (max == null ? 0 : max) + 1;
            jdbc.update("INSERT INTO termin_nummer (schulung_id, naechste_nummer) VALUES (?, ?)",
                    schulungId, nummer + 1);
        } else {
            nummer = werte.getFirst();
            if (nummer <= 9999) jdbc.update("UPDATE termin_nummer SET naechste_nummer=? WHERE schulung_id=?",
                    nummer + 1, schulungId);
        }
        if (nummer > 9999) throw fehler(HttpStatus.CONFLICT, "TERMIN_ID_ERSCHOEPFT",
                "Für diese Schulung sind alle Termin-Kennungen bis T9999 vergeben.");
        return "%s-T%04d".formatted(schulungId, nummer);
    }

    private NormalisierteFelder normalisiereFelder(TerminEingabe e, TerminZeile alt) {
        String zugang = normalisiereWert(e.zugangsart());
        String art = normalisiereWert(e.durchfuehrungsart());
        String ort = leerZuNull(e.ort());
        String firma = leerZuNull(e.kundenfirma());
        String online = leerZuNull(e.onlineZugang());
        if (zugang != null && !ZUGANGSARTEN.contains(zugang)) throw fehler(HttpStatus.BAD_REQUEST,
                "ZUGANGSART_UNGUELTIG", "Die Zugangsart ist ungültig.");
        if (art != null && !DURCHFUEHRUNGSARTEN.contains(art)) throw fehler(HttpStatus.BAD_REQUEST,
                "DURCHFUEHRUNGSART_UNGUELTIG", "Die Durchführungsart ist ungültig.");
        if ("exklusiv".equals(zugang)) {
            if (firma == null) throw fehler(HttpStatus.BAD_REQUEST, "KUNDENFIRMA_FEHLT", "Für exklusive Termine ist die Kundenfirma erforderlich.");
        } else if (firma != null) throw fehler(HttpStatus.BAD_REQUEST, "KUNDENFIRMA_NICHT_ERLAUBT", "Nur exklusive Termine tragen eine Kundenfirma.");
        if (Set.of("vor_ort", "beim_kunden", "hybrid").contains(Objects.toString(art, ""))) {
            if (ort == null) throw fehler(HttpStatus.BAD_REQUEST, "ORT_FEHLT", "Für diese Durchführungsart ist ein Ort erforderlich.");
        } else if (ort != null) throw fehler(HttpStatus.BAD_REQUEST, "ORT_NICHT_ERLAUBT", "Für Remote-Termine oder ohne Durchführungsart ist kein Ort erlaubt.");
        if (online != null) {
            if (!Set.of("remote", "hybrid").contains(Objects.toString(art, "")) || !gueltigeUrl(online))
                throw fehler(HttpStatus.BAD_REQUEST, "ONLINE_ZUGANG_UNGUELTIG", "Der Online-Zugang muss eine vollständige HTTP- oder HTTPS-URL für Remote oder Hybrid sein.");
        }
        if (alt != null && !e.entfernenBestaetigt()) {
            List<String> zuBestaetigen = new ArrayList<>();
            if (alt.ort() != null && ort == null) zuBestaetigen.add("Ort");
            if (alt.kundenfirma() != null && !Objects.equals(alt.kundenfirma(), firma)) zuBestaetigen.add("Kundenfirma");
            if (alt.onlineZugang() != null && online == null) zuBestaetigen.add("Online-Zugang");
            if (!zuBestaetigen.isEmpty()) throw bestaetigung(String.join(", ", zuBestaetigen));
        }
        return new NormalisierteFelder(zugang, art, ort, firma, online);
    }

    private void pruefeBuchungsfirmen(String terminId, TerminZeile alt,
                                      NormalisierteFelder neu, TerminEingabe eingabe) {
        boolean bestaetigteUmbenennung = "exklusiv".equals(alt.zugangsart())
                && alt.kundenfirma() != null && !gleichFirma(alt.kundenfirma(), neu.kundenfirma())
                && eingabe.entfernenBestaetigt();
        if ("exklusiv".equals(neu.zugangsart()) && !bestaetigteUmbenennung && existiert("""
                SELECT COUNT(*) FROM teilnehmerbuchung
                WHERE termin_id=? AND LOWER(TRIM(firma))<>LOWER(TRIM(?))
                """, terminId, neu.kundenfirma())) {
            throw fehler(HttpStatus.CONFLICT, "BUCHUNGEN_ANDERER_FIRMA",
                    "Vor dem Wechsel zu exklusiv müssen abweichende Buchungen entfernt werden.");
        }
        if (alt.kundenfirma() != null && neu.kundenfirma() != null
                && !gleichFirma(alt.kundenfirma(), neu.kundenfirma())
                && buchungenAnzahl(terminId) > 0 && !eingabe.entfernenBestaetigt()) throw bestaetigung("Kundenfirma");
    }

    private void pruefeAenderbarenZeitraum(TerminZeile alt, LocalDate start, LocalDate ende) {
        LocalDate heute = LocalDate.now(clock);
        boolean geaendert = !start.equals(alt.startdatum()) || !ende.equals(alt.enddatum());
        if (!geaendert) return;
        pruefeZeitraum(start, ende, false);
        if (heute.isBefore(alt.startdatum())) return;
        if (heute.isAfter(alt.enddatum()) || !start.equals(alt.startdatum()) || ende.isBefore(heute)) {
            throw fehler(HttpStatus.CONFLICT, "ZEITRAUM_UNVERAENDERLICH",
                    "Dieser Zeitraum kann nicht mehr geändert werden.");
        }
    }

    private void pruefeZeitraum(LocalDate start, LocalDate ende, boolean neu) {
        if (start == null || ende == null) throw fehler(HttpStatus.BAD_REQUEST, "ZEITRAUM_FEHLT", "Start- und Enddatum sind erforderlich.");
        if (ende.isBefore(start)) throw fehler(HttpStatus.BAD_REQUEST, "ZEITRAUM_UNGUELTIG", "Das Enddatum darf nicht vor dem Startdatum liegen.");
        if (neu && start.isBefore(LocalDate.now(clock))) throw fehler(HttpStatus.BAD_REQUEST, "STARTDATUM_VERGANGEN", "Das Startdatum darf nicht in der Vergangenheit liegen.");
        if (!istWerktag(start) || !istWerktag(ende)) throw fehler(HttpStatus.BAD_REQUEST, "WOCHENENDE", "Start und Ende müssen auf einen Wochentag fallen.");
    }

    private void pruefeZuweisungen(String terminId, LocalDate start, LocalDate ende) {
        TerminZeile termin = lade(terminId, false);
        if (termin.trainerId() != null) pruefeVerfuegbar(termin.trainerId(), start, ende, terminId);
        for (String id : beteiligteAssistentenIds(terminId)) pruefeVerfuegbar(id, start, ende, terminId);
    }

    private void pruefeTrainer(String trainerId, String schulungId, LocalDate start, LocalDate ende, String ausnahmeTermin) {
        pruefeAktivenTrainer(trainerId);
        if (!existiert("SELECT COUNT(*) FROM trainer_qualifikation WHERE benutzerkonto_id=? AND schulung_id=?",
                trainerId, schulungId)) throw fehler(HttpStatus.CONFLICT, "QUALIFIKATION_ERFORDERLICH",
                "Der Trainer ist für diese Schulung nicht qualifiziert.");
        pruefeVerfuegbar(trainerId, start, ende, ausnahmeTermin);
    }

    private void pruefeVerfuegbar(String kontoId, LocalDate start, LocalDate ende, String ausnahmeTermin) {
        jdbc.queryForObject("SELECT id FROM benutzerkonto WHERE id=? FOR UPDATE", String.class, kontoId);
        String grund = nichtVerfuegbarGrund(kontoId, start, ende, ausnahmeTermin);
        if (grund != null) throw fehler(HttpStatus.CONFLICT, "TRAINER_NICHT_VERFUEGBAR", grund);
    }

    private String nichtVerfuegbarGrund(String kontoId, LocalDate start, LocalDate ende, String ausnahmeTermin) {
        if (existiert("SELECT COUNT(*) FROM abwesenheit WHERE benutzerkonto_id=? AND von<=? AND bis>=?",
                kontoId, ende, start)) return "Der Trainer ist im Zeitraum abwesend.";
        if (existiert("""
                SELECT COUNT(*) FROM termin t WHERE t.status='geplant' AND t.termin_id<>?
                  AND t.startdatum<=? AND t.enddatum>=?
                  AND (t.trainer_id=? OR EXISTS (SELECT 1 FROM termin_assistent a
                       WHERE a.termin_id=t.termin_id AND a.benutzerkonto_id=?))
                """, Objects.toString(ausnahmeTermin, ""), ende, start, kontoId, kontoId))
            return "Der Trainer ist im Zeitraum bereits einem anderen Termin zugewiesen.";
        return null;
    }

    private void pruefeTeilnehmerpflege(String kontoId, TerminZeile termin) {
        if (!"geplant".equals(termin.status())) throw fehler(HttpStatus.CONFLICT, "BUCHUNGEN_UNVERAENDERLICH", "Buchungen abgeschlossener oder abgesagter Termine sind unveränderlich.");
        Benutzerkonto konto = konten.laden(kontoId);
        if (!konto.rollen().contains(Rolle.ADMINISTRATOR) && !kontoId.equals(termin.trainerId()))
            throw fehler(HttpStatus.FORBIDDEN, "ZUGRIFF_VERWEIGERT", "Teilnehmerbuchungen darf nur der Administrator oder der zugewiesene Trainer pflegen.");
    }

    private Katalogschulung schulung(String id, boolean mussAktivSein) {
        if (id == null) throw fehler(HttpStatus.BAD_REQUEST, "SCHULUNG_FEHLT", "Die Schulung fehlt.");
        SchulungId schulungId;
        try { schulungId = SchulungId.von(id); }
        catch (RuntimeException ex) { throw fehler(HttpStatus.BAD_REQUEST, "SCHULUNG_UNGUELTIG", "Die Schulungs-ID ist ungültig."); }
        Katalogschulung schulung = katalog.lade(schulungId).orElseThrow(() ->
                fehler(HttpStatus.NOT_FOUND, "SCHULUNG_NICHT_GEFUNDEN", "Die Schulung wurde nicht gefunden."));
        if (mussAktivSein && zustaende.lade(schulungId).map(z -> z.istArchiviert()).orElse(false))
            throw fehler(HttpStatus.CONFLICT, "SCHULUNG_ARCHIVIERT", "Zu einer archivierten Schulung kann kein Termin angelegt werden.");
        return schulung;
    }

    private TerminZeile lade(String id, boolean sperren) {
        List<TerminZeile> result = jdbc.query("SELECT * FROM termin WHERE termin_id=?" + (sperren ? " FOR UPDATE" : ""),
                (rs, row) -> zeile(rs), id);
        if (result.isEmpty()) throw fehler(HttpStatus.NOT_FOUND, "TERMIN_NICHT_GEFUNDEN", "Der Termin wurde nicht gefunden.");
        return result.getFirst();
    }

    private static TerminZeile zeile(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new TerminZeile(rs.getString("termin_id"), rs.getString("schulung_id"), rs.getString("schulung_titel"),
                rs.getDate("startdatum").toLocalDate(), rs.getDate("enddatum").toLocalDate(),
                rs.getString("zugangsart"), rs.getString("durchfuehrungsart"), rs.getString("ort"),
                rs.getString("kundenfirma"), rs.getString("online_zugang"), rs.getString("status"),
                rs.getString("trainer_id"), rs.getString("abschlussart"), datum(rs, "abgeschlossen_am"),
                rs.getString("bestaetigt_von"), datum(rs, "abgesagt_am"), rs.getString("abgesagt_von"),
                rs.getString("absagegrund"));
    }

    private static LocalDate datum(java.sql.ResultSet rs, String name) throws java.sql.SQLException {
        java.sql.Date wert = rs.getDate(name);
        return wert == null ? null : wert.toLocalDate();
    }

    private List<Teilnehmerbuchung> buchungen(String id) {
        return jdbc.query("""
                SELECT id, name, firma, bemerkung, teilnahmestatus FROM teilnehmerbuchung
                WHERE termin_id=? ORDER BY id
                """, (rs, row) -> new Teilnehmerbuchung(rs.getLong("id"), rs.getString("name"),
                rs.getString("firma"), rs.getString("bemerkung"), rs.getString("teilnahmestatus")), id);
    }

    private List<Beteiligter> assistenten(String id) {
        return jdbc.query("""
                SELECT a.benutzerkonto_id, COALESCE(k.name, a.name_snapshot) name, a.platz
                FROM termin_assistent a LEFT JOIN benutzerkonto k ON k.id=a.benutzerkonto_id
                WHERE a.termin_id=? ORDER BY a.platz
                """, (rs, row) -> new Beteiligter(rs.getString(1), rs.getString(2), rs.getInt(3)), id);
    }

    private List<String> beteiligteAssistenten(String id) {
        return assistenten(id).stream().map(Beteiligter::name).toList();
    }

    private List<String> beteiligteAssistentenIds(String id) {
        return assistenten(id).stream().map(Beteiligter::id).filter(Objects::nonNull).toList();
    }

    private List<Belegung> belegungen(String kontoId, LocalDate von, LocalDate bis) {
        List<Belegung> result = new ArrayList<>();
        result.addAll(jdbc.query("SELECT von, bis FROM abwesenheit WHERE benutzerkonto_id=? AND von<=? AND bis>=?",
                (rs, row) -> new Belegung("abwesend", rs.getDate(1).toLocalDate(), rs.getDate(2).toLocalDate()),
                kontoId, bis, von));
        result.addAll(jdbc.query("""
                SELECT startdatum, enddatum FROM termin t WHERE t.status='geplant'
                AND t.startdatum<=? AND t.enddatum>=? AND
                (t.trainer_id=? OR EXISTS (SELECT 1 FROM termin_assistent a WHERE a.termin_id=t.termin_id AND a.benutzerkonto_id=?))
                """, (rs, row) -> new Belegung("zugewiesen", rs.getDate(1).toLocalDate(), rs.getDate(2).toLocalDate()),
                bis, von, kontoId, kontoId));
        return result;
    }

    private void benachrichtigeBeteiligte(String terminId, String ausloeserId,
                                           Benachrichtigungsanlass anlasstyp, String text) {
        TerminZeile termin = lade(terminId, false);
        Set<String> ids = new LinkedHashSet<>(beteiligteAssistentenIds(terminId));
        if (termin.trainerId() != null) ids.add(termin.trainerId());
        ids.forEach(id -> benachrichtige(id, ausloeserId, anlasstyp, text, terminId));
    }

    private void benachrichtige(String kontoId, String ausloeserId, Benachrichtigungsanlass anlasstyp,
                                String text, String terminId) {
        benachrichtigungen.persoenlich(kontoId, ausloeserId, anlasstyp, text, "TERMIN", terminId);
    }

    private List<String> aenderungsnachrichten(TerminZeile alt, LocalDate start, LocalDate ende,
                                               NormalisierteFelder neu) {
        List<String> texte = new ArrayList<>();
        if (!alt.startdatum().equals(start) || !alt.enddatum().equals(ende)) texte.add("Zeitraum: " + alt.startdatum() + " bis " + alt.enddatum() + " → " + start + " bis " + ende);
        if (!Objects.equals(alt.ort(), neu.ort())) texte.add("Ort: " + alt.ort() + " → " + neu.ort());
        if (!Objects.equals(alt.durchfuehrungsart(), neu.durchfuehrungsart())) texte.add("Durchführungsart: " + alt.durchfuehrungsart() + " → " + neu.durchfuehrungsart());
        if (!Objects.equals(alt.kundenfirma(), neu.kundenfirma())) texte.add("Kundenfirma: " + alt.kundenfirma() + " → " + neu.kundenfirma());
        if (!Objects.equals(alt.onlineZugang(), neu.onlineZugang())) texte.add("Online-Zugang wurde geändert.");
        return texte;
    }

    private void pruefeAdministrator(String id) {
        if (!konten.laden(id).rollen().contains(Rolle.ADMINISTRATOR)) throw fehler(HttpStatus.FORBIDDEN,
                "ADMINISTRATOR_ERFORDERLICH", "Für diese Aktion sind Administratorrechte erforderlich.");
    }

    private void pruefeAktivenTrainer(String id) {
        Benutzerkonto konto = konten.laden(id);
        if (!konto.aktiv() || !konto.rollen().contains(Rolle.TRAINER)) throw fehler(HttpStatus.CONFLICT,
                "TRAINER_ERFORDERLICH", "Das Benutzerkonto ist kein aktiver Trainer.");
    }

    private void verlangeGeplant(TerminZeile t) {
        if (!"geplant".equals(t.status())) throw fehler(HttpStatus.CONFLICT, "TERMIN_UNVERAENDERLICH",
                "Abgeschlossene und abgesagte Termine sind unveränderlich.");
    }

    private boolean existiert(String sql, Object... parameter) { return anzahl(sql, parameter) > 0; }
    private int anzahl(String sql, Object... parameter) {
        Integer wert = jdbc.queryForObject(sql, Integer.class, parameter);
        return wert == null ? 0 : wert;
    }
    private int buchungenAnzahl(String id) { return anzahl("SELECT COUNT(*) FROM teilnehmerbuchung WHERE termin_id=?", id); }
    private String name(String id) { return id == null ? null : jdbc.queryForObject("SELECT name FROM benutzerkonto WHERE id=?", String.class, id); }
    private static boolean istWerktag(LocalDate tag) { return tag.getDayOfWeek() != DayOfWeek.SATURDAY && tag.getDayOfWeek() != DayOfWeek.SUNDAY; }
    private static boolean gleichFirma(String a, String b) { return a != null && b != null && a.trim().equalsIgnoreCase(b.trim()); }
    private static String leerZuNull(String wert) { if (wert == null) return null; String s = wert.trim(); return s.isEmpty() ? null : s; }
    private static String normalisiereWert(String wert) { String s = leerZuNull(wert); return s == null ? null : s.toLowerCase(Locale.ROOT); }
    private static String formatText(String art) { return art; }
    private static boolean gueltigeUrl(String wert) {
        try { URI uri = URI.create(wert); return uri.getScheme() != null && Set.of("http", "https").contains(uri.getScheme()) && uri.getHost() != null; }
        catch (IllegalArgumentException ex) { return false; }
    }
    private static int schulungsTage(LocalDate start, LocalDate ende) {
        int tage = 0; for (LocalDate tag = start; !tag.isAfter(ende); tag = tag.plusDays(1)) if (istWerktag(tag)) tage++; return tage;
    }
    private KontoFehler bestaetigung(String feld) { return fehler(HttpStatus.CONFLICT, "ENTFERNEN_BESTAETIGEN", "Das Entfernen von " + feld + " muss bestätigt werden."); }
    private KontoFehler fehler(HttpStatus status, String code, String text) { return new KontoFehler(status, code, text); }

    public record TerminEingabe(String schulungId, LocalDate startdatum, LocalDate enddatum,
            String zugangsart, String durchfuehrungsart, String ort, String kundenfirma,
            String onlineZugang, String trainerId, boolean entfernenBestaetigt) {}
    public record BuchungEingabe(String name, String firma, String bemerkung, String teilnahmestatus) {}
    public record Teilnehmerbuchung(Long id, String name, String firma, String bemerkung, String teilnahmestatus) {}
    public record Beteiligter(String id, String name, int platz) {}
    public record TerminAnsicht(String terminId, String schulungId, String schulungTitel,
            LocalDate startdatum, LocalDate enddatum, int schulungsTage, String zugangsart,
            String durchfuehrungsart, String ort, String kundenfirma, String onlineZugang,
            String status, String trainerId, String trainerName, List<Beteiligter> assistenten,
            List<Teilnehmerbuchung> teilnehmer, int anzahlBuchungen, String abschlussart,
            LocalDate abgeschlossenAm, String bestaetigtVon, LocalDate abgesagtAm,
            String abgesagtVon, String absagegrund, List<String> warnungen) {}
    public record Loeschwarnung(int anzahlBuchungen, String trainer, List<String> assistenten) {}
    public record DashboardEintrag(String terminId, String schulungTitel, LocalDate startdatum,
            LocalDate enddatum, boolean ueberfaellig, boolean ohneTrainer, boolean dringend,
            boolean mindestteilnehmerUnterschritten, boolean hoechstteilnehmerUeberschritten) {}
    public record TrainerOption(String id, String name, String email, boolean verfuegbar,
            String grund, List<Belegung> kalender) {}
    public record Belegung(String art, LocalDate von, LocalDate bis) {}
    public record Auswertung(int bestaetigteTermine, int teilgenommen) {}
    private record NormalisierteFelder(String zugangsart, String durchfuehrungsart, String ort,
            String kundenfirma, String onlineZugang) {}
    private record TerminZeile(String terminId, String schulungId, String schulungTitel, LocalDate startdatum,
            LocalDate enddatum, String zugangsart, String durchfuehrungsart, String ort,
            String kundenfirma, String onlineZugang, String status, String trainerId,
            String abschlussart, LocalDate abgeschlossenAm, String bestaetigtVon,
            LocalDate abgesagtAm, String abgesagtVon, String absagegrund) {}
}
