# Architekturüberblick

## Monorepo

- `backend/`: Spring Boot, Spring JDBC und H2 In-Memory
- `frontend/`: Vue 3, TypeScript und Vite
- `docs/`: technische Referenz und Workshopaufgaben
- `.github/copilot-instructions.md`: repositoryweiter Kontext für GitHub Copilot

## Datenquellen

- `backend/src/main/resources/seed/schulungen.json`
- `backend/src/main/resources/seed/trainer.json`

Der `SeedService` liest beide Classpath-Ressourcen beim Backend-Start und befüllt H2 einschließlich Trainerqualifikationen.

## Datenfluss

1. Das Frontend ruft `/api/schulungen` über den Vite-Proxy auf.
2. Das Backend liest den initialisierten Datenbestand über Spring JDBC.
3. Das Frontend rendert den Schulungskatalog.

Die vorhandene Katalogansicht ist die vorbereitete Ausgangsbasis. Weitere Funktionen werden über die Feature-Briefings ergänzt.
