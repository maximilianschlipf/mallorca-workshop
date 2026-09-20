import { expect, test, type Page } from "@playwright/test";
import { eigentuemerAnmelden } from "./anmeldung";

async function mutation(page: Page, url: string) {
  return page.evaluate(async (ziel) => {
    const { token } = await fetch("/api/auth/csrf").then((antwort) => antwort.json());
    return (await fetch(ziel, { method: "PUT", headers: { "X-XSRF-TOKEN": token } })).status;
  }, url);
}

// verifies: TEST_NAC_ANZ_01, TEST_NAC_ANZ_02, TEST_NAC_ANZ_08
test("Benachrichtigungen sind erreichbar, sortiert, lesbar und konkret verlinkt", async ({ page }) => {
  await eigentuemerAnmelden(page);
  expect(await mutation(page, "/api/e2e/benachrichtigungen")).toBe(204);
  await page.reload();
  await expect(page.getByLabel("3 ungelesene Benachrichtigungen")).toBeVisible();

  for (const pfad of ["/", "/planer", "/katalog", "/profil", "/benutzerkonten"]) {
    await page.goto(pfad);
    await expect(page.getByRole("link", { name: /Benachrichtigungen/ })).toBeVisible();
  }
  await page.getByRole("link", { name: /Benachrichtigungen/ }).click();

  const eintraege = page.locator(".notification-list li");
  await expect(eintraege).toHaveCount(3);
  await expect(eintraege).toHaveText([
    /Gelesen.*Neueste Mitteilung/,
    /Gelesen.*Mittlere Mitteilung/,
    /Gelesen.*Älteste Mitteilung/,
  ]);
  expect(await eintraege.locator("time").evaluateAll(elemente =>
    elemente.map(element => element.getAttribute("datetime"))))
    .toEqual(["2026-09-17T10:00:00", "2026-09-17T09:00:00", "2026-09-17T08:00:00"]);

  const allesGelesen = page.getByRole("button", { name: "Alles als gelesen" });
  await allesGelesen.focus();
  await page.keyboard.press("Enter");
  await expect(page.getByRole("status")).toBeFocused();

  const terminLink = eintraege.nth(1).getByRole("link", { name: "Gegenstand öffnen" });
  await terminLink.focus();
  await page.keyboard.press("Enter");
  await expect(page).toHaveURL(/\/planer\?termin=E2E-NAC-TERMIN#kalender$/);
  await expect(page.getByRole("dialog", { name: "Scrum Master Zertifizierung" })).toBeVisible();

  await page.goto("/benachrichtigungen");
  await eintraege.nth(2).getByRole("link", { name: "Gegenstand öffnen" }).click();
  await expect(page).toHaveURL(/\/katalog\/SCH-001$/);
  await expect(page.getByRole("heading", { name: "Scrum Master Zertifizierung" })).toBeVisible();

  await page.reload();
  await expect(page.getByLabel(/ungelesene Benachrichtigungen/)).toHaveCount(0);
});

// verifies: TEST_NAC_ANZ_06, TEST_DSH_GRUND_02
test("Entscheidungen stehen nur im Dashboard", async ({ page }) => {
  await eigentuemerAnmelden(page);
  expect(await mutation(page, "/api/e2e/benachrichtigungen")).toBe(204);

  await page.goto("/benachrichtigungen");
  await expect(page.getByRole("button", { name: /Annehmen|Ablehnen|Zurückziehen/ })).toHaveCount(0);
  await expect(page.getByText("Übernahmeanfrage")).toHaveCount(0);
  await expect(page.getByText(/Termin E2E-NAC-TERMIN wurde geändert/)).toBeVisible();

  await page.goto("/");
  const vorgang = page.getByRole("listitem").filter({ hasText: "Übernahmeanfrage" });
  await expect(vorgang).toBeVisible();
  await expect(vorgang.getByRole("button", { name: "Annehmen" })).toBeVisible();
  await expect(vorgang.getByRole("button", { name: "Ablehnen" })).toBeVisible();
  await expect(page.getByText(/Termin E2E-NAC-TERMIN wurde geändert/)).toHaveCount(0);
});
