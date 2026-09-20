package de.nordwind.schulungsplaner.service;

import de.nordwind.schulungsplaner.domain.Benutzerkonto;
import de.nordwind.schulungsplaner.domain.Rolle;
import de.nordwind.schulungsplaner.katalog.SchulungId;
import de.nordwind.schulungsplaner.katalog.ablage.KatalogRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DashboardService {
    private final JdbcTemplate jdbc;
    private final KontoService konten;
    private final KatalogRepository katalog;
    private final TerminService termine;
    private final VorgangService vorgaenge;
    private final Clock clock;

    public DashboardService(JdbcTemplate jdbc, KontoService konten, KatalogRepository katalog,
                            TerminService termine, Clock clock, VorgangService vorgaenge) {
        this.jdbc = jdbc;
        this.konten = konten;
        this.katalog = katalog;
        this.termine = termine;
        this.clock = clock;
        this.vorgaenge = vorgaenge;
    }

    @Transactional
    public Dashboard anzeigen(String kontoId) {
        Benutzerkonto konto = konten.laden(kontoId);
        boolean admin = konto.rollen().contains(Rolle.ADMINISTRATOR);
        boolean trainer = konto.rollen().contains(Rolle.TRAINER);
        List<Vorgang> offenAnMich = new java.util.ArrayList<>(
                admin ? bewerbungen("b.status='OFFEN'", null, "AN_MICH") : List.of());
        offenAnMich.addAll(vorgaenge.fuer(kontoId, true, false));
        List<Vorgang> offenVonMir = new java.util.ArrayList<>(trainer
                ? bewerbungen("b.status='OFFEN' AND b.benutzerkonto_id=?", kontoId, "VON_MIR") : List.of());
        offenVonMir.addAll(vorgaenge.fuer(kontoId, true, true));
        List<Vorgang> erledigtAnMich = new java.util.ArrayList<>(admin
                ? bewerbungen("b.status<>'OFFEN'", null, "AN_MICH") : List.of());
        erledigtAnMich.addAll(vorgaenge.fuer(kontoId, false, false));
        List<Vorgang> erledigtVonMir = new java.util.ArrayList<>(trainer
                ? bewerbungen("b.status<>'OFFEN' AND b.benutzerkonto_id=?", kontoId, "VON_MIR") : List.of());
        erledigtVonMir.addAll(vorgaenge.fuer(kontoId, false, true));
        List<TerminService.DashboardEintrag> pflichten = trainer ? eigenePflichten(kontoId) : List.of();
        List<TerminService.DashboardEintrag> dringlichkeiten = admin ? termine.dashboard(kontoId) : List.of();
        return new Dashboard(offenAnMich, pflichten, dringlichkeiten, offenVonMir,
                erledigtAnMich, erledigtVonMir);
    }

    private List<Vorgang> bewerbungen(String bedingung, String kontoId, String richtung) {
        String sql = """
                SELECT b.id, b.schulung_id, b.status, b.erstellt_am, b.entschieden_am,
                       b.begruendung, k.name
                FROM qualifikationsbewerbung b
                LEFT JOIN benutzerkonto k ON k.id=b.benutzerkonto_id
                WHERE %s ORDER BY b.erstellt_am DESC, b.id DESC
                """.formatted(bedingung);
        Object[] parameter = kontoId == null ? new Object[]{} : new Object[]{kontoId};
        return jdbc.query(sql, (rs, row) -> {
            String schulungId = rs.getString("schulung_id");
            String titel = katalog.lade(SchulungId.von(schulungId))
                    .map(s -> s.titel()).orElse(schulungId);
            return new Vorgang(rs.getLong("id"), "QUALIFIKATION", "QUALIFIKATION",
                    "Freigabeanfrage für eine Qualifikation", rs.getString("name"), titel,
                    "SCHULUNG", schulungId,
                    rs.getTimestamp("erstellt_am").toLocalDateTime(), rs.getString("status"),
                    rs.getTimestamp("entschieden_am") == null ? null
                            : rs.getTimestamp("entschieden_am").toLocalDateTime(),
                    rs.getString("begruendung"), null, richtung,
                    "AN_MICH".equals(richtung), "VON_MIR".equals(richtung), true);
        }, parameter);
    }

    private List<TerminService.DashboardEintrag> eigenePflichten(String kontoId) {
        LocalDate heute = LocalDate.now(clock);
        return jdbc.query("""
                SELECT termin_id, schulung_id, schulung_titel, startdatum, enddatum
                FROM termin WHERE trainer_id=? AND status='geplant' AND enddatum<?
                ORDER BY enddatum, termin_id
                """, (rs, row) -> {
            String schulungId = rs.getString("schulung_id");
            String titel = katalog.lade(SchulungId.von(schulungId)).map(s -> s.titel())
                    .orElse(rs.getString("schulung_titel"));
            return new TerminService.DashboardEintrag(rs.getString("termin_id"), titel,
                    rs.getDate("startdatum").toLocalDate(), rs.getDate("enddatum").toLocalDate(),
                    true, false, true, false, false);
        }, kontoId, heute);
    }

    public record Dashboard(List<Vorgang> vorgaenge, List<TerminService.DashboardEintrag> pflichten,
                            List<TerminService.DashboardEintrag> dringlichkeiten,
                            List<Vorgang> eigeneVorgaenge, List<Vorgang> erledigteVorgaenge,
                            List<Vorgang> erledigteEigeneVorgaenge) {}

    public record Vorgang(long id, String quelle, String artCode, String art,
                          String antragsteller, String bezug, String bezugArt, String bezugId,
                          LocalDateTime erstelltAm, String status, LocalDateTime entschiedenAm,
                          String begruendung, String entschiedenVon, String richtung,
                          boolean entscheidbar, boolean zurueckziehbar,
                          boolean ablehnungsgrundPflicht) {}
}
