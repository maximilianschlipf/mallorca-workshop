Teilnehmerbuchungen
===================

Eine Teilnehmerbuchung ist die Reservierung eines Platzes in einem Termin für
einen Teilnehmer. Sie ist von Terminplanung und Trainerzuweisung getrennt:
eine Buchung sagt nichts darüber aus, wer die Schulung hält.

Erfassung
---------

.. req:: Teilnehmerbuchungen am Termin
   :id: REQ_TLN_BUCH_01
   :status: draft
   :priority: high

   Zu einem Termin kann eine Liste von Teilnehmerbuchungen geführt werden.
   Jede Buchung steht für genau einen Teilnehmer und gehört zum Termin,
   nicht zur Schulung.

.. req:: Felder einer Teilnehmerbuchung
   :id: REQ_TLN_BUCH_02
   :status: draft
   :priority: high
   :links: REQ_TLN_BUCH_01

   Eine Teilnehmerbuchung besteht aus vier Feldern:

   * **Name** -- der Teilnehmer,
   * **Firma** -- das Unternehmen, für das er teilnimmt,
   * **Zuordnung** -- intern oder extern,
   * **Bemerkung** -- Freitext.

   Die Zuordnung ist nötig, weil öffentliche Termine mit internen
   Teilnehmern aufgefüllt werden, wenn Platz und Bedarf bestehen. Ohne das
   Feld ließe sich später nicht mehr sagen, wie viele Plätze tatsächlich
   verkauft wurden.

.. req:: Firma steht an jeder Buchung
   :id: REQ_TLN_BUCH_06
   :status: draft
   :links: REQ_TLN_BUCH_02

   Die Firma wird an jeder Buchung geführt, auch bei exklusiven Terminen,
   bei denen sie für alle Buchungen der Kundenfirma des Termins entspricht.
   Die Wiederholung ist der Preis dafür, dass eine Buchung für sich allein
   aussagekräftig bleibt.

   Hat ein exklusiver Termin bereits Buchungen, wird eine weitere Buchung
   mit einer anderen Firma als seiner Kundenfirma abgewiesen. Bei einer neuen
   Buchung ist die Firma mit der Kundenfirma vorbelegt. So lässt sich die
   Ein-Firma-Regel nicht über die Teilnehmerpflege umgehen.

   Für den Vergleich werden Leerzeichen am Anfang und Ende entfernt und
   Groß- und Kleinschreibung ignoriert. Angezeigt wird die Schreibweise der
   Kundenfirma am Termin.

.. req:: Teilnehmerbuchungen verwalten
   :id: REQ_TLN_BUCH_03
   :status: draft
   :priority: high
   :links: REQ_TLN_BUCH_01

   Ein Administrator kann einem Termin Teilnehmerbuchungen hinzufügen,
   bestehende Buchungen ändern und Buchungen löschen.

.. req:: Erfassung durch Administratoren
   :id: REQ_TLN_BUCH_04
   :status: draft
   :links: REQ_TLN_BUCH_03

   Teilnehmerbuchungen werden von Administratoren gepflegt. Der zugewiesene
   Trainer darf sie ebenfalls anpassen, solange der Termin geplant ist.

.. req:: Trainer sieht die Teilnehmerliste
   :id: REQ_TLN_BUCH_07
   :status: draft
   :priority: high
   :links: REQ_TLN_BUCH_02

   Der einem Termin zugewiesene Trainer sieht dessen Teilnehmerbuchungen mit
   allen Feldern. Ohne Namen, Firmen und Bemerkungen könnte er die Schulung
   nicht durchführen.

   Zugewiesene Assistenten sehen diese Teilnehmerbuchungen nicht -- weder in
   der Oberfläche noch über die Schnittstelle. Für ihre Rolle genügen die
   organisatorischen Termindaten einschließlich Kundenfirma und
   Online-Zugang; personenbezogene Teilnehmerdetails benötigen sie nicht.

.. req:: Keine Prüfung auf doppelte Namen
   :id: REQ_TLN_BUCH_08
   :status: draft
   :links: REQ_TLN_BUCH_02

   Derselbe Name darf in einem Termin mehrfach gebucht werden. Es findet
   keine Dublettenprüfung statt: Zwei Personen gleichen Namens in derselben
   Firma sind kein konstruierter Fall, und eine versehentliche
   Doppelerfassung fällt beim Eintragen ohnehin auf.

.. req:: Teilnehmerzahl eines Termins
   :id: REQ_TLN_BUCH_05
   :status: draft
   :links: REQ_TLN_BUCH_02

   Zu einem Termin ist die Anzahl seiner Teilnehmerbuchungen erkennbar,
   getrennt nach internen und externen Teilnehmern sowie als Gesamtzahl.

Tatsächliche Teilnahme
----------------------

.. req:: Teilnahme wird festgehalten
   :id: REQ_TLN_TEIL_01
   :status: draft
   :priority: high
   :links: REQ_TLN_BUCH_02

   Zu jeder Teilnehmerbuchung wird festgehalten, ob der Teilnehmer
   tatsächlich teilgenommen hat. Der Teilnahmestatus ist zunächst "offen"
   und wird ausdrücklich auf "teilgenommen" oder "nicht teilgenommen"
   gesetzt. Gebucht und nicht erschienen ist ein üblicher Fall.

.. req:: Trainer pflegt die Teilnahme vor dem Abschluss
   :id: REQ_TLN_TEIL_02
   :status: draft
   :priority: high
   :links: REQ_TLN_TEIL_01, REQ_TER_STAT_02

   Ergeben sich während der Durchführung Änderungen, passt der zugewiesene
   Trainer die Buchungen an, bevor er die Durchführung bestätigt. Die
   manuelle Bestätigung wird abgewiesen, solange mindestens eine Buchung den
   Teilnahmestatus "offen" trägt. Ein Termin ohne Buchungen kann bestätigt
   werden.

   Beim selbsttätigen Abschluss bleiben offene Teilnahmestatus erhalten. Der
   Termin geht ohnehin nicht in Teilnehmerauswertungen ein.

.. req:: Nach Abschluss oder Absage keine Änderung mehr
   :id: REQ_TLN_TEIL_03
   :status: draft
   :priority: high
   :links: REQ_TLN_TEIL_02, REQ_TER_STAT_01

   Ist ein Termin abgeschlossen oder abgesagt, können seine
   Teilnehmerbuchungen nicht mehr geändert, ergänzt oder gelöscht werden --
   auch nicht durch einen Administrator. Bei einem abgesagten Termin bleiben
   sie bis zum Ablauf der Aufbewahrungsfrist einsehbar, damit die
   Teilnehmer außerhalb des Systems informiert werden können.

.. req:: Erhalten bleibt die tatsächliche Teilnahme
   :id: REQ_TLN_TEIL_04
   :status: draft
   :priority: high
   :links: REQ_TLN_TEIL_01

   Die Zahl, die einen Termin dauerhaft beschreibt, ist die Zahl der
   tatsächlichen Teilnehmer, nicht die der Buchungen. Buchung und Bezahlung
   werden außerhalb dieses Systems verwaltet.

Abgleich mit den Teilnehmergrenzen
----------------------------------

.. req:: Warnung bei Über- oder Unterschreitung
   :id: REQ_TLN_GRENZ_01
   :status: draft
   :links: REQ_TLN_BUCH_05, REQ_TER_FORM_05

   Weicht die Teilnehmerzahl eines Termins von der für ihn maßgeblichen
   Grenze ab, wird dem Administrator eine Warnung angezeigt. Welche Grenze
   gilt, bestimmt die Zugangsart des Termins.

.. req:: Grenzen sind keine Sperre
   :id: REQ_TLN_GRENZ_02
   :status: draft
   :priority: high
   :links: REQ_TLN_GRENZ_01

   Die Warnung verhindert das Anlegen einer Teilnehmerbuchung nicht. Ein
   Termin kann über oder unter seinen Grenzen geführt werden; die
   Entscheidung darüber trifft der Administrator.

.. req:: Interne Teilnehmer zählen mit
   :id: REQ_TLN_GRENZ_03
   :status: draft
   :links: REQ_TLN_GRENZ_01

   Verglichen wird die **Gesamtzahl**: Interne Teilnehmer zählen gegen die
   Grenze mit, obwohl sie keinen Platz verkaufen. Ein öffentlicher Termin,
   der mit internen Teilnehmern aufgefüllt wurde, ist genauso voll wie einer
   mit externen.

Aufbewahrung
------------

.. req:: Teilnehmerdetails werden nach drei Kalendermonaten entfernt
   :id: REQ_TLN_AUFB_01
   :status: draft
   :priority: high
   :links: REQ_TLN_BUCH_02

   Drei Kalendermonate nach dem Beginn der Aufbewahrungsfrist werden die
   personenbezogenen Angaben seiner Teilnehmerbuchungen entfernt: Name und
   Bemerkung. Fehlt der entsprechende Tag im Zielmonat, gilt dessen letzter
   Tag. Beginnt die Frist am 31. Januar, werden die Angaben daher am 30. April
   entfernt.

.. req:: Beginn der Frist
   :id: REQ_TLN_AUFB_04
   :status: draft
   :priority: high
   :links: REQ_TLN_AUFB_01, REQ_TER_STAT_05, REQ_TER_AEND_04

   Die Frist beginnt bei einem abgeschlossenen Termin mit seinem Abschluss,
   bei einem abgesagten Termin mit dessen Absagedatum.

   Ein abgesagter Termin wird nie abgeschlossen. Das Absagedatum lässt genug
   Zeit für die manuelle Abwicklung, ohne Daten bis Monate nach einem weit in
   der Zukunft liegenden ursprünglichen Enddatum aufzubewahren.

.. req:: Zahlen und Firma bleiben erhalten
   :id: REQ_TLN_AUFB_02
   :status: draft
   :priority: high
   :links: REQ_TLN_AUFB_01, REQ_TLN_TEIL_04

   Erhalten bleiben die Zahl der tatsächlichen Teilnehmer, getrennt nach
   intern und extern, sowie die Firma jeder Buchung. Die Firma ist nicht
   personenbezogen und beantwortet die Frage, welche Kunden geschult wurden.

.. req:: Entfernen läuft ohne Zutun
   :id: REQ_TLN_AUFB_03
   :status: draft
   :links: REQ_TLN_AUFB_01

   Das Entfernen der Teilnehmerdetails geschieht selbsttätig nach Ablauf der
   Frist. Es ist keine Handlung eines Administrators nötig, und es lässt
   sich nicht rückgängig machen.

   War die lokale Anwendung am Stichtag nicht in Betrieb, holt sie das
   Entfernen beim nächsten Start nach. Bleibt sie über den Stichtag hinweg
   geöffnet, prüft sie die Fälligkeit einmal pro Kalendertag; spätestens vor
   dem ersten fachlichen Zugriff des Tages sind die Angaben entfernt. Eine
   sekundengenaue Ausführung um Mitternacht und eine zusätzliche technische
   Verarbeitungshistorie sind nicht erforderlich.
