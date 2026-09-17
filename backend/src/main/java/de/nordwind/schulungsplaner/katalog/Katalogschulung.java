package de.nordwind.schulungsplaner.katalog;

import java.util.List;

/**
 * Eine Schulung, wie der Katalog sie beschreibt -- genau die Felder aus
 * REQ_KAT_FELD_01 und kein Feld mehr.
 *
 * <p>Ausdruecklich <em>nicht</em> enthalten: der Zustand aktiv oder
 * archiviert, Termine, Trainer und Teilnehmer. Alles, was sich im Betrieb
 * aendert, steht in der Datenbank. Der Katalog ist reine Beschreibung.
 *
 * <p>Eine Instanz entsteht nur ueber {@link SchulungPruefung}; die Felder
 * sind damit bereits geprueft.
 */
public record Katalogschulung(
        SchulungId id,
        String titel,
        String kategorie,
        String kurzbeschreibung,
        List<String> voraussetzungen,
        int dauerInTagen,
        int mindestteilnehmerExklusiv,
        Integer maxTeilnehmerOeffentlich
) {

    public Katalogschulung {
        voraussetzungen = voraussetzungen == null ? List.of() : List.copyOf(voraussetzungen);
    }

    /**
     * Ob fuer oeffentliche Termine dieser Schulung eine Obergrenze gilt.
     * Fehlende Angabe und 0 bedeuten beide: keine (REQ_KAT_FELD_04).
     */
    public boolean hatObergrenze() {
        return maxTeilnehmerOeffentlich != null && maxTeilnehmerOeffentlich > 0;
    }
}
