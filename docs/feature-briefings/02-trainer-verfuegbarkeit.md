# Feature 2: Geeignete und verfügbare Trainer ermitteln

## Lernfokus

Kontextmanagement, Model Lab, Effort-Wahl, Geschäftsregeln und Grenzfalltests.

## Fachlicher Auftrag

Für eine Schulung und einen gewünschten Zeitraum werden fachlich qualifizierte und verfügbare Trainer angezeigt.

## Akzeptanzkriterien

- Die Schulungs-ID ist in den Qualifikationen des Trainers enthalten.
- Abwesenheitszeiträume gelten einschließlich beider Grenztage.
- Ein Anfangsdatum nach dem Enddatum wird als ungültige Anfrage abgelehnt.
- Treffer werden alphabetisch nach Trainername sortiert.
- Für keine verfügbaren Trainer erscheint ein eigener UI-Zustand.
- Eine Abwesenheit, die genau am ersten Schulungstag beginnt, macht den Trainer nicht verfügbar.

## Kontext-Gate

Vor dem Plan dokumentiert das Team höchstens fünf notwendige Dateien und mindestens zwei bewusst ausgeschlossene Dateien. Copilot muss offene fachliche Fragen nennen, bevor es einen Implementierungsplan erzeugt.

## Verifikation

- Backendtests decken vollständig getrennte, teilweise überlappende, vollständig enthaltene und exakt angrenzende Datumsintervalle ab.
- Frontendtest deckt Treffer-, Leer- und Fehlerzustand ab.
- Das Model Lab wird vor der Implementierung durchgeführt.
