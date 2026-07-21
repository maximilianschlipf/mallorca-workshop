# Vorbereitete Ausgangsbasis: Katalog Read

## Bereits implementiert

- Seed-Daten werden beim Backend-Start aus dem Classpath geladen.
- `GET /api/schulungen` liefert den Schulungskatalog.
- Das Frontend zeigt eine einfache Schulungsliste.
- Backend-, Frontend- und Playwright-Smoke-Tests sind vorhanden.

## Startverifikation

1. `cd backend && ./mvnw test` ausführen.
2. `cd frontend && npm run test && npm run build` ausführen.
3. `cd frontend && npm run test:e2e` ausführen.
4. Prüfen, dass alle Befehle grün sind und `git status --short` leer bleibt.

## Fachlicher Ausgangspunkt

Kalenderinteraktion, Trainer-Matching und Pflege von Abwesenheiten sind noch nicht implementiert. Sie werden ausschließlich über die nummerierten Feature-Briefings erweitert.
