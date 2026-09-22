# Workshop-Regeln

- Nicht überentwickeln. Maßgeblich sind `AUFGABEN.md` und der Scope unten.
- Betroffene Needs vor Änderungen vollständig lesen.
- Commits vor `workshop/06-start-v2` und andere `workshop/*`-Branches nicht als
  Lösungsquelle verwenden.
- `feature-code-review` und seine Gates sind optional und werden nur auf
  ausdrücklichen Wunsch ausgeführt.

## Aufgabe 6

Nur Dokumentation ändern: für einen ausgewählten Vormerkungs-Teilprozess
höchstens zwei Stories und drei fachliche Test-Needs entwerfen. Test-Needs
bleiben `draft`, `review` oder `approved` und ohne ausführbaren Nachweis niemals
`verified`. Produktcode ist nicht Teil der Aufgabe. Basis eines optionalen
Reviews ist `workshop/06-start-v2`; dafür genügen
`python3 -m unittest scripts.test_check_requirements`,
`python3 scripts/check_requirements.py` und `make -C docs html`.
