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

## Aufgabe 4 -- KI-gestützte Prüfung der Katalogverwaltung

**Schwerpunkt:** Testautomatisierung / Qualitätssicherung

### Ausgangslage

Der Schulungskatalog bleibt im Aufgaben-Branch vollständig umgesetzt
(Backend und Oberfläche, siehe `de.nordwind.schulungsplaner.katalog` und
`frontend/src/ansichten`). Seine ausführbaren Backend-, Frontend- und
E2E-Tests werden für die Aufgabe entfernt. Anforderungen und Test-Needs
bleiben als Spezifikation erhalten; die Test-Needs werden von `verified` auf
`approved` zurückgesetzt und ihre bisherigen ``Besteht als ...``-Nachweise
entfernt. Auch die historische Einleitung von `katalog-umsetzung.rst` wird
durch eine neutrale Beschreibung des testlosen Ausgangsstands ersetzt
(`docs/source/anforderungen/katalog.rst` und
`katalog-umsetzung.rst`, Kürzel `KAT`).

### Ziel

Mit KI-Unterstützung eine automatisierte Testsuite gegen die bestehende
Implementierung aufbauen -- Backend-Tests und E2E-Tests nach dem
Page-Object-Muster (siehe `frontend/tests/e2e`) -- und dabei prüfen, wie
vollständig sie die in `katalog-umsetzung.rst` verlinkten Test-Needs
abdeckt.

### Workshop-Scope

**Kernumfang:** Einen zusammengehörigen Ausschnitt wählen und höchstens zwei
Test-Needs automatisieren, zum Beispiel das Anlegen einer Schulung oder das
Archivieren. Mindestens ein Test soll auf der fachlichen Backend-Grenze
liegen; ein E2E-Test ist möglich, aber nicht verpflichtend.

**Optionale Erweiterung:** Einen E2E-Test für denselben Ausschnitt ergänzen
oder einen weiteren Test-Need analysieren. Eine vollständige Rekonstruktion
der Katalog-Testsuite ist kein Workshop-Ziel.

### Vorgehen

1. Testsuite aus den Test-Needs ableiten, ohne zunächst in die (nicht mehr
   vorhandenen) ursprünglichen Testdateien zu schauen.
2. Gegen die laufende Implementierung ausführen.
3. Abweichungen einordnen: Ist ein fehlgeschlagener Test ein Fehler in der
   Implementierung, eine Lücke in der eigenen Testableitung, oder eine
   Anforderung, die mehrdeutig formuliert ist?
4. Am Ende einen kurzen Abgleich: Welche Test-Needs sind abgedeckt, welche
   nicht, und warum.

Nur ein Test, der das gesamte beschriebene Szenario ausführt, erhält
`// verifies: TEST_KAT_...`; erst dann darf der zugehörige Test-Need wieder
den Status `verified` erhalten.

### Abgrenzung

Die Implementierung selbst wird nicht verändert, außer es stellt sich ein
tatsächlicher Fehler heraus -- dann getrennt vom Testaufbau dokumentieren.
