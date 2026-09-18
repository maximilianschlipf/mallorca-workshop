import type { Locator, Page } from "@playwright/test";
import { Seite } from "./Seite";

export class KatalogSeite extends Seite {
  protected get pfad(): string {
    return "/katalog";
  }

  protected get kennzeichen(): Locator {
    return this.page.getByTestId("katalogliste");
  }

  get zeilen(): Locator {
    return this.page
      .getByRole("row")
      .filter({ has: this.page.getByRole("cell") });
  }

  zeile(id: string): Locator {
    return this.page.getByRole("row").filter({ hasText: id });
  }

  get kennungen(): Locator {
    return this.page.locator("[data-kennung]");
  }

  get leermeldung(): Locator {
    return this.page.getByText(
      "Keine Schulung entspricht den gewählten Filtern",
    );
  }

  zustand(id: string): Locator {
    return this.zeile(id).getByRole("cell").nth(3);
  }

  async sucheNach(begriff: string): Promise<void> {
    await this.feld("Titel suchen").fill(begriff);
  }

  async filtereNachKategorie(kategorie: string): Promise<void> {
    await this.feld("Kategorie").selectOption(kategorie);
  }

  async setzeFilterZurueck(): Promise<void> {
    await this.schalter("Filter zurücksetzen").click();
  }

  async zurNeuanlage(): Promise<void> {
    await this.verweis("Schulung anlegen").click();
  }

  async zurAufnahme(): Promise<void> {
    await this.verweis("Dateien aufnehmen").click();
  }

  async zurKategorienpflege(): Promise<void> {
    await this.verweis("Kategorien pflegen").click();
  }

  async oeffne(id: string): Promise<void> {
    await this.zeile(id).getByRole("link", { name: id }).click();
  }

  async archiviere(id: string): Promise<void> {
    await this.zeile(id).getByRole("button", { name: "Archivieren" }).click();
  }

  async reaktiviere(id: string): Promise<void> {
    await this.zeile(id).getByRole("button", { name: "Reaktivieren" }).click();
  }

  /** Loescht und bestaetigt die Rueckfrage. */
  async loesche(id: string): Promise<void> {
    await this.zeile(id).getByRole("button", { name: "Löschen" }).click();
    await this.schalter("Endgültig löschen").click();
  }

  async brichLoeschenAb(id: string): Promise<void> {
    await this.zeile(id).getByRole("button", { name: "Löschen" }).click();
    await this.schalter("Abbrechen").click();
  }

  get rueckfrage(): Locator {
    return this.page.getByRole("dialog");
  }
}

export function katalogSeite(page: Page): KatalogSeite {
  return new KatalogSeite(page);
}
