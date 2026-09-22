# Aufgabe 4 -- Katalogverwaltung automatisiert prüfen

**Schwerpunkt:** Testautomatisierung und Qualitätssicherung

**Arbeitszeit:** 50 bis 60 Minuten; die Erweiterung ist optional.

## Vorbereitung

1. Die Regeln in [AGENTS.md](AGENTS.md) lesen.
2. Die Needs in `docs/source/anforderungen/katalog.rst` und
   `docs/source/anforderungen/katalog-umsetzung.rst` lesen.
3. Die Anwendung bei Bedarf mit reproduzierbaren Demodaten starten:

   ```bash
   APP_DEMO_SEED=true ./start.sh
   ```

   Dabei wird die lokale Datenbank zurückgesetzt. Testkonten stehen in der
   [README.md](README.md).

## Aufgabe

Für einen zusammenhängenden Ausschnitt der Katalogverwaltung automatisierte
Tests aus den zugehörigen `TEST_KAT_*`-Needs ableiten.

### Verbindlicher Umfang

- Höchstens zwei zusammengehörige Test-Needs auswählen.
- Mindestens einen Test an der fachlichen Backend-Grenze umsetzen.
- Das vollständige Verhalten des jeweiligen Test-Needs prüfen.
- Fehlschläge als Implementierungsfehler, unvollständigen Test oder
  mehrdeutige Anforderung einordnen.
- Testnachweise und Need-Status nur bei vollständiger Abdeckung aktualisieren.
- Die relevanten Tests und anschließend das vorgesehene Gate ausführen.

### Optionale Erweiterung

Für denselben Ausschnitt einen E2E-Test nach dem vorhandenen
Page-Object-Muster ergänzen oder einen weiteren Test-Need analysieren.

## Abgrenzung

Produktcode wird nur geändert, wenn ein tatsächlicher Fehler nachgewiesen und
getrennt dokumentiert wird. Eine vollständige Katalog-Testsuite ist nicht Teil
der Aufgabe.
