<script setup lang="ts">
import { nextTick, onMounted, ref } from "vue";
import { fetchDashboard, qualifikationsbewerbungAblehnen,
  qualifikationsbewerbungGenehmigen, qualifikationsbewerbungZurueckziehen } from "../api";
import type { Dashboard, DashboardVorgang } from "../types";

const daten = ref<Dashboard | null>(null);
const fehler = ref("");
const meldung = ref("");
const statusmeldung = ref<HTMLElement | null>(null);
const inArbeit = ref(false);
const ablehnung = ref<number | null>(null);
const begruendung = ref("");

async function laden() {
  daten.value = await fetchDashboard();
}

async function ausfuehren(aktion: () => Promise<void>, erfolg: string) {
  if (inArbeit.value) return;
  inArbeit.value = true;
  fehler.value = meldung.value = "";
  try {
    await aktion();
    meldung.value = erfolg;
    ablehnung.value = null;
    begruendung.value = "";
    await laden();
    await nextTick();
    statusmeldung.value?.focus();
  } catch (error) {
    fehler.value = error instanceof Error ? error.message : "Die Aktion ist fehlgeschlagen.";
  } finally {
    inArbeit.value = false;
  }
}

const annehmen = (vorgang: DashboardVorgang) => ausfuehren(
  () => qualifikationsbewerbungGenehmigen(vorgang.id), "Der Vorgang wurde angenommen.");
const ablehnen = (vorgang: DashboardVorgang) => ausfuehren(
  () => qualifikationsbewerbungAblehnen(vorgang.id, begruendung.value), "Der Vorgang wurde abgelehnt.");
const zurueckziehen = (vorgang: DashboardVorgang) => ausfuehren(
  () => qualifikationsbewerbungZurueckziehen(vorgang.bezugId), "Der Vorgang wurde zurückgezogen.");

function datum(iso: string) {
  return new Intl.DateTimeFormat("de-DE", { dateStyle: "medium", timeStyle: "short" }).format(new Date(iso));
}

onMounted(() => laden().catch((error) => {
  fehler.value = error instanceof Error ? error.message : "Das Dashboard konnte nicht geladen werden.";
}));
</script>

<template>
  <main class="dashboard-page">
    <header class="section-heading compact-heading">
      <h1>Was liegt bei mir?</h1>
      <p>Entscheidungen, eigene Pflichten und Dringlichkeiten auf einen Blick.</p>
    </header>
    <p v-if="meldung" ref="statusmeldung" class="success" role="status" tabindex="-1">{{ meldung }}</p>
    <p v-if="fehler" class="form-error" role="alert">{{ fehler }}</p>
    <div v-if="!daten && !fehler" class="state" aria-busy="true">Dashboard wird geladen.</div>
    <template v-if="daten">
      <section class="dashboard-rank" aria-labelledby="vorgaenge-heading">
        <header><span class="rank-label">Rang 1</span><h2 id="vorgaenge-heading">Vorgänge</h2></header>
        <ul v-if="daten.vorgaenge.length" class="task-list">
          <li v-for="vorgang in daten.vorgaenge" :key="vorgang.id" class="task-row">
            <div>
              <strong>{{ vorgang.art }}</strong>
              <p>{{ vorgang.antragsteller }} · {{ vorgang.bezug }}</p>
              <small>Gestellt {{ datum(vorgang.erstelltAm) }}</small>
            </div>
            <div class="task-actions">
              <RouterLink :to="`/katalog/${vorgang.bezugId}`">Gegenstand öffnen</RouterLink>
              <button type="button" class="primary-action" :disabled="inArbeit" @click="annehmen(vorgang)">Annehmen</button>
              <button type="button" class="sekundaer-aktion danger-action" :disabled="inArbeit" @click="ablehnung = vorgang.id">Ablehnen</button>
            </div>
            <form v-if="ablehnung === vorgang.id" class="rejection-form" @submit.prevent="ablehnen(vorgang)">
              <label :for="`grund-${vorgang.id}`">Begründung</label>
              <textarea :id="`grund-${vorgang.id}`" v-model="begruendung" :disabled="inArbeit" required maxlength="1000" />
              <button class="sekundaer-aktion danger-action" type="submit" :disabled="inArbeit">{{ inArbeit ? "Wird ausgeführt …" : "Ablehnung bestätigen" }}</button>
            </form>
          </li>
        </ul>
        <p v-else class="empty-copy">Keine Vorgänge warten auf Ihre Entscheidung.</p>
      </section>

      <section class="dashboard-rank" aria-labelledby="pflichten-heading">
        <header><span class="rank-label">Rang 2</span><h2 id="pflichten-heading">Handlungspflichten</h2></header>
        <ul v-if="daten.pflichten.length" class="task-list">
          <li v-for="termin in daten.pflichten" :key="termin.terminId" class="task-row">
            <div><strong>Durchführung bestätigen</strong><p>{{ termin.schulungTitel }}</p>
              <small>{{ termin.startdatum }} bis {{ termin.enddatum }}</small></div>
            <RouterLink to="/planer#kalender">Termin öffnen</RouterLink>
          </li>
        </ul>
        <p v-else class="empty-copy">Keine eigenen Handlungspflichten.</p>
      </section>

      <section class="dashboard-rank" aria-labelledby="dringend-heading">
        <header><span class="rank-label">Rang 3</span><h2 id="dringend-heading">Dringlichkeiten</h2></header>
        <ul v-if="daten.dringlichkeiten.length" class="task-list">
          <li v-for="termin in daten.dringlichkeiten" :key="termin.terminId" class="task-row" :class="{ urgent: termin.dringend || termin.ueberfaellig }">
            <div><strong>{{ termin.schulungTitel }}</strong><p>
              {{ termin.ueberfaellig ? "Überfälliger Termin" : termin.ohneTrainer ? "Trainerzuweisung fehlt" : "Mindestteilnehmerzahl nicht erreicht" }}
            </p><small>{{ termin.startdatum }} bis {{ termin.enddatum }}</small></div>
            <RouterLink to="/planer#kalender">Termin öffnen</RouterLink>
          </li>
        </ul>
        <p v-else class="empty-copy">Keine Dringlichkeiten.</p>
      </section>

      <section class="dashboard-rank own-processes" aria-labelledby="eigene-heading">
        <header><h2 id="eigene-heading">Von mir gestellt</h2></header>
        <ul v-if="daten.eigeneVorgaenge.length" class="task-list">
          <li v-for="vorgang in daten.eigeneVorgaenge" :key="vorgang.id" class="task-row">
            <div><strong>{{ vorgang.art }}</strong><p>{{ vorgang.bezug }}</p><small>Offen seit {{ datum(vorgang.erstelltAm) }}</small></div>
            <button v-if="vorgang.zurueckziehbar" type="button" class="sekundaer-aktion danger-action" :disabled="inArbeit" @click="zurueckziehen(vorgang)">Zurückziehen</button>
          </li>
        </ul>
        <p v-else class="empty-copy">Keine offenen eigenen Vorgänge.</p>
      </section>

      <details class="completed-processes">
        <summary>Erledigte Vorgänge</summary>
        <ul class="task-list">
          <li v-for="vorgang in [...daten.erledigteVorgaenge, ...daten.erledigteEigeneVorgaenge]" :key="`${vorgang.richtung}-${vorgang.id}`" class="task-row">
            <div><strong>{{ vorgang.art }}</strong><p>{{ vorgang.bezug }} · {{ vorgang.status }}</p>
              <small v-if="vorgang.entschiedenAm">Erledigt {{ datum(vorgang.entschiedenAm) }}</small></div>
          </li>
        </ul>
        <p v-if="!daten.erledigteVorgaenge.length && !daten.erledigteEigeneVorgaenge.length" class="empty-copy">Noch keine erledigten Vorgänge.</p>
      </details>
    </template>
  </main>
</template>
