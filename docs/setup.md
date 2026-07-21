# Setup

## Voraussetzungen

- Java 21
- Node.js 22 LTS und npm
- Maven Wrapper unter `backend/mvnw`
- Visual Studio Code mit GitHub Copilot

Die festen Workshopports `18081` für das Backend und `15173` für das Frontend müssen frei sein.

## Einmalige Installation

```bash
cd frontend
npm ci
npx playwright install chromium
```

## Anwendung starten

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

Danach sind die API unter http://localhost:18081/api/schulungen und das Frontend unter http://localhost:15173 erreichbar.

## Setup verifizieren

```bash
cd frontend
npm run test:e2e
```

Playwright startet Backend und Frontend für diesen Test selbst. Ein manueller Anwendungsstart ist dafür nicht erforderlich.
