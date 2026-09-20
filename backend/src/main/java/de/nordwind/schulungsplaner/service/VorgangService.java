package de.nordwind.schulungsplaner.service;

import de.nordwind.schulungsplaner.domain.Benutzerkonto;
import de.nordwind.schulungsplaner.domain.Rolle;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VorgangService {
    private final JdbcTemplate jdbc;
    private final KontoService konten;
    private final BenachrichtigungService benachrichtigungen;
    private final Clock clock;

    public VorgangService(JdbcTemplate jdbc, KontoService konten,
                          BenachrichtigungService benachrichtigungen, Clock clock) {
        this.jdbc = jdbc;
        this.konten = konten;
        this.benachrichtigungen = benachrichtigungen;
        this.clock = clock;
    }

    @Transactional
    public void anlegen(Vorgangsart art, String antragstellerId, String zustaendigId,
                        boolean adminZustaendig, String bezugArt, String bezugId,
                        String bezug, LocalDate von, LocalDate bis) {
        Benutzerkonto antragsteller = konten.laden(antragstellerId);
        jdbc.queryForObject("SELECT id FROM benutzerkonto WHERE id=? FOR UPDATE",
                String.class, antragstellerId);
        if (jdbc.queryForObject("""
                SELECT COUNT(*) FROM vorgang WHERE art=? AND antragsteller_id=?
                AND bezug_art=? AND bezug_id=? AND status='OFFEN'
                """, Integer.class, art.name(), antragstellerId, bezugArt, bezugId) > 0) {
            throw fehler(HttpStatus.CONFLICT, "VORGANG_BESTEHT",
                    "Ein entsprechender offener Vorgang besteht bereits.");
        }
        jdbc.update("""
                INSERT INTO vorgang (art, antragsteller_id, antragsteller_name,
                    zustaendig_id, zustaendig_rolle, bezug_art, bezug_id, bezug, von, bis)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, art.name(), antragstellerId, antragsteller.name(), zustaendigId,
                adminZustaendig ? "ADMINISTRATOR" : null, bezugArt, bezugId, bezug, von, bis);
    }

    @Transactional(readOnly = true)
    public List<DashboardService.Vorgang> fuer(String kontoId, boolean offen, boolean eigene) {
        Benutzerkonto konto = konten.laden(kontoId);
        boolean admin = konto.rollen().contains(Rolle.ADMINISTRATOR);
        String sicht = eigene ? "v.antragsteller_id=?" : """
                ((CASE WHEN v.art='ASSISTENZBEWERBUNG' AND v.status='OFFEN' THEN
                    CASE WHEN t.termin_id IS NULL THEN v.zustaendig_id ELSE t.trainer_id END
                    ELSE v.zustaendig_id END)=?
                OR (? AND v.zustaendig_rolle='ADMINISTRATOR'))
                """;
        String status = offen ? "v.status='OFFEN'" : "v.status<>'OFFEN'";
        Object[] parameter = eigene ? new Object[]{kontoId} : new Object[]{kontoId, admin};
        return jdbc.query("""
                SELECT v.*, e.name aktueller_entscheider FROM vorgang v
                LEFT JOIN benutzerkonto e ON e.id=v.entschieden_von_id
                LEFT JOIN termin t ON v.art='ASSISTENZBEWERBUNG' AND t.termin_id=v.bezug_id
                WHERE %s AND %s ORDER BY v.erstellt_am DESC, v.id DESC
                """.formatted(sicht, status), (rs, row) -> {
            Vorgangsart art = Vorgangsart.valueOf(rs.getString("art"));
            return new DashboardService.Vorgang(rs.getLong("id"), "VORGANG", art.name(),
                    art.bezeichnung(), rs.getString("antragsteller_name"), rs.getString("bezug"),
                    rs.getString("bezug_art"), rs.getString("bezug_id"),
                    rs.getTimestamp("erstellt_am").toLocalDateTime(), rs.getString("status"),
                    rs.getTimestamp("entschieden_am") == null ? null
                            : rs.getTimestamp("entschieden_am").toLocalDateTime(),
                    rs.getString("begruendung"), rs.getString("entschieden_von_name"),
                    eigene ? "VON_MIR" : "AN_MICH", !eigene && offen,
                    eigene && offen && art.zurueckziehbar(), art.ablehnungsgrundPflicht());
        }, parameter);
    }

    @Transactional
    public void entscheiden(String kontoId, long id, boolean angenommen, String begruendung) {
        Eintrag vorgang = laden(id);
        pruefeZustaendigkeit(kontoId, vorgang);
        String ungueltig = angenommen && vorgang.art() == Vorgangsart.ERSATZTRAINER_ANFRAGE
                ? ersatztrainerPruefgrund(vorgang) : null;
        if (ungueltig != null) {
            ersatztrainerUngueltig(id, vorgang, ungueltig);
            return;
        }
        String grund = begruendung == null ? "" : begruendung.trim();
        if (!angenommen && vorgang.art().ablehnungsgrundPflicht() && grund.isEmpty()) {
            throw fehler(HttpStatus.BAD_REQUEST, "BEGRUENDUNG_ERFORDERLICH",
                    "Für die Ablehnung ist eine Begründung erforderlich.");
        }
        Benutzerkonto entscheider = konten.laden(kontoId);
        String status = angenommen ? "ANGENOMMEN" : "ABGELEHNT";
        List<String> freiGewordeneTermine = angenommen
                && vorgang.art() == Vorgangsart.ABWESENHEITSANTRAG
                ? betroffeneTermine(vorgang) : List.of();
        int geaendert = jdbc.update("""
                UPDATE vorgang SET status=?, entschieden_am=?, entschieden_von_id=?,
                    entschieden_von_name=?, begruendung=?,
                    zustaendig_id=CASE WHEN art='ASSISTENZBEWERBUNG' THEN ? ELSE zustaendig_id END
                WHERE id=? AND status='OFFEN'
                """, status, LocalDateTime.now(clock), kontoId, entscheider.name(),
                grund.isEmpty() ? null : grund, vorgang.zustaendigId(), id);
        if (geaendert == 0) throw nichtGefunden();
        if (angenommen) wendeAn(id, vorgang);
        else if (vorgang.art() == Vorgangsart.ERSATZTRAINER_ANFRAGE) {
            regulärenAbwesenheitsantragAnlegen(vorgang, id);
        }
        String mitteilungsBezugArt = vorgang.art() == Vorgangsart.ABWESENHEITSANTRAG
                ? "VORGANG" : vorgang.bezugArt();
        String mitteilungsBezugId = vorgang.art() == Vorgangsart.ABWESENHEITSANTRAG
                ? String.valueOf(id) : vorgang.bezugId();
        String text = vorgang.art().bezeichnung() + " zu " + vorgang.bezug() + " wurde "
                + status.toLowerCase() + (grund.isEmpty() ? "." : ": " + grund);
        if (angenommen && vorgang.art() == Vorgangsart.ABWESENHEITSANTRAG) {
            text += " Frei gewordene Termine: "
                    + (freiGewordeneTermine.isEmpty() ? "keine" : String.join(", ", freiGewordeneTermine)) + ".";
        } else if (angenommen && vorgang.art() == Vorgangsart.ASSISTENZBEWERBUNG) {
            text += " Die Assistenzzuweisung wurde angelegt.";
        } else if (!angenommen && vorgang.art() == Vorgangsart.ERSATZTRAINER_ANFRAGE) {
            text += " Ein Abwesenheitsantrag wurde angelegt.";
        }
        if (vorgang.antragstellerId() != null) benachrichtigungen.persoenlich(
                vorgang.antragstellerId(), kontoId, anlass(vorgang.art(), angenommen),
                text,
                mitteilungsBezugArt, mitteilungsBezugId);
    }

    private String ersatztrainerPruefgrund(Eintrag vorgang) {
        if (jdbc.queryForObject("""
                SELECT COUNT(*) FROM termin t WHERE t.trainer_id=? AND t.status='geplant'
                AND t.startdatum<=? AND t.enddatum>=? AND NOT EXISTS (
                    SELECT 1 FROM trainer_qualifikation q WHERE q.benutzerkonto_id=?
                    AND q.schulung_id=t.schulung_id)
                """, Integer.class, vorgang.antragstellerId(), vorgang.bis(), vorgang.von(),
                vorgang.zustaendigId()) > 0) return "Qualifikation fehlt";
        if (jdbc.queryForObject("""
                SELECT COUNT(*) FROM abwesenheit WHERE benutzerkonto_id=? AND status='AKTIV'
                AND von<=? AND bis>=?
                """, Integer.class, vorgang.zustaendigId(), vorgang.bis(), vorgang.von()) > 0) {
            return "Ersatztrainer ist abwesend";
        }
        return null;
    }

    private void ersatztrainerUngueltig(long id, Eintrag vorgang, String grund) {
        LocalDateTime jetzt = LocalDateTime.now(clock);
        if (jdbc.update("""
                UPDATE vorgang SET status='UNGUELTIG', entschieden_am=?, begruendung=?
                WHERE id=? AND status='OFFEN'
                """, jetzt, grund, id) != 1) throw nichtGefunden();
        regulärenAbwesenheitsantragAnlegen(vorgang, id);
        String text = "Ersatztrainer-Anfrage wurde fachlich ungültig: " + grund
                + ". Ein Abwesenheitsantrag wurde angelegt.";
        benachrichtigungen.persoenlich(vorgang.antragstellerId(), null,
                Benachrichtigungsanlass.ERSATZTRAINER_ANFRAGE_BEENDET,
                text, "VORGANG", String.valueOf(id));
        benachrichtigungen.persoenlich(vorgang.zustaendigId(), null,
                Benachrichtigungsanlass.ERSATZTRAINER_ANFRAGE_BEENDET,
                text, "VORGANG", String.valueOf(id));
    }

    @Transactional
    public void trainerZugewiesen(String terminId, Long ausgenommenerVorgang) {
        entfallenBeiTermin(terminId, Vorgangsart.VORMERKUNG, ausgenommenerVorgang,
                "Zuweisung eines anderen Trainers",
                Benachrichtigungsanlass.VORMERKUNG_DURCH_ZUWEISUNG_ENTFALLEN);
    }

    @Transactional
    public void terminBeendet(String terminId, String trainerId, LocalDate start, LocalDate ende,
                              String ereignis) {
        for (Vorgangsart art : List.of(Vorgangsart.VORMERKUNG,
                Vorgangsart.ASSISTENZBEWERBUNG, Vorgangsart.UEBERNAHMEANFRAGE)) {
            for (Long id : jdbc.queryForList("""
                    SELECT id FROM vorgang WHERE art=? AND bezug_art='TERMIN'
                    AND bezug_id=? AND status='OFFEN'
                    """, Long.class, art.name(), terminId)) {
                entfallen(id, art, ereignis, Benachrichtigungsanlass.VORGANG_DURCH_TERMINENDE_ANGEPASST_ODER_ENTFALLEN,
                        null);
            }
        }
        if (trainerId == null) return;
        for (EintragMitId eintrag : offeneZeitraumvorgaenge(trainerId, start, ende)) {
            int verbleibend = jdbc.queryForObject("""
                    SELECT COUNT(*) FROM termin WHERE trainer_id=? AND status='geplant'
                    AND startdatum<=? AND enddatum>=?
                    """, Integer.class, trainerId, eintrag.eintrag().bis(), eintrag.eintrag().von());
            if (verbleibend == 0) {
                entfallen(eintrag.id(), eintrag.eintrag().art(), ereignis,
                        Benachrichtigungsanlass.VORGANG_DURCH_TERMINENDE_ANGEPASST_ODER_ENTFALLEN, null);
                abwesenheitAktivieren(eintrag.eintrag());
            } else {
                benachrichtigungen.persoenlich(trainerId, null,
                        Benachrichtigungsanlass.VORGANG_DURCH_TERMINENDE_ANGEPASST_ODER_ENTFALLEN,
                        eintrag.eintrag().art().bezeichnung() + " wurde wegen " + ereignis
                                + " um Termin " + terminId + " angepasst.",
                        "VORGANG", String.valueOf(eintrag.id()));
            }
        }
    }

    @EventListener
    @Transactional
    public void kontoBeendet(KontoBeendet ereignis) {
        List<EintragMitId> betroffen = jdbc.query("""
                SELECT id, art, antragsteller_id,
                       CASE WHEN art='ASSISTENZBEWERBUNG' AND EXISTS (
                           SELECT 1 FROM termin WHERE termin_id=vorgang.bezug_id) THEN
                           (SELECT trainer_id FROM termin WHERE termin_id=vorgang.bezug_id)
                           ELSE zustaendig_id END AS zustaendig_id,
                       zustaendig_rolle,
                       bezug_art, bezug_id, bezug, von, bis FROM vorgang
                WHERE status='OFFEN' AND (antragsteller_id=?
                    OR (art<>'ASSISTENZBEWERBUNG' AND zustaendig_id=?)
                    OR (art='ASSISTENZBEWERBUNG' AND EXISTS (
                        SELECT 1 FROM termin WHERE termin_id=vorgang.bezug_id AND trainer_id=?)))
                """, (rs, row) -> new EintragMitId(rs.getLong("id"), eintrag(rs)),
                ereignis.kontoId(), ereignis.kontoId(), ereignis.kontoId());
        for (EintragMitId zeile : betroffen) {
            Eintrag vorgang = zeile.eintrag();
            boolean adressatEndet = ereignis.kontoId().equals(vorgang.zustaendigId())
                    && !ereignis.kontoId().equals(vorgang.antragstellerId());
            String grund = ereignis.grund();
            if (adressatEndet && vorgang.art() == Vorgangsart.ERSATZTRAINER_ANFRAGE) {
                regulärenAbwesenheitsantragAnlegen(vorgang, zeile.id());
                grund += "; Abwesenheitsantrag angelegt";
            }
            entfallen(zeile.id(), vorgang.art(), grund,
                    Benachrichtigungsanlass.VORGANG_DURCH_KONTOENDE_ENTFALLEN,
                    ereignis.ausloeserId());
        }
        List<Long> freigaben = jdbc.queryForList("""
                SELECT id FROM qualifikationsbewerbung
                WHERE benutzerkonto_id=? AND status='OFFEN'
                """, Long.class, ereignis.kontoId());
        jdbc.update("""
                UPDATE qualifikationsbewerbung SET status='ENTFALLEN', entschieden_am=?,
                    begruendung=? WHERE benutzerkonto_id=? AND status='OFFEN'
                """, LocalDateTime.now(clock), ereignis.grund(), ereignis.kontoId());
        for (Long id : freigaben) {
            benachrichtigungen.persoenlich(ereignis.kontoId(), ereignis.ausloeserId(),
                    Benachrichtigungsanlass.VORGANG_DURCH_KONTOENDE_ENTFALLEN,
                    "Freigabeanfrage ist entfallen: " + ereignis.grund() + ".",
                    "VORGANG", String.valueOf(id));
        }
    }

    @Transactional
    public void nachziehen() {
        LocalDate heute = LocalDate.now(clock);
        for (EintragMitId zeile : jdbc.query("""
                SELECT id, art, antragsteller_id, zustaendig_id, zustaendig_rolle,
                       bezug_art, bezug_id, bezug, von, bis FROM vorgang
                WHERE status='OFFEN' AND art='ABWESENHEITSANTRAG' AND von<=?
                FOR UPDATE
                """, (rs, row) -> new EintragMitId(rs.getLong("id"), eintrag(rs)), heute.plusWeeks(1))) {
            Eintrag vorgang = zeile.eintrag();
            List<String> freiGewordeneTermine = betroffeneTermine(vorgang);
            abschliessenOhneEntscheider(zeile.id(), "ANGENOMMEN", "Fristablauf: automatisch genehmigt");
            wendeAn(zeile.id(), vorgang);
            String text = "Abwesenheitsantrag für " + vorgang.bezug()
                    + " wurde nach Fristablauf genehmigt. Frei gewordene Termine: "
                    + (freiGewordeneTermine.isEmpty() ? "keine"
                    : String.join(", ", freiGewordeneTermine)) + ".";
            benachrichtigungen.persoenlich(vorgang.antragstellerId(), null,
                    Benachrichtigungsanlass.ABWESENHEITSANTRAG_NACH_FRIST_GENEHMIGT,
                    text, "VORGANG", String.valueOf(zeile.id()));
            benachrichtigungen.adminbereich(
                    Benachrichtigungsanlass.ABWESENHEITSANTRAG_NACH_FRIST_GENEHMIGT,
                    text, "VORGANG", String.valueOf(zeile.id()));
        }
        LocalDateTime grenze = LocalDateTime.now(clock).minusWeeks(1);
        for (EintragMitId zeile : jdbc.query("""
                SELECT id, art, antragsteller_id, zustaendig_id, zustaendig_rolle,
                       bezug_art, bezug_id, bezug, von, bis FROM vorgang
                WHERE status='OFFEN' AND art='ERSATZTRAINER_ANFRAGE' AND erstellt_am<=?
                FOR UPDATE
                """, (rs, row) -> new EintragMitId(rs.getLong("id"), eintrag(rs)), grenze)) {
            entfallen(zeile.id(), zeile.eintrag().art(),
                    "Fristablauf; Abwesenheitsantrag angelegt",
                    Benachrichtigungsanlass.ERSATZTRAINER_ANFRAGE_BEENDET, null);
            regulärenAbwesenheitsantragAnlegen(zeile.eintrag(), zeile.id());
        }
    }

    @Transactional
    public void zurueckziehen(String kontoId, long id) {
        Eintrag vorgang = laden(id);
        if (!kontoId.equals(vorgang.antragstellerId()) || !vorgang.art().zurueckziehbar()) {
            throw nichtGefunden();
        }
        if (jdbc.update("""
                UPDATE vorgang SET status='ZURUECKGEZOGEN', entschieden_am=?
                WHERE id=? AND status='OFFEN'
                """, LocalDateTime.now(clock), id) == 0) throw nichtGefunden();
    }

    private void pruefeZustaendigkeit(String kontoId, Eintrag vorgang) {
        Benutzerkonto konto = konten.laden(kontoId);
        boolean zustaendig = kontoId.equals(vorgang.zustaendigId())
                || (vorgang.adminZustaendig() && konto.rollen().contains(Rolle.ADMINISTRATOR));
        if (!zustaendig) throw nichtGefunden();
    }

    private void wendeAn(long id, Eintrag vorgang) {
        switch (vorgang.art()) {
            case VORMERKUNG -> {
                jdbc.update("UPDATE termin SET trainer_id=?, version=version+1 WHERE termin_id=? AND status='geplant'",
                        vorgang.antragstellerId(), vorgang.bezugId());
                trainerZugewiesen(vorgang.bezugId(), id);
            }
            case UEBERNAHMEANFRAGE -> {
                String bisher = jdbc.queryForObject("SELECT trainer_id FROM termin WHERE termin_id=?",
                        String.class, vorgang.bezugId());
                jdbc.update("UPDATE termin SET trainer_id=?, version=version+1 WHERE termin_id=? AND status='geplant'",
                        vorgang.antragstellerId(), vorgang.bezugId());
                entfallenBeiTermin(vorgang.bezugId(), Vorgangsart.UEBERNAHMEANFRAGE, id,
                        "Tausch durch Übernahme",
                        Benachrichtigungsanlass.UEBERNAHMEANFRAGE_DURCH_TAUSCH_ENTFALLEN);
                trainerZugewiesen(vorgang.bezugId(), null);
                benachrichtigungen.adminbereich(Benachrichtigungsanlass.TRAINERWECHSEL_DURCH_UEBERNAHME,
                        "Trainerwechsel bei " + vorgang.bezugId() + " von " + name(bisher)
                                + " zu " + name(vorgang.antragstellerId()) + ".",
                        "TERMIN", vorgang.bezugId());
            }
            case ASSISTENZBEWERBUNG -> jdbc.update("""
                    INSERT INTO termin_assistent (termin_id, benutzerkonto_id, platz)
                    SELECT ?, ?, COALESCE(MAX(platz), 0) + 1 FROM termin_assistent WHERE termin_id=?
                    """, vorgang.bezugId(), vorgang.antragstellerId(), vorgang.bezugId());
            case ABWESENHEITSANTRAG -> {
                List<String> termine = jdbc.queryForList("""
                        SELECT termin_id FROM termin WHERE trainer_id=? AND status='geplant'
                        AND startdatum<=? AND enddatum>=? ORDER BY termin_id
                        """, String.class, vorgang.antragstellerId(), vorgang.bis(), vorgang.von());
                jdbc.update("UPDATE abwesenheit SET status='AKTIV' WHERE id=?", Long.valueOf(vorgang.bezugId()));
                jdbc.update("""
                        UPDATE termin SET trainer_id=NULL, version=version+1
                        WHERE trainer_id=? AND status='geplant' AND startdatum<=? AND enddatum>=?
                        """, vorgang.antragstellerId(), vorgang.bis(), vorgang.von());
                if (!termine.isEmpty()) benachrichtigungen.adminbereich(
                        Benachrichtigungsanlass.VERFUEGBARKEITSKONFLIKT_DURCH_ABWESENHEIT,
                        name(vorgang.antragstellerId()) + " ist von " + vorgang.von() + " bis "
                                + vorgang.bis() + " abwesend. Frei gewordene Termine: "
                                + String.join(", ", termine) + ".",
                        "VORGANG", String.valueOf(id));
            }
            case ERSATZTRAINER_ANFRAGE -> {
                jdbc.update("""
                        UPDATE termin SET trainer_id=?, version=version+1
                        WHERE trainer_id=? AND status='geplant' AND startdatum<=? AND enddatum>=?
                        """, vorgang.zustaendigId(), vorgang.antragstellerId(), vorgang.bis(), vorgang.von());
                abwesenheitAktivieren(vorgang);
            }
        }
    }

    private void entfallenBeiTermin(String terminId, Vorgangsart art, Long ausgenommen,
                                    String grund, Benachrichtigungsanlass anlass) {
        for (Long id : jdbc.queryForList("""
                SELECT id FROM vorgang WHERE art=? AND bezug_art='TERMIN' AND bezug_id=?
                AND status='OFFEN' AND (? IS NULL OR id<>?)
                """, Long.class, art.name(), terminId, ausgenommen, ausgenommen)) {
            entfallen(id, art, grund, anlass, null);
        }
    }

    private void entfallen(long id, Vorgangsart art, String grund,
                           Benachrichtigungsanlass anlass, String ausloeserId) {
        Eintrag vorgang = laden(id);
        if (art == Vorgangsart.ASSISTENZBEWERBUNG) {
            jdbc.update("UPDATE vorgang SET zustaendig_id=? WHERE id=?", vorgang.zustaendigId(), id);
        }
        abschliessenOhneEntscheider(id, "ENTFALLEN", grund);
        if (vorgang.antragstellerId() != null) benachrichtigungen.persoenlich(
                vorgang.antragstellerId(), ausloeserId, anlass,
                art.bezeichnung() + " zu " + vorgang.bezug() + " ist entfallen: " + grund + ".",
                "VORGANG", String.valueOf(id));
    }

    private void abschliessenOhneEntscheider(long id, String status, String grund) {
        if (jdbc.update("""
                UPDATE vorgang SET status=?, entschieden_am=?, begruendung=?,
                    entschieden_von_id=NULL, entschieden_von_name=NULL
                WHERE id=? AND status='OFFEN'
                """, status, LocalDateTime.now(clock), grund, id) != 1) throw nichtGefunden();
    }

    private void regulärenAbwesenheitsantragAnlegen(Eintrag vorgang, long ursprungId) {
        anlegen(Vorgangsart.ABWESENHEITSANTRAG, vorgang.antragstellerId(), null, true,
                "VORGANG", String.valueOf(ursprungId), vorgang.bezug(), vorgang.von(), vorgang.bis());
    }

    private void abwesenheitAktivieren(Eintrag vorgang) {
        if (vorgang.art() == Vorgangsart.ABWESENHEITSANTRAG) {
            try {
                jdbc.update("UPDATE abwesenheit SET status='AKTIV' WHERE id=?",
                        Long.valueOf(vorgang.bezugId()));
                return;
            } catch (NumberFormatException ignored) {
                // Test- und Altvorgänge können einen nichtnumerischen Bezug besitzen.
            }
        }
        jdbc.update("""
                INSERT INTO abwesenheit (benutzerkonto_id, von, bis, status)
                VALUES (?, ?, ?, 'AKTIV')
                """, vorgang.antragstellerId(), vorgang.von(), vorgang.bis());
    }

    private List<String> betroffeneTermine(Eintrag vorgang) {
        return jdbc.queryForList("""
                SELECT termin_id FROM termin WHERE trainer_id=? AND status='geplant'
                AND startdatum<=? AND enddatum>=? ORDER BY termin_id
                """, String.class, vorgang.antragstellerId(), vorgang.bis(), vorgang.von());
    }

    private List<EintragMitId> offeneZeitraumvorgaenge(String trainerId, LocalDate start, LocalDate ende) {
        return jdbc.query("""
                SELECT id, art, antragsteller_id, zustaendig_id, zustaendig_rolle,
                       bezug_art, bezug_id, bezug, von, bis FROM vorgang
                WHERE status='OFFEN' AND art IN ('ABWESENHEITSANTRAG','ERSATZTRAINER_ANFRAGE')
                AND antragsteller_id=? AND von<=? AND bis>=?
                """, (rs, row) -> new EintragMitId(rs.getLong("id"), eintrag(rs)), trainerId, ende, start);
    }

    private Eintrag eintrag(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new Eintrag(Vorgangsart.valueOf(rs.getString("art")),
                rs.getString("antragsteller_id"), rs.getString("zustaendig_id"),
                "ADMINISTRATOR".equals(rs.getString("zustaendig_rolle")),
                rs.getString("bezug_art"), rs.getString("bezug_id"), rs.getString("bezug"),
                rs.getDate("von") == null ? null : rs.getDate("von").toLocalDate(),
                rs.getDate("bis") == null ? null : rs.getDate("bis").toLocalDate());
    }

    private String name(String kontoId) {
        return kontoId == null ? "nicht zugewiesen"
                : jdbc.queryForObject("SELECT name FROM benutzerkonto WHERE id=?", String.class, kontoId);
    }

    private Benachrichtigungsanlass anlass(Vorgangsart art, boolean angenommen) {
        return switch (art) {
            case ABWESENHEITSANTRAG -> angenommen
                    ? Benachrichtigungsanlass.ABWESENHEITSANTRAG_MANUELL_GENEHMIGT
                    : Benachrichtigungsanlass.ABWESENHEITSANTRAG_ABGELEHNT;
            case VORMERKUNG -> angenommen ? Benachrichtigungsanlass.VORMERKUNG_BESTAETIGT
                    : Benachrichtigungsanlass.VORMERKUNG_ABGELEHNT;
            case ASSISTENZBEWERBUNG -> Benachrichtigungsanlass.ASSISTENZBEWERBUNG_ENTSCHIEDEN;
            case UEBERNAHMEANFRAGE -> Benachrichtigungsanlass.UEBERNAHMEANFRAGE_ENTSCHIEDEN;
            case ERSATZTRAINER_ANFRAGE -> Benachrichtigungsanlass.ERSATZTRAINER_ANFRAGE_BEENDET;
        };
    }

    private Eintrag laden(long id) {
        List<Eintrag> treffer = jdbc.query("""
                SELECT art, antragsteller_id,
                       CASE WHEN art='ASSISTENZBEWERBUNG' AND EXISTS (
                           SELECT 1 FROM termin WHERE termin_id=vorgang.bezug_id) THEN
                           (SELECT trainer_id FROM termin WHERE termin_id=vorgang.bezug_id)
                           ELSE zustaendig_id END AS aktueller_zustaendig,
                       zustaendig_rolle,
                       bezug_art, bezug_id, bezug, von, bis FROM vorgang
                WHERE id=? AND status='OFFEN' FOR UPDATE
                """, (rs, row) -> new Eintrag(Vorgangsart.valueOf(rs.getString("art")),
                rs.getString("antragsteller_id"), rs.getString("aktueller_zustaendig"),
                "ADMINISTRATOR".equals(rs.getString("zustaendig_rolle")),
                rs.getString("bezug_art"), rs.getString("bezug_id"), rs.getString("bezug"),
                rs.getDate("von") == null ? null : rs.getDate("von").toLocalDate(),
                rs.getDate("bis") == null ? null : rs.getDate("bis").toLocalDate()), id);
        if (treffer.isEmpty()) throw nichtGefunden();
        return treffer.getFirst();
    }

    private KontoFehler nichtGefunden() {
        return fehler(HttpStatus.NOT_FOUND, "VORGANG_NICHT_GEFUNDEN", "Der Vorgang wurde nicht gefunden.");
    }

    private KontoFehler fehler(HttpStatus status, String code, String text) {
        return new KontoFehler(status, code, text);
    }

    private record Eintrag(Vorgangsart art, String antragstellerId, String zustaendigId,
                           boolean adminZustaendig, String bezugArt, String bezugId,
                           String bezug, LocalDate von, LocalDate bis) {}

    private record EintragMitId(long id, Eintrag eintrag) {}

    public record KontoBeendet(String kontoId, String ausloeserId, String grund) {}
}
