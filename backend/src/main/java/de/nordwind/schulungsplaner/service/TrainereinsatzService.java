package de.nordwind.schulungsplaner.service;

import de.nordwind.schulungsplaner.domain.Benutzerkonto;
import de.nordwind.schulungsplaner.domain.Rolle;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TrainereinsatzService {
    private final JdbcTemplate jdbc;
    private final KontoService konten;

    public TrainereinsatzService(JdbcTemplate jdbc, KontoService konten) {
        this.jdbc = jdbc;
        this.konten = konten;
    }

    @Transactional
    public void aufQualifikationBewerben(String kontoId, String schulungId) {
        pruefeAktivenTrainer(kontoId);
        if (!existiert("SELECT COUNT(*) FROM schulung WHERE id = ?", schulungId)) {
            throw fehler(HttpStatus.NOT_FOUND, "SCHULUNG_NICHT_GEFUNDEN",
                    "Die Schulung wurde nicht gefunden.");
        }
        if (existiert("""
                SELECT COUNT(*) FROM trainer_qualifikation
                WHERE benutzerkonto_id = ? AND schulung_id = ?
                """, kontoId, schulungId)) {
            throw fehler(HttpStatus.CONFLICT, "BEREITS_QUALIFIZIERT",
                    "Für diese Schulung besteht bereits eine Qualifikation.");
        }
        einfuegenOderKonflikt("""
                INSERT INTO qualifikationsbewerbung (benutzerkonto_id, schulung_id)
                VALUES (?, ?)
                """, "BEWERBUNG_BESTEHT", "Für diese Schulung besteht bereits eine Bewerbung.",
                kontoId, schulungId);
    }

    @Transactional
    public void aufAssistenzplatzBewerben(String kontoId, String terminId) {
        pruefeAktivenTrainer(kontoId);
        TerminDaten termin = terminLaden(terminId, false);
        if (termin.enddatum().isBefore(LocalDate.now()) || "abgeschlossen".equals(termin.status())) {
            throw fehler(HttpStatus.CONFLICT, "TERMIN_ABGESCHLOSSEN",
                    "Auf einen abgeschlossenen Termin ist keine Bewerbung möglich.");
        }
        einfuegenOderKonflikt("""
                INSERT INTO assistenzbewerbung (termin_id, benutzerkonto_id)
                VALUES (?, ?)
                """, "BEWERBUNG_BESTEHT", "Für diesen Termin besteht bereits eine Bewerbung.",
                terminId, kontoId);
    }

    @Transactional
    public void abwesenheitEintragen(String kontoId, LocalDate von, LocalDate bis, String grund) {
        pruefeAktivenTrainer(kontoId);
        if (von.isAfter(bis)) {
            throw fehler(HttpStatus.BAD_REQUEST, "UNGUELTIGER_ZEITRAUM",
                    "Das Anfangsdatum darf nicht nach dem Enddatum liegen.");
        }
        jdbc.update("""
                INSERT INTO abwesenheit (benutzerkonto_id, von, bis, grund)
                VALUES (?, ?, ?, ?)
                """, kontoId, von, bis, grund);
    }

    @Transactional
    public void trainerZuweisen(String administratorId, String terminId, String trainerId) {
        pruefeAdministrator(administratorId);
        TerminDaten termin = terminLaden(terminId, true);
        pruefeAktivenTrainer(trainerId);
        if (!existiert("""
                SELECT COUNT(*) FROM trainer_qualifikation
                WHERE benutzerkonto_id = ? AND schulung_id = ?
                """, trainerId, termin.schulungId())) {
            throw fehler(HttpStatus.CONFLICT, "QUALIFIKATION_ERFORDERLICH",
                    "Der Trainer ist für diese Schulung nicht qualifiziert.");
        }
        jdbc.update("UPDATE termin SET trainer_id = ?, trainer_name_snapshot = NULL WHERE termin_id = ?",
                trainerId, terminId);
    }

    @Transactional
    public void assistentZuweisen(String administratorId, String terminId, String trainerId) {
        pruefeAdministrator(administratorId);
        terminLaden(terminId, true);
        pruefeAktivenTrainer(trainerId);
        List<Integer> belegtePlaetze = jdbc.queryForList(
                "SELECT platz FROM termin_assistent WHERE termin_id = ?", Integer.class, terminId);
        int freierPlatz = java.util.stream.IntStream.rangeClosed(1, 3)
                .filter(platz -> !belegtePlaetze.contains(platz))
                .findFirst()
                .orElseThrow(() -> fehler(HttpStatus.CONFLICT, "KEIN_ASSISTENZPLATZ",
                        "Für diesen Termin sind bereits drei Assistenzplätze belegt."));
        einfuegenOderKonflikt("""
                INSERT INTO termin_assistent (termin_id, benutzerkonto_id, platz)
                VALUES (?, ?, ?)
                """, "BEREITS_ZUGEWIESEN", "Das Benutzerkonto ist diesem Termin bereits zugewiesen.",
                terminId, trainerId, freierPlatz);
    }

    private void pruefeAdministrator(String kontoId) {
        if (!konten.laden(kontoId).rollen().contains(Rolle.ADMINISTRATOR)) {
            throw fehler(HttpStatus.FORBIDDEN, "ADMINISTRATOR_ERFORDERLICH",
                    "Für diese Aktion sind Administratorrechte erforderlich.");
        }
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
}
