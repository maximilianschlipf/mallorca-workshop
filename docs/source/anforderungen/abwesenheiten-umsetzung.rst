Abwesenheiten: Stories und Tests
=================================

Diese Seite übersetzt die Anforderungen aus :doc:`abwesenheiten` in Abläufe
und in Tests, die sie abprüfen. Der Bereich ist noch nicht umgesetzt --
jede Story und jeder Test stehen auf ``approved``, keiner auf ``verified``.

Die Stories sind absichtlich in kleine, einzeln umsetzbare Schritte
zerschnitten und in sieben Gruppen sortiert, die aufeinander aufbauen:
Grunderfassung, Zuweisungskonflikt, Verfügbarkeitskonflikt,
Genehmigungsverfahren, Fristablauf, Grund-Regeln, Tausch mit Ersatztrainer.
Innerhalb einer Gruppe ist die Reihenfolge nicht zwingend; zwischen den
Gruppen schon -- eine spätere Gruppe setzt die Ergebnisse der früheren
voraus.

Eine Besonderheit bei :need:`STORY_ABW_PRUEF_01`: Sie bildet den
Zuweisungskonflikt zunächst *ohne* Genehmigungsverfahren ab -- wer ihn
kollidierend einträgt, verliert die Zuweisung sofort. Das ist bewusst ein
Zwischenstand. Die Gruppe "Genehmigungsverfahren" ersetzt dieses Verhalten;
der zugehörige Test :need:`TEST_ABW_PRUEF_01` wird dabei hinfällig und sollte
durch die Tests der Genehmigungsverfahren-Gruppe abgelöst werden, nicht
daneben weiterbestehen.

Grunderfassung
---------------

.. story:: Ich trage eine Abwesenheit ein
   :id: STORY_ABW_ERF_01
   :status: approved
   :priority: high
   :implements: REQ_ABW_ERF_01, REQ_ABW_ERF_02

   Als Trainer trage ich für mich selbst eine Abwesenheit mit Zeitraum ein,
   optional mit einem Grund. Ohne Grund ist die Abwesenheit ebenso gültig.
   Eine Konfliktprüfung findet in diesem Schritt noch nicht statt.

.. test:: Abwesenheit wird mit und ohne Grund angelegt
   :id: TEST_ABW_ERF_01
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_ERF_01, REQ_ABW_ERF_01, REQ_ABW_ERF_02

   Ein Trainer trägt eine Abwesenheit mit Zeitraum und Grund ein -- sie
   existiert danach mit den eingegebenen Werten. Dasselbe ohne Grund führt
   ebenfalls zu einer gültigen Abwesenheit.

.. test:: Ein Trainer erfasst nur eigene Abwesenheiten
   :id: TEST_ABW_ERF_02
   :status: approved
   :automated: yes
   :level: integration
   :verifies: REQ_ABW_ERF_01

   Der Versuch, eine Abwesenheit für ein anderes Benutzerkonto einzutragen,
   wird abgewiesen.

.. story:: Ich ändere eine eigene Abwesenheit
   :id: STORY_ABW_ERF_02
   :status: approved
   :implements: REQ_ABW_ERF_03

   Als Trainer ändere ich den Zeitraum oder den Grund einer eigenen
   Abwesenheit.

.. test:: Ändern aktualisiert den Zeitraum
   :id: TEST_ABW_ERF_03
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_ERF_02, REQ_ABW_ERF_03

   Nach dem Ändern einer Abwesenheit trägt sie den neuen Zeitraum; der alte
   Zeitraum gilt nicht mehr.

.. story:: Ich lösche eine eigene Abwesenheit
   :id: STORY_ABW_ERF_03
   :status: approved
   :implements: REQ_ABW_ERF_04

   Als Trainer lösche ich eine eigene Abwesenheit und gelte danach im
   betroffenen Zeitraum wieder als verfügbar.

.. test:: Löschen macht wieder verfügbar
   :id: TEST_ABW_ERF_04
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_ERF_03, REQ_ABW_ERF_04

   Nach dem Löschen einer Abwesenheit gilt der Trainer im ehemals
   betroffenen Zeitraum wieder als verfügbar.

Zuweisungskonflikt
-------------------

.. story:: Kollidierende Zuweisung entfällt beim Eintragen
   :id: STORY_ABW_PRUEF_01
   :status: approved
   :priority: high
   :implements: REQ_ABW_PRUEF_01, REQ_ABW_PRUEF_02, REQ_ABW_ERF_05

   Als Trainer trage ich eine Abwesenheit ein, die mit einem Termin
   kollidiert, dem ich zugewiesen bin. In diesem Schritt entfällt meine
   Zuweisung dafür unmittelbar; ich werde beim Eintragen darauf hingewiesen.
   Ein Genehmigungsverfahren gibt es hier noch nicht -- das kommt in der
   Gruppe "Genehmigungsverfahren" und ersetzt dieses Verhalten.

.. test:: Zuweisungskonflikt entfernt die Zuweisung sofort
   :id: TEST_ABW_PRUEF_01
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_PRUEF_01, REQ_ABW_PRUEF_01

   Eintragen einer Abwesenheit, die mit einem zugewiesenen Termin
   kollidiert, entfernt die Zuweisung sofort; der Termin gilt danach als
   nicht zugewiesen. Die Antwort weist darauf hin.

   Wird durch die Tests der Gruppe "Genehmigungsverfahren" abgelöst, sobald
   diese umgesetzt ist -- nicht danebenstehen lassen.

.. test:: Teilweise Überschneidung genügt für den Konflikt
   :id: TEST_ABW_PRUEF_02
   :status: approved
   :automated: yes
   :level: integration
   :verifies: REQ_ABW_PRUEF_02

   Überschneidet die Abwesenheit nur einen von drei Tagen eines Termins,
   liegt trotzdem ein Konflikt vor.

.. story:: Gelöschte Abwesenheit gibt den Termin nicht zurück
   :id: STORY_ABW_PRUEF_02
   :status: approved
   :priority: high
   :implements: REQ_ABW_ERF_05

   Als Trainer stelle ich fest, dass das Löschen einer Abwesenheit, die eine
   Zuweisung entfallen ließ, den Termin nicht wieder mir zuweist.

.. test:: Löschen stellt die Zuweisung nicht wieder her
   :id: TEST_ABW_PRUEF_03
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_PRUEF_02, REQ_ABW_ERF_05

   Nach einer wegfallenden Zuweisung bleibt der Termin nach dem Löschen der
   Abwesenheit unbesetzt, statt automatisch wieder dem Trainer zugewiesen zu
   werden.

Verfügbarkeitskonflikt
------------------------

.. story:: Ich werde auf verpasste Übernahmemöglichkeiten hingewiesen
   :id: STORY_ABW_HINW_01
   :status: approved
   :implements: REQ_ABW_PRUEF_03, REQ_ABW_HINW_01

   Als Trainer trage ich eine Abwesenheit ein, die einen Termin überschneidet,
   den ich hätte übernehmen können, dem ich aber nicht zugewiesen bin. Ich
   erfahre beim Eintragen, welche Termine das sind; die Abwesenheit gilt
   ohne Genehmigung sofort.

.. test:: Verfügbarkeitskonflikt wird angezeigt, Abwesenheit gilt sofort
   :id: TEST_ABW_HINW_01
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_HINW_01, REQ_ABW_HINW_01

   Eine Abwesenheit, die ausschließlich einen Verfügbarkeitskonflikt
   auslöst, wird ohne Genehmigung eingetragen; die Antwort nennt die
   betroffenen Termine.

.. story:: Administratoren werden über den Verfügbarkeitskonflikt informiert
   :id: STORY_ABW_HINW_02
   :status: approved
   :implements: REQ_ABW_HINW_02

   Als Administrator sehe ich, dass ein für einen Termin in Frage kommender
   Trainer im fraglichen Zeitraum nicht verfügbar ist.

.. test:: Administratoren-Übersicht zeigt Verfügbarkeitskonflikte
   :id: TEST_ABW_HINW_02
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_HINW_02, REQ_ABW_HINW_02

   Nach dem Eintragen einer Abwesenheit mit Verfügbarkeitskonflikt taucht
   der Hinweis in der Sicht der Administratoren auf.

Genehmigungsverfahren
-----------------------

.. story:: Zuweisungskonflikt löst einen Genehmigungsantrag aus
   :id: STORY_ABW_KONFL_01
   :status: approved
   :priority: high
   :implements: REQ_ABW_KONFL_02, REQ_ABW_KONFL_03

   Als Trainer trage ich eine Abwesenheit ein, die mit einem zugewiesenen
   Termin kollidiert. Statt die Zuweisung sofort zu verlieren, entsteht ein
   Antrag; ich bleibe bis zur Entscheidung zugewiesen und gelte als
   verfügbar. Diese Story ersetzt das Verhalten aus
   :need:`STORY_ABW_PRUEF_01`.

.. test:: Zuweisungskonflikt erzeugt einen offenen Antrag
   :id: TEST_ABW_KONFL_01
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_KONFL_01, REQ_ABW_KONFL_02, REQ_ABW_KONFL_03

   Eintragen einer kollidierenden Abwesenheit erzeugt einen offenen Antrag,
   statt die Zuweisung zu entfernen; der Trainer bleibt bis zur Entscheidung
   zugewiesen.

.. story:: Administrator genehmigt den Antrag
   :id: STORY_ABW_KONFL_02
   :status: approved
   :priority: high
   :implements: REQ_ABW_KONFL_04

   Als Administrator genehmige ich einen Abwesenheitsantrag. Die Abwesenheit
   wird aktiv, die Zuweisungen aller kollidierenden Termine entfallen, und
   ich kann die frei gewordenen Termine neu vergeben.

.. test:: Genehmigung entfernt alle kollidierenden Zuweisungen
   :id: TEST_ABW_KONFL_02
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_KONFL_02, REQ_ABW_KONFL_04

   Nach der Genehmigung eines Antrags mit mehreren kollidierenden Terminen
   sind alle betroffenen Zuweisungen entfernt, auch bei nur teilweiser
   Überschneidung. Die Termine lassen sich neu vergeben.

.. story:: Administrator lehnt den Antrag ab
   :id: STORY_ABW_KONFL_03
   :status: approved
   :implements: REQ_ABW_KONFL_05

   Als Administrator lehne ich einen Abwesenheitsantrag ab. Die Zuweisung
   bleibt bestehen, der Trainer wird über die Ablehnung informiert.

.. test:: Ablehnung lässt Zuweisung bestehen und informiert
   :id: TEST_ABW_KONFL_03
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_KONFL_03, REQ_ABW_KONFL_05

   Nach Ablehnung eines Antrags ist die Zuweisung unverändert, und der
   Trainer erhält eine Benachrichtigung über die Ablehnung.

.. story:: Ich ziehe meinen Antrag zurück
   :id: STORY_ABW_KONFL_04
   :status: approved
   :implements: REQ_ABW_ERF_06

   Als Trainer ziehe ich einen eigenen, noch nicht entschiedenen Antrag
   zurück.

.. test:: Zurückziehen entfernt den offenen Antrag
   :id: TEST_ABW_KONFL_04
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_KONFL_04, REQ_ABW_ERF_06

   Nach dem Zurückziehen eines Antrags ist er nicht mehr offen und hat keine
   Wirkung auf die Zuweisung.

.. story:: Mehrteiliger Konflikt wird als Ganzes entschieden
   :id: STORY_ABW_KONFL_05
   :status: approved
   :implements: REQ_ABW_KONFL_06

   Als Administrator entscheide ich über einen Antrag, der mehrere Termine
   betrifft, in einem Zug -- nicht Termin für Termin.

.. test:: Eine Entscheidung wirkt auf alle kollidierenden Termine
   :id: TEST_ABW_KONFL_05
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_KONFL_05, REQ_ABW_KONFL_06

   Eine Abwesenheit, die drei Termine betrifft, lässt sich nur als ein
   Antrag entscheiden; es gibt keine Möglichkeit, einzelne Termine davon
   auszunehmen.

.. story:: Administrator entscheidet über eigenen Antrag
   :id: STORY_ABW_KONFL_06
   :status: approved
   :implements: REQ_ABW_KONFL_07

   Als Administrator, der zugleich Trainer ist, entscheide ich über meinen
   eigenen Abwesenheitsantrag.

.. test:: Administrator kann eigenen Antrag entscheiden
   :id: TEST_ABW_KONFL_06
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_KONFL_06, REQ_ABW_KONFL_07

   Ein Administrator, der selbst einen Antrag gestellt hat, kann ihn
   genehmigen oder ablehnen.

.. story:: Administratoren sehen offene Anträge in der Übersicht
   :id: STORY_ABW_UEBER_01
   :status: approved
   :implements: REQ_ABW_UEBER_01

   Als Administrator sehe ich alle offenen Abwesenheitsanträge gesammelt,
   klar unterscheidbar von reinen Verfügbarkeitshinweisen.

.. test:: Übersicht trennt Anträge von Hinweisen
   :id: TEST_ABW_UEBER_01
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_UEBER_01, REQ_ABW_UEBER_01

   Die Administratoren-Übersicht führt offene Anträge und
   Verfügbarkeitshinweise erkennbar getrennt.

Fristablauf
------------

.. story:: Unentschiedener Antrag wird automatisch genehmigt
   :id: STORY_ABW_AUTO_01
   :status: approved
   :priority: high
   :implements: REQ_ABW_AUTO_01, REQ_ABW_AUTO_02

   Als Trainer, dessen Antrag niemand entscheidet, sehe ich meine Abwesenheit
   eine Woche vor ihrem Beginn automatisch genehmigt; die betroffenen
   Termine werden frei und die Beteiligten informiert.

.. test:: Fristablauf genehmigt automatisch und informiert
   :id: TEST_ABW_AUTO_01
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_AUTO_01, REQ_ABW_AUTO_01, REQ_ABW_AUTO_02

   Ein Antrag, der eine Woche vor Beginn des Zeitraums noch offen ist, gilt
   danach als genehmigt; die kollidierenden Termine werden frei, Trainer und
   Administratoren werden benachrichtigt.

.. story:: Mindestvorlauf von einer Woche
   :id: STORY_ABW_ERF_05
   :status: approved
   :implements: REQ_ABW_ERF_07

   Als Trainer erfahre ich, dass ich eine Abwesenheit mindestens eine Woche
   vor ihrem Beginn eintragen muss.

.. test:: Zu kurzer Vorlauf wird abgewiesen
   :id: TEST_ABW_ERF_05
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_ERF_05, REQ_ABW_ERF_07

   Ein Zeitraum, der weniger als eine Woche vor seinem Beginn eingetragen
   wird, wird abgewiesen; ab genau einer Woche gelingt es.

.. story:: Offene Anträge zeigen ihre Restfrist
   :id: STORY_ABW_UEBER_02
   :status: approved
   :implements: REQ_ABW_UEBER_02

   Als Administrator sehe ich zu jedem offenen Antrag, wie viel Zeit bis zur
   automatischen Genehmigung bleibt.

.. test:: Übersicht zeigt die Restfrist je Antrag
   :id: TEST_ABW_UEBER_02
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_UEBER_02, REQ_ABW_UEBER_02

   Zu einem offenen Antrag zeigt die Übersicht die verbleibende Zeit bis zum
   Stichtag aus :need:`REQ_ABW_AUTO_01`.

Grund-Regeln
-------------

.. story:: Grund ist für Administratoren sichtbar und ohne Gesundheitsdaten
   :id: STORY_ABW_ERF_06
   :status: approved
   :implements: REQ_ABW_ERF_08, REQ_ABW_ERF_09

   Als Administrator sehe ich den von einem Trainer angegebenen Grund. Als
   Trainer werde ich beim Eintragen darauf hingewiesen, dass der Grund für
   Administratoren sichtbar ist und keine Gesundheitsdaten enthalten soll.

.. test:: Grund erscheint in der Administratorenansicht
   :id: TEST_ABW_ERF_06
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_ERF_06, REQ_ABW_ERF_08

   Ein bei der Abwesenheit angegebener Grund erscheint unverändert in der
   Ansicht der Administratoren.

.. test:: Hinweis auf Sichtbarkeit und Gesundheitsdaten beim Eintragen
   :id: TEST_ABW_ERF_07
   :status: approved
   :automated: no
   :level: manual
   :verifies: STORY_ABW_ERF_06, REQ_ABW_ERF_09

   Beim Eintragen eines Grundes erscheint der Hinweis, dass er für
   Administratoren sichtbar ist und keine Gesundheitsdaten enthalten soll.
   Reiner Oberflächentext -- manuell geprüft.

Tausch mit einem Ersatztrainer
--------------------------------

.. story:: Ich schlage einen Ersatztrainer vor
   :id: STORY_ABW_TAUSCH_01
   :status: approved
   :priority: high
   :implements: REQ_ABW_TAUSCH_01, REQ_ABW_TAUSCH_02

   Als Trainer schlage ich beim Eintragen einer Abwesenheit mit
   Zuweisungskonflikt einen bereits qualifizierten Ersatztrainer vor, statt
   einen Genehmigungsantrag auszulösen -- sofern der Zeitraum mindestens
   zwei Wochen vor seinem Beginn eingetragen wird.

.. test:: Tausch-Option nur mit zwei Wochen Vorlauf und passender Qualifikation
   :id: TEST_ABW_TAUSCH_01
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_TAUSCH_01, REQ_ABW_TAUSCH_01

   Bei mindestens zwei Wochen Vorlauf stehen als Ersatztrainer nur Trainer
   zur Auswahl, die für alle betroffenen Schulungen qualifiziert sind.

.. test:: Zu kurzer Vorlauf lässt nur den Genehmigungsantrag zu
   :id: TEST_ABW_TAUSCH_02
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_TAUSCH_01, REQ_ABW_TAUSCH_02

   Bei weniger als zwei Wochen Vorlauf wird keine Tausch-Option angeboten;
   das Eintragen führt direkt zum Genehmigungsantrag aus
   :need:`STORY_ABW_KONFL_01`.

.. story:: Ersatztrainer entscheidet über die Anfrage
   :id: STORY_ABW_TAUSCH_02
   :status: approved
   :implements: REQ_ABW_TAUSCH_03, REQ_ABW_TAUSCH_04

   Als vorgeschlagener Ersatztrainer nehme ich die Anfrage an und werde den
   betroffenen Terminen zugewiesen, ohne dass ein Administrator eingreifen
   muss.

.. test:: Annahme weist den Ersatztrainer allen betroffenen Terminen zu
   :id: TEST_ABW_TAUSCH_03
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_TAUSCH_02, REQ_ABW_TAUSCH_04

   Nimmt der Ersatztrainer an, ist er anschließend allen betroffenen
   Terminen zugewiesen, die Abwesenheit des ursprünglichen Trainers ist
   aktiv, und es liegt kein offener Antrag vor.

.. story:: Ablehnung oder Fristablauf fällt zurück in die Genehmigung
   :id: STORY_ABW_TAUSCH_03
   :status: approved
   :priority: high
   :implements: REQ_ABW_TAUSCH_05

   Als Trainer werde ich informiert, wenn mein vorgeschlagener Ersatztrainer
   ablehnt oder nicht reagiert -- daraus wird ein regulärer
   Genehmigungsantrag.

.. test:: Ablehnung erzeugt einen Genehmigungsantrag
   :id: TEST_ABW_TAUSCH_04
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_TAUSCH_03, REQ_ABW_TAUSCH_05

   Lehnt der Ersatztrainer ab, entsteht ein Antrag nach
   :need:`REQ_ABW_KONFL_02`; der ursprüngliche Trainer wird über die
   Ablehnung informiert.

.. test:: Ausbleibende Reaktion nach einer Woche erzeugt ebenfalls einen Antrag
   :id: TEST_ABW_TAUSCH_05
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_TAUSCH_03, REQ_ABW_TAUSCH_05

   Reagiert der Ersatztrainer eine Woche nach der Anfrage nicht, entsteht
   ebenfalls ein Antrag nach :need:`REQ_ABW_KONFL_02`; der ursprüngliche
   Trainer wird über den Fristablauf informiert.

.. story:: Administrator sieht offene Tauschanfrage informativ
   :id: STORY_ABW_UEBER_03
   :status: approved
   :implements: REQ_ABW_UEBER_03

   Als Administrator sehe ich, dass eine Tauschanfrage offen ist, ohne dass
   ich reagieren müsste.

.. test:: Übersicht zeigt Tauschanfragen getrennt von Anträgen
   :id: TEST_ABW_UEBER_03
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_ABW_UEBER_03, REQ_ABW_UEBER_03

   Eine offene Tauschanfrage erscheint in der Administratoren-Übersicht
   getrennt von den offenen Anträgen und ohne Aufforderung zur Entscheidung.
   Fällt sie nach Ablehnung oder Fristablauf in einen Antrag zurück,
   erscheint sie danach dort.
