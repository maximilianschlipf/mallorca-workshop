import { execFileSync } from "node:child_process";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";

/**
 * Das Katalogverzeichnis, gegen das die E2E-Tests laufen.
 *
 * Es liegt ausserhalb des Projekts in einem eigenen Git-Repository. Der Grund
 * ist nicht Ordnungsliebe: Die Tests legen Schulungen an, aendern und loeschen
 * sie, und jede dieser Aenderungen erzeugt einen Commit (REQ_KAT_ABL_03).
 * Liefe das gegen den echten Katalog, schriebe ein Testlauf Testdaten in die
 * Versionsgeschichte des Projekts.
 */
export const KATALOG_VERZEICHNIS = path.join(
  os.tmpdir(),
  "schulungsplaner-e2e",
  "katalog",
);

const QUELLE = path.resolve(import.meta.dirname, "../../../katalog");

/**
 * Legt eine frische Kopie des Katalogs in einem frischen Repository an.
 *
 * Wird beim Laden der Konfiguration aufgerufen und damit sicher, bevor
 * Playwright das Backend startet. Als globalSetup liefe es je nach Version
 * erst danach -- und das Backend lese den Katalog dann in dem Zustand, den
 * der vorige Lauf hinterlassen hat.
 */
export function katalogZuruecksetzen(): void {
  const wurzel = path.dirname(KATALOG_VERZEICHNIS);
  fs.rmSync(wurzel, { recursive: true, force: true });
  fs.mkdirSync(wurzel, { recursive: true });
  fs.cpSync(QUELLE, KATALOG_VERZEICHNIS, { recursive: true });

  const git = (...argumente: string[]) =>
    execFileSync("git", argumente, { cwd: wurzel, stdio: "ignore" });

  git("init", "--quiet");
  git("add", ".");
  git(
    "-c",
    "user.name=E2E",
    "-c",
    "user.email=e2e@localhost",
    "commit",
    "--quiet",
    "-m",
    "Ausgangsstand des Katalogs für die E2E-Tests",
  );
}

export default katalogZuruecksetzen;
