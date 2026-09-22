import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, describe, expect, it, vi } from "vitest";
import DashboardAnsicht from "./ansichten/DashboardAnsicht.vue";

describe("Dashboard", () => {
  afterEach(() => vi.restoreAllMocks());

  it("zeigt die offenen Aufgaben des angemeldeten Kontos", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue({
      ok: true,
      json: async () => [{
        terminId: "SCH-001-T1",
        schulungTitel: "Scrum Master",
        startdatum: "2026-09-20",
        enddatum: "2026-09-21",
        ueberfaellig: true,
        ohneTrainer: false,
        dringend: true,
        mindestteilnehmerUnterschritten: false,
      }],
    } as Response);

    const wrapper = mount(DashboardAnsicht, {
      global: { stubs: { RouterLink: { template: "<a><slot /></a>" } } },
    });
    await flushPromises();

    expect(wrapper.get("h1").text()).toBe("Was liegt bei mir?");
    expect(wrapper.text()).toContain("Scrum Master");
    expect(wrapper.text()).toContain("Durchführungsbestätigung ausstehend");
  });
});
