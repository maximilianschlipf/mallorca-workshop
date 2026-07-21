export interface Termin {
  terminId: string
  startdatum: string
  enddatum: string
  ort: string
  format?: string | null
  status: string
  trainerId?: string | null
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
