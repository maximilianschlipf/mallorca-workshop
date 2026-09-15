package de.nordwind.schulungsplaner.api;

import de.nordwind.schulungsplaner.config.KontoPrincipal;
import de.nordwind.schulungsplaner.service.TrainereinsatzService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class TrainereinsatzController {
    private final TrainereinsatzService einsaetze;

    public TrainereinsatzController(TrainereinsatzService einsaetze) {
        this.einsaetze = einsaetze;
    }

    @PostMapping("/ich/qualifikationsbewerbungen/{schulungId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void aufQualifikationBewerben(@AuthenticationPrincipal KontoPrincipal konto,
                                         @PathVariable String schulungId) {
        einsaetze.aufQualifikationBewerben(konto.id(), schulungId);
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

    @PutMapping("/termine/{terminId}/trainer/{trainerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void trainerZuweisen(@AuthenticationPrincipal KontoPrincipal konto,
                                @PathVariable String terminId, @PathVariable String trainerId) {
        einsaetze.trainerZuweisen(konto.id(), terminId, trainerId);
    }

    @PutMapping("/termine/{terminId}/assistenten/{trainerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assistentZuweisen(@AuthenticationPrincipal KontoPrincipal konto,
                                  @PathVariable String terminId, @PathVariable String trainerId) {
        einsaetze.assistentZuweisen(konto.id(), terminId, trainerId);
    }

    public record NeueAbwesenheit(
            @NotNull LocalDate von,
            @NotNull LocalDate bis,
            @Size(max = 255) String grund) {
    }
}
