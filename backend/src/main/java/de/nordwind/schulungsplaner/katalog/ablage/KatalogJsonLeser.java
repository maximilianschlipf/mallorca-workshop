package de.nordwind.schulungsplaner.katalog.ablage;

import de.nordwind.schulungsplaner.katalog.Schulungseingabe;

/**
 * Liest den Inhalt einer bereitgestellten Katalogdatei als ungepruefte
 * Eingabe.
 *
 * <p>Bewusst nach {@link Schulungseingabe} und nicht nach
 * {@link de.nordwind.schulungsplaner.katalog.Katalogschulung}: Eine Datei von
 * aussen ist Eingabematerial und hat die Pruefung noch vor sich. Faende die
 * Umwandlung direkt in die geprueften Typen statt, scheiterte sie schon an
 * einer unerlaubten ID -- mit einer Meldung aus dem Serialisierungswerkzeug
 * statt mit der Begruendung, die REQ_KAT_IMP_02 verlangt.
 */
public final class KatalogJsonLeser {

    private KatalogJsonLeser() {
    }

    public static Schulungseingabe lies(String inhalt) {
        return KatalogJson.mapper().readValue(inhalt, Schulungseingabe.class);
    }
}
