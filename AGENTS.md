# Arbeitsregeln

## Änderungen mit Anforderungen

Bei Änderungen, die Requirements umsetzen oder verändern:

1. Vor der Implementierung alle betroffenen Requirements, Stories und Tests
   mit Status `approved`, `implemented` oder `verified` vollständig lesen.
2. Neue Fachbereiche zusammen mit ihrer Umsetzung in
   `implemented-requirements.txt` aufnehmen. Jeder Eintrag verpflichtet zum
   vollständigen Nachweis vor der Fertigmeldung. Das Register ist kumulativ,
   kein Task-Scope; bestehende Einträge nicht entfernen.
3. Einen `TEST_*`-Nachweis erst dann mit `verifies: TEST_...` an einem
   ausführbaren Test ergänzen, wenn dieser das gesamte beschriebene Szenario
   prüft. Teilprüfungen zählen nicht.
4. Eine Einschränkung oder Verschiebung des vereinbarten Umfangs braucht die
   ausdrückliche Zustimmung des Benutzers.

## Abschluss-Gate

Vor jeder Fertigmeldung den gesamten Diff mit dem Repo-Skill
`feature-code-review` prüfen. Der Skill bestimmt den Task-Scope, prüft Standards,
Spezifikation und Test-/QA-Qualität getrennt und führt `./verify.sh` genau einmal
aus. Nach Korrekturen den vollständigen Diff erneut prüfen und das Gate erneut
ausführen.

Offene Findings, unvollständige Matrixzeilen oder ein fehlgeschlagenes Gate
verhindern die Aussage „alle Anforderungen umgesetzt“.

## Workshop-Ausnahme dieses Branches

Dieser Branch ist der absichtlich unvollständige Ausgangspunkt für Aufgabe 1
aus `AUFGABEN.md`. Der Bereich Abwesenheiten wird nicht in
`implemented-requirements.txt` aufgenommen und nicht als vollständig umgesetzt
gemeldet. Im Workshop ist ausschließlich `STORY_ABW_ERF_02` mit
`TEST_ABW_ERF_03` verbindlich; `STORY_ABW_ERF_03` mit `TEST_ABW_ERF_04` ist
optional. Weitere `ABW`-Needs sind Folgearbeit.
