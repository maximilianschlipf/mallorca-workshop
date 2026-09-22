# Aufgabe 6 -- Stories und Testfälle für Vormerkungen ableiten

**Schwerpunkt:** Testfallableitung und Anforderungsanalyse

**Arbeitszeit:** 50 bis 60 Minuten; die Erweiterung ist optional.

## Vorbereitung

1. Die Regeln in [AGENTS.md](AGENTS.md) lesen.
2. Die Begriffe in [CONTEXT.md](CONTEXT.md) und die Anforderungen in
   `docs/source/anforderungen/vormerkungen.rst` lesen.
3. Die Anwendung bei Bedarf mit reproduzierbaren Demodaten starten:

   ```bash
   APP_DEMO_SEED=true ./start.sh
   ```

   Dabei wird die lokale Datenbank zurückgesetzt. Testkonten stehen in der
   [README.md](README.md).

## Aufgabe

Einen zusammenhängenden Vormerkungsprozess auswählen und daraus prüfbare
Stories und Testfälle als sphinx-needs ableiten:

- Vormerkung und Rücknahme,
- Entscheidung und Benachrichtigung oder
- Konfliktprüfung.

### Verbindlicher Umfang

- Höchstens zwei `STORY_VOR_*`-Needs mit `:implements:` erstellen.
- Höchstens drei `TEST_VOR_*`-Needs mit `:verifies:` erstellen.
- Voraussetzungen, Aktion und erwartetes Ergebnis eindeutig beschreiben.
- Nicht spezifizierte Randfälle als offene Fragen festhalten, statt Verhalten
  zu erfinden.
- Status und geplante Automatisierbarkeit angeben. Ohne ausführbaren Nachweis
  bleibt ein Test-Need `draft`, `review` oder `approved`, niemals `verified`.
- Den Dokumentationsbuild erfolgreich ausführen.

### Optionale Erweiterung

Innerhalb derselben Obergrenzen einen zweiten Teilprozess betrachten oder
weitere fachliche Lücken als konkrete Rückfragen dokumentieren.

## Abgrenzung

Es wird kein Produktcode implementiert. Änderungen an bestehenden
Anforderungen werden als Vorschlag oder offene Frage dokumentiert.
