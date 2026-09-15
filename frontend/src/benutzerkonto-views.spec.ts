import { flushPromises, mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { createMemoryHistory, createRouter } from "vue-router";
import AnmeldenView from "./views/AnmeldenView.vue";
import KontenView from "./views/KontenView.vue";
import ProfilView from "./views/ProfilView.vue";
import RegistrierenView from "./views/RegistrierenView.vue";
import {
  anmelden, fetchKonten, nameAendern, passwortAendern, registrieren,
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
  });

  it("registriert und meldet ein Konto über die Formulare an", async () => {
    const router = testRouter();
    await router.push("/registrieren");
    vi.mocked(registrieren).mockResolvedValue(trainer);
    const registrierung = mount(RegistrierenView, { global: { plugins: [router] } });
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
    const anmeldung = mount(AnmeldenView, { global: { plugins: [router] } });
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
    const profil = mount(ProfilView);

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
    const admin = mount(KontenView, { global: { plugins: [router] } });
    await flushPromises();
    expect(admin.text()).toContain("Trainerrolle entziehen");
    expect(admin.text()).toContain("Stilllegen");
    expect(admin.text()).toContain("Löschen");
    expect(admin.text()).toContain("Passwort setzen");
    expect(admin.text()).not.toContain("Administratorrolle entziehen");
    expect(admin.text()).not.toContain("Eigentümerrolle übergeben");
    admin.unmount();

    setzeKonto({ ...trainer, rollen: ["TRAINER", "ADMINISTRATOR", "EIGENTUEMER"] });
    const eigentuemer = mount(KontenView, { global: { plugins: [router] } });
    await flushPromises();
    expect(eigentuemer.text()).toContain("Administratorrolle entziehen");
    expect(eigentuemer.text()).toContain("Eigentümerrolle übergeben");
  });
});
