package de.nordwind.schulungsplaner.service;

import de.nordwind.schulungsplaner.domain.Benutzerkonto;
import de.nordwind.schulungsplaner.domain.Rolle;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
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
        String sicht = eigene ? "v.antragsteller_id=?" :
                "(v.zustaendig_id=? OR (? AND v.zustaendig_rolle='ADMINISTRATOR'))";
        String status = offen ? "v.status='OFFEN'" : "v.status<>'OFFEN'";
        Object[] parameter = eigene ? new Object[]{kontoId} : new Object[]{kontoId, admin};
        return jdbc.query("""
                SELECT v.*, e.name aktueller_entscheider FROM vorgang v
                LEFT JOIN benutzerkonto e ON e.id=v.entschieden_von_id
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
        String grund = begruendung == null ? "" : begruendung.trim();
        if (!angenommen && vorgang.art().ablehnungsgrundPflicht() && grund.isEmpty()) {
            throw fehler(HttpStatus.BAD_REQUEST, "BEGRUENDUNG_ERFORDERLICH",
                    "Für die Ablehnung ist eine Begründung erforderlich.");
        }
        Benutzerkonto entscheider = konten.laden(kontoId);
        String status = angenommen ? "ANGENOMMEN" : "ABGELEHNT";
        int geaendert = jdbc.update("""
                UPDATE vorgang SET status=?, entschieden_am=?, entschieden_von_id=?,
                    entschieden_von_name=?, begruendung=? WHERE id=? AND status='OFFEN'
                """, status, LocalDateTime.now(clock), kontoId, entscheider.name(),
                grund.isEmpty() ? null : grund, id);
        if (geaendert == 0) throw nichtGefunden();
        if (angenommen) wendeAn(vorgang);
        String mitteilungsBezugArt = vorgang.art() == Vorgangsart.ABWESENHEITSANTRAG
                ? "VORGANG" : vorgang.bezugArt();
        String mitteilungsBezugId = vorgang.art() == Vorgangsart.ABWESENHEITSANTRAG
                ? String.valueOf(id) : vorgang.bezugId();
        if (vorgang.antragstellerId() != null) benachrichtigungen.persoenlich(
                vorgang.antragstellerId(), kontoId, anlass(vorgang.art(), angenommen),
                vorgang.art().bezeichnung() + " zu " + vorgang.bezug() + " wurde "
                        + status.toLowerCase() + (grund.isEmpty() ? "." : ": " + grund),
                mitteilungsBezugArt, mitteilungsBezugId);
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

    private void wendeAn(Eintrag vorgang) {
        switch (vorgang.art()) {
            case VORMERKUNG, UEBERNAHMEANFRAGE -> jdbc.update(
                    "UPDATE termin SET trainer_id=?, version=version+1 WHERE termin_id=? AND status='geplant'",
                    vorgang.antragstellerId(), vorgang.bezugId());
            case ASSISTENZBEWERBUNG -> jdbc.update("""
                    INSERT INTO termin_assistent (termin_id, benutzerkonto_id, platz)
                    SELECT ?, ?, COALESCE(MAX(platz), 0) + 1 FROM termin_assistent WHERE termin_id=?
                    """, vorgang.bezugId(), vorgang.antragstellerId(), vorgang.bezugId());
            case ABWESENHEITSANTRAG -> {
                jdbc.update("UPDATE abwesenheit SET status='AKTIV' WHERE id=?", Long.valueOf(vorgang.bezugId()));
                jdbc.update("""
                        UPDATE termin SET trainer_id=NULL, version=version+1
                        WHERE trainer_id=? AND status='geplant' AND startdatum<=? AND enddatum>=?
                        """, vorgang.antragstellerId(), vorgang.bis(), vorgang.von());
            }
            case ERSATZTRAINER_ANFRAGE -> jdbc.update("""
                    UPDATE termin SET trainer_id=?, version=version+1
                    WHERE trainer_id=? AND status='geplant' AND startdatum<=? AND enddatum>=?
                    """, vorgang.zustaendigId(), vorgang.antragstellerId(), vorgang.bis(), vorgang.von());
        }
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
                SELECT art, antragsteller_id, zustaendig_id, zustaendig_rolle,
                       bezug_art, bezug_id, bezug, von, bis FROM vorgang
                WHERE id=? AND status='OFFEN' FOR UPDATE
                """, (rs, row) -> new Eintrag(Vorgangsart.valueOf(rs.getString("art")),
                rs.getString("antragsteller_id"), rs.getString("zustaendig_id"),
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
}
