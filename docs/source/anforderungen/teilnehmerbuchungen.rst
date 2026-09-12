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
   bei denen sie für alle Buchungen dieselbe ist. Die Wiederholung ist der
   Preis dafür, dass eine Buchung für sich allein aussagekräftig bleibt.

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
   Trainer darf sie bis zum Abschluss seines Termins ebenfalls anpassen.

.. req:: Trainer sieht die Teilnehmerliste
   :id: REQ_TLN_BUCH_07
   :status: draft
   :priority: high
   :links: REQ_TLN_BUCH_02

   Der einem Termin zugewiesene Trainer sieht dessen Teilnehmerbuchungen mit
   allen Feldern. Ohne Namen, Firmen und Bemerkungen könnte er die Schulung
   nicht durchführen.

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
   tatsächlich teilgenommen hat. Gebucht und nicht erschienen ist ein
   üblicher Fall.

.. req:: Trainer pflegt die Teilnahme vor dem Abschluss
   :id: REQ_TLN_TEIL_02
   :status: draft
   :priority: high
   :links: REQ_TLN_TEIL_01, REQ_TER_STAT_02

   Ergeben sich während der Durchführung Änderungen, passt der zugewiesene
   Trainer die Buchungen an, bevor er den Termin abschließt. Der Abschluss
   ist die Erklärung, dass die Liste stimmt.

.. req:: Nach dem Abschluss keine Änderung mehr
   :id: REQ_TLN_TEIL_03
   :status: draft
   :priority: high
   :links: REQ_TLN_TEIL_02, REQ_TER_STAT_01

   Ist ein Termin abgeschlossen, können seine Teilnehmerbuchungen nicht mehr
   geändert, ergänzt oder gelöscht werden -- auch nicht durch einen
   Administrator.

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

.. req:: Teilnehmerdetails werden nach drei Monaten entfernt
   :id: REQ_TLN_AUFB_01
   :status: draft
   :priority: high
   :links: REQ_TLN_BUCH_02

   Drei Monate nachdem ein Termin vorüber ist, werden die
   personenbezogenen Angaben seiner Teilnehmerbuchungen entfernt: Name und
   Bemerkung.

.. req:: Beginn der Frist
   :id: REQ_TLN_AUFB_04
   :status: draft
   :priority: high
   :links: REQ_TLN_AUFB_01, REQ_TER_STAT_05

   Die Frist beginnt bei einem abgeschlossenen Termin mit seinem Abschluss,
   bei einem abgesagten Termin mit dessen ursprünglichem Enddatum.

   Ein abgesagter Termin wird nie abgeschlossen; ohne diese Festlegung
   blieben seine Teilnehmerdaten dauerhaft liegen.

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
