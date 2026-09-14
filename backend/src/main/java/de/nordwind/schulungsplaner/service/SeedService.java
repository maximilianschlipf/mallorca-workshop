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
import java.util.List;

@Service
public class SeedService {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    private final PasswordEncoder passwoerter;
    private final KatalogRepository katalog;
    private final SchulungszustandRepository zustaende;
    private final boolean demoSeed;
    private final boolean demoTrainerSeed;

    public SeedService(JdbcTemplate jdbc, ObjectMapper json, PasswordEncoder passwoerter,
                       KatalogRepository katalog, SchulungszustandRepository zustaende,
                       @Value("${app.demo-seed:false}") boolean demoSeed,
                       @Value("${app.demo-trainer-seed:true}") boolean demoTrainerSeed) {
        this.jdbc = jdbc;
        this.json = json;
        this.passwoerter = passwoerter;
        this.katalog = katalog;
        this.zustaende = zustaende;
        this.demoSeed = demoSeed;
        this.demoTrainerSeed = demoTrainerSeed;
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
        jdbc.update("DELETE FROM termin_assistent");
        jdbc.update("DELETE FROM trainer_qualifikation");
        jdbc.update("DELETE FROM termin");
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
        for (TermineSeedRoot.TerminSeed termin : wurzel.termine()) {
            jdbc.update("""
                    INSERT INTO termin
                    (termin_id, schulung_id, startdatum, enddatum, ort, format, status, trainer_id)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """, termin.terminId(), termin.schulungId(), LocalDate.parse(termin.startdatum()),
                    LocalDate.parse(termin.enddatum()), termin.ort(), termin.format(),
                    termin.status(), mitTrainern ? termin.trainerId() : null);
        }
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
