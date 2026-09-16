package de.nordwind.schulungsplaner.domain;

import java.util.Set;

public record Benutzerkonto(
        String id,
        String email,
        String name,
        String passwortHash,
        boolean aktiv,
        long aenderungsstand,
        long passwortVersion,
        Set<Rolle> rollen
) {
    public Zustand zustand() {
        return aktiv ? Zustand.AKTIV : Zustand.STILLGELEGT;
    }

    public enum Zustand { AKTIV, STILLGELEGT }
}
