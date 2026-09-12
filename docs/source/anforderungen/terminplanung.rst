Terminplanung
=============

Ein Termin ist die konkret geplante Durchführung einer Schulung. Termine
plant ausschließlich ein Administrator.

Termine anlegen
---------------

.. req:: Termin für aktive Schulung anlegen
   :id: REQ_TER_ANL_01
   :status: draft
   :priority: high

   Ein Administrator legt zu einer aktiven Schulung einen Termin mit
   Zeitraum und Ort an.

.. req:: Termin ohne Trainerzuweisung speicherbar
   :id: REQ_TER_ANL_02
   :status: draft
   :priority: high
   :links: REQ_TER_ANL_01

   Ein Termin kann ohne zugewiesenen Trainer gespeichert werden. Er gilt dann
   als nicht zugewiesen.

Entscheidungshilfe bei der Planung
----------------------------------

.. req:: Qualifizierte Trainer anzeigen
   :id: REQ_TER_VORS_01
   :status: draft
   :priority: high
   :links: REQ_TER_ANL_01

   Beim Planen eines Termins zeigt das System, welche Trainer für die
   zugehörige Schulung qualifiziert sind.

.. req:: Verfügbarkeit im Kalender anzeigen
   :id: REQ_TER_VORS_02
   :status: draft
   :links: REQ_TER_VORS_01

   Zu den qualifizierten Trainern zeigt eine Kalenderansicht, wann sie
   verfügbar sind. Sie berücksichtigt aktive Abwesenheiten und bestehende
   Trainerzuweisungen und macht damit geeignete Zeiträume erkennbar.

Trainerzuweisung
----------------

.. req:: Keine Zuweisung bei Abwesenheit
   :id: REQ_TER_ZUW_01
   :status: draft
   :priority: high

   Ein Trainer kann einem Termin nicht zugewiesen werden, wenn im Zeitraum
   des Termins eine aktive Abwesenheit dieses Trainers liegt. Diese Regel
   lässt sich nicht übergehen.

.. req:: Proaktive Zuweisung ohne Qualifikation
   :id: REQ_TER_ZUW_02
   :status: draft
   :links: REQ_QUA_ANG_01, REQ_QUA_ANG_03

   Ein Administrator kann einen Trainer ohne passende Qualifikation einem
   Termin zuweisen. Die Zuweisung gilt sofort; der Trainer erhält ein
   Qualifikationsangebot für die zugehörige Schulung. Lehnt er es ab, fällt
   der Termin zurück in den Zustand "nicht zugewiesen".

Dashboard der Administratoren
-----------------------------

.. req:: Termine ohne Trainer prominent anzeigen
   :id: REQ_TER_DASH_01
   :status: draft
   :priority: high
   :links: REQ_TER_ANL_02

   Das Dashboard eines Administrators zeigt alle zukünftigen Termine ohne
   Trainerzuweisung an hervorgehobener Stelle, aufsteigend nach Startdatum
   sortiert.

.. req:: Warnstufen nach Vorlauf
   :id: REQ_TER_DASH_02
   :status: draft
   :priority: high
   :links: REQ_TER_DASH_01

   Termine ohne Trainerzuweisung werden nach ihrem Vorlauf in Warnstufen
   unterschieden:

   * weniger als vier Wochen bis zum Start -- höchste Warnstufe,
   * weniger als drei Monate bis zum Start -- mittlere Warnstufe,
   * darüber hinaus -- keine Warnstufe.

   Die Warnstufen sind in der Übersicht visuell unterscheidbar.
