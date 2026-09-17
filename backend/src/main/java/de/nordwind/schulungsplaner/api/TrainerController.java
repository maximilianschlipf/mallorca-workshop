package de.nordwind.schulungsplaner.api;

import de.nordwind.schulungsplaner.domain.VerfuegbarerTrainer;
import de.nordwind.schulungsplaner.service.TrainerQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainer")
public class TrainerController {

    private final TrainerQueryService trainer;

    public TrainerController(TrainerQueryService trainer) {
        this.trainer = trainer;
    }

    @GetMapping("/verfuegbar")
    public List<VerfuegbarerTrainer> verfuegbar(
            @RequestParam String schulungId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate von,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bis) {
        if (von.isAfter(bis)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Das Anfangsdatum darf nicht nach dem Enddatum liegen.");
        }
        return trainer.findeVerfuegbareTrainer(schulungId, von, bis);
    }
}
