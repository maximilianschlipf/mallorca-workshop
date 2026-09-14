package de.nordwind.schulungsplaner.katalog;

import java.util.regex.Pattern;

/**
 * Die Kennung einer Schulung. Sie wird vom Menschen vergeben
 * (REQ_KAT_ID_01), ist nach dem Anlegen unveraenderlich (REQ_KAT_PFLEG_03)
 * und benennt zugleich die Katalogdatei.
 *
 * <p>Erlaubt sind ausschliesslich Grossbuchstaben, Ziffern und Bindestriche
 * (REQ_KAT_ID_03). Weil dieses Alphabet weder Punkt noch Schraegstrich
 * enthaelt, kann ein daraus gebildeter Dateiname das Ablageverzeichnis nicht
 * verlassen. Das ist der Zweck der Einschraenkung, nicht ein Nebeneffekt.
 */
public record SchulungId(String wert) implements Comparable<SchulungId> {

    /** Entspricht der Spaltenbreite von {@code schulung_zustand.schulung_id}. */
    public static final int MAX_LAENGE = 50;

    private static final Pattern ERLAUBT = Pattern.compile("[A-Z0-9-]+");

    public SchulungId {
        if (!istGueltig(wert)) {
            throw new IllegalArgumentException(
                    "Unzulaessige Schulungs-ID: '" + wert + "'. " + REGEL);
        }
    }

    public static final String REGEL =
            "Erlaubt sind Grossbuchstaben, Ziffern und Bindestriche, hoechstens "
                    + MAX_LAENGE + " Zeichen.";

    public static SchulungId von(String wert) {
        return new SchulungId(wert);
    }

    public static boolean istGueltig(String wert) {
        return wert != null
                && !wert.isEmpty()
                && wert.length() <= MAX_LAENGE
                && ERLAUBT.matcher(wert).matches();
    }

    /** Der Name der Katalogdatei dieser Schulung. */
    public String dateiname() {
        return wert + ".json";
    }

    @Override
    public int compareTo(SchulungId andere) {
        return wert.compareTo(andere.wert);
    }

    @Override
    public String toString() {
        return wert;
    }
}
