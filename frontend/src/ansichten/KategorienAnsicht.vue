<script setup lang="ts">
import { onMounted, ref } from "vue";
import {
  ApiFehler,
  benenneKategorieUm,
  fetchKategorien,
  legeKategorieAn,
  loescheKategorie,
} from "../api";
import LadeZustand from "../komponenten/LadeZustand.vue";

const kategorien = ref<string[]>([]);
const laedt = ref(true);
const ladefehler = ref<string | null>(null);
const meldung = ref<string | null>(null);

const neueKategorie = ref("");
const umzubenennen = ref<string | null>(null);
const neuerName = ref("");

async function laden() {
  laedt.value = true;
  ladefehler.value = null;
  try {
    kategorien.value = await fetchKategorien();
  } catch (fehler) {
    ladefehler.value = alsText(fehler);
  } finally {
    laedt.value = false;
  }
}

async function vorgang(tun: () => Promise<string[] | void>) {
  meldung.value = null;
  try {
    const liste = await tun();
    // Die Schreibvorgänge liefern die neue Liste gleich mit; nur das Löschen
    // antwortet ohne Inhalt und braucht ein Nachladen.
    if (liste) kategorien.value = liste;
    else await laden();
  } catch (fehler) {
    meldung.value = alsText(fehler);
  }
}

async function anlegen() {
  const name = neueKategorie.value;
  await vorgang(() => legeKategorieAn(name));
  if (!meldung.value) neueKategorie.value = "";
}

async function umbenennenBestaetigen() {
  const alt = umzubenennen.value;
  const neu = neuerName.value;
  if (!alt) return;
  await vorgang(() => benenneKategorieUm(alt, neu));
  if (!meldung.value) {
    umzubenennen.value = null;
    neuerName.value = "";
  }
}

function umbenennenBeginnen(name: string) {
  umzubenennen.value = name;
  neuerName.value = name;
  meldung.value = null;
}

function alsText(fehler: unknown) {
  if (fehler instanceof ApiFehler) return fehler.message;
  return fehler instanceof Error ? fehler.message : "Unbekannter Fehler";
}

onMounted(laden);
</script>

<template>
  <section class="page-section">
    <div class="section-heading">
      <h1>Kategorien pflegen</h1>
      <p>
        Die Kategorien sind eine eigene, gepflegte Liste. Beim Anlegen einer
        Schulung wird daraus ausgewählt -- eine neue Kategorie entsteht nur
        hier. Sonst stünden über die Zeit „IT-Security“ und „IT Security“
        nebeneinander, und der Filter zerfiele in zwei halb gefüllte Gruppen.
      </p>
    </div>

    <form class="filter-bar" @submit.prevent="anlegen">
      <div class="filter-field">
        <label for="neue-kategorie">Neue Kategorie</label>
        <input id="neue-kategorie" v-model="neueKategorie" type="text" autocomplete="off" />
      </div>
      <button class="primary-action" type="submit">Kategorie anlegen</button>
    </form>

    <div v-if="meldung" class="state state-error" role="alert">
      <p>Der Vorgang wurde abgewiesen.</p>
      <small>{{ meldung }}</small>
    </div>

    <LadeZustand :laedt="laedt" :fehler="ladefehler" gegenstand="Die Kategorien">
      <ul class="kategorienliste" data-testid="kategorienliste">
        <li v-for="name in kategorien" :key="name">
          <span>{{ name }}</span>

          <template v-if="umzubenennen === name">
            <label class="sr-only" :for="`neuer-name-${name}`">Neuer Name</label>
            <input
              :id="`neuer-name-${name}`"
              v-model="neuerName"
              type="text"
              autocomplete="off"
              aria-label="Neuer Name"
            />
            <button type="button" @click="umbenennenBestaetigen">
              Umbenennen bestätigen
            </button>
            <button type="button" @click="umzubenennen = null">Abbrechen</button>
          </template>

          <template v-else>
            <button type="button" @click="umbenennenBeginnen(name)">Umbenennen</button>
            <button
              type="button"
              class="gefahr"
              @click="vorgang(() => loescheKategorie(name))"
            >
              Löschen
            </button>
          </template>
        </li>
      </ul>
    </LadeZustand>

    <RouterLink class="sekundaer-action" to="/katalog">Zurück zum Katalog</RouterLink>
  </section>
</template>
