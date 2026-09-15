package de.nordwind.schulungsplaner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class SchulungsplanerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchulungsplanerApplication.class, args);
    }
}
