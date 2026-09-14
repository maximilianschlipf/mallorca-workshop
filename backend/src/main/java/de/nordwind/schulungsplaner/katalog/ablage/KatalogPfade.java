package de.nordwind.schulungsplaner.katalog.ablage;

import de.nordwind.schulungsplaner.katalog.SchulungId;

import java.nio.file.Path;

/**
 * Loest die Orte im Katalogverzeichnis auf (REQ_KAT_ABL_02).
 *
 * <pre>
 * &lt;wurzel&gt;/
 * ├── kategorien.json
 * └── schulungen/
 *     └── SCH-001.json
 * </pre>
 *
 * <p>Die Wurzel ist konfigurierbar. Das ist kein Selbstzweck: Tests zeigen
 * damit auf ein temporaeres Verzeichnis, sodass kein Test in das Repository
 * des Projekts schreibt.
 */
public record KatalogPfade(Path wurzel) {

    public static final String SCHULUNGEN_VERZEICHNIS = "schulungen";
    public static final String KATEGORIEN_DATEI = "kategorien.json";

    public KatalogPfade {
        wurzel = wurzel.toAbsolutePath().normalize();
    }

    public Path schulungenVerzeichnis() {
        return wurzel.resolve(SCHULUNGEN_VERZEICHNIS);
    }

    public Path kategorienDatei() {
        return wurzel.resolve(KATEGORIEN_DATEI);
    }

    /**
     * Der Ablageort einer Schulung. Der Dateiname entsteht aus der bereits
     * geprueften {@link SchulungId}, deren Alphabet weder Punkt noch
     * Schraegstrich enthaelt -- der Pfad kann das Verzeichnis daher nicht
     * verlassen (REQ_KAT_ID_03).
     */
    public Path dateiFuer(SchulungId id) {
        return schulungenVerzeichnis().resolve(id.dateiname());
    }
}
