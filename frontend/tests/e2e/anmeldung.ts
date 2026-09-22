import { expect, type Page } from "@playwright/test";

export const eigentuemerEmail = "e2e-eigentuemer@example.de";
export const eigentuemerPasswort = "e2e-eigentuemer-passwort";

const angemeldeteSeiten = new WeakSet<Page>();

export async function eigentuemerAnmelden(page: Page): Promise<void> {
  if (angemeldeteSeiten.has(page)) return;

  await page.goto("/anmelden");
  await page.getByLabel("E-Mail-Adresse").fill(eigentuemerEmail);
  await page.getByLabel("Passwort").fill(eigentuemerPasswort);
  await page.getByRole("button", { name: "Anmelden" }).click();

  const dashboard = page.getByRole("heading", { name: "Was liegt bei mir?" });
  try {
    await dashboard.waitFor({ timeout: 2_000 });
  } catch {
    await page.goto("/registrieren");
    await page.getByLabel("Name").fill("E2E Eigentümer");
    await page.getByLabel("E-Mail-Adresse").fill(eigentuemerEmail);
    await page.getByLabel("Passwort").fill(eigentuemerPasswort);
    await page.getByRole("button", { name: "Benutzerkonto anlegen" }).click();
    await expect(page.getByText("Das Benutzerkonto wurde angelegt.")).toBeVisible();
    await page.getByLabel("E-Mail-Adresse").fill(eigentuemerEmail);
    await page.getByLabel("Passwort").fill(eigentuemerPasswort);
    await page.getByRole("button", { name: "Anmelden" }).click();
  }

  await expect(dashboard).toBeVisible();
  angemeldeteSeiten.add(page);
}
