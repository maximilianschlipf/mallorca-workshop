package de.nordwind.schulungsplaner;

import de.nordwind.schulungsplaner.service.KontoFehler;
import de.nordwind.schulungsplaner.service.TerminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TerminZeitenIntegrationTest.FesteZeit.class)
@Transactional
class TerminZeitenIntegrationTest {
    static final String ADMIN = "TRN-001";
    static final String TRAINER = "TRN-005";
    static final LocalDate TAG = LocalDate.of(2028, 3, 6);

    @Autowired TerminService termine;
    @Autowired JdbcTemplate jdbc;

    static class FesteZeit {
        @Bean @Primary Clock testClock() {
            return Clock.fixed(Instant.parse("2026-09-17T10:00:00Z"), ZoneId.of("Europe/Berlin"));
        }
    }

    @BeforeEach
    void freierTag() {
        jdbc.update("DELETE FROM termin WHERE startdatum<=? AND enddatum>=?", TAG, TAG);
    }

    // verifies: TEST_TER_ZEIT_01
    @Test
    void terminMitUhrzeitWirdMitZeitenGespeichertUndAngezeigt() {
        var termin = termine.anlegen(ADMIN, eingabe("SCH-001", zeit(9, 0), zeit(10, 30)));
        assertThat(termin.startzeit()).isEqualTo(LocalTime.of(9, 0));
        assertThat(termin.endzeit()).isEqualTo(LocalTime.of(10, 30));
        assertThat(termine.details(ADMIN, termin.terminId()).startzeit()).isEqualTo(LocalTime.of(9, 0));
    }

    // verifies: TEST_TER_ZEIT_02
    @Test
    void terminOhneUhrzeitBleibtGanztaegig() {
        var termin = termine.anlegen(ADMIN, eingabe("SCH-001", null, null));
        assertThat(termin.startzeit()).isNull();
        assertThat(termin.endzeit()).isNull();
    }

    // verifies: TEST_TER_ZEIT_03
    @Test
    void ungueltigeUhrzeitenWerdenAbgewiesen() {
        assertFehler("ZEIT_UNVOLLSTAENDIG", () -> termine.anlegen(ADMIN, eingabe("SCH-001", zeit(9, 0), null)));
        assertFehler("ZEIT_UNVOLLSTAENDIG", () -> termine.anlegen(ADMIN, eingabe("SCH-001", null, zeit(10, 0))));
        assertFehler("ZEIT_UNGUELTIG", () -> termine.anlegen(ADMIN, eingabe("SCH-001", zeit(10, 0), zeit(9, 0))));
        assertFehler("ZEIT_UNGUELTIG", () -> termine.anlegen(ADMIN, eingabe("SCH-001", zeit(10, 0), zeit(10, 0))));
        assertFehler("ZEIT_RASTER", () -> termine.anlegen(ADMIN, eingabe("SCH-001", zeit(9, 3), zeit(10, 0))));
    }

    // verifies: TEST_TER_ZEIT_04
    @Test
    void zweiTermineDesselbenTrainersAmSelbenTagOhnePauseWerdenAbgewiesen() {
        zugewiesen("SCH-001", zeit(9, 0), zeit(10, 45));
        var zweiter = termine.anlegen(ADMIN, eingabe("SCH-006", zeit(10, 45), zeit(12, 0)));
        assertFehler("TRAINER_NICHT_VERFUEGBAR", () -> termine.trainerZuweisen(ADMIN, zweiter.terminId(), TRAINER, false));
    }

    // verifies: TEST_TER_ZEIT_05
    @Test
    void pauseVonFuenfzehnMinutenGenuegtFuerEinenFolgetermin() {
        zugewiesen("SCH-001", zeit(9, 0), zeit(10, 45));
        var zweiter = termine.anlegen(ADMIN, eingabe("SCH-006", zeit(11, 0), zeit(12, 30)));
        termine.trainerZuweisen(ADMIN, zweiter.terminId(), TRAINER, false);
        assertThat(termine.details(ADMIN, zweiter.terminId()).trainerId()).isEqualTo(TRAINER);
    }

    // verifies: TEST_TER_ZEIT_06
    @Test
    void ueberschneidendeUhrzeitenSindEinKonfliktMitHinweisAufDieUeberschneidung() {
        zugewiesen("SCH-001", zeit(9, 0), zeit(10, 30));
        var zweiter = termine.anlegen(ADMIN, eingabe("SCH-006", zeit(10, 0), zeit(11, 0)));
        assertThatThrownBy(() -> termine.trainerZuweisen(ADMIN, zweiter.terminId(), TRAINER, false))
                .isInstanceOfSatisfying(KontoFehler.class,
                        fehler -> assertThat(fehler.getMessage()).contains("bereits einem anderen Termin"));
    }

    // verifies: TEST_TER_ZEIT_07
    @Test
    void ganztaegigerTerminBlockiertJedeUhrzeitAmSelbenTag() {
        zugewiesen("SCH-001", null, null);
        var zweiter = termine.anlegen(ADMIN, eingabe("SCH-006", zeit(15, 0), zeit(16, 0)));
        assertFehler("TRAINER_NICHT_VERFUEGBAR", () -> termine.trainerZuweisen(ADMIN, zweiter.terminId(), TRAINER, false));
    }

    // verifies: TEST_TER_ZEIT_08
    @Test
    void trainerOptionenZeigenPausenkonfliktFuerDasGewuenschteZeitfenster() {
        zugewiesen("SCH-001", zeit(9, 0), zeit(10, 45));
        var zuNah = termine.trainerOptionen(ADMIN, "SCH-006", TAG, TAG, zeit(10, 50), zeit(12, 0));
        var frei = termine.trainerOptionen(ADMIN, "SCH-006", TAG, TAG, zeit(11, 0), zeit(12, 0));
        assertThat(zuNah).filteredOn(o -> o.id().equals(TRAINER)).singleElement()
                .satisfies(o -> {
                    assertThat(o.verfuegbar()).isFalse();
                    assertThat(o.grund()).contains("15 Minuten Pause");
                });
        assertThat(frei).filteredOn(o -> o.id().equals(TRAINER)).singleElement()
                .satisfies(o -> assertThat(o.verfuegbar()).isTrue());
    }

    // verifies: TEST_TER_ZEIT_09
    @Test
    void zeitAendernPrueftDieBestehendeZuweisungErneut() {
        zugewiesen("SCH-001", zeit(9, 0), zeit(10, 0));
        var zweiter = termine.anlegen(ADMIN, eingabe("SCH-006", zeit(11, 0), zeit(12, 0)));
        termine.trainerZuweisen(ADMIN, zweiter.terminId(), TRAINER, false);
        assertFehler("TRAINER_NICHT_VERFUEGBAR",
                () -> termine.aendern(ADMIN, zweiter.terminId(), eingabe("SCH-006", zeit(10, 5), zeit(12, 0))));
        var verschoben = termine.aendern(ADMIN, zweiter.terminId(), eingabe("SCH-006", zeit(10, 15), zeit(12, 0)));
        assertThat(verschoben.startzeit()).isEqualTo(LocalTime.of(10, 15));
    }

    // verifies: TEST_TER_ZEIT_10
    @Test
    void zeitAenderungWirdDemTrainerMitgeteilt() {
        var termin = zugewiesen("SCH-001", zeit(9, 0), zeit(10, 0));
        termine.aendern(ADMIN, termin.terminId(), eingabe("SCH-001", zeit(13, 0), zeit(14, 0)));
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM benachrichtigung WHERE empfaenger_id=? AND anlass LIKE 'Uhrzeit: 09:00–10:00 → 13:00–14:00'
                """, Integer.class, TRAINER)).isEqualTo(1);
    }

    private TerminService.TerminAnsicht zugewiesen(String schulung, LocalTime start, LocalTime ende) {
        var termin = termine.anlegen(ADMIN, eingabe(schulung, start, ende));
        termine.trainerZuweisen(ADMIN, termin.terminId(), TRAINER, false);
        return termin;
    }

    private static TerminService.TerminEingabe eingabe(String schulung, LocalTime start, LocalTime ende) {
        return new TerminService.TerminEingabe(schulung, TAG, TAG, start, ende,
                null, null, null, null, null, null, false);
    }

    private static LocalTime zeit(int stunde, int minute) { return LocalTime.of(stunde, minute); }

    private static void assertFehler(String code, Runnable aktion) {
        assertThatThrownBy(aktion::run).isInstanceOfSatisfying(KontoFehler.class,
                fehler -> assertThat(fehler.code()).isEqualTo(code));
    }
}
