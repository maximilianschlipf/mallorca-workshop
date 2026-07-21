package de.nordwind.schulungsplaner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.nordwind.schulungsplaner.domain.Abwesenheit;
import de.nordwind.schulungsplaner.domain.Schulung;
import de.nordwind.schulungsplaner.domain.Termin;
import de.nordwind.schulungsplaner.domain.Trainer;
import de.nordwind.schulungsplaner.seed.SchulungenSeedRoot;
import de.nordwind.schulungsplaner.seed.TrainerSeedRoot;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

@Service
public class SeedService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public SeedService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public void resetAndSeed() {
        clearData();
        importTrainer();
        importSchulungen();
    }

    private void clearData() {
        jdbcTemplate.update("DELETE FROM termin");
        jdbcTemplate.update("DELETE FROM voraussetzung");
        jdbcTemplate.update("DELETE FROM abwesenheit");
        jdbcTemplate.update("DELETE FROM schulung");
        jdbcTemplate.update("DELETE FROM trainer");
    }

    private void importTrainer() {
        TrainerSeedRoot seedRoot = readFromRepoRoot("trainer.json", TrainerSeedRoot.class);
        if (seedRoot == null || seedRoot.trainer() == null) {
            return;
        }
        for (Trainer trainer : seedRoot.trainer()) {
            jdbcTemplate.update(
                    "INSERT INTO trainer (id, name, email) VALUES (?, ?, ?)",
                    trainer.id(), trainer.name(), trainer.email()
            );
            List<Abwesenheit> abwesenheiten = trainer.abwesenheiten();
            if (abwesenheiten == null) {
                continue;
            }
            for (Abwesenheit abwesenheit : abwesenheiten) {
                jdbcTemplate.update(
                        "INSERT INTO abwesenheit (trainer_id, von, bis, grund) VALUES (?, ?, ?, ?)",
                        trainer.id(),
                        LocalDate.parse(abwesenheit.von()),
                        LocalDate.parse(abwesenheit.bis()),
                        abwesenheit.grund()
                );
            }
        }
    }

    private void importSchulungen() {
        SchulungenSeedRoot seedRoot = readFromRepoRoot("schulungen.json", SchulungenSeedRoot.class);
        if (seedRoot == null || seedRoot.schulungen() == null) {
            return;
        }
        for (Schulung schulung : seedRoot.schulungen()) {
            jdbcTemplate.update(
                    "INSERT INTO schulung (id, titel, kategorie, kurzbeschreibung, dauer_in_tagen, mindestteilnehmer_exklusiv, max_teilnehmer_oeffentlich) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    schulung.id(),
                    schulung.titel(),
                    schulung.kategorie(),
                    schulung.kurzbeschreibung(),
                    schulung.dauerInTagen(),
                    schulung.mindestteilnehmerExklusiv(),
                    schulung.maxTeilnehmerOeffentlich()
            );

            if (schulung.voraussetzungen() != null) {
                for (String voraussetzung : schulung.voraussetzungen()) {
                    jdbcTemplate.update(
                            "INSERT INTO voraussetzung (schulung_id, text) VALUES (?, ?)",
                            schulung.id(), voraussetzung
                    );
                }
            }

            if (schulung.oeffentlicheTermine() != null) {
                for (Termin termin : schulung.oeffentlicheTermine()) {
                    jdbcTemplate.update(
                            "INSERT INTO termin (termin_id, schulung_id, startdatum, enddatum, ort, format, status, trainer_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                            termin.terminId(),
                            schulung.id(),
                            LocalDate.parse(termin.startdatum()),
                            LocalDate.parse(termin.enddatum()),
                            termin.ort(),
                            termin.format(),
                            termin.status(),
                            termin.trainerId()
                    );
                }
            }
        }
    }

    private <T> T readFromRepoRoot(String fileName, Class<T> type) {
        Path rootFile = Path.of("..", fileName).toAbsolutePath().normalize();
        if (!Files.exists(rootFile)) {
            throw new IllegalStateException("Seed-Datei nicht gefunden: " + rootFile);
        }
        try {
            return objectMapper.readValue(rootFile.toFile(), type);
        } catch (IOException ex) {
            throw new IllegalStateException("Seed-Datei konnte nicht gelesen werden: " + rootFile, ex);
        }
    }
}
