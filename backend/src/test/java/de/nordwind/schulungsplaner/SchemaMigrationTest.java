package de.nordwind.schulungsplaner;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.sql.DriverManager;

import static org.assertj.core.api.Assertions.assertThatCode;

class SchemaMigrationTest {

    @Test
    void bestehendeTerminTabelleErhaeltSnapshotSpalten() throws Exception {
        try (var verbindung = DriverManager.getConnection(
                "jdbc:h2:mem:legacy-schema;DB_CLOSE_DELAY=-1;MODE=PostgreSQL", "sa", "")) {
            verbindung.createStatement().execute("""
                    CREATE TABLE termin (
                        termin_id VARCHAR(80) PRIMARY KEY,
                        schulung_id VARCHAR(50) NOT NULL,
                        startdatum DATE NOT NULL,
                        enddatum DATE NOT NULL,
                        ort VARCHAR(255) NOT NULL,
                        format VARCHAR(50),
                        status VARCHAR(50) NOT NULL,
                        trainer_id VARCHAR(36)
                    )
                    """);

            ScriptUtils.executeSqlScript(verbindung, new ClassPathResource("schema.sql"));

            assertThatCode(() -> verbindung.createStatement().execute("""
                    INSERT INTO termin
                    (termin_id, schulung_id, schulung_titel, trainer_name_snapshot,
                     startdatum, enddatum, status)
                    VALUES ('SCH-001-T0001', 'SCH-001', 'Titel', 'Trainer',
                            DATE '2030-01-02', DATE '2030-01-02', 'geplant')
                    """)).doesNotThrowAnyException();
        }
    }
}
