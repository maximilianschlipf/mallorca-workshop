# SimplyTest Academy

## Voraussetzungen

- Java 21 (wird auch von PlantUML benötigt)
- Node.js 22 LTS und npm
- Python 3.12 oder neuer (Sphinx 9 setzt mindestens 3.12 voraus)
- PlantUML (rendert die Diagramme der Doku)
- Visual Studio Code mit GitHub Copilot

PlantUML installieren:

| Plattform | Befehl |
| --------- | ------ |
| macOS | `brew install plantuml` |
| Windows | `choco install plantuml` oder `scoop install plantuml` |
| Linux (Debian/Ubuntu) | `sudo apt install plantuml` |

Sphinx findet das Programm über den PATH; in der `conf.py` ist nichts zu setzen.

## Einmalige Installation

```bash
cd frontend
npm ci
npx playwright install chromium
```

## Lokal starten

Terminal 1:

```bash
cd backend
./mvnw spring-boot:run
```

Terminal 2:

```bash
cd frontend
npm run dev
```

Frontend: http://localhost:15173  
Backend: http://localhost:18081

Beim ersten Start ist noch kein Benutzerkonto vorhanden. Über
„Jetzt registrieren“ entsteht das Eigentümerkonto mit allen drei Rollen.

### Workshop-Testkonten

Für die Workshop-Vorbereitung startet `APP_DEMO_SEED=true ./start.sh` die
Anwendung mit reproduzierbaren Demodaten. Dabei wird die lokale Datenbank bei
jedem Start zurückgesetzt. Alle Testkonten verwenden das Passwort
`test-passwort`:

| Rolle | E-Mail-Adresse |
| ----- | -------------- |
| Eigentümer, Administrator, Trainer | `julia.hoffmann@simplytest-academy.de` |
| Administrator | `admin@simplytest-academy.de` |
| Trainer | `sophie.bauer@simplytest-academy.de` |

## Verifizieren

Der vollständige Abschluss-Check wird aus dem Projektverzeichnis gestartet:

```bash
./verify.sh
```

Er erzeugt zuerst die Traceability-Matrix aus den freigegebenen Requirements
und ihren echten Testnachweisen. Offene oder nur teilweise geprüfte
`TEST_*`-Szenarien brechen den Check ab. Erst danach laufen Dokumentation,
Backendtests, Frontendtests, Build und ein isolierter E2E-Lauf. Lokale Server
müssen dafür beendet sein.

Einzelne Prüfschritte lassen sich weiterhin separat ausführen:

```bash
cd backend
./mvnw test
```

```bash
cd frontend
npm run test
npm run build
npm run test:e2e
```

Der E2E-Befehl startet Backend und Frontend selbst. Er verwendet dabei
bewusst **kein** laufendes Backend weiter: Die Tests legen Schulungen an und
löschen sie, und jede Änderung am Katalog erzeugt einen Commit. Sie laufen
deshalb gegen eine Kopie des Katalogs in einem eigenen Repository unter
`$TMPDIR`. Läuft auf Port 18081 schon ein Backend, bricht der Lauf ab --
beende es vorher.

## Dokumentation

- [Fachsprache](CONTEXT.md)
- [Anforderungen](docs/source/anforderungen/index.rst) (Quelle; gebaut unter `docs/build/html/`)

Die Anforderungsdokumentation liegt unter `docs/` und wird mit Sphinx und
sphinx-needs gebaut. `docs/source/conf.py` ist bereits fertig konfiguriert
(Need-Typen, ID-Schema, Link-Typen) und im Repository enthalten -- es ist kein
`sphinx-quickstart` nötig, der würde die Konfiguration überschreiben.

### Einmalige Installation

Virtuelle Umgebung im Projektwurzel-Verzeichnis anlegen und aktivieren:

```bash
python3 -m venv .venv
source .venv/bin/activate
```

Unter Windows stattdessen (PowerShell):

```powershell
py -3.12 -m venv .venv
.venv\Scripts\Activate.ps1
```

Unter Linux heißt der Interpreter je nach Distribution `python3.12`; das
Paket `python3-venv` muss dort gegebenenfalls separat installiert sein
(`sudo apt install python3.12-venv`).

Danach plattformunabhängig die gepinnten Abhängigkeiten installieren:

```bash
python -m pip install -U pip
python -m pip install -r docs/requirements.txt
```

Fehlt in der frischen Umgebung `pip` (manche Python-Installationen legen die
venv ohne an), hilft `python -m ensurepip --upgrade` vor den beiden Befehlen.

Das Corporate-Design-Theme `st-sphinx-theme` wird separat verteilt und ist
deshalb nicht in `docs/requirements.txt` gepinnt. Aus einem lokalen Checkout:

```bash
python -m pip install -e ../st-sphinx-theme
```

### Doku bauen

Bei aktivierter venv:

```bash
cd docs
make html
```

Unter Windows im Verzeichnis `docs`:

```powershell
.\make.bat html
```

Das Ergebnis liegt in `docs/build/html/index.html`, der Needs-Export für
Konsistenzchecks in `docs/build/html/needs.json`.

Der Build läuft mit `-W`, Warnungen sind also Fehler: Eine ID, die nicht dem
Schema in `conf.py` entspricht, oder ein Status außerhalb des erlaubten
Lebenszyklus bricht den Build ab. Zum Bauen eines Zwischenstands lässt sich
das überschreiben:

```bash
make html SPHINXOPTS=
```

## Geplanter Ausbau

- Freigabeanfragen für Trainerqualifikationen
- Planung von Schulungsterminen und Zuweisung qualifizierter Trainer
- Assistenzplätze an Terminen als Weg zum Anlernen
- Schulungs- und Abwesenheitskalender
- Traineransicht für passende zukünftige Termine und Vormerkungen
- Übernahmeanfragen zwischen Trainern
- Teilnehmerbuchungen mit Aufbewahrungsfrist

Benutzerkonten und Schulungskatalog verwenden
`spring-boot-starter-security`, `spring-boot-starter-validation`,
`vue-router` und JGit.

### Datenhaltung

Die Daten liegen an zwei Orten, getrennt entlang der Frage, ob etwas versioniert gehört:

- **Der Schulungskatalog** bleibt in JSON-Dateien, je Schulung eine, im Repository versioniert. Er enthält nur die Beschreibung — Titel, Kategorie, Kurzbeschreibung, Voraussetzungen, Dauer und die beiden Teilnehmergrenzen. Eine Änderung daran schreibt die Datei und sichert sie mit einem Commit.
- **Alles Veränderliche** — Benutzerkonten, Termine, Trainerzuweisungen, Qualifikationen, Abwesenheiten, Teilnehmerbuchungen und Benachrichtigungen — liegt in einer eingebetteten H2-Datenbank im Dateimodus unter `backend/data/`. Sie braucht keine eigene Installation und keinen Serverprozess; das Verzeichnis wird nicht versioniert.

Der Grund für die Trennung: Eine Katalogänderung ist eine seltene, bewusste Handlung und gehört in die Versionsgeschichte. Laufender Betriebszustand nicht — Benachrichtigungen allein würden die Historie mit Commits fluten. Die Schulungs-ID ist die Klammer zwischen beiden Ablagen.

Gleichzeitiges Bearbeiten wird optimistisch aufgelöst: Jeder Datensatz führt einen Änderungszähler, und ein Speichern auf einem überholten Stand wird abgewiesen. Gesperrt wird nichts; wer einen Datensatz offen hat, erscheint als Hinweis.

Qualifikationen werden an der Schulung geführt, nicht am Benutzerkonto.

Die Entscheidungen dazu stehen als `DEC_DAT_ABLAGE_02`, `DEC_DAT_SCHNITT_01`, `DEC_DAT_SCHEMA_01` und `DEC_DAT_NEBEN_01` in den Anforderungen.

### Berechtigungen

Ein Benutzerkonto trägt eine **Menge** von Rollen: Trainer, Administrator, oder beide. Die Tabelle beschreibt die einzelne Rolle; wer mehrere trägt, erhält die Summe der Spalten. Der Normalfall für einen Administrator ist, zusätzlich Trainer zu sein — nur so kann er qualifiziert werden, assistieren und eigene Abwesenheiten führen.

| Aktion                                  | Eigentümer | Administrator | Trainer |
| --------------------------------------- | :--------: | :-----------: | :-----: |
| Schulungen und Termine ansehen          |     ja     |      ja       |   ja    |
| Schulungen verwalten                    |     ja     |      ja       |  nein   |
| Termine planen und Trainer zuweisen     |     ja     |      ja       |  nein   |
| Freigabeanfrage entscheiden             |     ja     |      ja       |  nein   |
| Abwesenheitsantrag entscheiden          |     ja     |      ja       |  nein   |
| Teilnehmerbuchungen pflegen             |     ja     |      ja       |  nein   |
| Rollen erteilen und entziehen           |     ja     |      ja       |  nein   |
| Eigentümerrolle weitergeben             |     ja     |     nein      |  nein   |
| Qualifikation anfragen                  |    nein    |     nein      |   ja    |
| Auf einen Assistenzplatz bewerben       |    nein    |     nein      |   ja    |
| Eigene Abwesenheiten verwalten          |    nein    |     nein      |   ja    |
| Passende Termine ansehen und vormerken  |    nein    |     nein      |   ja    |
| Übernahme anbieten und entscheiden      |    nein    |     nein      |   ja    |
| Eigenen Termin abschließen              |    nein    |      ja       |   ja    |

Sobald ein Benutzerkonto existiert, trägt genau ein aktives Konto die Rolle **Eigentümer**. Sie sichert eine Instanz dagegen, sich selbst auszusperren: Ihr Träger behält zwingend die Administratorrolle, kann weder stillgelegt noch gelöscht werden, und gibt die Rolle nur atomar an ein anderes aktives Konto weiter. Das erste registrierte Konto erhält alle drei Rollen.

Die Standardkonfiguration bindet die Anwendung ausschließlich an die lokale Loopback-Schnittstelle und deaktiviert die H2-Konsole. Eine Erreichbarkeit von anderen Rechnern erfordert eine bewusst geänderte Sicherheitskonfiguration.

Ein Administrator entscheidet auch über Vorgänge, die er selbst ausgelöst hat — sonst wäre eine Instanz mit einem einzigen Administrator blockiert.

„Buchung“ bezeichnet ausschließlich eine Teilnehmerbuchung. Administratoren planen Termine; Trainer können sich dafür vormerken, aber nicht selbst zuweisen.
