export interface Termin {
  terminId: string;
  startdatum: string;
  enddatum: string;
  ort: string;
  format?: string | null;
  status: string;
  trainerId?: string | null;
  trainerName?: string | null;
  assistenten?: string[];
}

export type Schulungszustand = "AKTIV" | "ARCHIVIERT";

export interface Schulung {
  id: string;
  titel: string;
  kategorie: string;
  kurzbeschreibung: string;
  voraussetzungen: string[];
  dauerInTagen: number;
  mindestteilnehmerExklusiv: number;
  maxTeilnehmerOeffentlich?: number | null;
  zustand: Schulungszustand;
  oeffentlicheTermine: Termin[];
}

export interface Schulungseingabe {
  id: string;
  titel: string;
  kategorie: string;
  kurzbeschreibung: string;
  voraussetzungen: string[];
  dauerInTagen: number | null;
  mindestteilnehmerExklusiv: number | null;
  maxTeilnehmerOeffentlich: number | null;
}

export interface Feldfehler {
  feld: string | null;
  code: string | null;
  meldung: string;
}

export interface Warnung {
  code: string;
  meldung: string;
}

export interface Katalogantwort {
  schulung: Schulung;
  warnungen: Warnung[];
}

export interface Kennungsschema {
  muster: string | null;
  beispiele: string[];
}

export type Aufnahmeergebnis =
  | "AUFGENOMMEN"
  | "ERSETZT"
  | "ABGEWIESEN"
  | "ENTSCHEIDUNG_OFFEN";

export interface Aufnahmeeintrag {
  dateiname: string;
  schulungId: string | null;
  ergebnis: Aufnahmeergebnis;
  fehler: Feldfehler[];
}

export interface Aufnahmebericht {
  aufgenommen: number;
  abgewiesen: number;
  entscheidungOffen: number;
  ergebnisse: Aufnahmeeintrag[];
}

export interface VerwaisterTermin {
  terminId: string;
  schulungId: string;
}

export interface Trainer {
  id: string;
  name: string;
  email: string;
}

export type Rolle = "TRAINER" | "ADMINISTRATOR" | "EIGENTUEMER";
export type Kontozustand = "AKTIV" | "STILLGELEGT";

export interface Benutzerkonto {
  id: string;
  email: string;
  name: string;
  aenderungsstand: number;
  rollen: Rolle[];
  zustand: Kontozustand;
}
