package de.nordwind.schulungsplaner.katalog;

/**
 * Ein einzelner Befund der Pruefung: welches Feld, welcher Grund, und eine
 * Meldung, die den Grund benennt (REQ_KAT_IMP_02).
 */
public record Feldfehler(String feld, Fehlercode code, String meldung) {
}
