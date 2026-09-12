Qualifikationen
===============

Die Qualifikation ist die bestätigte Berechtigung eines Trainers, eine
bestimmte Schulung durchzuführen. Sie ist Voraussetzung für jede
Trainerzuweisung.

.. req:: Bewerbung auf eine Qualifikation
   :id: REQ_QUA_BEW_01
   :status: draft
   :priority: high

   Ein Trainer kann sich für eine Schulung aus dem Katalog auf die
   Qualifikation bewerben. Damit entsteht eine Freigabeanfrage an die
   Administratoren.

   Die Bewerbung gehört zum Registrierungsablauf eines neuen Trainers: Ohne
   sie bleibt ein frisch registriertes Konto dauerhaft ohne Qualifikation
   und damit ohne Nutzen.

.. req:: Entscheidung über eine Freigabeanfrage
   :id: REQ_QUA_BEW_02
   :status: draft
   :priority: high
   :links: REQ_QUA_BEW_01

   Ein Administrator genehmigt oder lehnt eine Freigabeanfrage ab. Bei
   Genehmigung entsteht eine Qualifikation des Trainers für diese Schulung,
   bei Ablehnung nicht.

.. req:: Benachrichtigung über die Entscheidung
   :id: REQ_QUA_BEW_03
   :status: draft
   :links: REQ_QUA_BEW_02

   Der Trainer wird über die Entscheidung zu seiner Freigabeanfrage
   benachrichtigt.

.. req:: Qualifikation schaltet Termine frei
   :id: REQ_QUA_BEW_04
   :status: draft
   :priority: high
   :links: REQ_QUA_BEW_02, REQ_TRA_UEBER_02

   Sobald ein Trainer für eine Schulung qualifiziert ist, erscheinen deren
   zukünftige Termine in seiner Übersicht der relevanten Termine. Vorher
   sind sie dort nicht sichtbar.

.. req:: Qualifikationsangebot aus proaktiver Zuweisung
   :id: REQ_QUA_ANG_01
   :status: draft

   Weist ein Administrator einen nicht qualifizierten Trainer einem Termin
   zu, erhält der Trainer ein Qualifikationsangebot für die zugehörige
   Schulung. Die Trainerzuweisung gilt ab der Zuweisung; das Angebot wird
   nicht abgewartet.

.. req:: Annahme des Qualifikationsangebots
   :id: REQ_QUA_ANG_02
   :status: draft
   :links: REQ_QUA_ANG_01

   Nimmt der Trainer das Angebot an, entsteht seine Qualifikation für die
   Schulung und die bestehende Trainerzuweisung bleibt unverändert.

.. req:: Ablehnung des Qualifikationsangebots
   :id: REQ_QUA_ANG_03
   :status: draft
   :priority: high
   :links: REQ_QUA_ANG_01

   Lehnt der Trainer das Angebot ab, entsteht keine Qualifikation und die
   Trainerzuweisung des Termins entfällt. Der Termin wechselt in den Zustand
   "nicht zugewiesen" und wird damit wieder unter den Terminen ohne Trainer
   geführt.

.. req:: Administrator sieht die Ablehnung
   :id: REQ_QUA_ANG_04
   :status: draft
   :links: REQ_QUA_ANG_03

   Lehnt ein Trainer ein Qualifikationsangebot ab, wird das den
   Administratoren angezeigt. Aus der Anzeige geht hervor, welcher Trainer
   für welchen Termin abgelehnt hat.
