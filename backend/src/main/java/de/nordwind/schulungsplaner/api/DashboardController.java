package de.nordwind.schulungsplaner.api;

import de.nordwind.schulungsplaner.config.KontoPrincipal;
import de.nordwind.schulungsplaner.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import de.nordwind.schulungsplaner.service.VorgangService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboard;
    private final VorgangService vorgaenge;

    public DashboardController(DashboardService dashboard, VorgangService vorgaenge) {
        this.dashboard = dashboard;
        this.vorgaenge = vorgaenge;
    }

    @GetMapping
    public DashboardService.Dashboard anzeigen(@AuthenticationPrincipal KontoPrincipal konto) {
        return dashboard.anzeigen(konto.id());
    }

    @PostMapping("/vorgaenge/{id}/annahme")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void annehmen(@AuthenticationPrincipal KontoPrincipal konto, @PathVariable long id) {
        vorgaenge.entscheiden(konto.id(), id, true, null);
    }

    @PostMapping("/vorgaenge/{id}/ablehnung")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ablehnen(@AuthenticationPrincipal KontoPrincipal konto, @PathVariable long id,
                         @RequestBody(required = false) Ablehnung anfrage) {
        vorgaenge.entscheiden(konto.id(), id, false, anfrage == null ? null : anfrage.begruendung());
    }

    @DeleteMapping("/vorgaenge/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void zurueckziehen(@AuthenticationPrincipal KontoPrincipal konto, @PathVariable long id) {
        vorgaenge.zurueckziehen(konto.id(), id);
    }

    public record Ablehnung(String begruendung) {}
}
