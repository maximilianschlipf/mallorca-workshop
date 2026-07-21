package de.nordwind.schulungsplaner.domain;

public record Abwesenheit(
        String von,
        String bis,
        String grund
) {
}
