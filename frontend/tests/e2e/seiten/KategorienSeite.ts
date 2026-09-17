import type { Locator, Page } from "@playwright/test";
import { Seite } from "./Seite";

/** Die Pflege der Kategorienliste. */
export class KategorienSeite extends Seite {
  protected get pfad(): string {
    return "/kategorien";
  }

  protected get kennzeichen(): Locator {
    return this.page.getByRole("heading", { name: "Kategorien pflegen" });
  }

  get eintraege(): Locator {
    return this.page.getByTestId("kategorienliste").getByRole("listitem");
  }

  eintrag(name: string): Locator {
    return this.eintraege.filter({ hasText: name });
  }

  async legeAn(name: string): Promise<void> {
    await this.feld("Neue Kategorie").fill(name);
    await this.schalter("Kategorie anlegen").click();
  }

  async benenneUm(name: string, neuerName: string): Promise<void> {
    await this.eintrag(name).getByRole("button", { name: "Umbenennen" }).click();
    await this.feld("Neuer Name").fill(neuerName);
    await this.schalter("Umbenennen bestätigen").click();
  }

  async loesche(name: string): Promise<void> {
    await this.eintrag(name).getByRole("button", { name: "Löschen" }).click();
  }
}

export function kategorienSeite(page: Page): KategorienSeite {
  return new KategorienSeite(page);
}
