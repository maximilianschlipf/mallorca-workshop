# Workshop-Aufgabe

Dieses Dokument beschreibt die Aufgabe dieses Branches für den Workshop rund
um die SimplyTest Academy (diesen Schulungsplaner).

Grundlage der Aufgabe:

- Die Fachsprache in [CONTEXT.md](CONTEXT.md).
- Die Anforderungen unter `docs/source/anforderungen/` (Quelle; gebaut mit
  Sphinx und sphinx-needs, siehe [README.md](README.md)). Jeder Bereich
  trägt ein Kürzel (`USR`, `QUA`, `KAT`, `TER`, `ASS`, `VOR`, ...), das in
  jeder Need-ID wiederkehrt.
- Der bereits umgesetzte Schulungskatalog (`de.nordwind.schulungsplaner.katalog`
  im Backend, die Ansichten unter `frontend/src/ansichten`) als Beispiel für
  Stil und Zuschnitt: kleine, reine Klassen für Prüfregeln, Page-Object-Muster
  unter `frontend/tests/e2e/seiten` und ein Need pro
  Anforderung/Story/Test mit nachvollziehbarem Testnachweis.

**Ausgangslage:** Dieser Aufgaben-Branch wurde aus einem passenden
historischen Produktstand aufgebaut, gezielt zurückgeschnitten und mit den
aktuellen Prüfskripten versehen. Sein
`implemented-requirements.txt` enthält nur die Bereiche, die in diesem
Ausgangsstand bereits vollständig umgesetzt und nachgewiesen sind. Die
branch-lokale `AGENTS.md` kennzeichnet den bearbeiteten Workshop-Ausschnitt
ausdrücklich als unvollständige Übung:
Er wird nicht als umgesetzter Fachbereich registriert, und die Teilnehmer
melden keine Feature-Vollständigkeit. Erst eine spätere vollständige Umsetzung
unterliegt wieder dem normalen kumulativen Abschluss-Gate.

### Anwendung mit Demodaten starten

Vor der Bearbeitung die Anwendung aus dem Repository-Wurzelverzeichnis mit
reproduzierbaren Demodaten starten:

```bash
APP_DEMO_SEED=true ./start.sh
```

Dabei wird die lokale Datenbank bei jedem Start zurückgesetzt.

### Zeitlicher Rahmen

Der **Kernumfang** ist auf etwa 50 bis 60 Minuten Arbeitszeit begrenzt.
Schnelle Gruppen bearbeiten anschließend die **optionale Erweiterung**. Der
übrige Zeitraum des zweistündigen Workshops ist für Einführung, Branch-Auswahl
und einen kurzen Ergebnisabgleich vorgesehen.

Die vollständigen Fachbereiche bleiben als Ausblick sichtbar, sind aber nicht
das Abnahmekriterium des Workshops. Entscheidend ist ein kleiner,
nachvollziehbarer Agenten-Workflow mit einem überprüfbaren Ergebnis.

---

## Aufgabe 3 -- KI-gestützter Testprozess für die Terminerstellung

**Schwerpunkt:** Testgetriebene Entwicklung / Testautomatisierung

### Ausgangslage

Für die Terminplanung bleiben Anforderungen, Stories und Tests als Needs
vorhanden (`docs/source/anforderungen/terminplanung.rst` und
`terminplanung-umsetzung.rst`, Kürzel `TER`). Die Terminerstellung ist bis auf
zwei bewusst entfernte Zeitraumprüfungen umgesetzt und der Aufgaben-Branch
baut von Beginn an. Für `TEST_TER_ANL_04` fehlt die Prüfung "Ende vor Start",
für `TEST_TER_ANL_05` die Prüfung "Start in der Vergangenheit". Ihre
ausführbaren Testnachweise wurden entfernt; die Test-Needs bleiben als
fachliche Testbeschreibungen erhalten. Alle anderen Terminregeln und Tests
bleiben als Referenz und Integrationsgrenze bestehen.

### Ziel

Nicht das Ergebnis steht im Vordergrund, sondern der Weg: Wie lässt sich mit
KI-Unterstützung ein test-getriebener Prozess für die Terminerstellung
aufbauen, der Rot-Grün-Zyklen konsequent einhält und pro ausgewähltem
Test-Need (`TEST_TER_...`) einen nachvollziehbaren Schritt erzeugt?

### Vorgehen

Für `TEST_TER_ANL_04` und optional `TEST_TER_ANL_05`: Test zuerst schreiben
(mit KI-Unterstützung aus der Formulierung des Needs ableiten), scheitern
lassen, Implementierung nachziehen und am vollständigen ausführbaren Szenario
`// verifies: TEST_TER_...` ergänzen. Ein Test-Need darf erst dann auf
`verified` gesetzt werden, wenn der Test das gesamte beschriebene Szenario
nachweist. Bewusst dokumentieren, an welchen Stellen die KI-generierten
Tests von Hand nachgeschärft werden mussten und warum.

### Workshop-Scope

**Kernumfang:** `TEST_TER_ANL_04` als vollständigen Rot-Grün-Zyklus
bearbeiten: Ende vor Start wird abgewiesen, Start und Ende am selben Tag
werden angenommen.

**Optionale Erweiterung:** `TEST_TER_ANL_05` als zweiten Rot-Grün-Zyklus
bearbeiten: Start in der Vergangenheit wird abgewiesen, Start am heutigen Tag
wird angenommen.

### Abgrenzung

Fokus auf die beiden Zeitraumregeln beim Anlegen. Weitere Schreiblogik,
Trainerzuweisung, Assistenzplätze und Teilnehmerbuchungen bleiben unverändert
und sind nicht Teil dieser Aufgabe.
