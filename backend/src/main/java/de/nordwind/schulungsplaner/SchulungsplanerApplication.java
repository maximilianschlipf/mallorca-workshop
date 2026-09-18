package de.nordwind.schulungsplaner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableScheduling
public class SchulungsplanerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchulungsplanerApplication.class, args);
    }
}
