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

## Aufgabe 5 -- Refinement: Assistenzplätze weiterentwerfen

**Schwerpunkt:** Anforderungsanalyse

### Ausgangslage

Zu Assistenzplätzen gibt es eine knappe Sammlung von Anforderungen
(`docs/source/anforderungen/assistenz.rst`, Kürzel `ASS`), überwiegend noch
im Status `draft`. Im Referenzsystem existieren bereits technische
Teilfunktionen für Assistenzzuweisung und -bewerbung, aber noch keine
freigegebene, durch Stories und `TEST_ASS_...`-Nachweise vollständig
beschriebene Umsetzung. Der Aufgaben-Branch behält diese Teilfunktionen als
späteres Vergleichsmaterial; sie sind keine Vorgabe für die fachliche
Entscheidung und werden erst nach dem eigenen Entwurf betrachtet.

### Ziel

Nicht Code entsteht hier, sondern ein tragfähiger Entwurf: Ein
KI-gestützter Refinement-Prozess, der aus der knappen Ausgangslage
vollständige, widerspruchsfreie Anforderungen und User Stories macht --
inklusive der Fragen, die dabei offenbleiben und im Team geklärt werden
müssten.

### Workshop-Scope

**Kernumfang:** Die acht vorhandenen Anforderungen auf Widersprüche und
Lücken prüfen, höchstens drei offene Fragen formulieren, eine davon als
Entscheidung festhalten und daraus ein bis zwei Stories ableiten.

**Optionale Erweiterung:** Den eigenen Entwurf mit den technischen
Teilfunktionen vergleichen und Abweichungen dokumentieren. Die Anforderungen
werden dadurch nicht automatisch an die vorhandene Implementierung
angepasst.

### Vorgehen

1. Vorhandene Anforderungen und den Kontext aus [CONTEXT.md](CONTEXT.md)
   lesen (Trainer, Qualifikation, Termin).
2. Ohne Blick auf die Teilimplementierung mit der KI als Sparringspartner
   echte Lücken sammeln, z. B.: Was passiert mit Bewerbungen und Zuweisungen
   bei Absage des Termins? Wird ein Platz nach dem Ausscheiden eines
   Assistenten wieder frei? Wie werden konkurrierende Bewerbungen behandelt?
3. Fragen zu Entscheidungen verdichten, Entscheidungen als eigene Needs
   festhalten (`dec::`, wie z. B. `DEC_DAT_ABLAGE_02` im Bereich Speicher-
   schicht), Anforderungen und Stories entsprechend ergänzen.
4. Den entstandenen Entwurf kurz gegenüberstellen: Was hätte eine erste,
   naive Formulierung übersehen, die der Refinement-Prozess aufgedeckt hat?

### Abgrenzung

Kein Test-Need, kein Code. Ergebnis ist ein erweiterter, in sich
konsistenter `assistenz.rst` (und ggf. eine begonnene
`assistenz-umsetzung.rst`), der `make html` mit `-W` durchlaufen lässt.
