# Testing

## Backend

```bash
cd backend
./mvnw test
```

Erwartung: Seed-Ressourcen, Datenbankinhalt und `GET /api/schulungen` werden erfolgreich geprüft.

## Frontend

```bash
cd frontend
npm run test
npm run build
```

Erwartung: Der Vitest-Komponententest und der TypeScript-/Produktionsbuild laufen grün.

## Ende-zu-Ende

```bash
cd frontend
npm run test:e2e
```

Playwright startet Backend und Frontend selbst. Der Smoke-Test prüft die API über den Frontend-Proxy und einen sichtbaren Katalogeintrag.

## Fehler eingrenzen

- Backend rot: Seed-Import, Schema und API prüfen.
- Frontend rot: Rendering, Typfehler und API-State prüfen.
- Playwright rot: Backend-Health, Vite-Proxy, Selektoren und Trace prüfen.
