package de.nordwind.schulungsplaner.config;

import de.nordwind.schulungsplaner.service.SeedService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeedInitializationConfig {

    @Bean
    ApplicationRunner initializeSeedData(SeedService seedService) {
        return args -> seedService.resetAndSeed();
    }
}
