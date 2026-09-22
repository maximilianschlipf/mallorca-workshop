# Aufgabe 2 -- Freigabeanfrage genehmigen

**Schwerpunkt:** Entwicklung und Anforderungsanalyse

**Arbeitszeit:** 50 bis 60 Minuten; die Erweiterung ist optional.

## Vorbereitung

1. Die Regeln in [AGENTS.md](AGENTS.md) lesen.
2. Die Begriffe in [CONTEXT.md](CONTEXT.md) und die Anforderungen in
   `docs/source/anforderungen/qualifikationen.rst` lesen.
3. Vorhandene Umsetzungsdateien wie `katalog-umsetzung.rst` als Muster für
   Stories, Tests und Traceability verwenden.
4. Die Anwendung bei Bedarf mit reproduzierbaren Demodaten starten:

   ```bash
   APP_DEMO_SEED=true ./start.sh
   ```

   Dabei wird die lokale Datenbank zurückgesetzt. Testkonten stehen in der
   [README.md](README.md).

## Aufgabe

Den fachlichen Ablauf zum Genehmigen einer Freigabeanfrage beschreiben,
implementieren und testen.

### Verbindlicher Umfang

- Für `REQ_QUA_BEW_02` eine Story und ein bis zwei Test-Needs erstellen.
- Den Backend-Ablauf zur Genehmigung durch einen Administrator umsetzen.
- Bei Genehmigung die Qualifikation des Trainers für die Schulung anlegen.
- Das vollständige Szenario mit ausführbaren Tests nachweisen.
- Dokumentation, Status und Testnachweise gemäß den Regeln aktualisieren.

### Optionale Erweiterung

Die Ablehnung aus `REQ_QUA_BEW_10` mit der Begründung aus
`REQ_QUA_BEW_08` beschreiben und wahlweise implementieren und testen.

## Abgrenzung

Eine neue Oberfläche und die übrige Qualifikationsverwaltung sind nicht Teil
der Aufgabe.
