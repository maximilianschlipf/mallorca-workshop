import { flushPromises, mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import GruppenAnsicht from "./ansichten/GruppenAnsicht.vue";
import { aktuellesKonto } from "./auth";

const json = (data: unknown, status = 200) =>
  ({ ok: status < 400, status, json: async () => data }) as Response;

const mallorca = {
  id: "G-1", name: "Mallorca", anzahlTermine: 2,
  trainer: { id: "T-1", name: "Trener", email: "trener@mailinator.com" },
  mitglieder: [
    { id: "M-1", name: "Anna", email: "anna@mailinator.com" },
    { id: "M-2", name: "Piotr", email: "piotr@mailinator.com" },
  ],
};

const konten = [
  { id: "T-1", name: "Trener", email: "trener@mailinator.com", zustand: "AKTIV", rollen: ["TRAINER"], aenderungsstand: 0 },
  { id: "M-1", name: "Anna", email: "anna@mailinator.com", zustand: "AKTIV", rollen: ["TRAINER"], aenderungsstand: 0 },
  { id: "M-2", name: "Piotr", email: "piotr@mailinator.com", zustand: "AKTIV", rollen: ["TRAINER"], aenderungsstand: 0 },
  { id: "M-3", name: "Ewa", email: "ewa@mailinator.com", zustand: "AKTIV", rollen: ["TRAINER"], aenderungsstand: 0 },
];

describe("Gruppen", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    aktuellesKonto.value = null;
  });

  // verifies: TEST_GRP_UI_01
  it("zeigt Gruppe, Gruppentrainer und alle Mitglieder", async () => {
    aktuellesKonto.value = {
      id: "M-1", email: "anna@mailinator.com", name: "Anna", aenderungsstand: 0,
      rollen: ["TRAINER"], zustand: "AKTIV",
    };
    vi.spyOn(globalThis, "fetch").mockImplementation(async () => json([mallorca]));

    const wrapper = mount(GruppenAnsicht);
    await flushPromises();

    const text = wrapper.text();
    expect(text).toContain("Mallorca");
    expect(text).toContain("Gruppentrainer: Trener");
    expect(text).toContain("Mitglieder (2)");
    expect(text).toContain("Anna");
    expect(text).toContain("Piotr");
    expect(wrapper.find("form").exists()).toBe(false);
    expect(wrapper.text()).not.toContain("Gruppe löschen");
  });

  // verifies: TEST_GRP_UI_02
  it("lässt Administratoren Mitglieder hinzufügen und entfernen", async () => {
    aktuellesKonto.value = {
      id: "A-1", email: "admin@example.de", name: "Admin", aenderungsstand: 0,
      rollen: ["ADMINISTRATOR"], zustand: "AKTIV",
    };
    const aufrufe: string[] = [];
    vi.spyOn(globalThis, "fetch").mockImplementation(async (input, init) => {
      const url = String(input);
      const methode = init?.method ?? "GET";
      if (url.includes("/api/auth/csrf")) return json({ token: "csrf" });
      if (url === "/api/gruppen" && methode === "GET") return json([mallorca]);
      if (url === "/api/benutzerkonten") return json(konten);
      aufrufe.push(`${methode} ${url}`);
      return json(mallorca);
    });

    const wrapper = mount(GruppenAnsicht);
    await flushPromises();

    const aktionen = wrapper.get('[aria-label="Aktionen für Mallorca"]');
    const kandidaten = aktionen.findAll("select")[1];
    expect(kandidaten.text()).toContain("Ewa");
    expect(kandidaten.text()).not.toContain("Anna");
    await kandidaten.setValue("M-3");
    await flushPromises();
    await wrapper.get('button[aria-label="Piotr aus Mallorca entfernen"]').trigger("click");
    await flushPromises();

    expect(aufrufe).toContain("PUT /api/gruppen/G-1/mitglieder/M-3");
    expect(aufrufe).toContain("DELETE /api/gruppen/G-1/mitglieder/M-2");
  });
});
