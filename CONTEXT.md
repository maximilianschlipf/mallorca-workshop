# Schulungsplanung

Dieser Kontext beschreibt die Verwaltung von Schulungen, Terminen, Trainern und deren Qualifikationen in der SimplyTest Academy.

## Language

**Benutzerkonto**:
Die Identität einer Person, die den Schulungsplaner als Administrator oder Trainer verwendet.
_Avoid_: Nutzerprofil, Account

**Administrator**:
Ein Benutzerkonto, das Schulungen und Termine verwaltet, Trainer zuweist und Freigabeanfragen entscheidet.
_Avoid_: Planer, Admin-User

**Eigentümer**:
Das eine aktive Benutzerkonto, das eine Instanz dauerhaft handlungsfähig hält. Es trägt stets auch die Administratorrolle, kann weder stillgelegt noch gelöscht werden und gibt die Rolle nur weiter, statt sie abzulegen. Sobald ein Benutzerkonto existiert, gibt es genau einen Eigentümer.
_Avoid_: Owner, Superadmin, Hauptadministrator

**Trainer**:
Ein Benutzerkonto mit der Rolle Trainer. Es kann jederzeit assistieren und eine Schulung halten, sobald es für sie qualifiziert ist. Ein gesondertes Trainerprofil gibt es nicht — Qualifikationen stehen an der Schulung, Abwesenheiten sind eigene Datensätze.
_Avoid_: Trainerprofil, Trainerkonto, Trainer-User

**Schulung**:
Ein wiederverwendbares Weiterbildungsangebot mit Titel, Inhalt, Kategorie und Voraussetzungen.
_Avoid_: Kurs, Termin

**Termin**:
Eine konkret geplante, ganztägige Durchführung einer Schulung mit Datumsbereich, Zustand und optionaler Trainerzuweisung. Uhrzeiten werden nicht geplant. Zustände sind geplant, abgeschlossen und abgesagt.
_Avoid_: Schulung, Buchung, Platzierung, ausgebucht als Zustand

**Schulungstag**:
Ein vollständig belegter Wochentag von Montag bis Freitag mit acht Stunden. Wochenenden innerhalb eines Terminzeitraums sind keine Schulungstage und zählen nicht zur Dauer; Feiertage werden nicht automatisch berücksichtigt.
_Avoid_: Kalendertag, Zeitfenster

**Zugangsart**:
Ob ein Termin **öffentlich** ist — einzeln buchbar für Teilnehmer verschiedener Firmen — oder **exklusiv** für eine Firma. Sie bestimmt, welche Teilnehmergrenze der Schulung gilt.
_Avoid_: Format, Terminart

**Durchführungsart**:
Wie ein Termin stattfindet: **remote** (ausschließlich online), **vor Ort** (in den eigenen Räumen), **beim Kunden** (in dessen Räumen) oder **hybrid** (vor Ort und zugleich online zugänglich). Unabhängig von der Zugangsart.
_Avoid_: Format, Präsenz, Online, Onsite, Inhouse

**Kundenfirma**:
Die eine Firma, für die ein exklusiver Termin reserviert ist. Öffentliche Termine tragen keine Kundenfirma; dort steht die Firma nur an den einzelnen Teilnehmerbuchungen.
_Avoid_: Teilnehmerfirma, Veranstaltungsort

**Online-Zugang**:
Die optionale URL, über die ein remote oder hybrid durchgeführter Termin erreichbar ist.
_Avoid_: Ort, Einwahlort

**Qualifikation**:
Die bestätigte Berechtigung eines Trainers, eine bestimmte Schulung durchzuführen.
_Avoid_: Kurszuweisung, Freigabe

**Freigabeanfrage**:
Der Antrag eines Trainers an die Administratoren, eine Qualifikation für eine Schulung zu erhalten.
_Avoid_: Berechtigungs-Ping-Pong, Vormerkung

**Trainerzuweisung**:
Die Zuordnung eines qualifizierten Trainers zu einem konkreten Termin durch einen Administrator.
_Avoid_: Qualifikation, Buchung

**Durchführungsbestätigung**:
Die Bestätigung durch den ausführenden Trainer oder ersatzweise einen Administrator, dass ein Termin stattgefunden hat und seine Teilnehmerdaten vollständig sind. Sie versetzt den Termin in den Zustand abgeschlossen.
_Avoid_: automatischer Abschluss, Ende des Datumsbereichs

**Vormerkung**:
Das unverbindliche Interesse eines Trainers an einem zukünftigen Termin. Eine Vormerkung ist keine Trainerzuweisung.
_Avoid_: Reservierung, Bewerbung, Buchung

**Abwesenheit**:
Ein Zeitraum, in dem ein Trainer nicht für Termine zur Verfügung steht.
_Avoid_: Sperrzeit, Urlaub

**Teilnehmerbuchung**:
Die Reservierung eines Platzes in einem Termin für einen Teilnehmer. Sie ist von Terminplanung und Trainerzuweisung getrennt.
_Avoid_: Terminbuchung durch Administratoren

**Schulungsimport**:
Die Übernahme extern bereitgestellter Schulungsdaten in den verwalteten Schulungskatalog.
_Avoid_: Dateispeicherung, Dateiablage
