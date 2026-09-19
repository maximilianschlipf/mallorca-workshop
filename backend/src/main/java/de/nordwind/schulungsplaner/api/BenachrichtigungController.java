package de.nordwind.schulungsplaner.api;

import de.nordwind.schulungsplaner.config.KontoPrincipal;
import de.nordwind.schulungsplaner.service.BenachrichtigungService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ich/benachrichtigungen")
public class BenachrichtigungController {
    private final BenachrichtigungService benachrichtigungen;

    public BenachrichtigungController(BenachrichtigungService benachrichtigungen) {
        this.benachrichtigungen = benachrichtigungen;
    }

    @GetMapping
    public List<BenachrichtigungService.Benachrichtigung> anzeigen(
            @AuthenticationPrincipal KontoPrincipal konto) {
        return benachrichtigungen.anzeigen(konto.id());
    }

    @GetMapping("/ungelesen")
    public Map<String, Integer> ungelesen(@AuthenticationPrincipal KontoPrincipal konto) {
        return Map.of("anzahl", benachrichtigungen.ungelesen(konto.id()));
    }

    @PostMapping("/gelesen")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void allesAlsGelesen(@AuthenticationPrincipal KontoPrincipal konto) {
        benachrichtigungen.allesAlsGelesen(konto.id());
    }

    @PostMapping("/{id}/gelesen")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alsGelesen(@AuthenticationPrincipal KontoPrincipal konto, @PathVariable long id) {
        benachrichtigungen.alsGelesen(konto.id(), id);
    }
}
