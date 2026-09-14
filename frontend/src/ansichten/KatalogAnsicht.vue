<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import {
  ApiFehler,
  archiviereSchulung,
  fetchKategorien,
  fetchSchulungen,
  loescheSchulung,
  reaktiviereSchulung,
} from "../api";
import type { Schulung } from "../types";
import LadeZustand from "../komponenten/LadeZustand.vue";
import RueckfrageDialog from "../komponenten/RueckfrageDialog.vue";
import ZustandsSchild from "../komponenten/ZustandsSchild.vue";

const schulungen = ref<Schulung[]>([]);
const kategorien = ref<string[]>([]);
const laedt = ref(true);
const ladefehler = ref<string | null>(null);
const meldung = ref<string | null>(null);

const suche = ref("");
const kategorie = ref("");
const zuLoeschen = ref<Schulung | null>(null);

const filterAktiv = computed(
  () => suche.value.trim() !== "" || kategorie.value !== "",
);

async function laden() {
  laedt.value = true;
  ladefehler.value = null;
  try {
    schulungen.value = await fetchSchulungen({
      suche: suche.value,
      kategorie: kategorie.value,
    });
  } catch (fehler) {
    ladefehler.value = alsText(fehler);
  } finally {
    laedt.value = false;
  }
}

/**
 * Führt einen Vorgang aus und lädt danach neu.
 *
 * Neu laden statt die Liste vor Ort zu ändern: Archivieren verschiebt die
 * Schulung ans Ende (REQ_KAT_SICHT_02), und diese Reihenfolge bestimmt das
 * Backend. Sie hier nachzubilden hiesse, dieselbe Regel zweimal zu pflegen.
 */
async function vorgang(tun: () => Promise<unknown>) {
  meldung.value = null;
  try {
    await tun();
    await laden();
  } catch (fehler) {
    meldung.value = alsText(fehler);
  }
}

function loeschenBestaetigt() {
  const schulung = zuLoeschen.value;
  zuLoeschen.value = null;
  if (schulung) vorgang(() => loescheSchulung(schulung.id));
}

function filterZuruecksetzen() {
  suche.value = "";
  kategorie.value = "";
}

function alsText(fehler: unknown) {
  if (fehler instanceof ApiFehler) return fehler.message;
  return fehler instanceof Error ? fehler.message : "Unbekannter Fehler";
}

let entprellung: ReturnType<typeof setTimeout> | undefined;
watch([suche, kategorie], () => {
  clearTimeout(entprellung);
  entprellung = setTimeout(laden, 250);
});

onMounted(async () => {
  try {
    kategorien.value = await fetchKategorien();
  } catch {
    kategorien.value = [];
  }
  await laden();
});
</script>

<template>
  <section class="page-section">
    <div class="section-heading">
      <h1>Katalog verwalten</h1>
      <p>
        Schulungen anlegen, beschreiben, archivieren und löschen. Änderungen
        werden im Repository als Commit gesichert.
      </p>
    </div>

    <div class="verwaltung-aktionen">
      <RouterLink class="primary-action" to="/katalog/neu"
        >Schulung anlegen</RouterLink
      >
      <RouterLink class="sekundaer-action" to="/katalog/aufnahme"
        >Dateien aufnehmen</RouterLink
      >
      <RouterLink class="sekundaer-action" to="/kategorien"
        >Kategorien pflegen</RouterLink
      >
    </div>

    <form class="filter-bar" role="search" @submit.prevent>
      <div class="filter-field">
        <label for="verwaltung-suche">Titel suchen</label>
        <input
          id="verwaltung-suche"
          v-model="suche"
          type="search"
          autocomplete="off"
          placeholder="z. B. Scrum, Kubernetes"
        />
      </div>
      <div class="filter-field">
        <label for="verwaltung-kategorie">Kategorie</label>
        <select id="verwaltung-kategorie" v-model="kategorie">
          <option value="">Alle Kategorien</option>
          <option v-for="kat in kategorien" :key="kat" :value="kat">
            {{ kat }}
          </option>
        </select>
      </div>
      <button
        type="button"
        class="filter-reset"
        :disabled="!filterAktiv"
        @click="filterZuruecksetzen"
      >
        Filter zurücksetzen
      </button>
    </form>

    <div v-if="meldung" class="state state-error" role="alert">
      <p>Der Vorgang wurde abgewiesen.</p>
      <small>{{ meldung }}</small>
    </div>

    <LadeZustand :laedt="laedt" :fehler="ladefehler" gegenstand="Der Katalog">
      <!--
        Eine Klammer um Liste und Leermeldung: Sie erscheint genau dann, wenn
        geladen ist, und gibt dem Test einen verlässlichen Anker.
      -->
      <div data-testid="katalogliste">
      <p v-if="schulungen.length === 0" class="state">
        Keine Schulung entspricht den gewählten Filtern.
      </p>

      <table v-else class="verwaltungstabelle">
        <caption class="sr-only">
          Schulungen des Katalogs, aktive vor archivierten
        </caption>
        <thead>
          <tr>
            <th scope="col">Kennung</th>
            <th scope="col">Titel</th>
            <th scope="col">Kategorie</th>
            <th scope="col">Zustand</th>
            <th scope="col">Termine</th>
            <th scope="col">Vorgänge</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="schulung in schulungen" :key="schulung.id">
            <td>
              <RouterLink
                :data-kennung="schulung.id"
                :to="`/katalog/${schulung.id}`"
                >{{ schulung.id }}</RouterLink
              >
            </td>
            <td>{{ schulung.titel }}</td>
            <td>{{ schulung.kategorie }}</td>
            <td><ZustandsSchild :zustand="schulung.zustand" /></td>
            <td>{{ schulung.oeffentlicheTermine.length }}</td>
            <td class="zeilen-aktionen">
              <RouterLink :to="`/katalog/${schulung.id}/bearbeiten`"
                >Bearbeiten</RouterLink
              >
              <button
                v-if="schulung.zustand === 'AKTIV'"
                type="button"
                @click="vorgang(() => archiviereSchulung(schulung.id))"
              >
                Archivieren
              </button>
              <button
                v-else
                type="button"
                @click="vorgang(() => reaktiviereSchulung(schulung.id))"
              >
                Reaktivieren
              </button>
              <button type="button" class="gefahr" @click="zuLoeschen = schulung">
                Löschen
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      </div>
    </LadeZustand>

    <RueckfrageDialog
      v-if="zuLoeschen"
      :titel="`${zuLoeschen.id} löschen?`"
      :text="`Die Katalogdatei von „${zuLoeschen.titel}“ wird entfernt und das Löschen als Commit gesichert. Das lässt sich nur über das Repository rückgängig machen.`"
      bestaetigung="Endgültig löschen"
      @bestaetigt="loeschenBestaetigt"
      @abgebrochen="zuLoeschen = null"
    />
  </section>
</template>
