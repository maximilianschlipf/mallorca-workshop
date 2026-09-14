package de.nordwind.schulungsplaner.katalog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Prueft Eingabematerial gegen die Regeln des Katalogs.
 *
 * <p>Diese Klasse kennt weder Spring noch Dateisystem noch Datenbank. Das ist
 * Absicht: REQ_KAT_IMP_02 verlangt, dass eine aufzunehmende Datei gegen
 * <em>dieselben</em> Regeln geprueft wird wie eine Eingabe ueber die
 * Oberflaeche. Beide Wege gehen deshalb durch diese eine Stelle.
 *
 * <p>Die Pruefung bricht nicht beim ersten Fehler ab, sondern sammelt alle.
 * Wer ein Formular ausfuellt, soll nicht Fehler um Fehler einzeln vorgesetzt
 * bekommen.
 */
public final class SchulungPruefung {

    private SchulungPruefung() {
    }

    public static Pruefergebnis pruefe(Schulungseingabe eingabe,
                                       Collection<String> bekannteKategorien) {
        List<Feldfehler> fehler = new ArrayList<>();

        SchulungId id = pruefeId(eingabe.id(), fehler);
        String titel = pflichttext("titel", eingabe.titel(), fehler);
        String kurzbeschreibung =
                pflichttext("kurzbeschreibung", eingabe.kurzbeschreibung(), fehler);
        String kategorie = pruefeKategorie(eingabe.kategorie(), bekannteKategorien, fehler);
        List<String> voraussetzungen = pruefeVoraussetzungen(eingabe.voraussetzungen(), fehler);
        Integer dauer = pruefeDauer(eingabe.dauerInTagen(), fehler);
        Integer mindest = pruefeMindestteilnehmer(eingabe.mindestteilnehmerExklusiv(), fehler);
        Integer hoechst = pruefeHoechstteilnehmer(
                eingabe.maxTeilnehmerOeffentlich(), mindest, fehler);

        if (!fehler.isEmpty()) {
            return new Pruefergebnis.Abgewiesen(fehler);
        }
        return new Pruefergebnis.Angenommen(new Katalogschulung(
                id, titel, kategorie, kurzbeschreibung, voraussetzungen,
                dauer, mindest, hoechst));
    }

    private static SchulungId pruefeId(String wert, List<Feldfehler> fehler) {
        if (istLeer(wert)) {
            fehler.add(pflichtangabeFehlt("id"));
            return null;
        }
        if (!SchulungId.istGueltig(wert)) {
            fehler.add(new Feldfehler("id", Fehlercode.ID_UNERLAUBTE_ZEICHEN,
                    "Die Schulungs-ID '" + wert + "' ist unzulaessig. " + SchulungId.REGEL));
            return null;
        }
        return SchulungId.von(wert);
    }

    private static String pruefeKategorie(String wert,
                                          Collection<String> bekannte,
                                          List<Feldfehler> fehler) {
        if (istLeer(wert)) {
            fehler.add(pflichtangabeFehlt("kategorie"));
            return null;
        }
        String getrimmt = wert.trim();
        if (!bekannte.contains(getrimmt)) {
            fehler.add(new Feldfehler("kategorie", Fehlercode.KATEGORIE_UNBEKANNT,
                    "Die Kategorie '" + getrimmt + "' steht nicht in der Kategorienliste. "
                            + "Eine neue Kategorie entsteht nur ueber die Pflege der Liste."));
            return null;
        }
        return getrimmt;
    }

    private static List<String> pruefeVoraussetzungen(List<String> werte,
                                                      List<Feldfehler> fehler) {
        if (werte == null || werte.isEmpty()) {
            return List.of();
        }
        if (werte.stream().anyMatch(SchulungPruefung::istLeer)) {
            fehler.add(new Feldfehler("voraussetzungen", Fehlercode.VORAUSSETZUNG_LEER,
                    "Eine Voraussetzung ohne Text ist nicht zulaessig. "
                            + "Ohne Voraussetzungen bleibt die Liste leer."));
            return List.of();
        }
        // Freitext bleibt unveraendert -- kein Trimmen (REQ_KAT_FELD_02).
        return List.copyOf(werte);
    }

    private static Integer pruefeDauer(Integer wert, List<Feldfehler> fehler) {
        if (wert == null) {
            fehler.add(pflichtangabeFehlt("dauerInTagen"));
            return null;
        }
        if (wert < 1) {
            fehler.add(new Feldfehler("dauerInTagen", Fehlercode.DAUER_ZU_KLEIN,
                    "Die Dauer betraegt mindestens einen Tag, angegeben war " + wert + "."));
            return null;
        }
        return wert;
    }

    private static Integer pruefeMindestteilnehmer(Integer wert, List<Feldfehler> fehler) {
        if (wert == null) {
            fehler.add(pflichtangabeFehlt("mindestteilnehmerExklusiv"));
            return null;
        }
        if (wert < 0) {
            fehler.add(new Feldfehler("mindestteilnehmerExklusiv",
                    Fehlercode.TEILNEHMERZAHL_NEGATIV,
                    "Die Mindestteilnehmerzahl kann nicht negativ sein, angegeben war "
                            + wert + "."));
            return null;
        }
        return wert;
    }

    /**
     * Fehlende Angabe und 0 bedeuten beide "keine Obergrenze" (REQ_KAT_FELD_04)
     * und werden deshalb einheitlich zu {@code null} vereinheitlicht -- sonst
     * fuehrte der Katalog zwei Schreibweisen fuer denselben Sachverhalt.
     */
    private static Integer pruefeHoechstteilnehmer(Integer wert,
                                                   Integer mindest,
                                                   List<Feldfehler> fehler) {
        if (wert == null || wert == 0) {
            return null;
        }
        if (wert < 0) {
            fehler.add(new Feldfehler("maxTeilnehmerOeffentlich",
                    Fehlercode.TEILNEHMERZAHL_NEGATIV,
                    "Die Hoechstteilnehmerzahl kann nicht negativ sein, angegeben war "
                            + wert + "."));
            return null;
        }
        if (mindest != null && wert <= mindest) {
            fehler.add(new Feldfehler("maxTeilnehmerOeffentlich",
                    Fehlercode.HOECHSTZAHL_NICHT_UEBER_MINDESTZAHL,
                    "Die Hoechstteilnehmerzahl " + wert + " muss ueber der "
                            + "Mindestteilnehmerzahl " + mindest + " liegen."));
            return null;
        }
        return wert;
    }

    private static String pflichttext(String feld, String wert, List<Feldfehler> fehler) {
        if (istLeer(wert)) {
            fehler.add(pflichtangabeFehlt(feld));
            return null;
        }
        return wert.trim();
    }

    private static Feldfehler pflichtangabeFehlt(String feld) {
        return new Feldfehler(feld, Fehlercode.PFLICHTANGABE_FEHLT,
                "Die Angabe '" + feld + "' ist eine Pflichtangabe und fehlt.");
    }

    private static boolean istLeer(String wert) {
        return wert == null || wert.isBlank();
    }
}
