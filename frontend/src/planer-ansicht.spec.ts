import { mount, flushPromises } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import PlanerAnsicht from "./ansichten/PlanerAnsicht.vue";
import { aktuellesKonto } from "./auth";

type FetchResponse = { ok: boolean; json: () => Promise<unknown> };

const schulung = {
  id: "SCH-001",
  titel: "Scrum Master Zertifizierung",
  kategorie: "Agile",
  kurzbeschreibung: "Praxisnah – ohne typografische Sonderstriche",
  voraussetzungen: [],
  dauerInTagen: 2,
  mindestteilnehmerExklusiv: 6,
  maxTeilnehmerOeffentlich: 12,
  oeffentlicheTermine: [],
};

const kalenderSchulung = {
  ...schulung,
  oeffentlicheTermine: [
    {
      terminId: "SCH-001-T1",
      startdatum: "2026-08-28",
      enddatum: "2026-09-01",
      ort: "Köln",
      format: "Präsenz",
      status: "geplant",
      trainerId: "TRN-001",
    },
    {
      terminId: "SCH-001-T2",
      startdatum: "2026-08-31",
      enddatum: "2026-08-31",
      ort: "Online",
      format: "Online",
      status: "abgeschlossen",
      trainerId: "TRN-001",
    },
    {
      terminId: "SCH-001-T3",
      startdatum: "2026-08-31",
      enddatum: "2026-08-31",
      ort: "Berlin",
      format: "Präsenz",
      status: "abgesagt",
      trainerId: null,
    },
  ],
};

function jsonResponse(data: unknown): FetchResponse {
  return { ok: true, json: async () => data };
}

function mockFetch(handler: (url: string) => unknown) {
  return vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
    const url = String(input);
    return jsonResponse(handler(url)) as Response;
  });
}

describe("Planer", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    vi.useRealTimers();
    window.history.replaceState({}, "", "/");
    aktuellesKonto.value = null;
  });

  it("opens the month selected by a dashboard period link", async () => {
    window.history.replaceState({}, "", "/planer?datum=2026-10-17#kalender");
    mockFetch(() => [kalenderSchulung]);

    const wrapper = mount(PlanerAnsicht);
    await flushPromises();

    expect(wrapper.find(".calendar-toolbar h3").text()).toBe("Oktober 2026");
  });

  it("renders a Monday-first calendar and navigates between months", async () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2026, 7, 26));
    mockFetch(() => [kalenderSchulung]);

    const wrapper = mount(PlanerAnsicht);
    await flushPromises();

    expect(wrapper.find(".calendar-toolbar h3").text()).toBe("August 2026");
    expect(wrapper.findAll(".calendar-weekday")[0].text()).toBe("Montag");

    await wrapper
      .findAll(".calendar-actions button")
      .find((button) => button.text() === "Weiter")!
      .trigger("click");

    expect(wrapper.find(".calendar-toolbar h3").text()).toBe("September 2026");
    expect(wrapper.find(".calendar-event").text()).toContain("Scrum Master");
  });

  it("shows multi-day events, status variants and selected details", async () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2026, 7, 26));
    mockFetch(() => [kalenderSchulung]);

    const wrapper = mount(PlanerAnsicht);
    await flushPromises();

    expect(wrapper.findAll(".calendar-event.status-geplant")).toHaveLength(3);
    expect(wrapper.find(".calendar-event.status-abgeschlossen").text()).toContain(
      "Abgeschlossen",
    );
    expect(wrapper.find(".calendar-event.status-abgesagt").text()).toContain(
      "Abgesagt",
    );

    const kalenderTermin = wrapper.find(".calendar-event.status-geplant");
    await kalenderTermin.trigger("click");
    expect(wrapper.get('[role="dialog"].calendar-detail').attributes("aria-modal")).toBe("true");
    expect(wrapper.find(".calendar-detail").text()).toContain(
      "28.08.2026 - 01.09.2026",
    );
    expect(wrapper.find(".calendar-detail").text()).toContain("Köln");
    expect(wrapper.find(".calendar-detail").text()).toContain("Geplant");
    await wrapper.get('button[aria-label="Termindetails schließen"]').trigger("click");
    await wrapper.vm.$nextTick();
    expect(wrapper.find(".calendar-detail").exists()).toBe(false);
  });

  it("ignores a late detail response for a previously selected appointment", async () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2026, 7, 26));
    const pending = new Map<string, (response: Response) => void>();
    vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
      const url = String(input);
      if (url.includes("/api/termine/dashboard")) return Promise.resolve(jsonResponse([]) as Response);
      if (url.startsWith("/api/termine/")) {
        return new Promise((resolve) => pending.set(url, resolve));
      }
      return Promise.resolve(jsonResponse([kalenderSchulung]) as Response);
    });

    const wrapper = mount(PlanerAnsicht);
    await flushPromises();
    await wrapper.get(".calendar-event.status-geplant").trigger("click");
    await wrapper.get(".calendar-event.status-abgeschlossen").trigger("click");
    pending.get("/api/termine/SCH-001-T2")!(jsonResponse({
      ...kalenderSchulung.oeffentlicheTermine[1], schulungId: "SCH-001",
      schulungTitel: schulung.titel, schulungsTage: 22, anzahlBuchungen: 0,
      assistenten: [], teilnehmer: [], warnungen: [],
    }) as Response);
    await flushPromises();
    pending.get("/api/termine/SCH-001-T1")!(jsonResponse({
      ...kalenderSchulung.oeffentlicheTermine[0], schulungId: "SCH-001",
      schulungTitel: schulung.titel, schulungsTage: 11, anzahlBuchungen: 0,
      assistenten: [], teilnehmer: [], warnungen: [],
    }) as Response);
    await flushPromises();

    expect(wrapper.find(".calendar-detail").text()).toContain("22");
    expect(wrapper.find(".calendar-detail").text()).not.toContain("11");
  });

  it("creates an appointment with an available trainer and clears hidden dependent fields", async () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2030, 0, 1));
    aktuellesKonto.value = {
      id: "ADMIN-1", email: "admin@example.de", name: "Admin", aenderungsstand: 0,
      rollen: ["ADMINISTRATOR"], zustand: "AKTIV",
    };
    let payload: Record<string, unknown> | undefined;
    vi.spyOn(globalThis, "fetch").mockImplementation(async (input, init) => {
      const url = String(input);
      if (url.includes("/api/auth/csrf")) return jsonResponse({ token: "csrf" }) as Response;
      if (url.includes("/api/termine/dashboard")) return jsonResponse([]) as Response;
      if (url.includes("/api/termine/enddatum-vorschlag")) return jsonResponse({ enddatum: "2030-02-04" }) as Response;
      if (url.includes("/api/termine/traineroptionen")) {
        return jsonResponse([
          { id: "TRN-005", name: "Elena Fischer", email: "elena@example.de", verfuegbar: true, kalender: [] },
          { id: "TRN-006", name: "Belegte Person", verfuegbar: false,
            grund: "bereits einem anderen Termin zugewiesen",
            kalender: [{ art: "zugewiesen", von: "2030-02-03", bis: "2030-02-04" }] },
        ]) as Response;
      }
      if (url === "/api/termine" && init?.method === "POST") {
        payload = JSON.parse(String(init.body));
        return jsonResponse({
          terminId: "SCH-001-T00001", schulungId: "SCH-001", schulungTitel: schulung.titel,
          startdatum: "2030-02-03", enddatum: "2030-02-04", status: "geplant",
          schulungsTage: 2, anzahlBuchungen: 0, assistenten: [], teilnehmer: [], warnungen: [],
        }) as Response;
      }
      return jsonResponse([schulung]) as Response;
    });

    const wrapper = mount(PlanerAnsicht, { attachTo: document.body });
    await flushPromises();
    await wrapper.get("button.primary-action").trigger("click");
    await flushPromises();
    const dialog = wrapper.get("[role=dialog]");
    expect(document.activeElement).toBe(dialog.find("select").element);

    const selects = dialog.findAll("select");
    await selects[0].setValue("SCH-001");
    await dialog.get('input[type="date"]').setValue("2030-02-03");
    await flushPromises();
    expect(selects[1].text()).toContain("Elena Fischer");
    expect(selects[1].find('option[value="TRN-006"]').attributes("disabled")).toBeDefined();
    expect(dialog.findAll(".trainer-calendar").some((kalender) => kalender.text().includes("zugewiesen"))).toBe(true);
    await selects[1].setValue("TRN-005");
    await dialog.findAll('input[type="date"]')[1].setValue("2030-02-05");
    await flushPromises();
    expect((selects[1].element as HTMLSelectElement).selectedIndex).toBe(0);
    await selects[1].setValue("TRN-005");
    await selects[2].setValue("exklusiv");
    await dialog.get('input[type="text"]').setValue("Veraltete Firma");
    await selects[3].setValue("vor_ort");
    await dialog.get('input[type="text"]').setValue("Veralteter Ort");
    await selects[2].setValue("oeffentlich");
    await selects[3].setValue("remote");
    await dialog.get('input[type="url"]').setValue("https://example.org/raum");
    await dialog.get("form").trigger("submit");
    await flushPromises();

    expect(payload).toMatchObject({
      trainerId: "TRN-005", zugangsart: "oeffentlich", durchfuehrungsart: "remote",
      kundenfirma: null, ort: null, onlineZugang: "https://example.org/raum",
    });
    wrapper.unmount();
  });

  it("keeps the latest end-date suggestion when form requests finish out of order", async () => {
    aktuellesKonto.value = {
      id: "ADMIN-1", email: "admin@example.de", name: "Admin", aenderungsstand: 0,
      rollen: ["ADMINISTRATOR"], zustand: "AKTIV",
    };
    const pending = new Map<string, (response: Response) => void>();
    vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
      const url = String(input);
      if (url.includes("/api/termine/dashboard")) return Promise.resolve(jsonResponse([]) as Response);
      if (url.includes("/api/termine/enddatum-vorschlag")) {
        return new Promise((resolve) => pending.set(url, resolve));
      }
      if (url.includes("/api/termine/traineroptionen")) return Promise.resolve(jsonResponse([]) as Response);
      return Promise.resolve(jsonResponse([schulung]) as Response);
    });

    const wrapper = mount(PlanerAnsicht);
    await flushPromises();
    await wrapper.get("button.primary-action").trigger("click");
    const dialog = wrapper.get("[role=dialog]");
    await dialog.findAll("select")[0].setValue("SCH-001");
    const start = dialog.get('input[type="date"]');
    await start.setValue("2030-03-04");
    await start.setValue("2030-03-11");
    const neu = [...pending.keys()].find((url) => url.includes("2030-03-11"))!;
    const alt = [...pending.keys()].find((url) => url.includes("2030-03-04"))!;
    pending.get(neu)!(jsonResponse({ enddatum: "2030-03-12" }) as Response);
    await flushPromises();
    pending.get(alt)!(jsonResponse({ enddatum: "2030-03-05" }) as Response);
    await flushPromises();

    expect((dialog.findAll('input[type="date"]')[1].element as HTMLInputElement).value).toBe("2030-03-12");
  });

  it("bietet Bewerbungen und eine qualifikationsgeprüfte Trainerzuweisung an", async () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2030, 1, 1));
    aktuellesKonto.value = {
      id: "ADMIN-1",
      email: "admin@example.de",
      name: "Admin Trainer",
      aenderungsstand: 0,
      rollen: ["TRAINER", "ADMINISTRATOR"],
      zustand: "AKTIV",
    };
    const zukunft = {
      ...schulung,
      oeffentlicheTermine: [{
        terminId: "SCH-001-ZUKUNFT",
        startdatum: "2030-02-10",
        enddatum: "2030-02-11",
        ort: "Köln",
        format: "Präsenz",
        status: "geplant",
        trainerId: null,
      }],
    };
    const aufrufe: string[] = [];
    vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
      const url = String(input);
      aufrufe.push(url);
      if (url.includes("/api/auth/csrf")) return jsonResponse({ token: "csrf" }) as Response;
      if (url.includes("/traineroptionen")) {
        return jsonResponse([{ id: "TRN-1", name: "Qualifizierte Person", email: "q@example.de" }]) as Response;
      }
      if (url === "/api/termine/SCH-001-ZUKUNFT") {
        return jsonResponse({
          ...zukunft.oeffentlicheTermine[0], schulungId: "SCH-001",
          schulungTitel: schulung.titel, schulungsTage: 2, anzahlBuchungen: 0,
          assistenten: [], teilnehmer: [], warnungen: [],
        }) as Response;
      }
      return jsonResponse(url.includes("/api/schulungen") ? [zukunft] : {}) as Response;
    });

    const wrapper = mount(PlanerAnsicht);
    await flushPromises();
    await wrapper.get(".calendar-event").trigger("click");
    await flushPromises();
    await wrapper.get(".calendar-detail-actions .filter-reset").trigger("click");
    await wrapper.get(".calendar-detail-actions .primary-action").trigger("click");
    await flushPromises();
    await wrapper.vm.$nextTick();
    expect(wrapper.get(".calendar-detail .trainer-row-actions button").text()).toBe("Diesem Termin zuweisen");
    expect(aufrufe).toContain("/api/termine/SCH-001-ZUKUNFT/traineroptionen");
    await wrapper.get(".calendar-detail .trainer-row-actions button").trigger("click");
    await flushPromises();

    expect(aufrufe).toContain("/api/ich/assistenzbewerbungen/SCH-001-ZUKUNFT");
    expect(aufrufe).toContain("/api/termine/SCH-001-ZUKUNFT/trainer/TRN-1");
  });

  it("verwirft verspätete Traineroptionen nach erneutem Öffnen desselben Termins", async () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2030, 1, 1));
    aktuellesKonto.value = {
      id: "ADMIN-1", email: "admin@example.de", name: "Admin",
      aenderungsstand: 0, rollen: ["ADMINISTRATOR"], zustand: "AKTIV",
    };
    const termine = [
      { terminId: "TERMIN-A", startdatum: "2030-02-11", enddatum: "2030-02-11", status: "geplant", trainerId: null },
    ];
    const bestand = { ...schulung, oeffentlicheTermine: termine };
    let antworteFuerA!: (antwort: Response) => void;
    const optionenFuerA = new Promise<Response>((resolve) => { antworteFuerA = resolve; });
    let optionenAufrufe = 0;
    vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
      const url = String(input);
      if (url.endsWith("/TERMIN-A/traineroptionen")) {
        optionenAufrufe++;
        return optionenAufrufe === 1
          ? optionenFuerA
          : jsonResponse([{ id: "TRN-NEU", name: "Trainer Neu" }]) as Response;
      }
      const termin = termine.find(({ terminId }) => url.endsWith(`/api/termine/${terminId}`));
      if (termin) {
        return jsonResponse({ ...termin, schulungId: "SCH-001", schulungTitel: schulung.titel,
          schulungsTage: 1, anzahlBuchungen: 0, assistenten: [], teilnehmer: [], warnungen: [] }) as Response;
      }
      return jsonResponse(url.includes("/api/schulungen") ? [bestand] : {}) as Response;
    });

    const wrapper = mount(PlanerAnsicht);
    await flushPromises();
    await wrapper.findAll(".calendar-event")[0].trigger("click");
    await flushPromises();
    await wrapper.get(".calendar-detail-actions .primary-action").trigger("click");
    await wrapper.get(".termin-detail-schliessen").trigger("click");
    await wrapper.get(".calendar-event").trigger("click");
    await flushPromises();
    await wrapper.get(".calendar-detail-actions .primary-action").trigger("click");
    await flushPromises();
    antworteFuerA(jsonResponse([{ id: "TRN-ALT", name: "Trainer Alt" }]) as Response);
    await flushPromises();

    expect(wrapper.get(".calendar-detail").text()).toContain("Trainer Neu");
    expect(wrapper.get(".calendar-detail").text()).not.toContain("Trainer Alt");
  });
});
