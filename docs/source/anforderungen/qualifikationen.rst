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
   :status: approved
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
   :status: approved
   :links: REQ_QUA_BEW_01

   Für eine Schulung, für die ein Trainer bereits qualifiziert ist, wird
   keine Bewerbung angeboten. Stattdessen ist für ihn erkennbar, dass er
   diese Schulung halten darf.

.. req:: Nur eine offene Bewerbung je Schulung
   :id: REQ_QUA_BEW_06
   :status: approved
   :priority: high
   :links: REQ_QUA_BEW_01

   Solange eine Freigabeanfrage eines Trainers zu einer Schulung offen ist,
   kann er zu derselben Schulung keine weitere stellen.

.. req:: Bewerbung zurückziehen
   :id: REQ_QUA_BEW_07
   :status: approved
   :links: REQ_QUA_BEW_06

   Ein Trainer kann eine eigene offene Bewerbung zurückziehen. Danach kann
   er sich erneut bewerben.

Entscheidung
------------

.. req:: Freigabeanfrage genehmigen
   :id: REQ_QUA_BEW_02
   :status: approved
   :priority: high
   :links: REQ_QUA_BEW_01, DEC_USR_SELBST_01, REQ_DSH_VORG_01

   Ein Administrator genehmigt eine Freigabeanfrage. Dadurch entsteht eine
   Qualifikation des Trainers für diese Schulung.

.. req:: Freigabeanfrage ablehnen
   :id: REQ_QUA_BEW_10
   :status: approved
   :priority: high
   :links: REQ_QUA_BEW_01, DEC_USR_SELBST_01, REQ_DSH_VORG_01

   Ein Administrator lehnt eine Freigabeanfrage ab. Dadurch entsteht keine
   Qualifikation des Trainers für diese Schulung.

.. req:: Ablehnung mit Begründung
   :id: REQ_QUA_BEW_08
   :status: approved
   :priority: high
   :links: REQ_QUA_BEW_10

   Lehnt ein Administrator eine Freigabeanfrage ab, gibt er dazu eine
   Begründung an.

.. req:: Benachrichtigung über die Entscheidung
   :id: REQ_QUA_BEW_03
   :status: approved
   :links: REQ_QUA_BEW_02, REQ_QUA_BEW_10, REQ_QUA_BEW_08, REQ_NAC_ANL_01

   Der Trainer erhält eine Mitteilung über die Entscheidung zu seiner
   Freigabeanfrage. Bei einer Ablehnung enthält sie die Begründung.

.. req:: Sperrfrist nach einer Ablehnung
   :id: REQ_QUA_BEW_09
   :status: approved
   :priority: high
   :links: REQ_QUA_BEW_08

   Nach einer abgelehnten Freigabeanfrage kann sich ein Trainer für
   dieselbe Schulung einen Tag lang nicht erneut bewerben. Damit lässt sich
   eine Ablehnung nicht durch sofortiges Wiederholen aushebeln.

.. req:: Qualifikation schaltet Termine frei
   :id: REQ_QUA_BEW_04
   :status: approved
   :priority: high
   :links: REQ_QUA_BEW_02, REQ_TRA_UEBER_02

   Sobald ein Trainer für eine Schulung qualifiziert ist, erscheinen deren
   zukünftige geplante Termine in seiner Übersicht der relevanten Termine.
   Vorher sind sie dort nicht sichtbar.

Entzug
------

.. req:: Qualifikation entziehen
   :id: REQ_QUA_ENTZ_01
   :status: approved
   :priority: high
   :links: REQ_QUA_UMF_01

   Ein Administrator kann einem Trainer eine Qualifikation wieder entziehen.

.. req:: Entzug löst zukünftige Zuweisungen
   :id: REQ_QUA_ENTZ_02
   :status: approved
   :priority: high
   :links: REQ_QUA_ENTZ_01

   Wird eine Qualifikation entzogen, wird der Trainer aus allen zukünftigen
   Terminen der betroffenen Schulung herausgenommen; diese wechseln in den
   Zustand "nicht zugewiesen". Bei abgeschlossenen Terminen bleibt er
   eingetragen.

.. req:: Benachrichtigung über den Entzug
   :id: REQ_QUA_ENTZ_03
   :status: approved
   :links: REQ_QUA_ENTZ_01, REQ_NAC_ANL_01

   Der Trainer erhält eine Mitteilung über den Entzug einer
   Qualifikation.

.. req:: Bewerbung nach einem Entzug
   :id: REQ_QUA_ENTZ_04
   :status: approved
   :links: REQ_QUA_ENTZ_01, REQ_QUA_BEW_01

   Nach einem Entzug kann sich der Trainer für dieselbe Schulung erneut
   bewerben. Die Sperrfrist aus einer Ablehnung gilt hier nicht.

Selbst-Ablegen
---------------

.. req:: Qualifikation selbst ablegen
   :id: REQ_QUA_ABLEGEN_01
   :status: approved
   :priority: high
   :links: REQ_QUA_UMF_01

   Ein Trainer kann eine eigene bestätigte Qualifikation selbst ablegen.
   Der Vorgang verlangt eine ausdrückliche Bestätigung, damit sie nicht
   durch einen Fehlklick verloren geht.

.. req:: Ablegen löst zukünftige Zuweisungen mit Warnung
   :id: REQ_QUA_ABLEGEN_02
   :status: approved
   :priority: high
   :links: REQ_QUA_ABLEGEN_01

   Bestehen für den Trainer künftige Termine dieser Schulung als
   ausführender Trainer, weist die Bestätigung ausdrücklich darauf hin.
   Nach dem Ablegen wechseln diese Termine in den Zustand "nicht
   zugewiesen"; bei abgeschlossenen Terminen bleibt er eingetragen.

.. req:: Bewerbung nach dem Ablegen ohne Sperrfrist
   :id: REQ_QUA_ABLEGEN_03
   :status: approved
   :links: REQ_QUA_ABLEGEN_01, REQ_QUA_BEW_01

   Nach dem Ablegen kann sich der Trainer für dieselbe Schulung sofort
   erneut bewerben. Die Sperrfrist aus einer Ablehnung gilt hier nicht.

.. req:: Benachrichtigung des Adminbereichs über das Ablegen
   :id: REQ_QUA_ABLEGEN_04
   :status: approved
   :links: REQ_QUA_ABLEGEN_01, REQ_NAC_ANL_01, REQ_NAC_ADM_01

   Der Adminbereich erhält eine Mitteilung, wenn ein Trainer eine
   Qualifikation ablegt. Sie nennt den Trainer und die Schulung.

Direktvergabe
--------------

.. decision:: Qualifikationsverwaltung liegt an der Schulung
   :id: DEC_QUA_VERWALTUNG_01
   :status: approved

   Offene Freigabeanfragen entscheiden, bestätigte Qualifikationen
   entziehen und Trainer direkt qualifizieren laufen gebündelt in einer
   Tabelle an der jeweiligen Schulung zusammen, nicht am Benutzerkonto des
   Trainers. Das Selbst-Ablegen einer eigenen Qualifikation bleibt davon
   unberührt und liegt am Profil des Trainers.

.. req:: Administrator qualifiziert einen Trainer direkt
   :id: REQ_QUA_DIREKT_01
   :status: approved
   :priority: high
   :links: REQ_QUA_UMF_01, DEC_QUA_VERWALTUNG_01

   Ein Administrator kann einen Trainer für eine Schulung qualifizieren,
   ohne dass dazu eine Bewerbung des Trainers vorliegt.

.. req:: Keine Direktvergabe bei bestehender Qualifikation
   :id: REQ_QUA_DIREKT_04
   :status: approved
   :links: REQ_QUA_DIREKT_01, REQ_QUA_BEW_05

   Für eine Schulung, für die ein Trainer bereits qualifiziert ist, bietet
   die Tabelle keine Direktvergabe an.

.. req:: Direktvergabe schließt eine offene Bewerbung
   :id: REQ_QUA_DIREKT_02
   :status: approved
   :links: REQ_QUA_DIREKT_01, REQ_QUA_BEW_06

   Hat der Trainer für dieselbe Schulung bereits eine offene
   Freigabeanfrage, gilt sie durch die Direktvergabe als genehmigt, statt
   parallel zur neu entstandenen Qualifikation offen zu bleiben.

.. req:: Benachrichtigung über die Direktvergabe
   :id: REQ_QUA_DIREKT_03
   :status: approved
   :links: REQ_QUA_DIREKT_01, REQ_QUA_BEW_03, REQ_NAC_ANL_01

   Der Trainer erhält eine Mitteilung über die Direktvergabe, wie bei der
   Genehmigung einer Bewerbung.
