package de.nordwind.schulungsplaner.service;

public enum Vorgangsart {
    ABWESENHEITSANTRAG("Abwesenheitsantrag", false, true),
    VORMERKUNG("Vormerkung auf einen Termin", true, true),
    ASSISTENZBEWERBUNG("Bewerbung auf einen Assistenzplatz", false, true),
    UEBERNAHMEANFRAGE("Übernahmeanfrage", false, true),
    ERSATZTRAINER_ANFRAGE("Ersatztrainer-Anfrage bei Abwesenheit", false, false);

    private final String bezeichnung;
    private final boolean ablehnungsgrundPflicht;
    private final boolean zurueckziehbar;

    Vorgangsart(String bezeichnung, boolean ablehnungsgrundPflicht, boolean zurueckziehbar) {
        this.bezeichnung = bezeichnung;
        this.ablehnungsgrundPflicht = ablehnungsgrundPflicht;
        this.zurueckziehbar = zurueckziehbar;
    }

    public String bezeichnung() { return bezeichnung; }
    public boolean ablehnungsgrundPflicht() { return ablehnungsgrundPflicht; }
    public boolean zurueckziehbar() { return zurueckziehbar; }
}
