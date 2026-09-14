package de.nordwind.schulungsplaner.katalog;

import de.nordwind.schulungsplaner.katalog.ablage.KatalogCommitter;
import de.nordwind.schulungsplaner.katalog.ablage.KatalogRepository;
import de.nordwind.schulungsplaner.katalog.ablage.KategorienRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Pflegt die Kategorienliste (REQ_KAT_KATG_01).
 *
 * <p>Eine neue Kategorie entsteht ausschliesslich hier und nie nebenbei beim
 * Anlegen einer Schulung (REQ_KAT_KATG_03). Der Grund steht in
 * REQ_KAT_KATG_02: Freitext an der Schulung liesse ueber die Zeit
 * "IT-Security" und "IT Security" nebeneinander entstehen, und der Filter
 * zerfiele in zwei halb gefuellte Gruppen.
 */
@Service
public class KategoriePflegeService {

    private final KategorienRepository kategorien;
    private final KatalogRepository katalog;
    private final KatalogCommitter committer;

    public KategoriePflegeService(KategorienRepository kategorien,
                                  KatalogRepository katalog,
                                  KatalogCommitter committer) {
        this.kategorien = kategorien;
        this.katalog = katalog;
        this.committer = committer;
    }

    public List<String> legeAn(String name) {
        String neu = pflichtname(name);
        List<String> bestand = kategorien.ladeAlle();
        if (bestand.contains(neu)) {
            throw KatalogFehler.konflikt(new Feldfehler("name", Fehlercode.KATEGORIE_VERGEBEN,
                    "Die Kategorie '" + neu + "' gibt es bereits."));
        }

        List<String> erweitert = new ArrayList<>(bestand);
        erweitert.add(neu);
        Path datei = kategorien.speichere(erweitert);
        committer.sichere("Kategorie '" + neu + "' angelegt", List.of(datei));

        return kategorien.ladeAlle();
    }

    /**
     * Benennt eine Kategorie um. Die Aenderung wirkt auf alle zugeordneten
     * Schulungen (REQ_KAT_KATG_01) -- und zwar in einem einzigen Commit:
     * Liste und Schulungen beschreiben denselben Vorgang, und ein halb
     * umbenannter Zwischenstand gehoert nicht in die Versionsgeschichte.
     */
    public List<String> benenneUm(String name, String neuerName) {
        String alt = pflichtname(name);
        String neu = pflichtname(neuerName, "neuerName");

        List<String> bestand = kategorien.ladeAlle();
        verlangeVorhanden(bestand, alt);
        if (!alt.equals(neu) && bestand.contains(neu)) {
            throw KatalogFehler.konflikt(new Feldfehler("neuerName",
                    Fehlercode.KATEGORIE_VERGEBEN,
                    "Die Kategorie '" + neu + "' gibt es bereits."));
        }

        List<String> umbenannt = bestand.stream()
                .map(vorhanden -> vorhanden.equals(alt) ? neu : vorhanden)
                .toList();

        List<Path> betroffen = new ArrayList<>();
        betroffen.add(kategorien.speichere(umbenannt));
        for (Katalogschulung schulung : katalog.ladeAlle()) {
            if (schulung.kategorie().equals(alt)) {
                betroffen.add(katalog.speichere(new Katalogschulung(
                        schulung.id(), schulung.titel(), neu, schulung.kurzbeschreibung(),
                        schulung.voraussetzungen(), schulung.dauerInTagen(),
                        schulung.mindestteilnehmerExklusiv(),
                        schulung.maxTeilnehmerOeffentlich())));
            }
        }
        committer.sichere(
                "Kategorie '" + alt + "' in '" + neu + "' umbenannt", betroffen);

        return kategorien.ladeAlle();
    }

    /**
     * Loescht eine Kategorie. Eine Kategorie, der mindestens eine Schulung
     * zugeordnet ist, bleibt erhalten (REQ_KAT_KATG_04) -- sonst truege die
     * Schulung eine Kategorie, die es nicht mehr gibt.
     */
    public void loesche(String name) {
        String zuLoeschen = pflichtname(name);
        List<String> bestand = kategorien.ladeAlle();
        verlangeVorhanden(bestand, zuLoeschen);

        List<String> zugeordnet = katalog.ladeAlle().stream()
                .filter(schulung -> schulung.kategorie().equals(zuLoeschen))
                .map(schulung -> schulung.id().wert())
                .toList();
        if (!zugeordnet.isEmpty()) {
            throw KatalogFehler.konflikt(new Feldfehler("name",
                    Fehlercode.KATEGORIE_IN_GEBRAUCH,
                    "Die Kategorie '" + zuLoeschen + "' ist noch " + zugeordnet.size()
                            + " Schulungen zugeordnet: " + String.join(", ", zugeordnet)
                            + ". Haenge sie um oder benenne die Kategorie um."));
        }

        Path datei = kategorien.speichere(bestand.stream()
                .filter(vorhanden -> !vorhanden.equals(zuLoeschen))
                .toList());
        committer.sichere("Kategorie '" + zuLoeschen + "' geloescht", List.of(datei));
    }

    private static void verlangeVorhanden(List<String> bestand, String name) {
        if (!bestand.contains(name)) {
            throw KatalogFehler.abgewiesen(List.of(new Feldfehler("name",
                    Fehlercode.KATEGORIE_UNBEKANNT,
                    "Die Kategorie '" + name + "' steht nicht in der Kategorienliste.")));
        }
    }

    private static String pflichtname(String name) {
        return pflichtname(name, "name");
    }

    private static String pflichtname(String name, String feld) {
        if (name == null || name.isBlank()) {
            throw KatalogFehler.abgewiesen(List.of(new Feldfehler(feld,
                    Fehlercode.PFLICHTANGABE_FEHLT,
                    "Der Name einer Kategorie ist eine Pflichtangabe und fehlt.")));
        }
        return name.trim();
    }
}
