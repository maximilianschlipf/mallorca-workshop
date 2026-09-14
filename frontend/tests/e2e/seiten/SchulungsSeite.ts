import type { Locator, Page } from "@playwright/test";
import { Seite } from "./Seite";

/** Die Einzelansicht einer Schulung. */
export class SchulungsSeite extends Seite {
  private readonly schulungId: string;

  constructor(page: Page, schulungId: string) {
    super(page);
    this.schulungId = schulungId;
  }

  protected get pfad(): string {
    return `/katalog/${this.schulungId}`;
  }

  protected get kennzeichen(): Locator {
    return this.page.getByTestId("schulung-titel");
  }

  get titel(): Locator {
    return this.page.getByTestId("schulung-titel");
  }

  angabe(name: string): Locator {
    return this.page.getByTestId(`angabe-${name}`);
  }

  get voraussetzungen(): Locator {
    return this.page.getByTestId("voraussetzungen").getByRole("listitem");
  }

  get termine(): Locator {
    return this.page.getByTestId("termine").getByRole("listitem");
  }

  get archivkennzeichen(): Locator {
    return this.page.getByTestId("archiviert");
  }

  async bearbeiten(): Promise<void> {
    await this.verweis("Bearbeiten").click();
  }
}

export function schulungsSeite(page: Page, id: string): SchulungsSeite {
  return new SchulungsSeite(page, id);
}
