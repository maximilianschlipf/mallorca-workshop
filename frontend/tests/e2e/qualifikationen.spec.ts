import { expect, test, type Browser, type BrowserContext, type Page } from "@playwright/test";
import { eigentuemerAnmelden } from "./anmeldung";

async function mutation(page: Page, url: string, method: string, body?: unknown) {
  return page.evaluate(async ({ url, method, body }) => {
    const { token } = await fetch("/api/auth/csrf").then((antwort) => antwort.json());
    const antwort = await fetch(url, {
      method,
      headers: { "Content-Type": "application/json", "X-XSRF-TOKEN": token },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
    return { status: antwort.status, body: antwort.status === 204 ? null : await antwort.json() };
  }, { url, method, body });
}

// Entspricht der festen Backenduhr aus application-e2e.yml.
const E2E_HEUTE = "2026-09-17";
const E2E_MORGEN = "2026-09-18";

async function trainerAnlegen(
  browser: Browser,
  baseURL: string | undefined,
  adminSeite: Page,
  kennung: string,
): Promise<{ context: BrowserContext; page: Page; id: string }> {
  const email = `${kennung.toLowerCase().replace(/[^a-z0-9]+/g, "-")}@example.de`;
  const registrierung = await mutation(adminSeite, "/api/auth/registrieren", "POST", {
    name: kennung, email, passwort: "e2e-passwort",
  });
  expect(registrierung.status).toBe(201);
  const context = await browser.newContext({ baseURL });
  const page = await context.newPage();
  await page.goto("/anmelden");
  expect((await mutation(page, "/api/auth/anmelden", "POST", {
    email, passwort: "e2e-passwort",
  })).status).toBe(200);
  return { context, page, id: registrierung.body.id as string };
}

// verifies: TEST_QUA_BEW_02
test("bestehende Qualifikation ersetzt die Bewerbung in Oberfläche und API", async ({
  browser, baseURL, page,
}) => {
  await eigentuemerAnmelden(page);
  const trainer = await trainerAnlegen(browser, baseURL, page, "E2E Qualifiziert");
  expect((await mutation(page,
    `/api/e2e/qualifikationen/${trainer.id}/SCH-006`, "PUT")).status).toBe(204);

  await trainer.page.goto("/katalog");
  const zeile = trainer.page.locator("tbody tr", { hasText: "SCH-006" });
  await expect(zeile).toContainText("Qualifiziert");
  await expect(zeile.getByRole("button", { name: "Auf Qualifikation bewerben" })).toHaveCount(0);
  const versuch = await mutation(
    trainer.page, "/api/ich/qualifikationsbewerbungen/SCH-006", "POST",
  );
  expect(versuch.status).toBe(409);
  expect(versuch.body.code).toBe("BEREITS_QUALIFIZIERT");
  await trainer.context.close();
});

// verifies: TEST_QUA_BEW_04
test("eine zurückgezogene Bewerbung kann in der Oberfläche neu gestellt werden", async ({
  browser, baseURL, page,
}) => {
  await eigentuemerAnmelden(page);
  const trainer = await trainerAnlegen(browser, baseURL, page, "E2E Zurueckziehen");
  await trainer.page.goto("/katalog");
  const zeile = trainer.page.locator("tbody tr", { hasText: "SCH-004" });

  await zeile.getByRole("button", { name: "Auf Qualifikation bewerben" }).click();
  await expect(trainer.page.getByText(
    "Die Bewerbung auf die Qualifikation wurde eingereicht.",
  )).toBeVisible();
  await zeile.getByRole("button", { name: "Bewerbung zurückziehen" }).click();
  await expect(trainer.page.getByText("Die Bewerbung wurde zurückgezogen.")).toBeVisible();
  await zeile.getByRole("button", { name: "Auf Qualifikation bewerben" }).click();
  await expect(zeile.getByRole("button", { name: "Bewerbung zurückziehen" })).toBeVisible();
  await trainer.context.close();
});

// verifies: TEST_QUA_ABLEGEN_02
test("Ablegen warnt und räumt nur die künftige Zuweisung ab", async ({
  browser, baseURL, page,
}) => {
  await eigentuemerAnmelden(page);
  const trainer = await trainerAnlegen(browser, baseURL, page, "E2E Ablegen");
  expect((await mutation(page,
    `/api/e2e/qualifikationen/${trainer.id}/SCH-006`, "PUT")).status).toBe(204);

  const abgeschlossen = await mutation(page, "/api/termine", "POST", {
    schulungId: "SCH-006", startdatum: E2E_HEUTE, enddatum: E2E_HEUTE,
    zugangsart: null, durchfuehrungsart: null, ort: null,
    kundenfirma: null, onlineZugang: null,
  });
  expect(abgeschlossen.status).toBe(201);
  expect((await mutation(page,
    `/api/termine/${abgeschlossen.body.terminId}/trainer/${trainer.id}`, "PUT")).status).toBe(204);
  expect((await mutation(page,
    `/api/termine/${abgeschlossen.body.terminId}/bestaetigung`, "POST")).status).toBe(200);

  const zukunft = await mutation(page, "/api/termine", "POST", {
    schulungId: "SCH-006", startdatum: E2E_MORGEN, enddatum: E2E_MORGEN,
    zugangsart: null, durchfuehrungsart: null, ort: null,
    kundenfirma: null, onlineZugang: null,
  });
  expect(zukunft.status).toBe(201);
  expect((await mutation(page,
    `/api/termine/${zukunft.body.terminId}/trainer/${trainer.id}`, "PUT")).status).toBe(204);

  await trainer.page.goto("/profil");
  await trainer.page.getByRole("button", { name: "Qualifikation ablegen" }).click();
  const dialog = trainer.page.getByRole("dialog", { name: "Qualifikation ablegen?" });
  await expect(dialog).toContainText("1 künftige Terminzuweisung");
  await dialog.getByRole("button", { name: "Qualifikation ablegen" }).click();
  await expect(trainer.page.getByText("Die Qualifikation wurde abgelegt.")).toBeVisible();
  await expect(trainer.page.getByText("Cloud Security Fundamentals")).toHaveCount(0);

  const kuenftig = await trainer.page.request.get(`/api/termine/${zukunft.body.terminId}`)
    .then((antwort) => antwort.json());
  const historisch = await trainer.page.request.get(`/api/termine/${abgeschlossen.body.terminId}`)
    .then((antwort) => antwort.json());
  expect(kuenftig.trainerId).toBeNull();
  expect(historisch.trainerId).toBe(trainer.id);

  await trainer.context.close();
});

// verifies: TEST_QUA_ABLEGEN_04
test("Ablegen erreicht den Adminbereich als Benachrichtigung", async ({
  browser, baseURL, page,
}) => {
  await eigentuemerAnmelden(page);
  const trainer = await trainerAnlegen(browser, baseURL, page, "E2E Ablegen Nachricht");
  expect((await mutation(page,
    `/api/e2e/qualifikationen/${trainer.id}/SCH-005`, "PUT")).status).toBe(204);

  await trainer.page.goto("/profil");
  await trainer.page.getByRole("button", { name: "Qualifikation ablegen" }).click();
  await trainer.page.getByRole("dialog", { name: "Qualifikation ablegen?" })
    .getByRole("button", { name: "Qualifikation ablegen" }).click();
  await expect(trainer.page.getByText("Die Qualifikation wurde abgelegt.")).toBeVisible();

  await page.goto("/profil");
  await expect(page.getByText(
    "E2E Ablegen Nachricht hat die Qualifikation für SCH-005 abgelegt.",
  )).toBeVisible();
  await trainer.context.close();
});

// verifies: TEST_QUA_ENTSCHEID_05
test("Genehmigung und Ablehnung erreichen die Trainer als Benachrichtigung", async ({
  browser, baseURL, page,
}) => {
  await eigentuemerAnmelden(page);
  const genehmigt = await trainerAnlegen(browser, baseURL, page, "E2E Genehmigt");
  const abgelehnt = await trainerAnlegen(browser, baseURL, page, "E2E Abgelehnt");
  expect((await mutation(genehmigt.page,
    "/api/ich/qualifikationsbewerbungen/SCH-001", "POST")).status).toBe(204);
  expect((await mutation(abgelehnt.page,
    "/api/ich/qualifikationsbewerbungen/SCH-002", "POST")).status).toBe(204);

  await page.goto("/katalog/SCH-001");
  await page.getByRole("button", { name: "Bewerbung von E2E Genehmigt genehmigen" }).click();
  await expect(page.getByText("Die Bewerbung wurde genehmigt.")).toBeVisible();

  await page.goto("/katalog/SCH-002");
  await page.getByRole("button", { name: "Bewerbung von E2E Abgelehnt ablehnen" }).click();
  const ablehnung = page.getByRole("dialog", { name: "Bewerbung von E2E Abgelehnt ablehnen?" });
  await ablehnung.getByLabel("Begründung").fill("Nachweis fehlt");
  await ablehnung.getByRole("button", { name: "Bewerbung ablehnen" }).click();
  await expect(page.getByText("Die Bewerbung wurde abgelehnt.")).toBeVisible();

  await genehmigt.page.goto("/profil");
  await expect(genehmigt.page.getByText(
    "Ihre Qualifikationsbewerbung für SCH-001 wurde genehmigt.",
  )).toBeVisible();
  await abgelehnt.page.goto("/profil");
  await expect(abgelehnt.page.getByText(
    "Ihre Qualifikationsbewerbung für SCH-002 wurde abgelehnt: Nachweis fehlt",
  )).toBeVisible();
  await genehmigt.context.close();
  await abgelehnt.context.close();
});

// verifies: TEST_QUA_DIREKT_04
test("Direktvergabe erreicht den Trainer als Benachrichtigung", async ({
  browser, baseURL, page,
}) => {
  await eigentuemerAnmelden(page);
  const trainer = await trainerAnlegen(browser, baseURL, page, "E2E Direkt Entzug");

  await page.goto("/katalog/SCH-003");
  await page.getByRole("button", { name: "E2E Direkt Entzug direkt qualifizieren" }).click();
  await expect(page.getByText("Die Qualifikation wurde erteilt.")).toBeVisible();
  await trainer.page.goto("/profil");
  await expect(trainer.page.getByText("Sie wurden direkt für SCH-003 qualifiziert.")).toBeVisible();
  await trainer.context.close();
});

// verifies: TEST_QUA_ENTZUG_02
test("Entzug erreicht den Trainer als Benachrichtigung", async ({
  browser, baseURL, page,
}) => {
  await eigentuemerAnmelden(page);
  const trainer = await trainerAnlegen(browser, baseURL, page, "E2E Entzug");
  expect((await mutation(page,
    `/api/e2e/qualifikationen/${trainer.id}/SCH-003`, "PUT")).status).toBe(204);

  await page.goto("/katalog/SCH-003");
  await page.getByRole("button", { name: "Qualifikation von E2E Entzug entziehen" }).click();
  const entzug = page.getByRole("dialog", { name: "Qualifikation von E2E Entzug entziehen?" });
  await expect(entzug).toContainText("Künftige Zuweisungen");
  await entzug.getByRole("button", { name: "Qualifikation entziehen" }).click();
  await expect(page.getByText("Die Qualifikation wurde entzogen.")).toBeVisible();
  await trainer.page.goto("/profil");
  await expect(trainer.page.getByText("Ihre Qualifikation für SCH-003 wurde entzogen.")).toBeVisible();
  await trainer.context.close();
});
