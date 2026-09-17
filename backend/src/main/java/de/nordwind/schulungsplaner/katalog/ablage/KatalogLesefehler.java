package de.nordwind.schulungsplaner.katalog.ablage;

import java.nio.file.Path;

/**
 * Eine Katalogdatei liess sich nicht lesen.
 *
 * <p>Die Dateien liegen versioniert im Repository, werden von Hand bearbeitet
 * und kommen bei der Aufnahme von aussen herein. Ein Syntaxfehler darf
 * deshalb nicht als leerer Katalog durchgehen, sondern muss auffallen -- und
 * die Meldung muss die betroffene Datei nennen.
 */
public class KatalogLesefehler extends RuntimeException {

    private final transient Path datei;

    public KatalogLesefehler(Path datei, Throwable ursache) {
        super("Die Katalogdatei '" + datei + "' ließ sich nicht lesen: "
                + ursache.getMessage(), ursache);
        this.datei = datei;
    }

    public Path datei() {
        return datei;
    }
}
