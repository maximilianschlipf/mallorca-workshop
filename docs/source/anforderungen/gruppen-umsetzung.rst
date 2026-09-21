Gruppen: Stories und Tests
==========================

Diese Seite übersetzt die Anforderungen der Gruppen in Abläufe und in Tests,
die sie abprüfen.

.. story:: Ich lege die Gruppe Mallorca an
   :id: STORY_GRP_ANL_01
   :status: approved
   :priority: high
   :implements: REQ_GRP_ANL_01, REQ_GRP_MIT_01, REQ_GRP_TRA_01, REQ_GRP_BER_01, REQ_GRP_BER_02

   Als Administrator lege ich eine Gruppe mit Namen an, bestimme ihren
   Gruppentrainer und füge die Mitarbeitenden als Mitglieder hinzu. Alle
   angemeldeten Benutzer sehen die Gruppe.

.. story:: Ich plane einen Termin für die Gruppe
   :id: STORY_GRP_TER_01
   :status: approved
   :priority: high
   :implements: REQ_GRP_TER_01, REQ_GRP_TER_02, REQ_GRP_TER_03, REQ_GRP_LOE_01

   Ich wähle beim Termin die Gruppe, das Formular schlägt ihren Gruppentrainer
   vor, und das System schützt die Gruppe vor überschneidenden Terminen.

.. test:: Gruppe mit Trainer und Mitgliedern wird angelegt
   :id: TEST_GRP_ANL_01
   :status: approved
   :automated: yes
   :verifies: STORY_GRP_ANL_01, REQ_GRP_ANL_01, REQ_GRP_BER_02

   Eine Gruppe wird mit Namen, Gruppentrainer und Mitgliedern angelegt; der
   Name wird von Leerzeichen befreit, und ein Mitglied sieht die Gruppe in
   der Liste.

.. test:: Nur Administratoren pflegen Gruppen
   :id: TEST_GRP_BER_01
   :status: approved
   :automated: yes
   :verifies: REQ_GRP_BER_01

   Ein Trainer ohne Administratorrolle kann keine Gruppe anlegen.

.. test:: Gruppenname ist erforderlich und eindeutig
   :id: TEST_GRP_ANL_02
   :status: approved
   :automated: yes
   :verifies: REQ_GRP_ANL_01

   Ein leerer Name und ein Name, der sich nur in der Groß- und
   Kleinschreibung von einem vorhandenen unterscheidet, werden abgewiesen.

.. test:: Gruppentrainer ist kein Mitglied
   :id: TEST_GRP_TRA_01
   :status: approved
   :automated: yes
   :verifies: REQ_GRP_TRA_01

   Ein Gruppentrainer kann weder beim Anlegen noch später zugleich Mitglied
   derselben Gruppe sein.

.. test:: Mitglieder hinzufügen und entfernen
   :id: TEST_GRP_MIT_01
   :status: approved
   :automated: yes
   :verifies: REQ_GRP_MIT_01

   Ein Mitglied wird hinzugefügt, ein zweites Hinzufügen ändert nichts, und
   das Entfernen leert die Mitgliederliste.

.. test:: Termin trägt die Gruppe
   :id: TEST_GRP_TER_01
   :status: approved
   :automated: yes
   :verifies: STORY_GRP_TER_01, REQ_GRP_TER_01

   Ein Termin mit Gruppe liefert Gruppen-ID und Gruppennamen, und die Gruppe
   zählt ihren Termin.

.. test:: Gruppe hat keine überschneidenden Termine
   :id: TEST_GRP_TER_02
   :status: approved
   :automated: yes
   :verifies: REQ_GRP_TER_02

   Ein zweiter Termin derselben Gruppe wird bei Überschneidung und bei
   weniger als 15 Minuten Pause abgewiesen und bei 15 Minuten Pause
   angenommen.

.. test:: Unbekannte Gruppe wird abgewiesen
   :id: TEST_GRP_TER_03
   :status: approved
   :automated: yes
   :verifies: REQ_GRP_TER_01

   Ein Termin mit einer nicht vorhandenen Gruppe wird abgewiesen.

.. test:: Gruppe mit geplanten Terminen lässt sich nicht löschen
   :id: TEST_GRP_LOE_01
   :status: approved
   :automated: yes
   :verifies: REQ_GRP_LOE_01

   Die Gruppe kann nicht gelöscht werden, solange ein geplanter Termin sie
   trägt. Nach der Absage wird sie gelöscht, und der Termin bleibt ohne
   Gruppe erhalten.

.. test:: Gruppenübersicht zeigt Trainer und Mitglieder
   :id: TEST_GRP_UI_01
   :status: approved
   :automated: yes
   :verifies: REQ_GRP_BER_02

   Ein Trainer sieht die Gruppe mit Gruppentrainer und allen Mitgliedern,
   aber weder das Formular zum Anlegen noch Aktionen zum Löschen.

.. test:: Administratoren pflegen Mitglieder in der Oberfläche
   :id: TEST_GRP_UI_02
   :status: approved
   :automated: yes
   :verifies: REQ_GRP_MIT_01

   Ein Administrator fügt ein Mitglied über die Auswahl hinzu, die nur noch
   nicht enthaltene Konten anbietet, und entfernt ein anderes.

.. test:: Formular schlägt den Gruppentrainer vor
   :id: TEST_GRP_UI_03
   :status: approved
   :automated: yes
   :verifies: REQ_GRP_TER_03

   Wählt der Administrator im Terminformular die Gruppe, ist deren
   Gruppentrainer als Trainer vorausgewählt, und Gruppe und Trainer werden
   mit dem Termin gesendet.

.. test:: Kalender und Details nennen die Gruppe
   :id: TEST_GRP_UI_04
   :status: approved
   :automated: yes
   :verifies: REQ_GRP_TER_01

   Die Monatsansicht und die Termindetails zeigen den Gruppennamen.
