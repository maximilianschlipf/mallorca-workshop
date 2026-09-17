import { test, expect } from "@playwright/test";
import { kategorienSeite } from "./seiten/KategorienSeite";
import { katalogSeite } from "./seiten/KatalogSeite";
import { schulungsformular } from "./seiten/SchulungsformularSeite";
import { schulungsSeite } from "./seiten/SchulungsSeite";

test.describe("Kategorien pflegen", () => {
  test("zeigt die gepflegte Liste alphabetisch", async ({ page }) => {
    const kategorien = await kategorienSeite(page).oeffnen();

    const namen = await kategorien.eintraege.allInnerTexts();
    const nurNamen = namen.map((zeile) => zeile.split("\n")[0].trim());
    expect(nurNamen).toEqual([...nurNamen].sort((a, b) => a.localeCompare(b)));
    await expect(kategorien.eintrag("Cloud & DevOps")).toBeVisible();
  });

  test("eine neue Kategorie anlegen und wieder löschen", async ({ page }) => {
    const kategorien = await kategorienSeite(page).oeffnen();

    await kategorien.legeAn("Datenbanken");
    await expect(kategorien.eintrag("Datenbanken")).toBeVisible();

    // Sie steht danach im Formular zur Auswahl (REQ_KAT_KATG_03).
    const formular = await schulungsformular(page).oeffnen();
    await formular.ausfuellen({ kategorie: "Datenbanken" });

    const zurueck = await kategorienSeite(page).oeffnen();
    await zurueck.loesche("Datenbanken");
    await expect(zurueck.eintrag("Datenbanken")).toHaveCount(0);
  });

  test("eine bereits vorhandene Kategorie wird abgewiesen", async ({ page }) => {
    const kategorien = await kategorienSeite(page).oeffnen();

    await kategorien.legeAn("Cloud & DevOps");

    await expect(kategorien.meldung).toContainText("gibt es bereits");
    await expect(kategorien.eintrag("Cloud & DevOps")).toHaveCount(1);
  });

  test("umbenennen wirkt auf alle zugeordneten Schulungen", async ({ page }) => {
    const id = "E2E-KATG";
    const formular = await schulungsformular(page).oeffnen();
    await formular.ausfuellen({
      id,
      titel: "Zur Kategorie",
      kategorie: "IT-Security",
      kurzbeschreibung: "Hängt an der umzubenennenden Kategorie.",
      dauerInTagen: 1,
      mindestteilnehmerExklusiv: 4,
    });
    await formular.speichern();
    await schulungsSeite(page, id).bereit();

    const kategorien = await kategorienSeite(page).oeffnen();
    await kategorien.benenneUm("IT-Security", "Informationssicherheit");

    await expect(kategorien.eintrag("Informationssicherheit")).toBeVisible();
    await expect(kategorien.eintrag("IT-Security")).toHaveCount(0);

    const schulung = await schulungsSeite(page, id).oeffnen();
    await expect(schulung.angabe("kategorie")).toHaveText("Informationssicherheit");

    // Und der Filter findet sie unter dem neuen Namen.
    const katalog = await katalogSeite(page).oeffnen();
    await katalog.filtereNachKategorie("Informationssicherheit");
    await expect(katalog.zeile(id)).toBeVisible();

    await kategorienSeite(page).oeffnen();
    await kategorien.benenneUm("Informationssicherheit", "IT-Security");
  });

  test("eine Kategorie in Gebrauch lässt sich nicht löschen", async ({ page }) => {
    const kategorien = await kategorienSeite(page).oeffnen();

    await kategorien.loesche("Cloud & DevOps");

    await expect(kategorien.meldung).toContainText("zugeordnet");
    await expect(kategorien.eintrag("Cloud & DevOps")).toBeVisible();
  });
});
