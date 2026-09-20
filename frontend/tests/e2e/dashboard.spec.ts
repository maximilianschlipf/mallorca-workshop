import { expect, test, type Page } from "@playwright/test";
import { eigentuemerAnmelden, eigentuemerPasswort } from "./anmeldung";

async function fixture(page: Page, name = "dashboard-pflichten") {
  return page.evaluate(async (fixtureName) => {
    const { token } = await fetch("/api/auth/csrf").then((antwort) => antwort.json());
    return (await fetch(`/api/e2e/${fixtureName}`, {
      method: "PUT", headers: { "X-XSRF-TOKEN": token },
    })).status;
  }, name);
}

// verifies: TEST_DSH_PFLI_01
test("nur der eigene überfällige geplante Termin wird zur Handlungspflicht", async ({ page }) => {
  await eigentuemerAnmelden(page);
  expect(await fixture(page)).toBe(204);
  await page.goto("/");

  const pflichten = page.getByRole("region", { name: "Handlungspflichten" });
  await expect(pflichten.locator(".task-list li")).toHaveCount(1);

  await pflichten.getByRole("link", { name: "Termin öffnen" }).click();
  await expect(page).toHaveURL(/termin=E2E-DSH-PFLI-UEBERFAELLIG/);
  const dialog = page.getByRole("dialog", { name: "Scrum Master Zertifizierung" });
  await expect(dialog).toBeVisible();
  page.once("dialog", bestaetigung => bestaetigung.accept());
  await dialog.getByRole("button", { name: "Durchführung bestätigen" }).click();
  await expect(page.getByRole("status")).toContainText("Durchführung bestätigt");
  await page.goto("/");
  await expect(pflichten).toContainText("Aktuell ist keine eigene Handlung erforderlich");
});

// verifies: TEST_DSH_SPRUNG_01
test("Termin öffnen zeigt aus Pflichten und Dringlichkeiten den Terminmonat und Details", async ({ page }) => {
  await page.clock.setFixedTime(new Date("2026-07-17T12:00:00"));
  await eigentuemerAnmelden(page);

  for (const [fixtureName, rang, terminId, datum] of [
    ["dashboard-pflichten", "Handlungspflichten", "E2E-DSH-PFLI-UEBERFAELLIG", "16.09.2026"],
    ["dashboard-historie", "Dringlichkeiten", "E2E-HIST-DRINGEND", "25.09.2026"],
  ]) {
    expect(await fixture(page, fixtureName)).toBe(204);
    await page.goto("/");
    await page.getByRole("region", { name: rang })
      .locator(`a[href*="${terminId}"]`).click();
    await expect(page).toHaveURL(new RegExp(`termin=${terminId}`));
    await expect(page.getByRole("heading", { name: "September 2026" })).toBeVisible();
    await expect(page.getByRole("dialog", { name: "Scrum Master Zertifizierung" }))
      .toContainText(datum);
  }
});

// verifies: TEST_DSH_VORG_03
test("eigene Vorgänge sind getrennt sichtbar und soweit erlaubt zurückziehbar", async ({ page }) => {
  await eigentuemerAnmelden(page);
  expect(await fixture(page, "dashboard-vorgaenge")).toBe(204);
  const nachrichtenVorher = await page.evaluate(() =>
    fetch("/api/ich/benachrichtigungen").then(antwort => antwort.json()).then(liste => liste.length));
  await page.goto("/");

  const eigene = page.getByRole("region", { name: "Worauf ich warte" });
  await expect(eigene.locator("li")).toHaveCount(6);
  for (const art of ["Freigabeanfrage", "Vormerkung", "Assistenzplatz",
    "Abwesenheitsantrag", "Übernahmeanfrage", "Ersatztrainer-Anfrage"]) {
    await expect(eigene.getByText(new RegExp(art)).first()).toBeVisible();
  }

  for (let i = 0; i < 5; i += 1) {
    await eigene.getByRole("button", { name: "Zurückziehen" }).first().click();
    await expect(page.getByRole("status")).toBeFocused();
  }
  await expect(eigene.locator("li")).toHaveCount(1);
  await expect(eigene).toContainText("Ersatztrainer-Anfrage");
  await expect(eigene.getByRole("button", { name: "Zurückziehen" })).toHaveCount(0);

  const erledigt = page.locator("details.completed-processes");
  await erledigt.locator("summary").click();
  await expect(erledigt.getByRole("region", { name: "Von mir gestellt" })
    .getByText("ZURUECKGEZOGEN")).toHaveCount(5);
  await page.reload();
  await expect(page.locator("details.completed-processes")).not.toHaveAttribute("open", "");
  const nachrichtenNachher = await page.evaluate(() =>
    fetch("/api/ich/benachrichtigungen").then(antwort => antwort.json()).then(liste => liste.length));
  expect(nachrichtenNachher).toBe(nachrichtenVorher);
});

// verifies: TEST_DSH_VORG_06, TEST_DSH_GRUND_03
test("erledigte Vorgänge bleiben getrennt und standardmäßig zugeklappt erhalten", async ({ page }) => {
  await eigentuemerAnmelden(page);
  expect(await fixture(page, "dashboard-historie")).toBe(204);
  await page.goto("/");

  await expect(page.getByRole("region", { name: "Vorgänge" })).toBeVisible();
  expect(await page.locator(".dashboard-rank h2").allTextContents())
    .toEqual(["Vorgänge", "Handlungspflichten", "Dringlichkeiten", "Worauf ich warte"]);
  const dringlichkeit = page.getByRole("region", { name: "Dringlichkeiten" })
    .getByRole("listitem").filter({ hasText: "2026-09-25 bis 2026-09-25" });
  await expect(dringlichkeit).toBeVisible();
  await expect(dringlichkeit).toHaveClass(/urgent/);
  await expect(dringlichkeit.getByText("Dringend")).toBeVisible();

  const anMich = page.getByRole("region", { name: "Vorgänge" });
  const annehmen = anMich.getByRole("listitem").filter({ hasText: "Übernahmeanfrage" })
    .getByRole("button", { name: "Annehmen" });
  await annehmen.focus();
  await page.keyboard.press("Enter");
  await expect(page.getByRole("status")).toBeFocused();
  const ablehnung = anMich.getByRole("listitem").filter({ hasText: "Ersatztrainer-Anfrage" });
  await ablehnung.getByRole("button", { name: "Ablehnen" }).focus();
  await page.keyboard.press("Enter");
  await ablehnung.getByRole("button", { name: "Ablehnung bestätigen" }).focus();
  await page.keyboard.press("Enter");
  await expect(page.getByRole("status")).toBeFocused();
  const zurueckziehen = page.getByRole("region", { name: "Worauf ich warte" })
    .getByRole("button", { name: "Zurückziehen" });
  await zurueckziehen.focus();
  await page.keyboard.press("Enter");
  await expect(page.getByRole("status")).toBeFocused();

  const historie = page.locator("details.completed-processes");
  await expect(anMich.getByText(/\d+ offen/)).toBeVisible();
  await expect(historie.locator("summary")).toHaveAccessibleName(/Erledigte Vorgänge \d+/);
  await expect(historie).not.toHaveAttribute("open", "");
  await historie.locator("summary").focus();
  await page.keyboard.press("Enter");
  await expect(historie).toHaveAttribute("open", "");
  await page.keyboard.press("Enter");
  await expect(historie).not.toHaveAttribute("open", "");
  await page.keyboard.press("Enter");
  const entschieden = historie.getByRole("region", { name: "An mich gerichtet" });
  await expect(entschieden).toContainText("ANGENOMMEN");
  await expect(entschieden).toContainText("ABGELEHNT");
  await expect(entschieden).toContainText("durch");
  const eigeneHistorie = historie.getByRole("region", { name: "Von mir gestellt" });
  await expect(eigeneHistorie).toContainText("ZURUECKGEZOGEN");
  await expect(eigeneHistorie).not.toContainText("durch");

  await page.reload();
  await expect(page.locator("details.completed-processes")).not.toHaveAttribute("open", "");
  await page.locator("details.completed-processes summary").click();
  await expect(page.locator("details.completed-processes")).toContainText("ANGENOMMEN");
  await expect(page.locator("details.completed-processes")).toContainText("ABGELEHNT");
  await expect(page.locator("details.completed-processes")).toContainText("ZURUECKGEZOGEN");
});

// verifies: TEST_DSH_GRUND_01
test("die drei Ränge stehen für Administrator und Trainer in fester Reihenfolge", async ({ page }) => {
  await eigentuemerAnmelden(page);
  expect(await fixture(page, "dashboard-raenge")).toBe(204);
  await page.goto("/");
  await expect(page.getByRole("region", { name: "Vorgänge" })).toContainText("Abwesenheitsantrag");
  await expect(page.getByRole("region", { name: "Handlungspflichten" })
    .locator('a[href*="E2E-RANG-ADMIN-PFLICHT"]'))
    .toHaveAttribute("href", /E2E-RANG-ADMIN-PFLICHT/);
  await expect(page.getByRole("region", { name: "Dringlichkeiten" }))
    .toContainText("2026-09-25");
  expect(await page.locator(".dashboard-rank h2").allTextContents())
    .toEqual(["Vorgänge", "Handlungspflichten", "Dringlichkeiten", "Worauf ich warte"]);

  await page.locator(".account-trigger").click();
  await page.getByRole("button", { name: "Abmelden" }).click();
  await page.getByLabel("E-Mail-Adresse").fill("e2e-nac@example.de");
  await page.getByLabel("Passwort").fill(eigentuemerPasswort);
  await page.getByRole("button", { name: "Anmelden" }).click();
  await expect(page.getByRole("region", { name: "Vorgänge" })).toContainText("Übernahmeanfrage");
  await expect(page.getByRole("region", { name: "Handlungspflichten" })
    .locator('a[href*="E2E-RANG-TRAINER-PFLICHT"]'))
    .toHaveAttribute("href", /E2E-RANG-TRAINER-PFLICHT/);
  await expect(page.getByRole("region", { name: "Dringlichkeiten" }))
    .toContainText("Aktuell verlangt kein Termin besondere Aufmerksamkeit");
  expect(await page.locator(".dashboard-rank h2").allTextContents())
    .toEqual(["Vorgänge", "Handlungspflichten", "Dringlichkeiten", "Worauf ich warte"]);
});
