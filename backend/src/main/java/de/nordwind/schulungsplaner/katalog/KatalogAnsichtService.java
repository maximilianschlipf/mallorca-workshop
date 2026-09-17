package de.nordwind.schulungsplaner.katalog;

import de.nordwind.schulungsplaner.domain.Schulung;
import de.nordwind.schulungsplaner.domain.Termin;
import de.nordwind.schulungsplaner.katalog.ablage.KatalogRepository;
import de.nordwind.schulungsplaner.katalog.ablage.KategorienRepository;
import de.nordwind.schulungsplaner.katalog.zustand.SchulungszustandRepository;
import de.nordwind.schulungsplaner.katalog.zustand.Schulungszustand;
import de.nordwind.schulungsplaner.katalog.zustand.Zustandseintrag;
import de.nordwind.schulungsplaner.service.TerminService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fuehrt die beiden Ablagen fuer die Anzeige zusammen: die Beschreibung aus
 * den Katalogdateien, den Zustand und die Termine aus der Datenbank.
 * Verbunden sind sie ueber die Schulungs-ID (REQ_KAT_TERM_01).
 */
@Service
public class KatalogAnsichtService {

    private final KatalogRepository katalog;
    private final KategorienRepository kategorien;
    private final SchulungszustandRepository zustaende;
    private final JdbcTemplate jdbcTemplate;
    private final TerminService terminService;

    public KatalogAnsichtService(KatalogRepository katalog,
                                 KategorienRepository kategorien,
                                 SchulungszustandRepository zustaende,
                                 JdbcTemplate jdbcTemplate,
                                 TerminService terminService) {
        this.katalog = katalog;
        this.kategorien = kategorien;
        this.zustaende = zustaende;
        this.jdbcTemplate = jdbcTemplate;
        this.terminService = terminService;
    }

    /**
     * Der Katalog, eingeschraenkt auf Suchbegriff und Kategorie. Beide sind
     * freiwillig und wirken zusammen (REQ_KAT_SUCH_04); ohne Angabe umfasst
     * das Ergebnis den ganzen Katalog, ohne Treffer ist es leer
     * (REQ_KAT_SUCH_05).
     */
    public List<Schulung> findeSchulungen(String suche, String kategorie) {
        terminService.nachziehen();
        String titelFilter = normalisiere(suche);
        String kategorieFilter = normalisiere(kategorie);

        Map<SchulungId, Zustandseintrag> zustandJeId = zustaende.alleZustaende();
        Map<String, List<Termin>> termineJeSchulung = termineJeSchulung();

        return katalog.ladeAlle().stream()
                .filter(schulung -> passtZumTitel(schulung, titelFilter))
                .filter(schulung -> passtZurKategorie(schulung, kategorieFilter))
                .map(schulung -> zusammenfuehren(schulung, zustandJeId, termineJeSchulung))
                .sorted(ANZEIGEREIHENFOLGE)
                .toList();
    }

    /** Eine einzelne Schulung samt Zustand und Terminen. */
    public java.util.Optional<Schulung> findeSchulung(SchulungId id) {
        terminService.nachziehen();
        return katalog.lade(id).map(schulung -> zusammenfuehren(
                schulung, zustaende.alleZustaende(), termineJeSchulung()));
    }

    /**
     * Das Schema, nach dem die bestehenden Schulungen benannt sind
     * (REQ_KAT_ID_01). Eine ID wird daraus ausdruecklich nicht abgeleitet --
     * die vergibt der Mensch.
     */
    public Kennungsschema kennungsschema() {
        return Kennungsschema.ausBestehenden(
                katalog.alleIds().stream().map(SchulungId::wert).toList());
    }

    /**
     * Die zur Auswahl stehenden Kategorien, doppelfrei und alphabetisch
     * sortiert (REQ_KAT_SUCH_06).
     *
     * <p>Sie stammen aus der gepflegten Liste und werden nicht aus den
     * Schulungen abgeleitet -- sonst verschwaende eine Kategorie, sobald ihr
     * keine Schulung mehr zugeordnet ist (REQ_KAT_KATG_01).
     */
    public List<String> findeKategorien() {
        return kategorien.ladeAlle();
    }

    /**
     * Termine, deren Schulungs-ID zu keiner Katalogdatei fuehrt
     * (REQ_KAT_TERM_02).
     */
    public List<VerwaisterTermin> verwaisteTermine() {
        Set<String> bekannt = katalog.alleIds().stream()
                .map(SchulungId::wert)
                .collect(Collectors.toSet());

        // Termine mit hinterlassenem Titel sind kein Verweis ins Leere: Ihre
        // Schulung wurde ueber die Anwendung geloescht, und der Titel steht
        // bewusst als Text an ihnen (REQ_KAT_LOE_04).
        return jdbcTemplate.query(
                        "SELECT termin_id, schulung_id FROM termin "
                                + "WHERE schulung_titel IS NULL ORDER BY termin_id",
                        (rs, zeile) -> new VerwaisterTermin(
                                rs.getString("termin_id"), rs.getString("schulung_id")))
                .stream()
                .filter(termin -> !bekannt.contains(termin.schulungId()))
                .toList();
    }

    /**
     * Aktive zuerst, archivierte dahinter, innerhalb der Gruppe nach ID
     * (REQ_KAT_SICHT_02). Archivierte verschwinden nicht -- zu ihnen laufen
     * weiterhin Termine --, treten aber nicht in den Vordergrund.
     */
    private static final Comparator<Schulung> ANZEIGEREIHENFOLGE =
            Comparator.comparing((Schulung s) -> s.zustand() == Schulungszustand.ARCHIVIERT)
                    .thenComparing(Schulung::id);

    private Schulung zusammenfuehren(Katalogschulung schulung,
                                     Map<SchulungId, Zustandseintrag> zustandJeId,
                                     Map<String, List<Termin>> termineJeSchulung) {
        Zustandseintrag eintrag = zustandJeId.get(schulung.id());
        return new Schulung(
                schulung.id().wert(),
                schulung.titel(),
                schulung.kategorie(),
                schulung.kurzbeschreibung(),
                schulung.voraussetzungen(),
                schulung.dauerInTagen(),
                schulung.mindestteilnehmerExklusiv(),
                schulung.maxTeilnehmerOeffentlich(),
                // Fehlt der Zustandssatz, gilt die Schulung als aktiv: Der
                // Katalog gibt vor, was es gibt, und eine frisch hinzugefuegte
                // Datei soll nicht unsichtbar bleiben.
                eintrag == null ? Schulungszustand.AKTIV : eintrag.zustand(),
                termineJeSchulung.getOrDefault(schulung.id().wert(), List.of())
        );
    }

    private Map<String, List<Termin>> termineJeSchulung() {
        Map<String, List<Termin>> jeSchulung = new java.util.HashMap<>();
        jdbcTemplate.query("""
                SELECT t.termin_id, t.schulung_id, t.startdatum, t.enddatum, t.ort, t.format,
                       t.status, t.trainer_id,
                       COALESCE(k.name, t.trainer_name_snapshot) AS trainer_name
                FROM termin t
                LEFT JOIN benutzerkonto k ON k.id = t.trainer_id
                ORDER BY startdatum, termin_id
                """, rs -> {
            jeSchulung.computeIfAbsent(rs.getString("schulung_id"), id -> new ArrayList<>())
                    .add(new Termin(
                            rs.getString("termin_id"),
                            alsText(rs.getDate("startdatum")),
                            alsText(rs.getDate("enddatum")),
                            rs.getString("ort"),
                            rs.getString("format"),
                            rs.getString("status"),
                            rs.getString("trainer_id"),
                            rs.getString("trainer_name"),
                            assistenten(rs.getString("termin_id"))));
        });
        return jeSchulung;
    }

    private List<String> assistenten(String terminId) {
        return jdbcTemplate.queryForList("""
                SELECT COALESCE(k.name, a.name_snapshot)
                FROM termin_assistent a
                LEFT JOIN benutzerkonto k ON k.id = a.benutzerkonto_id
                WHERE a.termin_id = ?
                ORDER BY a.platz
                """, String.class, terminId);
    }

    /** Die Suche findet jeden Titel, der den Begriff enthaelt (REQ_KAT_SUCH_01). */
    private static boolean passtZumTitel(Katalogschulung schulung, String filter) {
        return filter == null
                || schulung.titel().toLowerCase().contains(filter.toLowerCase());
    }

    /** Anders als die Suche trifft der Filter die Kategorie genau (REQ_KAT_SUCH_03). */
    private static boolean passtZurKategorie(Katalogschulung schulung, String filter) {
        return filter == null || schulung.kategorie().equalsIgnoreCase(filter);
    }

    /**
     * Fuehrender und abschliessender Leerraum bleibt ausser Betracht, ein
     * leerer Wert wirkt wie keine Angabe (REQ_KAT_SUCH_02, REQ_KAT_SUCH_04).
     */
    private static String normalisiere(String wert) {
        if (wert == null) {
            return null;
        }
        String getrimmt = wert.trim();
        return getrimmt.isEmpty() ? null : getrimmt;
    }

    private static String alsText(Date datum) {
        return datum == null ? null : datum.toLocalDate().toString();
    }
}
