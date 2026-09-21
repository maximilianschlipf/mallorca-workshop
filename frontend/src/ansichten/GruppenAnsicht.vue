<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import {
  aendereGruppe, fetchGruppen, fetchKonten, gruppeMitgliedEntfernen, gruppeMitgliedHinzufuegen,
  legeGruppeAn, loescheGruppe,
} from "../api";
import { aktuellesKonto } from "../auth";
import type { Benutzerkonto, Gruppe } from "../types";

const gruppen = ref<Gruppe[]>([]);
const konten = ref<Benutzerkonto[]>([]);
const neuerName = ref("");
const neuerTrainer = ref<string | null>(null);
const auswahl = ref<Record<string, string>>({});
const fehler = ref("");
const meldung = ref("");
const laedt = ref(true);
const istAdministrator = computed(() => aktuellesKonto.value?.rollen.includes("ADMINISTRATOR"));
const trainerKonten = computed(() => konten.value.filter((k) => k.zustand === "AKTIV" && k.rollen.includes("TRAINER")));

function kandidaten(gruppe: Gruppe) {
  const vergeben = new Set([...gruppe.mitglieder.map((m) => m.id), gruppe.trainer?.id]);
  return konten.value.filter((k) => k.zustand === "AKTIV" && !vergeben.has(k.id));
}

async function laden() {
  laedt.value = true;
  try {
    gruppen.value = await fetchGruppen();
    if (istAdministrator.value) konten.value = await fetchKonten();
  } catch (error) {
    fehler.value = error instanceof Error ? error.message : "Gruppen konnten nicht geladen werden.";
  } finally {
    laedt.value = false;
  }
}

async function ausfuehren(aktion: () => Promise<unknown>, erfolg: string) {
  fehler.value = meldung.value = "";
  try {
    await aktion();
    meldung.value = erfolg;
    await laden();
  } catch (error) {
    fehler.value = error instanceof Error ? error.message : "Die Aktion ist fehlgeschlagen.";
  }
}

const anlegen = () => ausfuehren(async () => {
  await legeGruppeAn(neuerName.value, neuerTrainer.value, []);
  neuerName.value = "";
  neuerTrainer.value = null;
}, "Die Gruppe wurde angelegt.");

const trainerSetzen = (gruppe: Gruppe, trainerId: string | null) =>
  ausfuehren(() => aendereGruppe(gruppe.id, gruppe.name, trainerId), "Der Gruppentrainer wurde geändert.");

function mitgliedHinzufuegen(gruppe: Gruppe) {
  const kontoId = auswahl.value[gruppe.id];
  if (!kontoId) return;
  auswahl.value[gruppe.id] = "";
  return ausfuehren(() => gruppeMitgliedHinzufuegen(gruppe.id, kontoId), "Das Mitglied wurde hinzugefügt.");
}

const mitgliedEntfernen = (gruppe: Gruppe, kontoId: string) =>
  ausfuehren(() => gruppeMitgliedEntfernen(gruppe.id, kontoId), "Das Mitglied wurde entfernt.");

function loeschen(gruppe: Gruppe) {
  if (!window.confirm(`Gruppe „${gruppe.name}“ löschen?`)) return;
  return ausfuehren(() => loescheGruppe(gruppe.id), "Die Gruppe wurde gelöscht.");
}

onMounted(laden);
</script>

<template>
  <main class="content-page">
    <div class="section-heading compact-heading">
      <p class="eyebrow">Planung</p>
      <h1>Gruppen</h1>
      <p>Eine Gruppe fasst Mitarbeitende und ihren Gruppentrainer zusammen. Termine können einer Gruppe zugeordnet werden.</p>
    </div>
    <p v-if="meldung" class="success" role="status">{{ meldung }}</p>
    <p v-if="fehler" class="form-error" role="alert">{{ fehler }}</p>

    <form v-if="istAdministrator" class="schulungsformular gruppen-formular" @submit.prevent="anlegen">
      <label>Neue Gruppe
        <input v-model="neuerName" type="text" maxlength="255" required placeholder="z. B. Mallorca" />
      </label>
      <label>Gruppentrainer (optional)
        <select v-model="neuerTrainer">
          <option :value="null">Kein Gruppentrainer</option>
          <option v-for="konto in trainerKonten" :key="konto.id" :value="konto.id">{{ konto.name }}</option>
        </select>
      </label>
      <button class="primary-action" type="submit">Gruppe anlegen</button>
    </form>

    <p v-if="laedt" aria-busy="true">Gruppen werden geladen.</p>
    <p v-else-if="!gruppen.length">Es gibt noch keine Gruppen.</p>
    <div v-else class="konten-liste">
      <article v-for="gruppe in gruppen" :key="gruppe.id" class="verwaltetes-konto gruppe">
        <div class="konto-identitaet">
          <h2>{{ gruppe.name }}</h2>
          <p class="konto-rollen">
            <strong>Gruppentrainer:</strong> {{ gruppe.trainer?.name ?? "nicht festgelegt" }}
          </p>
          <p class="konto-rollen">
            <strong>Mitglieder ({{ gruppe.mitglieder.length }}):</strong>
          </p>
          <ul class="gruppen-mitglieder">
            <li v-for="mitglied in gruppe.mitglieder" :key="mitglied.id">
              {{ mitglied.name }}
              <button
                v-if="istAdministrator" class="sekundaer-aktion" type="button"
                :aria-label="`${mitglied.name} aus ${gruppe.name} entfernen`"
                @click="mitgliedEntfernen(gruppe, mitglied.id)"
              >Entfernen</button>
            </li>
          </ul>
          <p class="konto-rollen"><strong>Termine:</strong> {{ gruppe.anzahlTermine }}</p>
        </div>
        <div v-if="istAdministrator" class="konto-aktionen" :aria-label="`Aktionen für ${gruppe.name}`">
          <label>Gruppentrainer
            <select
              :value="gruppe.trainer?.id ?? ''"
              @change="trainerSetzen(gruppe, ($event.target as HTMLSelectElement).value || null)"
            >
              <option value="">Kein Gruppentrainer</option>
              <option
                v-for="konto in trainerKonten" :key="konto.id" :value="konto.id"
                :disabled="gruppe.mitglieder.some((m) => m.id === konto.id)"
              >{{ konto.name }}</option>
            </select>
          </label>
          <label>Mitglied hinzufügen
            <select v-model="auswahl[gruppe.id]" @change="mitgliedHinzufuegen(gruppe)">
              <option value="">Person auswählen</option>
              <option v-for="konto in kandidaten(gruppe)" :key="konto.id" :value="konto.id">{{ konto.name }}</option>
            </select>
          </label>
          <button class="sekundaer-aktion" type="button" @click="loeschen(gruppe)">Gruppe löschen</button>
        </div>
      </article>
    </div>
  </main>
</template>
