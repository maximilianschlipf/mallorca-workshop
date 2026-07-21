# ADR-001: MVP Scope auf Katalog-Read begrenzt

## Status

Accepted

## Kontext

Das Training priorisiert schnelle Inbetriebnahme und Vergleichbarkeit der Ergebnisse. Der volle Funktionsumfang (Kalender, Trainerverwaltung, Rollenablauf) waere fuer den Start zu breit.

## Entscheidung

Iteration 1 wird auf Katalog-Read begrenzt:

- Seed laden
- `GET /api/schulungen`
- Einfache Frontend-Liste
- Smoke-Tests fuer den Tool-Stack

## Konsequenzen

- Schnellere Onboarding-Zeit fuer Teilnehmende.
- Fruehe, stabile Verifikation von Build, Run und Tests.
- Erweiterungen folgen in spaeteren Iterationen auf einem stabilen Fundament.
