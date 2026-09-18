import type { Locator, Page } from "@playwright/test";
import { Seite } from "./Seite";

export class StartSeite extends Seite {
  protected get pfad(): string {
    return "/";
  }

  protected get kennzeichen(): Locator {
    return this.page.getByRole("heading", { name: "Schulungskalender" });
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

  async termindetailsSchliessen(): Promise<void> {
    await this.schalter("Termindetails schließen").click();
  }
}

export function startSeite(page: Page): StartSeite {
  return new StartSeite(page);
}
