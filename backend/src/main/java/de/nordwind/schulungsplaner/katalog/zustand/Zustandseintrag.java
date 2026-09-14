package de.nordwind.schulungsplaner.katalog.zustand;

import de.nordwind.schulungsplaner.katalog.SchulungId;

import java.time.LocalDate;

/**
 * Der Zustand einer Schulung samt Aenderungszaehler.
 *
 * @param archiviertAm der Tag der Archivierung, sonst {@code null}. Er
 *                     entscheidet, ob eine archivierte Schulung schon
 *                     geloescht werden darf (REQ_KAT_LOE_02).
 * @param version      der Zaehler aus REQ_DAT_NEBEN_02, mit dem ein Speichern
 *                     auf ueberholtem Stand erkannt wird
 */
public record Zustandseintrag(
        SchulungId schulungId,
        Schulungszustand zustand,
        LocalDate archiviertAm,
        long version
) {

    public boolean istAktiv() {
        return zustand == Schulungszustand.AKTIV;
    }

    public boolean istArchiviert() {
        return zustand == Schulungszustand.ARCHIVIERT;
    }
}
