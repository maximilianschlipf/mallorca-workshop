# Aufgaben

Dieses Dokument beschreibt Schulungsaufgaben rund um die SimplyTest Academy
(diesen Schulungsplaner). Jede Aufgabe setzt einen anderen Schwerpunkt --
Entwicklung, Testautomatisierung oder Anforderungsanalyse und
Testfallableitung -- damit Teilnehmer je nach Interesse und Rolle eine
passende Aufgabe wählen können.

Gemeinsame Grundlage aller Aufgaben:

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

**Ausgangslage:** Das vollständige Referenzsystem enthält inzwischen mehrere
der hier behandelten Bereiche. Für jede Aufgabe wird deshalb ein eigener
Aufgaben-Branch erstellt und gezielt zurückgeschnitten. Die folgenden
Ausgangslagen beschreiben den vorgesehenen Stand dieser noch zu erstellenden
Branches, nicht den aktuellen Referenzstand. Was erhalten bleibt und was
entfernt wird, steht bei jeder Aufgabe unter "Ausgangslage".

Die Aufgaben-Branches werden aus einem passenden historischen Produktstand
aufgebaut und anschließend mit den aktuellen Prüfskripten versehen. Ihr
`implemented-requirements.txt` enthält nur die Bereiche, die in diesem
Ausgangsstand bereits vollständig umgesetzt und nachgewiesen sind; bestehende
Einträge werden nicht entfernt. Eine branch-lokale `AGENTS.md` kennzeichnet
den bearbeiteten Workshop-Ausschnitt ausdrücklich als unvollständige Übung:
Er wird nicht als umgesetzter Fachbereich registriert, und die Teilnehmer
melden keine Feature-Vollständigkeit. Erst eine spätere vollständige Umsetzung
unterliegt wieder dem normalen kumulativen Abschluss-Gate.

---

## Aufgabe 1 -- Trainerabwesenheit umsetzen

**Schwerpunkt:** Entwicklung

### Ausgangslage

Benutzerkonten, Katalog, Terminplanung und Qualifikationen bleiben als
Voraussetzungen erhalten. Sie werden für diese Aufgabe nicht
zurückgeschnitten.

Für Abwesenheiten bleiben Anforderungen, User Stories und Testbeschreibungen
als Needs erhalten
(`docs/source/anforderungen/abwesenheiten.rst` und
`abwesenheiten-umsetzung.rst`, Kürzel `ABW`). Aus dem Aufgaben-Branch werden
die Endpunkte, Bedienoberflächen und Abläufe zur Verwaltung von Abwesenheiten
entfernt; ausführbare `TEST_ABW_...`-Nachweise gibt es im Referenzstand noch
nicht. Das Datenbankschema, die lesende Konfliktprüfung der Terminplanung und
das `Abwesenheit`-Record bleiben als Integrationsgrenze beziehungsweise
fachlicher Platzhalter erhalten. Was eine Abwesenheit ist, und die
Unterscheidung von Trainerzuweisung, Vormerkung und Übernahmeanfrage stehen
in [CONTEXT.md](CONTEXT.md).

### Ziel

Trainerabwesenheiten nach den vorliegenden Needs umsetzen -- Backend und,
soweit die Aufgabe reicht, Oberfläche.

### Vorgehen

Der Bereich ist bewusst in sieben aufeinander aufbauende Gruppen von Stories
zerschnitten, jede für sich klein genug für einen eigenen Umsetzungsschritt.
Die Reihenfolge zwischen den Gruppen ist nicht beliebig -- eine spätere
Gruppe setzt die vorherige voraus:

1. **Grunderfassung** -- Abwesenheit anlegen, ändern, löschen, ganz ohne
   Konfliktprüfung.
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

### Abgrenzung

Nicht Teil der Aufgabe: Was mit offenen Vormerkungen oder
Übernahmeanfragen beim Eintreten einer Abwesenheit geschieht. Im
Referenzsystem existieren dafür bereits gemeinsame technische Strukturen;
sie erweitern den fachlichen Umfang dieser Aufgabe nicht. Maßgeblich sind die
`implements`- und `verifies`-Beziehungen der `ABW`-Needs.

---

## Aufgabe 2 -- Qualifikationen und Freigabeanfragen entwerfen und umsetzen

**Schwerpunkt:** Entwicklung (inklusive Anforderungsanalyse)

### Ausgangslage

Im Referenzsystem sind Qualifikationen bereits spezifiziert und umgesetzt.
Der Aufgaben-Branch behält nur die Anforderungen in
`docs/source/anforderungen/qualifikationen.rst` (Kürzel `QUA`). Die Datei
`qualifikationen-umsetzung.rst`, die Verwaltungsabläufe für Qualifikationen
und Freigabeanfragen sowie ihre ausführbaren Tests werden dort entfernt. Das
Datenbankschema und die lesende Qualifikationsprüfung der Terminplanung
bleiben als Integrationsgrenze erhalten. Benutzerkonten, Katalog und
Terminplanung bleiben als Voraussetzungen erhalten.

### Ziel

Erst User Stories und Tests als Needs entwerfen (nach dem Muster von
`katalog-umsetzung.rst` oder `benutzerkonten-umsetzung.rst`: Stories mit
`:implements:`, Tests mit `:verifies:`, dazu Status im Lebenszyklus), danach
umsetzen. Im Mittelpunkt steht die bestätigte
Berechtigung eines Trainers, eine Schulung durchzuführen, und der Antrag
darauf (Freigabeanfrage) -- beides ist in [CONTEXT.md](CONTEXT.md) definiert.

### Vorgehen

1. Anforderungen lesen, offene Fragen klären (z. B.: Kann ein Administrator
   eine Freigabeanfrage stellen? Was passiert mit einer Qualifikation, wenn
   die zugehörige Schulung archiviert wird?).
2. Stories und Tests als Needs entwerfen, `make html` muss dabei durchlaufen
   (`-W`, Warnungen sind Fehler).
3. Umsetzen wie in Aufgabe 1.

### Abgrenzung

Setzt die im Aufgaben-Branch vorhandenen Benutzerkonten mit den Rollen
Trainer und Administrator voraus. Rollenprüfung, Katalog und Terminplanung
werden wiederverwendet und nicht für diese Aufgabe neu implementiert.

---

## Aufgabe 3 -- KI-gestützter Testprozess für die Terminerstellung

**Schwerpunkt:** Testautomatisierung

### Ausgangslage

Für die Terminplanung bleiben Anforderungen, Stories und Tests als Needs
vorhanden (`docs/source/anforderungen/terminplanung.rst` und
`terminplanung-umsetzung.rst`, Kürzel `TER`). Datenbankschema, gemeinsame
Typen, lesende Zugriffe und Integrationsmethoden bleiben erhalten, damit der
Aufgaben-Branch von Beginn an baut. Die fachliche Schreiblogik der Abschnitte
"Termine anlegen", "Zustände und Abschluss" sowie "Ändern, absagen,
löschen" wird für `TEST_TER_ANL_01` bis `TEST_TER_ANL_08`,
`TEST_TER_STAT_01` bis `TEST_TER_STAT_06` sowie `TEST_TER_AEND_04` und
`TEST_TER_AEND_05` auf kompilierbare Platzhalter zurückgesetzt; die
zugehörigen ausführbaren Testnachweise werden entfernt. Die Test-Needs bleiben
als fachliche Testbeschreibungen erhalten.

### Ziel

Nicht das Ergebnis steht im Vordergrund, sondern der Weg: Wie lässt sich mit
KI-Unterstützung ein test-getriebener Prozess für die Terminerstellung
aufbauen, der Rot-Grün-Zyklen konsequent einhält und pro ausgewähltem
Test-Need (`TEST_TER_...`) einen nachvollziehbaren Schritt erzeugt?

### Vorgehen

Je ausgewähltem Test-Need aus `TEST_TER_ANL_01` bis `TEST_TER_ANL_08`,
`TEST_TER_STAT_01` bis `TEST_TER_STAT_06`, `TEST_TER_AEND_04` und
`TEST_TER_AEND_05`: Test zuerst schreiben (mit KI-Unterstützung aus der
Formulierung des Needs ableiten), scheitern lassen, Implementierung nachziehen
und am vollständigen ausführbaren Szenario
`// verifies: TEST_TER_...` ergänzen. Ein Test-Need darf erst dann auf
`verified` gesetzt werden, wenn der Test das gesamte beschriebene Szenario
nachweist. Bewusst dokumentieren, an welchen Stellen die KI-generierten
Tests von Hand nachgeschärft werden mussten und warum.

### Abgrenzung

Fokus auf die Terminerstellung (Anlegen, Zeitraum, Zustand geplant/
abgeschlossen/abgesagt). Trainerzuweisung, Assistenzplätze und
Teilnehmerbuchungen werden als vorhandene Integrationsgrenzen und, wo ein
ausgewähltes Testszenario sie verlangt, als Test-Fixtures wiederverwendet.
Ihre fachliche Erweiterung ist nicht Teil dieser Aufgabe.

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

---

## Aufgabe 5 -- Refinement: Assistenzplätze weiterentwerfen

**Schwerpunkt:** Anforderungsanalyse

### Ausgangslage

Zu Assistenzplätzen gibt es eine knappe Sammlung von Anforderungen
(`docs/source/anforderungen/assistenz.rst`, Kürzel `ASS`), überwiegend noch
im Status `draft`. Im Referenzsystem existieren bereits technische
Teilfunktionen für Assistenzzuweisung und -bewerbung, aber noch keine
freigegebene, durch Stories und `TEST_ASS_...`-Nachweise vollständig
beschriebene Umsetzung. Der Aufgaben-Branch behält diese Teilfunktionen als
Anschauungsmaterial; sie sind keine Vorgabe für die fachliche Entscheidung.

### Ziel

Nicht Code entsteht hier, sondern ein tragfähiger Entwurf: Ein
KI-gestützter Refinement-Prozess, der aus der knappen Ausgangslage
vollständige, widerspruchsfreie Anforderungen und User Stories macht --
inklusive der Fragen, die dabei offenbleiben und im Team geklärt werden
müssten.

### Vorgehen

1. Vorhandene Anforderungen und den Kontext aus [CONTEXT.md](CONTEXT.md)
   lesen (Trainer, Qualifikation, Termin).
2. Mit der KI als Sparringspartner offene Fragen sammeln, z. B.: Braucht ein
   Assistenzplatz eine eigene Qualifikation oder eine geringere? Wie viele
   Assistenzplätze hat ein Termin? Kann sich ein Trainer selbst bewerben oder
   wird er zugewiesen? Was passiert bei Absage des Termins?
3. Fragen zu Entscheidungen verdichten, Entscheidungen als eigene Needs
   festhalten (`dec::`, wie z. B. `DEC_DAT_ABLAGE_02` im Bereich Speicher-
   schicht), Anforderungen und Stories entsprechend ergänzen.
4. Den entstandenen Entwurf kurz gegenüberstellen: Was hätte eine erste,
   naive Formulierung übersehen, die der Refinement-Prozess aufgedeckt hat?

### Abgrenzung

Kein Test-Need, kein Code. Ergebnis ist ein erweiterter, in sich
konsistenter `assistenz.rst` (und ggf. eine begonnene
`assistenz-umsetzung.rst`), der `make html` mit `-W` durchlaufen lässt.

---

## Aufgabe 6 -- Stories und Testfälle zu Vormerkungen ableiten (ohne Programmierung)

**Schwerpunkt:** Testfallableitung / Anforderungsanalyse

### Ausgangslage

Zu Vormerkungen liegen 14 Anforderungen als Needs vor
(`docs/source/anforderungen/vormerkungen.rst`, Kürzel `VOR`), aber noch keine
Stories und keine `TEST_VOR_...`-Needs. Im Referenzsystem existieren bereits
gemeinsame technische Strukturen für Vorgänge und Vormerkungen, jedoch keine
vollständig auf die `VOR`-Needs zurückgeführte Umsetzung. Es geht um das
unverbindliche Interesse eines Trainers an einem zukünftigen Termin
(Definition in [CONTEXT.md](CONTEXT.md)); eine Vormerkung ist ausdrücklich
keine Trainerzuweisung.

### Ziel

Aus den vorhandenen Anforderungen mit KI-Unterstützung zunächst passende
Stories und daraus prüfbare Testfälle als Needs ableiten -- ganz ohne
Implementierung. Der Fokus liegt auf der Analyse: Welche Fälle deckt eine
Story ab, welche Randfälle fehlen ihr noch, und wie lässt sich das in einem
prüfbaren Testfall festhalten?

### Vorgehen

1. Die Anforderungen in `vormerkungen.rst` lesen, zusammengehörige
   Verhaltenserwartungen herausarbeiten und als `story::`-Needs mit
   `:implements:` auf die Anforderungen formulieren.
2. Je Story mindestens einen Testfall ableiten, dazu Randfälle suchen (leere
   Liste, doppelte Vormerkung, vormerken auf einen abgesagten Termin, ...).
3. Testfälle als `test::`-Needs mit `:verifies:` auf die jeweilige Story
   eintragen, Status und Automatisierbarkeit (`:automated:`) wie in den
   bestehenden Umsetzungsdateien einschätzen. `TEST_ABW_ERF_07` in
   `abwesenheiten-umsetzung.rst` ist ein Beispiel für einen bewusst manuellen
   Test. Da in dieser Aufgabe kein ausführbarer Nachweis entsteht, bleiben die
   neuen Test-Needs `draft`, `review` oder `approved`, niemals `verified`;
   `:automated:` beschreibt nur die geplante Automatisierbarkeit.
4. Kurz begründen, warum ein Testfall so und nicht anders formuliert wurde --
   das ist hier der eigentliche Lernpunkt, nicht das Ergebnis allein.

### Abgrenzung

Kein Code und keine Implementierung. Der Dokumentationsbuild muss mit `-W`
durchlaufen; weitere ausführbare Produkt- oder E2E-Tests entstehen in dieser
Aufgabe nicht. Wird ein Fehler oder eine Lücke in einer bestehenden
Anforderung entdeckt, wird das als Anmerkung festgehalten statt die
Anforderung selbst umzuschreiben.
