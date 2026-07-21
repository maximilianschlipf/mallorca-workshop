# KI-Arbeitszyklus

Jedes Feature durchläuft dieselben neun Gates:

1. Feature-Branch anlegen.
2. Anforderung lesen und offene fachliche Fragen sammeln.
3. Relevante Kontextdateien auswählen und irrelevante Dateien bewusst ausschließen.
4. Copilot ausschließlich einen Plan erstellen lassen.
5. Plan fachlich und technisch reviewen und explizit freigeben.
6. Den freigegebenen Plan im Agent-Modus umsetzen lassen.
7. Diff, Anwendung und relevante Tests menschlich prüfen.
8. Mindestens einen KI-Vorschlag begründet korrigieren oder ablehnen.
9. Commit erstellen und das Feature-Learning-Log abschließen.

## Rotierende Rollen

- AI Driver formuliert Prompts und bedient Copilot.
- Context Curator wählt Anforderungen, Dateien und Regeln aus.
- Reviewer/QA prüft Plan, Diff, Tests und fachliches Verhalten.

Die Rollen wechseln nach jedem Feature. Keine Person bedient Copilot in zwei aufeinanderfolgenden Features.

## Definition of Done

- Alle Akzeptanzkriterien sind demonstriert.
- Relevante automatisierte Tests laufen grün.
- Jeder geänderte Dateityp kann vom Team erklärt werden.
- Ein übernommener und ein verworfener oder korrigierter KI-Vorschlag sind dokumentiert.
- Der Git-Diff enthält keine fachfremden Änderungen.
