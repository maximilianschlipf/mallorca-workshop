package de.nordwind.schulungsplaner.service;

import de.nordwind.schulungsplaner.domain.VerfuegbarerTrainer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Beantwortet, welche Trainer fuer eine Schulung qualifiziert und in einem
 * Zeitraum nicht abwesend sind.
 */
@Service
public class TrainerQueryService {

    private final JdbcTemplate jdbcTemplate;

    public TrainerQueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<VerfuegbarerTrainer> findeVerfuegbareTrainer(
            String schulungId, LocalDate von, LocalDate bis) {
        return jdbcTemplate.query("""
                SELECT t.id, t.name, t.email
                FROM benutzerkonto t
                JOIN benutzerkonto_rolle r
                  ON r.benutzerkonto_id = t.id AND r.rolle = 'TRAINER'
                JOIN trainer_qualifikation q ON q.benutzerkonto_id = t.id
                WHERE q.schulung_id = ?
                  AND t.aktiv = TRUE
                  AND NOT EXISTS (
                    SELECT 1
                    FROM abwesenheit a
                    WHERE a.benutzerkonto_id = t.id
                      AND a.von <= ?
                      AND a.bis >= ?
                  )
                ORDER BY LOWER(t.name), t.name
                """,
                (rs, rowNum) -> new VerfuegbarerTrainer(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email")
                ),
                schulungId, bis, von
        );
    }
}
