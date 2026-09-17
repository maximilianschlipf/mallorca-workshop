package de.nordwind.schulungsplaner.katalog;

import de.nordwind.schulungsplaner.domain.Schulung;

import java.util.List;

/**
 * Das Ergebnis einer Katalogaenderung: die Schulung, wie sie danach dasteht,
 * und die Hinweise, die dabei entstanden sind.
 *
 * <p>Die Warnungen gehoeren mit in die Antwort und nicht in einen zweiten
 * Aufruf: REQ_KAT_PFLEG_04 warnt bei der Aenderung, ohne sie zu verhindern --
 * die Aenderung ist also bereits geschehen, wenn der Hinweis erscheint.
 */
public record Katalogantwort(Schulung schulung, List<Warnung> warnungen) {

    public Katalogantwort {
        warnungen = List.copyOf(warnungen);
    }

    public static Katalogantwort ohneWarnung(Schulung schulung) {
        return new Katalogantwort(schulung, List.of());
    }
}
