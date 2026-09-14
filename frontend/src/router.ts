import { createRouter, createWebHistory } from "vue-router";
import { ladeKonto } from "./auth";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", component: () => import("./views/KatalogView.vue"), meta: { auth: true } },
    { path: "/anmelden", component: () => import("./views/AnmeldenView.vue"), meta: { gast: true } },
    { path: "/registrieren", component: () => import("./views/RegistrierenView.vue"), meta: { gast: true } },
    { path: "/profil", component: () => import("./views/ProfilView.vue"), meta: { auth: true } },
    { path: "/benutzerkonten", component: () => import("./views/KontenView.vue"), meta: { auth: true, admin: true } },
    { path: "/:pfad(.*)*", component: () => import("./ansichten/NichtGefundenAnsicht.vue") },
  ],
  scrollBehavior(ziel, _von, gespeichert) {
    if (gespeichert) return gespeichert;
    if (ziel.hash) return { el: ziel.hash };
    return { top: 0 };
  },
});

router.beforeEach(async (ziel) => {
  const konto = await ladeKonto(true);
  if (ziel.meta.auth && !konto) return { path: "/anmelden", query: { weiter: ziel.fullPath } };
  if (ziel.meta.gast && konto) return "/";
  if (ziel.meta.admin && !konto?.rollen.includes("ADMINISTRATOR")) return "/";
});

export default router;
