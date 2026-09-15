<script setup lang="ts">
import { ref } from "vue";
import { RouterLink, useRouter } from "vue-router";
import { registrieren } from "../api";

const router = useRouter();
const name = ref("");
const email = ref("");
const passwort = ref("");
const fehler = ref("");
const laedt = ref(false);

async function submit() {
  fehler.value = "";
  laedt.value = true;
  try {
    await registrieren({ name: name.value, email: email.value, passwort: passwort.value });
    await router.push({ path: "/anmelden", query: { registriert: "1" } });
  } catch (error) {
    fehler.value = error instanceof Error ? error.message : "Die Registrierung ist fehlgeschlagen.";
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
        <h1>Registrieren</h1>
        <p>Das erste Benutzerkonto wird automatisch Eigentümer dieser Instanz.</p>
      </header>
      <p v-if="fehler" class="form-error" role="alert">{{ fehler }}</p>
      <div class="form-field">
        <label for="register-name">Name</label>
        <input id="register-name" v-model="name" autocomplete="name" required />
      </div>
      <div class="form-field">
        <label for="register-email">E-Mail-Adresse</label>
        <input id="register-email" v-model="email" type="email" autocomplete="username" required />
      </div>
      <div class="form-field">
        <label for="register-passwort">Passwort</label>
        <input id="register-passwort" v-model="passwort" type="password" autocomplete="new-password" required />
      </div>
      <button class="primary-action konto-hauptaktion" type="submit" :disabled="laedt">
        {{ laedt ? "Wird angelegt" : "Benutzerkonto anlegen" }}
      </button>
      <p class="konto-wechsel">
        Bereits registriert? <RouterLink to="/anmelden">Zur Anmeldung</RouterLink>
      </p>
    </form>
  </main>
</template>
