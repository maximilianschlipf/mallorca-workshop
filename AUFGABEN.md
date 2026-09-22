# Aufgabe 1 -- Eigene Abwesenheit ändern

**Schwerpunkt:** Entwicklung

**Arbeitszeit:** 50 bis 60 Minuten; die Erweiterung ist optional.

## Vorbereitung

1. Die Regeln in [AGENTS.md](AGENTS.md) lesen.
2. Die Begriffe in [CONTEXT.md](CONTEXT.md) sowie die Needs in
   `docs/source/anforderungen/abwesenheiten.rst` und
   `docs/source/anforderungen/abwesenheiten-umsetzung.rst` lesen.
3. Die Anwendung bei Bedarf mit reproduzierbaren Demodaten starten:

   ```bash
   APP_DEMO_SEED=true ./start.sh
   ```

   Dabei wird die lokale Datenbank zurückgesetzt. Testkonten stehen in der
   [README.md](README.md).

## Aufgabe

Den Backend-Ablauf zum Ändern einer eigenen Abwesenheit umsetzen.

### Verbindlicher Umfang

- `STORY_ABW_ERF_02` und `TEST_ABW_ERF_03` erfüllen.
- Zeitraum oder Grund einer eigenen Abwesenheit änderbar machen.
- Änderungen an fremden Abwesenheiten abweisen und deren Werte unverändert
  lassen.
- Das vollständige Szenario mit einem ausführbaren Test nachweisen.
- Dokumentation, Status und Testnachweis gemäß den Regeln aktualisieren.

### Optionale Erweiterung

Das Löschen einer eigenen Abwesenheit gemäß `STORY_ABW_ERF_03` und
`TEST_ABW_ERF_04` umsetzen und testen.

## Abgrenzung

Eine Erweiterung der Oberfläche und `REQ_ABW_PRUEF_04` sind nicht Teil der
Aufgabe.
