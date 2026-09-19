package de.nordwind.schulungsplaner.seed;

import de.nordwind.schulungsplaner.domain.Abwesenheit;
import de.nordwind.schulungsplaner.domain.Rolle;

import java.util.List;

public record KontoSeed(
        String id,
        String name,
        String email,
        List<Rolle> rollen,
        List<String> qualifikationen,
        List<Abwesenheit> abwesenheiten
) {
}
