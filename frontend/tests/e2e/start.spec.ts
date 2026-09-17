import { test, expect } from "@playwright/test";
import { startSeite } from "./seiten/StartSeite";

test("Katalog ist sichtbar und die Schnittstelle antwortet", async ({ page }) => {
  const start = await startSeite(page).oeffnen();

  expect((await page.request.get("/api/schulungen")).status()).toBe(200);
  await expect(start.schulungskarten.first()).toBeVisible();
});

test("Suche schränkt den Katalog ein, Zurücksetzen stellt ihn wieder her", async ({
  page,
}) => {
  const start = await startSeite(page).oeffnen();
  const gesamt = await start.schulungskarten.count();

  await start.sucheNach("Scrum");

  await expect(start.trefferzahl).toBeVisible();
  expect(await start.schulungskarten.count()).toBeLessThanOrEqual(gesamt);
  await expect(start.titelDerErstenKarte()).toContainText(/scrum/i);

  await start.setzeFilterZurueck();

  await expect(start.schulungskarten).toHaveCount(gesamt);
});

test("Ohne Treffer erscheint eine eigene Meldung", async ({ page }) => {
  const start = await startSeite(page).oeffnen();

  await start.sucheNach("gibtesnicht123");

  await expect(start.leermeldung).toBeVisible();
});

test("Der Kalender blättert durch die Monate und zeigt Termindetails", async ({
  page,
}) => {
  await page.clock.setFixedTime(new Date("2026-08-26T12:00:00"));
  const start = await startSeite(page).oeffnen();

  await expect(start.monatsueberschrift("August 2026")).toBeVisible();

  await start.terminWaehlen(/Cyber Security Awareness/);

  await expect(start.termindetails).toContainText("Cyber Security Awareness");
  await expect(start.termindetails).toContainText("Online");

  await start.naechsterMonat();

  await expect(start.monatsueberschrift("September 2026")).toBeVisible();
  await expect(start.termin(/Scrum Master/)).toBeVisible();
});

test("Auf dem Telefon wird der Kalender zur Liste", async ({ page }) => {
  await page.clock.setFixedTime(new Date("2026-08-26T12:00:00"));
  await page.setViewportSize({ width: 390, height: 844 });
  const start = await startSeite(page).oeffnen();

  await expect(start.kalenderAlsRaster).toBeHidden();
  await expect(start.kalenderAlsListe).toBeVisible();
  await expect(start.kalenderAlsListe.locator("time").first()).toHaveText(
    "25.08.2026",
  );
});
