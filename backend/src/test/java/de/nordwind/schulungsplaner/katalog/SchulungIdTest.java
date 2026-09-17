package de.nordwind.schulungsplaner.katalog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Prueft REQ_KAT_ID_03 (erlaubte Zeichen) und die daraus folgende
 * Absicherung des Ablageorts. Gegenstueck in der Anforderungsdoku:
 * TEST_KAT_ID_01.
 */
class SchulungIdTest {

    @ParameterizedTest
    @ValueSource(strings = {"SCH-009", "ABC-1", "A", "9", "SCH-009-B"})
    void shouldAcceptUppercaseDigitsAndHyphens(String wert) {
        assertThat(SchulungId.von(wert).wert()).isEqualTo(wert);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Scrum / Basis",
            "sch-009",
            "SCH_009",
            "../SCH-009",
            "SCH.009",
            "SCH 009",
            "SCH/009",
            "SCH\\009",
            "SCH-00Ä"
    })
    void shouldRejectEverythingOutsideTheAllowedAlphabet(String wert) {
        assertThat(SchulungId.istGueltig(wert)).isFalse();
        assertThatThrownBy(() -> SchulungId.von(wert))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(wert);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldRejectMissingId(String wert) {
        assertThat(SchulungId.istGueltig(wert)).isFalse();
    }

    @Test
    void shouldRejectIdLongerThanTheDatabaseColumn() {
        String gerade_noch = "A".repeat(SchulungId.MAX_LAENGE);
        String zu_lang = "A".repeat(SchulungId.MAX_LAENGE + 1);

        assertThat(SchulungId.istGueltig(gerade_noch)).isTrue();
        assertThat(SchulungId.istGueltig(zu_lang)).isFalse();
    }

    /**
     * Der Dateiname entsteht ausschliesslich aus der geprueften ID. Weil das
     * Alphabet weder Punkt noch Schraegstrich enthaelt, kann er den
     * Ablageort nicht verlassen -- die Begruendung hinter REQ_KAT_ID_03.
     */
    @Test
    void shouldDeriveFileNameFromId() {
        assertThat(SchulungId.von("SCH-009").dateiname()).isEqualTo("SCH-009.json");
    }

    @Test
    void shouldCompareByValue() {
        assertThat(SchulungId.von("SCH-001")).isEqualTo(SchulungId.von("SCH-001"));
        assertThat(SchulungId.von("SCH-001")).isNotEqualTo(SchulungId.von("SCH-002"));
    }
}
