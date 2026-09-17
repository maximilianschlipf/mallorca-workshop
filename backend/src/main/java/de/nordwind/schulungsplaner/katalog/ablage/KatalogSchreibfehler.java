package de.nordwind.schulungsplaner.katalog.ablage;

import java.nio.file.Path;

/** Eine Katalogdatei liess sich nicht schreiben oder entfernen. */
public class KatalogSchreibfehler extends RuntimeException {

    public KatalogSchreibfehler(Path datei, Throwable ursache) {
        super("Die Katalogdatei '" + datei + "' ließ sich nicht schreiben: "
                + ursache.getMessage(), ursache);
    }
}
