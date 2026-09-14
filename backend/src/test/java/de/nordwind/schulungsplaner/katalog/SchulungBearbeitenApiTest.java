package de.nordwind.schulungsplaner.katalog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Eine bestehende Schulung aendern. Gegenstuecke in der Anforderungsdoku:
 * TEST_KAT_PFLEG_01, TEST_KAT_PFLEG_02, TEST_KAT_ID_03 und TEST_KAT_ABL_02.
 */
class SchulungBearbeitenApiTest extends KatalogSchreibTest {

    @BeforeEach
    void bestehendeSchulung() {
        restTestClient.post().uri("/api/schulungen")
                .contentType(MediaType.APPLICATION_JSON)
                .body(felder())
                .exchange()
                .expectStatus().isCreated();
    }

    /** TEST_KAT_PFLEG_01: Alle Felder ausser der ID sind aenderbar. */
    @Test
    void shouldChangeEveryFieldExceptTheId() {
        Map<String, Object> geaendert = felder();
        geaendert.put("titel", "Scrum Master Aufbau");
        geaendert.put("kategorie", "Cloud & DevOps");
        geaendert.put("kurzbeschreibung", "Vertiefung der Rolle.");
        geaendert.put("voraussetzungen", List.of("Scrum Master Grundlagen"));
        geaendert.put("dauerInTagen", 3);
        geaendert.put("mindestteilnehmerExklusiv", 4);
        geaendert.put("maxTeilnehmerOeffentlich", 10);

        aendere("SCH-009", geaendert)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.schulung.titel").isEqualTo("Scrum Master Aufbau")
                .jsonPath("$.schulung.kategorie").isEqualTo("Cloud & DevOps")
                .jsonPath("$.schulung.kurzbeschreibung").isEqualTo("Vertiefung der Rolle.")
                .jsonPath("$.schulung.voraussetzungen[0]").isEqualTo("Scrum Master Grundlagen")
                .jsonPath("$.schulung.dauerInTagen").isEqualTo(3)
                .jsonPath("$.schulung.mindestteilnehmerExklusiv").isEqualTo(4)
                .jsonPath("$.schulung.maxTeilnehmerOeffentlich").isEqualTo(10);

        // Die Aenderung ist im Katalog wirksam, nicht nur in der Antwort.
        restTestClient.get().uri("/api/schulungen/SCH-009")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.titel").isEqualTo("Scrum Master Aufbau");
    }

    /** TEST_KAT_ID_03: Ein Aenderungsversuch an der ID wird abgewiesen. */
    @Test
    void shouldRejectAnAttemptToChangeTheId() throws Exception {
        String vorher = Files.readString(katalogdatei("SCH-009"));
        Map<String, Object> geaendert = felder();
        geaendert.put("id", "SCH-010");

        aendere("SCH-009", geaendert)
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.fehler[0].feld").isEqualTo("id")
                .jsonPath("$.fehler[0].code").isEqualTo("ID_UNVERAENDERLICH");

        assertThat(Files.readString(katalogdatei("SCH-009"))).isEqualTo(vorher);
        assertThat(katalogdatei("SCH-010")).doesNotExist();
    }

    @Test
    void shouldReportAnUnknownSchulungAsNotFound() {
        aendere("SCH-999", felder()).expectStatus().isNotFound();
    }

    /** TEST_KAT_ABL_02: Nach dem Aendern ein neuer Commit auf der Datei. */
    @Test
    void shouldSecureTheChangeWithACommit() {
        Map<String, Object> geaendert = felder();
        geaendert.put("titel", "Scrum Master Aufbau");

        aendere("SCH-009", geaendert).expectStatus().isOk();

        assertThat(commitsFuer(katalogdatei("SCH-009"))).hasSize(2);
        assertThat(commits().getFirst().getFullMessage())
                .isEqualTo("Schulung SCH-009 geaendert");
    }

    @Test
    void shouldNotCommitWhenNothingChanged() {
        aendere("SCH-009", felder()).expectStatus().isOk();

        assertThat(commits()).hasSize(1);
    }

    /**
     * TEST_KAT_PFLEG_02: Die Dauer wird von zwei auf drei Tage geaendert, zu
     * der Schulung besteht ein geplanter Termin. Die Aenderung wird mit einer
     * Warnung ausgefuehrt, Start- und Enddatum des Termins bleiben unberuehrt.
     */
    @Test
    void shouldWarnWhenChangingTheDurationOfASchulungThatHasTermine() {
        legeTerminAn("SCH-009-T1", "SCH-009", "2026-09-14", "2026-09-15");
        Map<String, Object> geaendert = felder();
        geaendert.put("dauerInTagen", 3);

        aendere("SCH-009", geaendert)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.schulung.dauerInTagen").isEqualTo(3)
                .jsonPath("$.warnungen[0].code").isEqualTo("DAUER_GEAENDERT_MIT_TERMINEN")
                .jsonPath("$.warnungen[0].meldung").value(meldung ->
                        assertThat((String) meldung).contains("1"))
                // REQ_KAT_PFLEG_05: Bestehende Termine behalten ihren Zeitraum.
                .jsonPath("$.schulung.oeffentlicheTermine[0].startdatum")
                .isEqualTo("2026-09-14")
                .jsonPath("$.schulung.oeffentlicheTermine[0].enddatum")
                .isEqualTo("2026-09-15");
    }

    /** Ohne Termine gibt es nichts zu warnen. */
    @Test
    void shouldNotWarnWhenChangingTheDurationWithoutAnyTermine() {
        Map<String, Object> geaendert = felder();
        geaendert.put("dauerInTagen", 3);

        aendere("SCH-009", geaendert)
                .expectStatus().isOk()
                .expectBody().jsonPath("$.warnungen").isEmpty();
    }

    /** Eine unveraenderte Dauer warnt nicht, auch wenn Termine bestehen. */
    @Test
    void shouldNotWarnWhenTheDurationStaysTheSame() {
        legeTerminAn("SCH-009-T1", "SCH-009", "2026-09-14", "2026-09-15");
        Map<String, Object> geaendert = felder();
        geaendert.put("titel", "Scrum Master Aufbau");

        aendere("SCH-009", geaendert)
                .expectStatus().isOk()
                .expectBody().jsonPath("$.warnungen").isEmpty();
    }

    @Test
    void shouldRejectAnInvalidChangeWithoutTouchingTheFile() throws Exception {
        String vorher = Files.readString(katalogdatei("SCH-009"));
        Map<String, Object> geaendert = felder();
        geaendert.put("dauerInTagen", 0);

        aendere("SCH-009", geaendert)
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.fehler[0].code").isEqualTo("DAUER_ZU_KLEIN");

        assertThat(Files.readString(katalogdatei("SCH-009"))).isEqualTo(vorher);
        assertThat(commits()).hasSize(1);
    }

    // --- Hilfsmittel -------------------------------------------------------

    private RestTestClient.ResponseSpec aendere(String id, Map<String, Object> felder) {
        return restTestClient.put().uri("/api/schulungen/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(felder)
                .exchange();
    }

    private static Map<String, Object> felder() {
        Map<String, Object> felder = new LinkedHashMap<>();
        felder.put("id", "SCH-009");
        felder.put("titel", "Scrum Master Zertifizierung");
        felder.put("kategorie", "Agile & Projektmanagement");
        felder.put("kurzbeschreibung", "Grundlagen der Rolle.");
        felder.put("voraussetzungen", List.of("Grundkenntnisse agiler Methoden"));
        felder.put("dauerInTagen", 2);
        felder.put("mindestteilnehmerExklusiv", 6);
        felder.put("maxTeilnehmerOeffentlich", 12);
        return felder;
    }
}
