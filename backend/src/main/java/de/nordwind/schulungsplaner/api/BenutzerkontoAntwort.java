package de.nordwind.schulungsplaner.api;

import de.nordwind.schulungsplaner.domain.Benutzerkonto;
import de.nordwind.schulungsplaner.domain.Rolle;

import java.util.Set;

public record BenutzerkontoAntwort(
        String id,
        String email,
        String name,
        long aenderungsstand,
        Set<Rolle> rollen,
        Benutzerkonto.Zustand zustand
) {
    public static BenutzerkontoAntwort from(Benutzerkonto konto) {
        return new BenutzerkontoAntwort(
                konto.id(), konto.email(), konto.name(), konto.aenderungsstand(),
                konto.rollen(), konto.zustand());
    }
}
