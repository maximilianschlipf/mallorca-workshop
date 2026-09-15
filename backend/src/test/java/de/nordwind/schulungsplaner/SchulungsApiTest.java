package de.nordwind.schulungsplaner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "TRAINER")
class SchulungsApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void liefertUndFiltertSchulungen() throws Exception {
        JsonNode alle = getJson("/api/schulungen");
        JsonNode suche = getJson(mvc.perform(get("/api/schulungen").param("suche", "  scRUM  ")));
        JsonNode kategorie = getJson(mvc.perform(get("/api/schulungen")
                .param("kategorie", "Cloud & DevOps")));
        JsonNode kombiniert = getJson(mvc.perform(get("/api/schulungen")
                .param("suche", "kubernetes").param("kategorie", "Cloud & DevOps")));
        JsonNode leer = getJson(mvc.perform(get("/api/schulungen")
                .param("suche", "gibtesnicht123")));

        assertThat(alle).isNotEmpty();
        assertThat(suche).allSatisfy(s ->
                assertThat(s.get("titel").asText().toLowerCase()).contains("scrum"));
        assertThat(kategorie).allSatisfy(s ->
                assertThat(s.get("kategorie").asText()).isEqualTo("Cloud & DevOps"));
        assertThat(kombiniert).hasSize(1);
        assertThat(leer).isEmpty();
    }

    @Test
    void liefertSortierteKategorien() throws Exception {
        JsonNode kategorien = getJson("/api/kategorien");
        assertThat(kategorien).isNotEmpty();
        assertThat(kategorien.valueStream().map(JsonNode::asText).toList()).isSorted();
    }

    @Test
    void findetNurQualifizierteVerfuegbareTrainer() throws Exception {
        JsonNode frei = getJson(
                "/api/trainer/verfuegbar?schulungId=SCH-001&von=2026-07-01&bis=2026-07-02");
        JsonNode abwesend = getJson(
                "/api/trainer/verfuegbar?schulungId=SCH-001&von=2026-08-03&bis=2026-08-03");

        assertThat(frei.valueStream().map(t -> t.get("name").asText()).toList())
                .containsExactly("Elena Fischer", "Julia Hoffmann");
        assertThat(abwesend.valueStream().map(t -> t.get("name").asText()).toList())
                .containsExactly("Elena Fischer");
    }

    @Test
    void weistUngueltigenZeitraumAb() throws Exception {
        mvc.perform(get("/api/trainer/verfuegbar")
                        .param("schulungId", "SCH-001")
                        .param("von", "2026-08-04")
                        .param("bis", "2026-08-03"))
                .andExpect(status().isBadRequest());
    }

    // verifies: TEST_USR_LOGIN_02
    @Test
    @WithAnonymousUser
    void weistNichtAngemeldeteAufrufeAb() throws Exception {
        mvc.perform(post("/api/auth/registrieren").with(csrf())
                        .contentType("application/json")
                        .content("{\"name\":\"Öffentlich\",\"email\":\"oeffentlich@example.de\",\"passwort\":\"pw\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/anmelden").with(csrf())
                        .contentType("application/json")
                        .content("{\"email\":\"oeffentlich@example.de\",\"passwort\":\"pw\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/schulungen")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/ich")).andExpect(status().isUnauthorized());
    }

    private JsonNode getJson(String url) throws Exception {
        return getJson(mvc.perform(get(url)));
    }

    private JsonNode getJson(ResultActions result) throws Exception {
        return json.readTree(result.andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }
}
