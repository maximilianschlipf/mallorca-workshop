Schulungskatalog
================

Der Schulungskatalog enthält die wiederverwendbaren Weiterbildungsangebote.
Er beschreibt sie nur -- ob eine Schulung aktiv oder archiviert ist, steht in
der Datenbank.

Pflege
------

.. req:: Schulung anlegen
   :id: REQ_KAT_PFLEG_01
   :status: draft
   :priority: high
   :links: REQ_DAT_KAT_02

   Ein Administrator legt eine neue Schulung im Katalog an. Eine neu
   angelegte Schulung ist aktiv.

.. req:: Schulung bearbeiten
   :id: REQ_KAT_PFLEG_02
   :status: draft
   :links: REQ_KAT_PFLEG_01

   Ein Administrator ändert die Beschreibung einer bestehenden Schulung.
   Alle Felder sind änderbar.

.. req:: Die ID bleibt unveränderlich
   :id: REQ_KAT_PFLEG_03
   :status: draft
   :priority: high
   :links: REQ_KAT_PFLEG_02, REQ_DAT_DB_03

   Die ID einer Schulung kann nach dem Anlegen nicht mehr geändert werden.
   Sie benennt die Katalogdatei und ist die Klammer zu allen Terminen in der
   Datenbank; eine Änderung würde beide Verbindungen zerreißen.

.. req:: Warnung bei geänderter Dauer
   :id: REQ_KAT_PFLEG_04
   :status: draft
   :priority: high
   :links: REQ_KAT_PFLEG_02

   Wird die Dauer einer Schulung geändert, zu der bereits Termine geplant
   sind, wird der Administrator gewarnt. Die Änderung wird dadurch nicht
   verhindert.

.. req:: Bestehende Termine behalten ihren Zeitraum
   :id: REQ_KAT_PFLEG_05
   :status: draft
   :priority: high
   :links: REQ_KAT_PFLEG_04

   Eine geänderte Dauer wirkt nur auf neu angelegte Termine. Bestehende
   Termine behalten Start- und Enddatum, mit denen sie geplant wurden; der
   tatsächliche Zeitraum eines Termins ergibt sich aus seinen eigenen Daten
   und nicht aus der Dauer im Katalog.

Archivieren
-----------

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

.. req:: Archivieren lehnt offene Freigabeanfragen ab
   :id: REQ_KAT_ARCH_05
   :status: draft
   :priority: high
   :links: REQ_KAT_ARCH_01, REQ_QUA_BEW_02

   Beim Archivieren einer Schulung werden alle offenen Freigabeanfragen zu
   dieser Schulung abgelehnt. Die betroffenen Trainer werden wie bei jeder
   Ablehnung benachrichtigt.

.. req:: Schulung reaktivieren
   :id: REQ_KAT_ARCH_04
   :status: draft
   :links: REQ_KAT_ARCH_01

   Ein Administrator setzt eine archivierte Schulung zurück in den Zustand
   aktiv. Danach sind wieder neue Termine möglich. Abgelehnte
   Freigabeanfragen leben dabei nicht wieder auf.

Sichtbarkeit
------------

.. req:: Katalog für Trainer sichtbar
   :id: REQ_KAT_SICHT_01
   :status: draft

   Ein Trainer kann den Schulungskatalog einsehen, ihn aber nicht verändern.

.. req:: Archivierte Schulungen sind nachrangig sichtbar
   :id: REQ_KAT_SICHT_02
   :status: draft
   :priority: high
   :links: REQ_KAT_SICHT_01

   Archivierte Schulungen bleiben auch für Trainer erreichbar, werden aber
   als archiviert gekennzeichnet und hinter den aktiven Schulungen
   einsortiert. Sie treten damit nicht in den Vordergrund, verschwinden aber
   auch nicht -- zu ihnen laufen weiterhin Termine.

.. req:: Keine Bewerbung auf archivierte Schulungen
   :id: REQ_KAT_SICHT_03
   :status: draft
   :links: REQ_KAT_SICHT_02, REQ_QUA_BEW_01

   Auf die Qualifikation für eine archivierte Schulung kann sich ein Trainer
   nicht bewerben.

.. req:: Termine archivierter Schulungen bleiben sichtbar
   :id: REQ_KAT_SICHT_04
   :status: draft
   :priority: high
   :links: REQ_KAT_SICHT_02, REQ_KAT_ARCH_02

   Ist ein Trainer einem Termin einer inzwischen archivierten Schulung
   zugewiesen, bleibt dieser Termin in seiner Übersicht sichtbar, obwohl die
   Schulung selbst nicht mehr in seinem Katalog erscheint. Sonst verschwände
   ein Termin, den er halten muss.

Löschen
-------

.. req:: Schulung ohne Termine löschen
   :id: REQ_KAT_LOE_01
   :status: draft
   :links: REQ_KAT_PFLEG_01

   Eine Schulung, zu der nie ein Termin existiert hat, kann von einem
   Administrator gelöscht werden. Damit lässt sich ein Fehlgriff beim
   Anlegen beseitigen, ohne eine Archivleiche zu hinterlassen.

.. req:: Archivierte Schulung nach sechs Monaten löschen
   :id: REQ_KAT_LOE_02
   :status: draft
   :links: REQ_KAT_ARCH_01

   Eine Schulung, die seit mindestens sechs Monaten archiviert ist, kann von
   einem Administrator gelöscht werden.

.. req:: Kein Löschen in allen anderen Fällen
   :id: REQ_KAT_LOE_03
   :status: draft
   :priority: high
   :links: REQ_KAT_LOE_01, REQ_KAT_LOE_02

   Außerhalb dieser beiden Fälle kann eine Schulung nicht gelöscht werden.
   Eine aktive Schulung mit Terminen wird archiviert, nicht gelöscht.

.. req:: Löschen erhält den Titel in der Historie
   :id: REQ_KAT_LOE_04
   :status: draft
   :priority: high
   :links: REQ_KAT_LOE_02

   Wird eine Schulung gelöscht, zu der abgeschlossene Termine bestehen,
   bleibt ihr Titel bei diesen Terminen als Text erhalten. Die Termine
   verweisen danach nicht mehr auf eine Katalogdatei.

   Ohne diese Regel entstünden Termine, die auf eine nicht mehr vorhandene
   Schulung zeigen -- genau der Fall, den :need:`REQ_DAT_DB_04` als Fehler
   meldet.

Aufnahme aus Dateien
--------------------

.. req:: JSON-Dateien in den Katalog aufnehmen
   :id: REQ_KAT_IMP_01
   :status: draft
   :priority: high

   Ein Administrator kann bereitgestellte JSON-Dateien in den Katalog
   aufnehmen. Das ist eine dauerhafte Fähigkeit der Anwendung, nicht ein
   einmaliger Schritt bei der Einrichtung.

.. req:: Prüfung vor der Aufnahme
   :id: REQ_KAT_IMP_02
   :status: draft
   :priority: high
   :links: REQ_KAT_IMP_01, REQ_DAT_KAT_02

   Vor der Aufnahme wird geprüft, ob eine Datei dem erwarteten Aufbau
   entspricht und alle nötigen Felder enthält. Eine Datei, die die Prüfung
   nicht besteht, wird nicht aufgenommen, und dem Administrator wird gesagt,
   woran es lag.

.. req:: Bereits vergebene ID bei der Aufnahme
   :id: REQ_KAT_IMP_03
   :status: draft
   :links: REQ_KAT_IMP_02, REQ_DAT_KAT_05

   Trägt eine aufzunehmende Datei eine ID, die im Katalog bereits vergeben
   ist, wird sie nicht stillschweigend übernommen. Der Administrator
   entscheidet, ob die bestehende Schulung ersetzt wird oder die Aufnahme
   unterbleibt.
