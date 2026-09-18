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
}
