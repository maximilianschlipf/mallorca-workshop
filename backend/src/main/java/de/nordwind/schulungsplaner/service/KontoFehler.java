package de.nordwind.schulungsplaner.service;

import org.springframework.http.HttpStatus;

public class KontoFehler extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public KontoFehler(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public String code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }
}
