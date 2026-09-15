import { test, expect } from "@playwright/test";

test.describe.configure({ mode: "serial" });

const eigentuemerEmail = "e2e-eigentuemer@example.de";
const eigentuemerPasswort = "e2e-eigentuemer-passwort";

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
  await registrieren(page, "E2E Eigentümer", eigentuemerEmail, eigentuemerPasswort);
  await registrieren(page, "E2E Trainer", trainerEmail, "trainer-startpasswort");

  await login(page, trainerEmail, "trainer-startpasswort");
  await page.getByRole("button", { name: "Auf Qualifikation bewerben" }).first().click();
  await expect(page.getByText("Die Bewerbung auf die Qualifikation wurde eingereicht.")).toBeVisible();
  await page.getByRole("button", { name: /Kubernetes Grundlagen/ }).first().click();
  await page.getByRole("button", { name: "Als Assistenz bewerben" }).click();
  await expect(page.getByText("Die Bewerbung auf den Assistenzplatz wurde eingereicht.")).toBeVisible();
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

// verifies: TEST_USR_LOGIN_08
test("verwirft die Sitzung beim Schließen des Browsers", async ({ browser, baseURL }) => {
  const angemeldeterBrowser = await browser.newContext({ baseURL });
  const angemeldeteSeite = await angemeldeterBrowser.newPage();
  await login(angemeldeteSeite);

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

test("catalog is visible and API returns 200", async ({ page }) => {
  await login(page);
  const apiResponse = await page.request.get("/api/schulungen");
  expect(apiResponse.status()).toBe(200);
  await expect(
    page.getByRole("heading", { name: "Schulungskatalog" }),
  ).toBeVisible();
  await expect(page.locator(".card").first()).toBeVisible();
});

test("search filters the catalog and reset restores it", async ({ page }) => {
  await login(page);
  await page.goto("/");
  await expect(page.locator(".card").first()).toBeVisible();

  const total = await page.locator(".course-card").count();

  await page.getByLabel("Titel suchen").fill("Scrum");
  await expect(page.getByText(/Schulung(en)? gefunden/)).toBeVisible();
  const filtered = await page.locator(".course-card").count();
  expect(filtered).toBeLessThanOrEqual(total);
  await expect(page.locator(".course-card h3").first()).toContainText(/scrum/i);

  await page.getByRole("button", { name: "Filter zurücksetzen" }).click();
  await expect(page.locator(".course-card")).toHaveCount(total);
});

test("shows empty state when no training matches", async ({ page }) => {
  await login(page);
  await page.goto("/");
  await expect(page.locator(".card").first()).toBeVisible();

  await page.getByLabel("Titel suchen").fill("gibtesnicht123");
  await expect(
    page.getByText("Keine Schulungen entsprechen den gewählten Filtern"),
  ).toBeVisible();
});

test("calendar navigates by month and shows training details", async ({ page }) => {
  await login(page);
  await page.clock.setFixedTime(new Date("2026-08-26T12:00:00"));
  await page.goto("/");

  await expect(
    page.getByRole("heading", { name: "August 2026" }),
  ).toBeVisible();
  await page.getByRole("button", { name: /Cyber Security Awareness/ }).first().click();
  await expect(page.locator(".calendar-detail")).toContainText(
    "Cyber Security Awareness",
  );
  await expect(page.locator(".calendar-detail")).toContainText("Online");

  await page.getByRole("button", { name: "Weiter" }).click();
  await expect(
    page.getByRole("heading", { name: "September 2026" }),
  ).toBeVisible();
  await expect(page.getByRole("button", { name: /Scrum Master/ }).first()).toBeVisible();
});

test("calendar uses the chronological list on mobile", async ({ page }) => {
  await login(page);
  await page.clock.setFixedTime(new Date("2026-08-26T12:00:00"));
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto("/");

  await expect(page.locator(".calendar-desktop")).toBeHidden();
  await expect(page.locator(".calendar-mobile")).toBeVisible();
  await expect(page.locator(".calendar-mobile time").first()).toHaveText(
    "25.08.2026",
  );
});
