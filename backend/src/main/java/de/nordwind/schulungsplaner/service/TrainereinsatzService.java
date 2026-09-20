package de.nordwind.schulungsplaner.service;

import de.nordwind.schulungsplaner.domain.Benutzerkonto;
import de.nordwind.schulungsplaner.domain.Rolle;
import de.nordwind.schulungsplaner.katalog.SchulungId;
import de.nordwind.schulungsplaner.katalog.ablage.KatalogRepository;
import de.nordwind.schulungsplaner.katalog.zustand.SchulungszustandRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrainereinsatzService {
    private final JdbcTemplate jdbc;
    private final KontoService konten;
    private final KatalogRepository katalog;
    private final SchulungszustandRepository zustaende;
    private final Clock clock;
    private final TerminService termine;
    private final BenachrichtigungService benachrichtigungen;
    private final VorgangService vorgaenge;

    public TrainereinsatzService(JdbcTemplate jdbc, KontoService konten,
                                 KatalogRepository katalog,
                                 SchulungszustandRepository zustaende, Clock clock,
                                 TerminService termine, BenachrichtigungService benachrichtigungen,
                                 VorgangService vorgaenge) {
        this.jdbc = jdbc;
        this.konten = konten;
        this.katalog = katalog;
        this.zustaende = zustaende;
        this.clock = clock;
        this.termine = termine;
        this.benachrichtigungen = benachrichtigungen;
        this.vorgaenge = vorgaenge;
    }

    @Transactional
    public void aufQualifikationBewerben(String kontoId, String schulungId) {
        pruefeAktivenTrainer(kontoId);
        kontoSperren(kontoId);
        SchulungId id = schulungId(schulungId);
        if (!katalog.existiert(id)) {
            throw fehler(HttpStatus.NOT_FOUND, "SCHULUNG_NICHT_GEFUNDEN",
                    "Die Schulung wurde nicht gefunden.");
        }
        if (zustaende.lade(id).filter(eintrag -> eintrag.istArchiviert()).isPresent()) {
            throw fehler(HttpStatus.CONFLICT, "SCHULUNG_ARCHIVIERT",
                    "Auf eine archivierte Schulung ist keine Bewerbung möglich.");
        }
        if (existiert("""
                SELECT COUNT(*) FROM trainer_qualifikation
                WHERE benutzerkonto_id = ? AND schulung_id = ?
                """, kontoId, schulungId)) {
            throw fehler(HttpStatus.CONFLICT, "BEREITS_QUALIFIZIERT",
                    "Für diese Schulung besteht bereits eine Qualifikation.");
        }
        List<Bewerbung> bestehend = jdbc.query("""
                SELECT id, status, entschieden_am FROM qualifikationsbewerbung
                WHERE benutzerkonto_id = ? AND schulung_id = ? FOR UPDATE
                """, (rs, row) -> new Bewerbung(rs.getLong("id"), rs.getString("status"),
                        rs.getTimestamp("entschieden_am") == null ? null
                                : rs.getTimestamp("entschieden_am").toLocalDateTime()),
                kontoId, schulungId);
        if (bestehend.isEmpty()) {
            einfuegenOderKonflikt("""
                    INSERT INTO qualifikationsbewerbung (benutzerkonto_id, schulung_id, antragsteller_name)
                    VALUES (?, ?, ?)
                    """, "BEWERBUNG_BESTEHT", "Für diese Schulung besteht bereits eine Bewerbung.",
                    kontoId, schulungId, konten.laden(kontoId).name());
            return;
        }
        Bewerbung bewerbung = bestehend.getFirst();
        if ("ZURUECKGEZOGEN".equals(bewerbung.status())) {
            jdbc.update("""
                    UPDATE qualifikationsbewerbung
                    SET status='OFFEN', erstellt_am=?, entschieden_am=NULL, begruendung=NULL
                    WHERE id=?
                    """, LocalDateTime.now(clock), bewerbung.id());
            return;
        }
        if ("ABGELEHNT".equals(bewerbung.status()) && bewerbung.entschiedenAm() != null
                && !bewerbung.entschiedenAm().plusDays(1).isAfter(LocalDateTime.now(clock))) {
            jdbc.update("""
                    UPDATE qualifikationsbewerbung
                    SET status='OFFEN', erstellt_am=?, entschieden_am=NULL, begruendung=NULL
                    WHERE id=?
                    """, LocalDateTime.now(clock), bewerbung.id());
            return;
        }
        if ("ABGELEHNT".equals(bewerbung.status())) {
            throw fehler(HttpStatus.CONFLICT, "BEWERBUNG_GESPERRT",
                    "Nach einer Ablehnung ist eine erneute Bewerbung einen Tag lang gesperrt.");
        }
        throw fehler(HttpStatus.CONFLICT, "BEWERBUNG_BESTEHT",
                "Für diese Schulung besteht bereits eine Bewerbung.");
    }

    @Transactional
    public void bewerbungZurueckziehen(String kontoId, String schulungId) {
        pruefeAktivenTrainer(kontoId);
        if (jdbc.update("""
                UPDATE qualifikationsbewerbung
                SET status='ZURUECKGEZOGEN', entschieden_am=?
                WHERE benutzerkonto_id=? AND schulung_id=? AND status='OFFEN'
                """, LocalDateTime.now(clock), kontoId, schulungId) == 0) {
            throw fehler(HttpStatus.CONFLICT, "KEINE_OFFENE_BEWERBUNG",
                    "Es besteht keine offene Bewerbung für diese Schulung.");
        }
    }

    @Transactional(readOnly = true)
    public List<EigenerQualifikationsstand> meineQualifikationen(String kontoId) {
        return jdbc.query("""
                SELECT z.schulung_id,
                       CASE WHEN q.benutzerkonto_id IS NOT NULL THEN 'QUALIFIZIERT'
                            ELSE b.status END AS status,
                       CASE WHEN q.benutzerkonto_id IS NOT NULL THEN NULL
                            ELSE b.begruendung END AS begruendung,
                       (SELECT COUNT(*) FROM termin t WHERE t.trainer_id=?
                        AND t.schulung_id=z.schulung_id AND t.status='geplant'
                        AND t.startdatum>?) AS kuenftige_termine
                FROM schulung_zustand z
                LEFT JOIN trainer_qualifikation q ON q.schulung_id=z.schulung_id
                    AND q.benutzerkonto_id=?
                LEFT JOIN qualifikationsbewerbung b ON b.schulung_id=z.schulung_id
                    AND b.benutzerkonto_id=?
                WHERE q.benutzerkonto_id IS NOT NULL OR (b.id IS NOT NULL AND
                    (b.status='OFFEN' OR (b.status='ABGELEHNT' AND b.entschieden_am>?)))
                ORDER BY z.schulung_id
                """, (rs, row) -> new EigenerQualifikationsstand(
                        rs.getString("schulung_id"),
                        katalog.lade(SchulungId.von(rs.getString("schulung_id")))
                                .map(s -> s.titel()).orElse(rs.getString("schulung_id")),
                        rs.getString("status"), rs.getString("begruendung"),
                        rs.getInt("kuenftige_termine")),
                kontoId, LocalDate.now(clock), kontoId, kontoId, LocalDateTime.now(clock).minusDays(1));
    }

    @Transactional(readOnly = true)
    public List<Qualifikationszeile> qualifikationenDerSchulung(String administratorId,
                                                                String schulungId) {
        pruefeAdministrator(administratorId);
        pruefeSchulung(schulungId);
        return jdbc.query("""
                SELECT k.id, k.name, q.benutzerkonto_id,
                       b.id AS bewerbung_id, b.status
                FROM benutzerkonto k
                LEFT JOIN benutzerkonto_rolle r ON r.benutzerkonto_id=k.id AND r.rolle='TRAINER'
                LEFT JOIN trainer_qualifikation q ON q.benutzerkonto_id=k.id AND q.schulung_id=?
                LEFT JOIN qualifikationsbewerbung b ON b.benutzerkonto_id=k.id AND b.schulung_id=?
                WHERE b.status='OFFEN' OR q.benutzerkonto_id IS NOT NULL
                   OR (k.aktiv=TRUE AND r.benutzerkonto_id IS NOT NULL)
                ORDER BY LOWER(k.name), k.id
                """, (rs, row) -> new Qualifikationszeile(
                        rs.getString("id"), rs.getString("name"),
                        rs.getObject("bewerbung_id") == null ? null : rs.getLong("bewerbung_id"),
                        rs.getString("benutzerkonto_id") != null ? "QUALIFIZIERT"
                                : rs.getString("status") == null ? "KEINE" : rs.getString("status")),
                schulungId, schulungId);
    }

    @Transactional
    public void bewerbungGenehmigen(String administratorId, long bewerbungId) {
        pruefeAdministrator(administratorId);
        Entscheidungsdaten bewerbung = offeneBewerbung(bewerbungId);
        pruefeAktivenTrainer(bewerbung.kontoId());
        qualifikationEintragen(bewerbung.kontoId(), bewerbung.schulungId());
        jdbc.update("""
                UPDATE qualifikationsbewerbung
                SET status='GENEHMIGT', entschieden_am=?, begruendung=NULL WHERE id=?
                """, LocalDateTime.now(clock), bewerbungId);
        benachrichtigungen.persoenlich(bewerbung.kontoId(), administratorId,
                Benachrichtigungsanlass.QUALIFIKATION_GENEHMIGT,
                "Ihre Qualifikationsbewerbung für " + bewerbung.schulungId() + " wurde genehmigt.",
                "SCHULUNG", bewerbung.schulungId());
    }

    @Transactional
    public void bewerbungAblehnen(String administratorId, long bewerbungId, String begruendung) {
        pruefeAdministrator(administratorId);
        String grund = begruendung == null ? "" : begruendung.trim();
        if (grund.isEmpty()) {
            throw fehler(HttpStatus.BAD_REQUEST, "BEGRUENDUNG_ERFORDERLICH",
                    "Für die Ablehnung ist eine Begründung erforderlich.");
        }
        Entscheidungsdaten bewerbung = offeneBewerbung(bewerbungId);
        jdbc.update("""
                UPDATE qualifikationsbewerbung
                SET status='ABGELEHNT', entschieden_am=?, begruendung=? WHERE id=?
                """, LocalDateTime.now(clock), grund, bewerbungId);
        benachrichtigungen.persoenlich(bewerbung.kontoId(), administratorId,
                Benachrichtigungsanlass.QUALIFIKATION_ABGELEHNT, "Ihre Qualifikationsbewerbung für "
                + bewerbung.schulungId() + " wurde abgelehnt: " + grund,
                "SCHULUNG", bewerbung.schulungId());
    }

    @Transactional
    public void direktQualifizieren(String administratorId, String schulungId, String trainerId) {
        pruefeAdministrator(administratorId);
        pruefeAktivenTrainer(trainerId);
        kontoSperren(trainerId);
        pruefeSchulung(schulungId);
        if (existiert("SELECT COUNT(*) FROM trainer_qualifikation WHERE benutzerkonto_id=? AND schulung_id=?",
                trainerId, schulungId)) {
            throw fehler(HttpStatus.CONFLICT, "BEREITS_QUALIFIZIERT",
                    "Für diese Schulung besteht bereits eine Qualifikation.");
        }
        qualifikationEintragen(trainerId, schulungId);
        jdbc.update("""
                UPDATE qualifikationsbewerbung
                SET status='GENEHMIGT', entschieden_am=?, begruendung=NULL
                WHERE benutzerkonto_id=? AND schulung_id=?
                """, LocalDateTime.now(clock), trainerId, schulungId);
        benachrichtigungen.persoenlich(trainerId, administratorId,
                Benachrichtigungsanlass.QUALIFIKATION_DIREKT,
                "Sie wurden direkt für " + schulungId + " qualifiziert.",
                "SCHULUNG", schulungId);
    }

    @Transactional
    public void qualifikationEntziehen(String administratorId, String schulungId, String trainerId) {
        pruefeAdministrator(administratorId);
        qualifikationEntfernen(trainerId, schulungId);
        benachrichtigungen.persoenlich(trainerId, administratorId,
                Benachrichtigungsanlass.QUALIFIKATION_ENTZOGEN,
                "Ihre Qualifikation für " + schulungId + " wurde entzogen.",
                "SCHULUNG", schulungId);
    }

    @Transactional
    public void eigeneQualifikationAblegen(String kontoId, String schulungId) {
        pruefeAktivenTrainer(kontoId);
        qualifikationEntfernen(kontoId, schulungId);
        benachrichtigungen.adminbereich(Benachrichtigungsanlass.QUALIFIKATION_ABGELEGT,
                konten.laden(kontoId).name() + " hat die Qualifikation für " + schulungId + " abgelegt.",
                "SCHULUNG", schulungId);
    }

    @Transactional
    public void aufAssistenzplatzBewerben(String kontoId, String terminId) {
        termine.nachziehen();
        pruefeAktivenTrainer(kontoId);
        TerminDaten termin = terminLaden(terminId, false);
        if (termin.enddatum().isBefore(LocalDate.now(clock)) || "abgeschlossen".equals(termin.status())) {
            throw fehler(HttpStatus.CONFLICT, "TERMIN_ABGESCHLOSSEN",
                    "Auf einen abgeschlossenen Termin ist keine Bewerbung möglich.");
        }
        einfuegenOderKonflikt("""
                INSERT INTO assistenzbewerbung (termin_id, benutzerkonto_id)
                VALUES (?, ?)
                """, "BEWERBUNG_BESTEHT", "Für diesen Termin besteht bereits eine Bewerbung.",
                terminId, kontoId);
        String trainerId = jdbc.queryForObject("SELECT trainer_id FROM termin WHERE termin_id=?",
                String.class, terminId);
        vorgaenge.anlegen(Vorgangsart.ASSISTENZBEWERBUNG, kontoId, trainerId, true,
                "TERMIN", terminId, terminId, null, null);
    }

    @Transactional
    public void vormerken(String kontoId, String terminId) {
        pruefeAktivenTrainer(kontoId);
        TerminDaten termin = terminLaden(terminId, false);
        if (!"geplant".equals(termin.status()) || !termin.enddatum().isAfter(LocalDate.now(clock))) {
            throw fehler(HttpStatus.CONFLICT, "TERMIN_UNVERAENDERLICH", "Der Termin liegt nicht in der Zukunft.");
        }
        if (jdbc.queryForObject("SELECT trainer_id IS NULL FROM termin WHERE termin_id=?",
                Boolean.class, terminId) != Boolean.TRUE) {
            throw fehler(HttpStatus.CONFLICT, "TERMIN_BEREITS_BESETZT", "Der Termin hat bereits einen Trainer.");
        }
        if (!existiert("SELECT COUNT(*) FROM trainer_qualifikation WHERE benutzerkonto_id=? AND schulung_id=?",
                kontoId, termin.schulungId())) {
            throw fehler(HttpStatus.CONFLICT, "QUALIFIKATION_ERFORDERLICH", "Die Qualifikation fehlt.");
        }
        vorgaenge.anlegen(Vorgangsart.VORMERKUNG, kontoId, null, true,
                "TERMIN", terminId, terminId, null, null);
    }

    @Transactional
    public void uebernahmeAnfragen(String kontoId, String terminId) {
        pruefeAktivenTrainer(kontoId);
        TerminDaten termin = terminLaden(terminId, false);
        String bisher = jdbc.queryForObject("SELECT trainer_id FROM termin WHERE termin_id=?",
                String.class, terminId);
        if (bisher == null || bisher.equals(kontoId) || !"geplant".equals(termin.status())) {
            throw fehler(HttpStatus.CONFLICT, "UEBERNAHME_NICHT_MOEGLICH", "Der Termin kann nicht übernommen werden.");
        }
        if (!existiert("SELECT COUNT(*) FROM trainer_qualifikation WHERE benutzerkonto_id=? AND schulung_id=?",
                kontoId, termin.schulungId())) {
            throw fehler(HttpStatus.CONFLICT, "QUALIFIKATION_ERFORDERLICH", "Die Qualifikation fehlt.");
        }
        vorgaenge.anlegen(Vorgangsart.UEBERNAHMEANFRAGE, kontoId, bisher, false,
                "TERMIN", terminId, terminId, null, null);
    }

    @Transactional
    public void ersatztrainerAnfragen(String kontoId, String ersatztrainerId,
                                      LocalDate von, LocalDate bis) {
        pruefeAktivenTrainer(kontoId);
        pruefeAktivenTrainer(ersatztrainerId);
        if (von.isAfter(bis) || von.isBefore(LocalDate.now(clock).plusWeeks(2))) {
            throw fehler(HttpStatus.BAD_REQUEST, "UNGUELTIGER_ZEITRAUM",
                    "Eine Ersatztrainer-Anfrage braucht zwei Wochen Vorlauf.");
        }
        if (!existiert("""
                SELECT COUNT(*) FROM termin WHERE trainer_id=? AND status='geplant'
                AND startdatum<=? AND enddatum>=?
                """, kontoId, bis, von)) {
            throw fehler(HttpStatus.CONFLICT, "KEIN_ZUWEISUNGSKONFLIKT", "Es sind keine Termine betroffen.");
        }
        vorgaenge.anlegen(Vorgangsart.ERSATZTRAINER_ANFRAGE, kontoId, ersatztrainerId, false,
                "VORGANG", von + "/" + bis, von + " bis " + bis, von, bis);
    }

    @Transactional
    public void abwesenheitEintragen(String kontoId, LocalDate von, LocalDate bis, String grund) {
        pruefeAktivenTrainer(kontoId);
        if (von.isAfter(bis)) {
            throw fehler(HttpStatus.BAD_REQUEST, "UNGUELTIGER_ZEITRAUM",
                    "Das Anfangsdatum darf nicht nach dem Enddatum liegen.");
        }
        if (von.isBefore(LocalDate.now(clock).plusWeeks(1))) {
            throw fehler(HttpStatus.BAD_REQUEST, "MINDESTVORLAUF_UNTERSCHRITTEN",
                    "Eine Abwesenheit muss mindestens eine Woche vorher eingetragen werden.");
        }
        kontoSperren(kontoId);
        boolean konflikt = existiert("""
                SELECT COUNT(*) FROM termin t WHERE t.status='geplant'
                AND t.startdatum<=? AND t.enddatum>=? AND
                (t.trainer_id=? OR EXISTS (SELECT 1 FROM termin_assistent a
                    WHERE a.termin_id=t.termin_id AND a.benutzerkonto_id=?))
                """, bis, von, kontoId, kontoId);
        jdbc.update("""
                INSERT INTO abwesenheit (benutzerkonto_id, von, bis, grund, status)
                VALUES (?, ?, ?, ?, ?)
                """, kontoId, von, bis, grund, konflikt ? "OFFEN" : "AKTIV");
        if (konflikt) {
            Long id = jdbc.queryForObject("""
                    SELECT id FROM abwesenheit WHERE benutzerkonto_id=?
                    ORDER BY id DESC LIMIT 1
                    """, Long.class, kontoId);
            vorgaenge.anlegen(Vorgangsart.ABWESENHEITSANTRAG, kontoId, null, true,
                    "VORGANG", String.valueOf(id), von + " bis " + bis, von, bis);
        }
    }

    @Transactional
    public List<TrainerTermin> meineTermine(String kontoId) {
        termine.nachziehen();
        return jdbc.query("""
                SELECT termin_id, schulung_id, startdatum, enddatum, ort, status
                FROM termin WHERE trainer_id = ? OR (
                    trainer_id IS NULL AND status='geplant' AND startdatum>?
                    AND EXISTS (SELECT 1 FROM trainer_qualifikation q
                        WHERE q.benutzerkonto_id=? AND q.schulung_id=termin.schulung_id)
                ) ORDER BY startdatum, termin_id
                """, (rs, zeile) -> {
            SchulungId schulungId = SchulungId.von(rs.getString("schulung_id"));
            String titel = katalog.lade(schulungId).map(schulung -> schulung.titel())
                    .orElse(rs.getString("schulung_id"));
            String schulungszustand = zustaende.lade(schulungId)
                    .map(eintrag -> eintrag.zustand().name()).orElse("AKTIV");
            return new TrainerTermin(
                    rs.getString("termin_id"), schulungId.wert(), titel,
                    rs.getDate("startdatum").toLocalDate(),
                    rs.getDate("enddatum").toLocalDate(), rs.getString("ort"),
                    rs.getString("status"), schulungszustand);
        }, kontoId, LocalDate.now(clock), kontoId);
    }

    @Transactional
    public void trainerZuweisen(String administratorId, String terminId, String trainerId) {
        termine.trainerZuweisen(administratorId, terminId, trainerId, false);
    }

    @Transactional
    public void assistentZuweisen(String administratorId, String terminId, String trainerId) {
        termine.assistentZuweisen(administratorId, terminId, trainerId);
    }

    private void pruefeAktivenTrainer(String kontoId) {
        Benutzerkonto konto = konten.laden(kontoId);
        if (!konto.aktiv()) {
            throw fehler(HttpStatus.CONFLICT, "KONTO_STILLGELEGT",
                    "Das Benutzerkonto ist stillgelegt.");
        }
        if (!konto.rollen().contains(Rolle.TRAINER)) {
            throw fehler(HttpStatus.CONFLICT, "TRAINERROLLE_ERFORDERLICH",
                    "Das Benutzerkonto besitzt keine Trainerrolle.");
        }
    }

    private void pruefeAdministrator(String kontoId) {
        if (!konten.laden(kontoId).rollen().contains(Rolle.ADMINISTRATOR)) {
            throw fehler(HttpStatus.FORBIDDEN, "ADMINISTRATOR_ERFORDERLICH",
                    "Für diese Aktion sind Administratorrechte erforderlich.");
        }
    }

    private void pruefeSchulung(String schulungId) {
        if (!katalog.existiert(schulungId(schulungId))) {
            throw fehler(HttpStatus.NOT_FOUND, "SCHULUNG_NICHT_GEFUNDEN",
                    "Die Schulung wurde nicht gefunden.");
        }
    }

    private SchulungId schulungId(String wert) {
        try {
            return SchulungId.von(wert);
        } catch (IllegalArgumentException ex) {
            throw fehler(HttpStatus.NOT_FOUND, "SCHULUNG_NICHT_GEFUNDEN",
                    "Die Schulung wurde nicht gefunden.");
        }
    }

    private void kontoSperren(String kontoId) {
        jdbc.queryForObject("SELECT id FROM benutzerkonto WHERE id=? FOR UPDATE", String.class, kontoId);
    }

    private Entscheidungsdaten offeneBewerbung(long id) {
        List<Entscheidungsdaten> bewerbungen = jdbc.query("""
                SELECT benutzerkonto_id, schulung_id FROM qualifikationsbewerbung
                WHERE id=? AND status='OFFEN' FOR UPDATE
                """, (rs, row) -> new Entscheidungsdaten(
                        rs.getString("benutzerkonto_id"), rs.getString("schulung_id")), id);
        if (bewerbungen.isEmpty()) {
            throw fehler(HttpStatus.CONFLICT, "KEINE_OFFENE_BEWERBUNG",
                    "Die Freigabeanfrage ist nicht mehr offen.");
        }
        return bewerbungen.getFirst();
    }

    private void qualifikationEintragen(String kontoId, String schulungId) {
        try {
            jdbc.update("INSERT INTO trainer_qualifikation VALUES (?, ?)", kontoId, schulungId);
        } catch (DataIntegrityViolationException ex) {
            throw fehler(HttpStatus.CONFLICT, "BEREITS_QUALIFIZIERT",
                    "Für diese Schulung besteht bereits eine Qualifikation.");
        }
    }

    private void qualifikationEntfernen(String kontoId, String schulungId) {
        if (jdbc.update("DELETE FROM trainer_qualifikation WHERE benutzerkonto_id=? AND schulung_id=?",
                kontoId, schulungId) == 0) {
            throw fehler(HttpStatus.CONFLICT, "NICHT_QUALIFIZIERT",
                    "Für diese Schulung besteht keine Qualifikation.");
        }
        jdbc.update("""
                UPDATE termin SET trainer_id=NULL, trainer_name_snapshot=NULL
                WHERE trainer_id=? AND schulung_id=? AND status='geplant' AND startdatum>?
                """, kontoId, schulungId, LocalDate.now(clock));
        jdbc.update("""
                DELETE FROM qualifikationsbewerbung
                WHERE benutzerkonto_id=? AND schulung_id=? AND status='GENEHMIGT'
                """, kontoId, schulungId);
    }

    private TerminDaten terminLaden(String terminId, boolean sperren) {
        List<TerminDaten> termine = jdbc.query("""
                SELECT schulung_id, enddatum, status FROM termin WHERE termin_id = ?%s
                """.formatted(sperren ? " FOR UPDATE" : ""),
                (rs, row) -> new TerminDaten(rs.getString("schulung_id"),
                        rs.getDate("enddatum").toLocalDate(), rs.getString("status")), terminId);
        if (termine.isEmpty()) {
            throw fehler(HttpStatus.NOT_FOUND, "TERMIN_NICHT_GEFUNDEN",
                    "Der Termin wurde nicht gefunden.");
        }
        return termine.getFirst();
    }

    private boolean existiert(String sql, Object... parameter) {
        return jdbc.queryForObject(sql, Integer.class, parameter) > 0;
    }

    private void einfuegenOderKonflikt(String sql, String code, String meldung, Object... parameter) {
        try {
            jdbc.update(sql, parameter);
        } catch (DataIntegrityViolationException ex) {
            throw fehler(HttpStatus.CONFLICT, code, meldung);
        }
    }

    private KontoFehler fehler(HttpStatus status, String code, String meldung) {
        return new KontoFehler(status, code, meldung);
    }

    private record TerminDaten(String schulungId, LocalDate enddatum, String status) {}

    private record Bewerbung(long id, String status, LocalDateTime entschiedenAm) {}
    private record Entscheidungsdaten(String kontoId, String schulungId) {}

    public record EigenerQualifikationsstand(String schulungId, String schulungstitel,
                                              String status, String begruendung,
                                              int kuenftigeTermine) {}
    public record Qualifikationszeile(String trainerId, String trainerName,
                                      Long bewerbungId, String status) {}
    public record TrainerTermin(
            String terminId, String schulungId, String schulungstitel,
            LocalDate startdatum, LocalDate enddatum, String ort,
            String status, String schulungszustand) {}
}
