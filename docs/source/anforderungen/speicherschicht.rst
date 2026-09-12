Speicherschicht
===============

Der bisherige Schulungsimport liest Seed-Daten einmalig ein und ist rein
lesend. Er wird zu einer vollwertigen, schreibenden Ablage ausgebaut.

.. decision:: Dateibasierte Ablage auf JSON-Basis statt Datenbank
   :id: DEC_DAT_ABLAGE_01
   :status: approved

   Die Anwendung speichert ihre Daten in JSON-Dateien im Dateisystem, je
   Schulung eine Datei. Diese Dateien sind die maßgebliche Datenquelle. Die
   Datenbank H2 wird damit abgelöst.

   Begründung: Die Nutzerzahlen sind sehr gering. Die Leistung des
   Dateisystems reicht dafür vollständig aus, und der Wegfall der Datenbank
   spart Schema, Migrationen und Betriebsaufwand. Der Preis -- keine
   Transaktionen, keine Abfragesprache, Sperren in Anwendungsverantwortung --
   ist bei dieser Größenordnung tragbar.

.. req:: Eine JSON-Datei je Schulung
   :id: REQ_DAT_ABLAGE_01
   :status: draft
   :component: backend
   :links: DEC_DAT_ABLAGE_01

   Jede Schulung wird in genau einer JSON-Datei gespeichert. Anlegen, Ändern
   und Archivieren einer Schulung schreibt in diese Datei zurück.

.. req:: Termine liegen in der Datei ihrer Schulung
   :id: REQ_DAT_ABLAGE_03
   :status: draft
   :component: backend
   :links: REQ_DAT_ABLAGE_01

   Die Schulung ist die Vorlage, der Termin die einzelne Durchführung. Die
   Termine einer Schulung werden gemeinsam mit ihr in derselben JSON-Datei
   gehalten, jeder Termin als eigenes Element mit eigenen Daten.

.. req:: Teilnehmerbuchungen liegen am Termin
   :id: REQ_DAT_ABLAGE_04
   :status: draft
   :component: backend
   :links: REQ_DAT_ABLAGE_03

   Die Teilnehmerbuchungen eines Termins werden innerhalb dieses Termins in
   der JSON-Datei der Schulung gehalten.

.. req:: Schreibender Zugriff auf den Katalog
   :id: REQ_DAT_ABLAGE_02
   :status: draft
   :component: backend
   :links: REQ_DAT_ABLAGE_01

   Die Anwendung kann Schulungsdaten nicht nur lesen, sondern auch anlegen
   und verändern. Änderungen sind nach einem Neustart der Anwendung noch
   vorhanden.

.. req:: Qualifizierte Trainer an der Schulung
   :id: REQ_DAT_QUALI_01
   :status: draft
   :component: backend
   :links: REQ_DAT_ABLAGE_01

   In der JSON-Datei einer Schulung ist vermerkt, welche Trainer für diese
   Schulung qualifiziert sind. Die Qualifikation wird damit an der Schulung
   geführt, nicht am Trainerprofil.
