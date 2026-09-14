package de.nordwind.schulungsplaner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Die Uhr als Bohne.
 *
 * <p>Fristen wie die sechs Monate aus REQ_KAT_LOE_02 haengen am heutigen Tag.
 * Ueber eine eingesetzte Uhr laesst sich dieser Tag im Test festlegen, statt
 * ihn dem Rechner zu ueberlassen.
 */
@Configuration
public class ZeitKonfiguration {

    @Bean
    Clock uhr() {
        return Clock.systemDefaultZone();
    }
}
