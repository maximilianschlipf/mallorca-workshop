import type { Schulung, Trainer } from "./types";

export interface SchulungFilter {
  suche?: string;
  kategorie?: string;
}

export async function fetchSchulungen(
  filter: SchulungFilter = {},
): Promise<Schulung[]> {
  const params = new URLSearchParams();
  const suche = filter.suche?.trim();
  const kategorie = filter.kategorie?.trim();
  if (suche) {
    params.set("suche", suche);
  }
  if (kategorie) {
    params.set("kategorie", kategorie);
  }

  const query = params.toString();
  const response = await fetch(`/api/schulungen${query ? `?${query}` : ""}`);
  if (!response.ok) {
    throw new Error(`API-Fehler: ${response.status}`);
  }
  return (await response.json()) as Schulung[];
}

export async function fetchKategorien(): Promise<string[]> {
  const response = await fetch("/api/kategorien");
  if (!response.ok) {
    throw new Error(`API-Fehler: ${response.status}`);
  }
  return (await response.json()) as string[];
}

export async function fetchVerfuegbareTrainer(
  schulungId: string,
  von: string,
  bis: string,
): Promise<Trainer[]> {
  const params = new URLSearchParams({ schulungId, von, bis });
  const response = await fetch(`/api/trainer/verfuegbar?${params}`);
  if (!response.ok) {
    throw new Error(
      response.status === 400
        ? "Der gewählte Zeitraum ist ungültig."
        : `API-Fehler: ${response.status}`,
    );
  }
  return (await response.json()) as Trainer[];
}
