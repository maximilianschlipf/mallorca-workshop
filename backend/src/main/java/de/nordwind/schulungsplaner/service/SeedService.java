package de.nordwind.schulungsplaner.service;

import de.nordwind.schulungsplaner.domain.Abwesenheit;
import de.nordwind.schulungsplaner.katalog.SchulungId;
import de.nordwind.schulungsplaner.katalog.ablage.KatalogRepository;
import de.nordwind.schulungsplaner.katalog.zustand.SchulungszustandRepository;
import de.nordwind.schulungsplaner.seed.TermineSeedRoot;
import de.nordwind.schulungsplaner.seed.TrainerSeed;
import de.nordwind.schulungsplaner.seed.TrainerSeedRoot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.Clock;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SeedService {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    private final PasswordEncoder passwoerter;
    private final KatalogRepository katalog;
    private final SchulungszustandRepository zustaende;
    private final boolean demoSeed;
    private final boolean demoTrainerSeed;
    private final Clock clock;

    public SeedService(JdbcTemplate jdbc, ObjectMapper json, PasswordEncoder passwoerter,
                       KatalogRepository katalog, SchulungszustandRepository zustaende,
                       Clock clock,
                       @Value("${app.demo-seed:false}") boolean demoSeed,
                       @Value("${app.demo-trainer-seed:true}") boolean demoTrainerSeed) {
        this.jdbc = jdbc;
        this.json = json;
        this.passwoerter = passwoerter;
        this.katalog = katalog;
        this.zustaende = zustaende;
        this.demoSeed = demoSeed;
        this.demoTrainerSeed = demoTrainerSeed;
        this.clock = clock;
    }

    @Transactional
    public void initialisieren() {
        if (demoSeed) {
            datenLeeren();
        }
        katalog.alleIds().forEach(zustaende::legeAn);
        if (!demoSeed) {
            return;
        }

        TrainerSeedRoot trainer = demoTrainerSeed
                ? lesen("trainer.json", TrainerSeedRoot.class) : new TrainerSeedRoot(List.of());
        if (demoTrainerSeed) {
            trainerImportieren(trainer);
        }
        termineImportieren(lesen("termine.json", TermineSeedRoot.class), demoTrainerSeed);
        if (demoTrainerSeed) {
            qualifikationenImportieren(trainer);
        }
    }

    private void datenLeeren() {
        jdbc.update("UPDATE instanz SET eigentuemer_id = NULL WHERE id = 1");
        jdbc.update("DELETE FROM assistenzbewerbung");
        jdbc.update("DELETE FROM qualifikationsbewerbung");
        jdbc.update("DELETE FROM teilnehmerbuchung");
        jdbc.update("DELETE FROM benachrichtigung");
        jdbc.update("DELETE FROM termin_assistent");
        jdbc.update("DELETE FROM trainer_qualifikation");
        jdbc.update("DELETE FROM termin");
        jdbc.update("DELETE FROM termin_nummer");
        jdbc.update("DELETE FROM abwesenheit");
        jdbc.update("DELETE FROM schulung_zustand");
        jdbc.update("DELETE FROM benutzerkonto_rolle");
        jdbc.update("DELETE FROM benutzerkonto");
    }

    private void trainerImportieren(TrainerSeedRoot wurzel) {
        if (wurzel == null || wurzel.trainer() == null) {
            return;
        }
        boolean erster = true;
        for (TrainerSeed trainer : wurzel.trainer()) {
            jdbc.update("""
                    INSERT INTO benutzerkonto (id, name, email, passwort_hash, aktiv)
                    VALUES (?, ?, ?, ?, TRUE)
                    """, trainer.id(), trainer.name(), trainer.email().toLowerCase(),
                    passwoerter.encode("test-passwort"));
            jdbc.update("INSERT INTO benutzerkonto_rolle VALUES (?, 'TRAINER')", trainer.id());
            if (erster) {
                jdbc.update("INSERT INTO benutzerkonto_rolle VALUES (?, 'ADMINISTRATOR')", trainer.id());
                jdbc.update("UPDATE instanz SET eigentuemer_id = ? WHERE id = 1", trainer.id());
                erster = false;
            }
            if (trainer.abwesenheiten() == null) {
                continue;
            }
            for (Abwesenheit abwesenheit : trainer.abwesenheiten()) {
                jdbc.update("""
                        INSERT INTO abwesenheit (benutzerkonto_id, von, bis, grund)
                        VALUES (?, ?, ?, ?)
                        """, trainer.id(), LocalDate.parse(abwesenheit.von()),
                        LocalDate.parse(abwesenheit.bis()), abwesenheit.grund());
            }
        }
    }

    private void termineImportieren(TermineSeedRoot wurzel, boolean mitTrainern) {
        if (wurzel == null || wurzel.termine() == null) {
            return;
        }
        LocalDate heute = LocalDate.now(clock);
        Map<String, Integer> nummern = new HashMap<>();
        int index = 0;
        for (TermineSeedRoot.TerminSeed termin : wurzel.termine()) {
            LocalDate[] zeitraum = seedZeitraum(heute, index);
            String status = index == 0 ? "abgeschlossen" : index == 3 ? "abgesagt" : "geplant";
            String durchfuehrung = switch (index % 4) {
                case 0 -> "remote";
                case 1 -> "vor_ort";
                case 2 -> "hybrid";
                default -> "beim_kunden";
            };
            String zugang = index % 2 == 0 ? "oeffentlich" : "exklusiv";
            String ort = "remote".equals(durchfuehrung) ? null : "Schulungsraum " + (index + 1);
            String firma = "exklusiv".equals(zugang) ? "Demo GmbH" : null;
            String online = Set.of("remote", "hybrid").contains(durchfuehrung)
                    ? "https://academy.example/termin-" + (index + 1) : null;
            if ("abgeschlossen".equals(status)) online = null;
            int nummer = nummern.merge(termin.schulungId(), 1, Integer::sum);
            String terminId = "%s-T%04d".formatted(termin.schulungId(), nummer);
            jdbc.update("""
                    INSERT INTO termin
                    (termin_id, schulung_id, startdatum, enddatum, ort, format, status, trainer_id,
                     zugangsart, durchfuehrungsart, kundenfirma, online_zugang,
                     abschlussart, abgeschlossen_am, bestaetigt_von, abgesagt_am, abgesagt_von)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, terminId, termin.schulungId(), zeitraum[0], zeitraum[1], ort,
                    durchfuehrung, status, mitTrainern && index != 2 ? termin.trainerId() : null,
                    zugang, durchfuehrung, firma, online,
                    "abgeschlossen".equals(status) ? (mitTrainern ? "manuell" : "automatisch") : null,
                    "abgeschlossen".equals(status) ? zeitraum[1] : null,
                    "abgeschlossen".equals(status) && mitTrainern ? termin.trainerId() : null,
                    "abgesagt".equals(status) ? heute : null,
                    "abgesagt".equals(status) && mitTrainern ? "TRN-001" : null);
            if (index == 1 && mitTrainern) {
                String assistent = "TRN-003".equals(termin.trainerId()) ? "TRN-001" : "TRN-003";
                jdbc.update("INSERT INTO termin_assistent (termin_id, benutzerkonto_id, platz) VALUES (?, ?, 1)", terminId, assistent);
            }
            jdbc.update("""
                    INSERT INTO teilnehmerbuchung (termin_id, name, firma, bemerkung, teilnahmestatus)
                    VALUES (?, ?, ?, ?, ?)
                    """, terminId, "Teilnehmer " + (index + 1),
                    firma == null ? "Beispiel AG" : firma, "Demo-Buchung",
                    "abgeschlossen".equals(status) ? "teilgenommen" : "offen");
            index++;
        }
        nummern.forEach((id, nummer) -> jdbc.update(
                "INSERT INTO termin_nummer (schulung_id, naechste_nummer) VALUES (?, ?)", id, nummer + 1));
    }

    private static LocalDate[] seedZeitraum(LocalDate heute, int index) {
        if (index == 0) {
            LocalDate ende = vorherigerWerktag(heute.minusDays(7));
            return new LocalDate[]{vorherigerWerktag(ende.minusDays(1)), ende};
        }
        if (index == 1) {
            LocalDate start = vorherigerWerktag(heute);
            LocalDate ende = naechsterWerktag(heute);
            return new LocalDate[]{start, ende};
        }
        LocalDate start = naechsterWerktag(heute.plusDays((long) (index - 1) * 7));
        return new LocalDate[]{start, naechsterWerktag(start.plusDays(1))};
    }

    private static LocalDate vorherigerWerktag(LocalDate tag) {
        while (tag.getDayOfWeek() == DayOfWeek.SATURDAY || tag.getDayOfWeek() == DayOfWeek.SUNDAY) tag = tag.minusDays(1);
        return tag;
    }

    private static LocalDate naechsterWerktag(LocalDate tag) {
        while (tag.getDayOfWeek() == DayOfWeek.SATURDAY || tag.getDayOfWeek() == DayOfWeek.SUNDAY) tag = tag.plusDays(1);
        return tag;
    }

    private void qualifikationenImportieren(TrainerSeedRoot wurzel) {
        for (TrainerSeed trainer : wurzel.trainer()) {
            if (trainer.qualifikationen() == null) {
                continue;
            }
            for (String schulungId : trainer.qualifikationen()) {
                if (zustaende.lade(SchulungId.von(schulungId)).isPresent()) {
                    jdbc.update("""
                            INSERT INTO trainer_qualifikation (benutzerkonto_id, schulung_id)
                            VALUES (?, ?)
                            """, trainer.id(), schulungId);
                }
            }
        }
    }

    private <T> T lesen(String datei, Class<T> typ) {
        ClassPathResource resource = new ClassPathResource("seed/" + datei);
        try (InputStream input = resource.getInputStream()) {
            return json.readValue(input, typ);
        } catch (IOException ex) {
            throw new IllegalStateException("Seed-Datei konnte nicht gelesen werden: " + datei, ex);
        }
    }
}
