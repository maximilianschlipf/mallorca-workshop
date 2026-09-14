.. _schulungskatalog:

Schulungskatalog
================

Der Schulungskatalog enthält die wiederverwendbaren Weiterbildungsangebote.
Er beschreibt sie nur -- ob eine Schulung aktiv oder archiviert ist, steht in
der Datenbank.

Ablage
------

.. req:: Eine JSON-Datei je Schulung
   :id: REQ_KAT_ABL_01
   :status: approved
   :component: backend
   :links: DEC_DAT_ABLAGE_02

   Der Schulungskatalog wird in JSON-Dateien gehalten, je Schulung eine
   Datei.

Was am Katalog hängt:

.. needflow::
   :root_id: REQ_KAT_ABL_01
   :root_depth: 1
   :direction: LR

.. req:: Ablageverzeichnis im Projekt
   :id: REQ_KAT_ABL_02
   :status: approved
   :component: backend
   :links: REQ_KAT_ABL_01

   Die Katalogdateien liegen an einem festen Ort im Projektverzeichnis. Jede
   Instanz wird lokal betrieben.

.. req:: Katalogänderung wird als Commit gesichert
   :id: REQ_KAT_ABL_03
   :status: approved
   :priority: high
   :component: backend
   :links: REQ_KAT_ABL_01

   Ändert ein Administrator die Beschreibung einer Schulung, schreibt die
   Anwendung die betroffene JSON-Datei und sichert die Änderung mit einem
   Commit im Repository.

   Das Archivieren und Reaktivieren einer Schulung gehört nicht dazu: Es
   ändert nur den Zustand in der Datenbank und lässt die Katalogdatei
   unberührt.

.. req:: Abgleich des Katalogs über das Repository
   :id: REQ_KAT_ABL_04
   :status: approved
   :links: REQ_KAT_ABL_03

   Ein Abgleich von Katalogdaten zwischen zwei Instanzen erfolgt über die
   üblichen Git-Vorgänge am Repository, nicht über eine Funktion der
   Anwendung. Datenbankinhalte werden dabei nicht übertragen.

Daten einer Schulung
--------------------

.. req:: Felder einer Schulung
   :id: REQ_KAT_FELD_01
   :status: approved
   :priority: high
   :component: backend
   :links: REQ_KAT_ABL_01

   Eine Katalogdatei enthält ausschließlich die Beschreibung einer Schulung:

   * **ID** -- die Kennung, zugleich Dateiname,
   * **Titel**,
   * **Kategorie**,
   * **Kurzbeschreibung**,
   * **Voraussetzungen** -- eine Liste von Texten,
   * **Dauer in Tagen**,
   * **Mindestteilnehmerzahl** für exklusive Termine,
   * **Höchstteilnehmerzahl** für öffentliche Termine.

   Der Katalog ist reine Beschreibung. Alles, was sich im Betrieb ändert --
   auch der Zustand aktiv oder archiviert -- steht nicht darin, sondern in
   der Datenbank.

.. req:: Voraussetzungen sind Freitext
   :id: REQ_KAT_FELD_02
   :status: approved
   :links: REQ_KAT_FELD_01

   Voraussetzungen sind eine Liste freier Texte wie "Grundkenntnisse agiler
   Methoden". Sie verweisen nicht auf andere Schulungen des Katalogs und
   werden nicht geprüft.

.. req:: Dauer beträgt mindestens einen Tag
   :id: REQ_KAT_FELD_03
   :status: approved
   :priority: high
   :links: REQ_KAT_FELD_01

   Die Dauer einer Schulung ist eine ganze Zahl von mindestens einem Tag.

.. req:: Höchstteilnehmerzahl ist freiwillig
   :id: REQ_KAT_FELD_04
   :status: approved
   :priority: high
   :links: REQ_KAT_FELD_01

   Die Höchstteilnehmerzahl muss nicht angegeben werden. Fehlt sie oder ist
   sie 0, gilt für öffentliche Termine dieser Schulung keine Obergrenze.

.. req:: Höchstzahl liegt über der Mindestzahl
   :id: REQ_KAT_FELD_05
   :status: approved
   :priority: high
   :links: REQ_KAT_FELD_04

   Ist eine Höchstteilnehmerzahl angegeben, muss sie größer sein als die
   Mindestteilnehmerzahl. Eine Schulung, deren Obergrenze unter ihrer
   Untergrenze liegt, könnte nie sinnvoll besetzt werden.

.. req:: Pflichtangaben einer Schulung
   :id: REQ_KAT_FELD_06
   :status: approved
   :priority: high
   :links: REQ_KAT_FELD_01

   ID, Titel, Kategorie, Kurzbeschreibung, Dauer und Mindestteilnehmerzahl
   sind anzugeben. Freiwillig sind die Voraussetzungen -- eine leere Liste
   ist zulässig -- und die Höchstteilnehmerzahl.

Kennung
-------

.. req:: Schulungs-ID wird vom Menschen vergeben
   :id: REQ_KAT_ID_01
   :status: approved
   :links: REQ_KAT_ABL_01

   Beim Anlegen einer Schulung vergibt der Administrator die ID selbst. Das
   System vergibt keine ID automatisch, zeigt aber beim Anlegen an, nach
   welchem Schema die bestehenden Schulungen benannt sind.

.. req:: Erlaubte Zeichen in der Schulungs-ID
   :id: REQ_KAT_ID_03
   :status: approved
   :priority: high
   :links: REQ_KAT_ID_01

   Eine Schulungs-ID besteht aus Großbuchstaben, Ziffern und Bindestrichen,
   wie die bestehenden Kennungen der Form ``SCH-001``. Andere Zeichen werden
   abgewiesen.

   Die ID wird zum Dateinamen. Ohne diese Einschränkung könnte ein
   Schrägstrich oder ein Punkt in der Eingabe den Ablageort verlassen.

.. req:: Eindeutigkeit der Schulungs-ID
   :id: REQ_KAT_ID_02
   :status: approved
   :links: REQ_KAT_ID_01

   Eine ID, die bereits vergeben ist, wird beim Anlegen abgewiesen.

.. req:: Die ID bleibt unveränderlich
   :id: REQ_KAT_PFLEG_03
   :status: approved
   :priority: high
   :links: REQ_KAT_ID_01, REQ_KAT_TERM_01

   Die ID einer Schulung kann nach dem Anlegen nicht mehr geändert werden.
   Sie benennt die Katalogdatei und ist die Klammer zu allen Terminen in der
   Datenbank; eine Änderung würde beide Verbindungen zerreißen.

Kategorien
----------

.. req:: Kategorien werden gesondert gepflegt
   :id: REQ_KAT_KATG_01
   :status: approved
   :priority: high
   :links: REQ_KAT_FELD_01

   Die Kategorien des Katalogs sind eine eigene, gepflegte Liste. Ein
   Administrator kann Kategorien anlegen und umbenennen.

.. req:: Kategorie wird beim Anlegen ausgewählt
   :id: REQ_KAT_KATG_02
   :status: approved
   :priority: high
   :links: REQ_KAT_KATG_01

   Beim Anlegen oder Bearbeiten einer Schulung wird die Kategorie aus der
   bestehenden Liste ausgewählt. Sie kann dort nicht frei eingetippt werden.

   Freitext an dieser Stelle führt über die Zeit zu "IT-Security" und
   "IT Security" nebeneinander -- und der Filter nach Kategorie zerfällt in
   zwei halb gefüllte Gruppen.

.. req:: Neue Kategorie ist ein eigener Schritt
   :id: REQ_KAT_KATG_03
   :status: approved
   :priority: high
   :links: REQ_KAT_KATG_02

   Eine neue Kategorie entsteht ausschließlich über die Pflege der
   Kategorienliste, nie nebenbei beim Anlegen einer Schulung.

.. req:: Kategorienliste als versionierte JSON-Datei
   :id: REQ_KAT_KATG_05
   :status: approved
   :priority: high
   :component: backend
   :links: REQ_KAT_ABL_01, REQ_KAT_ABL_03, DEC_DAT_SCHNITT_01

   Die Kategorienliste wird als eigene JSON-Datei im Katalogverzeichnis
   geführt und wie die Schulungen versioniert: Anlegen, Umbenennen und
   Löschen einer Kategorie schreiben die Datei und werden mit einem Commit
   gesichert.

   Kategorien sind Stammdaten, die sich selten und bewusst ändern. Sie
   gehören damit zum versionierten Teil und nicht in die Datenbank, in der
   der laufende Betriebszustand liegt.

.. req:: Kategorie in Gebrauch bleibt erhalten
   :id: REQ_KAT_KATG_04
   :status: approved
   :links: REQ_KAT_KATG_01

   Eine Kategorie, der mindestens eine Schulung zugeordnet ist, kann nicht
   gelöscht werden. Das Umbenennen bleibt möglich und wirkt auf alle
   zugeordneten Schulungen.

Pflege
------

.. req:: Schulung anlegen
   :id: REQ_KAT_PFLEG_01
   :status: approved
   :priority: high
   :links: REQ_KAT_FELD_01, REQ_KAT_FELD_06

   Ein Administrator legt eine neue Schulung im Katalog an. Eine neu
   angelegte Schulung ist aktiv.

.. req:: Schulung bearbeiten
   :id: REQ_KAT_PFLEG_02
   :status: approved
   :links: REQ_KAT_PFLEG_01

   Ein Administrator ändert die Beschreibung einer bestehenden Schulung.
   Alle Felder außer der ID sind änderbar.

.. req:: Warnung bei geänderter Dauer
   :id: REQ_KAT_PFLEG_04
   :status: approved
   :priority: high
   :links: REQ_KAT_PFLEG_02

   Wird die Dauer einer Schulung geändert, zu der bereits Termine geplant
   sind, wird der Administrator gewarnt. Die Änderung wird dadurch nicht
   verhindert.

.. req:: Bestehende Termine behalten ihren Zeitraum
   :id: REQ_KAT_PFLEG_05
   :status: approved
   :priority: high
   :links: REQ_KAT_PFLEG_04

   Eine geänderte Dauer wirkt nur auf neu angelegte Termine. Bestehende
   Termine behalten Start- und Enddatum, mit denen sie geplant wurden; der
   tatsächliche Zeitraum eines Termins ergibt sich aus seinen eigenen Daten
   und nicht aus der Dauer im Katalog.

Verknüpfung mit den Terminen
----------------------------

.. req:: Termin verweist auf die Schulung des Katalogs
   :id: REQ_KAT_TERM_01
   :status: approved
   :priority: high
   :links: REQ_DAT_DB_02, REQ_KAT_ID_01

   Ein Termin in der Datenbank verweist über die Schulungs-ID auf seine
   Schulung im Katalog. Die ID ist damit die Klammer zwischen beiden
   Ablagen.

.. req:: Verweis auf eine fehlende Schulung wird erkannt
   :id: REQ_KAT_TERM_02
   :status: approved
   :priority: high
   :links: REQ_KAT_TERM_01

   Verweist ein Termin auf eine Schulungs-ID, zu der es keine Katalogdatei
   gibt, erkennt die Anwendung das und meldet es, statt den Termin
   stillschweigend ohne Schulungsdaten anzuzeigen.

Ansehen, suchen und filtern
---------------------------

.. req:: Katalog für Trainer sichtbar
   :id: REQ_KAT_SICHT_01
   :status: approved

   Ein Trainer kann den Schulungskatalog einsehen, ihn aber nicht verändern.

.. req:: Archivierte Schulungen sind nachrangig sichtbar
   :id: REQ_KAT_SICHT_02
   :status: approved
   :priority: high
   :links: REQ_KAT_SICHT_01

   Archivierte Schulungen bleiben auch für Trainer erreichbar, werden aber
   als archiviert gekennzeichnet und hinter den aktiven Schulungen
   einsortiert. Sie treten damit nicht in den Vordergrund, verschwinden aber
   auch nicht -- zu ihnen laufen weiterhin Termine.

.. req:: Keine Bewerbung auf archivierte Schulungen
   :id: REQ_KAT_SICHT_03
   :status: approved
   :links: REQ_KAT_SICHT_02, REQ_QUA_BEW_01

   Auf die Qualifikation für eine archivierte Schulung kann sich ein Trainer
   nicht bewerben.

.. req:: Termine archivierter Schulungen bleiben sichtbar
   :id: REQ_KAT_SICHT_04
   :status: approved
   :priority: high
   :links: REQ_KAT_SICHT_02, REQ_KAT_ARCH_02

   Ist ein Trainer einem Termin einer inzwischen archivierten Schulung
   zugewiesen, bleibt dieser Termin in seiner Übersicht sichtbar, obwohl die
   Schulung selbst nicht mehr in seinem Katalog erscheint. Sonst verschwände
   ein Termin, den er halten muss.

.. req:: Suche über den Titel
   :id: REQ_KAT_SUCH_01
   :status: approved
   :priority: high
   :links: REQ_KAT_SICHT_01

   Der Katalog lässt sich über einen Suchbegriff einschränken. Gefunden wird
   jede Schulung, deren Titel den Begriff als Teilzeichenfolge enthält.

.. req:: Suche ignoriert Schreibweise und Leerraum
   :id: REQ_KAT_SUCH_02
   :status: approved
   :priority: high
   :links: REQ_KAT_SUCH_01

   Die Suche unterscheidet nicht zwischen Groß- und Kleinschreibung.
   Führender und abschließender Leerraum im Suchbegriff bleibt außer
   Betracht.

.. req:: Filter nach Kategorie
   :id: REQ_KAT_SUCH_03
   :status: approved
   :priority: high
   :links: REQ_KAT_SICHT_01, REQ_KAT_KATG_01

   Der Katalog lässt sich auf eine Kategorie einschränken. Anders als die
   Suche trifft der Filter die Kategorie genau.

.. req:: Suche und Filter wirken zusammen
   :id: REQ_KAT_SUCH_04
   :status: approved
   :priority: high
   :links: REQ_KAT_SUCH_01, REQ_KAT_SUCH_03

   Suchbegriff und Kategorie können gemeinsam angegeben werden; dann muss
   eine Schulung beide Bedingungen erfüllen. Wird nichts angegeben, umfasst
   das Ergebnis den ganzen Katalog.

.. req:: Kein Treffer ist kein Fehler
   :id: REQ_KAT_SUCH_05
   :status: approved
   :links: REQ_KAT_SUCH_04

   Findet eine Anfrage nichts, ist das Ergebnis eine leere Liste und keine
   Fehlermeldung.

.. req:: Kategorienliste zur Auswahl
   :id: REQ_KAT_SUCH_06
   :status: approved
   :priority: high
   :links: REQ_KAT_SUCH_03, REQ_KAT_KATG_01

   Die zur Auswahl stehenden Kategorien werden doppelfrei und alphabetisch
   sortiert bereitgestellt.

Archivieren
-----------

.. req:: Schulung archivieren
   :id: REQ_KAT_ARCH_01
   :status: approved
   :priority: high

   Ein Administrator setzt eine aktive Schulung in den Zustand archiviert.

.. req:: Archivieren trotz zukünftiger Termine
   :id: REQ_KAT_ARCH_02
   :status: approved
   :links: REQ_KAT_ARCH_01

   Eine Schulung kann auch dann archiviert werden, wenn zu ihr noch
   zukünftige Termine geplant sind. Bestehende zukünftige Termine bleiben
   unverändert bestehen und finden statt.

.. req:: Keine neuen Termine für archivierte Schulungen
   :id: REQ_KAT_ARCH_03
   :status: approved
   :priority: high
   :links: REQ_KAT_ARCH_01

   Zu einer archivierten Schulung können keine neuen Termine angelegt
   werden.

.. req:: Archivieren lehnt offene Freigabeanfragen ab
   :id: REQ_KAT_ARCH_05
   :status: approved
   :priority: high
   :links: REQ_KAT_ARCH_01, REQ_QUA_BEW_02

   Beim Archivieren einer Schulung werden alle offenen Freigabeanfragen zu
   dieser Schulung abgelehnt. Die betroffenen Trainer werden wie bei jeder
   Ablehnung benachrichtigt.

.. req:: Schulung reaktivieren
   :id: REQ_KAT_ARCH_04
   :status: approved
   :links: REQ_KAT_ARCH_01

   Ein Administrator setzt eine archivierte Schulung zurück in den Zustand
   aktiv. Danach sind wieder neue Termine möglich. Abgelehnte
   Freigabeanfragen leben dabei nicht wieder auf.

Löschen
-------

.. req:: Schulung ohne Termine löschen
   :id: REQ_KAT_LOE_01
   :status: approved
   :links: REQ_KAT_PFLEG_01, REQ_KAT_TERM_01

   Eine Schulung, zu der derzeit kein Termin besteht, kann von einem
   Administrator gelöscht werden. Das trifft eine frisch angelegte Schulung
   ebenso wie eine, deren Termine restlos gelöscht wurden.

.. req:: Archivierte Schulung nach sechs Monaten löschen
   :id: REQ_KAT_LOE_02
   :status: approved
   :links: REQ_KAT_ARCH_01

   Eine Schulung, die seit mindestens sechs Monaten archiviert ist, kann von
   einem Administrator gelöscht werden.

.. req:: Kein Löschen in allen anderen Fällen
   :id: REQ_KAT_LOE_03
   :status: approved
   :priority: high
   :links: REQ_KAT_LOE_01, REQ_KAT_LOE_02

   Außerhalb dieser beiden Fälle kann eine Schulung nicht gelöscht werden.
   Eine aktive Schulung mit Terminen wird archiviert, nicht gelöscht.

.. req:: Löschen erhält den Titel in der Historie
   :id: REQ_KAT_LOE_04
   :status: approved
   :priority: high
   :links: REQ_KAT_LOE_02, REQ_KAT_TERM_02

   Wird eine Schulung gelöscht, zu der abgeschlossene Termine bestehen,
   bleibt ihr Titel bei diesen Terminen als Text erhalten. Die Termine
   verweisen danach nicht mehr auf eine Katalogdatei.

   Ohne diese Regel entstünden Termine, die auf eine nicht mehr vorhandene
   Schulung zeigen -- genau der Fall, den :need:`REQ_KAT_TERM_02` als Fehler
   meldet.

Aufnahme aus Dateien
--------------------

.. req:: JSON-Dateien in den Katalog aufnehmen
   :id: REQ_KAT_IMP_01
   :status: approved
   :priority: high

   Ein Administrator kann bereitgestellte JSON-Dateien in den Katalog
   aufnehmen. Das ist eine dauerhafte Fähigkeit der Anwendung, nicht ein
   einmaliger Schritt bei der Einrichtung.

.. req:: Prüfung vor der Aufnahme
   :id: REQ_KAT_IMP_02
   :status: approved
   :priority: high
   :links: REQ_KAT_IMP_01, REQ_KAT_FELD_06, REQ_KAT_ID_03

   Vor der Aufnahme wird eine Datei gegen dieselben Regeln geprüft wie eine
   Eingabe über die Oberfläche: Pflichtangaben, erlaubte Zeichen in der ID,
   Dauer und Teilnehmergrenzen. Eine Datei, die die Prüfung nicht besteht,
   wird nicht aufgenommen, und dem Administrator wird gesagt, woran es lag.

.. req:: Unbekannte Kategorie bei der Aufnahme
   :id: REQ_KAT_IMP_04
   :status: approved
   :priority: high
   :links: REQ_KAT_IMP_02, REQ_KAT_KATG_03

   Nennt eine aufzunehmende Datei eine Kategorie, die es nicht gibt, wird
   sie nicht aufgenommen. Die Kategorie wird nicht nebenbei angelegt -- das
   bleibt ein eigener Schritt.

.. req:: Bereits vergebene ID bei der Aufnahme
   :id: REQ_KAT_IMP_03
   :status: approved
   :links: REQ_KAT_IMP_02, REQ_KAT_ID_02

   Trägt eine aufzunehmende Datei eine ID, die im Katalog bereits vergeben
   ist, wird sie nicht stillschweigend übernommen. Der Administrator
   entscheidet, ob die bestehende Schulung ersetzt wird oder die Aufnahme
   unterbleibt.
