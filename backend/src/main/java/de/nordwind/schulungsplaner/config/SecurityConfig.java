package de.nordwind.schulungsplaner.config;

import tools.jackson.databind.ObjectMapper;
import de.nordwind.schulungsplaner.api.ApiFehler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AktuellesKontoFilter kontoFilter,
                                            ObjectMapper json) throws Exception {
        CookieCsrfTokenRepository csrf = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrf.setCookiePath("/");
        http
                .csrf(config -> config.csrfTokenRepository(csrf))
                .authorizeHttpRequests(regeln -> regeln
                        .requestMatchers(HttpMethod.GET, "/api/health", "/api/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/registrieren", "/api/auth/anmelden").permitAll()
                        .requestMatchers("/api/benutzerkonten/**").hasRole("ADMINISTRATOR")
                        .requestMatchers("/api/qualifikationsbewerbungen/**").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.POST, "/api/schulungen/**", "/api/kategorien/**")
                                .hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/schulungen/**", "/api/kategorien/**")
                                .hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/schulungen/**", "/api/kategorien/**")
                                .hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.POST, "/api/gruppen/**").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/gruppen/**").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/gruppen/**").hasRole("ADMINISTRATOR")
                        .anyRequest().authenticated())
                .exceptionHandling(fehler -> fehler
                        .authenticationEntryPoint((request, response, ex) ->
                                schreibeFehler(response, json, HttpServletResponse.SC_UNAUTHORIZED,
                                        "ANMELDUNG_ERFORDERLICH", "Bitte melden Sie sich an."))
                        .accessDeniedHandler((request, response, ex) ->
                                schreibeFehler(response, json, HttpServletResponse.SC_FORBIDDEN,
                                        "ZUGRIFF_VERWEIGERT", "Für diese Aktion fehlen die Rechte.")))
                .addFilterAfter(kontoFilter, SecurityContextHolderFilter.class);
        return http.build();
    }

    private static void schreibeFehler(HttpServletResponse response, ObjectMapper json, int status,
                                       String code, String text) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        json.writeValue(response.getOutputStream(), new ApiFehler(code, text));
    }
}
