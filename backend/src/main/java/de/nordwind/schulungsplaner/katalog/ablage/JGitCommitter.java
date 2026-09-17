package de.nordwind.schulungsplaner.katalog.ablage;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Sichert Katalogaenderungen mit JGit (REQ_KAT_ABL_03).
 *
 * <p>JGit statt der git-Befehlszeile: Die Anwendung setzt damit keine
 * Installation voraus, und Tests laufen gegen ein temporaeres Repository,
 * ohne dass je in das Repository des Projekts geschrieben wird.
 *
 * <p>Die Commit-Identitaet gibt die Anwendung vor und liest sie nicht aus der
 * Git-Konfiguration des Rechners. Sonst haenge das Ergebnis daran, ob und wie
 * der Benutzer git eingerichtet hat -- auf einem frisch aufgesetzten Rechner
 * schluege der Commit schlicht fehl.
 */
public class JGitCommitter implements KatalogCommitter {

    private final Path katalogverzeichnis;
    private final PersonIdent identitaet;

    /**
     * @param katalogverzeichnis ein Verzeichnis innerhalb des Repositorys; das
     *                           Repository selbst wird von dort aus gesucht
     *                           und darf hoeher liegen
     */
    public JGitCommitter(Path katalogverzeichnis, String name, String email) {
        this.katalogverzeichnis = katalogverzeichnis.toAbsolutePath().normalize();
        this.identitaet = new PersonIdent(name, email);
    }

    @Override
    public void sichere(String nachricht, Collection<Path> dateien) {
        if (dateien.isEmpty()) {
            return;
        }
        try (Repository repository = oeffne();
             Git git = new Git(repository)) {

            Path arbeitsbaum = repository.getWorkTree().toPath().toAbsolutePath().normalize();
            Set<String> pfadmuster = pfadmusterFuer(dateien, arbeitsbaum);

            StatusCommand status = git.status();
            for (String muster : pfadmuster) {
                vormerken(git, arbeitsbaum, muster);
                status.addPath(muster);
            }
            if (status.call().getUncommittedChanges().isEmpty()) {
                // Nichts geaendert -- ein leerer Commit waere ein falsches Signal.
                return;
            }
            var commit = git.commit()
                    .setMessage(nachricht)
                    .setAuthor(identitaet)
                    .setCommitter(identitaet)
                    .setAllowEmpty(false);
            // setOnly begrenzt den Commit auf die genannten Dateien. Was sonst
            // noch vorgemerkt ist, geht die Katalogaenderung nichts an.
            pfadmuster.forEach(commit::setOnly);
            commit.call();

        } catch (IOException | GitAPIException ex) {
            throw new KatalogCommitFehler(nachricht, ex);
        }
    }

    /**
     * Merkt eine Datei vor. Existiert sie nicht mehr, wird ihre Entfernung
     * vorgemerkt -- das Loeschen einer Schulung gehoert ebenso in die
     * Versionsgeschichte wie ihr Anlegen (REQ_KAT_LOE_01).
     */
    private void vormerken(Git git, Path arbeitsbaum, String muster) throws GitAPIException {
        if (Files.exists(arbeitsbaum.resolve(muster))) {
            git.add().addFilepattern(muster).call();
        } else {
            git.rm().addFilepattern(muster).setCached(false).call();
        }
    }

    private Repository oeffne() throws IOException {
        Repository repository = new FileRepositoryBuilder()
                .findGitDir(katalogverzeichnis.toFile())
                .readEnvironment()
                .build();
        if (repository.getDirectory() == null || repository.isBare()) {
            repository.close();
            throw new IOException("Über '" + katalogverzeichnis
                    + "' liegt kein Git-Repository mit Arbeitsbaum.");
        }
        return repository;
    }

    /**
     * Rechnet die Dateien auf Pfade relativ zur Wurzel des Arbeitsbaums um --
     * nicht relativ zum Katalogverzeichnis. JGit erwartet seine Muster in
     * dieser Form und mit Schraegstrichen, und das Katalogverzeichnis liegt im
     * Regelfall unterhalb der Repository-Wurzel.
     */
    private Set<String> pfadmusterFuer(Collection<Path> dateien, Path arbeitsbaum) {
        Set<String> muster = new LinkedHashSet<>();
        for (Path datei : dateien) {
            Path absolut = datei.toAbsolutePath().normalize();
            if (!absolut.startsWith(arbeitsbaum)) {
                throw new IllegalArgumentException("Die Datei '" + absolut
                        + "' liegt ausserhalb des Arbeitsbaums '" + arbeitsbaum + "'.");
            }
            muster.add(arbeitsbaum.relativize(absolut).toString()
                    .replace(File.separatorChar, '/'));
        }
        return muster;
    }
}
