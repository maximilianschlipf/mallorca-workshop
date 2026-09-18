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
  in den E2E-Tests, ein Need pro Anforderung/Story/Test mit `:links:` zur
  Nachverfolgung. [PLAN-schulungskatalog.md](PLAN-schulungskatalog.md)
  zeigt, wie ein solcher Umsetzungsplan aussieht.

**Ausgangslage:** Für jede Aufgabe ist der jeweilige Bereich vorab gezielt
zurückgeschnitten worden, damit die Teilnehmer genau den Teil selbst
erarbeiten, um den es in der Aufgabe geht -- vorhandene Tests, entworfene
Test-Needs oder Stories wurden dafür entfernt. Was konkret vorhanden ist und
was fehlt, steht bei jeder Aufgabe unter "Ausgangslage".

---

## Aufgabe 1 -- Trainerabwesenheit umsetzen

**Schwerpunkt:** Entwicklung

### Ausgangslage

Benutzerkonten sind inzwischen umgesetzt -- viele andere Bereiche bauen
darauf auf, deshalb ist er nicht mehr Gegenstand einer eigenen Aufgabe.

Für Abwesenheiten sind Anforderungen, User Stories und Tests bereits
vollständig als Needs entworfen
(`docs/source/anforderungen/abwesenheiten.rst` und
`abwesenheiten-umsetzung.rst`, Kürzel `ABW`). Es gibt noch keinen Code dafür
-- weder Entität noch Endpunkt noch Oberfläche; das vorhandene
`Abwesenheit`-Record im Backend ist nur ein Platzhalter. Was eine
Abwesenheit ist, und die Unterscheidung von Trainerzuweisung, Vormerkung und
Übernahmeanfrage stehen in [CONTEXT.md](CONTEXT.md).

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
Übernahmeanfragen beim Eintreten einer Abwesenheit geschieht --
`abwesenheiten.rst` verweist bewusst nicht mehr darauf, weil es diese
Bereiche im Planer noch nicht gibt. Wer eine Schnittstelle dorthin offen
halten möchte, soll sie nicht vorwegnehmen, sondern nur nicht verbauen.

---

## Aufgabe 2 -- Qualifikationen und Freigabeanfragen entwerfen und umsetzen

**Schwerpunkt:** Entwicklung (inklusive Anforderungsanalyse)

### Ausgangslage

Für Qualifikationen liegen nur die reinen Anforderungen vor
(`docs/source/anforderungen/qualifikationen.rst`, Kürzel `QUA`), keine
Stories und keine Tests. Der Bereich ist im Code nicht vorhanden.

### Ziel

Erst User Stories und Tests als Needs entwerfen (nach dem Muster von
`katalog-umsetzung.rst` oder `benutzerkonten-umsetzung.rst`: `:links:` auf
die jeweilige Anforderung, Status im Lebenszyklus, `story::`- und
`test::`-Needs), danach umsetzen. Im Mittelpunkt steht die bestätigte
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

Setzt Benutzerkonten voraus (Rolle Trainer, Administrator). Falls Aufgabe 1
in der Schulung nicht parallel bearbeitet wird, reicht ein Platzhalter/Stub
für die Rollenprüfung -- das ist hier kein Stubben eines Fremdbereichs im
Sinne von `PLAN-schulungskatalog.md`, sondern eine bewusste Abgrenzung, die
in der eigenen Umsetzung zu vermerken ist.

---

## Aufgabe 3 -- KI-gestützter Testprozess für die Terminerstellung

**Schwerpunkt:** Testautomatisierung

### Ausgangslage

Für die Terminplanung sind Anforderungen und Stories bereits als Needs
vorhanden (`docs/source/anforderungen/terminplanung.rst` und
`terminplanung-umsetzung.rst`, Kürzel `TER`), ebenso die dort verlinkten
Tests -- als Beschreibung, nicht als Code. Es existiert noch keine
Implementierung und keine automatisierte Testsuite für diesen Bereich.

### Ziel

Nicht das Ergebnis steht im Vordergrund, sondern der Weg: Wie lässt sich mit
KI-Unterstützung ein test-getriebener Prozess für die Terminerstellung
aufbauen, der Rot-Grün-Zyklen konsequent einhält und pro Test-Need
(`TEST_TER_...`) einen nachvollziehbaren Schritt erzeugt?

### Vorgehen

Je Test-Need aus `terminplanung-umsetzung.rst`: Test zuerst schreiben (mit
KI-Unterstützung aus der Formulierung des Needs ableiten), scheitern lassen,
Implementierung nachziehen, Traceability im Need vermerken
(`Besteht als <methodenName>`, wie es die Katalog-Tests bereits vormachen).
Bewusst dokumentieren, an welchen Stellen die KI-generierten Tests von Hand
nachgeschärft werden mussten und warum.

### Abgrenzung

Fokus auf die Terminerstellung (Anlegen, Zeitraum, Zustand geplant/
abgeschlossen/abgesagt). Trainerzuweisung, Assistenzplätze und
Teilnehmerbuchungen an Terminen sind eigene Bereiche und nicht Teil dieser
Aufgabe.

---

## Aufgabe 4 -- KI-gestützte Prüfung der Katalogverwaltung

**Schwerpunkt:** Testautomatisierung / Qualitätssicherung

### Ausgangslage

Der Schulungskatalog ist vollständig umgesetzt (Backend und Oberfläche,
siehe `de.nordwind.schulungsplaner.katalog` und
`frontend/src/ansichten`) -- aber ohne die Testsuite, die ihn ursprünglich
abgesichert hat. Die Anforderungen und die dazugehörigen Test-Needs bleiben
als Spezifikation erhalten (`docs/source/anforderungen/katalog.rst` und
`katalog-umsetzung.rst`, Kürzel `KAT`); es fehlt der ausführbare Test-Code
dazu.

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

### Abgrenzung

Die Implementierung selbst wird nicht verändert, außer es stellt sich ein
tatsächlicher Fehler heraus -- dann getrennt vom Testaufbau dokumentieren.

---

## Aufgabe 5 -- Refinement: Assistenzplätze weiterentwerfen

**Schwerpunkt:** Anforderungsanalyse

### Ausgangslage

Zu Assistenzplätzen gibt es bislang nur eine knappe Sammlung von
Anforderungen (`docs/source/anforderungen/assistenz.rst`, Kürzel `ASS`) --
im Kern die Idee aus dem "Geplanten Ausbau" in [README.md](README.md):
"Assistenzplätze an Terminen als Weg zum Anlernen". Es gibt weder Stories
noch Tests noch Code dazu.

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

## Aufgabe 6 -- Testfälle zu Vormerkungen ableiten (ohne Programmierung)

**Schwerpunkt:** Testfallableitung / Anforderungsanalyse

### Ausgangslage

Zu Vormerkungen liegen Anforderungen und User Stories bereits als Needs vor
(`docs/source/anforderungen/vormerkungen.rst`, Kürzel `VOR`) -- aber ohne
Test-Needs. Es geht um das unverbindliche Interesse eines Trainers an einem
zukünftigen Termin (Definition in [CONTEXT.md](CONTEXT.md)); eine
Vormerkung ist ausdrücklich keine Trainerzuweisung.

### Ziel

Aus den vorhandenen Stories mit KI-Unterstützung die passenden Testfälle
ableiten und als `test::`-Needs formulieren -- ganz ohne Implementierung.
Der Fokus liegt auf der Analyse: Welche Fälle deckt eine Story ab, welche
Randfälle fehlen ihr noch, und wie lässt sich das in einem prüfbaren
Testfall festhalten?

### Vorgehen

1. Jede Story in `vormerkungen.rst` einzeln lesen und die darin enthaltenen
   Verhaltenserwartungen herausarbeiten.
2. Je Story mindestens einen Testfall ableiten, dazu Randfälle suchen (leere
   Liste, doppelte Vormerkung, vormerken auf einen abgesagten Termin, ...).
3. Testfälle als `test::`-Needs mit `:links:` auf die jeweilige Story
   eintragen, Status und Automatisierbarkeit (`:automated:`) wie in den
   bestehenden Bereichen (`katalog-umsetzung.rst`, `TEST_KAT_ABL_04` als
   Beispiel für einen bewusst nicht automatisierten Test) einschätzen.
4. Kurz begründen, warum ein Testfall so und nicht anders formuliert wurde --
   das ist hier der eigentliche Lernpunkt, nicht das Ergebnis allein.

### Abgrenzung

Kein Code, keine Implementierung, kein `make html`-Zwang über die reine
Needs-Konsistenz hinaus. Wird ein Fehler oder eine Lücke in einer
bestehenden Story entdeckt, wird das als Anmerkung festgehalten statt die
Story selbst umzuschreiben.
