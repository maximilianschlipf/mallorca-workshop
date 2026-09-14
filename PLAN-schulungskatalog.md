# Umsetzungsplan Schulungskatalog

Branch `schulungskatalog`. Grundlage: `docs/source/anforderungen/katalog.rst`
(Anforderungen) und `katalog-umsetzung.rst` (Stories und Tests).

## Getroffene Entscheidungen

| Frage | Entscheidung |
| --- | --- |
| Umgang mit Fremdbereichen | Nur autarke Tests. Keine Stubs für Benutzerkonten oder Qualifikationen. |
| Katalogablage | Eigenes Verzeichnis im Projekt-Repo, Commits über JGit. |
| Seed-Migration | Einmalig aufspalten, Termine bleiben in der Datenbank. |
| Frontend | Erst Backend; die Oberfläche folgt in einem eigenen Branch. |
| Endpunkt-Tests | Spring Boot auf 4.1.0 heben, dann `RestTestClient`. |

## Ausgangslage und was ihr widerspricht

Heute liegen die Schulungsbeschreibungen in H2, geseedet aus einer einzigen
`schulungen.json`. Kategorien werden per `SELECT DISTINCT` aus der Tabelle
abgeleitet. Es gibt nur Lese-Endpunkte.

Die Anforderungen verlangen etwas anderes:

- `REQ_KAT_ABL_01/02` — eine JSON-Datei **je Schulung** an festem Ort im Projekt.
- `REQ_KAT_ABL_03` — jede Beschreibungsänderung wird als **Commit** gesichert.
- `REQ_KAT_KATG_05` — Kategorien als **eigene, versionierte** JSON-Datei, nicht
  abgeleitet.
- `REQ_KAT_FELD_01` — der Zustand aktiv/archiviert steht **nicht** im Katalog,
  sondern in der Datenbank.

Das ist ein Umbau der Speicherschicht, kein Anbau. Deshalb steht er am Anfang.

## Testumfang

45 Tests sind spezifiziert. 38 kommen in diesen Branch, 7 nicht.

**Nicht in diesem Branch** — mit Begründung, damit nachvollziehbar bleibt, warum:

| Test | Blockiert durch |
| --- | --- |
| `TEST_KAT_SICHT_01` | Benutzerkonten und Rollen |
| `TEST_KAT_SICHT_03` | Qualifikationen, Freigabeanfragen |
| `TEST_KAT_SICHT_04` | Trainersicht |
| `TEST_KAT_ARCH_03` | Freigabeanfragen |
| `TEST_KAT_ARCH_04` | Freigabeanfragen |
| `TEST_KAT_ABL_04` | `:automated: no`, bleibt manuell |
| `TEST_KAT_TERM_02` (Teil) | Meldung in der Oberfläche; Backend-Teil kommt mit |

Tests, die nur die **bereits vorhandene** `termin`-Tabelle brauchen
(`PFLEG_02`, `TERM_01`, `ARCH_01/02`, `LOE_01`–`LOE_04`), sind drin. Das ist
kein Stubben eines Fremdbereichs, sondern Nutzung dessen, was steht.

## Zielarchitektur Backend

```
de.nordwind.schulungsplaner.katalog
├── SchulungId              Wertobjekt, Zeichenprüfung, Schutz vor ../
├── Katalogschulung         Record, genau die acht Felder aus REQ_KAT_FELD_01
├── ablage
│   ├── KatalogVerzeichnis  Pfadauflösung, konfigurierbar
│   ├── KatalogRepository   JSON je Schulung lesen/schreiben/löschen
│   ├── KategorienRepository kategorien.json
│   └── KatalogCommitter    Interface + JGitCommitter + NoOpCommitter
├── pruefung
│   └── SchulungPruefung    reine Funktion, ohne Spring, ohne Dateisystem
├── zustand
│   └── SchulungszustandRepository   aktiv/archiviert in der Datenbank
├── KatalogService          Anlegen, Ändern, Archivieren, Löschen, Aufnehmen
├── KategorieService
└── api.KatalogController
```

Leitgedanke: Die Prüfregeln (`SchulungPruefung`) kennen weder Dateisystem noch
Spring. Sie sind damit in reinen Unit-Tests abprüfbar, und derselbe Code trägt
die Eingabe über die Oberfläche **und** die Aufnahme aus Dateien — genau das,
was `REQ_KAT_IMP_02` fordert („dieselben Regeln wie eine Eingabe über die
Oberfläche").

### Ablage im Repository

```
katalog/
├── kategorien.json
└── schulungen/
    ├── SCH-001.json
    └── SCH-002.json
```

Der Pfad kommt aus `schulungsplaner.katalog.pfad`. Im Test zeigt er auf ein
`@TempDir`-Verzeichnis mit eigenem, von JGit angelegten Repository. **Kein Test
schreibt je ins echte Projekt-Repository.** Das ist die Bedingung dafür, dass
`TEST_KAT_ABL_02` (Commit-Prüfung) überhaupt wiederholbar laufen kann.

### Datenbank

`schema.sql` bekommt `schulung_zustand` (`schulung_id`, `zustand`,
`archiviert_am`, `version`). Die Tabellen `schulung` und `voraussetzung`
entfallen — ihre Inhalte wandern in den Katalog. Die Fremdschlüssel aus
`termin` und `trainer_qualifikation` hängen auf `schulung_zustand` um.

`DEC_DAT_SCHEMA_01` warnt, dass `CREATE TABLE IF NOT EXISTS` keine Spalte
nachrüstet. Das trifft hier nicht: Die Datenbank läuft im Speicher
(`jdbc:h2:mem:`) und entsteht bei jedem Start neu. Sobald `REQ_DAT_DB_01`
(Dateimodus) umgesetzt wird, ist die Entscheidung fällig — das gehört in den
Bereich `DAT`, nicht hierher.

### Endpunkte

| Methode | Pfad | Zweck | Test |
| --- | --- | --- | --- |
| `GET` | `/api/schulungen?suche&kategorie` | Liste, aktive vor archivierten | `SUCH_01`–`05`, `SICHT_02` |
| `GET` | `/api/schulungen/{id}` | Einzelne Schulung | `ANL_01` |
| `GET` | `/api/schulungen/id-schema` | Schema der bestehenden Kennungen | `ID_04` |
| `POST` | `/api/schulungen` | Anlegen | `ANL_01`–`05`, `ID_01`, `ID_02` |
| `PUT` | `/api/schulungen/{id}` | Ändern, Antwort trägt Dauer-Warnung | `PFLEG_01`, `PFLEG_02`, `ID_03` |
| `DELETE` | `/api/schulungen/{id}` | Löschen | `LOE_01`–`04` |
| `POST` | `/api/schulungen/{id}/archivierung` | Archivieren | `ARCH_01`, `ARCH_02` |
| `DELETE` | `/api/schulungen/{id}/archivierung` | Reaktivieren | `ARCH_02` |
| `POST` | `/api/schulungen/aufnahme?ersetzen=` | Dateien aufnehmen | `IMP_01`–`04` |
| `GET` | `/api/kategorien` | Liste, doppelfrei, sortiert | `SUCH_06` |
| `POST` | `/api/kategorien` | Anlegen | `KATG_04` |
| `PUT` | `/api/kategorien/{name}` | Umbenennen | `KATG_02` |
| `DELETE` | `/api/kategorien/{name}` | Löschen | `KATG_03` |

Einheitlicher Fehlerkörper mit `code`, `feld` und `meldung`. `TEST_KAT_IMP_02`
verlangt, dass „die Rückmeldung den Grund nennt" — ein nacktes 400 genügt dafür
nicht, der Test prüft den Grund.

## Arbeitsschritte

Jeder Schritt endet mit grünem `./mvnw test` und einem eigenen Commit.
Reihenfolge je Schritt: **Test schreiben, rot sehen, umsetzen, grün, aufräumen.**

### Schritt 0 — Spring Boot 4.1.0

Kein Katalogcode. Erst die Grundlage, auf der alle folgenden Tests stehen.

- `pom.xml`: Parent 3.5.6 → 4.1.0.
- Die fünf bestehenden Testklassen von `TestRestTemplate` auf `RestTestClient`
  umstellen. Sie sind die Probe, dass der Sprung sitzt — sie prüfen bereits
  Verhalten, das sich nicht ändern darf.
- Abnahme: `./mvnw test` grün, `npm run test:e2e` grün.

Risiko, das ich beim Umstellen im Blick habe: Boot 4 hat die Starter neu
geschnitten und einige Testannotationen umbenannt. Das Backend ist klein genug,
dass das überschaubar bleibt; falls der Sprung mehr aufreißt als erwartet,
melde ich mich, bevor ich weitermache.

### Schritt 1 — Ablage: Dateien und Commits

Reine Unit-Tests, noch kein Endpunkt.

1. `SchulungIdTest` → `SCH-009` und `ABC-1` angenommen; `Scrum / Basis`,
   `sch-009`, `SCH_009`, `../SCH-009` abgewiesen, und bei `../SCH-009` entsteht
   nachweislich keine Datei außerhalb des Verzeichnisses. — `TEST_KAT_ID_01`
2. `KatalogschulungTest` → Serialisierung führt genau die acht Felder, keinen
   Zustand, keine Termine; Voraussetzungen bleiben unveränderter Freitext, leere
   Liste zulässig. — `TEST_KAT_ANL_06`, `TEST_KAT_ANL_07`
3. `SchulungPruefungTest` → Pflichtangaben, Dauer ≥ 1, Höchstzahl über
   Mindestzahl, fehlende oder 0-Höchstzahl als „keine Obergrenze", fehlende ID.
   — `TEST_KAT_ANL_02`–`05`, `TEST_KAT_ID_04`
4. `KatalogRepositoryTest` → nach dem Anlegen genau eine neue Datei, benannt
   nach der ID. — `TEST_KAT_ABL_01`
5. `JGitCommitterTest` → gegen ein `@TempDir`-Repository: nach dem Schreiben ein
   neuer Commit, der genau diese Datei verändert. — `TEST_KAT_ABL_02`
6. `KategorienRepositoryTest` → `kategorien.json` im Katalogverzeichnis, Anlegen
   erzeugt einen Commit auf diese Datei. — `TEST_KAT_KATG_04`

### Schritt 2 — Zustand und Seed-Migration

- `schema.sql` umbauen, `SeedService` und `SchulungenSeedRoot` anpassen.
- Die vorhandene `schulungen.json` einmalig aufspalten: je Schulung eine Datei
  unter `katalog/schulungen/`, dazu `katalog/kategorien.json`. Beides wird
  eingecheckt. Der Seed behält Termine, Trainer, Qualifikationen und die
  Zustände.
- `SchulungszustandRepositoryTest`, dazu `TEST_KAT_ABL_03`: Archivieren und
  Reaktivieren lassen die Commit-Zahl unverändert und die Katalogdatei bitgleich.

Hier liegt der einzige Punkt, an dem bestehendes Verhalten kippen kann. Die
vorhandenen Tests `SeedDataIntegrationTest` und `SeedResourcesTest` sind das
Netz und müssen grün bleiben.

### Schritt 3 — Lesen: Suche, Filter, Sortierung

`KatalogQueryService` liest aus der Dateiablage statt aus SQL, verknüpft Termine
weiterhin über die Schulungs-ID aus der Datenbank.

- Die sechs bestehenden Suchtests (`SUCH_01`–`06`) müssen **unverändert** grün
  bleiben. Sie sind die Zusicherung, dass der Umbau der Ablage das Verhalten
  nicht verschiebt.
- Neu: `SICHT_02` (archivierte gekennzeichnet und hinter allen aktiven),
  `TERM_01` (Termin liefert Titel und Kategorie mit), `TERM_02` (Verweis ohne
  Katalogdatei wird ausdrücklich gemeldet).
- Endpunkt-Tests mit `RestTestClient`.

### Schritt 4 — Anlegen und Bearbeiten

`POST` und `PUT` samt Fehlerkörper.

- `ANL_01` vollständig und aktiv, `ID_02` vergebene ID abgewiesen und Bestand
  unverändert, `ID_03` ID nicht änderbar, `PFLEG_01` alle übrigen Felder
  änderbar, `PFLEG_02` geänderte Dauer warnt und lässt den Termin-Zeitraum stehen.

### Schritt 5 — Kategorien

- `KATG_01` unbekannte Kategorie beim Anlegen abgewiesen und Liste unverändert
  lang, `KATG_02` Umbenennen wirkt auf alle zugeordneten Schulungen und der
  Filter findet sie darunter, `KATG_03` Kategorie in Gebrauch nicht löschbar,
  nach dem Umhängen der letzten Schulung schon.

### Schritt 6 — Archivieren, Reaktivieren, Löschen

- `ARCH_01`, `ARCH_02`, `LOE_01`–`LOE_04`.
- `LOE_04` verlangt, dass der Titel bei abgeschlossenen Terminen als Text
  erhalten bleibt — dafür bekommt `termin` eine Spalte `schulung_titel`, die
  beim Löschen gefüllt wird.

### Schritt 7 — Aufnahme aus Dateien

- `IMP_01`–`IMP_04`, gegen dieselbe `SchulungPruefung` wie Schritt 1.
- `IMP_04`: Ohne `?ersetzen=true` bleibt die bestehende Schulung unverändert und
  die Antwort sagt, dass eine Entscheidung aussteht.

### Schritt 8 — Traceability nachziehen

`katalog-umsetzung.rst` bekommt je umgesetztem Test die Zeile „Besteht als
`methodenName`", so wie es die sechs Suchtests heute schon führen. Die sieben
zurückgestellten Tests werden als solche kenntlich gemacht. Danach `make html`
— der Build läuft mit `-W`, Warnungen sind Fehler.

## Später: Frontend

Eigener Branch, nach Abnahme des Backends. Damit die Entscheidung festgehalten
ist, hier der vorgesehene Zuschnitt:

- `vue-router` einführen, `App.vue` (603 Zeilen) in Komponenten zerlegen.
- Ansichten: Katalogliste, Schulungsdetail, Schulungsformular,
  Kategorienpflege, Aufnahme-Dialog.
- Playwright nach **Page-Object-Modell**: je Ansicht eine Klasse unter
  `tests/e2e/pages/`, die Selektoren kapselt und fachliche Methoden anbietet
  (`KatalogSeite.sucheNach(begriff)`, `SchulungsFormular.ausfuellen(daten)`).
  Die Spezifikationsdateien enthalten danach keine Selektoren mehr, nur noch
  Ablauf und Erwartung. Selektoren durchgehend über Rollen und Beschriftungen,
  nicht über CSS-Klassen — die heutigen `.course-card`-Zugriffe wandern in die
  Page Objects und werden dort ersetzt.
- Vitest weiterhin für Komponentenlogik, Playwright für die Abläufe.

## Was ich unterwegs melden werde

- Wenn der Boot-4-Sprung in Schritt 0 mehr aufreißt als die fünf Testklassen.
- Wenn sich beim Aufspalten des Seeds zeigt, dass Frontend oder E2E-Tests am
  bisherigen Antwortformat hängen, das ich nicht unverändert halten kann.
