package de.nordwind.schulungsplaner;

import de.nordwind.schulungsplaner.domain.Benutzerkonto;
import de.nordwind.schulungsplaner.domain.Rolle;
import de.nordwind.schulungsplaner.katalog.KatalogPflegeService;
import de.nordwind.schulungsplaner.katalog.SchulungId;
import de.nordwind.schulungsplaner.service.BenachrichtigungService;
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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.demo-seed=false",
        "spring.datasource.url=jdbc:h2:mem:dashboardvorgaenge;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"
})
@Transactional
@AutoConfigureMockMvc
class DashboardVorgaengeIntegrationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired KontoService konten;
    @Autowired VorgangService vorgaenge;
    @Autowired DashboardService dashboard;
    @Autowired TrainereinsatzService einsaetze;
    @Autowired TerminService termine;
    @Autowired BenachrichtigungService benachrichtigungen;
    @Autowired KatalogPflegeService katalogpflege;
    @Autowired MockMvc mvc;

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
        jdbc.update("INSERT INTO trainer_qualifikation (benutzerkonto_id, schulung_id) VALUES (?, 'SCH-001')",
                ersatz.id());
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

    // verifies: TEST_NAC_ANL_04
    @Test
    void ausloeserWerdenNurBeiPersoenlichenMitteilungenAusgeschlossen() {
        termin("T-EIGENE-ABSAGE", admin.id());
        jdbc.update("INSERT INTO termin_assistent (termin_id, benutzerkonto_id, platz) VALUES (?, ?, 1)",
                "T-EIGENE-ABSAGE", trainer.id());

        termine.absagen(admin.id(), "T-EIGENE-ABSAGE", null);

        assertThat(jdbc.queryForList("""
                SELECT empfaenger_id FROM benachrichtigung
                WHERE anlasstyp='TERMIN_ABGESAGT'
                """, String.class)).containsExactly(trainer.id());

        jdbc.update("DELETE FROM benachrichtigung");
        Benutzerkonto zweiterAdmin = konto("Zweiter Admin", "zweiter-admin@example.de", false);
        konten.rolleErteilen(admin.id(), zweiterAdmin.id(), Rolle.ADMINISTRATOR,
                zweiterAdmin.aenderungsstand());
        jdbc.update("INSERT INTO trainer_qualifikation (benutzerkonto_id, schulung_id) VALUES (?, 'SCH-001')",
                admin.id());

        einsaetze.eigeneQualifikationAblegen(admin.id(), "SCH-001");

        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM benachrichtigung
                WHERE anlasstyp='QUALIFIKATION_ABGELEGT' AND empfaenger_rolle='ADMINISTRATOR'
                """, Integer.class)).isOne();
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM benachrichtigung
                WHERE anlasstyp='QUALIFIKATION_ABGELEGT' AND empfaenger_id=?
                """, Integer.class, admin.id())).isZero();
        assertThat(benachrichtigungen.anzeigen(admin.id())).extracting(BenachrichtigungService.Benachrichtigung::anlasstyp)
                .contains("QUALIFIKATION_ABGELEGT");
        assertThat(benachrichtigungen.anzeigen(zweiterAdmin.id())).extracting(BenachrichtigungService.Benachrichtigung::anlasstyp)
                .contains("QUALIFIKATION_ABGELEGT");

        jdbc.update("DELETE FROM benachrichtigung");
        termin("T-ERSATZ-UNGUELTIG", antragsteller.id());
        long ersatzanfrage = anlegen(Vorgangsart.ERSATZTRAINER_ANFRAGE, ersatz.id(), false,
                "VORGANG", "ERSATZ-UNGUELTIG", "1. bis 2. Oktober");

        vorgaenge.entscheiden(ersatz.id(), ersatzanfrage, true, null);

        assertThat(jdbc.queryForObject("SELECT status FROM vorgang WHERE id=?", String.class, ersatzanfrage))
                .isEqualTo("UNGUELTIG");
        assertThat(jdbc.queryForObject("SELECT trainer_id FROM termin WHERE termin_id='T-ERSATZ-UNGUELTIG'",
                String.class)).isEqualTo(antragsteller.id());
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM vorgang WHERE art='ABWESENHEITSANTRAG'
                AND antragsteller_id=? AND status='OFFEN'
                """, Integer.class, antragsteller.id())).isOne();
        assertThat(jdbc.queryForList("""
                SELECT empfaenger_id FROM benachrichtigung
                WHERE anlasstyp='ERSATZTRAINER_ANFRAGE_BEENDET'
                ORDER BY empfaenger_id
                """, String.class)).containsExactlyInAnyOrder(antragsteller.id(), ersatz.id());
        assertThat(jdbc.queryForList("""
                SELECT anlass FROM benachrichtigung
                WHERE anlasstyp='ERSATZTRAINER_ANFRAGE_BEENDET'
                """, String.class)).hasSize(2).allSatisfy(text -> assertThat(text).contains("Qualifikation fehlt"));
    }

    // verifies: TEST_DSH_VORG_07, TEST_DSH_VORG_08, TEST_NAC_ANL_01
    @Test
    void alleFachlichenEreignisseBeendenOderReduzierenOffeneVorgaengeNachvollziehbar() {
        alleEntscheidungsmitteilungenPruefen();
        restlicheKataloganlaessePruefen();
        jdbc.update("INSERT INTO trainer_qualifikation (benutzerkonto_id, schulung_id) VALUES (?, 'SCH-001')",
                trainer.id());
        termin("T-ZUWEISUNG", null);
        long vormerkung = anlegen(Vorgangsart.VORMERKUNG, null, true,
                "TERMIN", "T-ZUWEISUNG", "Termin T-ZUWEISUNG");
        termine.trainerZuweisen(admin.id(), "T-ZUWEISUNG", trainer.id(), false);
        entfallenOhneEntscheider(vormerkung, "Zuweisung");
        assertEineVorgangsmitteilung(vormerkung, antragsteller.id(),
                "VORMERKUNG_DURCH_ZUWEISUNG_ENTFALLEN", "Zuweisung");

        termin("T-TAUSCH", trainer.id());
        long uebernahme = anlegen(Vorgangsart.UEBERNAHMEANFRAGE, trainer.id(), false,
                "TERMIN", "T-TAUSCH", "Termin T-TAUSCH");
        Benutzerkonto anderer = konto("Anderer", "anderer@example.de", false);
        long andereUebernahme = anlegenFuer(anderer, Vorgangsart.UEBERNAHMEANFRAGE,
                trainer.id(), false, "TERMIN", "T-TAUSCH", "Termin T-TAUSCH");
        vorgaenge.entscheiden(trainer.id(), uebernahme, true, null);
        entfallenOhneEntscheider(andereUebernahme, "Tausch");
        assertEineVorgangsmitteilung(andereUebernahme, anderer.id(),
                "UEBERNAHMEANFRAGE_DURCH_TAUSCH_ENTFALLEN", "Tausch");

        Benutzerkonto fristTrainer = konto("Frist Trainer", "frist@example.de", false);
        jdbc.update("""
                INSERT INTO abwesenheit (benutzerkonto_id, von, bis, status)
                VALUES (?, '2026-09-27', '2026-09-28', 'OFFEN')
                """, fristTrainer.id());
        long abwesenheitId = jdbc.queryForObject("SELECT MAX(id) FROM abwesenheit", Long.class);
        long abwesenheit = anlegenFuer(fristTrainer, Vorgangsart.ABWESENHEITSANTRAG,
                null, true, "VORGANG", String.valueOf(abwesenheitId), "27. bis 28. September");
        jdbc.update("UPDATE vorgang SET von='2026-09-27', bis='2026-09-28' WHERE id=?", abwesenheit);
        long ersatzFrist = anlegenFuer(fristTrainer, Vorgangsart.ERSATZTRAINER_ANFRAGE,
                ersatz.id(), false, "VORGANG", "ERSATZ-FRIST", "1. bis 2. Oktober");
        jdbc.update("UPDATE vorgang SET erstellt_am='2026-09-12 10:00:00' WHERE id=?", ersatzFrist);
        vorgaenge.nachziehen();
        assertThat(jdbc.queryForObject("SELECT status FROM vorgang WHERE id=?", String.class, abwesenheit))
                .isEqualTo("ANGENOMMEN");
        assertEineVorgangsmitteilung(abwesenheit, fristTrainer.id(),
                "ABWESENHEITSANTRAG_NACH_FRIST_GENEHMIGT", "Fristablauf");
        entfallenOhneEntscheider(ersatzFrist, "Fristablauf");
        assertEineVorgangsmitteilung(ersatzFrist, fristTrainer.id(),
                "ERSATZTRAINER_ANFRAGE_BEENDET", "Fristablauf");
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM vorgang WHERE art='ABWESENHEITSANTRAG'
                AND antragsteller_id=? AND status='OFFEN'
                """, Integer.class, fristTrainer.id())).isOne();

        Benutzerkonto ohneQualifikation = konto("Ohne Qualifikation", "ohne-qual@example.de", false);
        termin("T-PRUEFUNG", fristTrainer.id());
        long fachlichUngueltig = anlegenFuer(fristTrainer, Vorgangsart.ERSATZTRAINER_ANFRAGE,
                ohneQualifikation.id(), false, "VORGANG", "ERSATZ-PRUEFUNG", "1. bis 2. Oktober");
        vorgaenge.entscheiden(ohneQualifikation.id(), fachlichUngueltig, true, null);
        assertThat(jdbc.queryForMap("SELECT status, begruendung, entschieden_von_id FROM vorgang WHERE id=?",
                fachlichUngueltig)).containsEntry("STATUS", "UNGUELTIG")
                .satisfies(row -> assertThat(row.get("BEGRUENDUNG").toString()).contains("Qualifikation"))
                .containsEntry("ENTSCHIEDEN_VON_ID", null);
        assertEineVorgangsmitteilung(fachlichUngueltig, fristTrainer.id(),
                "ERSATZTRAINER_ANFRAGE_BEENDET", "Qualifikation fehlt");
        assertEineVorgangsmitteilung(fachlichUngueltig, ohneQualifikation.id(),
                "ERSATZTRAINER_ANFRAGE_BEENDET", "Qualifikation fehlt");

        termin("T-ABSAGE-VORGAENGE", trainer.id());
        long v = anlegen(Vorgangsart.VORMERKUNG, null, true, "TERMIN", "T-ABSAGE-VORGAENGE", "Termin");
        long a = anlegen(Vorgangsart.ASSISTENZBEWERBUNG, trainer.id(), true, "TERMIN", "T-ABSAGE-VORGAENGE", "Termin");
        long u = anlegen(Vorgangsart.UEBERNAHMEANFRAGE, trainer.id(), false, "TERMIN", "T-ABSAGE-VORGAENGE", "Termin");
        termine.absagen(admin.id(), "T-ABSAGE-VORGAENGE", "Kunde");
        for (long id : new long[]{v, a, u}) {
            entfallenOhneEntscheider(id, "Terminabsage");
            assertEineVorgangsmitteilung(id, antragsteller.id(),
                    "VORGANG_DURCH_TERMINENDE_ANGEPASST_ODER_ENTFALLEN", "Terminabsage");
        }

        termin("T-LOESCH-VORGAENGE", trainer.id());
        long lv = anlegen(Vorgangsart.VORMERKUNG, null, true, "TERMIN", "T-LOESCH-VORGAENGE", "Termin");
        long la = anlegen(Vorgangsart.ASSISTENZBEWERBUNG, trainer.id(), true, "TERMIN", "T-LOESCH-VORGAENGE", "Termin");
        long lu = anlegen(Vorgangsart.UEBERNAHMEANFRAGE, trainer.id(), false, "TERMIN", "T-LOESCH-VORGAENGE", "Termin");
        termine.loeschen(admin.id(), "T-LOESCH-VORGAENGE");
        for (long id : new long[]{lv, la, lu}) {
            entfallenOhneEntscheider(id, "Terminlöschung");
            assertEineVorgangsmitteilung(id, antragsteller.id(),
                    "VORGANG_DURCH_TERMINENDE_ANGEPASST_ODER_ENTFALLEN", "Terminlöschung");
        }

        Benutzerkonto zeitraumTrainer = konto("Zeitraum Trainer", "zeitraum@example.de", false);
        terminFuer("T-ZEIT-1", zeitraumTrainer.id(), "2026-10-01", "2026-10-01");
        terminFuer("T-ZEIT-2", zeitraumTrainer.id(), "2026-10-02", "2026-10-02");
        jdbc.update("""
                INSERT INTO abwesenheit (benutzerkonto_id, von, bis, status)
                VALUES (?, '2026-10-01', '2026-10-02', 'OFFEN')
                """, zeitraumTrainer.id());
        long zeitraumAbwesenheitId = jdbc.queryForObject("SELECT MAX(id) FROM abwesenheit", Long.class);
        long zeitraumAntrag = anlegenFuer(zeitraumTrainer, Vorgangsart.ABWESENHEITSANTRAG,
                null, true, "VORGANG", String.valueOf(zeitraumAbwesenheitId), "1. bis 2. Oktober");
        termine.absagen(admin.id(), "T-ZEIT-1", null);
        assertStatus(zeitraumAntrag, "OFFEN");
        termine.loeschen(admin.id(), "T-ZEIT-2");
        entfallenOhneEntscheider(zeitraumAntrag, "Terminlöschung");
        assertZweiAnpassungsmitteilungen(zeitraumAntrag, zeitraumTrainer.id());
        assertThat(jdbc.queryForObject("SELECT status FROM abwesenheit WHERE id=?", String.class,
                zeitraumAbwesenheitId)).isEqualTo("AKTIV");

        Benutzerkonto ersatzZeitraum = konto("Ersatz Zeitraum", "ersatz-zeitraum@example.de", false);
        terminFuer("T-ERS-Z-1", ersatzZeitraum.id(), "2026-10-01", "2026-10-01");
        terminFuer("T-ERS-Z-2", ersatzZeitraum.id(), "2026-10-02", "2026-10-02");
        long ersatzZeitraumAnfrage = anlegenFuer(ersatzZeitraum, Vorgangsart.ERSATZTRAINER_ANFRAGE,
                ersatz.id(), false, "VORGANG", "ERSATZ-ZEITRAUM", "1. bis 2. Oktober");
        termine.loeschen(admin.id(), "T-ERS-Z-1");
        assertStatus(ersatzZeitraumAnfrage, "OFFEN");
        termine.loeschen(admin.id(), "T-ERS-Z-2");
        entfallenOhneEntscheider(ersatzZeitraumAnfrage, "Terminlöschung");
        assertZweiAnpassungsmitteilungen(ersatzZeitraumAnfrage, ersatzZeitraum.id());
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM abwesenheit WHERE benutzerkonto_id=? AND status='AKTIV'
                """, Integer.class, ersatzZeitraum.id())).isOne();

        Benutzerkonto adressat = konto("Adressat", "adressat@example.de", false);
        long adressierteUebernahme = anlegen(Vorgangsart.UEBERNAHMEANFRAGE, adressat.id(), false,
                "TERMIN", "KONTO-UEB", "Termin");
        long adressierterErsatz = anlegen(Vorgangsart.ERSATZTRAINER_ANFRAGE, adressat.id(), false,
                "VORGANG", "KONTO-ERS", "Zeitraum");
        konten.stilllegen(admin.id(), adressat.id(), adressat.aenderungsstand());
        entfallenOhneEntscheider(adressierteUebernahme, "Stilllegung");
        entfallenOhneEntscheider(adressierterErsatz, "Stilllegung");
        assertEineVorgangsmitteilung(adressierteUebernahme, antragsteller.id(),
                "VORGANG_DURCH_KONTOENDE_ENTFALLEN", "Stilllegung");
        assertEineVorgangsmitteilung(adressierterErsatz, antragsteller.id(),
                "VORGANG_DURCH_KONTOENDE_ENTFALLEN", "Stilllegung");
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM vorgang WHERE art='ABWESENHEITSANTRAG'
                AND antragsteller_id=? AND status='OFFEN'
                """, Integer.class, antragsteller.id())).isPositive();

        Benutzerkonto stillgelegt = konto("Stillgelegt", "stillgelegt@example.de", false);
        long[] stillgelegteVorgaenge = alleSechsVorgaenge(stillgelegt);
        konten.stilllegen(admin.id(), stillgelegt.id(), stillgelegt.aenderungsstand());
        assertAlleEntfallen(stillgelegteVorgaenge, "Stilllegung");
        assertThat(jdbc.queryForList("""
                SELECT anlasstyp FROM benachrichtigung WHERE empfaenger_id=?
                """, String.class, stillgelegt.id())).hasSize(6)
                .allMatch("VORGANG_DURCH_KONTOENDE_ENTFALLEN"::equals);

        Benutzerkonto selbstStillgelegt = konto("Selbst Stillgelegt", "selbst-still@example.de", false);
        konten.rolleErteilen(admin.id(), selbstStillgelegt.id(), Rolle.ADMINISTRATOR,
                selbstStillgelegt.aenderungsstand());
        selbstStillgelegt = konten.laden(selbstStillgelegt.id());
        long[] selbstVorgaenge = alleSechsVorgaenge(selbstStillgelegt);
        konten.stilllegen(selbstStillgelegt.id(), selbstStillgelegt.id(), selbstStillgelegt.aenderungsstand());
        assertAlleEntfallen(selbstVorgaenge, "Stilllegung");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM benachrichtigung WHERE empfaenger_id=?",
                Integer.class, selbstStillgelegt.id())).isZero();

        Benutzerkonto geloescht = konto("Gelöscht", "geloescht@example.de", false);
        long[] geloeschteVorgaenge = alleSechsVorgaenge(geloescht);
        konten.loeschen(admin.id(), geloescht.id(), geloescht.aenderungsstand());
        assertAlleEntfallen(geloeschteVorgaenge, "Löschung");
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM vorgang WHERE id IN (?,?,?,?,?) AND antragsteller_id IS NOT NULL
                """, Integer.class, geloeschteVorgaenge[1], geloeschteVorgaenge[2],
                geloeschteVorgaenge[3], geloeschteVorgaenge[4], geloeschteVorgaenge[5])).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM benachrichtigung WHERE empfaenger_id=?",
                Integer.class, geloescht.id())).isZero();

        Benutzerkonto geloeschterAdressat = konto("Gelöschter Adressat", "adressat-loesch@example.de", false);
        long adressatLoeschUebernahme = anlegen(Vorgangsart.UEBERNAHMEANFRAGE,
                geloeschterAdressat.id(), false, "TERMIN", "KONTO-LOE-UEB", "Termin");
        long adressatLoeschErsatz = anlegen(Vorgangsart.ERSATZTRAINER_ANFRAGE,
                geloeschterAdressat.id(), false, "VORGANG", "KONTO-LOE-ERS", "Zeitraum");
        konten.loeschen(admin.id(), geloeschterAdressat.id(), geloeschterAdressat.aenderungsstand());
        entfallenOhneEntscheider(adressatLoeschUebernahme, "Löschung");
        entfallenOhneEntscheider(adressatLoeschErsatz, "Löschung");
        assertEineVorgangsmitteilung(adressatLoeschUebernahme, antragsteller.id(),
                "VORGANG_DURCH_KONTOENDE_ENTFALLEN", "Löschung");
        assertEineVorgangsmitteilung(adressatLoeschErsatz, antragsteller.id(),
                "VORGANG_DURCH_KONTOENDE_ENTFALLEN", "Löschung");

        Benutzerkonto archivBewerber = konto("Archiv Bewerber", "archiv@example.de", false);
        einsaetze.aufQualifikationBewerben(archivBewerber.id(), "SCH-001");
        long archivBewerbung = jdbc.queryForObject("""
                SELECT id FROM qualifikationsbewerbung WHERE benutzerkonto_id=?
                """, Long.class, archivBewerber.id());
        katalogpflege.archiviere(SchulungId.von("SCH-001"));
        assertThat(jdbc.queryForMap("""
                SELECT status, begruendung, entschieden_am FROM qualifikationsbewerbung WHERE id=?
                """, archivBewerbung)).containsEntry("STATUS", "ABGELEHNT")
                .containsEntry("BEGRUENDUNG", "Schulung archiviert")
                .satisfies(row -> assertThat(row.get("ENTSCHIEDEN_AM")).isNotNull());
        assertNurAnlass(archivBewerber.id(), "QUALIFIKATION_DURCH_ARCHIVIERUNG_ENTFALLEN");
        assertThat(jdbc.queryForObject("SELECT anlass FROM benachrichtigung WHERE empfaenger_id=?",
                String.class, archivBewerber.id())).contains("SCH-001");
        assertThat(jdbc.queryForList("SELECT DISTINCT anlasstyp FROM benachrichtigung", String.class))
                .containsExactlyInAnyOrder(java.util.Arrays.stream(
                        de.nordwind.schulungsplaner.service.Benachrichtigungsanlass.values())
                        .map(Enum::name).toArray(String[]::new));
        assertThat(jdbc.queryForList("SELECT anlass FROM benachrichtigung", String.class))
                .allSatisfy(text -> assertThat(text).isNotBlank());
    }

    // verifies: TEST_NAC_ANL_06
    @Test
    void spezifischeAnlaesseVerdraengenAllgemeineZuweisungsmitteilungen() {
        jdbc.update("INSERT INTO trainer_qualifikation (benutzerkonto_id, schulung_id) VALUES (?, 'SCH-001')",
                antragsteller.id());
        jdbc.update("INSERT INTO trainer_qualifikation (benutzerkonto_id, schulung_id) VALUES (?, 'SCH-001')",
                ersatz.id());

        termin("T-SPEZ-VOR", null);
        long vormerkung = anlegen(Vorgangsart.VORMERKUNG, null, true,
                "TERMIN", "T-SPEZ-VOR", "Termin");
        vorgaenge.entscheiden(admin.id(), vormerkung, true, null);
        assertNurAnlass(antragsteller.id(), "VORMERKUNG_BESTAETIGT");

        jdbc.update("DELETE FROM benachrichtigung");
        termin("T-SPEZ-UEB", trainer.id());
        long uebernahme = anlegen(Vorgangsart.UEBERNAHMEANFRAGE, trainer.id(), false,
                "TERMIN", "T-SPEZ-UEB", "Termin");
        vorgaenge.entscheiden(trainer.id(), uebernahme, true, null);
        assertNurAnlass(antragsteller.id(), "UEBERNAHMEANFRAGE_ENTSCHIEDEN");
        assertKeineAllgemeineZuweisung(antragsteller.id());

        jdbc.update("DELETE FROM benachrichtigung");
        termin("T-SPEZ-ERS", antragsteller.id());
        long ersatzanfrage = anlegen(Vorgangsart.ERSATZTRAINER_ANFRAGE, ersatz.id(), false,
                "VORGANG", "T-SPEZ-ERSATZ", "1. bis 2. Oktober");
        vorgaenge.entscheiden(ersatz.id(), ersatzanfrage, true, null);
        assertNurAnlass(antragsteller.id(), "ERSATZTRAINER_ANFRAGE_BEENDET");
        assertKeineAllgemeineZuweisung(antragsteller.id());

        jdbc.update("DELETE FROM benachrichtigung");
        Benutzerkonto rollenwechsler = konto("Rollenwechsler", "rollenwechsel@example.de", false);
        jdbc.update("INSERT INTO trainer_qualifikation (benutzerkonto_id, schulung_id) VALUES (?, 'SCH-001')",
                rollenwechsler.id());
        termin("T-SPEZ-ROLLE", trainer.id());
        jdbc.update("INSERT INTO termin_assistent (termin_id, benutzerkonto_id, platz) VALUES (?, ?, 1)",
                "T-SPEZ-ROLLE", rollenwechsler.id());
        termine.trainerZuweisen(admin.id(), "T-SPEZ-ROLLE", rollenwechsler.id(), true);
        assertNurAnlass(rollenwechsler.id(), "ROLLENWECHSEL");
        assertKeineAllgemeineZuweisung(rollenwechsler.id());

        jdbc.update("DELETE FROM benachrichtigung");
        Benutzerkonto direkt = konto("Direkt", "direkt@example.de", false);
        einsaetze.aufQualifikationBewerben(direkt.id(), "SCH-001");
        einsaetze.direktQualifizieren(admin.id(), "SCH-001", direkt.id());
        assertNurAnlass(direkt.id(), "QUALIFIKATION_DIREKT");

        jdbc.update("DELETE FROM benachrichtigung");
        Benutzerkonto manuell = konto("Manuell Abwesend", "manuell-abw@example.de", false);
        termin("T-SPEZ-ABW-M", manuell.id());
        jdbc.update("""
                INSERT INTO abwesenheit (benutzerkonto_id, von, bis, status)
                VALUES (?, '2026-10-01', '2026-10-02', 'OFFEN')
                """, manuell.id());
        long manuellAbwesenheit = jdbc.queryForObject("SELECT MAX(id) FROM abwesenheit", Long.class);
        long manuellAntrag = anlegenFuer(manuell, Vorgangsart.ABWESENHEITSANTRAG, null, true,
                "VORGANG", String.valueOf(manuellAbwesenheit), "1. bis 2. Oktober");
        vorgaenge.entscheiden(admin.id(), manuellAntrag, true, null);
        assertNurAnlass(manuell.id(), "ABWESENHEITSANTRAG_MANUELL_GENEHMIGT");
        assertKeineAllgemeineZuweisung(manuell.id());

        jdbc.update("DELETE FROM benachrichtigung");
        Benutzerkonto automatisch = konto("Automatisch Abwesend", "auto-abw@example.de", false);
        terminFuer("T-SPEZ-ABW-A", automatisch.id(), "2026-09-27", "2026-09-28");
        jdbc.update("""
                INSERT INTO abwesenheit (benutzerkonto_id, von, bis, status)
                VALUES (?, '2026-09-27', '2026-09-28', 'OFFEN')
                """, automatisch.id());
        long autoAbwesenheit = jdbc.queryForObject("SELECT MAX(id) FROM abwesenheit", Long.class);
        long autoAntrag = anlegenFuer(automatisch, Vorgangsart.ABWESENHEITSANTRAG, null, true,
                "VORGANG", String.valueOf(autoAbwesenheit), "27. bis 28. September");
        jdbc.update("UPDATE vorgang SET von='2026-09-27', bis='2026-09-28' WHERE id=?", autoAntrag);
        vorgaenge.nachziehen();
        assertNurAnlass(automatisch.id(), "ABWESENHEITSANTRAG_NACH_FRIST_GENEHMIGT");
        assertKeineAllgemeineZuweisung(automatisch.id());

        jdbc.update("DELETE FROM benachrichtigung");
        Benutzerkonto direktZugewiesen = konto("Direkt Zugewiesen", "direkt-zugewiesen@example.de", false);
        jdbc.update("INSERT INTO trainer_qualifikation (benutzerkonto_id, schulung_id) VALUES (?, 'SCH-001')",
                direktZugewiesen.id());
        termin("T-ALLGEMEIN", null);
        termine.trainerZuweisen(admin.id(), "T-ALLGEMEIN", direktZugewiesen.id(), false);
        assertNurAnlass(direktZugewiesen.id(), "TRAINERZUWEISUNG_GESETZT");
        jdbc.update("DELETE FROM benachrichtigung");
        termine.trainerAbziehen(admin.id(), "T-ALLGEMEIN");
        assertNurAnlass(direktZugewiesen.id(), "TRAINERZUWEISUNG_BEENDET");
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

    // verifies: TEST_DSH_VORG_09
    @Test
    void dashboardUndVorgangsmutationenSindGegenFremdzugriffUndFehlendesCsrfGeschuetzt()
            throws Exception {
        long fremd = anlegen(Vorgangsart.VORMERKUNG, null, true,
                "TERMIN", "SICHER-1", "Sicherer Vorgang");
        long uebernahme = anlegen(Vorgangsart.UEBERNAHMEANFRAGE, trainer.id(), false,
                "TERMIN", "SICHER-2", "Übernahme");
        long ersatzanfrage = anlegen(Vorgangsart.ERSATZTRAINER_ANFRAGE, ersatz.id(), false,
                "VORGANG", "SICHER-3", "Ersatz");
        MockHttpSession adminSitzung = login(admin);
        MockHttpSession antragstellerSitzung = login(antragsteller);

        mvc.perform(get("/api/dashboard")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/dashboard/vorgaenge/{id}/annahme", fremd).with(csrf()))
                .andExpect(status().isUnauthorized());

        var fremdeAntwort = mvc.perform(post("/api/dashboard/vorgaenge/{id}/annahme", fremd)
                        .session(antragstellerSitzung).with(csrf()))
                .andExpect(status().isNotFound()).andReturn().getResponse();
        var fehlendeAntwort = mvc.perform(post("/api/dashboard/vorgaenge/{id}/annahme", 999999)
                        .session(antragstellerSitzung).with(csrf()))
                .andExpect(status().isNotFound()).andReturn().getResponse();
        assertThat(fremdeAntwort.getContentAsString()).isEqualTo(fehlendeAntwort.getContentAsString());

        mvc.perform(post("/api/dashboard/vorgaenge/{id}/annahme", uebernahme)
                        .session(adminSitzung).with(csrf()))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/dashboard/vorgaenge/{id}/ablehnung", ersatzanfrage)
                        .session(adminSitzung).with(csrf()).contentType("application/json")
                        .content("{}"))
                .andExpect(status().isNotFound());

        mvc.perform(post("/api/dashboard/vorgaenge/{id}/annahme", fremd).session(adminSitzung))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/dashboard/vorgaenge/{id}/ablehnung", fremd)
                        .session(adminSitzung).contentType("application/json").content("{\"begruendung\":\"x\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/dashboard/vorgaenge/{id}", fremd).session(antragstellerSitzung))
                .andExpect(status().isForbidden());
        assertThat(jdbc.queryForObject("SELECT status FROM vorgang WHERE id=?", String.class, fremd))
                .isEqualTo("OFFEN");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM benachrichtigung", Integer.class)).isZero();
    }

    private long anlegen(Vorgangsart art, String zustaendig, boolean adminZustaendig,
                         String bezugArt, String bezugId, String bezug) {
        return anlegenFuer(antragsteller, art, zustaendig, adminZustaendig, bezugArt, bezugId, bezug);
    }

    private long anlegenFuer(Benutzerkonto konto, Vorgangsart art, String zustaendig,
                             boolean adminZustaendig, String bezugArt, String bezugId, String bezug) {
        vorgaenge.anlegen(art, konto.id(), zustaendig, adminZustaendig,
                bezugArt, bezugId, bezug, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 2));
        return jdbc.queryForObject("SELECT MAX(id) FROM vorgang", Long.class);
    }

    private long[] alleSechsVorgaenge(Benutzerkonto konto) {
        einsaetze.aufQualifikationBewerben(konto.id(), "SCH-001");
        long qualifikation = jdbc.queryForObject("""
                SELECT id FROM qualifikationsbewerbung WHERE benutzerkonto_id=?
                """, Long.class, konto.id());
        return new long[]{qualifikation,
                anlegenFuer(konto, Vorgangsart.ABWESENHEITSANTRAG, null, true,
                        "VORGANG", "KONTO-ABW-" + konto.id(), "Zeitraum"),
                anlegenFuer(konto, Vorgangsart.VORMERKUNG, null, true,
                        "TERMIN", "KONTO-VOR-" + konto.id(), "Termin"),
                anlegenFuer(konto, Vorgangsart.ASSISTENZBEWERBUNG, trainer.id(), true,
                        "TERMIN", "KONTO-ASS-" + konto.id(), "Termin"),
                anlegenFuer(konto, Vorgangsart.UEBERNAHMEANFRAGE, trainer.id(), false,
                        "TERMIN", "KONTO-UEB-" + konto.id(), "Termin"),
                anlegenFuer(konto, Vorgangsart.ERSATZTRAINER_ANFRAGE, trainer.id(), false,
                        "VORGANG", "KONTO-ERS-" + konto.id(), "Zeitraum")};
    }

    private void assertAlleEntfallen(long[] ids, String grund) {
        assertThat(jdbc.queryForMap("""
                SELECT status, begruendung FROM qualifikationsbewerbung WHERE id=?
                """, ids[0])).containsEntry("STATUS", "ENTFALLEN")
                .satisfies(row -> assertThat(row.get("BEGRUENDUNG").toString()).contains(grund));
        assertThatThrownBy(() -> einsaetze.bewerbungGenehmigen(admin.id(), ids[0]))
                .isInstanceOf(RuntimeException.class);
        for (int index = 1; index < ids.length; index++) {
            entfallenOhneEntscheider(ids[index], grund);
            long id = ids[index];
            assertThatThrownBy(() -> vorgaenge.entscheiden(admin.id(), id, true, null))
                    .isInstanceOf(KontoFehler.class);
        }
    }

    private void entfallenOhneEntscheider(long id, String grund) {
        assertThat(jdbc.queryForMap("""
                SELECT status, begruendung, entschieden_am, entschieden_von_id
                FROM vorgang WHERE id=?
                """, id)).containsEntry("STATUS", "ENTFALLEN")
                .containsEntry("ENTSCHIEDEN_VON_ID", null)
                .satisfies(row -> {
                    assertThat(row.get("BEGRUENDUNG").toString()).containsIgnoringCase(grund);
                    assertThat(row.get("ENTSCHIEDEN_AM")).isNotNull();
                });
    }

    private void assertStatus(long id, String status) {
        assertThat(jdbc.queryForObject("SELECT status FROM vorgang WHERE id=?", String.class, id))
                .isEqualTo(status);
    }

    private void assertNurAnlass(String kontoId, String anlass) {
        assertThat(jdbc.queryForList("""
                SELECT anlasstyp FROM benachrichtigung WHERE empfaenger_id=? ORDER BY id
                """, String.class, kontoId)).containsExactly(anlass);
    }

    private void assertKeineAllgemeineZuweisung(String kontoId) {
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM benachrichtigung WHERE empfaenger_id=?
                AND anlasstyp IN ('TRAINERZUWEISUNG_GESETZT','TRAINERZUWEISUNG_BEENDET')
                """, Integer.class, kontoId)).isZero();
    }

    private void assertEineVorgangsmitteilung(long vorgangId, String kontoId,
                                              String anlass, String inhalt) {
        assertThat(jdbc.queryForList("""
                SELECT anlasstyp, anlass FROM benachrichtigung
                WHERE empfaenger_id=? AND bezug_art='VORGANG' AND bezug_id=?
                """, kontoId, String.valueOf(vorgangId))).singleElement().satisfies(zeile -> {
            assertThat(zeile.get("ANLASSTYP")).isEqualTo(anlass);
            assertThat(zeile.get("ANLASS").toString()).containsIgnoringCase(inhalt);
        });
    }

    private void assertZweiAnpassungsmitteilungen(long vorgangId, String kontoId) {
        assertThat(jdbc.queryForList("""
                SELECT anlasstyp, anlass FROM benachrichtigung
                WHERE empfaenger_id=? AND bezug_art='VORGANG' AND bezug_id=? ORDER BY id
                """, kontoId, String.valueOf(vorgangId))).hasSize(2).allSatisfy(zeile ->
                assertThat(zeile.get("ANLASSTYP"))
                        .isEqualTo("VORGANG_DURCH_TERMINENDE_ANGEPASST_ODER_ENTFALLEN"))
                .anySatisfy(zeile -> assertThat(zeile.get("ANLASS").toString()).contains("angepasst"))
                .anySatisfy(zeile -> assertThat(zeile.get("ANLASS").toString()).contains("entfallen"));
    }

    private void alleEntscheidungsmitteilungenPruefen() {
        for (boolean angenommen : new boolean[]{true, false}) {
            Benutzerkonto bewerber = konto("Qualifikation " + angenommen,
                    "qualifikation-" + angenommen + "@example.de", false);
            einsaetze.aufQualifikationBewerben(bewerber.id(), "SCH-001");
            long id = jdbc.queryForObject("""
                    SELECT id FROM qualifikationsbewerbung WHERE benutzerkonto_id=?
                    """, Long.class, bewerber.id());
            if (angenommen) einsaetze.bewerbungGenehmigen(admin.id(), id);
            else einsaetze.bewerbungAblehnen(admin.id(), id, "Praxisnachweis fehlt");
            assertNurAnlass(bewerber.id(), angenommen
                    ? "QUALIFIKATION_GENEHMIGT" : "QUALIFIKATION_ABGELEHNT");
            assertThat(jdbc.queryForObject("""
                    SELECT anlass FROM benachrichtigung WHERE empfaenger_id=?
                    """, String.class, bewerber.id())).contains("SCH-001")
                    .contains(angenommen ? "genehmigt" : "Praxisnachweis fehlt");
            assertThat(jdbc.queryForObject("""
                    SELECT COUNT(*) FROM benachrichtigung WHERE empfaenger_id=?
                    """, Integer.class, admin.id())).isZero();
        }
        int nummer = 0;
        for (Vorgangsart art : Vorgangsart.values()) {
            for (boolean angenommen : new boolean[]{true, false}) {
                pruefeVorgangsentscheidung(art, angenommen, ++nummer);
            }
        }

        jdbc.update("""
                INSERT INTO abwesenheit (benutzerkonto_id, von, bis, status)
                VALUES (?, '2026-10-01', '2026-10-02', 'OFFEN')
                """, admin.id());
        long abwesenheitId = jdbc.queryForObject("SELECT MAX(id) FROM abwesenheit", Long.class);
        long eigenerAntrag = anlegenFuer(admin, Vorgangsart.ABWESENHEITSANTRAG, null, true,
                "VORGANG", String.valueOf(abwesenheitId), "Eigene Abwesenheit");
        vorgaenge.entscheiden(admin.id(), eigenerAntrag, true, null);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM benachrichtigung WHERE empfaenger_id=?",
                Integer.class, admin.id())).isZero();
        jdbc.update("DELETE FROM termin WHERE termin_id LIKE 'ENTSCHEIDUNG-%'");
    }

    private void pruefeVorgangsentscheidung(Vorgangsart art, boolean angenommen, int nummer) {
        Benutzerkonto bewerber = konto("Vorgang " + nummer, "vorgang-" + nummer + "@example.de", false);
        String bezugId = "ENTSCHEIDUNG-" + nummer;
        String bezugArt = "TERMIN";
        String zustaendig = null;
        boolean adminZustaendig = true;
        String akteur = admin.id();
        if (art == Vorgangsart.ABWESENHEITSANTRAG) {
            jdbc.update("""
                    INSERT INTO abwesenheit (benutzerkonto_id, von, bis, status)
                    VALUES (?, '2026-10-01', '2026-10-02', 'OFFEN')
                    """, bewerber.id());
            bezugId = String.valueOf(jdbc.queryForObject("SELECT MAX(id) FROM abwesenheit", Long.class));
            bezugArt = "VORGANG";
        } else if (art == Vorgangsart.VORMERKUNG) {
            termin(bezugId, null);
        } else if (art == Vorgangsart.ASSISTENZBEWERBUNG) {
            termin(bezugId, trainer.id());
            zustaendig = trainer.id();
        } else if (art == Vorgangsart.UEBERNAHMEANFRAGE) {
            termin(bezugId, trainer.id());
            zustaendig = trainer.id();
            adminZustaendig = false;
            akteur = trainer.id();
        } else if (art == Vorgangsart.ERSATZTRAINER_ANFRAGE) {
            termin(bezugId, bewerber.id());
            jdbc.update("""
                    MERGE INTO trainer_qualifikation (benutzerkonto_id, schulung_id)
                    KEY(benutzerkonto_id, schulung_id) VALUES (?, 'SCH-001')
                    """, ersatz.id());
            zustaendig = ersatz.id();
            adminZustaendig = false;
            akteur = ersatz.id();
            bezugArt = "VORGANG";
        }
        long id = anlegenFuer(bewerber, art, zustaendig, adminZustaendig,
                bezugArt, bezugId, "Bezug " + nummer);
        String grund = !angenommen && art.ablehnungsgrundPflicht() ? "Fachlicher Grund" : null;
        vorgaenge.entscheiden(akteur, id, angenommen, grund);

        assertNurAnlass(bewerber.id(), switch (art) {
            case ABWESENHEITSANTRAG -> angenommen
                    ? "ABWESENHEITSANTRAG_MANUELL_GENEHMIGT" : "ABWESENHEITSANTRAG_ABGELEHNT";
            case VORMERKUNG -> angenommen ? "VORMERKUNG_BESTAETIGT" : "VORMERKUNG_ABGELEHNT";
            case ASSISTENZBEWERBUNG -> "ASSISTENZBEWERBUNG_ENTSCHIEDEN";
            case UEBERNAHMEANFRAGE -> "UEBERNAHMEANFRAGE_ENTSCHIEDEN";
            case ERSATZTRAINER_ANFRAGE -> "ERSATZTRAINER_ANFRAGE_BEENDET";
        });
        assertThat(jdbc.queryForObject("SELECT anlass FROM benachrichtigung WHERE empfaenger_id=?",
                String.class, bewerber.id())).contains(angenommen ? "angenommen" : "abgelehnt");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM benachrichtigung WHERE empfaenger_id=?",
                Integer.class, akteur)).isZero();
    }

    private void restlicheKataloganlaessePruefen() {
        Benutzerkonto direkt = konto("Direktzuweisung", "katalog-direkt@example.de", false);
        jdbc.update("INSERT INTO trainer_qualifikation (benutzerkonto_id, schulung_id) VALUES (?, 'SCH-001')",
                direkt.id());
        termin("KATALOG-ZUWEISUNG", null);
        termine.trainerZuweisen(admin.id(), "KATALOG-ZUWEISUNG", direkt.id(), false);
        assertPersoenlicherAnlass(direkt.id(), "TRAINERZUWEISUNG_GESETZT", "KATALOG-ZUWEISUNG");
        termine.trainerAbziehen(admin.id(), "KATALOG-ZUWEISUNG");
        assertPersoenlicherAnlass(direkt.id(), "TRAINERZUWEISUNG_BEENDET", "KATALOG-ZUWEISUNG");

        Benutzerkonto rollenwechsler = konto("Katalog Rollenwechsel", "katalog-rolle@example.de", false);
        jdbc.update("INSERT INTO trainer_qualifikation (benutzerkonto_id, schulung_id) VALUES (?, 'SCH-001')",
                rollenwechsler.id());
        termin("KATALOG-ROLLE", trainer.id());
        jdbc.update("INSERT INTO termin_assistent (termin_id, benutzerkonto_id, platz) VALUES (?, ?, 1)",
                "KATALOG-ROLLE", rollenwechsler.id());
        termine.trainerZuweisen(admin.id(), "KATALOG-ROLLE", rollenwechsler.id(), true);
        assertNurAnlass(rollenwechsler.id(), "ROLLENWECHSEL");
        assertThat(jdbc.queryForObject("SELECT anlass FROM benachrichtigung WHERE empfaenger_id=?",
                String.class, rollenwechsler.id())).contains("KATALOG-ROLLE").contains("Assistent");

        Benutzerkonto geaendertTrainer = konto("Geändert Trainer", "katalog-aenderung@example.de", false);
        Benutzerkonto geaendertAssistent = konto("Geändert Assistenz", "katalog-assistenz@example.de", false);
        termin("KATALOG-AENDERUNG", geaendertTrainer.id());
        jdbc.update("""
                UPDATE termin SET zugangsart='oeffentlich', durchfuehrungsart='vor_ort',
                    ort='Altstadt' WHERE termin_id='KATALOG-AENDERUNG'
                """);
        jdbc.update("INSERT INTO termin_assistent (termin_id, benutzerkonto_id, platz) VALUES (?, ?, 1)",
                "KATALOG-AENDERUNG", geaendertAssistent.id());
        termine.aendern(admin.id(), "KATALOG-AENDERUNG", new TerminService.TerminEingabe(
                null, null, null, "oeffentlich", "vor_ort", "Berlin", null, null, null, false));
        assertPersoenlicherAnlass(geaendertTrainer.id(), "TERMIN_GEAENDERT", "Berlin");
        assertPersoenlicherAnlass(geaendertAssistent.id(), "TERMIN_GEAENDERT", "Berlin");

        Benutzerkonto absageTrainer = konto("Absage Trainer", "katalog-absage@example.de", false);
        Benutzerkonto absageAssistent = konto("Absage Assistenz", "katalog-absage-ass@example.de", false);
        termin("KATALOG-ABSAGE", absageTrainer.id());
        jdbc.update("INSERT INTO termin_assistent (termin_id, benutzerkonto_id, platz) VALUES (?, ?, 1)",
                "KATALOG-ABSAGE", absageAssistent.id());
        termine.absagen(admin.id(), "KATALOG-ABSAGE", "Kunde verhindert");
        assertPersoenlicherAnlass(absageTrainer.id(), "TERMIN_ABGESAGT", "Kunde verhindert");
        assertPersoenlicherAnlass(absageAssistent.id(), "TERMIN_ABGESAGT", "Kunde verhindert");

        Benutzerkonto loeschTrainer = konto("Lösch Trainer", "katalog-loesch@example.de", false);
        Benutzerkonto loeschAssistent = konto("Lösch Assistenz", "katalog-loesch-ass@example.de", false);
        termin("KATALOG-LOESCHUNG", loeschTrainer.id());
        jdbc.update("INSERT INTO termin_assistent (termin_id, benutzerkonto_id, platz) VALUES (?, ?, 1)",
                "KATALOG-LOESCHUNG", loeschAssistent.id());
        termine.loeschen(admin.id(), "KATALOG-LOESCHUNG");
        assertPersoenlicherAnlass(loeschTrainer.id(), "TERMIN_GELOESCHT", "KATALOG-LOESCHUNG");
        assertPersoenlicherAnlass(loeschAssistent.id(), "TERMIN_GELOESCHT", "KATALOG-LOESCHUNG");

        Benutzerkonto automatisch = konto("Automatisch abgeschlossen", "katalog-auto@example.de", false);
        terminFuer("KATALOG-AUTO", automatisch.id(), "2026-07-01", "2026-07-01");
        termine.nachziehen();
        assertPersoenlicherAnlass(automatisch.id(), "TERMIN_AUTOMATISCH_ABGESCHLOSSEN",
                "Teilnehmerauswertungen");

        Benutzerkonto qualifiziert = konto("Direkt qualifiziert", "katalog-qual@example.de", false);
        einsaetze.direktQualifizieren(admin.id(), "SCH-001", qualifiziert.id());
        assertPersoenlicherAnlass(qualifiziert.id(), "QUALIFIKATION_DIREKT", "SCH-001");
        einsaetze.qualifikationEntziehen(admin.id(), "SCH-001", qualifiziert.id());
        assertPersoenlicherAnlass(qualifiziert.id(), "QUALIFIKATION_ENTZOGEN", "SCH-001");

        Benutzerkonto ableger = konto("Qualifikation Ableger", "katalog-ableger@example.de", false);
        jdbc.update("INSERT INTO trainer_qualifikation (benutzerkonto_id, schulung_id) VALUES (?, 'SCH-001')",
                ableger.id());
        einsaetze.eigeneQualifikationAblegen(ableger.id(), "SCH-001");
        assertThat(jdbc.queryForList("""
                SELECT anlass FROM benachrichtigung WHERE empfaenger_rolle='ADMINISTRATOR'
                AND anlasstyp='QUALIFIKATION_ABGELEGT'
                """, String.class)).singleElement().satisfies(text ->
                assertThat(text).contains("Qualifikation Ableger").contains("SCH-001"));

        Benutzerkonto konflikt = konto("Konflikt Trainer", "katalog-konflikt@example.de", false);
        termin("KATALOG-KONFLIKT", konflikt.id());
        jdbc.update("""
                INSERT INTO abwesenheit (benutzerkonto_id, von, bis, status)
                VALUES (?, '2026-10-01', '2026-10-02', 'OFFEN')
                """, konflikt.id());
        long abwesenheit = jdbc.queryForObject("SELECT MAX(id) FROM abwesenheit", Long.class);
        long antrag = anlegenFuer(konflikt, Vorgangsart.ABWESENHEITSANTRAG, null, true,
                "VORGANG", String.valueOf(abwesenheit), "1. bis 2. Oktober");
        vorgaenge.entscheiden(admin.id(), antrag, true, null);
        assertThat(jdbc.queryForList("""
                SELECT anlass FROM benachrichtigung WHERE empfaenger_rolle='ADMINISTRATOR'
                AND anlasstyp='VERFUEGBARKEITSKONFLIKT_DURCH_ABWESENHEIT'
                """, String.class)).singleElement().satisfies(text -> assertThat(text)
                .contains("Konflikt Trainer").contains("2026-10-01").contains("KATALOG-KONFLIKT"));

        assertThat(jdbc.queryForList("""
                SELECT anlass FROM benachrichtigung WHERE empfaenger_rolle='ADMINISTRATOR'
                AND anlasstyp='TRAINERWECHSEL_DURCH_UEBERNAHME'
                """, String.class)).isNotEmpty().allSatisfy(text ->
                assertThat(text).contains("Trainerwechsel").contains("von").contains("zu"));
    }

    private void assertPersoenlicherAnlass(String kontoId, String anlass, String inhalt) {
        assertThat(jdbc.queryForList("""
                SELECT anlass FROM benachrichtigung WHERE empfaenger_id=? AND anlasstyp=?
                """, String.class, kontoId, anlass)).singleElement()
                .satisfies(text -> assertThat(text).containsIgnoringCase(inhalt));
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

    private void terminFuer(String id, String trainerId, String start, String ende) {
        jdbc.update("""
                INSERT INTO termin (termin_id, schulung_id, startdatum, enddatum, status, trainer_id)
                VALUES (?, 'SCH-001', ?, ?, 'geplant', ?)
                """, id, LocalDate.parse(start), LocalDate.parse(ende), trainerId);
    }

    private MockHttpSession login(Benutzerkonto konto) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/auth/anmelden").with(csrf())
                        .contentType("application/json")
                        .content("{\"email\":\"" + konto.email() + "\",\"passwort\":\"pw\"}"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }
}
