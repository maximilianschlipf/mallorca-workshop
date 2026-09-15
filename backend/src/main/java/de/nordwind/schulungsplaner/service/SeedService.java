package de.nordwind.schulungsplaner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.nordwind.schulungsplaner.domain.Abwesenheit;
import de.nordwind.schulungsplaner.domain.Schulung;
import de.nordwind.schulungsplaner.domain.Termin;
import de.nordwind.schulungsplaner.seed.SchulungenSeedRoot;
import de.nordwind.schulungsplaner.seed.TrainerSeed;
import de.nordwind.schulungsplaner.seed.TrainerSeedRoot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

@Service
public class SeedService {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    private final PasswordEncoder passwoerter;
    private final boolean demoSeed;
    private final boolean demoTrainerSeed;

    public SeedService(JdbcTemplate jdbc, ObjectMapper json, PasswordEncoder passwoerter,
                       @Value("${app.demo-seed:false}") boolean demoSeed,
                       @Value("${app.demo-trainer-seed:true}") boolean demoTrainerSeed) {
        this.jdbc = jdbc;
        this.json = json;
        this.passwoerter = passwoerter;
        this.demoSeed = demoSeed;
        this.demoTrainerSeed = demoTrainerSeed;
    }

    @Transactional
    public void initialisieren() {
        SchulungenSeedRoot schulungen = lesen("schulungen.json", SchulungenSeedRoot.class);
        if (demoSeed) datenLeeren();
        katalogImportieren(schulungen);
        if (!demoSeed) return;
        TrainerSeedRoot trainer = demoTrainerSeed
                ? lesen("trainer.json", TrainerSeedRoot.class) : new TrainerSeedRoot(List.of());
        if (demoTrainerSeed) trainerImportieren(trainer);
        termineImportieren(schulungen, demoTrainerSeed);
        if (demoTrainerSeed) qualifikationenImportieren(trainer);
    }

    private void datenLeeren() {
        jdbc.update("UPDATE instanz SET eigentuemer_id = NULL WHERE id = 1");
        jdbc.update("DELETE FROM assistenzbewerbung");
        jdbc.update("DELETE FROM qualifikationsbewerbung");
        jdbc.update("DELETE FROM termin_assistent");
        jdbc.update("DELETE FROM trainer_qualifikation");
        jdbc.update("DELETE FROM termin");
        jdbc.update("DELETE FROM voraussetzung");
        jdbc.update("DELETE FROM abwesenheit");
        jdbc.update("DELETE FROM schulung");
        jdbc.update("DELETE FROM benutzerkonto_rolle");
        jdbc.update("DELETE FROM benutzerkonto");
    }

    private void katalogImportieren(SchulungenSeedRoot wurzel) {
        if (wurzel == null || wurzel.schulungen() == null) return;
        for (Schulung schulung : wurzel.schulungen()) {
            jdbc.update("""
                    MERGE INTO schulung
                    (id, titel, kategorie, kurzbeschreibung, dauer_in_tagen,
                     mindestteilnehmer_exklusiv, max_teilnehmer_oeffentlich)
                    KEY (id) VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, schulung.id(), schulung.titel(), schulung.kategorie(),
                    schulung.kurzbeschreibung(), schulung.dauerInTagen(),
                    schulung.mindestteilnehmerExklusiv(), schulung.maxTeilnehmerOeffentlich());
            jdbc.update("DELETE FROM voraussetzung WHERE schulung_id = ?", schulung.id());
            if (schulung.voraussetzungen() != null) {
                for (String text : schulung.voraussetzungen()) {
                    jdbc.update("INSERT INTO voraussetzung (schulung_id, text) VALUES (?, ?)",
                            schulung.id(), text);
                }
            }
        }
    }

    private void trainerImportieren(TrainerSeedRoot wurzel) {
        if (wurzel == null || wurzel.trainer() == null) return;
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
            List<Abwesenheit> abwesenheiten = trainer.abwesenheiten();
            if (abwesenheiten != null) {
                for (Abwesenheit abwesenheit : abwesenheiten) {
                    jdbc.update("""
                            INSERT INTO abwesenheit (benutzerkonto_id, von, bis, grund)
                            VALUES (?, ?, ?, ?)
                            """, trainer.id(), LocalDate.parse(abwesenheit.von()),
                            LocalDate.parse(abwesenheit.bis()), abwesenheit.grund());
                }
            }
        }
    }

    private void termineImportieren(SchulungenSeedRoot wurzel, boolean mitTrainern) {
        for (Schulung schulung : wurzel.schulungen()) {
            if (schulung.oeffentlicheTermine() == null) continue;
            for (Termin termin : schulung.oeffentlicheTermine()) {
                jdbc.update("""
                        INSERT INTO termin
                        (termin_id, schulung_id, startdatum, enddatum, ort, format, status, trainer_id)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """, termin.terminId(), schulung.id(), LocalDate.parse(termin.startdatum()),
                        LocalDate.parse(termin.enddatum()), termin.ort(), termin.format(),
                        termin.status(), mitTrainern ? termin.trainerId() : null);
            }
        }
    }

    private void qualifikationenImportieren(TrainerSeedRoot wurzel) {
        for (TrainerSeed trainer : wurzel.trainer()) {
            if (trainer.qualifikationen() == null) continue;
            for (String schulungId : trainer.qualifikationen()) {
                jdbc.update("""
                        INSERT INTO trainer_qualifikation (benutzerkonto_id, schulung_id)
                        VALUES (?, ?)
                        """, trainer.id(), schulungId);
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
