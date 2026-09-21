<script setup lang="ts">
import { RouterLink, RouterView, useRouter } from "vue-router";
import { abmelden, aktuellesKonto } from "./auth";

const router = useRouter();

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
          <RouterLink class="nav-link" to="/">Planer</RouterLink>
          <RouterLink class="nav-link" to="/katalog">Katalog</RouterLink>
          <RouterLink class="nav-link" to="/gruppen">Gruppen</RouterLink>
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
