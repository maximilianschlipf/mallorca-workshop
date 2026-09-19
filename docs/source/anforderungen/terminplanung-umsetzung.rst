Terminplanung: Stories und Tests
================================

Diese Seite übersetzt die Anforderungen der Terminplanung in Abläufe und in
Tests, die sie abprüfen.

Termine anlegen
---------------

.. story:: Ich plane einen Termin
   :id: STORY_TER_ANL_01
   :status: approved
   :priority: high
   :implements: REQ_TER_ANL_01, REQ_TER_ANL_03, REQ_TER_ANL_04, REQ_TER_ANL_05, REQ_TER_ANL_06, REQ_TER_ANL_07, REQ_TER_ID_01, REQ_TER_ID_02

   Als Administrator wähle ich eine aktive Schulung und ein Startdatum. Das
   Enddatum schlägt mir das System aus der Dauer vor; ich kann es anpassen.
   Mehr muss ich nicht angeben -- die Kennung vergibt das System.

.. story:: Ich trage die Rahmendaten später nach
   :id: STORY_TER_ANL_02
   :status: approved
   :priority: high
   :implements: REQ_TER_ANL_02, REQ_TER_FORM_07, REQ_TER_FORM_08, REQ_TER_FORM_09, REQ_TER_AEND_03, REQ_TLN_BUCH_06

   Steht noch nicht fest, ob der Termin remote oder vor Ort läuft und wer
   ihn hält, lasse ich das offen und ergänze es, sobald es entschieden ist.
   Bei einem exklusiven Termin erfasse ich außerdem die Kundenfirma.

.. test:: Termin mit Pflichtangaben wird angelegt
   :id: TEST_TER_ANL_01
   :status: approved
   :automated: yes
   :verifies: STORY_TER_ANL_01, REQ_TER_ANL_03

   Mit Schulung, Startdatum und Enddatum wird ein Termin angelegt. Er ist
   geplant, nicht zugewiesen und ohne Zugangsart, Durchführungsart und Ort.

.. test:: Fehlende Pflichtangabe wird abgewiesen
   :id: TEST_TER_ANL_02
   :status: approved
   :automated: yes
   :verifies: REQ_TER_ANL_03

   Fehlt Schulung, Startdatum oder Enddatum, wird das Anlegen abgewiesen.

.. test:: Enddatum wird aus der Dauer vorgeschlagen
   :id: TEST_TER_ANL_03
   :status: approved
   :automated: yes
   :verifies: REQ_TER_ANL_04

   Bei einer dreitägigen Schulung ab Freitag schlägt das System den folgenden
   Dienstag als Enddatum vor. Ein abweichendes Enddatum -- kürzer wie länger
   -- wird angenommen, sofern es nicht am Wochenende liegt.

.. test:: Enddatum vor Startdatum wird abgewiesen
   :id: TEST_TER_ANL_04
   :status: approved
   :automated: yes
   :verifies: REQ_TER_ANL_05

   Ein Enddatum vor dem Startdatum wird abgewiesen; Start und Ende am selben
   Tag werden angenommen.

.. test:: Termin in der Vergangenheit wird abgewiesen
   :id: TEST_TER_ANL_05
   :status: approved
   :automated: yes
   :verifies: REQ_TER_ANL_06

   Ein Startdatum in der Vergangenheit wird abgewiesen, das heutige Datum
   angenommen.

.. test:: Kein Termin beginnt oder endet am Wochenende
   :id: TEST_TER_ANL_08
   :status: approved
   :automated: yes
   :verifies: REQ_TER_ANL_07

   Ein Start- oder Enddatum am Samstag oder Sonntag wird abgewiesen. Ein
   Termin von Freitag bis Montag wird angenommen; das Wochenende zählt nicht
   als Schulungszeit.

.. test:: Zu archivierter Schulung entsteht kein Termin
   :id: TEST_TER_ANL_06
   :status: approved
   :automated: yes
   :verifies: REQ_TER_ANL_01

   Das Anlegen eines Termins zu einer archivierten Schulung wird abgewiesen.

.. test:: Termin ohne Trainer ist gültig
   :id: TEST_TER_ANL_07
   :status: approved
   :automated: yes
   :verifies: REQ_TER_ANL_02

   Ein ohne Trainer gespeicherter Termin gilt als nicht zugewiesen und
   erscheint in der Übersicht der Termine ohne Trainer.

Kennung
-------

.. test:: Termin-ID wird fortlaufend vergeben
   :id: TEST_TER_ID_01
   :status: approved
   :automated: yes
   :verifies: REQ_TER_ID_01

   Der erste Termin zur Schulung ``SCH-001`` erhält ``SCH-001-T0001``, der
   zweite ``SCH-001-T0002``. Nach dessen Löschung erhält der nächste Termin
   ``SCH-001-T0003``; die gelöschte Kennung wird nicht erneut vergeben. Der
   erste Termin einer anderen Schulung beginnt wieder bei ``0001``. Eine vom
   Aufrufer mitgegebene Kennung wird nicht übernommen.

.. test:: Termin-ID bleibt beim Ändern bestehen
   :id: TEST_TER_ID_02
   :status: approved
   :automated: yes
   :verifies: REQ_TER_ID_02

   Nach Verschieben und Absagen trägt der Termin dieselbe Kennung wie beim
   Anlegen.

.. test:: Termin-ID läuft nicht über
   :id: TEST_TER_ID_03
   :status: approved
   :automated: yes
   :verifies: REQ_TER_ID_01

   Nach der Vergabe von ``T9999`` wird ein weiterer Termin derselben Schulung
   mit einer eindeutigen Fehlermeldung abgewiesen. Es wird weder eine alte
   Kennung wiederverwendet noch eine fünfstellige Kennung erzeugt.

Zugangsart und Durchführungsart
-------------------------------

.. story:: Ich lege fest, wie der Termin stattfindet
   :id: STORY_TER_FORM_01
   :status: approved
   :implements: REQ_TER_FORM_01, REQ_TER_FORM_02, REQ_TER_FORM_03, REQ_TER_FORM_04, REQ_TER_FORM_05, REQ_TER_FORM_06, REQ_TER_FORM_10, REQ_TER_FORM_11, REQ_TER_FORM_12

   Ich gebe an, ob der Termin öffentlich oder exklusiv ist und ob er remote,
   vor Ort, beim Kunden oder hybrid stattfindet. Außer bei remote trage ich
   einen Ort ein; für remote und hybrid kann ich einen Online-Zugang
   hinterlegen. Derselbe Ort darf gleichzeitig für andere Termine verwendet
   werden.

.. story:: Termindetails sind rollenbezogen sichtbar
   :id: STORY_TER_FORM_02
   :status: approved
   :priority: high
   :implements: REQ_TER_FORM_09, REQ_TER_FORM_12, REQ_TLN_BUCH_07

   Als angemeldeter Trainer sehe ich die Kundenfirma jedes für mich
   sichtbaren exklusiven Termins. Online-Zugang und Teilnehmerliste sehe ich
   dagegen nur, soweit meine Rolle am Termin sie benötigt.

.. test:: Zugangsart kennt zwei Werte
   :id: TEST_TER_FORM_01
   :status: approved
   :automated: yes
   :verifies: REQ_TER_FORM_01

   "öffentlich" und "exklusiv" werden angenommen, andere Werte abgewiesen.

.. test:: Durchführungsart kennt vier Werte
   :id: TEST_TER_FORM_02
   :status: approved
   :automated: yes
   :verifies: REQ_TER_FORM_02

   "remote", "vor Ort", "beim Kunden" und "hybrid" werden angenommen,
   andere Werte abgewiesen.

.. test:: Alle Kombinationen sind erlaubt
   :id: TEST_TER_FORM_03
   :status: approved
   :automated: yes
   :verifies: REQ_TER_FORM_03

   Jede der acht Kombinationen aus Zugangsart und Durchführungsart lässt
   sich speichern, einschließlich "öffentlich" mit "beim Kunden".

.. test:: Ort gehört zu drei Durchführungsarten
   :id: TEST_TER_FORM_04
   :status: approved
   :automated: yes
   :verifies: REQ_TER_FORM_04

   Bei "vor Ort", "beim Kunden" und "hybrid" wird ein fehlender Ort
   abgewiesen. Bei "remote" wird ein angegebener Ort abgewiesen. Beim Wechsel
   eines bestehenden Termins mit Ort zu "remote" wird vor dem Entfernen des
   Orts gefragt; nach Bestätigung ist er leer, ohne Bestätigung bleibt der
   Termin unverändert.

.. test:: Beide Angaben lassen sich nachtragen
   :id: TEST_TER_FORM_05
   :status: approved
   :automated: yes
   :verifies: REQ_TER_FORM_07

   Ein Termin ohne Zugangsart und Durchführungsart wird gespeichert; beide
   Angaben lassen sich später ergänzen.

.. test:: Ohne Zugangsart wird keine Grenze geprüft
   :id: TEST_TER_FORM_06
   :status: approved
   :automated: yes
   :verifies: REQ_TER_FORM_06

   An einem Termin ohne Zugangsart entstehen Teilnehmerbuchungen über der
   Höchstzahl der Schulung, ohne dass gewarnt wird. Nach dem Nachtragen von
   "öffentlich" erscheint die Warnung.

.. test:: Zugangsart bestimmt die Grenze
   :id: TEST_TER_FORM_07
   :status: approved
   :automated: yes
   :verifies: REQ_TER_FORM_05

   Bei einem öffentlichen Termin wird gegen die Höchstzahl gewarnt, bei
   einem exklusiven gegen die Mindestzahl -- unabhängig von der
   Durchführungsart.

.. test:: Exklusiv erfordert Buchungen einer Firma
   :id: TEST_TER_FORM_08
   :status: approved
   :automated: yes
   :verifies: REQ_TER_FORM_08, REQ_TLN_BUCH_06

   Ein öffentlicher Termin mit Buchungen außerhalb der gewählten Kundenfirma
   lässt sich nicht auf exklusiv ändern. Nach dem Entfernen der abweichenden
   Buchungen gelingt die Änderung. Ein exklusiver Termin lässt sich auch mit
   vorhandenen Buchungen auf öffentlich ändern.

.. test:: Exklusiver Termin trägt eine Kundenfirma
   :id: TEST_TER_FORM_09
   :status: approved
   :automated: yes
   :verifies: REQ_TER_FORM_09, REQ_TLN_BUCH_06

   "Exklusiv" ohne Kundenfirma wird abgewiesen; mit Kundenfirma gelingt die
   Speicherung. Eine Kundenfirma an einem öffentlichen Termin oder einem
   Termin ohne Zugangsart wird abgewiesen. Leerraum und
   Groß-/Kleinschreibung führen bei der Prüfung bestehender Buchungen nicht
   zu einem vermeintlichen Firmenunterschied.
   Beim Wechsel zu "öffentlich" wird vor dem Entfernen der Kundenfirma
   gefragt. Nach Bestätigung ist sie entfernt, ohne Bestätigung bleibt der
   Termin exklusiv. Beim Ändern der Kundenfirma eines exklusiven Termins mit
   Buchungen werden nach Bestätigung Termin und alle Buchungen gemeinsam
   geändert; ohne Bestätigung bleibt überall die bisherige Firma erhalten.
   Ein unbeteiligter angemeldeter Trainer, der den Termin sehen darf, sieht
   dessen Kundenfirma ebenfalls.

.. test:: Gleicher Ort blockiert keinen zweiten Termin
   :id: TEST_TER_FORM_10
   :status: approved
   :automated: yes
   :verifies: REQ_TER_FORM_10

   Zwei Termine mit unterschiedlichen Trainern lassen sich an denselben
   Schulungstagen mit demselben Ort speichern. Eine Raumkonfliktwarnung
   entsteht nicht.

.. test:: Online-Zugang gehört zu Remote und Hybrid
   :id: TEST_TER_FORM_11
   :status: approved
   :automated: yes
   :verifies: REQ_TER_FORM_11

   "Remote" und "hybrid" lassen sich mit oder ohne gültige URL speichern.
   Vollständige HTTP- und HTTPS-URLs werden angenommen und von umgebendem
   Leerraum bereinigt; relative Angaben, reiner Text sowie etwa
   ``javascript:`` und ``file:`` werden abgewiesen.
   Bei "vor Ort", "beim Kunden" und ohne Durchführungsart wird eine URL
   ebenfalls abgewiesen. Beim Wechsel eines Termins mit URL zu einer reinen
   Präsenzform wird vor dem Entfernen gefragt; nach Bestätigung ist die URL
   entfernt, ohne Bestätigung bleibt der Termin unverändert.

.. test:: Online-Zugang ist auf Beteiligte beschränkt
   :id: TEST_TER_FORM_12
   :status: approved
   :automated: yes
   :verifies: REQ_TER_FORM_12

   Administrator, zugewiesener Trainer und zugewiesener Assistent erhalten
   die Online-Zugangs-URL. Ein anderer angemeldeter Trainer sieht denselben
   Termin ohne URL; auch seine direkte Schnittstellenabfrage gibt sie nicht
   preis.

.. test:: Assistent sieht keine Teilnehmerdetails
   :id: TEST_TER_FORM_13
   :status: approved
   :automated: yes
   :verifies: REQ_TLN_BUCH_07

   Ein Administrator und der zugewiesene Trainer erhalten die vollständige
   Teilnehmerliste. Ein zugewiesener Assistent erhält weder in der
   Oberfläche noch über einen direkten Schnittstellenaufruf Namen, Firmen
   oder Bemerkungen der Teilnehmer.

Zustände und Abschluss
----------------------

.. story:: Ich bestätige die Durchführung meines Termins
   :id: STORY_TER_STAT_01
   :status: approved
   :priority: high
   :implements: REQ_TER_STAT_01, REQ_TER_STAT_02, REQ_TER_STAT_04, REQ_TER_STAT_05, REQ_TLN_TEIL_01, REQ_TLN_TEIL_02, REQ_TLN_TEIL_03, REQ_ASS_PLATZ_04

   Als zugewiesener Trainer bestätige ich die Durchführung meines Termins.
   Vor dem Enddatum geht das nicht; mit der Bestätigung steht fest, wer
   teilgenommen hat.

.. story:: Ein vergessener Termin kommt trotzdem zum Abschluss
   :id: STORY_TER_STAT_02
   :status: approved
   :implements: REQ_TER_STAT_03, REQ_TER_STAT_06, REQ_TER_STAT_07, REQ_TER_STAT_08, REQ_TLN_AUFB_01, REQ_TLN_AUFB_02, REQ_TLN_AUFB_03, REQ_TLN_AUFB_04

   Bestätigt der Trainer die Durchführung nicht, kann ein Administrator einspringen.
   Geschieht auch das nicht, schließt das System nach zwei Kalendermonaten selbst --
   erkennbar als ungeprüft, und ohne in Auswertungen einzugehen.

.. test:: Ein Termin trägt genau drei Zustände
   :id: TEST_TER_STAT_01
   :status: approved
   :automated: yes
   :verifies: REQ_TER_STAT_01

   Ein Termin ist geplant, abgeschlossen oder abgesagt. Ein Zustand
   "ausgebucht" existiert nicht; ein Termin über seiner Höchstteilnehmerzahl
   bleibt geplant.

.. test:: Der zugewiesene Trainer bestätigt die Durchführung
   :id: TEST_TER_STAT_02
   :status: approved
   :automated: yes
   :verifies: REQ_TER_STAT_02, REQ_TLN_TEIL_01, REQ_TLN_TEIL_02, REQ_TLN_TEIL_03, REQ_ASS_PLATZ_04

   Der zugewiesene Trainer kann die Durchführung am Enddatum oder danach
   bestätigen; ein anderer Trainer kann es nicht. Solange eine Buchung noch
   den Teilnahmestatus "offen" trägt, wird die Bestätigung abgewiesen. Nach
   der ausdrücklichen Kennzeichnung aller Buchungen gelingt sie; ebenso bei
   einem Termin ohne Buchungen. Ein vorhandener Online-Zugang ist danach
   entfernt, die Durchführungsart bleibt erhalten. Danach werden sowohl das
   Wiederöffnen als auch fachliche Änderungen des Termins abgewiesen.
   Der Versuch eines zugewiesenen Assistenten, die Durchführung zu
   bestätigen, wird ebenfalls abgewiesen.

.. test:: Administrator kann die Durchführung ersatzweise bestätigen
   :id: TEST_TER_STAT_03
   :status: approved
   :automated: yes
   :verifies: REQ_TER_STAT_03

   Ein Administrator bestätigt einen Termin, dem ein anderer qualifizierter
   Trainer zugewiesen ist. Bei einem Termin ohne Trainer wird die manuelle
   Bestätigung abgewiesen. Auch die Bestätigung durch den Administrator ist
   endgültig.

.. test:: Bestätigung ist ab dem Enddatum möglich
   :id: TEST_TER_STAT_04
   :status: approved
   :automated: yes
   :verifies: REQ_TER_STAT_04

   Die Bestätigung eines Termins vor seinem Enddatum wird abgewiesen. Am
   Enddatum wird sie angenommen.

.. test:: Abschluss hält Art, Zeitpunkt und optional die Person fest
   :id: TEST_TER_STAT_05
   :status: approved
   :automated: yes
   :verifies: REQ_TER_STAT_05

   Ein manuell bestätigter Termin führt die Abschlussart "manuell", den
   Zeitpunkt und den bestätigenden Benutzer. Ein selbsttätig abgeschlossener
   Termin führt die Abschlussart "automatisch" und den fachlichen Stichtag,
   aber keinen Benutzer.

.. test:: Selbsttätiger Abschluss nach zwei Kalendermonaten
   :id: TEST_TER_STAT_06
   :status: approved
   :automated: yes
   :verifies: REQ_TER_STAT_06, REQ_TER_STAT_07

   Einen Tag vor Ablauf der zwei Kalendermonate bleibt ein Termin geplant.
   Am Stichtag wird er selbsttätig abgeschlossen und als nicht bestätigt
   gekennzeichnet. Für ein Enddatum am 31. Dezember ist der Stichtag der
   letzte Februartag.

   War die Anwendung am Stichtag ausgeschaltet, wird der Abschluss beim
   nächsten Start nachgeholt. Gespeichert wird dennoch der ursprüngliche
   Stichtag als Abschlusszeitpunkt. Offene Teilnahmestatus verhindern diesen
   selbsttätigen Abschluss nicht.

   Bleibt die Anwendung über den Stichtag geöffnet, ist der Termin spätestens
   beim ersten fachlichen Zugriff des Tages abgeschlossen, ohne dass die
   Anwendung neu gestartet wurde. Ein vorhandener Online-Zugang ist entfernt.

   Ein zugewiesener Trainer erhält dabei eine Benachrichtigung mit dem
   Hinweis, dass der Termin nicht in Teilnehmerauswertungen eingeht. Für
   Administratoren entsteht keine Benachrichtigung; in den Termindetails ist
   der selbsttätige Abschluss erkennbar. Der Versuch, ihn danach manuell zu
   bestätigen oder seine Teilnehmerdaten zu ändern, wird abgewiesen.

.. test:: Ungeprüfte Termine bleiben aus Auswertungen heraus
   :id: TEST_TER_STAT_07
   :status: approved
   :automated: yes
   :verifies: REQ_TER_STAT_08

   Eine Auswertung der Teilnehmerzahlen berücksichtigt bestätigte Termine
   und lässt selbsttätig abgeschlossene aus. Deren Daten bleiben einsehbar.

.. test:: Teilnehmerdetails werden nach drei Kalendermonaten entfernt
   :id: TEST_TER_STAT_08
   :status: approved
   :automated: yes
   :verifies: REQ_TLN_AUFB_01, REQ_TLN_AUFB_02, REQ_TLN_AUFB_03, REQ_TLN_AUFB_04

   Bei einem bestätigten Termin beginnt die Frist mit der Bestätigung, bei
   einem abgesagten mit der Absage. Drei Kalendermonate später sind Name und
   Bemerkung jeder Buchung entfernt. Für einen Fristbeginn am 31. Januar ist
   der Stichtag der 30. April. Firma und aggregierte Teilnehmerzahlen bleiben
   erhalten. War die Anwendung am Stichtag aus, wird das Entfernen beim
   nächsten Start nachgeholt. Bleibt sie geöffnet, geschieht es spätestens
   vor dem ersten fachlichen Zugriff des Tages. Liegen beim Start sowohl der
   selbsttätige Abschluss als auch die anschließende Aufbewahrungsfrist
   bereits zurück, werden beide Schritte in fachlicher Reihenfolge im selben
   Nachholvorgang ausgeführt.

Ändern, absagen, löschen
------------------------

.. story:: Ich verschiebe einen Termin
   :id: STORY_TER_AEND_01
   :status: approved
   :priority: high
   :implements: REQ_TER_AEND_01, REQ_TER_AEND_02, REQ_TER_AEND_11, REQ_TER_AEND_12, REQ_TER_AEND_13

   Als Administrator ändere ich vor dem Start den Zeitraum eines geplanten
   Termins oder während seiner Durchführung das Enddatum. Passt eine
   bestehende Trainer- oder Assistenzzuweisung nicht mehr, erfahre ich das
   und die Änderung unterbleibt.

.. story:: Ich sage einen Termin ab
   :id: STORY_TER_AEND_02
   :status: approved
   :priority: high
   :implements: REQ_TER_AEND_04, REQ_TER_AEND_05, REQ_TER_AEND_07, REQ_TER_AEND_08, REQ_TER_AEND_09, REQ_TLN_TEIL_03, REQ_TLN_AUFB_04

   Vor der Absage sehe ich, was daran hängt -- Buchungen, Trainer,
   Assistenten. Trainer und Assistenten werden benachrichtigt, die
   Teilnehmer muss ich selbst erreichen. Rückgängig machen kann ich die
   Absage nicht.

.. story:: Ich lösche einen Termin
   :id: STORY_TER_AEND_03
   :status: approved
   :implements: REQ_TER_AEND_06, REQ_TER_AEND_10

   Vor seinem Start kann ich einen geplanten oder abgesagten Termin löschen.
   Ab dem Startdatum bleibt nur die fachliche Absage; einen abgeschlossenen
   Termin kann ich nie löschen, denn er ist die Aufzeichnung einer gehaltenen
   Schulung.

.. test:: Verschieben hält sich an die Zeitraumregeln
   :id: TEST_TER_AEND_01
   :status: approved
   :automated: yes
   :verifies: REQ_TER_AEND_01

   Vor dem Start lassen sich Start- und Enddatum in einen gültigen künftigen
   Zeitraum verschieben. Ab dem Start bleibt das Startdatum unveränderlich;
   während der Durchführung lässt sich das Enddatum auf heute oder einen
   späteren Wochentag ändern. Nach dem Enddatum wird jede Zeitraumänderung
   abgewiesen.

.. test:: Verschieben prüft die Zuweisung erneut
   :id: TEST_TER_AEND_02
   :status: approved
   :automated: yes
   :verifies: REQ_TER_AEND_02

   Wird ein Zeitraum so geändert, dass der zugewiesene Trainer oder ein
   Assistent abwesend oder anderweitig zugewiesen wäre, wird die Änderung
   abgewiesen und der Termin bleibt unverändert.

.. test:: Rahmendaten lassen sich ändern
   :id: TEST_TER_AEND_03
   :status: approved
   :automated: yes
   :verifies: REQ_TER_AEND_03

   Ort, Zugangsart, Durchführungsart, Kundenfirma und Online-Zugang eines
   geplanten Termins lassen sich ändern oder erstmals angeben. Das gelingt
   auch nach seinem Enddatum, solange er noch geplant ist. Dabei gelten
   dieselben Abhängigkeiten zwischen den Feldern wie beim Anlegen. Nach
   Bestätigung oder Absage wird jede solche Änderung abgewiesen.

.. test:: Operative Änderungen werden mitgeteilt
   :id: TEST_TER_AEND_11
   :status: review
   :automated: yes
   :verifies: REQ_TER_AEND_12

   Ein Administrator ändert Zeitraum, Ort, Durchführungsart, Kundenfirma und
   Online-Zugang eines Termins in einem Vorgang. Der zugewiesene Trainer und
   alle Assistenten erhalten je geändertem Feld eine Benachrichtigung. Beim
   Online-Zugang enthält sie keine URL, sondern den geschützten Sprung zum
   Termin; bei den übrigen Angaben alten und neuen Wert. Nach dem Abziehen
   eines Trainers oder Assistenten gibt weder die alte Mitteilung noch ihr
   Sprung die URL preis. Eine alleinige Änderung der Zugangsart ohne
   Kundenwechsel erzeugt keine Benachrichtigung.

.. test:: Zugeordnete Schulung lässt sich nicht ändern
   :id: TEST_TER_AEND_10
   :status: approved
   :automated: yes
   :verifies: REQ_TER_AEND_11

   Der Versuch, einen bestehenden Termin einer anderen Schulung zuzuordnen,
   wird abgewiesen. Schulungs-ID und Termin-ID bleiben unverändert.

.. test:: Bestehender Termin einer archivierten Schulung bleibt verwaltbar
   :id: TEST_TER_AEND_12
   :status: approved
   :automated: yes
   :verifies: REQ_TER_AEND_13

   Nach dem Archivieren der Schulung lassen sich ihr bestehender geplanter
   Termin ändern und mit einem qualifizierten Trainer besetzen. Seine
   Durchführung kann bestätigt oder er kann abgesagt werden. Nur eine
   Neuanlage für dieselbe Schulung wird abgewiesen.

.. test:: Absage erhält den Termin
   :id: TEST_TER_AEND_04
   :status: approved
   :automated: yes
   :verifies: REQ_TER_AEND_04, REQ_TLN_TEIL_03, REQ_TLN_AUFB_04

   Ein abgesagter Termin bleibt mit allen Daten bestehen, trägt den Zustand
   abgesagt, hält Absagedatum, absagenden Administrator und einen optionalen
   Absagegrund fest und erscheint nicht mehr unter den anstehenden Terminen.
   Trainer- und Assistenzzuweisungen bleiben zur Nachvollziehbarkeit erhalten,
   blockieren die Beteiligten aber nicht mehr für andere Termine. Ein
   vorhandener Online-Zugang ist entfernt, die Durchführungsart bleibt
   erhalten.
   Dasselbe gelingt mit einem bereits beendeten, aber noch geplanten Termin;
   er wird anschließend nicht selbsttätig abgeschlossen und zählt nicht als
   durchgeführt. Seine Teilnehmerbuchungen bleiben einsehbar, lassen sich
   aber weder durch einen Administrator noch durch einen Trainer ändern,
   ergänzen oder löschen.

.. test:: Absage ist nicht umkehrbar
   :id: TEST_TER_AEND_05
   :status: approved
   :automated: yes
   :verifies: REQ_TER_AEND_09

   Der Versuch, einen abgesagten Termin wieder in den Zustand geplant zu
   versetzen, wird abgewiesen.

.. test:: Absage informiert Beteiligte, aber keine Teilnehmer
   :id: TEST_TER_AEND_06
   :status: approved
   :automated: yes
   :verifies: REQ_TER_AEND_08

   Nach der Absage mit Grund liegen Benachrichtigungen einschließlich des
   Grunds für den zugewiesenen Trainer und alle Assistenten vor. Für gebuchte
   Teilnehmer entsteht keine. Ohne Grund enthält die Benachrichtigung keinen
   leeren Platzhalter. Beim Löschen eines noch nicht gestarteten Termins
   werden Trainer und Assistenten ebenfalls benachrichtigt; die Nachricht
   enthält keinen Löschgrund.

.. test:: Absage und Löschen bereinigen offene Vorgänge
   :id: TEST_TER_AEND_13
   :status: review
   :automated: yes
   :level: integration
   :verifies: REQ_TER_AEND_05

   Nach der Absage mit und ohne Grund sowie nach dem Löschen liegen die in
   :need:`REQ_TER_AEND_05` beschriebenen Mitteilungen für Trainer und
   Assistenten vor.

   Zu Absage und Löschung bestehen je eine offene Vormerkung,
   Assistenzbewerbung und Übernahmeanfrage. Danach sind alle drei als
   entfallen erledigt und nicht mehr entscheidbar; jeder Antragsteller hat
   je Vorgang genau eine Mitteilung mit Absage beziehungsweise Löschung als
   Grund.

   Zusätzlich betreffen ein Abwesenheitsantrag und eine Ersatztrainer-Anfrage
   jeweils zwei Termine. Nach Absage beziehungsweise Löschung des ersten
   Termins bleiben sie für den zweiten offen, enthalten nur noch diesen und
   ihr Antragsteller hat genau eine Mitteilung über die Anpassung. Nach dem
   gleichen Ereignis am zweiten Termin sind beide Vorgänge als entfallen
   erledigt, die Abwesenheit ist aktiv und der Antragsteller hat je Vorgang
   genau eine weitere Mitteilung mit dem Ereignis als Grund.

.. test:: Warnung nennt, was am Termin hängt
   :id: TEST_TER_AEND_07
   :status: approved
   :automated: yes
   :verifies: REQ_TER_AEND_07

   Vor Absage und Löschen werden Anzahl der Teilnehmerbuchungen,
   zugewiesener Trainer und Assistenten aufgeführt. Der Vorgang lässt sich
   danach ausführen.

.. test:: Geplante und abgesagte Termine lassen sich löschen
   :id: TEST_TER_AEND_08
   :status: approved
   :automated: yes
   :verifies: REQ_TER_AEND_06

   Vor dem Startdatum lassen sich ein geplanter und ein abgesagter Termin
   löschen und sind danach nicht mehr vorhanden. Am Startdatum wird das
   Löschen beider abgewiesen; stattdessen bleibt die Absage möglich.

.. test:: Abgeschlossener Termin lässt sich nicht löschen
   :id: TEST_TER_AEND_09
   :status: approved
   :automated: yes
   :verifies: REQ_TER_AEND_10

   Das Löschen eines abgeschlossenen Termins wird abgewiesen; er bleibt mit
   Trainer und Teilnehmerzahlen erhalten.

Trainerzuweisung
----------------

.. story:: Ich weise einem Termin einen Trainer zu
   :id: STORY_TER_ZUW_01
   :status: approved
   :priority: high
   :implements: REQ_TER_ZUW_01, REQ_TER_ZUW_02, REQ_TER_ZUW_03, REQ_TER_VORS_01, REQ_TER_VORS_03, REQ_TER_VORS_02

   Beim Zuweisen sehe ich ausschließlich die qualifizierten Trainer. Wer im
   Zeitraum abwesend oder schon eingeplant ist, ist erkennbar und nicht
   auswählbar; ein Kalender zeigt mir geeignete Zeiträume.

.. story:: Ich tausche einen Trainer aus
   :id: STORY_TER_ZUW_02
   :status: approved
   :implements: REQ_TER_ZUW_04, REQ_TER_ZUW_05, REQ_TER_ZUW_06, REQ_TER_ZUW_07, REQ_TER_ZUW_08, REQ_ASS_PLATZ_01

   Ich ziehe einen zugewiesenen Trainer ab oder ersetze ihn unmittelbar
   durch einen anderen. An abgeschlossenen und abgesagten Terminen geht das
   nicht mehr.

.. test:: Keine Zuweisung bei Abwesenheit
   :id: TEST_TER_ZUW_01
   :status: approved
   :automated: yes
   :verifies: REQ_TER_ZUW_01

   Die Zuweisung eines Trainers mit aktiver Abwesenheit im Zeitraum des
   Termins wird abgewiesen -- ohne Möglichkeit, sie zu übergehen.

.. test:: Keine überschneidende Zuweisung
   :id: TEST_TER_ZUW_02
   :status: approved
   :automated: yes
   :verifies: REQ_TER_ZUW_03

   Ein Trainer, der im Zeitraum bereits einem anderen Termin als Trainer
   oder als Assistent zugewiesen ist, kann nicht zugewiesen werden. Nach der
   Absage des anderen Termins gelingt die Zuweisung trotz der dort historisch
   erhaltenen Zuordnung.

.. test:: Keine Zuweisung ohne Qualifikation
   :id: TEST_TER_ZUW_03
   :status: approved
   :automated: yes
   :verifies: REQ_TER_ZUW_02

   Ein Trainer ohne Qualifikation für die Schulung lässt sich nicht zuweisen.
   Nach Erteilung der Qualifikation gelingt dieselbe Zuweisung.

.. test:: Abziehen gibt den Termin frei
   :id: TEST_TER_ZUW_04
   :status: approved
   :automated: yes
   :verifies: REQ_TER_ZUW_04

   Nach dem Abziehen gilt der Termin als nicht zugewiesen, und der
   bisherige Trainer ist benachrichtigt.

.. test:: Austauschen prüft wie eine Erstzuweisung
   :id: TEST_TER_ZUW_05
   :status: approved
   :automated: yes
   :verifies: REQ_TER_ZUW_05

   Der Austausch gegen einen im Zeitraum abwesenden oder anderweitig
   zugewiesenen Trainer wird abgewiesen; der bisherige bleibt zugewiesen. Bei
   einem zulässigen Austausch werden bisheriger und neuer Trainer
   benachrichtigt.

.. test:: Erstzuweisung wird mitgeteilt
   :id: TEST_TER_ZUW_07
   :status: approved
   :automated: yes
   :verifies: REQ_TER_ZUW_07

   Nach einer Zuweisung erhält der Trainer eine Benachrichtigung. Das gilt
   sowohl beim nachträglichen Zuweisen als auch beim Anlegen eines Termins
   mit Trainer. Auch die Zuweisung nach dem Enddatum eines noch geplanten
   Termins löst die Benachrichtigung aus.

.. test:: Assistent wird atomar zum ausführenden Trainer
   :id: TEST_TER_ZUW_08
   :status: approved
   :automated: yes
   :verifies: REQ_TER_ZUW_08, REQ_ASS_PLATZ_01

   Ohne Bestätigung bleibt ein Assistent in seiner bisherigen Rolle. Nach
   Bestätigung ist er ausschließlich ausführender Trainer, sein
   Assistenzplatz ist frei und er wurde über den Rollenwechsel benachrichtigt.
   Der Versuch, den ausführenden Trainer zusätzlich als Assistenten
   einzutragen, wird abgewiesen.

.. test:: Keine Zuweisung an abgeschlossenen Terminen
   :id: TEST_TER_ZUW_06
   :status: approved
   :automated: yes
   :verifies: REQ_TER_ZUW_06

   Zuweisen, Abziehen und Austauschen werden an abgeschlossenen und an
   abgesagten Terminen abgewiesen. Bei einem bereits beendeten, aber noch
   geplanten Termin gelingt dagegen die nachträgliche Zuweisung eines
   qualifizierten und für den Zeitraum verfügbaren Trainers; anschließend
   kann dessen Durchführung bestätigt werden.

.. test:: Trainerliste enthält nur Qualifizierte
   :id: TEST_TER_VORS_01
   :status: approved
   :automated: yes
   :verifies: REQ_TER_VORS_01

   Die Liste beim Planen enthält ausschließlich Trainer mit bestätigter
   Qualifikation für die Schulung.

.. test:: Nicht verfügbare Trainer sind gekennzeichnet
   :id: TEST_TER_VORS_02
   :status: approved
   :automated: yes
   :verifies: REQ_TER_VORS_03

   Ein im Zeitraum abwesender und ein anderweitig zugewiesener Trainer
   erscheinen mit dem jeweiligen Grund und sind nicht auswählbar.

.. test:: Kalender zeigt Abwesenheiten und Zuweisungen
   :id: TEST_TER_VORS_03
   :status: approved
   :automated: yes
   :verifies: REQ_TER_VORS_02

   Die Kalenderansicht zeigt für die angezeigten Trainer aktive
   Abwesenheiten und bestehende Zuweisungen im Umfeld des geplanten
   Zeitraums.

Terminübersicht und Dashboard
-----------------------------

.. story:: Ich verwalte Termine aus dem Kalender
   :id: STORY_TER_SICHT_01
   :status: approved
   :priority: high
   :implements: REQ_TER_SICHT_01, REQ_TER_SICHT_02, REQ_TER_SICHT_03

   Als Administrator sehe ich die Termine eines Monats und öffne einen
   Termin, um seine Details und zulässigen Aktionen zu sehen. Von derselben
   Übersicht aus lege ich einen neuen Termin an. Auf einem kleinen Bildschirm
   nutze ich dieselben Inhalte als chronologische Liste.

.. test:: Terminübersicht führt zu Details und Neuanlage
   :id: TEST_TER_SICHT_01
   :status: approved
   :automated: yes
   :verifies: REQ_TER_SICHT_01, REQ_TER_SICHT_02, REQ_TER_SICHT_03

   Die Monatsansicht zeigt geplante, abgeschlossene und abgesagte Termine
   unterscheidbar. Ein Termin von Freitag bis Dienstag erscheint am Freitag,
   Montag und Dienstag, nicht am Wochenende; seine Details nennen drei
   Schulungstage. Nach Auswahl eines Termins erscheinen seine Details in
   einem modalen Dialog über dem Kalender und nur die für ihn zulässigen
   Aktionen. Der Einstieg "Neuer Termin" öffnet ein modales Formular über
   dem Kalender. Abbrechen lässt den Kalender
   unverändert; nach erfolgreicher Neuanlage zeigt er den neuen Termin und
   dessen Details. In schmaler Darstellung erscheinen dieselben Termine nach
   Datum sortiert als Liste und das Formular füllt bei Bedarf den Bildschirm.

Dashboard
---------

.. story:: Ich sehe, worum ich mich kümmern muss
   :id: STORY_TER_DASH_01
   :status: approved
   :priority: high
   :implements: REQ_TER_ZEIT_01, REQ_DSH_DRIN_01, REQ_DSH_DRIN_02, REQ_DSH_DRIN_03, REQ_DSH_DRIN_04

   Auf meinem Dashboard stehen zuerst die überfälligen Termine ohne
   Abschluss, danach die Termine ohne Trainer nach Dringlichkeit, und ich
   sehe, wo die Mindestteilnehmerzahl in Gefahr ist.

.. story:: Als Trainer sehe ich ausstehende Durchführungsbestätigungen
   :id: STORY_TER_DASH_02
   :status: approved
   :priority: high
   :implements: REQ_DSH_PFLI_01

   Nach dem Enddatum finde ich meine noch geplanten Termine zuerst, damit
   ich deren Durchführung bestätigen kann. Abgesagte und abgeschlossene
   Termine erscheinen dort nicht.

.. test:: Termine ohne Trainer stehen nach Datum sortiert
   :id: TEST_TER_DASH_01
   :status: approved
   :automated: yes
   :verifies: REQ_TER_ZEIT_01, REQ_DSH_DRIN_01

   Das Dashboard führt alle geplanten Termine ohne Trainerzuweisung, deren
   Enddatum heute oder später liegt, aufsteigend nach Startdatum. Ein bereits
   begonnener Termin bleibt bis einschließlich seines Enddatums enthalten;
   am Folgetag gilt er nicht mehr als zukünftig. Abgesagte und abgeschlossene
   Termine sind nicht enthalten.

.. test:: Dringlichkeit richtet sich nach dem Vorlauf
   :id: TEST_TER_DASH_02
   :status: approved
   :automated: yes
   :verifies: REQ_DSH_DRIN_03

   Ein Termin ohne Trainer in weniger als vier Wochen ist als dringend
   markiert. Bei genau vier Wochen und mit mehr Vorlauf bleibt er sichtbar,
   trägt aber keine Dringlichkeitsmarkierung.

.. test:: Überfällige Termine stehen vor den anstehenden
   :id: TEST_TER_DASH_03
   :status: approved
   :automated: yes
   :verifies: REQ_DSH_DRIN_02

   Ein Termin, dessen Enddatum vorüber ist und der noch geplant ist, steht
   im Dashboard vor allen anstehenden Terminen. Ein abgesagter und ein
   abgeschlossener Termin mit demselben Enddatum erscheinen dort nicht.

.. test:: Zu wenige Teilnehmer werden gewarnt
   :id: TEST_TER_DASH_04
   :status: approved
   :automated: yes
   :verifies: REQ_DSH_DRIN_04

   Ein exklusiver Termin in drei Wochen unter seiner Mindestteilnehmerzahl
   wird im Dashboard als dringend markiert; fünf Wochen vor dem Start noch
   nicht. Ein öffentlicher Termin und ein Termin ohne Zugangsart erhalten
   unabhängig von ihrer Teilnehmerzahl keine solche Dringlichkeitsmarkierung.

.. test:: Trainer sieht nur geplante überfällige Termine als Aufgabe
   :id: TEST_TER_DASH_05
   :status: approved
   :automated: yes
   :verifies: REQ_DSH_PFLI_01

   Ein dem Trainer zugewiesener, noch geplanter Termin mit vergangenem
   Enddatum steht vor seinen anstehenden Terminen. Ein abgesagter und ein
   abgeschlossener Termin mit demselben Enddatum erscheinen dort nicht.

Demo-Daten
----------

.. story:: Eine frische Datenbank ist sofort demonstrierbar
   :id: STORY_TER_DEMO_01
   :status: approved
   :implements: REQ_TER_DEMO_01

   Nach dem initialen Seeding kann ich die wesentlichen Varianten der
   Terminplanung anhand sinnvoll verknüpfter Beispieldaten demonstrieren.

.. test:: Demo-Seed bildet die Terminplanung repräsentativ ab
   :id: TEST_TER_DEMO_01
   :status: approved
   :automated: yes
   :verifies: REQ_TER_DEMO_01

   Ausgehend von einem festen Referenzdatum enthält eine frisch angelegte
   Datenbank mindestens je einen vergangenen, laufenden und kommenden sowie
   geplanten, abgeschlossenen und abgesagten Termin. Vertreten sind Termine
   mit und ohne Trainer, öffentliche und exklusive Termine sowie Vor-Ort-,
   Remote- und Hybrid-Durchführungen. Qualifikationen, Assistenzen und
   Teilnehmerbuchungen passen zu ihren Terminen. Alle Start- und Enddaten
   liegen auf Werktagen und relativ zum Referenzdatum; feste Namen, Anzahlen
   und Datumsabstände werden nicht vorausgesetzt.

Abdeckung
---------

.. needtable::
   :types: req
   :filter: id.split("_")[1] == "TER"
   :columns: id, title, status, implements_back, verifies_back
   :style: table

Anforderungen dieses Bereichs ohne Test:

.. needtable::
   :types: req
   :filter: id.split("_")[1] == "TER" and not verifies_back
   :columns: id, title, priority
   :style: table
