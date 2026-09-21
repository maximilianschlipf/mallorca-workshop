package de.nordwind.schulungsplaner.service;

import de.nordwind.schulungsplaner.domain.Benutzerkonto;
import de.nordwind.schulungsplaner.domain.Rolle;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Verwaltet Gruppen: eine benannte Menge von Mitgliedern mit einem Gruppentrainer.
 * Termine können einer Gruppe zugeordnet werden.
 */
@Service
public class GruppeService {
    private final JdbcTemplate jdbc;
    private final KontoService konten;

    public GruppeService(JdbcTemplate jdbc, KontoService konten) {
        this.jdbc = jdbc;
        this.konten = konten;
    }

    @Transactional(readOnly = true)
    public List<GruppenAnsicht> alle(String kontoId) {
        konten.laden(kontoId);
        return jdbc.queryForList("SELECT id FROM gruppe ORDER BY LOWER(name), name", String.class)
                .stream().map(this::ansicht).toList();
    }

    @Transactional(readOnly = true)
    public GruppenAnsicht details(String kontoId, String gruppeId) {
        konten.laden(kontoId);
        return ansicht(vorhanden(gruppeId));
    }

    @Transactional
    public GruppenAnsicht anlegen(String administratorId, GruppenEingabe eingabe) {
        pruefeAdministrator(administratorId);
        String name = sauber(eingabe.name());
        pruefeNameFrei(name, null);
        String trainerId = leerZuNull(eingabe.trainerId());
        Set<String> mitglieder = new LinkedHashSet<>(eingabe.mitgliederIds() == null ? List.of() : eingabe.mitgliederIds());
        if (trainerId != null) pruefeTrainer(trainerId);
        pruefeKeinTrainerImMitglied(trainerId, mitglieder);
        mitglieder.forEach(this::pruefeMitglied);
        String id = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO gruppe (id, name, trainer_id) VALUES (?, ?, ?)", id, name, trainerId);
        mitglieder.forEach(m -> jdbc.update(
                "INSERT INTO gruppe_mitglied (gruppe_id, benutzerkonto_id) VALUES (?, ?)", id, m));
        return ansicht(id);
    }

    /** Ersetzt Name und Gruppentrainer; ein leerer Trainer entfernt den Gruppentrainer. */
    @Transactional
    public GruppenAnsicht aendern(String administratorId, String gruppeId, String name, String trainerId) {
        pruefeAdministrator(administratorId);
        vorhanden(gruppeId);
        String neuerName = sauber(name);
        pruefeNameFrei(neuerName, gruppeId);
        String neuerTrainer = leerZuNull(trainerId);
        if (neuerTrainer != null) {
            pruefeTrainer(neuerTrainer);
            if (existiert("SELECT COUNT(*) FROM gruppe_mitglied WHERE gruppe_id=? AND benutzerkonto_id=?",
                    gruppeId, neuerTrainer)) throw trainerIstMitglied();
        }
        jdbc.update("UPDATE gruppe SET name=?, trainer_id=? WHERE id=?", neuerName, neuerTrainer, gruppeId);
        return ansicht(gruppeId);
    }

    @Transactional
    public GruppenAnsicht mitgliedHinzufuegen(String administratorId, String gruppeId, String kontoId) {
        pruefeAdministrator(administratorId);
        vorhanden(gruppeId);
        pruefeMitglied(kontoId);
        if (kontoId.equals(jdbc.queryForObject("SELECT trainer_id FROM gruppe WHERE id=?", String.class, gruppeId))) {
            throw trainerIstMitglied();
        }
        if (!existiert("SELECT COUNT(*) FROM gruppe_mitglied WHERE gruppe_id=? AND benutzerkonto_id=?", gruppeId, kontoId)) {
            jdbc.update("INSERT INTO gruppe_mitglied (gruppe_id, benutzerkonto_id) VALUES (?, ?)", gruppeId, kontoId);
        }
        return ansicht(gruppeId);
    }

    @Transactional
    public GruppenAnsicht mitgliedEntfernen(String administratorId, String gruppeId, String kontoId) {
        pruefeAdministrator(administratorId);
        vorhanden(gruppeId);
        jdbc.update("DELETE FROM gruppe_mitglied WHERE gruppe_id=? AND benutzerkonto_id=?", gruppeId, kontoId);
        return ansicht(gruppeId);
    }

    @Transactional
    public void loeschen(String administratorId, String gruppeId) {
        pruefeAdministrator(administratorId);
        vorhanden(gruppeId);
        if (existiert("SELECT COUNT(*) FROM termin WHERE gruppe_id=? AND status='geplant'", gruppeId)) {
            throw fehler(HttpStatus.CONFLICT, "GRUPPE_HAT_TERMINE",
                    "Die Gruppe hat noch geplante Termine und kann nicht gelöscht werden.");
        }
        jdbc.update("DELETE FROM gruppe WHERE id=?", gruppeId);
    }

    private GruppenAnsicht ansicht(String id) {
        String name = jdbc.queryForObject("SELECT name FROM gruppe WHERE id=?", String.class, id);
        List<Person> trainer = jdbc.query("""
                SELECT k.id, k.name, k.email FROM gruppe g JOIN benutzerkonto k ON k.id=g.trainer_id
                WHERE g.id=?
                """, (rs, row) -> new Person(rs.getString(1), rs.getString(2), rs.getString(3)), id);
        List<Person> mitglieder = jdbc.query("""
                SELECT k.id, k.name, k.email FROM gruppe_mitglied m JOIN benutzerkonto k ON k.id=m.benutzerkonto_id
                WHERE m.gruppe_id=? ORDER BY LOWER(k.name), k.name
                """, (rs, row) -> new Person(rs.getString(1), rs.getString(2), rs.getString(3)), id);
        Integer termine = jdbc.queryForObject("SELECT COUNT(*) FROM termin WHERE gruppe_id=?", Integer.class, id);
        return new GruppenAnsicht(id, name, trainer.isEmpty() ? null : trainer.getFirst(), mitglieder,
                termine == null ? 0 : termine);
    }

    private String vorhanden(String gruppeId) {
        if (gruppeId == null || !existiert("SELECT COUNT(*) FROM gruppe WHERE id=?", gruppeId)) {
            throw fehler(HttpStatus.NOT_FOUND, "GRUPPE_NICHT_GEFUNDEN", "Die Gruppe wurde nicht gefunden.");
        }
        return gruppeId;
    }

    private void pruefeAdministrator(String id) {
        if (!konten.laden(id).rollen().contains(Rolle.ADMINISTRATOR)) {
            throw fehler(HttpStatus.FORBIDDEN, "ADMINISTRATOR_ERFORDERLICH",
                    "Für diese Aktion sind Administratorrechte erforderlich.");
        }
    }

    private void pruefeTrainer(String id) {
        Benutzerkonto konto = konten.laden(id);
        if (!konto.aktiv() || !konto.rollen().contains(Rolle.TRAINER)) {
            throw fehler(HttpStatus.CONFLICT, "TRAINER_ERFORDERLICH", "Das Benutzerkonto ist kein aktiver Trainer.");
        }
    }

    private void pruefeMitglied(String id) {
        Benutzerkonto konto = konten.laden(id);
        if (!konto.aktiv()) {
            throw fehler(HttpStatus.CONFLICT, "KONTO_STILLGELEGT", "Ein stillgelegtes Konto kann kein Mitglied sein.");
        }
    }

    private void pruefeKeinTrainerImMitglied(String trainerId, Set<String> mitglieder) {
        if (trainerId != null && mitglieder.contains(trainerId)) throw trainerIstMitglied();
    }

    private KontoFehler trainerIstMitglied() {
        return fehler(HttpStatus.CONFLICT, "TRAINER_KEIN_MITGLIED",
                "Der Gruppentrainer kann nicht zugleich Mitglied der Gruppe sein.");
    }

    private void pruefeNameFrei(String name, String ausnahmeId) {
        if (existiert("SELECT COUNT(*) FROM gruppe WHERE LOWER(name)=? AND id<>?",
                name.toLowerCase(Locale.ROOT), ausnahmeId == null ? "" : ausnahmeId)) {
            throw fehler(HttpStatus.CONFLICT, "GRUPPENNAME_VERGEBEN", "Eine Gruppe mit diesem Namen existiert bereits.");
        }
    }

    private String sauber(String name) {
        String s = leerZuNull(name);
        if (s == null) throw fehler(HttpStatus.BAD_REQUEST, "GRUPPENNAME_FEHLT", "Der Gruppenname fehlt.");
        if (s.length() > 255) throw fehler(HttpStatus.BAD_REQUEST, "GRUPPENNAME_ZU_LANG",
                "Der Gruppenname darf höchstens 255 Zeichen lang sein.");
        return s;
    }

    private static String leerZuNull(String wert) {
        if (wert == null) return null;
        String s = wert.trim();
        return s.isEmpty() ? null : s;
    }

    private boolean existiert(String sql, Object... parameter) {
        Integer wert = jdbc.queryForObject(sql, Integer.class, parameter);
        return wert != null && wert > 0;
    }

    private KontoFehler fehler(HttpStatus status, String code, String text) {
        return new KontoFehler(status, code, text);
    }

    public record GruppenEingabe(String name, String trainerId, List<String> mitgliederIds) {}
    public record Person(String id, String name, String email) {}
    public record GruppenAnsicht(String id, String name, Person trainer, List<Person> mitglieder, int anzahlTermine) {}
}
