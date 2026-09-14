package de.nordwind.schulungsplaner.katalog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.nio.file.Files;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Archivieren, Reaktivieren und Loeschen. Gegenstuecke in der
 * Anforderungsdoku: TEST_KAT_ARCH_01, TEST_KAT_ABL_03 und TEST_KAT_LOE_01
 * bis TEST_KAT_LOE_04.
 */
class SchulungLebenszyklusApiTest extends KatalogSchreibTest {

    @BeforeEach
    void bestehendeSchulung() {
        legeSchulungAn("SCH-009");
    }

    /**
     * TEST_KAT_ARCH_01: Eine Schulung mit einem zukuenftigen Termin laesst
     * sich archivieren; der Termin bleibt unveraendert bestehen.
     */
    @Test
    void shouldArchiveEvenWhenFutureTermineExistAndLeaveThemUntouched() {
        legeTerminAn("SCH-009-T1", "SCH-009", "2027-09-14", "2027-09-15");

        archiviere("SCH-009")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.schulung.zustand").isEqualTo("ARCHIVIERT")
                .jsonPath("$.schulung.oeffentlicheTermine[0].startdatum").isEqualTo("2027-09-14")
                .jsonPath("$.schulung.oeffentlicheTermine[0].enddatum").isEqualTo("2027-09-15");
    }

    /**
     * TEST_KAT_ABL_03: Archivieren und Reaktivieren lassen die Zahl der
     * Commits unveraendert und die Katalogdatei bitgleich.
     */
    @Test
    void shouldChangeNeitherTheFileNorTheHistoryWhenArchivingAndReactivating()
            throws Exception {
        String vorher = Files.readString(katalogdatei("SCH-009"));
        int commitsVorher = commits().size();

        archiviere("SCH-009").expectStatus().isOk();
        reaktiviere("SCH-009").expectStatus().isOk();

        assertThat(Files.readString(katalogdatei("SCH-009"))).isEqualTo(vorher);
        assertThat(commits()).hasSize(commitsVorher);
    }

    /** REQ_KAT_ARCH_04: Nach dem Reaktivieren ist die Schulung wieder aktiv. */
    @Test
    void shouldReactivateAnArchivedSchulung() {
        archiviere("SCH-009").expectStatus().isOk();

        reaktiviere("SCH-009")
                .expectStatus().isOk()
                .expectBody().jsonPath("$.schulung.zustand").isEqualTo("AKTIV");
    }

    @Test
    void shouldReportAnUnknownSchulungAsNotFoundWhenArchiving() {
        archiviere("SCH-999").expectStatus().isNotFound();
    }

    /** TEST_KAT_LOE_01: Eine frisch angelegte Schulung laesst sich loeschen. */
    @Test
    void shouldDeleteASchulungWithoutAnyTermine() {
        loesche("SCH-009").expectStatus().isNoContent();

        assertThat(katalogdatei("SCH-009")).doesNotExist();
        restTestClient.get().uri("/api/schulungen/SCH-009").exchange()
                .expectStatus().isNotFound();
    }

    /** TEST_KAT_LOE_01: Ebenso eine, deren letzter Termin zuvor geloescht wurde. */
    @Test
    void shouldDeleteASchulungAfterItsLastTerminWasRemoved() {
        legeTerminAn("SCH-009-T1", "SCH-009", "2027-09-14", "2027-09-15");
        loesche("SCH-009").expectStatus().isEqualTo(409);

        jdbcTemplate.update("DELETE FROM termin WHERE termin_id = ?", "SCH-009-T1");

        loesche("SCH-009").expectStatus().isNoContent();
    }

    /** Das Loeschen wird als Commit gesichert (REQ_KAT_ABL_03). */
    @Test
    void shouldSecureTheDeletionWithACommit() {
        loesche("SCH-009").expectStatus().isNoContent();

        assertThat(commitsFuer(katalogdatei("SCH-009"))).hasSize(2);
        assertThat(commits().getFirst().getFullMessage())
                .isEqualTo("Schulung SCH-009 geloescht");
    }

    /**
     * TEST_KAT_LOE_02: Eine aktive Schulung mit Terminen laesst sich nicht
     * loeschen, ebenso wenig eine erst seit einem Monat archivierte.
     */
    @Test
    void shouldRefuseToDeleteAnActiveSchulungWithTermineOrOneArchivedTooRecently() {
        legeTerminAn("SCH-009-T1", "SCH-009", "2027-09-14", "2027-09-15");

        loesche("SCH-009")
                .expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.fehler[0].code").isEqualTo("SCHULUNG_NICHT_LOESCHBAR");

        archiviere("SCH-009").expectStatus().isOk();
        archiviertSeit("SCH-009", LocalDate.now().minusMonths(1));

        loesche("SCH-009")
                .expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.fehler[0].meldung").value(meldung ->
                        assertThat((String) meldung).contains("sechs Monaten"));

        assertThat(katalogdatei("SCH-009")).exists();
    }

    /**
     * TEST_KAT_LOE_03: Eine seit mehr als sechs Monaten archivierte Schulung
     * laesst sich loeschen, auch wenn zu ihr abgeschlossene Termine bestehen.
     */
    @Test
    void shouldDeleteASchulungArchivedForMoreThanSixMonthsDespiteCompletedTermine() {
        legeTerminAn("SCH-009-T1", "SCH-009", "2025-01-14", "2025-01-15");
        jdbcTemplate.update(
                "UPDATE termin SET status = 'abgeschlossen' WHERE termin_id = ?", "SCH-009-T1");
        archiviere("SCH-009").expectStatus().isOk();
        archiviertSeit("SCH-009", LocalDate.now().minusMonths(7));

        loesche("SCH-009").expectStatus().isNoContent();

        assertThat(katalogdatei("SCH-009")).doesNotExist();
    }

    /**
     * TEST_KAT_LOE_04: Nach dem Loeschen zeigt ein abgeschlossener Termin
     * weiterhin den Titel der Schulung und meldet keinen fehlenden Verweis.
     */
    @Test
    void shouldLeaveTheTitleBehindOnCompletedTermineWithoutReportingABrokenLink() {
        legeTerminAn("SCH-009-T1", "SCH-009", "2025-01-14", "2025-01-15");
        jdbcTemplate.update(
                "UPDATE termin SET status = 'abgeschlossen' WHERE termin_id = ?", "SCH-009-T1");
        archiviere("SCH-009").expectStatus().isOk();
        archiviertSeit("SCH-009", LocalDate.now().minusMonths(7));

        loesche("SCH-009").expectStatus().isNoContent();

        String titel = jdbcTemplate.queryForObject(
                "SELECT schulung_titel FROM termin WHERE termin_id = ?",
                String.class, "SCH-009-T1");
        assertThat(titel).isEqualTo("Titel zu SCH-009");

        restTestClient.get().uri("/api/katalog/verwaiste-termine")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$").isEmpty();
    }

    /**
     * Ein Termin ohne hinterlassenen Titel bleibt ein gemeldeter Verweis ins
     * Leere (REQ_KAT_TERM_02) -- die Ausnahme gilt nur fuer das Loeschen ueber
     * die Anwendung.
     */
    @Test
    void shouldStillReportTermineThatLostTheirSchulungOutsideTheApplication() {
        legeTerminAn("SCH-404-T1", "SCH-404", "2027-01-14", "2027-01-15");

        restTestClient.get().uri("/api/katalog/verwaiste-termine")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$[0].terminId").isEqualTo("SCH-404-T1");
    }

    @Test
    void shouldReportAnUnknownSchulungAsNotFoundWhenDeleting() {
        loesche("SCH-999").expectStatus().isNotFound();
    }

    // --- Hilfsmittel -------------------------------------------------------

    private RestTestClient.ResponseSpec archiviere(String id) {
        return restTestClient.post().uri("/api/schulungen/{id}/archivierung", id).exchange();
    }

    private RestTestClient.ResponseSpec reaktiviere(String id) {
        return restTestClient.method(HttpMethod.DELETE)
                .uri("/api/schulungen/{id}/archivierung", id).exchange();
    }

    private RestTestClient.ResponseSpec loesche(String id) {
        return restTestClient.method(HttpMethod.DELETE)
                .uri("/api/schulungen/{id}", id).exchange();
    }

    private void archiviertSeit(String id, LocalDate tag) {
        jdbcTemplate.update(
                "UPDATE schulung_zustand SET archiviert_am = ? WHERE schulung_id = ?", tag, id);
    }

    private void legeSchulungAn(String id) {
        Map<String, Object> felder = new LinkedHashMap<>();
        felder.put("id", id);
        felder.put("titel", "Titel zu " + id);
        felder.put("kategorie", "Agile & Projektmanagement");
        felder.put("kurzbeschreibung", "Beschreibung zu " + id);
        felder.put("voraussetzungen", List.of());
        felder.put("dauerInTagen", 2);
        felder.put("mindestteilnehmerExklusiv", 6);
        felder.put("maxTeilnehmerOeffentlich", 12);

        restTestClient.post().uri("/api/schulungen")
                .contentType(MediaType.APPLICATION_JSON)
                .body(felder)
                .exchange()
                .expectStatus().isCreated();
    }
}
