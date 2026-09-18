package de.nordwind.schulungsplaner;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "TRAINER")
class SchulungsApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    // verifies: TEST_KAT_SUCH_01, TEST_KAT_SUCH_02, TEST_KAT_SUCH_03, TEST_KAT_SUCH_04, TEST_KAT_SUCH_05
    @Test
    void liefertUndFiltertSchulungen() throws Exception {
        JsonNode alle = getJson("/api/schulungen");
        JsonNode leereParameter = getJson(mvc.perform(get("/api/schulungen")
                .param("suche", " ").param("kategorie", " ")));
        JsonNode suche = getJson(mvc.perform(get("/api/schulungen").param("suche", "  scRUM  ")));
        JsonNode kategorie = getJson(mvc.perform(get("/api/schulungen")
                .param("kategorie", "Cloud & DevOps")));
        JsonNode kombiniert = getJson(mvc.perform(get("/api/schulungen")
                .param("suche", "kubernetes").param("kategorie", "Cloud & DevOps")));
        JsonNode leer = getJson(mvc.perform(get("/api/schulungen")
                .param("suche", "gibtesnicht123")));

        assertThat(alle).isNotEmpty();
        assertThat(leereParameter).isEqualTo(alle);
        assertThat(suche).allSatisfy(s ->
                assertThat(s.get("titel").stringValue().toLowerCase()).contains("scrum"));
        assertThat(kategorie).allSatisfy(s ->
                assertThat(s.get("kategorie").stringValue()).isEqualTo("Cloud & DevOps"));
        assertThat(kombiniert).singleElement().satisfies(s -> {
            assertThat(s.get("titel").stringValue().toLowerCase()).contains("kubernetes");
            assertThat(s.get("kategorie").stringValue()).isEqualTo("Cloud & DevOps");
        });
        assertThat(leer).isEmpty();
    }

    // verifies: TEST_KAT_SUCH_06
    @Test
    void liefertSortierteKategorien() throws Exception {
        JsonNode kategorien = getJson("/api/kategorien");
        assertThat(kategorien).isNotEmpty();
        assertThat(kategorien.valueStream().map(JsonNode::stringValue).toList())
                .isSorted().doesNotHaveDuplicates();
    }

    // verifies: TEST_KAT_SICHT_01
    @Test
    void trainerDarfDenKatalogNichtVeraendern() throws Exception {
        mvc.perform(get("/api/schulungen"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/schulungen").with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/schulungen/SCH-001").with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/schulungen/SCH-001/archivierung").with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/schulungen/SCH-001").with(csrf()))
                .andExpect(status().isForbidden());
    }

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
        mvc.perform(get("/api/ich/termine")).andExpect(status().isUnauthorized());
    }

    private JsonNode getJson(String url) throws Exception {
        return getJson(mvc.perform(get(url)));
    }

    private JsonNode getJson(ResultActions result) throws Exception {
        return json.readTree(result.andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }
}
