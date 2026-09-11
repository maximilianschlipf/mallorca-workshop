# SimplyTest Academy

## Voraussetzungen

- Java 21
- Node.js 22 LTS und npm
- Visual Studio Code mit GitHub Copilot

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
