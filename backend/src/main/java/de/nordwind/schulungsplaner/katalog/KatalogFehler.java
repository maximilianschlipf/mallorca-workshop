package de.nordwind.schulungsplaner.katalog;

import java.util.List;

/**
 * Eine Katalogaenderung wurde abgewiesen.
 *
 * <p>Traegt die Befunde je Feld mit, weil REQ_KAT_IMP_02 verlangt, dass dem
 * Administrator gesagt wird, woran es lag.
 */
public class KatalogFehler extends RuntimeException {

    private final transient List<Feldfehler> fehler;
    private final boolean konflikt;

    private KatalogFehler(List<Feldfehler> fehler, boolean konflikt) {
        super(fehler.stream().map(Feldfehler::meldung).reduce("", (a, b) -> a + " " + b).trim());
        this.fehler = List.copyOf(fehler);
        this.konflikt = konflikt;
    }

    /** Die Eingabe genuegt den Regeln des Katalogs nicht. */
    public static KatalogFehler abgewiesen(List<Feldfehler> fehler) {
        return new KatalogFehler(fehler, false);
    }

    /**
     * Die Eingabe ist in sich stimmig, steht aber im Widerspruch zum
     * vorhandenen Bestand -- etwa eine bereits vergebene ID.
     */
    public static KatalogFehler konflikt(Feldfehler fehler) {
        return new KatalogFehler(List.of(fehler), true);
    }

    public List<Feldfehler> fehler() {
        return fehler;
    }

    public boolean istKonflikt() {
        return konflikt;
    }
}
