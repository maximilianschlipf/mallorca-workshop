import { mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import { createMemoryHistory, createRouter } from "vue-router";
import App from "./App.vue";
import SeitenFooter from "./komponenten/SeitenFooter.vue";

vi.mock("./api", () => ({
  fetchUngeleseneBenachrichtigungen: vi.fn().mockResolvedValue({ anzahl: 0 }),
}));

describe("App", () => {
  it("zeigt den gemeinsamen Footer auf jeder Route", async () => {
    const ansicht = { template: "<main>Seite</main>" };
    const router = createRouter({
      history: createMemoryHistory(),
      routes: ["/", "/anmelden"].map(path => ({ path, component: ansicht })),
    });
    await router.push("/");
    await router.isReady();
    const app = mount(App, { global: { plugins: [router] } });

    expect(app.getComponent(SeitenFooter).text()).toContain("Ein Produkt von SimplyTest");
    await router.push("/anmelden");
    expect(app.getComponent(SeitenFooter).text()).toContain("Ein Produkt von SimplyTest");
  });
});
