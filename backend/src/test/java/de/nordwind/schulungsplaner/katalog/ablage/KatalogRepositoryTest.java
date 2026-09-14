package de.nordwind.schulungsplaner.katalog.ablage;

import de.nordwind.schulungsplaner.katalog.Katalogschulung;
import de.nordwind.schulungsplaner.katalog.SchulungId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * REQ_KAT_ABL_01 und REQ_KAT_ABL_02: eine JSON-Datei je Schulung an festem
 * Ort. Gegenstuecke in der Anforderungsdoku: TEST_KAT_ABL_01 und
 * TEST_KAT_ANL_07.
 */
class KatalogRepositoryTest {

    @TempDir
    Path katalogwurzel;

    private KatalogRepository repository;

    @BeforeEach
    void setUp() {
        repository = new KatalogRepository(new KatalogPfade(katalogwurzel));
    }

    /** TEST_KAT_ABL_01: genau eine neue Datei, benannt nach der Schulungs-ID. */
    @Test
    void shouldStoreEachSchulungInItsOwnFileNamedAfterTheId() throws IOException {
        repository.speichere(schulung("SCH-009"));

        assertThat(dateien()).containsExactly("SCH-009.json");

        repository.speichere(schulung("SCH-010"));

        assertThat(dateien()).containsExactly("SCH-009.json", "SCH-010.json");
    }

    @Test
    void shouldReturnTheStoredSchulungUnchanged() {
        Katalogschulung original = schulung("SCH-009");

        repository.speichere(original);

        assertThat(repository.lade(SchulungId.von("SCH-009"))).contains(original);
    }

    /**
     * TEST_KAT_ANL_07: Die Datei fuehrt genau die Felder aus REQ_KAT_FELD_01 --
     * weder den Zustand aktiv oder archiviert noch Termine, Trainer oder
     * Teilnehmer.
     */
    @Test
    void shouldWriteExactlyTheFieldsOfTheCatalogAndNothingElse() throws IOException {
        repository.speichere(schulung("SCH-009"));

        JsonNode inhalt = JsonMapper.builder().build()
                .readTree(Files.readString(datei("SCH-009.json")));

        assertThat(inhalt.propertyNames()).containsExactly(
                "id", "titel", "kategorie", "kurzbeschreibung",
                "voraussetzungen", "dauerInTagen",
                "mindestteilnehmerExklusiv", "maxTeilnehmerOeffentlich");
        assertThat(inhalt.get("id").asString()).isEqualTo("SCH-009");
    }

    /**
     * Die Dateien liegen versioniert im Repository und werden von Menschen
     * gelesen und im Diff verglichen. Deshalb eingerueckt, in stabiler
     * Feldreihenfolge und mit abschliessendem Zeilenumbruch.
     */
    @Test
    void shouldWriteHumanReadableJsonWithTrailingNewline() throws IOException {
        repository.speichere(schulung("SCH-009"));

        String inhalt = Files.readString(datei("SCH-009.json"));

        assertThat(inhalt).startsWith("{\n  \"id\" : \"SCH-009\"");
        assertThat(inhalt).endsWith("\n");
        assertThat(inhalt).doesNotContain("\r");
    }

    @Test
    void shouldOverwriteAnExistingFileWhenSavingAgain() throws IOException {
        repository.speichere(schulung("SCH-009"));
        repository.speichere(new Katalogschulung(
                SchulungId.von("SCH-009"), "Neuer Titel", "Cloud & DevOps",
                "Neue Beschreibung.", List.of(), 3, 4, null));

        assertThat(dateien()).containsExactly("SCH-009.json");
        assertThat(repository.lade(SchulungId.von("SCH-009")))
                .get()
                .extracting(Katalogschulung::titel)
                .isEqualTo("Neuer Titel");
    }

    @Test
    void shouldReportAnUnknownSchulungAsAbsentInsteadOfFailing() {
        assertThat(repository.lade(SchulungId.von("SCH-999"))).isEmpty();
        assertThat(repository.existiert(SchulungId.von("SCH-999"))).isFalse();
    }

    @Test
    void shouldLoadAllSchulungenSortedById() {
        repository.speichere(schulung("SCH-010"));
        repository.speichere(schulung("ABC-1"));
        repository.speichere(schulung("SCH-009"));

        assertThat(repository.ladeAlle())
                .extracting(s -> s.id().wert())
                .containsExactly("ABC-1", "SCH-009", "SCH-010");
    }

    @Test
    void shouldReturnAnEmptyCatalogWhenNothingWasStoredYet() {
        assertThat(repository.ladeAlle()).isEmpty();
        assertThat(repository.alleIds()).isEmpty();
    }

    @Test
    void shouldRemoveTheFileWhenDeleting() throws IOException {
        repository.speichere(schulung("SCH-009"));

        assertThat(repository.loesche(SchulungId.von("SCH-009"))).isTrue();

        assertThat(dateien()).isEmpty();
        assertThat(repository.loesche(SchulungId.von("SCH-009"))).isFalse();
    }

    /**
     * Die Dateien sind von Hand bearbeitbar und kommen von aussen herein.
     * Ein Syntaxfehler darf deshalb nicht als leerer Katalog durchgehen.
     */
    @Test
    void shouldFailLoudlyOnUnreadableJsonNamingTheFile() throws IOException {
        Files.createDirectories(katalogwurzel.resolve("schulungen"));
        Files.writeString(datei("SCH-009.json"), "{ kaputt");

        assertThatThrownBy(() -> repository.lade(SchulungId.von("SCH-009")))
                .isInstanceOf(KatalogLesefehler.class)
                .hasMessageContaining("SCH-009.json");
    }

    /**
     * Nur Dateien, die nach einer gueltigen Schulungs-ID benannt sind, zaehlen
     * zum Katalog. Eine Notiz oder eine Sicherungskopie im Verzeichnis darf
     * ihn nicht sprengen.
     */
    @Test
    void shouldIgnoreFilesThatAreNotNamedAfterASchulungId() throws IOException {
        repository.speichere(schulung("SCH-009"));
        Files.writeString(datei("notizen.txt"), "nur eine Notiz");
        Files.writeString(datei("SCH-009.json.bak"), "{}");

        assertThat(repository.alleIds()).containsExactly(SchulungId.von("SCH-009"));
    }

    // --- Hilfsmittel -------------------------------------------------------

    private static Katalogschulung schulung(String id) {
        return new Katalogschulung(
                SchulungId.von(id),
                "Scrum Master Zertifizierung",
                "Agile & Projektmanagement",
                "Grundlagen der Rolle.",
                List.of("Grundkenntnisse agiler Methoden"),
                2, 6, 12);
    }

    private Path datei(String name) {
        return katalogwurzel.resolve("schulungen").resolve(name);
    }

    private List<String> dateien() throws IOException {
        Path verzeichnis = katalogwurzel.resolve("schulungen");
        if (!Files.isDirectory(verzeichnis)) {
            return List.of();
        }
        try (var eintraege = Files.list(verzeichnis)) {
            return eintraege.map(p -> p.getFileName().toString()).sorted().toList();
        }
    }
}
