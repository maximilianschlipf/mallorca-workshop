package de.nordwind.schulungsplaner.katalog.zustand;

/**
 * Ob eine Schulung angeboten wird oder nicht (REQ_KAT_ARCH_01).
 *
 * <p>Der Zustand steht in der Datenbank und nicht im Katalog: Er aendert sich
 * im Betrieb, und sein Wechsel soll die Katalogdatei nicht anfassen und
 * keinen Commit erzeugen (REQ_KAT_ABL_03).
 */
public enum Schulungszustand {
    AKTIV,
    ARCHIVIERT
}
