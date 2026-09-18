package de.nordwind.schulungsplaner;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import de.nordwind.schulungsplaner.domain.Benutzerkonto;
import de.nordwind.schulungsplaner.domain.Rolle;
import de.nordwind.schulungsplaner.katalog.KatalogAnsichtService;
import de.nordwind.schulungsplaner.service.KontoFehler;
import de.nordwind.schulungsplaner.service.KontoService;
import de.nordwind.schulungsplaner.service.TrainerQueryService;
import de.nordwind.schulungsplaner.service.TrainereinsatzService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.demo-seed=false",
        "spring.datasource.url=jdbc:h2:mem:kontotest;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"
})
@AutoConfigureMockMvc
@Import(BenutzerkontoIntegrationTest.FixedClockConfig.class)
class BenutzerkontoIntegrationTest {
    private static final LocalDate HEUTE = LocalDate.of(2026, 9, 15);

    @Autowired KontoService konten;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired TrainerQueryService schulungen;
    @Autowired KatalogAnsichtService katalog;
    @Autowired TrainereinsatzService einsaetze;

    @BeforeEach
    void leeren() {
        jdbc.update("UPDATE instanz SET eigentuemer_id = NULL WHERE id = 1");
        jdbc.update("DELETE FROM assistenzbewerbung");
        jdbc.update("DELETE FROM qualifikationsbewerbung");
        jdbc.update("DELETE FROM termin_assistent");
        jdbc.update("DELETE FROM trainer_qualifikation");
        jdbc.update("DELETE FROM termin");
        jdbc.update("DELETE FROM abwesenheit");
        jdbc.update("DELETE FROM benutzerkonto_rolle");
        jdbc.update("DELETE FROM benutzerkonto");
    }

    // verifies: TEST_USR_EIGT_01, TEST_USR_EIGT_06, TEST_USR_PWD_04, TEST_USR_ROLLE_06
    @Test
    void ersteUndParalleleRegistrierungErzeugenGenauEinenEigentuemer() throws Exception {
        try (var pool = Executors.newFixedThreadPool(2)) {
            var start = new CyclicBarrier(2);
            List<Callable<Benutzerkonto>> aufgaben = List.of(
                    () -> { start.await(); return konten.registrieren("Eins", "eins@example.de", "gleich"); },
                    () -> { start.await(); return konten.registrieren("Zwei", "zwei@example.de", "gleich"); });
            List<Benutzerkonto> ergebnisse = pool.invokeAll(aufgaben).stream()
                    .map(f -> { try { return f.get(); } catch (Exception e) { throw new RuntimeException(e); } })
                    .toList();

            assertThat(ergebnisse).filteredOn(k -> k.rollen().contains(Rolle.EIGENTUEMER)).hasSize(1);
            assertThat(ergebnisse).filteredOn(k -> k.rollen().contains(Rolle.EIGENTUEMER))
                    .singleElement().satisfies(k -> assertThat(k.rollen())
                            .containsExactlyInAnyOrder(Rolle.TRAINER, Rolle.ADMINISTRATOR, Rolle.EIGENTUEMER));
            assertThat(ergebnisse).filteredOn(k -> !k.rollen().contains(Rolle.EIGENTUEMER))
                    .singleElement().satisfies(k -> assertThat(k.rollen()).containsExactly(Rolle.TRAINER));
            assertThat(konten.alle()).allSatisfy(k -> assertThat(k.rollen()).isNotEmpty());
            assertThat(jdbc.queryForList("SELECT passwort_hash FROM benutzerkonto", String.class))
                    .hasSize(2).doesNotHaveDuplicates().noneMatch("gleich"::equals);
        }
    }

    // verifies: TEST_USR_EIGT_02
    @Test
    void zweitesKontoIstNurTrainerUndAendertDenEigentuemerNicht() {
        Benutzerkonto erstes = konten.registrieren("Erstes", "erstes@example.de", "pw");
        Benutzerkonto zweites = konten.registrieren("Zweites", "zweites@example.de", "pw");

        assertThat(konten.laden(erstes.id()).rollen())
                .containsExactlyInAnyOrder(Rolle.TRAINER, Rolle.ADMINISTRATOR, Rolle.EIGENTUEMER);
        assertThat(konten.laden(zweites.id()).rollen()).containsExactly(Rolle.TRAINER);
        assertThat(konten.alle()).filteredOn(k -> k.rollen().contains(Rolle.EIGENTUEMER))
                .extracting(Benutzerkonto::id).containsExactly(erstes.id());
    }

    // verifies: TEST_USR_LOGIN_03, TEST_USR_REG_04
    @Test
    void emailIstNormalisiertEindeutigUndLoginMeldetKeineFalscheUrsache() throws Exception {
        konten.registrieren("Eins", " Eins@Example.DE ", "richtig");
        int anzahl = konten.alle().size();
        assertThatThrownBy(() -> konten.registrieren("Doppelt", "eins@example.de", "x"))
                .isInstanceOfSatisfying(KontoFehler.class,
                        f -> assertThat(f.code()).isEqualTo("EMAIL_VERGEBEN"));
        assertThat(konten.alle()).hasSize(anzahl);
        assertThat(konten.anmelden("  EINS@EXAMPLE.DE ", "richtig").email())
                .isEqualTo("eins@example.de");

        String unbekannt = loginFehler("niemand@example.de", "falsch");
        String falschesPasswort = loginFehler("EINS@example.de", "falsch");
        assertThat(unbekannt).isEqualTo(falschesPasswort).contains("UNGUELTIGE_ANMELDEDATEN");
        JsonNode fehler = json.readTree(unbekannt);
        assertThat(fehler.size()).isEqualTo(2);
        assertThat(fehler.path("code").stringValue()).isEqualTo("UNGUELTIGE_ANMELDEDATEN");
        assertThat(fehler.path("message").stringValue())
                .isEqualTo("E-Mail-Adresse oder Passwort ist falsch.");
    }

    // verifies: TEST_USR_PWD_01, TEST_USR_REG_02
    @Test
    void passwortwechselBehaeltAktuelleUndBeendetAndereSitzung() throws Exception {
        Benutzerkonto konto = konten.registrieren("Eins", "eins@example.de", "alt");
        MockHttpSession aktuell = login("eins@example.de", "alt");
        MockHttpSession andere = login("eins@example.de", "alt");

        assertThatThrownBy(() -> konten.passwortAendern(
                konto.id(), "falsch", "neu", konto.aenderungsstand()))
                .isInstanceOfSatisfying(KontoFehler.class,
                        f -> assertThat(f.code()).isEqualTo("PASSWORT_FALSCH"));
        assertThat(konten.anmelden(konto.email(), "alt").id()).isEqualTo(konto.id());

        mvc.perform(put("/api/ich/passwort")
                        .session(aktuell).with(csrf()).contentType("application/json")
                        .header("If-Match", konto.aenderungsstand())
                        .content("{\"bisherigesPasswort\":\"alt\",\"neuesPasswort\":\"neu\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/auth/ich").session(aktuell)).andExpect(status().isOk());
        mvc.perform(get("/api/auth/ich").session(andere)).andExpect(status().isUnauthorized());
        assertThat(konten.anmelden(konto.email(), "neu").id()).isEqualTo(konto.id());
        assertThatThrownBy(() -> konten.anmelden(konto.email(), "alt"))
                .isInstanceOf(KontoFehler.class);
    }

    // verifies: TEST_USR_REG_01
    @Test
    void registrierungVerlangtGenauDreiAngaben() throws Exception {
        String antwort = mvc.perform(post("/api/auth/registrieren").with(csrf())
                        .contentType("application/json")
                        .content("{\"name\":\"X\",\"email\":\"x@example.de\",\"passwort\":\"pw\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        JsonNode kontoAntwort = json.readTree(antwort);
        assertThat(kontoAntwort.has("id") && kontoAntwort.has("email") && kontoAntwort.has("name")
                && kontoAntwort.has("rollen") && kontoAntwort.has("zustand")).isTrue();
        assertThat(kontoAntwort.has("passwort") || kontoAntwort.has("passwortHash")
                || kontoAntwort.has("passwort_hash")).isFalse();
        assertThat(konten.alle()).singleElement().satisfies(konto -> {
            assertThat(konto.name()).isEqualTo("X");
            assertThat(konto.email()).isEqualTo("x@example.de");
        });
        List<String> unvollstaendig = List.of(
                "{\"email\":\"x@example.de\",\"passwort\":\"pw\"}",
                "{\"name\":\"X\",\"passwort\":\"pw\"}",
                "{\"name\":\"X\",\"email\":\"x@example.de\"}");
        for (String anfrage : unvollstaendig) {
            String fehler = mvc.perform(post("/api/auth/registrieren").with(csrf())
                            .contentType("application/json").content(anfrage))
                    .andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
            JsonNode fehlerAntwort = json.readTree(fehler);
            assertThat(fehlerAntwort.size()).isEqualTo(2);
            assertThat(fehlerAntwort.path("code").stringValue()).isEqualTo("UNGUELTIGE_EINGABE");
            assertThat(fehlerAntwort.path("message").stringValue()).isEqualTo("Bitte prüfen Sie Ihre Eingaben.");
        }
    }

    @Test
    void mutationenOhneGueltigesCsrfTokenWerdenAbgewiesen() throws Exception {
        mvc.perform(post("/api/auth/registrieren")
                        .contentType("application/json")
                        .content("{\"name\":\"X\",\"email\":\"x@example.de\",\"passwort\":\"pw\"}"))
                .andExpect(status().isForbidden());
        assertThat(konten.alle()).isEmpty();

        Benutzerkonto konto = konten.registrieren("X", "x@example.de", "pw");
        mvc.perform(patch("/api/ich/name").session(login(konto.email(), "pw"))
                        .header("If-Match", konto.aenderungsstand())
                        .contentType("application/json").content("{\"name\":\"Angriff\"}"))
                .andExpect(status().isForbidden());
        assertThat(konten.laden(konto.id()).name()).isEqualTo("X");
    }

    // verifies: TEST_USR_PWD_02
    @Test
    void administrativesPasswortsetzenBeendetAlleSitzungen() throws Exception {
        Benutzerkonto chef = konten.registrieren("Chef", "chef@example.de", "pw");
        Benutzerkonto ziel = konten.registrieren("Ziel", "ziel@example.de", "alt");
        MockHttpSession sitzung = login(ziel.email(), "alt");
        konten.passwortSetzen(chef.id(), ziel.id(), "neu", ziel.aenderungsstand());
        mvc.perform(get("/api/auth/ich").session(sitzung)).andExpect(status().isUnauthorized());
        assertThat(konten.anmelden(ziel.email(), "neu").id()).isEqualTo(ziel.id());
    }

    // verifies: TEST_USR_PWD_03
    @Test
    void fremderAdministratorKannEigentuemerpasswortNichtSetzen() {
        Benutzerkonto chef = konten.registrieren("Chef", "chef@example.de", "pw");
        Benutzerkonto ziel = konten.registrieren("Ziel", "ziel@example.de", "alt");
        konten.rolleErteilen(chef.id(), ziel.id(), Rolle.ADMINISTRATOR,
                konten.laden(ziel.id()).aenderungsstand());
        assertThatThrownBy(() -> konten.passwortSetzen(
                ziel.id(), chef.id(), "angriff", chef.aenderungsstand()))
                .isInstanceOfSatisfying(KontoFehler.class,
                        f -> assertThat(f.code()).isEqualTo("EIGENTUEMER_GESCHUETZT"));
        assertThat(konten.anmelden(chef.email(), "pw").id()).isEqualTo(chef.id());
    }

    // verifies: TEST_USR_LOGIN_06
    @Test
    void abmeldenBeendetDieSitzung() throws Exception {
        Benutzerkonto chef = konten.registrieren("Chef", "chef@example.de", "pw");
        MockHttpSession chefSitzung = login(chef.email(), "pw");
        mvc.perform(post("/api/auth/abmelden").session(chefSitzung).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/auth/ich").session(chefSitzung)).andExpect(status().isUnauthorized());
    }

    // verifies: TEST_USR_EIGT_04, TEST_USR_LOGIN_10, TEST_USR_ROLLE_01, TEST_USR_ROLLE_02, TEST_USR_ROLLE_03
    @Test
    void rollenGeltenInLaufenderSitzungUndSchutzregelnBleibenBestehen() throws Exception {
        Benutzerkonto eigentuemer = konten.registrieren("Chef", "chef@example.de", "pw");
        Benutzerkonto trainer = konten.registrieren("Trainer", "trainer@example.de", "pw");
        MockHttpSession trainerSitzung = login(trainer.email(), "pw");

        mvc.perform(get("/api/benutzerkonten").session(trainerSitzung)).andExpect(status().isForbidden());
        konten.rolleErteilen(eigentuemer.id(), trainer.id(), Rolle.ADMINISTRATOR,
                trainer.aenderungsstand());
        assertThat(konten.laden(trainer.id()).rollen())
                .containsExactlyInAnyOrder(Rolle.TRAINER, Rolle.ADMINISTRATOR);
        mvc.perform(get("/api/benutzerkonten").session(trainerSitzung)).andExpect(status().isOk());
        Benutzerkonto drittes = konten.registrieren("Drittes", "drittes@example.de", "pw");
        konten.rolleErteilen(eigentuemer.id(), drittes.id(), Rolle.ADMINISTRATOR,
                drittes.aenderungsstand());
        assertThatThrownBy(() -> konten.rolleEntziehen(trainer.id(), drittes.id(),
                Rolle.ADMINISTRATOR, konten.laden(drittes.id()).aenderungsstand()))
                .isInstanceOfSatisfying(KontoFehler.class,
                        f -> assertThat(f.code()).isEqualTo("NUR_EIGENTUEMER"));
        konten.rolleEntziehen(eigentuemer.id(), drittes.id(), Rolle.ADMINISTRATOR,
                konten.laden(drittes.id()).aenderungsstand());
        assertThat(konten.laden(drittes.id()).rollen()).containsExactly(Rolle.TRAINER);
        konten.rolleEntziehen(eigentuemer.id(), trainer.id(), Rolle.ADMINISTRATOR,
                konten.laden(trainer.id()).aenderungsstand());
        mvc.perform(get("/api/benutzerkonten").session(trainerSitzung)).andExpect(status().isForbidden());

        assertThatThrownBy(() -> konten.rolleEntziehen(eigentuemer.id(), eigentuemer.id(),
                Rolle.ADMINISTRATOR, konten.laden(eigentuemer.id()).aenderungsstand()))
                .isInstanceOfSatisfying(KontoFehler.class,
                        f -> assertThat(f.code()).isEqualTo("EIGENTUEMER_GESCHUETZT"));
        assertThatThrownBy(() -> konten.rolleEntziehen(eigentuemer.id(), trainer.id(),
                Rolle.TRAINER, konten.laden(trainer.id()).aenderungsstand()))
                .isInstanceOfSatisfying(KontoFehler.class,
                        f -> assertThat(f.code()).isEqualTo("LETZTE_ROLLE"));
    }

    // verifies: TEST_USR_EIGT_03
    @Test
    void eigentuemerWirdAtomarAnAktivesKontoUebergeben() throws Exception {
        Benutzerkonto alt = konten.registrieren("Alt", "alt@example.de", "pw");
        Benutzerkonto neu = konten.registrieren("Neu", "neu@example.de", "pw");
        konten.eigentuemerUebergeben(alt.id(), neu.id(), neu.aenderungsstand());

        assertThat(konten.laden(alt.id()).rollen()).contains(Rolle.ADMINISTRATOR).doesNotContain(Rolle.EIGENTUEMER);
        assertThat(konten.laden(neu.id()).rollen()).contains(Rolle.TRAINER, Rolle.ADMINISTRATOR, Rolle.EIGENTUEMER);
        Benutzerkonto eins = konten.registrieren("Eins", "eins@example.de", "pw");
        Benutzerkonto zwei = konten.registrieren("Zwei", "zwei@example.de", "pw");
        try (var pool = Executors.newFixedThreadPool(2)) {
            var start = new CyclicBarrier(2);
            List<Callable<Boolean>> uebergaben = List.of(
                    () -> { start.await(); konten.eigentuemerUebergeben(
                            neu.id(), eins.id(), eins.aenderungsstand()); return true; },
                    () -> { start.await(); konten.eigentuemerUebergeben(
                            neu.id(), zwei.id(), zwei.aenderungsstand()); return true; });
            var ergebnisse = pool.invokeAll(uebergaben);
            long erfolgreich = ergebnisse.stream().filter(f -> {
                try { return f.get(); } catch (Exception ignored) { return false; }
            }).count();
            assertThat(erfolgreich).isOne();
            assertThat(konten.alle()).filteredOn(k -> k.rollen().contains(Rolle.EIGENTUEMER)).hasSize(1);
        }
    }

    // verifies: TEST_USR_EIGT_07
    @Test
    void eigentuemerrolleGehtNichtAnStillgelegtesKonto() {
        Benutzerkonto alt = konten.registrieren("Alt", "alt@example.de", "pw");
        Benutzerkonto ziel = konten.registrieren("Ziel", "ziel@example.de", "pw");
        konten.stilllegen(alt.id(), ziel.id(), ziel.aenderungsstand());

        assertThatThrownBy(() -> konten.eigentuemerUebergeben(
                alt.id(), ziel.id(), konten.laden(ziel.id()).aenderungsstand()))
                .isInstanceOfSatisfying(KontoFehler.class,
                        f -> assertThat(f.code()).isEqualTo("KONTO_STILLGELEGT"));
        assertThat(konten.laden(alt.id()).rollen()).containsExactlyInAnyOrder(
                Rolle.TRAINER, Rolle.ADMINISTRATOR, Rolle.EIGENTUEMER);
        assertThat(konten.laden(ziel.id()).rollen()).containsExactly(Rolle.TRAINER);
    }

    // verifies: TEST_USR_EIGT_05
    @Test
    void eigentuemerIstBisZurUebergabeVorStilllegungUndLoeschungGeschuetzt() {
        Benutzerkonto alt = konten.registrieren("Alt", "alt@example.de", "pw");
        Benutzerkonto neu = konten.registrieren("Neu", "neu@example.de", "pw");
        assertThatThrownBy(() -> konten.stilllegen(alt.id(), alt.id(), alt.aenderungsstand()))
                .isInstanceOfSatisfying(KontoFehler.class,
                        f -> assertThat(f.code()).isEqualTo("EIGENTUEMER_GESCHUETZT"));
        assertThatThrownBy(() -> konten.loeschen(alt.id(), alt.id(), alt.aenderungsstand()))
                .isInstanceOfSatisfying(KontoFehler.class,
                        f -> assertThat(f.code()).isEqualTo("EIGENTUEMER_GESCHUETZT"));

        konten.eigentuemerUebergeben(alt.id(), neu.id(), neu.aenderungsstand());
        konten.stilllegen(neu.id(), alt.id(), konten.laden(alt.id()).aenderungsstand());
        konten.loeschen(neu.id(), alt.id(), konten.laden(alt.id()).aenderungsstand());
        assertThat(konten.finden(alt.id())).isEmpty();
    }

    // verifies: TEST_USR_LOGIN_04
    @Test
    void wiederholteFehlversucheSperrenDasKontoNicht() {
        Benutzerkonto konto = konten.registrieren("Eins", "eins@example.de", "richtig");
        for (int versuch = 0; versuch < 5; versuch++) {
            assertThatThrownBy(() -> konten.anmelden(konto.email(), "falsch"))
                    .isInstanceOf(KontoFehler.class);
        }
        assertThat(konten.anmelden(konto.email(), "richtig").id()).isEqualTo(konto.id());
    }

    // verifies: TEST_USR_LOGIN_09
    @Test
    void stilllegenUndLoeschenEntziehenLaufendenSitzungenDenZugriff() throws Exception {
        Benutzerkonto chef = konten.registrieren("Chef", "chef@example.de", "pw");
        Benutzerkonto stillgelegt = konten.registrieren("Still", "still@example.de", "pw");
        Benutzerkonto geloescht = konten.registrieren("Weg", "weg@example.de", "pw");
        MockHttpSession stillSitzung = login(stillgelegt.email(), "pw");
        MockHttpSession loeschSitzung = login(geloescht.email(), "pw");

        konten.stilllegen(chef.id(), stillgelegt.id(), stillgelegt.aenderungsstand());
        konten.loeschen(chef.id(), geloescht.id(), geloescht.aenderungsstand());
        mvc.perform(get("/api/auth/ich").session(stillSitzung)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/ich").session(loeschSitzung)).andExpect(status().isUnauthorized());
    }

    // verifies: TEST_USR_SICHER_01
    @Test
    void standardbetriebBindetLoopbackUndDeaktiviertDieH2Konsole() throws Exception {
        var properties = new YamlPropertySourceLoader().load(
                "application", new FileSystemResource("src/main/resources/application.yml"));

        assertThat(properties).singleElement().satisfies(source -> {
            assertThat(source.getProperty("server.address")).isEqualTo("127.0.0.1");
            assertThat(source.getProperty("spring.h2.console.enabled")).isEqualTo(false);
        });
    }

    // verifies: TEST_USR_ROLLE_05, TEST_USR_ROLLE_08
    @Test
    void reinesAdministratorkontoBehaeltDatenIstAberNichtMehrZuweisbar() {
        Benutzerkonto chef = konten.registrieren("Chef", "chef@example.de", "pw");
        Benutzerkonto ziel = konten.registrieren("Ziel", "ziel@example.de", "pw");
        konten.rolleErteilen(chef.id(), ziel.id(), Rolle.ADMINISTRATOR,
                ziel.aenderungsstand());
        jdbc.update("INSERT INTO trainer_qualifikation VALUES (?, 'SCH-001')", ziel.id());
        jdbc.update("INSERT INTO abwesenheit (benutzerkonto_id, von, bis) VALUES (?, DATE '2099-02-01', DATE '2099-02-02')", ziel.id());
        termin("ROLLEN", "2099-02-03", "geplant", null);
        einsaetze.aufQualifikationBewerben(ziel.id(), "SCH-002");
        einsaetze.aufAssistenzplatzBewerben(ziel.id(), "ROLLEN");
        assertThat(schulungen.findeVerfuegbareTrainer(
                "SCH-001", LocalDate.of(2099, 2, 3), LocalDate.of(2099, 2, 3)))
                .extracting("id").contains(ziel.id());
        konten.rolleEntziehen(chef.id(), ziel.id(), Rolle.TRAINER,
                konten.laden(ziel.id()).aenderungsstand());

        assertThat(konten.laden(ziel.id()).rollen()).containsExactly(Rolle.ADMINISTRATOR);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM trainer_qualifikation WHERE benutzerkonto_id=?", Integer.class, ziel.id())).isOne();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM abwesenheit WHERE benutzerkonto_id=?", Integer.class, ziel.id())).isOne();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qualifikationsbewerbung WHERE benutzerkonto_id=?", Integer.class, ziel.id())).isOne();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM assistenzbewerbung WHERE benutzerkonto_id=?", Integer.class, ziel.id())).isOne();
        assertThat(schulungen.findeVerfuegbareTrainer(
                "SCH-001", LocalDate.of(2099, 2, 3), LocalDate.of(2099, 2, 3)))
                .extracting("id").doesNotContain(ziel.id());
        assertThatThrownBy(() -> einsaetze.trainerZuweisen(chef.id(), "ROLLEN", ziel.id()))
                .isInstanceOfSatisfying(KontoFehler.class,
                        f -> assertThat(f.code()).isEqualTo("TRAINER_ERFORDERLICH"));
        assertThatThrownBy(() -> einsaetze.assistentZuweisen(chef.id(), "ROLLEN", ziel.id()))
                .isInstanceOfSatisfying(KontoFehler.class,
                        f -> assertThat(f.code()).isEqualTo("TRAINER_ERFORDERLICH"));

        konten.rolleErteilen(chef.id(), ziel.id(), Rolle.TRAINER,
                konten.laden(ziel.id()).aenderungsstand());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qualifikationsbewerbung WHERE benutzerkonto_id=?", Integer.class, ziel.id())).isOne();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM assistenzbewerbung WHERE benutzerkonto_id=?", Integer.class, ziel.id())).isOne();
        assertThat(schulungen.findeVerfuegbareTrainer(
                "SCH-001", LocalDate.of(2099, 2, 3), LocalDate.of(2099, 2, 3)))
                .extracting("id").contains(ziel.id());
        assertThat(schulungen.findeVerfuegbareTrainer(
                "SCH-001", LocalDate.of(2099, 2, 1), LocalDate.of(2099, 2, 2)))
                .extracting("id").doesNotContain(ziel.id());
        termin("ROLLEN-ASSISTENZ", "2099-02-04", "geplant", null);
        einsaetze.trainerZuweisen(chef.id(), "ROLLEN", ziel.id());
        einsaetze.assistentZuweisen(chef.id(), "ROLLEN-ASSISTENZ", ziel.id());
        assertThat(jdbc.queryForObject("SELECT trainer_id FROM termin WHERE termin_id='ROLLEN'", String.class))
                .isEqualTo(ziel.id());
        assertThat(jdbc.queryForObject("SELECT benutzerkonto_id FROM termin_assistent WHERE termin_id='ROLLEN-ASSISTENZ'", String.class))
                .isEqualTo(ziel.id());
    }

    // verifies: TEST_USR_REG_03
    @Test
    void neuesKontoKannSichBewerbenAberOhneQualifikationNichtAlsTrainerEingesetztWerden() {
        Benutzerkonto chef = konten.registrieren("Chef", "chef@example.de", "pw");
        Benutzerkonto neu = konten.registrieren("Neu", "neu@example.de", "pw");
        termin("BEWERBUNG", "2099-03-01", "geplant", null);

        einsaetze.aufQualifikationBewerben(neu.id(), "SCH-001");
        einsaetze.aufAssistenzplatzBewerben(neu.id(), "BEWERBUNG");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qualifikationsbewerbung WHERE benutzerkonto_id=?", Integer.class, neu.id())).isOne();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM assistenzbewerbung WHERE benutzerkonto_id=?", Integer.class, neu.id())).isOne();
        assertThatThrownBy(() -> einsaetze.trainerZuweisen(chef.id(), "BEWERBUNG", neu.id()))
                .isInstanceOfSatisfying(KontoFehler.class,
                        f -> assertThat(f.code()).isEqualTo("QUALIFIKATION_ERFORDERLICH"));
        assertThat(jdbc.queryForObject("SELECT trainer_id FROM termin WHERE termin_id='BEWERBUNG'", String.class))
                .isNull();
    }

    // verifies: TEST_USR_ROLLE_07
    @Test
    void administratorMitTrainerrolleKannAlleTrainerfunktionenNutzen() throws Exception {
        Benutzerkonto chef = konten.registrieren("Chef", "chef@example.de", "pw");
        einsaetze.aufQualifikationBewerben(chef.id(), "SCH-002");
        jdbc.update("INSERT INTO trainer_qualifikation VALUES (?, 'SCH-001')", chef.id());
        mvc.perform(post("/api/ich/abwesenheiten").session(login(chef.email(), "pw")).with(csrf())
                        .contentType("application/json")
                        .content("{\"von\":\"2099-01-01\",\"bis\":\"2099-01-02\",\"grund\":\"Verhindert\"}"))
                .andExpect(status().isNoContent());
        termin("ADMIN-TRAINER", "2099-03-01", "geplant", null);
        einsaetze.trainerZuweisen(chef.id(), "ADMIN-TRAINER", chef.id());

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qualifikationsbewerbung WHERE benutzerkonto_id=?", Integer.class, chef.id())).isOne();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM abwesenheit WHERE benutzerkonto_id=?", Integer.class, chef.id())).isOne();
        assertThat(jdbc.queryForObject("SELECT trainer_id FROM termin WHERE termin_id='ADMIN-TRAINER'", String.class))
                .isEqualTo(chef.id());
    }

    // verifies: TEST_USR_ROLLE_09
    @Test
    void reinerTrainerKannKeineFremdenKontenVerwalten() throws Exception {
        Benutzerkonto chef = konten.registrieren("Chef", "chef@example.de", "pw");
        Benutzerkonto trainer = konten.registrieren("Trainer", "trainer@example.de", "pw");
        MockHttpSession sitzung = login(trainer.email(), "pw");
        String basis = "/api/benutzerkonten/" + chef.id();

        mvc.perform(put(basis + "/rollen/ADMINISTRATOR").session(sitzung).with(csrf())
                        .header("If-Match", chef.aenderungsstand())).andExpect(status().isForbidden());
        mvc.perform(delete(basis + "/rollen/ADMINISTRATOR").session(sitzung).with(csrf())
                        .header("If-Match", chef.aenderungsstand())).andExpect(status().isForbidden());
        mvc.perform(put(basis + "/passwort").session(sitzung).with(csrf())
                        .header("If-Match", chef.aenderungsstand()).contentType("application/json")
                        .content("{\"passwort\":\"neu\"}")).andExpect(status().isForbidden());
        for (String aktion : List.of("stilllegen", "reaktivieren", "eigentuemer")) {
            mvc.perform(post(basis + "/" + aktion).session(sitzung).with(csrf())
                            .header("If-Match", chef.aenderungsstand())).andExpect(status().isForbidden());
        }
        mvc.perform(delete(basis).session(sitzung).with(csrf())
                        .header("If-Match", chef.aenderungsstand())).andExpect(status().isForbidden());
        mvc.perform(patch(basis + "/name").session(sitzung).with(csrf())
                        .header("If-Match", chef.aenderungsstand()).contentType("application/json")
                        .content("{\"name\":\"Angriff\"}")).andExpect(status().isForbidden());
        assertThat(konten.laden(chef.id()).name()).isEqualTo("Chef");
    }

    // verifies: TEST_USR_ROLLE_04
    @Test
    void trainerrollenentzugEntferntNurZukuenftigeZuweisungen() {
        Benutzerkonto chef = konten.registrieren("Chef", "chef@example.de", "pw");
        Benutzerkonto ziel = konten.registrieren("Ziel", "ziel@example.de", "pw");
        konten.rolleErteilen(chef.id(), ziel.id(), Rolle.ADMINISTRATOR, ziel.aenderungsstand());
        jdbc.update("INSERT INTO trainer_qualifikation VALUES (?, 'SCH-001')", ziel.id());
        termin("ZUKUNFT-TRAINER", "2099-01-01", "geplant", null);
        termin("ZUKUNFT-ASSISTENZ", "2099-01-02", "geplant", null);
        termin("HISTORIE-TRAINER", "2020-01-01", "abgeschlossen", null);
        termin("HISTORIE-ASSISTENZ", "2020-01-02", "abgeschlossen", null);
        einsaetze.trainerZuweisen(chef.id(), "ZUKUNFT-TRAINER", ziel.id());
        einsaetze.assistentZuweisen(chef.id(), "ZUKUNFT-ASSISTENZ", ziel.id());
        jdbc.update("UPDATE termin SET trainer_id=? WHERE termin_id='HISTORIE-TRAINER'", ziel.id());
        jdbc.update("INSERT INTO termin_assistent (termin_id, benutzerkonto_id, platz) VALUES ('HISTORIE-ASSISTENZ', ?, 1)", ziel.id());

        konten.rolleEntziehen(chef.id(), ziel.id(), Rolle.TRAINER,
                konten.laden(ziel.id()).aenderungsstand());
        assertThat(jdbc.queryForObject("SELECT trainer_id FROM termin WHERE termin_id='ZUKUNFT-TRAINER'", String.class)).isNull();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM termin_assistent WHERE termin_id='ZUKUNFT-ASSISTENZ'", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT trainer_id FROM termin WHERE termin_id='HISTORIE-TRAINER'", String.class)).isEqualTo(ziel.id());
        assertThat(jdbc.queryForObject("SELECT benutzerkonto_id FROM termin_assistent WHERE termin_id='HISTORIE-ASSISTENZ'", String.class)).isEqualTo(ziel.id());
    }

    // verifies: TEST_USR_ENDE_01, TEST_USR_ENDE_02, TEST_USR_ENDE_06, TEST_USR_LOGIN_05
    @Test
    void stilllegenUndLoeschenBereinigenZuweisungenUndErhaltenHistorie() throws Exception {
        Benutzerkonto chef = konten.registrieren("Chef", "chef@example.de", "pw");
        Benutzerkonto ziel = konten.registrieren("Ziel", "ziel@example.de", "pw");
        jdbc.update("INSERT INTO trainer_qualifikation VALUES (?, 'SCH-001')", ziel.id());
        jdbc.update("INSERT INTO abwesenheit (benutzerkonto_id, von, bis) VALUES (?, DATE '2099-01-01', DATE '2099-01-02')", ziel.id());
        termin("ZUKUNFT", "2099-01-01", "geplant", ziel.id());
        termin("HISTORIE", "2020-01-01", "abgeschlossen", ziel.id());
        jdbc.update("INSERT INTO termin_assistent (termin_id, benutzerkonto_id, platz) VALUES ('ZUKUNFT', ?, 1)", ziel.id());
        jdbc.update("INSERT INTO termin_assistent (termin_id, benutzerkonto_id, platz) VALUES ('HISTORIE', ?, 1)", ziel.id());
        einsaetze.aufQualifikationBewerben(ziel.id(), "SCH-002");
        einsaetze.aufAssistenzplatzBewerben(ziel.id(), "ZUKUNFT");
        jdbc.update("INSERT INTO vormerkung (termin_id, benutzerkonto_id) VALUES ('ZUKUNFT', ?)", ziel.id());
        jdbc.update("INSERT INTO benachrichtigung (empfaenger_id, anlass) VALUES (?, 'Test')", ziel.id());

        konten.stilllegen(chef.id(), ziel.id(), ziel.aenderungsstand());
        assertThat(jdbc.queryForObject("SELECT trainer_id FROM termin WHERE termin_id='ZUKUNFT'", String.class)).isNull();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM termin_assistent WHERE termin_id='ZUKUNFT'", Integer.class)).isZero();
        assertThatThrownBy(() -> konten.anmelden(ziel.email(), "pw"))
                .isInstanceOfSatisfying(KontoFehler.class,
                        f -> assertThat(f.code()).isEqualTo("KONTO_STILLGELEGT"));
        assertThat(konten.laden(ziel.id()).name()).isEqualTo("Ziel");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM trainer_qualifikation WHERE benutzerkonto_id=?", Integer.class, ziel.id())).isOne();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qualifikationsbewerbung WHERE benutzerkonto_id=?", Integer.class, ziel.id())).isOne();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM assistenzbewerbung WHERE benutzerkonto_id=?", Integer.class, ziel.id())).isOne();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM abwesenheit WHERE benutzerkonto_id=?", Integer.class, ziel.id())).isOne();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM vormerkung WHERE benutzerkonto_id=?", Integer.class, ziel.id())).isOne();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM benachrichtigung WHERE empfaenger_id=?", Integer.class, ziel.id())).isOne();
        konten.reaktivieren(chef.id(), ziel.id(),
                konten.laden(ziel.id()).aenderungsstand());
        assertThat(jdbc.queryForObject("SELECT trainer_id FROM termin WHERE termin_id='ZUKUNFT'", String.class)).isNull();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM termin_assistent WHERE termin_id='ZUKUNFT'", Integer.class)).isZero();
        assertThat(konten.anmelden(ziel.email(), "pw").id()).isEqualTo(ziel.id());

        konten.loeschen(chef.id(), ziel.id(),
                konten.laden(ziel.id()).aenderungsstand());
        assertThat(konten.finden(ziel.id())).isEmpty();
        assertThat(jdbc.queryForObject("SELECT trainer_name_snapshot FROM termin WHERE termin_id='HISTORIE'", String.class)).isEqualTo("Ziel");
        assertThat(jdbc.queryForObject("SELECT name_snapshot FROM termin_assistent WHERE termin_id='HISTORIE'", String.class)).isEqualTo("Ziel");
        assertThat(katalog.findeSchulungen(null, null).stream()
                .flatMap(s -> s.oeffentlicheTermine().stream())
                .filter(t -> t.terminId().equals("HISTORIE")).findFirst().orElseThrow().trainerName())
                .isEqualTo("Ziel");
    }

    // verifies: TEST_USR_ENDE_03, TEST_USR_ENDE_04, TEST_USR_ENDE_05
    @Test
    void loeschenEntferntVorgaengeGibtZukunftFreiUndErhaeltHistorischeNamen() {
        Benutzerkonto chef = konten.registrieren("Chef", "chef@example.de", "pw");
        Benutzerkonto ziel = konten.registrieren("Ziel", "ziel@example.de", "pw");
        jdbc.update("INSERT INTO trainer_qualifikation VALUES (?, 'SCH-001')", ziel.id());
        jdbc.update("INSERT INTO abwesenheit (benutzerkonto_id, von, bis) VALUES (?, DATE '2099-01-01', DATE '2099-01-02')", ziel.id());
        termin("LOESCH-ZUKUNFT-TRAINER", "2099-01-01", "geplant", ziel.id());
        termin("LOESCH-ZUKUNFT-ASSISTENZ", "2099-01-02", "geplant", null);
        termin("LOESCH-HISTORIE-TRAINER", "2020-01-01", "abgeschlossen", ziel.id());
        termin("LOESCH-HISTORIE-ASSISTENZ", "2020-01-02", "abgeschlossen", null);
        jdbc.update("INSERT INTO termin_assistent (termin_id, benutzerkonto_id, platz) VALUES ('LOESCH-ZUKUNFT-ASSISTENZ', ?, 1)", ziel.id());
        jdbc.update("INSERT INTO termin_assistent (termin_id, benutzerkonto_id, platz) VALUES ('LOESCH-HISTORIE-ASSISTENZ', ?, 1)", ziel.id());
        einsaetze.aufQualifikationBewerben(ziel.id(), "SCH-002");
        einsaetze.aufAssistenzplatzBewerben(ziel.id(), "LOESCH-ZUKUNFT-ASSISTENZ");
        jdbc.update("INSERT INTO vormerkung (termin_id, benutzerkonto_id) VALUES ('LOESCH-ZUKUNFT-TRAINER', ?)", ziel.id());
        jdbc.update("INSERT INTO benachrichtigung (empfaenger_id, anlass) VALUES (?, 'Test')", ziel.id());

        konten.loeschen(chef.id(), ziel.id(), ziel.aenderungsstand());

        assertThat(konten.finden(ziel.id())).isEmpty();
        for (String tabelle : List.of("trainer_qualifikation", "qualifikationsbewerbung",
                "assistenzbewerbung", "abwesenheit", "vormerkung")) {
            assertThat(jdbc.queryForObject(
                    "SELECT COUNT(*) FROM " + tabelle + " WHERE benutzerkonto_id = ?",
                    Integer.class, ziel.id())).isZero();
        }
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM benachrichtigung WHERE empfaenger_id = ?",
                Integer.class, ziel.id())).isZero();
        assertThat(jdbc.queryForObject("SELECT trainer_id FROM termin WHERE termin_id='LOESCH-ZUKUNFT-TRAINER'", String.class)).isNull();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM termin_assistent WHERE termin_id='LOESCH-ZUKUNFT-ASSISTENZ'", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT trainer_id FROM termin WHERE termin_id='LOESCH-HISTORIE-TRAINER'", String.class)).isNull();
        assertThat(jdbc.queryForObject("SELECT trainer_name_snapshot FROM termin WHERE termin_id='LOESCH-HISTORIE-TRAINER'", String.class)).isEqualTo("Ziel");
        assertThat(jdbc.queryForObject("SELECT benutzerkonto_id FROM termin_assistent WHERE termin_id='LOESCH-HISTORIE-ASSISTENZ'", String.class)).isNull();
        assertThat(jdbc.queryForObject("SELECT name_snapshot FROM termin_assistent WHERE termin_id='LOESCH-HISTORIE-ASSISTENZ'", String.class)).isEqualTo("Ziel");
        assertThat(katalog.findeSchulungen(null, null).stream()
                .flatMap(s -> s.oeffentlicheTermine().stream())
                .filter(t -> t.terminId().equals("LOESCH-HISTORIE-ASSISTENZ"))
                .findFirst().orElseThrow().assistenten()).containsExactly("Ziel");
    }

    // verifies: TEST_USR_PROF_02
    @Test
    void nameIstAenderbarAberEmailNichtTeilDerAenderung() {
        Benutzerkonto konto = konten.registrieren("Eins", "eins@example.de", "pw");
        Benutzerkonto geaendert = konten.nameAendern(
                konto.id(), "Gleicher Name", konto.aenderungsstand());
        Benutzerkonto zweites = konten.registrieren("Gleicher Name", "zwei@example.de", "pw");
        assertThat(geaendert.name()).isEqualTo(zweites.name());
        assertThat(geaendert.email()).isEqualTo("eins@example.de");
    }

    // verifies: TEST_USR_PROF_01
    @Test
    void emailadresseKannNichtUeberDasProfilGeaendertWerden() throws Exception {
        Benutzerkonto konto = konten.registrieren("Eins", "eins@example.de", "pw");
        MockHttpSession sitzung = login(konto.email(), "pw");

        mvc.perform(patch("/api/ich/name").session(sitzung).with(csrf())
                        .header("If-Match", konto.aenderungsstand())
                        .contentType("application/json")
                        .content("{\"name\":\"Neu\",\"email\":\"neu@example.de\"}"))
                .andExpect(status().isBadRequest());
        assertThat(konten.laden(konto.id()).email()).isEqualTo("eins@example.de");
    }

    // verifies: TEST_USR_PROF_03
    @Test
    void kontoFuehrtNurIdentitaetSicherheitRollenUndZustandAlsEigeneFelder() {
        Benutzerkonto eins = konten.registrieren("Eins", "eins@example.de", "gleich");
        Benutzerkonto zwei = konten.registrieren("Zwei", "zwei@example.de", "gleich");
        assertThat(eins.email()).isEqualTo("eins@example.de");
        assertThat(eins.name()).isEqualTo("Eins");
        assertThat(eins.passwortHash()).startsWith("$2").isNotEqualTo(zwei.passwortHash());
        assertThat(eins.rollen()).contains(Rolle.TRAINER, Rolle.ADMINISTRATOR, Rolle.EIGENTUEMER);
        assertThat(eins.zustand()).isEqualTo(Benutzerkonto.Zustand.AKTIV);
        assertThat(java.util.Arrays.stream(Benutzerkonto.class.getRecordComponents())
                .map(java.lang.reflect.RecordComponent::getName).toList())
                .doesNotContain("qualifikationen", "abwesenheiten", "zuweisungen");
    }

    // verifies: TEST_USR_PROF_04
    @Test
    void kontoKenntNurAktivStillgelegtOderNichtMehrVorhanden() {
        Benutzerkonto chef = konten.registrieren("Chef", "chef@example.de", "pw");
        Benutzerkonto ziel = konten.registrieren("Ziel", "ziel@example.de", "pw");
        assertThat(ziel.zustand()).isEqualTo(Benutzerkonto.Zustand.AKTIV);
        konten.stilllegen(chef.id(), ziel.id(), ziel.aenderungsstand());
        assertThat(konten.laden(ziel.id()).zustand()).isEqualTo(Benutzerkonto.Zustand.STILLGELEGT);
        konten.reaktivieren(chef.id(), ziel.id(), konten.laden(ziel.id()).aenderungsstand());
        assertThat(konten.laden(ziel.id()).zustand()).isEqualTo(Benutzerkonto.Zustand.AKTIV);
        konten.loeschen(chef.id(), ziel.id(), konten.laden(ziel.id()).aenderungsstand());
        assertThat(konten.finden(ziel.id())).isEmpty();
        assertThat(Benutzerkonto.Zustand.values()).containsExactly(
                Benutzerkonto.Zustand.AKTIV, Benutzerkonto.Zustand.STILLGELEGT);
    }

    @Test
    void veralteterAenderungsstandWirdAbgewiesen() {
        Benutzerkonto konto = konten.registrieren("Eins", "eins@example.de", "pw");
        konten.nameAendern(konto.id(), "Erste Änderung", konto.aenderungsstand());

        assertThatThrownBy(() -> konten.nameAendern(
                konto.id(), "Veraltete Änderung", konto.aenderungsstand()))
                .isInstanceOfSatisfying(KontoFehler.class, fehler -> {
                    assertThat(fehler.status()).isEqualTo(org.springframework.http.HttpStatus.CONFLICT);
                    assertThat(fehler.code()).isEqualTo("ZWISCHENZEITLICH_GEAENDERT");
                });
        assertThat(konten.laden(konto.id()).name()).isEqualTo("Erste Änderung");
    }

    // verifies: TEST_USR_GRUND_01
    @Test
    void kontobezogeneZuweisungenFolgenDenGrundregeln() {
        Benutzerkonto chef = konten.registrieren("Chef", "chef@example.de", "pw");
        Benutzerkonto trainer = konten.registrieren("Trainer", "trainer@example.de", "pw");
        Benutzerkonto ersatz = konten.registrieren("Ersatz", "ersatz@example.de", "pw");
        jdbc.update("INSERT INTO trainer_qualifikation VALUES (?, 'SCH-001')", trainer.id());
        jdbc.update("INSERT INTO trainer_qualifikation VALUES (?, 'SCH-001')", ersatz.id());

        termin("QUALIFIKATION-A", HEUTE.plusDays(10).toString(), "geplant", null);
        termin("QUALIFIKATION-B", HEUTE.plusDays(11).toString(), "geplant", null);
        einsaetze.trainerZuweisen(chef.id(), "QUALIFIKATION-A", trainer.id());
        einsaetze.trainerZuweisen(chef.id(), "QUALIFIKATION-B", trainer.id());
        einsaetze.trainerZuweisen(chef.id(), "QUALIFIKATION-A", ersatz.id());
        assertThat(jdbc.queryForObject(
                "SELECT trainer_id FROM termin WHERE termin_id='QUALIFIKATION-A'", String.class))
                .isEqualTo(ersatz.id());
        assertThat(jdbc.queryForObject(
                "SELECT trainer_id FROM termin WHERE termin_id='QUALIFIKATION-B'", String.class))
                .isEqualTo(trainer.id());

        termin("DREI-PLAETZE", HEUTE.plusDays(12).toString(), "geplant", null);
        for (int platz = 1; platz <= 3; platz++) {
            Benutzerkonto konto = konten.registrieren(
                    "Assistenz " + platz, "assistenz" + platz + "@example.de", "pw");
            einsaetze.assistentZuweisen(chef.id(), "DREI-PLAETZE", konto.id());
        }
        Benutzerkonto viertes = konten.registrieren("Assistenz 4", "assistenz4@example.de", "pw");

        assertThatThrownBy(() -> einsaetze.assistentZuweisen(chef.id(), "DREI-PLAETZE", viertes.id()))
                .isInstanceOfSatisfying(KontoFehler.class,
                        f -> assertThat(f.code()).isEqualTo("KEIN_ASSISTENZPLATZ"));
        assertThat(jdbc.queryForList(
                "SELECT platz FROM termin_assistent WHERE termin_id='DREI-PLAETZE' ORDER BY platz",
                Integer.class)).containsExactly(1, 2, 3);

        termin("VERGANGEN-GEPLANT", HEUTE.minusDays(1).toString(),
                "geplant", trainer.id());
        termin("HEUTE-GEPLANT", HEUTE.toString(),
                "geplant", trainer.id());
        termin("ZUKUNFT-ABGESCHLOSSEN", HEUTE.plusDays(1).toString(),
                "abgeschlossen", trainer.id());
        konten.rolleErteilen(chef.id(), trainer.id(), Rolle.ADMINISTRATOR,
                konten.laden(trainer.id()).aenderungsstand());
        konten.rolleEntziehen(chef.id(), trainer.id(), Rolle.TRAINER,
                konten.laden(trainer.id()).aenderungsstand());

        assertThat(jdbc.queryForObject(
                "SELECT trainer_id FROM termin WHERE termin_id='VERGANGEN-GEPLANT'", String.class))
                .isEqualTo(trainer.id());
        assertThat(jdbc.queryForObject(
                "SELECT trainer_id FROM termin WHERE termin_id='HEUTE-GEPLANT'", String.class))
                .isNull();
        assertThat(jdbc.queryForObject(
                "SELECT trainer_id FROM termin WHERE termin_id='ZUKUNFT-ABGESCHLOSSEN'", String.class))
                .isEqualTo(trainer.id());
    }

    private MockHttpSession login(String email, String passwort) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/auth/anmelden").with(csrf())
                        .contentType("application/json")
                        .content(json.writeValueAsString(java.util.Map.of("email", email, "passwort", passwort))))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }

    private String loginFehler(String email, String passwort) throws Exception {
        return mvc.perform(post("/api/auth/anmelden").with(csrf())
                        .contentType("application/json")
                        .content(json.writeValueAsString(java.util.Map.of("email", email, "passwort", passwort))))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
    }

    private void termin(String id, String datum, String status, String trainerId) {
        jdbc.update("""
                INSERT INTO termin
                (termin_id, schulung_id, startdatum, enddatum, ort, status, trainer_id)
                VALUES (?, 'SCH-001', ?, ?, 'Köln', ?, ?)
                """, id, java.time.LocalDate.parse(datum), java.time.LocalDate.parse(datum), status, trainerId);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class FixedClockConfig {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(HEUTE.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
        }
    }
}
