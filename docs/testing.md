# Testing

## Ziel

Mit minimalem Aufwand den Tool-Stack verifizieren.

## 1. Backend-Smoke

Kriterium:

- `GET /api/schulungen` liefert HTTP 200 und eine nicht-leere Liste.

Beispiel:

```bash
curl -i http://localhost:18081/api/schulungen
```

## 2. Frontend-Smoke (Vitest)

Kriterium:

- Mindestens ein Unit-/Component-Test fuer die Katalogdarstellung laeuft gruen.

Beispiel:

```bash
cd frontend
npm run test
```

## 3. Playwright E2E-Smoke

Kriterium:

- API-Antwort 200 ist vorhanden.
- Katalogliste ist im UI sichtbar.

Beispiel:

```bash
cd frontend
npx playwright test
```

## Fehlerinterpretation

- Backend-Smoke rot: API oder Seed-Import pruefen.
- Frontend-Smoke rot: Rendering/State der Liste pruefen.
- Playwright rot: Selektoren, API-Erreichbarkeit, Ladezeiten pruefen.
