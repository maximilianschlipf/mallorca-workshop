Übernahmeanfragen zwischen Trainern
===================================

Sieht ein Trainer einen Termin, der einem anderen Trainer zugewiesen ist und
den er selbst halten könnte, kann er dem zugewiesenen Trainer die Übernahme
anbieten. Die Abstimmung darüber findet im Regelfall außerhalb des Systems
statt; das System hält nur das Ergebnis fest.

.. req:: Übernahme anbieten
   :id: REQ_UEB_ANFR_01
   :status: draft
   :links: REQ_TRA_UEBER_03

   Ein Trainer kann zu einem zukünftigen Termin, der einem anderen Trainer
   zugewiesen ist und dessen Schulung er selbst halten könnte, eine
   Übernahmeanfrage an den zugewiesenen Trainer stellen.

.. req:: Anfrage im Dashboard des zugewiesenen Trainers
   :id: REQ_UEB_ANFR_02
   :status: draft
   :links: REQ_UEB_ANFR_01

   Der zugewiesene Trainer sieht offene Übernahmeanfragen in seinem
   Dashboard und entscheidet dort über sie. Die Darstellung entspricht der
   Entscheidung über einen Abwesenheitsantrag: offene Anfrage, Annahme,
   Ablehnung.

.. req:: Annahme wechselt den Trainer
   :id: REQ_UEB_ENTS_01
   :status: draft
   :priority: high
   :links: REQ_UEB_ANFR_02

   Nimmt der zugewiesene Trainer die Anfrage an, wird der anfragende Trainer
   dem Termin zugewiesen und die bisherige Trainerzuweisung entfällt. Der
   Termin bleibt durchgehend zugewiesen.

.. req:: Ablehnung lässt die Zuweisung bestehen
   :id: REQ_UEB_ENTS_02
   :status: draft
   :links: REQ_UEB_ANFR_02

   Lehnt der zugewiesene Trainer die Anfrage ab, bleibt die bestehende
   Trainerzuweisung unverändert.

.. req:: Benachrichtigung des anfragenden Trainers
   :id: REQ_UEB_ENTS_03
   :status: draft
   :links: REQ_UEB_ENTS_01, REQ_UEB_ENTS_02

   Der anfragende Trainer wird über die Entscheidung zu seiner
   Übernahmeanfrage benachrichtigt.

.. req:: Administratoren über den Trainerwechsel informieren
   :id: REQ_UEB_ENTS_05
   :status: draft
   :priority: high
   :links: REQ_UEB_ENTS_01

   Wechselt durch eine angenommene Übernahmeanfrage der zugewiesene Trainer
   eines Termins, werden die Administratoren darüber informiert. Aus der
   Information geht hervor, welcher Termin betroffen ist und welcher Trainer
   den bisherigen ersetzt.

.. req:: Keine Übernahme bei Abwesenheit
   :id: REQ_UEB_ENTS_04
   :status: draft
   :links: REQ_UEB_ENTS_01, REQ_TER_ZUW_01

   Eine Übernahmeanfrage kann nicht angenommen werden, wenn im Zeitraum des
   Termins eine aktive Abwesenheit des anfragenden Trainers liegt.
