# Pilotcheck vor einer Workshopkohorte

## Frische Umgebung

- Repository in einen neuen Ordner klonen.
- Repo-Root in VS Code öffnen.
- Java 21 und Node.js 22 bestätigen.
- Im Frontend `npm ci` und einmalig `npx playwright install chromium` ausführen.
- Prüfen, dass die festen Ports `18081` und `15173` frei sind.

## Technische Verifikation

- Backendtests laufen grün.
- Frontendtests und Produktionsbuild laufen grün.
- Playwright startet beide Anwendungen selbst und läuft grün.
- Nach allen Tests ist `git status --short` leer.
- Die API liefert 8 Schulungen; die Datenbank enthält 5 Trainer und 10 Qualifikationen.

## Copilot-Verifikation

- Copilot referenziert `.github/copilot-instructions.md`.
- Ein Planungsauftrag erzeugt keine Änderungen, bevor die Umsetzung freigegeben wird.
- Playwright MCP ist im Agent-Modus sichtbar und kann die lokale Katalogseite öffnen.

## Didaktische Verifikation

- Alle drei Feature-Briefings sind aus dem Dokumentationsindex erreichbar.
- Hilfestufen und Rollenwechsel sind für fünf bis sechs Personen vorbereitet.
- Das Model Lab kann mit den am Schulungstag verfügbaren Modellen und Effort-Leveln durchgeführt werden.
- Für jedes Feature steht ein frisches Learning Log bereit.
