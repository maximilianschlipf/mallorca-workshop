package de.nordwind.schulungsplaner.katalog;

/**
 * Ein Hinweis, der eine Aenderung begleitet, ohne sie zu verhindern.
 *
 * <p>REQ_KAT_PFLEG_04 ist der Anlass: Wird die Dauer einer Schulung
 * geaendert, zu der bereits Termine geplant sind, wird der Administrator
 * gewarnt -- die Aenderung geht trotzdem durch.
 */
public record Warnung(Warncode code, String meldung) {

    public enum Warncode {
        DAUER_GEAENDERT_MIT_TERMINEN
    }
}
