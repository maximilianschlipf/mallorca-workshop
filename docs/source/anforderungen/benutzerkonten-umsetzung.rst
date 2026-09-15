Benutzerkonten: Stories und Tests
=================================

Diese Seite übersetzt die Anforderungen aus
:need:`REQ_USR_PROF_01` und folgende in umsetzbare Stories und in Tests, die
sie abprüfen. Sie ist die Arbeitsgrundlage für die Umsetzung des Bereichs.

* Eine **Story** beschreibt einen Ablauf aus Sicht des Benutzers und
  verweist mit ``implements`` auf die Anforderungen, die sie erfüllt.
* Ein **Test** beschreibt eine nachprüfbare Beobachtung und verweist mit
  ``verifies`` auf die Story oder Anforderung, die er absichert.

Fachliche Grundlagen
---------------------

.. story:: Kontenregeln verwenden eindeutige Terminbegriffe
   :id: STORY_USR_GRUND_01
   :status: approved
   :priority: high
   :implements: REQ_QUA_UMF_01, REQ_TER_ANL_02, REQ_TER_STAT_01, REQ_TER_ZEIT_01, REQ_ASS_PLATZ_01

   Beim Verwalten eines Kontos kann das System eindeutig bestimmen, welche
   Qualifikationen, Trainerzuweisungen und Assistenzplätze betroffen sind und
   welche Termine zukünftig oder abgeschlossen sind.

.. test:: Grundlagen für kontobezogene Zuweisungen
   :id: TEST_USR_GRUND_01
   :status: approved
   :automated: yes
   :verifies: STORY_USR_GRUND_01, REQ_QUA_UMF_01, REQ_TER_ANL_02, REQ_TER_STAT_01, REQ_TER_ZEIT_01, REQ_ASS_PLATZ_01

   Eine Qualifikation gilt für alle Termine einer Schulung. Ein Termin kann
   ohne oder mit höchstens einem ausführenden Trainer sowie mit bis zu drei
   Assistenten bestehen. Ein Termin mit noch nicht vergangenem Enddatum gilt
   als zukünftig; als abgeschlossen gilt nur ein Termin mit diesem Zustand.

Erstinbetriebnahme
------------------

.. story:: Erste Registrierung macht mich zum Eigentümer
   :id: STORY_USR_EIGT_01
   :status: approved
   :priority: high
   :implements: REQ_USR_EIGT_01, REQ_USR_REG_01

   Als erste Person auf einer frischen Instanz registriere ich mich mit
   Name, E-Mail und Passwort und bin danach ohne weiteren Schritt
   handlungsfähig: Ich kann den Katalog verwalten, Termine planen und
   weitere Konten ernennen.

.. test:: Erstes Konto trägt alle drei Rollen
   :id: TEST_USR_EIGT_01
   :status: approved
   :automated: yes
   :verifies: STORY_USR_EIGT_01, REQ_USR_EIGT_01

   Auf einer Instanz ohne Benutzerkonten wird ein Konto registriert. Es
   trägt danach die Rollen Trainer, Administrator und Eigentümer.

.. test:: Parallele Erstregistrierung erzeugt genau einen Eigentümer
   :id: TEST_USR_EIGT_06
   :status: approved
   :automated: yes
   :verifies: REQ_USR_EIGT_01, REQ_USR_EIGT_02

   Treffen zwei Registrierungen gleichzeitig auf einer Instanz ohne Konten
   ein, entsteht genau ein Eigentümer. Das andere Konto trägt ausschließlich
   die Trainerrolle.

.. test:: Zweites Konto trägt nur die Trainerrolle
   :id: TEST_USR_EIGT_02
   :status: approved
   :automated: yes
   :verifies: REQ_USR_EIGT_02, REQ_USR_REG_01

   Auf einer Instanz mit einem bestehenden Konto wird ein zweites
   registriert. Es trägt ausschließlich die Trainerrolle; der Eigentümer
   bleibt unverändert das erste Konto.

Registrierung
-------------

.. story:: Ich registriere mich als Trainer
   :id: STORY_USR_REG_01
   :status: approved
   :priority: high
   :implements: REQ_USR_REG_01, REQ_USR_REG_04, REQ_USR_REG_02, REQ_USR_REG_03, REQ_USR_REG_05, REQ_USR_ROLLE_08

   Als neue Person gebe ich Name, E-Mail und Passwort an und kann mich
   sofort anmelden. Ich sehe den Schulungskatalog, kann mich auf
   Qualifikationen und auf Assistenzplätze bewerben -- eine Schulung halten
   kann ich noch nicht.

.. test:: Registrierung verlangt genau drei Angaben
   :id: TEST_USR_REG_01
   :status: approved
   :automated: yes
   :verifies: REQ_USR_REG_04

   Eine Registrierung mit Name, E-Mail und Passwort ist erfolgreich. Fehlt
   eine der drei Angaben, wird sie abgewiesen.

.. test:: Neues Konto ist sofort anmeldbar
   :id: TEST_USR_REG_02
   :status: approved
   :automated: yes
   :verifies: REQ_USR_REG_02

   Unmittelbar nach der Registrierung gelingt die Anmeldung mit den
   angegebenen Daten, ohne dass ein Administrator etwas tut.

.. test:: Neues Konto kann keinen Termin als Trainer übernehmen
   :id: TEST_USR_REG_03
   :status: approved
   :automated: yes
   :verifies: REQ_USR_REG_03, REQ_USR_ROLLE_08

   Ein frisch registriertes Konto ohne Qualifikation kann einem Termin
   nicht als ausführender Trainer zugewiesen werden. Eine Bewerbung auf
   einen Assistenzplatz desselben Termins ist möglich.

.. test:: Doppelte E-Mail-Adresse wird abgewiesen
   :id: TEST_USR_REG_04
   :status: approved
   :automated: yes
   :verifies: REQ_USR_REG_05

   Eine Registrierung mit einer bereits vergebenen E-Mail-Adresse wird mit
   einem Hinweis abgewiesen. Das gilt auch bei abweichender Groß- und
   Kleinschreibung sowie Leerzeichen am Anfang oder Ende. Die Zahl der Konten
   bleibt unverändert; die Anmeldung gelingt ebenfalls unabhängig von der
   Groß- und Kleinschreibung.

Anmeldung
---------

.. story:: Ich melde mich an und wieder ab
   :id: STORY_USR_LOGIN_01
   :status: approved
   :priority: high
   :implements: REQ_USR_LOGIN_01, REQ_USR_LOGIN_02, REQ_USR_LOGIN_03, REQ_USR_LOGIN_04, REQ_USR_LOGIN_05, REQ_USR_LOGIN_06, REQ_USR_LOGIN_07, REQ_USR_SICHER_01

   Ich melde mich mit E-Mail und Passwort an und arbeite, bis ich mich
   abmelde oder den Browser schließe. Ohne Anmeldung komme ich außer an
   Anmeldung und Registrierung an keine Ansicht der Anwendung.

.. story:: Konto- und Rollenänderungen wirken auf laufende Sitzungen
   :id: STORY_USR_LOGIN_02
   :status: approved
   :priority: high
   :implements: REQ_USR_LOGIN_08, REQ_USR_LOGIN_09

   Wird mein Konto stillgelegt oder gelöscht, endet mein Zugriff sofort.
   Werden meine Rollen geändert, gelten die neuen Rechte beim nächsten
   Aufruf.

.. test:: Anmeldung mit gültigen Daten gelingt
   :id: TEST_USR_LOGIN_01
   :status: approved
   :automated: yes
   :verifies: REQ_USR_LOGIN_01

   Mit der E-Mail-Adresse und dem Passwort eines aktiven Kontos gelingt die
   Anmeldung.

.. test:: Ansichten sind ohne Anmeldung nicht erreichbar
   :id: TEST_USR_LOGIN_02
   :status: approved
   :automated: yes
   :verifies: REQ_USR_LOGIN_02

   Anmeldung und Registrierung sind ohne Sitzung erreichbar. Der Aufruf
   einer fachlichen Ansicht oder Schnittstelle ohne bestehende Anmeldung
   führt zur Anmeldung beziehungsweise wird abgewiesen und gibt keine
   fachlichen Inhalte preis.

.. test:: Falsche Anmeldedaten nennen die Ursache nicht
   :id: TEST_USR_LOGIN_03
   :status: approved
   :automated: yes
   :verifies: REQ_USR_LOGIN_06

   Eine Anmeldung mit unbekannter E-Mail-Adresse und eine mit falschem
   Passwort werden beide abgewiesen, und beide liefern dieselbe Meldung.

.. test:: Wiederholte Fehlversuche sperren nicht
   :id: TEST_USR_LOGIN_04
   :status: approved
   :automated: yes
   :verifies: REQ_USR_LOGIN_06, DEC_USR_SICHER_01

   Nach mehreren aufeinanderfolgenden Fehlversuchen gelingt die Anmeldung
   mit dem richtigen Passwort weiterhin.

.. test:: Stillgelegtes Konto wird als solches gemeldet
   :id: TEST_USR_LOGIN_05
   :status: approved
   :automated: yes
   :verifies: REQ_USR_LOGIN_07

   Die Anmeldung eines stillgelegten Kontos mit richtigem Passwort wird
   abgewiesen, und die Meldung nennt die Stilllegung als Grund.

.. test:: Abmelden beendet die Sitzung
   :id: TEST_USR_LOGIN_06
   :status: approved
   :automated: yes
   :verifies: REQ_USR_LOGIN_05

   Nach dem Abmelden ist keine Ansicht mehr erreichbar, ohne sich erneut
   anzumelden.

.. test:: Die Sitzung überdauert den Browser nicht
   :id: TEST_USR_LOGIN_08
   :status: approved
   :automated: yes
   :level: e2e
   :verifies: REQ_USR_LOGIN_04

   Das Sitzungsmerkmal trägt kein Ablaufdatum und wird mit dem Browser
   verworfen: Nach dem Schließen und erneuten Öffnen ist eine Anmeldung
   nötig. Es gibt keine Möglichkeit, angemeldet zu bleiben.

.. test:: Stilllegen und Löschen beenden laufende Sitzungen
   :id: TEST_USR_LOGIN_09
   :status: approved
   :automated: yes
   :verifies: REQ_USR_LOGIN_08

   Ein angemeldetes Konto wird stillgelegt beziehungsweise gelöscht. Seine
   laufenden Sitzungen erhalten ab dem nächsten Aufruf keinen Zugriff mehr.

.. test:: Rollenänderungen gelten in laufenden Sitzungen
   :id: TEST_USR_LOGIN_10
   :status: approved
   :automated: yes
   :verifies: REQ_USR_LOGIN_09

   Einer angemeldeten Person wird eine Rolle entzogen. Bereits beim nächsten
   Aufruf ist eine mit dieser Rolle geschützte Funktion nicht mehr
   erreichbar.

.. test:: Standardbetrieb ist nur lokal erreichbar
   :id: TEST_USR_SICHER_01
   :status: approved
   :automated: yes
   :verifies: REQ_USR_SICHER_01

   Die Standardkonfiguration bindet den Server an die Loopback-Schnittstelle
   und schaltet die H2-Konsole ab.

Passwort
--------

.. story:: Ich ändere mein Passwort
   :id: STORY_USR_PWD_01
   :status: approved
   :implements: REQ_USR_PWD_01

   Als angemeldeter Benutzer ändere ich mein Passwort, indem ich das
   bisherige und das neue angebe.

.. story:: Ein Administrator hilft mir zurück ins Konto
   :id: STORY_USR_PWD_02
   :status: approved
   :implements: REQ_USR_PWD_02, REQ_USR_PWD_03

   Habe ich mein Passwort vergessen, setzt ein Administrator mir ein neues.
   Beim Eigentümerkonto geht das nicht -- dort ändert nur der Eigentümer
   selbst.

.. test:: Passwortänderung verlangt das bisherige Passwort
   :id: TEST_USR_PWD_01
   :status: approved
   :automated: yes
   :verifies: REQ_USR_PWD_01

   Mit dem richtigen bisherigen Passwort gelingt die Änderung, und die
   Anmeldung ist danach nur noch mit dem neuen möglich. Mit einem falschen
   bisherigen Passwort wird die Änderung abgewiesen und das alte bleibt
   gültig. Nach erfolgreicher Änderung enden alle anderen Sitzungen des
   Kontos.

.. test:: Administrator setzt ein Passwort ohne das bisherige
   :id: TEST_USR_PWD_02
   :status: approved
   :automated: yes
   :verifies: REQ_USR_PWD_02

   Ein Administrator setzt für ein fremdes Konto ein neues Passwort, ohne
   das bisherige anzugeben. Alle laufenden Sitzungen des Kontos enden und die
   Anmeldung mit dem neuen Passwort gelingt.

.. test:: Administrator kommt an das Eigentümerpasswort nicht heran
   :id: TEST_USR_PWD_03
   :status: approved
   :automated: yes
   :verifies: REQ_USR_PWD_03

   Der Versuch eines Administrators, für das Konto mit der Rolle Eigentümer
   ein Passwort zu setzen, wird abgewiesen. Das bisherige Passwort des
   Eigentümers bleibt gültig.

.. test:: Passwörter stehen nicht im Klartext
   :id: TEST_USR_PWD_04
   :status: approved
   :automated: yes
   :verifies: REQ_USR_LOGIN_03

   Nach einer Registrierung enthält der gespeicherte Datensatz das Passwort
   nicht im Klartext, und zwei Konten mit demselben Passwort haben
   unterschiedliche Hashwerte.

Rollen verwalten
----------------

.. story:: Ich ernenne einen weiteren Administrator
   :id: STORY_USR_ROLLE_01
   :status: approved
   :priority: high
   :implements: REQ_USR_ROLLE_01, REQ_USR_ROLLE_02, REQ_USR_ROLLE_03, REQ_USR_ROLLE_10

   Als Administrator gebe ich einem anderen Konto die Administratorrolle.
   Seine bisherigen Rollen behält es.

.. story:: Als Eigentümer nehme ich eine Administratorrolle zurück
   :id: STORY_USR_ROLLE_02
   :status: approved
   :priority: high
   :implements: REQ_USR_ROLLE_04, REQ_USR_ROLLE_05

   Als Eigentümer entziehe ich einem Konto die Administratorrolle. Ein
   gewöhnlicher Administrator kann das nicht.

.. story:: Ich lege ein reines Administratorkonto an
   :id: STORY_USR_ROLLE_03
   :status: approved
   :implements: REQ_USR_ROLLE_06, REQ_USR_ROLLE_09, REQ_USR_ROLLE_07

   Ein Konto registriert sich, wird zum Administrator ernannt und legt
   danach die Trainerrolle ab. Es plant fortan nur noch und hält selbst
   keine Schulungen mehr.

.. test:: Administratorrolle erteilen erhält bestehende Rollen
   :id: TEST_USR_ROLLE_01
   :status: approved
   :automated: yes
   :verifies: REQ_USR_ROLLE_03

   Nach dem Erteilen der Administratorrolle trägt das Konto sowohl Trainer
   als auch Administrator.

.. test:: Ein Administrator kann keine Administratorrolle entziehen
   :id: TEST_USR_ROLLE_02
   :status: approved
   :automated: yes
   :verifies: REQ_USR_ROLLE_04

   Der Versuch eines Administrators, einem anderen Konto die
   Administratorrolle zu entziehen, wird abgewiesen. Derselbe Vorgang durch
   den Eigentümer gelingt.

.. test:: Die letzte Rolle lässt sich nicht entziehen
   :id: TEST_USR_ROLLE_03
   :status: approved
   :automated: yes
   :verifies: REQ_USR_ROLLE_05

   Bei einem Konto mit nur einer Rolle wird der Entzug dieser Rolle
   abgewiesen; das Konto behält sie.

.. test:: Entzug der Trainerrolle räumt Zuweisungen ab
   :id: TEST_USR_ROLLE_04
   :status: approved
   :automated: yes
   :verifies: REQ_USR_ROLLE_07

   Ein Konto ist einem zukünftigen Termin als Trainer und einem zweiten als
   Assistent zugewiesen. Nach dem Entzug der Trainerrolle ist der erste
   Termin nicht zugewiesen, der Assistenzplatz des zweiten wieder frei. Ein
   abgeschlossener Termin führt das Konto weiterhin.

.. test:: Reines Administratorkonto ist nicht zuweisbar
   :id: TEST_USR_ROLLE_05
   :status: approved
   :automated: yes
   :verifies: REQ_USR_ROLLE_06

   Ein Konto ohne Trainerrolle kann weder als ausführender Trainer noch als
   Assistent einem Termin zugewiesen werden. Seine Qualifikationen, Anfragen
   und Abwesenheiten bleiben gespeichert, sind aber nicht nutzbar. Nach
   erneuter Vergabe der Trainerrolle sind sie wieder nutzbar.

.. test:: Trainer kann keine Konten verwalten
   :id: TEST_USR_ROLLE_09
   :status: approved
   :automated: yes
   :verifies: REQ_USR_ROLLE_10

   Ein Konto mit ausschließlich der Trainerrolle kann keine Rollen vergeben
   oder entziehen, kein fremdes Passwort setzen und kein Konto stilllegen,
   reaktivieren oder löschen. Den Namen eines anderen Kontos kann es ebenfalls
   nicht ändern.

.. test:: Jedes Konto trägt mindestens eine Rolle
   :id: TEST_USR_ROLLE_06
   :status: approved
   :automated: yes
   :verifies: REQ_USR_ROLLE_01

   Jedes bestehende Benutzerkonto trägt mindestens eine der Rollen Trainer,
   Administrator oder Eigentümer.

.. test:: Administrator mit Trainerrolle ist voll einsetzbar
   :id: TEST_USR_ROLLE_07
   :status: approved
   :automated: yes
   :verifies: REQ_USR_ROLLE_02

   Ein Konto mit Administrator- und Trainerrolle kann sich auf eine
   Qualifikation bewerben, eine Abwesenheit eintragen und einem Termin
   zugewiesen werden.

.. test:: Trainerrolle entziehen gelingt einem Administrator
   :id: TEST_USR_ROLLE_08
   :status: approved
   :automated: yes
   :verifies: REQ_USR_ROLLE_09

   Ein Administrator entzieht einem Konto mit zwei Rollen die Trainerrolle.
   Danach trägt das Konto nur noch die Administratorrolle.

Eigentümerrolle
---------------

.. story:: Ich gebe die Eigentümerrolle weiter
   :id: STORY_USR_EIGT_02
   :status: approved
   :priority: high
   :implements: REQ_USR_EIGT_02, REQ_USR_EIGT_03, REQ_USR_EIGT_04, REQ_USR_EIGT_05

   Bevor ich mein Konto beenden lasse, übergebe ich die Eigentümerrolle an
   ein anderes Konto. Danach bin ich ein gewöhnlicher Administrator und mein
   Konto kann stillgelegt werden.

.. test:: Weitergabe verschiebt die Rolle
   :id: TEST_USR_EIGT_03
   :status: approved
   :automated: yes
   :verifies: REQ_USR_EIGT_03, REQ_USR_EIGT_02

   Nach der Weitergabe an ein aktives reines Trainerkonto trägt das
   empfangende Konto die Eigentümer- und Administratorrolle und das abgebende
   weiterhin die Administrator-, aber nicht mehr die Eigentümerrolle. Es gibt
   weiterhin genau einen Eigentümer. Auch bei zwei gleichzeitigen
   Übergabeversuchen erhält nur eines der Zielkonten die Eigentümerrolle.

.. test:: Weitergabe an ein stillgelegtes Konto wird abgewiesen
   :id: TEST_USR_EIGT_07
   :status: approved
   :automated: yes
   :verifies: REQ_USR_EIGT_03

   Der Versuch, die Eigentümerrolle an ein stillgelegtes Konto zu übergeben,
   wird abgewiesen. Rollen und Eigentümer bleiben unverändert.

.. test:: Eigentümer behält die Administratorrolle
   :id: TEST_USR_EIGT_04
   :status: approved
   :automated: yes
   :verifies: REQ_USR_EIGT_04

   Der Versuch, dem Eigentümer die Administratorrolle zu entziehen, wird
   abgewiesen -- auch durch den Eigentümer selbst.

.. test:: Eigentümerkonto lässt sich nicht beenden
   :id: TEST_USR_EIGT_05
   :status: approved
   :automated: yes
   :verifies: REQ_USR_EIGT_05

   Stilllegen und Löschen des Eigentümerkontos werden abgewiesen. Nach der
   Weitergabe der Rolle gelingen beide.

Konto beenden
-------------

.. story:: Ich lege ein Konto still und aktiviere es wieder
   :id: STORY_USR_ENDE_01
   :status: approved
   :implements: REQ_USR_ENDE_01, REQ_USR_ENDE_04, REQ_USR_ENDE_05, REQ_USR_PROF_02

   Als Administrator lege ich ein Konto still. Es kann sich nicht mehr
   anmelden und ist aus den zukünftigen Terminen heraus. Später aktiviere
   ich es wieder; seine alten Zuweisungen kommen nicht zurück.

.. story:: Ich lösche ein Konto
   :id: STORY_USR_ENDE_02
   :status: approved
   :implements: REQ_USR_ENDE_02, REQ_USR_ENDE_03, REQ_USR_ENDE_06

   Als Administrator lösche ich ein Konto. Es verschwindet mit allen seinen
   Vorgängen, seine zukünftigen Termine werden frei -- in den
   abgeschlossenen Terminen bleibt sein Name stehen.

.. test:: Stilllegen sperrt die Anmeldung
   :id: TEST_USR_ENDE_06
   :status: approved
   :automated: yes
   :verifies: REQ_USR_ENDE_01

   Ein Administrator legt ein aktives Konto still. Dessen Anmeldung gelingt
   danach nicht mehr. Profildaten und Vorgänge bleiben erhalten; nur
   zukünftige Trainer- und Assistenzzuweisungen entfallen.

.. test:: Stilllegen räumt zukünftige Zuweisungen ab
   :id: TEST_USR_ENDE_01
   :status: approved
   :automated: yes
   :verifies: REQ_USR_ENDE_04

   Ein stillgelegtes Konto ist aus zukünftigen Terminen heraus -- als
   Trainer wie als Assistent. In abgeschlossenen Terminen bleibt es
   eingetragen.

.. test:: Reaktivieren stellt keine Zuweisung wieder her
   :id: TEST_USR_ENDE_02
   :status: approved
   :automated: yes
   :verifies: REQ_USR_ENDE_05

   Nach dem Reaktivieren ist das Konto wieder anmeldbar, die beim
   Stilllegen entfallenen Zuweisungen bestehen aber nicht wieder.

.. test:: Löschen entfernt Konto und Vorgänge
   :id: TEST_USR_ENDE_03
   :status: approved
   :automated: yes
   :verifies: REQ_USR_ENDE_02

   Nach dem Löschen existiert weder das Konto noch eine seiner
   Qualifikationen, Vormerkungen, Assistenzbewerbungen, Abwesenheiten oder
   Benachrichtigungen.

.. test:: Löschen gibt zukünftige Termine frei
   :id: TEST_USR_ENDE_04
   :status: approved
   :automated: yes
   :verifies: REQ_USR_ENDE_03

   Ein zukünftiger Termin des gelöschten Kontos ist danach nicht
   zugewiesen; ein Assistenzplatz ist wieder frei.

.. test:: Abgeschlossene Termine behalten den Namen
   :id: TEST_USR_ENDE_05
   :status: approved
   :automated: yes
   :verifies: REQ_USR_ENDE_06

   Ein abgeschlossener Termin zeigt nach dem Löschen weiterhin den Namen des
   ausführenden Trainers oder Assistenten, verweist aber nicht mehr auf ein
   Konto.

Profil
------

.. story:: Ich pflege mein Profil
   :id: STORY_USR_PROF_01
   :status: approved
   :implements: REQ_USR_PROF_01, REQ_USR_PROF_03, REQ_USR_PROF_04

   Als angemeldeter Benutzer sehe ich mein Konto mit Name, E-Mail-Adresse
   und meinen Rollen. Den Namen kann ich ändern, die E-Mail-Adresse nicht --
   sie ist meine Kennung.

.. test:: Ein Konto führt die vorgesehenen Felder
   :id: TEST_USR_PROF_03
   :status: approved
   :automated: yes
   :verifies: REQ_USR_PROF_01

   Ein Benutzerkonto führt E-Mail-Adresse, Name, Passworthash mit Salt,
   seine Rollen und seinen Zustand -- und keine Qualifikationen,
   Abwesenheiten oder Zuweisungen als eigene Felder.

.. test:: Ein Konto ist aktiv oder stillgelegt
   :id: TEST_USR_PROF_04
   :status: approved
   :automated: yes
   :verifies: REQ_USR_PROF_02

   Ein neu registriertes Konto ist aktiv. Nach dem Stilllegen ist es
   stillgelegt, nach dem Reaktivieren wieder aktiv. Ein gelöschtes Konto
   existiert nicht mehr, statt einen dritten Zustand zu tragen.

.. test:: E-Mail-Adresse lässt sich nicht ändern
   :id: TEST_USR_PROF_01
   :status: approved
   :automated: yes
   :verifies: REQ_USR_PROF_03

   Es gibt keinen Weg, die E-Mail-Adresse eines bestehenden Kontos zu
   ändern; ein Änderungsversuch wird abgewiesen.

.. test:: Name lässt sich ändern und muss nicht eindeutig sein
   :id: TEST_USR_PROF_02
   :status: approved
   :automated: yes
   :verifies: REQ_USR_PROF_04

   Ein Benutzer ändert seinen Namen auf einen bereits von einem anderen
   Konto verwendeten. Die Änderung gelingt.

.. test:: Benutzerkonten-Hauptablauf funktioniert im Browser
   :id: TEST_USR_E2E_01
   :status: approved
   :automated: yes
   :level: e2e
   :verifies: REQ_USR_EIGT_01, REQ_USR_REG_01, REQ_USR_LOGIN_01, REQ_USR_LOGIN_05, REQ_USR_PWD_01, REQ_USR_ROLLE_03, REQ_USR_PROF_04

   Auf einer frischen Instanz wird das erste Eigentümerkonto registriert.
   Danach werden ein zweites Konto, Anmeldung, Kontenverwaltung, Profil- und
   Passwortänderung und Abmeldung über die sichtbare Browseroberfläche
   erfolgreich durchlaufen.

Abdeckung
---------

Welche Anforderung durch welche Story umgesetzt und durch welchen Test
abgesichert ist:

.. needtable::
   :types: req
   :filter: id.split("_")[1] == "USR"
   :columns: id, title, status, implements_back, verifies_back
   :style: table

Anforderungen dieses Bereichs ohne Test:

.. needtable::
   :types: req
   :filter: id.split("_")[1] == "USR" and not verifies_back
   :columns: id, title, priority
   :style: table
