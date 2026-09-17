package de.nordwind.schulungsplaner.seed;

import java.util.List;

/**
 * Die Termine des Seeds. Die Beschreibung der Schulungen steht nicht mehr
 * hier, sondern im versionierten Katalog (DEC_DAT_ABLAGE_02); ein Termin
 * verweist nur noch ueber die Schulungs-ID darauf (REQ_KAT_TERM_01).
 */
public record TermineSeedRoot(List<TerminSeed> termine) {

    public record TerminSeed(
            String terminId,
            String schulungId,
            String startdatum,
            String enddatum,
            String ort,
            String format,
            String status,
            String trainerId
    ) {
    }
}
