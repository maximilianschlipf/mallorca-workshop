Abwesenheiten
=============

Eine Abwesenheit ist ein Zeitraum, in dem ein Trainer nicht für Termine zur
Verfügung steht. Beim Eintragen wird geprüft, ob der Zeitraum mit Terminen
kollidiert -- und das Ergebnis dieser Prüfung entscheidet über den weiteren
Ablauf.

Erfassung
---------

.. req:: Abwesenheit eintragen
   :id: REQ_ABW_ERF_01
   :status: draft
   :priority: high

   Ein Trainer trägt für sich selbst eine Abwesenheit mit Zeitraum ein. Er
   kann Abwesenheiten nur für sich selbst erfassen.

.. req:: Mindestvorlauf von einer Woche
   :id: REQ_ABW_ERF_07
   :status: draft
   :priority: high
   :links: REQ_ABW_ERF_01

   Eine Abwesenheit muss mindestens eine Woche vor ihrem Beginn eingetragen
   werden. Ein Zeitraum, der früher beginnt, wird abgewiesen.

   Der Planer ist kein Werkzeug für Krankmeldungen. Fällt ein Trainer
   kurzfristig aus, klärt der Administrator das auf anderem Weg. Der
   Mindestvorlauf sorgt zugleich dafür, dass die Frist aus
   :need:`REQ_ABW_AUTO_01` immer greifen kann -- sonst läge ihr Stichtag bei
   kurzfristigen Anträgen bereits in der Vergangenheit.

.. req:: Grund ist freiwillig
   :id: REQ_ABW_ERF_02
   :status: draft
   :links: REQ_ABW_ERF_01

   Zu einer Abwesenheit kann ein Grund angegeben werden. Die Angabe ist
   freiwillig; eine Abwesenheit ohne Grund ist gültig und wird nicht anders
   behandelt.

.. req:: Grund ist für Administratoren sichtbar
   :id: REQ_ABW_ERF_08
   :status: draft
   :priority: high
   :links: REQ_ABW_ERF_02

   Der angegebene Grund ist für alle Administratoren sichtbar; sie
   entscheiden anhand dessen über den Antrag. Beim Eintragen wird darauf
   hingewiesen.

.. req:: Keine Gesundheitsdaten im Grund
   :id: REQ_ABW_ERF_09
   :status: draft
   :priority: high
   :links: REQ_ABW_ERF_08

   Der Grund ist eine kurze Einordnung wie "abwesend" oder "verhindert",
   keine Erhebung von Gesundheitsdaten. Darauf wird beim Eintragen
   hingewiesen; Näheres klären Trainer und Administrator außerhalb des
   Systems.

.. req:: Abwesenheit ändern
   :id: REQ_ABW_ERF_03
   :status: draft
   :priority: high
   :links: REQ_ABW_ERF_01

   Ein Trainer kann eine eigene Abwesenheit ändern. Der geänderte Zeitraum
   durchläuft die Konfliktprüfung erneut, als wäre er neu eingetragen
   worden.

.. req:: Abwesenheit löschen
   :id: REQ_ABW_ERF_04
   :status: draft
   :links: REQ_ABW_ERF_01

   Ein Trainer kann eine eigene Abwesenheit löschen. Danach gilt er im
   betroffenen Zeitraum wieder als verfügbar.

.. req:: Löschen stellt keine Zuweisung wieder her
   :id: REQ_ABW_ERF_05
   :status: draft
   :priority: high
   :links: REQ_ABW_ERF_04

   Wurde wegen einer Abwesenheit eine Trainerzuweisung aufgehoben, entsteht
   sie durch das Löschen der Abwesenheit nicht wieder. Der Termin bleibt
   unbesetzt, bis ein Administrator ihn neu vergibt.

.. req:: Antrag zurückziehen
   :id: REQ_ABW_ERF_06
   :status: draft
   :links: REQ_ABW_ERF_01

   Ein Trainer kann einen eigenen, noch nicht entschiedenen
   Abwesenheitsantrag zurückziehen.

Konfliktprüfung
---------------

.. req:: Konfliktprüfung beim Eintragen
   :id: REQ_ABW_PRUEF_01
   :status: draft
   :priority: high
   :links: REQ_ABW_ERF_01

   Beim Eintragen einer Abwesenheit prüft das System alle zukünftigen
   Termine im angegebenen Zeitraum auf zwei Arten von Konflikt:

   * **Zuweisungskonflikt** -- der Trainer ist einem Termin im Zeitraum
     zugewiesen, als ausführender Trainer oder als Assistent.
   * **Verfügbarkeitskonflikt** -- im Zeitraum liegt ein Termin, dessen
     Schulung der Trainer halten könnte, dem er aber nicht zugewiesen ist.

Die Konfliktprüfung ist der Angelpunkt des Bereichs -- beide Abläufe hängen an ihr:

.. needflow::
   :root_id: REQ_ABW_PRUEF_01
   :root_depth: 1
   :direction: LR

.. req:: Teilweise Überschneidung genügt
   :id: REQ_ABW_PRUEF_02
   :status: draft
   :priority: high
   :links: REQ_ABW_PRUEF_01

   Ein Konflikt liegt bereits vor, wenn die Abwesenheit den Termin nur an
   einem Tag überschneidet. Ein Trainer, der einen Tag eines dreitägigen
   Termins fehlt, kann ihn nicht halten.

Ablauf ohne Konflikt
--------------------

.. req:: Konfliktfreie Abwesenheit gilt sofort
   :id: REQ_ABW_KONFL_01
   :status: draft
   :links: REQ_ABW_PRUEF_01

   Liegt kein Zuweisungskonflikt vor, wird die Abwesenheit ohne Genehmigung
   eingetragen und ist unmittelbar aktiv.

Ablauf bei Zuweisungskonflikt
-----------------------------

.. req:: Zuweisungskonflikt ist genehmigungspflichtig
   :id: REQ_ABW_KONFL_02
   :status: draft
   :priority: high
   :links: REQ_ABW_PRUEF_01

   Liegt ein Zuweisungskonflikt vor, wird die Abwesenheit als Antrag erfasst
   und muss von einem Administrator entschieden werden.

Was an der Genehmigungspflicht hängt:

.. needflow::
   :root_id: REQ_ABW_KONFL_02
   :root_depth: 1
   :direction: LR

.. req:: Ein Antrag wird als Ganzes entschieden
   :id: REQ_ABW_KONFL_06
   :status: draft
   :priority: high
   :links: REQ_ABW_KONFL_02

   Kollidiert eine Abwesenheit mit mehreren Terminen, wird der Antrag
   trotzdem in einem Stück entschieden -- ganz oder gar nicht. Es gibt keine
   Entscheidung je Termin.

   Soll ein einzelner Termin doch gehalten werden, passt der Trainer seine
   Abwesenheit an; die Absprache darüber findet außerhalb des Systems statt.

.. req:: Antrag gilt bis zur Entscheidung nicht
   :id: REQ_ABW_KONFL_03
   :status: draft
   :links: REQ_ABW_KONFL_02

   Solange ein Abwesenheitsantrag nicht entschieden ist, gilt die Abwesenheit
   nicht: Der Trainer bleibt dem Termin zugewiesen und gilt als verfügbar.

.. req:: Genehmigung hebt die Trainerzuweisung auf
   :id: REQ_ABW_KONFL_04
   :status: draft
   :priority: high
   :links: REQ_ABW_KONFL_02, REQ_ABW_PRUEF_02

   Genehmigt ein Administrator den Antrag, wird die Abwesenheit aktiv und
   die Zuweisungen aller kollidierenden Termine entfallen. Das gilt auch,
   wenn die Abwesenheit einen Termin nur teilweise überschneidet; die
   Termine wechseln in den Zustand "nicht zugewiesen".

.. req:: Ablehnung lässt die Zuweisung bestehen
   :id: REQ_ABW_KONFL_05
   :status: draft
   :links: REQ_ABW_KONFL_02

   Lehnt ein Administrator den Antrag ab, wird die Abwesenheit nicht aktiv
   und die Trainerzuweisung bleibt unverändert bestehen. Der Trainer wird
   über die Ablehnung benachrichtigt.

.. req:: Administrator entscheidet auch über eigene Anträge
   :id: REQ_ABW_KONFL_07
   :status: draft
   :links: REQ_ABW_KONFL_02, DEC_USR_SELBST_01

   Ein Administrator darf einen Abwesenheitsantrag entscheiden, den er
   selbst gestellt hat.

Unentschiedene Anträge
----------------------

.. req:: Genehmigung eine Woche vor Beginn
   :id: REQ_ABW_AUTO_01
   :status: draft
   :priority: high
   :links: REQ_ABW_KONFL_03

   Ist ein Abwesenheitsantrag eine Woche vor Beginn des beantragten
   Zeitraums noch nicht entschieden, gilt er als genehmigt. Die Abwesenheit
   wird aktiv, und die kollidierenden Termine werden frei.

   Im Zweifel für die Abwesenheit: Ein Trainer, dessen Antrag niemand
   beantwortet, wäre sonst weiterhin eingeplant, obwohl er nicht kommt. Ein
   Termin, der eine Woche vorher unbesetzt ist, lässt sich noch vergeben --
   ein Trainer, der am Terminmorgen fehlt, nicht ersetzen.

.. req:: Beteiligte werden über die Genehmigung nach Frist informiert
   :id: REQ_ABW_AUTO_02
   :status: draft
   :priority: high
   :links: REQ_ABW_AUTO_01

   Wird ein Antrag durch Fristablauf genehmigt, werden der Trainer und die
   Administratoren benachrichtigt. Die frei gewordenen Termine erscheinen
   danach unter den Terminen ohne Trainer.

Ablauf bei Verfügbarkeitskonflikt
---------------------------------

.. req:: Hinweis an den Trainer
   :id: REQ_ABW_HINW_01
   :status: draft
   :links: REQ_ABW_PRUEF_01

   Liegt ausschließlich ein Verfügbarkeitskonflikt vor, wird die Abwesenheit
   ohne Genehmigung eingetragen. Dem Trainer wird beim Eintragen angezeigt,
   welche Termine er durch die Abwesenheit nicht mehr übernehmen kann.

.. req:: Information an die Administratoren
   :id: REQ_ABW_HINW_02
   :status: draft
   :links: REQ_ABW_HINW_01

   Bei einem Verfügbarkeitskonflikt werden die Administratoren darüber
   informiert, dass ein für diese Termine in Frage kommender Trainer im
   Zeitraum nicht zur Verfügung steht.

Offene Vorgänge des Trainers
----------------------------

.. req:: Betroffene Vorgänge werden geschlossen
   :id: REQ_ABW_VORG_01
   :status: draft
   :priority: high
   :links: REQ_ABW_ERF_01

   Wird eine Abwesenheit aktiv, werden alle offenen Vormerkungen und
   Übernahmeanfragen des Trainers zu Terminen in diesem Zeitraum
   geschlossen. Sie könnten ohnehin nicht mehr zu einer Zuweisung führen.

.. req:: Geschlossene Vorgänge werden dem Trainer gezeigt
   :id: REQ_ABW_VORG_02
   :status: draft
   :priority: high
   :links: REQ_ABW_VORG_01, REQ_ABW_HINW_01

   Dem Trainer wird deutlich angezeigt, welche seiner Vormerkungen und
   Übernahmeanfragen durch die Abwesenheit entfallen sind -- ebenso
   hervorgehoben wie die Termine, die er nicht mehr übernehmen kann.

.. req:: Offene Vorgänge sperren die Abwesenheit nicht
   :id: REQ_ABW_VORG_03
   :status: draft
   :priority: high
   :links: REQ_ABW_VORG_01

   Offene Vormerkungen oder Übernahmeanfragen im Zeitraum machen eine
   Abwesenheit weder genehmigungspflichtig noch verhindern sie deren
   Eintragen. Der Hinweis darauf ist rein informativ.

Übersicht für Administratoren
-----------------------------

.. req:: Abwesenheiten in der Administratorenübersicht
   :id: REQ_ABW_UEBER_01
   :status: draft
   :links: REQ_ABW_KONFL_02, REQ_ABW_HINW_02

   Administratoren sehen in ihrer Übersicht die offenen Abwesenheitsanträge
   sowie die Informationen zu Verfügbarkeitskonflikten. Offene Anträge sind
   von reinen Informationen unterscheidbar, da nur Erstere eine Entscheidung
   erfordern.

.. req:: Offene Anträge zeigen ihre Restfrist
   :id: REQ_ABW_UEBER_02
   :status: draft
   :priority: high
   :links: REQ_ABW_UEBER_01, REQ_ABW_AUTO_01

   Zu jedem offenen Abwesenheitsantrag ist erkennbar, wie lange er noch
   entschieden werden kann, bevor er durch Fristablauf genehmigt wird.
