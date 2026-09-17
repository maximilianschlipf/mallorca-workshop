package de.nordwind.schulungsplaner.katalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class KatalogArchitekturTest {

    @Autowired
    RequestMappingHandlerMapping mappings;

    // verifies: TEST_KAT_ABL_04
    @Test
    void shouldOfferNoSynchronizationAndKeepTheProductionDatabaseOutsideTheCatalog() throws Exception {
        assertThat(mappings.getHandlerMethods().keySet())
                .flatExtracting(info -> info.getPatternValues())
                .noneMatch(path -> path.contains("abgleich") || path.contains("synchron"));

        var quelle = new YamlPropertySourceLoader().load("production",
                new FileSystemResource("src/main/resources/application.yml")).getFirst();
        String datenbank = (String) quelle.getProperty("spring.datasource.url");
        String katalog = (String) quelle.getProperty("schulungsplaner.katalog.pfad");
        Path datenbankpfad = Path.of(datenbank.substring("jdbc:h2:file:".length()))
                .toAbsolutePath().normalize();
        Path katalogpfad = Path.of(katalog).toAbsolutePath().normalize();

        assertThat(datenbankpfad.startsWith(katalogpfad)).isFalse();
        assertThat(datenbankpfad.startsWith(Path.of("data").toAbsolutePath().normalize())).isTrue();
        assertThat(Files.readAllLines(Path.of(".gitignore"))).contains("data/");
    }
}
