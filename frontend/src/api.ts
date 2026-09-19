import type {
  Aufnahmebericht,
  Benachrichtigung,
  Benutzerkonto,
  Feldfehler,
  Katalogantwort,
  Kennungsschema,
  EigenerQualifikationsstand,
  Qualifikationszeile,
  Rolle,
  Schulung,
  Schulungseingabe,
  Trainer,
  TerminDetail,
  TerminEingabe,
  DashboardTermin,
  Dashboard,
  VerwaisterTermin,
} from "./types";

export class ApiError extends Error {
  status: number;
  code: string;

  constructor(status: number, code: string, message: string) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

export class ApiFehler extends Error {
  readonly status: number;
  readonly fehler: Feldfehler[];

  constructor(status: number, fehler: Feldfehler[], meldung: string) {
    super(meldung);
    this.name = "ApiFehler";
    this.status = status;
    this.fehler = fehler;
  }

  zuFeld(feld: string): string | undefined {
    return this.fehler.find((f) => f.feld === feld)?.meldung;
  }

  get codes(): string[] {
    return this.fehler.map((f) => f.code).filter((code): code is string => code !== null);
  }
}

let csrfToken: string | null = null;

async function api<T>(url: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  if (options.body && !(options.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }
  if (options.method && options.method !== "GET") {
    if (!csrfToken) {
      const response = await fetch("/api/auth/csrf");
      csrfToken = ((await response.json()) as { token: string }).token;
    }
    headers.set("X-XSRF-TOKEN", csrfToken);
  }

  const response = await fetch(url, { ...options, headers });
  if (!response.ok) {
    const body = await response.json().catch(() => ({})) as {
      code?: string;
      message?: string;
      fehler?: Feldfehler[];
    };
    if (body.fehler) {
      throw new ApiFehler(
        response.status,
        body.fehler,
        body.fehler.map((fehler) => fehler.meldung).join(" ") || `API-Fehler: ${response.status}`,
      );
    }
    throw new ApiError(
      response.status,
      body.code ?? "API_FEHLER",
      body.message ?? `API-Fehler: ${response.status}`,
    );
  }
  return response.status === 204 ? undefined as T : await response.json() as T;
}

export interface SchulungFilter {
  suche?: string;
  kategorie?: string;
}

export function fetchSchulungen(filter: SchulungFilter = {}): Promise<Schulung[]> {
  const params = new URLSearchParams();
  const suche = filter.suche?.trim();
  const kategorie = filter.kategorie?.trim();
  if (suche) params.set("suche", suche);
  if (kategorie) params.set("kategorie", kategorie);
  const query = params.toString();
  return api(`/api/schulungen${query ? `?${query}` : ""}`);
}

export const fetchSchulung = (id: string) =>
  api<Schulung>(`/api/schulungen/${encodeURIComponent(id)}`);
export const fetchKennungsschema = () => api<Kennungsschema>("/api/schulungen/id-schema");
export const legeSchulungAn = (eingabe: Schulungseingabe) =>
  api<Katalogantwort>("/api/schulungen", { method: "POST", body: JSON.stringify(eingabe) });
export const aendereSchulung = (id: string, eingabe: Schulungseingabe) =>
  api<Katalogantwort>(`/api/schulungen/${encodeURIComponent(id)}`, {
    method: "PUT", body: JSON.stringify(eingabe),
  });
export const archiviereSchulung = (id: string) =>
  api<Katalogantwort>(`/api/schulungen/${encodeURIComponent(id)}/archivierung`, { method: "POST" });
export const reaktiviereSchulung = (id: string) =>
  api<Katalogantwort>(`/api/schulungen/${encodeURIComponent(id)}/archivierung`, { method: "DELETE" });
export const loescheSchulung = (id: string) =>
  api<void>(`/api/schulungen/${encodeURIComponent(id)}`, { method: "DELETE" });

export const fetchKategorien = () => api<string[]>("/api/kategorien");
export const legeKategorieAn = (name: string) =>
  api<string[]>("/api/kategorien", { method: "POST", body: JSON.stringify({ name }) });
export const benenneKategorieUm = (name: string, neuerName: string) =>
  api<string[]>("/api/kategorien", { method: "PUT", body: JSON.stringify({ name, neuerName }) });
export const loescheKategorie = (name: string) =>
  api<void>(`/api/kategorien?name=${encodeURIComponent(name)}`, { method: "DELETE" });
export const fetchVerwaisteTermine = () => api<VerwaisterTermin[]>("/api/katalog/verwaiste-termine");

export async function nimmDateienAuf(dateien: File[], ersetzen: boolean): Promise<Aufnahmebericht> {
  const body = new FormData();
  for (const datei of dateien) body.append("dateien", datei, datei.name);
  return api(`/api/schulungen/aufnahme?ersetzen=${ersetzen}`, { method: "POST", body });
}

export const registrieren = (daten: { name: string; email: string; passwort: string }) =>
  api<Benutzerkonto>("/api/auth/registrieren", { method: "POST", body: JSON.stringify(daten) });
export const anmelden = (email: string, passwort: string) =>
  api<Benutzerkonto>("/api/auth/anmelden", {
    method: "POST", body: JSON.stringify({ email, passwort }),
  });
export const fetchIch = () => api<Benutzerkonto>("/api/auth/ich");
export async function logout() {
  await api<void>("/api/auth/abmelden", { method: "POST" });
  csrfToken = null;
}
export const nameAendern = (name: string, aenderungsstand: number) =>
  api<Benutzerkonto>("/api/ich/name", {
    method: "PATCH", headers: { "If-Match": String(aenderungsstand) }, body: JSON.stringify({ name }),
  });
export const passwortAendern = (
  bisherigesPasswort: string,
  neuesPasswort: string,
  aenderungsstand: number,
) => api<Benutzerkonto>("/api/ich/passwort", {
  method: "PUT",
  headers: { "If-Match": String(aenderungsstand) },
  body: JSON.stringify({ bisherigesPasswort, neuesPasswort }),
});
export const fetchKonten = () => api<Benutzerkonto[]>("/api/benutzerkonten");
const stand = (aenderungsstand: number) => ({ "If-Match": String(aenderungsstand) });
export const rolleErteilen = (id: string, rolle: Rolle, aenderungsstand: number) =>
  api<void>(`/api/benutzerkonten/${id}/rollen/${rolle}`, { method: "PUT", headers: stand(aenderungsstand) });
export const rolleEntziehen = (id: string, rolle: Rolle, aenderungsstand: number) =>
  api<void>(`/api/benutzerkonten/${id}/rollen/${rolle}`, { method: "DELETE", headers: stand(aenderungsstand) });
export const kontoAktion = (
  id: string,
  aktion: "stilllegen" | "reaktivieren" | "eigentuemer",
  aenderungsstand: number,
) => api<void>(`/api/benutzerkonten/${id}/${aktion}`, { method: "POST", headers: stand(aenderungsstand) });
export const fremdesPasswortSetzen = (id: string, passwort: string, aenderungsstand: number) =>
  api<void>(`/api/benutzerkonten/${id}/passwort`, {
    method: "PUT", headers: stand(aenderungsstand), body: JSON.stringify({ passwort }),
  });
export const kontoLoeschen = (id: string, aenderungsstand: number) =>
  api<void>(`/api/benutzerkonten/${id}`, { method: "DELETE", headers: stand(aenderungsstand) });
export const aufQualifikationBewerben = (schulungId: string) =>
  api<void>(`/api/ich/qualifikationsbewerbungen/${schulungId}`, { method: "POST" });
export const qualifikationsbewerbungZurueckziehen = (schulungId: string) =>
  api<void>(`/api/ich/qualifikationsbewerbungen/${schulungId}`, { method: "DELETE" });
export const fetchEigeneQualifikationen = () =>
  api<EigenerQualifikationsstand[]>("/api/ich/qualifikationen");
export const eigeneQualifikationAblegen = (schulungId: string) =>
  api<void>(`/api/ich/qualifikationen/${schulungId}`, { method: "DELETE" });
export const fetchBenachrichtigungen = () =>
  api<Benachrichtigung[]>("/api/ich/benachrichtigungen");
export const fetchUngeleseneBenachrichtigungen = () =>
  api<{ anzahl: number }>("/api/ich/benachrichtigungen/ungelesen");
export const markiereAlleBenachrichtigungenGelesen = () =>
  api<void>("/api/ich/benachrichtigungen/gelesen", { method: "POST" });
export const fetchDashboard = () => api<Dashboard>("/api/dashboard");
export const fetchQualifikationenDerSchulung = (schulungId: string) =>
  api<Qualifikationszeile[]>(`/api/schulungen/${schulungId}/qualifikationen`);
export const qualifikationDirektErteilen = (schulungId: string, trainerId: string) =>
  api<void>(`/api/schulungen/${schulungId}/qualifikationen/${trainerId}`, { method: "PUT" });
export const qualifikationEntziehen = (schulungId: string, trainerId: string) =>
  api<void>(`/api/schulungen/${schulungId}/qualifikationen/${trainerId}`, { method: "DELETE" });
export const qualifikationsbewerbungGenehmigen = (bewerbungId: number) =>
  api<void>(`/api/qualifikationsbewerbungen/${bewerbungId}/genehmigung`, { method: "POST" });
export const qualifikationsbewerbungAblehnen = (bewerbungId: number, begruendung: string) =>
  api<void>(`/api/qualifikationsbewerbungen/${bewerbungId}/ablehnung`, {
    method: "POST", body: JSON.stringify({ begruendung }),
  });
export const aufAssistenzplatzBewerben = (terminId: string) =>
  api<void>(`/api/ich/assistenzbewerbungen/${terminId}`, { method: "POST" });
export const abwesenheitEintragen = (von: string, bis: string, grund: string) =>
  api<void>("/api/ich/abwesenheiten", {
    method: "POST", body: JSON.stringify({ von, bis, grund: grund || null }),
  });
export const trainerZuweisen = (terminId: string, trainerId: string, rollenwechselBestaetigt = false) =>
  api<void>(`/api/termine/${terminId}/trainer/${trainerId}${rollenwechselBestaetigt ? "?rollenwechselBestaetigt=true" : ""}`, { method: "PUT" });
export const assistentZuweisen = (terminId: string, trainerId: string) =>
  api<void>(`/api/termine/${terminId}/assistenten/${trainerId}`, { method: "PUT" });

export const fetchTermin = (terminId: string) =>
  api<TerminDetail>(`/api/termine/${encodeURIComponent(terminId)}`);
export const fetchTerminDashboard = () => api<DashboardTermin[]>("/api/termine/dashboard");
export const schlageEnddatumVor = (schulungId: string, startdatum: string) =>
  api<{ enddatum: string }>(`/api/termine/enddatum-vorschlag?${new URLSearchParams({ schulungId, startdatum })}`);
export const legeTerminAn = (eingabe: TerminEingabe) =>
  api<TerminDetail>("/api/termine", { method: "POST", body: JSON.stringify(eingabe) });
export const aendereTermin = (terminId: string, eingabe: TerminEingabe) =>
  api<TerminDetail>(`/api/termine/${encodeURIComponent(terminId)}`, {
    method: "PUT", body: JSON.stringify(eingabe),
  });
export const bestaetigeTermin = (terminId: string) =>
  api<TerminDetail>(`/api/termine/${encodeURIComponent(terminId)}/bestaetigung`, { method: "POST" });
export const sageTerminAb = (terminId: string, grund: string | null) =>
  api<TerminDetail>(`/api/termine/${encodeURIComponent(terminId)}/absage`, {
    method: "POST", body: JSON.stringify({ grund }),
  });
export const loescheTermin = (terminId: string) =>
  api<void>(`/api/termine/${encodeURIComponent(terminId)}`, { method: "DELETE" });
export const zieheTrainerAb = (terminId: string) =>
  api<void>(`/api/termine/${encodeURIComponent(terminId)}/trainer`, { method: "DELETE" });
export const fetchTrainerOptionen = (terminId: string) =>
  api<Trainer[]>(`/api/termine/${encodeURIComponent(terminId)}/traineroptionen`);
export const fetchTrainerOptionenFuerPlanung = (schulungId: string, startdatum: string, enddatum: string) =>
  api<Trainer[]>(`/api/termine/traineroptionen?${new URLSearchParams({ schulungId, startdatum, enddatum })}`);
