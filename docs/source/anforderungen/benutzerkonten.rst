Benutzerkonten und Anmeldung
============================

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

.. req:: Anmeldung mit E-Mail und Passwort
   :id: REQ_USR_LOGIN_01
   :status: draft
   :priority: high

   Ein Benutzerkonto meldet sich mit seiner E-Mail-Adresse und einem Passwort
   an. Die E-Mail-Adresse ist die Anmeldekennung und über alle Benutzerkonten
   eindeutig.

.. req:: Zugriff nur nach Anmeldung
   :id: REQ_USR_LOGIN_02
   :status: draft
   :priority: high
   :links: REQ_USR_LOGIN_01

   Ohne erfolgreiche Anmeldung ist keine Ansicht der Anwendung erreichbar.

.. req:: Rollen Trainer und Administrator
   :id: REQ_USR_ROLLE_01
   :status: draft
   :links: DEC_USR_ROLLE_01

   Ein Benutzerkonto trägt die Rolle Trainer, die Rolle Administrator oder
   beide. Die Rolle Administrator umfasst die Verwaltung von
   Schulungskatalog, Terminen, Qualifikationen und Abwesenheitsanträgen.

.. req:: Administrator mit Trainerprofil
   :id: REQ_USR_ROLLE_02
   :status: draft
   :links: DEC_USR_ROLLE_01

   Trägt ein Benutzerkonto beide Rollen, stehen ihm die Trainerfunktionen in
   vollem Umfang zur Verfügung: es besitzt ein Trainerprofil, kann für
   Schulungen qualifiziert werden, Abwesenheiten pflegen und Terminen
   zugewiesen werden.

.. req:: Administratorrolle vergeben
   :id: REQ_USR_ROLLE_03
   :status: draft
   :links: DEC_USR_ROLLE_01

   Ein Administrator kann einem anderen Benutzerkonto die Rolle
   Administrator erteilen. Bestehende Rollen des Kontos bleiben dabei
   erhalten.

.. req:: Selbstregistrierung als Trainer
   :id: REQ_USR_REG_01
   :status: draft

   Eine Person kann sich ohne Zutun eines Administrators selbst als Trainer
   registrieren. Dabei entsteht ein Benutzerkonto mit der Rolle Trainer und
   ein zugehöriges Trainerprofil.

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
