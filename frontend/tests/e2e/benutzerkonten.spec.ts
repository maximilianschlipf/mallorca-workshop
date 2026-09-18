import { test, expect } from "@playwright/test";
import {
  eigentuemerAnmelden,
  eigentuemerEmail,
  eigentuemerPasswort,
} from "./anmeldung";

async function login(
  page: import("@playwright/test").Page,
  email = eigentuemerEmail,
  passwort = eigentuemerPasswort,
) {
  await page.goto("/anmelden");
  await page.getByLabel("E-Mail-Adresse").fill(email);
  await page.getByLabel("Passwort").fill(passwort);
  await page.getByRole("button", { name: "Anmelden" }).click();
  await expect(page.getByRole("heading", { name: "Schulungskalender" })).toBeVisible();
}

async function registrieren(
  page: import("@playwright/test").Page,
  name: string,
  email: string,
  passwort: string,
) {
  await page.goto("/registrieren");
  await page.getByLabel("Name").fill(name);
  await page.getByLabel("E-Mail-Adresse").fill(email);
  await page.getByLabel("Passwort").fill(passwort);
  await page.getByRole("button", { name: "Benutzerkonto anlegen" }).click();
  await expect(page.getByText("Das Benutzerkonto wurde angelegt.")).toBeVisible();
}

async function logout(page: import("@playwright/test").Page) {
  const antwort = page.waitForResponse(response => response.url().endsWith("/api/auth/abmelden"));
  await page.getByRole("button", { name: "Abmelden" }).click();
  expect((await antwort).status()).toBe(204);
  await expect(page.getByRole("heading", { name: "Anmelden" })).toBeVisible();
}

// verifies: TEST_USR_E2E_01, TEST_USR_LOGIN_01
test("durchläuft den Benutzerkonten-Hauptablauf auf einer frischen Instanz", async ({ page }) => {
  const trainerEmail = "e2e-trainer@example.de";
  // Der Kalender oeffnet den Monat der Browseruhr; der geseedete geplante
  // Termin liegt im September 2026 wie die feste Backenduhr.
  await page.clock.setFixedTime(new Date("2026-09-17T12:00:00"));
  await eigentuemerAnmelden(page);
  await logout(page);
  await registrieren(page, "E2E Trainer", trainerEmail, "trainer-startpasswort");

  await login(page, trainerEmail, "trainer-startpasswort");
  await page.getByRole("link", { name: "Katalog" }).click();
  await page.getByRole("button", { name: "Auf Qualifikation bewerben" }).first().click();
  await expect(page.getByText("Die Bewerbung auf die Qualifikation wurde eingereicht.")).toBeVisible();
  await page.getByRole("link", { name: "Planer" }).click();
  await page.getByRole("button", { name: /Kubernetes Grundlagen/ }).first().click();
  await page.getByRole("button", { name: "Als Assistenz bewerben" }).click();
  await expect(page.getByText("Die Bewerbung auf den Assistenzplatz wurde eingereicht.")).toBeVisible();
  await page.getByRole("button", { name: "Termindetails schließen" }).click();
  await page.getByRole("link", { name: "Profil" }).click();
  await page.getByLabel("Name").fill("E2E Trainer Neu");
  await page.getByRole("button", { name: "Name speichern" }).click();
  await expect(page.getByText("Der Name wurde gespeichert.")).toBeVisible();
  await page.getByLabel("Anfangsdatum").fill("2031-01-10");
  await page.getByLabel("Enddatum").fill("2031-01-12");
  await page.getByLabel("Grund (optional)").fill("Verhindert");
  await page.getByRole("button", { name: "Abwesenheit eintragen" }).click();
  await expect(page.getByText("Die Abwesenheit wurde eingetragen.")).toBeVisible();
  await page.getByLabel("Bisheriges Passwort").fill("trainer-startpasswort");
  await page.getByLabel("Neues Passwort").fill("trainer-neuespasswort");
  await page.getByRole("button", { name: "Passwort ändern" }).click();
  await expect(page.getByText(/Das Passwort wurde geändert/)).toBeVisible();
  await logout(page);
  await login(page, trainerEmail, "trainer-neuespasswort");
  await logout(page);

  await login(page);
  await page.getByRole("link", { name: "Konten" }).click();
  const eigentuemerKonto = page.locator(".verwaltetes-konto").filter({ hasText: eigentuemerEmail });
  await expect(eigentuemerKonto).toContainText("Trainer");
  await expect(eigentuemerKonto).toContainText("Administrator");
  await expect(eigentuemerKonto).toContainText("Eigentümer");
  const trainerKarte = page.locator(".verwaltetes-konto").filter({ hasText: trainerEmail });
  await expect(trainerKarte).toContainText("E2E Trainer Neu");
  await trainerKarte.getByRole("button", { name: "Administratorrolle erteilen" }).click();
  await expect(page.getByText("Die Rollen wurden aktualisiert.")).toBeVisible();
  await expect(trainerKarte).toContainText("Administrator");
  await logout(page);
});

// verifies: TEST_USR_LOGIN_02
test("öffentliche und geschützte Ansichten trennen anonyme Zugriffe", async ({ page }) => {
  await page.goto("/anmelden");
  await expect(page.getByRole("heading", { name: "Anmelden" })).toBeVisible();
  await page.goto("/registrieren");
  await expect(page.getByRole("heading", { name: "Registrieren" })).toBeVisible();

  for (const path of ["/", "/profil", "/katalog", "/benutzerkonten"]) {
    await page.goto(path);
    await expect(page).toHaveURL(/\/anmelden/);
    await expect(page.getByRole("heading", { name: "Anmelden" })).toBeVisible();
  }
  expect((await page.request.get("/api/schulungen")).status()).toBe(401);
  expect((await page.request.get("/api/auth/ich")).status()).toBe(401);
});

// verifies: TEST_USR_LOGIN_08
test("verwirft die Sitzung beim Schließen des Browsers", async ({ browser, baseURL, page }) => {
  await registrieren(page, "E2E Sitzung", "e2e-sitzung@example.de", "e2e-passwort");
  const angemeldeterBrowser = await browser.newContext({ baseURL });
  const angemeldeteSeite = await angemeldeterBrowser.newPage();
  await login(angemeldeteSeite, "e2e-sitzung@example.de", "e2e-passwort");

  const sitzung = (await angemeldeterBrowser.cookies()).find(cookie => cookie.name === "JSESSIONID");
  expect(sitzung?.expires).toBe(-1);
  await expect(angemeldeteSeite.getByLabel(/angemeldet bleiben/i)).toHaveCount(0);
  await angemeldeterBrowser.close();

  const neuerBrowser = await browser.newContext({ baseURL });
  const neueSeite = await neuerBrowser.newPage();
  await neueSeite.goto("/");
  await expect(neueSeite).toHaveURL(/\/anmelden/);
  await neuerBrowser.close();
});
