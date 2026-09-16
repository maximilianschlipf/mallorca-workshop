export interface Termin {
  terminId: string
  startdatum: string
  enddatum: string
  ort: string
  format?: string | null
  status: string
  trainerId?: string | null
  trainerName?: string | null
  assistenten?: string[]
}

export interface Schulung {
  id: string
  titel: string
  kategorie: string
  kurzbeschreibung: string
  voraussetzungen: string[]
  dauerInTagen: number
  mindestteilnehmerExklusiv: number
  maxTeilnehmerOeffentlich?: number | null
  oeffentlicheTermine: Termin[]
}

export interface Trainer {
  id: string
  name: string
  email: string
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
