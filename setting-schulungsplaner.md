# Schulungsplaner – Setting-Dokument für das Schulungsprojekt

Dieses Dokument beschreibt das Setting für ein Demo-Projekt, mit dem Teilnehmende den Umgang mit generativer KI entlang des gesamten Entwicklungszyklus üben sollen: von Anforderungsanalyse über Architektur und Entwurf bis zur Umsetzung mit programmierenden Agenten.

## 1. Ausgangslage & Rahmen

Es soll eine Webanwendung zur **Planung von Schulungen** entstehen. Das Projekt dient als Übungsgegenstand in einer Schulung zu generativer KI in der Softwareentwicklung. Damit die Ergebnisse der Teilnehmenden vergleichbar bleiben und sich gut diskutieren lassen, gelten folgende technische Leitplanken:

- **Backend:** Spring Boot (Java)
- **Frontend:** Vue 3 mit TypeScript und Vite 
- **Kommunikation:** REST-API mit JSON

Alles andere – Domainmodell, Architektur innerhalb dieser Leitplanken, Persistenzlösung, UI-Umsetzung – ist bewusst offen und Teil der Übung.

## 2. Lernziele bezüglich GenAI-Nutzung

Das Projekt soll den kompletten Lebenszyklus einer KI-unterstützten Umsetzung abdecken:

1. **Anforderungsanalyse:** Aus diesem Setting-Dokument gemeinsam mit einer KI User Stories bzw. ein Backlog ableiten und priorisieren.
2. **Architektur & Entwurf:** Domainmodell, Schichtenarchitektur und API-Design mit KI als Sparringspartner erarbeiten und begründen lassen.
3. **API-Vertrag:** Optional eine OpenAPI-Spezifikation erstellen bzw. von der KI generieren lassen, um Frontend und Backend zu entkoppeln.
4. **Umsetzung mit programmierenden Agenten:** Features schrittweise von einem KI-Coding-Agenten implementieren lassen, inklusive geeigneter Prompting- und Review-Strategien.
5. **Qualitätssicherung:** Unit- und Integrationstests von der KI generieren und kritisch prüfen lassen.
6. **Reflexion:** Bewusst machen, wo die KI gut unterstützt hat und wo manuelles Eingreifen nötig war.

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

Als Ausgangsdatengrundlage werden zwei JSON-Dateien mitgeliefert: `schulungen.json` und `trainer.json`. Sie können z. B. beim Start des Backends eingelesen werden (Seed-Daten) – die eigentliche Persistenzlösung (Datenbank, JPA-Entities o. Ä.) entwerfen die Teilnehmenden selbst.

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

Die Felder `format` bei Terminen sowie das generelle Aufbrechen in weitere Felder (z. B. Preis, Sprache) sind bewusst als **optionale, erweiterbare Beispiele** zu verstehen – die Teilnehmenden dürfen das Modell im Rahmen der Übung sinnvoll ergänzen oder anpassen.

## 6. Technische Leitplanken

- **Backend:** Spring Boot, REST-API. Persistenz frei wählbar; für den Demo-Kontext reicht z. B. eine H2-In-Memory-Datenbank mit Import der mitgelieferten JSON-Dateien als Seed-Daten.
- **Frontend:** Angular *oder* Vue – Entscheidung liegt beim Team bzw. der einzelnen Person.
- Kommunikation zwischen Frontend und Backend ausschließlich über eine REST-Schnittstelle (JSON).
- Empfehlenswert (optional): eine OpenAPI-Spezifikation als Vertrag zwischen Frontend und Backend definieren – lässt sich gut mit KI-Unterstützung erstellen und pflegen.
- Kein vorgegebener Styleguide – übliche Clean-Code-Prinzipien gelten trotzdem.

## 7. Nicht-funktionale Anforderungen

- **Rollen-/Rechtekonzept:** mindestens zwei fachliche Rollen unterscheiden – *Trainer* (pflegt eigene Abwesenheiten und Qualifikationen) und *Planer/Admin* (pflegt Katalog und Termine). Ein einfacher Login-Mechanismus genügt; SSO/OAuth ist nicht erforderlich.
- **Responsive Bedienbarkeit** ist wünschenswert, aber für die erste Version kein Muss.
- **Mehrsprachigkeit** ist nicht erforderlich – Deutsch reicht aus.
- **Performance/Skalierung:** keine besonderen Anforderungen, da reiner Lern-/Demo-Kontext ohne echte Last.

## 8. Abgrenzung – was NICHT gebraucht wird

Um Scope Creep während der Übung zu vermeiden, ausdrücklich **kein** Bestandteil der Aufgabe:

- Bezahl- oder Rechnungsfunktionen
- Automatischer E-Mail-/Benachrichtigungsversand (kann als freiwillige Erweiterung ergänzt werden)
- Mandantenfähigkeit / Unterstützung mehrerer Unternehmen
- Ein öffentliches Self-Service-Buchungsportal für Endkunden – der Fokus liegt auf der **Planungsperspektive** (Kalender, Katalog, Trainerverwaltung)
- Integration in externe Kalendersysteme (Outlook, Google Kalender o. Ä.)

## 9. Empfohlener Ablauf für die Teilnehmenden

1. Anforderungen aus diesem Dokument mit KI-Unterstützung in User Stories bzw. ein Backlog übersetzen und priorisieren.
2. Domainmodell, Architektur und API-Design entwerfen – KI als Sparringspartner nutzen und Entscheidungen begründen lassen.
3. Grundgerüst aufsetzen (Spring Boot Backend, Angular- oder Vue-Frontend).
4. Features schrittweise mit einem programmierenden Agenten umsetzen, z. B. zuerst Schulungskatalog, dann Kalender, dann Trainerverwaltung.
5. Tests generieren lassen und kritisch reviewen.
6. Kurze Retrospektive: Wo hat die KI gut unterstützt, wo musste manuell nachgesteuert werden?

## 10. Gelieferte Artefakte

- Dieses Setting-Dokument
- `schulungen.json` – Beispiel-Schulungskatalog inkl. öffentlicher Termine
- `trainer.json` – Beispiel-Trainerdaten inkl. Qualifikationen und Abwesenheiten
