import { test, expect } from "@playwright/test";

test("catalog is visible and API returns 200", async ({ page, request }) => {
  const apiResponse = await request.get("/api/schulungen");
  expect(apiResponse.status()).toBe(200);

  await page.goto("/");
  await expect(
    page.getByRole("heading", { name: "Schulungskatalog" }),
  ).toBeVisible();
  await expect(page.locator(".card").first()).toBeVisible();
});

test("search filters the catalog and reset restores it", async ({ page }) => {
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
  await page.goto("/");
  await expect(page.locator(".card").first()).toBeVisible();

  await page.getByLabel("Titel suchen").fill("gibtesnicht123");
  await expect(
    page.getByText("Keine Schulungen entsprechen den gewählten Filtern"),
  ).toBeVisible();
});

test("calendar navigates by month and shows training details", async ({ page }) => {
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
  await page.clock.setFixedTime(new Date("2026-08-26T12:00:00"));
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto("/");

  await expect(page.locator(".calendar-desktop")).toBeHidden();
  await expect(page.locator(".calendar-mobile")).toBeVisible();
  await expect(page.locator(".calendar-mobile time").first()).toHaveText(
    "25.08.2026",
  );
});
