Schulungskatalog
================

Der Schulungskatalog enthält die wiederverwendbaren Weiterbildungsangebote.
Schulungen werden nicht gelöscht, sondern archiviert.

.. req:: Schulung anlegen
   :id: REQ_KAT_PFLEG_01
   :status: draft
   :priority: high

   Ein Administrator legt eine neue Schulung im Katalog an. Eine neu
   angelegte Schulung ist aktiv.

.. req:: Schulung bearbeiten
   :id: REQ_KAT_PFLEG_02
   :status: draft

   Ein Administrator ändert die Daten einer bestehenden Schulung.

.. req:: Schulung archivieren
   :id: REQ_KAT_ARCH_01
   :status: draft
   :priority: high

   Ein Administrator setzt eine aktive Schulung in den Zustand archiviert.

.. req:: Archivieren trotz zukünftiger Termine
   :id: REQ_KAT_ARCH_02
   :status: draft
   :links: REQ_KAT_ARCH_01

   Eine Schulung kann auch dann archiviert werden, wenn zu ihr noch
   zukünftige Termine geplant sind. Bestehende zukünftige Termine bleiben
   unverändert bestehen und finden statt.

.. req:: Keine neuen Termine für archivierte Schulungen
   :id: REQ_KAT_ARCH_03
   :status: draft
   :priority: high
   :links: REQ_KAT_ARCH_01

   Zu einer archivierten Schulung können keine neuen Termine angelegt
   werden.

.. req:: Schulung reaktivieren
   :id: REQ_KAT_ARCH_04
   :status: draft
   :links: REQ_KAT_ARCH_01

   Ein Administrator setzt eine archivierte Schulung zurück in den Zustand
   aktiv. Danach sind wieder neue Termine möglich.

.. req:: Katalog für Trainer sichtbar
   :id: REQ_KAT_SICHT_01
   :status: draft

   Ein Trainer kann den Schulungskatalog einsehen, ihn aber nicht verändern.
