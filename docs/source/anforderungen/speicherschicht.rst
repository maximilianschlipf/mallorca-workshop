Speicherschicht
===============

Die Daten der Anwendung liegen an zwei Orten: Der Schulungskatalog bleibt als
JSON-Dateien im Repository, alles Veränderliche wandert in eine Datenbank.
Die Trennlinie verläuft entlang der Frage, ob ein Datum versioniert gehören
will oder nicht.

Grundsatzentscheidungen
-----------------------

.. decision:: Dateibasierte Ablage auf JSON-Basis statt Datenbank
   :id: DEC_DAT_ABLAGE_01
   :status: superseded

   Die Anwendung speichert ihre Daten in JSON-Dateien im Dateisystem. Diese
   Dateien sind die maßgebliche Datenquelle. Die Datenbank H2 wird damit
   abgelöst.

   Begründung: Die Nutzerzahlen sind sehr gering. Die Leistung des
   Dateisystems reicht dafür vollständig aus, und der Wegfall der Datenbank
   spart Schema, Migrationen und Betriebsaufwand.

   Abgelöst durch :need:`DEC_DAT_ABLAGE_02`.

.. decision:: Katalog als JSON, veränderliche Daten in der Datenbank
   :id: DEC_DAT_ABLAGE_02
   :status: approved
   :supersedes: DEC_DAT_ABLAGE_01

   Die Daten werden aufgeteilt:

   * Der **Schulungskatalog** bleibt in JSON-Dateien, je Schulung eine, im
     Repository versioniert. Er enthält nur die Basisdaten einer Schulung.
   * Alles **Veränderliche** -- Termine, Trainer, Teilnehmer, Vorgänge und
     Benachrichtigungen -- liegt in einer eingebetteten Datenbank.

   Was die Vorgängerentscheidung gekippt hat: Benachrichtigungen entstehen
   laufend und in großer Zahl. Jede einzelne als Commit auf eine Datei zu
   schreiben, erzeugt eine Flut von Commits, die die Versionsgeschichte des
   Katalogs unbrauchbar macht. Dasselbe gilt abgeschwächt für Termine,
   Vormerkungen und Buchungen.

   Die Aufteilung behält den Vorteil der Vorgängerentscheidung dort, wo er
   zählt: Eine Änderung am Katalog ist eine bewusste, seltene, nachvollziehbar
   zu haltende Handlung -- und bleibt versioniert. Laufender Betriebszustand
   ist das nicht und gehört nicht in die Historie.

.. decision:: Vorerst kein Schema-Migrationswerkzeug
   :id: DEC_DAT_SCHEMA_01
   :status: approved
   :links: DEC_DAT_ABLAGE_02

   Das Schema wird weiterhin über ``schema.sql`` mit
   ``CREATE TABLE IF NOT EXISTS`` angelegt. Ein Migrationswerkzeug wie
   Flyway kommt vorerst nicht zum Einsatz.

   Begründung: Nach dem Durchgang durch die Anforderungen steht das Schema
   voraussichtlich stabil. Solange nur neue Tabellen dazukommen, trägt der
   jetzige Weg.

   Die Grenze ist benannt: ``CREATE TABLE IF NOT EXISTS`` fügt einer
   bestehenden Tabelle **keine Spalte** hinzu. Sobald sich eine Tabelle
   ändert statt neu zu entstehen, ist diese Entscheidung fällig -- dann
   entweder Flyway oder das Verwerfen der lokalen Datenbankdatei.

.. decision:: Zuschnitt der Daten nach Zugriffspfad
   :id: DEC_DAT_SCHNITT_01
   :status: approved
   :links: DEC_DAT_ABLAGE_02

   Ein Datum wird dort abgelegt, wo es gelesen wird, und nicht dort, wo es
   fachlich "hingehört". Leitlinie ist, möglichst wenige Vorgänge zu haben,
   die viele Orte gleichzeitig benötigen.

   Konkret: Eine Vormerkung verbindet Trainer und Termin, hängt aber am
   Termin -- der Trainer bekommt seine Liste ohnehin über die Termine, in
   denen er vorkommt. Ebenso hängen Freigabeanfragen an der Schulung, um die
   es geht, nicht am anfragenden Trainer.

Schulungskatalog als JSON
-------------------------

.. req:: Eine JSON-Datei je Schulung
   :id: REQ_DAT_KAT_01
   :status: draft
   :component: backend
   :links: DEC_DAT_ABLAGE_02

   Der Schulungskatalog wird in JSON-Dateien gehalten, je Schulung eine
   Datei.

Was am Katalog hängt:

.. needflow::
   :root_id: REQ_DAT_KAT_01
   :root_depth: 1
   :direction: LR

.. req:: Nur Basisdaten im Katalog
   :id: REQ_DAT_KAT_02
   :status: draft
   :priority: high
   :component: backend
   :links: REQ_DAT_KAT_01

   Eine Katalogdatei enthält ausschließlich die Beschreibung einer Schulung:

   * Titel,
   * Kategorie,
   * Kurzbeschreibung,
   * Voraussetzungen,
   * Dauer in Tagen,
   * Mindestteilnehmerzahl für exklusive Termine,
   * Höchstteilnehmerzahl für öffentliche Termine.

   Der Katalog ist reine Beschreibung. Alles, was sich im Betrieb ändert --
   auch der Zustand aktiv oder archiviert -- steht nicht darin, sondern in
   der Datenbank.

.. req:: Ablageverzeichnis im Projekt
   :id: REQ_DAT_KAT_03
   :status: draft
   :component: backend
   :links: REQ_DAT_KAT_01

   Die Katalogdateien liegen an einem festen Ort im Projektverzeichnis. Jede
   Instanz wird lokal betrieben.

.. req:: Schulungs-ID wird vom Menschen vergeben
   :id: REQ_DAT_KAT_04
   :status: draft
   :links: REQ_DAT_KAT_01

   Beim Anlegen einer Schulung vergibt der Administrator die ID selbst. Das
   System vergibt keine ID automatisch, zeigt aber beim Anlegen an, nach
   welchem Schema die bestehenden Schulungen benannt sind.

.. req:: Eindeutigkeit der Schulungs-ID
   :id: REQ_DAT_KAT_05
   :status: draft
   :links: REQ_DAT_KAT_04

   Eine ID, die bereits vergeben ist, wird beim Anlegen abgewiesen.

.. req:: Katalogänderung wird als Commit gesichert
   :id: REQ_DAT_GIT_01
   :status: draft
   :priority: high
   :component: backend
   :links: REQ_DAT_KAT_01

   Ändert ein Administrator die Beschreibung einer Schulung, schreibt die
   Anwendung die betroffene JSON-Datei und sichert die Änderung mit einem
   Commit im Repository.

   Das Archivieren und Reaktivieren einer Schulung gehört nicht dazu: Es
   ändert nur den Zustand in der Datenbank und lässt die Katalogdatei
   unberührt.

.. req:: Abgleich des Katalogs über das Repository
   :id: REQ_DAT_GIT_02
   :status: draft
   :links: REQ_DAT_GIT_01

   Ein Abgleich von Katalogdaten zwischen zwei Instanzen erfolgt über die
   üblichen Git-Vorgänge am Repository, nicht über eine Funktion der
   Anwendung. Datenbankinhalte werden dabei nicht übertragen.

Veränderliche Daten in der Datenbank
------------------------------------

.. req:: Eingebettete Datenbank ohne eigene Installation
   :id: REQ_DAT_DB_01
   :status: draft
   :priority: high
   :component: backend
   :links: DEC_DAT_ABLAGE_02

   Die Datenbank läuft eingebettet im Anwendungsprozess und speichert ihren
   Inhalt in einer lokalen Datei. Für den Betrieb ist keine gesonderte
   Installation und kein eigener Serverprozess nötig.

.. req:: Was in der Datenbank liegt
   :id: REQ_DAT_DB_02
   :status: draft
   :priority: high
   :component: backend
   :links: REQ_DAT_DB_01

   In der Datenbank liegen alle veränderlichen Daten:

   * Benutzerkonten, Rollen und Trainerprofile,
   * Abwesenheiten und Abwesenheitsanträge,
   * den Zustand einer Schulung (aktiv oder archiviert),
   * Termine und Trainerzuweisungen,
   * Teilnehmerbuchungen,
   * Qualifikationen und Freigabeanfragen,
   * Vormerkungen und Übernahmeanfragen,
   * Benachrichtigungen.

.. req:: Termin verweist auf die Schulung des Katalogs
   :id: REQ_DAT_DB_03
   :status: draft
   :priority: high
   :links: REQ_DAT_DB_02, REQ_DAT_KAT_04

   Ein Termin in der Datenbank verweist über die Schulungs-ID auf seine
   Schulung im Katalog. Die ID ist damit die Klammer zwischen beiden
   Ablagen.

.. req:: Verweis auf eine fehlende Schulung wird erkannt
   :id: REQ_DAT_DB_04
   :status: draft
   :links: REQ_DAT_DB_03

   Verweist ein Termin auf eine Schulungs-ID, zu der es keine Katalogdatei
   gibt, erkennt die Anwendung das und meldet es, statt den Termin
   stillschweigend ohne Schulungsdaten anzuzeigen.

.. req:: Datenbankdatei ist lokaler Zustand
   :id: REQ_DAT_DB_05
   :status: draft
   :links: REQ_DAT_DB_01

   Die Datenbankdatei gehört nicht ins Repository. Sie ist Zustand der
   einzelnen Instanz und wird nicht zwischen Instanzen abgeglichen.

.. req:: Benachrichtigungen in der Datenbank
   :id: REQ_DAT_NACHR_01
   :status: draft
   :priority: high
   :links: REQ_DAT_DB_02

   Eine Benachrichtigung wird als Datensatz gehalten, mit Empfänger, Anlass,
   Zeitpunkt und einem Kennzeichen, ob sie gelesen wurde. Ein eigenes
   Nachrichtensystem wird dafür nicht eingesetzt: Es gibt nur einen Prozess,
   und Zustellung über Prozessgrenzen hinweg ist nicht nötig.

Gleichzeitige Bearbeitung
-------------------------

.. decision:: Optimistisches statt pessimistisches Sperren
   :id: DEC_DAT_NEBEN_01
   :status: approved

   Ein Datensatz wird beim Bearbeiten nicht gesperrt. Stattdessen führt er
   einen Änderungszähler, und beim Speichern wird geprüft, ob die Änderung
   noch auf dem zuletzt gelesenen Stand beruht.

   Die zuvor erwogene Sperre hätte eine Kette von Folgeproblemen nach sich
   gezogen: Der Server kann ein geschlossenes Browserfenster nicht erkennen,
   also braucht eine Sperre Lebenszeichen; Lebenszeichen brauchen eine
   Verfallsfrist; eine Verfallsfrist braucht eine Regel, was mit dem
   Speichern nach Ablauf geschieht; und für Fälle dazwischen braucht es
   jemanden, der Sperren bricht. Der Zähler ersetzt all das durch eine
   einzige Prüfung beim Speichern.

   Der Preis: Ein Konflikt fällt erst beim Speichern auf, nicht beim
   Öffnen. Wer lange an einem Termin arbeitet, kann seine Arbeit umsonst
   gemacht haben. Dagegen hilft der Anwesenheitshinweis -- als Hinweis,
   nicht als Verbot.

.. req:: Lesen ist immer möglich
   :id: REQ_DAT_NEBEN_01
   :status: draft
   :links: DEC_DAT_NEBEN_01

   Lesen ist jederzeit und für beliebig viele Benutzer gleichzeitig möglich
   und wird nie blockiert. Auch das Öffnen zur Bearbeitung hindert niemanden
   daran, denselben Datensatz zu öffnen.

.. req:: Änderungszähler je Datensatz
   :id: REQ_DAT_NEBEN_02
   :status: draft
   :priority: high
   :component: backend
   :links: DEC_DAT_NEBEN_01

   Jeder veränderliche Datensatz führt einen Zähler, der bei jedem
   erfolgreichen Speichern erhöht wird.

.. req:: Speichern prüft den Stand
   :id: REQ_DAT_NEBEN_03
   :status: draft
   :priority: high
   :component: backend
   :links: REQ_DAT_NEBEN_02

   Beim Speichern wird geprüft, ob der Zähler des Datensatzes noch dem
   Stand entspricht, der beim Öffnen gelesen wurde. Weicht er ab, wird das
   Speichern abgewiesen.

.. req:: Meldung bei zwischenzeitlicher Änderung
   :id: REQ_DAT_NEBEN_04
   :status: draft
   :priority: high
   :links: REQ_DAT_NEBEN_03

   Wird ein Speichern wegen abweichendem Stand abgewiesen, erfährt der
   Benutzer, dass der Datensatz inzwischen von jemand anderem geändert
   wurde. Seine Eingaben bleiben dabei erhalten.

.. req:: Hinweis auf gleichzeitige Bearbeitung
   :id: REQ_DAT_NEBEN_05
   :status: draft
   :links: REQ_DAT_NEBEN_01

   Öffnet ein Benutzer einen Datensatz zur Bearbeitung, den bereits jemand
   anders offen hat, wird ihm angezeigt, wer das ist. Der Hinweis verfällt
   nach fünf Minuten ohne Aktivität.

.. req:: Der Hinweis verbietet nichts
   :id: REQ_DAT_NEBEN_06
   :status: draft
   :priority: high
   :links: REQ_DAT_NEBEN_05

   Der Hinweis auf eine gleichzeitige Bearbeitung hindert niemanden am
   Bearbeiten oder Speichern. Ob ein Speichern durchgeht, entscheidet
   allein die Prüfung des Änderungszählers.
