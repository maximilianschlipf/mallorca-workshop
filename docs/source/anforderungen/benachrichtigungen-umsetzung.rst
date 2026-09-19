Benachrichtigungen: Stories und Tests
=====================================

Diese Seite übersetzt die Anforderungen aus :need:`REQ_NAC_GRUND_01` und
folgende in umsetzbare Stories und in Tests, die sie abprüfen.

* Eine **Story** beschreibt einen Ablauf aus Sicht des Benutzers und
  verweist mit ``implements`` auf die Anforderungen, die sie erfüllt.
* Ein **Test** beschreibt eine nachprüfbare Beobachtung und verweist mit
  ``verifies`` auf die Story oder Anforderung, die er absichert.

Die Anforderungen dieses Bereichs stehen auf ``review``. Stories und Tests
stehen deshalb ebenfalls auf ``review`` und sind noch nicht implementiert;
sie zählen erst nach der fachlichen Freigabe in das Requirements-Gate.

Anzeige und Lesezustand
-----------------------

.. story:: Ich sehe meine Benachrichtigungen
   :id: STORY_NAC_ANZ_01
   :status: review
   :priority: high
   :implements: REQ_NAC_GRUND_01, REQ_NAC_GRUND_03, REQ_NAC_ANZ_02, REQ_NAC_ANZ_03

   Als angemeldeter Benutzer öffne ich von jeder Seite aus meine
   Benachrichtigungen und sehe meine Mitteilungen, die neueste zuerst, jede
   mit Anlass, Zeitpunkt und dem Sprung zum betroffenen Gegenstand.

.. story:: Ich erkenne Ungelesenes, ohne die Anzeige zu öffnen
   :id: STORY_NAC_ANZ_02
   :status: review
   :priority: high
   :implements: REQ_NAC_ANZ_01

   Als Benutzer sehe ich an der Anwendung selbst, dass und wie viele
   ungelesene Mitteilungen für mich vorliegen.

.. story:: Gelesenes bleibt gelesen
   :id: STORY_NAC_ANZ_03
   :status: review
   :priority: high
   :implements: REQ_NAC_ANZ_04, REQ_NAC_ANZ_05

   Als Benutzer gilt eine Mitteilung als gelesen, sobald sie mir angezeigt
   wurde; zusätzlich markiere ich alles auf einmal als gelesen. Gelesene
   Mitteilungen bleiben dauerhaft in meiner Liste und lassen sich weder
   löschen noch wieder auf ungelesen setzen.

.. story:: Ich sehe keine Handlungen in den Benachrichtigungen
   :id: STORY_NAC_ANZ_04
   :status: review
   :priority: high
   :implements: REQ_NAC_GRUND_02

   Als Benutzer finde ich in den Benachrichtigungen keine Aktion zum
   Annehmen, Ablehnen oder Zurückziehen -- diese Entscheidungen treffe ich im
   Dashboard.

.. test:: Mitteilungen erscheinen absteigend mit Anlass und Zeitpunkt
   :id: TEST_NAC_ANZ_01
   :status: review
   :automated: yes
   :level: e2e
   :verifies: STORY_NAC_ANZ_01, REQ_NAC_GRUND_01, REQ_NAC_ANZ_02, REQ_NAC_ANZ_03

   Ein Trainer mit drei Mitteilungen aus verschiedenen Anlässen öffnet seine
   Benachrichtigungen über den gemeinsamen Einstieg jeder angemeldeten
   Hauptansicht. Stets stehen alle drei Mitteilungen da, die neueste zuerst,
   jede mit Anlasstext und Zeitpunkt. Das Lesen ändert ihre Reihenfolge
   nicht. Die Sprünge einer termin- und einer schulungsbezogenen Mitteilung
   führen jeweils zum richtigen Gegenstand.

.. test:: Ungelesene sind mit Anzahl erkennbar
   :id: TEST_NAC_ANZ_02
   :status: review
   :automated: yes
   :level: e2e
   :verifies: STORY_NAC_ANZ_02, REQ_NAC_ANZ_01

   Für einen Trainer mit zwei ungelesenen Mitteilungen ist ohne Öffnen der
   Anzeige die Zahl zwei erkennbar. Nach dem Öffnen und erneutem Laden der
   Anwendung ist keine ungelesene Mitteilung mehr ausgewiesen.

.. test:: Anzeigen markiert als gelesen und ist nicht umkehrbar
   :id: TEST_NAC_ANZ_03
   :status: review
   :automated: yes
   :level: integration
   :verifies: STORY_NAC_ANZ_03, REQ_NAC_ANZ_04

   Eine dem Empfänger angezeigte Mitteilung ist danach als gelesen geführt.
   Ein Versuch, sie wieder auf ungelesen zu setzen, wird abgewiesen. Ein
   zweiter Benutzer mit eigenen Mitteilungen bleibt davon unberührt.

.. test:: Alles als gelesen markieren erfasst nur Ungelesenes
   :id: TEST_NAC_ANZ_04
   :status: review
   :automated: yes
   :level: integration
   :verifies: STORY_NAC_ANZ_03, REQ_NAC_ANZ_04

   Ein Benutzer mit zwei gelesenen und drei ungelesenen Mitteilungen
   markiert alles als gelesen. Danach ist keine seiner fünf Mitteilungen
   ungelesen, und die Zeitpunkte der bereits gelesenen sind unverändert.

.. test:: Persönliche Mitteilungen enden nur mit dem Konto
   :id: TEST_NAC_ANZ_05
   :status: review
   :automated: yes
   :level: integration
   :verifies: STORY_NAC_ANZ_03, REQ_NAC_ANZ_05

   Ein Löschversuch auf eine eigene Mitteilung wird abgewiesen, und die
   Mitteilung besteht auch nach einem weit in die Zukunft gesetzten
   Zeitpunkt unverändert fort. Nach dem Löschen des Benutzerkontos nach
   :need:`REQ_USR_ENDE_02` ist keine persönliche Mitteilung dieses
   Empfängers mehr vorhanden; Mitteilungen an den Adminbereich bleiben
   bestehen.

.. test:: Lesezustand und Bedienung sind barrierefrei erkennbar
   :id: TEST_NAC_ANZ_08
   :status: review
   :automated: yes
   :level: e2e
   :verifies: REQ_NAC_ANZ_01, REQ_NAC_ANZ_04

   Gelesene und ungelesene Mitteilungen sowie ihre Anzahl besitzen
   programmatisch ermittelbare Namen und sind nicht allein durch Farbe
   unterscheidbar. Mit der Tastatur wird eine Mitteilung geöffnet und
   "Alles als gelesen" ausgelöst; danach liegt der Fokus an der Anzeige oder
   an ihrer Statusmeldung.

.. test:: Benachrichtigungen bieten keine Entscheidung an
   :id: TEST_NAC_ANZ_06
   :status: review
   :automated: yes
   :level: e2e
   :verifies: STORY_NAC_ANZ_04, REQ_NAC_GRUND_02

   Ein Trainer, der sowohl eine offene Übernahmeanfrage an sich als auch
   Mitteilungen hat, findet in den Benachrichtigungen weder Annehmen noch
   Ablehnen noch Zurückziehen. Die offene Übernahmeanfrage erscheint dort
   nicht, sondern nur im Dashboard.

.. test:: Gelöschter Termin lässt die Mitteilung bestehen
   :id: TEST_NAC_ANZ_07
   :status: review
   :automated: yes
   :level: integration
   :verifies: REQ_NAC_ANZ_06

   Ein Trainer erhält eine Mitteilung zu einem Termin; anschließend wird der
   Termin gelöscht. Die Mitteilung ist weiterhin mit unverändertem Text
   vorhanden, weist den Gegenstand aber als nicht mehr bestehend aus, und
   der Sprung dorthin wird nicht mehr angeboten.

Anlässe
-------

.. story:: Ich werde über Entscheidungen und Änderungen informiert
   :id: STORY_NAC_ANL_01
   :status: review
   :priority: high
   :implements: REQ_NAC_ANL_01, REQ_NAC_ANL_03

   Als Trainer erhalte ich zu jedem Anlass aus dem Katalog eine Mitteilung
   mit den dort verlangten Pflichtinhalten -- bei einer Ablehnung
   einschließlich der Begründung, bei einer Terminänderung mit altem und
   neuem Wert.

.. story:: Ich werde nicht über meine eigenen Handlungen informiert
   :id: STORY_NAC_ANL_02
   :status: review
   :implements: REQ_NAC_ANL_04

   Als Administrator, der einen Termin absagt, finde ich darüber keine
   Mitteilung in meinen eigenen Benachrichtigungen -- auch dann nicht, wenn
   ich diesem Termin selbst als Trainer zugewiesen bin.

.. test:: Jeder Anlass des Katalogs erzeugt seine Mitteilung
   :id: TEST_NAC_ANL_01
   :status: review
   :automated: yes
   :level: integration
   :verifies: STORY_NAC_ANL_01, REQ_NAC_ANL_01

   Für jeden Anlass aus :need:`REQ_NAC_ANL_01` wird das auslösende Ereignis
   herbeigeführt. Danach liegt bei jedem dort genannten Empfänger genau eine
   Mitteilung mit dem passenden Anlasstyp vor, und ihr Inhalt enthält alle
   für diesen Anlass verlangten Pflichtinhalte. Für keinen Anlass entsteht
   eine Mitteilung bei einem dort nicht genannten Empfänger.

.. test:: Terminänderung nennt alten und neuen Wert je Feld
   :id: TEST_NAC_ANL_02
   :status: review
   :automated: yes
   :level: integration
   :verifies: STORY_NAC_ANL_01, REQ_NAC_ANL_03, REQ_TER_AEND_12

   Ein Administrator ändert an einem Termin mit Trainer und Assistent
   Zeitraum, Ort, Durchführungsart, Kundenfirma und Online-Zugang in einem
   Vorgang. Trainer und Assistent erhalten je Feld eine eigene Mitteilung;
   die zu den ersten vier Feldern nennen alten und neuen Wert. Die zum
   Online-Zugang enthält keine URL, sondern den geschützten Sprung zum
   Termin. Nach dem Abziehen eines Empfängers liefert weder Mitteilung noch
   Sprung ihm die URL.

.. test:: Der spezifische Anlass verhindert doppelte Mitteilungen
   :id: TEST_NAC_ANL_06
   :status: review
   :automated: yes
   :level: integration
   :verifies: REQ_NAC_ANL_03

   Eine bestätigte Vormerkung, eine angenommene Übernahmeanfrage, eine
   angenommene Ersatztrainer-Anfrage, ein Rollenwechsel und die direkte
   Vergabe einer bereits beantragten Qualifikation sowie eine manuell und
   eine durch Fristablauf genehmigte Abwesenheit erzeugen beim jeweils
   Betroffenen genau eine Mitteilung mit dem spezifischen Anlass.
   Zusätzliche Mitteilungen "Trainerzuweisung gesetzt" oder
   "Trainerzuweisung beendet" beziehungsweise "Freigabeanfrage genehmigt"
   entstehen nicht. Eine direkte Zuweisung und ein direktes Abziehen durch
   einen Administrator erzeugen dagegen je die allgemeine Mitteilung.

.. test:: Kein Anlass außerhalb des Katalogs
   :id: TEST_NAC_ANL_03
   :status: review
   :automated: yes
   :level: unit
   :verifies: REQ_NAC_ANL_02

   Jeder im Produktcode verwendete Anlasstyp ist einer aus
   :need:`REQ_NAC_ANL_01`, und das Erzeugen einer Mitteilung mit einem
   unbekannten Anlasstyp wird abgewiesen.

.. test:: Der Auslöser erhält keine Mitteilung
   :id: TEST_NAC_ANL_04
   :status: review
   :automated: yes
   :level: integration
   :verifies: STORY_NAC_ANL_02, REQ_NAC_ANL_04

   Ein Administrator, der zugleich Trainer des Termins ist, sagt diesen
   Termin ab. Der zugewiesene Assistent erhält eine Mitteilung, der
   absagende Administrator keine persönliche Mitteilung.

   Derselbe Administrator legt anschließend eine eigene Qualifikation ab.
   Die dadurch entstandene gemeinsame Mitteilung an den Adminbereich bleibt
   für ihn und einen zweiten Administrator sichtbar; es entsteht kein
   persönlicher Datensatz für den Auslöser.

   Schließlich versucht ein vorgeschlagener Ersatztrainer, eine inzwischen
   fachlich ungültige Anfrage anzunehmen. Als ausdrücklich geregelte Ausnahme
   erhält er genau eine persönliche Mitteilung mit dem Prüfgrund; der
   ursprüngliche Trainer erhält seine eigene Mitteilung.

.. test:: Teilnehmer erhalten keine Mitteilungen
   :id: TEST_NAC_ANL_05
   :status: review
   :automated: yes
   :level: integration
   :verifies: REQ_NAC_ANL_05, REQ_TER_AEND_08

   Ein Termin mit zwei Teilnehmerbuchungen wird abgesagt. Trainer und
   Assistent erhalten je eine Mitteilung; zu den Buchungen entsteht keine.

Adminbereich als Empfänger
--------------------------

.. story:: Wir sehen als Administratoren dieselben Mitteilungen
   :id: STORY_NAC_ADM_01
   :status: review
   :priority: high
   :implements: REQ_NAC_ADM_01, REQ_NAC_ADM_02, REQ_NAC_DAT_02

   Als Administrator sehe ich die Mitteilungen an den Adminbereich in meiner
   Anzeige. Hat ein Kollege eine davon gelesen, gilt sie auch für mich als
   gelesen und bleibt für uns beide sichtbar.

.. test:: Eine Mitteilung erreicht alle Administratoren
   :id: TEST_NAC_ADM_01
   :status: review
   :automated: yes
   :level: integration
   :verifies: STORY_NAC_ADM_01, REQ_NAC_ADM_01, REQ_NAC_DAT_02

   Ein Trainer legt eine Qualifikation ab. Es entsteht genau ein Datensatz
   mit dem Adminbereich als Empfänger, und er erscheint in der Anzeige
   beider vorhandenen Administratoren. Ein weiterer Administrator, der
   danach angelegt wird, sieht dieselbe Mitteilung. Der Datensatz enthält
   weder ein persönliches Empfängerkonto noch einen davon getrennten
   Lesezustand. Eine persönliche Mitteilung aus demselben Test trägt dagegen
   genau ein Empfängerkonto und ist für den anderen Administrator unsichtbar.
   Nach dem Löschen des genannten Trainerkontos bleibt die Mitteilung an den
   Adminbereich mit dessen gespeichertem Namen, aber ohne Kontoverweis,
   erhalten.

.. test:: Gelesen durch einen gilt für alle
   :id: TEST_NAC_ADM_02
   :status: review
   :automated: yes
   :level: integration
   :verifies: STORY_NAC_ADM_01, REQ_NAC_ADM_02

   Von zwei Administratoren öffnet einer die Mitteilung an den Adminbereich.
   Danach ist sie für beide als gelesen geführt und bleibt für beide in der
   Liste sichtbar. Die persönlichen Mitteilungen des zweiten Administrators
   bleiben ungelesen.

Datenhaltung
------------

.. story:: Mitteilungen tragen ihren Anlasstyp und ihren Bezug
   :id: STORY_NAC_DAT_01
   :status: review
   :priority: high
   :implements: REQ_NAC_DAT_01, REQ_NAC_DAT_03

   Als Entwickler finde ich zu jeder Mitteilung Anlasstyp und Bezug in der
   Datenbank, damit die Anzeige zum betroffenen Gegenstand springen kann.
   Ein Versand nach außen findet nicht statt.

.. test:: Anlasstyp und Bezug werden gespeichert
   :id: TEST_NAC_DAT_01
   :status: review
   :automated: yes
   :level: integration
   :verifies: STORY_NAC_DAT_01, REQ_NAC_DAT_01

   Nach der Absage eines Termins trägt die Mitteilung den Anlasstyp für eine
   Absage, die ID des betroffenen Termins, den Zeitpunkt, den Lesezustand
   und den Text. Nach der Ablehnung einer Freigabeanfrage trägt sie den
   Bezug auf die Schulung und die Begründung. Nach der Ablehnung eines
   Abwesenheitsantrags trägt sie den Bezug auf genau diesen Vorgang.

.. test:: Kein Versand nach außen
   :id: TEST_NAC_DAT_02
   :status: review
   :automated: yes
   :level: unit
   :verifies: STORY_NAC_DAT_01, REQ_NAC_DAT_03

   Das Erzeugen einer Mitteilung löst keinen ausgehenden Versand aus: Der
   Produktcode enthält keinen Zustellweg neben der Datenbank.

Zugriffsschutz
--------------

.. test:: Mitteilungen sind gegen fremde Zugriffe geschützt
   :id: TEST_NAC_SICHER_01
   :status: review
   :automated: yes
   :level: integration
   :verifies: REQ_NAC_SICHER_01

   Ohne Anmeldung sind Anzeige und Schnittstellen nicht erreichbar. Ein
   Benutzer kann eine fremde persönliche Mitteilung weder lesen noch als
   gelesen markieren; ein reiner Trainer kann dasselbe mit einer Mitteilung
   an den Adminbereich nicht. Direkte Aufrufe mit der jeweiligen ID geben
   Inhalt und Existenz nicht preis. Das Markieren einer einzelnen oder aller
   Mitteilungen ohne gültigen CSRF-Schutz wird abgewiesen und verändert
   keinen Lesezustand.
