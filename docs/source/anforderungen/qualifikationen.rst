Qualifikationen
===============

Die Qualifikation ist die bestätigte Berechtigung eines Trainers, eine
bestimmte Schulung durchzuführen. Sie ist Voraussetzung für jede
Trainerzuweisung.

.. decision:: Administratoren entscheiden auch über eigene Bewerbungen
   :id: DEC_QUA_SELBST_01
   :status: superseded

   Ein Administrator darf eine Freigabeanfrage entscheiden, die er selbst
   gestellt hat. Ein Vieraugenprinzip gibt es nicht.

   Abgelöst durch :need:`DEC_USR_SELBST_01`, das dieselbe Überlegung auf
   alle Vorgänge ausdehnt statt nur auf Freigabeanfragen.

.. req:: Qualifikation gilt für die Schulung
   :id: REQ_QUA_UMF_01
   :status: approved
   :priority: high

   Eine Qualifikation gilt für eine Schulung als Ganzes und damit für alle
   ihre Termine, auch für künftige. Sie wird nicht je Termin erteilt.

Bewerbung
---------

.. req:: Bewerbung auf eine Qualifikation
   :id: REQ_QUA_BEW_01
   :status: draft
   :priority: high
   :links: REQ_QUA_UMF_01

   Ein Trainer kann sich für eine Schulung aus dem Katalog auf die
   Qualifikation bewerben. Damit entsteht eine Freigabeanfrage an die
   Administratoren.

   Die Bewerbung gehört zum Registrierungsablauf eines neuen Trainers: Ohne
   sie bleibt ein frisch registriertes Konto dauerhaft ohne Qualifikation
   und damit ohne Nutzen.

.. req:: Keine Bewerbung bei bestehender Qualifikation
   :id: REQ_QUA_BEW_05
   :status: draft
   :links: REQ_QUA_BEW_01

   Für eine Schulung, für die ein Trainer bereits qualifiziert ist, wird
   keine Bewerbung angeboten. Stattdessen ist für ihn erkennbar, dass er
   diese Schulung halten darf.

.. req:: Nur eine offene Bewerbung je Schulung
   :id: REQ_QUA_BEW_06
   :status: draft
   :priority: high
   :links: REQ_QUA_BEW_01

   Solange eine Freigabeanfrage eines Trainers zu einer Schulung offen ist,
   kann er zu derselben Schulung keine weitere stellen.

.. req:: Bewerbung zurückziehen
   :id: REQ_QUA_BEW_07
   :status: draft
   :links: REQ_QUA_BEW_06

   Ein Trainer kann eine eigene offene Bewerbung zurückziehen. Danach kann
   er sich erneut bewerben.

Entscheidung
------------

.. req:: Entscheidung über eine Freigabeanfrage
   :id: REQ_QUA_BEW_02
   :status: draft
   :priority: high
   :links: REQ_QUA_BEW_01, DEC_USR_SELBST_01

   Ein Administrator genehmigt oder lehnt eine Freigabeanfrage ab. Bei
   Genehmigung entsteht eine Qualifikation des Trainers für diese Schulung,
   bei Ablehnung nicht.

.. req:: Ablehnung mit Begründung
   :id: REQ_QUA_BEW_08
   :status: draft
   :priority: high
   :links: REQ_QUA_BEW_02

   Lehnt ein Administrator eine Freigabeanfrage ab, gibt er dazu eine
   Begründung an.

.. req:: Benachrichtigung über die Entscheidung
   :id: REQ_QUA_BEW_03
   :status: draft
   :links: REQ_QUA_BEW_02, REQ_QUA_BEW_08

   Der Trainer wird über die Entscheidung zu seiner Freigabeanfrage
   benachrichtigt. Bei einer Ablehnung erhält er die Begründung mit.

.. req:: Sperrfrist nach einer Ablehnung
   :id: REQ_QUA_BEW_09
   :status: draft
   :priority: high
   :links: REQ_QUA_BEW_08

   Nach einer abgelehnten Freigabeanfrage kann sich ein Trainer für
   dieselbe Schulung einen Tag lang nicht erneut bewerben. Damit lässt sich
   eine Ablehnung nicht durch sofortiges Wiederholen aushebeln.

.. req:: Qualifikation schaltet Termine frei
   :id: REQ_QUA_BEW_04
   :status: draft
   :priority: high
   :links: REQ_QUA_BEW_02, REQ_TRA_UEBER_02

   Sobald ein Trainer für eine Schulung qualifiziert ist, erscheinen deren
   zukünftige Termine in seiner Übersicht der relevanten Termine. Vorher
   sind sie dort nicht sichtbar.

Angebot aus proaktiver Zuweisung
--------------------------------

.. req:: Qualifikationsangebot aus proaktiver Zuweisung
   :id: REQ_QUA_ANG_01
   :status: draft
   :links: REQ_QUA_UMF_01

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

.. req:: Offene Angebote bleiben offen
   :id: REQ_QUA_ANG_05
   :status: draft
   :priority: high
   :links: REQ_QUA_ANG_01

   Ein Qualifikationsangebot verfällt nicht. Antwortet ein Trainer nicht,
   bleibt er dem Termin zugewiesen und das Angebot offen. Es gibt keine
   Frist, nach der die Zuweisung selbsttätig entfällt.

.. req:: Offene Angebote sind für Administratoren sichtbar
   :id: REQ_QUA_ANG_06
   :status: draft
   :priority: high
   :links: REQ_QUA_ANG_05

   Administratoren sehen, welche Qualifikationsangebote unbeantwortet sind,
   zu welchem Termin sie gehören und wie nah dieser Termin ist.

   Nachzuhalten, ob ein Trainer antwortet, liegt in der Verantwortung des
   Administrators. Diese Anzeige ist die Voraussetzung dafür -- ohne sie
   wäre ein unbeantwortetes Angebot unsichtbar, bis der Termin da ist.

Entzug
------

.. req:: Qualifikation entziehen
   :id: REQ_QUA_ENTZ_01
   :status: draft
   :priority: high
   :links: REQ_QUA_UMF_01

   Ein Administrator kann einem Trainer eine Qualifikation wieder entziehen.

.. req:: Entzug löst zukünftige Zuweisungen
   :id: REQ_QUA_ENTZ_02
   :status: draft
   :priority: high
   :links: REQ_QUA_ENTZ_01

   Wird eine Qualifikation entzogen, wird der Trainer aus allen zukünftigen
   Terminen der betroffenen Schulung herausgenommen; diese wechseln in den
   Zustand "nicht zugewiesen". Bei abgeschlossenen Terminen bleibt er
   eingetragen.

.. req:: Benachrichtigung über den Entzug
   :id: REQ_QUA_ENTZ_03
   :status: draft
   :links: REQ_QUA_ENTZ_01

   Der Trainer wird über den Entzug einer Qualifikation benachrichtigt.

.. req:: Bewerbung nach einem Entzug
   :id: REQ_QUA_ENTZ_04
   :status: draft
   :links: REQ_QUA_ENTZ_01, REQ_QUA_BEW_01

   Nach einem Entzug kann sich der Trainer für dieselbe Schulung erneut
   bewerben. Die Sperrfrist aus einer Ablehnung gilt hier nicht.
