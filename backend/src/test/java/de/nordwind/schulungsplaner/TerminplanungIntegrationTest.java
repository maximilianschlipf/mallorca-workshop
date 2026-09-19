package de.nordwind.schulungsplaner;

import de.nordwind.schulungsplaner.service.KontoFehler;
import de.nordwind.schulungsplaner.service.DashboardService;
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
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TerminplanungIntegrationTest.FesteZeit.class)
@Transactional
class TerminplanungIntegrationTest {
    static final LocalDate HEUTE = LocalDate.of(2026, 9, 17);
    static final String ADMIN = "TRN-001";
    @Autowired TerminService termine;
    @Autowired DashboardService dashboard;
    @Autowired TrainereinsatzService trainereinsaetze;
    @Autowired JdbcTemplate jdbc;

    static class FesteZeit {
        @Bean @Primary Clock testClock() {
            return Clock.fixed(Instant.parse("2026-09-17T10:00:00Z"), ZoneId.of("Europe/Berlin"));
        }
    }

    @BeforeEach
    void keineTesttermine() {
        jdbc.update("DELETE FROM termin WHERE termin_id LIKE '%-TEST-%'");
    }

    // verifies: TEST_TER_ANL_01, TEST_TER_ANL_07
    @Test
    void pflichtangabenErzeugenGeplantenNichtZugewiesenenTermin() {
        var termin = termine.anlegen(ADMIN, minimal("SCH-001", "2028-01-03", "2028-01-04"));
        assertThat(termin.status()).isEqualTo("geplant");
        assertThat(termin.trainerId()).isNull();
        assertThat(termin.zugangsart()).isNull();
        assertThat(termin.durchfuehrungsart()).isNull();
        assertThat(termin.ort()).isNull();
        assertThat(termine.dashboard(ADMIN)).anyMatch(e -> e.terminId().equals(termin.terminId()) && e.ohneTrainer());
    }

    // verifies: TEST_TER_ANL_02
    @Test
    void jedeFehlendePflichtangabeWirdAbgewiesen() {
        assertFehler(() -> termine.anlegen(ADMIN, minimal(null, "2028-01-03", "2028-01-04")));
        assertFehler(() -> termine.anlegen(ADMIN, minimal("SCH-001", null, "2028-01-04")));
        assertFehler(() -> termine.anlegen(ADMIN, minimal("SCH-001", "2028-01-03", null)));
    }

    // verifies: TEST_TER_ANL_03
    @Test
    void enddatumUeberspringtDasWochenendeUndBleibtNurEinVorschlag() {
        assertThat(termine.enddatumVorschlagen("SCH-002", LocalDate.of(2026, 9, 18)))
                .isEqualTo(LocalDate.of(2026, 9, 22));
        assertThat(termine.anlegen(ADMIN, minimal("SCH-002", "2028-01-03", "2028-01-03")).enddatum())
                .isEqualTo(LocalDate.of(2028, 1, 3));
        assertThat(termine.anlegen(ADMIN, minimal("SCH-002", "2028-01-03", "2028-01-10")).enddatum())
                .isEqualTo(LocalDate.of(2028, 1, 10));
    }

    // verifies: TEST_TER_ANL_04, TEST_TER_ANL_05, TEST_TER_ANL_08
    @Test
    void zeitraumregelnGeltenBeimAnlegen() {
        assertFehler(() -> termine.anlegen(ADMIN, minimal("SCH-001", "2028-01-04", "2028-01-03")));
        assertThat(termine.anlegen(ADMIN, minimal("SCH-001", "2028-01-03", "2028-01-03"))).isNotNull();
        assertFehler(() -> termine.anlegen(ADMIN, minimal("SCH-001", "2026-09-16", "2026-09-16")));
        assertThat(termine.anlegen(ADMIN, minimal("SCH-001", "2026-09-17", "2026-09-17"))).isNotNull();
        assertFehler(() -> termine.anlegen(ADMIN, minimal("SCH-001", "2026-09-19", "2026-09-21")));
        assertFehler(() -> termine.anlegen(ADMIN, minimal("SCH-001", "2026-09-18", "2026-09-20")));
        assertThat(termine.anlegen(ADMIN, minimal("SCH-001", "2026-09-18", "2026-09-21")).schulungsTage()).isEqualTo(2);
    }

    // verifies: TEST_TER_ANL_06, TEST_TER_AEND_12
    @Test
    void archivierungVerhindertNurDieNeuanlage() {
        var termin = termine.anlegen(ADMIN, minimal("SCH-001", "2028-02-01", "2028-02-02"));
        var absage = termine.anlegen(ADMIN, minimal("SCH-001", "2028-02-07", "2028-02-08"));
        jdbc.update("UPDATE schulung_zustand SET zustand='ARCHIVIERT', archiviert_am=? WHERE schulung_id='SCH-001'", HEUTE);
        assertFehler(() -> termine.anlegen(ADMIN, minimal("SCH-001", "2028-03-01", "2028-03-02")));
        var geaendert = termine.aendern(ADMIN, termin.terminId(), minimal("SCH-001", "2028-02-03", "2028-02-04"));
        assertThat(geaendert.startdatum()).isEqualTo(LocalDate.of(2028, 2, 3));
        termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-005", false);
        jdbc.update("UPDATE termin SET startdatum=?, enddatum=? WHERE termin_id=?", HEUTE.minusDays(1), HEUTE, termin.terminId());
        assertThat(termine.bestaetigen(ADMIN, termin.terminId()).status()).isEqualTo("abgeschlossen");
        assertThat(termine.absagen(ADMIN, absage.terminId(), null).status()).isEqualTo("abgesagt");
    }

    // verifies: TEST_TER_ID_01, TEST_TER_ID_02
    @Test
    void idIstJeSchulungFortlaufendUndUnveraenderlich() {
        jdbc.update("DELETE FROM teilnehmerbuchung WHERE termin_id IN (SELECT termin_id FROM termin WHERE schulung_id='SCH-008')");
        jdbc.update("DELETE FROM termin WHERE schulung_id='SCH-008'");
        jdbc.update("DELETE FROM termin_nummer WHERE schulung_id='SCH-008'");
        var eins = termine.anlegen(ADMIN, minimal("SCH-008", "2028-01-03", "2028-01-04"));
        var zwei = termine.anlegen(ADMIN, minimal("SCH-008", "2028-02-01", "2028-02-02"));
        termine.loeschen(ADMIN, zwei.terminId());
        var drei = termine.anlegen(ADMIN, minimal("SCH-008", "2028-03-01", "2028-03-02"));
        assertThat(List.of(eins.terminId(), zwei.terminId(), drei.terminId()))
                .containsExactly("SCH-008-T0001", "SCH-008-T0002", "SCH-008-T0003");
        jdbc.update("DELETE FROM teilnehmerbuchung WHERE termin_id IN (SELECT termin_id FROM termin WHERE schulung_id='SCH-007')");
        jdbc.update("DELETE FROM termin_assistent WHERE termin_id IN (SELECT termin_id FROM termin WHERE schulung_id='SCH-007')");
        jdbc.update("DELETE FROM termin WHERE schulung_id='SCH-007'");
        jdbc.update("DELETE FROM termin_nummer WHERE schulung_id='SCH-007'");
        assertThat(termine.anlegen(ADMIN, minimal("SCH-007", "2028-04-03", "2028-04-04")).terminId())
                .isEqualTo("SCH-007-T0001");
        var verschoben = termine.aendern(ADMIN, eins.terminId(), minimal("SCH-008", "2028-01-05", "2028-01-06"));
        assertThat(verschoben.terminId()).isEqualTo(eins.terminId());
        assertThat(termine.absagen(ADMIN, eins.terminId(), null).terminId()).isEqualTo(eins.terminId());
    }

    // verifies: TEST_TER_ID_03
    @Test
    void idLaeuftNachT9999NichtUeber() {
        jdbc.update("DELETE FROM termin_nummer WHERE schulung_id='SCH-008'");
        jdbc.update("INSERT INTO termin_nummer VALUES ('SCH-008', 9999)");
        assertThat(termine.anlegen(ADMIN, minimal("SCH-008", "2028-01-03", "2028-01-04")).terminId())
                .isEqualTo("SCH-008-T9999");
        assertThatThrownBy(() -> termine.anlegen(ADMIN, minimal("SCH-008", "2028-02-01", "2028-02-02")))
                .isInstanceOf(KontoFehler.class).extracting("code").isEqualTo("TERMIN_ID_ERSCHOEPFT");
    }

    // verifies: TEST_TER_FORM_01, TEST_TER_FORM_02, TEST_TER_FORM_03
    @Test
    void zugangsUndDurchfuehrungsartenSindGeschlosseneFreiKombinierbareMengen() {
        int tag = 3;
        for (String zugang : List.of("oeffentlich", "exklusiv")) {
            for (String art : List.of("remote", "vor_ort", "beim_kunden", "hybrid")) {
                var eingabe = form("SCH-001", LocalDate.of(2028, 4, tag), LocalDate.of(2028, 4, tag),
                        zugang, art, Setzt.ort(art), "exklusiv".equals(zugang) ? "Acme GmbH" : null,
                        Setzt.online(art), false);
                assertThat(termine.anlegen(ADMIN, eingabe)).isNotNull();
                tag++;
                while (LocalDate.of(2028, 4, tag).getDayOfWeek().getValue() > 5) tag++;
            }
        }
        assertFehler(() -> termine.anlegen(ADMIN, form("SCH-001", d("2028-05-01"), d("2028-05-01"), "intern", null, null, null, null, false)));
        assertFehler(() -> termine.anlegen(ADMIN, form("SCH-001", d("2028-05-02"), d("2028-05-02"), null, "video", null, null, null, false)));
    }

    // verifies: TEST_TER_FORM_04, TEST_TER_FORM_05
    @Test
    void ortFolgtDerDurchfuehrungsartUndAngabenSindNachtragbar() {
        for (String art : List.of("vor_ort", "beim_kunden", "hybrid"))
            assertFehler(() -> termine.anlegen(ADMIN, form("SCH-001", d("2028-06-01"), d("2028-06-01"), null, art, null, null, null, false)));
        assertFehler(() -> termine.anlegen(ADMIN, form("SCH-001", d("2028-06-02"), d("2028-06-02"), null, "remote", "Köln", null, null, false)));
        var offen = termine.anlegen(ADMIN, minimal("SCH-001", "2028-06-05", "2028-06-05"));
        var vorOrt = termine.aendern(ADMIN, offen.terminId(), form("SCH-001", offen.startdatum(), offen.enddatum(), "oeffentlich", "vor_ort", "Köln", null, null, false));
        assertThat(vorOrt.zugangsart()).isEqualTo("oeffentlich");
        assertFehler(() -> termine.aendern(ADMIN, offen.terminId(), form("SCH-001", offen.startdatum(), offen.enddatum(), "oeffentlich", "remote", null, null, null, false)));
        assertThat(termine.details(ADMIN, offen.terminId()).ort()).isEqualTo("Köln");
        assertThat(termine.aendern(ADMIN, offen.terminId(), form("SCH-001", offen.startdatum(), offen.enddatum(), "oeffentlich", "remote", null, null, null, true)).ort()).isNull();
    }

    // verifies: TEST_TER_FORM_06, TEST_TER_FORM_07
    @Test
    void teilnehmergrenzenRichtenSichNurNachDerZugangsart() {
        var ohne = termine.anlegen(ADMIN, minimal("SCH-001", "2028-07-03", "2028-07-03"));
        for (int i = 0; i < 13; i++) termine.buchungAnlegen(ADMIN, ohne.terminId(), buchung("P" + i, "A GmbH"));
        assertThat(termine.details(ADMIN, ohne.terminId()).warnungen()).isEmpty();
        var oeffentlich = termine.aendern(ADMIN, ohne.terminId(), form("SCH-001", ohne.startdatum(), ohne.enddatum(), "oeffentlich", null, null, null, null, false));
        assertThat(oeffentlich.warnungen()).contains("Höchstteilnehmerzahl überschritten");
        var exklusiv = termine.anlegen(ADMIN, form("SCH-001", d("2028-07-04"), d("2028-07-04"), "exklusiv", "remote", null, "Acme", null, false));
        assertThat(exklusiv.warnungen()).contains("Mindestteilnehmerzahl nicht erreicht");
    }

    // verifies: TEST_TER_FORM_08, TEST_TER_FORM_09
    @Test
    void exklusivErzwingtEineGetrimmteKundenfirmaUndEinheitlicheBuchungen() {
        assertFehler(() -> termine.anlegen(ADMIN, form("SCH-001", d("2028-08-01"), d("2028-08-01"), "exklusiv", "remote", null, null, null, false)));
        assertFehler(() -> termine.anlegen(ADMIN, form("SCH-001", d("2028-08-02"), d("2028-08-02"), "oeffentlich", "remote", null, "Acme", null, false)));
        assertFehler(() -> termine.anlegen(ADMIN, form("SCH-001", d("2028-08-02"), d("2028-08-02"), null, "remote", null, "Acme", null, false)));
        var termin = termine.anlegen(ADMIN, form("SCH-001", d("2028-08-03"), d("2028-08-03"), "oeffentlich", "remote", null, null, null, false));
        var abweichend = termine.buchungAnlegen(ADMIN, termin.terminId(), buchung("A", "Andere AG"));
        assertFehler(() -> termine.aendern(ADMIN, termin.terminId(), form("SCH-001", termin.startdatum(), termin.enddatum(), "exklusiv", "remote", null, "Acme GmbH", null, false)));
        termine.buchungLoeschen(ADMIN, termin.terminId(), abweichend.id());
        termine.buchungAnlegen(ADMIN, termin.terminId(), buchung("A", " acme gmbh "));
        var exklusiv = termine.aendern(ADMIN, termin.terminId(), form("SCH-001", termin.startdatum(), termin.enddatum(), "exklusiv", "remote", null, " Acme GmbH ", null, false));
        assertThat(exklusiv.kundenfirma()).isEqualTo("Acme GmbH");
        assertThat(termine.details("TRN-002", termin.terminId()).kundenfirma()).isEqualTo("Acme GmbH");
        assertFehler(() -> termine.aendern(ADMIN, termin.terminId(), form("SCH-001", termin.startdatum(), termin.enddatum(), "exklusiv", "remote", null, "Neue GmbH", null, false)));
        assertThat(termine.details(ADMIN, termin.terminId()).kundenfirma()).isEqualTo("Acme GmbH");
        assertThat(termine.details(ADMIN, termin.terminId()).teilnehmer()).allMatch(b -> b.firma().equals("Acme GmbH"));
        var umbenannt = termine.aendern(ADMIN, termin.terminId(), form("SCH-001", termin.startdatum(), termin.enddatum(), "exklusiv", "remote", null, "Neue GmbH", null, true));
        assertThat(umbenannt.teilnehmer()).allMatch(b -> b.firma().equals("Neue GmbH"));
        assertFehler(() -> termine.aendern(ADMIN, termin.terminId(), form("SCH-001", termin.startdatum(), termin.enddatum(), "oeffentlich", "remote", null, null, null, false)));
        assertThat(termine.aendern(ADMIN, termin.terminId(), form("SCH-001", termin.startdatum(), termin.enddatum(), "oeffentlich", "remote", null, null, null, true)).kundenfirma()).isNull();
    }

    // verifies: TEST_TER_FORM_10, TEST_TER_FORM_11
    @Test
    void ortKonfligiertNichtUndOnlineZugangWirdStrengValidiert() {
        var ortEins = termine.anlegen(ADMIN, form("SCH-001", d("2028-09-01"), d("2028-09-01"), "oeffentlich", "vor_ort", "Raum 1", null, null, false));
        var ortZwei = termine.anlegen(ADMIN, form("SCH-002", d("2028-09-01"), d("2028-09-01"), "oeffentlich", "vor_ort", "Raum 1", null, null, false));
        termine.trainerZuweisen(ADMIN, ortEins.terminId(), "TRN-005", false);
        termine.trainerZuweisen(ADMIN, ortZwei.terminId(), "TRN-002", false);
        assertThat(ortEins.warnungen()).noneMatch(w -> w.toLowerCase().contains("raum"));
        for (String art : List.of("remote", "hybrid")) {
            String ort = "hybrid".equals(art) ? "Raum 2" : null;
            assertThat(termine.anlegen(ADMIN, form("SCH-001", d("2028-09-04"), d("2028-09-04"), null, art, ort, null, " https://example.org/raum ", false)).onlineZugang()).isEqualTo("https://example.org/raum");
        }
        assertThat(termine.anlegen(ADMIN, form("SCH-001", d("2028-09-07"), d("2028-09-07"), null, "remote", null, null, null, false))).isNotNull();
        assertThat(termine.anlegen(ADMIN, form("SCH-001", d("2028-09-08"), d("2028-09-08"), null, "hybrid", "Raum 3", null, null, false))).isNotNull();
        for (String url : List.of("raum", "/raum", "javascript:alert(1)", "file:///tmp/x"))
            assertFehler(() -> termine.anlegen(ADMIN, form("SCH-001", d("2028-09-05"), d("2028-09-05"), null, "remote", null, null, url, false)));
        assertFehler(() -> termine.anlegen(ADMIN, form("SCH-001", d("2028-09-06"), d("2028-09-06"), null, "vor_ort", "Köln", null, "https://example.org", false)));
        assertFehler(() -> termine.anlegen(ADMIN, form("SCH-001", d("2028-09-06"), d("2028-09-06"), null, "beim_kunden", "Köln", null, "https://example.org", false)));
        assertFehler(() -> termine.anlegen(ADMIN, form("SCH-001", d("2028-09-06"), d("2028-09-06"), null, null, null, null, "https://example.org", false)));
        assertThat(termine.anlegen(ADMIN, form("SCH-001", d("2028-09-12"), d("2028-09-12"), null, "remote", null, null, "http://example.org", false)).onlineZugang())
                .isEqualTo("http://example.org");
        var mitUrl = termine.anlegen(ADMIN, form("SCH-001", d("2028-09-11"), d("2028-09-11"), null, "remote", null, null, "https://example.org", false));
        assertFehler(() -> termine.aendern(ADMIN, mitUrl.terminId(), form("SCH-001", mitUrl.startdatum(), mitUrl.enddatum(), null, "vor_ort", "Köln", null, null, false)));
        assertThat(termine.details(ADMIN, mitUrl.terminId()).onlineZugang()).isNotNull();
        assertThat(termine.aendern(ADMIN, mitUrl.terminId(), form("SCH-001", mitUrl.startdatum(), mitUrl.enddatum(), null, "vor_ort", "Köln", null, null, true)).onlineZugang()).isNull();
    }

    @Test
    void sensibleDetailsSindNurFuerDieBeteiligtenSichtbar() {
        var termin = termine.anlegen(ADMIN, form("SCH-001", d("2028-10-02"), d("2028-10-02"), "exklusiv", "remote", null, "Acme", "https://example.org", false));
        termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-005", false);
        termine.assistentZuweisen(ADMIN, termin.terminId(), "TRN-003");
        termine.buchungAnlegen(ADMIN, termin.terminId(), new TerminService.BuchungEingabe("Person", "Acme", "Hinweis", "offen"));
        assertThat(termine.details(ADMIN, termin.terminId()).teilnehmer()).hasSize(1);
        assertThat(termine.details("TRN-005", termin.terminId()).onlineZugang()).isNotNull();
        assertThat(termine.details("TRN-003", termin.terminId()).onlineZugang()).isNotNull();
        assertThat(termine.details("TRN-003", termin.terminId()).teilnehmer()).isEmpty();
        assertThat(termine.details("TRN-002", termin.terminId()).onlineZugang()).isNull();
        assertThat(termine.details("TRN-002", termin.terminId()).kundenfirma()).isEqualTo("Acme");
        assertThat(termine.details("TRN-002", termin.terminId()).teilnehmer()).isEmpty();
    }

    // verifies: TEST_TER_STAT_01
    @Test
    void esGibtGenauDreiTerminzustaendeUndAusgebuchtIstKeinerDavon() {
        assertThat(jdbc.queryForList("SELECT DISTINCT status FROM termin ORDER BY status", String.class))
                .containsExactlyInAnyOrder("geplant", "abgeschlossen", "abgesagt")
                .doesNotContain("ausgebucht");
        var termin = termine.anlegen(ADMIN, form("SCH-001", d("2028-10-09"), d("2028-10-09"), "oeffentlich", "remote", null, null, null, false));
        for (int i = 0; i < 13; i++) termine.buchungAnlegen(ADMIN, termin.terminId(), buchung("P" + i, "Firma"));
        assertThat(termine.details(ADMIN, termin.terminId()).status()).isEqualTo("geplant");
        assertThatThrownBy(() -> jdbc.update("UPDATE termin SET status='ausgebucht' WHERE termin_id=?", termin.terminId()))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    // verifies: TEST_TER_STAT_02, TEST_TER_STAT_04, TEST_TER_STAT_05
    @Test
    void manuelleBestaetigungPrueftPersonDatumUndTeilnahmestatusUndIstEndgueltig() {
        var termin = termine.anlegen(ADMIN, form("SCH-001", HEUTE.plusDays(1), HEUTE.plusDays(1), "oeffentlich", "remote", null, null, "https://example.org", false));
        termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-005", false);
        termine.assistentZuweisen(ADMIN, termin.terminId(), "TRN-003");
        assertFehler(() -> termine.bestaetigen("TRN-005", termin.terminId()));
        jdbc.update("UPDATE termin SET startdatum=?, enddatum=? WHERE termin_id=?", HEUTE.minusDays(1), HEUTE, termin.terminId());
        var buchung = termine.buchungAnlegen(ADMIN, termin.terminId(), buchung("Person", "Acme"));
        assertFehler(() -> termine.bestaetigen("TRN-002", termin.terminId()));
        assertFehler(() -> termine.bestaetigen("TRN-003", termin.terminId()));
        assertFehler(() -> termine.bestaetigen("TRN-005", termin.terminId()));
        termine.buchungStatus("TRN-005", termin.terminId(), buchung.id(), "teilgenommen");
        var fertig = termine.bestaetigen("TRN-005", termin.terminId());
        assertThat(fertig.status()).isEqualTo("abgeschlossen");
        assertThat(fertig.abschlussart()).isEqualTo("manuell");
        assertThat(fertig.abgeschlossenAm()).isEqualTo(HEUTE);
        assertThat(fertig.bestaetigtVon()).isEqualTo("TRN-005");
        assertThat(fertig.onlineZugang()).isNull();
        assertThat(fertig.durchfuehrungsart()).isEqualTo("remote");
        assertFehler(() -> termine.bestaetigen(ADMIN, termin.terminId()));
        assertFehler(() -> termine.aendern(ADMIN, termin.terminId(), minimal("SCH-001", HEUTE.toString(), HEUTE.toString())));

        var ohneBuchung = termine.anlegen(ADMIN, minimal("SCH-001", "2028-10-10", "2028-10-10"));
        termine.trainerZuweisen(ADMIN, ohneBuchung.terminId(), "TRN-005", false);
        jdbc.update("UPDATE termin SET startdatum=?, enddatum=? WHERE termin_id=?", HEUTE, HEUTE, ohneBuchung.terminId());
        assertThat(termine.bestaetigen("TRN-005", ohneBuchung.terminId()).status()).isEqualTo("abgeschlossen");
    }

    // verifies: TEST_TER_STAT_03
    @Test
    void administratorBestaetigtErsatzweiseAberNieOhneTrainer() {
        var ohne = termine.anlegen(ADMIN, minimal("SCH-001", "2028-10-03", "2028-10-03"));
        jdbc.update("UPDATE termin SET startdatum=?, enddatum=? WHERE termin_id=?", HEUTE, HEUTE, ohne.terminId());
        assertFehler(() -> termine.bestaetigen(ADMIN, ohne.terminId()));
        var mit = termine.anlegen(ADMIN, minimal("SCH-001", "2028-10-04", "2028-10-04"));
        termine.trainerZuweisen(ADMIN, mit.terminId(), "TRN-005", false);
        jdbc.update("UPDATE termin SET startdatum=?, enddatum=? WHERE termin_id=?", HEUTE, HEUTE, mit.terminId());
        assertThat(termine.bestaetigen(ADMIN, mit.terminId()).bestaetigtVon()).isEqualTo(ADMIN);
        assertFehler(() -> termine.bestaetigen(ADMIN, mit.terminId()));
    }

    // verifies: TEST_TER_STAT_06, TEST_TER_STAT_07, TEST_TER_STAT_08
    @Test
    void automatischeAbschluesseUndAufbewahrungWerdenInReihenfolgeNachgezogen() {
        var termin = termine.anlegen(ADMIN, form("SCH-001", d("2028-01-03"), d("2028-01-03"), "oeffentlich", "remote", null, null, "https://example.org", false));
        var nochNicht = termine.anlegen(ADMIN, minimal("SCH-001", "2028-01-04", "2028-01-04"));
        termine.buchungAnlegen(ADMIN, termin.terminId(), buchung("Person", "Acme"));
        jdbc.update("UPDATE termin SET startdatum='2026-07-17', enddatum='2026-07-18' WHERE termin_id=?", nochNicht.terminId());
        jdbc.update("UPDATE termin SET startdatum='2026-07-16', enddatum='2026-07-17', trainer_id='TRN-005' WHERE termin_id=?", termin.terminId());
        termine.details(ADMIN, nochNicht.terminId());
        assertThat(termine.details(ADMIN, nochNicht.terminId()).status()).isEqualTo("geplant");
        var fertig = termine.details(ADMIN, termin.terminId());
        assertThat(fertig.status()).isEqualTo("abgeschlossen");
        assertThat(fertig.abschlussart()).isEqualTo("automatisch");
        assertThat(fertig.abgeschlossenAm()).isEqualTo(LocalDate.of(2026, 9, 17));
        assertThat(fertig.bestaetigtVon()).isNull();
        assertThat(fertig.onlineZugang()).isNull();
        assertThat(fertig.teilnehmer()).singleElement()
                .extracting(TerminService.Teilnehmerbuchung::teilnahmestatus).isEqualTo("offen");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM benachrichtigung WHERE empfaenger_id='TRN-005' AND anlass LIKE '%nicht in Teilnehmerauswertungen%'", Integer.class)).isEqualTo(1);
        assertThat(termine.auswertung(ADMIN).bestaetigteTermine()).isLessThan(jdbc.queryForObject("SELECT COUNT(*) FROM termin WHERE status='abgeschlossen'", Integer.class));
        assertFehler(() -> termine.bestaetigen(ADMIN, termin.terminId()));
        assertFehler(() -> termine.buchungStatus(ADMIN, termin.terminId(),
                fertig.teilnehmer().getFirst().id(), "teilgenommen"));
        assertFehler(() -> termine.buchungLoeschen(ADMIN, termin.terminId(), fertig.teilnehmer().getFirst().id()));
        assertFehler(() -> termine.buchungAnlegen(ADMIN, termin.terminId(), buchung("Neu", "Acme")));

        var adminTermin = termine.anlegen(ADMIN, minimal("SCH-001", "2028-01-10", "2028-01-10"));
        termine.trainerZuweisen(ADMIN, adminTermin.terminId(), ADMIN, false);
        jdbc.update("UPDATE termin SET startdatum='2026-07-16', enddatum='2026-07-17' WHERE termin_id=?", adminTermin.terminId());
        termine.nachziehen();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM benachrichtigung WHERE empfaenger_id=? AND anlass LIKE '%automatisch abgeschlossen%'", Integer.class, ADMIN)).isZero();

        var monatsende = termine.anlegen(ADMIN, minimal("SCH-001", "2028-01-05", "2028-01-05"));
        jdbc.update("UPDATE termin SET startdatum='2025-12-31', enddatum='2025-12-31' WHERE termin_id=?", monatsende.terminId());
        termine.nachziehen();
        assertThat(termine.details(ADMIN, monatsende.terminId()).abgeschlossenAm()).isEqualTo(LocalDate.of(2026, 2, 28));

        jdbc.update("UPDATE termin SET abgeschlossen_am='2026-06-17' WHERE termin_id=?", termin.terminId());
        var einenTagZuFrueh = termine.anlegen(ADMIN, minimal("SCH-001", "2028-01-11", "2028-01-11"));
        var bleibt = termine.buchungAnlegen(ADMIN, einenTagZuFrueh.terminId(), buchung("Bleibt", "Firma"));
        jdbc.update("UPDATE termin SET status='abgeschlossen', abschlussart='manuell', abgeschlossen_am='2026-06-18' WHERE termin_id=?", einenTagZuFrueh.terminId());
        termine.nachziehen();
        assertThat(jdbc.queryForMap("SELECT name, firma, bemerkung FROM teilnehmerbuchung WHERE termin_id=?", termin.terminId()))
                .containsEntry("FIRMA", "Acme").containsEntry("NAME", null).containsEntry("BEMERKUNG", null);
        assertThat(jdbc.queryForObject("SELECT name FROM teilnehmerbuchung WHERE id=?", String.class, bleibt.id()))
                .isEqualTo("Bleibt");

        var beides = termine.anlegen(ADMIN, minimal("SCH-001", "2028-01-06", "2028-01-06"));
        termine.buchungAnlegen(ADMIN, beides.terminId(), buchung("Zu löschen", "Firma bleibt"));
        jdbc.update("UPDATE termin SET startdatum='2026-04-16', enddatum='2026-04-17' WHERE termin_id=?", beides.terminId());
        termine.nachziehen();
        assertThat(termine.details(ADMIN, beides.terminId()).status()).isEqualTo("abgeschlossen");
        assertThat(jdbc.queryForObject("SELECT name FROM teilnehmerbuchung WHERE termin_id=?", String.class, beides.terminId())).isNull();

        var abgesagt = termine.anlegen(ADMIN, minimal("SCH-001", "2028-01-07", "2028-01-07"));
        var buchung = termine.buchungAnlegen(ADMIN, abgesagt.terminId(), buchung("Alt", "Firma"));
        jdbc.update("UPDATE termin SET status='abgesagt', abgesagt_am='2026-06-17' WHERE termin_id=?", abgesagt.terminId());
        termine.nachziehen();
        assertThat(jdbc.queryForObject("SELECT name FROM teilnehmerbuchung WHERE id=?", String.class, buchung.id())).isNull();

        var monatsfrist = termine.anlegen(ADMIN, minimal("SCH-001", "2028-01-10", "2028-01-10"));
        var monatsbuchung = termine.buchungAnlegen(ADMIN, monatsfrist.terminId(), buchung("Januar", "Firma"));
        jdbc.update("UPDATE termin SET status='abgeschlossen', abschlussart='manuell', abgeschlossen_am='2026-01-31' WHERE termin_id=?", monatsfrist.terminId());
        termine.nachziehen();
        assertThat(jdbc.queryForObject("SELECT name FROM teilnehmerbuchung WHERE id=?", String.class, monatsbuchung.id())).isNull();
    }

    // verifies: TEST_TER_AEND_01, TEST_TER_AEND_02, TEST_TER_AEND_03
    @Test
    void aenderungenBeachtenLebenszyklusFeldregelnUndZuweisungen() {
        var termin = termine.anlegen(ADMIN, form("SCH-001", d("2028-11-01"), d("2028-11-02"), "oeffentlich", "remote", null, null, null, false));
        termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-005", false);
        var verschoben = termine.aendern(ADMIN, termin.terminId(), form("SCH-001", d("2028-11-06"), d("2028-11-07"), "oeffentlich", "hybrid", "Köln", null, "https://example.org", false));
        assertThat(verschoben.ort()).isEqualTo("Köln");
        jdbc.update("INSERT INTO abwesenheit (benutzerkonto_id, von, bis) VALUES ('TRN-005', '2028-11-08', '2028-11-09')");
        assertFehler(() -> termine.aendern(ADMIN, termin.terminId(), form("SCH-001", d("2028-11-08"), d("2028-11-09"), "oeffentlich", "hybrid", "Köln", null, "https://example.org", false)));
        var mitAssistenz = termine.anlegen(ADMIN, minimal("SCH-001", "2028-11-13", "2028-11-14"));
        termine.assistentZuweisen(ADMIN, mitAssistenz.terminId(), "TRN-003");
        jdbc.update("INSERT INTO abwesenheit (benutzerkonto_id, von, bis) VALUES ('TRN-003', '2028-11-15', '2028-11-16')");
        assertFehler(() -> termine.aendern(ADMIN, mitAssistenz.terminId(), minimal("SCH-001", "2028-11-15", "2028-11-16")));
        jdbc.update("UPDATE termin SET trainer_id=NULL WHERE trainer_id='TRN-005' AND termin_id<>?", termin.terminId());
        jdbc.update("UPDATE termin SET startdatum=?, enddatum=? WHERE termin_id=?", HEUTE.minusDays(1), HEUTE.plusDays(1), termin.terminId());
        assertFehler(() -> termine.aendern(ADMIN, termin.terminId(), form("SCH-001", HEUTE, HEUTE.plusDays(1), "oeffentlich", "hybrid", "Köln", null, "https://example.org", false)));
        assertThat(termine.aendern(ADMIN, termin.terminId(), form("SCH-001", HEUTE.minusDays(1), HEUTE, "oeffentlich", "hybrid", "Berlin", null, "https://example.org", false)).enddatum()).isEqualTo(HEUTE);
        jdbc.update("UPDATE termin SET enddatum=? WHERE termin_id=?", HEUTE.minusDays(1), termin.terminId());
        assertFehler(() -> termine.aendern(ADMIN, termin.terminId(), form("SCH-001", HEUTE.minusDays(2), HEUTE, "oeffentlich", "hybrid", "Berlin", null, "https://example.org", false)));

        var beendet = termine.anlegen(ADMIN, minimal("SCH-001", "2028-11-20", "2028-11-20"));
        jdbc.update("UPDATE termin SET startdatum=?, enddatum=? WHERE termin_id=?", HEUTE.minusDays(2), HEUTE.minusDays(1), beendet.terminId());
        assertThat(termine.aendern(ADMIN, beendet.terminId(), form("SCH-001", HEUTE.minusDays(2), HEUTE.minusDays(1),
                "oeffentlich", "vor_ort", "Bonn", null, null, false)).ort()).isEqualTo("Bonn");
        termine.absagen(ADMIN, beendet.terminId(), null);
        assertFehler(() -> termine.aendern(ADMIN, beendet.terminId(), form("SCH-001", HEUTE.minusDays(2), HEUTE.minusDays(1),
                "oeffentlich", "vor_ort", "Berlin", null, null, false)));
    }

    // verifies: TEST_TER_AEND_10
    @Test
    void schulungBleibtUnveraenderlichUndOperativeAenderungenWerdenMitgeteilt() {
        var termin = termine.anlegen(ADMIN, form("SCH-001", d("2028-12-01"), d("2028-12-01"), "oeffentlich", "vor_ort", "Köln", null, null, false));
        termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-005", false);
        termine.assistentZuweisen(ADMIN, termin.terminId(), "TRN-003");
        assertFehler(() -> termine.aendern(ADMIN, termin.terminId(), form("SCH-002", termin.startdatum(), termin.enddatum(), "oeffentlich", "vor_ort", "Köln", null, null, false)));
        termine.aendern(ADMIN, termin.terminId(), form("SCH-001", termin.startdatum(), termin.enddatum(), "oeffentlich", "hybrid", "Berlin", null, "https://neu.example", false));
        termine.aendern(ADMIN, termin.terminId(), form("SCH-001", d("2028-12-05"), d("2028-12-05"), "oeffentlich", "hybrid", "Berlin", null, "https://neu.example", false));
        assertThat(jdbc.queryForList("SELECT anlass FROM benachrichtigung WHERE empfaenger_id IN ('TRN-005','TRN-003')", String.class))
                .anyMatch(t -> t.contains("Ort: Köln") && t.contains("Berlin"))
                .anyMatch(t -> t.contains("Durchführungsart: vor_ort") && t.contains("hybrid"))
                .anyMatch(t -> t.contains("Zeitraum:") && t.contains("2028-12-05"))
                .anyMatch(t -> t.equals("Online-Zugang wurde geändert."));
        int vorher = jdbc.queryForObject("SELECT COUNT(*) FROM benachrichtigung", Integer.class);
        termine.aendern(ADMIN, termin.terminId(), form("SCH-001", d("2028-12-05"), d("2028-12-05"), null, "hybrid", "Berlin", null, "https://neu.example", false));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM benachrichtigung", Integer.class)).isEqualTo(vorher);
        termine.aendern(ADMIN, termin.terminId(), form("SCH-001", d("2028-12-05"), d("2028-12-05"), "exklusiv", "hybrid", "Berlin", "Acme", "https://neu.example", false));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM benachrichtigung WHERE anlass='Kundenfirma: null → Acme'", Integer.class)).isEqualTo(2);
    }

    // verifies: TEST_TER_AEND_04, TEST_TER_AEND_05, TEST_TER_AEND_06, TEST_TER_AEND_07, TEST_NAC_ANL_05
    @Test
    void absageIstEndgueltigErhaeltDatenWarntUndBenachrichtigtNurBeteiligte() {
        var termin = termine.anlegen(ADMIN, form("SCH-001", d("2028-12-04"), d("2028-12-04"), "oeffentlich", "remote", null, null, "https://example.org", false));
        termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-005", false);
        termine.assistentZuweisen(ADMIN, termin.terminId(), "TRN-003");
        termine.buchungAnlegen(ADMIN, termin.terminId(), buchung("Person", "Acme"));
        termine.buchungAnlegen(ADMIN, termin.terminId(), buchung("Zweite Person", "Acme"));
        var warnung = termine.warnung(ADMIN, termin.terminId());
        assertThat(warnung.anzahlBuchungen()).isEqualTo(2);
        assertThat(warnung.trainer()).isNotBlank();
        assertThat(warnung.assistenten()).hasSize(1);
        var abgesagt = termine.absagen(ADMIN, termin.terminId(), "Kunde verhindert");
        assertThat(abgesagt.status()).isEqualTo("abgesagt");
        assertThat(abgesagt.abgesagtAm()).isEqualTo(HEUTE);
        assertThat(abgesagt.absagegrund()).isEqualTo("Kunde verhindert");
        assertThat(abgesagt.onlineZugang()).isNull();
        assertThat(abgesagt.trainerId()).isEqualTo("TRN-005");
        assertThat(abgesagt.teilnehmer()).hasSize(2);
        assertFehler(() -> termine.aendern(ADMIN, termin.terminId(), minimal("SCH-001", "2028-12-04", "2028-12-04")));
        assertFehler(() -> termine.buchungAnlegen(ADMIN, termin.terminId(), buchung("Neu", "Acme")));
        assertFehler(() -> termine.buchungStatus(ADMIN, termin.terminId(),
                abgesagt.teilnehmer().getFirst().id(), "teilgenommen"));
        assertFehler(() -> termine.buchungLoeschen("TRN-005", termin.terminId(),
                abgesagt.teilnehmer().getFirst().id()));
        assertFehler(() -> termine.buchungLoeschen(ADMIN, termin.terminId(), abgesagt.teilnehmer().getFirst().id()));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM benachrichtigung WHERE anlass LIKE '%Kunde verhindert%'", Integer.class)).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM benachrichtigung WHERE anlasstyp='TERMIN_ABGESAGT'",
                Integer.class)).isEqualTo(2);
        var frei = termine.anlegen(ADMIN, minimal("SCH-006", "2028-12-04", "2028-12-04"));
        termine.trainerZuweisen(ADMIN, frei.terminId(), "TRN-005", false);

        var beendet = termine.anlegen(ADMIN, minimal("SCH-001", "2028-12-07", "2028-12-07"));
        termine.trainerZuweisen(ADMIN, beendet.terminId(), "TRN-005", false);
        jdbc.update("UPDATE termin SET startdatum=?, enddatum=? WHERE termin_id=?", HEUTE.minusDays(2), HEUTE.minusDays(1), beendet.terminId());
        assertThat(termine.absagen(ADMIN, beendet.terminId(), null).status()).isEqualTo("abgesagt");
        assertThat(jdbc.queryForList("SELECT anlass FROM benachrichtigung WHERE empfaenger_id='TRN-005'", String.class))
                .anyMatch(text -> text.equals("Der Termin " + beendet.terminId() + " wurde abgesagt."));

        var zuLoeschen = termine.anlegen(ADMIN, minimal("SCH-001", "2028-12-08", "2028-12-08"));
        termine.trainerZuweisen(ADMIN, zuLoeschen.terminId(), "TRN-005", false);
        termine.assistentZuweisen(ADMIN, zuLoeschen.terminId(), "TRN-003");
        termine.buchungAnlegen(ADMIN, zuLoeschen.terminId(), buchung("Person", "Acme"));
        assertThat(termine.warnung(ADMIN, zuLoeschen.terminId())).satisfies(w -> {
            assertThat(w.anzahlBuchungen()).isOne();
            assertThat(w.trainer()).isNotBlank();
            assertThat(w.assistenten()).hasSize(1);
        });
        termine.loeschen(ADMIN, zuLoeschen.terminId());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM benachrichtigung WHERE anlass LIKE '%wurde gelöscht%'", Integer.class)).isEqualTo(2);
    }

    // verifies: TEST_NAC_ANL_02
    @Test
    void mehrfeldAenderungBenachrichtigtJeFeldOhneOnlineZugangPreiszugeben() {
        var termin = termine.anlegen(ADMIN, form("SCH-001", d("2029-01-08"), d("2029-01-08"),
                "exklusiv", "hybrid", "Köln", "Alt GmbH", "https://alt.example", false));
        termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-005", false);
        termine.assistentZuweisen(ADMIN, termin.terminId(), "TRN-003");
        jdbc.update("DELETE FROM benachrichtigung");

        termine.aendern(ADMIN, termin.terminId(), form("SCH-001", d("2029-01-09"), d("2029-01-09"),
                "exklusiv", "beim_kunden", "Berlin", "Neu GmbH", null, true));

        for (String empfaenger : List.of("TRN-005", "TRN-003")) {
            var texte = jdbc.queryForList("""
                    SELECT anlass FROM benachrichtigung
                    WHERE empfaenger_id=? AND anlasstyp='TERMIN_GEAENDERT' ORDER BY id
                    """, String.class, empfaenger);
            assertThat(texte).hasSize(5)
                    .anyMatch(t -> t.contains("Zeitraum:") && t.contains("2029-01-08") && t.contains("2029-01-09"))
                    .anyMatch(t -> t.contains("Ort: Köln") && t.contains("Berlin"))
                    .anyMatch(t -> t.contains("Durchführungsart: hybrid") && t.contains("beim_kunden"))
                    .anyMatch(t -> t.contains("Kundenfirma: Alt GmbH") && t.contains("Neu GmbH"))
                    .anyMatch(t -> t.equals("Online-Zugang wurde geändert."));
            assertThat(texte).noneMatch(t -> t.contains("https://"));
        }

        termine.trainerAbziehen(ADMIN, termin.terminId());
        jdbc.update("DELETE FROM termin_assistent WHERE termin_id=? AND benutzerkonto_id='TRN-003'", termin.terminId());
        assertThat(termine.details("TRN-005", termin.terminId()).onlineZugang()).isNull();
        assertThat(termine.details("TRN-003", termin.terminId()).onlineZugang()).isNull();
    }

    // verifies: TEST_TER_AEND_08, TEST_TER_AEND_09
    @Test
    void nurNichtGestarteteGeplanteOderAbgesagteTermineSindLoeschbar() {
        var geplant = termine.anlegen(ADMIN, minimal("SCH-001", "2028-12-05", "2028-12-05"));
        termine.loeschen(ADMIN, geplant.terminId());
        assertFehler(() -> termine.details(ADMIN, geplant.terminId()));
        var abgesagt = termine.anlegen(ADMIN, minimal("SCH-001", "2028-12-06", "2028-12-06"));
        termine.absagen(ADMIN, abgesagt.terminId(), null);
        termine.loeschen(ADMIN, abgesagt.terminId());
        assertFehler(() -> termine.details(ADMIN, abgesagt.terminId()));
        var heute = termine.anlegen(ADMIN, minimal("SCH-001", HEUTE.toString(), HEUTE.toString()));
        assertFehler(() -> termine.loeschen(ADMIN, heute.terminId()));
        termine.absagen(ADMIN, heute.terminId(), null);
        assertFehler(() -> termine.loeschen(ADMIN, heute.terminId()));
        jdbc.update("UPDATE termin SET status='abgeschlossen', abschlussart='manuell', abgeschlossen_am=? WHERE termin_id=?", HEUTE, heute.terminId());
        jdbc.update("UPDATE termin SET trainer_id='TRN-005' WHERE termin_id=?", heute.terminId());
        jdbc.update("INSERT INTO teilnehmerbuchung (termin_id,name,firma,teilnahmestatus) VALUES (?, 'Historisch','Acme','teilgenommen')", heute.terminId());
        assertFehler(() -> termine.loeschen(ADMIN, heute.terminId()));
        assertThat(termine.details(ADMIN, heute.terminId())).satisfies(t -> {
            assertThat(t.trainerId()).isEqualTo("TRN-005");
            assertThat(t.anzahlBuchungen()).isPositive();
        });
    }

    // verifies: TEST_TER_ZUW_01, TEST_TER_ZUW_02, TEST_TER_ZUW_03
    @Test
    void zuweisungErfordertQualifikationUndKonfliktfreienZeitraum() {
        var termin = termine.anlegen(ADMIN, minimal("SCH-001", "2029-01-08", "2029-01-09"));
        jdbc.update("INSERT INTO abwesenheit (benutzerkonto_id, von, bis) VALUES ('TRN-005','2029-01-08','2029-01-08')");
        assertFehler(() -> termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-005", false));
        jdbc.update("DELETE FROM abwesenheit WHERE benutzerkonto_id='TRN-005' AND von='2029-01-08'");
        var assistenzTermin = termine.anlegen(ADMIN, minimal("SCH-006", "2029-01-08", "2029-01-09"));
        termine.assistentZuweisen(ADMIN, assistenzTermin.terminId(), "TRN-005");
        assertFehler(() -> termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-005", false));
        termine.absagen(ADMIN, assistenzTermin.terminId(), null);
        var anderer = termine.anlegen(ADMIN, minimal("SCH-006", "2029-01-08", "2029-01-09"));
        termine.trainerZuweisen(ADMIN, anderer.terminId(), "TRN-005", false);
        assertFehler(() -> termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-005", false));
        termine.absagen(ADMIN, anderer.terminId(), null);
        termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-005", false);
        assertFehler(() -> termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-002", false));
        jdbc.update("INSERT INTO trainer_qualifikation VALUES ('TRN-002','SCH-001')");
        termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-002", false);
    }

    // verifies: TEST_TER_ZUW_04, TEST_TER_ZUW_05, TEST_TER_ZUW_07
    @Test
    void abziehenAustauschenUndErstzuweisungWerdenKorrektMitgeteilt() {
        var termin = termine.anlegen(ADMIN, minimal("SCH-001", "2029-02-01", "2029-02-02"));
        termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-005", false);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM benachrichtigung WHERE empfaenger_id='TRN-005'", Integer.class)).isPositive();
        termine.trainerAbziehen(ADMIN, termin.terminId());
        assertThat(termine.details(ADMIN, termin.terminId()).trainerId()).isNull();
        assertThat(jdbc.queryForList("SELECT anlass FROM benachrichtigung WHERE empfaenger_id='TRN-005'", String.class))
                .anyMatch(text -> text.contains("wurde beendet"));
        jdbc.update("INSERT INTO trainer_qualifikation VALUES ('TRN-002','SCH-001')");
        termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-005", false);
        jdbc.update("INSERT INTO abwesenheit (benutzerkonto_id,von,bis) VALUES ('TRN-002','2029-02-01','2029-02-02')");
        assertFehler(() -> termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-002", false));
        assertThat(termine.details(ADMIN, termin.terminId()).trainerId()).isEqualTo("TRN-005");
        jdbc.update("DELETE FROM abwesenheit WHERE benutzerkonto_id='TRN-002' AND von='2029-02-01'");
        var belegt = termine.anlegen(ADMIN, minimal("SCH-008", "2029-02-01", "2029-02-02"));
        termine.trainerZuweisen(ADMIN, belegt.terminId(), "TRN-002", false);
        assertFehler(() -> termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-002", false));
        assertThat(termine.details(ADMIN, termin.terminId()).trainerId()).isEqualTo("TRN-005");
        termine.absagen(ADMIN, belegt.terminId(), null);
        termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-002", false);
        assertThat(termine.details(ADMIN, termin.terminId()).trainerId()).isEqualTo("TRN-002");
        assertThat(jdbc.queryForList("SELECT empfaenger_id FROM benachrichtigung", String.class))
                .contains("TRN-005", "TRN-002");

        var beimAnlegen = new TerminService.TerminEingabe("SCH-001", d("2029-02-05"), d("2029-02-05"),
                null, null, null, null, null, "TRN-005", false);
        var direkt = termine.anlegen(ADMIN, beimAnlegen);
        assertThat(direkt.trainerId()).isEqualTo("TRN-005");
        jdbc.update("UPDATE termin SET startdatum=?, enddatum=? WHERE termin_id=?", HEUTE.minusDays(1), HEUTE.minusDays(1), direkt.terminId());
        termine.trainerAbziehen(ADMIN, direkt.terminId());
        termine.trainerZuweisen(ADMIN, direkt.terminId(), "TRN-005", false);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM benachrichtigung WHERE empfaenger_id='TRN-005' AND anlass LIKE '%zugewiesen%'", Integer.class)).isGreaterThanOrEqualTo(2);
    }

    // verifies: TEST_TER_ZUW_08
    @Test
    void assistentWirdNurBestaetigtAtomarZumTrainer() {
        var termin = termine.anlegen(ADMIN, minimal("SCH-001", "2029-03-01", "2029-03-02"));
        termine.assistentZuweisen(ADMIN, termin.terminId(), "TRN-005");
        assertFehler(() -> termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-005", false));
        assertThat(termine.details(ADMIN, termin.terminId()).assistenten()).hasSize(1);
        termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-005", true);
        assertThat(termine.details(ADMIN, termin.terminId()).assistenten()).isEmpty();
        assertThat(termine.details(ADMIN, termin.terminId()).trainerId()).isEqualTo("TRN-005");
        assertThat(jdbc.queryForList("SELECT anlass FROM benachrichtigung WHERE empfaenger_id='TRN-005'", String.class))
                .anyMatch(text -> text.contains("statt Assistent"));
        assertFehler(() -> termine.assistentZuweisen(ADMIN, termin.terminId(), "TRN-005"));
    }

    // verifies: TEST_TER_ZUW_06
    @Test
    void zuweisungenSindNurAmGeplantenTerminAenderbarAuchNachDessenEnde() {
        var termin = termine.anlegen(ADMIN, minimal("SCH-001", "2029-04-02", "2029-04-02"));
        jdbc.update("UPDATE termin SET startdatum=?, enddatum=? WHERE termin_id=?", HEUTE.minusDays(1), HEUTE.minusDays(1), termin.terminId());
        termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-005", false);
        assertThat(termine.bestaetigen("TRN-005", termin.terminId()).status()).isEqualTo("abgeschlossen");
        assertFehler(() -> termine.trainerAbziehen(ADMIN, termin.terminId()));
        assertFehler(() -> termine.trainerZuweisen(ADMIN, termin.terminId(), ADMIN, false));

        var abgesagt = termine.anlegen(ADMIN, minimal("SCH-001", "2029-04-03", "2029-04-03"));
        termine.trainerZuweisen(ADMIN, abgesagt.terminId(), "TRN-005", false);
        termine.absagen(ADMIN, abgesagt.terminId(), null);
        assertFehler(() -> termine.trainerAbziehen(ADMIN, abgesagt.terminId()));
        assertFehler(() -> termine.trainerZuweisen(ADMIN, abgesagt.terminId(), ADMIN, false));
    }

    // verifies: TEST_TER_VORS_01, TEST_TER_VORS_02, TEST_TER_VORS_03
    @Test
    void traineroptionenZeigenNurQualifizierteMitGrundUndKalender() {
        var termin = termine.anlegen(ADMIN, minimal("SCH-001", "2029-05-02", "2029-05-03"));
        jdbc.update("INSERT INTO abwesenheit (benutzerkonto_id, von, bis) VALUES ('TRN-005','2029-05-02','2029-05-03')");
        var optionen = termine.trainerOptionen(ADMIN, termin.terminId());
        assertThat(optionen).extracting(TerminService.TrainerOption::id).containsExactlyInAnyOrder("TRN-001", "TRN-005");
        assertThat(optionen).filteredOn(o -> o.id().equals("TRN-005")).singleElement().satisfies(o -> {
            assertThat(o.verfuegbar()).isFalse();
            assertThat(o.grund()).contains("abwesend");
            assertThat(o.kalender()).anyMatch(k -> k.art().equals("abwesend"));
        });
        jdbc.update("INSERT INTO termin (termin_id,schulung_id,startdatum,enddatum,ort,status,trainer_id) VALUES ('SCH-001-TEST-BELEGT','SCH-001','2029-05-02','2029-05-03',NULL,'geplant','TRN-001')");
        assertThat(termine.trainerOptionen(ADMIN, termin.terminId()))
                .filteredOn(o -> o.id().equals("TRN-001")).singleElement()
                .satisfies(o -> {
                    assertThat(o.verfuegbar()).isFalse();
                    assertThat(o.grund()).contains("anderen Termin");
                    assertThat(o.kalender()).anyMatch(k -> k.art().equals("zugewiesen"));
                });
        assertThat(termine.trainerOptionen(ADMIN, "SCH-001", d("2029-05-02"), d("2029-05-03")))
                .extracting(TerminService.TrainerOption::id)
                .containsExactlyInAnyOrder("TRN-001", "TRN-005");
    }

    // verifies: TEST_TER_DASH_01, TEST_TER_DASH_02, TEST_TER_DASH_03, TEST_TER_DASH_04, TEST_TER_DASH_05
    @Test
    void dashboardsSortierenUndMarkierenRollenbezogen() {
        var ueberfaellig = termine.anlegen(ADMIN, minimal("SCH-001", "2029-06-01", "2029-06-01"));
        jdbc.update("UPDATE termin SET startdatum=?, enddatum=? WHERE termin_id=?", HEUTE.minusDays(3), HEUTE.minusDays(2), ueberfaellig.terminId());
        var dringend = termine.anlegen(ADMIN, minimal("SCH-001", HEUTE.plusWeeks(3).toString(), HEUTE.plusWeeks(3).toString()));
        var vierWochen = termine.anlegen(ADMIN, minimal("SCH-001", HEUTE.plusWeeks(4).toString(), HEUTE.plusWeeks(4).toString()));
        var exklusiv = termine.anlegen(ADMIN, form("SCH-001", HEUTE.plusWeeks(3), HEUTE.plusWeeks(3), "exklusiv", "remote", null, "Acme", null, false));
        termine.trainerZuweisen(ADMIN, exklusiv.terminId(), "TRN-005", false);
        var exklusivSpaeter = termine.anlegen(ADMIN, form("SCH-001", HEUTE.plusWeeks(5), HEUTE.plusWeeks(5), "exklusiv", "remote", null, "Acme", null, false));
        var ohneZugang = termine.anlegen(ADMIN, minimal("SCH-001", HEUTE.plusWeeks(3).plusDays(1).toString(), HEUTE.plusWeeks(3).plusDays(1).toString()));
        var laufend = termine.anlegen(ADMIN, minimal("SCH-001", "2029-06-04", "2029-06-04"));
        jdbc.update("UPDATE termin SET startdatum=?, enddatum=? WHERE termin_id=?", HEUTE.minusDays(1), HEUTE, laufend.terminId());
        var dashboard = termine.dashboard(ADMIN);
        assertThat(dashboard.getFirst().terminId()).isEqualTo(ueberfaellig.terminId());
        assertThat(dashboard.indexOf(dashboard.stream().filter(e -> e.terminId().equals(dringend.terminId())).findFirst().orElseThrow()))
                .isLessThan(dashboard.indexOf(dashboard.stream().filter(e -> e.terminId().equals(vierWochen.terminId())).findFirst().orElseThrow()));
        assertThat(dashboard).filteredOn(e -> e.terminId().equals(dringend.terminId())).singleElement().satisfies(e -> assertThat(e.dringend()).isTrue());
        assertThat(dashboard).filteredOn(e -> e.terminId().equals(vierWochen.terminId())).singleElement().satisfies(e -> assertThat(e.dringend()).isFalse());
        assertThat(dashboard).filteredOn(e -> e.terminId().equals(exklusiv.terminId())).singleElement().satisfies(e -> assertThat(e.mindestteilnehmerUnterschritten()).isTrue());
        assertThat(dashboard).filteredOn(e -> e.terminId().equals(exklusivSpaeter.terminId())).singleElement()
                .satisfies(e -> assertThat(e.mindestteilnehmerUnterschritten()).isFalse());
        assertThat(dashboard).filteredOn(e -> e.terminId().equals(ohneZugang.terminId())).singleElement()
                .satisfies(e -> assertThat(e.mindestteilnehmerUnterschritten()).isFalse());
        assertThat(dashboard).anyMatch(e -> e.terminId().equals(laufend.terminId()) && !e.ueberfaellig());
        var oeffentlich = termine.anlegen(ADMIN, form("SCH-001", HEUTE.plusWeeks(3).plusDays(1), HEUTE.plusWeeks(3).plusDays(1), "oeffentlich", "remote", null, null, null, false));
        assertThat(termine.dashboard(ADMIN)).filteredOn(e -> e.terminId().equals(oeffentlich.terminId())).singleElement()
                .satisfies(e -> assertThat(e.mindestteilnehmerUnterschritten()).isFalse());
        var oeffentlichVoll = termine.anlegen(ADMIN, form("SCH-001", HEUTE.plusWeeks(3).plusDays(4), HEUTE.plusWeeks(3).plusDays(4), "oeffentlich", "remote", null, null, null, false));
        termine.trainerZuweisen(ADMIN, oeffentlichVoll.terminId(), "TRN-005", false);
        var oeffentlichUeberbelegt = termine.anlegen(ADMIN, form("SCH-001", HEUTE.plusWeeks(3).plusDays(5), HEUTE.plusWeeks(3).plusDays(5), "oeffentlich", "remote", null, null, null, false));
        termine.trainerZuweisen(ADMIN, oeffentlichUeberbelegt.terminId(), "TRN-005", false);
        for (int i = 0; i < 12; i++) {
            termine.buchungAnlegen(ADMIN, oeffentlichVoll.terminId(), buchung("Voll " + i, "Acme"));
            termine.buchungAnlegen(ADMIN, oeffentlichUeberbelegt.terminId(), buchung("Überbelegt " + i, "Acme"));
        }
        termine.buchungAnlegen(ADMIN, oeffentlichUeberbelegt.terminId(), buchung("Überbelegt 12", "Acme"));
        dashboard = termine.dashboard(ADMIN);
        assertThat(dashboard).noneMatch(e -> e.terminId().equals(oeffentlichVoll.terminId()));
        assertThat(dashboard).filteredOn(e -> e.terminId().equals(oeffentlichUeberbelegt.terminId())).singleElement()
                .satisfies(e -> assertThat(e.hoechstteilnehmerUeberschritten()).isTrue());
        termine.trainerZuweisen(ADMIN, ueberfaellig.terminId(), "TRN-005", false);
        var anstehend = termine.anlegen(ADMIN, minimal("SCH-001", HEUTE.plusWeeks(6).toString(), HEUTE.plusWeeks(6).toString()));
        termine.trainerZuweisen(ADMIN, anstehend.terminId(), "TRN-005", false);
        assertThat(termine.dashboard("TRN-005").getFirst().terminId()).isEqualTo(ueberfaellig.terminId());
        jdbc.update("UPDATE termin SET status='abgesagt' WHERE termin_id=?", ueberfaellig.terminId());
        assertThat(termine.dashboard("TRN-005")).noneMatch(e -> e.terminId().equals(ueberfaellig.terminId()));
        jdbc.update("UPDATE termin SET status='abgeschlossen', abschlussart='manuell', abgeschlossen_am=? WHERE termin_id=?", HEUTE, anstehend.terminId());
        assertThat(termine.dashboard("TRN-005")).noneMatch(e -> e.terminId().equals(anstehend.terminId()));
    }

    // verifies: TEST_DSH_DRIN_01
    @Test
    void adminDashboardSortiertAlleZukuenftigenTrainerlosenTermineUndMarkiertVierWochenGrenze() {
        var inDreiWochen = termine.anlegen(ADMIN, minimal("SCH-001", HEUTE.plusWeeks(3).toString(), HEUTE.plusWeeks(3).toString()));
        var inVierWochen = termine.anlegen(ADMIN, minimal("SCH-001", HEUTE.plusWeeks(4).toString(), HEUTE.plusWeeks(4).toString()));
        var inSechsWochen = termine.anlegen(ADMIN, minimal("SCH-001", HEUTE.plusWeeks(6).toString(), HEUTE.plusWeeks(6).toString()));

        var eintraege = dashboard.anzeigen(ADMIN).dringlichkeiten().stream()
                .filter(e -> Set.of(inDreiWochen.terminId(), inVierWochen.terminId(), inSechsWochen.terminId())
                        .contains(e.terminId())).toList();

        assertThat(eintraege).extracting(TerminService.DashboardEintrag::terminId)
                .containsExactly(inDreiWochen.terminId(), inVierWochen.terminId(), inSechsWochen.terminId());
        assertThat(eintraege.get(0).dringend()).isTrue();
        assertThat(eintraege.get(1).dringend()).isFalse();
        assertThat(eintraege.get(2).dringend()).isFalse();
    }

    // verifies: TEST_DSH_DRIN_02
    @Test
    void adminDashboardStelltNurGeplanteUeberfaelligeVorAnstehendeTermine() {
        var ueberfaellig = termine.anlegen(ADMIN, minimal("SCH-001", "2029-06-04", "2029-06-04"));
        var abgesagt = termine.anlegen(ADMIN, minimal("SCH-001", "2029-06-04", "2029-06-04"));
        var abgeschlossen = termine.anlegen(ADMIN, minimal("SCH-001", "2029-06-04", "2029-06-04"));
        var anstehend = termine.anlegen(ADMIN, minimal("SCH-001", HEUTE.plusWeeks(2).toString(), HEUTE.plusWeeks(2).toString()));
        jdbc.update("UPDATE termin SET startdatum=?, enddatum=? WHERE termin_id=?",
                HEUTE.minusDays(4), HEUTE.minusDays(3), ueberfaellig.terminId());
        jdbc.update("UPDATE termin SET startdatum=?, enddatum=?, status='abgesagt' WHERE termin_id=?",
                HEUTE.minusDays(4), HEUTE.minusDays(3), abgesagt.terminId());
        jdbc.update("UPDATE termin SET startdatum=?, enddatum=?, status='abgeschlossen', abschlussart='manuell', abgeschlossen_am=? WHERE termin_id=?",
                HEUTE.minusDays(4), HEUTE.minusDays(3), HEUTE.minusDays(3), abgeschlossen.terminId());

        var ids = dashboard.anzeigen(ADMIN).dringlichkeiten().stream()
                .map(TerminService.DashboardEintrag::terminId).toList();

        assertThat(ids).contains(ueberfaellig.terminId(), anstehend.terminId())
                .doesNotContain(abgesagt.terminId(), abgeschlossen.terminId());
        assertThat(ids.indexOf(ueberfaellig.terminId())).isLessThan(ids.indexOf(anstehend.terminId()));
    }

    // verifies: TEST_DSH_DRIN_03
    @Test
    void adminDashboardWarntExklusivUnterMinimumUndOeffentlichNurUeberMaximum() {
        var exklusiv = terminMitTrainer("exklusiv", HEUTE.plusWeeks(3), "Acme");
        var oeffentlichLeer = terminMitTrainer("oeffentlich", HEUTE.plusWeeks(3).plusDays(1), null);
        var ohneZugang = terminMitTrainer(null, HEUTE.plusWeeks(3).plusDays(4), null);
        var oeffentlichVoll = terminMitTrainer("oeffentlich", HEUTE.plusWeeks(3).plusDays(5), null);
        var oeffentlichUeberbelegt = terminMitTrainer("oeffentlich", HEUTE.plusWeeks(3).plusDays(6), null);
        for (int i = 0; i < 12; i++) {
            termine.buchungAnlegen(ADMIN, oeffentlichVoll.terminId(), buchung("Voll " + i, "Acme"));
            termine.buchungAnlegen(ADMIN, oeffentlichUeberbelegt.terminId(), buchung("Über " + i, "Acme"));
        }
        termine.buchungAnlegen(ADMIN, oeffentlichUeberbelegt.terminId(), buchung("Über 12", "Acme"));

        var eintraege = dashboard.anzeigen(ADMIN).dringlichkeiten();

        assertThat(eintraege).filteredOn(e -> e.terminId().equals(exklusiv.terminId())).singleElement()
                .satisfies(e -> assertThat(e.mindestteilnehmerUnterschritten()).isTrue());
        assertThat(eintraege).filteredOn(e -> e.terminId().equals(oeffentlichUeberbelegt.terminId())).singleElement()
                .satisfies(e -> assertThat(e.hoechstteilnehmerUeberschritten()).isTrue());
        assertThat(eintraege).noneMatch(e -> Set.of(oeffentlichLeer.terminId(), ohneZugang.terminId(),
                oeffentlichVoll.terminId()).contains(e.terminId()));
    }

    @Test
    void faelligeTermineWerdenVorJederMutationNachgezogen() {
        var termin = termine.anlegen(ADMIN, minimal("SCH-001", "2028-01-03", "2028-01-03"));
        termine.buchungAnlegen(ADMIN, termin.terminId(), buchung("Person", "Acme"));
        jdbc.update("UPDATE termin SET startdatum='2026-07-16', enddatum='2026-07-17' WHERE termin_id=?",
                termin.terminId());

        assertFehler(() -> termine.absagen(ADMIN, termin.terminId(), null));
        assertThat(termine.details(ADMIN, termin.terminId()).status()).isEqualTo("abgeschlossen");
        assertFehler(() -> termine.buchungAnlegen(ADMIN, termin.terminId(), buchung("Zu spät", "Acme")));
    }

    @Test
    void trainerzugriffZiehtFaelligeTermineVorDerAntwortNach() {
        var termin = termine.anlegen(ADMIN, minimal("SCH-001", "2028-01-03", "2028-01-03"));
        termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-005", false);
        jdbc.update("UPDATE termin SET startdatum='2026-07-16', enddatum='2026-07-17' WHERE termin_id=?",
                termin.terminId());

        assertThat(trainereinsaetze.meineTermine("TRN-005"))
                .filteredOn(e -> e.terminId().equals(termin.terminId())).singleElement()
                .extracting(TrainereinsatzService.TrainerTermin::status).isEqualTo("abgeschlossen");
    }

    @Test
    void historischeTermineNutzenDenGespeichertenSchulungstitel() {
        jdbc.update("""
                INSERT INTO termin (termin_id, schulung_id, schulung_titel, startdatum, enddatum, status)
                VALUES ('SCH-999-T0001', 'SCH-999', 'Gelöschte Schulung', '2028-01-03', '2028-01-03', 'abgeschlossen')
                """);

        assertThat(termine.details(ADMIN, "SCH-999-T0001").schulungTitel()).isEqualTo("Gelöschte Schulung");
    }

    @Test
    void manuelleBestaetigungVerlangtWeiterhinDieQualifikation() {
        var termin = termine.anlegen(ADMIN, minimal("SCH-001", "2028-01-03", "2028-01-03"));
        termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-005", false);
        jdbc.update("DELETE FROM trainer_qualifikation WHERE benutzerkonto_id='TRN-005' AND schulung_id='SCH-001'");
        jdbc.update("UPDATE termin SET startdatum=?, enddatum=? WHERE termin_id=?", HEUTE, HEUTE, termin.terminId());

        assertThatThrownBy(() -> termine.bestaetigen(ADMIN, termin.terminId()))
                .isInstanceOf(KontoFehler.class).extracting("code").isEqualTo("QUALIFIKATION_ERFORDERLICH");
    }

    @Test
    void langeGueltigeFachwertePassenInBenachrichtigungen() {
        var termin = termine.anlegen(ADMIN, minimal("SCH-001", "2028-01-03", "2028-01-03"));
        termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-005", false);
        String grund = "x".repeat(1000);

        assertThat(termine.absagen(ADMIN, termin.terminId(), grund).absagegrund()).isEqualTo(grund);
        assertThat(jdbc.queryForObject("SELECT MAX(LENGTH(anlass)) FROM benachrichtigung", Integer.class))
                .isGreaterThan(1000);
    }

    // verifies: TEST_TER_DEMO_01
    @Test
    void demoSeedIstRelativRepraesentativUndFachlichVerknuepft() {
        assertThat(jdbc.queryForList("SELECT DISTINCT status FROM termin", String.class))
                .contains("geplant", "abgeschlossen", "abgesagt");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM termin WHERE startdatum<=? AND enddatum>=?", Integer.class, HEUTE, HEUTE)).isPositive();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM termin WHERE enddatum<?", Integer.class, HEUTE)).isPositive();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM termin WHERE startdatum>?", Integer.class, HEUTE)).isPositive();
        assertThat(jdbc.queryForList("SELECT DISTINCT zugangsart FROM termin", String.class)).contains("oeffentlich", "exklusiv");
        assertThat(jdbc.queryForList("SELECT DISTINCT durchfuehrungsart FROM termin", String.class)).contains("remote", "vor_ort", "hybrid");
        assertThat(jdbc.queryForList("SELECT DISTINCT durchfuehrungsart FROM termin", String.class)).contains("beim_kunden");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM termin WHERE trainer_id IS NULL", Integer.class)).isPositive();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM termin WHERE trainer_id IS NOT NULL", Integer.class)).isPositive();
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM termin WHERE status='abgeschlossen'
                  AND (online_zugang IS NOT NULL OR abschlussart='manuell' AND bestaetigt_von IS NULL)
                """, Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM termin WHERE status='abgesagt' AND abgesagt_von IS NULL", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM termin WHERE DAY_OF_WEEK(startdatum) IN (1,7) OR DAY_OF_WEEK(enddatum) IN (1,7)", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM teilnehmerbuchung", Integer.class)).isPositive();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM termin_assistent", Integer.class)).isPositive();
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM termin t LEFT JOIN trainer_qualifikation q
                  ON q.benutzerkonto_id=t.trainer_id AND q.schulung_id=t.schulung_id
                WHERE t.trainer_id IS NOT NULL AND q.benutzerkonto_id IS NULL
                """, Integer.class)).isZero();
    }

    private TerminService.TerminEingabe minimal(String schulung, String start, String ende) {
        return form(schulung, start == null ? null : d(start), ende == null ? null : d(ende), null, null, null, null, null, false);
    }

    private TerminService.TerminEingabe form(String schulung, LocalDate start, LocalDate ende,
            String zugang, String art, String ort, String firma, String online, boolean bestaetigt) {
        return new TerminService.TerminEingabe(schulung, start, ende, zugang, art, ort, firma, online, null, bestaetigt);
    }

    private TerminService.BuchungEingabe buchung(String name, String firma) {
        return new TerminService.BuchungEingabe(name, firma, "Bemerkung", "offen");
    }

    private TerminService.TerminAnsicht terminMitTrainer(String zugang, LocalDate datum, String firma) {
        var termin = termine.anlegen(ADMIN, form("SCH-001", datum, datum, zugang, "remote",
                null, firma, null, false));
        termine.trainerZuweisen(ADMIN, termin.terminId(), "TRN-005", false);
        return termin;
    }

    private static LocalDate d(String text) { return LocalDate.parse(text); }
    private static void assertFehler(Runnable aktion) { assertThatThrownBy(aktion::run).isInstanceOf(KontoFehler.class); }

    private static final class Setzt {
        static String ort(String art) { return Set.of("vor_ort", "beim_kunden", "hybrid").contains(art) ? "Köln" : null; }
        static String online(String art) { return Set.of("remote", "hybrid").contains(art) ? "https://example.org" : null; }
    }
}
