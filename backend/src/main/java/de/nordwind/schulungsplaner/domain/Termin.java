package de.nordwind.schulungsplaner.domain;

import java.util.List;

public record Termin(
        String terminId,
        String startdatum,
        String enddatum,
        String ort,
        String format,
        String status,
        String trainerId,
        String trainerName,
        List<String> assistenten
) {
}
