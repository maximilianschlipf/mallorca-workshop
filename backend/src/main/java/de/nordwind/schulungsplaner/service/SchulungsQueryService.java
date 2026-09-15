package de.nordwind.schulungsplaner.service;

import de.nordwind.schulungsplaner.domain.Schulung;
import de.nordwind.schulungsplaner.domain.Termin;
import de.nordwind.schulungsplaner.domain.VerfuegbarerTrainer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SchulungsQueryService {

    private final JdbcTemplate jdbcTemplate;

    public SchulungsQueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Schulung> findSchulungen(String suche, String kategorie) {
        String titelFilter = normalize(suche);
        String kategorieFilter = normalize(kategorie);

        List<String> matchingIds = findMatchingIds(titelFilter, kategorieFilter);
        if (matchingIds.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(", ", matchingIds.stream().map(id -> "?").toList());
        String sql = """
                SELECT s.id,
                       s.titel,
                       s.kategorie,
                       s.kurzbeschreibung,
                       s.dauer_in_tagen,
                       s.mindestteilnehmer_exklusiv,
                       s.max_teilnehmer_oeffentlich,
                       t.termin_id,
                       t.startdatum,
                       t.enddatum,
                       t.ort,
                       t.format,
                       t.status,
                       t.trainer_id,
                       COALESCE(bt.name, t.trainer_name_snapshot) AS trainer_name,
                       v.text AS voraussetzung
                FROM schulung s
                LEFT JOIN termin t ON t.schulung_id = s.id
                LEFT JOIN benutzerkonto bt ON bt.id = t.trainer_id
                LEFT JOIN voraussetzung v ON v.schulung_id = s.id
                WHERE s.id IN (%s)
                ORDER BY s.id, t.startdatum, t.termin_id
                """.formatted(placeholders);

        Map<String, SchulungBuilder> byId = new LinkedHashMap<>();
        jdbcTemplate.query(sql, rs -> {
            String id = rs.getString("id");
            String titel = rs.getString("titel");
            String kategorieWert = rs.getString("kategorie");
            String kurzbeschreibung = rs.getString("kurzbeschreibung");
            int dauerInTagen = rs.getInt("dauer_in_tagen");
            int mindestteilnehmerExklusiv = rs.getInt("mindestteilnehmer_exklusiv");
            Integer maxTeilnehmerOeffentlich = (Integer) rs.getObject("max_teilnehmer_oeffentlich");

            SchulungBuilder builder = byId.computeIfAbsent(id, key -> new SchulungBuilder(
                id,
                titel,
                kategorieWert,
                kurzbeschreibung,
                dauerInTagen,
                mindestteilnehmerExklusiv,
                maxTeilnehmerOeffentlich
            ));

            String voraussetzung = rs.getString("voraussetzung");
            if (voraussetzung != null && !builder.voraussetzungen.contains(voraussetzung)) {
                builder.voraussetzungen.add(voraussetzung);
            }

            String terminId = rs.getString("termin_id");
            if (terminId != null && !builder.terminIds.contains(terminId)) {
                builder.terminIds.add(terminId);
                builder.oeffentlicheTermine.add(new Termin(
                        terminId,
                    toDateString(rs.getDate("startdatum")),
                    toDateString(rs.getDate("enddatum")),
                        rs.getString("ort"),
                        rs.getString("format"),
                        rs.getString("status"),
                        rs.getString("trainer_id"),
                        rs.getString("trainer_name"),
                        findAssistenten(terminId)
                ));
            }
        }, matchingIds.toArray());

        return byId.values().stream().map(SchulungBuilder::build).toList();
    }

    public List<String> findKategorien() {
        return jdbcTemplate.queryForList(
                "SELECT DISTINCT kategorie FROM schulung ORDER BY kategorie",
                String.class
        );
    }

    public List<VerfuegbarerTrainer> findVerfuegbareTrainer(
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

    private List<String> findAssistenten(String terminId) {
        return jdbcTemplate.queryForList("""
                SELECT COALESCE(k.name, a.name_snapshot)
                FROM termin_assistent a
                LEFT JOIN benutzerkonto k ON k.id = a.benutzerkonto_id
                WHERE a.termin_id = ?
                ORDER BY a.platz
                """, String.class, terminId);
    }

    private List<String> findMatchingIds(String titelFilter, String kategorieFilter) {
        StringBuilder sql = new StringBuilder("SELECT id FROM schulung WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (titelFilter != null) {
            sql.append(" AND LOWER(titel) LIKE LOWER(?) ESCAPE '\\'");
            args.add("%" + escapeLike(titelFilter) + "%");
        }
        if (kategorieFilter != null) {
            sql.append(" AND LOWER(kategorie) = LOWER(?)");
            args.add(kategorieFilter);
        }
        sql.append(" ORDER BY id");
        return jdbcTemplate.queryForList(sql.toString(), String.class, args.toArray());
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String escapeLike(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    private String toDateString(Date date) {
        if (date == null) {
            return null;
        }
        LocalDate localDate = date.toLocalDate();
        return localDate.toString();
    }

    private static final class SchulungBuilder {
        private final String id;
        private final String titel;
        private final String kategorie;
        private final String kurzbeschreibung;
        private final int dauerInTagen;
        private final int mindestteilnehmerExklusiv;
        private final Integer maxTeilnehmerOeffentlich;
        private final List<String> voraussetzungen = new ArrayList<>();
        private final List<String> terminIds = new ArrayList<>();
        private final List<Termin> oeffentlicheTermine = new ArrayList<>();

        private SchulungBuilder(String id,
                                String titel,
                                String kategorie,
                                String kurzbeschreibung,
                                int dauerInTagen,
                                int mindestteilnehmerExklusiv,
                                Integer maxTeilnehmerOeffentlich) {
            this.id = id;
            this.titel = titel;
            this.kategorie = kategorie;
            this.kurzbeschreibung = kurzbeschreibung;
            this.dauerInTagen = dauerInTagen;
            this.mindestteilnehmerExklusiv = mindestteilnehmerExklusiv;
            this.maxTeilnehmerOeffentlich = maxTeilnehmerOeffentlich;
        }

        private Schulung build() {
            return new Schulung(
                    id,
                    titel,
                    kategorie,
                    kurzbeschreibung,
                    List.copyOf(voraussetzungen),
                    dauerInTagen,
                    mindestteilnehmerExklusiv,
                    maxTeilnehmerOeffentlich,
                    List.copyOf(oeffentlicheTermine)
            );
        }
    }
}
