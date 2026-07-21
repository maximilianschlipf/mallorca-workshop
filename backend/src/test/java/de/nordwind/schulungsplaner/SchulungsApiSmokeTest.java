package de.nordwind.schulungsplaner;

import de.nordwind.schulungsplaner.domain.Schulung;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SchulungsApiSmokeTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnSchulungenFromSeedData() {
        ResponseEntity<Schulung[]> response = restTemplate.getForEntity("/api/schulungen", Schulung[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody()[0].titel()).isNotBlank();
    }
}
