Benutzerkonten und Anmeldung
============================

Rollenmodell
------------

.. decision:: Ein Benutzerkonto kann mehrere Rollen tragen
   :id: DEC_USR_ROLLE_01
   :status: approved

   Ein Benutzerkonto hält eine Menge von Rollen, nicht genau eine. Ein
   Benutzerkonto kann die Rolle Trainer tragen, die Rolle Administrator oder
   beide. Trägt es beide, besitzt es ein eigenes Trainerprofil und kann
   Schulungen halten.

   Die Alternative -- genau eine Rolle, bei der "Administrator" die
   Trainerrechte implizit einschließt -- wurde verworfen, weil ein
   Administrator ohne Trainerprofil keine Qualifikationen und keine
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
   :status: draft
   :links: DEC_USR_ROLLE_01

   Ein Benutzerkonto trägt mindestens eine der Rollen Trainer,
   Administrator oder Eigentümer. Die Rolle Administrator umfasst die
   Verwaltung von Schulungskatalog, Terminen, Qualifikationen und
   Abwesenheitsanträgen.

.. req:: Administrator mit Trainerprofil
   :id: REQ_USR_ROLLE_02
   :status: draft
   :links: DEC_USR_ROLLE_01

   Trägt ein Benutzerkonto Administrator- und Trainerrolle, stehen ihm die
   Trainerfunktionen in vollem Umfang zur Verfügung: es besitzt ein
   Trainerprofil, kann für Schulungen qualifiziert werden, Abwesenheiten
   pflegen und Terminen zugewiesen werden. Das ist der Normalfall für
   Administratoren.

.. req:: Administratorrolle vergeben
   :id: REQ_USR_ROLLE_03
   :status: draft
   :links: DEC_USR_ROLLE_01

   Ein Administrator kann einem anderen Benutzerkonto die Rolle
   Administrator erteilen. Bestehende Rollen des Kontos bleiben dabei
   erhalten.

.. req:: Rollen entziehen
   :id: REQ_USR_ROLLE_04
   :status: draft
   :links: REQ_USR_ROLLE_03

   Ein Administrator kann einem Benutzerkonto die Rolle Administrator oder
   die Rolle Trainer wieder entziehen.

.. req:: Ein Konto behält mindestens eine Rolle
   :id: REQ_USR_ROLLE_05
   :status: draft
   :links: REQ_USR_ROLLE_04

   Das Entziehen der letzten Rolle eines Kontos wird abgewiesen. Ein Konto
   ohne Rolle könnte sich anmelden, aber nichts tun.

.. req:: Reines Administratorkonto
   :id: REQ_USR_ROLLE_06
   :status: draft
   :links: REQ_USR_ROLLE_04

   Ein Konto, dem die Trainerrolle entzogen wurde, ist ein reines
   Administratorkonto: ohne Trainerprofil, ohne Qualifikationen, keinem
   Termin zuweisbar. Der Weg dorthin führt über Registrieren, Ernennen zum
   Administrator und Ablegen der Trainerrolle.

.. req:: Entzug der Trainerrolle löst Zuweisungen
   :id: REQ_USR_ROLLE_07
   :status: draft
   :priority: high
   :links: REQ_USR_ROLLE_06

   Wird einem Konto die Trainerrolle entzogen, wird es aus allen
   zukünftigen Terminen herausgenommen, denen es zugewiesen ist. Diese
   Termine wechseln in den Zustand "nicht zugewiesen". Bei abgeschlossenen
   Terminen bleibt es eingetragen.

Eigentümer
----------

.. req:: Erstes Konto erhält alle Rollen
   :id: REQ_USR_EIGT_01
   :status: draft
   :priority: high
   :links: DEC_USR_EIGT_01

   Das erste Benutzerkonto einer Instanz erhält bei seiner Registrierung die
   Rollen Trainer, Administrator und Eigentümer. Damit ist eine frische
   Instanz ohne weiteres Zutun handlungsfähig.

.. req:: Genau ein Eigentümer
   :id: REQ_USR_EIGT_02
   :status: draft
   :priority: high
   :links: DEC_USR_EIGT_01

   Zu jedem Zeitpunkt trägt genau ein Benutzerkonto die Rolle Eigentümer.

.. req:: Eigentümerrolle weitergeben
   :id: REQ_USR_EIGT_03
   :status: draft
   :priority: high
   :links: REQ_USR_EIGT_02

   Der Eigentümer kann die Rolle an ein anderes Benutzerkonto übergeben.
   Mit der Übergabe verliert er sie selbst; es gibt keinen Weg, sie
   abzulegen, ohne sie weiterzugeben.

.. req:: Eigentümer behält die Administratorrolle
   :id: REQ_USR_EIGT_04
   :status: draft
   :links: REQ_USR_EIGT_02

   Solange ein Konto die Rolle Eigentümer trägt, trägt es auch die Rolle
   Administrator. Sie kann ihm nicht entzogen werden.

.. req:: Eigentümerkonto ist geschützt
   :id: REQ_USR_EIGT_05
   :status: draft
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
   :status: draft
   :priority: high

   Ein Benutzerkonto meldet sich mit seiner E-Mail-Adresse und einem
   Passwort an. Die E-Mail-Adresse ist die Anmeldekennung und über alle
   Benutzerkonten eindeutig.

.. req:: Zugriff nur nach Anmeldung
   :id: REQ_USR_LOGIN_02
   :status: draft
   :priority: high
   :links: REQ_USR_LOGIN_01

   Ohne erfolgreiche Anmeldung ist keine Ansicht der Anwendung erreichbar.

.. req:: Passwörter nur als gesalzener Hash
   :id: REQ_USR_LOGIN_03
   :status: draft
   :priority: high
   :links: REQ_USR_LOGIN_01

   Ein Passwort wird niemals im Klartext gespeichert, sondern ausschließlich
   als Hash mit einem je Konto eigenen Salt. In der Datenbank steht nur der
   Hash.

.. req:: Sitzung endet mit dem Browser
   :id: REQ_USR_LOGIN_04
   :status: draft
   :links: REQ_USR_LOGIN_01

   Eine Anmeldung gilt, bis der Browser geschlossen wird. Es gibt keine
   darüber hinausgehende Ablauffrist und kein "angemeldet bleiben".

.. req:: Abmelden
   :id: REQ_USR_LOGIN_05
   :status: draft
   :links: REQ_USR_LOGIN_04

   Ein angemeldeter Benutzer kann sich abmelden, ohne den Browser zu
   schließen.

.. req:: Passwort ändern
   :id: REQ_USR_PWD_01
   :status: draft
   :links: REQ_USR_LOGIN_03

   Ein angemeldeter Benutzer kann sein eigenes Passwort ändern.

.. req:: Passwort durch Administrator setzen
   :id: REQ_USR_PWD_02
   :status: draft
   :links: DEC_USR_SICHER_01

   Ein Administrator kann für ein Benutzerkonto ein neues Passwort setzen.
   Das ist der einzige Weg zurück in ein Konto mit vergessenem Passwort.

Registrierung
-------------

.. req:: Selbstregistrierung als Trainer
   :id: REQ_USR_REG_01
   :status: draft

   Eine Person kann sich ohne Zutun eines Administrators selbst als Trainer
   registrieren. Dabei entsteht ein Benutzerkonto mit der Rolle Trainer und
   ein zugehöriges Trainerprofil.

.. req:: Angaben bei der Registrierung
   :id: REQ_USR_REG_04
   :status: draft
   :links: REQ_USR_REG_01

   Die Registrierung verlangt Name, E-Mail-Adresse und Passwort. Weitere
   Angaben sind nicht nötig.

.. req:: Trainerkonto ist sofort nutzbar
   :id: REQ_USR_REG_02
   :status: draft
   :links: REQ_USR_REG_01

   Ein neu registriertes Trainerkonto ist unmittelbar nach der Registrierung
   anmeldbar. Es wartet auf keine Freischaltung durch einen Administrator.

.. req:: Neues Trainerkonto ohne Qualifikationen
   :id: REQ_USR_REG_03
   :status: draft
   :links: REQ_USR_REG_02

   Ein neu registriertes Trainerkonto besitzt keine Qualifikationen. Es kann
   den Schulungskatalog einsehen und sich auf Qualifikationen bewerben, kann
   aber keinem Termin zugewiesen werden, solange keine Qualifikation
   vorliegt.

Konto beenden
-------------

.. req:: Benutzerkonto stilllegen
   :id: REQ_USR_ENDE_01
   :status: draft
   :priority: low

   Ein Administrator kann ein Benutzerkonto stilllegen. Ein stillgelegtes
   Konto kann sich nicht mehr anmelden, bleibt aber mit allen Daten
   erhalten.

.. req:: Stilllegen löst zukünftige Zuweisungen
   :id: REQ_USR_ENDE_04
   :status: draft
   :priority: high
   :links: REQ_USR_ENDE_01

   Beim Stilllegen wird das Konto aus allen zukünftigen Terminen
   herausgenommen, denen es zugewiesen ist; diese wechseln in den Zustand
   "nicht zugewiesen". Bei abgeschlossenen Terminen bleibt es eingetragen.

   Ohne diese Regel hätte ein zukünftiger Termin einen Trainer, der sich
   nicht mehr anmelden kann, und er erschiene dem Administrator nicht unter
   den Terminen ohne Trainer.

.. req:: Stillgelegtes Konto reaktivieren
   :id: REQ_USR_ENDE_05
   :status: draft
   :priority: low
   :links: REQ_USR_ENDE_01

   Ein Administrator kann ein stillgelegtes Konto wieder aktivieren. Es ist
   danach wieder anmeldbar. Zuweisungen, die beim Stilllegen entfallen sind,
   entstehen dabei nicht neu.

.. req:: Benutzerkonto löschen
   :id: REQ_USR_ENDE_02
   :status: draft
   :priority: low
   :links: REQ_USR_ENDE_01

   Ein Administrator kann ein Benutzerkonto löschen. Das Konto verschwindet
   samt Trainerprofil, Qualifikationen, Vormerkungen, offenen Anfragen und
   Abwesenheiten.

.. req:: Termine verlieren durch Löschen ihren Trainer
   :id: REQ_USR_ENDE_03
   :status: draft
   :priority: low
   :links: REQ_USR_ENDE_02

   War das gelöschte Konto einem zukünftigen Termin zugewiesen, wechselt
   dieser Termin in den Zustand "nicht zugewiesen" und erscheint wieder
   unter den Terminen ohne Trainer.

.. req:: Historie behält den Namen
   :id: REQ_USR_ENDE_06
   :status: draft
   :priority: high
   :links: REQ_USR_ENDE_02

   Bei abgeschlossenen Terminen wird die Trainerzuweisung beim Löschen von
   einem Verweis auf das Konto in den reinen Namen umgeschrieben. Die
   Angabe, wer eine Schulung gehalten hat, überlebt damit das Löschen, ohne
   dass das Konto selbst weiterbestehen muss. Das Löschen wird deswegen
   nicht verweigert.
