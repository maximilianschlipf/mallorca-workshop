# Schulungsplaner Demo-Runbook

Dieses Runbook ist fuer den Schulungstag optimiert und kann 1:1 copy-paste verwendet werden.

## Voraussetzungen

- Java 21+
- Node.js 22 LTS+
- npm

## Standard-Ports

- Backend: 18081
- Frontend: 15173

## 1. Start (zwei Kommandos)

Terminal 1 (Backend):

```bash
cd /Users/philippe/Documents/Projects/assisted-coding/schulungsplaner/backend
./mvnw spring-boot:run
```

Terminal 2 (Frontend):

```bash
cd /Users/philippe/Documents/Projects/assisted-coding/schulungsplaner/frontend
npm install
npm run dev -- --port 15173
```

## 2. Kurzchecks

API-Check (HTTP 200 erwartet):

```bash
curl -i http://localhost:18081/api/schulungen
```

Frontend-Check:

- Browser oeffnen: http://localhost:15173
- Ueberschrift "Schulungskatalog" und mindestens eine Karte sichtbar

## 3. Smoke-Tests

Terminal 3 (Backend-Smoke):

```bash
cd /Users/philippe/Documents/Projects/assisted-coding/schulungsplaner/backend
./mvnw test
```

Terminal 3 (Frontend-Smoke mit Vitest):

```bash
cd /Users/philippe/Documents/Projects/assisted-coding/schulungsplaner/frontend
npm run test
```

Terminal 3 (Playwright E2E-Smoke):

```bash
cd /Users/philippe/Documents/Projects/assisted-coding/schulungsplaner/frontend
npx playwright install chromium
npm run test:e2e
```

## 4. Stoppen

- Backend-Terminal: Ctrl+C
- Frontend-Terminal: Ctrl+C

## 5. Seed-Reset

Die Datenbank ist H2 In-Memory. Ein sauberer Seed-Zustand wird so wiederhergestellt:

1. Backend stoppen (Ctrl+C)
2. Backend erneut starten mit `./mvnw spring-boot:run`
3. API erneut pruefen mit `curl -i http://localhost:18081/api/schulungen`

## 6. Portkonflikte

Backend auf anderem Port starten:

```bash
cd /Users/philippe/Documents/Projects/assisted-coding/schulungsplaner/backend
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=19081
```

Frontend auf anderem Port starten:

```bash
cd /Users/philippe/Documents/Projects/assisted-coding/schulungsplaner/frontend
npm run dev -- --port 16173
```
