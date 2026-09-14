package de.nordwind.schulungsplaner.katalog;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import tools.jackson.databind.JsonNode;

import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Eine Schulung ueber die Schnittstelle anlegen. Gegenstuecke in der
 * Anforderungsdoku: TEST_KAT_ANL_01, TEST_KAT_ID_01, TEST_KAT_ID_02,
 * TEST_KAT_ID_04, TEST_KAT_KATG_01, TEST_KAT_ABL_01 und TEST_KAT_ABL_02.
 */
class SchulungAnlegenApiTest extends KatalogSchreibTest {

    /**
     * TEST_KAT_ANL_01: Nach dem Anlegen mit allen Pflichtangaben existiert die
     * Schulung, ist aktiv und gibt die eingegebenen Werte unveraendert zurueck.
     */
    @Test
    void shouldCreateAnActiveSchulungReturningTheValuesUnchanged() {
        anlegen(vollstaendig())
                .expectStatus().isCreated()
                .expectHeader().valueEquals("Location", "/api/schulungen/SCH-009")
                .expectBody()
                .jsonPath("$.schulung.id").isEqualTo("SCH-009")
                .jsonPath("$.schulung.titel").isEqualTo("Scrum Master Zertifizierung")
                .jsonPath("$.schulung.kategorie").isEqualTo("Agile & Projektmanagement")
                .jsonPath("$.schulung.kurzbeschreibung").isEqualTo("Grundlagen der Rolle.")
                .jsonPath("$.schulung.voraussetzungen[0]")
                .isEqualTo("Grundkenntnisse agiler Methoden")
                .jsonPath("$.schulung.dauerInTagen").isEqualTo(2)
                .jsonPath("$.schulung.mindestteilnehmerExklusiv").isEqualTo(6)
                .jsonPath("$.schulung.maxTeilnehmerOeffentlich").isEqualTo(12)
                .jsonPath("$.schulung.zustand").isEqualTo("AKTIV")
                .jsonPath("$.schulung.oeffentlicheTermine").isEmpty()
                .jsonPath("$.warnungen").isEmpty();

        assertThat(imKatalog()).extracting(s -> s.get("id").asString())
                .containsExactly("SCH-009");
    }

    /** TEST_KAT_ABL_01: genau eine neue Datei, benannt nach der Schulungs-ID. */
    @Test
    void shouldWriteExactlyOneFileNamedAfterTheId() {
        anlegen(vollstaendig()).expectStatus().isCreated();

        assertThat(katalogdatei("SCH-009")).exists();
    }

    /** TEST_KAT_ABL_02: Die Aenderung wird als Commit gesichert. */
    @Test
    void shouldSecureTheNewSchulungWithACommit() {
        anlegen(vollstaendig()).expectStatus().isCreated();

        assertThat(commitsFuer(katalogdatei("SCH-009"))).hasSize(1);
        assertThat(commits().getFirst().getFullMessage())
                .isEqualTo("Schulung SCH-009 angelegt");
    }

    /** TEST_KAT_ID_02: Eine bereits vergebene ID wird abgewiesen. */
    @Test
    void shouldRejectAnIdThatIsAlreadyTaken() throws Exception {
        anlegen(vollstaendig()).expectStatus().isCreated();
        String vorher = Files.readString(katalogdatei("SCH-009"));

        anlegen(vollstaendig().with("titel", "Ein ganz anderer Titel"))
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.fehler[0].feld").isEqualTo("id")
                .jsonPath("$.fehler[0].code").isEqualTo("ID_VERGEBEN");

        assertThat(Files.readString(katalogdatei("SCH-009"))).isEqualTo(vorher);
        assertThat(commits()).hasSize(1);
    }

    /** TEST_KAT_ANL_02: Fehlende Pflichtangaben werden abgewiesen. */
    @Test
    void shouldRejectMissingMandatoryFieldsWithoutWritingAnything() {
        anlegen(vollstaendig().ohne("titel"))
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.fehler[0].feld").isEqualTo("titel")
                .jsonPath("$.fehler[0].code").isEqualTo("PFLICHTANGABE_FEHLT");

        assertThat(imKatalog()).isEmpty();
        assertThat(commits()).isEmpty();
    }

    /** TEST_KAT_ANL_02: Ohne die beiden freiwilligen Angaben gelingt es. */
    @Test
    void shouldCreateWithoutTheOptionalFields() {
        anlegen(vollstaendig().ohne("voraussetzungen").ohne("maxTeilnehmerOeffentlich"))
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.schulung.voraussetzungen").isEmpty()
                .jsonPath("$.schulung.maxTeilnehmerOeffentlich").doesNotExist();
    }

    /**
     * TEST_KAT_ID_01: Unerlaubte Zeichen werden abgewiesen, und es entsteht
     * keine Datei ausserhalb des Ablageverzeichnisses.
     */
    @Test
    void shouldRejectForbiddenCharactersWithoutEscapingTheStorageDirectory() {
        for (String id : List.of("Scrum / Basis", "sch-009", "SCH_009", "../SCH-009")) {
            anlegen(vollstaendig().with("id", id))
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.fehler[0].code").isEqualTo("ID_UNERLAUBTE_ZEICHEN");
        }

        assertThat(imKatalog()).isEmpty();
        assertThat(katalogdatei("SCH-009").getParent().getParent().getParent()
                .resolve("SCH-009.json")).doesNotExist();
    }

    /** TEST_KAT_ID_04: Ohne Eingabe einer ID wird das Anlegen abgewiesen. */
    @Test
    void shouldRejectCreationWithoutAnId() {
        anlegen(vollstaendig().ohne("id"))
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.fehler[0].feld").isEqualTo("id")
                .jsonPath("$.fehler[0].code").isEqualTo("PFLICHTANGABE_FEHLT");
    }

    /** TEST_KAT_ID_04: Das Schema der bestehenden Kennungen wird angezeigt. */
    @Test
    void shouldShowThePatternOfExistingIdsWithoutSuggestingOne() {
        anlegen(vollstaendig()).expectStatus().isCreated();
        anlegen(vollstaendig().with("id", "SCH-010")).expectStatus().isCreated();

        restTestClient.get().uri("/api/schulungen/id-schema")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.muster").isEqualTo("AAA-999")
                .jsonPath("$.beispiele[0]").isEqualTo("SCH-009")
                .jsonPath("$.vorschlag").doesNotExist();
    }

    /** TEST_KAT_KATG_01: Eine Kategorie ausserhalb der Liste wird abgewiesen. */
    @Test
    void shouldRejectACategoryOutsideTheMaintainedList() {
        anlegen(vollstaendig().with("kategorie", "IT Security"))
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.fehler[0].feld").isEqualTo("kategorie")
                .jsonPath("$.fehler[0].code").isEqualTo("KATEGORIE_UNBEKANNT");

        assertThat(imKatalog()).isEmpty();
    }

    /** TEST_KAT_ANL_03 und TEST_KAT_ANL_04 an der Schnittstelle. */
    @Test
    void shouldRejectInvalidDurationAndParticipantLimits() {
        anlegen(vollstaendig().with("dauerInTagen", 0))
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.fehler[0].code").isEqualTo("DAUER_ZU_KLEIN");

        anlegen(vollstaendig().with("mindestteilnehmerExklusiv", 6)
                .with("maxTeilnehmerOeffentlich", 4))
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.fehler[0].code")
                .isEqualTo("HOECHSTZAHL_NICHT_UEBER_MINDESTZAHL");
    }

    /** TEST_KAT_ANL_05: Fehlende und 0-Hoechstzahl bedeuten keine Obergrenze. */
    @Test
    void shouldAcceptZeroAsNoUpperLimit() {
        anlegen(vollstaendig().with("maxTeilnehmerOeffentlich", 0))
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.schulung.maxTeilnehmerOeffentlich").doesNotExist();
    }

    // --- Hilfsmittel -------------------------------------------------------

    private RestTestClient.ResponseSpec anlegen(Eingabe eingabe) {
        return restTestClient.post().uri("/api/schulungen")
                .contentType(MediaType.APPLICATION_JSON)
                .body(eingabe.felder())
                .exchange();
    }

    private List<JsonNode> imKatalog() {
        return restTestClient.get().uri("/api/schulungen")
                .exchange()
                .expectStatus().isOk()
                .expectBody(JsonNode.class)
                .returnResult()
                .getResponseBody()
                .valueStream()
                .toList();
    }

    private static Eingabe vollstaendig() {
        return new Eingabe(new java.util.LinkedHashMap<>(Map.of(
                "id", "SCH-009",
                "titel", "Scrum Master Zertifizierung",
                "kategorie", "Agile & Projektmanagement",
                "kurzbeschreibung", "Grundlagen der Rolle.",
                "voraussetzungen", List.of("Grundkenntnisse agiler Methoden"),
                "dauerInTagen", 2,
                "mindestteilnehmerExklusiv", 6,
                "maxTeilnehmerOeffentlich", 12)));
    }

    /** Erlaubt "vollstaendige Eingabe, ein Feld anders" knapp auszudruecken. */
    private record Eingabe(Map<String, Object> felder) {

        Eingabe with(String feld, Object wert) {
            var kopie = new java.util.LinkedHashMap<>(felder);
            kopie.put(feld, wert);
            return new Eingabe(kopie);
        }

        Eingabe ohne(String feld) {
            var kopie = new java.util.LinkedHashMap<>(felder);
            kopie.remove(feld);
            return new Eingabe(kopie);
        }
    }
}
