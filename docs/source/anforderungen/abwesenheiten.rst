Abwesenheiten
=============

Eine Abwesenheit ist ein Zeitraum, in dem ein Trainer nicht für Termine zur
Verfügung steht. Beim Eintragen wird geprüft, ob der Zeitraum mit Terminen
kollidiert -- und das Ergebnis dieser Prüfung entscheidet über den weiteren
Ablauf.

Erfassung und Prüfung
---------------------

.. req:: Abwesenheit eintragen
   :id: REQ_ABW_ERF_01
   :status: draft
   :priority: high

   Ein Trainer trägt für sich selbst eine Abwesenheit mit Zeitraum und Grund
   ein. Er kann Abwesenheiten nur für sich selbst erfassen.

.. req:: Konfliktprüfung beim Eintragen
   :id: REQ_ABW_PRUEF_01
   :status: draft
   :priority: high
   :links: REQ_ABW_ERF_01

   Beim Eintragen einer Abwesenheit prüft das System alle zukünftigen Termine
   im angegebenen Zeitraum auf zwei Arten von Konflikt:

   * **Zuweisungskonflikt** -- der Trainer ist einem Termin im Zeitraum
     zugewiesen.
   * **Verfügbarkeitskonflikt** -- im Zeitraum liegt ein Termin, dessen
     Schulung der Trainer halten könnte, dem er aber nicht zugewiesen ist.

Ablauf ohne Konflikt
--------------------

.. req:: Konfliktfreie Abwesenheit gilt sofort
   :id: REQ_ABW_KONFL_01
   :status: draft
   :links: REQ_ABW_PRUEF_01

   Liegt kein Zuweisungskonflikt vor, wird die Abwesenheit ohne Genehmigung
   eingetragen und ist unmittelbar aktiv.

Ablauf bei Zuweisungskonflikt
-----------------------------

.. req:: Zuweisungskonflikt ist genehmigungspflichtig
   :id: REQ_ABW_KONFL_02
   :status: draft
   :priority: high
   :links: REQ_ABW_PRUEF_01

   Liegt ein Zuweisungskonflikt vor, wird die Abwesenheit als Antrag erfasst
   und muss von einem Administrator entschieden werden.

.. req:: Antrag gilt bis zur Entscheidung nicht
   :id: REQ_ABW_KONFL_03
   :status: draft
   :links: REQ_ABW_KONFL_02

   Solange ein Abwesenheitsantrag nicht entschieden ist, gilt die Abwesenheit
   nicht: Der Trainer bleibt dem Termin zugewiesen und gilt als verfügbar.

.. req:: Genehmigung hebt die Trainerzuweisung auf
   :id: REQ_ABW_KONFL_04
   :status: draft
   :priority: high
   :links: REQ_ABW_KONFL_02

   Genehmigt ein Administrator den Antrag, wird die Abwesenheit aktiv und die
   Trainerzuweisung des kollidierenden Termins entfällt. Der Termin wechselt
   in den Zustand "nicht zugewiesen".

.. req:: Ablehnung lässt die Zuweisung bestehen
   :id: REQ_ABW_KONFL_05
   :status: draft
   :links: REQ_ABW_KONFL_02

   Lehnt ein Administrator den Antrag ab, wird die Abwesenheit nicht aktiv
   und die Trainerzuweisung bleibt unverändert bestehen. Der Trainer wird
   über die Ablehnung benachrichtigt.

Ablauf bei Verfügbarkeitskonflikt
---------------------------------

.. req:: Hinweis an den Trainer
   :id: REQ_ABW_HINW_01
   :status: draft
   :links: REQ_ABW_PRUEF_01

   Liegt ausschließlich ein Verfügbarkeitskonflikt vor, wird die Abwesenheit
   ohne Genehmigung eingetragen. Dem Trainer wird beim Eintragen angezeigt,
   welche Termine er durch die Abwesenheit nicht mehr übernehmen kann.

.. req:: Information an die Administratoren
   :id: REQ_ABW_HINW_02
   :status: draft
   :links: REQ_ABW_HINW_01

   Bei einem Verfügbarkeitskonflikt werden die Administratoren darüber
   informiert, dass ein für diese Termine in Frage kommender Trainer im
   Zeitraum nicht zur Verfügung steht.

Übersicht für Administratoren
-----------------------------

.. req:: Abwesenheiten in der Administratorenübersicht
   :id: REQ_ABW_UEBER_01
   :status: draft
   :links: REQ_ABW_KONFL_02, REQ_ABW_HINW_02

   Administratoren sehen in ihrer Übersicht die offenen Abwesenheitsanträge
   sowie die Informationen zu Verfügbarkeitskonflikten. Offene Anträge sind
   von reinen Informationen unterscheidbar, da nur Erstere eine Entscheidung
   erfordern.
