package de.nordwind.schulungsplaner.config;

import de.nordwind.schulungsplaner.domain.Benutzerkonto;

public record KontoPrincipal(String id, long passwortVersion) {
    public static KontoPrincipal from(Benutzerkonto konto) {
        return new KontoPrincipal(konto.id(), konto.passwortVersion());
    }
}
