package de.nordwind.schulungsplaner;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class SeedResourcesTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldPackageSchulungenSeedInClasspath() throws IOException {
        assertSeedResource("seed/schulungen.json", "schulungen", 8);
    }

    @Test
    void shouldPackageTrainerSeedInClasspath() throws IOException {
        assertSeedResource("seed/trainer.json", "trainer", 5);
    }

    private void assertSeedResource(String path, String arrayField, int expectedSize) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        assertThat(resource.exists()).isTrue();

        try (InputStream input = resource.getInputStream()) {
            JsonNode root = objectMapper.readTree(input);
            assertThat(root.path(arrayField).isArray()).isTrue();
            assertThat(root.path(arrayField).size()).isEqualTo(expectedSize);
        }
    }
}
