package de.nordwind.schulungsplaner.katalog;

import java.util.List;

/**
 * Ungeprueftes Eingabematerial fuer eine Schulung -- aus einem Formular oder
 * aus einer aufzunehmenden Datei (REQ_KAT_IMP_02). Jedes Feld darf fehlen,
 * damit {@link SchulungPruefung} eine fehlende Pflichtangabe als solche
 * melden kann statt an einem Standardwert zu scheitern.
 *
 * <p>Deshalb sind auch Dauer und Mindestteilnehmerzahl {@code Integer} und
 * nicht {@code int}: 0 ist eine Eingabe, keine fehlende Angabe.
 */
public record Schulungseingabe(
        String id,
        String titel,
        String kategorie,
        String kurzbeschreibung,
        List<String> voraussetzungen,
        Integer dauerInTagen,
        Integer mindestteilnehmerExklusiv,
        Integer maxTeilnehmerOeffentlich
) {

    public Schulungseingabe mitId(String wert) {
        return new Schulungseingabe(wert, titel, kategorie, kurzbeschreibung,
                voraussetzungen, dauerInTagen, mindestteilnehmerExklusiv, maxTeilnehmerOeffentlich);
    }

    public Schulungseingabe mitTitel(String wert) {
        return new Schulungseingabe(id, wert, kategorie, kurzbeschreibung,
                voraussetzungen, dauerInTagen, mindestteilnehmerExklusiv, maxTeilnehmerOeffentlich);
    }

    public Schulungseingabe mitKategorie(String wert) {
        return new Schulungseingabe(id, titel, wert, kurzbeschreibung,
                voraussetzungen, dauerInTagen, mindestteilnehmerExklusiv, maxTeilnehmerOeffentlich);
    }

    public Schulungseingabe mitKurzbeschreibung(String wert) {
        return new Schulungseingabe(id, titel, kategorie, wert,
                voraussetzungen, dauerInTagen, mindestteilnehmerExklusiv, maxTeilnehmerOeffentlich);
    }

    public Schulungseingabe mitVoraussetzungen(List<String> wert) {
        return new Schulungseingabe(id, titel, kategorie, kurzbeschreibung,
                wert, dauerInTagen, mindestteilnehmerExklusiv, maxTeilnehmerOeffentlich);
    }

    public Schulungseingabe mitDauer(Integer wert) {
        return new Schulungseingabe(id, titel, kategorie, kurzbeschreibung,
                voraussetzungen, wert, mindestteilnehmerExklusiv, maxTeilnehmerOeffentlich);
    }

    public Schulungseingabe mitMindestteilnehmer(Integer wert) {
        return new Schulungseingabe(id, titel, kategorie, kurzbeschreibung,
                voraussetzungen, dauerInTagen, wert, maxTeilnehmerOeffentlich);
    }

    public Schulungseingabe mitMaxTeilnehmer(Integer wert) {
        return new Schulungseingabe(id, titel, kategorie, kurzbeschreibung,
                voraussetzungen, dauerInTagen, mindestteilnehmerExklusiv, wert);
    }
}
