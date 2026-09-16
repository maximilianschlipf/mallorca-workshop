package de.nordwind.schulungsplaner.config;

import de.nordwind.schulungsplaner.domain.Benutzerkonto;
import de.nordwind.schulungsplaner.service.KontoService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class AktuellesKontoFilter extends OncePerRequestFilter {
    private final KontoService konten;
    private final KontoAuthentifizierung authentifizierung;

    public AktuellesKontoFilter(KontoService konten, KontoAuthentifizierung authentifizierung) {
        this.konten = konten;
        this.authentifizierung = authentifizierung;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof KontoPrincipal principal) {
            Optional<Benutzerkonto> konto = konten.finden(principal.id());
            if (konto.isEmpty() || !konto.get().aktiv()
                    || konto.get().passwortVersion() != principal.passwortVersion()) {
                SecurityContextHolder.clearContext();
                if (request.getSession(false) != null) request.getSession(false).invalidate();
            } else {
                authentifizierung.aktualisieren(request, konto.get());
            }
        }
        filterChain.doFilter(request, response);
    }
}
