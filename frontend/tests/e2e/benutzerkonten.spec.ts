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
  await expect(page.getByRole("heading", { name: "Was liegt bei mir?" })).toBeVisible();
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
  const abmelden = page.getByRole("button", { name: "Abmelden" });
  if (!await abmelden.isVisible()) await page.locator(".account-trigger").click();
  await abmelden.click();
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
  await page.getByRole("link", { name: "Schulungskatalog" }).click();
  await page.getByRole("button", { name: "Auf Qualifikation bewerben" }).first().click();
  await expect(page.getByText("Die Bewerbung auf die Qualifikation wurde eingereicht.")).toBeVisible();
  await page.getByRole("link", { name: "Terminplaner" }).click();
  await page.getByRole("button", { name: /Kubernetes Grundlagen/ }).first().click();
  await page.getByRole("button", { name: "Als Assistenz bewerben" }).click();
  await expect(page.getByText("Die Bewerbung auf den Assistenzplatz wurde eingereicht.")).toBeVisible();
  await page.getByRole("button", { name: "Termindetails schließen" }).click();
  await page.locator(".account-trigger").click();
  await page.getByRole("link", { name: "Mein Profil" }).click();
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
  await page.getByRole("link", { name: "Benutzerkonten" }).click();
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

// verifies: TEST_USR_NAV_01
test("trennt Arbeitsbereiche, Verwaltung und persönliches Konto rollenabhängig", async ({ page }) => {
  const trainerEmail = "e2e-navigation-trainer@example.de";
  await page.setViewportSize({ width: 1440, height: 900 });
  await eigentuemerAnmelden(page);
  expect(await page.evaluate(async () => {
    const { token } = await fetch("/api/auth/csrf").then(antwort => antwort.json());
    return (await fetch("/api/e2e/benachrichtigungen", {
      method: "PUT",
      headers: { "X-XSRF-TOKEN": token },
    })).status;
  })).toBe(204);
  await page.reload();

  const navigation = page.getByRole("navigation", { name: "Hauptnavigation" });
  const arbeitsbereiche = navigation.getByRole("list", { name: "Arbeitsbereiche" });
  const menueschalter = navigation.getByRole("button", { name: "Menü" });
  await expect(arbeitsbereiche.getByRole("link")).toHaveText([
    "Dashboard",
    "Terminplaner",
    "Schulungskatalog",
  ]);
  await expect(arbeitsbereiche.getByRole("link", { name: "Dashboard" })).toHaveAttribute("aria-current", "page");
  await expect(navigation.getByRole("link", { name: "3 ungelesene Benachrichtigungen" })).toBeVisible();
  await expect(navigation.getByRole("link", { name: "Benutzerkonten" })).toBeVisible();
  const kontoschalter = navigation.getByLabel(/^Kontomenü für/);
  await expect(kontoschalter).toHaveAccessibleName(
    "Kontomenü für E2E Eigentümer, Rollen: Eigentümer · Administrator · Trainer",
  );
  await kontoschalter.locator("strong").evaluate(element => element.textContent = "L".repeat(255));
  await navigation.locator(".notification-count").evaluate(element => element.textContent = "9999");
  await page.setViewportSize({ width: 1201, height: 800 });
  await expect(menueschalter).toBeHidden();
  for (const ziel of ["Dashboard", "Terminplaner", "Schulungskatalog"]) {
    await expect(arbeitsbereiche.getByRole("link", { name: ziel })).toBeVisible();
  }
  await expect(navigation.getByRole("link", { name: "Benutzerkonten" })).toBeVisible();
  await expect(navigation.getByRole("link", { name: "3 ungelesene Benachrichtigungen" })).toBeVisible();
  await expect(kontoschalter).toBeVisible();
  expect(await page.locator(".nav").evaluate(element => element.scrollWidth <= element.clientWidth)).toBe(true);
  expect(await kontoschalter.locator("small").evaluate(element => element.scrollWidth <= element.clientWidth)).toBe(true);
  for (const breite of [1300, 1301]) {
    await page.setViewportSize({ width: breite, height: 800 });
    await expect(menueschalter).toBeHidden();
    expect(await page.locator(".nav").evaluate(element => element.scrollWidth <= element.clientWidth)).toBe(true);
    expect(await kontoschalter.locator("small").evaluate(element => element.scrollWidth <= element.clientWidth)).toBe(true);
  }

  await page.setViewportSize({ width: 1200, height: 800 });
  await expect(menueschalter).toBeVisible();
  const positionGeschlossen = await menueschalter.boundingBox();
  await menueschalter.click();
  await expect(menueschalter).toHaveAttribute("aria-expanded", "true");
  for (const ziel of ["Dashboard", "Terminplaner", "Schulungskatalog"]) {
    await expect(arbeitsbereiche.getByRole("link", { name: ziel })).toBeVisible();
  }
  await expect(navigation.getByRole("link", { name: "Benutzerkonten" })).toBeVisible();
  await expect(navigation.getByRole("link", { name: "3 ungelesene Benachrichtigungen" })).toBeVisible();
  await expect(kontoschalter).toBeVisible();
  const positionOffen = await menueschalter.boundingBox();
  expect(positionOffen?.y).toBeCloseTo(positionGeschlossen?.y ?? 0, 1);

  await page.setViewportSize({ width: 900, height: 800 });
  const verwaltungsbereich = navigation.locator(".utility-navigation");
  const rollenanzeige = kontoschalter.locator("small");
  await expect(arbeitsbereiche).toBeVisible();
  await expect(verwaltungsbereich).toBeVisible();
  await expect(rollenanzeige).toBeVisible();
  const arbeitsbereichBox = await arbeitsbereiche.boundingBox();
  const verwaltungsbereichBox = await verwaltungsbereich.boundingBox();
  expect(arbeitsbereichBox).not.toBeNull();
  expect(verwaltungsbereichBox).not.toBeNull();
  expect(arbeitsbereichBox!.y + arbeitsbereichBox!.height).toBeLessThanOrEqual(verwaltungsbereichBox!.y);
  expect(await rollenanzeige.evaluate(element => element.scrollWidth <= element.clientWidth)).toBe(true);

  await menueschalter.click();
  await page.setViewportSize({ width: 1440, height: 900 });
  await page.reload();

  await page.emulateMedia({ colorScheme: "dark" });
  await expect(kontoschalter.locator("strong")).toHaveCSS("color", "rgb(255, 255, 255)");
  await expect(navigation.locator(".notification-count")).toHaveCSS("color", "rgb(17, 19, 40)");
  await page.emulateMedia({ colorScheme: "light" });
  await kontoschalter.click();
  const kontomenue = navigation.getByRole("group", { name: "Persönliches Konto" });
  await expect(kontomenue.getByRole("link")).toHaveText(["Mein Profil"]);
  await expect(kontomenue.getByRole("button")).toHaveText(["Abmelden"]);
  await kontomenue.getByRole("link", { name: "Mein Profil" }).focus();
  await page.keyboard.press("Enter");
  await expect(page).toHaveURL(/\/profil$/);
  await expect(page.locator("#hauptinhalt")).toBeFocused();

  for (const pfad of ["/", "/planer", "/katalog", "/profil", "/benutzerkonten"]) {
    await page.goto(pfad);
    await expect(navigation.getByRole("link", { name: "3 ungelesene Benachrichtigungen" })).toBeVisible();
  }
  await page.goto("/kategorien");
  await expect(page.locator('.work-navigation a[href="/katalog"]')).toHaveAttribute("aria-current", "location");
  await page.goto("/");

  await page.setViewportSize({ width: 390, height: 844 });
  await menueschalter.focus();
  await page.keyboard.press("Enter");
  await expect(arbeitsbereiche.getByRole("link")).toHaveText([
    "Dashboard",
    "Terminplaner",
    "Schulungskatalog",
  ]);
  for (const ziel of ["Dashboard", "Terminplaner", "Schulungskatalog"]) {
    await expect(arbeitsbereiche.getByRole("link", { name: ziel })).toBeVisible();
  }
  await expect(arbeitsbereiche.getByRole("link", { name: "Terminplaner" })).toBeVisible();
  await expect(navigation.getByRole("link", { name: "Benutzerkonten" })).toBeVisible();
  await expect(navigation.getByRole("link", { name: "3 ungelesene Benachrichtigungen" })).toBeVisible();
  await expect(kontoschalter).toHaveAccessibleName(
    "Kontomenü für E2E Eigentümer, Rollen: Eigentümer · Administrator · Trainer",
  );
  await arbeitsbereiche.getByRole("link", { name: "Terminplaner" }).focus();
  await page.keyboard.press("Enter");
  await expect(page).toHaveURL(/\/planer$/);
  await expect(page.locator('.work-navigation a[href="/planer"]')).toHaveAttribute("aria-current", "page");
  await expect(page.locator("#hauptinhalt")).toBeFocused();
  await menueschalter.focus();
  await page.keyboard.press("Enter");
  await kontoschalter.focus();
  await page.keyboard.press("Enter");
  await expect(kontomenue.getByRole("link")).toHaveText(["Mein Profil"]);
  await expect(kontomenue.getByRole("button")).toHaveText(["Abmelden"]);
  await kontomenue.getByRole("link", { name: "Mein Profil" }).focus();
  await page.keyboard.press("Enter");
  await expect(page).toHaveURL(/\/profil$/);
  await menueschalter.focus();
  await page.keyboard.press("Enter");
  await kontoschalter.focus();
  await page.keyboard.press("Enter");
  await kontomenue.getByRole("button", { name: "Abmelden" }).focus();
  await page.keyboard.press("Enter");
  await expect(page.getByRole("heading", { name: "Anmelden" })).toBeVisible();

  await registrieren(page, "Navigation Trainer", trainerEmail, "trainer-passwort");
  await login(page, trainerEmail, "trainer-passwort");
  await page.setViewportSize({ width: 1440, height: 900 });
  await expect(arbeitsbereiche.getByRole("link")).toHaveText([
    "Dashboard",
    "Terminplaner",
    "Schulungskatalog",
  ]);
  for (const ziel of ["Dashboard", "Terminplaner", "Schulungskatalog"]) {
    await expect(arbeitsbereiche.getByRole("link", { name: ziel })).toBeVisible();
  }
  await expect(navigation.getByRole("link", { name: "Benachrichtigungen" })).toBeVisible();
  await expect(navigation.getByRole("link", { name: "Benutzerkonten" })).toHaveCount(0);
  await expect(kontoschalter).toHaveAccessibleName("Kontomenü für Navigation Trainer, Rollen: Trainer");
  await kontoschalter.click();
  await expect(kontomenue.getByRole("link")).toHaveText(["Mein Profil"]);
  await expect(kontomenue.getByRole("button")).toHaveText(["Abmelden"]);
  await kontoschalter.click();
  await page.goto("/benutzerkonten");
  await expect(page).toHaveURL(/\/$/);

  await page.setViewportSize({ width: 390, height: 844 });
  await menueschalter.focus();
  await page.keyboard.press("Enter");
  await expect(navigation.getByRole("link", { name: "Benutzerkonten" })).toHaveCount(0);
  await expect(arbeitsbereiche.getByRole("link")).toHaveText([
    "Dashboard",
    "Terminplaner",
    "Schulungskatalog",
  ]);
  for (const ziel of ["Dashboard", "Terminplaner", "Schulungskatalog"]) {
    await expect(arbeitsbereiche.getByRole("link", { name: ziel })).toBeVisible();
  }
  await expect(navigation.getByRole("link", { name: "Benachrichtigungen" })).toBeVisible();
  await expect(kontoschalter).toHaveAccessibleName("Kontomenü für Navigation Trainer, Rollen: Trainer");
  await kontoschalter.focus();
  await page.keyboard.press("Enter");
  await expect(kontomenue.getByRole("link")).toHaveText(["Mein Profil"]);
  await expect(kontomenue.getByRole("button")).toHaveText(["Abmelden"]);
  await kontomenue.getByRole("button", { name: "Abmelden" }).focus();
  await page.keyboard.press("Enter");
  await expect(page.getByRole("heading", { name: "Anmelden" })).toBeVisible();
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
