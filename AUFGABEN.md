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

**Ausgangslage:** Dieser Aufgaben-Branch wurde aus dem aktuellen
Referenzstand für die Analyseaufgabe eingerichtet. Sein
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

### Workshop-Scope

**Kernumfang:** Einen zusammenhängenden Teilprozess auswählen -- Vormerkung
und Rücknahme, Entscheidung und Benachrichtigung oder Konfliktprüfung -- und
dazu höchstens zwei Stories sowie höchstens drei Test-Needs ableiten.

**Optionale Erweiterung:** Einen zweiten Teilprozess bearbeiten oder die
gefundenen fachlichen Lücken als konkrete Rückfragen dokumentieren. Alle 14
Anforderungen vollständig abzudecken ist kein Workshop-Ziel.

### Vorgehen

1. Die Anforderungen in `vormerkungen.rst` lesen, zusammengehörige
   Verhaltenserwartungen herausarbeiten und als `story::`-Needs mit
   `:implements:` auf die Anforderungen formulieren.
2. Je Story mindestens einen Testfall ableiten, dazu nicht spezifizierte
   Randfälle als Fragen sichtbar machen (z. B. doppelte Vormerkung oder
   Vormerkung auf einen abgesagten Termin), statt das erwartete Verhalten zu
   erfinden.
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
