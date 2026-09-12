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

Was der Trainerwechsel nach sich zieht:

.. needflow::
   :root_id: REQ_UEB_ENTS_01
   :root_depth: 1
   :direction: LR

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

.. req:: Keine Übernahme bei Abwesenheit oder Doppelbuchung
   :id: REQ_UEB_ENTS_04
   :status: draft
   :priority: high
   :links: REQ_UEB_ENTS_01, REQ_TER_ZUW_01, REQ_TER_ZUW_03

   Eine Übernahmeanfrage kann nicht angenommen werden, wenn im Zeitraum des
   Termins eine aktive Abwesenheit des anfragenden Trainers liegt oder er
   dort bereits einem anderen Termin zugewiesen ist -- als Trainer oder als
   Assistent.

   Für den Weg über die Übernahme gelten damit dieselben harten Sperren wie
   für die direkte Trainerzuweisung.

.. req:: Übernahme gilt nur für den Trainerplatz
   :id: REQ_UEB_ANFR_03
   :status: draft
   :priority: high
   :links: REQ_UEB_ANFR_01

   Eine Übernahmeanfrage bezieht sich ausschließlich auf den ausführenden
   Trainer eines Termins. Assistenzplätze können nicht übernommen werden;
   für sie gibt es nur den Weg über die Bewerbung.

.. req:: Anfrage zurückziehen
   :id: REQ_UEB_ANFR_04
   :status: draft
   :links: REQ_UEB_ANFR_01

   Ein Trainer kann eine eigene Übernahmeanfrage zurückziehen, solange sie
   nicht entschieden ist.

.. req:: Keine einander ausschließenden Anfragen
   :id: REQ_UEB_ANFR_05
   :status: draft
   :priority: high
   :links: REQ_UEB_ANFR_01, REQ_TER_ZUW_03

   Ein Trainer kann keine Übernahmeanfrage zu einem Termin stellen, dessen
   Zeitraum sich mit einem anderen Termin überschneidet, den er bereits hält
   oder für den er bereits eine offene Übernahmeanfrage hat. Er kann nur
   anbieten, was er auch halten könnte.

.. req:: Ein Tausch lehnt die übrigen Anfragen ab
   :id: REQ_UEB_ENTS_06
   :status: draft
   :priority: high
   :links: REQ_UEB_ENTS_01

   Wird eine Übernahmeanfrage angenommen, gelten alle übrigen offenen
   Anfragen zu diesem Termin als abgelehnt. Die betroffenen Trainer werden
   benachrichtigt.

.. req:: Administratoren greifen nicht ein
   :id: REQ_UEB_ENTS_07
   :status: draft
   :priority: high
   :links: REQ_UEB_ENTS_05

   Ein Administrator kann einen vollzogenen Trainerwechsel nicht ablehnen
   oder zurücknehmen. Er wird informiert, entscheidet aber nicht mit.

   Der Tausch ist eine Absprache zwischen zwei Trainern, die beide für die
   Schulung qualifiziert sind; an der Besetzbarkeit des Termins ändert er
   nichts. Missfällt einem Administrator das Ergebnis, kann er den Trainer
   nach :need:`REQ_TER_ZUW_05` austauschen -- das ist der reguläre Weg und
   braucht keinen eigenen Widerspruch.
