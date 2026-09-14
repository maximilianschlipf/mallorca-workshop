package de.nordwind.schulungsplaner.katalog;

import de.nordwind.schulungsplaner.domain.Schulung;
import de.nordwind.schulungsplaner.katalog.ablage.KatalogCommitter;
import de.nordwind.schulungsplaner.katalog.ablage.KatalogRepository;
import de.nordwind.schulungsplaner.katalog.ablage.KategorienRepository;
import de.nordwind.schulungsplaner.katalog.zustand.SchulungszustandRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
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

    public KatalogPflegeService(KatalogRepository katalog,
                                KategorienRepository kategorien,
                                SchulungszustandRepository zustaende,
                                KatalogCommitter committer,
                                KatalogAnsichtService ansicht,
                                JdbcTemplate jdbcTemplate) {
        this.katalog = katalog;
        this.kategorien = kategorien;
        this.zustaende = zustaende;
        this.committer = committer;
        this.ansicht = ansicht;
        this.jdbcTemplate = jdbcTemplate;
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
                    "Die Kennung einer Schulung kann nach dem Anlegen nicht geaendert werden. "
                            + "Sie benennt die Katalogdatei und bindet die Termine an.")));
        }

        Katalogschulung geaendert = pruefe(eingabe.mitId(id.wert()));
        List<Warnung> warnungen = warneBeiGeaenderterDauer(bisher, geaendert);

        Path datei = katalog.speichere(geaendert);
        committer.sichere("Schulung " + id + " geaendert", List.of(datei));

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
                            + geaendert.dauerInTagen() + " Tage geaendert. Zu dieser Schulung "
                            + "bestehen bereits " + termine + " Termine; sie behalten ihren "
                            + "Zeitraum. Die neue Dauer gilt fuer neu angelegte Termine."));
        }
        return warnungen;
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
