import { mount, flushPromises } from "@vue/test-utils";
import type { VueWrapper } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import KatalogAnsicht from "./ansichten/KatalogAnsicht.vue";
import { aktuellesKonto } from "./auth";
import type { Rolle } from "./types";

const aktiv = {
  id: "SCH-001",
  titel: "Scrum Master Zertifizierung",
  kategorie: "Agile",
  kurzbeschreibung: "Praxisnah",
  voraussetzungen: [],
  dauerInTagen: 2,
  mindestteilnehmerExklusiv: 6,
  maxTeilnehmerOeffentlich: 12,
  zustand: "AKTIV",
  oeffentlicheTermine: [],
};

const archiviert = { ...aktiv, id: "SCH-002", titel: "Altes Thema", zustand: "ARCHIVIERT" };

function jsonResponse(data: unknown) {
  return { ok: true, status: 200, json: async () => data } as Response;
}

/** Kategorien immer, Schulungen aus dem Rückgabewert. */
function mockFetch(schulungen: (url: string) => unknown, protokoll: string[] = []) {
  let beworben = false;
  vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
    const url = String(input);
    protokoll.push(url);
    if (url.includes("/api/auth/csrf")) return jsonResponse({ token: "csrf" });
    if (url.includes("/api/kategorien")) return jsonResponse(["Agile", "Cloud & DevOps"]);
    if (url.includes("/api/ich/qualifikationsbewerbungen/")) {
      beworben = true;
      return { ok: true, status: 204, json: async () => ({}) } as Response;
    }
    if (url.includes("/api/ich/qualifikationen")) return jsonResponse(beworben ? [{
      schulungId: "SCH-001", schulungstitel: aktiv.titel, status: "OFFEN",
      begruendung: null, kuenftigeTermine: 0,
    }] : []);
    return jsonResponse(schulungen(url));
  });
}

function anmelden(rollen: Rolle[]) {
  aktuellesKonto.value = {
    id: "K-1",
    email: "person@example.de",
    name: "Person",
    aenderungsstand: 0,
    rollen,
    zustand: "AKTIV",
  };
}

function montieren(): VueWrapper {
  return mount(KatalogAnsicht, { global: { stubs: { RouterLink: true } } });
}

describe("Katalogansicht", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    vi.useRealTimers();
    aktuellesKonto.value = null;
  });

  it("zeigt die Schulungen des Katalogs mit ihrem Zustand", async () => {
    anmelden(["TRAINER"]);
    mockFetch(() => [aktiv, archiviert]);

    const wrapper = montieren();
    await flushPromises();

    expect(wrapper.text()).toContain("Schulungskatalog");
    expect(wrapper.text()).toContain("Scrum Master Zertifizierung");
    expect(wrapper.text()).toContain("Altes Thema");
    // Die Reihenfolge stammt aus dem Backend (TEST_KAT_SICHT_02); hier zaehlt,
    // dass die Ansicht sie ungefiltert uebernimmt und den Zustand ausweist.
    const zeilen = wrapper.findAll("tbody tr");
    expect(zeilen).toHaveLength(2);
    expect(zeilen.map((zeile) => zeile.get(".zustand").text())).toEqual([
      "Aktiv",
      "Archiviert",
    ]);
  });

  it("unterscheidet den leeren Katalog vom leeren Filtertreffer", async () => {
    anmelden(["TRAINER"]);
    mockFetch(() => []);

    const wrapper = montieren();
    await flushPromises();

    expect(wrapper.text()).toContain("Der Katalog enthält noch keine Schulung.");
    expect(wrapper.text()).not.toContain("Keine Schulung entspricht");
  });

  it("lädt die Kategorien in den Filter", async () => {
    anmelden(["TRAINER"]);
    mockFetch(() => [aktiv]);

    const wrapper = montieren();
    await flushPromises();

    const optionen = wrapper.findAll("#verwaltung-kategorie option").map((o) => o.text());
    expect(optionen).toEqual(["Alle Kategorien", "Agile", "Cloud & DevOps"]);
  });

  it("sendet Suchbegriff ohne Leerraum und Kategorie als Abfrageparameter", async () => {
    vi.useFakeTimers();
    anmelden(["TRAINER"]);
    const aufrufe: string[] = [];
    mockFetch(() => [aktiv], aufrufe);

    const wrapper = montieren();
    await vi.runOnlyPendingTimersAsync();

    await wrapper.find("#verwaltung-suche").setValue("  scrum  ");
    await wrapper.find("#verwaltung-kategorie").setValue("Agile");
    await vi.runAllTimersAsync();

    const schulungsAufrufe = aufrufe.filter((url) => url.includes("/api/schulungen"));
    const letzter = schulungsAufrufe[schulungsAufrufe.length - 1];
    expect(letzter).toContain("suche=scrum");
    expect(letzter).toContain("kategorie=Agile");
  });

  it("meldet einen leeren Filtertreffer als eigene Aussage", async () => {
    vi.useFakeTimers();
    anmelden(["TRAINER"]);
    mockFetch((url) => (url.includes("suche=") ? [] : [aktiv]));

    const wrapper = montieren();
    await vi.runOnlyPendingTimersAsync();

    await wrapper.find("#verwaltung-suche").setValue("gibtesnicht");
    await vi.runAllTimersAsync();
    await flushPromises();

    expect(wrapper.text()).toContain("Keine Schulung entspricht den gewählten Filtern");
  });

  // Deckt REQ_KAT_SICHT_03 an der Oberflaeche ab; TEST_KAT_SICHT_03 traegt die Regel im Backend.
  it("bietet die Qualifikationsbewerbung nur bei aktiven Schulungen an", async () => {
    anmelden(["TRAINER"]);
    const aufrufe: string[] = [];
    mockFetch(() => [aktiv, archiviert], aufrufe);

    const wrapper = montieren();
    await flushPromises();

    const zeilen = wrapper.findAll("tbody tr");
    const schalter = zeilen.map((zeile) => zeile.find("button"));
    expect(schalter[1].exists()).toBe(false);

    expect(schalter[0].text()).toBe("Auf Qualifikation bewerben");
    await schalter[0].trigger("click");
    await flushPromises();

    expect(aufrufe).toContain("/api/ich/qualifikationsbewerbungen/SCH-001");
    expect(wrapper.text()).toContain("Die Bewerbung auf die Qualifikation wurde eingereicht.");
    expect(wrapper.findAll("tbody tr")[0].find("button").text()).toBe("Bewerbung zurückziehen");
  });

  it("zeigt für eine bestehende Qualifikation keine Bewerbungsaktion", async () => {
    anmelden(["TRAINER"]);
    vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
      const url = String(input);
      if (url.includes("/api/kategorien")) return jsonResponse([]);
      if (url.includes("/api/ich/qualifikationen")) return jsonResponse([{
        schulungId: "SCH-001", schulungstitel: aktiv.titel, status: "QUALIFIZIERT",
        begruendung: null, kuenftigeTermine: 0,
      }]);
      return jsonResponse([aktiv]);
    });

    const wrapper = montieren();
    await flushPromises();

    expect(wrapper.find("tbody tr").text()).toContain("Qualifiziert");
    expect(wrapper.find("tbody tr").text()).not.toContain("Auf Qualifikation bewerben");
  });

  // Deckt REQ_KAT_SICHT_01 an der Oberflaeche ab; die Schutzschicht ist SecurityConfig.
  it("zeigt Verwaltungsvorgänge nur Administratoren", async () => {
    anmelden(["TRAINER"]);
    mockFetch(() => [aktiv]);

    const trainerAnsicht = montieren();
    await flushPromises();

    expect(trainerAnsicht.find(".verwaltung-aktionen").exists()).toBe(false);
    expect(trainerAnsicht.text()).not.toContain("Archivieren");
    expect(trainerAnsicht.text()).not.toContain("Löschen");

    anmelden(["TRAINER", "ADMINISTRATOR"]);
    const adminAnsicht = montieren();
    await flushPromises();

    expect(adminAnsicht.find(".verwaltung-aktionen").exists()).toBe(true);
    expect(adminAnsicht.text()).toContain("Katalog verwalten");
    expect(adminAnsicht.text()).toContain("Archivieren");
    expect(adminAnsicht.text()).toContain("Löschen");
  });
});
