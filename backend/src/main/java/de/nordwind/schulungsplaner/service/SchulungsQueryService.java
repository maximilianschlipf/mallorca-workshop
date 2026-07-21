package de.nordwind.schulungsplaner.service;

import de.nordwind.schulungsplaner.domain.Schulung;
import de.nordwind.schulungsplaner.domain.Termin;
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

    public List<Schulung> findAllSchulungen() {
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
                       v.text AS voraussetzung
                FROM schulung s
                LEFT JOIN termin t ON t.schulung_id = s.id
                LEFT JOIN voraussetzung v ON v.schulung_id = s.id
                ORDER BY s.id, t.startdatum, t.termin_id
                """;

        Map<String, SchulungBuilder> byId = new LinkedHashMap<>();
        jdbcTemplate.query(sql, rs -> {
            String id = rs.getString("id");
            String titel = rs.getString("titel");
            String kategorie = rs.getString("kategorie");
            String kurzbeschreibung = rs.getString("kurzbeschreibung");
            int dauerInTagen = rs.getInt("dauer_in_tagen");
            int mindestteilnehmerExklusiv = rs.getInt("mindestteilnehmer_exklusiv");
            Integer maxTeilnehmerOeffentlich = (Integer) rs.getObject("max_teilnehmer_oeffentlich");

            SchulungBuilder builder = byId.computeIfAbsent(id, key -> new SchulungBuilder(
                id,
                titel,
                kategorie,
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
                        rs.getString("trainer_id")
                ));
            }
        });

        return byId.values().stream().map(SchulungBuilder::build).toList();
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
