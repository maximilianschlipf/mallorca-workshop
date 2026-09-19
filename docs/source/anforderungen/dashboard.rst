.. _dashboard:

Dashboard
=========

Das Dashboard ist der Einstieg in die Anwendung und beantwortet eine einzige
Frage: *Was liegt bei mir?* Es gab es bisher zweimal -- einmal für Trainer in
der Trainersicht, einmal für Administratoren in der Terminplanung --, und
beide Beschreibungen kannten nur einen Teil dessen, was tatsächlich eine
Handlung erwartet.

Dieser Bereich fasst beide zusammen. Er beschreibt **Vorgänge**: alles, worüber
jemand mit Annehmen oder Ablehnen entscheidet. Was nur zur Kenntnis genommen
wird, steht in den :ref:`Benachrichtigungen <benachrichtigungen>` und nicht
hier.

Grundsatz
---------

.. req:: Dashboard als Einstieg
   :id: REQ_DSH_GRUND_01
   :status: review
   :priority: high
   :links: DEC_NAC_TRENNUNG_01

   Jeder angemeldete Benutzer hat ein Dashboard als Einstieg. Es führt
   zusammen, was eine Handlung von ihm erwartet -- unabhängig davon, aus
   welchem Fachbereich es stammt.

   Welche Rolle der Benutzer hat, bestimmt den Inhalt, nicht den Aufbau: Der
   Aufbau nach :need:`REQ_DSH_GRUND_03` ist für alle Rollen derselbe.

.. req:: Nur Handlungsbedarf gehört ins Dashboard
   :id: REQ_DSH_GRUND_02
   :status: review
   :priority: high
   :links: REQ_DSH_GRUND_01, DEC_NAC_TRENNUNG_01, REQ_NAC_GRUND_02

   Im Dashboard steht, was eine Handlung erwartet. Mitteilungen über
   Geschehenes gehören in die Benachrichtigungen und erscheinen nicht im
   Dashboard.

.. req:: Drei Ränge und ihre Reihenfolge
   :id: REQ_DSH_GRUND_03
   :status: review
   :priority: high
   :links: REQ_DSH_GRUND_01

   Das Dashboard gliedert sich für jede Rolle in drei Ränge, in dieser
   Reihenfolge:

   #. **Vorgänge**, über die der Benutzer entscheidet -- Annehmen oder
      Ablehnen nach :need:`REQ_DSH_VORG_01`.
   #. **Eigene Handlungspflichten** ohne Gegenüber, die niemand sonst
      erledigen kann -- etwa eine ausstehende Durchführungsbestätigung.
   #. **Dringlichkeiten** ohne festgelegte Handlung, die nur Aufmerksamkeit
      verlangen -- etwa ein Termin ohne Trainer.

   Die Reihenfolge folgt dem Grad der Verbindlichkeit: Ein Vorgang lässt
   jemand anderen warten, eine Handlungspflicht nur den Benutzer selbst, eine
   Dringlichkeit niemanden unmittelbar.

   Rang, Dringlichkeit und Zustand sind programmatisch erkennbar und werden
   nicht allein durch Farbe vermittelt. Alle Aktionen sowie der auf- und
   zuklappbare Abschnitt der erledigten Vorgänge sind mit der Tastatur
   bedienbar; nach einer Aktion bleibt der Fokus am geänderten Vorgang oder
   wechselt nachvollziehbar zur zugehörigen Statusmeldung.

Vorgänge
--------

.. req:: Was ein Vorgang ist
   :id: REQ_DSH_VORG_01
   :status: review
   :priority: high
   :links: REQ_DSH_GRUND_03, DEC_NAC_TRENNUNG_01

   Ein Vorgang ist eine gerichtete Bitte um Entscheidung: Eine Person stellt
   sie, eine andere Person oder eine Rolle entscheidet sie mit Annehmen oder
   Ablehnen. Es gibt genau diese Vorgangsarten:

   .. list-table::
      :header-rows: 1
      :widths: 24 20 20 36

      * - Vorgangsart
        - Gestellt von
        - Entschieden von
        - Fachliche Anforderung
      * - Abwesenheitsantrag
        - Trainer
        - Administrator
        - :need:`REQ_ABW_KONFL_02`, :need:`REQ_ABW_KONFL_04`,
          :need:`REQ_ABW_KONFL_05`
      * - Freigabeanfrage für eine Qualifikation
        - Trainer
        - Administrator
        - :need:`REQ_QUA_BEW_02`
      * - Vormerkung auf einen Termin
        - Trainer
        - Administrator
        - :need:`REQ_VOR_BEST_01`, :need:`REQ_VOR_ABL_01`
      * - Bewerbung auf einen Assistenzplatz
        - Trainer
        - zugewiesener Trainer oder Administrator
        - :need:`REQ_ASS_BEW_02`
      * - Übernahmeanfrage
        - Trainer
        - zugewiesener Trainer
        - :need:`REQ_UEB_ANFR_02`
      * - Ersatztrainer-Anfrage bei Abwesenheit
        - abwesender Trainer
        - vorgeschlagener Ersatztrainer
        - :need:`REQ_ABW_TAUSCH_03`

   Eine ausstehende Durchführungsbestätigung ist kein Vorgang in diesem
   Sinne: Sie hat kein Gegenüber, das auf eine Antwort wartet, und kennt
   kein Ablehnen. Sie gehört in den zweiten Rang nach
   :need:`REQ_DSH_PFLI_01`.

.. req:: Beide Richtungen im Dashboard
   :id: REQ_DSH_VORG_02
   :status: review
   :priority: high
   :links: REQ_DSH_VORG_01, REQ_DSH_GRUND_03

   Das Dashboard führt Vorgänge in beiden Richtungen, getrennt voneinander:

   * **An mich gerichtet** -- Vorgänge, über die ich entscheide. Sie stehen
     im ersten Rang nach :need:`REQ_DSH_GRUND_03`.
   * **Von mir gestellt** -- eigene Vorgänge, auf deren Entscheidung ich
     warte. Sie stehen nach den drei Rängen. Soweit der Fachbereich das
     erlaubt, lassen sie sich dort zurückziehen.

   Ein Trainer sieht damit an einer Stelle, worüber er zu entscheiden hat und
   worauf er selbst wartet -- seine Bewerbungen, Vormerkungen,
   Assistenzbewerbungen, Abwesenheitsanträge, gestellten
   Übernahmeanfragen und Ersatztrainer-Anfragen.

.. req:: Einheitliche Darstellung eines Vorgangs
   :id: REQ_DSH_VORG_03
   :status: review
   :priority: high
   :links: REQ_DSH_VORG_01

   Jeder Vorgang wird nach demselben Muster dargestellt, unabhängig von
   seiner Art: Vorgangsart, wer ihn gestellt hat, worauf er sich bezieht
   -- Termin, Schulung oder Zeitraum --, wann er gestellt wurde und der
   Sprung zum betroffenen Gegenstand.

   Bei einem an mich gerichteten Vorgang stehen dort die beiden Aktionen
   Annehmen und Ablehnen. Bei einem von mir gestellten Vorgang steht das
   Zurückziehen nach :need:`REQ_DSH_VORG_07` nur dann bereit, wenn sein
   Fachbereich es erlaubt.

.. req:: Eine Entscheidung ist endgültig
   :id: REQ_DSH_VORG_04
   :status: review
   :priority: high
   :links: REQ_DSH_VORG_01

   Über einen Vorgang wird genau einmal entschieden. Danach ist er
   entschieden und lässt sich weder umentscheiden noch erneut entscheiden;
   er verlässt den ersten Rang und erscheint nach
   :need:`REQ_DSH_VORG_08` unter den erledigten Vorgängen.

   Eine Entscheidung zieht unmittelbar fachliche Folgen nach sich -- eine
   Zuweisung entsteht oder entfällt. Eine Rücknahme wäre deshalb keine
   Korrektur, sondern ein zweiter fachlicher Vorgang; dafür gibt es die
   Wege des jeweiligen Fachbereichs.

.. req:: Begründung bei einer Ablehnung
   :id: REQ_DSH_VORG_05
   :status: review
   :links: REQ_DSH_VORG_04, REQ_QUA_BEW_08, REQ_VOR_ABL_04

   Ob eine Ablehnung eine Begründung verlangt, bestimmt die Vorgangsart. Für
   die Freigabeanfrage nach :need:`REQ_QUA_BEW_08` und die Vormerkung nach
   :need:`REQ_VOR_ABL_04` ist sie Pflicht; bei den übrigen Vorgangsarten
   entfällt sie.

   Wo eine Begründung verlangt ist, erreicht sie den Antragsteller mit der
   Mitteilung nach :need:`REQ_NAC_ANL_01`. Eine Annahme wird nie begründet.

.. req:: Vorgänge mit mehreren Zuständigen
   :id: REQ_DSH_VORG_06
   :status: review
   :priority: high
   :links: REQ_DSH_VORG_01, REQ_ASS_BEW_02

   Sind für einen Vorgang mehrere Stellen zuständig, erscheint er im
   Dashboard jeder dieser Stellen. Wer zuerst entscheidet, entscheidet für
   alle: Der Vorgang verlässt bei allen Zuständigen gleichzeitig den ersten
   Rang und ist bei allen als erledigt zu sehen, mit Angabe, wer entschieden
   hat.

   Das betrifft die Bewerbung auf einen Assistenzplatz, über die nach
   :need:`REQ_ASS_BEW_02` der zugewiesene Trainer oder ein Administrator
   entscheidet. Ist dem Termin kein Trainer zugewiesen, bleibt der
   Adminbereich allein zuständig.

.. req:: Zurückziehen aus dem Dashboard heraus
   :id: REQ_DSH_VORG_07
   :status: review
   :links: REQ_DSH_VORG_02, REQ_QUA_BEW_07, REQ_VOR_ABG_03, REQ_ASS_BEW_04, REQ_ABW_ERF_06, REQ_UEB_ANFR_04

   Jeder eigene Vorgang, der nach seinem Fachbereich zurückgezogen werden
   kann, lässt sich aus dem Dashboard heraus zurückziehen, solange er nicht
   entschieden ist.

   Ein zurückgezogener Vorgang gilt als erledigt nach
   :need:`REQ_DSH_VORG_08`. Er erzeugt keine Mitteilung: Es hat niemand
   entschieden, und der Zurückziehende weiß, was er getan hat.

.. req:: Erledigte Vorgänge bleiben dauerhaft
   :id: REQ_DSH_VORG_08
   :status: review
   :priority: high
   :links: REQ_DSH_VORG_04, REQ_DSH_VORG_02

   Entschiedene, entfallene und zurückgezogene Vorgänge bleiben dauerhaft in
   einem eigenen Abschnitt des Dashboards, getrennt nach denselben beiden
   Richtungen wie die offenen. Der Abschnitt ist zusammengeklappt, solange
   der Benutzer ihn nicht öffnet.

   Er nennt zu jedem Vorgang das Ergebnis, den Zeitpunkt der Entscheidung
   und -- sofern jemand entschieden hat -- wer das war.

   Wird ein beteiligtes Benutzerkonto gelöscht, bleibt der erledigte Vorgang
   erhalten. Der Name von Antragsteller und Entscheider wird dafür wie bei
   abgeschlossenen Terminen als unveränderlicher Snapshot geführt; ein
   Verweis auf das gelöschte Konto bleibt nicht bestehen.

   Er ist standardmäßig zugeklappt, weil das Dashboard die Frage "Was liegt
   bei mir?" beantwortet und Erledigtes darauf keine Antwort ist.

.. req:: Entfallene Vorgänge werden als erledigt geführt
   :id: REQ_DSH_VORG_09
   :status: review
   :priority: high
   :links: REQ_DSH_VORG_08, REQ_VOR_ABL_02, REQ_UEB_ENTS_06, REQ_KAT_ARCH_05, REQ_ABW_AUTO_01, REQ_TER_AEND_05, REQ_USR_ENDE_01, REQ_USR_ENDE_02

   Ein Vorgang kann auch ohne Entscheidung enden, weil ein anderes Ereignis
   ihn gegenstandslos macht. Er gilt dann als erledigt, und das Ergebnis
   nennt das Ereignis als Grund statt einer entscheidenden Person.

   Das betrifft: die übrigen Vormerkungen nach der Zuweisung eines Trainers
   (:need:`REQ_VOR_ABL_02`), die übrigen Übernahmeanfragen nach einem Tausch
   (:need:`REQ_UEB_ENTS_06`), offene Freigabeanfragen beim Archivieren einer
   Schulung (:need:`REQ_KAT_ARCH_05`), den nach Fristablauf selbsttätig
   genehmigten Abwesenheitsantrag (:need:`REQ_ABW_AUTO_01`), die nach einer
   Woche abgelaufene oder bei der erneuten Prüfung fachlich ungültige
   Ersatztrainer-Anfrage (:need:`REQ_ABW_TAUSCH_04`,
   :need:`REQ_ABW_TAUSCH_05`) sowie
   alle noch offenen Vormerkungen, Assistenzbewerbungen und
   Übernahmeanfragen, wenn ihr Termin abgesagt oder gelöscht wird. Ein
   Abwesenheitsantrag oder eine Ersatztrainer-Anfrage endet aus diesem Grund
   nur, wenn nach dem Entfernen des Termins kein betroffener Termin übrig
   bleibt (:need:`REQ_TER_AEND_05`).

   Wird der adressierte Trainer stillgelegt oder gelöscht, endet eine offene
   Übernahmeanfrage ebenfalls als entfallen. Eine an ihn gerichtete
   Ersatztrainer-Anfrage endet als entfallen und wird für ihren Antragsteller
   in einen neuen regulären Abwesenheitsantrag überführt
   (:need:`REQ_USR_ENDE_01`, :need:`REQ_USR_ENDE_02`).

   Wird der Antragsteller selbst stillgelegt oder gelöscht, enden alle seine
   noch offenen Vorgänge als entfallen. Sie bleiben mit dem Kontoende als
   Grund in der Historie und lassen sich nicht mehr entscheiden.

.. req:: Jede Entscheidung erreicht den Antragsteller
   :id: REQ_DSH_VORG_10
   :status: review
   :priority: high
   :links: REQ_DSH_VORG_04, REQ_NAC_ANL_01, REQ_NAC_ANL_04

   Sobald ein Vorgang entschieden ist oder entfällt, entsteht beim
   Antragsteller genau eine Mitteilung nach :need:`REQ_NAC_ANL_01`. Der
   Entscheider erhält keine -- für ihn ist der erledigte Vorgang nach
   :need:`REQ_DSH_VORG_08` der Beleg. Entscheidet ein Administrator einen
   eigenen Vorgang nach :need:`DEC_USR_SELBST_01`, gilt die Regel für den
   Auslöser: Es entsteht keine Mitteilung an ihn selbst.

   Entfällt ein Vorgang durch Kontoende des Antragstellers, entsteht die
   Mitteilung nur, wenn sein Konto fortbesteht und er die Stilllegung nicht
   selbst ausgelöst hat. Die Historie nach :need:`REQ_DSH_VORG_08` bleibt
   unabhängig davon erhalten.

.. req:: Nur Zuständige entscheiden und Antragsteller ziehen zurück
   :id: REQ_DSH_SICHER_01
   :status: review
   :priority: high
   :links: REQ_DSH_VORG_01, REQ_DSH_VORG_07, REQ_USR_LOGIN_02

   Einen Vorgang sehen und entscheiden ausschließlich seine nach
   :need:`REQ_DSH_VORG_01` zuständigen Personen oder Rollen. Zurückziehen
   darf ihn ausschließlich sein Antragsteller. Direkte Aufrufe mit einer
   fremden Vorgangs-ID geben weder Inhalt noch Existenz preis. Annehmen,
   Ablehnen und Zurückziehen verlangen den CSRF-Schutz der Anwendung.

Eigene Handlungspflichten
-------------------------

.. req:: Überfällige eigene Termine stehen im zweiten Rang
   :id: REQ_DSH_PFLI_01
   :status: review
   :priority: high
   :links: REQ_DSH_GRUND_03, REQ_TER_STAT_02

   Eigene Termine, deren Enddatum vorüber ist und die noch geplant sind,
   stehen im zweiten Rang des Dashboards. Abgesagte und abgeschlossene
   Termine erscheinen dort nicht.

   Ohne diese Anzeige könnte ein Trainer die Durchführung seines Termins gar
   nicht bestätigen, weil er ihn nicht mehr fände. Jeder Termin liefe dann
   in den selbsttätigen Abschluss nach :need:`REQ_TER_STAT_06`.

Dringlichkeiten
---------------

.. req:: Termine ohne Trainer prominent anzeigen
   :id: REQ_DSH_DRIN_01
   :status: review
   :priority: high
   :links: REQ_DSH_GRUND_03, REQ_TER_ANL_02

   Das Dashboard eines Administrators zeigt im dritten Rang alle zukünftigen
   geplanten Termine ohne Trainerzuweisung, aufsteigend nach Startdatum
   sortiert.

.. req:: Überfällige Termine stehen ganz oben
   :id: REQ_DSH_DRIN_02
   :status: review
   :priority: high
   :links: REQ_DSH_DRIN_01, REQ_TER_STAT_06

   Termine, deren Enddatum vorüber ist und die noch geplant sind, stehen im
   Dashboard eines Administrators vor den anstehenden Terminen. Abgesagte
   und abgeschlossene Termine erscheinen dort nicht.

   Sie sind der Regelweg, auf dem ein versäumter Abschluss auffällt. Der
   selbsttätige Abschluss nach zwei Kalendermonaten ist erst das Auffangnetz
   dahinter.

.. req:: Dringliche Termine nach Vorlauf
   :id: REQ_DSH_DRIN_03
   :status: review
   :priority: high
   :links: REQ_DSH_DRIN_01

   Ein zukünftiger geplanter Termin ohne Trainerzuweisung wird als dringend
   markiert, wenn sein Startdatum weniger als vier Wochen entfernt ist. Bei
   genau vier Wochen oder mehr erscheint er weiterhin in der sortierten
   Übersicht, aber ohne Dringlichkeitsmarkierung.

   Die Markierung dient nur der visuellen Priorisierung. Sie blockiert nichts
   und löst keine automatische Aktion aus.

.. req:: Warnung bei zu wenigen Teilnehmern
   :id: REQ_DSH_DRIN_04
   :status: review
   :priority: high
   :links: REQ_DSH_DRIN_03, REQ_TLN_GRENZ_01

   Erreicht ein exklusiver Termin weniger als vier Wochen vor dem Start seine
   Mindestteilnehmerzahl nicht, wird er im Dashboard ebenso als dringend
   markiert wie ein Termin ohne Trainer. Nur so bleibt Zeit, ihn abzusagen
   oder zu bewerben.

   Öffentliche Termine haben keine Mindestteilnehmerzahl und erhalten diese
   Dringlichkeitsmarkierung nicht. Für sie wird lediglich das Überschreiten
   der Höchstteilnehmerzahl nach :need:`REQ_TLN_GRENZ_01` gewarnt. Ohne
   Zugangsart findet keine Grenzprüfung statt.

Ablösung der bisherigen Dashboard-Anforderungen
-----------------------------------------------

.. note::

   Die Anforderungen dieses Bereichs lösen ``REQ_TRA_DASH_01``,
   ``REQ_TRA_DASH_02``, ``REQ_TRA_VORG_01``, ``REQ_TRA_VORG_02``,
   ``REQ_TRA_NACHR_01`` bis ``REQ_TRA_NACHR_03`` sowie ``REQ_TER_DASH_01``
   bis ``REQ_TER_DASH_04`` ab. Die abgelösten Anforderungen behalten ihren
   bisherigen Status, solange dieser Bereich in Prüfung ist -- die
   freigegebenen ``REQ_TER_DASH_*`` bleiben mit ihren Abnahmetests in Kraft.

   Mit der Freigabe dieses Bereichs wechseln sie auf ``superseded``, und die
   ``:verifies:``-Einträge von ``TEST_TER_DASH_01`` bis ``TEST_TER_DASH_05``
   werden auf die neuen IDs gezogen. Vorher darf das nicht geschehen: Sonst
   verlören freigegebene Anforderungen ihren Nachweis, obwohl fachlich
   nichts entschieden ist.
