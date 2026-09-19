# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

Primäre Nutzer sind die **Administratoren** und **Trainer** der SimplyTest
Academy — dieselben Menschen, die den Schulungsbetrieb heute organisieren.

- **Administrator**: pflegt den Schulungskatalog, plant Termine, weist
  qualifizierte Trainer zu und entscheidet Freigabeanfragen. Arbeitet
  vorbereitend und am Schreibtisch, meist mit mehreren Terminen und
  Trainerverfügbarkeiten gleichzeitig im Blick.
- **Trainer**: sieht die eigenen Termine und Qualifikationen, beantragt
  Freigaben, meldet Abwesenheiten und merkt sich Termine vor.
- **Eigentümer**: genau ein aktives Konto je Instanz, trägt stets auch die
  Administratorrolle und hält die Instanz dauerhaft handlungsfähig.

Es gibt genau eine Kontoart; Rechte ergeben sich aus den Rollen. Ein
gesondertes Trainerprofil existiert nicht.

Zweite, gleichzeitig reale Zielgruppe: **Teilnehmende eines AI-Coding-Trainings**,
die dieses Projekt als Referenz lesen. Fachlichkeit, Dokumentation und
Requirements-Gates sind deshalb bewusst produktionsnah.

## Product Purpose

Der Schulungsplaner verwaltet Schulungen, Termine, Trainer und deren
Qualifikationen an einem Ort. Erfolg heißt: ein Termin ist geplant, mit einem
tatsächlich qualifizierten und verfügbaren Trainer besetzt, und alle
Beteiligten sehen denselben Stand — ohne Tabellen, Mailketten oder
Rückfragen.

Das Projekt entsteht im Rahmen eines AI-Coding-Trainings und soll danach
tatsächlich produktiv von der Academy genutzt werden. Beides gilt
gleichzeitig: keine Wegwerf-Demo, aber auch ein lesbares Lehrstück.

## Positioning

Qualifikation ist keine Verwaltungsnotiz, sondern die harte Bedingung für
eine Trainerzuweisung: Ein Trainer kann einem Termin nur zugewiesen werden,
wenn er für genau diese Schulung qualifiziert ist. Freigabeanfrage,
Qualifikation, Vormerkung und Zuweisung sind vier getrennte Begriffe mit
getrennten Zuständen — ein generischer Kalender oder ein LMS bildet diese
Kette nicht ab.

Zweite Besonderheit: Der Schulungskatalog liegt als JSON-Dateien im
Projektverzeichnis, jede Änderung wird als Commit gesichert. Die Instanz
läuft lokal, der Katalog ist versioniert und diffbar statt in einer Datenbank
verborgen.

## Operating Context

- Jede Instanz wird **lokal betrieben** (Frontend `:15173`, Backend `:18081`).
- Beim ersten Start existiert kein Konto; die Registrierung erzeugt das
  Eigentümerkonto mit allen drei Rollen.
- Der Schulungskatalog liegt unter `katalog/` — `kategorien.json` plus eine
  JSON-Datei je Schulung (`katalog/schulungen/SCH-NNN.json`).
- Anforderungen leben als Sphinx-`req::`-Bedarfe unter
  `docs/source/anforderungen/`, gegliedert nach den Kürzeln DAT, USR, QUA,
  TRA, ABW, KAT, TER, ASS, VOR, UEB, TLN.
- Vor jeder Fertigmeldung läuft `./verify.sh`: Traceability-Matrix,
  Dokumentation, Backend- und Frontendtests, Build und ein isolierter
  E2E-Lauf. Der umgesetzte Produktumfang steht im kumulativen Register
  `implemented-requirements.txt`.
- Die Fachsprache in `CONTEXT.md` ist verbindlich, samt der dort notierten
  _Avoid_-Begriffe. Produkt- und Oberflächensprache ist Deutsch.

## Capabilities and Constraints

`implemented-requirements.txt` ist das autoritative kumulative Register der
Fachbereiche, deren vollständige Umsetzung das Abschluss-Gate prüft.
Freigegebene Anforderungen außerhalb des Registers sind nur spezifiziert.

**Feste Regeln**:

- Die E-Mail-Adresse ist Anmeldekennung, unveränderlich und ausdrücklich
  keine zugesicherte Zustellanschrift — es wird nichts versandt.
- Genau ein Eigentümer je Instanz; das Konto kann weder stillgelegt noch
  gelöscht werden und gibt die Rolle nur weiter.
- Termine kennen die Zustände geplant, abgeschlossen, abgesagt.
- Zugangsart (öffentlich / exklusiv) und Durchführungsart (remote / vor Ort /
  beim Kunden / hybrid) sind unabhängig voneinander.
- Vormerkung ≠ Trainerzuweisung; Qualifikation ≠ Freigabeabfrage.

**Technischer Rahmen**: Vue 3 + TypeScript + Vite (Frontend, Routen und
Komponenten deutsch benannt), Spring Boot / Java 21 (Backend), Sphinx mit
PlantUML (Doku), Vitest und Playwright (Tests).

**Offen**: Ein Kontowechsel auf eine andere E-Mail-Adresse (Export/Import des
Profils) ist ausdrücklich noch nicht ausgearbeitet.

## Brand Commitments

Die SimplyTest-CI ist **bindend**:

- Name: SimplyTest Academy.
- Kernfarben: Blau `#006fae` (Akzent), Orange `#ff6633` (Highlight),
  Dunkelblau `#171942` (Text), Flächenton `#e7ecf0`.
- Schrift: Roboto.

Vorhandene Token liegen in `frontend/src/style.css`, die Wortmarken unter
`frontend/public/`. Diese Festlegungen dürfen von späterer Gestaltungsarbeit
nicht ersetzt werden.

## Evidence on Hand

- Echter Beispielkatalog: `katalog/schulungen/SCH-*.json` und fünf Kategorien
  in `katalog/kategorien.json` (Agile & Projektmanagement, Cloud & DevOps,
  IT-Security, Soft Skills & Führung, Softwareentwicklung).
- Vollständige, freigegebene Anforderungen inklusive Traceability unter
  `docs/source/anforderungen/`.
- Verbindliche Fachsprache in `CONTEXT.md`.

Nicht vorhanden und **nicht zu erfinden**: Kundenreferenzen, Testimonials,
Presse, Nutzerzahlen, Preise, Zertifikate, Lizenz- oder Deployment-Aussagen.

## Product Principles

1. **Die Fachsprache aus `CONTEXT.md` gilt überall** — in UI-Texten,
   Code und Doku. Vermiedene Begriffe bleiben vermieden.
2. **Qualifikation vor Zuweisung.** Die Oberfläche macht sichtbar, warum
   jemand zuweisbar ist oder nicht, statt den Fehler erst beim Speichern zu
   zeigen.
3. **Ein Stand für alle.** Administratoren und Trainer sehen dieselbe
   Wahrheit über Termin, Qualifikation und Verfügbarkeit.
4. **Belegte Fertigstellung.** Nichts gilt als fertig ohne grünes
   `./verify.sh` und nachgewiesene `TEST_*`-Abdeckung.
5. **Nicht überkonstruieren.** Die einfache, offensichtliche Lösung gewinnt;
   der Katalog bleibt lesbare, diffbare JSON-Ablage.

## Accessibility & Inclusion

Kein Standard formal festgelegt (bestätigt). Es gilt gute Praxis:
Tastaturbedienbarkeit, ausreichende Kontraste und verständliche
Fehlermeldungen in deutscher Sprache.
