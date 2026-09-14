package de.nordwind.schulungsplaner.katalog.ablage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Haelt die Kategorienliste als eigene JSON-Datei im Katalogverzeichnis
 * (REQ_KAT_KATG_05).
 *
 * <p>Die Liste wird gepflegt, nicht aus den Schulungen abgeleitet. Nur so
 * kann es eine Kategorie geben, der noch keine Schulung zugeordnet ist -- und
 * nur so laesst sich eine Kategorie beim Anlegen einer Schulung auswaehlen,
 * statt sie nebenbei entstehen zu lassen (REQ_KAT_KATG_03).
 *
 * <p>Gespeichert wird doppelfrei und alphabetisch sortiert, wie
 * REQ_KAT_SUCH_06 es fuer die Auswahl verlangt. Das geschieht beim
 * Schreiben, damit die Datei selbst schon in dieser Ordnung im Repository
 * liegt und ihr Diff der Aenderung entspricht.
 */
public class KategorienRepository {

    private final KatalogPfade pfade;

    public KategorienRepository(KatalogPfade pfade) {
        this.pfade = pfade;
    }

    public List<String> ladeAlle() {
        Path datei = pfade.kategorienDatei();
        if (!Files.isRegularFile(datei)) {
            return List.of();
        }
        try {
            KategorienDatei inhalt = KatalogJson.mapper()
                    .readValue(Files.readString(datei), KategorienDatei.class);
            return sortiertUndDoppelfrei(inhalt.kategorien());
        } catch (IOException | RuntimeException ex) {
            throw new KatalogLesefehler(datei, ex);
        }
    }

    /** Der Filter trifft die Kategorie genau (REQ_KAT_SUCH_03). */
    public boolean kennt(String kategorie) {
        return ladeAlle().contains(kategorie);
    }

    /** Schreibt die Liste und liefert den Ablageort fuer den Commit. */
    public Path speichere(Collection<String> kategorien) {
        Path datei = pfade.kategorienDatei();
        try {
            Files.createDirectories(datei.getParent());
            Files.writeString(datei, KatalogJson.schreibe(
                    new KategorienDatei(sortiertUndDoppelfrei(kategorien))));
            return datei;
        } catch (IOException ex) {
            throw new KatalogSchreibfehler(datei, ex);
        }
    }

    private static List<String> sortiertUndDoppelfrei(Collection<String> kategorien) {
        if (kategorien == null) {
            return List.of();
        }
        Set<String> geordnet = new TreeSet<>(kategorien);
        return List.copyOf(geordnet);
    }

    record KategorienDatei(List<String> kategorien) {
    }
}
