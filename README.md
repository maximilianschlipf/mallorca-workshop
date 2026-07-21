# Nordwind Academy Schulungsplaner

Vorbereitetes Monorepo für einen Workshop zu KI-gestützter Softwareentwicklung mit GitHub Copilot.

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

- [Setting](setting-schulungsplaner.md)
- [Setup](docs/setup.md)
- [Runbook](docs/runbook.md)
- [Architektur](docs/architecture-overview.md)
- [Testing](docs/testing.md)
- [Workshop-Arbeitszyklus](docs/workshop/arbeitszyklus.md)
- [Feature-Briefings](docs/feature-briefings)
