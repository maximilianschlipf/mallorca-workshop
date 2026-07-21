# Feature 1: Katalog durchsuchen und filtern

## Lernfokus

Prompting-Grundlagen, Plan vor Implementierung, Git-Diff und erster Feature-Commit.

## Fachlicher Auftrag

Planer können Schulungen über einen Teil des Titels suchen und nach Kategorie filtern.

## Akzeptanzkriterien

- Die Titelsuche ignoriert Groß-/Kleinschreibung.
- Führende und nachfolgende Leerzeichen werden ignoriert.
- Suche und Kategorie lassen sich kombinieren.
- Beide Filter lassen sich gemeinsam zurücksetzen.
- Ohne Treffer erscheint ein verständlicher Leerzustand.
- Ohne aktive Filter bleibt die vorhandene vollständige Liste sichtbar.

## KI-Aufgabe

Copilot erhält zunächst ohne Schreibrechte nur den Satz „Baue Suche und Filter für Schulungen ein“. Das Team markiert fehlenden Kontext, Annahmen und unklare Prüfkriterien. Anschließend formuliert es einen zweiten Auftrag mit Ziel, Kontext, Grenzen und Definition of Done und lässt ausschließlich einen Plan erzeugen.

## Verifikation

- Vitest deckt kombinierte Filterung, Leerzeichen und Leerzustand ab.
- Playwright deckt mindestens einen sichtbaren Such- und Reset-Ablauf ab.
- Das Team prüft den vollständigen Diff vor dem Commit.
