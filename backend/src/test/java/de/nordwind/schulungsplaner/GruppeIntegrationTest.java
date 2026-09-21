package de.nordwind.schulungsplaner;

import de.nordwind.schulungsplaner.service.GruppeService;
import de.nordwind.schulungsplaner.service.KontoFehler;
import de.nordwind.schulungsplaner.service.TerminService;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(GruppeIntegrationTest.FesteZeit.class)
@Transactional
class GruppeIntegrationTest {
    static final String ADMIN = "TRN-001";
    static final String TRAINER = "TRN-005";
    static final LocalDate TAG = LocalDate.of(2028, 4, 3);

    @Autowired GruppeService gruppen;
    @Autowired TerminService termine;
    @Autowired JdbcTemplate jdbc;

    static class FesteZeit {
        @Bean @Primary Clock testClock() {
            return Clock.fixed(Instant.parse("2026-09-17T10:00:00Z"), ZoneId.of("Europe/Berlin"));
        }
    }

    // verifies: TEST_GRP_ANL_01
    @Test
    void administratorLegtGruppeMitMitgliedernUndGruppentrainerAn() {
        var gruppe = gruppen.anlegen(ADMIN, new GruppeService.GruppenEingabe(" Mallorca ", TRAINER,
                List.of("TRN-002", "TRN-003")));
        assertThat(gruppe.name()).isEqualTo("Mallorca");
        assertThat(gruppe.trainer().id()).isEqualTo(TRAINER);
        assertThat(gruppe.mitglieder()).extracting(GruppeService.Person::id).containsExactlyInAnyOrder("TRN-002", "TRN-003");
        assertThat(gruppen.alle("TRN-002")).extracting(GruppeService.GruppenAnsicht::name).contains("Mallorca");
    }

    // verifies: TEST_GRP_BER_01
    @Test
    void nurAdministratorenPflegenGruppen() {
        assertFehler("ADMINISTRATOR_ERFORDERLICH",
                () -> gruppen.anlegen("TRN-002", new GruppeService.GruppenEingabe("Nope", null, List.of())));
    }

    // verifies: TEST_GRP_ANL_02
    @Test
    void gruppennameIstEindeutigOhneBeachtungVonGrossUndKleinschreibung() {
        gruppen.anlegen(ADMIN, new GruppeService.GruppenEingabe("Mallorca", null, List.of()));
        assertFehler("GRUPPENNAME_VERGEBEN",
                () -> gruppen.anlegen(ADMIN, new GruppeService.GruppenEingabe("MALLORCA", null, List.of())));
        assertFehler("GRUPPENNAME_FEHLT",
                () -> gruppen.anlegen(ADMIN, new GruppeService.GruppenEingabe("  ", null, List.of())));
    }

    // verifies: TEST_GRP_TRA_01
    @Test
    void gruppentrainerIstKeinMitglied() {
        assertFehler("TRAINER_KEIN_MITGLIED", () -> gruppen.anlegen(ADMIN,
                new GruppeService.GruppenEingabe("Mallorca", TRAINER, List.of(TRAINER))));
        var gruppe = gruppen.anlegen(ADMIN, new GruppeService.GruppenEingabe("Mallorca", TRAINER, List.of()));
        assertFehler("TRAINER_KEIN_MITGLIED", () -> gruppen.mitgliedHinzufuegen(ADMIN, gruppe.id(), TRAINER));
    }

    // verifies: TEST_GRP_MIT_01
    @Test
    void mitgliederKoennenHinzugefuegtUndEntferntWerden() {
        var gruppe = gruppen.anlegen(ADMIN, new GruppeService.GruppenEingabe("Mallorca", null, List.of()));
        assertThat(gruppen.mitgliedHinzufuegen(ADMIN, gruppe.id(), "TRN-002").mitglieder()).hasSize(1);
        assertThat(gruppen.mitgliedHinzufuegen(ADMIN, gruppe.id(), "TRN-002").mitglieder()).hasSize(1);
        assertThat(gruppen.mitgliedEntfernen(ADMIN, gruppe.id(), "TRN-002").mitglieder()).isEmpty();
    }

    // verifies: TEST_GRP_TER_01
    @Test
    void terminTraegtDieGruppeUndZeigtSieAn() {
        var gruppe = gruppen.anlegen(ADMIN, new GruppeService.GruppenEingabe("Mallorca", TRAINER, List.of("TRN-002")));
        var termin = termine.anlegen(ADMIN, eingabe(gruppe.id(), "09:00", "10:30"));
        assertThat(termin.gruppeId()).isEqualTo(gruppe.id());
        assertThat(termin.gruppeName()).isEqualTo("Mallorca");
        assertThat(gruppen.details(ADMIN, gruppe.id()).anzahlTermine()).isEqualTo(1);
    }

    // verifies: TEST_GRP_TER_02
    @Test
    void gruppeKannNichtZweiTermineZurGleichenZeitOhnePauseHaben() {
        var gruppe = gruppen.anlegen(ADMIN, new GruppeService.GruppenEingabe("Mallorca", null, List.of()));
        termine.anlegen(ADMIN, eingabe(gruppe.id(), "09:00", "10:45"));
        assertFehler("GRUPPE_NICHT_VERFUEGBAR", () -> termine.anlegen(ADMIN, eingabe(gruppe.id(), "10:00", "11:00")));
        assertFehler("GRUPPE_NICHT_VERFUEGBAR", () -> termine.anlegen(ADMIN, eingabe(gruppe.id(), "10:45", "12:00")));
        assertThat(termine.anlegen(ADMIN, eingabe(gruppe.id(), "11:00", "12:30")).gruppeId()).isEqualTo(gruppe.id());
    }

    // verifies: TEST_GRP_TER_03
    @Test
    void unbekannteGruppeWirdBeimTerminAbgewiesen() {
        assertFehler("GRUPPE_NICHT_GEFUNDEN", () -> termine.anlegen(ADMIN, eingabe("gibt-es-nicht", "09:00", "10:00")));
    }

    // verifies: TEST_GRP_LOE_01
    @Test
    void gruppeMitGeplantenTerminenLaesstSichNichtLoeschen() {
        var gruppe = gruppen.anlegen(ADMIN, new GruppeService.GruppenEingabe("Mallorca", null, List.of()));
        var termin = termine.anlegen(ADMIN, eingabe(gruppe.id(), "09:00", "10:00"));
        assertFehler("GRUPPE_HAT_TERMINE", () -> gruppen.loeschen(ADMIN, gruppe.id()));
        termine.absagen(ADMIN, termin.terminId(), null);
        gruppen.loeschen(ADMIN, gruppe.id());
        assertThat(gruppen.alle(ADMIN)).extracting(GruppeService.GruppenAnsicht::id).doesNotContain(gruppe.id());
        assertThat(termine.details(ADMIN, termin.terminId()).gruppeId()).isNull();
    }

    private TerminService.TerminEingabe eingabe(String gruppeId, String von, String bis) {
        return new TerminService.TerminEingabe("SCH-001", TAG, TAG, LocalTime.parse(von), LocalTime.parse(bis),
                null, null, null, null, null, null, false, gruppeId);
    }

    private static void assertFehler(String code, Runnable aktion) {
        assertThatThrownBy(aktion::run).isInstanceOfSatisfying(KontoFehler.class,
                fehler -> assertThat(fehler.code()).isEqualTo(code));
    }
}
