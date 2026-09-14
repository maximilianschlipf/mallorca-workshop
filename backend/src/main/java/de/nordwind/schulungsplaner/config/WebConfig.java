package de.nordwind.schulungsplaner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Laesst die Oberflaeche auf die Schnittstelle zugreifen.
 *
 * <p>Die Herkunft bleibt auf den Entwicklungsserver beschraenkt. Zusammen mit
 * REQ_USR_SICHER_01 -- die Anwendung bindet nur an die Loopback-Schnittstelle
 * -- heisst das: Von aussen ist hier ohnehin nichts erreichbar.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String erlaubteHerkunft;

    public WebConfig(
            @Value("${schulungsplaner.cors.herkunft:http://localhost:15173}")
            String erlaubteHerkunft) {
        this.erlaubteHerkunft = erlaubteHerkunft;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:15173", "http://localhost:15174")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                .allowCredentials(true);
    }
}
