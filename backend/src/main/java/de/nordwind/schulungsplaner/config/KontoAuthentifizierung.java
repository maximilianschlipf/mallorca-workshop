package de.nordwind.schulungsplaner.config;

import de.nordwind.schulungsplaner.domain.Benutzerkonto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

@Component
public class KontoAuthentifizierung {
    public void anmelden(HttpServletRequest request, Benutzerkonto konto) {
        if (request.getSession(false) != null) request.changeSessionId();
        setze(request, konto);
    }

    public void aktualisieren(HttpServletRequest request, Benutzerkonto konto) {
        setze(request, konto);
    }

    public UsernamePasswordAuthenticationToken token(Benutzerkonto konto) {
        return UsernamePasswordAuthenticationToken.authenticated(
                KontoPrincipal.from(konto), null,
                konto.rollen().stream()
                        .map(rolle -> new SimpleGrantedAuthority("ROLE_" + rolle.name()))
                        .toList());
    }

    private void setze(HttpServletRequest request, Benutzerkonto konto) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token(konto));
        SecurityContextHolder.setContext(context);
        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }
}
