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

Was an der Vormerkung hängt:

.. needflow::
   :root_id: REQ_VOR_ABG_01
   :root_depth: 1
   :direction: LR

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
   :links: REQ_VOR_SICHT_01, REQ_DSH_VORG_01

   Ein Administrator kann eine Vormerkung bestätigen. Damit wird der
   vorgemerkte Trainer dem Termin zugewiesen und der Termin gilt als
   zugewiesen.

.. req:: Benachrichtigung über die Bestätigung
   :id: REQ_VOR_BEST_02
   :status: draft
   :links: REQ_VOR_BEST_01, REQ_NAC_ANL_01

   Der Trainer erhält eine Mitteilung darüber, dass seine Vormerkung
   bestätigt und er dem Termin zugewiesen wurde.

.. req:: Vormerkung ablehnen
   :id: REQ_VOR_ABL_01
   :status: draft
   :links: REQ_VOR_SICHT_01, REQ_DSH_VORG_01

   Ein Administrator kann eine Vormerkung ablehnen, ohne einen Trainer
   zuzuweisen.

.. req:: Eine Zuweisung lehnt die übrigen Vormerkungen ab
   :id: REQ_VOR_ABL_02
   :status: draft
   :priority: high
   :links: REQ_VOR_ABG_01, REQ_DSH_VORG_09

   Sobald einem Termin ein Trainer zugewiesen ist, gelten alle übrigen
   offenen Vormerkungen zu diesem Termin als abgelehnt. Das gilt unabhängig
   davon, wie die Zuweisung zustande kam -- durch Bestätigung einer
   Vormerkung, durch direkte Zuweisung oder durch eine Übernahme.

.. req:: Benachrichtigung über die Ablehnung
   :id: REQ_VOR_ABL_03
   :status: draft
   :links: REQ_VOR_ABL_01, REQ_VOR_ABL_02, REQ_NAC_ANL_01

   Der Trainer erhält eine Mitteilung über die Ablehnung seiner Vormerkung
   -- unabhängig davon, ob sie direkt abgelehnt wurde oder durch die
   Zuweisung eines anderen Trainers entfallen ist.

.. req:: Ablehnung mit Begründung
   :id: REQ_VOR_ABL_04
   :status: draft
   :links: REQ_VOR_ABL_01, REQ_NAC_ANL_01, REQ_DSH_VORG_05

   Lehnt ein Administrator eine Vormerkung ab, gibt er dazu eine Begründung
   an. Sie erreicht den Trainer mit der Mitteilung. Für eine Ablehnung
   nach :need:`REQ_VOR_ABL_02` entfällt die Begründung -- dort ist der Grund
   die Zuweisung selbst.

.. req:: Sperrfrist nach einer Ablehnung
   :id: REQ_VOR_ABL_05
   :status: draft
   :priority: high
   :links: REQ_VOR_ABL_01

   Nach einer abgelehnten Vormerkung kann sich ein Trainer für denselben
   Termin einen Tag lang nicht erneut vormerken.

.. req:: Keine Bestätigung bei Abwesenheit oder Doppelbuchung
   :id: REQ_VOR_BEST_03
   :status: draft
   :priority: high
   :links: REQ_VOR_BEST_01, REQ_TER_ZUW_01, REQ_TER_ZUW_03

   Eine Vormerkung kann nicht bestätigt werden, wenn im Zeitraum des Termins
   inzwischen eine aktive Abwesenheit des vorgemerkten Trainers liegt oder
   er dort bereits einem anderen Termin zugewiesen ist -- als Trainer oder
   als Assistent.

   Für den Weg über die Vormerkung gelten damit dieselben harten Sperren wie
   für die direkte Trainerzuweisung. Ohne diese Regel wäre die Vormerkung
   der Weg, sie zu umgehen.

Zusammentreffen mit der Assistenz
---------------------------------

.. req:: Vormerkung und Assistenzbewerbung nebeneinander
   :id: REQ_VOR_ASS_01
   :status: draft
   :priority: high
   :links: REQ_VOR_ABG_01, REQ_ASS_BEW_01

   Ein Trainer kann sich für denselben Termin vormerken und zugleich auf
   einen Assistenzplatz bewerben. Wird ein anderer Trainer gesetzt und er
   selbst als Assistent angenommen, ist das ein gültiges Ergebnis.

.. req:: Zuweisung als Trainer beendet die Assistenz
   :id: REQ_VOR_ASS_02
   :status: draft
   :priority: high
   :links: REQ_VOR_ASS_01

   Wird ein Trainer einem Termin als ausführender Trainer zugewiesen,
   erlöschen seine Assistenzbewerbung und eine bereits bestehende
   Assistenzzuweisung für denselben Termin. Der Assistenzplatz wird wieder
   frei.
