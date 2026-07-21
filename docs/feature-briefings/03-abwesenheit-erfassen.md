# Feature 3: Abwesenheit erfassen

## Lernfokus

Selbstständige Agentennutzung, Erweiterung der Copilot-Instructions, Playwright MCP und Cross-Team-Review.

## Fachlicher Auftrag

Trainer können einen Abwesenheitszeitraum mit optionalem Grund erfassen.

## Akzeptanzkriterien

- Von- und Bis-Datum sind Pflichtfelder.
- Das Bis-Datum darf nicht vor dem Von-Datum liegen.
- Überlappende Abwesenheiten werden abgelehnt.
- Backendfehler werden verständlich im Frontend dargestellt.
- Nach erfolgreicher Speicherung wird die Abwesenheitsliste aktualisiert.

## KI-Gates

- Das Team organisiert alle neun Gates des Arbeitszyklus selbst.
- Genau eine wiederholt benötigte Regel wird aus einem beobachteten Problem in `.github/copilot-instructions.md` übernommen.
- Copilot prüft das laufende Feature mit Playwright MCP anhand der Akzeptanzkriterien.
- Ein relevanter MCP-Fund wird als dauerhafter Playwright-Test umgesetzt.
- Ein anderes Team reviewt den Pull Request und verlangt für jede Beanstandung einen Code-, Test- oder Verhaltensnachweis.
