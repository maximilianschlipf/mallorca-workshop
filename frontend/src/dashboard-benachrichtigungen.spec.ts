import { flushPromises, mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { createMemoryHistory, createRouter } from "vue-router";
import DashboardAnsicht from "./ansichten/DashboardAnsicht.vue";
import BenachrichtigungenAnsicht from "./ansichten/BenachrichtigungenAnsicht.vue";
import { fetchBenachrichtigungen, fetchDashboard, markiereAlleBenachrichtigungenGelesen,
  qualifikationsbewerbungGenehmigen } from "./api";

vi.mock("./api", () => ({
  fetchDashboard: vi.fn(),
  fetchBenachrichtigungen: vi.fn(),
  markiereAlleBenachrichtigungenGelesen: vi.fn(),
  qualifikationsbewerbungGenehmigen: vi.fn(),
  qualifikationsbewerbungAblehnen: vi.fn(),
  qualifikationsbewerbungZurueckziehen: vi.fn(),
}));

const router = createRouter({
  history: createMemoryHistory(),
  routes: ["/", "/katalog/:id", "/planer"].map(path => ({ path, component: { template: "<div />" } })),
});

describe("Dashboard und Benachrichtigungen", () => {
  beforeEach(() => vi.clearAllMocks());

  it("ordnet die drei Dashboard-Ränge und entscheidet einen Vorgang", async () => {
    const vorgang = { id: 4, art: "Freigabeanfrage für eine Qualifikation", antragsteller: "Tara",
      bezug: "Scrum", bezugId: "SCH-001", erstelltAm: "2026-09-19T10:00:00", status: "OFFEN",
      entschiedenAm: null, begruendung: null, richtung: "AN_MICH", entscheidbar: true,
      zurueckziehbar: false } as const;
    vi.mocked(fetchDashboard)
      .mockResolvedValueOnce({ vorgaenge: [vorgang], pflichten: [], dringlichkeiten: [],
        eigeneVorgaenge: [], erledigteVorgaenge: [], erledigteEigeneVorgaenge: [] })
      .mockResolvedValueOnce({ vorgaenge: [], pflichten: [], dringlichkeiten: [],
        eigeneVorgaenge: [], erledigteVorgaenge: [{ ...vorgang, status: "GENEHMIGT" }],
        erledigteEigeneVorgaenge: [] });
    const wrapper = mount(DashboardAnsicht, { attachTo: document.body, global: { plugins: [router] } });
    await flushPromises();

    expect(wrapper.findAll(".dashboard-rank h2").map(h => h.text()).slice(0, 3))
      .toEqual(["Vorgänge", "Handlungspflichten", "Dringlichkeiten"]);
    await wrapper.get("button.primary-action").trigger("click");
    await flushPromises();
    expect(qualifikationsbewerbungGenehmigen).toHaveBeenCalledWith(4);
    expect(wrapper.text()).toContain("Der Vorgang wurde angenommen.");
    expect(document.activeElement).toBe(wrapper.get('[role="status"]').element);
    wrapper.unmount();
  });

  it("zeigt Mitteilungen neueste zuerst mit Lesezustand und Bezug", async () => {
    vi.mocked(fetchBenachrichtigungen).mockResolvedValue([
      { id: 2, anlasstyp: "QUALIFIKATION_GENEHMIGT", anlass: "Genehmigt", bezugArt: "SCHULUNG",
        bezugId: "SCH-001", erstelltAm: "2026-09-19T11:00:00", gelesen: false, bezugVorhanden: true },
      { id: 1, anlasstyp: "TERMIN_GELOESCHT", anlass: "Gelöscht", bezugArt: "TERMIN",
        bezugId: "T-1", erstelltAm: "2026-09-19T10:00:00", gelesen: true, bezugVorhanden: false },
    ]);
    const wrapper = mount(BenachrichtigungenAnsicht, { attachTo: document.body, global: { plugins: [router] } });
    await flushPromises();

    expect(wrapper.findAll(".notification-list li")[0].text()).toContain("Ungelesen");
    expect(wrapper.findAll(".notification-list li")[0].text()).toContain("Genehmigt");
    expect(wrapper.text()).toContain("Gegenstand nicht mehr vorhanden");
    await wrapper.get("button").trigger("click");
    await flushPromises();
    expect(markiereAlleBenachrichtigungenGelesen).toHaveBeenCalledOnce();
    expect(document.activeElement).toBe(wrapper.get('[role="status"]').element);
    wrapper.unmount();
  });
});
