package de.nordwind.schulungsplaner.katalog;

import java.util.List;

/**
 * Was bei der Aufnahme bereitgestellter Dateien geschehen ist
 * (REQ_KAT_IMP_01).
 *
 * <p>Je Datei ein Eintrag. Eine abgewiesene Datei haelt die uebrigen nicht
 * auf: Der Administrator soll den Stapel einmal einreichen und danach
 * vollstaendig wissen, was daraus geworden ist, statt Datei um Datei einzeln
 * vorgesetzt zu bekommen.
 */
public record Aufnahmebericht(
        int aufgenommen,
        int abgewiesen,
        int entscheidungOffen,
        List<Eintrag> ergebnisse
) {

    public Aufnahmebericht {
        ergebnisse = List.copyOf(ergebnisse);
    }

    public static Aufnahmebericht aus(List<Eintrag> ergebnisse) {
        return new Aufnahmebericht(
                zaehle(ergebnisse, Ergebnis.AUFGENOMMEN) + zaehle(ergebnisse, Ergebnis.ERSETZT),
                zaehle(ergebnisse, Ergebnis.ABGEWIESEN),
                zaehle(ergebnisse, Ergebnis.ENTSCHEIDUNG_OFFEN),
                ergebnisse);
    }

    private static int zaehle(List<Eintrag> ergebnisse, Ergebnis gesucht) {
        return (int) ergebnisse.stream().filter(e -> e.ergebnis() == gesucht).count();
    }

    public record Eintrag(String dateiname, String schulungId,
                          Ergebnis ergebnis, List<Feldfehler> fehler) {

        public Eintrag {
            fehler = List.copyOf(fehler);
        }

        static Eintrag aufgenommen(String dateiname, SchulungId id, boolean ersetzt) {
            return new Eintrag(dateiname, id.wert(),
                    ersetzt ? Ergebnis.ERSETZT : Ergebnis.AUFGENOMMEN, List.of());
        }

        static Eintrag abgewiesen(String dateiname, List<Feldfehler> fehler) {
            return new Eintrag(dateiname, null, Ergebnis.ABGEWIESEN, fehler);
        }

        static Eintrag entscheidungOffen(String dateiname, SchulungId id, Feldfehler fehler) {
            return new Eintrag(dateiname, id.wert(),
                    Ergebnis.ENTSCHEIDUNG_OFFEN, List.of(fehler));
        }
    }

    public enum Ergebnis {
        AUFGENOMMEN,
        ERSETZT,
        ABGEWIESEN,
        /** Die ID ist vergeben; ob ersetzt wird, entscheidet der Administrator. */
        ENTSCHEIDUNG_OFFEN
    }
}
