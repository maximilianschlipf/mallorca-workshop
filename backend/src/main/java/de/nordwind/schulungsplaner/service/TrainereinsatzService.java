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
import java.util.List;

@Service
public class TrainereinsatzService {
    private final JdbcTemplate jdbc;
    private final KontoService konten;
    private final KatalogRepository katalog;
    private final SchulungszustandRepository zustaende;
    private final Clock clock;
    private final TerminService termine;

    public TrainereinsatzService(JdbcTemplate jdbc, KontoService konten,
                                 KatalogRepository katalog,
                                 SchulungszustandRepository zustaende, Clock clock,
                                 TerminService termine) {
        this.jdbc = jdbc;
        this.konten = konten;
        this.katalog = katalog;
        this.zustaende = zustaende;
        this.clock = clock;
        this.termine = termine;
    }

    @Transactional
    public void aufQualifikationBewerben(String kontoId, String schulungId) {
        pruefeAktivenTrainer(kontoId);
        SchulungId id = SchulungId.von(schulungId);
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
        einfuegenOderKonflikt("""
                INSERT INTO qualifikationsbewerbung (benutzerkonto_id, schulung_id)
                VALUES (?, ?)
                """, "BEWERBUNG_BESTEHT", "Für diese Schulung besteht bereits eine Bewerbung.",
                kontoId, schulungId);
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
    public List<TrainerTermin> meineTermine(String kontoId) {
        termine.nachziehen();
        return jdbc.query("""
                SELECT termin_id, schulung_id, startdatum, enddatum, ort, status
                FROM termin WHERE trainer_id = ? ORDER BY startdatum, termin_id
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
        }, kontoId);
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

    public record TrainerTermin(
            String terminId, String schulungId, String schulungstitel,
            LocalDate startdatum, LocalDate enddatum, String ort,
            String status, String schulungszustand) {}
}
