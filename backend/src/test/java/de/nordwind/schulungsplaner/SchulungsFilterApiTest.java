package de.nordwind.schulungsplaner;

import de.nordwind.schulungsplaner.domain.Schulung;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SchulungsFilterApiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnAllSchulungenWithoutParams() {
        Schulung[] all = get(schulungenUri(null, null));
        Schulung[] alsoAll = get(schulungenUri("", ""));

        assertThat(all).isNotEmpty();
        assertThat(alsoAll).hasSameSizeAs(all);
    }

    @Test
    void shouldSearchCaseInsensitiveAndTrimmed() {
        Schulung[] result = get(schulungenUri("  scRUM  ", null));

        assertThat(result).isNotEmpty();
        assertThat(result).allSatisfy(s ->
                assertThat(s.titel().toLowerCase()).contains("scrum"));
    }

    @Test
    void shouldFilterByKategorie() {
        Schulung[] result = get(schulungenUri(null, "Cloud & DevOps"));

        assertThat(result).isNotEmpty();
        assertThat(result).allSatisfy(s ->
                assertThat(s.kategorie()).isEqualTo("Cloud & DevOps"));
    }

    @Test
    void shouldCombineSucheAndKategorie() {
        Schulung[] result = get(schulungenUri("kubernetes", "Cloud & DevOps"));

        assertThat(result).isNotEmpty();
        assertThat(result).allSatisfy(s -> {
            assertThat(s.titel().toLowerCase()).contains("kubernetes");
            assertThat(s.kategorie()).isEqualTo("Cloud & DevOps");
        });
    }

    @Test
    void shouldReturnEmptyArrayWhenNoMatch() {
        ResponseEntity<Schulung[]> response =
                restTemplate.getForEntity(schulungenUri("gibtesnicht123", null), Schulung[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void shouldReturnDistinctSortedKategorien() {
        ResponseEntity<String[]> response =
                restTemplate.getForEntity("/api/kategorien", String[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody()).doesNotHaveDuplicates();
        assertThat(response.getBody()).isSortedAccordingTo(String::compareTo);
    }

    private Schulung[] get(URI uri) {
        ResponseEntity<Schulung[]> response = restTemplate.getForEntity(uri, Schulung[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private static URI schulungenUri(String suche, String kategorie) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/schulungen");
        if (suche != null) {
            builder.queryParam("suche", suche);
        }
        if (kategorie != null) {
            builder.queryParam("kategorie", kategorie);
        }
        return builder.build().encode().toUri();
    }
}
