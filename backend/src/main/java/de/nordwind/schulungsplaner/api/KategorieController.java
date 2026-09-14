package de.nordwind.schulungsplaner.api;

import de.nordwind.schulungsplaner.katalog.KatalogAnsichtService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/kategorien")
public class KategorieController {

    private final KatalogAnsichtService ansicht;

    public KategorieController(KatalogAnsichtService ansicht) {
        this.ansicht = ansicht;
    }

    @GetMapping
    public List<String> liste() {
        return ansicht.findeKategorien();
    }
}
