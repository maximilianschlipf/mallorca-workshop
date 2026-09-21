package de.nordwind.schulungsplaner.api;

import de.nordwind.schulungsplaner.config.KontoPrincipal;
import de.nordwind.schulungsplaner.service.GruppeService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gruppen")
public class GruppeController {
    private final GruppeService gruppen;

    public GruppeController(GruppeService gruppen) {
        this.gruppen = gruppen;
    }

    @GetMapping
    public List<GruppeService.GruppenAnsicht> alle(@AuthenticationPrincipal KontoPrincipal konto) {
        return gruppen.alle(konto.id());
    }

    @GetMapping("/{gruppeId}")
    public GruppeService.GruppenAnsicht details(@AuthenticationPrincipal KontoPrincipal konto,
                                                @PathVariable String gruppeId) {
        return gruppen.details(konto.id(), gruppeId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GruppeService.GruppenAnsicht anlegen(@AuthenticationPrincipal KontoPrincipal konto,
                                                @Valid @RequestBody GruppenAnfrage anfrage) {
        return gruppen.anlegen(konto.id(),
                new GruppeService.GruppenEingabe(anfrage.name(), anfrage.trainerId(), anfrage.mitgliederIds()));
    }

    @PutMapping("/{gruppeId}")
    public GruppeService.GruppenAnsicht aendern(@AuthenticationPrincipal KontoPrincipal konto,
                                                @PathVariable String gruppeId,
                                                @Valid @RequestBody GruppenAnfrage anfrage) {
        return gruppen.aendern(konto.id(), gruppeId, anfrage.name(), anfrage.trainerId());
    }

    @PutMapping("/{gruppeId}/mitglieder/{kontoId}")
    public GruppeService.GruppenAnsicht mitgliedHinzufuegen(@AuthenticationPrincipal KontoPrincipal konto,
                                                            @PathVariable String gruppeId,
                                                            @PathVariable String kontoId) {
        return gruppen.mitgliedHinzufuegen(konto.id(), gruppeId, kontoId);
    }

    @DeleteMapping("/{gruppeId}/mitglieder/{kontoId}")
    public GruppeService.GruppenAnsicht mitgliedEntfernen(@AuthenticationPrincipal KontoPrincipal konto,
                                                          @PathVariable String gruppeId,
                                                          @PathVariable String kontoId) {
        return gruppen.mitgliedEntfernen(konto.id(), gruppeId, kontoId);
    }

    @DeleteMapping("/{gruppeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void loeschen(@AuthenticationPrincipal KontoPrincipal konto, @PathVariable String gruppeId) {
        gruppen.loeschen(konto.id(), gruppeId);
    }

    public record GruppenAnfrage(@Size(max = 255) String name, String trainerId, List<String> mitgliederIds) {}
}
