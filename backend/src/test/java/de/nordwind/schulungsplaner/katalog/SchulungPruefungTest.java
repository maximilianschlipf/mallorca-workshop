package de.nordwind.schulungsplaner.katalog;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueft die Regeln, die fuer die Eingabe ueber die Oberflaeche und fuer die
 * Aufnahme aus Dateien gleichermassen gelten (REQ_KAT_IMP_02). Gegenstuecke
 * in der Anforderungsdoku: TEST_KAT_ANL_02 bis TEST_KAT_ANL_06 und
 * TEST_KAT_ID_04.
 */
class SchulungPruefungTest {

    private static final Set<String> BEKANNTE_KATEGORIEN =
            Set.of("Agile & Projektmanagement", "Cloud & DevOps");

    @Nested
    class Pflichtangaben {

        /** TEST_KAT_ANL_02, erster Teil. */
        @Test
        void shouldAcceptInputWithAllMandatoryFields() {
            Pruefergebnis ergebnis = pruefe(vollstaendig());

            assertThat(ergebnis).isInstanceOf(Pruefergebnis.Angenommen.class);
            Katalogschulung schulung = angenommen(ergebnis);
            assertThat(schulung.id()).isEqualTo(SchulungId.von("SCH-009"));
            assertThat(schulung.titel()).isEqualTo("Scrum Master Zertifizierung");
            assertThat(schulung.kategorie()).isEqualTo("Agile & Projektmanagement");
            assertThat(schulung.kurzbeschreibung()).isEqualTo("Grundlagen der Rolle.");
            assertThat(schulung.dauerInTagen()).isEqualTo(2);
            assertThat(schulung.mindestteilnehmerExklusiv()).isEqualTo(6);
            assertThat(schulung.maxTeilnehmerOeffentlich()).isEqualTo(12);
        }

        /** TEST_KAT_ANL_02, zweiter Teil: jede Pflichtangabe einzeln weglassen. */
        @Test
        void shouldRejectEachMissingMandatoryFieldNamingIt() {
            assertThat(fehlerFelder(vollstaendig().mitId(null))).containsExactly("id");
            assertThat(fehlerFelder(vollstaendig().mitTitel(null))).containsExactly("titel");
            assertThat(fehlerFelder(vollstaendig().mitKategorie(null))).containsExactly("kategorie");
            assertThat(fehlerFelder(vollstaendig().mitKurzbeschreibung(null)))
                    .containsExactly("kurzbeschreibung");
            assertThat(fehlerFelder(vollstaendig().mitDauer(null))).containsExactly("dauerInTagen");
            assertThat(fehlerFelder(vollstaendig().mitMindestteilnehmer(null)))
                    .containsExactly("mindestteilnehmerExklusiv");
        }

        @Test
        void shouldTreatBlankTextAsMissing() {
            assertThat(fehlerFelder(vollstaendig().mitTitel("   "))).containsExactly("titel");
            assertThat(fehlerFelder(vollstaendig().mitKurzbeschreibung("  ")))
                    .containsExactly("kurzbeschreibung");
        }

        /** TEST_KAT_ANL_02, dritter Teil: die beiden freiwilligen Angaben. */
        @Test
        void shouldAcceptInputWithoutOptionalFields() {
            Pruefergebnis ergebnis = pruefe(vollstaendig()
                    .mitVoraussetzungen(null)
                    .mitMaxTeilnehmer(null));

            Katalogschulung schulung = angenommen(ergebnis);
            assertThat(schulung.voraussetzungen()).isEmpty();
            assertThat(schulung.maxTeilnehmerOeffentlich()).isNull();
        }

        @Test
        void shouldCollectAllErrorsInsteadOfStoppingAtTheFirst() {
            Pruefergebnis ergebnis = pruefe(vollstaendig()
                    .mitTitel(null)
                    .mitDauer(0)
                    .mitKategorie("Gibt es nicht"));

            assertThat(fehlerFelder(ergebnis))
                    .containsExactlyInAnyOrder("titel", "dauerInTagen", "kategorie");
        }
    }

    @Nested
    class Kennung {

        /** TEST_KAT_ID_04: ohne Eingabe einer ID wird abgewiesen, nichts vorbelegt. */
        @Test
        void shouldRejectMissingIdWithoutInventingOne() {
            Pruefergebnis ergebnis = pruefe(vollstaendig().mitId(null));

            assertThat(fehler(ergebnis))
                    .singleElement()
                    .satisfies(f -> {
                        assertThat(f.feld()).isEqualTo("id");
                        assertThat(f.code()).isEqualTo(Fehlercode.PFLICHTANGABE_FEHLT);
                    });
        }

        /** TEST_KAT_ID_01 auf Ebene der Pruefung. */
        @ParameterizedTest
        @ValueSource(strings = {"sch-009", "SCH_009", "../SCH-009", "Scrum / Basis"})
        void shouldRejectIdWithForbiddenCharacters(String id) {
            Pruefergebnis ergebnis = pruefe(vollstaendig().mitId(id));

            assertThat(fehler(ergebnis))
                    .singleElement()
                    .satisfies(f -> {
                        assertThat(f.feld()).isEqualTo("id");
                        assertThat(f.code()).isEqualTo(Fehlercode.ID_UNERLAUBTE_ZEICHEN);
                        assertThat(f.meldung()).contains("Grossbuchstaben", "Ziffern", "Bindestriche");
                    });
        }
    }

    @Nested
    class Dauer {

        /** TEST_KAT_ANL_03. */
        @ParameterizedTest
        @ValueSource(ints = {0, -1, -7})
        void shouldRejectDurationBelowOneDay(int dauer) {
            assertThat(fehler(pruefe(vollstaendig().mitDauer(dauer))))
                    .singleElement()
                    .satisfies(f -> assertThat(f.code()).isEqualTo(Fehlercode.DAUER_ZU_KLEIN));
        }

        @Test
        void shouldAcceptDurationOfOneDay() {
            assertThat(angenommen(pruefe(vollstaendig().mitDauer(1))).dauerInTagen()).isEqualTo(1);
        }
    }

    @Nested
    class Teilnehmergrenzen {

        /** TEST_KAT_ANL_04. */
        @Test
        void shouldRejectMaximumBelowMinimum() {
            Pruefergebnis ergebnis = pruefe(vollstaendig()
                    .mitMindestteilnehmer(6)
                    .mitMaxTeilnehmer(4));

            assertThat(fehler(ergebnis))
                    .singleElement()
                    .satisfies(f -> {
                        assertThat(f.feld()).isEqualTo("maxTeilnehmerOeffentlich");
                        assertThat(f.code())
                                .isEqualTo(Fehlercode.HOECHSTZAHL_NICHT_UEBER_MINDESTZAHL);
                    });
        }

        @Test
        void shouldAcceptMaximumAboveMinimum() {
            Katalogschulung schulung = angenommen(pruefe(vollstaendig()
                    .mitMindestteilnehmer(6)
                    .mitMaxTeilnehmer(12)));

            assertThat(schulung.maxTeilnehmerOeffentlich()).isEqualTo(12);
        }

        /** REQ_KAT_FELD_05: gleich gross genuegt nicht, es muss groesser sein. */
        @Test
        void shouldRejectMaximumEqualToMinimum() {
            assertThat(fehler(pruefe(vollstaendig()
                    .mitMindestteilnehmer(6)
                    .mitMaxTeilnehmer(6))))
                    .singleElement()
                    .satisfies(f -> assertThat(f.code())
                            .isEqualTo(Fehlercode.HOECHSTZAHL_NICHT_UEBER_MINDESTZAHL));
        }

        /** TEST_KAT_ANL_05: fehlend und 0 bedeuten beide "keine Obergrenze". */
        @Test
        void shouldTreatMissingAndZeroMaximumAsNoLimit() {
            Katalogschulung ohneAngabe = angenommen(pruefe(vollstaendig().mitMaxTeilnehmer(null)));
            Katalogschulung mitNull = angenommen(pruefe(vollstaendig().mitMaxTeilnehmer(0)));

            assertThat(ohneAngabe.maxTeilnehmerOeffentlich()).isNull();
            assertThat(mitNull.maxTeilnehmerOeffentlich()).isNull();
            assertThat(ohneAngabe.hatObergrenze()).isFalse();
            assertThat(mitNull.hatObergrenze()).isFalse();
        }

        @Test
        void shouldRejectNegativeParticipantCounts() {
            assertThat(fehlerFelder(vollstaendig().mitMindestteilnehmer(-1)))
                    .containsExactly("mindestteilnehmerExklusiv");
            assertThat(fehlerFelder(vollstaendig().mitMaxTeilnehmer(-1)))
                    .containsExactly("maxTeilnehmerOeffentlich");
        }
    }

    @Nested
    class Voraussetzungen {

        /** TEST_KAT_ANL_06: unveraenderter Freitext, kein Bezug auf andere Schulungen. */
        @Test
        void shouldKeepFreeTextExactlyAsEntered() {
            List<String> eingegeben = List.of("  Grundkenntnisse agiler Methoden  ", "SCH-001");

            Katalogschulung schulung =
                    angenommen(pruefe(vollstaendig().mitVoraussetzungen(eingegeben)));

            assertThat(schulung.voraussetzungen()).containsExactlyElementsOf(eingegeben);
        }

        @Test
        void shouldAcceptEmptyList() {
            assertThat(angenommen(pruefe(vollstaendig().mitVoraussetzungen(List.of())))
                    .voraussetzungen()).isEmpty();
        }

        @Test
        void shouldRejectBlankEntry() {
            assertThat(fehlerFelder(vollstaendig().mitVoraussetzungen(List.of("Sinnvoll", "   "))))
                    .containsExactly("voraussetzungen");
        }
    }

    @Nested
    class Kategorie {

        /** TEST_KAT_KATG_01 und REQ_KAT_IMP_04: nur aus der Liste, nie nebenbei angelegt. */
        @Test
        void shouldRejectCategoryOutsideTheMaintainedList() {
            assertThat(fehler(pruefe(vollstaendig().mitKategorie("IT Security"))))
                    .singleElement()
                    .satisfies(f -> {
                        assertThat(f.feld()).isEqualTo("kategorie");
                        assertThat(f.code()).isEqualTo(Fehlercode.KATEGORIE_UNBEKANNT);
                        assertThat(f.meldung()).contains("IT Security");
                    });
        }

        /** Der Filter trifft die Kategorie genau -- also auch hier exakt. */
        @Test
        void shouldMatchCategoryExactlyIncludingCase() {
            assertThat(fehlerFelder(vollstaendig().mitKategorie("cloud & devops")))
                    .containsExactly("kategorie");
        }
    }

    // --- Hilfsmittel -------------------------------------------------------

    private static Schulungseingabe vollstaendig() {
        return new Schulungseingabe(
                "SCH-009",
                "Scrum Master Zertifizierung",
                "Agile & Projektmanagement",
                "Grundlagen der Rolle.",
                List.of("Grundkenntnisse agiler Methoden"),
                2,
                6,
                12
        );
    }

    private static Pruefergebnis pruefe(Schulungseingabe eingabe) {
        return SchulungPruefung.pruefe(eingabe, BEKANNTE_KATEGORIEN);
    }

    private static Katalogschulung angenommen(Pruefergebnis ergebnis) {
        assertThat(ergebnis).isInstanceOf(Pruefergebnis.Angenommen.class);
        return ((Pruefergebnis.Angenommen) ergebnis).schulung();
    }

    private static List<Feldfehler> fehler(Pruefergebnis ergebnis) {
        assertThat(ergebnis).isInstanceOf(Pruefergebnis.Abgewiesen.class);
        return ((Pruefergebnis.Abgewiesen) ergebnis).fehler();
    }

    private static List<String> fehlerFelder(Schulungseingabe eingabe) {
        return fehlerFelder(pruefe(eingabe));
    }

    private static List<String> fehlerFelder(Pruefergebnis ergebnis) {
        return fehler(ergebnis).stream().map(Feldfehler::feld).toList();
    }
}
