<script setup lang="ts">
import { ref, watchEffect } from "vue";
import { abwesenheitEintragen, nameAendern, passwortAendern } from "../api";
import { aktuellesKonto, setzeKonto } from "../auth";
import type { Rolle } from "../types";

const name = ref("");
const bisher = ref("");
const neu = ref("");
const meldung = ref("");
const fehler = ref("");
const abwesendVon = ref("");
const abwesendBis = ref("");
const abwesenheitsgrund = ref("");
const rollenNamen: Record<Rolle, string> = {
  TRAINER: "Trainer",
  ADMINISTRATOR: "Administrator",
  EIGENTUEMER: "Eigentümer",
};
watchEffect(() => name.value = aktuellesKonto.value?.name ?? "");

async function speichereName() {
  fehler.value = meldung.value = "";
  try {
    setzeKonto(await nameAendern(name.value, aktuellesKonto.value!.aenderungsstand));
    meldung.value = "Der Name wurde gespeichert.";
  } catch (error) {
    fehler.value = error instanceof Error ? error.message : "Speichern fehlgeschlagen.";
  }
}

async function speicherePasswort() {
  fehler.value = meldung.value = "";
  try {
    setzeKonto(await passwortAendern(
      bisher.value, neu.value, aktuellesKonto.value!.aenderungsstand,
    ));
    bisher.value = neu.value = "";
    meldung.value = "Das Passwort wurde geändert. Andere Sitzungen wurden beendet.";
  } catch (error) {
    fehler.value = error instanceof Error ? error.message : "Passwortänderung fehlgeschlagen.";
  }
}

async function speichereAbwesenheit() {
  fehler.value = meldung.value = "";
  try {
    await abwesenheitEintragen(abwesendVon.value, abwesendBis.value, abwesenheitsgrund.value);
    abwesendVon.value = abwesendBis.value = abwesenheitsgrund.value = "";
    meldung.value = "Die Abwesenheit wurde eingetragen.";
  } catch (error) {
    fehler.value = error instanceof Error ? error.message : "Eintragen fehlgeschlagen.";
  }
}
</script>

<template>
  <main class="content-page" v-if="aktuellesKonto">
    <div class="section-heading compact-heading">
      <p class="eyebrow">Benutzerkonto</p>
      <h1>Mein Profil</h1>
    </div>
    <p v-if="meldung" class="success" role="status">{{ meldung }}</p>
    <p v-if="fehler" class="form-error" role="alert">{{ fehler }}</p>
    <div class="profile-grid">
      <form class="konto-karte" @submit.prevent="speichereName">
        <header class="konto-kartenkopf">
          <h2>Profildaten</h2>
          <p>Name und Kontoinformationen.</p>
        </header>
        <div class="form-field">
          <label for="profile-email">E-Mail-Adresse</label>
          <input id="profile-email" :value="aktuellesKonto.email" disabled />
        </div>
        <div class="form-field">
          <label for="profile-name">Name</label>
          <input id="profile-name" v-model="name" required />
        </div>
        <p class="konto-rollen">
          <strong>Rollen:</strong>
          {{ aktuellesKonto.rollen.map((rolle) => rollenNamen[rolle]).join(", ") }}
        </p>
        <button class="primary-action" type="submit">Name speichern</button>
      </form>
      <form class="konto-karte" @submit.prevent="speicherePasswort">
        <header class="konto-kartenkopf">
          <h2>Passwort ändern</h2>
          <p>Andere Sitzungen enden nach dem Speichern.</p>
        </header>
        <div class="form-field">
          <label for="password-old">Bisheriges Passwort</label>
          <input id="password-old" v-model="bisher" type="password" autocomplete="current-password" required />
        </div>
        <div class="form-field">
          <label for="password-new">Neues Passwort</label>
          <input id="password-new" v-model="neu" type="password" autocomplete="new-password" required />
        </div>
        <button class="primary-action" type="submit">Passwort ändern</button>
      </form>
      <form
        v-if="aktuellesKonto.rollen.includes('TRAINER')"
        class="konto-karte"
        @submit.prevent="speichereAbwesenheit"
      >
        <header class="konto-kartenkopf">
          <h2>Abwesenheit eintragen</h2>
          <p>Zeitraum und optionalen Grund angeben.</p>
        </header>
        <div class="form-field">
          <label for="absence-from">Anfangsdatum</label>
          <input id="absence-from" v-model="abwesendVon" type="date" required />
        </div>
        <div class="form-field">
          <label for="absence-to">Enddatum</label>
          <input id="absence-to" v-model="abwesendBis" type="date" required />
        </div>
        <div class="form-field">
          <label for="absence-reason">Grund (optional)</label>
          <input id="absence-reason" v-model="abwesenheitsgrund" maxlength="255" />
        </div>
        <button class="primary-action" type="submit">Abwesenheit eintragen</button>
      </form>
    </div>
  </main>
</template>
