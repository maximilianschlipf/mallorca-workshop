package de.nordwind.schulungsplaner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP-Schicht zu Aufgabe 1 (STORY_ABW_ERF_02/TEST_ABW_ERF_03,
 * STORY_ABW_ERF_03/TEST_ABW_ERF_04): Anmeldung, CSRF und die Schnittstelle
 * selbst, ergänzend zu den service-Tests in AbwesenheitIntegrationTest.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(AbwesenheitApiTest.FesteZeit.class)
class AbwesenheitApiTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    static class FesteZeit {
        @Bean @Primary Clock testClock() {
            return Clock.fixed(Instant.parse("2026-09-17T10:00:00Z"), ZoneId.of("Europe/Berlin"));
        }
    }

    // verifies: TEST_ABW_ERF_03
    @Test
    void geschuetzteAbwesenheitsmutationenErfordernAnmeldungUndCsrf() throws Exception {
        MockHttpSession trainer = anmelden("elena.fischer@simplytest-academy.de");
        long id = abwesenheitAnlegen("TRN-005", "2029-01-08", "2029-01-09");
        String aenderung = "{\"von\":\"2029-02-01\",\"bis\":\"2029-02-02\",\"grund\":null}";

        // Ohne Anmeldung: CSRF-Token beigefügt, damit tatsächlich die fehlende
        // Authentifizierung geprüft wird und nicht das vorgelagerte CSRF-Filter greift.
        mvc.perform(put("/api/ich/abwesenheiten/{id}", id).with(csrf())
                        .contentType("application/json").content(aenderung))
                .andExpect(status().isUnauthorized());
        // Angemeldet, aber ohne CSRF-Token.
        mvc.perform(put("/api/ich/abwesenheiten/{id}", id).session(trainer)
                        .contentType("application/json").content(aenderung))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/ich/abwesenheiten/{id}", id).session(trainer))
                .andExpect(status().isForbidden());

        mvc.perform(put("/api/ich/abwesenheiten/{id}", id).session(trainer).with(csrf())
                        .contentType("application/json").content(aenderung))
                .andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("SELECT von FROM abwesenheit WHERE id=?", LocalDate.class, id))
                .isEqualTo(LocalDate.parse("2029-02-01"));

        mvc.perform(delete("/api/ich/abwesenheiten/{id}", id).session(trainer).with(csrf()))
                .andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM abwesenheit WHERE id=?", Integer.class, id)).isZero();
    }

    // verifies: TEST_ABW_ERF_03, TEST_ABW_ERF_04
    @Test
    void fremdeAbwesenheitWirdUeberDieSchnittstelleMit404Abgewiesen() throws Exception {
        MockHttpSession andererTrainer = anmelden("sophie.bauer@simplytest-academy.de");
        long id = abwesenheitAnlegen("TRN-005", "2029-03-08", "2029-03-09");

        mvc.perform(put("/api/ich/abwesenheiten/{id}", id).session(andererTrainer).with(csrf())
                        .contentType("application/json")
                        .content("{\"von\":\"2029-05-01\",\"bis\":\"2029-05-02\",\"grund\":null}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/ich/abwesenheiten/{id}", id).session(andererTrainer).with(csrf()))
                .andExpect(status().isNotFound());

        assertThat(jdbc.queryForObject("SELECT von FROM abwesenheit WHERE id=?", LocalDate.class, id))
                .isEqualTo(LocalDate.parse("2029-03-08"));
    }

    @Test
    @WithAnonymousUser
    void abwesenheitsSchnittstelleWeistNichtAngemeldeteAb() throws Exception {
        mvc.perform(delete("/api/ich/abwesenheiten/1").with(csrf())).andExpect(status().isUnauthorized());
    }

    private long abwesenheitAnlegen(String kontoId, String von, String bis) {
        jdbc.update("INSERT INTO abwesenheit (benutzerkonto_id, von, bis) VALUES (?, ?, ?)",
                kontoId, LocalDate.parse(von), LocalDate.parse(bis));
        return jdbc.queryForObject("SELECT id FROM abwesenheit WHERE benutzerkonto_id=? AND von=?",
                Long.class, kontoId, LocalDate.parse(von));
    }

    private MockHttpSession anmelden(String email) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/auth/anmelden").with(csrf())
                        .contentType("application/json")
                        .content("{\"email\":\"" + email + "\",\"passwort\":\"test-passwort\"}"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession();
    }
}
