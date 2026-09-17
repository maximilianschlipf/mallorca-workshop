package de.nordwind.schulungsplaner.katalog.ablage;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * REQ_KAT_ABL_03: Eine Katalogaenderung schreibt die Datei und sichert sie
 * mit einem Commit. Gegenstuecke in der Anforderungsdoku: TEST_KAT_ABL_02
 * und TEST_KAT_KATG_04.
 *
 * <p>Alle Tests laufen gegen ein frisch angelegtes Repository unter
 * {@link TempDir}. Kein Test beruehrt das Repository des Projekts, und keiner
 * haengt an der globalen Git-Konfiguration des Rechners -- die
 * Commit-Identitaet gibt die Anwendung selbst vor.
 */
class JGitCommitterTest {

    @TempDir
    Path repowurzel;

    private Git git;
    private JGitCommitter committer;

    @BeforeEach
    void setUp() throws Exception {
        git = Git.init().setDirectory(repowurzel.toFile()).call();
        committer = new JGitCommitter(repowurzel, "Schulungsplaner", "sp@localhost");
    }

    /** TEST_KAT_ABL_02: Nach dem Aendern ein neuer Commit auf der Datei. */
    @Test
    void shouldCommitTheChangedFile() throws Exception {
        Path datei = schreibe("katalog/schulungen/SCH-009.json", "{ \"id\": \"SCH-009\" }");

        committer.sichere("Schulung SCH-009 angelegt", List.of(datei));

        assertThat(commitsFuer("katalog/schulungen/SCH-009.json")).hasSize(1);
        assertThat(alleCommits()).hasSize(1);
        assertThat(alleCommits().getFirst().getFullMessage())
                .isEqualTo("Schulung SCH-009 angelegt");
    }

    @Test
    void shouldUseTheConfiguredIdentityInsteadOfTheMachineWideGitConfig() throws Exception {
        committer.sichere("Erste Aenderung",
                List.of(schreibe("katalog/kategorien.json", "{}")));

        RevCommit commit = alleCommits().getFirst();
        assertThat(commit.getAuthorIdent().getName()).isEqualTo("Schulungsplaner");
        assertThat(commit.getAuthorIdent().getEmailAddress()).isEqualTo("sp@localhost");
        assertThat(commit.getCommitterIdent().getName()).isEqualTo("Schulungsplaner");
    }

    @Test
    void shouldLeaveUnrelatedFilesOutOfTheCommit() throws Exception {
        Path katalogdatei = schreibe("katalog/schulungen/SCH-009.json", "{}");
        schreibe("notiz.txt", "nicht Teil des Katalogs");

        committer.sichere("Schulung SCH-009 angelegt", List.of(katalogdatei));

        assertThat(commitsFuer("notiz.txt")).isEmpty();
        assertThat(git.status().call().getUntracked()).containsExactly("notiz.txt");
    }

    @Test
    void shouldCommitADeletedFile() throws Exception {
        Path datei = schreibe("katalog/schulungen/SCH-009.json", "{}");
        committer.sichere("Schulung SCH-009 angelegt", List.of(datei));

        Files.delete(datei);
        committer.sichere("Schulung SCH-009 geloescht", List.of(datei));

        assertThat(commitsFuer("katalog/schulungen/SCH-009.json")).hasSize(2);
        assertThat(git.status().call().isClean()).isTrue();
    }

    /**
     * TEST_KAT_ABL_03 stuetzt sich darauf: Archivieren aendert nur die
     * Datenbank. Wird dabei nichts geschrieben, darf auch kein leerer Commit
     * entstehen -- sonst waere die Zahl der Commits nicht mehr unveraendert.
     */
    @Test
    void shouldNotCreateAnEmptyCommitWhenNothingChanged() throws Exception {
        Path datei = schreibe("katalog/schulungen/SCH-009.json", "{}");
        committer.sichere("Schulung SCH-009 angelegt", List.of(datei));

        committer.sichere("Nichts geaendert", List.of(datei));

        assertThat(alleCommits()).hasSize(1);
    }

    /**
     * Der Regelfall im Betrieb: Das Katalogverzeichnis liegt <em>unterhalb</em>
     * der Repository-Wurzel, nicht auf ihr. Die Pfadmuster fuer JGit muessen
     * sich deshalb auf den Arbeitsbaum beziehen und nicht auf das
     * Verzeichnis, mit dem der Committer eingerichtet wurde.
     */
    @Test
    void shouldCommitWhenTheCatalogSitsBelowTheRepositoryRoot() throws Exception {
        JGitCommitter unterhalb = new JGitCommitter(
                repowurzel.resolve("katalog"), "Schulungsplaner", "sp@localhost");
        Path datei = schreibe("katalog/schulungen/SCH-009.json", "{}");

        unterhalb.sichere("Schulung SCH-009 angelegt", List.of(datei));

        assertThat(commitsFuer("katalog/schulungen/SCH-009.json")).hasSize(1);
        assertThat(git.status().call().isClean()).isTrue();
    }

    @Test
    void shouldCommitSeveralFilesTogether() throws Exception {
        Path schulung = schreibe("katalog/schulungen/SCH-009.json", "{}");
        Path kategorien = schreibe("katalog/kategorien.json", "{}");

        committer.sichere("Kategorie umbenannt", List.of(schulung, kategorien));

        assertThat(alleCommits()).hasSize(1);
        assertThat(commitsFuer("katalog/schulungen/SCH-009.json")).hasSize(1);
        assertThat(commitsFuer("katalog/kategorien.json")).hasSize(1);
    }

    // --- Hilfsmittel -------------------------------------------------------

    private Path schreibe(String relativerPfad, String inhalt) throws IOException {
        Path datei = repowurzel.resolve(relativerPfad);
        Files.createDirectories(datei.getParent());
        Files.writeString(datei, inhalt);
        return datei;
    }

    private List<RevCommit> alleCommits() throws Exception {
        return commits(null);
    }

    private List<RevCommit> commitsFuer(String relativerPfad) throws Exception {
        return commits(relativerPfad);
    }

    private List<RevCommit> commits(String relativerPfad) throws Exception {
        if (git.getRepository().resolve("HEAD") == null) {
            return List.of();
        }
        var log = git.log();
        if (relativerPfad != null) {
            log.addPath(relativerPfad);
        }
        List<RevCommit> commits = new java.util.ArrayList<>();
        log.call().forEach(commits::add);
        return commits;
    }
}
