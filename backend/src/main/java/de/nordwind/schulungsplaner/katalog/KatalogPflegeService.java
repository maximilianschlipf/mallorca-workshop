package de.nordwind.schulungsplaner.katalog;

import de.nordwind.schulungsplaner.domain.Schulung;
import de.nordwind.schulungsplaner.katalog.ablage.KatalogCommitter;
import de.nordwind.schulungsplaner.katalog.ablage.KatalogRepository;
import de.nordwind.schulungsplaner.katalog.ablage.KategorienRepository;
import de.nordwind.schulungsplaner.katalog.zustand.SchulungszustandRepository;
import de.nordwind.schulungsplaner.service.BenachrichtigungService;
import de.nordwind.schulungsplaner.service.Benachrichtigungsanlass;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Legt Schulungen an und aendert sie (REQ_KAT_PFLEG_01, REQ_KAT_PFLEG_02).
 *
 * <p>Jede Aenderung durchlaeuft dieselbe Reihenfolge: pruefen, dann schreiben,
 * dann committen. Wird bei der Pruefung etwas abgewiesen, bleibt die Datei
 * unberuehrt und es entsteht kein Commit -- eine abgewiesene Eingabe darf in
 * der Versionsgeschichte keine Spur hinterlassen.
 */
@Service
public class KatalogPflegeService {

    private final KatalogRepository katalog;
    private final KategorienRepository kategorien;
    private final SchulungszustandRepository zustaende;
    private final KatalogCommitter committer;
    private final KatalogAnsichtService ansicht;
    private final JdbcTemplate jdbcTemplate;
    private final Clock uhr;
    private final BenachrichtigungService benachrichtigungen;

    /**
     * Die Frist aus REQ_KAT_LOE_02, ab der eine archivierte Schulung
     * geloescht werden darf.
     */
    private static final int AUFBEWAHRUNG_MONATE = 6;

    public KatalogPflegeService(KatalogRepository katalog,
                                KategorienRepository kategorien,
                                SchulungszustandRepository zustaende,
                                KatalogCommitter committer,
                                KatalogAnsichtService ansicht,
                                JdbcTemplate jdbcTemplate,
                                Clock uhr,
                                BenachrichtigungService benachrichtigungen) {
        this.katalog = katalog;
        this.kategorien = kategorien;
        this.zustaende = zustaende;
        this.committer = committer;
        this.ansicht = ansicht;
        this.jdbcTemplate = jdbcTemplate;
        this.uhr = uhr;
        this.benachrichtigungen = benachrichtigungen;
    }

    /** Eine neu angelegte Schulung ist aktiv (REQ_KAT_PFLEG_01). */
    @Transactional
    public Katalogantwort legeAn(Schulungseingabe eingabe) {
        Katalogschulung schulung = pruefe(eingabe);

        if (katalog.existiert(schulung.id())) {
            throw KatalogFehler.konflikt(new Feldfehler("id", Fehlercode.ID_VERGEBEN,
                    "Die Schulungs-ID '" + schulung.id() + "' ist bereits vergeben."));
        }

        Path datei = katalog.speichere(schulung);
        zustaende.legeAn(schulung.id());
        committer.sichere("Schulung " + schulung.id() + " angelegt", List.of(datei));

        return Katalogantwort.ohneWarnung(ansicht.findeSchulung(schulung.id()).orElseThrow());
    }

    /**
     * Aendert die Beschreibung einer bestehenden Schulung. Alle Felder ausser
     * der ID sind aenderbar (REQ_KAT_PFLEG_02, REQ_KAT_PFLEG_03).
     */
    @Transactional
    public Katalogantwort aendere(SchulungId id, Schulungseingabe eingabe) {
        Katalogschulung bisher = katalog.lade(id)
                .orElseThrow(() -> new SchulungNichtGefunden(id));

        if (eingabe.id() != null && !eingabe.id().equals(id.wert())) {
            throw KatalogFehler.abgewiesen(List.of(new Feldfehler(
                    "id", Fehlercode.ID_UNVERAENDERLICH,
                    "Die Kennung einer Schulung kann nach dem Anlegen nicht geändert werden. "
                            + "Sie benennt die Katalogdatei und bindet die Termine an.")));
        }

        Katalogschulung geaendert = pruefe(eingabe.mitId(id.wert()));
        List<Warnung> warnungen = warneBeiGeaenderterDauer(bisher, geaendert);

        Path datei = katalog.speichere(geaendert);
        committer.sichere("Schulung " + id + " geändert", List.of(datei));

        return new Katalogantwort(ansicht.findeSchulung(id).orElseThrow(), warnungen);
    }

    /**
     * REQ_KAT_PFLEG_04: Wird die Dauer geaendert und bestehen bereits Termine,
     * wird gewarnt. REQ_KAT_PFLEG_05 sorgt dafuer, dass die Termine dabei
     * unberuehrt bleiben -- diese Klasse fasst sie schlicht nicht an.
     */
    private List<Warnung> warneBeiGeaenderterDauer(Katalogschulung bisher,
                                                   Katalogschulung geaendert) {
        List<Warnung> warnungen = new ArrayList<>();
        if (bisher.dauerInTagen() == geaendert.dauerInTagen()) {
            return warnungen;
        }
        int termine = zaehleTermine(geaendert.id());
        if (termine > 0) {
            warnungen.add(new Warnung(Warnung.Warncode.DAUER_GEAENDERT_MIT_TERMINEN,
                    "Die Dauer wurde von " + bisher.dauerInTagen() + " auf "
                            + geaendert.dauerInTagen() + " Tage geändert. Zu dieser Schulung "
                            + "bestehen bereits " + termine + " Termine; sie behalten ihren "
                            + "Zeitraum. Die neue Dauer gilt für neu angelegte Termine."));
        }
        return warnungen;
    }

    /**
     * Setzt eine aktive Schulung in den Zustand archiviert (REQ_KAT_ARCH_01).
     *
     * <p>Das gelingt auch dann, wenn noch zukuenftige Termine geplant sind --
     * sie bleiben unveraendert bestehen und finden statt (REQ_KAT_ARCH_02).
     * Die Katalogdatei wird dabei nicht angefasst und kein Commit erzeugt
     * (REQ_KAT_ABL_03).
     */
    @Transactional
    public Katalogantwort archiviere(SchulungId id) {
        verlangeImKatalog(id);
        zustaende.archiviere(id, LocalDate.now(uhr));
        List<String> bewerber = jdbcTemplate.queryForList("""
                SELECT benutzerkonto_id FROM qualifikationsbewerbung
                WHERE schulung_id = ? AND status = 'OFFEN'
                """, String.class, id.wert());
        jdbcTemplate.update("""
                UPDATE qualifikationsbewerbung SET status = 'ABGELEHNT'
                WHERE schulung_id = ? AND status = 'OFFEN'
                """, id.wert());
        bewerber.forEach(kontoId -> benachrichtigungen.persoenlich(kontoId, null,
                Benachrichtigungsanlass.QUALIFIKATION_DURCH_ARCHIVIERUNG_ENTFALLEN,
                "Qualifikationsbewerbung für " + id + " abgelehnt", "SCHULUNG", id.wert()));
        return Katalogantwort.ohneWarnung(ansicht.findeSchulung(id).orElseThrow());
    }

    /** Setzt eine archivierte Schulung zurueck auf aktiv (REQ_KAT_ARCH_04). */
    @Transactional
    public Katalogantwort reaktiviere(SchulungId id) {
        verlangeImKatalog(id);
        zustaende.reaktiviere(id);
        return Katalogantwort.ohneWarnung(ansicht.findeSchulung(id).orElseThrow());
    }

    /**
     * Loescht eine Schulung aus dem Katalog.
     *
     * <p>Zulaessig ist das in genau zwei Faellen (REQ_KAT_LOE_03): wenn zu ihr
     * kein Termin besteht (REQ_KAT_LOE_01) oder wenn sie seit mindestens
     * sechs Monaten archiviert ist (REQ_KAT_LOE_02).
     */
    @Transactional
    public void loesche(SchulungId id) {
        Katalogschulung schulung = katalog.lade(id)
                .orElseThrow(() -> new SchulungNichtGefunden(id));
        verlangeLoeschbar(id, schulung);

        // REQ_KAT_LOE_04: Der Titel bleibt bei den Terminen als Text stehen.
        // Ohne ihn zeigten sie danach auf eine Schulung, die es nicht mehr
        // gibt -- genau der Fall, den REQ_KAT_TERM_02 als Fehler meldet.
        jdbcTemplate.update(
                "UPDATE termin SET schulung_titel = ? WHERE schulung_id = ?",
                schulung.titel(), id.wert());
        // Eine Qualifikation fuer eine geloeschte Schulung hat keinen
        // Gegenstand mehr.
        jdbcTemplate.update(
                "DELETE FROM trainer_qualifikation WHERE schulung_id = ?", id.wert());
        jdbcTemplate.update(
                "DELETE FROM qualifikationsbewerbung WHERE schulung_id = ?", id.wert());
        zustaende.entferne(id);

        Path datei = katalog.dateiFuer(id);
        katalog.loesche(id);
        committer.sichere("Schulung " + id + " gelöscht", List.of(datei));
    }

    private void verlangeLoeschbar(SchulungId id, Katalogschulung schulung) {
        if (zaehleTermine(id) == 0) {
            return;
        }
        boolean langeGenugArchiviert = zustaende.lade(id)
                .filter(eintrag -> eintrag.istArchiviert())
                .map(eintrag -> eintrag.archiviertAm() != null
                        && !eintrag.archiviertAm().isAfter(
                                LocalDate.now(uhr).minusMonths(AUFBEWAHRUNG_MONATE)))
                .orElse(false);
        if (langeGenugArchiviert) {
            return;
        }
        throw KatalogFehler.konflikt(new Feldfehler("id",
                Fehlercode.SCHULUNG_NICHT_LOESCHBAR,
                "Zur Schulung '" + schulung.id() + "' bestehen Termine. Löschen ist nur "
                        + "möglich, solange kein Termin besteht, oder wenn die Schulung "
                        + "seit mindestens sechs Monaten archiviert ist. Eine aktive "
                        + "Schulung mit Terminen wird archiviert, nicht gelöscht."));
    }

    private void verlangeImKatalog(SchulungId id) {
        if (!katalog.existiert(id)) {
            throw new SchulungNichtGefunden(id);
        }
    }

    private int zaehleTermine(SchulungId id) {
        Integer anzahl = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM termin WHERE schulung_id = ?", Integer.class, id.wert());
        return anzahl == null ? 0 : anzahl;
    }

    private Katalogschulung pruefe(Schulungseingabe eingabe) {
        Pruefergebnis ergebnis = SchulungPruefung.pruefe(eingabe, kategorien.ladeAlle());
        return switch (ergebnis) {
            case Pruefergebnis.Angenommen angenommen -> angenommen.schulung();
            case Pruefergebnis.Abgewiesen abgewiesen ->
                    throw KatalogFehler.abgewiesen(abgewiesen.fehler());
        };
    }
}
