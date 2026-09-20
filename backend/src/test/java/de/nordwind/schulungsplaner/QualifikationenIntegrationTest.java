package de.nordwind.schulungsplaner;

import de.nordwind.schulungsplaner.domain.Benutzerkonto;
import de.nordwind.schulungsplaner.service.KontoFehler;
import de.nordwind.schulungsplaner.service.KontoService;
import de.nordwind.schulungsplaner.service.BenachrichtigungService;
import de.nordwind.schulungsplaner.service.Benachrichtigungsanlass;
import de.nordwind.schulungsplaner.service.DashboardService;
import de.nordwind.schulungsplaner.service.TrainereinsatzService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.demo-seed=false",
        "spring.datasource.url=jdbc:h2:mem:qualifikationentest;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"
})
@Import(QualifikationenIntegrationTest.Zeit.class)
@AutoConfigureMockMvc
@Transactional
class QualifikationenIntegrationTest {
    static final LocalDate HEUTE = LocalDate.of(2026, 9, 19);

    @Autowired TrainereinsatzService einsaetze;
    @Autowired KontoService konten;
    @Autowired JdbcTemplate jdbc;
    @Autowired VeraenderbareUhr uhr;
    @Autowired MockMvc mvc;
    @Autowired BenachrichtigungService benachrichtigungen;
    @Autowired DashboardService dashboard;

    Benutzerkonto admin;
    Benutzerkonto trainer;

    @BeforeEach
    void vorbereiten() {
        uhr.setze(HEUTE);
        jdbc.update("DELETE FROM benachrichtigung");
        jdbc.update("DELETE FROM qualifikationsbewerbung");
        jdbc.update("DELETE FROM trainer_qualifikation");
        jdbc.update("DELETE FROM termin");
        jdbc.update("UPDATE instanz SET eigentuemer_id=NULL WHERE id=1");
        jdbc.update("DELETE FROM benutzerkonto_rolle");
        jdbc.update("DELETE FROM benutzerkonto");
        admin = konten.registrieren("Admin", "admin@example.de", "pw");
        trainer = konten.registrieren("Trainer", "trainer@example.de", "pw");
    }

    // verifies: TEST_QUA_ENTSCHEID_01
    @Test
    void genehmigungErteiltGenauEineQualifikationUndSchliesstDieBewerbung() {
        long id = bewerben();
        einsaetze.bewerbungGenehmigen(admin.id(), id);

        assertThat(qualifikationen()).isOne();
        assertThat(bewerbungsstatus(id)).isEqualTo("GENEHMIGT");
        assertThatThrownBy(() -> einsaetze.aufQualifikationBewerben(trainer.id(), "SCH-001"))
                .isInstanceOfSatisfying(KontoFehler.class,
                        fehler -> assertThat(fehler.code()).isEqualTo("BEREITS_QUALIFIZIERT"));
    }

    // verifies: TEST_QUA_BEW_01
    @Test
    void eineQualifikationGiltFuerAlleTermineDerSchulung() {
        einsaetze.bewerbungGenehmigen(admin.id(), bewerben());
        termin("ERSTER", HEUTE.plusDays(2), "geplant", null);
        termin("ZWEITER", HEUTE.plusDays(3), "geplant", null);

        einsaetze.trainerZuweisen(admin.id(), "ERSTER", trainer.id());
        einsaetze.trainerZuweisen(admin.id(), "ZWEITER", trainer.id());

        assertThat(trainerVon("ERSTER")).isEqualTo(trainer.id());
        assertThat(trainerVon("ZWEITER")).isEqualTo(trainer.id());
        assertThat(qualifikationen()).isOne();
    }

    @Test
    void bestehendeQualifikationVerhindertEineBewerbung() {
        qualifizieren();
        assertThatThrownBy(() -> einsaetze.aufQualifikationBewerben(trainer.id(), "SCH-001"))
                .isInstanceOfSatisfying(KontoFehler.class,
                        fehler -> assertThat(fehler.code()).isEqualTo("BEREITS_QUALIFIZIERT"));
        assertThat(einsaetze.meineQualifikationen(trainer.id())).singleElement()
                .extracting(TrainereinsatzService.EigenerQualifikationsstand::status)
                .isEqualTo("QUALIFIZIERT");
    }

    // verifies: TEST_QUA_BEW_03
    @Test
    void nurEineOffeneBewerbungJeSchulungIstMoeglich() {
        bewerben();
        assertThatThrownBy(() -> einsaetze.aufQualifikationBewerben(trainer.id(), "SCH-001"))
                .isInstanceOfSatisfying(KontoFehler.class,
                        fehler -> assertThat(fehler.code()).isEqualTo("BEWERBUNG_BESTEHT"));
        assertThat(offeneBewerbungen()).isOne();
    }

    @Test
    void zurueckziehenErmoeglichtEineNeueBewerbung() {
        bewerben();
        einsaetze.bewerbungZurueckziehen(trainer.id(), "SCH-001");
        assertThat(offeneBewerbungen()).isZero();
        einsaetze.aufQualifikationBewerben(trainer.id(), "SCH-001");
        assertThat(offeneBewerbungen()).isOne();
    }

    @Test
    void qualifikationsverwaltungIstAuthentifiziertRollenUndCsrfGeschuetzt() throws Exception {
        long id = bewerben();
        MockHttpSession trainerSitzung = login(trainer);
        MockHttpSession adminSitzung = login(admin);

        mvc.perform(get("/api/ich/qualifikationen")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/ich/benachrichtigungen")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/schulungen/SCH-001/qualifikationen"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/schulungen/SCH-001/qualifikationen").session(trainerSitzung))
                .andExpect(status().isForbidden());
        for (MockHttpServletRequestBuilder anfrage : mutationen(id)) {
            mvc.perform(anfrage.with(csrf())).andExpect(status().isUnauthorized());
        }
        for (MockHttpServletRequestBuilder anfrage : eigeneMutationen()) {
            mvc.perform(anfrage.session(trainerSitzung)).andExpect(status().isForbidden());
        }
        for (MockHttpServletRequestBuilder anfrage : adminMutationen(id)) {
            mvc.perform(anfrage.session(adminSitzung)).andExpect(status().isForbidden());
        }
        for (MockHttpServletRequestBuilder anfrage : adminMutationen(id)) {
            mvc.perform(anfrage.session(trainerSitzung).with(csrf())).andExpect(status().isForbidden());
        }
        assertThat(bewerbungsstatus(id)).isEqualTo("OFFEN");

        mvc.perform(post("/api/qualifikationsbewerbungen/{id}/genehmigung", id)
                        .session(adminSitzung).with(csrf()))
                .andExpect(status().isNoContent());
        assertThat(qualifikationen()).isOne();
    }

    @Test
    void restVertraegeLiefernErfolgValidierungsfehlerUndKonflikt() throws Exception {
        MockHttpSession trainerSitzung = login(trainer);
        MockHttpSession adminSitzung = login(admin);

        mvc.perform(post("/api/ich/qualifikationsbewerbungen/SCH-001")
                        .session(trainerSitzung).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/ich/qualifikationsbewerbungen/SCH-001")
                        .session(trainerSitzung).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BEWERBUNG_BESTEHT"));
        long id = jdbc.queryForObject("SELECT id FROM qualifikationsbewerbung WHERE benutzerkonto_id=?",
                Long.class, trainer.id());
        mvc.perform(post("/api/qualifikationsbewerbungen/{id}/ablehnung", id)
                        .session(adminSitzung).with(csrf()).contentType("application/json")
                        .content("{\"begruendung\":\"\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/qualifikationsbewerbungen/{id}/genehmigung", id)
                        .session(adminSitzung).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/ich/qualifikationen").session(trainerSitzung))
                .andExpect(status().isOk());
    }

    @Test
    void ungueltigeSchulungsIdWirdAlsNichtGefundenBehandelt() {
        assertThatThrownBy(() -> einsaetze.qualifikationenDerSchulung(admin.id(), "../falsch"))
                .isInstanceOfSatisfying(KontoFehler.class, fehler -> {
                    assertThat(fehler.status()).isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
                    assertThat(fehler.code()).isEqualTo("SCHULUNG_NICHT_GEFUNDEN");
                });
    }

    @Test
    void offeneBewerbungBleibtNachStilllegungUndRollenentzugImAdminbereichSichtbar() {
        bewerben();
        jdbc.update("UPDATE benutzerkonto SET aktiv=FALSE WHERE id=?", trainer.id());
        jdbc.update("DELETE FROM benutzerkonto_rolle WHERE benutzerkonto_id=? AND rolle='TRAINER'", trainer.id());

        assertThat(einsaetze.qualifikationenDerSchulung(admin.id(), "SCH-001"))
                .filteredOn(zeile -> zeile.trainerId().equals(trainer.id())).singleElement()
                .extracting(TrainereinsatzService.Qualifikationszeile::status)
                .isEqualTo("OFFEN");
    }

    // verifies: TEST_QUA_ENTSCHEID_02
    @Test
    void genehmigungSchaltetNurKuenftigeGeplanteTermineFrei() {
        termin("ZUKUNFT", HEUTE.plusDays(2), "geplant", null);
        termin("VERGANGEN", HEUTE.minusDays(2), "geplant", null);
        assertThat(einsaetze.meineTermine(trainer.id())).isEmpty();

        einsaetze.bewerbungGenehmigen(admin.id(), bewerben());

        assertThat(einsaetze.meineTermine(trainer.id()))
                .extracting(TrainereinsatzService.TrainerTermin::terminId)
                .containsExactly("ZUKUNFT");
    }

    // verifies: TEST_QUA_ENTSCHEID_03
    @Test
    void ablehnungVerlangtEineBegruendungUndErteiltKeineQualifikation() {
        long id = bewerben();
        assertThatThrownBy(() -> einsaetze.bewerbungAblehnen(admin.id(), id, "  "))
                .isInstanceOfSatisfying(KontoFehler.class,
                        fehler -> assertThat(fehler.code()).isEqualTo("BEGRUENDUNG_ERFORDERLICH"));
        assertThat(bewerbungsstatus(id)).isEqualTo("OFFEN");

        einsaetze.bewerbungAblehnen(admin.id(), id, "Nachweis fehlt");
        assertThat(bewerbungsstatus(id)).isEqualTo("ABGELEHNT");
        assertThat(qualifikationen()).isZero();
    }

    // verifies: TEST_QUA_ENTSCHEID_04
    @Test
    void sperrfristEndetNachEinemTag() {
        long id = bewerben();
        einsaetze.bewerbungAblehnen(admin.id(), id, "Noch nicht");
        assertThatThrownBy(() -> einsaetze.aufQualifikationBewerben(trainer.id(), "SCH-001"))
                .isInstanceOfSatisfying(KontoFehler.class,
                        fehler -> assertThat(fehler.code()).isEqualTo("BEWERBUNG_GESPERRT"));

        uhr.setze(HEUTE.plusDays(1));
        assertThat(einsaetze.meineQualifikationen(trainer.id())).isEmpty();
        einsaetze.aufQualifikationBewerben(trainer.id(), "SCH-001");
        assertThat(bewerbungsstatus(id)).isEqualTo("OFFEN");
    }

    @Test
    void benachrichtigungNenntEntscheidungUndNurBeiAblehnungDenGrund() {
        einsaetze.bewerbungGenehmigen(admin.id(), bewerben());
        String genehmigt = letzteNachricht(trainer.id());
        assertThat(genehmigt).contains("genehmigt").doesNotContain("Nachweis fehlt");

        Benutzerkonto zweiter = konten.registrieren("Zweiter", "zwei@example.de", "pw");
        einsaetze.aufQualifikationBewerben(zweiter.id(), "SCH-002");
        long id = jdbc.queryForObject("SELECT id FROM qualifikationsbewerbung WHERE benutzerkonto_id=?",
                Long.class, zweiter.id());
        einsaetze.bewerbungAblehnen(admin.id(), id, "Nachweis fehlt");
        assertThat(letzteNachricht(zweiter.id())).contains("abgelehnt", "Nachweis fehlt");
    }

    // verifies: TEST_QUA_ENTZUG_01
    @Test
    void entzugRaeumtKuenftigeZuweisungAbUndBewahrtAbgeschlossene() {
        qualifizieren();
        termin("ZUKUNFT", HEUTE.plusDays(2), "geplant", trainer.id());
        termin("FERTIG", HEUTE.minusDays(2), "abgeschlossen", trainer.id());

        einsaetze.qualifikationEntziehen(admin.id(), "SCH-001", trainer.id());

        assertThat(trainerVon("ZUKUNFT")).isNull();
        assertThat(trainerVon("FERTIG")).isEqualTo(trainer.id());
    }

    @Test
    void entzugBenachrichtigtDenTrainer() {
        qualifizieren();
        einsaetze.qualifikationEntziehen(admin.id(), "SCH-001", trainer.id());
        assertThat(letzteNachricht(trainer.id())).contains("entzogen", "SCH-001");
    }

    // verifies: TEST_QUA_ENTZUG_03
    @Test
    void entzugOhneQualifikationWirdAbgewiesen() {
        assertThatThrownBy(() -> einsaetze.qualifikationEntziehen(admin.id(), "SCH-001", trainer.id()))
                .isInstanceOfSatisfying(KontoFehler.class,
                        fehler -> assertThat(fehler.code()).isEqualTo("NICHT_QUALIFIZIERT"));
    }

    // verifies: TEST_QUA_ENTZUG_04
    @Test
    void nachEntzugIstEineBewerbungSofortMoeglich() {
        long id = bewerben();
        einsaetze.bewerbungAblehnen(admin.id(), id, "Noch nicht");
        einsaetze.direktQualifizieren(admin.id(), "SCH-001", trainer.id());
        einsaetze.qualifikationEntziehen(admin.id(), "SCH-001", trainer.id());
        einsaetze.aufQualifikationBewerben(trainer.id(), "SCH-001");
        assertThat(offeneBewerbungen()).isOne();
    }

    @Test
    void ablegenMeldetKuenftigeTermineUndRaeumtNurDieseAb() {
        qualifizieren();
        termin("ZUKUNFT", HEUTE.plusDays(2), "geplant", trainer.id());
        termin("FERTIG", HEUTE.minusDays(2), "abgeschlossen", trainer.id());
        assertThat(einsaetze.meineQualifikationen(trainer.id())).singleElement()
                .extracting(TrainereinsatzService.EigenerQualifikationsstand::kuenftigeTermine)
                .isEqualTo(1);

        einsaetze.eigeneQualifikationAblegen(trainer.id(), "SCH-001");
        assertThat(trainerVon("ZUKUNFT")).isNull();
        assertThat(trainerVon("FERTIG")).isEqualTo(trainer.id());
    }

    // verifies: TEST_QUA_ABLEGEN_03
    @Test
    void nachAblegenIstEineBewerbungSofortMoeglich() {
        einsaetze.bewerbungGenehmigen(admin.id(), bewerben());
        einsaetze.eigeneQualifikationAblegen(trainer.id(), "SCH-001");
        einsaetze.aufQualifikationBewerben(trainer.id(), "SCH-001");
        assertThat(offeneBewerbungen()).isOne();
    }

    @Test
    void ablegenBenachrichtigtAlleAdministratoren() {
        qualifizieren();
        einsaetze.eigeneQualifikationAblegen(trainer.id(), "SCH-001");
        assertThat(letzteNachricht(admin.id())).contains("Trainer", "abgelegt", "SCH-001");
    }

    // verifies: TEST_QUA_DIREKT_01
    @Test
    void direktvergabeErteiltOhneBewerbungUndSchaltetTermineFrei() {
        termin("ZUKUNFT", HEUTE.plusDays(2), "geplant", null);
        einsaetze.direktQualifizieren(admin.id(), "SCH-001", trainer.id());
        assertThat(qualifikationen()).isOne();
        assertThat(einsaetze.meineTermine(trainer.id()))
                .extracting(TrainereinsatzService.TrainerTermin::terminId).containsExactly("ZUKUNFT");
    }

    @Test
    void bereitsQualifizierterTrainerIstInDerTabelleNichtErneutVergabefaehig() {
        qualifizieren();
        assertThat(einsaetze.qualifikationenDerSchulung(admin.id(), "SCH-001"))
                .filteredOn(zeile -> zeile.trainerId().equals(trainer.id())).singleElement()
                .extracting(TrainereinsatzService.Qualifikationszeile::status)
                .isEqualTo("QUALIFIZIERT");
        assertThatThrownBy(() -> einsaetze.direktQualifizieren(admin.id(), "SCH-001", trainer.id()))
                .isInstanceOf(KontoFehler.class);
    }

    // verifies: TEST_QUA_DIREKT_03
    @Test
    void direktvergabeGenehmigtOffeneBewerbungUndErzeugtEineQualifikation() {
        long id = bewerben();
        einsaetze.direktQualifizieren(admin.id(), "SCH-001", trainer.id());
        assertThat(bewerbungsstatus(id)).isEqualTo("GENEHMIGT");
        assertThat(qualifikationen()).isOne();
    }

    @Test
    void direktvergabeBenachrichtigtDenTrainerWieEineGenehmigung() {
        einsaetze.direktQualifizieren(admin.id(), "SCH-001", trainer.id());
        assertThat(letzteNachricht(trainer.id())).contains("direkt", "qualifiziert", "SCH-001");
    }

    @Test
    void explizitesMarkierenErfasstPersoenlicheUndGemeinsameAdminMitteilungen() {
        benachrichtigungen.persoenlich(trainer.id(), admin.id(),
                Benachrichtigungsanlass.QUALIFIKATION_GENEHMIGT,
                "Persönlich", "SCHULUNG", "SCH-001");
        benachrichtigungen.adminbereich(Benachrichtigungsanlass.QUALIFIKATION_ABGELEGT,
                "Gemeinsam", "SCHULUNG", "SCH-001");
        Benutzerkonto zweiterAdmin = konten.registrieren("Zweiter Admin", "admin2@example.de", "pw");
        konten.rolleErteilen(admin.id(), zweiterAdmin.id(), de.nordwind.schulungsplaner.domain.Rolle.ADMINISTRATOR,
                zweiterAdmin.aenderungsstand());

        assertThat(benachrichtigungen.ungelesen(trainer.id())).isEqualTo(1);
        assertThat(benachrichtigungen.ungelesen(admin.id())).isEqualTo(1);
        assertThat(benachrichtigungen.anzeigen(admin.id())).singleElement()
                .extracting(BenachrichtigungService.Benachrichtigung::anlass).isEqualTo("Gemeinsam");
        benachrichtigungen.allesAlsGelesen(admin.id());
        assertThat(benachrichtigungen.ungelesen(admin.id())).isZero();
        assertThat(benachrichtigungen.ungelesen(zweiterAdmin.id())).isZero();
        assertThat(benachrichtigungen.ungelesen(trainer.id())).isEqualTo(1);
    }

    @Test
    void dashboardTrenntAnMichVonVonMirUndBewahrtErledigtes() {
        long id = bewerben();
        assertThat(dashboard.anzeigen(admin.id()).vorgaenge()).singleElement()
                .extracting(DashboardService.Vorgang::id).isEqualTo(id);
        assertThat(dashboard.anzeigen(trainer.id()).eigeneVorgaenge()).singleElement()
                .extracting(DashboardService.Vorgang::id).isEqualTo(id);

        einsaetze.bewerbungGenehmigen(admin.id(), id);

        assertThat(dashboard.anzeigen(admin.id()).vorgaenge()).isEmpty();
        assertThat(dashboard.anzeigen(admin.id()).erledigteVorgaenge()).singleElement()
                .extracting(DashboardService.Vorgang::status).isEqualTo("GENEHMIGT");
    }

    private long bewerben() {
        einsaetze.aufQualifikationBewerben(trainer.id(), "SCH-001");
        return jdbc.queryForObject("SELECT id FROM qualifikationsbewerbung WHERE benutzerkonto_id=?",
                Long.class, trainer.id());
    }

    private void qualifizieren() {
        jdbc.update("INSERT INTO trainer_qualifikation VALUES (?, 'SCH-001')", trainer.id());
    }

    private int qualifikationen() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM trainer_qualifikation WHERE benutzerkonto_id=?",
                Integer.class, trainer.id());
    }

    private int offeneBewerbungen() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM qualifikationsbewerbung WHERE benutzerkonto_id=? AND status='OFFEN'",
                Integer.class, trainer.id());
    }

    private String bewerbungsstatus(long id) {
        return jdbc.queryForObject("SELECT status FROM qualifikationsbewerbung WHERE id=?", String.class, id);
    }

    private String letzteNachricht(String kontoId) {
        return jdbc.queryForObject("""
                SELECT anlass FROM benachrichtigung
                WHERE empfaenger_id=? OR (empfaenger_rolle='ADMINISTRATOR' AND EXISTS (
                    SELECT 1 FROM benutzerkonto_rolle WHERE benutzerkonto_id=? AND rolle='ADMINISTRATOR'))
                ORDER BY id DESC LIMIT 1
                """, String.class, kontoId, kontoId);
    }

    private void termin(String id, LocalDate datum, String status, String trainerId) {
        jdbc.update("""
                INSERT INTO termin (termin_id, schulung_id, schulung_titel, startdatum, enddatum,
                                    status, trainer_id, trainer_name_snapshot)
                VALUES (?, 'SCH-001', 'Schulung', ?, ?, ?, ?, ?)
                """, id, datum, datum, status, trainerId, trainerId == null ? null : trainer.name());
    }

    private String trainerVon(String terminId) {
        return jdbc.queryForObject("SELECT trainer_id FROM termin WHERE termin_id=?", String.class, terminId);
    }

    private MockHttpSession login(Benutzerkonto konto) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/auth/anmelden").with(csrf())
                        .contentType("application/json")
                        .content("{\"email\":\"" + konto.email() + "\",\"passwort\":\"pw\"}"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }

    private java.util.List<MockHttpServletRequestBuilder> mutationen(long bewerbungId) {
        var anfragen = new java.util.ArrayList<>(eigeneMutationen());
        anfragen.addAll(adminMutationen(bewerbungId));
        return anfragen;
    }

    private java.util.List<MockHttpServletRequestBuilder> eigeneMutationen() {
        return java.util.List.of(
                post("/api/ich/qualifikationsbewerbungen/SCH-001"),
                delete("/api/ich/qualifikationsbewerbungen/SCH-001"),
                delete("/api/ich/qualifikationen/SCH-001"));
    }

    private java.util.List<MockHttpServletRequestBuilder> adminMutationen(long bewerbungId) {
        return java.util.List.of(
                put("/api/schulungen/SCH-001/qualifikationen/{id}", trainer.id()),
                delete("/api/schulungen/SCH-001/qualifikationen/{id}", trainer.id()),
                post("/api/qualifikationsbewerbungen/{id}/genehmigung", bewerbungId),
                post("/api/qualifikationsbewerbungen/{id}/ablehnung", bewerbungId)
                        .contentType("application/json").content("{\"begruendung\":\"Grund\"}"));
    }

    static class Zeit {
        @Bean @Primary VeraenderbareUhr testClock() {
            return new VeraenderbareUhr();
        }
    }

    static class VeraenderbareUhr extends Clock {
        private Instant jetzt = HEUTE.atStartOfDay(ZoneOffset.UTC).toInstant();
        void setze(LocalDate datum) { jetzt = datum.atStartOfDay(ZoneOffset.UTC).toInstant(); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return jetzt; }
    }
}
