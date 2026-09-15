# Arbeitsregeln

Don't over engineer!

## Abschluss-Gates für Anforderungen

Bei Änderungen, die Requirements umsetzen oder verändern:

1. Vor der Implementierung alle betroffenen freigegebenen Requirements,
   Stories und Tests vollständig lesen.
2. Den vollständigen fachlichen Umfang in `requirements-scope.txt` eintragen.
   Eine Einschränkung oder Verschiebung ist nur mit ausdrücklicher Zustimmung
   des Benutzers zulässig.
3. Einen `TEST_*`-Nachweis erst dann mit `verifies: TEST_...` an einem
   ausführbaren Test ergänzen, wenn dieser das gesamte beschriebene Szenario
   prüft. Teilprüfungen zählen nicht.
4. Vor einer Fertigmeldung muss `./verify.sh` vollständig erfolgreich sein.
   Bei einem Fehler ist die Arbeit als unvollständig zu melden, einschließlich
   der offenen IDs.
5. Danach den gesamten Diff unabhängig gegen die Spezifikation prüfen. Offene,
   partielle oder nicht Ende-zu-Ende erreichbare Anforderungen verhindern die
   Fertigmeldung auch bei grünen Builds.
6. Der Abschlussbericht nennt für jeden Scope das Ergebnis des
   Requirements-Gates und der vier Test-/Build-Befehle. „Alle Anforderungen
   umgesetzt“ ist nur bei einem vollständig grünen Gate zulässig.
