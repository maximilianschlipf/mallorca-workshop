package de.nordwind.schulungsplaner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SeedDataIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldImportCompleteSeedGraph() {
        assertCount("schulung", 8);
        assertCount("termin", 14);
        assertCount("voraussetzung", 8);
        assertCount("trainer", 5);
        assertCount("trainer_qualifikation", 10);
        assertCount("abwesenheit", 5);
    }

    private void assertCount(String table, int expected) {
        Integer actual = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
        assertThat(actual).isEqualTo(expected);
    }
}
