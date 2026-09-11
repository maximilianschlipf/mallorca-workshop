package de.nordwind.schulungsplaner;

import de.nordwind.schulungsplaner.domain.VerfuegbarerTrainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TrainerVerfuegbarkeitApiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnQualifiedAvailableTrainersSortedByName() {
        ResponseEntity<VerfuegbarerTrainer[]> response = restTemplate.getForEntity(
                "/api/trainer/verfuegbar?schulungId=SCH-001&von=2026-07-01&bis=2026-07-02",
                VerfuegbarerTrainer[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .extracting(VerfuegbarerTrainer::name)
                .containsExactly("Elena Fischer", "Julia Hoffmann");
    }

    @Test
    void shouldTreatAbsenceBoundaryDaysAsUnavailable() {
        for (String boundary : new String[]{"2026-08-03", "2026-08-14"}) {
            ResponseEntity<VerfuegbarerTrainer[]> response = restTemplate.getForEntity(
                    "/api/trainer/verfuegbar?schulungId=SCH-001&von=" + boundary + "&bis=" + boundary,
                    VerfuegbarerTrainer[].class
            );

            assertThat(response.getBody())
                    .extracting(VerfuegbarerTrainer::name)
                    .containsExactly("Elena Fischer");
        }
    }

    @Test
    void shouldRejectStartDateAfterEndDate() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/trainer/verfuegbar?schulungId=SCH-001&von=2026-08-04&bis=2026-08-03",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
