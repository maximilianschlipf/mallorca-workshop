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

Dieser Branch ist der Ausgangspunkt für Aufgabe 5 aus `AUFGABEN.md`. Die
Assistenz-Needs werden hier fachlich weiterentwickelt, aber nicht implementiert
oder als vollständig nachgewiesen gemeldet. Verbindlicher Workshop-Umfang sind
höchstens drei offene Fragen, eine dokumentierte Entscheidung und ein bis zwei
abgeleitete Stories; Test-Needs und Produktcode sind nicht Teil der Aufgabe.

Review-Basis ist der Tag `workshop/05-start-v2`. Geprüft werden nur Änderungen
danach sowie staged, unstaged und untracked Änderungen. Für diese reine
Dokumentationsaufgabe ersetzt folgendes Gate `./verify.sh`:

Commits vor dem Review-Basis-Tag und andere `workshop/*`-Branches dürfen vor
Abschluss der eigenen Lösung nicht als Lösungsquelle untersucht werden.

1. `python3 -m unittest scripts.test_check_requirements`
2. `python3 scripts/check_requirements.py`
3. `make -C docs html`

Produkt-, Frontend- und E2E-Tests sind nur erforderlich, wenn entgegen dem
Aufgabenscope Produktcode verändert wurde. Die Dokumentationsmatrix des
Review-Skills belegt die Erfüllung der Workshop-Aufgabe.
