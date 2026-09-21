Schulungskatalog: Stories und Tests
===================================

Diese Seite übersetzt die Anforderungen des Katalogs in Abläufe und in
Tests, die sie abprüfen.

Die Katalogverwaltung ist in Backend und Oberfläche umgesetzt. Die
ausführbaren Tests wurden für die Workshop-Aufgabe entfernt. Die folgenden
Test-Needs beschreiben den erwarteten fachlichen Nachweis und stehen deshalb
auf ``approved``; sie nennen bewusst keine frühere Testmethode.

Schulungen anlegen
------------------

.. story:: Ich lege eine Schulung an
   :id: STORY_KAT_ANL_01
   :status: approved
   :priority: high
   :implements: REQ_KAT_PFLEG_01, REQ_KAT_FELD_01, REQ_KAT_FELD_02, REQ_KAT_FELD_03, REQ_KAT_FELD_04, REQ_KAT_FELD_05, REQ_KAT_FELD_06, REQ_KAT_ID_01, REQ_KAT_ID_02, REQ_KAT_ID_03, REQ_KAT_KATG_02

   Als Administrator lege ich eine Schulung an: Ich vergebe die ID -- das
   Schema der bestehenden sehe ich dabei --, gebe Titel, Kurzbeschreibung,
   Dauer und Mindestteilnehmerzahl an und wähle die Kategorie aus der Liste.
   Voraussetzungen und Höchstteilnehmerzahl kann ich weglassen. Die Schulung
   ist danach aktiv.

.. test:: Angelegte Schulung ist aktiv und vollständig
   :id: TEST_KAT_ANL_01
   :status: approved
   :automated: yes
   :verifies: STORY_KAT_ANL_01, REQ_KAT_PFLEG_01

   Nach dem Anlegen mit allen Pflichtangaben existiert die Schulung im
   Katalog, ist aktiv und gibt die eingegebenen Werte unverändert zurück.


.. test:: Fehlende Pflichtangabe wird abgewiesen
   :id: TEST_KAT_ANL_02
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_FELD_06

   Fehlt eine von ID, Titel, Kategorie, Kurzbeschreibung, Dauer oder
   Mindestteilnehmerzahl, wird das Anlegen abgewiesen. Ohne Voraussetzungen
   und ohne Höchstteilnehmerzahl gelingt es.


.. test:: Dauer unter einem Tag wird abgewiesen
   :id: TEST_KAT_ANL_03
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_FELD_03

   Eine Dauer von 0 oder weniger wird abgewiesen, eine Dauer von 1 Tag
   angenommen.


.. test:: Höchstzahl unter der Mindestzahl wird abgewiesen
   :id: TEST_KAT_ANL_04
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_FELD_05

   Bei einer Mindestteilnehmerzahl von 6 wird eine Höchstteilnehmerzahl von
   4 abgewiesen, eine von 12 angenommen.


.. test:: Fehlende oder leere Höchstzahl bedeutet keine Obergrenze
   :id: TEST_KAT_ANL_05
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_FELD_04

   Eine Schulung ohne Höchstteilnehmerzahl und eine mit dem Wert 0 werden
   beide angenommen; für öffentliche Termine beider gilt keine Obergrenze.


.. test:: Der Katalog führt genau die vorgesehenen Felder
   :id: TEST_KAT_ANL_07
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_FELD_01

   Eine Katalogdatei enthält ID, Titel, Kategorie, Kurzbeschreibung,
   Voraussetzungen, Dauer und die beiden Teilnehmerzahlen -- und weder den
   Zustand aktiv oder archiviert noch Termine, Trainer oder Teilnehmer.


.. test:: Voraussetzungen bleiben unveränderter Freitext
   :id: TEST_KAT_ANL_06
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_FELD_02

   Eine Voraussetzung wie "Grundkenntnisse agiler Methoden" wird
   unverändert gespeichert und zurückgegeben, ohne auf eine andere Schulung
   bezogen zu werden. Eine leere Liste ist zulässig.


Kennung
-------

.. test:: Die ID gibt der Mensch vor
   :id: TEST_KAT_ID_04
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_ID_01

   Beim Anlegen wird keine ID vorbelegt oder selbsttätig vergeben; ohne
   Eingabe einer ID wird das Anlegen abgewiesen. Das Schema der bestehenden
   Kennungen wird dabei angezeigt.


.. test:: Unerlaubte Zeichen in der ID werden abgewiesen
   :id: TEST_KAT_ID_01
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_ID_03

   Die IDs ``SCH-009`` und ``ABC-1`` werden angenommen. ``Scrum / Basis``,
   ``sch-009``, ``SCH_009`` und ``../SCH-009`` werden abgewiesen, und es
   entsteht keine Datei außerhalb des Ablageverzeichnisses.


.. test:: Bereits vergebene ID wird abgewiesen
   :id: TEST_KAT_ID_02
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_ID_02

   Das Anlegen einer Schulung mit der ID einer bestehenden wird abgewiesen;
   die bestehende bleibt unverändert.


.. test:: Die ID lässt sich nicht ändern
   :id: TEST_KAT_ID_03
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_PFLEG_03

   Ein Änderungsversuch an der ID einer bestehenden Schulung wird
   abgewiesen. Dateiname und die Verweise bestehender Termine bleiben
   unberührt.


Kategorien
----------

.. story:: Ich pflege die Kategorienliste
   :id: STORY_KAT_KATG_01
   :status: approved
   :priority: high
   :implements: REQ_KAT_KATG_01, REQ_KAT_KATG_03, REQ_KAT_KATG_04, REQ_KAT_KATG_05

   Als Administrator lege ich eine Kategorie an oder benenne sie um. Beim
   Anlegen einer Schulung wähle ich nur noch aus -- eine neue Kategorie
   entsteht dort nicht nebenbei. Eine Kategorie, die in Gebrauch ist, kann
   ich nicht löschen.

.. test:: Kategorie wird ausgewählt, nicht eingetippt
   :id: TEST_KAT_KATG_01
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_KATG_02, REQ_KAT_KATG_03

   Das Anlegen einer Schulung mit einer Kategorie, die nicht in der Liste
   steht, wird abgewiesen. Die Kategorienliste bleibt dabei unverändert
   lang.


.. test:: Umbenennen wirkt auf alle zugeordneten Schulungen
   :id: TEST_KAT_KATG_02
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_KATG_01

   Nach dem Umbenennen einer Kategorie führen alle zuvor zugeordneten
   Schulungen den neuen Namen, und der Filter findet sie darunter.


.. test:: Kategorienliste liegt versioniert im Katalog
   :id: TEST_KAT_KATG_04
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_KATG_05

   Die Kategorien liegen in einer JSON-Datei im Katalogverzeichnis. Nach dem
   Anlegen einer Kategorie enthält das Repository einen neuen Commit, der
   diese Datei verändert.


.. test:: Kategorie in Gebrauch lässt sich nicht löschen
   :id: TEST_KAT_KATG_03
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_KATG_04

   Das Löschen einer Kategorie mit mindestens einer zugeordneten Schulung
   wird abgewiesen. Nach dem Umhängen der letzten Schulung gelingt es.


Bearbeiten
----------

.. story:: Ich ändere eine bestehende Schulung
   :id: STORY_KAT_PFLEG_01
   :status: approved
   :implements: REQ_KAT_PFLEG_02, REQ_KAT_PFLEG_03, REQ_KAT_PFLEG_04, REQ_KAT_PFLEG_05

   Als Administrator ändere ich die Beschreibung einer Schulung. Ändere ich
   die Dauer und es bestehen bereits Termine, werde ich gewarnt -- bestehende
   Termine behalten ihren Zeitraum, die neue Dauer gilt für neue.

.. test:: Alle Felder außer der ID sind änderbar
   :id: TEST_KAT_PFLEG_01
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_PFLEG_02

   Titel, Kategorie, Kurzbeschreibung, Voraussetzungen, Dauer und beide
   Teilnehmerzahlen lassen sich ändern und sind danach im Katalog wirksam.


.. test:: Geänderte Dauer warnt und lässt Termine unberührt
   :id: TEST_KAT_PFLEG_02
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_PFLEG_04, REQ_KAT_PFLEG_05

   Bei einer Schulung mit einem geplanten Termin wird die Dauer von zwei auf
   drei Tage geändert. Die Änderung wird mit einer Warnung ausgeführt; Start-
   und Enddatum des bestehenden Termins bleiben unverändert.


Ablage und Versionierung
------------------------

.. story:: Meine Änderungen sind nachvollziehbar
   :id: STORY_KAT_ABL_01
   :status: approved
   :implements: REQ_KAT_ABL_01, REQ_KAT_ABL_02, REQ_KAT_ABL_03, REQ_KAT_ABL_04

   Was ich am Katalog ändere, landet in der JSON-Datei der Schulung und wird
   als Commit gesichert. Mit anderen gleiche ich den Katalog über das
   Repository ab.

.. test:: Jede Schulung liegt in einer eigenen Datei
   :id: TEST_KAT_ABL_01
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_ABL_01, REQ_KAT_ABL_02

   Nach dem Anlegen existiert im Ablageverzeichnis genau eine neue Datei,
   benannt nach der Schulungs-ID.


.. test:: Änderung erzeugt einen Commit
   :id: TEST_KAT_ABL_02
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_ABL_03

   Nach dem Ändern einer Beschreibung enthält das Repository einen neuen
   Commit, der die betroffene Datei verändert.


.. test:: Archivieren erzeugt keinen Commit
   :id: TEST_KAT_ABL_03
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_ABL_03

   Nach dem Archivieren und dem Reaktivieren einer Schulung ist die Zahl der
   Commits unverändert und die Katalogdatei bitgleich.


.. test:: Der Abgleich läuft nicht über die Anwendung
   :id: TEST_KAT_ABL_04
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_ABL_04

   Die Anwendung bietet keine Funktion, Katalogdaten mit einer anderen
   Instanz abzugleichen; Datenbankinhalte liegen nicht im Repository.


Ansehen, suchen und filtern
---------------------------

.. story:: Ich finde eine Schulung im Katalog
   :id: STORY_KAT_SUCH_01
   :status: approved
   :priority: high
   :implements: REQ_KAT_SICHT_01, REQ_KAT_SUCH_01, REQ_KAT_SUCH_02, REQ_KAT_SUCH_03, REQ_KAT_SUCH_04, REQ_KAT_SUCH_05, REQ_KAT_SUCH_06

   Ich durchsuche den Katalog nach einem Wort im Titel und schränke auf eine
   Kategorie ein -- einzeln oder beides zusammen. Ohne Angabe sehe ich alles,
   ohne Treffer eine leere Liste.

.. story:: Ich sehe, was archiviert ist
   :id: STORY_KAT_SICHT_01
   :status: approved
   :implements: REQ_KAT_SICHT_02, REQ_KAT_SICHT_03, REQ_KAT_SICHT_04

   Als Trainer sehe ich archivierte Schulungen gekennzeichnet und hinter den
   aktiven. Bewerben kann ich mich auf sie nicht; ein Termin, dem ich
   zugewiesen bin, bleibt mir aber erhalten.

.. test:: Ohne Angaben umfasst das Ergebnis den ganzen Katalog
   :id: TEST_KAT_SUCH_01
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_SUCH_04

   Eine Abfrage ohne Suchbegriff und ohne Kategorie liefert alle Schulungen.
   Leere Werte für beide Angaben liefern dasselbe Ergebnis wie gar keine.


.. test:: Suche ignoriert Schreibweise und Leerraum
   :id: TEST_KAT_SUCH_02
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_SUCH_01, REQ_KAT_SUCH_02

   Die Suche nach ``"  ScRuM  "`` liefert ausschließlich Schulungen, deren
   Titel "scrum" enthält, und ist nicht leer.


.. test:: Filter trifft die Kategorie genau
   :id: TEST_KAT_SUCH_03
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_SUCH_03

   Der Filter auf "Cloud & DevOps" liefert ausschließlich Schulungen dieser
   Kategorie und ist nicht leer.


.. test:: Suche und Filter greifen gemeinsam
   :id: TEST_KAT_SUCH_04
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_SUCH_04

   Suchbegriff "kubernetes" zusammen mit der Kategorie "Cloud & DevOps"
   liefert nur Schulungen, die beides erfüllen.


.. test:: Kein Treffer liefert eine leere Liste
   :id: TEST_KAT_SUCH_05
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_SUCH_05

   Eine Suche ohne Treffer wird erfolgreich beantwortet und liefert eine
   leere Liste, keine Fehlermeldung.


.. test:: Kategorienliste ist doppelfrei und sortiert
   :id: TEST_KAT_SUCH_06
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_SUCH_06

   Die bereitgestellten Kategorien enthalten keine Dubletten und sind
   alphabetisch sortiert.


.. test:: Trainer kann den Katalog nicht verändern
   :id: TEST_KAT_SICHT_01
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_SICHT_01

   Ein Konto ohne Administratorrolle kann den Katalog lesen; Anlegen,
   Ändern, Archivieren und Löschen werden ihm verweigert.


.. test:: Archivierte stehen gekennzeichnet hinter den aktiven
   :id: TEST_KAT_SICHT_02
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_SICHT_02

   In der Katalogansicht eines Trainers erscheinen archivierte Schulungen
   als archiviert gekennzeichnet und nach allen aktiven.


.. test:: Keine Bewerbung auf eine archivierte Schulung
   :id: TEST_KAT_SICHT_03
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_SICHT_03

   Der Versuch, sich auf die Qualifikation für eine archivierte Schulung zu
   bewerben, wird abgewiesen.


.. test:: Termin einer archivierten Schulung bleibt sichtbar
   :id: TEST_KAT_SICHT_04
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_SICHT_04

   Ein Trainer ist einem zukünftigen Termin zugewiesen; dessen Schulung wird
   archiviert. Der Termin erscheint weiterhin in seiner Übersicht.


Verknüpfung mit den Terminen
----------------------------

.. story:: Ein Termin findet seine Schulung
   :id: STORY_KAT_TERM_01
   :status: approved
   :priority: high
   :implements: REQ_KAT_TERM_01, REQ_KAT_TERM_02

   Ein Termin trägt die Schulungs-ID und zieht Titel, Dauer und Grenzen aus
   dem Katalog. Fehlt die Schulung dort, erfahre ich das, statt einen Termin
   ohne Inhalt zu sehen.

.. test:: Termin zieht seine Daten über die Schulungs-ID
   :id: TEST_KAT_TERM_01
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_TERM_01

   Ein Termin mit der Schulungs-ID einer bestehenden Schulung liefert deren
   Titel und Kategorie mit aus.


.. test:: Fehlende Schulung wird gemeldet
   :id: TEST_KAT_TERM_02
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_TERM_02

   Zeigt ein Termin auf eine Schulungs-ID ohne Katalogdatei, meldet die
   Anwendung das ausdrücklich, statt den Termin ohne Schulungsdaten
   anzuzeigen.


Archivieren
-----------

.. story:: Ich nehme eine Schulung aus dem Angebot
   :id: STORY_KAT_ARCH_01
   :status: approved
   :priority: high
   :implements: REQ_KAT_ARCH_01, REQ_KAT_ARCH_02, REQ_KAT_ARCH_03, REQ_KAT_ARCH_04, REQ_KAT_ARCH_05

   Als Administrator archiviere ich eine Schulung. Geplante Termine finden
   weiter statt, neue kann ich nicht mehr anlegen, offene Freigabeanfragen
   werden abgelehnt. Später reaktiviere ich sie und kann wieder planen.

.. test:: Archivieren lässt zukünftige Termine bestehen
   :id: TEST_KAT_ARCH_01
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_ARCH_01, REQ_KAT_ARCH_02

   Eine Schulung mit einem zukünftigen Termin lässt sich archivieren; der
   Termin bleibt unverändert bestehen.


.. test:: Kein neuer Termin zu einer archivierten Schulung
   :id: TEST_KAT_ARCH_02
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_ARCH_03

   Der Versuch, zu einer archivierten Schulung einen Termin anzulegen, wird
   abgewiesen. Nach dem Reaktivieren gelingt er.

   Die Regel liegt beim Katalog, weil er weiss, ob eine Schulung angeboten
   wird.

.. test:: Archivieren lehnt offene Freigabeanfragen ab
   :id: TEST_KAT_ARCH_03
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_ARCH_05

   Zu einer Schulung besteht eine offene Freigabeanfrage. Nach dem
   Archivieren ist sie abgelehnt und der Trainer benachrichtigt.


.. test:: Reaktivieren belebt keine Freigabeanfrage
   :id: TEST_KAT_ARCH_04
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_ARCH_04

   Nach dem Reaktivieren einer Schulung sind die beim Archivieren
   abgelehnten Freigabeanfragen weiterhin abgelehnt.


Löschen
-------

.. story:: Ich räume eine Schulung ab
   :id: STORY_KAT_LOE_01
   :status: approved
   :implements: REQ_KAT_LOE_01, REQ_KAT_LOE_02, REQ_KAT_LOE_03, REQ_KAT_LOE_04

   Eine Schulung ohne Termine kann ich löschen -- frisch angelegt oder
   restlos aufgeräumt. Eine seit sechs Monaten archivierte ebenfalls; dort
   bleibt der Titel in den abgeschlossenen Terminen stehen. Sonst archiviere
   ich.

.. test:: Schulung ohne Termine lässt sich löschen
   :id: TEST_KAT_LOE_01
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_LOE_01

   Eine frisch angelegte Schulung lässt sich löschen. Ebenso eine, deren
   letzter Termin zuvor gelöscht wurde.


.. test:: Aktive Schulung mit Terminen lässt sich nicht löschen
   :id: TEST_KAT_LOE_02
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_LOE_03

   Das Löschen einer aktiven Schulung mit mindestens einem Termin wird
   abgewiesen. Ebenso das einer erst seit einem Monat archivierten.


.. test:: Archivierte Schulung nach sechs Monaten löschbar
   :id: TEST_KAT_LOE_03
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_LOE_02

   Eine seit mehr als sechs Monaten archivierte Schulung lässt sich löschen,
   auch wenn zu ihr abgeschlossene Termine bestehen.


.. test:: Gelöschte Schulung hinterlässt ihren Titel
   :id: TEST_KAT_LOE_04
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_LOE_04

   Nach dem Löschen zeigt ein abgeschlossener Termin weiterhin den Titel der
   Schulung und meldet keinen fehlenden Verweis.


Aufnahme aus Dateien
--------------------

.. story:: Ich nehme bereitgestellte Dateien auf
   :id: STORY_KAT_IMP_01
   :status: approved
   :priority: high
   :implements: REQ_KAT_IMP_01, REQ_KAT_IMP_02, REQ_KAT_IMP_03, REQ_KAT_IMP_04

   Als Administrator nehme ich JSON-Dateien in den Katalog auf. Sie
   durchlaufen dieselbe Prüfung wie eine Eingabe von Hand; bei einer
   unbekannten Kategorie oder einer vergebenen ID erfahre ich, woran es lag,
   und entscheide.

.. test:: Gültige Datei wird aufgenommen
   :id: TEST_KAT_IMP_01
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_IMP_01

   Eine Datei mit allen Pflichtangaben und bekannter Kategorie wird
   aufgenommen; die Schulung erscheint danach im Katalog.


.. test:: Ungültige Datei wird mit Begründung abgewiesen
   :id: TEST_KAT_IMP_02
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_IMP_02

   Dateien mit fehlender Pflichtangabe, unerlaubten Zeichen in der ID, einer
   Dauer von 0 und einer Höchstzahl unter der Mindestzahl werden je einzeln
   abgewiesen, und die Rückmeldung nennt den Grund.


.. test:: Unbekannte Kategorie wird nicht angelegt
   :id: TEST_KAT_IMP_03
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_IMP_04

   Eine Datei mit einer unbekannten Kategorie wird abgewiesen, und die
   Kategorienliste bleibt unverändert.


.. test:: Vergebene ID wird nicht stillschweigend ersetzt
   :id: TEST_KAT_IMP_04
   :status: approved
   :automated: yes
   :verifies: REQ_KAT_IMP_03

   Eine Datei mit bereits vergebener ID wird nicht ohne Rückfrage
   übernommen; ohne ausdrückliche Entscheidung bleibt die bestehende
   Schulung unverändert.


Abdeckung
---------

.. needtable::
   :types: req
   :filter: id.split("_")[1] == "KAT"
   :columns: id, title, status, implements_back, verifies_back
   :style: table

Anforderungen dieses Bereichs ohne Test:

.. needtable::
   :types: req
   :filter: id.split("_")[1] == "KAT" and not verifies_back
   :columns: id, title, priority
   :style: table
