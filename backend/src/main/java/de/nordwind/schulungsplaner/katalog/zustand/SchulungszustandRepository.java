package de.nordwind.schulungsplaner.katalog.zustand;

import de.nordwind.schulungsplaner.katalog.SchulungId;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Fuehrt den Zustand der Schulungen in der Datenbank.
 *
 * <p>Diese Tabelle ist der einzige Ort, an dem die Datenbank etwas ueber eine
 * Schulung weiss. Alles Beschreibende steht im Katalog; verbunden sind beide
 * ueber die Schulungs-ID (REQ_KAT_TERM_01).
 */
@Repository
public class SchulungszustandRepository {

    private static final RowMapper<Zustandseintrag> ZEILE = (rs, zeile) -> new Zustandseintrag(
            SchulungId.von(rs.getString("schulung_id")),
            Schulungszustand.valueOf(rs.getString("zustand")),
            toLocalDate(rs.getDate("archiviert_am")),
            rs.getLong("version")
    );

    private final JdbcTemplate jdbcTemplate;

    public SchulungszustandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Zustandseintrag> lade(SchulungId id) {
        return jdbcTemplate.query(
                        "SELECT schulung_id, zustand, archiviert_am, version "
                                + "FROM schulung_zustand WHERE schulung_id = ?",
                        ZEILE, id.wert())
                .stream()
                .findFirst();
    }

    public Map<SchulungId, Zustandseintrag> alleZustaende() {
        List<Zustandseintrag> eintraege = jdbcTemplate.query(
                "SELECT schulung_id, zustand, archiviert_am, version "
                        + "FROM schulung_zustand ORDER BY schulung_id", ZEILE);

        Map<SchulungId, Zustandseintrag> nachId = new LinkedHashMap<>();
        eintraege.forEach(eintrag -> nachId.put(eintrag.schulungId(), eintrag));
        return nachId;
    }

    /** Eine neu angelegte Schulung ist aktiv (REQ_KAT_PFLEG_01). */
    public void legeAn(SchulungId id) {
        jdbcTemplate.update(
                "INSERT INTO schulung_zustand (schulung_id, zustand, archiviert_am, version) "
                        + "VALUES (?, ?, NULL, 0)",
                id.wert(), Schulungszustand.AKTIV.name());
    }

    public void archiviere(SchulungId id, LocalDate am) {
        setzeZustand(id, Schulungszustand.ARCHIVIERT, am);
    }

    /**
     * Setzt die Schulung zurueck auf aktiv. Der Archivierungszeitpunkt
     * entfaellt dabei -- sonst zaehlte die Frist aus REQ_KAT_LOE_02 nach einer
     * Reaktivierung weiter.
     */
    public void reaktiviere(SchulungId id) {
        setzeZustand(id, Schulungszustand.AKTIV, null);
    }

    public boolean entferne(SchulungId id) {
        return jdbcTemplate.update(
                "DELETE FROM schulung_zustand WHERE schulung_id = ?", id.wert()) > 0;
    }

    private void setzeZustand(SchulungId id, Schulungszustand zustand, LocalDate archiviertAm) {
        jdbcTemplate.update(
                "UPDATE schulung_zustand SET zustand = ?, archiviert_am = ?, "
                        + "version = version + 1 WHERE schulung_id = ?",
                zustand.name(), archiviertAm, id.wert());
    }

    private static LocalDate toLocalDate(Date datum) {
        return datum == null ? null : datum.toLocalDate();
    }
}
