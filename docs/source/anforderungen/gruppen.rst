.. _gruppen:

Gruppen
=======

Eine Gruppe fasst Mitarbeitende zusammen, die gemeinsam an Schulungen
teilnehmen, etwa die Gruppe "Mallorca". Sie hat einen Gruppentrainer, der
ihre Termine üblicherweise hält. Ein Termin kann einer Gruppe zugeordnet
werden; die Gruppe ist damit das "wer" neben dem "was" der Schulung.

Gruppen pflegen
---------------

.. req:: Gruppe anlegen
   :id: REQ_GRP_ANL_01
   :status: approved
   :priority: high

   Ein Administrator legt eine Gruppe mit einem Namen an. Der Name ist
   erforderlich, höchstens 255 Zeichen lang und ohne Beachtung von Groß- und
   Kleinschreibung eindeutig. Führende und nachfolgende Leerzeichen werden
   entfernt.

.. req:: Mitglieder der Gruppe
   :id: REQ_GRP_MIT_01
   :status: approved
   :priority: high
   :links: REQ_GRP_ANL_01

   Ein Administrator fügt einer Gruppe aktive Benutzerkonten als Mitglieder
   hinzu und entfernt sie wieder. Das mehrfache Hinzufügen desselben Kontos
   ändert nichts.

.. req:: Gruppentrainer
   :id: REQ_GRP_TRA_01
   :status: approved
   :priority: high
   :links: REQ_GRP_ANL_01

   Eine Gruppe hat höchstens einen Gruppentrainer. Er ist ein aktiver
   Trainer und zugleich kein Mitglied derselben Gruppe.

.. req:: Nur Administratoren pflegen Gruppen
   :id: REQ_GRP_BER_01
   :status: approved
   :priority: high
   :links: REQ_GRP_ANL_01

   Anlegen, Ändern, Mitglieder pflegen und Löschen einer Gruppe sind
   Administratoren vorbehalten.

.. req:: Gruppen sind für alle Angemeldeten sichtbar
   :id: REQ_GRP_BER_02
   :status: approved
   :links: REQ_GRP_BER_01

   Jeder angemeldete Benutzer sieht die Gruppen mit ihrem Gruppentrainer und
   ihren Mitgliedern.

.. req:: Gruppe löschen
   :id: REQ_GRP_LOE_01
   :status: approved
   :links: REQ_GRP_ANL_01, REQ_TER_ZEIT_02

   Eine Gruppe mit geplanten Terminen kann nicht gelöscht werden. Andernfalls
   wird sie gelöscht; ihre bisherigen Termine bleiben ohne Gruppe erhalten.

Gruppen und Termine
-------------------

.. req:: Termin einer Gruppe zuordnen
   :id: REQ_GRP_TER_01
   :status: approved
   :priority: high
   :links: REQ_GRP_ANL_01, REQ_TER_ANL_03

   Ein Termin kann einer bestehenden Gruppe zugeordnet werden, beim Anlegen
   wie später. Eine unbekannte Gruppe wird abgewiesen. Die Monatsansicht und
   die Termindetails nennen die Gruppe; eine Änderung der Gruppe wird
   Trainer und Assistenten mitgeteilt.

.. req:: Gruppe hat keine überschneidenden Termine
   :id: REQ_GRP_TER_02
   :status: approved
   :priority: high
   :links: REQ_GRP_TER_01, REQ_TER_ZEIT_04

   Eine Gruppe kann nicht zur selben Zeit an zwei geplanten Terminen
   teilnehmen. Für ihre Termine gelten dieselben Regeln wie für Trainer:
   keine Überschneidung und mindestens 15 Minuten Pause; ein ganztägiger
   Termin blockiert den Tag.

.. req:: Gruppentrainer wird vorgeschlagen
   :id: REQ_GRP_TER_03
   :status: approved
   :links: REQ_GRP_TER_01, REQ_GRP_TRA_01, REQ_TER_ZUW_02

   Beim Planen eines Termins für eine Gruppe schlägt das Formular deren
   Gruppentrainer als Trainer vor, sofern er für die Schulung qualifiziert
   und verfügbar ist. Der Administrator kann einen anderen Trainer wählen.
   Es gibt keine selbsttätige Zuweisung; es gelten die üblichen Prüfungen
   der Trainerzuweisung.
