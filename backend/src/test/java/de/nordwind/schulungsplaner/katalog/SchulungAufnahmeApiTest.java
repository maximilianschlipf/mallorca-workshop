package de.nordwind.schulungsplaner.katalog;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bereitgestellte JSON-Dateien in den Katalog aufnehmen. Gegenstuecke in der
 * Anforderungsdoku: TEST_KAT_IMP_01 bis TEST_KAT_IMP_04.
 */
class SchulungAufnahmeApiTest extends KatalogSchreibTest {

    private static final String GUELTIG = """
            {
              "id" : "SCH-009",
              "titel" : "Scrum Master Zertifizierung",
              "kategorie" : "Agile & Projektmanagement",
              "kurzbeschreibung" : "Grundlagen der Rolle.",
              "voraussetzungen" : [ "Grundkenntnisse agiler Methoden" ],
              "dauerInTagen" : 2,
              "mindestteilnehmerExklusiv" : 6,
              "maxTeilnehmerOeffentlich" : 12
            }
            """;

    /** TEST_KAT_IMP_01: Eine gueltige Datei wird aufgenommen. */
    // verifies: TEST_KAT_IMP_01
    @Test
    void shouldTakeInAValidFile() {
        aufnehmen(false, datei("SCH-009.json", GUELTIG))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.aufgenommen").isEqualTo(1)
                .jsonPath("$.ergebnisse[0].dateiname").isEqualTo("SCH-009.json")
                .jsonPath("$.ergebnisse[0].ergebnis").isEqualTo("AUFGENOMMEN");

        restTestClient.get().uri("/api/schulungen/SCH-009")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.titel").isEqualTo("Scrum Master Zertifizierung")
                .jsonPath("$.zustand").isEqualTo("AKTIV");
    }

    /** Die Aufnahme wird als Commit gesichert (REQ_KAT_ABL_03). */
    @Test
    void shouldSecureTheImportWithASingleCommitForAllFiles() {
        aufnehmen(false,
                datei("SCH-009.json", GUELTIG),
                datei("SCH-010.json", GUELTIG.replace("SCH-009", "SCH-010")))
                .expectStatus().isOk()
                .expectBody().jsonPath("$.aufgenommen").isEqualTo(2);

        assertThat(commits()).hasSize(1);
        assertThat(commits().getFirst().getFullMessage())
                .isEqualTo("2 Schulungen aus Dateien aufgenommen");
        assertThat(commitsFuer(katalogdatei("SCH-009"))).hasSize(1);
        assertThat(commitsFuer(katalogdatei("SCH-010"))).hasSize(1);
    }

    /**
     * TEST_KAT_IMP_02: Fehlende Pflichtangabe, unerlaubte Zeichen in der ID,
     * Dauer 0 und Hoechstzahl unter der Mindestzahl werden je einzeln
     * abgewiesen, und die Rueckmeldung nennt den Grund.
     */
    // verifies: TEST_KAT_IMP_02
    @Test
    void shouldRejectEachInvalidFileNamingTheReason() {
        aufnehmen(false,
                datei("ohne-titel.json", GUELTIG.replace(
                        "\"titel\" : \"Scrum Master Zertifizierung\",", "")),
                datei("falsche-id.json", GUELTIG.replace("SCH-009", "sch-009")),
                datei("dauer-null.json", GUELTIG.replace("\"dauerInTagen\" : 2",
                        "\"dauerInTagen\" : 0")),
                datei("grenzen.json", GUELTIG.replace("\"maxTeilnehmerOeffentlich\" : 12",
                        "\"maxTeilnehmerOeffentlich\" : 4")))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.aufgenommen").isEqualTo(0)
                .jsonPath("$.ergebnisse[0].ergebnis").isEqualTo("ABGEWIESEN")
                .jsonPath("$.ergebnisse[0].fehler[0].code").isEqualTo("PFLICHTANGABE_FEHLT")
                .jsonPath("$.ergebnisse[1].fehler[0].code").isEqualTo("ID_UNERLAUBTE_ZEICHEN")
                .jsonPath("$.ergebnisse[2].fehler[0].code").isEqualTo("DAUER_ZU_KLEIN")
                .jsonPath("$.ergebnisse[3].fehler[0].code")
                .isEqualTo("HOECHSTZAHL_NICHT_UEBER_MINDESTZAHL");

        assertThat(commits()).isEmpty();
    }

    @Test
    void shouldRejectAFileThatIsNotReadableJson() {
        aufnehmen(false, datei("kaputt.json", "{ das ist kein JSON"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.ergebnisse[0].ergebnis").isEqualTo("ABGEWIESEN")
                .jsonPath("$.ergebnisse[0].fehler[0].meldung").value(meldung ->
                        assertThat((String) meldung).isNotBlank());
    }

    /**
     * TEST_KAT_IMP_03: Eine unbekannte Kategorie wird abgewiesen, und die
     * Kategorienliste bleibt unveraendert.
     */
    // verifies: TEST_KAT_IMP_03
    @Test
    void shouldRejectAnUnknownCategoryWithoutCreatingIt() {
        aufnehmen(false, datei("SCH-009.json",
                GUELTIG.replace("Agile & Projektmanagement", "Robotik")))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.ergebnisse[0].ergebnis").isEqualTo("ABGEWIESEN")
                .jsonPath("$.ergebnisse[0].fehler[0].code").isEqualTo("KATEGORIE_UNBEKANNT");

        restTestClient.get().uri("/api/kategorien")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String[].class)
                .value(liste -> assertThat(liste).containsExactlyElementsOf(KATEGORIEN));
    }

    /**
     * TEST_KAT_IMP_04: Eine Datei mit bereits vergebener ID wird nicht ohne
     * Rueckfrage uebernommen; ohne ausdrueckliche Entscheidung bleibt die
     * bestehende Schulung unveraendert.
     */
    // verifies: TEST_KAT_IMP_04
    @Test
    void shouldNotSilentlyReplaceAnExistingSchulung() {
        aufnehmen(false, datei("SCH-009.json", GUELTIG)).expectStatus().isOk();

        aufnehmen(false, datei("SCH-009.json",
                GUELTIG.replace("Scrum Master Zertifizierung", "Ein anderer Titel")))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.aufgenommen").isEqualTo(0)
                .jsonPath("$.ergebnisse[0].ergebnis").isEqualTo("ENTSCHEIDUNG_OFFEN")
                .jsonPath("$.ergebnisse[0].fehler[0].code").isEqualTo("ID_VERGEBEN");

        restTestClient.get().uri("/api/schulungen/SCH-009")
                .exchange()
                .expectBody().jsonPath("$.titel").isEqualTo("Scrum Master Zertifizierung");
        assertThat(commits()).hasSize(1);

        aufnehmen(true, datei("SCH-009.json",
                GUELTIG.replace("Scrum Master Zertifizierung", "Ein anderer Titel")))
                .expectStatus().isOk().expectBody()
                .jsonPath("$.aufgenommen").isEqualTo(1)
                .jsonPath("$.ergebnisse[0].ergebnis").isEqualTo("ERSETZT");
        restTestClient.get().uri("/api/schulungen/SCH-009")
                .exchange().expectBody().jsonPath("$.titel").isEqualTo("Ein anderer Titel");
    }

    /** TEST_KAT_IMP_04: Mit ausdruecklicher Entscheidung wird ersetzt. */
    @Test
    void shouldReplaceAnExistingSchulungWhenExplicitlyAsked() {
        aufnehmen(false, datei("SCH-009.json", GUELTIG)).expectStatus().isOk();

        aufnehmen(true, datei("SCH-009.json",
                GUELTIG.replace("Scrum Master Zertifizierung", "Ein anderer Titel")))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.aufgenommen").isEqualTo(1)
                .jsonPath("$.ergebnisse[0].ergebnis").isEqualTo("ERSETZT");

        restTestClient.get().uri("/api/schulungen/SCH-009")
                .exchange()
                .expectBody().jsonPath("$.titel").isEqualTo("Ein anderer Titel");
    }

    /** Ein Ersetzen laesst den Zustand unangetastet -- es ist keine Neuanlage. */
    @Test
    void shouldKeepTheStateOfAReplacedSchulung() {
        aufnehmen(false, datei("SCH-009.json", GUELTIG)).expectStatus().isOk();
        restTestClient.post().uri("/api/schulungen/SCH-009/archivierung")
                .exchange().expectStatus().isOk();

        aufnehmen(true, datei("SCH-009.json",
                GUELTIG.replace("Scrum Master Zertifizierung", "Ein anderer Titel")))
                .expectStatus().isOk();

        restTestClient.get().uri("/api/schulungen/SCH-009")
                .exchange()
                .expectBody().jsonPath("$.zustand").isEqualTo("ARCHIVIERT");
    }

    /**
     * Eine abgewiesene Datei haelt die uebrigen nicht auf -- der Administrator
     * soll nicht Datei um Datei einzeln vorgesetzt bekommen.
     */
    @Test
    void shouldTakeInTheValidFilesEvenWhenOthersAreRejected() {
        aufnehmen(false,
                datei("SCH-009.json", GUELTIG),
                datei("kaputt.json", "{ kaputt"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.aufgenommen").isEqualTo(1)
                .jsonPath("$.abgewiesen").isEqualTo(1);

        assertThat(katalogdatei("SCH-009")).exists();
    }

    /** Ohne den Teil "dateien" ist die Anfrage unvollstaendig. */
    @Test
    void shouldRejectAnUploadWithoutTheExpectedPart() {
        MultiValueMap<String, Object> koerper = new LinkedMultiValueMap<>();
        koerper.add("etwasAnderes", alsAnhang(datei("SCH-009.json", GUELTIG)));

        restTestClient.post().uri("/api/schulungen/aufnahme")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(koerper)
                .exchange()
                .expectStatus().isBadRequest();
    }

    // --- Hilfsmittel -------------------------------------------------------

    private record Datei(String name, String inhalt) {
    }

    private static Datei datei(String name, String inhalt) {
        return new Datei(name, inhalt);
    }

    private RestTestClient.ResponseSpec aufnehmen(boolean ersetzen, Datei... dateien) {
        MultiValueMap<String, Object> koerper = new LinkedMultiValueMap<>();
        for (Datei datei : dateien) {
            koerper.add("dateien", alsAnhang(datei));
        }
        return restTestClient.post()
                .uri("/api/schulungen/aufnahme?ersetzen={ersetzen}", ersetzen)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(koerper)
                .exchange();
    }

    /**
     * Eine Ressource, die ihren Dateinamen kennt -- daran erkennt der
     * Nachrichtenwandler einen Dateianhang statt eines Textfeldes.
     */
    private static ByteArrayResource alsAnhang(Datei datei) {
        return new ByteArrayResource(datei.inhalt().getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return datei.name();
            }
        };
    }
}
