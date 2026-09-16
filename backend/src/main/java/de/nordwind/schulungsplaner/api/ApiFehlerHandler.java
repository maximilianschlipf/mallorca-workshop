package de.nordwind.schulungsplaner.api;

import de.nordwind.schulungsplaner.service.KontoFehler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiFehlerHandler {
    @ExceptionHandler(KontoFehler.class)
    ResponseEntity<ApiFehler> kontoFehler(KontoFehler fehler) {
        return ResponseEntity.status(fehler.status())
                .body(new ApiFehler(fehler.code(), fehler.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiFehler> validierung() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiFehler("UNGUELTIGE_EINGABE", "Bitte prüfen Sie Ihre Eingaben."));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class,
            MissingRequestHeaderException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiFehler> unlesbareEingabe() {
        return ResponseEntity.badRequest()
                .body(new ApiFehler("UNGUELTIGE_EINGABE", "Bitte prüfen Sie Ihre Eingaben."));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiFehler> fachlicherHttpFehler(ResponseStatusException fehler) {
        return ResponseEntity.status(fehler.getStatusCode())
                .body(new ApiFehler("UNGUELTIGE_EINGABE", fehler.getReason()));
    }
}
