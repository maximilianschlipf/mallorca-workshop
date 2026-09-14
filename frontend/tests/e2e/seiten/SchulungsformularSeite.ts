import type { Locator, Page } from "@playwright/test";
import { Seite } from "./Seite";

export interface Schulungsangaben {
  id?: string;
  titel?: string;
  kategorie?: string;
  kurzbeschreibung?: string;
  voraussetzungen?: string[];
  dauerInTagen?: number;
  mindestteilnehmerExklusiv?: number;
  maxTeilnehmerOeffentlich?: number | "";
}

/** Das Formular zum Anlegen und Bearbeiten einer Schulung. */
export class SchulungsformularSeite extends Seite {
  private readonly schulungId?: string;

  constructor(page: Page, schulungId?: string) {
    super(page);
    this.schulungId = schulungId;
  }

  protected get pfad(): string {
    return this.schulungId ? `/katalog/${this.schulungId}/bearbeiten` : "/katalog/neu";
  }

  protected get kennzeichen(): Locator {
    return this.page.getByRole("heading", {
      name: this.schulungId ? "Schulung bearbeiten" : "Schulung anlegen",
    });
  }

  get kennungsschema(): Locator {
    return this.page.getByTestId("kennungsschema");
  }

  get kennungsfeld(): Locator {
    return this.feld("Kennung");
  }

  /** Die Meldung an einem einzelnen Feld. */
  fehlerAn(feld: string): Locator {
    return this.page.getByTestId(`fehler-${feld}`);
  }

  get warnung(): Locator {
    return this.page.getByTestId("warnung");
  }

  async ausfuellen(angaben: Schulungsangaben): Promise<void> {
    if (angaben.id !== undefined) await this.kennungsfeld.fill(angaben.id);
    if (angaben.titel !== undefined) await this.feld("Titel").fill(angaben.titel);
    if (angaben.kategorie !== undefined) {
      await this.feld("Kategorie").selectOption(angaben.kategorie);
    }
    if (angaben.kurzbeschreibung !== undefined) {
      await this.feld("Kurzbeschreibung").fill(angaben.kurzbeschreibung);
    }
    if (angaben.voraussetzungen !== undefined) {
      await this.feld("Voraussetzungen").fill(angaben.voraussetzungen.join("\n"));
    }
    if (angaben.dauerInTagen !== undefined) {
      await this.feld("Dauer in Tagen").fill(String(angaben.dauerInTagen));
    }
    if (angaben.mindestteilnehmerExklusiv !== undefined) {
      await this.feld("Mindestteilnehmerzahl").fill(
        String(angaben.mindestteilnehmerExklusiv),
      );
    }
    if (angaben.maxTeilnehmerOeffentlich !== undefined) {
      await this.feld("Höchstteilnehmerzahl").fill(
        String(angaben.maxTeilnehmerOeffentlich),
      );
    }
  }

  async speichern(): Promise<void> {
    await this.schalter("Speichern").click();
  }

  async abbrechen(): Promise<void> {
    await this.verweis("Abbrechen").click();
  }

  async wert(feld: string): Promise<string> {
    return this.feld(feld).inputValue();
  }
}

export function schulungsformular(
  page: Page,
  schulungId?: string,
): SchulungsformularSeite {
  return new SchulungsformularSeite(page, schulungId);
}
