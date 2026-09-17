package de.nordwind.schulungsplaner.api;

import de.nordwind.schulungsplaner.domain.Schulung;
import de.nordwind.schulungsplaner.katalog.KatalogAnsichtService;
import de.nordwind.schulungsplaner.katalog.KatalogPflegeService;
import de.nordwind.schulungsplaner.katalog.Aufnahmebericht;
import de.nordwind.schulungsplaner.katalog.Katalogantwort;
import de.nordwind.schulungsplaner.katalog.SchulungAufnahmeService;
import de.nordwind.schulungsplaner.katalog.Kennungsschema;
import de.nordwind.schulungsplaner.katalog.SchulungId;
import de.nordwind.schulungsplaner.katalog.SchulungNichtGefunden;
import de.nordwind.schulungsplaner.katalog.Schulungseingabe;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/schulungen")
public class SchulungController {

    private final KatalogAnsichtService ansicht;
    private final KatalogPflegeService pflege;
    private final SchulungAufnahmeService aufnahme;

    public SchulungController(KatalogAnsichtService ansicht,
                              KatalogPflegeService pflege,
                              SchulungAufnahmeService aufnahme) {
        this.ansicht = ansicht;
        this.pflege = pflege;
        this.aufnahme = aufnahme;
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
     * Nimmt bereitgestellte JSON-Dateien in den Katalog auf (REQ_KAT_IMP_01).
     *
     * <p>Antwortet immer mit 200 und einem Bericht je Datei. Ein Stapel, in
     * dem eine Datei abgewiesen wurde, ist keine fehlerhafte Anfrage -- er
     * hat ein gemischtes Ergebnis, und das gehoert in den Koerper.
     *
     * @param ersetzen die ausdrueckliche Entscheidung aus REQ_KAT_IMP_03
     */
    @PostMapping("/aufnahme")
    public Aufnahmebericht aufnehmen(
            @RequestPart("dateien") List<MultipartFile> dateien,
            @RequestParam(defaultValue = "false") boolean ersetzen) {
        List<SchulungAufnahmeService.Aufzunehmen> aufzunehmen = new ArrayList<>();
        for (MultipartFile datei : dateien) {
            aufzunehmen.add(new SchulungAufnahmeService.Aufzunehmen(
                    datei.getOriginalFilename(), lies(datei)));
        }
        return aufnahme.nimmAuf(aufzunehmen, ersetzen);
    }

    private static String lies(MultipartFile datei) {
        try {
            return new String(datei.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException(
                    "Die Datei '" + datei.getOriginalFilename() + "' ließ sich nicht lesen.",
                    ex);
        }
    }

    /** Archiviert eine Schulung (REQ_KAT_ARCH_01). */
    @PostMapping("/{id}/archivierung")
    public Katalogantwort archivieren(@PathVariable String id) {
        return pflege.archiviere(kennung(id));
    }

    /**
     * Nimmt die Archivierung zurueck (REQ_KAT_ARCH_04). Als Wegnahme der
     * Archivierung ausgedrueckt, nicht als eigener Vorgang -- es ist
     * derselbe Schalter in der Gegenrichtung.
     */
    @DeleteMapping("/{id}/archivierung")
    public Katalogantwort reaktivieren(@PathVariable String id) {
        return pflege.reaktiviere(kennung(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> loeschen(@PathVariable String id) {
        pflege.loesche(kennung(id));
        return ResponseEntity.noContent().build();
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
