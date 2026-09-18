import { createRouter, createWebHistory } from "vue-router";
import { ladeKonto } from "./auth";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", component: () => import("./ansichten/PlanerAnsicht.vue"), meta: { auth: true } },
    { path: "/anmelden", component: () => import("./ansichten/AnmeldenAnsicht.vue"), meta: { gast: true } },
    { path: "/registrieren", component: () => import("./ansichten/RegistrierenAnsicht.vue"), meta: { gast: true } },
    { path: "/profil", component: () => import("./ansichten/ProfilAnsicht.vue"), meta: { auth: true } },
    { path: "/benutzerkonten", component: () => import("./ansichten/KontenAnsicht.vue"), meta: { auth: true, admin: true } },
    { path: "/katalog", component: () => import("./ansichten/KatalogAnsicht.vue"), meta: { auth: true } },
    { path: "/katalog/neu", component: () => import("./ansichten/SchulungsformularAnsicht.vue"), meta: { auth: true, admin: true } },
    { path: "/katalog/aufnahme", component: () => import("./ansichten/AufnahmeAnsicht.vue"), meta: { auth: true, admin: true } },
    { path: "/kategorien", component: () => import("./ansichten/KategorienAnsicht.vue"), meta: { auth: true, admin: true } },
    { path: "/katalog/:id", component: () => import("./ansichten/SchulungsAnsicht.vue"), props: true, meta: { auth: true } },
    { path: "/katalog/:id/bearbeiten", component: () => import("./ansichten/SchulungsformularAnsicht.vue"), props: true, meta: { auth: true, admin: true } },
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
