package de.nordwind.schulungsplaner.api;

import de.nordwind.schulungsplaner.domain.Schulung;
import de.nordwind.schulungsplaner.service.SchulungsQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SchulungController {

    private final SchulungsQueryService queryService;

    public SchulungController(SchulungsQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/schulungen")
    public List<Schulung> listSchulungen() {
        return queryService.findAllSchulungen();
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("ok");
    }

    public record HealthResponse(String status) {
    }
}
