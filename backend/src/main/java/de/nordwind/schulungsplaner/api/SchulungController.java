package de.nordwind.schulungsplaner.api;

import de.nordwind.schulungsplaner.domain.Schulung;
import de.nordwind.schulungsplaner.domain.VerfuegbarerTrainer;
import de.nordwind.schulungsplaner.service.SchulungsQueryService;
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
@RequestMapping("/api")
public class SchulungController {

    private final SchulungsQueryService queryService;

    public SchulungController(SchulungsQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/schulungen")
    public List<Schulung> listSchulungen(
            @RequestParam(required = false) String suche,
            @RequestParam(required = false) String kategorie) {
        return queryService.findSchulungen(suche, kategorie);
    }

    @GetMapping("/kategorien")
    public List<String> listKategorien() {
        return queryService.findKategorien();
    }

    @GetMapping("/trainer/verfuegbar")
    public List<VerfuegbarerTrainer> listVerfuegbareTrainer(
            @RequestParam String schulungId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate von,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bis) {
        if (von.isAfter(bis)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Das Anfangsdatum darf nicht nach dem Enddatum liegen."
            );
        }
        return queryService.findVerfuegbareTrainer(schulungId, von, bis);
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("ok");
    }

    public record HealthResponse(String status) {
    }
}
