package de.nordwind.schulungsplaner.api;

import de.nordwind.schulungsplaner.config.KontoPrincipal;
import de.nordwind.schulungsplaner.domain.Rolle;
import de.nordwind.schulungsplaner.service.KontoService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Profile("e2e")
@RestController
@RequestMapping("/api/e2e")
class E2eFixtureController {
    private final JdbcTemplate jdbc;
    private final KontoService konten;

    E2eFixtureController(JdbcTemplate jdbc, KontoService konten) {
        this.jdbc = jdbc;
        this.konten = konten;
    }

    @PutMapping("/qualifikationen/{kontoId}/{schulungId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void qualifikationErteilen(@AuthenticationPrincipal KontoPrincipal akteur,
                               @PathVariable String kontoId, @PathVariable String schulungId) {
        if (!konten.laden(akteur.id()).rollen().contains(Rolle.ADMINISTRATOR)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        jdbc.update("INSERT INTO trainer_qualifikation (benutzerkonto_id, schulung_id) VALUES (?, ?)",
                kontoId, schulungId);
    }

    @PutMapping("/benachrichtigungen")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void benachrichtigungenAnlegen(@AuthenticationPrincipal KontoPrincipal akteur) {
        jdbc.update("DELETE FROM benachrichtigung WHERE empfaenger_id=?", akteur.id());
        jdbc.update("""
                MERGE INTO termin (termin_id, schulung_id, startdatum, enddatum, status)
                KEY (termin_id) VALUES ('E2E-NAC-TERMIN', 'SCH-001', '2026-09-18', '2026-09-18', 'geplant')
                """);
        nachricht(akteur.id(), "QUALIFIKATION_GENEHMIGT", "Älteste Mitteilung",
                "SCHULUNG", "SCH-001", LocalDateTime.of(2026, 9, 17, 8, 0));
        nachricht(akteur.id(), "TERMIN_GEAENDERT", "Mittlere Mitteilung",
                "TERMIN", "E2E-NAC-TERMIN", LocalDateTime.of(2026, 9, 17, 9, 0));
        nachricht(akteur.id(), "QUALIFIKATION_ENTZOGEN", "Neueste Mitteilung",
                null, null, LocalDateTime.of(2026, 9, 17, 10, 0));
    }

    @PutMapping("/dashboard-pflichten")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void dashboardPflichtenAnlegen(@AuthenticationPrincipal KontoPrincipal akteur) {
        jdbc.update("DELETE FROM termin WHERE termin_id LIKE 'E2E-DSH-PFLI-%'");
        jdbc.update("MERGE INTO trainer_qualifikation (benutzerkonto_id, schulung_id) KEY (benutzerkonto_id, schulung_id) VALUES (?, 'SCH-001')",
                akteur.id());
        termin("E2E-DSH-PFLI-UEBERFAELLIG", "2026-09-16", "geplant", akteur.id());
        termin("E2E-DSH-PFLI-ZUKUNFT", "2026-09-18", "geplant", akteur.id());
        termin("E2E-DSH-PFLI-ABGESAGT", "2026-09-16", "abgesagt", akteur.id());
        termin("E2E-DSH-PFLI-ABGESCHLOSSEN", "2026-09-16", "abgeschlossen", akteur.id());
    }

    private void termin(String id, String datum, String status, String trainerId) {
        jdbc.update("""
                INSERT INTO termin
                    (termin_id, schulung_id, schulung_titel, startdatum, enddatum, status, trainer_id)
                VALUES (?, 'SCH-001', 'Scrum Master Zertifizierung', ?, ?, ?, ?)
                """, id, datum, datum, status, trainerId);
    }

    private void nachricht(String kontoId, String typ, String text, String bezugArt,
                           String bezugId, LocalDateTime zeitpunkt) {
        jdbc.update("""
                INSERT INTO benachrichtigung
                    (empfaenger_id, anlasstyp, anlass, bezug_art, bezug_id, erstellt_am)
                VALUES (?, ?, ?, ?, ?, ?)
                """, kontoId, typ, text, bezugArt, bezugId, zeitpunkt);
    }
}
