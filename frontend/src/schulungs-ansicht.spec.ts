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
    vi.spyOn(globalThis, "fetch").mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => schulung,
    } as Response);
  });

  // REQ_KAT_SICHT_01: Der Trainer sieht den Katalog, veraendert ihn aber nicht.
  it("bietet die Bearbeitung nur Administratoren an", async () => {
    anmelden(["TRAINER"]);
    const trainerAnsicht = montieren();
    await flushPromises();

    expect(trainerAnsicht.text()).toContain("Scrum Master Zertifizierung");
    expect(trainerAnsicht.text()).toContain("Zurück zum Katalog");
    expect(trainerAnsicht.text()).not.toContain("Bearbeiten");

    anmelden(["TRAINER", "ADMINISTRATOR"]);
    const adminAnsicht = montieren();
    await flushPromises();

    expect(adminAnsicht.text()).toContain("Bearbeiten");
  });
});
