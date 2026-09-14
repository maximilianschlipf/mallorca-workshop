package de.nordwind.schulungsplaner.katalog.ablage;

import de.nordwind.schulungsplaner.katalog.Katalogschulung;
import de.nordwind.schulungsplaner.katalog.SchulungId;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Haelt den Schulungskatalog als JSON-Dateien, je Schulung eine
 * (REQ_KAT_ABL_01).
 *
 * <p>Diese Klasse schreibt nur Dateien. Der Commit, den REQ_KAT_ABL_03
 * verlangt, ist Sache des {@link KatalogCommitter} und wird eine Ebene
 * darueber ausgeloest -- Archivieren etwa schreibt keine Datei und darf
 * deshalb auch keinen Commit erzeugen.
 */
public class KatalogRepository {

    private static final String ENDUNG = ".json";

    private final KatalogPfade pfade;

    public KatalogRepository(KatalogPfade pfade) {
        this.pfade = pfade;
    }

    /** Der Ablageort einer Schulung -- etwa um ihn nach dem Loeschen zu committen. */
    public Path dateiFuer(SchulungId id) {
        return pfade.dateiFuer(id);
    }

    public Optional<Katalogschulung> lade(SchulungId id) {
        Path datei = pfade.dateiFuer(id);
        if (!Files.isRegularFile(datei)) {
            return Optional.empty();
        }
        return Optional.of(leseDatei(datei));
    }

    public boolean existiert(SchulungId id) {
        return Files.isRegularFile(pfade.dateiFuer(id));
    }

    /** Der ganze Katalog, nach ID sortiert. */
    public List<Katalogschulung> ladeAlle() {
        return alleIds().stream()
                .map(pfade::dateiFuer)
                .map(this::leseDatei)
                .toList();
    }

    /**
     * Die Kennungen aller abgelegten Schulungen, sortiert.
     *
     * <p>Beruecksichtigt werden ausschliesslich Dateien, deren Name aus einer
     * gueltigen Schulungs-ID und der Endung besteht. Eine Notiz oder eine
     * Sicherungskopie im Verzeichnis gehoert nicht zum Katalog.
     */
    public List<SchulungId> alleIds() {
        Path verzeichnis = pfade.schulungenVerzeichnis();
        if (!Files.isDirectory(verzeichnis)) {
            return List.of();
        }
        try (Stream<Path> eintraege = Files.list(verzeichnis)) {
            return eintraege
                    .filter(Files::isRegularFile)
                    .map(datei -> datei.getFileName().toString())
                    .filter(name -> name.endsWith(ENDUNG))
                    .map(name -> name.substring(0, name.length() - ENDUNG.length()))
                    .filter(SchulungId::istGueltig)
                    .map(SchulungId::von)
                    .sorted(Comparator.naturalOrder())
                    .toList();
        } catch (IOException ex) {
            throw new UncheckedIOException(
                    "Das Katalogverzeichnis '" + verzeichnis + "' liess sich nicht lesen.", ex);
        }
    }

    /** Schreibt die Schulung und liefert den Ablageort fuer den Commit. */
    public Path speichere(Katalogschulung schulung) {
        Path datei = pfade.dateiFuer(schulung.id());
        try {
            Files.createDirectories(datei.getParent());
            Files.writeString(datei, KatalogJson.schreibe(schulung));
            return datei;
        } catch (IOException ex) {
            throw new KatalogSchreibfehler(datei, ex);
        }
    }

    /** Liefert {@code false}, wenn es nichts zu loeschen gab. */
    public boolean loesche(SchulungId id) {
        Path datei = pfade.dateiFuer(id);
        try {
            return Files.deleteIfExists(datei);
        } catch (IOException ex) {
            throw new KatalogSchreibfehler(datei, ex);
        }
    }

    private Katalogschulung leseDatei(Path datei) {
        try {
            return KatalogJson.mapper().readValue(
                    Files.readString(datei), Katalogschulung.class);
        } catch (IOException | RuntimeException ex) {
            throw new KatalogLesefehler(datei, ex);
        }
    }
}
