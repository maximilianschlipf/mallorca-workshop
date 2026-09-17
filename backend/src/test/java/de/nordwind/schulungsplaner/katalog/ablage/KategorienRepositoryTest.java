package de.nordwind.schulungsplaner.katalog.ablage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * REQ_KAT_KATG_05: Die Kategorienliste ist eine eigene JSON-Datei im
 * Katalogverzeichnis, nicht aus den Schulungen abgeleitet. REQ_KAT_SUCH_06
 * verlangt sie doppelfrei und alphabetisch sortiert.
 */
class KategorienRepositoryTest {

    @TempDir
    Path katalogwurzel;

    private KategorienRepository repository;

    @BeforeEach
    void setUp() {
        repository = new KategorienRepository(new KatalogPfade(katalogwurzel));
    }

    @Test
    void shouldStoreCategoriesInASingleFileInTheCatalogDirectory() {
        repository.speichere(List.of("Cloud & DevOps", "Agile & Projektmanagement"));

        assertThat(katalogwurzel.resolve("kategorien.json")).exists();
    }

    /** REQ_KAT_SUCH_06. */
    @Test
    void shouldKeepCategoriesDistinctAndAlphabeticallySorted() {
        repository.speichere(List.of(
                "Cloud & DevOps", "Agile & Projektmanagement", "Cloud & DevOps"));

        assertThat(repository.ladeAlle())
                .containsExactly("Agile & Projektmanagement", "Cloud & DevOps");
    }

    @Test
    void shouldWriteHumanReadableJsonWithTrailingNewline() throws IOException {
        repository.speichere(List.of("Cloud & DevOps"));

        String inhalt = Files.readString(katalogwurzel.resolve("kategorien.json"));

        assertThat(inhalt).isEqualTo("""
                {
                  "kategorien" : [ "Cloud & DevOps" ]
                }
                """);
    }

    @Test
    void shouldReturnAnEmptyListBeforeTheFileExists() {
        assertThat(repository.ladeAlle()).isEmpty();
        assertThat(repository.kennt("Cloud & DevOps")).isFalse();
    }

    /** REQ_KAT_KATG_02: Die Kategorie wird ausgewaehlt, der Filter trifft genau. */
    @Test
    void shouldMatchCategoryNamesExactly() {
        repository.speichere(List.of("Cloud & DevOps"));

        assertThat(repository.kennt("Cloud & DevOps")).isTrue();
        assertThat(repository.kennt("cloud & devops")).isFalse();
        assertThat(repository.kennt("Cloud")).isFalse();
    }

    @Test
    void shouldFailLoudlyOnUnreadableJsonNamingTheFile() throws IOException {
        Files.createDirectories(katalogwurzel);
        Files.writeString(katalogwurzel.resolve("kategorien.json"), "{ kaputt");

        assertThatThrownBy(() -> repository.ladeAlle())
                .isInstanceOf(KatalogLesefehler.class)
                .hasMessageContaining("kategorien.json");
    }
}
