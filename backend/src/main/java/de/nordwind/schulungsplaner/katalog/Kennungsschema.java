package de.nordwind.schulungsplaner.katalog;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Beschreibt, nach welchem Schema die bestehenden Schulungen benannt sind.
 *
 * <p>REQ_KAT_ID_01 laesst die ID bewusst vom Menschen vergeben und verlangt
 * zugleich, ihm beim Anlegen zu zeigen, wie die vorhandenen Kennungen
 * aussehen. Das ist eine Hilfe, keine Vorgabe: Eine ID, die dem Muster nicht
 * folgt, ist zulaessig, solange sie dem Alphabet aus REQ_KAT_ID_03 genuegt.
 *
 * @param muster    das vorherrschende Muster, Buchstaben als {@code A} und
 *                  Ziffern als {@code 9}, oder {@code null} bei leerem Katalog
 * @param beispiele hoechstens {@link #MAX_BEISPIELE} bestehende Kennungen
 */
public record Kennungsschema(String muster, List<String> beispiele) {

    public static final int MAX_BEISPIELE = 3;

    public Kennungsschema {
        beispiele = List.copyOf(beispiele);
    }

    public static Kennungsschema ausBestehenden(Collection<String> bestehendeIds) {
        if (bestehendeIds.isEmpty()) {
            return new Kennungsschema(null, List.of());
        }
        List<String> sortiert = bestehendeIds.stream().sorted().toList();
        return new Kennungsschema(
                haeufigstesMuster(sortiert),
                sortiert.stream().limit(MAX_BEISPIELE).toList()
        );
    }

    private static String haeufigstesMuster(List<String> ids) {
        Map<String, Long> haeufigkeit = ids.stream()
                .collect(Collectors.groupingBy(Kennungsschema::musterVon, Collectors.counting()));
        return haeufigkeit.entrySet().stream()
                // Bei Gleichstand entscheidet das alphabetisch erste Muster,
                // damit die Anzeige zwischen zwei Aufrufen nicht springt.
                .max(Comparator.<Map.Entry<String, Long>, Long>comparing(Map.Entry::getValue)
                        .thenComparing(Map.Entry::getKey, Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .orElseThrow();
    }

    private static String musterVon(String id) {
        StringBuilder muster = new StringBuilder(id.length());
        for (char zeichen : id.toCharArray()) {
            muster.append(Character.isDigit(zeichen) ? '9'
                    : Character.isLetter(zeichen) ? 'A'
                    : zeichen);
        }
        return muster.toString();
    }
}
