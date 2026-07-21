# Workshop Scaffold Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Das Schulungsplaner-Grundgerüst wird portabel, fachlich vollständig und didaktisch konsistent, sodass eine Gruppe nach dem Klonen zuverlässig starten, testen und die vorgesehenen Copilot-Übungen durchführen kann.

**Architecture:** Die Seed-Daten werden als Backend-Classpath-Ressourcen geladen und vollständig in H2 persistiert. Playwright verwaltet Backend und Frontend auf den festgelegten Workshopports selbst. Repositoryweite Copilot-Regeln, konsolidierte Einstiegsdokumentation und konkrete Feature-Briefings bilden den didaktischen Vertrag, ohne die Übungsfeatures vorzuimplementieren.

**Tech Stack:** Java 21, Spring Boot 3.5.6, Spring JDBC, H2, Maven Wrapper, Vue 3, TypeScript 6, Vite 8, Vitest 4, Playwright Test 1.61, GitHub Copilot in VS Code

## Global Constraints

- Backend bleibt Spring Boot mit Java 21, Spring JDBC und H2 In-Memory.
- Frontend bleibt Vue 3 mit TypeScript und Vite; kein weiteres UI-Framework einführen.
- Standard-Port Backend bleibt `18081`; Standard-Port Frontend bleibt `15173`.
- Die Workshopports bleiben bewusst fest; vorhandene Override-Versprechen werden entfernt statt eine zusätzliche Konfigurationsschicht einzuführen.
- Die Anwendung benötigt keine Cloud-Dienste, Authentifizierung oder externe Persistenz.
- Deutsche UI-Texte und deutsche Workshopdokumentation beibehalten.
- Die bereits funktionierende Katalogansicht bleibt die unveränderte Ausgangsbasis.
- Suche/Filter, Trainer-Matching und Abwesenheitspflege werden nur als Briefings angelegt, nicht in diesem Hardening implementiert.
- Seed-Daten haben genau 8 Schulungen, 14 Termine, 8 Voraussetzungen, 5 Trainer, 10 Qualifikationen und 5 Abwesenheiten.
- Jeder Produktionscode-Fix beginnt mit einem fehlschlagenden Test und endet mit einem separaten Commit.
- Playwright MCP ist in der einheitlichen Workshopumgebung bereits aktiviert; dieses Hardening fügt keine zweite MCP-Konfiguration hinzu.
- Es werden keine zusätzlichen Lint-/Format-Abhängigkeiten eingeführt; Java-Tests, TypeScript-Typecheck, Vitest, Vite-Build und Playwright bilden die verpflichtenden Gates.

---

## File Structure

### Runtime and data

- `backend/src/main/resources/seed/schulungen.json`: kanonische Schulungs-Seed-Daten im Classpath.
- `backend/src/main/resources/seed/trainer.json`: kanonische Trainer-Seed-Daten im Classpath.
- `backend/src/main/resources/schema.sql`: H2-Schema einschließlich Trainer-Schulungs-Zuordnung.
- `backend/src/main/java/de/nordwind/schulungsplaner/service/SeedService.java`: liest Classpath-Ressourcen und persistiert den vollständigen Seed-Graphen.
- `backend/src/test/java/de/nordwind/schulungsplaner/SeedResourcesTest.java`: sichert Paketierung und JSON-Grundstruktur.
- `backend/src/test/java/de/nordwind/schulungsplaner/SeedDataIntegrationTest.java`: sichert die erwarteten Tabelleninhalte und Beziehungen.

### Self-contained E2E execution

- `frontend/playwright.config.ts`: startet Backend und Frontend und wartet auf beide Health-URLs.
- `frontend/tests/e2e/catalog-smoke.spec.ts`: prüft die API über denselben Vite-Proxy wie die Anwendung.

### AI and workshop contract

- `.github/copilot-instructions.md`: stabiler, knapper Repositorykontext für Copilot.
- `README.md`: einziger Einstiegspunkt aus dem Repo-Root.
- `docs/setup.md`, `docs/runbook.md`, `docs/testing.md`, `docs/architecture-overview.md`: portable, widerspruchsfreie technische Referenz.
- `setting-schulungsplaner.md`: beschreibt den vorbereiteten Ist-Zustand und den tatsächlichen Workshopumfang.
- `docs/workshop/arbeitszyklus.md`: gemeinsamer KI-Arbeitszyklus und rotierende Rollen.
- `docs/feature-briefings/00-ausgangsbasis.md`: dokumentiert den bereits implementierten Katalog-Read.
- `docs/feature-briefings/01-katalog-suche-filter.md`: geführtes Prompting-/Git-Feature.
- `docs/feature-briefings/02-trainer-verfuegbarkeit.md`: Kontext-, Modell- und Testdesign-Feature.
- `docs/feature-briefings/03-abwesenheit-erfassen.md`: selbstständiges Agent-/MCP-Feature.
- `docs/exercises/model-lab.md`: gemeinsamer, anonym ausgewerteter Modell-/Effort-Vergleich.
- `docs/templates/feature-learning-log.md`: knapper Reflexionsnachweis pro Feature.
- `docs/pilot-checklist.md`: reproduzierbarer technischer und didaktischer Pilotcheck.

### Repository hygiene

- `frontend/.gitignore`: ignoriert Playwright-Ausgaben.

---

## Execution Prerequisite

- [ ] **Plan als eigenes Dokument committen, bevor Implementierungsdateien geändert werden**

```bash
git add docs/superpowers/plans/2026-07-21-workshop-scaffold-hardening.md
git commit -m "docs: add workshop scaffold hardening plan"
```

Expected: `git status --short` ist leer und Task 1 beginnt auf einem nachvollziehbaren Stand.

---

### Task 1: Seed-Dateien unabhängig vom Arbeitsverzeichnis laden

**Files:**
- Create: `backend/src/test/java/de/nordwind/schulungsplaner/SeedResourcesTest.java`
- Move: `schulungen.json` → `backend/src/main/resources/seed/schulungen.json`
- Move: `trainer.json` → `backend/src/main/resources/seed/trainer.json`
- Modify: `backend/src/main/java/de/nordwind/schulungsplaner/service/SeedService.java`

**Interfaces:**
- Consumes: `SchulungenSeedRoot`, `TrainerSeedRoot`, Jackson `ObjectMapper`.
- Produces: `SeedService.readFromClasspath(String, Class<T>)`; die Anwendung startet aus jedem Arbeitsverzeichnis mit denselben Seed-Daten.

- [ ] **Step 1: Failing Classpath-Test anlegen**

```java
package de.nordwind.schulungsplaner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class SeedResourcesTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldPackageSchulungenSeedInClasspath() throws IOException {
        assertSeedResource("seed/schulungen.json", "schulungen", 8);
    }

    @Test
    void shouldPackageTrainerSeedInClasspath() throws IOException {
        assertSeedResource("seed/trainer.json", "trainer", 5);
    }

    private void assertSeedResource(String path, String arrayField, int expectedSize) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        assertThat(resource.exists()).isTrue();

        try (InputStream input = resource.getInputStream()) {
            JsonNode root = objectMapper.readTree(input);
            assertThat(root.path(arrayField).isArray()).isTrue();
            assertThat(root.path(arrayField).size()).isEqualTo(expectedSize);
        }
    }
}
```

- [ ] **Step 2: Test ausführen und erwartetes Rot bestätigen**

Run: `cd backend && ./mvnw -q -Dtest=SeedResourcesTest test`

Expected: FAIL, weil `seed/schulungen.json` und `seed/trainer.json` noch nicht im Classpath existieren.

- [ ] **Step 3: Kanonische Seed-Dateien in die Backend-Ressourcen verschieben**

```bash
mkdir -p backend/src/main/resources/seed
git mv schulungen.json backend/src/main/resources/seed/schulungen.json
git mv trainer.json backend/src/main/resources/seed/trainer.json
```

- [ ] **Step 4: `SeedService` auf Classpath-Lesen umstellen**

Die Imports `java.nio.file.Files` und `java.nio.file.Path` entfernen und ergänzen:

```java
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
```

Beide Aufrufe von `readFromRepoRoot` durch `readFromClasspath` ersetzen und die Methode vollständig so definieren:

```java
private <T> T readFromClasspath(String fileName, Class<T> type) {
    ClassPathResource resource = new ClassPathResource("seed/" + fileName);
    try (InputStream input = resource.getInputStream()) {
        return objectMapper.readValue(input, type);
    } catch (IOException ex) {
        throw new IllegalStateException(
                "Seed-Datei konnte nicht aus dem Classpath gelesen werden: " + resource.getPath(),
                ex
        );
    }
}
```

- [ ] **Step 5: Ressourcen- und vorhandenen API-Test grün ausführen**

Run: `./backend/mvnw -f backend/pom.xml test`

Expected: BUILD SUCCESS; `SeedResourcesTest` führt 2 Tests und `SchulungsApiSmokeTest` 1 Test erfolgreich aus. Der Aufruf erfolgt bewusst aus dem Repo-Root.

- [ ] **Step 6: Paketierung verifizieren**

Run: `./backend/mvnw -f backend/pom.xml package && jar tf backend/target/schulungsplaner-backend-0.0.1-SNAPSHOT.jar | rg 'BOOT-INF/classes/seed/(schulungen|trainer).json'`

Expected: beide JSON-Dateien werden unter `BOOT-INF/classes/seed/` ausgegeben.

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/resources/seed backend/src/main/java/de/nordwind/schulungsplaner/service/SeedService.java backend/src/test/java/de/nordwind/schulungsplaner/SeedResourcesTest.java
git commit -m "fix: load seed data from classpath"
```

---

### Task 2: Vollständigen Seed-Graph einschließlich Qualifikationen persistieren

**Files:**
- Create: `backend/src/test/java/de/nordwind/schulungsplaner/SeedDataIntegrationTest.java`
- Modify: `backend/src/main/resources/schema.sql`
- Modify: `backend/src/main/java/de/nordwind/schulungsplaner/service/SeedService.java`

**Interfaces:**
- Consumes: Classpath-Seeds aus Task 1.
- Produces: Tabelle `trainer_qualifikation(trainer_id, schulung_id)` und `SeedService.importTrainerQualifikationen(TrainerSeedRoot)`.

- [ ] **Step 1: Failing Integrationstest für alle Seed-Invarianten schreiben**

```java
package de.nordwind.schulungsplaner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SeedDataIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldImportCompleteSeedGraph() {
        assertCount("schulung", 8);
        assertCount("termin", 14);
        assertCount("voraussetzung", 8);
        assertCount("trainer", 5);
        assertCount("trainer_qualifikation", 10);
        assertCount("abwesenheit", 5);
    }

    private void assertCount(String table, int expected) {
        Integer actual = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
        assertThat(actual).isEqualTo(expected);
    }
}
```

- [ ] **Step 2: Test ausführen und erwartetes Rot bestätigen**

Run: `cd backend && ./mvnw -q -Dtest=SeedDataIntegrationTest test`

Expected: FAIL mit fehlender Tabelle `TRAINER_QUALIFIKATION`.

- [ ] **Step 3: Zuordnungstabelle nach `schulung` und vor `voraussetzung` anlegen**

```sql
CREATE TABLE IF NOT EXISTS trainer_qualifikation (
    trainer_id VARCHAR(50) NOT NULL,
    schulung_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (trainer_id, schulung_id),
    CONSTRAINT fk_qualifikation_trainer FOREIGN KEY (trainer_id) REFERENCES trainer(id),
    CONSTRAINT fk_qualifikation_schulung FOREIGN KEY (schulung_id) REFERENCES schulung(id)
);
```

- [ ] **Step 4: Importreihenfolge explizit machen**

`resetAndSeed()` vollständig ersetzen:

```java
public void resetAndSeed() {
    TrainerSeedRoot trainerSeed = readFromClasspath("trainer.json", TrainerSeedRoot.class);
    SchulungenSeedRoot schulungenSeed = readFromClasspath("schulungen.json", SchulungenSeedRoot.class);

    clearData();
    importTrainer(trainerSeed);
    importSchulungen(schulungenSeed);
    importTrainerQualifikationen(trainerSeed);
}
```

`clearData()` mit der Zuordnungstabelle beginnen lassen:

```java
private void clearData() {
    jdbcTemplate.update("DELETE FROM trainer_qualifikation");
    jdbcTemplate.update("DELETE FROM termin");
    jdbcTemplate.update("DELETE FROM voraussetzung");
    jdbcTemplate.update("DELETE FROM abwesenheit");
    jdbcTemplate.update("DELETE FROM schulung");
    jdbcTemplate.update("DELETE FROM trainer");
}
```

Die vorhandenen Methoden so ändern, dass sie die bereits gelesenen Roots erhalten:

```java
private void importTrainer(TrainerSeedRoot seedRoot) {
    if (seedRoot == null || seedRoot.trainer() == null) {
        throw new IllegalStateException("Trainer-Seed enthält keine Trainerliste");
    }
    for (Trainer trainer : seedRoot.trainer()) {
        jdbcTemplate.update(
                "INSERT INTO trainer (id, name, email) VALUES (?, ?, ?)",
                trainer.id(), trainer.name(), trainer.email()
        );
        List<Abwesenheit> abwesenheiten = trainer.abwesenheiten();
        if (abwesenheiten == null) {
            continue;
        }
        for (Abwesenheit abwesenheit : abwesenheiten) {
            jdbcTemplate.update(
                    "INSERT INTO abwesenheit (trainer_id, von, bis, grund) VALUES (?, ?, ?, ?)",
                    trainer.id(),
                    LocalDate.parse(abwesenheit.von()),
                    LocalDate.parse(abwesenheit.bis()),
                    abwesenheit.grund()
            );
        }
    }
}

private void importSchulungen(SchulungenSeedRoot seedRoot) {
    if (seedRoot == null || seedRoot.schulungen() == null) {
        throw new IllegalStateException("Schulungs-Seed enthält keine Schulungsliste");
    }
    for (Schulung schulung : seedRoot.schulungen()) {
        jdbcTemplate.update(
                "INSERT INTO schulung (id, titel, kategorie, kurzbeschreibung, dauer_in_tagen, mindestteilnehmer_exklusiv, max_teilnehmer_oeffentlich) VALUES (?, ?, ?, ?, ?, ?, ?)",
                schulung.id(),
                schulung.titel(),
                schulung.kategorie(),
                schulung.kurzbeschreibung(),
                schulung.dauerInTagen(),
                schulung.mindestteilnehmerExklusiv(),
                schulung.maxTeilnehmerOeffentlich()
        );

        if (schulung.voraussetzungen() != null) {
            for (String voraussetzung : schulung.voraussetzungen()) {
                jdbcTemplate.update(
                        "INSERT INTO voraussetzung (schulung_id, text) VALUES (?, ?)",
                        schulung.id(), voraussetzung
                );
            }
        }

        if (schulung.oeffentlicheTermine() != null) {
            for (Termin termin : schulung.oeffentlicheTermine()) {
                jdbcTemplate.update(
                        "INSERT INTO termin (termin_id, schulung_id, startdatum, enddatum, ort, format, status, trainer_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        termin.terminId(),
                        schulung.id(),
                        LocalDate.parse(termin.startdatum()),
                        LocalDate.parse(termin.enddatum()),
                        termin.ort(),
                        termin.format(),
                        termin.status(),
                        termin.trainerId()
                );
            }
        }
    }
}
```

- [ ] **Step 5: Qualifikationen nach Trainer- und Schulungsimport persistieren**

```java
private void importTrainerQualifikationen(TrainerSeedRoot seedRoot) {
    for (Trainer trainer : seedRoot.trainer()) {
        List<String> qualifikationen = trainer.qualifikationen();
        if (qualifikationen == null) {
            continue;
        }
        for (String schulungId : qualifikationen) {
            jdbcTemplate.update(
                    "INSERT INTO trainer_qualifikation (trainer_id, schulung_id) VALUES (?, ?)",
                    trainer.id(),
                    schulungId
            );
        }
    }
}
```

- [ ] **Step 6: Integrationstest und Gesamtsuite grün ausführen**

Run: `cd backend && ./mvnw test`

Expected: BUILD SUCCESS; alle Seed-Zählungen entsprechen den Global Constraints.

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/resources/schema.sql backend/src/main/java/de/nordwind/schulungsplaner/service/SeedService.java backend/src/test/java/de/nordwind/schulungsplaner/SeedDataIntegrationTest.java
git commit -m "fix: persist trainer qualifications from seed"
```

---

### Task 3: Playwright selbstständig lauffähig machen

**Files:**
- Modify: `frontend/playwright.config.ts`
- Modify: `frontend/tests/e2e/catalog-smoke.spec.ts`

**Interfaces:**
- Consumes: die für den Workshop festgelegten Ports `18081` und `15173`.
- Produces: `npm run test:e2e` startet Backend und Frontend ohne manuelle Vorarbeit.

- [ ] **Step 1: Bekannten Standalone-Fehler reproduzieren**

Backend und Frontend müssen gestoppt sein.

Run: `cd frontend && npm run test:e2e`

Expected: FAIL mit `ECONNREFUSED` auf Port `18081`, weil die bisherige Konfiguration nur Vite startet.

- [ ] **Step 2: Playwright beide Anwendungen verwalten lassen**

`frontend/playwright.config.ts` vollständig ersetzen:

```ts
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { defineConfig } from '@playwright/test'

const frontendDir = path.dirname(fileURLToPath(import.meta.url))
const backendDir = path.resolve(frontendDir, '../backend')
const mavenCommand =
  process.platform === 'win32'
    ? 'mvnw.cmd spring-boot:run'
    : './mvnw spring-boot:run'

export default defineConfig({
  testDir: './tests/e2e',
  fullyParallel: false,
  use: {
    baseURL: 'http://localhost:15173',
    trace: 'on-first-retry',
  },
  webServer: [
    {
      name: 'backend',
      command: mavenCommand,
      cwd: backendDir,
      url: 'http://localhost:18081/api/health',
      reuseExistingServer: !process.env.CI,
      timeout: 120000,
      gracefulShutdown: { signal: 'SIGTERM', timeout: 5000 },
    },
    {
      name: 'frontend',
      command: 'npm run dev',
      cwd: frontendDir,
      url: 'http://localhost:15173',
      reuseExistingServer: !process.env.CI,
      timeout: 120000,
      gracefulShutdown: { signal: 'SIGTERM', timeout: 5000 },
    },
  ],
})
```

- [ ] **Step 3: E2E-Test über den realen Frontend-Proxy ausführen**

`catalog-smoke.spec.ts` vollständig ersetzen:

```ts
import { test, expect } from '@playwright/test'

test('catalog is visible and API returns 200', async ({ page, request }) => {
  const apiResponse = await request.get('/api/schulungen')
  expect(apiResponse.status()).toBe(200)

  await page.goto('/')
  await expect(page.getByRole('heading', { name: 'Schulungskatalog' })).toBeVisible()
  await expect(page.locator('.card').first()).toBeVisible()
})
```

- [ ] **Step 4: Standalone-E2E-Test verifizieren**

Run: `cd frontend && npm run test:e2e`

Expected: 1 passed; vorher laufende Server sind nicht erforderlich.

- [ ] **Step 5: Frontend-Suite erneut ausführen**

Run: `cd frontend && npm run test && npm run build`

Expected: 1 Vitest-Test passed und Vite-Build erfolgreich.

- [ ] **Step 6: Commit**

```bash
git add frontend/playwright.config.ts frontend/tests/e2e/catalog-smoke.spec.ts
git commit -m "fix: make e2e test self-contained"
```

---

### Task 4: Minimalen, verlässlichen Copilot-Kontext bereitstellen

**Files:**
- Create: `.github/copilot-instructions.md`

**Interfaces:**
- Consumes: den nach Tasks 1–3 tatsächlich verifizierten technischen Zustand.
- Produces: repositoryweite Regeln, die Copilot automatisch als Kontext verwenden kann und die im Workshop gezielt erweitert werden.

- [ ] **Step 1: Repositoryweite Copilot-Instructions anlegen**

````markdown
# Nordwind Academy Schulungsplaner

## Projektziel

Dieses Repository ist ein vorbereitetes Übungsprojekt für KI-gestützte Softwareentwicklung. Die vorhandene Katalogansicht ist die stabile Ausgangsbasis; neue Funktionen werden als kleine vertikale Features ergänzt.

## Technische Leitplanken

- Backend: Java 21, Spring Boot, Spring JDBC und H2 In-Memory.
- Frontend: Vue 3 mit TypeScript, Composition API und Vite.
- Kommunikation: REST mit JSON unter `/api`.
- Seed-Daten liegen unter `backend/src/main/resources/seed`.
- UI-Texte und fachliche Bezeichner bleiben deutsch.
- Ersetze den vorhandenen Stack nicht und füge Abhängigkeiten nur mit nachvollziehbarer Begründung hinzu.
- Authentifizierung, Cloud-Dienste und externe Datenbanken gehören nicht zum Workshopumfang.

## Architekturgrenzen

- Controller behandeln HTTP-Belange.
- Services enthalten Geschäftslogik und Datenzugriff.
- Vue-Komponenten rufen API-Funktionen aus `frontend/src/api.ts` auf; direkte `fetch`-Aufrufe gehören nicht in Templates.
- Kleine, fokussierte Dateien und Funktionen sind größeren Sammelkomponenten vorzuziehen.

## Verifikation

- Backend: `cd backend && ./mvnw test`
- Frontend: `cd frontend && npm run test && npm run build`
- Ende-zu-Ende: `cd frontend && npm run test:e2e`
- Neue Geschäftsregeln benötigen fokussierte Tests für Happy Path, Grenzen und Fehlerfälle.

## Zusammenarbeit mit Copilot

- Nenne Annahmen und offene fachliche Fragen, bevor du einen Implementierungsplan erstellst.
- Plane vor der Implementierung und warte auf Freigabe, wenn der Auftrag nur Analyse oder Planung verlangt.
- Ändere nur Dateien, die für das aktuelle Feature erforderlich sind.
- Prüfe den abschließenden Diff und führe die relevanten Verifikationsbefehle aus.

## Workshop-Erweiterungen

Das Team ergänzt hier nur Regeln, die aus einem konkret beobachteten Problem abgeleitet, gemeinsam verstanden und anschließend überprüft wurden.
```

- [ ] **Step 2: Dateipfad und Kernregeln verifizieren**

Run: `test -f .github/copilot-instructions.md && rg -n "Java 21|Vue 3|Plane vor der Implementierung|npm run test:e2e" .github/copilot-instructions.md`

Expected: vier Treffergruppen; die Datei liegt exakt unter `.github/copilot-instructions.md`.

- [ ] **Step 3: Commit**

```bash
git add .github/copilot-instructions.md
git commit -m "docs: add minimal copilot project instructions"
```

---

### Task 5: Einstieg und Setting auf den vorbereiteten Ist-Zustand ausrichten

**Files:**
- Create: `README.md`
- Delete: `RUNBOOK.md`
- Modify: `docs/README.md`
- Modify: `docs/setup.md`
- Modify: `docs/runbook.md`
- Modify: `docs/testing.md`
- Modify: `docs/architecture-overview.md`
- Modify: `setting-schulungsplaner.md`

**Interfaces:**
- Consumes: den selbstständigen E2E-Aufruf aus Task 3 und den Copilot-Kontext aus Task 4.
- Produces: einen widerspruchsfreien Einstieg mit genau einer Runbook-Quelle.

- [ ] **Step 1: Root-README als einzigen Einstiegspunkt anlegen**

```markdown
# Nordwind Academy Schulungsplaner

Vorbereitetes Monorepo für einen Workshop zu KI-gestützter Softwareentwicklung mit GitHub Copilot.

## Voraussetzungen

- Java 21
- Node.js 22 LTS und npm
- Visual Studio Code mit GitHub Copilot

## Einmalige Installation

```bash
cd frontend
npm ci
npx playwright install chromium
```

## Lokal starten

Terminal 1:

```bash
cd backend
./mvnw spring-boot:run
```

Terminal 2:

```bash
cd frontend
npm run dev
```

Frontend: http://localhost:15173  
Backend: http://localhost:18081

## Verifizieren

```bash
cd backend
./mvnw test
```

```bash
cd frontend
npm run test
npm run build
npm run test:e2e
```

Der E2E-Befehl startet Backend und Frontend bei Bedarf selbst.

## Dokumentation

- [Setting](setting-schulungsplaner.md)
- [Setup](docs/setup.md)
- [Runbook](docs/runbook.md)
- [Architektur](docs/architecture-overview.md)
- [Testing](docs/testing.md)
- [Workshop-Arbeitszyklus](docs/workshop/arbeitszyklus.md)
- [Feature-Briefings](docs/feature-briefings)
````

- [ ] **Step 2: Doppeltes Root-Runbook entfernen**

Run: `git rm RUNBOOK.md`

Expected: Das benutzerspezifische Runbook ist gelöscht; `docs/runbook.md` bleibt die einzige Runbook-Quelle.

- [ ] **Step 3: Technische Dokumente exakt anpassen**

In `docs/setup.md`:

- „Maven Wrapper (im spaeteren backend/ erwartet)“ durch „Maven Wrapper ist unter `backend/mvnw` enthalten“ ersetzen.
- `npm install` durch `npm ci` ersetzen.
- Den E2E-Befehl als selbstständig startend beschreiben.
- Die festen Workshopports `18081` und `15173` nennen und darauf hinweisen, dass beide vor dem Start frei sein müssen. Keine abweichende Konfiguration versprechen.

In `docs/testing.md` die drei verifizierten Befehle und ihre Erwartungen dokumentieren; beim E2E-Test ausdrücklich festhalten, dass Playwright beide Anwendungen startet.

In `docs/runbook.md` nur relative Befehle verwenden und folgende Troubleshooting-Reihenfolge festhalten: Java/Node-Version, belegte Ports, Seed-Import, Backend-Health, Frontend-Proxy, Playwright-Trace.

In `docs/architecture-overview.md` die Seed-Pfade auf `backend/src/main/resources/seed` ändern und `.github/copilot-instructions.md` als tatsächlich vorhandenen Projektkontext beschreiben.

In `docs/README.md` den Link auf das gelöschte Root-Runbook entfernen und Links zu Workshop-Arbeitszyklus, Model Lab und Feature-Briefings ergänzen.

- [ ] **Step 4: Widersprüche im Setting entfernen**

In Abschnitt 1 den offenen Technikabsatz durch diesen Text ersetzen:

```markdown
Das Repository enthält bereits eine lauffähige Referenzarchitektur mit Spring Boot, Spring JDBC, H2, Vue 3 und REST. Diese Architektur ist für den Workshop verbindlich. Die Teilnehmenden analysieren und erweitern sie, statt ein neues Grundgerüst oder eine alternative Persistenz aufzusetzen.
```

Abschnitt 7 so abgrenzen:

```markdown
- Fachliche Rollen dienen nur zur Einordnung der Anforderungen; ein technischer Login und eine Rechteprüfung sind kein Bestandteil des Workshops.
- Responsive Bedienbarkeit ist wünschenswert, aber kein Abnahmekriterium.
- Deutsch ist die einzige benötigte Sprache.
- Cloud-Betrieb, besondere Last und externe Persistenz sind nicht erforderlich.
- Setup, Start und Tests müssen auf einer frischen Workshopmaschine reproduzierbar sein.
```

Abschnitt 9 vollständig ersetzen:

```markdown
## 9. Workshopablauf im Repository

1. Vorhandenes System mit Copilot untersuchen und Aussagen am Code verifizieren.
2. Katalogsuche und Kategorie-Filter als geführten KI-Arbeitszyklus umsetzen.
3. Trainerverfügbarkeit mit bewusstem Kontext- und Modellvergleich planen und implementieren.
4. Abwesenheitspflege als zunehmend selbstständiges Agentenfeature bearbeiten.
5. Features mit Tests, Playwright MCP, Diff-Review und Git absichern.
6. Pro Feature festhalten, welche KI-Vorschläge übernommen, korrigiert oder verworfen wurden.
```

Abschnitt 10 in „Vorbereitete Ausgangsbasis“ umbenennen und eindeutig als bereits implementiert beschreiben.

- [ ] **Step 5: Veraltete und benutzerspezifische Aussagen suchen**

Run: `rg -n "/Users/philippe|im spaeteren backend|Alles andere.*bewusst offen|Grundgeruest im Monorepo aufsetzen|MVP Iteration 1 umsetzen" README.md docs setting-schulungsplaner.md`

Expected: keine Treffer.

- [ ] **Step 6: Links prüfen**

Run: `rg -n "RUNBOOK.md|schulungen.json|trainer.json" README.md docs setting-schulungsplaner.md`

Expected: kein Link auf das gelöschte Root-Runbook; Seed-Pfade zeigen auf `backend/src/main/resources/seed`.

- [ ] **Step 7: Commit**

```bash
git add README.md docs/README.md docs/setup.md docs/runbook.md docs/testing.md docs/architecture-overview.md setting-schulungsplaner.md
git commit -m "docs: align onboarding with prepared workshop baseline"
```

---

### Task 6: Konkrete Workshop-Briefings und Reflexionsartefakte ergänzen

**Files:**
- Create: `docs/workshop/arbeitszyklus.md`
- Move: `docs/feature-briefings/mvp-katalog-read.md` → `docs/feature-briefings/00-ausgangsbasis.md`
- Create: `docs/feature-briefings/01-katalog-suche-filter.md`
- Create: `docs/feature-briefings/02-trainer-verfuegbarkeit.md`
- Create: `docs/feature-briefings/03-abwesenheit-erfassen.md`
- Create: `docs/exercises/model-lab.md`
- Create: `docs/templates/feature-learning-log.md`
- Modify: `docs/README.md`

**Interfaces:**
- Consumes: vorbereitete Anwendung und Copilot-Instructions.
- Produces: drei zunehmend selbstständige Featurezyklen, ein gruppenweites Model Lab und einen einheitlichen Reflexionsnachweis.

- [ ] **Step 1: Gemeinsamen Arbeitszyklus dokumentieren**

`docs/workshop/arbeitszyklus.md` muss exakt diese Gates enthalten:

```markdown
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
```

- [ ] **Step 2: Ausgangsbasis korrekt umbenennen**

```bash
git mv docs/feature-briefings/mvp-katalog-read.md docs/feature-briefings/00-ausgangsbasis.md
```

Die Datei vollständig auf diesen Inhalt reduzieren:

```markdown
# Vorbereitete Ausgangsbasis: Katalog Read

## Bereits implementiert

- Seed-Daten werden beim Backend-Start aus dem Classpath geladen.
- `GET /api/schulungen` liefert den Schulungskatalog.
- Das Frontend zeigt eine einfache Schulungsliste.
- Backend-, Frontend- und Playwright-Smoke-Tests sind vorhanden.

## Startverifikation

1. `cd backend && ./mvnw test` ausführen.
2. `cd frontend && npm run test && npm run build` ausführen.
3. `cd frontend && npm run test:e2e` ausführen.
4. Prüfen, dass alle Befehle grün sind und `git status --short` leer bleibt.

## Fachlicher Ausgangspunkt

Kalenderinteraktion, Trainer-Matching und Pflege von Abwesenheiten sind noch nicht implementiert. Sie werden ausschließlich über die nummerierten Feature-Briefings erweitert.
```

- [ ] **Step 3: Geführtes Feature-Briefing für Suche und Filter anlegen**

```markdown
# Feature 1: Katalog durchsuchen und filtern

## Lernfokus

Prompting-Grundlagen, Plan vor Implementierung, Git-Diff und erster Feature-Commit.

## Fachlicher Auftrag

Planer können Schulungen über einen Teil des Titels suchen und nach Kategorie filtern.

## Akzeptanzkriterien

- Die Titelsuche ignoriert Groß-/Kleinschreibung.
- Führende und nachfolgende Leerzeichen werden ignoriert.
- Suche und Kategorie lassen sich kombinieren.
- Beide Filter lassen sich gemeinsam zurücksetzen.
- Ohne Treffer erscheint ein verständlicher Leerzustand.
- Ohne aktive Filter bleibt die vorhandene vollständige Liste sichtbar.

## KI-Aufgabe

Copilot erhält zunächst ohne Schreibrechte nur den Satz „Baue Suche und Filter für Schulungen ein“. Das Team markiert fehlenden Kontext, Annahmen und unklare Prüfkriterien. Anschließend formuliert es einen zweiten Auftrag mit Ziel, Kontext, Grenzen und Definition of Done und lässt ausschließlich einen Plan erzeugen.

## Verifikation

- Vitest deckt kombinierte Filterung, Leerzeichen und Leerzustand ab.
- Playwright deckt mindestens einen sichtbaren Such- und Reset-Ablauf ab.
- Das Team prüft den vollständigen Diff vor dem Commit.
```

- [ ] **Step 4: Trainer-Verfügbarkeits-Briefing anlegen**

```markdown
# Feature 2: Geeignete und verfügbare Trainer ermitteln

## Lernfokus

Kontextmanagement, Model Lab, Effort-Wahl, Geschäftsregeln und Grenzfalltests.

## Fachlicher Auftrag

Für eine Schulung und einen gewünschten Zeitraum werden fachlich qualifizierte und verfügbare Trainer angezeigt.

## Akzeptanzkriterien

- Die Schulungs-ID ist in den Qualifikationen des Trainers enthalten.
- Abwesenheitszeiträume gelten einschließlich beider Grenztage.
- Ein Anfangsdatum nach dem Enddatum wird als ungültige Anfrage abgelehnt.
- Treffer werden alphabetisch nach Trainername sortiert.
- Für keine verfügbaren Trainer erscheint ein eigener UI-Zustand.
- Eine Abwesenheit, die genau am ersten Schulungstag beginnt, macht den Trainer nicht verfügbar.

## Kontext-Gate

Vor dem Plan dokumentiert das Team höchstens fünf notwendige Dateien und mindestens zwei bewusst ausgeschlossene Dateien. Copilot muss offene fachliche Fragen nennen, bevor es einen Implementierungsplan erzeugt.

## Verifikation

- Backendtests decken vollständig getrennte, teilweise überlappende, vollständig enthaltene und exakt angrenzende Datumsintervalle ab.
- Frontendtest deckt Treffer-, Leer- und Fehlerzustand ab.
- Das Model Lab wird vor der Implementierung durchgeführt.
```

- [ ] **Step 5: Abwesenheits-Briefing anlegen**

```markdown
# Feature 3: Abwesenheit erfassen

## Lernfokus

Selbstständige Agentennutzung, Erweiterung der Copilot-Instructions, Playwright MCP und Cross-Team-Review.

## Fachlicher Auftrag

Trainer können einen Abwesenheitszeitraum mit optionalem Grund erfassen.

## Akzeptanzkriterien

- Von- und Bis-Datum sind Pflichtfelder.
- Das Bis-Datum darf nicht vor dem Von-Datum liegen.
- Überlappende Abwesenheiten werden abgelehnt.
- Backendfehler werden verständlich im Frontend dargestellt.
- Nach erfolgreicher Speicherung wird die Abwesenheitsliste aktualisiert.

## KI-Gates

- Das Team organisiert alle neun Gates des Arbeitszyklus selbst.
- Genau eine wiederholt benötigte Regel wird aus einem beobachteten Problem in `.github/copilot-instructions.md` übernommen.
- Copilot prüft das laufende Feature mit Playwright MCP anhand der Akzeptanzkriterien.
- Ein relevanter MCP-Fund wird als dauerhafter Playwright-Test umgesetzt.
- Ein anderes Team reviewt den Pull Request und verlangt für jede Beanstandung einen Code-, Test- oder Verhaltensnachweis.
```

- [ ] **Step 6: Gemeinsames Model Lab dokumentieren**

```markdown
# Model Lab: gleiche Aufgabe, unterschiedliche Konfigurationen

## Dauer

30 Minuten vor Feature 2.

## Versuchsaufbau

Alle Teilnehmenden erhalten denselben Prompt, dieselben Kontextdateien, ein neues Chatfenster und nur lesenden Zugriff. Jede Person verwendet eine zugewiesene Kombination aus verfügbarem Modell und Effort-Level.

## Einheitlicher Prompt

> Entwickle einen strukturierten Testentwurf für die Ermittlung verfügbarer Trainer. Berücksichtige Qualifikation, Datumsüberschneidungen, ungültige Zeiträume und fehlende Treffer. Nenne Eingaben und erwartete Ergebnisse.

## Blinde Auswertung

Die anonymisierten Antworten werden gemeinsam anhand folgender Kriterien bewertet:

- fachliche Korrektheit
- Grenzfallabdeckung
- nachvollziehbare Struktur
- unnötige Annahmen
- Verständlichkeit
- erwarteter Nachbearbeitungsaufwand
- Antwortdauer

Erst danach werden Modell und Effort offengelegt. Jede Featuregruppe begründet anschließend in einem Satz, welche Konfiguration sie für Planung und Testentwurf verwendet. Ein einzelner Durchlauf gilt nicht als allgemeines Modellranking.
```

- [ ] **Step 7: Learning-Log-Template anlegen**

```markdown
# Feature Learning Log

## Aufgabe und Rollen

- Feature:
- AI Driver:
- Context Curator:
- Reviewer/QA:

## Kontextentscheidung

- Verwendete Dateien:
- Bewusst ausgeschlossene Dateien:
- Wichtigste offene Frage vor der Planung:

## KI-Entscheidungen

- Übernommener Vorschlag und Begründung:
- Korrigierter oder verworfener Vorschlag und Begründung:
- Verwendetes Modell/Effort und Begründung:

## Verifikation

- Ausgeführte Tests:
- Manuell oder mit MCP geprüfter Ablauf:
- Ergebnis des Diff-Reviews:

## Reflexion

- Was hat Copilot beschleunigt?
- Wo war menschliches Fachwissen entscheidend?
```

- [ ] **Step 8: Dokumentationsindex aktualisieren und Links prüfen**

In `docs/README.md` Links auf alle sieben neuen bzw. umbenannten Workshopdateien aufnehmen.

Run: `find docs -name '*.md' -print0 | xargs -0 rg -n "mvp-katalog-read.md"`

Expected: keine Treffer auf den alten Namen.

- [ ] **Step 9: Commit**

```bash
git add docs/README.md docs/workshop docs/feature-briefings docs/exercises docs/templates
git commit -m "docs: add progressive copilot workshop exercises"
```

---

### Task 7: Generierte Playwright-Artefakte aus Git entfernen

**Files:**
- Modify: `frontend/.gitignore`
- Delete: `frontend/test-results/.last-run.json`

**Interfaces:**
- Consumes: die von Playwright erzeugten Ergebnisverzeichnisse.
- Produces: sauberer Git-Status nach Testläufen.

- [ ] **Step 1: Playwright-Ausgaben ignorieren und versioniertes Ergebnis entfernen**

An `frontend/.gitignore` ergänzen:

```gitignore

# Playwright output
test-results/
playwright-report/
blob-report/
```

Run: `git rm frontend/test-results/.last-run.json`

- [ ] **Step 2: Tests erzeugen keinen schmutzigen Git-Status**

Run: `cd frontend && npm run test && npm run build && npm run test:e2e`

Expected: alle Befehle erfolgreich.

Run: `git status --short`

Expected: nur die bewusst vorgenommenen Task-7-Änderungen; keine Dateien unter `frontend/test-results` oder `frontend/playwright-report`.

- [ ] **Step 3: Commit**

```bash
git add frontend
git commit -m "chore: ignore generated playwright output"
```

---

### Task 8: Pilotcheck dokumentieren und vollständige Regression ausführen

**Files:**
- Create: `docs/pilot-checklist.md`
- Modify: `docs/README.md`

**Interfaces:**
- Consumes: alle vorherigen Tasks.
- Produces: wiederholbarer Abnahmeprozess vor jeder Workshopkohorte.

- [ ] **Step 1: Pilotcheckliste anlegen**

```markdown
# Pilotcheck vor einer Workshopkohorte

## Frische Umgebung

- Repository in einen neuen Ordner klonen.
- Repo-Root in VS Code öffnen.
- Java 21 und Node.js 22 bestätigen.
- Im Frontend `npm ci` und einmalig `npx playwright install chromium` ausführen.
- Prüfen, dass die festen Ports `18081` und `15173` frei sind.

## Technische Verifikation

- Backendtests laufen grün.
- Frontendtests und Produktionsbuild laufen grün.
- Playwright startet beide Anwendungen selbst und läuft grün.
- Nach allen Tests ist `git status --short` leer.
- Die API liefert 8 Schulungen; die Datenbank enthält 5 Trainer und 10 Qualifikationen.

## Copilot-Verifikation

- Copilot referenziert `.github/copilot-instructions.md`.
- Ein Planungsauftrag erzeugt keine Änderungen, bevor die Umsetzung freigegeben wird.
- Playwright MCP ist im Agent-Modus sichtbar und kann die lokale Katalogseite öffnen.

## Didaktische Verifikation

- Alle drei Feature-Briefings sind aus dem Dokumentationsindex erreichbar.
- Hilfestufen und Rollenwechsel sind für fünf bis sechs Personen vorbereitet.
- Das Model Lab kann mit den am Schulungstag verfügbaren Modellen und Effort-Leveln durchgeführt werden.
- Für jedes Feature steht ein frisches Learning Log bereit.
```

- [ ] **Step 2: Pilotcheck im Dokumentationsindex verlinken**

In `docs/README.md` den Link `- [pilot-checklist.md](pilot-checklist.md): technischer und didaktischer Check vor jeder Kohorte` ergänzen.

- [ ] **Step 3: Saubere Installation und vollständige Suite ausführen**

Run: `cd frontend && npm ci`

Expected: Installation erfolgreich und `npm audit` meldet keine bekannten Schwachstellen.

Run: `cd backend && ./mvnw test`

Expected: BUILD SUCCESS; Ressourcen-, Seed- und API-Tests laufen grün.

Run: `cd frontend && npm run test && npm run build && npm run test:e2e`

Expected: Vitest, Typecheck, Vite-Build und Playwright laufen grün.

- [ ] **Step 4: Dokumentations- und Hygienechecks ausführen**

Run: `rg -n '/Users/philippe|TO''DO|TB''D|mvp-katalog-read.md|im spaeteren backend' README.md docs setting-schulungsplaner.md .github`

Expected: keine Treffer.

Run: `git ls-files | rg "test-results|playwright-report"`

Expected: keine Treffer.

Run: `git status --short`

Expected: nur `docs/pilot-checklist.md` und `docs/README.md` sind geändert.

- [ ] **Step 5: Commit**

```bash
git add docs/pilot-checklist.md docs/README.md
git commit -m "docs: add workshop pilot readiness checklist"
```

- [ ] **Step 6: Abschließenden Repositoryzustand bestätigen**

Run: `git status --short --branch`

Expected: sauberer Arbeitsbaum auf dem Arbeitsbranch.

Run: `git log -8 --oneline`

Expected: je ein nachvollziehbarer Commit für Seed-Ressourcen, Qualifikationen, E2E, Copilot-Instructions, Onboarding, Workshopübungen, Hygiene und Pilotcheck.
