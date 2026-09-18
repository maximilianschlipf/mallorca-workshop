Assistenz
=========

Ein Termin hat einen ausführenden Trainer und daneben bis zu drei
Assistenzplätze. Die Assistenz ist der Weg, sich eine Schulung anzueignen,
für die man noch nicht qualifiziert ist -- sie bindet den Assistenten wie
ein eigener Einsatz, macht ihn aber nicht zum Trainer.

Plätze
------

.. req:: Assistenzplätze an einem Termin
   :id: REQ_ASS_PLATZ_01
   :status: approved
   :priority: high
   :links: REQ_TER_ANL_02

   Ein Termin hat höchstens einen ausführenden Trainer und zusätzlich bis zu
   drei Assistenzplätze. Dieselbe Person kann am selben Termin nicht beide
   Rollen tragen; einen Rollenwechsel regelt :need:`REQ_TER_ZUW_08`.

.. req:: Assistenz ohne Qualifikation
   :id: REQ_ASS_PLATZ_02
   :status: draft
   :priority: high
   :links: REQ_ASS_PLATZ_01

   Für einen Assistenzplatz ist keine Qualifikation für die Schulung nötig.
   Die Assistenz ist der Weg, sich eine Schulung anzueignen, für die man
   noch nicht qualifiziert ist.

Bewerbung
---------

.. req:: Bewerbung auf einen Assistenzplatz
   :id: REQ_ASS_BEW_01
   :status: draft
   :priority: high
   :links: REQ_ASS_PLATZ_02

   Ein Trainer kann sich auf einen freien Assistenzplatz eines zukünftigen
   Termins bewerben, unabhängig davon, ob er für die Schulung qualifiziert
   ist.

.. req:: Entscheidung über eine Assistenzbewerbung
   :id: REQ_ASS_BEW_02
   :status: draft
   :priority: high
   :links: REQ_ASS_BEW_01

   Über eine Bewerbung auf einen Assistenzplatz entscheidet der dem Termin
   zugewiesene Trainer oder ein Administrator. Wer zuerst entscheidet,
   entscheidet. Bei Annahme ist der Bewerber dem Termin als Assistent
   zugewiesen; er wird über die Entscheidung benachrichtigt.

   Der ausführende Trainer soll mitreden können, weil er die Person anlernt.
   Ist dem Termin noch kein Trainer zugewiesen, bleibt nur der
   Administrator.

.. req:: Assistenz unterliegt denselben Sperren
   :id: REQ_ASS_BEW_03
   :status: draft
   :priority: high
   :links: REQ_ASS_BEW_02, REQ_TER_ZUW_03

   Eine Assistenzzuweisung ist ausgeschlossen, wenn im Zeitraum des Termins
   eine aktive Abwesenheit des Trainers liegt oder er bereits einem anderen
   Termin zugewiesen ist. Ein Assistent ist genauso gebunden wie der
   ausführende Trainer.

.. req:: Assistenz ist keine Qualifikation
   :id: REQ_ASS_PLATZ_03
   :status: draft
   :priority: high
   :links: REQ_ASS_PLATZ_02

   Aus einer Assistenz entsteht keine Qualifikation. Wer nach einer
   Assistenz selbst Trainer der Schulung werden will, bewirbt sich dafür wie
   jeder andere.

.. req:: Assistent bestätigt keine Durchführung
   :id: REQ_ASS_PLATZ_04
   :status: draft
   :links: REQ_ASS_PLATZ_01, REQ_TER_STAT_02

   Die Durchführung eines Termins kann nur der ausführende Trainer oder ein
   Administrator bestätigen, nicht ein Assistent.
