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

   Eine Teilnehmerbuchung besteht aus drei Feldern:

   * **Name** -- der Teilnehmer,
   * **Firma** -- das Unternehmen, für das er teilnimmt,
   * **Bemerkung** -- Freitext.

.. req:: Teilnehmerbuchungen verwalten
   :id: REQ_TLN_BUCH_03
   :status: draft
   :priority: high
   :links: REQ_TLN_BUCH_01

   Ein Administrator kann einem Termin Teilnehmerbuchungen hinzufügen,
   bestehende Buchungen ändern und Buchungen löschen.

.. req:: Erfassung nur durch Administratoren
   :id: REQ_TLN_BUCH_04
   :status: draft
   :links: REQ_TLN_BUCH_03

   Teilnehmerbuchungen werden ausschließlich von Administratoren gepflegt.

.. req:: Teilnehmerzahl eines Termins
   :id: REQ_TLN_BUCH_05
   :status: draft
   :links: REQ_TLN_BUCH_01

   Zu einem Termin ist die Anzahl seiner Teilnehmerbuchungen erkennbar.

Abgleich mit den Teilnehmergrenzen
----------------------------------

.. req:: Warnung bei Über- oder Unterschreitung
   :id: REQ_TLN_GRENZ_01
   :status: draft
   :links: REQ_TLN_BUCH_05

   Weicht die Teilnehmerzahl eines Termins von den an der Schulung
   hinterlegten Grenzen ab, wird dem Administrator eine Warnung angezeigt.

.. req:: Grenzen sind keine Sperre
   :id: REQ_TLN_GRENZ_02
   :status: draft
   :priority: high
   :links: REQ_TLN_GRENZ_01

   Die Warnung verhindert das Anlegen einer Teilnehmerbuchung nicht. Ein
   Termin kann über oder unter seinen Grenzen geführt werden; die
   Entscheidung darüber trifft der Administrator.

.. req:: Maßgebliche Grenze je Terminformat
   :id: REQ_TLN_GRENZ_03
   :status: draft
   :links: REQ_TLN_GRENZ_01

   Welche Grenze für einen Termin gilt, richtet sich nach seinem Format: Für
   öffentliche Termine ist die Höchstzahl an Teilnehmern maßgeblich, für
   exklusive Termine die Mindestzahl.
