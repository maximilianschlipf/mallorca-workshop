Qualifikationen: Stories und Tests
===================================

Diese Seite übersetzt die Anforderungen aus :need:`REQ_QUA_BEW_02` und
folgende in umsetzbare Stories und in Tests, die sie abprüfen. Sie ist die
Arbeitsgrundlage für die Umsetzung des Adminbereichs: die Bewerbung selbst
ist bereits umgesetzt, die Entscheidung darüber und der Entzug einer
Qualifikation noch nicht.

* Eine **Story** beschreibt einen Ablauf aus Sicht des Benutzers und
  verweist mit ``implements`` auf die Anforderungen, die sie erfüllt.
* Ein **Test** beschreibt eine nachprüfbare Beobachtung und verweist mit
  ``verifies`` auf die Story oder Anforderung, die er absichert.

Die zugrundeliegenden Anforderungen ``REQ_QUA_BEW_02`` und folgende tragen
noch den Status ``draft``. Stories und Tests dieser Seite bleiben deshalb
ebenfalls ``draft``, bis die Anforderungen freigegeben sind -- erst danach
darf die Umsetzung beginnen.

Freigabeanfragen entscheiden
-----------------------------

.. story:: Ich entscheide über eine Freigabeanfrage
   :id: STORY_QUA_ENTSCHEID_01
   :status: draft
   :priority: high
   :implements: REQ_QUA_BEW_02, REQ_QUA_BEW_04

   Als Administrator sehe ich alle offenen Freigabeanfragen mit Trainer und
   Schulung und genehmige oder lehne jede einzeln ab. Bei einer Genehmigung
   ist der Trainer sofort für die Schulung qualifiziert, und ihre künftigen
   geplanten Termine erscheinen in seiner Übersicht der relevanten Termine.

.. story:: Ich lehne eine Freigabeanfrage begründet ab
   :id: STORY_QUA_ENTSCHEID_02
   :status: draft
   :priority: high
   :implements: REQ_QUA_BEW_08, REQ_QUA_BEW_09

   Als Administrator gebe ich beim Ablehnen einer Freigabeanfrage eine
   Begründung an. Danach kann sich der Trainer für dieselbe Schulung einen
   Tag lang nicht erneut bewerben.

.. story:: Der Trainer erfährt von der Entscheidung
   :id: STORY_QUA_ENTSCHEID_03
   :status: draft
   :implements: REQ_QUA_BEW_03

   Als Trainer werde ich benachrichtigt, sobald über meine Freigabeanfrage
   entschieden wurde. Bei einer Ablehnung sehe ich die angegebene
   Begründung, bei einer Genehmigung nicht.

.. test:: Genehmigung erteilt die Qualifikation
   :id: TEST_QUA_ENTSCHEID_01
   :status: draft
   :automated: yes
   :verifies: STORY_QUA_ENTSCHEID_01, REQ_QUA_BEW_02

   Eine offene Freigabeanfrage eines Trainers zu einer Schulung wird von
   einem Administrator genehmigt. Danach ist der Trainer für diese Schulung
   qualifiziert, die Anfrage ist nicht mehr offen, und der Trainer kann sich
   für dieselbe Schulung nicht erneut bewerben, da er bereits qualifiziert
   ist.

.. test:: Genehmigung schaltet künftige Termine frei
   :id: TEST_QUA_ENTSCHEID_02
   :status: draft
   :automated: yes
   :verifies: STORY_QUA_ENTSCHEID_01, REQ_QUA_BEW_04

   Zu einer Schulung existieren ein künftiger und ein bereits vergangener
   geplanter Termin. Nach der Genehmigung der Freigabeanfrage erscheint nur
   der künftige Termin in der Übersicht der relevanten Termine des
   Trainers; vor der Genehmigung erschien keiner von beiden.

.. test:: Ablehnung verlangt eine Begründung
   :id: TEST_QUA_ENTSCHEID_03
   :status: draft
   :automated: yes
   :verifies: STORY_QUA_ENTSCHEID_02, REQ_QUA_BEW_08

   Der Versuch, eine offene Freigabeanfrage ohne Begründung abzulehnen, wird
   abgewiesen und die Anfrage bleibt offen. Mit einer Begründung gelingt die
   Ablehnung; der Trainer erhält danach keine Qualifikation für die
   Schulung.

.. test:: Sperrfrist nach einer Ablehnung
   :id: TEST_QUA_ENTSCHEID_04
   :status: draft
   :automated: yes
   :verifies: STORY_QUA_ENTSCHEID_02, REQ_QUA_BEW_09

   Unmittelbar nach einer abgelehnten Freigabeanfrage wird eine erneute
   Bewerbung desselben Trainers für dieselbe Schulung abgewiesen. Nach
   Ablauf eines Tages gelingt eine erneute Bewerbung.

.. test:: Benachrichtigung nennt die Entscheidung
   :id: TEST_QUA_ENTSCHEID_05
   :status: draft
   :automated: yes
   :verifies: STORY_QUA_ENTSCHEID_03, REQ_QUA_BEW_03

   Nach einer Genehmigung erhält der Trainer eine Benachrichtigung ohne
   Begründungstext. Nach einer Ablehnung erhält er eine Benachrichtigung mit
   der beim Ablehnen angegebenen Begründung.

Qualifikation entziehen
------------------------

.. story:: Ich entziehe eine Qualifikation
   :id: STORY_QUA_ENTZUG_01
   :status: draft
   :priority: high
   :implements: REQ_QUA_ENTZ_01, REQ_QUA_ENTZ_02, REQ_QUA_ENTZ_03

   Als Administrator entziehe ich einem Trainer eine bestätigte
   Qualifikation. Seine künftigen Termine dieser Schulung werden nicht mehr
   zugewiesen, ein abgeschlossener Termin bleibt unverändert, und der
   Trainer wird über den Entzug benachrichtigt.

.. story:: Nach einem Entzug kann ich mich sofort erneut bewerben
   :id: STORY_QUA_ENTZUG_02
   :status: draft
   :implements: REQ_QUA_ENTZ_04

   Als Trainer, dem eine Qualifikation entzogen wurde, kann ich mich für
   dieselbe Schulung sofort wieder bewerben -- ohne die Sperrfrist, die nach
   einer Ablehnung gilt.

.. test:: Entzug räumt künftige Zuweisungen ab
   :id: TEST_QUA_ENTZUG_01
   :status: draft
   :automated: yes
   :verifies: STORY_QUA_ENTZUG_01, REQ_QUA_ENTZ_02

   Ein qualifizierter Trainer ist einem künftigen und einem abgeschlossenen
   Termin der Schulung als ausführender Trainer zugewiesen. Nach dem Entzug
   der Qualifikation ist der künftige Termin nicht zugewiesen; der
   abgeschlossene führt den Trainer weiterhin.

.. test:: Entzug benachrichtigt den Trainer
   :id: TEST_QUA_ENTZUG_02
   :status: draft
   :automated: yes
   :verifies: STORY_QUA_ENTZUG_01, REQ_QUA_ENTZ_03

   Nach dem Entzug einer Qualifikation erhält der betroffene Trainer eine
   Benachrichtigung darüber.

.. test:: Entzug ohne bestehende Qualifikation wird abgewiesen
   :id: TEST_QUA_ENTZUG_03
   :status: draft
   :automated: yes
   :verifies: REQ_QUA_ENTZ_01

   Der Versuch, einem Trainer eine Qualifikation zu entziehen, die er nicht
   besitzt, wird abgewiesen.

.. test:: Nach Entzug ist sofort erneute Bewerbung möglich
   :id: TEST_QUA_ENTZUG_04
   :status: draft
   :automated: yes
   :verifies: STORY_QUA_ENTZUG_02, REQ_QUA_ENTZ_04

   Unmittelbar nach dem Entzug einer Qualifikation kann sich der Trainer für
   dieselbe Schulung erneut bewerben; anders als nach einer Ablehnung gilt
   hier keine Sperrfrist.

Qualifikation selbst ablegen
------------------------------

.. story:: Ich lege eine eigene Qualifikation ab
   :id: STORY_QUA_ABLEGEN_01
   :status: draft
   :priority: high
   :implements: REQ_QUA_ABLEGEN_01, REQ_QUA_ABLEGEN_02

   Als Trainer sehe ich in meinem Profil meine bestätigten Qualifikationen
   und kann jede davon ablegen. Bevor der Vorgang wirkt, bestätige ich ihn
   ausdrücklich; bestehen künftige Termine dieser Schulung, weist der
   Bestätigungsdialog darauf hin, dass sie danach nicht mehr zugewiesen
   sind.

.. story:: Nach dem Ablegen kann ich mich sofort erneut bewerben
   :id: STORY_QUA_ABLEGEN_02
   :status: draft
   :implements: REQ_QUA_ABLEGEN_03

   Als Trainer, der eine Qualifikation abgelegt hat, kann ich mich für
   dieselbe Schulung sofort wieder bewerben -- ohne die Sperrfrist, die nach
   einer Ablehnung gilt.

.. test:: Ablegen verlangt eine ausdrückliche Bestätigung
   :id: TEST_QUA_ABLEGEN_01
   :status: draft
   :automated: yes
   :verifies: STORY_QUA_ABLEGEN_01, REQ_QUA_ABLEGEN_01

   Ein Trainer stößt das Ablegen einer eigenen Qualifikation an, bricht die
   Bestätigung ab. Die Qualifikation bleibt bestehen. Erst mit der
   ausdrücklichen Bestätigung wird sie entfernt.

.. test:: Ablegen warnt bei künftigen Terminen und räumt sie ab
   :id: TEST_QUA_ABLEGEN_02
   :status: draft
   :automated: yes
   :verifies: STORY_QUA_ABLEGEN_01, REQ_QUA_ABLEGEN_02

   Ein Trainer mit einem künftigen und einem abgeschlossenen Termin der
   Schulung als ausführender Trainer legt die Qualifikation ab. Der
   Bestätigungsschritt weist auf den künftigen Termin hin. Nach dem Ablegen
   ist der künftige Termin nicht zugewiesen; der abgeschlossene führt den
   Trainer weiterhin.

.. test:: Nach dem Ablegen ist sofort erneute Bewerbung möglich
   :id: TEST_QUA_ABLEGEN_03
   :status: draft
   :automated: yes
   :verifies: STORY_QUA_ABLEGEN_02, REQ_QUA_ABLEGEN_03

   Unmittelbar nach dem Ablegen einer Qualifikation kann sich der Trainer
   für dieselbe Schulung erneut bewerben; anders als nach einer Ablehnung
   gilt hier keine Sperrfrist.

.. test:: Ablegen benachrichtigt den Adminbereich
   :id: TEST_QUA_ABLEGEN_04
   :status: draft
   :automated: yes
   :verifies: REQ_QUA_ABLEGEN_04

   Nachdem ein Trainer eine Qualifikation abgelegt hat, erhalten
   Administratoren eine Benachrichtigung darüber.

Trainer direkt qualifizieren
------------------------------

.. story:: Ich qualifiziere einen Trainer direkt für eine Schulung
   :id: STORY_QUA_DIREKT_01
   :status: draft
   :priority: high
   :implements: REQ_QUA_DIREKT_01, REQ_QUA_DIREKT_04

   Als Administrator trage ich in der Qualifikationstabelle einer Schulung
   einen Trainer ein, der sich nicht beworben hat, und er ist danach für
   diese Schulung qualifiziert. Für einen bereits qualifizierten Trainer
   bietet die Tabelle das nicht an.

.. story:: Direktvergabe schließt eine offene Bewerbung
   :id: STORY_QUA_DIREKT_02
   :status: draft
   :implements: REQ_QUA_DIREKT_02, REQ_QUA_DIREKT_03

   Hat der direkt qualifizierte Trainer für dieselbe Schulung noch eine
   offene Freigabeanfrage, gilt sie danach als genehmigt statt weiterhin
   offen zu stehen. Der Trainer wird über die Direktvergabe benachrichtigt,
   wie bei einer Genehmigung.

.. test:: Direktvergabe erteilt die Qualifikation ohne Bewerbung
   :id: TEST_QUA_DIREKT_01
   :status: draft
   :automated: yes
   :verifies: STORY_QUA_DIREKT_01, REQ_QUA_DIREKT_01

   Ein Trainer ohne Bewerbung und ohne Qualifikation für eine Schulung wird
   vom Administrator direkt qualifiziert. Danach ist er für die Schulung
   qualifiziert und ihre künftigen Termine erscheinen in seiner Übersicht.

.. test:: Bereits qualifizierter Trainer wird nicht erneut angeboten
   :id: TEST_QUA_DIREKT_02
   :status: draft
   :automated: yes
   :verifies: STORY_QUA_DIREKT_01, REQ_QUA_DIREKT_04

   Für einen Trainer, der für eine Schulung bereits qualifiziert ist, bietet
   die Tabelle die Direktvergabe für dieselbe Schulung nicht an.

.. test:: Direktvergabe genehmigt eine offene Bewerbung automatisch
   :id: TEST_QUA_DIREKT_03
   :status: draft
   :automated: yes
   :verifies: STORY_QUA_DIREKT_02, REQ_QUA_DIREKT_02

   Ein Trainer hat eine offene Freigabeanfrage zu einer Schulung. Der
   Administrator qualifiziert ihn über die Direktvergabe für dieselbe
   Schulung. Danach ist die Freigabeanfrage nicht mehr offen, sondern
   genehmigt, und es existiert genau eine Qualifikation des Trainers für
   die Schulung.

.. test:: Direktvergabe benachrichtigt den Trainer
   :id: TEST_QUA_DIREKT_04
   :status: draft
   :automated: yes
   :verifies: STORY_QUA_DIREKT_02, REQ_QUA_DIREKT_03

   Nach einer Direktvergabe erhält der betroffene Trainer eine
   Benachrichtigung, die der einer genehmigten Bewerbung entspricht.

Abdeckung
---------

Welche Anforderung durch welche Story umgesetzt und durch welchen Test
abgesichert ist:

.. needtable::
   :types: req
   :filter: id.split("_")[1] == "QUA"
   :columns: id, title, status, implements_back, verifies_back
   :style: table

Anforderungen dieses Bereichs ohne Test:

.. needtable::
   :types: req
   :filter: id.split("_")[1] == "QUA" and not verifies_back
   :columns: id, title, priority
   :style: table
