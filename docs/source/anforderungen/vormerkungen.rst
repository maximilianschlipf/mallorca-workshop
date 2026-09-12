Vormerkungen
============

Eine Vormerkung ist das unverbindliche Interesse eines Trainers an einem
zukünftigen Termin. Sie ist keine Trainerzuweisung -- erst die Bestätigung
durch einen Administrator macht daraus eine.

.. req:: Vormerkung abgeben
   :id: REQ_VOR_ABG_01
   :status: draft
   :priority: high
   :links: REQ_TRA_UEBER_02

   Ein Trainer kann sich für einen zukünftigen Termin ohne Trainerzuweisung
   vormerken, dessen Schulung er aufgrund seiner Qualifikationen halten
   könnte.

.. req:: Vormerkung ist unverbindlich
   :id: REQ_VOR_ABG_02
   :status: draft
   :links: REQ_VOR_ABG_01

   Eine Vormerkung verändert den Zustand des Termins nicht. Der Termin gilt
   weiterhin als nicht zugewiesen und erscheint weiter in der Übersicht der
   Termine ohne Trainer.

.. req:: Vormerkung zurücknehmen
   :id: REQ_VOR_ABG_03
   :status: draft
   :links: REQ_VOR_ABG_01

   Ein Trainer kann eine eigene Vormerkung zurücknehmen, solange sie nicht
   bestätigt ist.

.. req:: Vormerkungen für Administratoren sichtbar
   :id: REQ_VOR_SICHT_01
   :status: draft
   :priority: high
   :links: REQ_VOR_ABG_01

   Ein Administrator sieht zu einem Termin, welche Trainer sich dafür
   vorgemerkt haben. Die Anzeige macht erkennbar, dass sich ein Trainer als
   Trainer für diesen Termin anbietet.

.. req:: Vormerkung bestätigen
   :id: REQ_VOR_BEST_01
   :status: draft
   :priority: high
   :links: REQ_VOR_SICHT_01

   Ein Administrator kann eine Vormerkung bestätigen. Damit wird der
   vorgemerkte Trainer dem Termin zugewiesen und der Termin gilt als
   zugewiesen.

.. req:: Benachrichtigung über die Bestätigung
   :id: REQ_VOR_BEST_02
   :status: draft
   :links: REQ_VOR_BEST_01

   Der Trainer wird darüber benachrichtigt, dass seine Vormerkung bestätigt
   und er dem Termin zugewiesen wurde.

.. req:: Vormerkung ablehnen
   :id: REQ_VOR_ABL_01
   :status: draft
   :links: REQ_VOR_SICHT_01

   Ein Administrator kann eine Vormerkung ablehnen, ohne einen Trainer
   zuzuweisen.

.. req:: Zuweisung eines anderen Trainers lehnt Vormerkungen ab
   :id: REQ_VOR_ABL_02
   :status: draft
   :priority: high
   :links: REQ_VOR_ABG_01

   Wird einem Termin ein anderer als ein vorgemerkter Trainer zugewiesen,
   gelten alle offenen Vormerkungen zu diesem Termin als abgelehnt.

.. req:: Benachrichtigung über die Ablehnung
   :id: REQ_VOR_ABL_03
   :status: draft
   :links: REQ_VOR_ABL_01, REQ_VOR_ABL_02

   Der Trainer wird über die Ablehnung seiner Vormerkung benachrichtigt --
   unabhängig davon, ob sie direkt abgelehnt wurde oder durch die Zuweisung
   eines anderen Trainers entfallen ist.

.. req:: Keine Bestätigung bei Abwesenheit
   :id: REQ_VOR_BEST_03
   :status: draft
   :links: REQ_VOR_BEST_01, REQ_TER_ZUW_01

   Eine Vormerkung kann nicht bestätigt werden, wenn im Zeitraum des Termins
   inzwischen eine aktive Abwesenheit des vorgemerkten Trainers liegt. Damit
   gilt für den Weg über die Vormerkung dieselbe Regel wie für die direkte
   Trainerzuweisung.
