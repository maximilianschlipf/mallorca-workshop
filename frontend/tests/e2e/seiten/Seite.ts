import type { Page, Locator } from "@playwright/test";
import { eigentuemerAnmelden } from "../anmeldung";

/**
 * Gemeinsame Grundlage aller Seitenobjekte.
 *
 * Ein Seitenobjekt kapselt die Selektoren einer Ansicht und bietet nach aussen
 * fachliche Vorgaenge an. Die Spezifikationen enthalten danach keinen
 * Selektor mehr, sondern nur noch Ablauf und Erwartung -- aendert sich das
 * Markup, ist genau eine Datei zu aendern.
 *
 * Gesucht wird ueber Rollen und Beschriftungen, nicht ueber CSS-Klassen. Ein
 * Test, der ueber die Rolle findet, prueft nebenbei mit, dass die Oberflaeche
 * auch fuer Hilfsmittel bedienbar ist.
 */
export abstract class Seite {
  constructor(protected readonly page: Page) {}

  /** Der Pfad, unter dem diese Ansicht erreichbar ist. */
  protected abstract get pfad(): string;

  /** Ein Element, das genau dann da ist, wenn die Ansicht geladen hat. */
  protected abstract get kennzeichen(): Locator;

  async oeffnen(): Promise<this> {
    await eigentuemerAnmelden(this.page);
    await this.page.goto(this.pfad);
    await this.kennzeichen.waitFor();
    return this;
  }

  /** Wartet darauf, dass die Ansicht steht -- etwa nach einem Klick. */
  async bereit(): Promise<this> {
    await this.kennzeichen.waitFor();
    return this;
  }

  get meldung(): Locator {
    return this.page.getByRole("alert");
  }

  get hinweis(): Locator {
    return this.page.getByRole("status");
  }

  protected feld(beschriftung: string | RegExp): Locator {
    return this.page.getByLabel(beschriftung);
  }

  protected schalter(beschriftung: string | RegExp): Locator {
    return this.page.getByRole("button", { name: beschriftung });
  }

  protected verweis(beschriftung: string | RegExp): Locator {
    return this.page.getByRole("link", { name: beschriftung });
  }
}
