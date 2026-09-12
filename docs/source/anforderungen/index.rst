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
``VOR``    Vormerkungen
``UEB``    Übernahmeanfragen zwischen Trainern
``TLN``    Teilnehmerbuchungen
=========  ============================================

.. toctree::
   :maxdepth: 2

   speicherschicht
   benutzerkonten
   qualifikationen
   trainersicht
   abwesenheiten
   katalog
   terminplanung
   vormerkungen
   uebernahme
   teilnehmerbuchungen

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
