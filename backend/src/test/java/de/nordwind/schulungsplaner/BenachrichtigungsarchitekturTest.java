package de.nordwind.schulungsplaner;

import de.nordwind.schulungsplaner.service.Benachrichtigungsanlass;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class BenachrichtigungsarchitekturTest {
    private static final Set<String> KATALOG = Set.of(
            "TRAINERZUWEISUNG_GESETZT", "TRAINERZUWEISUNG_BEENDET", "ROLLENWECHSEL",
            "TERMIN_GEAENDERT", "TERMIN_ABGESAGT", "TERMIN_GELOESCHT",
            "TERMIN_AUTOMATISCH_ABGESCHLOSSEN", "QUALIFIKATION_GENEHMIGT",
            "QUALIFIKATION_ABGELEHNT", "QUALIFIKATION_DURCH_ARCHIVIERUNG_ENTFALLEN",
            "QUALIFIKATION_DIREKT", "QUALIFIKATION_ENTZOGEN", "QUALIFIKATION_ABGELEGT",
            "VORMERKUNG_BESTAETIGT", "VORMERKUNG_ABGELEHNT",
            "VORMERKUNG_DURCH_ZUWEISUNG_ENTFALLEN", "ASSISTENZBEWERBUNG_ENTSCHIEDEN",
            "UEBERNAHMEANFRAGE_ENTSCHIEDEN", "UEBERNAHMEANFRAGE_DURCH_TAUSCH_ENTFALLEN",
            "TRAINERWECHSEL_DURCH_UEBERNAHME", "ABWESENHEITSANTRAG_ABGELEHNT",
            "ABWESENHEITSANTRAG_MANUELL_GENEHMIGT",
            "ABWESENHEITSANTRAG_NACH_FRIST_GENEHMIGT", "ERSATZTRAINER_ANFRAGE_BEENDET",
            "VERFUEGBARKEITSKONFLIKT_DURCH_ABWESENHEIT",
            "VORGANG_DURCH_TERMINENDE_ANGEPASST_ODER_ENTFALLEN",
            "VORGANG_DURCH_KONTOENDE_ENTFALLEN");

    // verifies: TEST_NAC_ANL_03
    @Test
    void produktcodeKannNurAnlaesseAusDemFreigegebenenKatalogVerwenden() {
        assertThat(Set.of(Benachrichtigungsanlass.values()).stream()
                .map(Enum::name).collect(Collectors.toSet())).isEqualTo(KATALOG);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Benachrichtigungsanlass.von("UNBEKANNT"));
    }

    // verifies: TEST_NAC_DAT_02
    @Test
    void produktcodeEnthaeltKeinenExternenZustellweg() throws IOException {
        try (var dateien = Files.walk(Path.of("src/main/java"))) {
            String produktcode = dateien.filter(pfad -> pfad.toString().endsWith(".java"))
                    .map(BenachrichtigungsarchitekturTest::lesen)
                    .collect(Collectors.joining("\n"));
            assertThat(produktcode)
                    .doesNotContain("jakarta.mail", "javax.mail", "java.net.http",
                            "org.springframework.mail", "org.springframework.web.client",
                            "WebClient", "RestClient");
        }
    }

    private static String lesen(Path pfad) {
        try {
            return Files.readString(pfad);
        } catch (IOException fehler) {
            throw new IllegalStateException(fehler);
        }
    }
}
