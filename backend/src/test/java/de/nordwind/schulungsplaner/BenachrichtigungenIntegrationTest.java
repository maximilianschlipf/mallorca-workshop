package de.nordwind.schulungsplaner;

import de.nordwind.schulungsplaner.domain.Benutzerkonto;
import de.nordwind.schulungsplaner.domain.Rolle;
import de.nordwind.schulungsplaner.service.BenachrichtigungService;
import de.nordwind.schulungsplaner.service.Benachrichtigungsanlass;
import de.nordwind.schulungsplaner.service.KontoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.demo-seed=false",
        "spring.datasource.url=jdbc:h2:mem:benachrichtigungentest;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"
})
@AutoConfigureMockMvc
@Transactional
class BenachrichtigungenIntegrationTest {
    @Autowired BenachrichtigungService benachrichtigungen;
    @Autowired KontoService konten;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;

    Benutzerkonto admin;
    Benutzerkonto trainer;
    Benutzerkonto anderer;

    @BeforeEach
    void vorbereiten() {
        jdbc.update("DELETE FROM benachrichtigung");
        jdbc.update("UPDATE instanz SET eigentuemer_id=NULL WHERE id=1");
        jdbc.update("DELETE FROM benutzerkonto_rolle");
        jdbc.update("DELETE FROM benutzerkonto");
        admin = konten.registrieren("Admin", "admin@example.de", "pw");
        trainer = konten.registrieren("Trainer", "trainer@example.de", "pw");
        anderer = konten.registrieren("Anderer", "anderer@example.de", "pw");
    }

    // verifies: TEST_NAC_ANZ_03
    @Test
    void anzeigenMarkiertNurEigeneMitteilungenEndgueltigAlsGelesen() throws Exception {
        long eigene = persoenlich(trainer, "Eigene");
        long fremde = persoenlich(anderer, "Fremde");

        assertThat(benachrichtigungen.anzeigen(trainer.id())).singleElement()
                .satisfies(n -> assertThat(n.gelesen()).isFalse());
        assertThat(gelesen(eigene)).isTrue();
        assertThat(gelesen(fremde)).isFalse();
        mvc.perform(put("/api/ich/benachrichtigungen/{id}/ungelesen", eigene)
                        .session(login(trainer)).with(csrf()))
                .andExpect(status().is4xxClientError());
        assertThat(gelesen(eigene)).isTrue();
    }

    // verifies: TEST_NAC_ANZ_04
    @Test
    void allesAlsGelesenVeraendertNurUngelesene() {
        LocalDateTime alt = LocalDateTime.of(2025, 1, 2, 3, 4);
        long gelesenEins = persoenlich(trainer, "Gelesen 1");
        long gelesenZwei = persoenlich(trainer, "Gelesen 2");
        jdbc.update("UPDATE benachrichtigung SET gelesen=TRUE, gelesen_am=? WHERE id IN (?,?)",
                alt, gelesenEins, gelesenZwei);
        persoenlich(trainer, "Ungelesen 1");
        persoenlich(trainer, "Ungelesen 2");
        persoenlich(trainer, "Ungelesen 3");

        benachrichtigungen.allesAlsGelesen(trainer.id());

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM benachrichtigung WHERE empfaenger_id=? AND gelesen=TRUE",
                Integer.class, trainer.id())).isEqualTo(5);
        assertThat(jdbc.queryForObject("SELECT gelesen_am FROM benachrichtigung WHERE id=?",
                Timestamp.class, gelesenEins).toLocalDateTime()).isEqualTo(alt);
        assertThat(jdbc.queryForObject("SELECT gelesen_am FROM benachrichtigung WHERE id=?",
                Timestamp.class, gelesenZwei).toLocalDateTime()).isEqualTo(alt);
    }

    // verifies: TEST_NAC_ADM_01
    @Test
    void adminbereichIstEinDauerhafterGemeinsamerEmpfaenger() {
        Benutzerkonto zweiterAdmin = admin("Zweiter", "admin2@example.de");
        benachrichtigungen.adminbereich(Benachrichtigungsanlass.QUALIFIKATION_ABGELEGT,
                trainer.name() + " hat SCH-001 abgelegt.", "SCHULUNG", "SCH-001");
        benachrichtigungen.persoenlich(admin.id(), trainer.id(),
                Benachrichtigungsanlass.QUALIFIKATION_GENEHMIGT,
                "Persönlich", "SCHULUNG", "SCH-001");

        assertThat(benachrichtigungen.anzeigen(admin.id())).hasSize(2);
        assertThat(benachrichtigungen.anzeigen(zweiterAdmin.id())).singleElement()
                .satisfies(n -> assertThat(n.anlass()).contains(trainer.name()));
        Benutzerkonto dritterAdmin = admin("Dritter", "admin3@example.de");
        assertThat(benachrichtigungen.anzeigen(dritterAdmin.id())).singleElement();
        jdbc.update("DELETE FROM benutzerkonto WHERE id=?", trainer.id());
        assertThat(jdbc.queryForList("SELECT anlass FROM benachrichtigung WHERE empfaenger_rolle='ADMINISTRATOR'",
                String.class)).singleElement().asString().contains("Trainer");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM benachrichtigung WHERE empfaenger_rolle='ADMINISTRATOR'",
                Integer.class)).isOne();
    }

    // verifies: TEST_NAC_ADM_02
    @Test
    void adminLesezustandGiltFuerAlleUndNichtFuerPersoenlicheMitteilungen() {
        Benutzerkonto zweiterAdmin = admin("Zweiter", "admin2@example.de");
        benachrichtigungen.adminbereich(Benachrichtigungsanlass.QUALIFIKATION_ABGELEGT,
                "Gemeinsam", "SCHULUNG", "SCH-001");
        persoenlich(zweiterAdmin, "Persönlich");

        benachrichtigungen.anzeigen(admin.id());

        assertThat(benachrichtigungen.ungelesen(admin.id())).isZero();
        assertThat(benachrichtigungen.ungelesen(zweiterAdmin.id())).isEqualTo(1);
        assertThat(benachrichtigungen.anzeigen(zweiterAdmin.id())).hasSize(2);
    }

    // verifies: TEST_NAC_ANZ_07
    @Test
    void geloeschterTerminLaesstTextOhneSprungBestehen() {
        jdbc.update("""
                INSERT INTO termin (termin_id, schulung_id, startdatum, enddatum, status)
                VALUES ('T-1', 'SCH-001', '2030-01-02', '2030-01-02', 'geplant')
                """);
        benachrichtigungen.persoenlich(trainer.id(), admin.id(),
                Benachrichtigungsanlass.TERMIN_GELOESCHT,
                "Termin T-1 wurde gelöscht.", "TERMIN", "T-1");
        jdbc.update("DELETE FROM termin WHERE termin_id='T-1'");

        assertThat(benachrichtigungen.anzeigen(trainer.id())).singleElement().satisfies(n -> {
            assertThat(n.anlass()).isEqualTo("Termin T-1 wurde gelöscht.");
            assertThat(n.bezugVorhanden()).isFalse();
        });
    }

    // verifies: TEST_NAC_ANZ_05
    @Test
    void persoenlicheMitteilungenEndenNurMitDemEmpfaengerkonto() throws Exception {
        long persoenlich = persoenlich(trainer, "Dauerhaft");
        benachrichtigungen.adminbereich(Benachrichtigungsanlass.QUALIFIKATION_ABGELEGT,
                "Gemeinsam", "SCHULUNG", "SCH-001");
        jdbc.update("UPDATE benachrichtigung SET erstellt_am='2000-01-01 00:00:00' WHERE id=?", persoenlich);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/ich/benachrichtigungen/{id}", persoenlich)
                        .session(login(trainer)).with(csrf()))
                .andExpect(status().is4xxClientError());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM benachrichtigung WHERE id=?",
                Integer.class, persoenlich)).isOne();

        jdbc.update("DELETE FROM benutzerkonto WHERE id=?", trainer.id());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM benachrichtigung WHERE id=?",
                Integer.class, persoenlich)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM benachrichtigung WHERE empfaenger_rolle='ADMINISTRATOR'",
                Integer.class)).isOne();
    }

    // verifies: TEST_NAC_SICHER_01
    @Test
    void schnittstellenSchuetzenEmpfaengerAdminbereichUndLesezustand() throws Exception {
        long eigene = persoenlich(trainer, "Eigene");
        long fremde = persoenlich(anderer, "Fremde");
        benachrichtigungen.adminbereich(Benachrichtigungsanlass.QUALIFIKATION_ABGELEGT,
                "Admin", "SCHULUNG", "SCH-001");
        long adminNachricht = jdbc.queryForObject(
                "SELECT id FROM benachrichtigung WHERE empfaenger_rolle='ADMINISTRATOR'", Long.class);
        MockHttpSession trainerSitzung = login(trainer);
        MockHttpSession andereSitzung = login(anderer);

        assertThat(benachrichtigungen.anzeigen(trainer.id()))
                .extracting(BenachrichtigungService.Benachrichtigung::id)
                .containsExactly(eigene);
        jdbc.update("UPDATE benachrichtigung SET gelesen=FALSE, gelesen_am=NULL WHERE id=?", eigene);

        mvc.perform(get("/api/ich/benachrichtigungen")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/ich/benachrichtigungen/{id}/gelesen", fremde)
                        .session(trainerSitzung).with(csrf()))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/ich/benachrichtigungen/{id}/gelesen", adminNachricht)
                        .session(trainerSitzung).with(csrf()))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/ich/benachrichtigungen/{id}/gelesen", eigene)
                        .session(trainerSitzung))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/ich/benachrichtigungen/gelesen").session(andereSitzung))
                .andExpect(status().isForbidden());
        assertThat(gelesen(eigene)).isFalse();
        assertThat(gelesen(fremde)).isFalse();
        assertThat(gelesen(adminNachricht)).isFalse();
    }

    private long persoenlich(Benutzerkonto empfaenger, String text) {
        benachrichtigungen.persoenlich(empfaenger.id(), admin.id(),
                Benachrichtigungsanlass.QUALIFIKATION_GENEHMIGT,
                text, "SCHULUNG", "SCH-001");
        return jdbc.queryForObject("SELECT MAX(id) FROM benachrichtigung", Long.class);
    }

    private boolean gelesen(long id) {
        return jdbc.queryForObject("SELECT gelesen FROM benachrichtigung WHERE id=?", Boolean.class, id);
    }

    private Benutzerkonto admin(String name, String email) {
        Benutzerkonto konto = konten.registrieren(name, email, "pw");
        konten.rolleErteilen(admin.id(), konto.id(), Rolle.ADMINISTRATOR, konto.aenderungsstand());
        return konten.laden(konto.id());
    }

    private MockHttpSession login(Benutzerkonto konto) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/auth/anmelden").with(csrf())
                        .contentType("application/json")
                        .content("{\"email\":\"" + konto.email() + "\",\"passwort\":\"pw\"}"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }
}
