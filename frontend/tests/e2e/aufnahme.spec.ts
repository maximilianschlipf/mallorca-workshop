import { test, expect } from "@playwright/test";
import { aufnahmeSeite } from "./seiten/AufnahmeSeite";
import { schulungsSeite } from "./seiten/SchulungsSeite";

const datei = (id: string, titel = "Aufgenommene Schulung", kategorie = "Agile & Projektmanagement") =>
  JSON.stringify(
    {
      id,
      titel,
      kategorie,
      kurzbeschreibung: "Aus einer Datei aufgenommen.",
      voraussetzungen: [],
      dauerInTagen: 1,
      mindestteilnehmerExklusiv: 4,
      maxTeilnehmerOeffentlich: null,
    },
    null,
    2,
  );

test.describe("Dateien aufnehmen", () => {
  test("eine gültige Datei wird aufgenommen", async ({ page }) => {
    const aufnahme = await aufnahmeSeite(page).oeffnen();

    await aufnahme.waehleDateien([
      { name: "E2E-IMP1.json", inhalt: datei("E2E-IMP1") },
    ]);
    await aufnahme.aufnehmen();

    await expect(aufnahme.eintrag("E2E-IMP1.json")).toContainText("Aufgenommen");

    const schulung = await schulungsSeite(page, "E2E-IMP1").oeffnen();
    await expect(schulung.titel).toHaveText("Aufgenommene Schulung");
  });

  test("gültige und ungültige Dateien im selben Stapel", async ({ page }) => {
    const aufnahme = await aufnahmeSeite(page).oeffnen();

    await aufnahme.waehleDateien([
      { name: "E2E-IMP2.json", inhalt: datei("E2E-IMP2") },
      { name: "kaputt.json", inhalt: "{ das ist kein JSON" },
      { name: "unbekannt.json", inhalt: datei("E2E-IMP3", "Unbekannt", "Robotik") },
    ]);
    await aufnahme.aufnehmen();

    await expect(aufnahme.eintrag("E2E-IMP2.json")).toContainText("Aufgenommen");
    await expect(aufnahme.eintrag("kaputt.json")).toContainText("Abgewiesen");
    // REQ_KAT_IMP_02: Die Rückmeldung nennt den Grund.
    await expect(aufnahme.eintrag("unbekannt.json")).toContainText(
      "Kategorienliste",
    );
  });

  test("eine vergebene Kennung wird erst nach ausdrücklicher Entscheidung ersetzt", async ({
    page,
  }) => {
    const aufnahme = await aufnahmeSeite(page).oeffnen();
    await aufnahme.waehleDateien([
      { name: "E2E-IMP4.json", inhalt: datei("E2E-IMP4", "Erste Fassung") },
    ]);
    await aufnahme.aufnehmen();
    await expect(aufnahme.eintrag("E2E-IMP4.json")).toContainText("Aufgenommen");

    const erneut = await aufnahmeSeite(page).oeffnen();
    await erneut.waehleDateien([
      { name: "E2E-IMP4.json", inhalt: datei("E2E-IMP4", "Zweite Fassung") },
    ]);
    await erneut.aufnehmen();

    await expect(erneut.eintrag("E2E-IMP4.json")).toContainText(
      "Entscheidung offen",
    );
    await expect(
      (await schulungsSeite(page, "E2E-IMP4").oeffnen()).titel,
    ).toHaveText("Erste Fassung");

    const ersetzen = await aufnahmeSeite(page).oeffnen();
    await ersetzen.waehleDateien([
      { name: "E2E-IMP4.json", inhalt: datei("E2E-IMP4", "Zweite Fassung") },
    ]);
    await ersetzen.erlaubeErsetzen();
    await ersetzen.aufnehmen();

    await expect(ersetzen.eintrag("E2E-IMP4.json")).toContainText("Ersetzt");
    await expect(
      (await schulungsSeite(page, "E2E-IMP4").oeffnen()).titel,
    ).toHaveText("Zweite Fassung");
  });

  test("ohne Datei lässt sich nichts aufnehmen", async ({ page }) => {
    const aufnahme = await aufnahmeSeite(page).oeffnen();

    await expect(
      page.getByRole("button", { name: "Aufnehmen" }),
    ).toBeDisabled();
  });
});
