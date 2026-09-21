import { mount, flushPromises } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import SchulungsAnsicht from "./ansichten/SchulungsAnsicht.vue";
import { aktuellesKonto } from "./auth";
import type { Rolle } from "./types";

const schulung = {
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

function montieren() {
  return mount(SchulungsAnsicht, {
    props: { id: schulung.id },
    global: { stubs: { RouterLink: { template: "<a><slot /></a>" } } },
  });
}

describe("Schulungsansicht", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    aktuellesKonto.value = null;
    vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => ({
      ok: true,
      status: 200,
      json: async () => String(input).endsWith("/qualifikationen") ? [] : schulung,
    } as Response));
  });

  // verifies: TEST_QUA_DIREKT_02
  it("bietet für bereits Qualifizierte keine Direktvergabe an", async () => {
    anmelden(["TRAINER", "ADMINISTRATOR"]);
    vi.mocked(fetch).mockImplementation(async (input) => ({
      ok: true, status: 200,
      json: async () => String(input).endsWith("/qualifikationen") ? [{
        trainerId: "K-2", trainerName: "Qualifiziert", bewerbungId: null,
        status: "QUALIFIZIERT",
      }] : schulung,
    } as Response));
    const wrapper = montieren();
    await flushPromises();

    const zeile = wrapper.find(".verwaltungstabelle tbody tr");
    expect(zeile.text()).toContain("Qualifiziert");
    expect(zeile.text()).toContain("Entziehen");
    expect(zeile.text()).not.toContain("Direkt qualifizieren");
    const aufrufe = vi.mocked(fetch).mock.calls.length;
    await zeile.get("button").trigger("click");
    expect(wrapper.get("[role=dialog]").text()).toContain("Künftige Zuweisungen");
    await wrapper.get("[role=dialog]").findAll("button")[0].trigger("click");
    expect(vi.mocked(fetch).mock.calls).toHaveLength(aufrufe);
  });
});
