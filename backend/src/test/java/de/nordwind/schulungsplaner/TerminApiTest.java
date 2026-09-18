package de.nordwind.schulungsplaner;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TerminApiTest.FesteZeit.class)
class TerminApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    static class FesteZeit {
        @Bean @Primary Clock testClock() {
            return Clock.fixed(Instant.parse("2026-09-17T10:00:00Z"), ZoneId.of("Europe/Berlin"));
        }
    }

    @Test
    void geschuetzteTerminmutationenErfordernAnmeldungCsrfUndAdministratorrolle() throws Exception {
        MockHttpSession admin = anmelden("julia.hoffmann@simplytest-academy.de");
        MockHttpSession trainer = anmelden("markus.weber@simplytest-academy.de");
        String body = """
                {"schulungId":"SCH-001","startdatum":"2031-01-02","enddatum":"2031-01-02",
                 "zugangsart":null,"durchfuehrungsart":null,"ort":null,
                 "kundenfirma":null,"onlineZugang":null}
                """;
        mvc.perform(post("/api/termine").session(admin).contentType("application/json").content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/termine").session(trainer).with(csrf())
                        .contentType("application/json").content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/termine").session(admin).with(csrf())
                        .contentType("application/json").content(body))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "PUT /api/termine/T", "POST /api/termine/T/bestaetigung",
            "POST /api/termine/T/absage", "DELETE /api/termine/T",
            "POST /api/termine/T/buchungen", "PUT /api/termine/T/buchungen/1/teilnahmestatus",
            "DELETE /api/termine/T/buchungen/1", "PUT /api/termine/T/trainer/TRN-005",
            "DELETE /api/termine/T/trainer", "PUT /api/termine/T/assistenten/TRN-003"
    })
    void jedeTerminmutationErfordertCsrf(String fall) throws Exception {
        MockHttpSession admin = anmelden("julia.hoffmann@simplytest-academy.de");
        String[] teile = fall.split(" ", 2);
        var anfrage = switch (teile[0]) {
            case "POST" -> post(teile[1]);
            case "PUT" -> put(teile[1]);
            case "DELETE" -> delete(teile[1]);
            default -> throw new IllegalArgumentException(fall);
        };
        mvc.perform(anfrage.session(admin).contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void mitgesendeteTerminIdWirdNichtAlsKennungUebernommen() throws Exception {
        MockHttpSession admin = anmelden("julia.hoffmann@simplytest-academy.de");
        JsonNode antwort = json.readTree(mvc.perform(post("/api/termine").session(admin).with(csrf())
                        .contentType("application/json").content("""
                                {"terminId":"FREMD-T0042","schulungId":"SCH-007",
                                 "startdatum":"2031-01-06","enddatum":"2031-01-06"}
                                """))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        assertThat(antwort.get("terminId").stringValue()).isNotEqualTo("FREMD-T0042");
    }

    @Test
    void zuLangerAbsagegrundWirdVorDerFachlogikAbgewiesen() throws Exception {
        MockHttpSession admin = anmelden("julia.hoffmann@simplytest-academy.de");
        mvc.perform(post("/api/termine/NICHT-VORHANDEN/absage").session(admin).with(csrf())
                        .contentType("application/json")
                        .content("{\"grund\":\"" + "x".repeat(1001) + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithAnonymousUser
    void terminSchnittstelleWeistNichtAngemeldeteAb() throws Exception {
        mvc.perform(get("/api/termine")).andExpect(status().isUnauthorized());
    }

    // verifies: TEST_TER_FORM_12
    @Test
    void direkteDetailabfrageBlendetOnlineZugangUndTeilnehmerFuerUnbeteiligteAus() throws Exception {
        MockHttpSession admin = anmelden("julia.hoffmann@simplytest-academy.de");
        MockHttpSession trainer = anmelden("elena.fischer@simplytest-academy.de");
        MockHttpSession assistent = anmelden("sophie.bauer@simplytest-academy.de");
        MockHttpSession fremd = anmelden("markus.weber@simplytest-academy.de");
        String body = """
                {"schulungId":"SCH-001","startdatum":"2031-02-03","enddatum":"2031-02-03",
                 "zugangsart":"oeffentlich","durchfuehrungsart":"remote","ort":null,
                 "kundenfirma":null,"onlineZugang":"https://example.org/geheim"}
                """;
        JsonNode angelegt = json.readTree(mvc.perform(post("/api/termine").session(admin).with(csrf())
                        .contentType("application/json").content(body)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        String id = angelegt.get("terminId").stringValue();
        mvc.perform(put("/api/termine/{id}/trainer/TRN-005", id).session(admin).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(put("/api/termine/{id}/assistenten/TRN-003", id).session(admin).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/termine/{id}/buchungen", id).session(admin).with(csrf())
                        .contentType("application/json")
                        .content("{\"name\":\"Person\",\"firma\":\"Acme\",\"bemerkung\":\"Privat\",\"teilnahmestatus\":\"offen\"}"))
                .andExpect(status().isCreated());
        JsonNode fuerAdmin = details(id, admin);
        JsonNode fuerTrainer = details(id, trainer);
        JsonNode fuerAssistent = details(id, assistent);
        JsonNode fuerFremden = details(id, fremd);
        assertThat(fuerAdmin.get("onlineZugang").stringValue()).isEqualTo("https://example.org/geheim");
        assertThat(fuerAdmin.get("teilnehmer")).hasSize(1);
        assertThat(fuerTrainer.get("onlineZugang").stringValue()).isEqualTo("https://example.org/geheim");
        assertThat(fuerTrainer.get("teilnehmer")).hasSize(1);
        assertThat(fuerAssistent.get("onlineZugang").stringValue()).isEqualTo("https://example.org/geheim");
        assertThat(fuerAssistent.get("teilnehmer")).isEmpty();
        assertThat(fuerFremden.get("onlineZugang").isNull()).isTrue();
        assertThat(fuerFremden.get("teilnehmer")).isEmpty();
    }

    private JsonNode details(String id, MockHttpSession session) throws Exception {
        return json.readTree(mvc.perform(get("/api/termine/{id}", id).session(session))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    private MockHttpSession anmelden(String email) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/auth/anmelden").with(csrf())
                        .contentType("application/json")
                        .content("{\"email\":\"" + email + "\",\"passwort\":\"test-passwort\"}"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession();
    }
}
