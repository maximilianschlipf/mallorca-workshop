import type { Locator, Page } from "@playwright/test";
import { Seite } from "./Seite";

export interface Dateivorlage {
  name: string;
  inhalt: string;
}

/** Die Aufnahme bereitgestellter JSON-Dateien. */
export class AufnahmeSeite extends Seite {
  protected get pfad(): string {
    return "/katalog/aufnahme";
  }

  protected get kennzeichen(): Locator {
    return this.page.getByRole("heading", { name: "Dateien aufnehmen" });
  }

  async waehleDateien(dateien: Dateivorlage[]): Promise<void> {
    await this.feld("JSON-Dateien").setInputFiles(
      dateien.map((datei) => ({
        name: datei.name,
        mimeType: "application/json",
        buffer: Buffer.from(datei.inhalt, "utf-8"),
      })),
    );
  }

  async erlaubeErsetzen(): Promise<void> {
    await this.feld("Bestehende Schulungen ersetzen").check();
  }

  async aufnehmen(): Promise<void> {
    await this.schalter("Aufnehmen").click();
  }

  get bericht(): Locator {
    return this.page.getByTestId("aufnahmebericht");
  }

  eintrag(dateiname: string): Locator {
    return this.bericht.getByRole("listitem").filter({ hasText: dateiname });
  }
}

export function aufnahmeSeite(page: Page): AufnahmeSeite {
  return new AufnahmeSeite(page);
}
