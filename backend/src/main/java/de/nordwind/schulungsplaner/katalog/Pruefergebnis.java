package de.nordwind.schulungsplaner.katalog;

import java.util.List;

/**
 * Das Ergebnis einer Pruefung: entweder eine gepruefte Schulung oder die
 * Liste der Gruende, aus denen es keine gibt. Beides zugleich gibt es nicht,
 * deshalb eine versiegelte Schnittstelle statt eines Records mit zwei
 * Feldern, von denen immer eines leer ist.
 */
public sealed interface Pruefergebnis {

    record Angenommen(Katalogschulung schulung) implements Pruefergebnis {
    }

    record Abgewiesen(List<Feldfehler> fehler) implements Pruefergebnis {
        public Abgewiesen {
            fehler = List.copyOf(fehler);
        }
    }
}
