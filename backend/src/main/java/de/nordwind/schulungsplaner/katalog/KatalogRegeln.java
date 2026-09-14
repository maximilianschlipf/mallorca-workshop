package de.nordwind.schulungsplaner.katalog;

import de.nordwind.schulungsplaner.katalog.ablage.KatalogRepository;
import de.nordwind.schulungsplaner.katalog.zustand.SchulungszustandRepository;
import de.nordwind.schulungsplaner.katalog.zustand.Zustandseintrag;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Auskuenfte des Katalogs, die andere Bereiche brauchen.
 *
 * <p>Die Regeln gehoeren hierher und nicht dorthin, wo sie angewandt werden:
 * Ob eine Schulung angeboten wird, weiss der Katalog. Die Terminplanung fragt
 * nach, statt den Zustand selbst auszuwerten.
 */
@Service
public class KatalogRegeln {

    private final KatalogRepository katalog;
    private final SchulungszustandRepository zustaende;

    public KatalogRegeln(KatalogRepository katalog, SchulungszustandRepository zustaende) {
        this.katalog = katalog;
        this.zustaende = zustaende;
    }

    /**
     * Stellt sicher, dass zu dieser Schulung ein neuer Termin angelegt werden
     * darf (REQ_KAT_ARCH_03).
     *
     * @throws SchulungNichtGefunden wenn es die Schulung im Katalog nicht gibt
     * @throws KatalogFehler         wenn sie archiviert ist
     */
    public void verlangeTerminfaehig(SchulungId id) {
        if (!katalog.existiert(id)) {
            throw new SchulungNichtGefunden(id);
        }
        Zustandseintrag eintrag = zustaende.lade(id).orElse(null);
        if (eintrag != null && eintrag.istArchiviert()) {
            throw KatalogFehler.konflikt(new Feldfehler("schulungId",
                    Fehlercode.SCHULUNG_ARCHIVIERT,
                    "Die Schulung '" + id + "' ist archiviert. Zu einer archivierten "
                            + "Schulung koennen keine neuen Termine angelegt werden."));
        }
    }

    /** Die Kennungen aller Schulungen, zu denen neue Termine moeglich sind. */
    public List<SchulungId> terminfaehigeSchulungen() {
        return katalog.alleIds().stream()
                .filter(id -> zustaende.lade(id).map(Zustandseintrag::istAktiv).orElse(true))
                .toList();
    }
}
