<script setup lang="ts">
import { ref } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import { anmelden } from "../api";
import { setzeKonto } from "../auth";

const route = useRoute();
const router = useRouter();
const email = ref("");
const passwort = ref("");
const fehler = ref("");
const laedt = ref(false);

async function submit() {
  fehler.value = "";
  laedt.value = true;
  try {
    setzeKonto(await anmelden(email.value, passwort.value));
    await router.push(typeof route.query.weiter === "string" ? route.query.weiter : "/");
  } catch (error) {
    fehler.value = error instanceof Error ? error.message : "Die Anmeldung ist fehlgeschlagen.";
  } finally {
    laedt.value = false;
  }
}
</script>

<template>
  <main class="konto-seite">
    <form class="konto-einstieg" @submit.prevent="submit">
      <header class="konto-einstieg-kopf">
        <p class="eyebrow">Benutzerkonto</p>
        <h1>Anmelden</h1>
        <p>Öffnen Sie Ihren persönlichen Schulungsplaner.</p>
      </header>
      <p v-if="route.query.registriert" class="success" role="status">
        Das Benutzerkonto wurde angelegt.
      </p>
      <p v-if="fehler" class="form-error" role="alert">{{ fehler }}</p>
      <div class="form-field">
        <label for="login-email">E-Mail-Adresse</label>
        <input id="login-email" v-model="email" type="email" autocomplete="username" required />
      </div>
      <div class="form-field">
        <label for="login-passwort">Passwort</label>
        <input id="login-passwort" v-model="passwort" type="password" autocomplete="current-password" required />
      </div>
      <button class="primary-action konto-hauptaktion" type="submit" :disabled="laedt">
        {{ laedt ? "Anmeldung läuft" : "Anmelden" }}
      </button>
      <p class="konto-wechsel">
        Noch kein Benutzerkonto? <RouterLink to="/registrieren">Jetzt registrieren</RouterLink>
      </p>
    </form>
  </main>
</template>
