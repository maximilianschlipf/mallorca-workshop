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
   :status: approved
   :priority: high

   Ein Trainer trägt für sich selbst eine Abwesenheit mit Zeitraum ein. Er
   kann Abwesenheiten nur für sich selbst erfassen.

.. req:: Mindestvorlauf von einer Woche
   :id: REQ_ABW_ERF_07
   :status: approved
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
   :status: approved
   :links: REQ_ABW_ERF_01

   Zu einer Abwesenheit kann ein Grund angegeben werden. Die Angabe ist
   freiwillig; eine Abwesenheit ohne Grund ist gültig und wird nicht anders
   behandelt.

.. req:: Grund ist für Administratoren sichtbar
   :id: REQ_ABW_ERF_08
   :status: approved
   :priority: high
   :links: REQ_ABW_ERF_02

   Der angegebene Grund ist für alle Administratoren sichtbar; sie
   entscheiden anhand dessen über den Antrag. Beim Eintragen wird darauf
   hingewiesen.

.. req:: Keine Gesundheitsdaten im Grund
   :id: REQ_ABW_ERF_09
   :status: approved
   :priority: high
   :links: REQ_ABW_ERF_08

   Der Grund ist eine kurze Einordnung wie "abwesend" oder "verhindert",
   keine Erhebung von Gesundheitsdaten. Darauf wird beim Eintragen
   hingewiesen; Näheres klären Trainer und Administrator außerhalb des
   Systems.

.. req:: Abwesenheit ändern
   :id: REQ_ABW_ERF_03
   :status: approved
   :priority: high
   :links: REQ_ABW_ERF_01

   Ein Trainer kann Zeitraum und Grund einer eigenen Abwesenheit ändern.

.. req:: Abwesenheit löschen
   :id: REQ_ABW_ERF_04
   :status: approved
   :links: REQ_ABW_ERF_01

   Ein Trainer kann eine eigene Abwesenheit löschen. Danach gilt er im
   betroffenen Zeitraum wieder als verfügbar.

.. req:: Löschen stellt keine Zuweisung wieder her
   :id: REQ_ABW_ERF_05
   :status: approved
   :priority: high
   :links: REQ_ABW_ERF_04

   Wurde wegen einer Abwesenheit eine Trainerzuweisung aufgehoben, entsteht
   sie durch das Löschen der Abwesenheit nicht wieder. Der Termin bleibt
   unbesetzt, bis ein Administrator ihn neu vergibt.

.. req:: Antrag zurückziehen
   :id: REQ_ABW_ERF_06
   :status: approved
   :links: REQ_ABW_ERF_01

   Ein Trainer kann einen eigenen, noch nicht entschiedenen
   Abwesenheitsantrag zurückziehen.

Konfliktprüfung
---------------

.. req:: Konfliktprüfung nach einer Änderung
   :id: REQ_ABW_PRUEF_04
   :status: approved
   :priority: high
   :links: REQ_ABW_ERF_03, REQ_ABW_PRUEF_01, REQ_ABW_PRUEF_03

   Wird der Zeitraum einer Abwesenheit geändert, durchläuft der neue Zeitraum
   dieselbe Konfliktprüfung wie beim Eintragen einer Abwesenheit.

.. req:: Zuweisungskonflikt beim Eintragen erkannt
   :id: REQ_ABW_PRUEF_01
   :status: approved
   :priority: high
   :links: REQ_ABW_ERF_01

   Beim Eintragen einer Abwesenheit prüft das System alle zukünftigen
   Termine im angegebenen Zeitraum: Ein Zuweisungskonflikt liegt vor, wenn
   der Trainer einem Termin im Zeitraum zugewiesen ist -- als ausführender
   Trainer oder als Assistent.

Die Konfliktprüfung ist der Angelpunkt des Bereichs -- daran hängt der
weitere Ablauf:

.. needflow::
   :root_id: REQ_ABW_PRUEF_01
   :root_depth: 1
   :direction: LR

.. req:: Verfügbarkeitskonflikt beim Eintragen erkannt
   :id: REQ_ABW_PRUEF_03
   :status: approved
   :priority: high
   :links: REQ_ABW_ERF_01

   Zusätzlich zum Zuweisungskonflikt prüft das System auf einen
   Verfügbarkeitskonflikt: Im Zeitraum liegt ein Termin, dessen Schulung der
   Trainer halten könnte, dem er aber nicht zugewiesen ist.

.. req:: Teilweise Überschneidung genügt
   :id: REQ_ABW_PRUEF_02
   :status: approved
   :priority: high
   :links: REQ_ABW_PRUEF_01, REQ_ABW_PRUEF_03

   Ein Konflikt -- Zuweisung wie Verfügbarkeit -- liegt bereits vor, wenn die
   Abwesenheit den Termin nur an einem Tag überschneidet. Ein Trainer, der
   einen Tag eines dreitägigen Termins fehlt, kann ihn nicht halten.

Ablauf ohne Konflikt
--------------------

.. req:: Konfliktfreie Abwesenheit gilt sofort
   :id: REQ_ABW_KONFL_01
   :status: approved
   :links: REQ_ABW_PRUEF_01

   Liegt kein Zuweisungskonflikt vor, wird die Abwesenheit ohne Genehmigung
   eingetragen und ist unmittelbar aktiv.

Ablauf bei Zuweisungskonflikt
-----------------------------

.. req:: Zuweisungskonflikt ist genehmigungspflichtig
   :id: REQ_ABW_KONFL_02
   :status: approved
   :priority: high
   :links: REQ_ABW_PRUEF_01

   Liegt ein Zuweisungskonflikt vor, wird die Abwesenheit als Antrag erfasst
   und muss von einem Administrator entschieden werden -- es sei denn, der
   Trainer schlägt stattdessen einen Ersatztrainer vor
   (:need:`REQ_ABW_TAUSCH_01`).

Was an der Genehmigungspflicht hängt:

.. needflow::
   :root_id: REQ_ABW_KONFL_02
   :root_depth: 1
   :direction: LR

.. req:: Ein Antrag wird als Ganzes entschieden
   :id: REQ_ABW_KONFL_06
   :status: approved
   :priority: high
   :links: REQ_ABW_KONFL_02

   Kollidiert eine Abwesenheit mit mehreren Terminen, wird der Antrag
   trotzdem in einem Stück entschieden -- ganz oder gar nicht. Es gibt keine
   Entscheidung je Termin.

   Soll ein einzelner Termin doch gehalten werden, passt der Trainer seine
   Abwesenheit an; die Absprache darüber findet außerhalb des Systems statt.
   Ebenso kann ein Trainer seine Abwesenheit von vornherein in mehrere
   Zeiträume teilen, um für sie getrennte Anträge -- oder getrennte
   Tauschanfragen nach :need:`REQ_ABW_TAUSCH_01` -- zu stellen. Der Planer
   ist eine Planungshilfe, kein Kommunikationswerkzeug: Wer abstimmen will,
   wer welchen Termin übernimmt, tut das in der Trainerkonferenz oder direkt
   mit dem Administrator.

.. req:: Antrag gilt bis zur Entscheidung nicht
   :id: REQ_ABW_KONFL_03
   :status: approved
   :links: REQ_ABW_KONFL_02

   Solange ein Abwesenheitsantrag nicht entschieden ist, gilt die Abwesenheit
   nicht: Der Trainer bleibt dem Termin zugewiesen und gilt als verfügbar.
   Das gilt ebenso für die Zeit, in der stattdessen eine Tauschanfrage nach
   :need:`REQ_ABW_TAUSCH_01` offen ist.

.. req:: Genehmigung hebt die Trainerzuweisung auf
   :id: REQ_ABW_KONFL_04
   :status: approved
   :priority: high
   :links: REQ_ABW_KONFL_02, REQ_ABW_PRUEF_02

   Genehmigt ein Administrator den Antrag, wird die Abwesenheit aktiv und
   die Zuweisungen aller kollidierenden Termine entfallen. Das gilt auch,
   wenn die Abwesenheit einen Termin nur teilweise überschneidet; die
   Termine wechseln in den Zustand "nicht zugewiesen".

.. req:: Ablehnung lässt die Zuweisung bestehen
   :id: REQ_ABW_KONFL_05
   :status: approved
   :links: REQ_ABW_KONFL_02

   Lehnt ein Administrator den Antrag ab, wird die Abwesenheit nicht aktiv
   und die Trainerzuweisung bleibt unverändert bestehen. Der Trainer wird
   über die Ablehnung benachrichtigt.

.. req:: Administrator entscheidet auch über eigene Anträge
   :id: REQ_ABW_KONFL_07
   :status: approved
   :links: REQ_ABW_KONFL_02, DEC_USR_SELBST_01

   Ein Administrator darf einen Abwesenheitsantrag entscheiden, den er
   selbst gestellt hat.

Unentschiedene Anträge
----------------------

.. req:: Genehmigung eine Woche vor Beginn
   :id: REQ_ABW_AUTO_01
   :status: approved
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
   :status: approved
   :priority: high
   :links: REQ_ABW_AUTO_01

   Wird ein Antrag durch Fristablauf genehmigt, werden der Trainer und die
   Administratoren benachrichtigt. Die frei gewordenen Termine erscheinen
   danach unter den Terminen ohne Trainer.

Ablauf bei Verfügbarkeitskonflikt
---------------------------------

.. req:: Hinweis an den Trainer
   :id: REQ_ABW_HINW_01
   :status: approved
   :links: REQ_ABW_PRUEF_03

   Liegt ausschließlich ein Verfügbarkeitskonflikt vor, wird die Abwesenheit
   ohne Genehmigung eingetragen. Dem Trainer wird beim Eintragen angezeigt,
   welche Termine er durch die Abwesenheit nicht mehr übernehmen kann.

.. req:: Information an die Administratoren
   :id: REQ_ABW_HINW_02
   :status: approved
   :links: REQ_ABW_HINW_01

   Bei einem Verfügbarkeitskonflikt werden die Administratoren darüber
   informiert, dass ein für diese Termine in Frage kommender Trainer im
   Zeitraum nicht zur Verfügung steht.

Tausch mit einem Ersatztrainer
-------------------------------

Statt einen Genehmigungsantrag auszulösen, kann ein Trainer bei einem
Zuweisungskonflikt einen bereits qualifizierten Ersatztrainer vorschlagen.

.. req:: Ersatztrainer statt Genehmigungsantrag
   :id: REQ_ABW_TAUSCH_01
   :status: approved
   :priority: high
   :links: REQ_ABW_KONFL_02

   Liegt ein Zuweisungskonflikt vor, kann der Trainer beim Eintragen der
   Abwesenheit statt eines Genehmigungsantrags einen Ersatztrainer
   vorschlagen. Zur Auswahl stehen nur Trainer, die für **alle** von der
   Abwesenheit betroffenen Schulungen bereits qualifiziert sind -- der
   Antrag wird nach :need:`REQ_ABW_KONFL_06` als Ganzes entschieden, also
   auch als Ganzes übernommen.

.. req:: Tausch braucht zwei Wochen Vorlauf
   :id: REQ_ABW_TAUSCH_02
   :status: approved
   :priority: high
   :links: REQ_ABW_TAUSCH_01, REQ_ABW_ERF_07

   Die Tausch-Option steht nur zur Verfügung, wenn der Abwesenheitszeitraum
   mindestens zwei Wochen vor seinem Beginn eingetragen wird -- eine Woche
   mehr als der allgemeine Mindestvorlauf aus :need:`REQ_ABW_ERF_07`. Bei
   kürzerem Vorlauf steht nur der reguläre Genehmigungsantrag nach
   :need:`REQ_ABW_KONFL_02` zur Verfügung.

   Die zusätzliche Woche ist Absicht: Reagiert der Ersatztrainer nicht
   innerhalb einer Woche (:need:`REQ_ABW_TAUSCH_05`), bleibt bis zur
   automatischen Genehmigung nach :need:`REQ_ABW_AUTO_01` noch genau die
   reguläre Woche Frist übrig. Ein kurzfristiger Tausch würde diesen Puffer
   aufzehren und den Zustand unnötig lange in der Schwebe halten -- dafür
   ist der reguläre Genehmigungsweg da.

.. req:: Anfrage an den Ersatztrainer
   :id: REQ_ABW_TAUSCH_03
   :status: approved
   :links: REQ_ABW_TAUSCH_01

   Der ausgewählte Ersatztrainer erhält eine Anfrage, die betroffenen
   Termine zu übernehmen, und kann sie annehmen oder ablehnen.

.. req:: Annahme weist den Ersatztrainer zu
   :id: REQ_ABW_TAUSCH_04
   :status: approved
   :priority: high
   :links: REQ_ABW_TAUSCH_03

   Nimmt der Ersatztrainer an, wird er den betroffenen Terminen zugewiesen,
   und die Abwesenheit des ursprünglichen Trainers wird aktiv. Es ist keine
   weitere Entscheidung durch einen Administrator nötig.

.. req:: Ablehnung oder Fristablauf führt zur Genehmigung
   :id: REQ_ABW_TAUSCH_05
   :status: approved
   :priority: high
   :links: REQ_ABW_TAUSCH_03, REQ_ABW_KONFL_02

   Lehnt der Ersatztrainer ab, oder reagiert er eine Woche nach der Anfrage
   nicht, wird daraus ein regulärer Genehmigungsantrag nach
   :need:`REQ_ABW_KONFL_02`. Der ursprüngliche Trainer wird über die
   Ablehnung beziehungsweise den Fristablauf informiert; danach unterscheidet
   sich der weitere Ablauf nicht mehr von einem von Anfang an gestellten
   Genehmigungsantrag.

Offene Vorgänge des Trainers
----------------------------

Vormerkungen und Übernahmeanfragen zu Terminen gibt es im Planer noch nicht;
was mit ihnen beim Eintreten einer Abwesenheit geschieht, gehört in die
Anforderungen dieser Bereiche, sobald sie entstehen.

Übersicht für Administratoren
-----------------------------

.. req:: Abwesenheiten in der Administratorenübersicht
   :id: REQ_ABW_UEBER_01
   :status: approved
   :links: REQ_ABW_KONFL_02, REQ_ABW_HINW_02

   Administratoren sehen in ihrer Übersicht die offenen Abwesenheitsanträge
   sowie die Informationen zu Verfügbarkeitskonflikten. Offene Anträge sind
   von reinen Informationen unterscheidbar, da nur Erstere eine Entscheidung
   erfordern.

.. req:: Offene Anträge zeigen ihre Restfrist
   :id: REQ_ABW_UEBER_02
   :status: approved
   :priority: high
   :links: REQ_ABW_UEBER_01, REQ_ABW_AUTO_01

   Zu jedem offenen Abwesenheitsantrag ist erkennbar, wie lange er noch
   entschieden werden kann, bevor er durch Fristablauf genehmigt wird.

.. req:: Offene Tauschanfragen sind rein informativ
   :id: REQ_ABW_UEBER_03
   :status: approved
   :links: REQ_ABW_UEBER_01, REQ_ABW_TAUSCH_01

   Administratoren sehen eine offene Tauschanfrage in ihrer Übersicht, klar
   unterscheidbar von den offenen Genehmigungsanträgen. Von ihr geht kein
   Handlungsbedarf aus -- solange sie offen ist, muss kein Administrator
   reagieren. Erst wenn sie nach :need:`REQ_ABW_TAUSCH_05` in einen
   Genehmigungsantrag übergeht, erscheint sie unter den offenen Anträgen.
