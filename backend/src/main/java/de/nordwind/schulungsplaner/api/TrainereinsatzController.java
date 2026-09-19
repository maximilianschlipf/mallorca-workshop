package de.nordwind.schulungsplaner.api;

import de.nordwind.schulungsplaner.config.KontoPrincipal;
import de.nordwind.schulungsplaner.service.TrainereinsatzService;
import de.nordwind.schulungsplaner.service.TerminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TrainereinsatzController {
    private final TrainereinsatzService einsaetze;
    private final TerminService termine;

    public TrainereinsatzController(TrainereinsatzService einsaetze, TerminService termine) {
        this.einsaetze = einsaetze;
        this.termine = termine;
    }

    @PostMapping("/ich/qualifikationsbewerbungen/{schulungId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void aufQualifikationBewerben(@AuthenticationPrincipal KontoPrincipal konto,
                                         @PathVariable String schulungId) {
        einsaetze.aufQualifikationBewerben(konto.id(), schulungId);
    }

    @DeleteMapping("/ich/qualifikationsbewerbungen/{schulungId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bewerbungZurueckziehen(@AuthenticationPrincipal KontoPrincipal konto,
                                       @PathVariable String schulungId) {
        einsaetze.bewerbungZurueckziehen(konto.id(), schulungId);
    }

    @GetMapping("/ich/qualifikationen")
    public List<TrainereinsatzService.EigenerQualifikationsstand> meineQualifikationen(
            @AuthenticationPrincipal KontoPrincipal konto) {
        return einsaetze.meineQualifikationen(konto.id());
    }

    @DeleteMapping("/ich/qualifikationen/{schulungId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eigeneQualifikationAblegen(@AuthenticationPrincipal KontoPrincipal konto,
                                            @PathVariable String schulungId) {
        einsaetze.eigeneQualifikationAblegen(konto.id(), schulungId);
    }

    @GetMapping("/ich/benachrichtigungen")
    public List<TrainereinsatzService.Benachrichtigung> meineBenachrichtigungen(
            @AuthenticationPrincipal KontoPrincipal konto) {
        return einsaetze.meineBenachrichtigungen(konto.id());
    }

    @GetMapping("/schulungen/{schulungId}/qualifikationen")
    public List<TrainereinsatzService.Qualifikationszeile> qualifikationenDerSchulung(
            @AuthenticationPrincipal KontoPrincipal konto, @PathVariable String schulungId) {
        return einsaetze.qualifikationenDerSchulung(konto.id(), schulungId);
    }

    @PutMapping("/schulungen/{schulungId}/qualifikationen/{trainerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void direktQualifizieren(@AuthenticationPrincipal KontoPrincipal konto,
                                    @PathVariable String schulungId,
                                    @PathVariable String trainerId) {
        einsaetze.direktQualifizieren(konto.id(), schulungId, trainerId);
    }

    @DeleteMapping("/schulungen/{schulungId}/qualifikationen/{trainerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void qualifikationEntziehen(@AuthenticationPrincipal KontoPrincipal konto,
                                       @PathVariable String schulungId,
                                       @PathVariable String trainerId) {
        einsaetze.qualifikationEntziehen(konto.id(), schulungId, trainerId);
    }

    @PostMapping("/qualifikationsbewerbungen/{bewerbungId}/genehmigung")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bewerbungGenehmigen(@AuthenticationPrincipal KontoPrincipal konto,
                                     @PathVariable long bewerbungId) {
        einsaetze.bewerbungGenehmigen(konto.id(), bewerbungId);
    }

    @PostMapping("/qualifikationsbewerbungen/{bewerbungId}/ablehnung")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bewerbungAblehnen(@AuthenticationPrincipal KontoPrincipal konto,
                                  @PathVariable long bewerbungId,
                                  @Valid @RequestBody Ablehnung anfrage) {
        einsaetze.bewerbungAblehnen(konto.id(), bewerbungId, anfrage.begruendung());
    }

    @PostMapping("/ich/assistenzbewerbungen/{terminId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void aufAssistenzplatzBewerben(@AuthenticationPrincipal KontoPrincipal konto,
                                          @PathVariable String terminId) {
        einsaetze.aufAssistenzplatzBewerben(konto.id(), terminId);
    }

    @PostMapping("/ich/abwesenheiten")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void abwesenheitEintragen(@AuthenticationPrincipal KontoPrincipal konto,
                                     @Valid @RequestBody NeueAbwesenheit anfrage) {
        einsaetze.abwesenheitEintragen(konto.id(), anfrage.von(), anfrage.bis(), anfrage.grund());
    }

    @GetMapping("/ich/termine")
    public List<TrainereinsatzService.TrainerTermin> meineTermine(
            @AuthenticationPrincipal KontoPrincipal konto) {
        return einsaetze.meineTermine(konto.id());
    }

    @PutMapping("/termine/{terminId}/trainer/{trainerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void trainerZuweisen(@AuthenticationPrincipal KontoPrincipal konto,
                                @PathVariable String terminId, @PathVariable String trainerId,
                                @RequestParam(defaultValue = "false") boolean rollenwechselBestaetigt) {
        termine.trainerZuweisen(konto.id(), terminId, trainerId, rollenwechselBestaetigt);
    }

    @DeleteMapping("/termine/{terminId}/trainer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void trainerAbziehen(@AuthenticationPrincipal KontoPrincipal konto,
                                @PathVariable String terminId) {
        termine.trainerAbziehen(konto.id(), terminId);
    }

    @PutMapping("/termine/{terminId}/assistenten/{trainerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assistentZuweisen(@AuthenticationPrincipal KontoPrincipal konto,
                                  @PathVariable String terminId, @PathVariable String trainerId) {
        termine.assistentZuweisen(konto.id(), terminId, trainerId);
    }

    public record NeueAbwesenheit(
            @NotNull LocalDate von,
            @NotNull LocalDate bis,
            @Size(max = 255) String grund) {
    }

    public record Ablehnung(@NotNull @Size(min = 1, max = 1000) String begruendung) {}
}
