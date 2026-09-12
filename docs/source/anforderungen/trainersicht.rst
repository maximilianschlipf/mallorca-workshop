Trainersicht
============

Die Trainersicht beantwortet einem Trainer die Frage, welche Termine für ihn
relevant sind -- die eigenen und die, die er übernehmen könnte.

.. req:: Eigene zukünftige Termine
   :id: REQ_TRA_UEBER_01
   :status: draft
   :priority: high

   Ein Trainer sieht alle zukünftigen Termine, denen er zugewiesen ist.

.. req:: Übernehmbare Termine ohne Trainer
   :id: REQ_TRA_UEBER_02
   :status: draft
   :priority: high

   Ein Trainer sieht alle zukünftigen Termine ohne Trainerzuweisung, deren
   Schulung er aufgrund seiner Qualifikationen halten könnte.

.. req:: Übernehmbare Termine mit anderem Trainer
   :id: REQ_TRA_UEBER_03
   :status: draft

   Ein Trainer sieht alle zukünftigen Termine, die einem anderen Trainer
   zugewiesen sind und deren Schulung er selbst halten könnte. Die Anzeige
   ist rein informativ und löst keine Aktion aus.

.. req:: Unterscheidbarkeit der Kategorien
   :id: REQ_TRA_UEBER_04
   :status: draft
   :links: REQ_TRA_UEBER_01, REQ_TRA_UEBER_02, REQ_TRA_UEBER_03

   Die drei Kategorien -- eigene Zuweisung, ohne Trainer, anderer Trainer --
   sind in der Übersicht voneinander unterscheidbar.

.. req:: Vergangene Termine bleiben unverändert
   :id: REQ_TRA_HIST_01
   :status: draft

   Termine in der Vergangenheit werden dauerhaft gespeichert und nicht mehr
   verändert. Sie erscheinen nicht in der Übersicht der relevanten Termine.
