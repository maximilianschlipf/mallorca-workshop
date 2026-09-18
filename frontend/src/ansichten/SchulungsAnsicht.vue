<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { fetchSchulung } from "../api";
import { aktuellesKonto } from "../auth";
import type { Schulung } from "../types";
import LadeZustand from "../komponenten/LadeZustand.vue";
import ZustandsSchild from "../komponenten/ZustandsSchild.vue";

const props = defineProps<{ id: string }>();

const istAdministrator = computed(() =>
  aktuellesKonto.value?.rollen.includes("ADMINISTRATOR") ?? false,
);

const schulung = ref<Schulung | null>(null);
const laedt = ref(true);
const fehler = ref<string | null>(null);

async function laden() {
  laedt.value = true;
  fehler.value = null;
  try {
    schulung.value = await fetchSchulung(props.id);
  } catch (ursache) {
    fehler.value = ursache instanceof Error ? ursache.message : "Unbekannter Fehler";
  } finally {
    laedt.value = false;
  }
}

function zeitraum(von: string, bis: string) {
  const format = new Intl.DateTimeFormat("de-DE", { dateStyle: "medium" });
  const anfang = format.format(new Date(von));
  return von === bis ? anfang : `${anfang} – ${format.format(new Date(bis))}`;
}

watch(() => props.id, laden);
onMounted(laden);
</script>

<template>
  <section class="page-section">
    <LadeZustand :laedt="laedt" :fehler="fehler" gegenstand="Die Schulung">
      <template v-if="schulung">
        <div class="section-heading">
          <p class="kennung">{{ schulung.id }}</p>
          <h1 data-testid="schulung-titel">{{ schulung.titel }}</h1>
          <p>{{ schulung.kurzbeschreibung }}</p>
        </div>

        <p v-if="schulung.zustand === 'ARCHIVIERT'" data-testid="archiviert" class="state">
          Diese Schulung ist archiviert. Zu ihr können keine neuen Termine
          angelegt werden; bestehende finden weiterhin statt.
        </p>
        <ZustandsSchild v-else :zustand="schulung.zustand" />

        <div class="verwaltung-aktionen">
          <RouterLink
            v-if="istAdministrator"
            class="primary-action"
            :to="`/katalog/${schulung.id}/bearbeiten`"
            >Bearbeiten</RouterLink
          >
          <RouterLink class="sekundaer-action" to="/katalog"
            >Zurück zum Katalog</RouterLink
          >
        </div>

        <dl class="angaben">
          <div>
            <dt>Kategorie</dt>
            <dd data-testid="angabe-kategorie">{{ schulung.kategorie }}</dd>
          </div>
          <div>
            <dt>Dauer</dt>
            <dd data-testid="angabe-dauer">
              {{ schulung.dauerInTagen }}
              {{ schulung.dauerInTagen === 1 ? "Tag" : "Tage" }}
            </dd>
          </div>
          <div>
            <dt>Mindestteilnehmerzahl</dt>
            <dd data-testid="angabe-mindest">
              {{ schulung.mindestteilnehmerExklusiv }} für exklusive Termine
            </dd>
          </div>
          <div>
            <dt>Höchstteilnehmerzahl</dt>
            <dd data-testid="angabe-hoechst">
              <!-- REQ_KAT_FELD_04: Fehlt die Angabe, gilt keine Obergrenze. -->
              {{
                schulung.maxTeilnehmerOeffentlich
                  ? `${schulung.maxTeilnehmerOeffentlich} für öffentliche Termine`
                  : "Keine Obergrenze"
              }}
            </dd>
          </div>
        </dl>

        <h2>Voraussetzungen</h2>
        <ul v-if="schulung.voraussetzungen.length" data-testid="voraussetzungen">
          <li v-for="(text, index) in schulung.voraussetzungen" :key="index">
            {{ text }}
          </li>
        </ul>
        <p v-else class="state">Für diese Schulung sind keine Voraussetzungen vermerkt.</p>

        <h2>Öffentliche Termine</h2>
        <ul v-if="schulung.oeffentlicheTermine.length" data-testid="termine">
          <li v-for="termin in schulung.oeffentlicheTermine" :key="termin.terminId">
            {{ zeitraum(termin.startdatum, termin.enddatum) }} · {{ termin.ort }} ·
            {{ termin.status }}
          </li>
        </ul>
        <p v-else class="state">Zu dieser Schulung ist derzeit kein Termin geplant.</p>
      </template>
    </LadeZustand>
  </section>
</template>
