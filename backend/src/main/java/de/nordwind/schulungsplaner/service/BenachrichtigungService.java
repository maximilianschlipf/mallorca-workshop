package de.nordwind.schulungsplaner.service;

import de.nordwind.schulungsplaner.domain.Benutzerkonto;
import de.nordwind.schulungsplaner.domain.Rolle;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BenachrichtigungService {
    private final JdbcTemplate jdbc;
    private final KontoService konten;
    private final Clock clock;

    public BenachrichtigungService(JdbcTemplate jdbc, KontoService konten, Clock clock) {
        this.jdbc = jdbc;
        this.konten = konten;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Benachrichtigung> anzeigen(String kontoId) {
        Benutzerkonto konto = konten.laden(kontoId);
        boolean admin = konto.rollen().contains(Rolle.ADMINISTRATOR);
        return jdbc.query("""
                SELECT b.id, b.anlasstyp, b.anlass, b.bezug_art, b.bezug_id,
                       b.erstellt_am, b.gelesen,
                       CASE
                           WHEN b.bezug_art IS NULL OR b.bezug_id IS NULL THEN TRUE
                           WHEN b.bezug_art='TERMIN' THEN EXISTS (
                               SELECT 1 FROM termin t WHERE t.termin_id=b.bezug_id)
                           WHEN b.bezug_art='SCHULUNG' THEN EXISTS (
                               SELECT 1 FROM schulung_zustand s WHERE s.schulung_id=b.bezug_id)
                           WHEN b.bezug_art='VORGANG' THEN EXISTS (
                               SELECT 1 FROM vorgang v WHERE CAST(v.id AS VARCHAR)=b.bezug_id)
                           ELSE FALSE
                       END AS bezug_vorhanden
                FROM benachrichtigung b
                WHERE b.empfaenger_id=? OR (? AND b.empfaenger_rolle='ADMINISTRATOR')
                ORDER BY b.erstellt_am DESC, b.id DESC
                """, (rs, row) -> new Benachrichtigung(
                rs.getLong("id"), rs.getString("anlasstyp"), rs.getString("anlass"),
                rs.getString("bezug_art"), rs.getString("bezug_id"),
                rs.getTimestamp("erstellt_am").toLocalDateTime(), rs.getBoolean("gelesen"),
                rs.getBoolean("bezug_vorhanden")), kontoId, admin);
    }

    @Transactional(readOnly = true)
    public int ungelesen(String kontoId) {
        boolean admin = konten.laden(kontoId).rollen().contains(Rolle.ADMINISTRATOR);
        Integer wert = jdbc.queryForObject("""
                SELECT COUNT(*) FROM benachrichtigung
                WHERE gelesen=FALSE AND (empfaenger_id=? OR (? AND empfaenger_rolle='ADMINISTRATOR'))
                """, Integer.class, kontoId, admin);
        return wert == null ? 0 : wert;
    }

    @Transactional
    public void allesAlsGelesen(String kontoId) {
        boolean admin = konten.laden(kontoId).rollen().contains(Rolle.ADMINISTRATOR);
        jdbc.update("""
                UPDATE benachrichtigung SET gelesen=TRUE, gelesen_am=?
                WHERE gelesen=FALSE AND (empfaenger_id=? OR (? AND empfaenger_rolle='ADMINISTRATOR'))
                """, LocalDateTime.now(clock), kontoId, admin);
    }

    @Transactional
    public void alsGelesen(String kontoId, long id) {
        boolean admin = konten.laden(kontoId).rollen().contains(Rolle.ADMINISTRATOR);
        int geaendert = jdbc.update("""
                UPDATE benachrichtigung SET gelesen=TRUE, gelesen_am=?
                WHERE id=? AND (empfaenger_id=? OR (? AND empfaenger_rolle='ADMINISTRATOR'))
                """, LocalDateTime.now(clock), id, kontoId, admin);
        if (geaendert == 0) throw new KontoFehler(HttpStatus.NOT_FOUND,
                "BENACHRICHTIGUNG_NICHT_GEFUNDEN", "Die Benachrichtigung wurde nicht gefunden.");
    }

    public void persoenlich(String empfaengerId, String ausloeserId, Benachrichtigungsanlass anlasstyp,
                            String text, String bezugArt, String bezugId) {
        if (empfaengerId.equals(ausloeserId)) return;
        jdbc.update("""
                INSERT INTO benachrichtigung
                    (empfaenger_id, anlasstyp, anlass, bezug_art, bezug_id)
                VALUES (?, ?, ?, ?, ?)
                """, empfaengerId, anlasstyp.name(), text, bezugArt, bezugId);
    }

    public void adminbereich(Benachrichtigungsanlass anlasstyp, String text, String bezugArt, String bezugId) {
        jdbc.update("""
                INSERT INTO benachrichtigung
                    (empfaenger_rolle, anlasstyp, anlass, bezug_art, bezug_id)
                VALUES ('ADMINISTRATOR', ?, ?, ?, ?)
                """, anlasstyp.name(), text, bezugArt, bezugId);
    }

    public record Benachrichtigung(long id, String anlasstyp, String anlass, String bezugArt,
                                   String bezugId, LocalDateTime erstelltAm, boolean gelesen,
                                   boolean bezugVorhanden) {}
}
