package de.nordwind.schulungsplaner.katalog;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Die Kategorienliste pflegen. Gegenstuecke in der Anforderungsdoku:
 * TEST_KAT_KATG_01 bis TEST_KAT_KATG_04.
 */
class KategorienApiTest extends KatalogSchreibTest {

    /** REQ_KAT_SUCH_06: doppelfrei und alphabetisch sortiert. */
    @Test
    void shouldListCategoriesDistinctAndSorted() {
        assertThat(kategorien()).containsExactlyElementsOf(KATEGORIEN);
    }

    @Test
    void shouldAddANewCategory() {
        anlegen("Datenbanken")
                .expectStatus().isCreated()
                .expectBody(String[].class)
                .value(liste -> assertThat(liste).contains("Datenbanken").isSorted());

        assertThat(kategorien()).contains("Datenbanken");
    }

    /** TEST_KAT_KATG_04: Die Liste liegt versioniert im Katalog. */
    @Test
    void shouldSecureANewCategoryWithACommit() {
        anlegen("Datenbanken").expectStatus().isCreated();

        assertThat(commitsFuer(kategorienDatei())).hasSize(1);
        assertThat(commits().getFirst().getFullMessage())
                .isEqualTo("Kategorie 'Datenbanken' angelegt");
    }

    @Test
    void shouldRejectACategoryThatAlreadyExists() {
        anlegen("Cloud & DevOps")
                .expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.fehler[0].code").isEqualTo("KATEGORIE_VERGEBEN");

        assertThat(commits()).isEmpty();
    }

    @Test
    void shouldRejectACategoryWithoutAName() {
        anlegen("   ")
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.fehler[0].code").isEqualTo("PFLICHTANGABE_FEHLT");
    }

    /**
     * TEST_KAT_KATG_02: Nach dem Umbenennen fuehren alle zuvor zugeordneten
     * Schulungen den neuen Namen, und der Filter findet sie darunter.
     */
    @Test
    void shouldRenameACategoryAcrossEveryAssignedSchulung() {
        legeSchulungAn("SCH-009", "Cloud & DevOps");
        legeSchulungAn("SCH-010", "Cloud & DevOps");
        legeSchulungAn("SCH-011", "IT-Security");

        umbenennen("Cloud & DevOps", "Cloud und Betrieb")
                .expectStatus().isOk();

        assertThat(kategorien()).contains("Cloud und Betrieb")
                .doesNotContain("Cloud & DevOps");
        assertThat(schulungenMitKategorie("Cloud und Betrieb"))
                .containsExactly("SCH-009", "SCH-010");
        assertThat(schulungenMitKategorie("Cloud & DevOps")).isEmpty();
        assertThat(schulungenMitKategorie("IT-Security")).containsExactly("SCH-011");
    }

    /** Das Umbenennen sichert Liste und betroffene Schulungen in einem Commit. */
    @Test
    void shouldSecureTheRenameOfListAndSchulungenInASingleCommit() {
        legeSchulungAn("SCH-009", "Cloud & DevOps");
        int vorher = commits().size();

        umbenennen("Cloud & DevOps", "Cloud und Betrieb").expectStatus().isOk();

        assertThat(commits()).hasSize(vorher + 1);
        assertThat(commits().getFirst().getFullMessage())
                .isEqualTo("Kategorie 'Cloud & DevOps' in 'Cloud und Betrieb' umbenannt");
        assertThat(commitsFuer(kategorienDatei())).hasSize(1);
        assertThat(commitsFuer(katalogdatei("SCH-009"))).hasSize(2);
    }

    @Test
    void shouldRejectRenamingAnUnknownCategory() {
        umbenennen("Gibt es nicht", "Neuer Name")
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.fehler[0].code").isEqualTo("KATEGORIE_UNBEKANNT");
    }

    @Test
    void shouldRejectRenamingOntoAnExistingCategory() {
        umbenennen("Cloud & DevOps", "IT-Security")
                .expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.fehler[0].code").isEqualTo("KATEGORIE_VERGEBEN");

        assertThat(kategorien()).containsExactlyElementsOf(KATEGORIEN);
    }

    /**
     * TEST_KAT_KATG_03: Eine Kategorie in Gebrauch laesst sich nicht loeschen;
     * nach dem Umhaengen der letzten Schulung gelingt es.
     */
    @Test
    void shouldRefuseToDeleteACategoryInUseAndAllowItAfterTheLastSchulungMoved() {
        legeSchulungAn("SCH-009", "Cloud & DevOps");

        loeschen("Cloud & DevOps")
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.fehler[0].code").isEqualTo("KATEGORIE_IN_GEBRAUCH")
                .jsonPath("$.fehler[0].meldung").value(meldung ->
                        assertThat((String) meldung).contains("SCH-009"));

        haengeUm("SCH-009", "IT-Security");

        loeschen("Cloud & DevOps").expectStatus().isNoContent();
        assertThat(kategorien()).doesNotContain("Cloud & DevOps");
    }

    @Test
    void shouldSecureADeletedCategoryWithACommit() {
        loeschen("Cloud & DevOps").expectStatus().isNoContent();

        assertThat(commitsFuer(kategorienDatei())).hasSize(1);
        assertThat(commits().getFirst().getFullMessage())
                .isEqualTo("Kategorie 'Cloud & DevOps' gelöscht");
    }

    @Test
    void shouldRejectDeletingAnUnknownCategory() {
        loeschen("Gibt es nicht")
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.fehler[0].code").isEqualTo("KATEGORIE_UNBEKANNT");
    }

    // --- Hilfsmittel -------------------------------------------------------

    private RestTestClient.ResponseSpec anlegen(String name) {
        return restTestClient.post().uri("/api/kategorien")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("name", name))
                .exchange();
    }

    private RestTestClient.ResponseSpec umbenennen(String name, String neuerName) {
        return restTestClient.put().uri("/api/kategorien")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("name", name, "neuerName", neuerName))
                .exchange();
    }

    private RestTestClient.ResponseSpec loeschen(String name) {
        return restTestClient.method(org.springframework.http.HttpMethod.DELETE)
                .uri("/api/kategorien?name={name}", name)
                .exchange();
    }

    private List<String> kategorien() {
        return List.of(restTestClient.get().uri("/api/kategorien")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String[].class)
                .returnResult()
                .getResponseBody());
    }

    private List<String> schulungenMitKategorie(String kategorie) {
        return java.util.Arrays.stream(restTestClient.get()
                        .uri("/api/schulungen?kategorie={kategorie}", kategorie)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(tools.jackson.databind.JsonNode[].class)
                        .returnResult()
                        .getResponseBody())
                .map(knoten -> knoten.get("id").asString())
                .toList();
    }

    private void legeSchulungAn(String id, String kategorie) {
        restTestClient.post().uri("/api/schulungen")
                .contentType(MediaType.APPLICATION_JSON)
                .body(felder(id, kategorie))
                .exchange()
                .expectStatus().isCreated();
    }

    private void haengeUm(String id, String kategorie) {
        restTestClient.put().uri("/api/schulungen/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(felder(id, kategorie))
                .exchange()
                .expectStatus().isOk();
    }

    private static Map<String, Object> felder(String id, String kategorie) {
        Map<String, Object> felder = new LinkedHashMap<>();
        felder.put("id", id);
        felder.put("titel", "Titel zu " + id);
        felder.put("kategorie", kategorie);
        felder.put("kurzbeschreibung", "Beschreibung zu " + id);
        felder.put("voraussetzungen", List.of());
        felder.put("dauerInTagen", 2);
        felder.put("mindestteilnehmerExklusiv", 6);
        felder.put("maxTeilnehmerOeffentlich", 12);
        return felder;
    }
}
