# Nordwind Academy Schulungsplaner

## Projektziel

Dieses Repository ist ein vorbereitetes Übungsprojekt für KI-gestützte Softwareentwicklung. Die vorhandene Katalogansicht ist die stabile Ausgangsbasis; neue Funktionen werden als kleine vertikale Features ergänzt.

## Technische Leitplanken

- Backend: Java 21, Spring Boot, Spring JDBC und H2 In-Memory.
- Frontend: Vue 3 mit TypeScript, Composition API und Vite.
- Kommunikation: REST mit JSON unter `/api`.
- Seed-Daten liegen unter `backend/src/main/resources/seed`.
- UI-Texte und fachliche Bezeichner bleiben deutsch.
- Ersetze den vorhandenen Stack nicht und füge Abhängigkeiten nur mit nachvollziehbarer Begründung hinzu.
- Authentifizierung, Cloud-Dienste und externe Datenbanken gehören nicht zum Workshopumfang.

## Architekturgrenzen

- Controller behandeln HTTP-Belange.
- Services enthalten Geschäftslogik und Datenzugriff.
- Vue-Komponenten rufen API-Funktionen aus `frontend/src/api.ts` auf; direkte `fetch`-Aufrufe gehören nicht in Templates.
- Kleine, fokussierte Dateien und Funktionen sind größeren Sammelkomponenten vorzuziehen.

## Verifikation

- Backend: `cd backend && ./mvnw test`
- Frontend: `cd frontend && npm run test && npm run build`
- Ende-zu-Ende: `cd frontend && npm run test:e2e`
- Neue Geschäftsregeln benötigen fokussierte Tests für Happy Path, Grenzen und Fehlerfälle.

## Zusammenarbeit mit Copilot

- Nenne Annahmen und offene fachliche Fragen, bevor du einen Implementierungsplan erstellst.
- Plane vor der Implementierung und warte auf Freigabe, wenn der Auftrag nur Analyse oder Planung verlangt.
- Ändere nur Dateien, die für das aktuelle Feature erforderlich sind.
- Prüfe den abschließenden Diff und führe die relevanten Verifikationsbefehle aus.

## Workshop-Erweiterungen

Das Team ergänzt hier nur Regeln, die aus einem konkret beobachteten Problem abgeleitet, gemeinsam verstanden und anschließend überprüft wurden.
