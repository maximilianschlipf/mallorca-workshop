package de.nordwind.schulungsplaner.config;

import de.nordwind.schulungsplaner.katalog.ablage.JGitCommitter;
import de.nordwind.schulungsplaner.katalog.ablage.KatalogCommitter;
import de.nordwind.schulungsplaner.katalog.ablage.KatalogPfade;
import de.nordwind.schulungsplaner.katalog.ablage.KatalogRepository;
import de.nordwind.schulungsplaner.katalog.ablage.KategorienRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

/**
 * Bindet die Katalogablage an (REQ_KAT_ABL_02).
 *
 * <p>Der Ablageort ist konfigurierbar, weil Tests auf ein temporaeres
 * Verzeichnis zeigen muessen -- sonst schriebe jeder Testlauf in das
 * Repository des Projekts.
 */
@Configuration
public class KatalogKonfiguration {

    @Bean
    KatalogPfade katalogPfade(@Value("${schulungsplaner.katalog.pfad}") String pfad) {
        return new KatalogPfade(Path.of(pfad));
    }

    @Bean
    KatalogRepository katalogRepository(KatalogPfade pfade) {
        return new KatalogRepository(pfade);
    }

    @Bean
    KategorienRepository kategorienRepository(KatalogPfade pfade) {
        return new KategorienRepository(pfade);
    }

    @Bean
    KatalogCommitter katalogCommitter(
            KatalogPfade pfade,
            @Value("${schulungsplaner.katalog.commit.name}") String name,
            @Value("${schulungsplaner.katalog.commit.email}") String email) {
        return new JGitCommitter(pfade.wurzel(), name, email);
    }
}
