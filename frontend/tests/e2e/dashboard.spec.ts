import { expect, test, type Page } from "@playwright/test";
import { eigentuemerAnmelden } from "./anmeldung";

async function fixture(page: Page) {
  return page.evaluate(async () => {
    const { token } = await fetch("/api/auth/csrf").then((antwort) => antwort.json());
    return (await fetch("/api/e2e/dashboard-pflichten", {
      method: "PUT", headers: { "X-XSRF-TOKEN": token },
    })).status;
  });
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
  await expect(pflichten).toContainText("Keine eigenen Handlungspflichten");
});
