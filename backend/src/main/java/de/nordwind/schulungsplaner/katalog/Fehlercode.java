package de.nordwind.schulungsplaner.katalog;

/**
 * Der Grund, aus dem eine Eingabe abgewiesen wurde. REQ_KAT_IMP_02 verlangt,
 * dass dem Administrator gesagt wird, woran es lag -- ein nacktes "ungueltig"
 * genuegt dafuer nicht. Der Code traegt den Grund maschinenlesbar, die
 * Meldung im {@link Feldfehler} traegt ihn fuer Menschen.
 */
public enum Fehlercode {
    PFLICHTANGABE_FEHLT,
    ID_VERGEBEN,
    ID_UNVERAENDERLICH,
    ID_UNERLAUBTE_ZEICHEN,
    KATEGORIE_UNBEKANNT,
    KATEGORIE_VERGEBEN,
    KATEGORIE_IN_GEBRAUCH,
    DAUER_ZU_KLEIN,
    TEILNEHMERZAHL_NEGATIV,
    HOECHSTZAHL_NICHT_UEBER_MINDESTZAHL,
    VORAUSSETZUNG_LEER
}
