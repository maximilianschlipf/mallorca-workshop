# Schulungsplaner – Setting-Dokument für das Schulungsprojekt

Dieses Dokument beschreibt das Setting für ein Demo-Projekt, mit dem Teilnehmende den Umgang mit generativer KI entlang des gesamten Entwicklungszyklus üben sollen: von Anforderungsanalyse über Architektur und Entwurf bis zur Umsetzung mit programmierenden Agenten.

## 1. Ausgangslage & Rahmen

Es soll eine Webanwendung zur **Planung von Schulungen** entstehen. Das Projekt dient als Übungsgegenstand in einer Schulung zu generativer KI in der Softwareentwicklung. Damit die Ergebnisse der Teilnehmenden vergleichbar bleiben und sich gut diskutieren lassen, gelten folgende technische Leitplanken:

- **Backend:** Spring Boot (Java)
- **Frontend:** Vue 3 mit TypeScript und Vite 
- **Kommunikation:** REST-API mit JSON
- **Entwicklungsumgebung:** Visual Studio Code in Verbindung mit GitHub Copilot

Das Repository enthält bereits eine lauffähige Referenzarchitektur mit Spring Boot, Spring JDBC, H2, Vue 3 und REST. Diese Architektur ist für den Workshop verbindlich. Die Teilnehmenden analysieren und erweitern sie, statt ein neues Grundgerüst oder eine alternative Persistenz aufzusetzen.

### 1.1 Projektzielbild für die Demo

Für die Schulung wird ein **Monorepo** vorbereitet, das möglichst schnell lokal startbar ist:

- `backend/`: Spring Boot, H2 In-Memory, Seed-Import
- `frontend/`: Vue 3, TypeScript, Vite
- `docs/`: Setup, Runbook, Architekturüberblick, Testing, Feature-Briefings
- `.github/`: minimale Copilot-Instruktionen sowie später erweiterbare Prompt-/Instructions-Dateien

Ziel ist ein stabiles Grundgerüst mit geringem Setup-Aufwand, damit Teilnehmende den Schwerpunkt auf KI-gestützte Analyse, Implementierung und Review legen können.

## 2. Lernziele bezüglich GenAI-Nutzung

Das Projekt soll den kompletten Lebenszyklus einer KI-unterstützten Umsetzung abdecken:

1. **Anforderungsanalyse:** Feature-Briefings mit KI untersuchen, offene Fragen erkennen und Akzeptanzkriterien präzisieren.
2. **Prompting und Kontext:** Ziele, Grenzen und relevante Dateien bewusst auswählen und den Einfluss des Kontexts beobachten.
3. **Planung:** Implementierungspläne getrennt von der Umsetzung erzeugen und fachlich wie technisch prüfen.
4. **Agentennutzung:** Freigegebene Features schrittweise im Agent-Modus umsetzen lassen.
5. **Qualitätssicherung:** Tests, Diff-Review und Playwright MCP zur Überprüfung von KI-Ergebnissen einsetzen.
6. **Modellauswahl und Reflexion:** Modelle und Effort-Level anhand derselben Aufgabe vergleichen sowie übernommene und verworfene Vorschläge dokumentieren.

## 3. Das Szenario

Die fiktive **Nordwind Systems AG** ist ein mittelständisches IT-Unternehmen mit einer eigenen internen Weiterbildungsabteilung, der **Nordwind Academy**. Diese bietet sowohl offene (öffentliche) Schulungstermine als auch exklusive Inhouse-Schulungen für Teams an. Bisher wurde die Planung über Excel-Listen und E-Mails organisiert – das ist unübersichtlich und fehleranfällig geworden. Die Nordwind Academy möchte deshalb ein **Schulungsplaner-Tool**, mit dem sich

- der aktuelle Schulungskatalog übersichtlich pflegen,
- verfügbare Termine kalendarisch darstellen und
- die Verfügbarkeit und Qualifikation der Trainer verwalten

lässt.

**Rollen im Setting** (fachlich, nicht zwingend 1:1 als technische Login-Rollen umzusetzen):

- **Planer/Admin** bei der Nordwind Academy: pflegt Schulungskatalog und Termine.
- **Trainer:innen** (intern und extern): pflegen ihre Abwesenheiten und hinterlegen, welche Schulungen sie halten können.
- **Interessent:innen/Buchende:** möchten den Kalender und Katalog einsehen, um eine passende Schulung zu finden (Buchung selbst kann, muss aber nicht Teil der Umsetzung sein – siehe Abgrenzung).

## 4. Funktionale Anforderungen (Kernfeatures)

### 4.1 Interaktiver Kalender

- Darstellung aller **öffentlichen Schulungstermine** in einer Kalenderansicht (Monats- oder Wochenansicht – Detailumsetzung ist den Teilnehmenden überlassen).
- Auswahl eines Termins zeigt Details: zugehörige Schulung, Ort/Format, Trainer, Status (z. B. geplant, ausgebucht, abgesagt).

### 4.2 Schulungskatalog

Der Katalog zeigt alle angebotenen Schulungen mit:

- Titel, Kategorie und Kurzbeschreibung
- **Voraussetzungen** für die Teilnahme
- **Dauer** – meist 2 oder 3 Tage, es gibt aber auch Ein-Tages-Formate
- **Öffentliche Termine** (verknüpft mit dem Kalender aus 4.1)
- **Mindestteilnehmerzahl**, ab der die Schulung **exklusiv** (als Inhouse-Termin für ein Team) gebucht werden kann

### 4.3 Trainerverwaltung

- Trainer:innen können ihre **Abwesenheiten** (Zeiträume, optional mit Grund) eintragen und pflegen.
- Trainer:innen hinterlegen, **welche Schulungen** sie halten können (Qualifikationen).
- Für die Terminplanung soll erkennbar sein, welche Trainer:innen für eine Schulung fachlich passen und in einem gewünschten Zeitraum verfügbar sind (Abgleich mit den Abwesenheiten).

## 5. Datenmodell & Datengrundlage

Als Ausgangsdatengrundlage werden `backend/src/main/resources/seed/schulungen.json` und `backend/src/main/resources/seed/trainer.json` mitgeliefert. Das vorbereitete Backend liest sie beim Start in eine H2-In-Memory-Datenbank ein.

### 5.1 Schema `schulungen.json`

| Feld | Typ | Pflicht | Beschreibung |
|---|---|---|---|
| `id` | string | ja | Eindeutige Kennung der Schulung |
| `titel` | string | ja | Titel der Schulung |
| `kategorie` | string | ja | Themenkategorie |
| `kurzbeschreibung` | string | ja | Kurzbeschreibung |
| `voraussetzungen` | string[] | ja | Liste von Voraussetzungen (kann leer sein) |
| `dauerInTagen` | number | ja | Dauer in Tagen (meist 2–3, teils 1) |
| `mindestteilnehmerExklusiv` | number | ja | Ab dieser Teilnehmerzahl exklusiv (Inhouse) buchbar |
| `maxTeilnehmerOeffentlich` | number | empfohlen | Kapazität eines öffentlichen Termins |
| `oeffentlicheTermine` | Termin[] | ja | Siehe unten |

Verschachteltes Objekt `Termin`:

| Feld | Typ | Pflicht | Beschreibung |
|---|---|---|---|
| `terminId` | string | ja | Eindeutige Kennung des Termins |
| `startdatum` | string (ISO-Datum) | ja | Erster Schulungstag |
| `enddatum` | string (ISO-Datum) | ja | Letzter Schulungstag |
| `ort` | string | ja | Stadt oder `"Online"` |
| `format` | string | optional | z. B. `"Präsenz"`, `"Online"`, `"Hybrid"` |
| `status` | string | ja | `"geplant"`, `"ausgebucht"` oder `"abgesagt"` |
| `trainerId` | string \| null | optional | Referenz auf `trainer.json` – kann `null` sein, wenn noch nicht zugeteilt |

### 5.2 Schema `trainer.json`

| Feld | Typ | Pflicht | Beschreibung |
|---|---|---|---|
| `id` | string | ja | Eindeutige Kennung des Trainers |
| `name` | string | ja | Name |
| `email` | string | ja | Kontakt-E-Mail |
| `qualifikationen` | string[] | ja | Liste von Schulungs-IDs (Referenz auf `schulungen.json`), die der Trainer halten kann |
| `abwesenheiten` | Abwesenheit[] | ja | Siehe unten (kann leer sein) |

Verschachteltes Objekt `Abwesenheit`:

| Feld | Typ | Pflicht | Beschreibung |
|---|---|---|---|
| `von` | string (ISO-Datum) | ja | Beginn der Abwesenheit |
| `bis` | string (ISO-Datum) | ja | Ende der Abwesenheit |
| `grund` | string | optional | z. B. `"Urlaub"`, `"Konferenz"`, `"Krankheit"` |

Die Feature-Briefings legen fest, welche Teile dieses Modells im Workshop erweitert werden.

## 6. Technische Leitplanken

- **Backend:** Spring Boot, Spring JDBC, REST-API und H2-In-Memory-Datenbank mit Seed-Import.
- **Frontend:** Vue 3 mit TypeScript und Vite.
- Kommunikation zwischen Frontend und Backend ausschließlich über eine REST-Schnittstelle (JSON).
- Repositoryweite Regeln stehen in `.github/copilot-instructions.md`.

### 6.1 Lokale Laufzeitkonventionen für die Schulung

- Standard-Port Backend: `18081`
- Standard-Port Frontend (Vite): `15173`
- Start erfolgt bewusst mit **zwei Kommandos** (Backend und Frontend getrennt).
- Beide Ports müssen vor dem Start frei sein.

## 7. Nicht-funktionale Anforderungen

- Fachliche Rollen dienen nur zur Einordnung der Anforderungen; ein technischer Login und eine Rechteprüfung sind kein Bestandteil des Workshops.
- Responsive Bedienbarkeit ist wünschenswert, aber kein Abnahmekriterium.
- Deutsch ist die einzige benötigte Sprache.
- Cloud-Betrieb, besondere Last und externe Persistenz sind nicht erforderlich.
- Setup, Start und Tests müssen auf einer frischen Workshopmaschine reproduzierbar sein.

## 8. Abgrenzung – was NICHT gebraucht wird

Um Scope Creep während der Übung zu vermeiden, ausdrücklich **kein** Bestandteil der Aufgabe:

- Bezahl- oder Rechnungsfunktionen
- Automatischer E-Mail-/Benachrichtigungsversand (kann als freiwillige Erweiterung ergänzt werden)
- Mandantenfähigkeit / Unterstützung mehrerer Unternehmen
- Ein öffentliches Self-Service-Buchungsportal für Endkunden – der Fokus liegt auf der **Planungsperspektive** (Kalender, Katalog, Trainerverwaltung)
- Integration in externe Kalendersysteme (Outlook, Google Kalender o. Ä.)

Zusätzlich für die erste Iteration außerhalb des Scopes:

- Vollständige Kalender-UI
- Pflege-Workflows für Trainer-Abwesenheiten und Qualifikationen

## 9. Workshopablauf im Repository

1. Vorhandenes System mit Copilot untersuchen und Aussagen am Code verifizieren.
2. Katalogsuche und Kategorie-Filter als geführten KI-Arbeitszyklus umsetzen.
3. Trainerverfügbarkeit mit bewusstem Kontext- und Modellvergleich planen und implementieren.
4. Abwesenheitspflege als zunehmend selbstständiges Agentenfeature bearbeiten.
5. Features mit Tests, Playwright MCP, Diff-Review und Git absichern.
6. Pro Feature festhalten, welche KI-Vorschläge übernommen, korrigiert oder verworfen wurden.

## 10. Vorbereitete Ausgangsbasis

Der minimale Katalog-Read ist bereits implementiert:

- Das Backend lädt die Seed-Daten aus dem Classpath.
- `GET /api/schulungen` liefert den Schulungskatalog als JSON.
- Das Frontend zeigt eine einfache Liste von Schulungen.
- Backend-, Frontend- und Playwright-Smoke-Tests sind vorhanden.

Kalenderinteraktionen, Trainer-Matching und Pflege von Abwesenheiten sind noch nicht implementiert.

## 11. Smoke-Test Anforderungen

Für die Verifikation des Tool-Stacks sind drei Smoke-Tests vorgesehen:

- **Backend-Smoke:** API-Endpunkt `GET /api/schulungen` antwortet mit HTTP 200 und nicht-leerer Liste.
- **Frontend-Smoke (Vitest):** mindestens ein Unit-/Component-Smoke-Test für die Katalogdarstellung.
- **Playwright E2E-Smoke:** prüft API 200 und dass der Katalog im UI sichtbar ist.

## 12. Seed-Reset für Demo-Durchläufe

Da eine H2-In-Memory-Datenbank genutzt wird, gilt:

- Ein Neustart des Backends stellt den sauberen Seed-Zustand wieder her.
- Ein separater manueller Seed-Reset ist für Iteration 1 nicht notwendig.

## 13. Gelieferte Artefakte

- Dieses Setting-Dokument
- `backend/src/main/resources/seed/schulungen.json` – Beispiel-Schulungskatalog inkl. öffentlicher Termine
- `backend/src/main/resources/seed/trainer.json` – Beispiel-Trainerdaten inkl. Qualifikationen und Abwesenheiten
