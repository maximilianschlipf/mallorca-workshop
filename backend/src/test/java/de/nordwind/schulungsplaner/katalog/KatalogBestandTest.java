package de.nordwind.schulungsplaner.katalog;

import de.nordwind.schulungsplaner.katalog.ablage.KatalogPfade;
import de.nordwind.schulungsplaner.katalog.ablage.KatalogRepository;
import de.nordwind.schulungsplaner.katalog.ablage.KategorienRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueft den eingecheckten Katalog selbst.
 *
 * <p>Die Dateien unter {@code katalog/} sind von Hand aus dem alten Seed
 * entstanden und werden kuenftig auch von Hand bearbeitet werden. Dieser Test
 * stellt sicher, dass sie den Regeln genuegen, die die Anwendung an eine
 * Eingabe stellt -- und dass ihr Format genau das ist, das die Anwendung
 * selbst schreiben wuerde. Sonst erzeugte die erste Aenderung ueber die
 * Oberflaeche einen Diff ueber die ganze Datei.
 */
class KatalogBestandTest {

    private static final KatalogPfade PFADE = new KatalogPfade(Path.of("..", "katalog"));

    private final KatalogRepository katalog = new KatalogRepository(PFADE);
    private final KategorienRepository kategorien = new KategorienRepository(PFADE);

    @Test
    void shouldContainReadableSchulungen() {
        assertThat(katalog.ladeAlle()).isNotEmpty();
    }

    /** Jede Katalogdatei genuegt denselben Regeln wie eine Eingabe von Hand. */
    @Test
    void shouldSatisfyTheSameRulesAsManualInput() {
        Set<String> bekannteKategorien = Set.copyOf(kategorien.ladeAlle());

        assertThat(katalog.ladeAlle()).allSatisfy(schulung -> {
            Pruefergebnis ergebnis = SchulungPruefung.pruefe(
                    new Schulungseingabe(
                            schulung.id().wert(),
                            schulung.titel(),
                            schulung.kategorie(),
                            schulung.kurzbeschreibung(),
                            schulung.voraussetzungen(),
                            schulung.dauerInTagen(),
                            schulung.mindestteilnehmerExklusiv(),
                            schulung.maxTeilnehmerOeffentlich()),
                    bekannteKategorien);

            assertThat(ergebnis)
                    .as("Katalogdatei %s", schulung.id())
                    .isInstanceOf(Pruefergebnis.Angenommen.class);
        });
    }

    /**
     * Die abgelegten Dateien sind bitgleich mit dem, was die Anwendung
     * schreiben wuerde.
     */
    @Test
    void shouldBeFormattedExactlyAsTheApplicationWritesIt(@TempDir Path vergleich)
            throws IOException {
        KatalogRepository neuGeschrieben =
                new KatalogRepository(new KatalogPfade(vergleich));

        for (Katalogschulung schulung : katalog.ladeAlle()) {
            Path erwartet = neuGeschrieben.speichere(schulung);
            Path vorhanden = PFADE.dateiFuer(schulung.id());

            assertThat(Files.readString(vorhanden))
                    .as("Format von %s", schulung.id())
                    .isEqualTo(Files.readString(erwartet));
        }
    }

    /** REQ_KAT_KATG_02: Jede benutzte Kategorie steht in der gepflegten Liste. */
    @Test
    void shouldOnlyUseCategoriesFromTheMaintainedList() {
        List<String> liste = kategorien.ladeAlle();

        assertThat(liste).isNotEmpty();
        assertThat(katalog.ladeAlle())
                .allSatisfy(schulung -> assertThat(liste).contains(schulung.kategorie()));
    }
}
