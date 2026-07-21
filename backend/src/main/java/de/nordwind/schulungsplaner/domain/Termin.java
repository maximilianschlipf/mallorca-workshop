package de.nordwind.schulungsplaner.domain;

public record Termin(
        String terminId,
        String startdatum,
        String enddatum,
        String ort,
        String format,
        String status,
        String trainerId
) {
}
