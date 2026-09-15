import type { Benutzerkonto, Rolle, Schulung, Trainer } from "./types";

export class ApiError extends Error {
  status: number;
  code: string;

  constructor(status: number, code: string, message: string) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

let csrfToken: string | null = null;

async function api<T>(url: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  if (options.body) headers.set("Content-Type", "application/json");
  if (options.method && options.method !== "GET") {
    if (!csrfToken) {
      const response = await fetch("/api/auth/csrf");
      csrfToken = ((await response.json()) as { token: string }).token;
    }
    headers.set("X-XSRF-TOKEN", csrfToken);
  }
  const response = await fetch(url, { ...options, headers });
  if (!response.ok) {
    const fehler = await response.json().catch(() => ({})) as Partial<ApiError>;
    throw new ApiError(response.status, fehler.code ?? "API_FEHLER",
      fehler.message ?? `API-Fehler: ${response.status}`);
  }
  return response.status === 204 ? undefined as T : await response.json() as T;
}

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
  return api(`/api/schulungen${query ? `?${query}` : ""}`);
}

export async function fetchKategorien(): Promise<string[]> {
  return api("/api/kategorien");
}

export async function fetchVerfuegbareTrainer(
  schulungId: string,
  von: string,
  bis: string,
): Promise<Trainer[]> {
  const params = new URLSearchParams({ schulungId, von, bis });
  return api(`/api/trainer/verfuegbar?${params}`);
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
export const nameAendern = (name: string, aenderungsstand: number) => api<Benutzerkonto>("/api/ich/name", {
  method: "PATCH", headers: { "If-Match": String(aenderungsstand) }, body: JSON.stringify({ name }),
});
export const passwortAendern = (bisherigesPasswort: string, neuesPasswort: string, aenderungsstand: number) =>
  api<Benutzerkonto>("/api/ich/passwort", {
    method: "PUT", headers: { "If-Match": String(aenderungsstand) },
    body: JSON.stringify({ bisherigesPasswort, neuesPasswort }),
  });
export const fetchKonten = () => api<Benutzerkonto[]>("/api/benutzerkonten");
const stand = (aenderungsstand: number) => ({ "If-Match": String(aenderungsstand) });
export const rolleErteilen = (id: string, rolle: Rolle, aenderungsstand: number) =>
  api<void>(`/api/benutzerkonten/${id}/rollen/${rolle}`, { method: "PUT", headers: stand(aenderungsstand) });
export const rolleEntziehen = (id: string, rolle: Rolle, aenderungsstand: number) =>
  api<void>(`/api/benutzerkonten/${id}/rollen/${rolle}`, { method: "DELETE", headers: stand(aenderungsstand) });
export const kontoAktion = (id: string, aktion: "stilllegen" | "reaktivieren" | "eigentuemer", aenderungsstand: number) =>
  api<void>(`/api/benutzerkonten/${id}/${aktion}`, { method: "POST", headers: stand(aenderungsstand) });
export const fremdesPasswortSetzen = (id: string, passwort: string, aenderungsstand: number) =>
  api<void>(`/api/benutzerkonten/${id}/passwort`, {
    method: "PUT", headers: stand(aenderungsstand), body: JSON.stringify({ passwort }),
  });
export const kontoLoeschen = (id: string, aenderungsstand: number) =>
  api<void>(`/api/benutzerkonten/${id}`, { method: "DELETE", headers: stand(aenderungsstand) });
export const aufQualifikationBewerben = (schulungId: string) =>
  api<void>(`/api/ich/qualifikationsbewerbungen/${schulungId}`, { method: "POST" });
export const aufAssistenzplatzBewerben = (terminId: string) =>
  api<void>(`/api/ich/assistenzbewerbungen/${terminId}`, { method: "POST" });
export const abwesenheitEintragen = (von: string, bis: string, grund: string) =>
  api<void>("/api/ich/abwesenheiten", {
    method: "POST", body: JSON.stringify({ von, bis, grund: grund || null }),
  });
export const trainerZuweisen = (terminId: string, trainerId: string) =>
  api<void>(`/api/termine/${terminId}/trainer/${trainerId}`, { method: "PUT" });
export const assistentZuweisen = (terminId: string, trainerId: string) =>
  api<void>(`/api/termine/${terminId}/assistenten/${trainerId}`, { method: "PUT" });
