package de.nordwind.schulungsplaner.katalog;

import de.nordwind.schulungsplaner.domain.Schulung;
import de.nordwind.schulungsplaner.katalog.zustand.SchulungszustandRepository;
import de.nordwind.schulungsplaner.katalog.zustand.Schulungszustand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Die Leseseite des Katalogs: Sie zieht die Beschreibung aus den
 * Katalogdateien und den Zustand samt Terminen aus der Datenbank.
 * Gegenstuecke in der Anforderungsdoku: TEST_KAT_SICHT_02, TEST_KAT_TERM_01
 * und TEST_KAT_TERM_02.
 */
@SpringBootTest
@Transactional
class KatalogAnsichtServiceTest {

    @Autowired
    private KatalogAnsichtService ansicht;

    @Autowired
    private SchulungszustandRepository zustaende;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** TEST_KAT_TERM_01: Der Termin zieht seine Daten ueber die Schulungs-ID. */
    // verifies: TEST_KAT_TERM_01
    @Test
    void shouldJoinTerminDataFromTheDatabaseToTheCatalogDescription() {
        Schulung schulung = eine("SCH-001");

        assertThat(schulung.titel()).isEqualTo("Scrum Master Zertifizierung");
        assertThat(schulung.kategorie()).isEqualTo("Agile & Projektmanagement");
        assertThat(schulung.voraussetzungen())
                .containsExactly("Grundkenntnisse agiler Methoden");
        assertThat(schulung.oeffentlicheTermine())
                .isNotEmpty()
                .allSatisfy(termin -> assertThat(termin.terminId()).startsWith("SCH-001-"));
    }

    @Test
    void shouldSortTermineChronologically() {
        List<String> starttage = eine("SCH-001").oeffentlicheTermine().stream()
                .map(t -> t.startdatum())
                .toList();

        assertThat(starttage).isSorted();
    }

    /** REQ_KAT_FELD_01: Der Zustand kommt aus der Datenbank, nicht aus der Datei. */
    @Test
    void shouldReportEverySeededSchulungAsActive() {
        assertThat(ansicht.findeSchulungen(null, null))
                .isNotEmpty()
                .allSatisfy(s -> assertThat(s.zustand()).isEqualTo(Schulungszustand.AKTIV));
    }

    /**
     * TEST_KAT_SICHT_02: Archivierte Schulungen erscheinen gekennzeichnet und
     * nach allen aktiven.
     */
    // verifies: TEST_KAT_SICHT_02
    @Test
    void shouldListArchivedSchulungenMarkedAndAfterAllActiveOnes() {
        zustaende.archiviere(SchulungId.von("SCH-001"), LocalDate.of(2026, 3, 1));
        zustaende.archiviere(SchulungId.von("SCH-003"), LocalDate.of(2026, 3, 1));

        List<Schulung> alle = ansicht.findeSchulungen(null, null);

        List<String> archivierte = alle.stream()
                .filter(s -> s.zustand() == Schulungszustand.ARCHIVIERT)
                .map(Schulung::id)
                .toList();
        assertThat(archivierte).containsExactly("SCH-001", "SCH-003");

        int ersteArchivierte = indexDerErstenArchivierten(alle);
        assertThat(alle.subList(0, ersteArchivierte))
                .allSatisfy(s -> assertThat(s.zustand()).isEqualTo(Schulungszustand.AKTIV));
        assertThat(alle.subList(ersteArchivierte, alle.size()))
                .allSatisfy(s -> assertThat(s.zustand()).isEqualTo(Schulungszustand.ARCHIVIERT));
    }

    @Test
    void shouldKeepArchivedSchulungenReachableBySearchAndFilter() {
        zustaende.archiviere(SchulungId.von("SCH-001"), LocalDate.of(2026, 3, 1));

        assertThat(ansicht.findeSchulungen("scrum master", null))
                .extracting(Schulung::id)
                .contains("SCH-001");
    }

    /** REQ_KAT_KATG_05: Die Kategorien kommen aus der gepflegten Liste. */
    @Test
    void shouldTakeCategoriesFromTheMaintainedListNotFromTheSchulungen() {
        assertThat(ansicht.findeKategorien())
                .isNotEmpty()
                .doesNotHaveDuplicates()
                .isSortedAccordingTo(String::compareTo)
                .contains("Agile & Projektmanagement", "Cloud & DevOps");
    }

    /**
     * TEST_KAT_TERM_02: Ein Termin, dessen Schulung es im Katalog nicht gibt,
     * wird erkannt und gemeldet -- statt stillschweigend ohne Schulungsdaten
     * aufzutauchen.
     */
    // verifies: TEST_KAT_TERM_02
    @Test
    void shouldDetectTerminePointingAtAMissingSchulung() {
        assertThat(ansicht.verwaisteTermine()).isEmpty();

        jdbcTemplate.update(
                "INSERT INTO termin (termin_id, schulung_id, startdatum, enddatum, ort, "
                        + "format, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
                "SCH-404-T1", "SCH-404", LocalDate.of(2026, 5, 4), LocalDate.of(2026, 5, 5),
                "Köln", "Präsenz", "geplant");

        assertThat(ansicht.verwaisteTermine())
                .singleElement()
                .satisfies(verwaist -> {
                    assertThat(verwaist.terminId()).isEqualTo("SCH-404-T1");
                    assertThat(verwaist.schulungId()).isEqualTo("SCH-404");
                });
    }

    /** Ein verwaister Termin darf die Katalogansicht nicht sprengen. */
    @Test
    void shouldNotLetAnOrphanedTerminAppearInTheCatalogView() {
        jdbcTemplate.update(
                "INSERT INTO termin (termin_id, schulung_id, startdatum, enddatum, ort, "
                        + "format, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
                "SCH-404-T1", "SCH-404", LocalDate.of(2026, 5, 4), LocalDate.of(2026, 5, 5),
                "Köln", "Präsenz", "geplant");

        assertThat(ansicht.findeSchulungen(null, null))
                .extracting(Schulung::id)
                .doesNotContain("SCH-404");
    }

    // --- Hilfsmittel -------------------------------------------------------

    private Schulung eine(String id) {
        return ansicht.findeSchulungen(null, null).stream()
                .filter(s -> s.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Schulung " + id + " fehlt im Katalog."));
    }

    private static int indexDerErstenArchivierten(List<Schulung> alle) {
        for (int i = 0; i < alle.size(); i++) {
            if (alle.get(i).zustand() == Schulungszustand.ARCHIVIERT) {
                return i;
            }
        }
        return alle.size();
    }
}
