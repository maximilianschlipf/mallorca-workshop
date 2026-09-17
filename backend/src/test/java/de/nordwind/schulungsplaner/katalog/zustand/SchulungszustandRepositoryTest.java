package de.nordwind.schulungsplaner.katalog.zustand;

import de.nordwind.schulungsplaner.katalog.SchulungId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * REQ_KAT_FELD_01: Der Zustand aktiv oder archiviert steht in der Datenbank,
 * nicht im Katalog. REQ_DAT_NEBEN_02: Jeder veraenderliche Datensatz fuehrt
 * einen Zaehler, der bei jedem Speichern erhoeht wird.
 */
@SpringBootTest
@Transactional
class SchulungszustandRepositoryTest {

    private static final SchulungId NEU = SchulungId.von("SCH-901");

    @Autowired
    private SchulungszustandRepository repository;

    /** REQ_KAT_PFLEG_01: Eine neu angelegte Schulung ist aktiv. */
    @Test
    void shouldCreateANewSchulungInTheActiveState() {
        repository.legeAn(NEU);

        assertThat(repository.lade(NEU))
                .get()
                .satisfies(eintrag -> {
                    assertThat(eintrag.zustand()).isEqualTo(Schulungszustand.AKTIV);
                    assertThat(eintrag.archiviertAm()).isNull();
                    assertThat(eintrag.version()).isZero();
                });
    }

    @Test
    void shouldReportAnUnknownSchulungAsAbsent() {
        assertThat(repository.lade(SchulungId.von("SCH-999"))).isEmpty();
    }

    /** REQ_KAT_ARCH_01 und REQ_KAT_LOE_02: Der Zeitpunkt wird festgehalten. */
    @Test
    void shouldRecordWhenASchulungWasArchived() {
        repository.legeAn(NEU);

        repository.archiviere(NEU, LocalDate.of(2026, 3, 1));

        assertThat(repository.lade(NEU))
                .get()
                .satisfies(eintrag -> {
                    assertThat(eintrag.zustand()).isEqualTo(Schulungszustand.ARCHIVIERT);
                    assertThat(eintrag.archiviertAm()).isEqualTo(LocalDate.of(2026, 3, 1));
                    assertThat(eintrag.version()).isEqualTo(1);
                });
    }

    /** REQ_KAT_ARCH_04: Reaktivieren loescht den Archivierungszeitpunkt. */
    @Test
    void shouldClearTheArchivalDateWhenReactivating() {
        repository.legeAn(NEU);
        repository.archiviere(NEU, LocalDate.of(2026, 3, 1));

        repository.reaktiviere(NEU);

        assertThat(repository.lade(NEU))
                .get()
                .satisfies(eintrag -> {
                    assertThat(eintrag.zustand()).isEqualTo(Schulungszustand.AKTIV);
                    assertThat(eintrag.archiviertAm()).isNull();
                    assertThat(eintrag.version()).isEqualTo(2);
                });
    }

    /**
     * Ein Zustandssatz kann ohne Katalogdatei zurueckbleiben: Wer den Katalog
     * ueber das Repository abgleicht (REQ_KAT_ABL_04), entfernt Dateien an der
     * Anwendung vorbei. Wird die Kennung danach neu angelegt, darf das nicht
     * an einem Schluesselkonflikt scheitern -- der Katalog entscheidet, was es
     * gibt, die Datenbank haelt nur fest, was davon angeboten wird.
     */
    @Test
    void shouldReuseALeftoverStateRowInsteadOfFailing() {
        repository.legeAn(NEU);
        repository.archiviere(NEU, LocalDate.of(2026, 3, 1));

        repository.legeAn(NEU);

        assertThat(repository.lade(NEU))
                .get()
                .satisfies(eintrag -> {
                    assertThat(eintrag.zustand()).isEqualTo(Schulungszustand.AKTIV);
                    assertThat(eintrag.archiviertAm()).isNull();
                });
    }

    @Test
    void shouldRemoveTheState() {
        repository.legeAn(NEU);

        assertThat(repository.entferne(NEU)).isTrue();

        assertThat(repository.lade(NEU)).isEmpty();
        assertThat(repository.entferne(NEU)).isFalse();
    }

    /** Der Seed legt fuer jede Schulung des Katalogs einen Zustand an. */
    @Test
    void shouldKnowTheStateOfEverySeededSchulung() {
        assertThat(repository.alleZustaende())
                .containsKey(SchulungId.von("SCH-001"))
                .allSatisfy((id, eintrag) ->
                        assertThat(eintrag.zustand()).isEqualTo(Schulungszustand.AKTIV));
    }
}
