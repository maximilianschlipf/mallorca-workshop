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

    // verifies: TEST_DSH_VORG_07
    @Test
    void alleFachlichenEreignisseBeendenOderReduzierenOffeneVorgaengeNachvollziehbar() {
        jdbc.update("INSERT INTO trainer_qualifikation (benutzerkonto_id, schulung_id) VALUES (?, 'SCH-001')",
                trainer.id());
        termin("T-ZUWEISUNG", null);
        long vormerkung = anlegen(Vorgangsart.VORMERKUNG, null, true,
                "TERMIN", "T-ZUWEISUNG", "Termin T-ZUWEISUNG");
        termine.trainerZuweisen(admin.id(), "T-ZUWEISUNG", trainer.id(), false);
        entfallenOhneEntscheider(vormerkung, "Zuweisung");

        termin("T-TAUSCH", trainer.id());
        long uebernahme = anlegen(Vorgangsart.UEBERNAHMEANFRAGE, trainer.id(), false,
                "TERMIN", "T-TAUSCH", "Termin T-TAUSCH");
        Benutzerkonto anderer = konto("Anderer", "anderer@example.de", false);
        long andereUebernahme = anlegenFuer(anderer, Vorgangsart.UEBERNAHMEANFRAGE,
                trainer.id(), false, "TERMIN", "T-TAUSCH", "Termin T-TAUSCH");
        vorgaenge.entscheiden(trainer.id(), uebernahme, true, null);
        entfallenOhneEntscheider(andereUebernahme, "Tausch");

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
        entfallenOhneEntscheider(ersatzFrist, "Fristablauf");
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

        termin("T-ABSAGE-VORGAENGE", trainer.id());
        long v = anlegen(Vorgangsart.VORMERKUNG, null, true, "TERMIN", "T-ABSAGE-VORGAENGE", "Termin");
        long a = anlegen(Vorgangsart.ASSISTENZBEWERBUNG, trainer.id(), true, "TERMIN", "T-ABSAGE-VORGAENGE", "Termin");
        long u = anlegen(Vorgangsart.UEBERNAHMEANFRAGE, trainer.id(), false, "TERMIN", "T-ABSAGE-VORGAENGE", "Termin");
        termine.absagen(admin.id(), "T-ABSAGE-VORGAENGE", "Kunde");
        for (long id : new long[]{v, a, u}) entfallenOhneEntscheider(id, "Terminabsage");

        termin("T-LOESCH-VORGAENGE", trainer.id());
        long lv = anlegen(Vorgangsart.VORMERKUNG, null, true, "TERMIN", "T-LOESCH-VORGAENGE", "Termin");
        long la = anlegen(Vorgangsart.ASSISTENZBEWERBUNG, trainer.id(), true, "TERMIN", "T-LOESCH-VORGAENGE", "Termin");
        long lu = anlegen(Vorgangsart.UEBERNAHMEANFRAGE, trainer.id(), false, "TERMIN", "T-LOESCH-VORGAENGE", "Termin");
        termine.loeschen(admin.id(), "T-LOESCH-VORGAENGE");
        for (long id : new long[]{lv, la, lu}) entfallenOhneEntscheider(id, "Terminlöschung");

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
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM vorgang WHERE art='ABWESENHEITSANTRAG'
                AND antragsteller_id=? AND status='OFFEN'
                """, Integer.class, antragsteller.id())).isPositive();

        Benutzerkonto stillgelegt = konto("Stillgelegt", "stillgelegt@example.de", false);
        long[] stillgelegteVorgaenge = alleSechsVorgaenge(stillgelegt);
        konten.stilllegen(admin.id(), stillgelegt.id(), stillgelegt.aenderungsstand());
        assertAlleEntfallen(stillgelegteVorgaenge, "Stilllegung");

        Benutzerkonto geloescht = konto("Gelöscht", "geloescht@example.de", false);
        long[] geloeschteVorgaenge = alleSechsVorgaenge(geloescht);
        konten.loeschen(admin.id(), geloescht.id(), geloescht.aenderungsstand());
        assertAlleEntfallen(geloeschteVorgaenge, "Löschung");
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM vorgang WHERE id IN (?,?,?,?,?) AND antragsteller_id IS NOT NULL
                """, Integer.class, geloeschteVorgaenge[1], geloeschteVorgaenge[2],
                geloeschteVorgaenge[3], geloeschteVorgaenge[4], geloeschteVorgaenge[5])).isZero();

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
