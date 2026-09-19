Benutzerkonten und Anmeldung
============================

Es gibt genau eine Art von Konto. Was jemand darf, ergibt sich aus seinen
Rollen -- ein gesondertes Trainerprofil gibt es nicht.

Konto und Profil
----------------

.. req:: Benutzerkonto mit Profil
   :id: REQ_USR_PROF_01
   :status: approved
   :priority: high
   :component: backend

   Ein Benutzerkonto besteht aus:

   * **E-Mail-Adresse** -- zugleich Anmeldekennung,
   * **Name**,
   * **Passwort** -- gespeichert als Hash mit Salt,
   * **Rollen** -- eine Menge aus Trainer, Administrator und Eigentümer,
   * **Zustand** -- aktiv oder stillgelegt.

   Weitere Daten hängen nicht am Konto: Qualifikationen stehen an der
   Schulung, Abwesenheiten und Zuweisungen sind eigene Datensätze mit einem
   Verweis auf das Konto.

.. req:: Zustände eines Benutzerkontos
   :id: REQ_USR_PROF_02
   :status: approved
   :priority: high
   :links: REQ_USR_PROF_01

   Ein Benutzerkonto ist **aktiv** oder **stillgelegt**. Nur ein aktives
   Konto kann sich anmelden. Löschen ist kein Zustand, sondern entfernt das
   Konto.

.. req:: E-Mail-Adresse ist unveränderlich
   :id: REQ_USR_PROF_03
   :status: approved
   :priority: high
   :links: REQ_USR_PROF_01

   Die E-Mail-Adresse eines Kontos kann nach der Registrierung nicht mehr
   geändert werden. Sie ist die eindeutige Kennung des Kontos.

   Sie ist ausdrücklich keine zugesicherte Zustellanschrift: Es wird nichts
   an sie versandt und sie wird nicht geprüft. Soll ein Konto später auf eine
   andere Adresse wandern, wäre der Weg ein Export und Import des Profils --
   das ist noch nicht ausgearbeitet.

.. req:: Name ist änderbar
   :id: REQ_USR_PROF_04
   :status: approved
   :links: REQ_USR_PROF_01

   Ein Benutzer kann seinen eigenen Namen ändern. Der Name ist keine Kennung
   und muss nicht eindeutig sein.

Rollenmodell
------------

.. decision:: Ein Benutzerkonto kann mehrere Rollen tragen
   :id: DEC_USR_ROLLE_01
   :status: approved

   Ein Benutzerkonto hält eine Menge von Rollen, nicht genau eine. Ein
   Benutzerkonto kann die Rolle Trainer tragen, die Rolle Administrator oder
   beide.

   Die Alternative -- genau eine Rolle, bei der "Administrator" die
   Trainerrechte implizit einschließt -- wurde verworfen, weil ein
   Administrator ohne Trainerrolle keine Qualifikationen und keine
   Abwesenheiten führen könnte.

.. decision:: Eigentümer als Sicherung gegen das Aussperren
   :id: DEC_USR_EIGT_01
   :status: approved
   :links: DEC_USR_ROLLE_01

   Neben Trainer und Administrator gibt es die Rolle Eigentümer. Sie trägt
   genau ein Konto, sie lässt sich nur weitergeben und nicht ablegen, und ihr
   Träger kann weder stillgelegt noch gelöscht werden.

   Ohne sie ließe sich eine Instanz unbrauchbar machen: Die
   Administratorrolle kann entzogen werden, und ein Administrator, der sich
   selbst die Rolle nimmt oder sein Konto stilllegt, hinterlässt
   möglicherweise ein System ohne Administrator. Neue Administratoren kann
   dann niemand mehr ernennen, und die Selbstregistrierung erzeugt nur
   Trainer. Der Eigentümer ist die Zusicherung, dass immer genau ein Konto
   handlungsfähig bleibt.

.. decision:: Administratoren entscheiden auch in eigener Sache
   :id: DEC_USR_SELBST_01
   :status: approved
   :supersedes: DEC_QUA_SELBST_01
   :links: DEC_USR_ROLLE_01

   Ein Administrator darf jeden Vorgang entscheiden, den er selbst
   ausgelöst hat -- seine eigene Freigabeanfrage ebenso wie seinen eigenen
   Abwesenheitsantrag. Ein Vieraugenprinzip gibt es nirgends.

   Begründung: Ein Administrator kann sich ohnehin jede Qualifikation
   erteilen und jede Abwesenheit eintragen -- die Beschränkung ließe sich
   also nur umgehen, nicht durchsetzen. Vor allem aber wäre sie in einer
   Instanz mit einem einzigen Administrator eine Sackgasse: Niemand könnte
   seine Vorgänge entscheiden, und genau so startet jede neue Instanz.

   Diese Entscheidung löst die zuvor auf Freigabeanfragen beschränkte
   Fassung ab; die Überlegung trug von Anfang an weiter als der eine
   Vorgang.

.. req:: Rollen Trainer, Administrator und Eigentümer
   :id: REQ_USR_ROLLE_01
   :status: approved
   :priority: high
   :links: DEC_USR_ROLLE_01, REQ_USR_PROF_01

   Ein Benutzerkonto trägt mindestens eine der Rollen Trainer,
   Administrator oder Eigentümer. Die Rolle Administrator umfasst die
   Verwaltung von Schulungskatalog, Terminen, Qualifikationen,
   Abwesenheitsanträgen und Teilnehmerbuchungen.

.. req:: Was die Trainerrolle erlaubt
   :id: REQ_USR_ROLLE_08
   :status: approved
   :priority: high
   :links: REQ_USR_ROLLE_01, REQ_QUA_UMF_01

   Die Trainerrolle erlaubt zweierlei mit unterschiedlicher Voraussetzung:

   * **Assistieren** -- jederzeit, ohne Qualifikation,
   * **eine Schulung halten** -- nur mit einer bestätigten Qualifikation für
     diese Schulung.

   Ein frisch registriertes Konto kann damit sofort mitarbeiten, ohne schon
   qualifiziert zu sein.

.. req:: Administrator mit Trainerrolle
   :id: REQ_USR_ROLLE_02
   :status: approved
   :links: DEC_USR_ROLLE_01

   Trägt ein Benutzerkonto Administrator- und Trainerrolle, stehen ihm die
   Trainerfunktionen in vollem Umfang zur Verfügung: Es kann für Schulungen
   qualifiziert werden, Abwesenheiten pflegen und Terminen zugewiesen
   werden. Das ist der Normalfall für Administratoren.

.. req:: Administratorrolle vergeben
   :id: REQ_USR_ROLLE_03
   :status: approved
   :links: DEC_USR_ROLLE_01

   Ein Administrator kann einem anderen Benutzerkonto die Rolle
   Administrator erteilen. Bestehende Rollen des Kontos bleiben dabei
   erhalten.

.. req:: Administratorrolle entzieht nur der Eigentümer
   :id: REQ_USR_ROLLE_04
   :status: approved
   :priority: high
   :links: REQ_USR_ROLLE_03, DEC_USR_EIGT_01

   Die Rolle Administrator kann ausschließlich der Eigentümer entziehen.

   Dürften Administratoren einander entmachten, könnten sich zwei
   gegenseitig die Rolle nehmen; der Eigentümer als einzige entziehende
   Stelle macht daraus eine eindeutige Zuständigkeit.

.. req:: Trainerrolle entziehen
   :id: REQ_USR_ROLLE_09
   :status: approved
   :links: REQ_USR_ROLLE_01

   Ein Administrator kann einem Benutzerkonto die Rolle Trainer entziehen.

.. req:: Ein Konto behält mindestens eine Rolle
   :id: REQ_USR_ROLLE_05
   :status: approved
   :priority: high
   :links: REQ_USR_ROLLE_04, REQ_USR_ROLLE_09

   Das Entziehen der letzten Rolle eines Kontos wird abgewiesen. Ein Konto
   ohne Rolle könnte sich anmelden, aber nichts tun.

.. req:: Reines Administratorkonto
   :id: REQ_USR_ROLLE_06
   :status: approved
   :links: REQ_USR_ROLLE_09

   Ein Konto, dem die Trainerrolle entzogen wurde, ist ein reines
   Administratorkonto: Es kann vorhandene Qualifikationen nicht nutzen und
   ist weder als Trainer noch als Assistent einem Termin zuweisbar.
   Qualifikationen, Anfragen und Abwesenheiten bleiben gespeichert und werden
   bei einer erneuten Vergabe der Trainerrolle wieder nutzbar. Der Weg dorthin
   führt über Registrieren, Ernennen zum Administrator und Ablegen der
   Trainerrolle.

.. req:: Entzug der Trainerrolle löst Zuweisungen
   :id: REQ_USR_ROLLE_07
   :status: approved
   :priority: high
   :links: REQ_USR_ROLLE_06, REQ_ASS_PLATZ_01, REQ_TER_STAT_01, REQ_TER_ZEIT_01

   Wird einem Konto die Trainerrolle entzogen, wird es aus allen zukünftigen
   Terminen herausgenommen, denen es zugewiesen ist -- als ausführender
   Trainer wie als Assistent. Termine ohne Trainer wechseln in den Zustand
   "nicht zugewiesen", frei gewordene Assistenzplätze stehen wieder offen.
   Bei abgeschlossenen Terminen bleibt es eingetragen.

.. req:: Kontoverwaltung erfordert Administratorrechte
   :id: REQ_USR_ROLLE_10
   :status: approved
   :priority: high
   :links: REQ_USR_ROLLE_01

   Nur ein Administrator darf fremde Konten verwalten, Passwörter anderer
   Konten setzen, Rollen vergeben oder entziehen sowie Konten stilllegen,
   reaktivieren oder löschen. Die ausdrücklich dem Eigentümer vorbehaltenen
   Vorgänge bleiben auch für andere Administratoren gesperrt. Ein Benutzer
   darf ausschließlich den Namen und das Passwort seines eigenen Kontos
   ändern.

Eigentümer
----------

.. req:: Erstes Konto erhält alle Rollen
   :id: REQ_USR_EIGT_01
   :status: approved
   :priority: high
   :links: DEC_USR_EIGT_01

   Das erste Benutzerkonto einer Instanz erhält bei seiner Registrierung die
   Rollen Trainer, Administrator und Eigentümer. Damit ist eine frische
   Instanz ohne weiteres Zutun handlungsfähig. Auch bei gleichzeitig
   eintreffenden Registrierungen entsteht genau ein erstes Konto mit diesen
   Rollen; alle weiteren erhalten ausschließlich die Trainerrolle.

.. req:: Genau ein Eigentümer
   :id: REQ_USR_EIGT_02
   :status: approved
   :priority: high
   :links: DEC_USR_EIGT_01

   Sobald mindestens ein Benutzerkonto existiert, trägt genau ein aktives
   Benutzerkonto die Rolle Eigentümer.

.. req:: Eigentümerrolle weitergeben
   :id: REQ_USR_EIGT_03
   :status: approved
   :priority: high
   :links: REQ_USR_EIGT_02

   Der Eigentümer kann die Rolle an ein anderes aktives Benutzerkonto
   übergeben. Mit der Übergabe verliert er sie selbst; es gibt keinen Weg,
   sie abzulegen, ohne sie weiterzugeben. Vergabe und Entzug erfolgen in
   einem einzigen Vorgang, sodass zu keinem Zeitpunkt zwei Eigentümer oder
   gar kein Eigentümer existieren.

.. req:: Eigentümer behält die Administratorrolle
   :id: REQ_USR_EIGT_04
   :status: approved
   :links: REQ_USR_EIGT_02

   Solange ein Konto die Rolle Eigentümer trägt, trägt es auch die Rolle
   Administrator. Sie kann ihm nicht entzogen werden. Erhält ein reines
   Trainerkonto die Eigentümerrolle, erhält es deshalb im selben Vorgang
   zusätzlich die Administratorrolle.

.. req:: Eigentümerkonto ist geschützt
   :id: REQ_USR_EIGT_05
   :status: approved
   :priority: high
   :links: REQ_USR_EIGT_03

   Ein Konto mit der Rolle Eigentümer kann weder stillgelegt noch gelöscht
   werden. Wer sein Konto beenden will, muss die Rolle zuvor weitergeben.

Anmeldung
---------

.. decision:: Bewusst einfache Kontosicherheit
   :id: DEC_USR_SICHER_01
   :status: approved

   Es gibt keine Prüfung der E-Mail-Adresse, keine Regeln für die
   Passwortstärke, keine Sperre nach Fehlversuchen und kein "Passwort
   vergessen". Ein vergessenes Passwort setzt ein Administrator neu.

   Begründung: Jede Instanz läuft lokal bei einer einzelnen Person. Es gibt
   keinen Netzzugang von außen, keine fremden Nutzer und nichts zu
   erbeuten. Ein Bestätigungsversand setzte zudem einen Mailausgang voraus,
   den es nicht gibt.

   Die Grenze ist benannt: Sobald eine Instanz für mehrere Personen
   erreichbar betrieben wird, ist diese Entscheidung hinfällig.

.. req:: Anmeldung mit E-Mail und Passwort
   :id: REQ_USR_LOGIN_01
   :status: approved
   :priority: high
   :links: REQ_USR_PROF_01

   Ein Benutzerkonto meldet sich mit seiner E-Mail-Adresse und einem
   Passwort an. Die E-Mail-Adresse ist über alle Benutzerkonten eindeutig.

.. req:: Zugriff nur nach Anmeldung
   :id: REQ_USR_LOGIN_02
   :status: approved
   :priority: high
   :links: REQ_USR_LOGIN_01

   Ohne erfolgreiche Anmeldung sind ausschließlich Anmeldung und
   Registrierung erreichbar. Alle fachlichen Ansichten und Schnittstellen
   erfordern eine Anmeldung.

.. req:: Passwörter nur als gesalzener Hash
   :id: REQ_USR_LOGIN_03
   :status: approved
   :priority: high
   :component: backend
   :links: REQ_USR_PROF_01

   Ein Passwort wird niemals im Klartext gespeichert, sondern ausschließlich
   als Hash mit einem je Konto eigenen Salt. In der Datenbank steht nur der
   Hash.

.. req:: Fehlgeschlagene Anmeldung
   :id: REQ_USR_LOGIN_06
   :status: approved
   :priority: high
   :links: REQ_USR_LOGIN_01, DEC_USR_SICHER_01

   Stimmen E-Mail-Adresse und Passwort nicht überein, wird die Anmeldung mit
   einer Meldung abgewiesen, die offen lässt, welches von beidem falsch war.
   Es gibt keine Sperre nach mehreren Fehlversuchen.

.. req:: Anmeldung eines stillgelegten Kontos
   :id: REQ_USR_LOGIN_07
   :status: approved
   :priority: high
   :links: REQ_USR_LOGIN_01, REQ_USR_PROF_02

   Ein stillgelegtes Konto kann sich auch mit richtigem Passwort nicht
   anmelden. Ihm wird gesagt, dass das Konto stillgelegt ist, damit es sich
   an einen Administrator wenden kann statt das Passwort für falsch zu
   halten.

.. req:: Sitzung endet mit dem Browser
   :id: REQ_USR_LOGIN_04
   :status: approved
   :links: REQ_USR_LOGIN_01

   Eine Anmeldung gilt, bis der Browser geschlossen wird. Es gibt keine
   darüber hinausgehende Ablauffrist und kein "angemeldet bleiben".

.. req:: Stilllegen und Löschen beenden laufende Sitzungen
   :id: REQ_USR_LOGIN_08
   :status: approved
   :priority: high
   :links: REQ_USR_PROF_02

   Wird ein Konto stillgelegt oder gelöscht, enden alle seine laufenden
   Sitzungen sofort. Ein bereits angemeldetes Konto behält dadurch keinen
   Zugriff.

.. req:: Rollenänderungen gelten sofort
   :id: REQ_USR_LOGIN_09
   :status: approved
   :priority: high
   :links: REQ_USR_ROLLE_01

   Werden die Rollen eines Kontos geändert, gelten seine neuen Berechtigungen
   spätestens beim nächsten Aufruf. Eine bestehende Sitzung behält keine
   entzogenen Rechte.

.. req:: Abmelden
   :id: REQ_USR_LOGIN_05
   :status: approved
   :links: REQ_USR_LOGIN_04

   Ein angemeldeter Benutzer kann sich abmelden, ohne den Browser zu
   schließen.

.. req:: Anwendung ist standardmäßig nur lokal erreichbar
   :id: REQ_USR_SICHER_01
   :status: approved
   :priority: high
   :component: backend
   :links: DEC_USR_SICHER_01

   Die Anwendung lauscht in der Standardkonfiguration ausschließlich auf
   der Loopback-Schnittstelle. Die H2-Konsole ist im Standardbetrieb
   abgeschaltet. Eine Erreichbarkeit von anderen Rechnern erfordert eine
   bewusst geänderte Sicherheitskonfiguration.

Passwort
--------

.. req:: Passwort ändern
   :id: REQ_USR_PWD_01
   :status: approved
   :priority: high
   :links: REQ_USR_LOGIN_03

   Ein angemeldeter Benutzer kann sein eigenes Passwort ändern. Dabei gibt
   er sein bisheriges Passwort an; stimmt es nicht, wird die Änderung
   abgewiesen. Nach erfolgreicher Änderung enden alle anderen laufenden
   Sitzungen dieses Kontos.

.. req:: Passwort durch Administrator setzen
   :id: REQ_USR_PWD_02
   :status: approved
   :priority: high
   :links: DEC_USR_SICHER_01

   Ein Administrator kann für ein anderes Benutzerkonto ein neues Passwort
   setzen, ohne das bisherige zu kennen. Das ist der einzige Weg zurück in ein
   Konto mit vergessenem Passwort. Nach dem Setzen enden alle laufenden
   Sitzungen des betroffenen Kontos.

.. req:: Das Eigentümerpasswort setzt nur der Eigentümer
   :id: REQ_USR_PWD_03
   :status: approved
   :priority: high
   :links: REQ_USR_PWD_02, REQ_USR_EIGT_05

   Für das Konto mit der Rolle Eigentümer kann kein Administrator ein
   Passwort setzen. Nur der Eigentümer selbst ändert es.

   Ohne diese Regel wäre der Schutz des Eigentümerkontos wirkungslos: Ein
   Administrator setzte ein neues Passwort, meldete sich an und gäbe sich
   die Rolle selbst weiter.

Registrierung
-------------

.. req:: Selbstregistrierung
   :id: REQ_USR_REG_01
   :status: approved
   :priority: high

   Eine Person kann sich ohne Zutun eines Administrators selbst
   registrieren. Dabei entsteht ein aktives Benutzerkonto mit der Rolle
   Trainer.

.. req:: Angaben bei der Registrierung
   :id: REQ_USR_REG_04
   :status: approved
   :priority: high
   :links: REQ_USR_REG_01, REQ_USR_PROF_01

   Die Registrierung verlangt Name, E-Mail-Adresse und Passwort. Weitere
   Angaben sind nicht nötig.

.. req:: Bereits vergebene E-Mail-Adresse
   :id: REQ_USR_REG_05
   :status: approved
   :priority: high
   :links: REQ_USR_REG_04, REQ_USR_LOGIN_01

   Vor der Speicherung werden Leerzeichen am Anfang und Ende der
   E-Mail-Adresse entfernt. E-Mail-Adressen werden bei Registrierung und
   Anmeldung unabhängig von Groß- und Kleinschreibung verglichen. Ist die
   angegebene E-Mail-Adresse danach bereits vergeben, wird die Registrierung
   abgewiesen und darauf hingewiesen. Es entsteht kein zweites Konto zur
   selben Adresse.

.. req:: Konto ist sofort nutzbar
   :id: REQ_USR_REG_02
   :status: approved
   :links: REQ_USR_REG_01

   Ein neu registriertes Konto ist unmittelbar nach der Registrierung
   anmeldbar. Es wartet auf keine Freischaltung durch einen Administrator.

.. req:: Neues Konto ohne Qualifikationen
   :id: REQ_USR_REG_03
   :status: approved
   :links: REQ_USR_REG_02, REQ_USR_ROLLE_08

   Ein neu registriertes Konto besitzt keine Qualifikationen. Es kann den
   Schulungskatalog einsehen, sich auf Qualifikationen bewerben und auf
   Assistenzplätze bewerben, kann aber keinen Termin als ausführender
   Trainer übernehmen.

Konto beenden
-------------

.. req:: Benutzerkonto stilllegen
   :id: REQ_USR_ENDE_01
   :status: approved
   :priority: low
   :links: REQ_USR_PROF_02

   Ein Administrator kann ein Benutzerkonto stilllegen. Ein stillgelegtes
   Konto kann sich nicht mehr anmelden. Seine Profildaten und Vorgänge
   bleiben erhalten; ausschließlich zukünftige Trainer- und
   Assistenzzuweisungen werden nach :need:`REQ_USR_ENDE_04` entfernt.

.. req:: Stilllegen löst zukünftige Zuweisungen
   :id: REQ_USR_ENDE_04
   :status: approved
   :priority: high
   :links: REQ_USR_ENDE_01, REQ_ASS_PLATZ_01, REQ_TER_STAT_01, REQ_TER_ZEIT_01

   Beim Stilllegen wird das Konto aus allen zukünftigen Terminen
   herausgenommen, denen es zugewiesen ist -- als ausführender Trainer wie
   als Assistent. Bei abgeschlossenen Terminen bleibt es eingetragen.

   Ohne diese Regel hätte ein zukünftiger Termin einen Trainer, der sich
   nicht mehr anmelden kann, und er erschiene dem Administrator nicht unter
   den Terminen ohne Trainer.

.. req:: Stillgelegtes Konto reaktivieren
   :id: REQ_USR_ENDE_05
   :status: approved
   :priority: low
   :links: REQ_USR_ENDE_01

   Ein Administrator kann ein stillgelegtes Konto wieder aktivieren. Es ist
   danach wieder anmeldbar. Zuweisungen, die beim Stilllegen entfallen sind,
   entstehen dabei nicht neu.

.. req:: Benutzerkonto löschen
   :id: REQ_USR_ENDE_02
   :status: approved
   :priority: low
   :links: REQ_USR_ENDE_01, REQ_NAC_ANZ_05

   Ein Administrator kann ein Benutzerkonto löschen. Es verschwindet samt
   seinen Qualifikationen, Vormerkungen, Assistenzbewerbungen, offenen
   Anfragen, Abwesenheiten und Mitteilungen.

.. req:: Termine verlieren durch Löschen ihren Trainer
   :id: REQ_USR_ENDE_03
   :status: approved
   :priority: low
   :links: REQ_USR_ENDE_02, REQ_ASS_PLATZ_01, REQ_TER_ZEIT_01

   War das gelöschte Konto einem zukünftigen Termin zugewiesen, wechselt
   dieser in den Zustand "nicht zugewiesen"; war es dort Assistent, wird der
   Assistenzplatz wieder frei.

.. req:: Historie behält den Namen
   :id: REQ_USR_ENDE_06
   :status: approved
   :priority: high
   :links: REQ_USR_ENDE_02, REQ_ASS_PLATZ_01, REQ_TER_STAT_01

   Bei abgeschlossenen Terminen werden die Zuweisungen als ausführender
   Trainer und als Assistent beim Löschen von einem Verweis auf das Konto in
   einen unveränderlichen Namens-Snapshot umgeschrieben. Die Angabe, wer eine
   Schulung gehalten oder assistiert hat, überlebt damit das Löschen, ohne
   dass das Konto selbst weiterbestehen muss. Das Löschen wird deswegen
   nicht verweigert.
