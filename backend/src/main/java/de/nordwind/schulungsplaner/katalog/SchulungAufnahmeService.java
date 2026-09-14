package de.nordwind.schulungsplaner.katalog;

import de.nordwind.schulungsplaner.katalog.ablage.KatalogCommitter;
import de.nordwind.schulungsplaner.katalog.ablage.KatalogJsonLeser;
import de.nordwind.schulungsplaner.katalog.ablage.KatalogRepository;
import de.nordwind.schulungsplaner.katalog.ablage.KategorienRepository;
import de.nordwind.schulungsplaner.katalog.zustand.SchulungszustandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Nimmt bereitgestellte JSON-Dateien in den Katalog auf (REQ_KAT_IMP_01).
 *
 * <p>Die Dateien durchlaufen dieselbe {@link SchulungPruefung} wie eine
 * Eingabe ueber die Oberflaeche -- das ist der Kern von REQ_KAT_IMP_02 und
 * der Grund, warum die Pruefung eine Funktion ohne Spring und ohne
 * Dateisystem ist.
 */
@Service
public class SchulungAufnahmeService {

    private final KatalogRepository katalog;
    private final KategorienRepository kategorien;
    private final SchulungszustandRepository zustaende;
    private final KatalogCommitter committer;

    public SchulungAufnahmeService(KatalogRepository katalog,
                                   KategorienRepository kategorien,
                                   SchulungszustandRepository zustaende,
                                   KatalogCommitter committer) {
        this.katalog = katalog;
        this.kategorien = kategorien;
        this.zustaende = zustaende;
        this.committer = committer;
    }

    /**
     * @param ersetzen ob eine Datei mit bereits vergebener ID die bestehende
     *                 Schulung ersetzen darf. Ohne diese ausdrueckliche
     *                 Entscheidung bleibt die bestehende unveraendert
     *                 (REQ_KAT_IMP_03).
     */
    @Transactional
    public Aufnahmebericht nimmAuf(List<Aufzunehmen> dateien, boolean ersetzen) {
        List<String> bekannteKategorien = kategorien.ladeAlle();
        List<Aufnahmebericht.Eintrag> ergebnisse = new ArrayList<>();
        List<Path> geschrieben = new ArrayList<>();

        for (Aufzunehmen datei : dateien) {
            ergebnisse.add(verarbeite(datei, bekannteKategorien, ersetzen, geschrieben));
        }

        if (!geschrieben.isEmpty()) {
            // Ein Stapel ist ein Vorgang und damit ein Commit. Ein halb
            // aufgenommener Zwischenstand gehoert nicht in die Historie.
            committer.sichere(geschrieben.size() + " Schulungen aus Dateien aufgenommen",
                    geschrieben);
        }
        return Aufnahmebericht.aus(ergebnisse);
    }

    private Aufnahmebericht.Eintrag verarbeite(Aufzunehmen datei,
                                               List<String> bekannteKategorien,
                                               boolean ersetzen,
                                               List<Path> geschrieben) {
        Schulungseingabe eingabe;
        try {
            eingabe = KatalogJsonLeser.lies(datei.inhalt());
        } catch (RuntimeException ex) {
            return Aufnahmebericht.Eintrag.abgewiesen(datei.dateiname(), List.of(
                    new Feldfehler(null, Fehlercode.DATEI_UNLESBAR,
                            "Die Datei '" + datei.dateiname()
                                    + "' ist kein lesbares JSON: " + ex.getMessage())));
        }

        Pruefergebnis ergebnis = SchulungPruefung.pruefe(eingabe, bekannteKategorien);
        if (ergebnis instanceof Pruefergebnis.Abgewiesen abgewiesen) {
            return Aufnahmebericht.Eintrag.abgewiesen(datei.dateiname(), abgewiesen.fehler());
        }

        Katalogschulung schulung = ((Pruefergebnis.Angenommen) ergebnis).schulung();
        boolean vorhanden = katalog.existiert(schulung.id());
        if (vorhanden && !ersetzen) {
            return Aufnahmebericht.Eintrag.entscheidungOffen(datei.dateiname(), schulung.id(),
                    new Feldfehler("id", Fehlercode.ID_VERGEBEN,
                            "Die Schulungs-ID '" + schulung.id() + "' ist bereits vergeben. "
                                    + "Die bestehende Schulung bleibt unveraendert, bis Du "
                                    + "das Ersetzen ausdruecklich verlangst."));
        }

        geschrieben.add(katalog.speichere(schulung));
        if (!vorhanden) {
            zustaende.legeAn(schulung.id());
        }
        // Beim Ersetzen bleibt der Zustand, wie er ist: Die Aufnahme
        // beschreibt die Schulung neu, sie legt sie nicht neu an.
        return Aufnahmebericht.Eintrag.aufgenommen(datei.dateiname(), schulung.id(), vorhanden);
    }

    /** Eine aufzunehmende Datei: ihr Name und ihr Inhalt. */
    public record Aufzunehmen(String dateiname, String inhalt) {
    }
}
