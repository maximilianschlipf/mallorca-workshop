# Architecture Overview

## Monorepo Zielstruktur

- backend/: Spring Boot Anwendung
- frontend/: Vue 3 + TypeScript + Vite Anwendung
- docs/: Projektdokumentation
- .github/: Copilot-Instruktionen und spaetere Erweiterungen

## Datenquellen

- schulungen.json
- trainer.json

Diese Dateien werden beim Backend-Start als Seed-Daten eingelesen.

## MVP Datenfluss

1. Frontend ruft `GET /api/schulungen` auf.
2. Backend liefert den Katalog aus dem initialisierten Datenbestand.
3. Frontend rendert eine einfache Schulungsliste.

## Nicht-Ziel in Iteration 1

- Kein vollstaendiger Kalender-Workflow
- Keine Trainer-Pflegeoberflaeche
