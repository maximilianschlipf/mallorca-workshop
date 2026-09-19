Dashboard: Stories und Tests
=============================

Diese Seite übersetzt die Anforderungen aus :need:`REQ_DSH_GRUND_01` und
folgende in umsetzbare Stories und in Tests, die sie abprüfen.

* Eine **Story** beschreibt einen Ablauf aus Sicht des Benutzers und
  verweist mit ``implements`` auf die Anforderungen, die sie erfüllt.
* Ein **Test** beschreibt eine nachprüfbare Beobachtung und verweist mit
  ``verifies`` auf die Story oder Anforderung, die er absichert.

Die Anforderungen dieses Bereichs stehen auf ``review``. Stories und Tests
stehen deshalb ebenfalls auf ``review`` und sind noch nicht implementiert;
sie zählen erst nach der fachlichen Freigabe in das Requirements-Gate. Die
Dringlichkeiten im dritten Rang sind heute durch ``TEST_TER_DASH_01`` bis
``TEST_TER_DASH_05`` abgedeckt; diese Tests wandern mit der Freigabe hierher.

Aufbau
------

.. story:: Ich sehe auf dem Dashboard, was bei mir liegt
   :id: STORY_DSH_GRUND_01
   :status: review
   :priority: high
   :implements: REQ_DSH_GRUND_01, REQ_DSH_GRUND_03

   Als angemeldeter Benutzer lande ich auf dem Dashboard und finde dort in
   fester Reihenfolge: die Vorgänge, über die ich entscheide, meine eigenen
   Handlungspflichten und die Dringlichkeiten meiner Rolle.

.. story:: Mitteilungen stören mein Dashboard nicht
   :id: STORY_DSH_GRUND_02
   :status: review
   :priority: high
   :implements: REQ_DSH_GRUND_02

   Als Benutzer finde ich auf dem Dashboard nichts, was ich nur zur Kenntnis
   nehmen soll -- das steht in den Benachrichtigungen.

.. test:: Die drei Ränge stehen in fester Reihenfolge
   :id: TEST_DSH_GRUND_01
   :status: review
   :automated: yes
   :level: e2e
   :verifies: STORY_DSH_GRUND_01, REQ_DSH_GRUND_01, REQ_DSH_GRUND_03

   Ein Administrator mit einem offenen Abwesenheitsantrag, einem eigenen
   überfälligen Termin und einem Termin ohne Trainer sieht auf dem
   Dashboard alle drei Ränge in der Reihenfolge Vorgänge,
   Handlungspflichten, Dringlichkeiten. Ein Trainer in derselben Lage sieht
   denselben Aufbau mit den Inhalten seiner Rolle.

.. test:: Mitteilungen erscheinen nicht im Dashboard
   :id: TEST_DSH_GRUND_02
   :status: review
   :automated: yes
   :level: e2e
   :verifies: STORY_DSH_GRUND_02, REQ_DSH_GRUND_02

   Ein Trainer, dessen Termin geändert wurde und der eine offene
   Übernahmeanfrage an sich hat, findet im Dashboard nur die
   Übernahmeanfrage. Die Mitteilung über die Terminänderung steht
   ausschließlich in den Benachrichtigungen.

Vorgänge entscheiden
--------------------

.. story:: Ich entscheide die Vorgänge, die an mich gerichtet sind
   :id: STORY_DSH_VORG_01
   :status: review
   :priority: high
   :implements: REQ_DSH_VORG_01, REQ_DSH_VORG_03, REQ_DSH_VORG_04

   Als Entscheider sehe ich jeden an mich gerichteten Vorgang nach demselben
   Muster -- Art, Antragsteller, Bezug, Zeitpunkt -- und entscheide ihn mit
   Annehmen oder Ablehnen. Danach lässt er sich nicht erneut entscheiden.

.. story:: Ich sehe, worauf ich selbst warte
   :id: STORY_DSH_VORG_02
   :status: review
   :priority: high
   :implements: REQ_DSH_VORG_02, REQ_DSH_VORG_07

   Als Trainer sehe ich auf demselben Dashboard meine eigenen offenen
   Vorgänge -- Bewerbungen, Vormerkungen, Assistenzbewerbungen,
   Abwesenheitsanträge und gestellte Übernahmeanfragen -- und ziehe sie von
   dort zurück.

.. story:: Ich begründe eine Ablehnung, wo es verlangt ist
   :id: STORY_DSH_VORG_03
   :status: review
   :implements: REQ_DSH_VORG_05

   Als Administrator gebe ich beim Ablehnen einer Freigabeanfrage und einer
   Vormerkung eine Begründung an; bei den übrigen Vorgangsarten verlangt das
   System keine.

.. story:: Erledigtes bleibt auffindbar
   :id: STORY_DSH_VORG_04
   :status: review
   :priority: high
   :implements: REQ_DSH_VORG_08, REQ_DSH_VORG_09

   Als Benutzer finde ich entschiedene, entfallene und zurückgezogene
   Vorgänge in einem zugeklappten Abschnitt wieder, mit Ergebnis, Zeitpunkt
   und -- falls jemand entschieden hat -- der entscheidenden Person.

.. test:: Alle fünf Vorgangsarten erscheinen beim Zuständigen
   :id: TEST_DSH_VORG_01
   :status: review
   :automated: yes
   :level: integration
   :verifies: STORY_DSH_VORG_01, REQ_DSH_VORG_01, REQ_DSH_VORG_03

   Zu jeder der fünf Vorgangsarten aus :need:`REQ_DSH_VORG_01` wird ein
   offener Vorgang angelegt. Jeder erscheint im ersten Rang des Dashboards
   seines Zuständigen -- und nur dort -- mit Vorgangsart, Antragsteller,
   Bezug und Zeitpunkt sowie den Aktionen Annehmen und Ablehnen.

.. test:: Eine Entscheidung wirkt genau einmal
   :id: TEST_DSH_VORG_02
   :status: review
   :automated: yes
   :level: integration
   :verifies: STORY_DSH_VORG_01, REQ_DSH_VORG_04

   Ein Administrator nimmt eine Vormerkung an; der Trainer ist danach dem
   Termin zugewiesen. Ein zweiter Entscheidungsversuch auf denselben
   Vorgang -- annehmen wie ablehnen -- wird abgewiesen und ändert die
   Zuweisung nicht.

.. test:: Eigene Vorgänge erscheinen und lassen sich zurückziehen
   :id: TEST_DSH_VORG_03
   :status: review
   :automated: yes
   :level: e2e
   :verifies: STORY_DSH_VORG_02, REQ_DSH_VORG_02, REQ_DSH_VORG_07

   Ein Trainer mit je einer offenen Bewerbung, Vormerkung,
   Assistenzbewerbung, einem Abwesenheitsantrag und einer gestellten
   Übernahmeanfrage sieht alle fünf getrennt von den an ihn gerichteten
   Vorgängen. Er zieht jeden davon aus dem Dashboard zurück; danach ist
   keiner mehr offen, jeder steht als zurückgezogen unter den erledigten,
   und es ist zu keinem eine Mitteilung entstanden.

.. test:: Begründung ist nur bei zwei Vorgangsarten Pflicht
   :id: TEST_DSH_VORG_04
   :status: review
   :automated: yes
   :level: integration
   :verifies: STORY_DSH_VORG_03, REQ_DSH_VORG_05

   Eine Ablehnung ohne Begründung wird bei Freigabeanfrage und Vormerkung
   abgewiesen und bei Abwesenheitsantrag, Assistenzbewerbung und
   Übernahmeanfrage angenommen. Eine Annahme verlangt in keinem Fall eine
   Begründung, und die Begründung erreicht den Antragsteller mit seiner
   Mitteilung.

.. test:: Ein Vorgang mit zwei Zuständigen entfällt bei beiden
   :id: TEST_DSH_VORG_05
   :status: review
   :automated: yes
   :level: integration
   :verifies: REQ_DSH_VORG_06, REQ_ASS_BEW_02

   Eine Assistenzbewerbung zu einem Termin mit zugewiesenem Trainer
   erscheint im Dashboard dieses Trainers und in dem des Administrators.
   Entscheidet der Trainer, ist sie bei beiden erledigt, mit ihm als
   Entscheider, und der Administrator kann sie nicht mehr entscheiden. Ohne
   zugewiesenen Trainer erscheint sie nur beim Administrator.

.. test:: Erledigte Vorgänge bleiben zugeklappt erhalten
   :id: TEST_DSH_VORG_06
   :status: review
   :automated: yes
   :level: e2e
   :verifies: STORY_DSH_VORG_04, REQ_DSH_VORG_08

   Nach einer Annahme, einer Ablehnung und einem Zurückziehen ist der
   Abschnitt der erledigten Vorgänge zugeklappt. Geöffnet enthält er alle
   drei -- getrennt nach an mich gerichtet und von mir gestellt -- mit
   Ergebnis, Zeitpunkt und entscheidender Person, beim Zurückgezogenen ohne
   Person. Sie bleiben auch nach erneutem Laden erhalten.

.. test:: Entfallene Vorgänge nennen das Ereignis als Grund
   :id: TEST_DSH_VORG_07
   :status: review
   :automated: yes
   :level: integration
   :verifies: STORY_DSH_VORG_04, REQ_DSH_VORG_09

   Für alle vier Fälle aus :need:`REQ_DSH_VORG_09` -- Zuweisung eines
   anderen Trainers, Tausch durch Übernahme, Archivieren einer Schulung und
   Fristablauf beim Abwesenheitsantrag -- endet der betroffene Vorgang als
   erledigt. Das Ergebnis nennt jeweils das auslösende Ereignis und keine
   entscheidende Person.

.. test:: Jede Entscheidung erreicht den Antragsteller
   :id: TEST_DSH_VORG_08
   :status: review
   :automated: yes
   :level: integration
   :verifies: REQ_DSH_VORG_10, REQ_NAC_ANL_04

   Nach jeder Entscheidung über einen Vorgang liegt beim Antragsteller genau
   eine Mitteilung mit dem Ergebnis vor. Beim Entscheider entsteht keine;
   für ihn steht der Vorgang unter den erledigten.

Handlungspflichten und Dringlichkeiten
--------------------------------------

.. story:: Meine überfälligen Termine gehen mir nicht verloren
   :id: STORY_DSH_PFLI_01
   :status: review
   :priority: high
   :implements: REQ_DSH_PFLI_01

   Als Trainer finde ich meine überfälligen geplanten Termine im zweiten
   Rang des Dashboards und kann ihre Durchführung von dort bestätigen.

.. story:: Als Administrator sehe ich, wo es eng wird
   :id: STORY_DSH_DRIN_01
   :status: review
   :priority: high
   :implements: REQ_DSH_DRIN_01, REQ_DSH_DRIN_02, REQ_DSH_DRIN_03, REQ_DSH_DRIN_04

   Als Administrator sehe ich im dritten Rang die zukünftigen geplanten
   Termine ohne Trainer, aufsteigend sortiert und ab weniger als vier Wochen
   Vorlauf als dringend markiert, die überfälligen davor und die exklusiven
   Termine unter ihrer Mindestteilnehmerzahl ebenfalls als dringend.

.. test:: Überfällige eigene Termine stehen im zweiten Rang
   :id: TEST_DSH_PFLI_01
   :status: review
   :automated: yes
   :level: e2e
   :verifies: STORY_DSH_PFLI_01, REQ_DSH_PFLI_01

   Ein Trainer mit einem überfälligen geplanten, einem anstehenden, einem
   abgesagten und einem abgeschlossenen eigenen Termin sieht im zweiten Rang
   nur den überfälligen und bestätigt von dort dessen Durchführung.

.. test:: Termine ohne Trainer erscheinen sortiert und markiert
   :id: TEST_DSH_DRIN_01
   :status: review
   :automated: yes
   :level: integration
   :verifies: STORY_DSH_DRIN_01, REQ_DSH_DRIN_01, REQ_DSH_DRIN_03

   Das Dashboard eines Administrators führt alle zukünftigen geplanten
   Termine ohne Trainerzuweisung aufsteigend nach Startdatum. Ein Termin
   mit weniger als vier Wochen Vorlauf ist als dringend markiert, einer mit
   genau vier Wochen erscheint ohne Markierung.

.. test:: Überfällige Termine stehen vor den anstehenden
   :id: TEST_DSH_DRIN_02
   :status: review
   :automated: yes
   :level: integration
   :verifies: STORY_DSH_DRIN_01, REQ_DSH_DRIN_02

   Im Dashboard eines Administrators stehen überfällige geplante Termine vor
   den anstehenden. Abgesagte und abgeschlossene Termine erscheinen nicht.

.. test:: Zu wenige Teilnehmer machen einen exklusiven Termin dringend
   :id: TEST_DSH_DRIN_03
   :status: review
   :automated: yes
   :level: integration
   :verifies: STORY_DSH_DRIN_01, REQ_DSH_DRIN_04

   Ein exklusiver Termin unter seiner Mindestteilnehmerzahl mit weniger als
   vier Wochen Vorlauf ist als dringend markiert. Ein öffentlicher Termin
   und ein Termin ohne Zugangsart erhalten diese Markierung nicht.
