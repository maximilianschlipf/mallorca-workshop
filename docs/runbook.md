# Runbook

## Tagesstart

1. Backend starten: `cd backend && ./mvnw spring-boot:run`
2. Frontend starten: `cd frontend && npm run dev`
3. API prüfen: `curl -i http://localhost:18081/api/schulungen`
4. http://localhost:15173 öffnen und den Schulungskatalog prüfen.

## Seed-Zustand zurücksetzen

Die Anwendung verwendet eine H2-In-Memory-Datenbank. Ein Neustart des Backends lädt den vollständigen Seed-Zustand erneut.

## Troubleshooting

1. Mit `java -version` Java 21 und mit `node --version` Node.js 22 prüfen.
2. Prüfen, ob die festen Ports `18081` und `15173` bereits belegt sind; den störenden Prozess beenden.
3. Backend-Log auf Fehler beim Import aus `backend/src/main/resources/seed` prüfen.
4. Mit `curl -i http://localhost:18081/api/health` den Backend-Status prüfen.
5. Bei leerer oder fehlerhafter UI den Vite-Proxy für `/api` prüfen.
6. Bei einem E2E-Fehler den erzeugten Playwright-Trace öffnen.
