Dashboard: Stories und Tests
=============================

Diese Seite übersetzt die Anforderungen aus :need:`REQ_DSH_GRUND_01` und
folgende in umsetzbare Stories und in Tests, die sie abprüfen.

* Eine **Story** beschreibt einen Ablauf aus Sicht des Benutzers und
  verweist mit ``implements`` auf die Anforderungen, die sie erfüllt.
* Ein **Test** beschreibt eine nachprüfbare Beobachtung und verweist mit
  ``verifies`` auf die Story oder Anforderung, die er absichert.

Die Anforderungen, Stories und Tests dieses Bereichs sind fachlich
freigegeben und zählen im Requirements-Gate. Die bestehenden Nachweise
``TEST_TER_DASH_01`` bis ``TEST_TER_DASH_05`` sind auf die neuen
Dashboard-Anforderungen umgezogen.

Aufbau
------

.. story:: Ich sehe auf dem Dashboard, was bei mir liegt
   :id: STORY_DSH_GRUND_01
   :status: approved
   :priority: high
   :implements: REQ_DSH_GRUND_01, REQ_DSH_GRUND_03

   Als angemeldeter Benutzer lande ich auf dem Dashboard und finde dort in
   fester Reihenfolge: die Vorgänge, über die ich entscheide, meine eigenen
   Handlungspflichten und die Dringlichkeiten meiner Rolle.

.. story:: Mitteilungen stören mein Dashboard nicht
   :id: STORY_DSH_GRUND_02
   :status: approved
   :priority: high
   :implements: REQ_DSH_GRUND_02

   Als Benutzer finde ich auf dem Dashboard nichts, was ich nur zur Kenntnis
   nehmen soll -- das steht in den Benachrichtigungen.

.. test:: Die drei Ränge stehen in fester Reihenfolge
   :id: TEST_DSH_GRUND_01
   :status: approved
   :automated: yes
   :level: e2e
   :verifies: STORY_DSH_GRUND_01, REQ_DSH_GRUND_01, REQ_DSH_GRUND_03

   Nach erfolgreicher Anmeldung landet ein Administrator mit einem offenen
   Abwesenheitsantrag, einem eigenen überfälligen Termin und einem Termin
   ohne Trainer auf dem Dashboard und sieht alle drei Ränge in der Reihenfolge Vorgänge,
   Handlungspflichten, Dringlichkeiten. Ein Trainer in derselben Lage sieht
   denselben Aufbau mit den Inhalten seiner Rolle.

.. test:: Mitteilungen erscheinen nicht im Dashboard
   :id: TEST_DSH_GRUND_02
   :status: approved
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
   :status: approved
   :priority: high
   :implements: REQ_DSH_VORG_01, REQ_DSH_VORG_03, REQ_DSH_VORG_04, REQ_DSH_VORG_06, REQ_DSH_VORG_10, REQ_DSH_SICHER_01

   Als Entscheider sehe ich jeden an mich gerichteten Vorgang nach demselben
   Muster -- Art, Antragsteller, Bezug, Zeitpunkt -- und entscheide ihn mit
   Annehmen oder Ablehnen. Danach lässt er sich nicht erneut entscheiden.

.. story:: Ich sehe, worauf ich selbst warte
   :id: STORY_DSH_VORG_02
   :status: approved
   :priority: high
   :implements: REQ_DSH_VORG_02, REQ_DSH_VORG_07

   Als Trainer sehe ich auf demselben Dashboard meine eigenen offenen
   Vorgänge -- Bewerbungen, Vormerkungen, Assistenzbewerbungen,
   Abwesenheitsanträge, gestellte Übernahmeanfragen und
   Ersatztrainer-Anfragen. Die fünf nach ihrem Fachbereich zurückziehbaren
   Arten ziehe ich von dort zurück.

.. story:: Ich begründe eine Ablehnung, wo es verlangt ist
   :id: STORY_DSH_VORG_03
   :status: approved
   :implements: REQ_DSH_VORG_05

   Als Administrator gebe ich beim Ablehnen einer Freigabeanfrage und einer
   Vormerkung eine Begründung an; bei den übrigen Vorgangsarten verlangt das
   System keine.

.. story:: Erledigtes bleibt auffindbar
   :id: STORY_DSH_VORG_04
   :status: approved
   :priority: high
   :implements: REQ_DSH_VORG_08, REQ_DSH_VORG_09

   Als Benutzer finde ich entschiedene, entfallene und zurückgezogene
   Vorgänge in einem zugeklappten Abschnitt wieder, mit Ergebnis, Zeitpunkt
   und -- falls jemand entschieden hat -- der entscheidenden Person.

.. test:: Alle sechs Vorgangsarten erscheinen beim Zuständigen
   :id: TEST_DSH_VORG_01
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_DSH_VORG_01, REQ_DSH_VORG_01, REQ_DSH_VORG_03

   Zu jeder der sechs Vorgangsarten aus :need:`REQ_DSH_VORG_01` wird ein
   offener Vorgang angelegt. Jeder erscheint im ersten Rang des Dashboards
   seines Zuständigen -- und nur dort -- mit Vorgangsart, Antragsteller,
   Bezug und Zeitpunkt sowie den Aktionen Annehmen und Ablehnen. Der Sprung
   führt jeweils zum betroffenen Termin, zur Schulung oder zum Zeitraum.

.. test:: Eine Entscheidung wirkt genau einmal
   :id: TEST_DSH_VORG_02
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_DSH_VORG_01, REQ_DSH_VORG_04

   Ein Administrator nimmt eine Vormerkung an; der Trainer ist danach dem
   Termin zugewiesen, der Vorgang ist aus dem ersten Rang verschwunden und
   steht als angenommen unter den erledigten. Ein zweiter
   Entscheidungsversuch auf denselben Vorgang -- annehmen wie ablehnen --
   wird abgewiesen und ändert weder Zuweisung noch Vorgangshistorie.

.. test:: Eigene Vorgänge erscheinen und lassen sich zurückziehen
   :id: TEST_DSH_VORG_03
   :status: approved
   :automated: yes
   :level: e2e
   :verifies: STORY_DSH_VORG_02, REQ_DSH_VORG_02, REQ_DSH_VORG_07

   Ein Trainer mit je einer offenen Bewerbung, Vormerkung,
   Assistenzbewerbung, einem Abwesenheitsantrag und einer gestellten
   Übernahmeanfrage sowie einer Ersatztrainer-Anfrage sieht alle sechs
   getrennt von den an ihn gerichteten Vorgängen. Er zieht die ersten fünf
   aus dem Dashboard zurück; danach ist keiner dieser fünf mehr offen, jeder
   steht als zurückgezogen unter den erledigten, und es ist zu keinem eine
   Mitteilung entstanden. Die nicht
   zurückziehbare Ersatztrainer-Anfrage bleibt offen sichtbar.

.. test:: Begründung ist nur bei zwei Vorgangsarten Pflicht
   :id: TEST_DSH_VORG_04
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_DSH_VORG_03, REQ_DSH_VORG_05

   Eine Ablehnung ohne Begründung wird bei Freigabeanfrage und Vormerkung
   abgewiesen und bei Abwesenheitsantrag, Assistenzbewerbung und
   Übernahmeanfrage sowie Ersatztrainer-Anfrage angenommen. Eine Annahme
   verlangt in keinem Fall eine Begründung, und die Begründung erreicht den
   Antragsteller mit seiner Mitteilung.

.. test:: Ein Vorgang mit zwei Zuständigen entfällt bei beiden
   :id: TEST_DSH_VORG_05
   :status: approved
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
   :status: approved
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
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_DSH_VORG_04, REQ_DSH_VORG_09

   Für die ersten fünf Fälle aus :need:`REQ_DSH_VORG_09` -- Zuweisung eines
   anderen Trainers, Tausch durch Übernahme, Archivieren einer Schulung,
   Fristablauf beim Abwesenheitsantrag und Fristablauf bei der
   Ersatztrainer-Anfrage -- endet der betroffene Vorgang als erledigt. Das
   Ergebnis nennt jeweils das auslösende Ereignis und keine entscheidende
   Person. Nach dem Ablauf der Ersatztrainer-Anfrage ist der daraus
   entstandene Abwesenheitsantrag offen.

   Dasselbe gilt, wenn die Annahme einer Ersatztrainer-Anfrage an der
   erneuten fachlichen Prüfung scheitert: Der konkrete Prüfgrund steht am
   erledigten Vorgang und der daraus entstandene Abwesenheitsantrag ist
   offen.

   Zusätzlich werden bei Absage und bei Löschung eines Termins dessen offene
   Vormerkung, Assistenzbewerbung und Übernahmeanfrage jeweils als entfallen
   erledigt; keine davon lässt sich danach noch entscheiden.

   Betrifft ein Abwesenheitsantrag oder eine Ersatztrainer-Anfrage zwei
   Termine, entfernt Absage oder Löschung des ersten nur diesen Termin. Nach
   demselben Ereignis am letzten Termin endet der Vorgang als entfallen und
   die Abwesenheit ist aktiv.

   Wird ein adressierter Trainer stillgelegt oder gelöscht, endet eine an ihn
   gerichtete Übernahmeanfrage als entfallen. Eine an ihn gerichtete
   Ersatztrainer-Anfrage endet ebenfalls; zugleich ist der daraus entstandene
   Abwesenheitsantrag offen. Die alten Vorgänge lassen sich nicht mehr
   entscheiden.

   Wird ein Antragsteller stillgelegt oder gelöscht, enden je ein offener
   Vorgang aller sechs Arten als entfallen und bleiben mit dem Kontoende als
   Grund historisiert. Keiner lässt sich anschließend noch entscheiden.

.. test:: Jede Entscheidung erreicht den Antragsteller
   :id: TEST_DSH_VORG_08
   :status: approved
   :automated: yes
   :level: integration
   :verifies: REQ_DSH_VORG_10, REQ_NAC_ANL_04

   Für jede der sechs Vorgangsarten wird je eine Annahme und Ablehnung
   herbeigeführt; nach jeder Entscheidung liegt beim Antragsteller genau eine
   Mitteilung mit dem Ergebnis vor. Dasselbe gilt für jeden Entfall aus
   :need:`REQ_DSH_VORG_09` und für die manuelle Genehmigung eines
   Abwesenheitsantrags. Beim Entscheider entsteht keine; entscheidet ein
   Administrator seinen eigenen Vorgang, entsteht auch für ihn als
   Antragsteller keine Mitteilung.

   Bei Stilllegung des Antragstellers entstehen dieselben Mitteilungen nur,
   wenn ein anderer Administrator sie ausgelöst hat; bei Selbststilllegung
   entsteht keine. Wird das Konto gelöscht, bleibt jeder entfallene Vorgang
   historisiert, aber keine persönliche Mitteilung des Antragstellers
   erhalten.

.. test:: Dashboard schützt Vorgänge und Mutationen
   :id: TEST_DSH_VORG_09
   :status: approved
   :automated: yes
   :level: integration
   :verifies: REQ_DSH_SICHER_01

   Ohne Anmeldung sind Dashboard und Vorgangsschnittstellen nicht
   erreichbar. Ein Trainer kann weder einen fremden Vorgang zurückziehen
   noch einen Vorgang entscheiden, für den er nicht zuständig ist; ein
   direkter Aufruf mit fremder Vorgangs-ID wird mit demselben Status und
   Fehlerkörper wie der Aufruf einer nicht existierenden ID abgewiesen. Ein
   Administrator kann weder eine Übernahmeanfrage noch eine
   Ersatztrainer-Anfrage entscheiden, wenn er nicht selbst der jeweils
   adressierte Trainer ist. Annehmen, Ablehnen und Zurückziehen werden ohne
   gültigen CSRF-Schutz abgewiesen und lassen Vorgang, fachliche Daten und
   Mitteilungen unverändert.

.. test:: Dashboard ist mit Tastatur und Statusnamen bedienbar
   :id: TEST_DSH_GRUND_03
   :status: approved
   :automated: yes
   :level: e2e
   :verifies: REQ_DSH_GRUND_03

   Alle drei Ränge, Dringlichkeiten sowie offene und erledigte Zustände
   besitzen programmatisch ermittelbare Namen und sind nicht allein durch
   Farbe unterscheidbar. Mit der Tastatur lassen sich der Abschnitt der
   erledigten Vorgänge öffnen und schließen sowie je ein Vorgang annehmen,
   ablehnen und zurückziehen; nach jeder Aktion liegt der Fokus am geänderten
   Vorgang oder an seiner Statusmeldung.

Handlungspflichten und Dringlichkeiten
--------------------------------------

.. story:: Meine überfälligen Termine gehen mir nicht verloren
   :id: STORY_DSH_PFLI_01
   :status: approved
   :priority: high
   :implements: REQ_DSH_PFLI_01

   Als Trainer finde ich meine überfälligen geplanten Termine im zweiten
   Rang des Dashboards und kann ihre Durchführung von dort bestätigen.

.. story:: Als Administrator sehe ich, wo es eng wird
   :id: STORY_DSH_DRIN_01
   :status: approved
   :priority: high
   :implements: REQ_DSH_DRIN_01, REQ_DSH_DRIN_02, REQ_DSH_DRIN_03, REQ_DSH_DRIN_04

   Als Administrator sehe ich im dritten Rang die zukünftigen geplanten
   Termine ohne Trainer, aufsteigend sortiert und ab weniger als vier Wochen
   Vorlauf als dringend markiert, die überfälligen davor und die exklusiven
   Termine unter ihrer Mindestteilnehmerzahl ebenfalls als dringend.

.. test:: Überfällige eigene Termine stehen im zweiten Rang
   :id: TEST_DSH_PFLI_01
   :status: approved
   :automated: yes
   :level: e2e
   :verifies: STORY_DSH_PFLI_01, REQ_DSH_PFLI_01

   Ein Trainer mit einem überfälligen geplanten, einem anstehenden, einem
   abgesagten und einem abgeschlossenen eigenen Termin sieht im zweiten Rang
   nur den überfälligen und bestätigt von dort dessen Durchführung.

.. test:: Termine ohne Trainer erscheinen sortiert und markiert
   :id: TEST_DSH_DRIN_01
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_DSH_DRIN_01, REQ_DSH_DRIN_01, REQ_DSH_DRIN_03

   Das Dashboard eines Administrators führt alle zukünftigen geplanten
   Termine ohne Trainerzuweisung aufsteigend nach Startdatum. Ein Termin
   mit weniger als vier Wochen Vorlauf ist als dringend markiert, einer mit
   genau vier Wochen erscheint ohne Markierung.

.. test:: Überfällige Termine stehen vor den anstehenden
   :id: TEST_DSH_DRIN_02
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_DSH_DRIN_01, REQ_DSH_DRIN_02

   Im Dashboard eines Administrators stehen überfällige geplante Termine vor
   den anstehenden. Abgesagte und abgeschlossene Termine erscheinen nicht.

.. test:: Zu wenige Teilnehmer machen einen exklusiven Termin dringend
   :id: TEST_DSH_DRIN_03
   :status: approved
   :automated: yes
   :level: integration
   :verifies: STORY_DSH_DRIN_01, REQ_DSH_DRIN_04

   Ein exklusiver Termin unter seiner Mindestteilnehmerzahl mit weniger als
   vier Wochen Vorlauf ist als dringend markiert. Ein öffentlicher Termin
   und ein Termin ohne Zugangsart erhalten diese Markierung nicht. Ein
   öffentlicher Termin oberhalb seiner Höchstteilnehmerzahl zeigt stattdessen
   die dafür vorgesehene Warnung; bei eingehaltener Höchstzahl fehlt sie.
