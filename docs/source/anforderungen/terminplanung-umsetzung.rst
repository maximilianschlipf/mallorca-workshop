Terminplanung: Stories und Tests
================================

Diese Seite übersetzt die Anforderungen der Terminplanung in Abläufe und in
Tests, die sie abprüfen.

Termine anlegen
---------------

.. story:: Ich plane einen Termin
   :id: STORY_TER_ANL_01
   :status: draft
   :priority: high
   :implements: REQ_TER_ANL_01, REQ_TER_ANL_03, REQ_TER_ANL_04, REQ_TER_ANL_05, REQ_TER_ANL_06, REQ_TER_ID_01, REQ_TER_ID_02

   Als Administrator wähle ich eine aktive Schulung und ein Startdatum. Das
   Enddatum schlägt mir das System aus der Dauer vor; ich kann es anpassen.
   Mehr muss ich nicht angeben -- die Kennung vergibt das System.

.. story:: Ich trage die Rahmendaten später nach
   :id: STORY_TER_ANL_02
   :status: draft
   :priority: high
   :implements: REQ_TER_ANL_02, REQ_TER_FORM_07, REQ_TER_AEND_03

   Steht noch nicht fest, ob der Termin remote oder vor Ort läuft und wer
   ihn hält, lasse ich das offen und ergänze es, sobald es entschieden ist.

.. test:: Termin mit Pflichtangaben wird angelegt
   :id: TEST_TER_ANL_01
   :status: draft
   :automated: yes
   :verifies: STORY_TER_ANL_01, REQ_TER_ANL_03

   Mit Schulung, Startdatum und Enddatum wird ein Termin angelegt. Er ist
   geplant, nicht zugewiesen und ohne Zugangsart, Durchführungsart und Ort.

.. test:: Fehlende Pflichtangabe wird abgewiesen
   :id: TEST_TER_ANL_02
   :status: draft
   :automated: yes
   :verifies: REQ_TER_ANL_03

   Fehlt Schulung, Startdatum oder Enddatum, wird das Anlegen abgewiesen.

.. test:: Enddatum wird aus der Dauer vorgeschlagen
   :id: TEST_TER_ANL_03
   :status: draft
   :automated: yes
   :verifies: REQ_TER_ANL_04

   Bei einer dreitägigen Schulung und einem Startdatum schlägt das System
   ein Enddatum zwei Tage später vor. Ein abweichendes Enddatum -- kürzer
   wie länger -- wird angenommen.

.. test:: Enddatum vor Startdatum wird abgewiesen
   :id: TEST_TER_ANL_04
   :status: draft
   :automated: yes
   :verifies: REQ_TER_ANL_05

   Ein Enddatum vor dem Startdatum wird abgewiesen; Start und Ende am selben
   Tag werden angenommen.

.. test:: Termin in der Vergangenheit wird abgewiesen
   :id: TEST_TER_ANL_05
   :status: draft
   :automated: yes
   :verifies: REQ_TER_ANL_06

   Ein Startdatum in der Vergangenheit wird abgewiesen, das heutige Datum
   angenommen.

.. test:: Zu archivierter Schulung entsteht kein Termin
   :id: TEST_TER_ANL_06
   :status: draft
   :automated: yes
   :verifies: REQ_TER_ANL_01

   Das Anlegen eines Termins zu einer archivierten Schulung wird abgewiesen.

.. test:: Termin ohne Trainer ist gültig
   :id: TEST_TER_ANL_07
   :status: draft
   :automated: yes
   :verifies: REQ_TER_ANL_02

   Ein ohne Trainer gespeicherter Termin gilt als nicht zugewiesen und
   erscheint in der Übersicht der Termine ohne Trainer.

Kennung
-------

.. test:: Termin-ID wird fortlaufend vergeben
   :id: TEST_TER_ID_01
   :status: draft
   :automated: yes
   :verifies: REQ_TER_ID_01

   Der erste Termin zur Schulung ``SCH-001`` erhält ``SCH-001-T0001``, der
   zweite ``SCH-001-T0002``. Der erste Termin einer anderen Schulung beginnt
   wieder bei ``0001``. Eine vom Aufrufer mitgegebene Kennung wird nicht
   übernommen.

.. test:: Termin-ID bleibt beim Ändern bestehen
   :id: TEST_TER_ID_02
   :status: draft
   :automated: yes
   :verifies: REQ_TER_ID_02

   Nach Verschieben und Absagen trägt der Termin dieselbe Kennung wie beim
   Anlegen.

Zugangsart und Durchführungsart
-------------------------------

.. story:: Ich lege fest, wie der Termin stattfindet
   :id: STORY_TER_FORM_01
   :status: draft
   :implements: REQ_TER_FORM_01, REQ_TER_FORM_02, REQ_TER_FORM_03, REQ_TER_FORM_04, REQ_TER_FORM_05, REQ_TER_FORM_06

   Ich gebe an, ob der Termin öffentlich oder exklusiv ist und ob er remote,
   vor Ort, beim Kunden oder hybrid stattfindet. Außer bei remote trage ich
   einen Ort ein.

.. test:: Zugangsart kennt zwei Werte
   :id: TEST_TER_FORM_01
   :status: draft
   :automated: yes
   :verifies: REQ_TER_FORM_01

   "öffentlich" und "exklusiv" werden angenommen, andere Werte abgewiesen.

.. test:: Durchführungsart kennt vier Werte
   :id: TEST_TER_FORM_02
   :status: draft
   :automated: yes
   :verifies: REQ_TER_FORM_02

   "remote", "vor Ort", "beim Kunden" und "hybrid" werden angenommen,
   andere Werte abgewiesen.

.. test:: Alle Kombinationen sind erlaubt
   :id: TEST_TER_FORM_03
   :status: draft
   :automated: yes
   :verifies: REQ_TER_FORM_03

   Jede der acht Kombinationen aus Zugangsart und Durchführungsart lässt
   sich speichern, einschließlich "öffentlich" mit "beim Kunden".

.. test:: Ort gehört zu drei Durchführungsarten
   :id: TEST_TER_FORM_04
   :status: draft
   :automated: yes
   :verifies: REQ_TER_FORM_04

   Bei "vor Ort", "beim Kunden" und "hybrid" wird ein Ort als freier Text
   gespeichert. Bei "remote" bleibt er leer.

.. test:: Beide Angaben lassen sich nachtragen
   :id: TEST_TER_FORM_05
   :status: draft
   :automated: yes
   :verifies: REQ_TER_FORM_07

   Ein Termin ohne Zugangsart und Durchführungsart wird gespeichert; beide
   Angaben lassen sich später ergänzen.

.. test:: Ohne Zugangsart wird keine Grenze geprüft
   :id: TEST_TER_FORM_06
   :status: draft
   :automated: yes
   :verifies: REQ_TER_FORM_06

   An einem Termin ohne Zugangsart entstehen Teilnehmerbuchungen über der
   Höchstzahl der Schulung, ohne dass gewarnt wird. Nach dem Nachtragen von
   "öffentlich" erscheint die Warnung.

.. test:: Zugangsart bestimmt die Grenze
   :id: TEST_TER_FORM_07
   :status: draft
   :automated: yes
   :verifies: REQ_TER_FORM_05

   Bei einem öffentlichen Termin wird gegen die Höchstzahl gewarnt, bei
   einem exklusiven gegen die Mindestzahl -- unabhängig von der
   Durchführungsart.

Zustände und Abschluss
----------------------

.. story:: Ich schließe meinen Termin ab
   :id: STORY_TER_STAT_01
   :status: draft
   :priority: high
   :implements: REQ_TER_STAT_01, REQ_TER_STAT_02, REQ_TER_STAT_04, REQ_TER_STAT_05

   Als zugewiesener Trainer schließe ich meinen Termin nach der
   Durchführung ab. Vorher geht das nicht; mit dem Abschluss steht fest, wer
   teilgenommen hat.

.. story:: Ein vergessener Termin kommt trotzdem zum Abschluss
   :id: STORY_TER_STAT_02
   :status: draft
   :implements: REQ_TER_STAT_03, REQ_TER_STAT_06, REQ_TER_STAT_07, REQ_TER_STAT_08

   Schließt der Trainer nicht ab, kann ein Administrator einspringen.
   Geschieht auch das nicht, schließt das System nach zwei Monaten selbst --
   erkennbar als ungeprüft, und ohne in Auswertungen einzugehen.

.. test:: Ein Termin trägt genau drei Zustände
   :id: TEST_TER_STAT_01
   :status: draft
   :automated: yes
   :verifies: REQ_TER_STAT_01

   Ein Termin ist geplant, abgeschlossen oder abgesagt. Ein Zustand
   "ausgebucht" existiert nicht; ein Termin über seiner Höchstteilnehmerzahl
   bleibt geplant.

.. test:: Der zugewiesene Trainer schließt ab
   :id: TEST_TER_STAT_02
   :status: draft
   :automated: yes
   :verifies: REQ_TER_STAT_02

   Der zugewiesene Trainer kann seinen Termin nach dem Enddatum
   abschließen; ein anderer Trainer kann es nicht.

.. test:: Administrator kann ersatzweise abschließen
   :id: TEST_TER_STAT_03
   :status: draft
   :automated: yes
   :verifies: REQ_TER_STAT_03

   Ein Administrator schließt einen Termin ab, dem er selbst nicht
   zugewiesen ist.

.. test:: Kein Abschluss vor dem Enddatum
   :id: TEST_TER_STAT_04
   :status: draft
   :automated: yes
   :verifies: REQ_TER_STAT_04

   Der Abschluss eines Termins, dessen Enddatum noch nicht erreicht ist,
   wird abgewiesen.

.. test:: Abschluss hält Zeitpunkt und Person fest
   :id: TEST_TER_STAT_05
   :status: draft
   :automated: yes
   :verifies: REQ_TER_STAT_05

   Ein abgeschlossener Termin führt den Zeitpunkt des Abschlusses und wer
   ihn vorgenommen hat.

.. test:: Selbsttätiger Abschluss nach zwei Monaten
   :id: TEST_TER_STAT_06
   :status: draft
   :automated: yes
   :verifies: REQ_TER_STAT_06, REQ_TER_STAT_07

   Ein Termin, dessen Enddatum mehr als zwei Monate zurückliegt und der noch
   geplant ist, wird selbsttätig abgeschlossen und ist als nicht bestätigt
   gekennzeichnet.

.. test:: Ungeprüfte Termine bleiben aus Auswertungen heraus
   :id: TEST_TER_STAT_07
   :status: draft
   :automated: yes
   :verifies: REQ_TER_STAT_08

   Eine Auswertung der Teilnehmerzahlen berücksichtigt bestätigte Termine
   und lässt selbsttätig abgeschlossene aus. Deren Daten bleiben einsehbar.

Ändern, absagen, löschen
------------------------

.. story:: Ich verschiebe einen Termin
   :id: STORY_TER_AEND_01
   :status: draft
   :priority: high
   :implements: REQ_TER_AEND_01, REQ_TER_AEND_02

   Als Administrator ändere ich den Zeitraum eines geplanten Termins. Passt
   die bestehende Trainerzuweisung nicht mehr, erfahre ich das und die
   Verschiebung unterbleibt.

.. story:: Ich sage einen Termin ab
   :id: STORY_TER_AEND_02
   :status: draft
   :priority: high
   :implements: REQ_TER_AEND_04, REQ_TER_AEND_05, REQ_TER_AEND_07, REQ_TER_AEND_08, REQ_TER_AEND_09

   Vor der Absage sehe ich, was daran hängt -- Buchungen, Trainer,
   Assistenten. Trainer und Assistenten werden benachrichtigt, die
   Teilnehmer muss ich selbst erreichen. Rückgängig machen kann ich die
   Absage nicht.

.. story:: Ich lösche einen Termin
   :id: STORY_TER_AEND_03
   :status: draft
   :implements: REQ_TER_AEND_06, REQ_TER_AEND_10

   Einen geplanten oder abgesagten Termin kann ich löschen. Einen
   abgeschlossenen nicht -- er ist die Aufzeichnung einer gehaltenen
   Schulung.

.. test:: Verschieben hält sich an die Zeitraumregeln
   :id: TEST_TER_AEND_01
   :status: draft
   :automated: yes
   :verifies: REQ_TER_AEND_01

   Ein geplanter Termin lässt sich in die Zukunft verschieben. Ein Zeitraum
   in der Vergangenheit und ein Enddatum vor dem Startdatum werden
   abgewiesen.

.. test:: Verschieben prüft die Zuweisung erneut
   :id: TEST_TER_AEND_02
   :status: draft
   :automated: yes
   :verifies: REQ_TER_AEND_02

   Wird ein Termin in einen Zeitraum verschoben, in dem der zugewiesene
   Trainer abwesend oder anderweitig zugewiesen ist, wird das Verschieben
   abgewiesen und der Termin bleibt unverändert.

.. test:: Rahmendaten lassen sich ändern
   :id: TEST_TER_AEND_03
   :status: draft
   :automated: yes
   :verifies: REQ_TER_AEND_03

   Ort, Zugangsart und Durchführungsart eines geplanten Termins lassen sich
   ändern und erstmals angeben.

.. test:: Absage erhält den Termin
   :id: TEST_TER_AEND_04
   :status: draft
   :automated: yes
   :verifies: REQ_TER_AEND_04

   Ein abgesagter Termin bleibt mit allen Daten bestehen, trägt den Zustand
   abgesagt und erscheint nicht mehr unter den anstehenden Terminen.

.. test:: Absage ist nicht umkehrbar
   :id: TEST_TER_AEND_05
   :status: draft
   :automated: yes
   :verifies: REQ_TER_AEND_09

   Der Versuch, einen abgesagten Termin wieder in den Zustand geplant zu
   versetzen, wird abgewiesen.

.. test:: Absage benachrichtigt Trainer und Assistenten
   :id: TEST_TER_AEND_06
   :status: draft
   :automated: yes
   :verifies: REQ_TER_AEND_05, REQ_TER_AEND_08

   Nach der Absage liegen Benachrichtigungen für den zugewiesenen Trainer
   und alle Assistenten vor. Für gebuchte Teilnehmer entsteht keine.

.. test:: Warnung nennt, was am Termin hängt
   :id: TEST_TER_AEND_07
   :status: draft
   :automated: yes
   :verifies: REQ_TER_AEND_07

   Vor Absage und Löschen werden Anzahl der Teilnehmerbuchungen,
   zugewiesener Trainer und Assistenten aufgeführt. Der Vorgang lässt sich
   danach ausführen.

.. test:: Geplante und abgesagte Termine lassen sich löschen
   :id: TEST_TER_AEND_08
   :status: draft
   :automated: yes
   :verifies: REQ_TER_AEND_06

   Ein geplanter und ein abgesagter Termin lassen sich löschen und sind
   danach nicht mehr vorhanden.

.. test:: Abgeschlossener Termin lässt sich nicht löschen
   :id: TEST_TER_AEND_09
   :status: draft
   :automated: yes
   :verifies: REQ_TER_AEND_10

   Das Löschen eines abgeschlossenen Termins wird abgewiesen; er bleibt mit
   Trainer und Teilnehmerzahlen erhalten.

Trainerzuweisung
----------------

.. story:: Ich weise einem Termin einen Trainer zu
   :id: STORY_TER_ZUW_01
   :status: draft
   :priority: high
   :implements: REQ_TER_ZUW_01, REQ_TER_ZUW_03, REQ_TER_VORS_01, REQ_TER_VORS_03, REQ_TER_VORS_02

   Beim Zuweisen sehe ich zuerst die qualifizierten Trainer, danach die
   übrigen. Wer im Zeitraum abwesend oder schon eingeplant ist, ist
   erkennbar und nicht auswählbar; ein Kalender zeigt mir geeignete
   Zeiträume.

.. story:: Ich tausche einen Trainer aus
   :id: STORY_TER_ZUW_02
   :status: draft
   :implements: REQ_TER_ZUW_04, REQ_TER_ZUW_05, REQ_TER_ZUW_06

   Ich ziehe einen zugewiesenen Trainer ab oder ersetze ihn unmittelbar
   durch einen anderen. An abgeschlossenen und abgesagten Terminen geht das
   nicht mehr.

.. story:: Ich setze einen noch nicht qualifizierten Trainer ein
   :id: STORY_TER_ZUW_03
   :status: draft
   :implements: REQ_TER_ZUW_02

   Ich weise einen Trainer ohne Qualifikation zu. Er ist sofort eingeplant
   und bekommt ein Qualifikationsangebot; lehnt er ab, ist der Termin wieder
   frei.

.. test:: Keine Zuweisung bei Abwesenheit
   :id: TEST_TER_ZUW_01
   :status: draft
   :automated: yes
   :verifies: REQ_TER_ZUW_01

   Die Zuweisung eines Trainers mit aktiver Abwesenheit im Zeitraum des
   Termins wird abgewiesen -- ohne Möglichkeit, sie zu übergehen.

.. test:: Keine überschneidende Zuweisung
   :id: TEST_TER_ZUW_02
   :status: draft
   :automated: yes
   :verifies: REQ_TER_ZUW_03

   Ein Trainer, der im Zeitraum bereits einem anderen Termin als Trainer
   oder als Assistent zugewiesen ist, kann nicht zugewiesen werden.

.. test:: Proaktive Zuweisung erzeugt ein Angebot
   :id: TEST_TER_ZUW_03
   :status: draft
   :automated: yes
   :verifies: REQ_TER_ZUW_02

   Nach der Zuweisung eines nicht qualifizierten Trainers gilt der Termin
   als zugewiesen und für den Trainer liegt ein Qualifikationsangebot vor.

.. test:: Abziehen gibt den Termin frei
   :id: TEST_TER_ZUW_04
   :status: draft
   :automated: yes
   :verifies: REQ_TER_ZUW_04

   Nach dem Abziehen gilt der Termin als nicht zugewiesen, und der
   bisherige Trainer ist benachrichtigt.

.. test:: Austauschen prüft wie eine Erstzuweisung
   :id: TEST_TER_ZUW_05
   :status: draft
   :automated: yes
   :verifies: REQ_TER_ZUW_05

   Der Austausch gegen einen im Zeitraum abwesenden oder anderweitig
   zugewiesenen Trainer wird abgewiesen; der bisherige bleibt zugewiesen.

.. test:: Keine Zuweisung an abgeschlossenen Terminen
   :id: TEST_TER_ZUW_06
   :status: draft
   :automated: yes
   :verifies: REQ_TER_ZUW_06

   Zuweisen, Abziehen und Austauschen werden an abgeschlossenen und an
   abgesagten Terminen abgewiesen.

.. test:: Trainerliste ist gruppiert
   :id: TEST_TER_VORS_01
   :status: draft
   :automated: yes
   :verifies: REQ_TER_VORS_01

   Die Liste beim Planen führt zuerst die für die Schulung qualifizierten
   Trainer, danach die übrigen, und beide Gruppen sind unterscheidbar.

.. test:: Nicht verfügbare Trainer sind gekennzeichnet
   :id: TEST_TER_VORS_02
   :status: draft
   :automated: yes
   :verifies: REQ_TER_VORS_03

   Ein im Zeitraum abwesender und ein anderweitig zugewiesener Trainer
   erscheinen mit dem jeweiligen Grund und sind nicht auswählbar.

.. test:: Kalender zeigt Abwesenheiten und Zuweisungen
   :id: TEST_TER_VORS_03
   :status: draft
   :automated: no
   :verifies: REQ_TER_VORS_02

   Die Kalenderansicht zeigt für die angezeigten Trainer aktive
   Abwesenheiten und bestehende Zuweisungen im Umfeld des geplanten
   Zeitraums.

Dashboard
---------

.. story:: Ich sehe, worum ich mich kümmern muss
   :id: STORY_TER_DASH_01
   :status: draft
   :priority: high
   :implements: REQ_TER_DASH_01, REQ_TER_DASH_02, REQ_TER_DASH_03, REQ_TER_DASH_04

   Auf meinem Dashboard stehen zuerst die überfälligen Termine ohne
   Abschluss, danach die Termine ohne Trainer nach Dringlichkeit, und ich
   sehe, wo die Mindestteilnehmerzahl in Gefahr ist.

.. test:: Termine ohne Trainer stehen nach Datum sortiert
   :id: TEST_TER_DASH_01
   :status: draft
   :automated: yes
   :verifies: REQ_TER_DASH_01

   Das Dashboard führt alle zukünftigen Termine ohne Trainerzuweisung
   aufsteigend nach Startdatum.

.. test:: Warnstufen richten sich nach dem Vorlauf
   :id: TEST_TER_DASH_02
   :status: draft
   :automated: yes
   :verifies: REQ_TER_DASH_02

   Ein Termin in zwei Wochen trägt die höchste Warnstufe, einer in zwei
   Monaten die mittlere, einer in einem halben Jahr keine.

.. test:: Überfällige Termine stehen vor den anstehenden
   :id: TEST_TER_DASH_03
   :status: draft
   :automated: yes
   :verifies: REQ_TER_DASH_04

   Ein Termin, dessen Enddatum vorüber ist und der noch geplant ist, steht
   im Dashboard vor allen anstehenden Terminen.

.. test:: Zu wenige Teilnehmer werden gewarnt
   :id: TEST_TER_DASH_04
   :status: draft
   :automated: yes
   :verifies: REQ_TER_DASH_03

   Ein Termin in drei Wochen unter seiner Mindestteilnehmerzahl wird im
   Dashboard gewarnt; einer in drei Monaten noch nicht.

Übernahme der bestehenden Daten
-------------------------------

.. story:: Die vorhandenen Termine ziehen mit um
   :id: STORY_TER_MIGR_01
   :status: draft
   :implements: REQ_TER_MIGR_01

   Beim Umbau auf die neue Ablage übernehme ich die vorhandenen Termine,
   ohne sie von Hand nachzupflegen: Zustände, Durchführungsart, Zugangsart
   und Kennungen werden umgeschlüsselt.

.. test:: Bestehende Termine werden vollständig umgeschlüsselt
   :id: TEST_TER_MIGR_01
   :status: draft
   :automated: yes
   :verifies: REQ_TER_MIGR_01

   Nach der Übernahme trägt kein Termin mehr den Zustand "ausgebucht",
   keiner die Durchführungsart "Präsenz" oder "Online", alle bisherigen
   Termine sind öffentlich, und jede Kennung hat die vierstellige Form.

Abdeckung
---------

.. needtable::
   :types: req
   :filter: id.split("_")[1] == "TER"
   :columns: id, title, status, implements_back, verifies_back
   :style: table

Anforderungen dieses Bereichs ohne Test:

.. needtable::
   :types: req
   :filter: id.split("_")[1] == "TER" and not verifies_back
   :columns: id, title, priority
   :style: table
