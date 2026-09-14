.. _terminplanung:

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

   Ein Administrator legt zu einer aktiven Schulung einen Termin an.

.. req:: Pflichtangaben eines Termins
   :id: REQ_TER_ANL_03
   :status: draft
   :priority: high
   :links: REQ_TER_ANL_01

   Anzugeben sind die Schulung, das Startdatum und das Enddatum. Alles
   Weitere -- Zugangsart, Durchführungsart, Ort und Trainerzuweisung -- ist
   freiwillig und kann später ergänzt werden.

   Termine entstehen oft, bevor feststeht, wie und wo sie stattfinden. Ein
   Termin, der nur den Zeitraum kennt, ist deshalb ein gültiger Termin.

.. req:: Termin ohne Trainerzuweisung speicherbar
   :id: REQ_TER_ANL_02
   :status: approved
   :priority: high
   :links: REQ_TER_ANL_03

   Ein Termin kann ohne zugewiesenen Trainer gespeichert werden. Er gilt
   dann als nicht zugewiesen.

.. req:: Enddatum wird aus der Dauer vorgeschlagen
   :id: REQ_TER_ANL_04
   :status: draft
   :priority: high
   :links: REQ_TER_ANL_03, REQ_KAT_FELD_03

   Beim Anlegen schlägt das System aus der Dauer der Schulung ein Enddatum
   vor. Der Vorschlag ist nicht bindend: Der Administrator kann einen
   kürzeren wie einen längeren Zeitraum wählen.

.. req:: Enddatum liegt nicht vor dem Startdatum
   :id: REQ_TER_ANL_05
   :status: draft
   :priority: high
   :links: REQ_TER_ANL_03

   Ein Termin, dessen Enddatum vor seinem Startdatum liegt, wird
   abgewiesen. Start- und Enddatum dürfen auf denselben Tag fallen.

.. req:: Kein Termin in der Vergangenheit
   :id: REQ_TER_ANL_06
   :status: draft
   :priority: high
   :links: REQ_TER_ANL_03

   Ein Termin, dessen Startdatum in der Vergangenheit liegt, wird
   abgewiesen. Gehaltene Schulungen werden nicht nachgetragen.

Kennung
-------

.. req:: Termin-ID wird selbsttätig vergeben
   :id: REQ_TER_ID_01
   :status: draft
   :priority: high
   :component: backend
   :links: REQ_TER_ANL_01, REQ_KAT_ID_01

   Die Kennung eines Termins vergibt das System. Sie besteht aus der
   Schulungs-ID, einem ``-T`` und einer je Schulung fortlaufenden,
   vierstelligen Nummer -- etwa ``SCH-001-T0001``.

   Anders als bei der Schulung gibt es hier nichts zu benennen: Eine
   Termin-ID ist reine Kennung, und vier Stellen reichen für jede Schulung
   weit über die Lebensdauer des Planers hinaus.

.. req:: Termin-ID ist unveränderlich
   :id: REQ_TER_ID_02
   :status: draft
   :links: REQ_TER_ID_01

   Die Kennung eines Termins ändert sich nach dem Anlegen nicht mehr, auch
   nicht beim Verschieben oder Absagen.

Zugangsart und Durchführungsart
-------------------------------

.. req:: Zugangsart eines Termins
   :id: REQ_TER_FORM_01
   :status: draft
   :priority: high
   :links: REQ_TER_ANL_03

   Die **Zugangsart** eines Termins ist eine von zwei:

   * **öffentlich** -- einzeln buchbar für Teilnehmer verschiedener Firmen,
   * **exklusiv** -- für eine Firma.

.. req:: Durchführungsart eines Termins
   :id: REQ_TER_FORM_02
   :status: draft
   :priority: high
   :links: REQ_TER_ANL_03

   Die **Durchführungsart** eines Termins ist eine von vier:

   * **remote** -- ausschließlich online, ohne gemeinsamen Ort,
   * **vor Ort** -- in den eigenen Räumen,
   * **beim Kunden** -- in den Räumen des Kunden,
   * **hybrid** -- in den eigenen Räumen und zugleich online zugänglich.

.. req:: Beide Angaben sind nachtragbar
   :id: REQ_TER_FORM_07
   :status: draft
   :priority: high
   :links: REQ_TER_FORM_01, REQ_TER_FORM_02, REQ_TER_ANL_03

   Zugangsart und Durchführungsart können beim Anlegen angegeben oder
   später nachgetragen werden. Ein Termin ohne diese Angaben ist gültig.

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

   Bei "vor Ort", "beim Kunden" und "hybrid" trägt der Termin einen Ort als
   freien Text. Bei "remote" entfällt die Angabe eines physischen Orts.

.. req:: Zugangsart bestimmt die maßgebliche Teilnehmergrenze
   :id: REQ_TER_FORM_05
   :status: draft
   :priority: high
   :links: REQ_TER_FORM_01, REQ_TLN_GRENZ_03

   Welche Teilnehmergrenze einer Schulung für einen Termin gilt, richtet
   sich nach seiner Zugangsart, nicht nach seiner Durchführungsart.

.. req:: Ohne Zugangsart keine Grenzprüfung
   :id: REQ_TER_FORM_06
   :status: draft
   :priority: high
   :links: REQ_TER_FORM_05, REQ_TER_FORM_07

   Solange die Zugangsart eines Termins nicht angegeben ist, lässt sich
   keine Teilnehmergrenze bestimmen, und es wird dazu auch nicht gewarnt.
   Teilnehmerbuchungen sind trotzdem möglich.

Zustände eines Termins
----------------------

.. req:: Zustände eines Termins
   :id: REQ_TER_STAT_01
   :status: approved
   :priority: high

   Ein Termin ist **geplant**, **abgeschlossen** oder **abgesagt**. Der
   Zustand ist unabhängig davon, ob ein Trainer zugewiesen ist -- das ist
   eine eigene Eigenschaft. Als abgeschlossen gilt ein Termin ausschließlich
   mit dem Zustand "abgeschlossen".

   "Ausgebucht" ist kein Zustand: Ob ein Termin voll ist, ergibt sich aus
   seinen Buchungen, und das Überschreiten der Grenze ist nach
   :need:`REQ_TLN_GRENZ_02` ohnehin erlaubt und nur eine Warnung.

.. req:: Zukünftiger Termin
   :id: REQ_TER_ZEIT_01
   :status: approved
   :priority: high

   Als zukünftig gilt ein Termin, dessen Enddatum am aktuellen Datum noch
   nicht vergangen ist. Das schließt einen bereits begonnenen, aber noch
   nicht beendeten Termin ein.

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

   Ein Administrator kann den Zeitraum eines geplanten Termins ändern. Es
   gelten dieselben Regeln wie beim Anlegen: nicht in die Vergangenheit, und
   das Enddatum nicht vor dem Startdatum.

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
   :links: REQ_TER_ANL_01, REQ_TER_FORM_07

   Ein Administrator kann Ort, Zugangsart und Durchführungsart eines
   geplanten Termins ändern oder erstmals angeben.

.. req:: Termin absagen
   :id: REQ_TER_AEND_04
   :status: draft
   :priority: high
   :links: REQ_TER_STAT_01

   Ein Administrator kann einen geplanten Termin absagen. Der Termin bleibt
   mit allen Daten erhalten und wechselt in den Zustand abgesagt; er wird
   nicht mehr als anstehend geführt.

.. req:: Eine Absage ist endgültig
   :id: REQ_TER_AEND_09
   :status: draft
   :priority: high
   :links: REQ_TER_AEND_04

   Ein abgesagter Termin kann nicht wieder in den Zustand geplant versetzt
   werden. Soll er doch stattfinden, wird ein neuer Termin angelegt.

   Damit bleibt die Absage als Tatsache stehen, statt rückwirkend zu
   verschwinden -- und der neue Termin trägt eine eigene Kennung, unter der
   die Beteiligten ihn von dem abgesagten unterscheiden können.

.. req:: Absage wird mitgeteilt
   :id: REQ_TER_AEND_05
   :status: draft
   :links: REQ_TER_AEND_04

   Ist dem abgesagten Termin ein Trainer zugewiesen, wird er über die Absage
   benachrichtigt. Dasselbe gilt für zugewiesene Assistenten.

.. req:: Termin löschen
   :id: REQ_TER_AEND_06
   :status: draft
   :links: REQ_TER_AEND_04, REQ_TER_STAT_01

   Ein Administrator kann einen geplanten oder abgesagten Termin löschen.
   Damit verschwindet er vollständig, im Unterschied zur Absage, die ihn
   erhält.

.. req:: Abgeschlossene Termine lassen sich nicht löschen
   :id: REQ_TER_AEND_10
   :status: draft
   :priority: high
   :links: REQ_TER_AEND_06, REQ_TRA_HIST_01

   Ein abgeschlossener Termin kann nicht gelöscht werden. Er ist die
   Aufzeichnung einer durchgeführten Schulung: Wer sie gehalten hat und wie
   viele teilgenommen haben, bleibt dauerhaft erhalten.

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

.. req:: Keine Zuweisung an abgeschlossenen oder abgesagten Terminen
   :id: REQ_TER_ZUW_06
   :status: draft
   :priority: high
   :links: REQ_TER_ZUW_04, REQ_TER_STAT_01

   An einem abgeschlossenen oder abgesagten Termin lässt sich die
   Trainerzuweisung nicht mehr ändern.

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

.. req:: Nicht verfügbare Trainer sind erkennbar
   :id: REQ_TER_VORS_03
   :status: draft
   :priority: high
   :links: REQ_TER_VORS_01, REQ_TER_ZUW_01, REQ_TER_ZUW_03

   In der Liste ist erkennbar, welche Trainer im Zeitraum des Termins nicht
   zur Verfügung stehen -- wegen einer Abwesenheit oder einer anderen
   Zuweisung -- und aus welchem der beiden Gründe. Sie sind nicht
   auswählbar.

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

Übernahme der bestehenden Termindaten
-------------------------------------

.. req:: Bestehende Termine werden umgeschlüsselt
   :id: REQ_TER_MIGR_01
   :status: draft
   :priority: high
   :component: backend
   :links: REQ_TER_STAT_01, REQ_TER_FORM_01, REQ_TER_FORM_02

   Die vorhandenen Termindaten werden beim Umbau auf die neuen Felder
   umgeschlüsselt:

   * Zustand ``ausgebucht`` wird zu ``geplant``; die Auslastung ergibt sich
     künftig aus den Teilnehmerbuchungen.
   * Durchführungsart: ``Präsenz`` wird zu ``vor Ort``, ``Online`` zu
     ``remote``.
   * Zugangsart: Alle bestehenden Termine sind öffentlich -- sie stehen
     heute unter ``oeffentlicheTermine``. Diese Zuordnung wird übernommen,
     bevor die Struktur verschwindet.
   * Die Kennungen der Form ``SCH-001-T1`` werden auf die vierstellige Form
     gebracht.
