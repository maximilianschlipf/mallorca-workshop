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

### Zeitlicher Rahmen

Der **Kernumfang** ist auf etwa 50 bis 60 Minuten Arbeitszeit begrenzt.
Schnelle Gruppen bearbeiten anschließend die **optionale Erweiterung**. Der
übrige Zeitraum des zweistündigen Workshops ist für Einführung, Branch-Auswahl
und einen kurzen Ergebnisabgleich vorgesehen.

Die vollständigen Fachbereiche bleiben als Ausblick sichtbar, sind aber nicht
das Abnahmekriterium des Workshops. Entscheidend ist ein kleiner,
nachvollziehbarer Agenten-Workflow mit einem überprüfbaren Ergebnis.

---

## Aufgabe 2 -- Qualifikationen aus Anforderungen rekonstruieren

**Schwerpunkt:** Entwicklung (inklusive Anforderungsanalyse)

### Ausgangslage

Im Referenzsystem sind Qualifikationen bereits spezifiziert und umgesetzt.
Diese Aufgabe ist bewusst eine Rekonstruktionsübung, keine noch offene
Produktentwicklung. Der historische Aufgabenstand enthält bereits das Stellen
einer Freigabeanfrage, weil dieser Ablauf eine geprüfte Trainerfunktion des
Benutzerkonten-Bereichs ist. Der Aufgaben-Branch behält die Anforderungen in
`docs/source/anforderungen/qualifikationen.rst` (Kürzel `QUA`). Die Datei
`qualifikationen-umsetzung.rst`, der administrative Entscheidungsablauf und
seine ausführbaren Tests sind dort noch nicht vorhanden. Das Datenbankschema,
die Bewerbung und die lesende Qualifikationsprüfung der Terminplanung bleiben
als Integrationsgrenze erhalten. Benutzerkonten, Katalog und Terminplanung
bleiben als Voraussetzungen erhalten.

### Ziel

Erst User Stories und Tests als Needs entwerfen (nach dem Muster von
`katalog-umsetzung.rst` oder `benutzerkonten-umsetzung.rst`: Stories mit
`:implements:`, Tests mit `:verifies:`, dazu Status im Lebenszyklus), danach
umsetzen. Im Mittelpunkt steht die bestätigte
Berechtigung eines Trainers, eine Schulung durchzuführen, und der Antrag
darauf (Freigabeanfrage) -- beides ist in [CONTEXT.md](CONTEXT.md) definiert.

### Workshop-Scope

**Kernumfang:** Für `REQ_QUA_BEW_02` eine Story und ein bis zwei Test-Needs
entwerfen und anschließend den Backend-Ablauf zum Genehmigen einer
Freigabeanfrage umsetzen. Die vollständige Qualifikationsverwaltung und eine
neue Oberfläche gehören nicht zum Kernumfang.

**Optionale Erweiterung:** Die Ablehnung mit Begründung aus
`REQ_QUA_BEW_08` ergänzen oder nur Story und Tests dafür entwerfen. Die
übrigen Qualifikationsanforderungen bleiben Folgearbeit.

### Vorgehen

1. Anforderungen lesen, offene Fragen und fachübergreifende Folgen klären
   (z. B.: Bleiben bestehende Qualifikationen beim Archivieren einer Schulung
   erhalten? Was geschieht beim Löschen der Schulung?). Bereits beantwortete
   Fragen nicht erneut entscheiden.
2. Stories und Tests als Needs entwerfen, `make html` muss dabei durchlaufen
   (`-W`, Warnungen sind Fehler).
3. Umsetzen wie in Aufgabe 1.

### Abgrenzung

Setzt die im Aufgaben-Branch vorhandenen Benutzerkonten mit den Rollen
Trainer und Administrator voraus. Rollenprüfung, Katalog und Terminplanung
werden wiederverwendet und nicht für diese Aufgabe neu implementiert.
