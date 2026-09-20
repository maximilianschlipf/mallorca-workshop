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
  vorgangAnnehmen: vi.fn(),
  vorgangAblehnen: vi.fn(),
  vorgangZurueckziehen: vi.fn(),
}));

const router = createRouter({
  history: createMemoryHistory(),
  routes: ["/", "/katalog/:id", "/planer"].map(path => ({ path, component: { template: "<div />" } })),
});

describe("Dashboard und Benachrichtigungen", () => {
  beforeEach(() => {
    vi.useRealTimers();
    vi.clearAllMocks();
  });

  it("ordnet die drei Dashboard-Ränge und entscheidet einen Vorgang", async () => {
    const vorgang = { id: 4, quelle: "QUALIFIKATION", artCode: "QUALIFIKATION",
      art: "Freigabeanfrage für eine Qualifikation", antragsteller: "Tara",
      bezug: "Scrum", bezugArt: "SCHULUNG", bezugId: "SCH-001",
      erstelltAm: "2026-09-19T10:00:00", status: "OFFEN", entschiedenAm: null,
      begruendung: null, entschiedenVon: null, richtung: "AN_MICH", entscheidbar: true,
      zurueckziehbar: false, ablehnungsgrundPflicht: true } as const;
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
    expect(wrapper.findAll(".dashboard-worklist .rank-count").map(count => count.text()))
      .toEqual(["1 offen", "0 offen", "0 offen"]);
    expect(wrapper.findAll(".dashboard-rank")[0].classes()).toContain("is-priority");
    await wrapper.get("button.primary-action").trigger("click");
    await flushPromises();
    expect(qualifikationsbewerbungGenehmigen).toHaveBeenCalledWith(4);
    expect(wrapper.text()).toContain("Der Vorgang wurde angenommen.");
    expect(document.activeElement).toBe(wrapper.get('[role="status"]').element);
    wrapper.unmount();
  });

  it("zeigt alle Vorgangsarten mit Pflichtangaben, Aktionen und Sprungziel", async () => {
    const basis = { antragsteller: "Tara", erstelltAm: "2026-09-19T10:00:00",
      status: "OFFEN", entschiedenAm: null, begruendung: null, entschiedenVon: null,
      richtung: "AN_MICH", entscheidbar: true, zurueckziehbar: false,
      ablehnungsgrundPflicht: false } as const;
    const vorgaenge = [
      { ...basis, id: 1, quelle: "QUALIFIKATION", artCode: "QUALIFIKATION", art: "Freigabeanfrage",
        bezug: "Scrum", bezugArt: "SCHULUNG", bezugId: "SCH-001" },
      { ...basis, id: 2, quelle: "VORGANG", artCode: "ABWESENHEITSANTRAG", art: "Abwesenheitsantrag",
        bezug: "1. bis 2. Oktober", bezugArt: "VORGANG", bezugId: "2", von: "2026-10-01" },
      { ...basis, id: 3, quelle: "VORGANG", artCode: "VORMERKUNG", art: "Vormerkung",
        bezug: "Scrum am 3. Oktober", bezugArt: "TERMIN", bezugId: "T-3" },
      { ...basis, id: 4, quelle: "VORGANG", artCode: "ASSISTENZBEWERBUNG", art: "Assistenzplatz",
        bezug: "Scrum am 4. Oktober", bezugArt: "TERMIN", bezugId: "T-4" },
      { ...basis, id: 5, quelle: "VORGANG", artCode: "UEBERNAHMEANFRAGE", art: "Übernahmeanfrage",
        bezug: "Scrum am 5. Oktober", bezugArt: "TERMIN", bezugId: "T-5" },
      { ...basis, id: 6, quelle: "VORGANG", artCode: "ERSATZTRAINER_ANFRAGE", art: "Ersatztrainer-Anfrage",
        bezug: "6. bis 7. Oktober", bezugArt: "VORGANG", bezugId: "6", von: "2026-10-06" },
    ] as const;
    vi.mocked(fetchDashboard).mockResolvedValue({ vorgaenge: [...vorgaenge], pflichten: [],
      dringlichkeiten: [], eigeneVorgaenge: [], erledigteVorgaenge: [], erledigteEigeneVorgaenge: [] });
    const wrapper = mount(DashboardAnsicht, { global: { plugins: [router] } });
    await flushPromises();

    const zeilen = wrapper.findAll(".dashboard-worklist .task-row");
    const sprungziele = ["/katalog/SCH-001", "/planer?datum=2026-10-01#kalender",
      "/planer?termin=T-3#kalender", "/planer?termin=T-4#kalender",
      "/planer?termin=T-5#kalender", "/planer?datum=2026-10-06#kalender"];
    expect(zeilen).toHaveLength(6);
    for (const [index, vorgang] of vorgaenge.entries()) {
      expect(zeilen[index].text()).toContain(vorgang.art);
      expect(zeilen[index].text()).toContain(`Tara · ${vorgang.bezug}`);
      expect(zeilen[index].text()).toContain("Gestellt");
      expect(zeilen[index].findAll("button").map(button => button.text()))
        .toEqual(["Annehmen", "Ablehnen"]);
      expect(zeilen[index].get("a").attributes("href")).toBe(sprungziele[index]);
    }
  });

  it("trennt gleich nummerierte Vorgänge und verlinkt eigene Gegenstände", async () => {
    const basis = { id: 1, antragsteller: "Tara", bezug: "Scrum", bezugArt: "SCHULUNG",
      bezugId: "SCH-001", erstelltAm: "2026-09-19T10:00:00", status: "OFFEN",
      entschiedenAm: null, begruendung: null, entschiedenVon: null, richtung: "AN_MICH",
      entscheidbar: true, zurueckziehbar: false, ablehnungsgrundPflicht: true } as const;
    const qualifikation = { ...basis, quelle: "QUALIFIKATION", artCode: "QUALIFIKATION",
      art: "Qualifikation" } as const;
    const vormerkung = { ...basis, quelle: "VORGANG", artCode: "VORMERKUNG", art: "Vormerkung",
      bezugArt: "TERMIN", bezugId: "T-1" } as const;
    const zeitraum = { ...vormerkung, id: 2, artCode: "ERSATZTRAINER_ANFRAGE",
      art: "Ersatztrainer-Anfrage", bezugArt: "VORGANG", bezugId: "2026-10-01/2026-10-02",
      von: "2026-10-01", bis: "2026-10-02" } as const;
    vi.mocked(fetchDashboard).mockResolvedValue({ vorgaenge: [qualifikation, vormerkung],
      pflichten: [], dringlichkeiten: [], eigeneVorgaenge: [{ ...vormerkung, richtung: "VON_MIR",
        entscheidbar: false, zurueckziehbar: true }, { ...zeitraum, richtung: "VON_MIR",
        entscheidbar: false, zurueckziehbar: false }], erledigteVorgaenge: [],
      erledigteEigeneVorgaenge: [] });
    const wrapper = mount(DashboardAnsicht, { global: { plugins: [router] } });
    await flushPromises();

    await wrapper.findAll("button.danger-action")[1].trigger("click");

    expect(wrapper.findAll("form.rejection-form")).toHaveLength(1);
    expect(wrapper.get("form.rejection-form textarea").attributes("id")).toBe("grund-VORGANG-1");
    expect(wrapper.find(".own-processes a").attributes("href")).toBe("/planer?termin=T-1#kalender");
    expect(wrapper.findAll(".own-processes a").map(link => link.attributes("href")))
      .toContain("/planer?datum=2026-10-01#kalender");
  });

  it("zeigt Mitteilungen neueste zuerst mit Lesezustand und Bezug", async () => {
    vi.mocked(fetchBenachrichtigungen).mockResolvedValue([
      { id: 2, anlasstyp: "QUALIFIKATION_GENEHMIGT", anlass: "Genehmigt", bezugArt: "SCHULUNG",
        bezugId: "SCH-001", erstelltAm: "2026-09-19T11:00:00", gelesen: false, bezugVorhanden: true },
      { id: 1, anlasstyp: "TERMIN_GELOESCHT", anlass: "Gelöscht", bezugArt: "TERMIN",
        bezugId: "T-1", erstelltAm: "2026-09-19T10:00:00", gelesen: true, bezugVorhanden: false },
      { id: 3, anlasstyp: "TERMIN_GEAENDERT", anlass: "Geändert", bezugArt: "TERMIN",
        bezugId: "T 2", erstelltAm: "2026-09-19T09:00:00", gelesen: true, bezugVorhanden: true },
    ]);
    const wrapper = mount(BenachrichtigungenAnsicht, { attachTo: document.body, global: { plugins: [router] } });
    await flushPromises();

    expect(wrapper.findAll(".notification-state").map(status => status.text()))
      .toEqual(["Gelesen", "Gelesen", "Gelesen"]);
    expect(wrapper.findAll(".notification-list li")[0].text()).toContain("Genehmigt");
    expect(wrapper.text()).toContain("Gegenstand nicht mehr vorhanden");
    expect(wrapper.findAll("a").map(link => link.attributes("href")))
      .toContain("/planer?termin=T%202#kalender");
    await wrapper.get("button").trigger("click");
    await flushPromises();
    expect(markiereAlleBenachrichtigungenGelesen).toHaveBeenCalledTimes(2);
    expect(document.activeElement).toBe(wrapper.get('[role="status"]').element);
    wrapper.unmount();
  });

  it("benennt eine überschrittene Höchstteilnehmerzahl", async () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-09-20T12:00:00"));
    vi.mocked(fetchDashboard).mockResolvedValue({ vorgaenge: [], pflichten: [], eigeneVorgaenge: [],
      erledigteVorgaenge: [], erledigteEigeneVorgaenge: [], dringlichkeiten: [{
        terminId: "SCH-001-T0001", schulungTitel: "Scrum", startdatum: "2026-10-12",
        enddatum: "2026-10-12", ueberfaellig: false, ohneTrainer: true, dringend: true,
        mindestteilnehmerUnterschritten: false, hoechstteilnehmerUeberschritten: true,
      }, {
        terminId: "SCH-001-T0002", schulungTitel: "Scrum später", startdatum: "2026-10-18",
        enddatum: "2026-10-18", ueberfaellig: false, ohneTrainer: true, dringend: false,
        mindestteilnehmerUnterschritten: false, hoechstteilnehmerUeberschritten: false,
      }] });
    const wrapper = mount(DashboardAnsicht, { global: { plugins: [router] } });
    await flushPromises();
    expect(wrapper.text()).toContain("Trainerzuweisung fehlt");
    expect(wrapper.text()).toContain("Höchstteilnehmerzahl überschritten");
    expect(wrapper.text()).toContain("Beginnt in 22 Tagen");
    const dringlichkeiten = wrapper.findAll(".dashboard-worklist .dashboard-rank")[2].findAll(".task-row");
    expect(dringlichkeiten[0].classes()).toContain("urgent");
    expect(dringlichkeiten[0].text()).toContain("Dringend");
    expect(dringlichkeiten[1].classes()).not.toContain("urgent");
    expect(dringlichkeiten[1].text()).not.toContain("Dringend");
  });
});
