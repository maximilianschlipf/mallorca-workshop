.. _anforderungen:

Anforderungen
=============

Die Anforderungen sind nach Bereichen gegliedert. Jeder Bereich trägt ein
dreistelliges Kürzel, das in der Need-ID wiederkehrt:

=========  ============================================
Kürzel     Bereich
=========  ============================================
``DAT``    Speicherschicht
``USR``    Benutzerkonten und Anmeldung
``QUA``    Qualifikationen
``TRA``    Trainersicht
``ABW``    Abwesenheiten
``KAT``    Schulungskatalog
``TER``    Terminplanung
``ASS``    Assistenz
``VOR``    Vormerkungen
``UEB``    Übernahmeanfragen zwischen Trainern
``TLN``    Teilnehmerbuchungen
``GRP``    Gruppen
=========  ============================================

.. toctree::
   :maxdepth: 2

   speicherschicht
   benutzerkonten
   benutzerkonten-umsetzung
   qualifikationen
   qualifikationen-umsetzung
   trainersicht
   abwesenheiten
   abwesenheiten-umsetzung
   katalog
   katalog-umsetzung
   terminplanung
   terminplanung-umsetzung
   assistenz
   vormerkungen
   uebernahme
   teilnehmerbuchungen
   gruppen
   gruppen-umsetzung
   traceability

Gesamtübersicht
---------------

.. needtable::
   :types: req, decision
   :columns: id, title, status, priority
   :style: datatables

Offene Entscheidungen
---------------------

.. needtable::
   :types: decision
   :filter: status in ["draft", "review"]
   :columns: id, title, status
