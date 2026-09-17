import type { Locator, Page } from "@playwright/test";
import { Seite } from "./Seite";

/** Die oeffentliche Startseite mit Kalender, Katalog und Trainersuche. */
export class StartSeite extends Seite {
  protected get pfad(): string {
    return "/";
  }

  protected get kennzeichen(): Locator {
    return this.page.getByRole("heading", { name: "Schulungskatalog" });
  }

  override async oeffnen(): Promise<this> {
    await super.oeffnen();
    await this.page.locator(".course-card.skeleton").first().waitFor({ state: "detached" });
    return this;
  }

  // --- Katalog ----------------------------------------------------------

  get schulungskarten(): Locator {
    return this.page.locator(".course-card");
  }

  get trefferzahl(): Locator {
    return this.page.getByText(/Schulung(en)? gefunden/);
  }

  get leermeldung(): Locator {
    return this.page.getByText("Keine Schulungen entsprechen den gewählten Filtern");
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

  titelDerErstenKarte(): Locator {
    return this.schulungskarten.locator("h3").first();
  }

  // --- Kalender ---------------------------------------------------------

  monatsueberschrift(monat: string): Locator {
    return this.page.getByRole("heading", { name: monat });
  }

  termin(titel: string | RegExp): Locator {
    return this.schalter(titel).first();
  }

  get termindetails(): Locator {
    return this.page.locator(".calendar-detail");
  }

  get kalenderAlsRaster(): Locator {
    return this.page.locator(".calendar-desktop");
  }

  get kalenderAlsListe(): Locator {
    return this.page.locator(".calendar-mobile");
  }

  async naechsterMonat(): Promise<void> {
    await this.schalter("Weiter").click();
  }

  async terminWaehlen(titel: string | RegExp): Promise<void> {
    await this.termin(titel).click();
  }
}

export function startSeite(page: Page): StartSeite {
  return new StartSeite(page);
}
