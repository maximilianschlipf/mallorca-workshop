<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import {
  fetchKonten, fremdesPasswortSetzen, kontoAktion, kontoLoeschen,
  rolleEntziehen, rolleErteilen,
} from "../api";
import { aktuellesKonto, ladeKonto } from "../auth";
import type { Benutzerkonto, Rolle } from "../types";

const router = useRouter();
const konten = ref<Benutzerkonto[]>([]);
const passwoerter = reactive<Record<string, string>>({});
const fehler = ref("");
const meldung = ref("");
const laedt = ref(true);
const istEigentuemer = computed(() => aktuellesKonto.value?.rollen.includes("EIGENTUEMER"));
const rollenNamen: Record<Rolle, string> = {
  TRAINER: "Trainer",
  ADMINISTRATOR: "Administrator",
  EIGENTUEMER: "Eigentümer",
};

async function laden() {
  laedt.value = true;
  try { konten.value = await fetchKonten(); }
  catch (error) { fehler.value = error instanceof Error ? error.message : "Konten konnten nicht geladen werden."; }
  finally { laedt.value = false; }
}

async function ausfuehren(aktion: () => Promise<void>, erfolg: string) {
  fehler.value = meldung.value = "";
  try {
    await aktion();
    meldung.value = erfolg;
    await ladeKonto(true);
    if (!aktuellesKonto.value) return router.push("/anmelden");
    await laden();
  } catch (error) {
    fehler.value = error instanceof Error ? error.message : "Die Aktion ist fehlgeschlagen.";
  }
}

function rolleUmschalten(konto: Benutzerkonto, rolle: Rolle) {
  const hatRolle = konto.rollen.includes(rolle);
  return ausfuehren(
    () => hatRolle
      ? rolleEntziehen(konto.id, rolle, konto.aenderungsstand)
      : rolleErteilen(konto.id, rolle, konto.aenderungsstand),
    "Die Rollen wurden aktualisiert.",
  );
}

function statusUmschalten(konto: Benutzerkonto) {
  const reaktivieren = konto.zustand === "STILLGELEGT";
  return ausfuehren(
    () => kontoAktion(
      konto.id, reaktivieren ? "reaktivieren" : "stilllegen", konto.aenderungsstand,
    ),
    reaktivieren ? "Das Benutzerkonto wurde reaktiviert." : "Das Benutzerkonto wurde stillgelegt.",
  );
}

function eigentuemerUebergeben(konto: Benutzerkonto) {
  if (!window.confirm(`Eigentümerrolle an ${konto.name} übergeben?`)) return;
  return ausfuehren(
    () => kontoAktion(konto.id, "eigentuemer", konto.aenderungsstand),
    "Die Eigentümerrolle wurde übergeben.",
  );
}

function loeschen(konto: Benutzerkonto) {
  if (!window.confirm(`Benutzerkonto von ${konto.name} endgültig löschen?`)) return;
  return ausfuehren(
    () => kontoLoeschen(konto.id, konto.aenderungsstand),
    "Das Benutzerkonto wurde gelöscht.",
  );
}

function passwortSetzen(konto: Benutzerkonto) {
  const passwort = passwoerter[konto.id];
  return ausfuehren(async () => {
    await fremdesPasswortSetzen(konto.id, passwort, konto.aenderungsstand);
    passwoerter[konto.id] = "";
  }, "Das Passwort wurde gesetzt; laufende Sitzungen wurden beendet.");
}

onMounted(laden);
</script>

<template>
  <main class="content-page">
    <div class="section-heading compact-heading">
      <p class="eyebrow">Administration</p>
      <h1>Benutzerkonten</h1>
      <p>Rollen, Zugang und Eigentümerschaft verwalten.</p>
    </div>
    <p v-if="meldung" class="success" role="status">{{ meldung }}</p>
    <p v-if="fehler" class="form-error" role="alert">{{ fehler }}</p>
    <div v-if="laedt" class="konten-liste konto-skelett" aria-busy="true">
      <span class="sr-only">Benutzerkonten werden geladen.</span>
      <article v-for="index in 2" :key="index" class="verwaltetes-konto" aria-hidden="true">
        <div class="konto-skelett-text"><span></span><span></span><span></span></div>
        <div class="konto-skelett-aktionen"><span></span><span></span></div>
      </article>
    </div>
    <div v-else class="konten-liste">
      <article v-for="konto in konten" :key="konto.id" class="verwaltetes-konto">
        <div class="konto-identitaet">
          <p class="konto-status" :class="{ 'ist-stillgelegt': konto.zustand === 'STILLGELEGT' }">
            {{ konto.zustand === "AKTIV" ? "Aktiv" : "Stillgelegt" }}
          </p>
          <h2>{{ konto.name }}</h2>
          <a :href="`mailto:${konto.email}`">{{ konto.email }}</a>
          <p class="konto-rollen">
            <strong>Rollen:</strong> {{ konto.rollen.map((rolle) => rollenNamen[rolle]).join(", ") }}
          </p>
        </div>
        <div class="konto-aktionen" :aria-label="`Aktionen für ${konto.name}`">
          <button class="sekundaer-aktion" type="button" @click="rolleUmschalten(konto, 'TRAINER')">
            Trainerrolle {{ konto.rollen.includes("TRAINER") ? "entziehen" : "erteilen" }}
          </button>
          <button
            v-if="!konto.rollen.includes('ADMINISTRATOR')"
            class="sekundaer-aktion"
            type="button"
            @click="rolleUmschalten(konto, 'ADMINISTRATOR')"
          >Administratorrolle erteilen</button>
          <button
            v-else-if="istEigentuemer && !konto.rollen.includes('EIGENTUEMER')"
            class="sekundaer-aktion"
            type="button"
            @click="rolleUmschalten(konto, 'ADMINISTRATOR')"
          >Administratorrolle entziehen</button>
          <button
            v-if="istEigentuemer && konto.zustand === 'AKTIV' && !konto.rollen.includes('EIGENTUEMER')"
            class="sekundaer-aktion"
            type="button"
            @click="eigentuemerUebergeben(konto)"
          >Eigentümerrolle übergeben</button>
          <button
            v-if="!konto.rollen.includes('EIGENTUEMER')"
            class="sekundaer-aktion"
            type="button"
            @click="statusUmschalten(konto)"
          >{{ konto.zustand === "AKTIV" ? "Stilllegen" : "Reaktivieren" }}</button>
          <button
            v-if="!konto.rollen.includes('EIGENTUEMER')"
            class="sekundaer-aktion danger-action"
            type="button"
            @click="loeschen(konto)"
          >Löschen</button>
        </div>
        <form
          v-if="konto.id !== aktuellesKonto?.id && !konto.rollen.includes('EIGENTUEMER')"
          class="password-reset"
          @submit.prevent="passwortSetzen(konto)"
        >
          <div class="form-field">
            <label :for="`passwort-${konto.id}`">Neues Passwort</label>
            <input
              :id="`passwort-${konto.id}`"
              v-model="passwoerter[konto.id]"
              type="password"
              autocomplete="new-password"
              required
            />
          </div>
          <button class="primary-action" type="submit">Passwort setzen</button>
        </form>
      </article>
    </div>
  </main>
</template>
