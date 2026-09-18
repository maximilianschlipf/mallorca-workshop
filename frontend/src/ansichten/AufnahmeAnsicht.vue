<script setup lang="ts">
import { computed, ref, useTemplateRef } from "vue";
import { ApiFehler, nimmDateienAuf } from "../api";
import type { Aufnahmebericht, Aufnahmeergebnis } from "../types";

const dateien = ref<File[]>([]);
const ersetzen = ref(false);
const laeuft = ref(false);
const meldung = ref<string | null>(null);
const bericht = ref<Aufnahmebericht | null>(null);

const bereit = computed(() => dateien.value.length > 0 && !laeuft.value);

const dateiEingabe = useTemplateRef<HTMLInputElement>("dateiEingabe");

/**
 * Der Stand der Auswahl in eigener Sprache. Das native Feld beschriftet sich
 * selbst in der Browsersprache -- auf einer deutschen Oberfläche stünde dort
 * "No file chosen".
 */
const dateistand = computed(() => {
  if (dateien.value.length === 0) return "Keine Datei gewählt";
  if (dateien.value.length === 1) return dateien.value[0].name;
  return `${dateien.value.length} Dateien gewählt`;
});

const ERGEBNISTEXT: Record<Aufnahmeergebnis, string> = {
  AUFGENOMMEN: "Aufgenommen",
  ERSETZT: "Ersetzt",
  ABGEWIESEN: "Abgewiesen",
  ENTSCHEIDUNG_OFFEN: "Entscheidung offen",
};

function dateienGewaehlt(ereignis: Event) {
  const eingabe = ereignis.target as HTMLInputElement;
  dateien.value = Array.from(eingabe.files ?? []);
  bericht.value = null;
  meldung.value = null;
}

async function aufnehmen() {
  laeuft.value = true;
  meldung.value = null;
  try {
    bericht.value = await nimmDateienAuf(dateien.value, ersetzen.value);
  } catch (fehler) {
    meldung.value =
      fehler instanceof ApiFehler || fehler instanceof Error
        ? fehler.message
        : "Unbekannter Fehler";
  } finally {
    laeuft.value = false;
  }
}
</script>

<template>
  <section class="page-section">
    <div class="section-heading">
      <h1>Dateien aufnehmen</h1>
      <p>
        Bereitgestellte JSON-Dateien werden gegen dieselben Regeln geprüft wie
        eine Eingabe von Hand. Eine abgewiesene Datei hält die übrigen nicht
        auf.
      </p>
    </div>

    <form class="filter-bar" @submit.prevent="aufnehmen">
      <div class="filter-field">
        <label for="aufnahme-dateien">JSON-Dateien</label>
        <!--
          Das native Feld bleibt der Bedienung und den Hilfsmitteln erhalten,
          tritt aber hinter eine eigene Schaltfläche zurück: Seine Beschriftung
          kommt aus dem Browser und ließe sich sonst nicht übersetzen.
        -->
        <input
          id="aufnahme-dateien"
          ref="dateiEingabe"
          class="sr-only"
          type="file"
          tabindex="-1"
          accept="application/json,.json"
          multiple
          @change="dateienGewaehlt"
        />
        <div class="dateiwahl">
          <button
            type="button"
            class="dateiwahl-knopf"
            @click="dateiEingabe?.click()"
          >
            Dateien wählen
          </button>
          <span class="dateiwahl-stand">{{ dateistand }}</span>
        </div>
      </div>
      <div class="filter-field">
        <label for="aufnahme-ersetzen">
          <input id="aufnahme-ersetzen" v-model="ersetzen" type="checkbox" />
          Bestehende Schulungen ersetzen
        </label>
        <!--
          REQ_KAT_IMP_03: Ohne diese ausdrückliche Entscheidung bleibt eine
          bestehende Schulung unverändert.
        -->
        <small>
          Ohne Häkchen bleibt eine Schulung mit bereits vergebener Kennung
          unverändert; die Datei wird dann als offene Entscheidung gemeldet.
        </small>
      </div>
      <button class="primary-action" type="submit" :disabled="!bereit">
        Aufnehmen
      </button>
    </form>

    <div v-if="meldung" class="state state-error" role="alert">
      <p>Die Aufnahme konnte nicht ausgeführt werden.</p>
      <small>{{ meldung }}</small>
    </div>

    <template v-if="bericht">
      <h2>Ergebnis</h2>
      <p role="status">
        {{ bericht.aufgenommen }} aufgenommen, {{ bericht.abgewiesen }}
        abgewiesen, {{ bericht.entscheidungOffen }} offen.
      </p>
      <ul class="aufnahmebericht" data-testid="aufnahmebericht">
        <li
          v-for="eintrag in bericht.ergebnisse"
          :key="eintrag.dateiname"
          :data-datei="eintrag.dateiname"
        >
          <strong>{{ eintrag.dateiname }}</strong>
          <span> — {{ ERGEBNISTEXT[eintrag.ergebnis] }}</span>
          <RouterLink
            v-if="eintrag.schulungId && eintrag.ergebnis !== 'ABGEWIESEN'"
            :to="`/katalog/${eintrag.schulungId}`"
          >
            {{ eintrag.schulungId }}
          </RouterLink>
          <ul v-if="eintrag.fehler.length">
            <li v-for="(fehler, index) in eintrag.fehler" :key="index">
              {{ fehler.meldung }}
            </li>
          </ul>
        </li>
      </ul>
    </template>

    <RouterLink class="sekundaer-action" to="/katalog">Zurück zum Katalog</RouterLink>
  </section>
</template>
