import { flushPromises, mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { createMemoryHistory, createRouter } from "vue-router";
import AnmeldenAnsicht from "./ansichten/AnmeldenAnsicht.vue";
import KontenAnsicht from "./ansichten/KontenAnsicht.vue";
import ProfilAnsicht from "./ansichten/ProfilAnsicht.vue";
import RegistrierenAnsicht from "./ansichten/RegistrierenAnsicht.vue";
import {
  anmelden, eigeneQualifikationAblegen, fetchEigeneQualifikationen,
  fetchBenachrichtigungen, fetchKonten, nameAendern, passwortAendern, registrieren,
} from "./api";
import { aktuellesKonto, setzeKonto } from "./auth";
import type { Benutzerkonto } from "./types";

vi.mock("./api", () => ({
  anmelden: vi.fn(),
  fetchKonten: vi.fn(),
  fremdesPasswortSetzen: vi.fn(),
  kontoAktion: vi.fn(),
  kontoLoeschen: vi.fn(),
  rolleEntziehen: vi.fn(),
  rolleErteilen: vi.fn(),
  nameAendern: vi.fn(),
  passwortAendern: vi.fn(),
  registrieren: vi.fn(),
  abwesenheitEintragen: vi.fn(),
  eigeneQualifikationAblegen: vi.fn(),
  fetchEigeneQualifikationen: vi.fn().mockResolvedValue([]),
  fetchBenachrichtigungen: vi.fn().mockResolvedValue([]),
}));

const trainer: Benutzerkonto = {
  id: "K-1",
  email: "trainer@example.de",
  name: "Trainer",
  aenderungsstand: 0,
  rollen: ["TRAINER"],
  zustand: "AKTIV",
};

function testRouter() {
  const component = { template: "<div />" };
  return createRouter({
    history: createMemoryHistory(),
    routes: ["/", "/anmelden", "/registrieren"].map(path => ({ path, component })),
  });
}

describe("Benutzerkonto-Ansichten", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    aktuellesKonto.value = null;
    vi.mocked(fetchEigeneQualifikationen).mockResolvedValue([]);
    vi.mocked(fetchBenachrichtigungen).mockResolvedValue([]);
  });

  it("zeigt Benachrichtigungen auch reinen Administratoren", async () => {
    setzeKonto({ ...trainer, rollen: ["ADMINISTRATOR"] });
    vi.mocked(fetchBenachrichtigungen).mockResolvedValue([{
      id: 1, anlass: "Trainer hat eine Qualifikation abgelegt.", erstelltAm: "2026-09-19T10:00:00",
    }]);
    const profil = mount(ProfilAnsicht);
    await flushPromises();

    expect(profil.text()).toContain("Trainer hat eine Qualifikation abgelegt.");
    expect(fetchEigeneQualifikationen).not.toHaveBeenCalled();
  });

  // verifies: TEST_QUA_ABLEGEN_01
  it("legt eine Qualifikation erst nach bestätigter Warnung ab", async () => {
    setzeKonto(trainer);
    const stand = {
      schulungId: "SCH-001", schulungstitel: "Scrum", status: "QUALIFIZIERT",
      begruendung: null, kuenftigeTermine: 2,
    } as const;
    vi.mocked(fetchEigeneQualifikationen)
      .mockResolvedValueOnce([stand]).mockResolvedValueOnce([]);
    const profil = mount(ProfilAnsicht);
    await flushPromises();
    const schalter = profil.findAll("button").find((button) => button.text() === "Qualifikation ablegen")!;

    await schalter.trigger("click");
    expect(eigeneQualifikationAblegen).not.toHaveBeenCalled();
    expect(profil.get("[role=dialog]").text()).toContain("2 künftige Terminzuweisung");
    await profil.get("[role=dialog]").findAll("button")[0].trigger("click");
    expect(eigeneQualifikationAblegen).not.toHaveBeenCalled();

    await schalter.trigger("click");
    await profil.get("[role=dialog]").findAll("button")[1].trigger("click");
    await flushPromises();
    expect(eigeneQualifikationAblegen).toHaveBeenCalledWith("SCH-001");
    expect(profil.text()).not.toContain("Scrum");
    expect(profil.text()).toContain("Die Qualifikation wurde abgelegt.");
  });

  it("registriert und meldet ein Konto über die Formulare an", async () => {
    const router = testRouter();
    await router.push("/registrieren");
    vi.mocked(registrieren).mockResolvedValue(trainer);
    const registrierung = mount(RegistrierenAnsicht, { global: { plugins: [router] } });
    await registrierung.find("#register-name").setValue("Trainer");
    await registrierung.find("#register-email").setValue(trainer.email);
    await registrierung.find("#register-passwort").setValue("geheim");
    await registrierung.find("form").trigger("submit");
    await flushPromises();

    expect(registrieren).toHaveBeenCalledWith({
      name: "Trainer", email: trainer.email, passwort: "geheim",
    });
    expect(router.currentRoute.value.fullPath).toBe("/anmelden?registriert=1");
    registrierung.unmount();

    vi.mocked(anmelden).mockResolvedValue(trainer);
    const anmeldung = mount(AnmeldenAnsicht, { global: { plugins: [router] } });
    await anmeldung.find("#login-email").setValue(trainer.email);
    await anmeldung.find("#login-passwort").setValue("geheim");
    await anmeldung.find("form").trigger("submit");
    await flushPromises();

    expect(anmelden).toHaveBeenCalledWith(trainer.email, "geheim");
    expect(aktuellesKonto.value).toEqual(trainer);
    expect(router.currentRoute.value.path).toBe("/");
  });

  it("ändert Name und Passwort über das Profil", async () => {
    setzeKonto(trainer);
    vi.mocked(nameAendern).mockResolvedValue({ ...trainer, name: "Neu", aenderungsstand: 1 });
    vi.mocked(passwortAendern).mockResolvedValue({ ...trainer, name: "Neu", aenderungsstand: 2 });
    const profil = mount(ProfilAnsicht);

    await profil.find("#profile-name").setValue("Neu");
    await profil.findAll("form")[0].trigger("submit");
    await flushPromises();
    expect(nameAendern).toHaveBeenCalledWith("Neu", 0);

    await profil.find("#password-old").setValue("alt");
    await profil.find("#password-new").setValue("neu");
    await profil.findAll("form")[1].trigger("submit");
    await flushPromises();
    expect(passwortAendern).toHaveBeenCalledWith("alt", "neu", 1);
  });

  it("zeigt Eigentümeraktionen nur dem Eigentümer", async () => {
    const ziel = { ...trainer, id: "K-2", rollen: ["TRAINER", "ADMINISTRATOR"] } as Benutzerkonto;
    vi.mocked(fetchKonten).mockResolvedValue([ziel]);
    const router = testRouter();
    await router.push("/");

    setzeKonto({ ...trainer, rollen: ["TRAINER", "ADMINISTRATOR"] });
    const admin = mount(KontenAnsicht, { global: { plugins: [router] } });
    await flushPromises();
    expect(admin.text()).toContain("Trainerrolle entziehen");
    expect(admin.text()).toContain("Stilllegen");
    expect(admin.text()).toContain("Löschen");
    expect(admin.text()).toContain("Passwort setzen");
    expect(admin.text()).not.toContain("Administratorrolle entziehen");
    expect(admin.text()).not.toContain("Eigentümerrolle übergeben");
    admin.unmount();

    setzeKonto({ ...trainer, rollen: ["TRAINER", "ADMINISTRATOR", "EIGENTUEMER"] });
    const eigentuemer = mount(KontenAnsicht, { global: { plugins: [router] } });
    await flushPromises();
    expect(eigentuemer.text()).toContain("Administratorrolle entziehen");
    expect(eigentuemer.text()).toContain("Eigentümerrolle übergeben");
  });
});
