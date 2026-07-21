# MVP Briefing: Katalog Read

## Scope

Iteration 1 umfasst nur:

- Seed-Daten beim Backend-Start laden
- `GET /api/schulungen` bereitstellen
- Einfache Liste im Frontend rendern

## Akzeptanzkriterien

- API unter `/api/schulungen` liefert HTTP 200.
- Antwort enthaelt mindestens einen Schulungseintrag.
- Frontend zeigt mindestens einen Eintrag sichtbar an.
- Smoke-Tests (Backend, Frontend, Playwright) sind gruen.

## Nicht-Ziele

- Kalenderinteraktion
- Trainer-Abwesenheiten pflegen
- Rollen-/Rechte-Workflow im UI

## Demo-Skript (Kurz)

1. Beide Prozesse starten.
2. API im Terminal pruefen.
3. Frontend im Browser oeffnen.
4. Katalogliste zeigen.
5. Playwright-Smoke ausfuehren.
