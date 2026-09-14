package de.nordwind.schulungsplaner.katalog;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * REQ_KAT_ID_01: Das System vergibt keine ID, zeigt beim Anlegen aber an,
 * nach welchem Schema die bestehenden Schulungen benannt sind. Teil von
 * TEST_KAT_ID_04.
 */
class KennungsschemaTest {

    @Test
    void shouldDescribeThePrevailingPatternOfExistingIds() {
        Kennungsschema schema = Kennungsschema.ausBestehenden(
                List.of("SCH-001", "SCH-002", "SCH-003"));

        assertThat(schema.muster()).isEqualTo("AAA-999");
        assertThat(schema.beispiele()).containsExactly("SCH-001", "SCH-002", "SCH-003");
    }

    @Test
    void shouldPickTheMostFrequentPatternWhenIdsDiffer() {
        Kennungsschema schema = Kennungsschema.ausBestehenden(
                List.of("SCH-001", "SCH-002", "SCH-003", "WORKSHOP-7"));

        assertThat(schema.muster()).isEqualTo("AAA-999");
    }

    @Test
    void shouldLimitTheNumberOfExamples() {
        Kennungsschema schema = Kennungsschema.ausBestehenden(
                List.of("SCH-001", "SCH-002", "SCH-003", "SCH-004", "SCH-005"));

        assertThat(schema.beispiele()).hasSize(Kennungsschema.MAX_BEISPIELE);
    }

    @Test
    void shouldReportNoPatternForAnEmptyCatalog() {
        Kennungsschema schema = Kennungsschema.ausBestehenden(List.of());

        assertThat(schema.muster()).isNull();
        assertThat(schema.beispiele()).isEmpty();
    }
}
