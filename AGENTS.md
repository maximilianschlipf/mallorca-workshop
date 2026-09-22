# Workshop-Regeln

- Nicht überentwickeln. Maßgeblich sind `AUFGABEN.md` und der Scope unten.
- Betroffene Needs vor Änderungen vollständig lesen.
- Commits vor `workshop/05-start-v2` und andere `workshop/*`-Branches nicht als
  Lösungsquelle verwenden.
- `feature-code-review` und seine Gates sind optional und werden nur auf
  ausdrücklichen Wunsch ausgeführt.

## Aufgabe 5

Nur Dokumentation ändern: höchstens drei offene Fragen, eine Entscheidung und
ein bis zwei abgeleitete Stories. Test-Needs und Produktcode sind nicht Teil der
Aufgabe; Assistenz nicht als vollständig nachgewiesen melden. Basis eines
optionalen Reviews ist `workshop/05-start-v2`; dafür genügen
`python3 -m unittest scripts.test_check_requirements`,
`python3 scripts/check_requirements.py` und `make -C docs html`.
