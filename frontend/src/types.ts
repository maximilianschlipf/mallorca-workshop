export interface Termin {
  terminId: string;
  startdatum: string;
  enddatum: string;
  ort: string | null;
  format?: string | null;
  status: string;
  trainerId?: string | null;
  trainerName?: string | null;
  assistenten?: string[];
}

export interface TerminDetail extends Omit<Termin, "assistenten"> {
  schulungId: string;
  schulungTitel: string;
  schulungsTage: number;
  zugangsart?: "oeffentlich" | "exklusiv" | null;
  durchfuehrungsart?: "remote" | "vor_ort" | "beim_kunden" | "hybrid" | null;
  kundenfirma?: string | null;
  onlineZugang?: string | null;
  anzahlBuchungen: number;
  abschlussart?: "manuell" | "automatisch" | null;
  abgeschlossenAm?: string | null;
  abgesagtAm?: string | null;
  absagegrund?: string | null;
  assistenten: Array<{ id: string; name: string; platz: number }>;
  teilnehmer: Array<{ id: number; name: string; firma: string | null; bemerkung: string | null; teilnahmestatus: string }>;
  warnungen: string[];
}

export interface TerminEingabe {
  schulungId: string;
  startdatum: string;
  enddatum: string;
  zugangsart: string | null;
  durchfuehrungsart: string | null;
  ort: string | null;
  kundenfirma: string | null;
  onlineZugang: string | null;
  trainerId?: string | null;
  entfernenBestaetigt?: boolean;
}

export interface DashboardTermin {
  terminId: string;
  schulungTitel: string;
  startdatum: string;
  enddatum: string;
  ueberfaellig: boolean;
  ohneTrainer: boolean;
  dringend: boolean;
  mindestteilnehmerUnterschritten: boolean;
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
  email?: string;
  verfuegbar?: boolean;
  grund?: string | null;
  kalender?: Array<{ art: string; von: string; bis: string }>;
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
