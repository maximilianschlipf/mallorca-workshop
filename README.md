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

## Verifizieren

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

Der E2E-Befehl startet Backend und Frontend bei Bedarf selbst.

## Dokumentation

- [Fachsprache](CONTEXT.md)

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

- Benutzerkonten mit den Rollen Administrator und Trainer
- Freigabeanfragen für Trainerqualifikationen
- Planung von Schulungsterminen und Zuweisung qualifizierter Trainer
- Schulungs- und Abwesenheitskalender
- Traineransicht für passende zukünftige Termine und Vormerkungen
- validierter JSON-Import für Schulungsdaten

Die bestehende Architektur bleibt bestehen. Bei der jeweiligen Umsetzung kommen nur `spring-boot-starter-security`, `spring-boot-starter-validation` und `vue-router` hinzu. H2 bleibt für lokale, reproduzierbare Workshop-Instanzen ausreichend.

Importierte JSON-Dateien sind nur Eingabe. Nach Validierung und Bestätigung ist die Datenbank die maßgebliche Datenquelle; eine Ablage der Originaldateien ist vorerst nicht vorgesehen.

### Berechtigungen

| Aktion                                 | Administrator | Trainer |
| -------------------------------------- | :-----------: | :-----: |
| Schulungen und Termine ansehen         |      ja       |   ja    |
| Schulungen verwalten oder importieren  |      ja       |  nein   |
| Termine planen und Trainer zuweisen    |      ja       |  nein   |
| Qualifikation anfragen                 |     nein      |   ja    |
| Freigabeanfrage entscheiden            |      ja       |  nein   |
| Eigene Abwesenheiten verwalten         |     nein      |   ja    |
| Passende Termine ansehen und vormerken |     nein      |   ja    |

„Buchung“ bezeichnet künftig ausschließlich eine Teilnehmerbuchung. Administratoren planen Termine; Trainer können sich dafür vormerken, aber nicht selbst zuweisen.
