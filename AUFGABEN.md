# Aufgabe 3 -- Zeitraumregeln testgetrieben umsetzen

**Schwerpunkt:** Testgetriebene Entwicklung

**Arbeitszeit:** 50 bis 60 Minuten; die Erweiterung ist optional.

## Vorbereitung

1. Die Regeln in [AGENTS.md](AGENTS.md) lesen.
2. Die Needs in `docs/source/anforderungen/terminplanung.rst` und
   `docs/source/anforderungen/terminplanung-umsetzung.rst` lesen.
3. Die Anwendung bei Bedarf mit reproduzierbaren Demodaten starten:

   ```bash
   APP_DEMO_SEED=true ./start.sh
   ```

   Dabei wird die lokale Datenbank zurückgesetzt. Testkonten stehen in der
   [README.md](README.md).

## Aufgabe

Die Zeitraumprüfung beim Anlegen eines Termins in einem vollständigen
Rot-Grün-Aufräumen-Zyklus umsetzen.

### Verbindlicher Umfang

- `TEST_TER_ANL_04` zuerst als ausführbaren Test schreiben und rot ausführen.
- Nachweisen, dass ein Ende vor dem Start abgewiesen wird.
- Nachweisen, dass Start und Ende am selben Tag zulässig sind.
- Nur die notwendige Implementierung ergänzen und den Test grün ausführen.
- Zeitabhängige Testdaten mit einer festen `Clock` deterministisch halten.
- Den Testnachweis und den Need-Status erst nach dem vollständigen Szenario
  aktualisieren.

### Optionale Erweiterung

`TEST_TER_ANL_05` im selben Ablauf umsetzen: Ein Start in der Vergangenheit
wird abgewiesen, ein Start am heutigen Tag wird angenommen.

## Abgrenzung

Weitere Terminregeln, Trainerzuweisung, Assistenzplätze und
Teilnehmerbuchungen sind nicht Teil der Aufgabe.
