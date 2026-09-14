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
Das eine Benutzerkonto, das eine Instanz dauerhaft handlungsfähig hält. Es trägt stets auch die Administratorrolle, kann weder stillgelegt noch gelöscht werden und gibt die Rolle nur weiter, statt sie abzulegen. Zu jedem Zeitpunkt gibt es genau einen.
_Avoid_: Owner, Superadmin, Hauptadministrator

**Trainer**:
Ein Benutzerkonto mit der Rolle Trainer. Es kann jederzeit assistieren und eine Schulung halten, sobald es für sie qualifiziert ist. Ein gesondertes Trainerprofil gibt es nicht — Qualifikationen stehen an der Schulung, Abwesenheiten sind eigene Datensätze.
_Avoid_: Trainerprofil, Trainerkonto, Trainer-User

**Schulung**:
Ein wiederverwendbares Weiterbildungsangebot mit Titel, Inhalt, Kategorie und Voraussetzungen.
_Avoid_: Kurs, Termin

**Termin**:
Eine konkret geplante Durchführung einer Schulung mit Zeitraum, Ort, Status und optionaler Trainerzuweisung.
_Avoid_: Schulung, Buchung, Platzierung

**Qualifikation**:
Die bestätigte Berechtigung eines Trainers, eine bestimmte Schulung durchzuführen.
_Avoid_: Kurszuweisung, Freigabe

**Freigabeanfrage**:
Der Antrag eines Trainers an die Administratoren, eine Qualifikation für eine Schulung zu erhalten.
_Avoid_: Berechtigungs-Ping-Pong, Vormerkung

**Trainerzuweisung**:
Die Zuordnung eines qualifizierten Trainers zu einem konkreten Termin durch einen Administrator.
_Avoid_: Qualifikation, Buchung

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
