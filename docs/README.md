# Schulungsplaner Docs

Diese Dokumentation beschreibt das Demo-Grundgeruest fuer den Schulungsplaner.

## Ziel

Schneller, reproduzierbarer lokaler Start mit:

- Backend: Spring Boot + H2 In-Memory + Seed-Daten
- Frontend: Vue 3 + TypeScript + Vite
- Verifikation: Backend-Smoke, Frontend-Smoke (Vitest), Playwright-Smoke

## Schnellstart (2 Kommandos)

1. Backend starten (Port 18081)
2. Frontend starten (Port 15173)

Details stehen in:

- [setup.md](setup.md)
- [runbook.md](runbook.md)
- [testing.md](testing.md)

## Dokumente

- [setup.md](setup.md): Voraussetzungen, Installation, Start
- [runbook.md](runbook.md): Alltagsablaeufe, Seed-Reset, Troubleshooting
- [architecture-overview.md](architecture-overview.md): Monorepo und Datenfluss
- [testing.md](testing.md): Smoke-Test Strategie und Kommandos
- [feature-briefings/mvp-katalog-read.md](feature-briefings/mvp-katalog-read.md): MVP Scope und Demo-Skript
- [adr/ADR-001-mvp-scope.md](adr/ADR-001-mvp-scope.md): Scope-Entscheidung
