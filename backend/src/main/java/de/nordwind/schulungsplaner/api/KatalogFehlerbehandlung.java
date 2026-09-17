package de.nordwind.schulungsplaner.api;

import de.nordwind.schulungsplaner.katalog.Feldfehler;
import de.nordwind.schulungsplaner.katalog.KatalogFehler;
import de.nordwind.schulungsplaner.katalog.SchulungNichtGefunden;
import de.nordwind.schulungsplaner.katalog.ablage.KatalogLesefehler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Uebersetzt die Fehler des Katalogs in Antworten.
 *
 * <p>Der Koerper nennt je Feld den Grund. REQ_KAT_IMP_02 verlangt das
 * ausdruecklich -- ein nacktes 400 sagt dem Administrator nicht, woran es lag.
 */
@RestControllerAdvice
public class KatalogFehlerbehandlung {

    @ExceptionHandler(KatalogFehler.class)
    ResponseEntity<Fehlerantwort> abgewiesen(KatalogFehler fehler) {
        // Ein Konflikt mit dem Bestand ist keine fehlerhafte Eingabe: Die
        // Angaben sind stimmig, nur der Bestand steht entgegen.
        HttpStatus status = fehler.istKonflikt() ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(new Fehlerantwort(fehler.fehler()));
    }

    @ExceptionHandler(SchulungNichtGefunden.class)
    ResponseEntity<Fehlerantwort> nichtGefunden(SchulungNichtGefunden fehler) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new Fehlerantwort(List.of(new Feldfehler("id", null, fehler.getMessage()))));
    }

    /**
     * REQ_KAT_TERM_02 in seiner allgemeinen Form: Eine unlesbare Katalogdatei
     * wird gemeldet, statt als leerer Katalog durchzugehen.
     */
    @ExceptionHandler(KatalogLesefehler.class)
    ResponseEntity<Fehlerantwort> unlesbar(KatalogLesefehler fehler) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Fehlerantwort(List.of(
                        new Feldfehler(null, null, fehler.getMessage()))));
    }

    public record Fehlerantwort(List<Feldfehler> fehler) {
    }
}
