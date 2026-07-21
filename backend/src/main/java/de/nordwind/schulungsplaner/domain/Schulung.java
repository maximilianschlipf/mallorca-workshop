package de.nordwind.schulungsplaner.domain;

import java.util.List;

public record Schulung(
        String id,
        String titel,
        String kategorie,
        String kurzbeschreibung,
        List<String> voraussetzungen,
        int dauerInTagen,
        int mindestteilnehmerExklusiv,
        Integer maxTeilnehmerOeffentlich,
        List<Termin> oeffentlicheTermine
) {
}
