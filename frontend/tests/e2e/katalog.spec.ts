import { test, expect } from "@playwright/test";
import { katalogSeite } from "./seiten/KatalogSeite";
import { schulungsformular } from "./seiten/SchulungsformularSeite";
import { schulungsSeite } from "./seiten/SchulungsSeite";

/**
 * Die Tests legen eigene Schulungen an, damit sie sich nicht gegenseitig in
 * die Quere kommen. Die geseedeten SCH-00x bleiben unangetastet und dienen
 * als Lesestoff.
 */
const kennung = (name: string) => `E2E-${name}`;

test.describe("Katalog", () => {
  test("die Schnittstelle antwortet und die Liste erscheint", async ({ page }) => {
    const katalog = await katalogSeite(page).oeffnen();

    expect((await page.request.get("/api/schulungen")).status()).toBe(200);
    await expect(katalog.zeilen.first()).toBeVisible();
  });

  test("zeigt die Schulungen des Katalogs mit Zustand", async ({ page }) => {
    const katalog = await katalogSeite(page).oeffnen();

    await expect(katalog.zeile("SCH-001")).toContainText(
      "Scrum Master Zertifizierung",
    );
    await expect(katalog.zustand("SCH-001")).toHaveText("Aktiv");
  });

  test("Suche und Kategoriefilter wirken zusammen", async ({ page }) => {
    const katalog = await katalogSeite(page).oeffnen();
    const gesamt = await katalog.zeilen.count();

    await katalog.sucheNach("kubernetes");
    await katalog.filtereNachKategorie("Cloud & DevOps");

    // Auf die erwartete Zeilenzahl warten, nicht sie ablesen: Die Suche ist
    // entprellt, ein sofortiges Zählen träfe noch die ungefilterte Liste.
    await expect(katalog.zeilen).toHaveCount(1);
    await expect(katalog.zeile("SCH-002")).toBeVisible();
    expect(gesamt).toBeGreaterThan(1);

    await katalog.setzeFilterZurueck();

    await expect(katalog.zeilen).toHaveCount(gesamt);
  });

  test("ohne Treffer erscheint eine eigene Meldung", async ({ page }) => {
    const katalog = await katalogSeite(page).oeffnen();

    await katalog.sucheNach("gibtesnicht123");

    await expect(katalog.leermeldung).toBeVisible();
    await expect(katalog.zeilen).toHaveCount(0);
  });

  test("eine Schulung anlegen, ansehen und bearbeiten", async ({ page }) => {
    const id = kennung("ANL");
    const katalog = await katalogSeite(page).oeffnen();

    await katalog.zurNeuanlage();
    const formular = await schulungsformular(page).bereit();

    // REQ_KAT_ID_01: Das Schema wird gezeigt, die ID aber nicht vorbelegt.
    await expect(formular.kennungsschema).toContainText("AAA-999");
    await expect(formular.kennungsfeld).toHaveValue("");

    await formular.ausfuellen({
      id,
      titel: "End-to-End Grundlagen",
      kategorie: "Agile & Projektmanagement",
      kurzbeschreibung: "Eine im Test angelegte Schulung.",
      voraussetzungen: ["Neugier", "Etwas Geduld"],
      dauerInTagen: 2,
      mindestteilnehmerExklusiv: 6,
      maxTeilnehmerOeffentlich: 12,
    });
    await formular.speichern();

    const schulung = await schulungsSeite(page, id).bereit();
    await expect(schulung.titel).toHaveText("End-to-End Grundlagen");
    await expect(schulung.angabe("kategorie")).toHaveText(
      "Agile & Projektmanagement",
    );
    await expect(schulung.angabe("dauer")).toContainText("2");
    await expect(schulung.voraussetzungen).toHaveText([
      "Neugier",
      "Etwas Geduld",
    ]);

    await schulung.bearbeiten();
    const bearbeiten = await schulungsformular(page, id).bereit();

    // REQ_KAT_PFLEG_03: Die Kennung ist nicht mehr änderbar.
    await expect(bearbeiten.kennungsfeld).toBeDisabled();
    expect(await bearbeiten.wert("Titel")).toBe("End-to-End Grundlagen");

    await bearbeiten.ausfuellen({ titel: "End-to-End Aufbau" });
    await bearbeiten.speichern();

    await expect(
      (await schulungsSeite(page, id).bereit()).titel,
    ).toHaveText("End-to-End Aufbau");
  });

  test("fehlerhafte Eingaben werden am Feld gemeldet", async ({ page }) => {
    const formular = await schulungsformular(page).oeffnen();

    await formular.ausfuellen({
      id: "kleingeschrieben",
      titel: "Unzulässige Kennung",
      kategorie: "Agile & Projektmanagement",
      kurzbeschreibung: "Wird abgewiesen.",
      dauerInTagen: 1,
      mindestteilnehmerExklusiv: 4,
    });
    await formular.speichern();

    await expect(formular.fehlerAn("id")).toContainText("Großbuchstaben");
    // Die Eingaben bleiben stehen, damit nichts erneut getippt werden muss.
    await expect(formular.kennungsfeld).toHaveValue("kleingeschrieben");
  });

  test("eine Höchstzahl unter der Mindestzahl wird abgewiesen", async ({
    page,
  }) => {
    const formular = await schulungsformular(page).oeffnen();

    await formular.ausfuellen({
      id: kennung("GRENZE"),
      titel: "Falsche Grenzen",
      kategorie: "Agile & Projektmanagement",
      kurzbeschreibung: "Wird abgewiesen.",
      dauerInTagen: 1,
      mindestteilnehmerExklusiv: 6,
      maxTeilnehmerOeffentlich: 4,
    });
    await formular.speichern();

    await expect(formular.fehlerAn("maxTeilnehmerOeffentlich")).toContainText(
      "Mindestteilnehmerzahl",
    );
  });

  test("eine vergebene Kennung wird abgewiesen", async ({ page }) => {
    const formular = await schulungsformular(page).oeffnen();

    await formular.ausfuellen({
      id: "SCH-001",
      titel: "Doppelte Kennung",
      kategorie: "Agile & Projektmanagement",
      kurzbeschreibung: "Wird abgewiesen.",
      dauerInTagen: 1,
      mindestteilnehmerExklusiv: 4,
    });
    await formular.speichern();

    await expect(formular.fehlerAn("id")).toContainText("bereits vergeben");
  });

  test("archivieren und reaktivieren", async ({ page }) => {
    const id = kennung("ARCH");
    await lege(page, id, "Zum Archivieren");

    const katalog = await katalogSeite(page).oeffnen();
    await katalog.archiviere(id);

    await expect(katalog.zustand(id)).toHaveText("Archiviert");

    await katalog.reaktiviere(id);

    await expect(katalog.zustand(id)).toHaveText("Aktiv");
  });

  test("archivierte Schulungen stehen hinter den aktiven", async ({ page }) => {
    const id = kennung("SORT");
    await lege(page, id, "Zum Einsortieren");

    const katalog = await katalogSeite(page).oeffnen();
    await katalog.archiviere(id);
    await expect(katalog.zustand(id)).toHaveText("Archiviert");

    const kennungen = await katalog.kennungen.allInnerTexts();
    expect(kennungen.at(-1)).toBe(id);
  });

  test("löschen fragt zurück und lässt sich abbrechen", async ({ page }) => {
    const id = kennung("LOE");
    await lege(page, id, "Zum Löschen");

    const katalog = await katalogSeite(page).oeffnen();

    await katalog.brichLoeschenAb(id);
    await expect(katalog.rueckfrage).toBeHidden();
    await expect(katalog.zeile(id)).toBeVisible();

    await katalog.loesche(id);
    await expect(katalog.zeile(id)).toHaveCount(0);
  });

  test("eine Schulung mit Terminen lässt sich nicht löschen", async ({
    page,
  }) => {
    const katalog = await katalogSeite(page).oeffnen();

    await katalog.loesche("SCH-001");

    await expect(katalog.meldung).toContainText("Termine");
    await expect(katalog.zeile("SCH-001")).toBeVisible();
  });

  test("eine geänderte Dauer warnt, wenn Termine bestehen", async ({ page }) => {
    const formular = await schulungsformular(page, "SCH-002").oeffnen();
    const vorher = await formular.wert("Dauer in Tagen");

    await formular.ausfuellen({ dauerInTagen: Number(vorher) + 1 });
    await formular.speichern();

    await expect(formular.warnung).toContainText("Termine");

    // Wieder zurückdrehen, damit der Katalog bleibt, wie er war.
    const zurueck = await schulungsformular(page, "SCH-002").oeffnen();
    await zurueck.ausfuellen({ dauerInTagen: Number(vorher) });
    await zurueck.speichern();
  });
});

async function lege(page: import("@playwright/test").Page, id: string, titel: string) {
  const formular = await schulungsformular(page).oeffnen();
  await formular.ausfuellen({
    id,
    titel,
    kategorie: "Agile & Projektmanagement",
    kurzbeschreibung: `Angelegt für ${titel}.`,
    dauerInTagen: 1,
    mindestteilnehmerExklusiv: 4,
  });
  await formular.speichern();
  await schulungsSeite(page, id).bereit();
}
