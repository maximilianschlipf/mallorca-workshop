import { beforeEach, describe, expect, it, vi } from "vitest";
import { registrieren, ApiError } from "./api";
import router from "./router";

const trainer = {
  id: "K-1",
  email: "trainer@example.de",
  name: "Trainer",
  aenderungsstand: 0,
  rollen: ["TRAINER"],
  zustand: "AKTIV",
};

describe("Benutzerkonten", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    vi.stubGlobal("scrollTo", vi.fn());
  });

  it("lässt nur Anmelde- und Registrierungsroute öffentlich", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue({
      ok: false,
      status: 401,
      json: async () => ({ code: "ANMELDUNG_ERFORDERLICH", message: "Bitte anmelden." }),
    } as Response);
    for (const path of ["/anmelden", "/registrieren"]) {
      await router.push(path);
      expect(router.currentRoute.value.path).toBe(path);
    }
    for (const path of ["/", "/profil", "/katalog", "/benutzerkonten"]) {
      await router.push(path);
      expect(router.currentRoute.value.path).toBe("/anmelden");
    }

    vi.mocked(fetch).mockResolvedValue({ ok: true, status: 200, json: async () => trainer } as Response);
    await router.push("/katalog");
    expect(router.currentRoute.value.path).toBe("/katalog");
    // Der Katalog steht allen offen, seine Vorgaenge nur Administratoren.
    for (const path of [
      "/katalog/neu",
      "/katalog/aufnahme",
      "/kategorien",
      "/katalog/SCH-001/bearbeiten",
      "/benutzerkonten",
    ]) {
      await router.push(path);
      expect(router.currentRoute.value.path).toBe("/");
    }
  });

  it("sendet Registrierung als CSRF-geschützten JSON-Request", async () => {
    const fetchMock = vi.spyOn(globalThis, "fetch")
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => ({ token: "csrf" }) } as Response)
      .mockResolvedValueOnce({ ok: true, status: 201, json: async () => trainer } as Response);

    await registrieren({ name: "Trainer", email: "trainer@example.de", passwort: "geheim" });

    const [, options] = fetchMock.mock.calls[1];
    expect(fetchMock.mock.calls[1][0]).toBe("/api/auth/registrieren");
    expect(new Headers(options?.headers).get("X-XSRF-TOKEN")).toBe("csrf");
    expect(options?.body).toBe(JSON.stringify({
      name: "Trainer", email: "trainer@example.de", passwort: "geheim",
    }));
  });

  it("übernimmt stabile API-Fehlercodes und Meldungen", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue({
      ok: false,
      status: 409,
      json: async () => ({ code: "EMAIL_VERGEBEN", message: "Bereits vergeben." }),
    } as Response);

    await expect(registrieren({ name: "X", email: "x@example.de", passwort: "x" }))
      .rejects.toEqual(new ApiError(409, "EMAIL_VERGEBEN", "Bereits vergeben."));
  });
});
