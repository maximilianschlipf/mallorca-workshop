.. _benachrichtigungen:

Benachrichtigungen
==================

Das System teilt seinen Benutzern laufend etwas mit: dass ein Termin abgesagt
wurde, dass eine Bewerbung entschieden ist, dass eine Qualifikation entzogen
wurde. Bis hierher stand jede dieser Mitteilungen einzeln in dem Bereich, der
sie auslöst -- mit dem Ergebnis, dass es zwar zwei Dutzend Auslöser gab, aber
keine Stelle, an der stand, was eine Benachrichtigung überhaupt ist, wer sie
sieht und wie lange sie bleibt.

Dieser Bereich schließt diese Lücke. Er beschreibt **Mitteilungen**: alles,
was ein Benutzer zur Kenntnis nimmt, ohne darauf antworten zu müssen. Was eine
Entscheidung verlangt, steht im :ref:`Dashboard <dashboard>` und nicht hier.

Grundsatz
---------

.. decision:: Kenntnisnahme und Entscheidung sind getrennte Orte
   :id: DEC_NAC_TRENNUNG_01
   :status: review

   Was das System einem Benutzer zu sagen hat, wird nach der erwarteten
   Reaktion getrennt:

   * Eine **Mitteilung** wird zur Kenntnis genommen. Sie berichtet über
     etwas, das bereits geschehen ist, und bietet keine Aktion an. Sie
     erscheint in den Benachrichtigungen.
   * Ein **Vorgang** verlangt eine Entscheidung mit Annehmen oder Ablehnen.
     Er erscheint im Dashboard und wird dort entschieden.

   Begründung: Die beiden Arten haben unterschiedliche Lebensdauern und
   unterschiedliche Dringlichkeiten. Ein offener Vorgang blockiert jemand
   anderen und muss deshalb sichtbar bleiben, bis er entschieden ist; eine
   Mitteilung ist mit dem Lesen erledigt. Lägen beide in derselben Liste,
   ginge das Entscheidungsbedürftige zwischen dem Berichtenden unter -- und
   genau das ist der Fall, den das Dashboard verhindern soll.

   Die Trennung verläuft entlang der Reaktion, nicht entlang des Themas:
   Dieselbe Sache erzeugt nacheinander beides. Eine gestellte
   Freigabeanfrage ist ein Vorgang beim Administrator; die Entscheidung
   darüber ist eine Mitteilung beim Trainer.

.. req:: Eigene Anzeige für Benachrichtigungen
   :id: REQ_NAC_GRUND_01
   :status: review
   :priority: high
   :links: DEC_NAC_TRENNUNG_01, REQ_DAT_NACHR_01
   :supersedes: REQ_TRA_NACHR_01

   Jeder angemeldete Benutzer hat eine eigene Anzeige für die
   Benachrichtigungen, die an ihn gerichtet sind. Dort laufen alle
   Mitteilungen zusammen, die ihn betreffen.

   Die Anzeige ist von jeder Seite der Anwendung aus erreichbar.

.. req:: Benachrichtigungen verlangen keine Handlung
   :id: REQ_NAC_GRUND_02
   :status: review
   :priority: high
   :links: REQ_NAC_GRUND_01, DEC_NAC_TRENNUNG_01, REQ_DSH_GRUND_02

   In den Benachrichtigungen stehen ausschließlich Mitteilungen über
   Geschehenes. Sie bieten keine Entscheidung an -- weder Annehmen noch
   Ablehnen noch Zurückziehen. Was eine Handlung des Benutzers erwartet,
   gehört ins Dashboard.

   Ein Sprung zum betroffenen Gegenstand nach :need:`REQ_NAC_ANZ_03` ist
   keine Handlung in diesem Sinne: Er zeigt nur, worum es geht.

.. req:: Benachrichtigungen gelten für alle Rollen
   :id: REQ_NAC_GRUND_03
   :status: review
   :links: REQ_NAC_GRUND_01

   Die Benachrichtigungen sind nicht auf Trainer beschränkt. Administratoren
   haben dieselbe Anzeige; sie erhalten dort die Mitteilungen, die nach
   :need:`REQ_NAC_ANL_01` an den Adminbereich gerichtet sind.

Anlässe
-------

.. req:: Katalog der Anlässe
   :id: REQ_NAC_ANL_01
   :status: review
   :priority: high
   :links: REQ_NAC_GRUND_01

   Eine Mitteilung entsteht genau bei den folgenden Anlässen. Die Spalte
   "Pflichtinhalte" nennt, was die Mitteilung über Anlass und Zeitpunkt
   hinaus enthalten muss.

   .. list-table::
      :header-rows: 1
      :widths: 26 18 30 26

      * - Anlass
        - Empfänger
        - Pflichtinhalte
        - Auslösende Anforderung
      * - Trainerzuweisung gesetzt
        - zugewiesener Trainer
        - Termin
        - :need:`REQ_TER_ZUW_07`
      * - Trainerzuweisung beendet
        - bisheriger Trainer
        - Termin
        - :need:`REQ_TER_ZUW_04`, :need:`REQ_TER_ZUW_05`
      * - Rollenwechsel zum Trainer
        - betroffene Person
        - Termin, bisherige und neue Rolle
        - :need:`REQ_TER_ZUW_08`
      * - Termin geändert
        - Trainer und Assistenten
        - je geändertem Feld alter und neuer Wert; beim Online-Zugang
          ausschließlich die neue URL
        - :need:`REQ_TER_AEND_12`
      * - Termin abgesagt
        - Trainer und Assistenten
        - Termin, Absagegrund falls angegeben
        - :need:`REQ_TER_AEND_05`
      * - Termin gelöscht
        - Trainer und Assistenten
        - Termin
        - :need:`REQ_TER_AEND_05`
      * - Termin selbsttätig abgeschlossen
        - zugewiesener Trainer
        - Termin, Hinweis auf die ungeprüften Teilnehmerzahlen
        - :need:`REQ_TER_STAT_06`
      * - Freigabeanfrage genehmigt
        - antragstellender Trainer
        - Schulung
        - :need:`REQ_QUA_BEW_03`
      * - Freigabeanfrage abgelehnt
        - antragstellender Trainer
        - Schulung, Begründung
        - :need:`REQ_QUA_BEW_03`, :need:`REQ_QUA_BEW_08`
      * - Freigabeanfrage durch Archivierung abgelehnt
        - antragstellender Trainer
        - Schulung, Archivierung als Grund
        - :need:`REQ_KAT_ARCH_05`
      * - Qualifikation direkt vergeben
        - betroffener Trainer
        - Schulung
        - :need:`REQ_QUA_DIREKT_03`
      * - Qualifikation entzogen
        - betroffener Trainer
        - Schulung
        - :need:`REQ_QUA_ENTZ_03`
      * - Qualifikation abgelegt
        - Adminbereich
        - Trainer, Schulung
        - :need:`REQ_QUA_ABLEGEN_04`
      * - Vormerkung bestätigt
        - vorgemerkter Trainer
        - Termin
        - :need:`REQ_VOR_BEST_02`
      * - Vormerkung abgelehnt
        - vorgemerkter Trainer
        - Termin, Begründung
        - :need:`REQ_VOR_ABL_03`, :need:`REQ_VOR_ABL_04`
      * - Vormerkung durch Zuweisung entfallen
        - vorgemerkter Trainer
        - Termin, Zuweisung eines anderen Trainers als Grund
        - :need:`REQ_VOR_ABL_02`, :need:`REQ_VOR_ABL_03`
      * - Assistenzbewerbung entschieden
        - beworbener Trainer
        - Termin, Ergebnis; bei Annahme die entstandene Assistenzzuweisung
        - :need:`REQ_ASS_BEW_02`
      * - Übernahmeanfrage entschieden
        - anfragender Trainer
        - Termin, Ergebnis
        - :need:`REQ_UEB_ENTS_03`
      * - Übernahmeanfrage durch Tausch entfallen
        - übrige anfragende Trainer
        - Termin
        - :need:`REQ_UEB_ENTS_06`
      * - Trainerwechsel durch Übernahme
        - Adminbereich
        - Termin, bisheriger und neuer Trainer
        - :need:`REQ_UEB_ENTS_05`
      * - Abwesenheitsantrag abgelehnt
        - antragstellender Trainer
        - Zeitraum
        - :need:`REQ_ABW_KONFL_05`
      * - Abwesenheitsantrag nach Fristablauf genehmigt
        - antragstellender Trainer und Adminbereich
        - Zeitraum, frei gewordene Termine
        - :need:`REQ_ABW_AUTO_02`

.. req:: Keine Mitteilung ohne Anlass im Katalog
   :id: REQ_NAC_ANL_02
   :status: review
   :priority: high
   :links: REQ_NAC_ANL_01

   Das System erzeugt keine Mitteilung zu einem Anlass, der nicht in
   :need:`REQ_NAC_ANL_01` steht. Ein neuer Anlass wird dort aufgenommen,
   bevor er ausgelöst wird.

   Ohne diese Regel wächst der Bestand an Mitteilungen unkontrolliert, und
   niemand kann mehr sagen, was ein Benutzer erwarten darf und was nicht.

.. req:: Ein Ereignis, eine Mitteilung je Empfänger
   :id: REQ_NAC_ANL_03
   :status: review
   :links: REQ_NAC_ANL_01

   Betrifft ein Anlass mehrere Empfänger, entsteht für jeden von ihnen eine
   eigene Mitteilung mit eigenem Lesezustand. Der Adminbereich zählt dabei
   nach :need:`REQ_NAC_ADM_01` als ein einziger Empfänger.

   Ändert ein Administrator mehrere Felder eines Termins in einem Vorgang,
   entsteht je geändertem Feld eine Mitteilung -- so, wie
   :need:`REQ_TER_AEND_12` es für den Inhalt verlangt.

.. req:: Der Auslöser erhält keine Mitteilung
   :id: REQ_NAC_ANL_04
   :status: review
   :links: REQ_NAC_ANL_01

   Wer eine Handlung selbst ausgelöst hat, erhält darüber keine Mitteilung.
   Ein Administrator, der einen Termin absagt, findet die Absage nicht in
   seinen eigenen Benachrichtigungen.

   Das gilt auch, wenn er als Trainer desselben Termins zugewiesen ist: Die
   eigene Handlung ist ihm bekannt.

.. req:: Teilnehmer erhalten keine Mitteilungen
   :id: REQ_NAC_ANL_05
   :status: review
   :priority: high
   :links: REQ_NAC_ANL_01, REQ_TER_AEND_08

   Gebuchte Teilnehmer sind keine Empfänger von Mitteilungen. Sie haben kein
   Benutzerkonto, und eine Teilnehmerbuchung führt keine Anschrift. Die
   Information der Teilnehmer erfolgt außerhalb des Systems.

Anzeige
-------

.. req:: Ungelesene Benachrichtigungen sind erkennbar
   :id: REQ_NAC_ANZ_01
   :status: review
   :priority: high
   :links: REQ_NAC_GRUND_01
   :supersedes: REQ_TRA_NACHR_02

   Ungelesene Benachrichtigungen sind als solche erkennbar, ohne die Anzeige
   zu öffnen. Erkennbar ist dabei nicht nur, dass es ungelesene gibt,
   sondern auch wie viele.

.. req:: Neueste Mitteilung zuerst
   :id: REQ_NAC_ANZ_02
   :status: review
   :links: REQ_NAC_GRUND_01

   Die Mitteilungen erscheinen absteigend nach ihrem Zeitpunkt, die neueste
   zuerst. Gelesene und ungelesene stehen in derselben Liste; der Lesezustand
   ändert die Reihenfolge nicht.

.. req:: Inhalt einer Mitteilung
   :id: REQ_NAC_ANZ_03
   :status: review
   :priority: high
   :links: REQ_NAC_GRUND_01, REQ_NAC_ANL_01

   Eine Mitteilung nennt ihren Anlass in verständlicher Sprache, ihren
   Zeitpunkt und die Pflichtinhalte aus :need:`REQ_NAC_ANL_01`. Bezieht sie
   sich auf einen Termin oder eine Schulung, führt sie dorthin.

.. req:: Gelesen durch Anzeigen
   :id: REQ_NAC_ANZ_04
   :status: review
   :priority: high
   :links: REQ_NAC_ANZ_01, REQ_DAT_NACHR_01

   Eine Mitteilung gilt als gelesen, sobald sie dem Empfänger in der Anzeige
   dargestellt wurde. Zusätzlich lassen sich alle ungelesenen Mitteilungen
   in einem Schritt als gelesen markieren.

   Der Lesezustand ist nicht umkehrbar: Eine gelesene Mitteilung lässt sich
   nicht wieder auf ungelesen setzen. Sie bleibt über
   :need:`REQ_NAC_ANZ_05` auffindbar, und ein erneutes Hervorheben würde
   nur vortäuschen, dass etwas Neues geschehen sei.

.. req:: Mitteilungen bleiben dauerhaft
   :id: REQ_NAC_ANZ_05
   :status: review
   :links: REQ_NAC_GRUND_01, REQ_USR_ENDE_02

   Mitteilungen werden nicht durch Zeitablauf entfernt und lassen sich vom
   Empfänger nicht löschen. Sie enden allein mit dem Benutzerkonto, zu dem
   sie gehören.

   Sie sind der einzige dauerhafte Nachweis darüber, worüber ein Benutzer
   informiert wurde. Ein Löschrecht des Empfängers würde genau diesen
   Nachweis entwerten.

.. req:: Entfallener Bezug macht die Mitteilung nicht ungültig
   :id: REQ_NAC_ANZ_06
   :status: review
   :links: REQ_NAC_ANZ_03, REQ_NAC_ANZ_05

   Ist der Gegenstand einer Mitteilung nicht mehr vorhanden -- etwa ein
   gelöschter Termin --, bleibt die Mitteilung mit ihrem Text erhalten. Der
   Sprung dorthin entfällt, und die Anzeige weist darauf hin, dass der
   Gegenstand nicht mehr besteht.

Mitteilungen an den Adminbereich
--------------------------------

.. req:: Der Adminbereich ist ein gemeinsamer Empfänger
   :id: REQ_NAC_ADM_01
   :status: review
   :priority: high
   :links: REQ_NAC_GRUND_03, REQ_NAC_ANL_01

   Eine Mitteilung an den Adminbereich richtet sich an die Rolle, nicht an
   eine bestimmte Person. Sie entsteht einmal und erscheint in der Anzeige
   jedes Administrators.

   Sie wird nicht je Administrator vervielfältigt: Anlässe wie das Ablegen
   einer Qualifikation betreffen den Adminbereich als Ganzes, und ein
   Administrator, der später hinzukommt, sieht dieselbe Geschichte wie
   seine Kollegen.

.. req:: Gelesen gilt für den gesamten Adminbereich
   :id: REQ_NAC_ADM_02
   :status: review
   :priority: high
   :links: REQ_NAC_ADM_01, REQ_NAC_ANZ_04

   Hat ein Administrator eine Mitteilung an den Adminbereich gelesen, gilt
   sie für alle Administratoren als gelesen. Sie bleibt in der Liste
   sichtbar, zählt aber bei niemandem mehr als ungelesen.

   Der Adminbereich arbeitet als ein Gegenüber: Wer sich kümmert, kümmert
   sich für alle. Ein Lesezustand je Person würde dieselbe Kenntnisnahme
   mehrfach einfordern.

Datenhaltung
------------

.. req:: Anlasstyp und Bezug werden mitgeführt
   :id: REQ_NAC_DAT_01
   :status: review
   :priority: high
   :links: REQ_DAT_NACHR_01, REQ_NAC_ANL_01, REQ_NAC_ANZ_03

   Zu jeder Mitteilung werden neben ihrem Text ein Anlasstyp aus
   :need:`REQ_NAC_ANL_01` und der Bezug auf den betroffenen Gegenstand
   gehalten -- Termin, Schulung oder Vorgang.

   Der Text allein trägt den Bezug nicht: Ohne Anlasstyp und Bezug lässt
   sich weder zum Termin springen, noch filtern, noch feststellen, ob ein
   Anlass überhaupt im Katalog steht.

.. req:: Empfänger ist eine Person oder der Adminbereich
   :id: REQ_NAC_DAT_02
   :status: review
   :links: REQ_NAC_DAT_01, REQ_NAC_ADM_01, REQ_DAT_NACHR_01

   Der Empfänger einer Mitteilung ist entweder ein Benutzerkonto oder der
   Adminbereich als Rolle. Der Lesezustand wird beim Datensatz selbst
   geführt und gilt damit für den jeweiligen Empfänger als Ganzes.

.. req:: Zustellung ausschließlich in der Anwendung
   :id: REQ_NAC_DAT_03
   :status: review
   :priority: high
   :links: REQ_DAT_NACHR_01, REQ_NAC_GRUND_01

   Mitteilungen werden ausschließlich in der Anwendung angezeigt. Es gibt
   keinen Versand per E-Mail und keinen weiteren Zustellweg nach außen.

   Das ist eine bewusste Festlegung und keine offene Stelle: Es gibt nur
   einen Prozess, und eine Zustellung über Prozessgrenzen hinweg ist nicht
   nötig -- so, wie :need:`REQ_DAT_NACHR_01` es bereits für das
   Nachrichtensystem festhält.
