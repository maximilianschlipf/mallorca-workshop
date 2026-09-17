package de.nordwind.schulungsplaner.katalog.ablage;

import java.nio.file.Path;
import java.util.Collection;

/**
 * Sichert eine Katalogaenderung in der Versionsgeschichte (REQ_KAT_ABL_03).
 *
 * <p>Der Aufrufer bestimmt, was in einen Commit gehoert. Das ist wesentlich:
 * Archivieren und Reaktivieren aendern nur den Zustand in der Datenbank und
 * beruehren den Katalog nicht -- sie rufen diese Schnittstelle gar nicht erst
 * auf, und so bleibt die Zahl der Commits unveraendert (TEST_KAT_ABL_03).
 */
public interface KatalogCommitter {

    /**
     * Sichert genau die genannten Dateien. Wurde an keiner von ihnen etwas
     * geaendert, entsteht kein Commit.
     *
     * @param nachricht die Commit-Nachricht
     * @param dateien   die betroffenen Dateien; geloeschte eingeschlossen
     */
    void sichere(String nachricht, Collection<Path> dateien);
}
