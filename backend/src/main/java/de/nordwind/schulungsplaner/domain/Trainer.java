package de.nordwind.schulungsplaner.domain;

import java.util.List;

public record Trainer(
        String id,
        String name,
        String email,
        List<String> qualifikationen,
        List<Abwesenheit> abwesenheiten
) {
}
