package de.nordwind.schulungsplaner.api;

import de.nordwind.schulungsplaner.domain.Schulung;
import de.nordwind.schulungsplaner.katalog.KatalogAnsichtService;
import de.nordwind.schulungsplaner.katalog.KatalogPflegeService;
import de.nordwind.schulungsplaner.katalog.Katalogantwort;
import de.nordwind.schulungsplaner.katalog.Kennungsschema;
import de.nordwind.schulungsplaner.katalog.SchulungId;
import de.nordwind.schulungsplaner.katalog.SchulungNichtGefunden;
import de.nordwind.schulungsplaner.katalog.Schulungseingabe;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/schulungen")
public class SchulungController {

    private final KatalogAnsichtService ansicht;
    private final KatalogPflegeService pflege;

    public SchulungController(KatalogAnsichtService ansicht, KatalogPflegeService pflege) {
        this.ansicht = ansicht;
        this.pflege = pflege;
    }

    @GetMapping
    public List<Schulung> liste(
            @RequestParam(required = false) String suche,
            @RequestParam(required = false) String kategorie) {
        return ansicht.findeSchulungen(suche, kategorie);
    }

    /**
     * Das Benennungsschema der bestehenden Schulungen (REQ_KAT_ID_01).
     *
     * <p>Steht vor {@code /{id}}, damit "id-schema" nicht als Kennung
     * gelesen wird.
     */
    @GetMapping("/id-schema")
    public Kennungsschema kennungsschema() {
        return ansicht.kennungsschema();
    }

    @GetMapping("/{id}")
    public Schulung eine(@PathVariable String id) {
        SchulungId kennung = kennung(id);
        return ansicht.findeSchulung(kennung)
                .orElseThrow(() -> new SchulungNichtGefunden(kennung));
    }

    @PostMapping
    public ResponseEntity<Katalogantwort> anlegen(@RequestBody Schulungseingabe eingabe) {
        Katalogantwort antwort = pflege.legeAn(eingabe);
        return ResponseEntity
                .created(URI.create("/api/schulungen/" + antwort.schulung().id()))
                .body(antwort);
    }

    @PutMapping("/{id}")
    public Katalogantwort aendern(@PathVariable String id,
                                  @RequestBody Schulungseingabe eingabe) {
        return pflege.aendere(kennung(id), eingabe);
    }

    /**
     * Eine Kennung, die dem Alphabet nicht genuegt, kann es im Katalog nicht
     * geben -- sie wird wie eine unbekannte Schulung behandelt und nicht als
     * Eingabefehler, denn sie steht im Pfad und nicht im Formular.
     */
    private static SchulungId kennung(String id) {
        if (!SchulungId.istGueltig(id)) {
            throw new SchulungNichtGefunden(SchulungId.von("SCH-000"));
        }
        return SchulungId.von(id);
    }
}
