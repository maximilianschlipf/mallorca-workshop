package de.nordwind.schulungsplaner.api;

import de.nordwind.schulungsplaner.katalog.KatalogAnsichtService;
import de.nordwind.schulungsplaner.katalog.VerwaisterTermin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Auskuenfte ueber den Zustand des Katalogs selbst. */
@RestController
@RequestMapping("/api/katalog")
public class KatalogController {

    private final KatalogAnsichtService ansicht;

    public KatalogController(KatalogAnsichtService ansicht) {
        this.ansicht = ansicht;
    }

    /**
     * Termine, deren Schulung es im Katalog nicht gibt (REQ_KAT_TERM_02).
     *
     * <p>Der Fall entsteht ausserhalb der Anwendung -- etwa wenn eine
     * Katalogdatei beim Abgleich ueber das Repository verschwindet
     * (REQ_KAT_ABL_04). Er wird gemeldet, statt den Termin stillschweigend
     * ohne Schulungsdaten anzuzeigen.
     */
    @GetMapping("/verwaiste-termine")
    public List<VerwaisterTermin> verwaisteTermine() {
        return ansicht.verwaisteTermine();
    }
}
