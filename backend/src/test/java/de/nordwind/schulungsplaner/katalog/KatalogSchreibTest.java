package de.nordwind.schulungsplaner.katalog;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Grundlage aller Tests, die den Katalog veraendern.
 *
 * <p>Jeder Test bekommt ein frisches Katalogverzeichnis in einem frischen
 * Git-Repository ausserhalb des Projekts. Ohne diese Trennung liesse sich
 * REQ_KAT_ABL_03 nicht pruefen: Ein Test, der Commits zaehlt, muss bei null
 * anfangen, und er darf keinesfalls in die Versionsgeschichte des Projekts
 * schreiben.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
abstract class KatalogSchreibTest {

    /** Die Kategorien, die in jedem Test zur Verfuegung stehen. */
    protected static final List<String> KATEGORIEN =
            List.of("Agile & Projektmanagement", "Cloud & DevOps", "IT-Security");

    private static final Path REPOWURZEL = temporaereWurzel();

    @Autowired
    protected RestTestClient restTestClient;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void eigeneUmgebung(DynamicPropertyRegistry registry) {
        registry.add("schulungsplaner.katalog.pfad",
                () -> REPOWURZEL.resolve("katalog").toString());
        // Eigene Datenbank: Die H2-Instanz im Speicher wird ueber ihren Namen
        // JVM-weit geteilt. Ohne eigenen Namen leerte das Aufraeumen dieser
        // Tests die Daten aller anderen mit -- je nach Reihenfolge.
        registry.add("spring.datasource.url",
                () -> "jdbc:h2:mem:katalog-schreibtest;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
    }

    @BeforeEach
    void frischerKatalogUndFrischesRepository() throws Exception {
        leereVerzeichnis(REPOWURZEL);
        Git.init().setDirectory(REPOWURZEL.toFile()).call().close();

        Path katalog = REPOWURZEL.resolve("katalog");
        Files.createDirectories(katalog.resolve("schulungen"));
        Files.writeString(katalog.resolve("kategorien.json"), """
                {
                  "kategorien" : [ "Agile & Projektmanagement", "Cloud & DevOps", "IT-Security" ]
                }
                """);

        jdbcTemplate.update("DELETE FROM trainer_qualifikation");
        jdbcTemplate.update("DELETE FROM termin");
        jdbcTemplate.update("DELETE FROM schulung_zustand");

        var csrf = restTestClient.get().uri("/api/auth/csrf").exchange()
                .expectStatus().isOk().returnResult(Map.class);
        String token = (String) csrf.getResponseBody().get("token");
        String csrfCookie = csrf.getResponseCookies().getFirst("XSRF-TOKEN").getValue();
        var login = restTestClient.post().uri("/api/auth/anmelden")
                .cookie("XSRF-TOKEN", csrfCookie)
                .header("X-XSRF-TOKEN", token)
                .body(Map.of(
                        "email", "julia.hoffmann@simplytest-academy.de",
                        "passwort", "test-passwort"))
                .exchange().expectStatus().isOk().returnResult();
        ResponseCookie session = login.getResponseCookies().getFirst("JSESSIONID");
        restTestClient = restTestClient.mutate()
                .defaultCookie("JSESSIONID", session.getValue())
                .defaultCookie("XSRF-TOKEN", csrfCookie)
                .defaultHeader("X-XSRF-TOKEN", token)
                .build();
    }

    // --- Hilfsmittel fuer die abgeleiteten Tests ---------------------------

    protected Path katalogdatei(String id) {
        return REPOWURZEL.resolve("katalog").resolve("schulungen").resolve(id + ".json");
    }

    protected Path kategorienDatei() {
        return REPOWURZEL.resolve("katalog").resolve("kategorien.json");
    }

    /** Alle Commits, neueste zuerst. */
    protected List<RevCommit> commits() {
        return commits(null);
    }

    /** Die Commits, die den angegebenen Pfad beruehren. */
    protected List<RevCommit> commitsFuer(Path datei) {
        return commits(REPOWURZEL.relativize(datei.toAbsolutePath().normalize()).toString());
    }

    protected void legeTerminAn(String terminId, String schulungId,
                                String startdatum, String enddatum) {
        jdbcTemplate.update(
                "INSERT INTO termin (termin_id, schulung_id, startdatum, enddatum, ort, "
                        + "format, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
                terminId, schulungId, java.time.LocalDate.parse(startdatum),
                java.time.LocalDate.parse(enddatum), "Köln", "Präsenz", "geplant");
    }

    private List<RevCommit> commits(String relativerPfad) {
        try (Git git = Git.open(REPOWURZEL.toFile())) {
            if (git.getRepository().resolve("HEAD") == null) {
                return List.of();
            }
            var log = git.log();
            if (relativerPfad != null) {
                log.addPath(relativerPfad.replace(java.io.File.separatorChar, '/'));
            }
            List<RevCommit> gefunden = new ArrayList<>();
            log.call().forEach(gefunden::add);
            return gefunden;
        } catch (Exception ex) {
            throw new IllegalStateException("Die Commits liessen sich nicht lesen.", ex);
        }
    }

    private static void leereVerzeichnis(Path verzeichnis) throws IOException {
        if (!Files.isDirectory(verzeichnis)) {
            Files.createDirectories(verzeichnis);
            return;
        }
        try (Stream<Path> eintraege = Files.walk(verzeichnis)) {
            eintraege.sorted(Comparator.reverseOrder())
                    .filter(pfad -> !pfad.equals(verzeichnis))
                    .forEach(pfad -> {
                        try {
                            Files.deleteIfExists(pfad);
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                    });
        }
    }

    private static Path temporaereWurzel() {
        try {
            Path wurzel = Files.createTempDirectory("schulungskatalog-test");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    leereVerzeichnis(wurzel);
                    Files.deleteIfExists(wurzel);
                } catch (IOException ignoriert) {
                    // Ein liegengebliebenes Testverzeichnis ist kein Grund,
                    // den Testlauf scheitern zu lassen.
                }
            }));
            return wurzel;
        } catch (IOException ex) {
            throw new UncheckedIOException("Kein temporaeres Katalogverzeichnis moeglich.", ex);
        }
    }
}
