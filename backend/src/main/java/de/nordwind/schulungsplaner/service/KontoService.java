package de.nordwind.schulungsplaner.service;

import de.nordwind.schulungsplaner.domain.Benutzerkonto;
import de.nordwind.schulungsplaner.domain.Rolle;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class KontoService {
    private static final String UNGUELTIGER_LOGIN_HASH =
            "$2a$10$7EqJtq98hPqEX7fNZaFWoO5u8Kq4StL6.eHgF0cH8qfF5MCxI4f6a";

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwoerter;

    public KontoService(JdbcTemplate jdbc, PasswordEncoder passwoerter) {
        this.jdbc = jdbc;
        this.passwoerter = passwoerter;
    }

    @Transactional
    public Benutzerkonto registrieren(String name, String email, String passwort) {
        String normalisierteEmail = normalisiereEmail(email);
        String eigentuemer = jdbc.queryForObject(
                "SELECT eigentuemer_id FROM instanz WHERE id = 1 FOR UPDATE", String.class);
        String id = UUID.randomUUID().toString();
        try {
            jdbc.update("""
                    INSERT INTO benutzerkonto (id, email, name, passwort_hash, aktiv)
                    VALUES (?, ?, ?, ?, TRUE)
                    """, id, normalisierteEmail, name.trim(), passwoerter.encode(passwort));
        } catch (DataIntegrityViolationException ex) {
            throw fehler(HttpStatus.CONFLICT, "EMAIL_VERGEBEN",
                    "Für diese E-Mail-Adresse besteht bereits ein Benutzerkonto.");
        }
        rolleEinfuegen(id, Rolle.TRAINER);
        if (eigentuemer == null) {
            rolleEinfuegen(id, Rolle.ADMINISTRATOR);
            jdbc.update("UPDATE instanz SET eigentuemer_id = ? WHERE id = 1", id);
        }
        return laden(id);
    }

    public Benutzerkonto anmelden(String email, String passwort) {
        Optional<Benutzerkonto> konto = anhandEmail(email);
        String hash = konto.map(Benutzerkonto::passwortHash).orElse(UNGUELTIGER_LOGIN_HASH);
        if (!passwoerter.matches(passwort, hash)) {
            throw fehler(HttpStatus.UNAUTHORIZED, "UNGUELTIGE_ANMELDEDATEN",
                    "E-Mail-Adresse oder Passwort ist falsch.");
        }
        Benutzerkonto gefunden = konto.orElseThrow();
        if (!gefunden.aktiv()) {
            throw fehler(HttpStatus.FORBIDDEN, "KONTO_STILLGELEGT",
                    "Dieses Benutzerkonto ist stillgelegt.");
        }
        return gefunden;
    }

    public Optional<Benutzerkonto> finden(String id) {
        List<Benutzerkonto> konten = jdbc.query("""
                SELECT k.id, k.email, k.name, k.passwort_hash, k.aktiv,
                       k.aenderungsstand, k.passwort_version,
                       i.eigentuemer_id
                FROM benutzerkonto k CROSS JOIN instanz i
                WHERE k.id = ? AND i.id = 1
                """, this::kontoAusZeile, id);
        return konten.stream().findFirst();
    }

    public Benutzerkonto laden(String id) {
        return finden(id).orElseThrow(() -> fehler(HttpStatus.NOT_FOUND,
                "KONTO_NICHT_GEFUNDEN", "Das Benutzerkonto wurde nicht gefunden."));
    }

    public List<Benutzerkonto> alle() {
        return jdbc.query("""
                SELECT k.id, k.email, k.name, k.passwort_hash, k.aktiv,
                       k.aenderungsstand, k.passwort_version,
                       i.eigentuemer_id
                FROM benutzerkonto k CROSS JOIN instanz i
                WHERE i.id = 1
                ORDER BY LOWER(k.name), k.name, k.id
                """, this::kontoAusZeile);
    }

    @Transactional
    public Benutzerkonto nameAendern(String id, String name, long aenderungsstand) {
        pruefeAenderungsstand(jdbc.update("""
                UPDATE benutzerkonto SET name = ?, aenderungsstand = aenderungsstand + 1
                WHERE id = ? AND aenderungsstand = ?
                """, name.trim(), id, aenderungsstand), id);
        return laden(id);
    }

    @Transactional
    public Benutzerkonto passwortAendern(String id, String bisher, String neu,
                                          long aenderungsstand) {
        Benutzerkonto konto = laden(id);
        if (!passwoerter.matches(bisher, konto.passwortHash())) {
            throw fehler(HttpStatus.BAD_REQUEST, "PASSWORT_FALSCH",
                    "Das bisherige Passwort ist falsch.");
        }
        passwortSpeichern(id, neu, aenderungsstand);
        return laden(id);
    }

    @Transactional
    public void passwortSetzen(String akteurId, String zielId, String passwort,
                               long aenderungsstand) {
        pruefeAdministrator(akteurId);
        if (akteurId.equals(zielId)) {
            throw fehler(HttpStatus.BAD_REQUEST, "EIGENES_PASSWORT",
                    "Das eigene Passwort wird im Profil geändert.");
        }
        Benutzerkonto ziel = laden(zielId);
        if (ziel.rollen().contains(Rolle.EIGENTUEMER)) {
            throw fehler(HttpStatus.FORBIDDEN, "EIGENTUEMER_GESCHUETZT",
                    "Das Passwort des Eigentümers kann nur dieser selbst ändern.");
        }
        passwortSpeichern(zielId, passwort, aenderungsstand);
    }

    @Transactional
    public void rolleErteilen(String akteurId, String zielId, Rolle rolle, long aenderungsstand) {
        pruefeAdministrator(akteurId);
        Benutzerkonto ziel = laden(zielId);
        if (rolle == Rolle.EIGENTUEMER) {
            throw fehler(HttpStatus.BAD_REQUEST, "EIGENTUEMER_NUR_UEBERGEBEN",
                    "Die Eigentümerrolle kann nur übergeben werden.");
        }
        if (ziel.rollen().contains(rolle)) return;
        aenderungsstandErhoehen(zielId, aenderungsstand);
        rolleEinfuegen(zielId, rolle);
    }

    @Transactional
    public void rolleEntziehen(String akteurId, String zielId, Rolle rolle, long aenderungsstand) {
        Benutzerkonto akteur = pruefeAdministrator(akteurId);
        Benutzerkonto ziel = laden(zielId);
        if (rolle == Rolle.EIGENTUEMER) {
            throw fehler(HttpStatus.BAD_REQUEST, "EIGENTUEMER_NUR_UEBERGEBEN",
                    "Die Eigentümerrolle kann nur übergeben werden.");
        }
        if (rolle == Rolle.ADMINISTRATOR && !akteur.rollen().contains(Rolle.EIGENTUEMER)) {
            throw fehler(HttpStatus.FORBIDDEN, "NUR_EIGENTUEMER",
                    "Nur der Eigentümer darf die Administratorrolle entziehen.");
        }
        if (rolle == Rolle.ADMINISTRATOR && ziel.rollen().contains(Rolle.EIGENTUEMER)) {
            throw fehler(HttpStatus.FORBIDDEN, "EIGENTUEMER_GESCHUETZT",
                    "Der Eigentümer muss Administrator bleiben.");
        }
        if (!ziel.rollen().contains(rolle)) return;
        if (ziel.rollen().size() == 1) {
            throw fehler(HttpStatus.CONFLICT, "LETZTE_ROLLE",
                    "Ein Benutzerkonto muss mindestens eine Rolle behalten.");
        }
        aenderungsstandErhoehen(zielId, aenderungsstand);
        jdbc.update("DELETE FROM benutzerkonto_rolle WHERE benutzerkonto_id = ? AND rolle = ?",
                zielId, rolle.name());
        if (rolle == Rolle.TRAINER) entferneZukuenftigeZuweisungen(zielId);
    }

    @Transactional
    public void stilllegen(String akteurId, String zielId, long aenderungsstand) {
        pruefeAdministrator(akteurId);
        Benutzerkonto ziel = laden(zielId);
        if (ziel.rollen().contains(Rolle.EIGENTUEMER)) geschuetzterEigentuemer();
        pruefeAenderungsstand(jdbc.update("""
                UPDATE benutzerkonto SET aktiv = FALSE, aenderungsstand = aenderungsstand + 1
                WHERE id = ? AND aenderungsstand = ?
                """, zielId, aenderungsstand), zielId);
        entferneZukuenftigeZuweisungen(zielId);
    }

    @Transactional
    public void reaktivieren(String akteurId, String zielId, long aenderungsstand) {
        pruefeAdministrator(akteurId);
        pruefeAenderungsstand(jdbc.update("""
                UPDATE benutzerkonto SET aktiv = TRUE, aenderungsstand = aenderungsstand + 1
                WHERE id = ? AND aenderungsstand = ?
                """, zielId, aenderungsstand), zielId);
    }

    @Transactional
    public void eigentuemerUebergeben(String akteurId, String zielId, long aenderungsstand) {
        String aktuellerEigentuemer = jdbc.queryForObject(
                "SELECT eigentuemer_id FROM instanz WHERE id = 1 FOR UPDATE", String.class);
        if (!akteurId.equals(aktuellerEigentuemer)) {
            throw fehler(HttpStatus.FORBIDDEN, "NUR_EIGENTUEMER",
                    "Nur der Eigentümer darf die Eigentümerrolle übergeben.");
        }
        if (akteurId.equals(zielId)) {
            throw fehler(HttpStatus.BAD_REQUEST, "GLEICHES_KONTO",
                    "Die Eigentümerrolle muss an ein anderes Benutzerkonto gehen.");
        }
        Benutzerkonto ziel = laden(zielId);
        if (!ziel.aktiv()) {
            throw fehler(HttpStatus.CONFLICT, "KONTO_STILLGELEGT",
                    "Ein stillgelegtes Benutzerkonto kann nicht Eigentümer werden.");
        }
        aenderungsstandErhoehen(zielId, aenderungsstand);
        rolleEinfuegen(zielId, Rolle.ADMINISTRATOR);
        jdbc.update("UPDATE instanz SET eigentuemer_id = ? WHERE id = 1", zielId);
        jdbc.update("UPDATE benutzerkonto SET aenderungsstand = aenderungsstand + 1 WHERE id = ?",
                akteurId);
    }

    @Transactional
    public void loeschen(String akteurId, String zielId, long aenderungsstand) {
        pruefeAdministrator(akteurId);
        Benutzerkonto ziel = laden(zielId);
        if (ziel.rollen().contains(Rolle.EIGENTUEMER)) geschuetzterEigentuemer();
        aenderungsstandErhoehen(zielId, aenderungsstand);
        jdbc.update("""
                UPDATE termin SET trainer_name_snapshot = ?, trainer_id = NULL
                WHERE trainer_id = ? AND status = 'abgeschlossen'
                """, ziel.name(), zielId);
        jdbc.update("UPDATE termin SET trainer_id = NULL WHERE trainer_id = ?", zielId);
        jdbc.update("""
                UPDATE termin_assistent SET name_snapshot = ?, benutzerkonto_id = NULL
                WHERE benutzerkonto_id = ? AND termin_id IN
                    (SELECT termin_id FROM termin WHERE status = 'abgeschlossen')
                """, ziel.name(), zielId);
        jdbc.update("DELETE FROM termin_assistent WHERE benutzerkonto_id = ?", zielId);
        jdbc.update("DELETE FROM benutzerkonto WHERE id = ?", zielId);
    }

    private Benutzerkonto pruefeAdministrator(String id) {
        Benutzerkonto konto = laden(id);
        if (!konto.rollen().contains(Rolle.ADMINISTRATOR)) {
            throw fehler(HttpStatus.FORBIDDEN, "ADMINISTRATOR_ERFORDERLICH",
                    "Für diese Aktion sind Administratorrechte erforderlich.");
        }
        return konto;
    }

    private void entferneZukuenftigeZuweisungen(String id) {
        jdbc.update("""
                UPDATE termin SET trainer_id = NULL
                WHERE trainer_id = ? AND enddatum >= CURRENT_DATE AND status <> 'abgeschlossen'
                """, id);
        jdbc.update("""
                DELETE FROM termin_assistent WHERE benutzerkonto_id = ? AND termin_id IN
                    (SELECT termin_id FROM termin
                     WHERE enddatum >= CURRENT_DATE AND status <> 'abgeschlossen')
                """, id);
    }

    private void passwortSpeichern(String id, String passwort, long aenderungsstand) {
        pruefeAenderungsstand(jdbc.update("""
                UPDATE benutzerkonto
                SET passwort_hash = ?, passwort_version = passwort_version + 1,
                    aenderungsstand = aenderungsstand + 1
                WHERE id = ? AND aenderungsstand = ?
                """, passwoerter.encode(passwort), id, aenderungsstand), id);
    }

    private Optional<Benutzerkonto> anhandEmail(String email) {
        String normalisiert = normalisiereEmail(email);
        return jdbc.query("SELECT id FROM benutzerkonto WHERE email = ?",
                (rs, row) -> laden(rs.getString("id")), normalisiert).stream().findFirst();
    }

    private Benutzerkonto kontoAusZeile(ResultSet rs, int zeile) throws SQLException {
        String id = rs.getString("id");
        String eigentuemer = rs.getString("eigentuemer_id");
        Set<Rolle> rollen = EnumSet.noneOf(Rolle.class);
        rollen.addAll(jdbc.queryForList(
                "SELECT rolle FROM benutzerkonto_rolle WHERE benutzerkonto_id = ?",
                String.class, id).stream().map(Rolle::valueOf).toList());
        if (id.equals(eigentuemer)) rollen.add(Rolle.EIGENTUEMER);
        return new Benutzerkonto(id, rs.getString("email"), rs.getString("name"),
                rs.getString("passwort_hash"), rs.getBoolean("aktiv"),
                rs.getLong("aenderungsstand"), rs.getLong("passwort_version"), Set.copyOf(rollen));
    }

    private void rolleEinfuegen(String id, Rolle rolle) {
        jdbc.update("""
                INSERT INTO benutzerkonto_rolle (benutzerkonto_id, rolle)
                SELECT ?, ? WHERE NOT EXISTS (
                    SELECT 1 FROM benutzerkonto_rolle WHERE benutzerkonto_id = ? AND rolle = ?)
                """, id, rolle.name(), id, rolle.name());
    }

    private String normalisiereEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void aenderungsstandErhoehen(String id, long aenderungsstand) {
        pruefeAenderungsstand(jdbc.update("""
                UPDATE benutzerkonto SET aenderungsstand = aenderungsstand + 1
                WHERE id = ? AND aenderungsstand = ?
                """, id, aenderungsstand), id);
    }

    private void pruefeAenderungsstand(int anzahl, String id) {
        if (anzahl > 0) return;
        if (finden(id).isEmpty()) {
            throw fehler(HttpStatus.NOT_FOUND,
                    "KONTO_NICHT_GEFUNDEN", "Das Benutzerkonto wurde nicht gefunden.");
        }
        throw fehler(HttpStatus.CONFLICT, "ZWISCHENZEITLICH_GEAENDERT",
                "Das Benutzerkonto wurde inzwischen geändert. Bitte laden Sie es neu.");
    }

    private void geschuetzterEigentuemer() {
        throw fehler(HttpStatus.FORBIDDEN, "EIGENTUEMER_GESCHUETZT",
                "Das Eigentümerkonto kann weder stillgelegt noch gelöscht werden.");
    }

    private KontoFehler fehler(HttpStatus status, String code, String text) {
        return new KontoFehler(status, code, text);
    }
}
