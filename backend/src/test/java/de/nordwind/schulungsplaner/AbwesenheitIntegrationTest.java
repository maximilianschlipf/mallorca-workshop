package de.nordwind.schulungsplaner;

import de.nordwind.schulungsplaner.service.KontoFehler;
import de.nordwind.schulungsplaner.service.TerminService;
import de.nordwind.schulungsplaner.service.TrainereinsatzService;
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
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Aufgabe 1 (workshop/01-abwesenheiten): Kern -- STORY_ABW_ERF_02 / TEST_ABW_ERF_03
 * (eigene Abwesenheit ändern) -- und optionale Erweiterung STORY_ABW_ERF_03 /
 * TEST_ABW_ERF_04 (eigene Abwesenheit löschen).
 */
@SpringBootTest
@Import(AbwesenheitIntegrationTest.FesteZeit.class)
@Transactional
class AbwesenheitIntegrationTest {
    static final String TRAINER = "TRN-005";
    static final String ANDERER_TRAINER = "TRN-003";
    static final String ADMIN = "TRN-001";

    @Autowired TrainereinsatzService einsaetze;
    @Autowired TerminService termine;
    @Autowired JdbcTemplate jdbc;

    static class FesteZeit {
        @Bean @Primary Clock testClock() {
            return Clock.fixed(Instant.parse("2026-09-17T10:00:00Z"), ZoneId.of("Europe/Berlin"));
        }
    }

    @BeforeEach
    void keineAbwesenheitenDerTestkonten() {
        jdbc.update("DELETE FROM abwesenheit WHERE benutzerkonto_id IN (?, ?)", TRAINER, ANDERER_TRAINER);
    }

    // verifies: TEST_ABW_ERF_03
    @Test
    void eigeneAbwesenheitWirdMitNeuemZeitraumUndGrundGeaendert() {
        long id = abwesenheitAnlegen(TRAINER, "2029-01-08", "2029-01-09", "Konferenz");

        einsaetze.abwesenheitAendern(TRAINER, id, LocalDate.parse("2029-02-01"), LocalDate.parse("2029-02-03"), "Urlaub");

        assertThat(zeile(id)).isEqualTo(new AbwesenheitZeile(LocalDate.parse("2029-02-01"), LocalDate.parse("2029-02-03"), "Urlaub"));
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM abwesenheit WHERE benutzerkonto_id=? AND von='2029-01-08'",
                Integer.class, TRAINER)).isZero();
    }

    // verifies: TEST_ABW_ERF_03
    @Test
    void versuchEineFremdeAbwesenheitZuAendernWirdAbgewiesenUndLaesstSieUnveraendert() {
        long id = abwesenheitAnlegen(ANDERER_TRAINER, "2029-01-08", "2029-01-09", "Konferenz");

        assertThatThrownBy(() -> einsaetze.abwesenheitAendern(
                TRAINER, id, LocalDate.parse("2029-05-01"), LocalDate.parse("2029-05-02"), "Übernommen"))
                .isInstanceOfSatisfying(KontoFehler.class,
                        fehler -> assertThat(fehler.code()).isEqualTo("ABWESENHEIT_NICHT_GEFUNDEN"));

        assertThat(zeile(id)).isEqualTo(new AbwesenheitZeile(LocalDate.parse("2029-01-08"), LocalDate.parse("2029-01-09"), "Konferenz"));
    }

    @Test
    void eineNichtVorhandeneAbwesenheitWirdBeimAendernAbgewiesen() {
        assertFehler("ABWESENHEIT_NICHT_GEFUNDEN",
                () -> einsaetze.abwesenheitAendern(TRAINER, 999_999L,
                        LocalDate.parse("2029-01-01"), LocalDate.parse("2029-01-02"), null));
    }

    @Test
    void einUngueltigerZeitraumWirdBeimAendernAbgewiesen() {
        long id = abwesenheitAnlegen(TRAINER, "2029-01-08", "2029-01-09", null);
        assertFehler("UNGUELTIGER_ZEITRAUM", () -> einsaetze.abwesenheitAendern(
                TRAINER, id, LocalDate.parse("2029-01-10"), LocalDate.parse("2029-01-09"), null));
        assertThat(zeile(id)).isEqualTo(new AbwesenheitZeile(LocalDate.parse("2029-01-08"), LocalDate.parse("2029-01-09"), null));
    }

    // verifies: TEST_ABW_ERF_04
    @Test
    void loeschenMachtDenTrainerImEhemalsBetroffenenZeitraumWiederVerfuegbar() {
        long id = abwesenheitAnlegen(TRAINER, "2029-01-08", "2029-01-09", "Konferenz");
        var termin = termine.anlegen(ADMIN, eingabe("SCH-001", "2029-01-08", "2029-01-09"));
        assertThatThrownBy(() -> termine.trainerZuweisen(ADMIN, termin.terminId(), TRAINER, false))
                .isInstanceOfSatisfying(KontoFehler.class,
                        fehler -> assertThat(fehler.code()).isEqualTo("TRAINER_NICHT_VERFUEGBAR"));

        einsaetze.abwesenheitLoeschen(TRAINER, id);

        termine.trainerZuweisen(ADMIN, termin.terminId(), TRAINER, false);
        assertThat(termine.details(ADMIN, termin.terminId()).trainerId()).isEqualTo(TRAINER);
    }

    // verifies: TEST_ABW_ERF_04
    @Test
    void versuchEineFremdeAbwesenheitZuLoeschenWirdAbgewiesenUndLaesstSieBestehen() {
        long id = abwesenheitAnlegen(ANDERER_TRAINER, "2029-01-08", "2029-01-09", "Konferenz");

        assertFehler("ABWESENHEIT_NICHT_GEFUNDEN", () -> einsaetze.abwesenheitLoeschen(TRAINER, id));

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM abwesenheit WHERE id=?", Integer.class, id)).isOne();
    }

    @Test
    void eineNichtVorhandeneAbwesenheitWirdBeimLoeschenAbgewiesen() {
        assertFehler("ABWESENHEIT_NICHT_GEFUNDEN", () -> einsaetze.abwesenheitLoeschen(TRAINER, 999_999L));
    }

    private long abwesenheitAnlegen(String kontoId, String von, String bis, String grund) {
        einsaetze.abwesenheitEintragen(kontoId, LocalDate.parse(von), LocalDate.parse(bis), grund);
        return jdbc.queryForObject(
                "SELECT id FROM abwesenheit WHERE benutzerkonto_id=? AND von=? AND bis=?",
                Long.class, kontoId, LocalDate.parse(von), LocalDate.parse(bis));
    }

    private AbwesenheitZeile zeile(long id) {
        return jdbc.queryForObject("SELECT von, bis, grund FROM abwesenheit WHERE id=?",
                (rs, row) -> new AbwesenheitZeile(rs.getObject("von", LocalDate.class),
                        rs.getObject("bis", LocalDate.class), rs.getString("grund")),
                id);
    }

    private record AbwesenheitZeile(LocalDate von, LocalDate bis, String grund) {}

    private TerminService.TerminEingabe eingabe(String schulung, String von, String bis) {
        return new TerminService.TerminEingabe(schulung, LocalDate.parse(von), LocalDate.parse(bis),
                null, null, null, null, null, null, false);
    }

    private static void assertFehler(String code, Runnable aktion) {
        assertThatThrownBy(aktion::run).isInstanceOfSatisfying(KontoFehler.class,
                fehler -> assertThat(fehler.code()).isEqualTo(code));
    }
}
