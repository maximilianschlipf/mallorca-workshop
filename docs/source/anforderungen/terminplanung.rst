Terminplanung
=============

Ein Termin ist die konkret geplante Durchführung einer Schulung. Termine
plant ausschließlich ein Administrator.

Termine anlegen
---------------

.. req:: Termin für aktive Schulung anlegen
   :id: REQ_TER_ANL_01
   :status: draft
   :priority: high

   Ein Administrator legt zu einer aktiven Schulung einen Termin an. Dazu
   gehören Zeitraum, Ort, Zugangsart und Durchführungsart.

.. req:: Termin ohne Trainerzuweisung speicherbar
   :id: REQ_TER_ANL_02
   :status: draft
   :priority: high
   :links: REQ_TER_ANL_01

   Ein Termin kann ohne zugewiesenen Trainer gespeichert werden. Er gilt
   dann als nicht zugewiesen.

Zugangsart und Durchführungsart
-------------------------------

.. req:: Zugangsart eines Termins
   :id: REQ_TER_FORM_01
   :status: draft
   :priority: high
   :links: REQ_TER_ANL_01

   Ein Termin ist entweder **öffentlich** -- einzeln buchbar für Teilnehmer
   verschiedener Firmen -- oder **exklusiv** für eine Firma.

.. req:: Durchführungsart eines Termins
   :id: REQ_TER_FORM_02
   :status: draft
   :priority: high
   :links: REQ_TER_ANL_01

   Ein Termin wird auf eine von drei Arten durchgeführt:

   * **remote** -- online, ohne gemeinsamen Ort,
   * **vor Ort** -- in den eigenen Räumen,
   * **beim Kunden** -- in den Räumen des Kunden.

.. req:: Zugangsart und Durchführungsart sind frei kombinierbar
   :id: REQ_TER_FORM_03
   :status: draft
   :priority: high
   :links: REQ_TER_FORM_01, REQ_TER_FORM_02

   Jede Zugangsart lässt sich mit jeder Durchführungsart verbinden. Es gibt
   keine verbotenen Kombinationen.

   "Beim Kunden" tritt üblicherweise mit "exklusiv" auf, aber eine
   öffentliche Schulung in Kundenräumen soll möglich bleiben, ohne dass
   dafür eine Regel geändert werden muss.

.. req:: Ort richtet sich nach der Durchführungsart
   :id: REQ_TER_FORM_04
   :status: draft
   :links: REQ_TER_FORM_02

   Bei "vor Ort" und "beim Kunden" trägt der Termin einen Ort. Bei "remote"
   entfällt die Angabe eines physischen Orts.

.. req:: Zugangsart bestimmt die maßgebliche Teilnehmergrenze
   :id: REQ_TER_FORM_05
   :status: draft
   :priority: high
   :links: REQ_TER_FORM_01, REQ_TLN_GRENZ_03

   Welche Teilnehmergrenze einer Schulung für einen Termin gilt, richtet
   sich nach seiner Zugangsart, nicht nach seiner Durchführungsart.

Zustände eines Termins
----------------------

.. req:: Zustände eines Termins
   :id: REQ_TER_STAT_01
   :status: draft
   :priority: high

   Ein Termin ist **geplant**, **abgeschlossen** oder **abgesagt**. Der
   Zustand ist unabhängig davon, ob ein Trainer zugewiesen ist -- das ist
   eine eigene Eigenschaft.

.. req:: Der ausführende Trainer schließt den Termin ab
   :id: REQ_TER_STAT_02
   :status: draft
   :priority: high
   :links: REQ_TER_STAT_01

   Der zugewiesene Trainer schließt seinen Termin nach der Durchführung ab.
   Das ist der vorgesehene Weg.

.. req:: Administrator kann ersatzweise abschließen
   :id: REQ_TER_STAT_03
   :status: draft
   :links: REQ_TER_STAT_02

   Ein Administrator kann einen Termin ebenfalls abschließen, etwa wenn der
   Trainer es versäumt oder nicht mehr erreichbar ist. Das ist der
   Ausweichweg, nicht der Regelfall.

.. req:: Abschluss erst nach dem Enddatum
   :id: REQ_TER_STAT_04
   :status: draft
   :links: REQ_TER_STAT_01

   Ein Termin kann erst abgeschlossen werden, wenn sein Enddatum erreicht
   ist.

.. req:: Abschlusszeitpunkt wird festgehalten
   :id: REQ_TER_STAT_05
   :status: draft
   :priority: high
   :links: REQ_TER_STAT_01, REQ_TLN_AUFB_01

   Zu einem abgeschlossenen Termin wird festgehalten, wann und durch wen er
   abgeschlossen wurde. Der Zeitpunkt ist der Beginn der Frist, nach der die
   Teilnehmerdetails entfernt werden.

.. req:: Automatischer Abschluss nach zwei Monaten
   :id: REQ_TER_STAT_06
   :status: draft
   :priority: high
   :links: REQ_TER_STAT_01

   Ein Termin, der zwei Monate nach seinem Enddatum noch geplant ist, wird
   selbsttätig abgeschlossen.

   Damit kommt jeder Termin zum Abschluss, auch ein vergessener. Ohne diese
   Regel begänne für ihn nie die Frist, nach der die Teilnehmerdetails
   entfernt werden -- die Namen blieben unbegrenzt liegen.

.. req:: Automatischer Abschluss ist erkennbar
   :id: REQ_TER_STAT_07
   :status: draft
   :priority: high
   :links: REQ_TER_STAT_06, REQ_TER_STAT_05

   Ein selbsttätig abgeschlossener Termin ist als solcher erkennbar. Aus dem
   Eintrag geht hervor, dass ihn niemand bestätigt hat.

   Das ist keine Förmlichkeit: Nach :need:`REQ_TLN_TEIL_03` sind die
   Teilnehmerbuchungen mit dem Abschluss unveränderlich. Bei einem
   selbsttätigen Abschluss hat aber niemand geprüft, wer tatsächlich
   teilgenommen hat -- die eingefrorenen Zahlen sind ungeprüft, und das muss
   sichtbar sein.

.. req:: Selbsttätig abgeschlossene Termine zählen nicht in Auswertungen
   :id: REQ_TER_STAT_08
   :status: draft
   :priority: high
   :links: REQ_TER_STAT_07, REQ_TLN_TEIL_04

   Ein selbsttätig abgeschlossener Termin geht nicht in Auswertungen über
   Teilnehmerzahlen ein. Nur Termine, die ein Trainer oder ein Administrator
   bestätigt hat, liefern belastbare Zahlen.

   Die Daten bleiben erhalten und einsehbar -- sie werden nur nicht
   mitgerechnet. Eine ungeprüfte Zahl in einer Auswertung wäre schlimmer als
   eine fehlende, weil ihr niemand ansieht, dass sie nichts wert ist.

Ändern, absagen, löschen
------------------------

.. req:: Termin verschieben
   :id: REQ_TER_AEND_01
   :status: draft
   :priority: high
   :links: REQ_TER_ANL_01

   Ein Administrator kann den Zeitraum eines geplanten Termins ändern.

.. req:: Verschieben prüft die Zuweisung erneut
   :id: REQ_TER_AEND_02
   :status: draft
   :priority: high
   :links: REQ_TER_AEND_01, REQ_TER_ZUW_01

   Beim Verschieben wird geprüft, ob die bestehende Trainerzuweisung im
   neuen Zeitraum noch zulässig ist. Kollidiert sie mit einer Abwesenheit
   oder einem anderen Termin desselben Trainers, wird das Verschieben
   abgewiesen.

.. req:: Übrige Termindaten ändern
   :id: REQ_TER_AEND_03
   :status: draft
   :links: REQ_TER_ANL_01

   Ein Administrator kann Ort, Zugangsart und Durchführungsart eines
   geplanten Termins ändern.

.. req:: Termin absagen
   :id: REQ_TER_AEND_04
   :status: draft
   :priority: high
   :links: REQ_TER_STAT_01

   Ein Administrator kann einen geplanten Termin absagen. Der Termin bleibt
   mit allen Daten erhalten und wechselt in den Zustand abgesagt; er wird
   nicht mehr als anstehend geführt.

.. req:: Absage wird mitgeteilt
   :id: REQ_TER_AEND_05
   :status: draft
   :links: REQ_TER_AEND_04

   Ist dem abgesagten Termin ein Trainer zugewiesen, wird er über die Absage
   benachrichtigt. Dasselbe gilt für zugewiesene Assistenten.

.. req:: Termin löschen
   :id: REQ_TER_AEND_06
   :status: draft
   :links: REQ_TER_AEND_04

   Ein Administrator kann einen Termin löschen. Damit verschwindet er
   vollständig, im Unterschied zur Absage, die ihn erhält.

.. req:: Warnung vor Absage und Löschen
   :id: REQ_TER_AEND_07
   :status: draft
   :priority: high
   :links: REQ_TER_AEND_04, REQ_TER_AEND_06, REQ_TLN_BUCH_01

   Vor dem Absagen oder Löschen eines Termins wird gewarnt und aufgeführt,
   was daran hängt: Anzahl der Teilnehmerbuchungen, zugewiesener Trainer und
   zugewiesene Assistenten. Bestehende Teilnehmerbuchungen sind der Anlass,
   am deutlichsten zu warnen.

   Die Warnung verhindert den Vorgang nicht -- sie stellt nur sicher, dass
   er nicht nebenbei geschieht.

.. req:: Teilnehmer werden nicht selbsttätig benachrichtigt
   :id: REQ_TER_AEND_08
   :status: draft
   :priority: high
   :links: REQ_TER_AEND_07, REQ_TLN_BUCH_02

   Über eine Absage werden Trainer und Assistenten benachrichtigt,
   gebuchte Teilnehmer nicht: Eine Teilnehmerbuchung führt Name, Firma und
   Bemerkung, aber keine Anschrift. Die Warnung vor der Absage nennt deshalb
   die betroffenen Buchungen, damit der Administrator die Teilnehmer
   außerhalb des Systems informieren kann.

Trainerzuweisung
----------------

.. req:: Keine Zuweisung bei Abwesenheit
   :id: REQ_TER_ZUW_01
   :status: draft
   :priority: high

   Ein Trainer kann einem Termin nicht zugewiesen werden, wenn im Zeitraum
   des Termins eine aktive Abwesenheit dieses Trainers liegt. Diese Regel
   lässt sich nicht übergehen.

Auf diese Regel verweisen auch Vormerkung und Übernahme, damit sie nicht umgangen wird:

.. needflow::
   :root_id: REQ_TER_ZUW_01
   :root_depth: 1
   :direction: LR

.. req:: Keine überschneidenden Zuweisungen
   :id: REQ_TER_ZUW_03
   :status: draft
   :priority: high
   :links: REQ_TER_ZUW_01

   Ein Trainer kann einem Termin nicht zugewiesen werden, wenn er im selben
   Zeitraum bereits einem anderen Termin zugewiesen ist -- als Trainer oder
   als Assistent. Auch diese Regel lässt sich nicht übergehen.

.. req:: Proaktive Zuweisung ohne Qualifikation
   :id: REQ_TER_ZUW_02
   :status: draft
   :links: REQ_QUA_ANG_01, REQ_QUA_ANG_03

   Ein Administrator kann einen Trainer ohne passende Qualifikation einem
   Termin zuweisen. Die Zuweisung gilt sofort; der Trainer erhält ein
   Qualifikationsangebot für die zugehörige Schulung. Lehnt er es ab, fällt
   der Termin zurück in den Zustand "nicht zugewiesen".

.. req:: Trainer abziehen
   :id: REQ_TER_ZUW_04
   :status: draft
   :priority: high
   :links: REQ_TER_ANL_02

   Ein Administrator kann die Trainerzuweisung eines Termins aufheben. Der
   Termin gilt danach als nicht zugewiesen. Der bisherige Trainer wird
   benachrichtigt.

.. req:: Trainer austauschen
   :id: REQ_TER_ZUW_05
   :status: draft
   :links: REQ_TER_ZUW_04

   Ein Administrator kann einen zugewiesenen Trainer unmittelbar durch einen
   anderen ersetzen. Für den neuen Trainer gelten dieselben Prüfungen wie
   bei einer erstmaligen Zuweisung.

Entscheidungshilfe bei der Planung
----------------------------------

.. req:: Trainer gruppiert anzeigen
   :id: REQ_TER_VORS_01
   :status: draft
   :priority: high
   :links: REQ_TER_ANL_01, REQ_TER_ZUW_02

   Beim Planen eines Termins zeigt das System eine Liste der Trainer, klar
   getrennt in zwei Gruppen: zuerst die für die Schulung qualifizierten,
   danach die übrigen. So bleibt die proaktive Zuweisung eines nicht
   qualifizierten Trainers möglich, ohne dass sie versehentlich geschieht.

.. req:: Verfügbarkeit im Kalender anzeigen
   :id: REQ_TER_VORS_02
   :status: draft
   :links: REQ_TER_VORS_01

   Zu den angezeigten Trainern zeigt eine Kalenderansicht, wann sie
   verfügbar sind. Sie berücksichtigt aktive Abwesenheiten und bestehende
   Zuweisungen und macht damit geeignete Zeiträume erkennbar.

Dashboard der Administratoren
-----------------------------

.. req:: Termine ohne Trainer prominent anzeigen
   :id: REQ_TER_DASH_01
   :status: draft
   :priority: high
   :links: REQ_TER_ANL_02

   Das Dashboard eines Administrators zeigt alle zukünftigen Termine ohne
   Trainerzuweisung an hervorgehobener Stelle, aufsteigend nach Startdatum
   sortiert.

.. req:: Warnstufen nach Vorlauf
   :id: REQ_TER_DASH_02
   :status: draft
   :priority: high
   :links: REQ_TER_DASH_01

   Termine ohne Trainerzuweisung werden nach ihrem Vorlauf in Warnstufen
   unterschieden:

   * weniger als vier Wochen bis zum Start -- höchste Warnstufe,
   * weniger als drei Monate bis zum Start -- mittlere Warnstufe,
   * darüber hinaus -- keine Warnstufe.

   Die Warnstufen sind in der Übersicht visuell unterscheidbar.

.. req:: Überfällige Termine stehen ganz oben
   :id: REQ_TER_DASH_04
   :status: draft
   :priority: high
   :links: REQ_TER_DASH_01, REQ_TER_STAT_06

   Termine, deren Enddatum vorüber ist, die aber noch nicht abgeschlossen
   sind, stehen im Dashboard eines Administrators an erster Stelle -- vor
   den anstehenden Terminen.

   Sie sind der Regelweg, auf dem ein versäumter Abschluss auffällt. Der
   selbsttätige Abschluss nach zwei Monaten ist erst das Auffangnetz
   dahinter.

.. req:: Warnung bei zu wenigen Teilnehmern
   :id: REQ_TER_DASH_03
   :status: draft
   :priority: high
   :links: REQ_TER_DASH_02, REQ_TLN_GRENZ_01

   Erreicht ein Termin weniger als vier Wochen vor dem Start seine
   Mindestteilnehmerzahl nicht, wird er im Dashboard ebenso gewarnt wie ein
   Termin ohne Trainer. Nur so bleibt Zeit, ihn abzusagen oder zu
   bewerben.
