package de.nordwind.schulungsplaner.domain;

import de.nordwind.schulungsplaner.katalog.zustand.Schulungszustand;

import java.util.List;

/**
 * Eine Schulung, wie die Oberflaeche sie sieht: die Beschreibung aus dem
 * Katalog, der Zustand aus der Datenbank und die dazugehoerigen Termine.
 *
 * <p>Das ist die Zusammenfuehrung beider Ablagen, nicht der Katalog selbst --
 * der fuehrt ausschliesslich die Beschreibung
 * ({@link de.nordwind.schulungsplaner.katalog.Katalogschulung}).
 */
public record Schulung(
        String id,
        String titel,
        String kategorie,
        String kurzbeschreibung,
        List<String> voraussetzungen,
        int dauerInTagen,
        int mindestteilnehmerExklusiv,
        Integer maxTeilnehmerOeffentlich,
        Schulungszustand zustand,
        List<Termin> oeffentlicheTermine
) {
}
