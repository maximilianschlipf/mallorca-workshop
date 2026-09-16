package de.nordwind.schulungsplaner.api;

import de.nordwind.schulungsplaner.config.KontoAuthentifizierung;
import de.nordwind.schulungsplaner.config.KontoPrincipal;
import de.nordwind.schulungsplaner.domain.Benutzerkonto;
import de.nordwind.schulungsplaner.service.KontoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final KontoService konten;
    private final KontoAuthentifizierung authentifizierung;

    public AuthController(KontoService konten, KontoAuthentifizierung authentifizierung) {
        this.konten = konten;
        this.authentifizierung = authentifizierung;
    }

    @GetMapping("/auth/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken());
    }

    @PostMapping("/auth/registrieren")
    @ResponseStatus(HttpStatus.CREATED)
    public BenutzerkontoAntwort registrieren(@Valid @RequestBody Registrierung anfrage) {
        return BenutzerkontoAntwort.from(
                konten.registrieren(anfrage.name(), anfrage.email(), anfrage.passwort()));
    }

    @PostMapping("/auth/anmelden")
    public BenutzerkontoAntwort anmelden(@Valid @RequestBody Anmeldung anfrage,
                                         HttpServletRequest request) {
        Benutzerkonto konto = konten.anmelden(anfrage.email(), anfrage.passwort());
        authentifizierung.anmelden(request, konto);
        return BenutzerkontoAntwort.from(konto);
    }

    @GetMapping("/auth/ich")
    public BenutzerkontoAntwort ich(@AuthenticationPrincipal KontoPrincipal principal) {
        return BenutzerkontoAntwort.from(konten.laden(principal.id()));
    }

    @PostMapping("/auth/abmelden")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void abmelden(HttpServletRequest request) {
        request.getSession().invalidate();
    }

    @PatchMapping("/ich/name")
    public BenutzerkontoAntwort nameAendern(@AuthenticationPrincipal KontoPrincipal principal,
                                             @Valid @RequestBody NeuerName anfrage,
                                             @RequestHeader("If-Match") long aenderungsstand,
                                             HttpServletRequest request) {
        Benutzerkonto konto = konten.nameAendern(principal.id(), anfrage.name(), aenderungsstand);
        authentifizierung.aktualisieren(request, konto);
        return BenutzerkontoAntwort.from(konto);
    }

    @PutMapping("/ich/passwort")
    public BenutzerkontoAntwort passwortAendern(@AuthenticationPrincipal KontoPrincipal principal,
                                                 @Valid @RequestBody PasswortAenderung anfrage,
                                                 @RequestHeader("If-Match") long aenderungsstand,
                                                 HttpServletRequest request) {
        Benutzerkonto konto = konten.passwortAendern(
                principal.id(), anfrage.bisherigesPasswort(), anfrage.neuesPasswort(),
                aenderungsstand);
        authentifizierung.aktualisieren(request, konto);
        return BenutzerkontoAntwort.from(konto);
    }

    public record Registrierung(
            @NotBlank @Size(max = 255) String name,
            @NotBlank @Email @Size(max = 255) String email,
            @NotEmpty @Size(max = 1000) String passwort) {
    }

    public record Anmeldung(@NotBlank @Email String email, @NotEmpty String passwort) {
    }

    public record NeuerName(@NotBlank @Size(max = 255) String name) {
    }

    public record PasswortAenderung(
            @NotEmpty String bisherigesPasswort,
            @NotEmpty @Size(max = 1000) String neuesPasswort) {
    }
}
