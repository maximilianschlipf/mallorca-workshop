import { test, expect } from "@playwright/test";
import { startSeite } from "./seiten/StartSeite";

async function mutation(page: import("@playwright/test").Page, url: string, method: string, body?: unknown) {
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

test("Der Kalender blättert durch die Monate und zeigt Termindetails", async ({
  page,
}) => {
  await page.clock.setFixedTime(new Date("2026-09-17T12:00:00"));
  const start = await startSeite(page).oeffnen();

  await expect(start.monatsueberschrift("September 2026")).toBeVisible();

  await start.terminWaehlen(/Scrum Master/);

  await expect(start.termindetails).toContainText("Scrum Master");
  await expect(start.termindetails).toContainText(/remote/i);
  await expect(start.termindetails).toHaveAttribute("aria-modal", "true");

  await start.termindetailsSchliessen();
  await expect(start.termin(/Scrum Master/)).toBeFocused();
  await start.naechsterMonat();

  await expect(start.monatsueberschrift("Oktober 2026")).toBeVisible();
  await expect(page.locator(".calendar-event").first()).toBeVisible();
});

test("Auf dem Telefon wird der Kalender zur Liste", async ({ page }) => {
  await page.clock.setFixedTime(new Date("2026-09-17T12:00:00"));
  await page.setViewportSize({ width: 390, height: 844 });
  const start = await startSeite(page).oeffnen();

  await expect(start.kalenderAlsRaster).toBeHidden();
  await expect(start.kalenderAlsListe).toBeVisible();
  await expect(start.kalenderAlsListe.locator("time").first()).toBeVisible();
});

// verifies: TEST_TER_SICHT_01
test("Terminübersicht führt responsiv von der Neuanlage zu Details und zulässigen Aktionen", async ({ page }) => {
  await page.clock.setFixedTime(new Date("2026-09-17T12:00:00"));
  const start = await startSeite(page).oeffnen();
  await expect(page.locator(".calendar-event.status-geplant").first()).toBeVisible();
  await expect(page.locator(".calendar-event.status-abgeschlossen").first()).toBeVisible();
  await page.setViewportSize({ width: 390, height: 844 });
  const mobileTermine = start.kalenderAlsListe.locator("li");
  expect(await mobileTermine.count()).toBeGreaterThan(1);
  const mobileStartdaten = await mobileTermine.locator("time").evaluateAll((elemente) =>
    elemente.map((element) => element.getAttribute("datetime")),
  );
  expect(mobileStartdaten).toEqual([...mobileStartdaten].sort());
  expect((await mobileTermine.allTextContents()).join(" ")).toMatch(/Abgeschlossen.*Geplant/);
  await page.setViewportSize({ width: 1280, height: 720 });
  await page.locator(".calendar-event.status-abgeschlossen").first().click();
  await expect(page.getByRole("button", { name: "Bearbeiten" })).toHaveCount(0);
  await expect(page.getByRole("button", { name: "Absagen" })).toHaveCount(0);
  await expect(page.getByRole("button", { name: "Löschen" })).toHaveCount(0);
  await expect(page.getByRole("button", { name: "Durchführung bestätigen" })).toHaveCount(0);
  await start.termindetailsSchliessen();
  await start.naechsterMonat();
  await expect(page.locator(".calendar-event.status-abgesagt").first()).toBeVisible();
  await page.locator(".calendar-event.status-abgesagt").first().click();
  await expect(page.getByRole("button", { name: "Bearbeiten" })).toHaveCount(0);
  await expect(page.getByRole("button", { name: "Absagen" })).toHaveCount(0);
  await expect(page.getByRole("button", { name: "Durchführung bestätigen" })).toHaveCount(0);
  await expect(page.getByRole("button", { name: "Löschen" })).toBeVisible();

  await start.termindetailsSchliessen();
  await page.clock.setFixedTime(new Date("2030-01-31T12:00:00"));
  await page.getByRole("button", { name: "Heute" }).click();

  const kalenderVorAbbruch = await page.locator(".calendar-desktop").textContent();
  await page.getByRole("button", { name: "Neuer Termin" }).click();
  await expect(page.getByRole("dialog")).toBeVisible();
  await expect(page.getByRole("dialog").getByLabel("Schulung")).toHaveValue("");
  await expect(page.getByRole("dialog").getByLabel("Startdatum")).toHaveValue("");
  await page.getByRole("button", { name: "Abbrechen" }).click();
  await expect(page.getByRole("dialog")).toBeHidden();
  await expect(page.locator(".calendar-desktop")).toHaveText(kalenderVorAbbruch!);
  await page.locator(".calendar-day:not(:has(.calendar-event))").first().click();
  await expect(page.getByRole("dialog")).toBeHidden();

  await page.getByRole("button", { name: "Neuer Termin" }).click();
  const dialog = page.getByRole("dialog");
  await dialog.getByLabel("Schulung").selectOption("SCH-003");
  await dialog.getByLabel("Startdatum").fill("2030-02-01");
  await expect(dialog.getByLabel("Enddatum")).toHaveValue("2030-02-05");
  await dialog.getByRole("button", { name: "Speichern" }).click();

  await expect(page.getByRole("dialog", { name: "Java Spring Boot" })).toBeVisible();
  const neuerTermin = page.locator(".calendar-event", { hasText: "Java Spring Boot" });
  await expect(start.termindetails).toContainText("3");
  await expect(start.termindetails).toContainText("Geplant");
  await expect(page.getByRole("button", { name: "Bearbeiten" })).toBeVisible();
  await expect(page.getByRole("button", { name: "Absagen" })).toBeVisible();
  await expect(page.getByRole("button", { name: "Löschen" })).toBeVisible();
  await expect(page.getByRole("button", { name: "Durchführung bestätigen" })).toHaveCount(0);
  await expect(start.termindetails.getByText("Schulungstage").locator("..")).toContainText("3");
  await expect(start.termindetails.getByText("Zeitraum").locator("..")).toContainText("01.02.2030 - 05.02.2030");

  await page.getByRole("button", { name: "Bearbeiten" }).click();
  await expect(page.getByRole("dialog", { name: "Termin bearbeiten" })).toBeVisible();
  await page.getByRole("button", { name: "Abbrechen" }).click();
  await neuerTermin.first().click();
  await page.getByRole("button", { name: "Verfügbare Trainer laden" }).click();
  // Auf das Ergebnis warten, nicht auf den Platzhalter: Das Ladeskelett traegt
  // dieselbe Klasse wie die Trefferliste, aber keinen Text.
  await expect(start.termindetails).toContainText(/qualifizierten? Trainer/i);
  await start.termindetailsSchliessen();

  const katalog = await page.request.get("/api/schulungen").then((antwort) => antwort.json());
  const terminId = katalog.find((eintrag: { id: string }) => eintrag.id === "SCH-003")
    .oeffentlicheTermine.find((termin: { startdatum: string }) => termin.startdatum === "2030-02-01").terminId;
  const trainer = await mutation(page, "/api/auth/registrieren", "POST", {
    name: "E2E Kalendertrainer", email: "e2e-kalendertrainer@example.de", passwort: "e2e-passwort",
  });
  expect((await mutation(page, `/api/e2e/qualifikationen/${trainer.body.id}/SCH-003`, "PUT")).status).toBe(204);
  expect((await mutation(page, `/api/termine/${terminId}/trainer/${trainer.body.id}`, "PUT")).status).toBe(204);
  await page.clock.setFixedTime(new Date("2030-02-06T12:00:00"));
  await neuerTermin.first().click();
  await expect(page.getByRole("button", { name: "Trainer abziehen" })).toBeVisible();
  await expect(page.getByRole("button", { name: "Verfügbare Trainer laden" })).toBeVisible();
  await expect(page.getByRole("button", { name: "Durchführung bestätigen" })).toBeVisible();
  await start.termindetailsSchliessen();

  await expect(neuerTermin).toHaveCount(3);
  await expect(page.locator('.calendar-day:has(time[datetime="2030-02-02"]) .calendar-event', { hasText: "Java Spring Boot" })).toHaveCount(0);
  await expect(page.locator('.calendar-day:has(time[datetime="2030-02-03"]) .calendar-event', { hasText: "Java Spring Boot" })).toHaveCount(0);

  await page.setViewportSize({ width: 390, height: 844 });
  await expect(start.kalenderAlsRaster).toBeHidden();
  await expect(start.kalenderAlsListe.locator("li", { hasText: "Java Spring Boot" })).toHaveCount(1);
  await page.getByRole("button", { name: "Neuer Termin" }).click();
  const dialogBox = await page.getByRole("dialog").boundingBox();
  expect(dialogBox?.width).toBeGreaterThanOrEqual(389);
  expect(dialogBox?.height).toBeGreaterThanOrEqual(843);
});

// verifies: TEST_TER_FORM_13
test("Teilnehmerdetails bleiben für Assistenten in Oberfläche und Schnittstelle verborgen", async ({ browser, baseURL, page }) => {
  await page.clock.setFixedTime(new Date("2031-03-03T12:00:00"));
  await startSeite(page).oeffnen();

  const trainerKontext = await browser.newContext({ baseURL });
  const trainerSeite = await trainerKontext.newPage();
  await trainerSeite.goto("/registrieren");
  expect((await mutation(trainerSeite, "/api/auth/registrieren", "POST", {
    name: "E2E Trainer", email: "e2e-teilnehmer-trainer@example.de", passwort: "e2e-passwort",
  })).status).toBe(201);
  expect((await mutation(trainerSeite, "/api/auth/anmelden", "POST", {
    email: "e2e-teilnehmer-trainer@example.de", passwort: "e2e-passwort",
  })).status).toBe(200);
  const trainer = await trainerSeite.request.get("/api/auth/ich").then((antwort) => antwort.json());

  const assistenzKontext = await browser.newContext({ baseURL });
  const assistenzSeite = await assistenzKontext.newPage();
  await assistenzSeite.goto("/registrieren");
  expect((await mutation(assistenzSeite, "/api/auth/registrieren", "POST", {
    name: "E2E Assistenz", email: "e2e-assistenz@example.de", passwort: "e2e-passwort",
  })).status).toBe(201);
  expect((await mutation(assistenzSeite, "/api/auth/anmelden", "POST", {
    email: "e2e-assistenz@example.de", passwort: "e2e-passwort",
  })).status).toBe(200);
  const assistenz = await assistenzSeite.request.get("/api/auth/ich").then((antwort) => antwort.json());

  const angelegt = await mutation(page, "/api/termine", "POST", {
    schulungId: "SCH-001", startdatum: "2031-03-03", enddatum: "2031-03-03",
    zugangsart: "exklusiv", durchfuehrungsart: "remote", ort: null,
    kundenfirma: "Acme", onlineZugang: "https://example.org/geheim",
  });
  expect(angelegt.status).toBe(201);
  const terminId = angelegt.body.terminId as string;
  expect((await mutation(page, `/api/termine/${terminId}/buchungen`, "POST", {
    name: "Geheime Person", firma: "Acme", bemerkung: "Vertraulich", teilnahmestatus: "offen",
  })).status).toBe(201);
  expect((await mutation(page, `/api/e2e/qualifikationen/${trainer.id}/SCH-001`, "PUT")).status).toBe(204);
  expect((await mutation(page, `/api/termine/${terminId}/trainer/${trainer.id}`, "PUT")).status).toBe(204);
  expect((await mutation(page, `/api/termine/${terminId}/assistenten/${assistenz.id}`, "PUT")).status).toBe(204);

  await page.reload();
  await page.getByRole("button", { name: /Scrum Master/ }).first().click();
  await expect(page.locator(".teilnehmerliste")).toContainText("Geheime Person");

  await trainerSeite.clock.setFixedTime(new Date("2031-03-03T12:00:00"));
  await trainerSeite.goto("/");
  await trainerSeite.getByRole("button", { name: /Scrum Master/ }).first().click();
  await expect(trainerSeite.locator(".teilnehmerliste")).toContainText("Geheime Person");

  await assistenzSeite.clock.setFixedTime(new Date("2031-03-03T12:00:00"));
  await assistenzSeite.goto("/");
  await assistenzSeite.getByRole("button", { name: /Scrum Master/ }).first().click();
  await expect(assistenzSeite.locator(".teilnehmerliste")).toHaveCount(0);
  await expect(assistenzSeite.getByText("Geheime Person")).toHaveCount(0);
  const direkteDetails = await assistenzSeite.request.get(`/api/termine/${terminId}`).then((antwort) => antwort.json());
  expect(direkteDetails.teilnehmer).toEqual([]);
  await trainerKontext.close();
  await assistenzKontext.close();
});
