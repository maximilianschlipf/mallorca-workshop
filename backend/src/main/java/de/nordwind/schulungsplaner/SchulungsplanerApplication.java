package de.nordwind.schulungsplaner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class SchulungsplanerApplication {

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    public static void main(String[] args) {
        SpringApplication.run(SchulungsplanerApplication.class, args);
    }
}
