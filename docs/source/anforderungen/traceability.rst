Traceability
============

Diese Seite zeigt, wie die Anforderungen zusammenhängen. Sie enthält keine
eigenen Anforderungen -- alles hier ist aus den Needs der Bereiche abgeleitet
und aktualisiert sich beim Bauen von selbst.

.. note::

   Einen Graphen über alle Needs auf einmal gibt es hier bewusst nicht. Er
   überschreitet PlantUMLs Größenlimit von 4096 Pixeln und würde ohne
   Fehlermeldung abgeschnitten -- ein Bild, das vollständig aussieht, aber
   Knoten verschweigt, ist schlechter als kein Bild. Die Zusammenhänge
   stehen deshalb in mehreren Graphen: die Bereichsgrenzen zuerst, danach
   jeder Bereich für sich. Die vollständige Liste aller Needs steht in der
   :ref:`Gesamtübersicht <anforderungen>`.

Bereichsübergreifende Verbindungen
----------------------------------

Die interessanten Stellen sind die, an denen ein Bereich auf einen anderen
angewiesen ist: Dort brechen Änderungen am ehesten etwas, das woanders steht.
Gezeigt werden nur Needs, die mindestens eine Verbindung über eine
Bereichsgrenze hinweg haben.

.. needflow::
   :filter: any(l.split("_")[1] != id.split("_")[1] for l in links + links_back)
   :direction: LR
   :show_link_names:

Entscheidungen und ihre Folgen
------------------------------

Welche Anforderungen auf welcher Entscheidung aufsetzen.

.. needflow::
   :filter: type == "decision" or any(l.startswith("DEC_") for l in links)
   :link_types: links, supersedes
   :show_link_names:
   :direction: LR

Anforderungen ohne Verbindung
-----------------------------

Needs, die weder auf etwas verweisen noch von etwas referenziert werden. Das
ist kein Fehler -- eine Anforderung kann für sich allein stehen --, aber die
Liste ist die schnellste Kontrolle darauf, ob eine Verbindung schlicht
vergessen wurde.

.. needtable::
   :filter: len(links) == 0 and len(links_back) == 0
   :columns: id, title, status
   :style: table

Bereiche im Einzelnen
---------------------

Speicherschicht
~~~~~~~~~~~~~~~

.. needflow::
   :filter: id.split("_")[1] == "DAT"
   :direction: LR

Benutzerkonten
~~~~~~~~~~~~~~

.. needflow::
   :filter: id.split("_")[1] == "USR"
   :direction: LR

Qualifikationen
~~~~~~~~~~~~~~~

.. needflow::
   :filter: id.split("_")[1] == "QUA"
   :direction: LR

Trainersicht
~~~~~~~~~~~~

.. needflow::
   :filter: id.split("_")[1] == "TRA"
   :direction: LR

Dashboard
~~~~~~~~~

.. needflow::
   :filter: id.split("_")[1] == "DSH"
   :direction: LR

Benachrichtigungen
~~~~~~~~~~~~~~~~~~

.. needflow::
   :filter: id.split("_")[1] == "NAC"
   :direction: LR

Abwesenheiten
~~~~~~~~~~~~~

.. needflow::
   :filter: id.split("_")[1] == "ABW"
   :direction: LR

Schulungskatalog
~~~~~~~~~~~~~~~~

.. needflow::
   :filter: id.split("_")[1] == "KAT"
   :direction: LR

Terminplanung
~~~~~~~~~~~~~

.. needflow::
   :filter: id.split("_")[1] == "TER"
   :direction: LR

Assistenz
~~~~~~~~~

.. needflow::
   :filter: id.split("_")[1] == "ASS"
   :direction: LR

Vormerkungen
~~~~~~~~~~~~

.. needflow::
   :filter: id.split("_")[1] == "VOR"
   :direction: LR

Übernahmeanfragen
~~~~~~~~~~~~~~~~~

.. needflow::
   :filter: id.split("_")[1] == "UEB"
   :direction: LR

Teilnehmerbuchungen
~~~~~~~~~~~~~~~~~~~

.. needflow::
   :filter: id.split("_")[1] == "TLN"
   :direction: LR
