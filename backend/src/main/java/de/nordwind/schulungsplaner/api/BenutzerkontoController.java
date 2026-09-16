package de.nordwind.schulungsplaner.api;

import de.nordwind.schulungsplaner.config.KontoPrincipal;
import de.nordwind.schulungsplaner.domain.Rolle;
import de.nordwind.schulungsplaner.service.KontoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/benutzerkonten")
public class BenutzerkontoController {
    private final KontoService konten;

    public BenutzerkontoController(KontoService konten) {
        this.konten = konten;
    }

    @GetMapping
    public List<BenutzerkontoAntwort> alle() {
        return konten.alle().stream().map(BenutzerkontoAntwort::from).toList();
    }

    @PutMapping("/{id}/rollen/{rolle}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rolleErteilen(@AuthenticationPrincipal KontoPrincipal akteur,
                              @PathVariable String id, @PathVariable Rolle rolle,
                              @RequestHeader("If-Match") long aenderungsstand) {
        konten.rolleErteilen(akteur.id(), id, rolle, aenderungsstand);
    }

    @DeleteMapping("/{id}/rollen/{rolle}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rolleEntziehen(@AuthenticationPrincipal KontoPrincipal akteur,
                               @PathVariable String id, @PathVariable Rolle rolle,
                               @RequestHeader("If-Match") long aenderungsstand) {
        konten.rolleEntziehen(akteur.id(), id, rolle, aenderungsstand);
    }

    @PostMapping("/{id}/stilllegen")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void stilllegen(@AuthenticationPrincipal KontoPrincipal akteur, @PathVariable String id,
                           @RequestHeader("If-Match") long aenderungsstand) {
        konten.stilllegen(akteur.id(), id, aenderungsstand);
    }

    @PostMapping("/{id}/reaktivieren")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reaktivieren(@AuthenticationPrincipal KontoPrincipal akteur, @PathVariable String id,
                             @RequestHeader("If-Match") long aenderungsstand) {
        konten.reaktivieren(akteur.id(), id, aenderungsstand);
    }

    @PutMapping("/{id}/passwort")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void passwortSetzen(@AuthenticationPrincipal KontoPrincipal akteur,
                               @PathVariable String id,
                               @RequestHeader("If-Match") long aenderungsstand,
                               @Valid @RequestBody NeuesPasswort anfrage) {
        konten.passwortSetzen(akteur.id(), id, anfrage.passwort(), aenderungsstand);
    }

    @PostMapping("/{id}/eigentuemer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eigentuemerUebergeben(@AuthenticationPrincipal KontoPrincipal akteur,
                                      @PathVariable String id,
                                      @RequestHeader("If-Match") long aenderungsstand) {
        konten.eigentuemerUebergeben(akteur.id(), id, aenderungsstand);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void loeschen(@AuthenticationPrincipal KontoPrincipal akteur, @PathVariable String id,
                         @RequestHeader("If-Match") long aenderungsstand) {
        konten.loeschen(akteur.id(), id, aenderungsstand);
    }

    public record NeuesPasswort(@NotEmpty @Size(max = 1000) String passwort) {
    }
}
