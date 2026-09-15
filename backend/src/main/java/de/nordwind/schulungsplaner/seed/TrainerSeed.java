package de.nordwind.schulungsplaner.seed;

import de.nordwind.schulungsplaner.domain.Abwesenheit;

import java.util.List;

public record TrainerSeed(
        String id,
        String name,
        String email,
        List<String> qualifikationen,
        List<Abwesenheit> abwesenheiten
) {
}
