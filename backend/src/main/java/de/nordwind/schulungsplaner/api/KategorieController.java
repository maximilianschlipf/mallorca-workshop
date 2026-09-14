package de.nordwind.schulungsplaner.api;

import de.nordwind.schulungsplaner.katalog.KatalogAnsichtService;
import de.nordwind.schulungsplaner.katalog.KategoriePflegeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Die Kategorienliste (REQ_KAT_KATG_01).
 *
 * <p>Der Name steht in Koerper oder Abfrage, nicht im Pfad: Kategorien sind
 * Freitext und enthalten Zeichen wie "&amp;" und Leerraum, die im Pfad nur
 * mit Kodierung ueberleben -- ein Schraegstrich gar nicht.
 */
@RestController
@RequestMapping("/api/kategorien")
public class KategorieController {

    private final KatalogAnsichtService ansicht;
    private final KategoriePflegeService pflege;

    public KategorieController(KatalogAnsichtService ansicht, KategoriePflegeService pflege) {
        this.ansicht = ansicht;
        this.pflege = pflege;
    }

    @GetMapping
    public List<String> liste() {
        return ansicht.findeKategorien();
    }

    @PostMapping
    public ResponseEntity<List<String>> anlegen(@RequestBody Kategorieeingabe eingabe) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pflege.legeAn(eingabe.name()));
    }

    @PutMapping
    public List<String> umbenennen(@RequestBody Kategorieeingabe eingabe) {
        return pflege.benenneUm(eingabe.name(), eingabe.neuerName());
    }

    @DeleteMapping
    public ResponseEntity<Void> loeschen(@RequestParam String name) {
        pflege.loesche(name);
        return ResponseEntity.noContent().build();
    }

    public record Kategorieeingabe(String name, String neuerName) {
    }
}
