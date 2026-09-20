package de.nordwind.schulungsplaner;

import de.nordwind.schulungsplaner.domain.Benutzerkonto;
import de.nordwind.schulungsplaner.service.DashboardService;
import de.nordwind.schulungsplaner.service.KontoFehler;
import de.nordwind.schulungsplaner.service.KontoService;
import de.nordwind.schulungsplaner.service.TrainereinsatzService;
import de.nordwind.schulungsplaner.service.TerminService;
import de.nordwind.schulungsplaner.service.VorgangService;
import de.nordwind.schulungsplaner.service.Vorgangsart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "app.demo-seed=false",
        "spring.datasource.url=jdbc:h2:mem:dashboardvorgaenge;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"
})
@Transactional
class DashboardVorgaengeIntegrationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired KontoService konten;
    @Autowired VorgangService vorgaenge;
    @Autowired DashboardService dashboard;
    @Autowired TrainereinsatzService einsaetze;
    @Autowired TerminService termine;

    Benutzerkonto admin;
    Benutzerkonto antragsteller;
    Benutzerkonto trainer;
    Benutzerkonto ersatz;

    @BeforeEach
    void vorbereiten() {
        jdbc.update("DELETE FROM benachrichtigung");
        jdbc.update("DELETE FROM vorgang");
        jdbc.update("DELETE FROM assistenzbewerbung");
        jdbc.update("DELETE FROM termin_assistent");
        jdbc.update("DELETE FROM qualifikationsbewerbung");
        jdbc.update("DELETE FROM trainer_qualifikation");
        jdbc.update("DELETE FROM termin");
        jdbc.update("UPDATE instanz SET eigentuemer_id=NULL WHERE id=1");
        jdbc.update("DELETE FROM benutzerkonto_rolle");
        jdbc.update("DELETE FROM benutzerkonto");
        admin = konto("Admin", "admin@example.de", true);
        antragsteller = konto("Antragsteller", "antrag@example.de", false);
        trainer = konto("Trainer", "trainer@example.de", false);
        ersatz = konto("Ersatz", "ersatz@example.de", false);
        jdbc.update("MERGE INTO schulung_zustand (schulung_id, zustand, version) KEY(schulung_id) VALUES ('SCH-001','AKTIV',0)");
    }

    // verifies: TEST_DSH_VORG_01
    @Test
    void alleSechsVorgangsartenErscheinenNurBeiIhrenZustaendigenMitPflichtangaben() {
        einsaetze.aufQualifikationBewerben(antragsteller.id(), "SCH-001");
        long abwesenheit = anlegen(Vorgangsart.ABWESENHEITSANTRAG, null, true, "VORGANG", "11", "1. bis 2. Oktober");
        long vormerkung = anlegen(Vorgangsart.VORMERKUNG, null, true, "TERMIN", "T-VOR", "Scrum am 1. Oktober");
        long assistenz = anlegen(Vorgangsart.ASSISTENZBEWERBUNG, trainer.id(), true, "TERMIN", "T-ASS", "Scrum am 2. Oktober");
        long uebernahme = anlegen(Vorgangsart.UEBERNAHMEANFRAGE, trainer.id(), false, "TERMIN", "T-UEB", "Scrum am 3. Oktober");
        long ersatzanfrage = anlegen(Vorgangsart.ERSATZTRAINER_ANFRAGE, ersatz.id(), false, "VORGANG", "4/5", "4. bis 5. Oktober");

        assertThat(dashboard.anzeigen(admin.id()).vorgaenge()).extracting(DashboardService.Vorgang::artCode)
                .containsExactlyInAnyOrder("QUALIFIKATION", "ABWESENHEITSANTRAG", "VORMERKUNG", "ASSISTENZBEWERBUNG");
        assertThat(dashboard.anzeigen(trainer.id()).vorgaenge()).extracting(DashboardService.Vorgang::id)
                .containsExactlyInAnyOrder(assistenz, uebernahme);
        assertThat(dashboard.anzeigen(ersatz.id()).vorgaenge()).extracting(DashboardService.Vorgang::id)
                .containsExactly(ersatzanfrage);
        assertThat(dashboard.anzeigen(antragsteller.id()).vorgaenge()).isEmpty();
        assertThat(dashboard.anzeigen(admin.id()).vorgaenge()).allSatisfy(v -> {
            assertThat(v.art()).isNotBlank();
            assertThat(v.antragsteller()).isEqualTo("Antragsteller");
            assertThat(v.bezug()).isNotBlank();
            assertThat(v.erstelltAm()).isNotNull();
            assertThat(v.entscheidbar()).isTrue();
            assertThat(v.bezugArt()).isIn("TERMIN", "SCHULUNG", "VORGANG");
        });
        assertThat(abwesenheit).isPositive();
        assertThat(vormerkung).isPositive();
    }

    // verifies: TEST_DSH_VORG_02
    @Test
    void vormerkungWirdGenauEinmalEntschiedenUndWeistDenTrainerZu() {
        termin("T-VOR", null);
        long id = anlegen(Vorgangsart.VORMERKUNG, null, true, "TERMIN", "T-VOR", "Scrum");

        vorgaenge.entscheiden(admin.id(), id, true, null);

        assertThat(jdbc.queryForObject("SELECT trainer_id FROM termin WHERE termin_id='T-VOR'", String.class))
                .isEqualTo(antragsteller.id());
        assertThat(dashboard.anzeigen(admin.id()).vorgaenge()).isEmpty();
        assertThat(dashboard.anzeigen(admin.id()).erledigteVorgaenge()).singleElement()
                .satisfies(v -> assertThat(v.status()).isEqualTo("ANGENOMMEN"));
        assertThatThrownBy(() -> vorgaenge.entscheiden(admin.id(), id, true, null))
                .isInstanceOf(KontoFehler.class);
        assertThatThrownBy(() -> vorgaenge.entscheiden(admin.id(), id, false, "anders"))
                .isInstanceOf(KontoFehler.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM vorgang WHERE id=?", Integer.class, id)).isOne();
    }

    // verifies: TEST_DSH_VORG_05
    @Test
    void assistenzbewerbungKannTrainerOderAdminAberNurEinmalEntscheiden() {
        termin("T-ASS", trainer.id());
        long id = anlegen(Vorgangsart.ASSISTENZBEWERBUNG, trainer.id(), true,
                "TERMIN", "T-ASS", "Scrum");
        assertThat(dashboard.anzeigen(trainer.id()).vorgaenge()).extracting(DashboardService.Vorgang::id).contains(id);
        assertThat(dashboard.anzeigen(admin.id()).vorgaenge()).extracting(DashboardService.Vorgang::id).contains(id);

        vorgaenge.entscheiden(trainer.id(), id, false, null);

        assertThat(dashboard.anzeigen(trainer.id()).erledigteVorgaenge()).singleElement()
                .satisfies(v -> assertThat(v.entschiedenVon()).isEqualTo("Trainer"));
        assertThat(dashboard.anzeigen(admin.id()).vorgaenge()).isEmpty();
        assertThatThrownBy(() -> vorgaenge.entscheiden(admin.id(), id, true, null))
                .isInstanceOf(KontoFehler.class);

        long nurAdmin = anlegen(Vorgangsart.ASSISTENZBEWERBUNG, null, true,
                "TERMIN", "T-OHNE", "Scrum");
        assertThat(dashboard.anzeigen(admin.id()).vorgaenge()).extracting(DashboardService.Vorgang::id).contains(nurAdmin);
        assertThat(dashboard.anzeigen(trainer.id()).vorgaenge()).extracting(DashboardService.Vorgang::id).doesNotContain(nurAdmin);
    }

    // verifies: TEST_DSH_VORG_04
    @Test
    void nurQualifikationUndVormerkungVerlangenBeiAblehnungEineBegruendung() {
        einsaetze.aufQualifikationBewerben(antragsteller.id(), "SCH-001");
        long qualifikation = jdbc.queryForObject("SELECT id FROM qualifikationsbewerbung", Long.class);
        assertThatThrownBy(() -> einsaetze.bewerbungAblehnen(admin.id(), qualifikation, ""))
                .isInstanceOf(KontoFehler.class);
        einsaetze.bewerbungAblehnen(admin.id(), qualifikation, "Praxisnachweis fehlt");

        long vormerkung = anlegen(Vorgangsart.VORMERKUNG, null, true,
                "TERMIN", "T-VOR-AB", "Scrum");
        assertThatThrownBy(() -> vorgaenge.entscheiden(admin.id(), vormerkung, false, ""))
                .isInstanceOf(KontoFehler.class);
        vorgaenge.entscheiden(admin.id(), vormerkung, false, "Termin bereits anderweitig geplant");

        for (Vorgangsart art : new Vorgangsart[]{Vorgangsart.ABWESENHEITSANTRAG,
                Vorgangsart.ASSISTENZBEWERBUNG, Vorgangsart.UEBERNAHMEANFRAGE,
                Vorgangsart.ERSATZTRAINER_ANFRAGE}) {
            String zustaendig = art == Vorgangsart.UEBERNAHMEANFRAGE
                    || art == Vorgangsart.ERSATZTRAINER_ANFRAGE ? trainer.id() : null;
            long id = anlegen(art, zustaendig, zustaendig == null,
                    art == Vorgangsart.ASSISTENZBEWERBUNG || art == Vorgangsart.UEBERNAHMEANFRAGE
                            ? "TERMIN" : "VORGANG", "ABLEHNEN-" + art, "Bezug");
            vorgaenge.entscheiden(zustaendig == null ? admin.id() : trainer.id(), id, false, null);
            assertThat(jdbc.queryForObject("SELECT status FROM vorgang WHERE id=?", String.class, id))
                    .isEqualTo("ABGELEHNT");
        }
        assertThat(jdbc.queryForList("SELECT anlass FROM benachrichtigung WHERE empfaenger_id=?",
                String.class, antragsteller.id()))
                .anySatisfy(text -> assertThat(text).contains("Praxisnachweis fehlt"))
                .anySatisfy(text -> assertThat(text).contains("Termin bereits anderweitig geplant"));

        Benutzerkonto zweiter = konto("Zweiter", "zweiter@example.de", false);
        einsaetze.aufQualifikationBewerben(zweiter.id(), "SCH-001");
        long zweiteQualifikation = jdbc.queryForObject("""
                SELECT id FROM qualifikationsbewerbung WHERE benutzerkonto_id=?
                """, Long.class, zweiter.id());
        einsaetze.bewerbungGenehmigen(admin.id(), zweiteQualifikation);
        assertThat(jdbc.queryForObject("SELECT status FROM qualifikationsbewerbung WHERE id=?",
                String.class, zweiteQualifikation)).isEqualTo("GENEHMIGT");

        termin("T-VOR-AN", null);
        termin("T-ASS-AN", trainer.id());
        termin("T-UEB-AN", trainer.id());
        long vormerkungAn = anlegen(Vorgangsart.VORMERKUNG, null, true, "TERMIN", "T-VOR-AN", "Scrum");
        long assistenzAn = anlegen(Vorgangsart.ASSISTENZBEWERBUNG, trainer.id(), true, "TERMIN", "T-ASS-AN", "Scrum");
        long uebernahmeAn = anlegen(Vorgangsart.UEBERNAHMEANFRAGE, trainer.id(), false, "TERMIN", "T-UEB-AN", "Scrum");
        long ersatzAn = anlegen(Vorgangsart.ERSATZTRAINER_ANFRAGE, ersatz.id(), false, "VORGANG", "ERSATZ-AN", "Zeitraum");
        jdbc.update("INSERT INTO abwesenheit (id, benutzerkonto_id, von, bis, status) VALUES (999, ?, '2026-10-01', '2026-10-02', 'OFFEN')",
                antragsteller.id());
        vorgaenge.anlegen(Vorgangsart.ABWESENHEITSANTRAG, antragsteller.id(), null, true,
                "VORGANG", "999", "Zeitraum", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 2));
        long abwesenheitAn = jdbc.queryForObject("SELECT MAX(id) FROM vorgang", Long.class);
        vorgaenge.entscheiden(admin.id(), vormerkungAn, true, null);
        vorgaenge.entscheiden(trainer.id(), assistenzAn, true, null);
        vorgaenge.entscheiden(trainer.id(), uebernahmeAn, true, null);
        vorgaenge.entscheiden(ersatz.id(), ersatzAn, true, null);
        vorgaenge.entscheiden(admin.id(), abwesenheitAn, true, null);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM vorgang WHERE status='ANGENOMMEN'",
                Integer.class)).isEqualTo(5);
    }

    // verifies: TEST_NAC_DAT_01
    @Test
    void mitteilungenSpeichernAnlasstypBezugZeitpunktLesezustandTextUndBegruendung() {
        termin("T-ABSAGE", trainer.id());
        termine.absagen(admin.id(), "T-ABSAGE", "Kunde verhindert");

        einsaetze.aufQualifikationBewerben(antragsteller.id(), "SCH-001");
        long qualifikation = jdbc.queryForObject("SELECT id FROM qualifikationsbewerbung", Long.class);
        einsaetze.bewerbungAblehnen(admin.id(), qualifikation, "Praxisnachweis fehlt");

        long abwesenheit = anlegen(Vorgangsart.ABWESENHEITSANTRAG, null, true,
                "VORGANG", "ABW-77", "1. bis 2. Oktober");
        vorgaenge.entscheiden(admin.id(), abwesenheit, false, null);

        assertThat(jdbc.queryForMap("""
                SELECT anlasstyp, bezug_art, bezug_id, erstellt_am, gelesen, anlass
                FROM benachrichtigung WHERE anlasstyp='TERMIN_ABGESAGT'
                """))
                .containsEntry("ANLASSTYP", "TERMIN_ABGESAGT")
                .containsEntry("BEZUG_ART", "TERMIN")
                .containsEntry("BEZUG_ID", "T-ABSAGE")
                .containsEntry("GELESEN", false)
                .satisfies(zeile -> {
                    assertThat(zeile.get("ERSTELLT_AM")).isNotNull();
                    assertThat(zeile.get("ANLASS").toString()).contains("Kunde verhindert");
                });
        assertThat(jdbc.queryForMap("""
                SELECT bezug_art, bezug_id, anlass FROM benachrichtigung
                WHERE anlasstyp='QUALIFIKATION_ABGELEHNT'
                """))
                .containsEntry("BEZUG_ART", "SCHULUNG")
                .containsEntry("BEZUG_ID", "SCH-001")
                .satisfies(zeile -> assertThat(zeile.get("ANLASS").toString())
                        .contains("Praxisnachweis fehlt"));
        assertThat(jdbc.queryForMap("""
                SELECT bezug_art, bezug_id FROM benachrichtigung
                WHERE anlasstyp='ABWESENHEITSANTRAG_ABGELEHNT'
                """))
                .containsEntry("BEZUG_ART", "VORGANG")
                .containsEntry("BEZUG_ID", String.valueOf(abwesenheit));
    }

    private long anlegen(Vorgangsart art, String zustaendig, boolean adminZustaendig,
                         String bezugArt, String bezugId, String bezug) {
        vorgaenge.anlegen(art, antragsteller.id(), zustaendig, adminZustaendig,
                bezugArt, bezugId, bezug, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 2));
        return jdbc.queryForObject("SELECT MAX(id) FROM vorgang", Long.class);
    }

    private Benutzerkonto konto(String name, String email, boolean istAdmin) {
        Benutzerkonto konto = konten.registrieren(name, email, "pw");
        assertThat(konto.rollen().stream().anyMatch(rolle -> rolle.name().equals("ADMINISTRATOR")))
                .isEqualTo(istAdmin);
        return konten.laden(konto.id());
    }

    private void termin(String id, String trainerId) {
        jdbc.update("""
                INSERT INTO termin (termin_id, schulung_id, startdatum, enddatum, status, trainer_id)
                VALUES (?, 'SCH-001', '2026-10-01', '2026-10-02', 'geplant', ?)
                """, id, trainerId);
    }
}
