package de.nordwind.schulungsplaner;

import de.nordwind.schulungsplaner.domain.Rolle;
import de.nordwind.schulungsplaner.service.KontoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.config.location=file:src/main/resources/application.yml",
        "APP_DEMO_SEED=true",
        "spring.datasource.url=jdbc:h2:mem:seedtest;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"
})
class SeedDataIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private KontoService konten;

    @Test
    void shouldImportCompleteSeedGraph() {
        assertCount("schulung_zustand", 8);
        assertCount("termin", 14);
        assertCount("benutzerkonto", 6);
        assertCount("trainer_qualifikation", 10);
        assertCount("abwesenheit", 5);
    }

    @Test
    void shouldProvideDocumentedWorkshopAccountsForEveryRole() {
        assertThat(konten.anmelden("julia.hoffmann@simplytest-academy.de", "test-passwort").rollen())
                .containsExactlyInAnyOrder(Rolle.TRAINER, Rolle.ADMINISTRATOR, Rolle.EIGENTUEMER);
        assertThat(konten.anmelden("admin@simplytest-academy.de", "test-passwort").rollen())
                .containsExactly(Rolle.ADMINISTRATOR);
        assertThat(konten.anmelden("sophie.bauer@simplytest-academy.de", "test-passwort").rollen())
                .containsExactly(Rolle.TRAINER);

        assertThat(jdbcTemplate.queryForList(
                "SELECT passwort_hash FROM benutzerkonto", String.class))
                .allMatch(hash -> hash.startsWith("$2"))
                .doesNotContain("test-passwort")
                .doesNotHaveDuplicates();
    }

    private void assertCount(String table, int expected) {
        Integer actual = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
        assertThat(actual).isEqualTo(expected);
    }
}
