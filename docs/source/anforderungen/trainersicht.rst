Trainersicht
============

Die Trainersicht beantwortet einem Trainer drei Fragen: Was muss ich tun, was
könnte ich übernehmen, und worauf warte ich noch.

Dashboard
---------

Das Dashboard ist rollenübergreifend beschrieben und steht deshalb in einem
eigenen Bereich: :ref:`Dashboard <dashboard>`. Die folgenden Anforderungen
werden davon abgelöst und stehen nach dessen Freigabe auf ``superseded``.

.. req:: Dashboard als Einstieg
   :id: REQ_TRA_DASH_01
   :status: superseded
   :priority: high

   Ein Trainer hat ein Dashboard als Einstieg. Es führt seine Termine und
   die offenen Übernahmeanfragen an ihn zusammen -- also alles, was eine
   Handlung von ihm erwartet.

   Abgelöst durch :need:`REQ_DSH_GRUND_01`, das dasselbe für alle Rollen
   festlegt.

.. req:: Überfällige eigene Termine stehen ganz oben
   :id: REQ_TRA_DASH_02
   :status: superseded
   :priority: high
   :links: REQ_TRA_DASH_01, REQ_TER_STAT_02

   Eigene Termine, deren Enddatum vorüber ist und die noch geplant sind,
   stehen an erster Stelle -- vor den anstehenden. Abgesagte und
   abgeschlossene Termine erscheinen dort nicht.

   Abgelöst durch :need:`REQ_DSH_PFLI_01`.

.. req:: Offene Übernahmeanfragen im Dashboard
   :id: REQ_TRA_DASH_03
   :status: superseded
   :priority: high
   :links: REQ_TRA_DASH_01, REQ_UEB_ANFR_02

   Übernahmeanfragen anderer Trainer zu seinen Terminen erscheinen im
   Dashboard und werden dort entschieden.

   Abgelöst durch :need:`REQ_DSH_VORG_01`, das die Übernahmeanfrage als eine
   von sechs Vorgangsarten führt.

Terminübersicht
---------------

.. req:: Eigene zukünftige Termine
   :id: REQ_TRA_UEBER_01
   :status: draft
   :priority: high

   Ein Trainer sieht alle zukünftigen geplanten Termine, denen er als
   ausführender Trainer zugewiesen ist.

.. req:: Eigene Assistenzeinsätze
   :id: REQ_TRA_UEBER_05
   :status: draft
   :priority: high
   :links: REQ_TRA_UEBER_01, REQ_ASS_PLATZ_01

   Ein Trainer sieht alle zukünftigen geplanten Termine, denen er als
   Assistent zugewiesen ist. Sie binden ihn genauso wie eigene Termine.

.. req:: Übernehmbare Termine ohne Trainer
   :id: REQ_TRA_UEBER_02
   :status: draft
   :priority: high

   Ein Trainer sieht alle zukünftigen geplanten Termine ohne
   Trainerzuweisung, deren Schulung er aufgrund seiner Qualifikationen halten
   könnte.

.. req:: Termine mit anderem Trainer
   :id: REQ_TRA_UEBER_03
   :status: draft

   Ein Trainer sieht alle zukünftigen geplanten Termine, die einem anderen
   Trainer zugewiesen sind und deren Schulung er selbst halten könnte. Von
   dort aus kann er dem zugewiesenen Trainer die Übernahme anbieten.

.. req:: Freie Assistenzplätze
   :id: REQ_TRA_UEBER_06
   :status: draft
   :priority: high
   :links: REQ_ASS_BEW_01

   Ein Trainer sieht die zukünftigen geplanten Termine mit freien
   Assistenzplätzen und kann sich von dort aus bewerben. Das gilt für alle
   Schulungen, nicht nur für solche, für die er noch nicht qualifiziert ist.

.. req:: Eigene Abwesenheiten in derselben Übersicht
   :id: REQ_TRA_UEBER_07
   :status: draft
   :priority: high
   :links: REQ_TRA_UEBER_01, REQ_ABW_ERF_01

   Die eigenen Abwesenheiten erscheinen gemeinsam mit den Terminen in einer
   Übersicht. Ein Trainer sieht damit Einsätze und Abwesenheiten
   nebeneinander, statt sie an zwei Stellen zusammensuchen zu müssen.

.. req:: Unterscheidbarkeit der Kategorien
   :id: REQ_TRA_UEBER_04
   :status: draft
   :links: REQ_TRA_UEBER_01, REQ_TRA_UEBER_02, REQ_TRA_UEBER_03, REQ_TRA_UEBER_05, REQ_TRA_UEBER_06, REQ_TRA_UEBER_07

   Die Kategorien sind in der Übersicht voneinander unterscheidbar: eigener
   Termin als Trainer, eigener Termin als Assistent, Termin ohne Trainer,
   Termin mit anderem Trainer, freier Assistenzplatz und eigene Abwesenheit.

Benachrichtigungen
------------------

Benachrichtigungen sind rollenübergreifend beschrieben und stehen deshalb in
einem eigenen Bereich: :ref:`Benachrichtigungen <benachrichtigungen>`.

.. req:: Eigene Anzeige für Benachrichtigungen
   :id: REQ_TRA_NACHR_01
   :status: superseded
   :priority: high
   :links: REQ_DAT_NACHR_01

   Ein Trainer hat eine eigene Anzeige für seine Benachrichtigungen. Dort
   laufen alle Mitteilungen zusammen.

   Abgelöst durch :need:`REQ_NAC_GRUND_01`. Welche Anlässe eine Mitteilung
   erzeugen, steht abschließend in :need:`REQ_NAC_ANL_01`.

.. req:: Ungelesene Benachrichtigungen sind erkennbar
   :id: REQ_TRA_NACHR_02
   :status: superseded
   :priority: high
   :links: REQ_TRA_NACHR_01

   Ungelesene Benachrichtigungen sind als solche erkennbar, ohne die Anzeige
   zu öffnen.

   Abgelöst durch :need:`REQ_NAC_ANZ_01`.

.. req:: Benachrichtigungen sind keine Aufgaben
   :id: REQ_TRA_NACHR_03
   :status: superseded
   :links: REQ_TRA_NACHR_01, REQ_TRA_DASH_01

   In den Benachrichtigungen stehen Mitteilungen über Geschehenes. Was eine
   Handlung des Trainers erwartet, gehört ins Dashboard und nicht dorthin.

   Abgelöst durch :need:`REQ_NAC_GRUND_02` und :need:`REQ_DSH_GRUND_02`; die
   Trennung selbst steht in :need:`DEC_NAC_TRENNUNG_01`.

Offene eigene Vorgänge
----------------------

Die eigenen offenen Vorgänge stehen nach :need:`REQ_DSH_VORG_02` im
Dashboard, getrennt von den Vorgängen, über die der Trainer entscheidet.

.. req:: Liste der offenen eigenen Vorgänge
   :id: REQ_TRA_VORG_01
   :status: superseded
   :priority: high

   Ein Trainer sieht in einer eigenen Liste, worauf er noch wartet: seine
   Bewerbungen auf Qualifikationen, seine Vormerkungen, seine
   Assistenzbewerbungen, seine Abwesenheitsanträge und seine gestellten
   Übernahmeanfragen.

   Abgelöst durch :need:`REQ_DSH_VORG_02`.

.. req:: Zurückziehen aus der Liste heraus
   :id: REQ_TRA_VORG_02
   :status: superseded
   :links: REQ_TRA_VORG_01, REQ_QUA_BEW_07, REQ_VOR_ABG_03, REQ_ABW_ERF_06, REQ_UEB_ANFR_04

   Jeder Vorgang, der zurückgezogen werden kann, lässt sich aus dieser Liste
   heraus zurückziehen.

   Abgelöst durch :need:`REQ_DSH_VORG_07`.

Vergangene Termine
------------------

.. req:: Eigene vergangene Termine einsehen
   :id: REQ_TRA_HIST_02
   :status: draft
   :links: REQ_TRA_UEBER_01

   Ein Trainer kann die Termine einsehen, die er gehalten hat. Sie erscheinen
   nicht in der Übersicht der anstehenden Termine.

.. req:: Abgeschlossene Termine sind unveränderlich
   :id: REQ_TRA_HIST_01
   :status: draft
   :priority: high
   :links: REQ_TER_STAT_01, REQ_TLN_TEIL_03

   Ein abgeschlossener Termin wird nicht mehr verändert und bleibt dauerhaft
   erhalten.

   Maßgeblich ist der Abschluss, nicht das Enddatum: Zwischen Enddatum und
   Abschluss passt der Trainer die Teilnehmerbuchungen noch an, wie
   :need:`REQ_TLN_TEIL_02` es verlangt.
