<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import { abmelden, aktuellesKonto } from "./auth";
import { fetchUngeleseneBenachrichtigungen } from "./api";
import SeitenFooter from "./komponenten/SeitenFooter.vue";

const router = useRouter();
const route = useRoute();
const ungelesen = ref(0);
const benachrichtigungsfehler = ref(false);
const abmeldungLaeuft = ref(false);
const abmeldefehler = ref("");
const navigationOffen = ref(false);
const kontomenue = ref<HTMLDetailsElement | null>(null);

const istAdministrator = computed(() =>
  aktuellesKonto.value?.rollen.includes("ADMINISTRATOR") ?? false,
);
const rollen = computed(() => (["EIGENTUEMER", "ADMINISTRATOR", "TRAINER"] as const)
  .filter(rolle => aktuellesKonto.value?.rollen.includes(rolle))
  .map(rolle => ({
    TRAINER: "Trainer",
    ADMINISTRATOR: "Administrator",
    EIGENTUEMER: "Eigentümer",
  })[rolle])
  .join(" · "));
const istKatalog = computed(() => route.path.startsWith("/katalog") || route.path === "/kategorien");

async function ladeUngelesen() {
  benachrichtigungsfehler.value = false;
  try {
    ungelesen.value = aktuellesKonto.value
      ? (await fetchUngeleseneBenachrichtigungen()).anzahl : 0;
  } catch {
    ungelesen.value = 0;
    benachrichtigungsfehler.value = true;
  }
}

function aktualisiereUngelesen() {
  void ladeUngelesen();
}

watch(aktuellesKonto, aktualisiereUngelesen);
onMounted(() => {
  aktualisiereUngelesen();
  window.addEventListener("benachrichtigungen-gelesen", aktualisiereUngelesen);
});
onBeforeUnmount(() => window.removeEventListener("benachrichtigungen-gelesen", aktualisiereUngelesen));

async function logout() {
  abmeldungLaeuft.value = true;
  abmeldefehler.value = "";
  try {
    await abmelden();
    await router.push("/anmelden");
  } catch {
    abmeldefehler.value = "Abmelden ist fehlgeschlagen. Bitte erneut versuchen.";
  } finally {
    abmeldungLaeuft.value = false;
  }
}

function schliesseMenues() {
  const fokusInsDokument = navigationOffen.value || Boolean(kontomenue.value?.open);
  navigationOffen.value = false;
  kontomenue.value?.removeAttribute("open");
  if (fokusInsDokument) void nextTick(() => document.getElementById("hauptinhalt")?.focus());
}
</script>

<template>
  <div class="page">
    <a class="skip-link" href="#hauptinhalt">Zum Hauptinhalt</a>
    <header id="top" class="site-header">
      <nav class="nav" aria-label="Hauptnavigation">
        <RouterLink class="brand" to="/" aria-label="SimplyTest Academy Startseite">
          <span class="brand-lockup" aria-hidden="true">
            <img src="/simplytest-wordmark-white.svg" alt="" />
            <strong>Academy</strong>
          </span>
        </RouterLink>
        <div v-if="aktuellesKonto" class="navigation-shell">
          <button
            class="navigation-toggle"
            type="button"
            aria-controls="navigation-content"
            :aria-expanded="navigationOffen"
            @click="navigationOffen = !navigationOffen"
          >
            <span>Menü</span>
            <svg aria-hidden="true" viewBox="0 0 24 24">
              <path d="M4 7h16M4 12h16M4 17h16" />
            </svg>
          </button>

          <div id="navigation-content" class="navigation-content" :class="{ 'is-open': navigationOffen }">
            <ul class="work-navigation" aria-label="Arbeitsbereiche">
              <li><RouterLink class="nav-link" to="/" @click="schliesseMenues">Dashboard</RouterLink></li>
              <li><RouterLink class="nav-link" to="/planer" @click="schliesseMenues">Terminplaner</RouterLink></li>
              <li>
                <RouterLink
                  class="nav-link"
                  :class="{ 'section-active': istKatalog }"
                  :aria-current="istKatalog ? 'location' : undefined"
                  to="/katalog"
                  @click="schliesseMenues"
                >Schulungskatalog</RouterLink>
              </li>
            </ul>

            <div class="utility-navigation">
              <RouterLink
                v-if="istAdministrator"
                class="utility-link administration-link"
                to="/benutzerkonten"
                @click="schliesseMenues"
              >
                <svg aria-hidden="true" viewBox="0 0 24 24">
                  <path d="M16 20v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 10a4 4 0 1 0 0-8 4 4 0 0 0 0 8ZM22 20v-2a4 4 0 0 0-3-3.87M16 2.13a4 4 0 0 1 0 7.75" />
                </svg>
                <span>Benutzerkonten</span>
              </RouterLink>

              <RouterLink
                class="utility-link notification-link"
                to="/benachrichtigungen"
                :aria-label="ungelesen ? `${ungelesen} ungelesene Benachrichtigungen` : 'Benachrichtigungen'"
                @click="schliesseMenues"
              >
                <svg aria-hidden="true" viewBox="0 0 24 24">
                  <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9ZM10 21h4" />
                </svg>
                <span>Benachrichtigungen</span>
                <span v-if="ungelesen" class="notification-count">{{ ungelesen }}</span>
              </RouterLink>
              <span v-if="benachrichtigungsfehler" class="sr-only" role="status">
                Benachrichtigungen konnten nicht geladen werden.
              </span>

              <details ref="kontomenue" class="account-menu">
                <summary
                  class="account-trigger"
                  :aria-label="`Kontomenü für ${aktuellesKonto.name}, Rollen: ${rollen}`"
                >
                  <span class="account-copy">
                    <strong>{{ aktuellesKonto.name }}</strong>
                    <small>{{ rollen }}</small>
                  </span>
                  <svg aria-hidden="true" viewBox="0 0 24 24"><path d="m7 10 5 5 5-5" /></svg>
                </summary>
                <div class="account-panel" role="group" aria-label="Persönliches Konto">
                  <RouterLink to="/profil" @click="schliesseMenues">Mein Profil</RouterLink>
                  <button type="button" :disabled="abmeldungLaeuft" @click="logout">
                    {{ abmeldungLaeuft ? "Wird abgemeldet …" : "Abmelden" }}
                  </button>
                  <p v-if="abmeldefehler" role="alert">{{ abmeldefehler }}</p>
                </div>
              </details>
            </div>
          </div>
        </div>
      </nav>
    </header>
    <div id="hauptinhalt" class="site-content" tabindex="-1">
      <RouterView />
    </div>
    <SeitenFooter />
  </div>
</template>
