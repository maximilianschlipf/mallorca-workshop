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

## Aufgabe 1 -- Trainerabwesenheit erweitern

**Schwerpunkt:** Entwicklung

### Ausgangslage

Benutzerkonten, Katalog, Terminplanung und Qualifikationen bleiben als
Voraussetzungen erhalten. Sie werden für diese Aufgabe nicht
zurückgeschnitten.

Für Abwesenheiten bleiben Anforderungen, User Stories und Testbeschreibungen
als Needs erhalten
(`docs/source/anforderungen/abwesenheiten.rst` und
`abwesenheiten-umsetzung.rst`, Kürzel `ABW`). Das Eintragen einer eigenen
Abwesenheit bleibt erhalten, weil dieser Ablauf bereits eine geprüfte
Trainerfunktion des Benutzerkonten-Bereichs ist. Ändern und Löschen sind im
historischen Aufgabenstand noch nicht umgesetzt; ausführbare
`TEST_ABW_...`-Nachweise gibt es ebenfalls nicht. Das Datenbankschema, die
lesende Konfliktprüfung der Terminplanung und das `Abwesenheit`-Record bleiben
als Integrationsgrenze erhalten. Was eine Abwesenheit ist, und die
Unterscheidung von Trainerzuweisung, Vormerkung und Übernahmeanfrage stehen in
[CONTEXT.md](CONTEXT.md).

### Ziel

Die vorhandene Erfassung um das Ändern einer eigenen Abwesenheit erweitern.

### Workshop-Scope

**Kernumfang:** `STORY_ABW_ERF_02` mit `TEST_ABW_ERF_03`: Zeitraum oder Grund
einer eigenen Abwesenheit über einen Backend-Endpunkt ändern, den Zugriff auf
fremde Abwesenheiten abweisen und den Testnachweis herstellen. Eine
Erweiterung der Oberfläche gehört nicht zum Kernumfang.

**Optionale Erweiterung:** `STORY_ABW_ERF_03` mit `TEST_ABW_ERF_04` zum
Löschen einer eigenen Abwesenheit. `REQ_ABW_PRUEF_04` und die übrigen
Konfliktregeln sind Folgeaufgaben nach dem Workshop.

### Vorgehen

Der Bereich ist bewusst in sieben aufeinander aufbauende Gruppen von Stories
zerschnitten. Sie zeigen den weiteren Ausbau nach dem Workshop. Die
Reihenfolge zwischen den Gruppen ist nicht beliebig -- eine spätere Gruppe
setzt die vorherige voraus:

1. **Grunderfassung** -- Abwesenheit anlegen (im Aufgabenstand bereits
   vorhanden), ändern und löschen, zunächst ohne Konfliktprüfung.
2. **Zuweisungskonflikt** -- kollidiert die Abwesenheit mit einem
   zugewiesenen Termin, entfällt die Zuweisung vorerst sofort. Das ist ein
   bewusster Zwischenstand.
3. **Verfügbarkeitskonflikt** -- rein informativer Hinweis, wenn der Trainer
   einen Termin durch die Abwesenheit nicht mehr übernehmen könnte.
4. **Genehmigungsverfahren** -- ersetzt das sofortige Entfernen aus Gruppe 2
   durch einen Antrag, den ein Administrator bestätigt oder ablehnt.
5. **Fristablauf** -- ein unentschiedener Antrag gilt eine Woche vor Beginn
   automatisch als genehmigt; dazu der Mindestvorlauf beim Eintragen.
6. **Grund-Regeln** -- Sichtbarkeit des Grundes für Administratoren, Hinweis
   auf Gesundheitsdaten.
7. **Tausch mit einem Ersatztrainer** -- Alternative zum Genehmigungsantrag:
   Der Trainer schlägt einen bereits qualifizierten Ersatztrainer vor, statt
   einen Administrator zu behelligen.

Am Zuschnitt des Katalog-Branches orientieren: Je Story einen Test
schreiben, rot sehen, umsetzen, grün, aufräumen, eigener Commit. Bei Gruppe
4 ausdrücklich mitbedenken, dass sie den Test aus Gruppe 2
(`TEST_ABW_PRUEF_01`) ablöst -- das steht so auch im Kopf von
`abwesenheiten-umsetzung.rst` und ist eine gute Gelegenheit, über den
Umgang mit Tests zu sprechen, die durch eine spätere Anforderungsänderung
hinfällig werden.

Im Workshop sind nur Needs mit Status `approved` verbindliche
Umsetzungsgrundlage. Needs im Status `review` dürfen zur Diskussion genutzt,
aber nicht stillschweigend als freigegebener Umfang behandelt werden. Das ist
insbesondere für die späteren Gruppen relevant.

### Abgrenzung

Nicht Teil der Aufgabe: Was mit offenen Vormerkungen oder
Übernahmeanfragen beim Eintreten einer Abwesenheit geschieht. Im
Referenzsystem existieren dafür bereits gemeinsame technische Strukturen;
sie erweitern den fachlichen Umfang dieser Aufgabe nicht. Maßgeblich sind die
`implements`- und `verifies`-Beziehungen der `ABW`-Needs.
