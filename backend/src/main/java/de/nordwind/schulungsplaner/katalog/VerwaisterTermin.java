package de.nordwind.schulungsplaner.katalog;

/**
 * Ein Termin, dessen Schulungs-ID zu keiner Katalogdatei fuehrt.
 *
 * <p>REQ_KAT_TERM_02 verlangt, dass die Anwendung diesen Fall erkennt und
 * meldet, statt den Termin stillschweigend ohne Schulungsdaten anzuzeigen.
 * Entstehen kann er, wenn eine Katalogdatei ausserhalb der Anwendung entfernt
 * wurde -- etwa beim Abgleich ueber das Repository (REQ_KAT_ABL_04).
 */
public record VerwaisterTermin(String terminId, String schulungId) {
}
