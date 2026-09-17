package de.nordwind.schulungsplaner.katalog.ablage;

/** Eine Katalogaenderung liess sich nicht in der Versionsgeschichte sichern. */
public class KatalogCommitFehler extends RuntimeException {

    public KatalogCommitFehler(String nachricht, Throwable ursache) {
        super("Die Katalogaenderung '" + nachricht
                + "' ließ sich nicht als Commit sichern: " + ursache.getMessage(), ursache);
    }
}
