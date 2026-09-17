package de.nordwind.schulungsplaner.katalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * REQ_KAT_ARCH_03: Zu einer archivierten Schulung koennen keine neuen Termine
 * angelegt werden.
 *
 * <p>Die Regel gehoert dem Katalog -- er weiss, ob eine Schulung angeboten
 * wird. Die Terminplanung fragt sie ab, sobald es sie gibt. Bis dahin ist sie
 * hier geprueft und nicht an einem Endpunkt, den es noch nicht gibt; der
 * Endpunktteil von TEST_KAT_ARCH_02 folgt mit der Terminplanung.
 */
class KatalogRegelnTest extends KatalogSchreibTest {

    @Autowired
    private KatalogRegeln regeln;

    @Test
    void shouldAllowANewTerminForAnActiveSchulung() {
        legeSchulungAn("SCH-009");

        assertThatNoException().isThrownBy(
                () -> regeln.verlangeTerminfaehig(SchulungId.von("SCH-009")));
    }

    // verifies: TEST_KAT_ARCH_02
    @Test
    void shouldRefuseANewTerminForAnArchivedSchulung() {
        legeSchulungAn("SCH-009");
        restTestClient.post().uri("/api/schulungen/SCH-009/archivierung")
                .exchange().expectStatus().isOk();

        assertThatThrownBy(() -> regeln.verlangeTerminfaehig(SchulungId.von("SCH-009")))
                .isInstanceOf(KatalogFehler.class)
                .satisfies(fehler -> assertThat(((KatalogFehler) fehler).fehler())
                        .singleElement()
                        .satisfies(f -> assertThat(f.code())
                                .isEqualTo(Fehlercode.SCHULUNG_ARCHIVIERT)));

        restTestClient.method(org.springframework.http.HttpMethod.DELETE)
                .uri("/api/schulungen/SCH-009/archivierung")
                .exchange().expectStatus().isOk();
        assertThatNoException().isThrownBy(
                () -> regeln.verlangeTerminfaehig(SchulungId.von("SCH-009")));
    }

    /** Nach dem Reaktivieren sind neue Termine wieder moeglich (REQ_KAT_ARCH_04). */
    @Test
    void shouldAllowANewTerminAgainAfterReactivation() {
        legeSchulungAn("SCH-009");
        restTestClient.post().uri("/api/schulungen/SCH-009/archivierung")
                .exchange().expectStatus().isOk();
        restTestClient.method(org.springframework.http.HttpMethod.DELETE)
                .uri("/api/schulungen/SCH-009/archivierung")
                .exchange().expectStatus().isOk();

        assertThatNoException().isThrownBy(
                () -> regeln.verlangeTerminfaehig(SchulungId.von("SCH-009")));
    }

    @Test
    void shouldRefuseANewTerminForASchulungThatIsNotInTheCatalog() {
        assertThatThrownBy(() -> regeln.verlangeTerminfaehig(SchulungId.von("SCH-404")))
                .isInstanceOf(SchulungNichtGefunden.class);
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
