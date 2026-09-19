<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from "vue";
import { RouterLink, RouterView, useRouter } from "vue-router";
import { abmelden, aktuellesKonto } from "./auth";
import { fetchUngeleseneBenachrichtigungen } from "./api";

const router = useRouter();
const ungelesen = ref(0);

async function ladeUngelesen() {
  ungelesen.value = aktuellesKonto.value
    ? (await fetchUngeleseneBenachrichtigungen()).anzahl : 0;
}

function aktualisiereUngelesen() {
  ladeUngelesen().catch(() => ungelesen.value = 0);
}

watch(aktuellesKonto, aktualisiereUngelesen);
onMounted(() => {
  aktualisiereUngelesen();
  window.addEventListener("benachrichtigungen-gelesen", aktualisiereUngelesen);
});
onBeforeUnmount(() => window.removeEventListener("benachrichtigungen-gelesen", aktualisiereUngelesen));

async function logout() {
  await abmelden();
  await router.push("/anmelden");
}
</script>

<template>
  <div class="page">
    <header id="top" class="site-header">
      <nav class="nav" aria-label="Hauptnavigation">
        <RouterLink class="brand" to="/" aria-label="SimplyTest Academy Startseite">
          <span class="brand-lockup" aria-hidden="true">
            <img src="/simplytest-wordmark-white.svg" alt="" />
            <strong>Academy</strong>
          </span>
        </RouterLink>
        <div v-if="aktuellesKonto" class="nav-links" aria-label="Benutzerbereich">
          <RouterLink class="nav-link" to="/">Dashboard</RouterLink>
          <RouterLink class="nav-link" to="/planer">Planer</RouterLink>
          <RouterLink class="nav-link" to="/katalog">Katalog</RouterLink>
          <RouterLink class="nav-link notification-link" to="/benachrichtigungen">
            Benachrichtigungen
            <span v-if="ungelesen" class="notification-count" :aria-label="`${ungelesen} ungelesene Benachrichtigungen`">{{ ungelesen }}</span>
          </RouterLink>
          <RouterLink class="nav-link" to="/profil">Profil</RouterLink>
          <RouterLink
            v-if="aktuellesKonto.rollen.includes('ADMINISTRATOR')"
            class="nav-link"
            to="/benutzerkonten"
          >Konten</RouterLink>
          <button class="nav-link nav-button" type="button" @click="logout">Abmelden</button>
        </div>
      </nav>
    </header>
    <RouterView />
  </div>
</template>
