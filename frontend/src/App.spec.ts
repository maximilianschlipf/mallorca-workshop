import { mount, flushPromises } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import App from "./App.vue";

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
      startdatum: "2026-08-27",
      enddatum: "2026-08-29",
      ort: "Köln",
      format: "Präsenz",
      status: "geplant",
      trainerId: "TRN-001",
    },
    {
      terminId: "SCH-001-T2",
      startdatum: "2026-08-30",
      enddatum: "2026-08-30",
      ort: "Online",
      format: "Online",
      status: "ausgebucht",
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

describe("App smoke", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    vi.useRealTimers();
  });

  it("renders at least one training entry from API data", async () => {
    mockFetch((url) =>
      url.includes("/api/kategorien") ? ["Agile"] : [schulung],
    );

    const wrapper = mount(App);
    await flushPromises();

    expect(wrapper.text()).toContain("Schulungskatalog");
    expect(wrapper.text()).toContain("Scrum Master Zertifizierung");
    expect(wrapper.text()).toContain(
      "Praxisnah - ohne typografische Sonderstriche",
    );
    expect(wrapper.text()).not.toContain("–");
  });

  it("loads categories into the filter dropdown", async () => {
    mockFetch((url) =>
      url.includes("/api/kategorien")
        ? ["Agile", "Cloud & DevOps"]
        : [schulung],
    );

    const wrapper = mount(App);
    await flushPromises();

    const options = wrapper
      .findAll("#filter-kategorie option")
      .map((o) => o.text());
    expect(options).toContain("Alle Kategorien");
    expect(options).toContain("Agile");
    expect(options).toContain("Cloud & DevOps");
  });

  it("sends trimmed search and category as query params", async () => {
    vi.useFakeTimers();
    const requestedUrls: string[] = [];
    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockImplementation(async (input) => {
        const url = String(input);
        requestedUrls.push(url);
        return jsonResponse(
          url.includes("/api/kategorien") ? ["Agile"] : [schulung],
        ) as Response;
      });

    const wrapper = mount(App);
    await vi.runOnlyPendingTimersAsync();

    await wrapper.find("#filter-suche").setValue("  scrum  ");
    await wrapper.find("#filter-kategorie").setValue("Agile");
    await vi.runAllTimersAsync();

    const schulungenCalls = requestedUrls.filter((url) =>
      url.includes("/api/schulungen"),
    );
    const lastCall = schulungenCalls[schulungenCalls.length - 1];
    expect(lastCall).toContain("suche=scrum");
    expect(lastCall).toContain("kategorie=Agile");
    expect(fetchMock).toHaveBeenCalled();
  });

  it("shows a dedicated empty state when filters have no match", async () => {
    vi.useFakeTimers();
    let filtered = false;
    vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
      const url = String(input);
      if (url.includes("/api/kategorien")) {
        return jsonResponse(["Agile"]) as Response;
      }
      return jsonResponse(filtered ? [] : [schulung]) as Response;
    });

    const wrapper = mount(App);
    await vi.runOnlyPendingTimersAsync();

    filtered = true;
    await wrapper.find("#filter-suche").setValue("gibtesnicht");
    await vi.runAllTimersAsync();
    await flushPromises();

    expect(wrapper.text()).toContain(
      "Keine Schulungen entsprechen den gewählten Filtern",
    );
  });

  it("queries available trainers for the selected course and period", async () => {
    const requestedUrls: string[] = [];
    vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
      const url = String(input);
      requestedUrls.push(url);
      if (url.includes("/api/kategorien")) return jsonResponse(["Agile"]) as Response;
      if (url.includes("/api/trainer/verfuegbar")) {
        return jsonResponse([
          { id: "TRN-005", name: "Elena Fischer", email: "elena@example.de" },
        ]) as Response;
      }
      return jsonResponse([schulung]) as Response;
    });

    const wrapper = mount(App);
    await flushPromises();
    await wrapper.find("#trainer-schulung").setValue("SCH-001");
    await wrapper.find("#trainer-von").setValue("2026-08-01");
    await wrapper.find("#trainer-bis").setValue("2026-08-02");
    await wrapper.find(".trainer-form").trigger("submit");
    await flushPromises();

    expect(requestedUrls.at(-1)).toContain(
      "schulungId=SCH-001&von=2026-08-01&bis=2026-08-02",
    );
    expect(wrapper.text()).toContain("Elena Fischer");
  });

  it("rejects an invalid period without calling the trainer API", async () => {
    const fetchMock = mockFetch((url) =>
      url.includes("/api/kategorien") ? ["Agile"] : [schulung],
    );

    const wrapper = mount(App);
    await flushPromises();
    await wrapper.find("#trainer-schulung").setValue("SCH-001");
    await wrapper.find("#trainer-von").setValue("2026-08-03");
    await wrapper.find("#trainer-bis").setValue("2026-08-02");
    await wrapper.find(".trainer-form").trigger("submit");

    expect(wrapper.text()).toContain(
      "Das Anfangsdatum darf nicht nach dem Enddatum liegen",
    );
    expect(
      fetchMock.mock.calls.some(([url]) =>
        String(url).includes("/api/trainer/verfuegbar"),
      ),
    ).toBe(false);
  });

  it("shows a dedicated state when no trainer is available", async () => {
    vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
      const url = String(input);
      if (url.includes("/api/kategorien")) return jsonResponse(["Agile"]) as Response;
      if (url.includes("/api/trainer/verfuegbar")) return jsonResponse([]) as Response;
      return jsonResponse([schulung]) as Response;
    });

    const wrapper = mount(App);
    await flushPromises();
    await wrapper.find("#trainer-schulung").setValue("SCH-001");
    await wrapper.find("#trainer-von").setValue("2026-08-03");
    await wrapper.find("#trainer-bis").setValue("2026-08-04");
    await wrapper.find(".trainer-form").trigger("submit");
    await flushPromises();

    expect(wrapper.text()).toContain("Keine verfügbaren Trainer");
  });

  it("renders a Monday-first calendar and navigates between months", async () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2026, 7, 26));
    mockFetch((url) =>
      url.includes("/api/kategorien") ? ["Agile"] : [kalenderSchulung],
    );

    const wrapper = mount(App);
    await flushPromises();

    expect(wrapper.find(".calendar-toolbar h3").text()).toBe("August 2026");
    expect(wrapper.findAll(".calendar-weekday")[0].text()).toBe("Montag");

    await wrapper
      .findAll(".calendar-actions button")
      .find((button) => button.text() === "Weiter")!
      .trigger("click");

    expect(wrapper.find(".calendar-toolbar h3").text()).toBe("September 2026");
    expect(wrapper.text()).toContain(
      "In diesem Monat finden keine Schulungen statt",
    );
  });

  it("shows multi-day events, status variants and selected details", async () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2026, 7, 26));
    mockFetch((url) =>
      url.includes("/api/kategorien") ? ["Agile"] : [kalenderSchulung],
    );

    const wrapper = mount(App);
    await flushPromises();

    expect(wrapper.findAll(".calendar-event.status-geplant")).toHaveLength(3);
    expect(wrapper.find(".calendar-event.status-ausgebucht").text()).toContain(
      "Ausgebucht",
    );
    expect(wrapper.find(".calendar-event.status-abgesagt").text()).toContain(
      "Abgesagt",
    );

    await wrapper.find(".calendar-event.status-geplant").trigger("click");
    expect(wrapper.find(".calendar-detail").text()).toContain(
      "27.08.2026 - 29.08.2026",
    );
    expect(wrapper.find(".calendar-detail").text()).toContain("Köln");
    expect(wrapper.find(".calendar-detail").text()).toContain("Geplant");
  });

  it("keeps calendar events when the catalog filter changes", async () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2026, 7, 26));
    vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
      const url = String(input);
      if (url.includes("/api/kategorien")) return jsonResponse(["Agile"]) as Response;
      return jsonResponse(url.includes("suche=") ? [] : [kalenderSchulung]) as Response;
    });

    const wrapper = mount(App);
    await flushPromises();
    expect(wrapper.findAll(".calendar-event")).toHaveLength(5);

    await wrapper.find("#filter-suche").setValue("gibtesnicht");
    await vi.runAllTimersAsync();
    await flushPromises();

    expect(wrapper.findAll(".calendar-event")).toHaveLength(5);
    expect(wrapper.text()).toContain(
      "Keine Schulungen entsprechen den gewählten Filtern",
    );
  });
});
