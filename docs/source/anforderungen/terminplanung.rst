.. _terminplanung:

Terminplanung
=============

Ein Termin ist die konkret geplante Durchführung einer Schulung. Termine
plant ausschließlich ein Administrator.

.. decision:: Termine werden nur tageweise geplant
   :id: DEC_TER_ZEIT_01
   :status: approved

   Ein Termin belegt jeden Schulungstag seines Zeitraums vollständig. Ein
   Schulungstag liegt zwischen Montag und Freitag und entspricht einem
   Arbeitstag von acht Stunden; Start- und Enduhrzeiten werden nicht geführt.
   Zwei Termine am selben Schulungstag überschneiden sich daher immer.

   Am Wochenende finden keine Schulungen statt. Wochenenden innerhalb eines
   Terminzeitraums zählen nicht zur Schulungsdauer.

   Feiertage werden nicht automatisch berücksichtigt. Sie können je nach
   Kunde, Trainer und Ort unterschiedlich sein; der Administrator vermeidet
   sie bei der Planung selbst. Für das System zählt daher jeder Montag bis
   Freitag als möglicher Schulungstag.

   Eine feinere Planung nach Uhrzeiten wird erst eingeführt, wenn tatsächlich
   Schulungen unter einem Arbeitstag geplant werden müssen.

Termine anlegen
---------------

.. req:: Termin für aktive Schulung anlegen
   :id: REQ_TER_ANL_01
   :status: approved
   :priority: high

   Ein Administrator legt zu einer aktiven Schulung einen Termin an.

.. req:: Pflichtangaben eines Termins
   :id: REQ_TER_ANL_03
   :status: approved
   :priority: high
   :links: REQ_TER_ANL_01, DEC_TER_ZEIT_01

   Anzugeben sind die Schulung, das Startdatum und das Enddatum. Alles
   Weitere -- Zugangsart, Durchführungsart, Ort, Kundenfirma,
   Online-Zugang und Trainerzuweisung -- ist freiwillig, soweit es zur
   gewählten Zugangs- und Durchführungsart passt, und kann später ergänzt
   werden.

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
   :status: approved
   :priority: high
   :links: REQ_TER_ANL_03, REQ_KAT_FELD_03

   Beim Anlegen schlägt das System aus der Dauer der Schulung ein Enddatum
   vor. Dabei zählen nur die Wochentage von Montag bis Freitag. Für eine
   dreitägige Schulung ab Freitag wird daher der folgende Dienstag
   vorgeschlagen. Der Vorschlag ist nicht bindend: Der Administrator kann
   einen kürzeren wie einen längeren Zeitraum wählen.

.. req:: Enddatum liegt nicht vor dem Startdatum
   :id: REQ_TER_ANL_05
   :status: approved
   :priority: high
   :links: REQ_TER_ANL_03

   Ein Termin, dessen Enddatum vor seinem Startdatum liegt, wird
   abgewiesen. Start- und Enddatum dürfen auf denselben Tag fallen.

.. req:: Kein Termin in der Vergangenheit
   :id: REQ_TER_ANL_06
   :status: approved
   :priority: high
   :links: REQ_TER_ANL_03

   Ein Termin, dessen Startdatum in der Vergangenheit liegt, wird
   abgewiesen. Gehaltene Schulungen werden nicht nachgetragen.

.. req:: Kein Schulungstag am Wochenende
   :id: REQ_TER_ANL_07
   :status: approved
   :priority: high
   :links: DEC_TER_ZEIT_01, REQ_TER_ANL_03

   Start- und Enddatum eines Termins müssen zwischen Montag und Freitag
   liegen. Ein Wochenende zwischen beiden Daten ist zulässig, zählt aber
   nicht als Schulungszeit.

Kennung
-------

.. req:: Termin-ID wird selbsttätig vergeben
   :id: REQ_TER_ID_01
   :status: approved
   :priority: high
   :component: backend
   :links: REQ_TER_ANL_01, REQ_KAT_ID_01

   Die Kennung eines Termins vergibt das System. Sie besteht aus der
   Schulungs-ID, einem ``-T`` und einer je Schulung fortlaufenden,
   vierstelligen Nummer -- etwa ``SCH-001-T0001``.

   Eine einmal vergebene Nummer wird auch nach dem Löschen des Termins nicht
   erneut verwendet. Lücken bleiben bestehen; nach einem gelöschten
   ``SCH-001-T0003`` folgt daher ``SCH-001-T0004``.

   Nach ``T9999`` wird das Anlegen eines weiteren Termins für diese Schulung
   mit einer eindeutigen Fehlermeldung abgewiesen. Es gibt weder einen
   Überlauf noch die Wiederverwendung alter Nummern. Eine Erweiterung des
   Schemas erfordert eine bewusste Migration.

   Anders als bei der Schulung gibt es hier nichts zu benennen: Eine
   Termin-ID ist reine Kennung, und vier Stellen reichen für jede Schulung
   weit über die Lebensdauer des Planers hinaus.

.. req:: Termin-ID ist unveränderlich
   :id: REQ_TER_ID_02
   :status: approved
   :links: REQ_TER_ID_01

   Die Kennung eines Termins ändert sich nach dem Anlegen nicht mehr, auch
   nicht beim Verschieben, Absagen oder Löschen anderer Termine.

Zugangsart und Durchführungsart
-------------------------------

.. req:: Zugangsart eines Termins
   :id: REQ_TER_FORM_01
   :status: approved
   :priority: high
   :links: REQ_TER_ANL_03

   Die **Zugangsart** eines Termins ist eine von zwei:

   * **öffentlich** -- einzeln buchbar für Teilnehmer verschiedener Firmen,
   * **exklusiv** -- für eine Firma.

.. req:: Durchführungsart eines Termins
   :id: REQ_TER_FORM_02
   :status: approved
   :priority: high
   :links: REQ_TER_ANL_03

   Die **Durchführungsart** eines Termins ist eine von vier:

   * **remote** -- ausschließlich online, ohne gemeinsamen Ort,
   * **vor Ort** -- in den eigenen Räumen,
   * **beim Kunden** -- in den Räumen des Kunden,
   * **hybrid** -- in den eigenen Räumen und zugleich online zugänglich.

.. req:: Beide Angaben sind nachtragbar
   :id: REQ_TER_FORM_07
   :status: approved
   :priority: high
   :links: REQ_TER_FORM_01, REQ_TER_FORM_02, REQ_TER_ANL_03

   Zugangsart und Durchführungsart können beim Anlegen angegeben oder
   später nachgetragen werden. Ein Termin ohne diese Angaben ist gültig.

.. req:: Zugangsart und Durchführungsart sind frei kombinierbar
   :id: REQ_TER_FORM_03
   :status: approved
   :priority: high
   :links: REQ_TER_FORM_01, REQ_TER_FORM_02

   Jede Zugangsart lässt sich mit jeder Durchführungsart verbinden. Es gibt
   keine verbotenen Kombinationen.

   "Beim Kunden" tritt üblicherweise mit "exklusiv" auf, aber eine
   öffentliche Schulung in Kundenräumen soll möglich bleiben, ohne dass
   dafür eine Regel geändert werden muss.

.. req:: Ort richtet sich nach der Durchführungsart
   :id: REQ_TER_FORM_04
   :status: approved
   :links: REQ_TER_FORM_02

   Ohne Durchführungsart darf der Ort leer bleiben. Bei "vor Ort", "beim
   Kunden" und "hybrid" ist ein Ort als freier Text erforderlich. Bei
   "remote" darf kein physischer Ort gespeichert sein.

   Wird ein bestehender Termin auf "remote" geändert, muss das Entfernen
   seines bisherigen Orts ausdrücklich bestätigt werden. Mit der Bestätigung
   wird der Ort entfernt; ohne sie bleibt der Termin unverändert.

.. req:: Zugangsart bestimmt die maßgebliche Teilnehmergrenze
   :id: REQ_TER_FORM_05
   :status: approved
   :priority: high
   :links: REQ_TER_FORM_01, REQ_TLN_GRENZ_03

   Welche Teilnehmergrenze einer Schulung für einen Termin gilt, richtet
   sich nach seiner Zugangsart, nicht nach seiner Durchführungsart. Bei einem
   exklusiven Termin gilt die Mindestteilnehmerzahl, bei einem öffentlichen
   Termin die Höchstteilnehmerzahl.

.. req:: Ohne Zugangsart keine Grenzprüfung
   :id: REQ_TER_FORM_06
   :status: approved
   :priority: high
   :links: REQ_TER_FORM_05, REQ_TER_FORM_07

   Solange die Zugangsart eines Termins nicht angegeben ist, lässt sich
   keine Teilnehmergrenze bestimmen, und es wird dazu auch nicht gewarnt.
   Teilnehmerbuchungen sind trotzdem möglich.

.. req:: Exklusiver Termin hat Buchungen einer Firma
   :id: REQ_TER_FORM_08
   :status: approved
   :priority: high
   :links: REQ_TER_FORM_01, REQ_TLN_BUCH_06

   Ein geplanter Termin kann nur dann von "öffentlich" auf "exklusiv"
   geändert werden, wenn er keine Buchungen oder ausschließlich Buchungen
   der gewählten Kundenfirma hat. Bei abweichenden Firmen wird die Änderung
   abgewiesen und der Termin bleibt öffentlich.

   Die Änderung von "exklusiv" zu "öffentlich" ist bei einem geplanten
   Termin unabhängig von seinen Buchungen möglich.

.. req:: Kundenfirma eines exklusiven Termins
   :id: REQ_TER_FORM_09
   :status: approved
   :priority: high
   :links: REQ_TER_FORM_01, REQ_TER_FORM_08

   Ein exklusiver Termin trägt genau eine Kundenfirma als freien Text. Sie
   ist erforderlich, sobald die Zugangsart "exklusiv" gewählt ist. Ein
   öffentlicher Termin und ein Termin ohne Zugangsart tragen keine
   Kundenfirma.

   Führende und nachfolgende Leerzeichen werden entfernt. Für Prüfungen wird
   die Firma ohne Beachtung von Groß- und Kleinschreibung verglichen;
   gespeichert und angezeigt wird die Schreibweise am Termin. ``ACME GmbH``
   und ``Acme GmbH`` gelten daher als dieselbe Firma.

   Beim Wechsel eines exklusiven Termins zu "öffentlich" muss das Entfernen
   der Kundenfirma ausdrücklich bestätigt werden. Mit der Bestätigung wird
   sie entfernt; ohne sie bleibt der Termin unverändert.

   Wird die Kundenfirma eines exklusiven Termins mit bestehenden Buchungen
   geändert, ist ebenfalls eine ausdrückliche Bestätigung erforderlich. Mit
   ihr wird die Firma am Termin und an allen Buchungen in einem Vorgang
   geändert. Ohne Bestätigung bleibt alles unverändert.

   Die Kundenfirma ist für jeden angemeldeten Trainer sichtbar, der den
   Termin sehen darf. Ihre Anzeige ist nicht auf den zugewiesenen Trainer und
   die Assistenten beschränkt.

.. req:: Keine Raum- oder Ortskonfliktprüfung
   :id: REQ_TER_FORM_10
   :status: approved
   :links: REQ_TER_FORM_04

   Das System verwaltet keine Räume und prüft keine Konflikte anhand des frei
   eingegebenen Orts. Zwei Termine dürfen am selben Schulungstag denselben Ort
   tragen, solange keine beteiligte Person doppelt belegt ist. Die räumliche
   Planung liegt beim Administrator.

.. req:: Online-Zugang für Remote und Hybrid
   :id: REQ_TER_FORM_11
   :status: approved
   :links: REQ_TER_FORM_02, REQ_TER_FORM_07

   Ein Termin mit Durchführungsart "remote" oder "hybrid" kann eine optionale
   URL als Online-Zugang tragen. Sie kann beim Anlegen angegeben oder später
   nachgetragen werden. Fehlende Einwahldaten verhindern die Speicherung
   nicht.

   Führende und nachfolgende Leerzeichen werden entfernt. Akzeptiert werden
   ausschließlich vollständige URLs mit ``https://`` oder ``http://``.
   Relative Angaben, reiner Text und andere Schemata werden abgewiesen.

   Bei "vor Ort", "beim Kunden" und ohne Durchführungsart darf kein
   Online-Zugang gespeichert sein. Beim Wechsel von "remote" oder "hybrid"
   zu einer dieser Varianten muss das Entfernen einer vorhandenen URL
   ausdrücklich bestätigt werden. Mit der Bestätigung wird sie entfernt;
   ohne sie bleibt der Termin unverändert.

.. req:: Online-Zugang ist nur Beteiligten sichtbar
   :id: REQ_TER_FORM_12
   :status: approved
   :priority: high
   :links: REQ_TER_FORM_11, REQ_TRA_UEBER_01, REQ_TRA_UEBER_05

   Die URL des Online-Zugangs sehen ausschließlich Administratoren, der
   zugewiesene Trainer und die zugewiesenen Assistenten des Termins. Andere
   Trainer dürfen den Termin sehen, erhalten die URL aber weder in
   Übersichten noch über die Schnittstelle.

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

.. req:: Der ausführende Trainer bestätigt die Durchführung
   :id: REQ_TER_STAT_02
   :status: approved
   :priority: high
   :links: REQ_TER_STAT_01, REQ_TLN_TEIL_02

   Der zugewiesene Trainer bestätigt nach der Durchführung, dass der Termin
   stattgefunden hat und seine Teilnehmerdaten vollständig sind. Dadurch
   wechselt der Termin in den Zustand abgeschlossen. Das ist der vorgesehene
   Weg.

   Die Bestätigung setzt voraus, dass der Teilnahmestatus jeder vorhandenen
   Buchung geklärt ist. Ein Termin ohne Buchungen kann bestätigt werden.

   Ein vorhandener Online-Zugang wird mit der Bestätigung entfernt. Die
   Durchführungsart bleibt als historische Information erhalten.

   Die Bestätigung ist endgültig. Ein abgeschlossener Termin kann weder
   wieder geöffnet noch fachlich geändert werden. Ein Korrekturprozess für
   versehentliche Bestätigungen ist zunächst nicht vorgesehen.

.. req:: Administrator kann die Durchführung ersatzweise bestätigen
   :id: REQ_TER_STAT_03
   :status: approved
   :links: REQ_TER_STAT_02

   Ein Administrator kann die Durchführung eines Termins ebenfalls
   bestätigen, etwa wenn der zugewiesene Trainer es versäumt oder nicht mehr
   erreichbar ist. Das ist der Ausweichweg, nicht der Regelfall.

   Eine manuelle Durchführungsbestätigung setzt immer einen zugewiesenen,
   qualifizierten Trainer voraus. Ohne Trainer kann der Termin nur abgesagt
   oder nach zwei Kalendermonaten selbsttätig als nicht bestätigt
   abgeschlossen werden.

.. req:: Durchführung am Enddatum bestätigbar
   :id: REQ_TER_STAT_04
   :status: approved
   :links: REQ_TER_STAT_01

   Ein Termin kann ab seinem Enddatum als durchgeführt bestätigt werden.
   Weil keine Uhrzeiten geführt werden, ist die Bestätigung an diesem Tag
   ganztägig möglich. Vor dem Enddatum wird sie abgewiesen.

.. req:: Abschlusszeitpunkt wird festgehalten
   :id: REQ_TER_STAT_05
   :status: approved
   :priority: high
   :links: REQ_TER_STAT_01, REQ_TLN_AUFB_01

   Zu einem abgeschlossenen Termin wird die Abschlussart "manuell" oder
   "automatisch" und der fachliche Abschlusszeitpunkt festgehalten. Bei
   einer manuellen Bestätigung wird zusätzlich der bestätigende Benutzer
   gespeichert; bei einem automatischen Abschluss bleibt dieser leer. Ein
   künstlicher Systembenutzer wird nicht angelegt.

   Der Abschlusszeitpunkt ist der Beginn der Frist, nach der die
   Teilnehmerdetails entfernt werden.

.. req:: Automatischer Abschluss nach zwei Kalendermonaten
   :id: REQ_TER_STAT_06
   :status: approved
   :priority: high
   :links: REQ_TER_STAT_01

   Ein Termin, der zwei Kalendermonate nach seinem Enddatum noch geplant ist,
   wird selbsttätig abgeschlossen. Fehlt der entsprechende Tag im Zielmonat,
   gilt dessen letzter Tag. Ein Termin mit Enddatum 31. Dezember wird daher
   am letzten Februartag selbsttätig abgeschlossen.

   War die lokale Anwendung am Stichtag nicht in Betrieb, holt sie den
   Abschluss beim nächsten Start nach. Als fachlicher Abschlusszeitpunkt
   gilt der ursprüngliche Stichtag, nicht der spätere Startzeitpunkt. Eine
   zusätzliche technische Verarbeitungshistorie wird nicht geführt.

   Bleibt die Anwendung über den Stichtag hinweg geöffnet, prüft sie die
   Fälligkeit einmal pro Kalendertag. Spätestens vor dem ersten fachlichen
   Zugriff des Tages sind die Zustände nachgezogen; eine sekundengenaue
   Ausführung um Mitternacht ist nicht erforderlich.

   Auch beim selbsttätigen Abschluss wird ein vorhandener Online-Zugang
   entfernt.

   Damit kommt jeder Termin zum Abschluss, auch ein vergessener. Ohne diese
   Regel begänne für ihn nie die Frist, nach der die Teilnehmerdetails
   entfernt werden -- die Namen blieben unbegrenzt liegen.

.. req:: Automatischer Abschluss ist erkennbar
   :id: REQ_TER_STAT_07
   :status: approved
   :priority: high
   :links: REQ_TER_STAT_06, REQ_TER_STAT_05

   Ein selbsttätig abgeschlossener Termin ist als solcher erkennbar. Aus dem
   Eintrag geht hervor, dass ihn niemand bestätigt hat.

   Das ist keine Förmlichkeit: Nach :need:`REQ_TLN_TEIL_03` sind die
   Teilnehmerbuchungen mit dem Abschluss unveränderlich. Bei einem
   selbsttätigen Abschluss hat aber niemand geprüft, wer tatsächlich
   teilgenommen hat -- die eingefrorenen Zahlen sind ungeprüft, und das muss
   sichtbar sein.

   Der zugewiesene Trainer wird benachrichtigt, dass der Termin selbsttätig
   abgeschlossen wurde und nicht in Teilnehmerauswertungen eingeht.
   Administratoren erkennen dies in den Termindetails, erhalten aber keine
   zusätzliche Benachrichtigung.

   Der selbsttätige Abschluss ist endgültig. Der Termin kann später nicht
   manuell bestätigt werden, und seine Teilnehmerdaten bleiben
   unveränderlich. Korrekturen erfolgen außerhalb des Systems.

.. req:: Selbsttätig abgeschlossene Termine zählen nicht in Auswertungen
   :id: REQ_TER_STAT_08
   :status: approved
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
   :status: approved
   :priority: high
   :links: REQ_TER_ANL_01

   Vor dem Start kann ein Administrator Start- und Enddatum eines geplanten
   Termins ändern. Es gelten dieselben Regeln wie beim Anlegen: nicht in die
   Vergangenheit, nicht am Wochenende und das Enddatum nicht vor dem
   Startdatum.

   Ab dem Startdatum ist dieses unveränderlich. Solange der Termin noch
   läuft, kann sein Enddatum auf das aktuelle Datum oder einen späteren
   Wochentag geändert werden. Nach dem Enddatum lässt sich der Zeitraum nicht
   mehr ändern.

.. req:: Verschieben prüft die Zuweisung erneut
   :id: REQ_TER_AEND_02
   :status: approved
   :priority: high
   :links: REQ_TER_AEND_01, REQ_TER_ZUW_01

   Beim Ändern des Zeitraums wird geprüft, ob die bestehenden Trainer- und
   Assistenzzuweisungen im neuen Zeitraum noch zulässig sind. Kollidiert eine
   von ihnen mit einer Abwesenheit oder einem anderen Termin derselben
   Person, wird die Änderung abgewiesen.

.. req:: Übrige Termindaten ändern
   :id: REQ_TER_AEND_03
   :status: approved
   :links: REQ_TER_ANL_01, REQ_TER_FORM_07

   Ein Administrator kann Ort, Zugangsart und Durchführungsart eines
   geplanten Termins ändern oder erstmals angeben. Dasselbe gilt für die
   Kundenfirma und den Online-Zugang nach deren jeweiligen Regeln.

   Diese Rahmendaten bleiben auch nach dem Enddatum korrigierbar, solange
   der Termin noch geplant ist. Mit der Durchführungsbestätigung oder Absage
   werden sie endgültig und lassen sich nicht mehr ändern.

.. req:: Operative Änderungen werden mitgeteilt
   :id: REQ_TER_AEND_12
   :status: approved
   :priority: high
   :links: REQ_TER_AEND_01, REQ_TER_AEND_03, REQ_TRA_NACHR_01

   Ändert ein Administrator Zeitraum, Ort, Durchführungsart, Kundenfirma oder
   Online-Zugang eines Termins, werden der zugewiesene Trainer und alle
   zugewiesenen Assistenten benachrichtigt. Die Nachricht nennt jeweils den
   alten und den neuen Wert. Beim Online-Zugang enthält sie ausschließlich
   die neue URL, damit veraltete Einwahldaten nicht weiterverwendet werden.

   Eine reine Änderung der Zugangsart ohne Änderung einer Kundenfirma erzeugt
   keine Benachrichtigung.

.. req:: Zugeordnete Schulung ist unveränderlich
   :id: REQ_TER_AEND_11
   :status: approved
   :priority: high
   :links: REQ_TER_ID_01, REQ_TER_ID_02

   Die Schulung, zu der ein Termin angelegt wurde, lässt sich nicht ändern.
   Ihre Kennung ist Bestandteil der Termin-ID. Bei einer falschen Auswahl
   wird der noch nicht gestartete Termin gelöscht und für die richtige
   Schulung neu angelegt. Ab dem Startdatum bleibt nur Absage und Neuanlage.

   Änderungen an den Inhalten der zugeordneten Schulung im Katalog sind davon
   unberührt.

.. req:: Termine archivierter Schulungen bleiben verwaltbar
   :id: REQ_TER_AEND_13
   :status: approved
   :priority: high
   :links: REQ_KAT_ARCH_01, REQ_KAT_ARCH_03

   Wird eine Schulung archiviert, bleiben ihre bereits bestehenden Termine
   vollständig verwaltbar. Solange ihr Zustand es erlaubt, können Zeitraum
   und Rahmendaten geändert, Trainer und Assistenten zugewiesen, die
   Durchführung bestätigt sowie der Termin abgesagt oder gelöscht werden.

   Die Archivierung verhindert ausschließlich das Anlegen weiterer Termine
   für diese Schulung.

.. req:: Termin absagen
   :id: REQ_TER_AEND_04
   :status: approved
   :priority: high
   :links: REQ_TER_STAT_01

   Ein Administrator kann einen geplanten Termin absagen. Der Termin bleibt
   mit allen Daten erhalten und wechselt in den Zustand abgesagt; er wird
   nicht mehr als anstehend geführt. Datum und absagender Administrator
   werden festgehalten; das Datum beginnt die Aufbewahrungsfrist der
   Teilnehmerdetails. Zusätzlich kann der Administrator einen optionalen
   Absagegrund als Freitext angeben.

   Das gilt auch nach dem Enddatum, solange die Durchführung noch nicht
   bestätigt und der Termin noch nicht selbsttätig abgeschlossen wurde. Ein
   abgesagter Termin wird nicht mehr selbsttätig abgeschlossen und zählt
   nicht als durchgeführt.

   Trainer- und Assistenzzuweisungen bleiben am abgesagten Termin als
   historische Information erhalten. Für Verfügbarkeits- und
   Kollisionsprüfungen werden sie ab der Absage nicht mehr berücksichtigt.

   Ein vorhandener Online-Zugang wird bei der Absage automatisch entfernt.
   Die Durchführungsart bleibt als historische Information erhalten.

.. req:: Eine Absage ist endgültig
   :id: REQ_TER_AEND_09
   :status: approved
   :priority: high
   :links: REQ_TER_AEND_04

   Ein abgesagter Termin kann nicht wieder in den Zustand geplant versetzt
   werden. Soll er doch stattfinden, wird ein neuer Termin angelegt.

   Damit bleibt die Absage als Tatsache stehen, statt rückwirkend zu
   verschwinden -- und der neue Termin trägt eine eigene Kennung, unter der
   die Beteiligten ihn von dem abgesagten unterscheiden können.

.. req:: Absage und Löschen werden mitgeteilt
   :id: REQ_TER_AEND_05
   :status: approved
   :links: REQ_TER_AEND_04

   Ist dem abgesagten Termin ein Trainer zugewiesen, wird er über die Absage
   benachrichtigt. Dasselbe gilt für zugewiesene Assistenten. Wurde ein
   Absagegrund angegeben, enthält die Benachrichtigung diesen Grund.

   Wird ein noch nicht gestarteter Termin gelöscht, werden sein zugewiesener
   Trainer und seine Assistenten ebenfalls benachrichtigt. Einen Löschgrund
   gibt es nicht; ein fachlich relevanter Ausfall wird stattdessen abgesagt.

.. req:: Termin löschen
   :id: REQ_TER_AEND_06
   :status: approved
   :links: REQ_TER_AEND_04, REQ_TER_AEND_05, REQ_TER_STAT_01

   Ein Administrator kann einen geplanten oder abgesagten Termin löschen,
   solange sein Startdatum noch nicht erreicht ist. Damit verschwindet er
   vollständig, im Unterschied zur Absage, die ihn erhält.

   Ab dem Startdatum kann ein Termin nur noch abgesagt oder nach der
   Durchführung bestätigt werden. So bleiben vergangene Planungen
   nachvollziehbar.

.. req:: Abgeschlossene Termine lassen sich nicht löschen
   :id: REQ_TER_AEND_10
   :status: approved
   :priority: high
   :links: REQ_TER_AEND_06, REQ_TRA_HIST_01

   Ein abgeschlossener Termin kann unabhängig von seinem Datum nicht gelöscht
   werden. Er ist die
   Aufzeichnung einer durchgeführten Schulung: Wer sie gehalten hat und wie
   viele teilgenommen haben, bleibt dauerhaft erhalten.

.. req:: Warnung vor Absage und Löschen
   :id: REQ_TER_AEND_07
   :status: approved
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
   :status: approved
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
   :status: approved
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
   :status: approved
   :priority: high
   :links: REQ_TER_ZUW_01

   Ein Trainer kann einem Termin nicht zugewiesen werden, wenn er im selben
   Zeitraum bereits einem anderen geplanten Termin zugewiesen ist -- als
   Trainer oder als Assistent. Auch diese Regel lässt sich nicht übergehen.
   Zuordnungen an abgesagten Terminen blockieren nicht.

.. req:: Zuweisung erfordert Qualifikation
   :id: REQ_TER_ZUW_02
   :status: approved
   :priority: high
   :links: REQ_QUA_UMF_01

   Ein Administrator kann einem Termin ausschließlich einen Trainer mit
   bestätigter Qualifikation für die zugehörige Schulung zuweisen. Soll ein
   anderer Trainer eingesetzt werden, erteilt der Administrator zuerst die
   Qualifikation und weist ihn anschließend zu.

.. req:: Trainer abziehen
   :id: REQ_TER_ZUW_04
   :status: approved
   :priority: high
   :links: REQ_TER_ANL_02

   Ein Administrator kann die Trainerzuweisung eines Termins aufheben. Der
   Termin gilt danach als nicht zugewiesen. Der bisherige Trainer wird
   benachrichtigt.

.. req:: Trainer austauschen
   :id: REQ_TER_ZUW_05
   :status: approved
   :links: REQ_TER_ZUW_04

   Ein Administrator kann einen zugewiesenen Trainer unmittelbar durch einen
   anderen ersetzen. Für den neuen Trainer gelten dieselben Prüfungen wie
   bei einer erstmaligen Zuweisung.

.. req:: Zuweisung wird mitgeteilt
   :id: REQ_TER_ZUW_07
   :status: approved
   :priority: high
   :links: REQ_TER_ZUW_04, REQ_TER_ZUW_05, REQ_TRA_NACHR_01

   Ein Trainer wird benachrichtigt, sobald ein Administrator ihn einem Termin
   zuweist. Das gilt auch für eine Zuweisung direkt beim Anlegen des Termins.

   Beim Austausch werden der bisherige Trainer über das Ende seiner
   Zuweisung und der neue Trainer über seine Zuweisung benachrichtigt. Beim
   bloßen Abziehen wird nur der bisherige Trainer benachrichtigt.

.. req:: Trainer und Assistent sind am selben Termin verschieden
   :id: REQ_TER_ZUW_08
   :status: approved
   :priority: high
   :links: REQ_ASS_PLATZ_01, REQ_TER_ZUW_07

   Eine Person kann an demselben Termin nicht zugleich ausführender Trainer
   und Assistent sein. Wird ein bereits zugewiesener Assistent als
   ausführender Trainer eingesetzt, muss der Administrator den Rollenwechsel
   bestätigen. Danach wird die Assistenzzuweisung entfernt und die
   Trainerzuweisung in einem Vorgang gesetzt; der Assistenzplatz wird frei.

   Ein bereits ausführender Trainer kann nicht zusätzlich als Assistent
   zugewiesen werden. Die betroffene Person wird über den Rollenwechsel
   benachrichtigt.

.. req:: Keine Zuweisung an abgeschlossenen oder abgesagten Terminen
   :id: REQ_TER_ZUW_06
   :status: approved
   :priority: high
   :links: REQ_TER_ZUW_04, REQ_TER_STAT_01

   An einem abgeschlossenen oder abgesagten Termin lässt sich die
   Trainerzuweisung nicht mehr ändern.

   Solange ein Termin noch geplant ist, darf ein Administrator auch nach
   dessen Enddatum einen qualifizierten Trainer nachtragen. Damit kann die
   tatsächliche Durchführung anschließend bestätigt werden. Die üblichen
   Verfügbarkeitsprüfungen und die Benachrichtigung über die Zuweisung gelten
   auch in diesem Fall.

Entscheidungshilfe bei der Planung
----------------------------------

.. req:: Qualifizierte Trainer anzeigen
   :id: REQ_TER_VORS_01
   :status: approved
   :priority: high
   :links: REQ_TER_ANL_01, REQ_TER_ZUW_02

   Beim Planen eines Termins zeigt das System ausschließlich die für die
   Schulung qualifizierten Trainer zur Auswahl an.

.. req:: Nicht verfügbare Trainer sind erkennbar
   :id: REQ_TER_VORS_03
   :status: approved
   :priority: high
   :links: REQ_TER_VORS_01, REQ_TER_ZUW_01, REQ_TER_ZUW_03

   In der Liste ist erkennbar, welche Trainer im Zeitraum des Termins nicht
   zur Verfügung stehen -- wegen einer Abwesenheit oder einer anderen
   Zuweisung -- und aus welchem der beiden Gründe. Sie sind nicht
   auswählbar.

.. req:: Verfügbarkeit im Kalender anzeigen
   :id: REQ_TER_VORS_02
   :status: approved
   :links: REQ_TER_VORS_01

   Zu den angezeigten Trainern zeigt eine Kalenderansicht, wann sie
   verfügbar sind. Sie berücksichtigt aktive Abwesenheiten und bestehende
   Zuweisungen und macht damit geeignete Zeiträume erkennbar.

Terminverwaltung der Administratoren
-------------------------------------

.. req:: Monatskalender als Terminübersicht
   :id: REQ_TER_SICHT_01
   :status: approved
   :priority: high

   Administratoren verwalten Termine aus einer Monatsansicht. Sie können
   zwischen Monaten wechseln und sehen darin geplante, abgeschlossene und
   abgesagte Termine mit unterscheidbarem Zustand.

   Überspannt ein Termin ein Wochenende, erscheint er nur an seinen
   Schulungstagen von Montag bis Freitag. Die Details nennen den vollständigen
   Datumsbereich und zusätzlich die Anzahl der Schulungstage, etwa "Freitag
   bis Dienstag, 3 Schulungstage".

   Auf kleinen Bildschirmen erscheint statt des Kalendergitters eine nach
   Datum sortierte Liste der Termine des ausgewählten Monats.

.. req:: Termindetails führen zu den Aktionen
   :id: REQ_TER_SICHT_02
   :status: approved
   :priority: high
   :links: REQ_TER_SICHT_01

   Die Auswahl eines Termins öffnet seine Details. Dort stehen abhängig von
   Zustand und Datum die zulässigen Aktionen zum Bearbeiten, Zuweisen,
   Abziehen, Austauschen, Bestätigen, Absagen und Löschen bereit.

.. req:: Neuer Termin ist aus der Übersicht erreichbar
   :id: REQ_TER_SICHT_03
   :status: approved
   :priority: high
   :links: REQ_TER_SICHT_01, REQ_TER_ANL_01

   Die Terminübersicht bietet Administratoren den sichtbaren Einstieg
   "Neuer Termin". Er öffnet ein modales Formular über dem Kalender; auf
   kleinen Bildschirmen darf es den verfügbaren Bildschirm ausfüllen.

   Schulung und Startdatum sind zunächst leer. Sobald beide gewählt sind,
   wird das Enddatum aus der Schulungsdauer vorgeschlagen. Abbrechen schließt
   das Formular ohne Änderung und führt zum unveränderten Kalender zurück.
   Ein Klick auf einen Kalendertag legt keinen Termin an.

   Nach erfolgreichem Anlegen schließt sich das Formular. Der Kalender zeigt
   den Monat des neuen Termins und öffnet dessen Details.

.. decision:: Vorerst keine Kalenderfilter
   :id: DEC_TER_SICHT_01
   :status: approved

   Die erste Terminübersicht bietet Monatsnavigation, unterscheidbare
   Zustände und Termindetails, aber keine Filter nach Schulung, Trainer,
   Zustand oder Zugangsart. Filter werden erst ergänzt, wenn die reale Menge
   der Termine den Monatskalender unübersichtlich macht.

Dashboard der Administratoren
-----------------------------

.. req:: Termine ohne Trainer prominent anzeigen
   :id: REQ_TER_DASH_01
   :status: approved
   :priority: high
   :links: REQ_TER_ANL_02

   Das Dashboard eines Administrators zeigt alle zukünftigen geplanten
   Termine ohne Trainerzuweisung an hervorgehobener Stelle, aufsteigend nach
   Startdatum sortiert.

.. req:: Dringliche Termine nach Vorlauf
   :id: REQ_TER_DASH_02
   :status: approved
   :priority: high
   :links: REQ_TER_DASH_01

   Ein zukünftiger geplanter Termin ohne Trainerzuweisung wird als dringend
   markiert, wenn sein Startdatum weniger als vier Wochen entfernt ist. Bei
   genau vier Wochen oder mehr erscheint er weiterhin in der sortierten
   Übersicht, aber ohne Dringlichkeitsmarkierung.

   Die Markierung dient nur der visuellen Priorisierung. Sie blockiert nichts
   und löst keine automatische Aktion aus.

.. req:: Überfällige Termine stehen ganz oben
   :id: REQ_TER_DASH_04
   :status: approved
   :priority: high
   :links: REQ_TER_DASH_01, REQ_TER_STAT_06

   Termine, deren Enddatum vorüber ist und die noch geplant sind, stehen im
   Dashboard eines Administrators an erster Stelle -- vor den anstehenden
   Terminen. Abgesagte und abgeschlossene Termine erscheinen dort nicht.

   Sie sind der Regelweg, auf dem ein versäumter Abschluss auffällt. Der
   selbsttätige Abschluss nach zwei Kalendermonaten ist erst das Auffangnetz
   dahinter.

.. req:: Warnung bei zu wenigen Teilnehmern
   :id: REQ_TER_DASH_03
   :status: approved
   :priority: high
   :links: REQ_TER_DASH_02, REQ_TLN_GRENZ_01

   Erreicht ein exklusiver Termin weniger als vier Wochen vor dem Start seine
   Mindestteilnehmerzahl nicht, wird er im Dashboard ebenso als dringend
   markiert wie ein Termin ohne Trainer. Nur so bleibt Zeit, ihn abzusagen
   oder zu bewerben.

   Öffentliche Termine haben keine Mindestteilnehmerzahl und erhalten diese
   Dringlichkeitsmarkierung nicht. Für sie wird lediglich das Überschreiten
   der Höchstteilnehmerzahl nach :need:`REQ_TLN_GRENZ_01` gewarnt. Ohne
   Zugangsart findet keine Grenzprüfung statt.

Umgang mit den bestehenden Demo-Daten
-------------------------------------

.. decision:: Keine Migration der Demo-Termine
   :id: DEC_TER_MIGR_01
   :status: approved

   Die vorhandenen Termine sind ausschließlich Demo-Daten. Beim Umbau der
   Terminplanung werden sie verworfen und die Datenbank wird mit neuen, zum
   neuen Modell passenden Demo-Terminen befüllt.

   Der neue Seed bildet einen sinnvollen, zusammenhängenden Demo-Bestand
   ähnlich dem bisherigen ab. Er enthält vergangene, laufende und kommende
   sowie geplante, abgeschlossene und abgesagte Termine. Außerdem zeigt er
   Termine mit und ohne Trainer, öffentliche und exklusive Zugangsarten,
   Vor-Ort-, Remote- und Hybrid-Durchführungen sowie dazu passende
   Qualifikationen, Assistenzen und Teilnehmerbuchungen.

   Die Termindaten werden relativ zum Zeitpunkt des initialen Seedings
   bestimmt und liegen auf Werktagen. Dadurch bleibt eine frisch angelegte
   Demo-Datenbank unabhängig vom Kalenderjahr aussagekräftig. Genaue Anzahl,
   Namen und Datumsabstände sind Implementierungsdetails.

   Eine Umschlüsselung alter Zustände, Formate oder Kennungen ist nicht
   erforderlich. Es gibt keine produktiven Termindaten, die migriert werden
   müssen.

.. req:: Repräsentativer Demo-Bestand
   :id: REQ_TER_DEMO_01
   :status: approved
   :links: DEC_TER_MIGR_01

   Beim initialen Seeding entsteht der in :need:`DEC_TER_MIGR_01`
   beschriebene zusammenhängende Demo-Bestand mit relativen Werktagsdaten.
   Seine Verknüpfungen zwischen Schulungen, Qualifikationen, Trainern,
   Assistenzen und Teilnehmerbuchungen sind fachlich gültig.
