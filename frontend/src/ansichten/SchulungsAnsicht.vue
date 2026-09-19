<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import {
  fetchQualifikationenDerSchulung, fetchSchulung, qualifikationDirektErteilen,
  qualifikationEntziehen, qualifikationsbewerbungAblehnen,
  qualifikationsbewerbungGenehmigen,
} from "../api";
import { aktuellesKonto } from "../auth";
import type { Qualifikationszeile, Schulung } from "../types";
import LadeZustand from "../komponenten/LadeZustand.vue";
import RueckfrageDialog from "../komponenten/RueckfrageDialog.vue";
import ZustandsSchild from "../komponenten/ZustandsSchild.vue";

const props = defineProps<{ id: string }>();

const istAdministrator = computed(() =>
  aktuellesKonto.value?.rollen.includes("ADMINISTRATOR") ?? false,
);

const schulung = ref<Schulung | null>(null);
const laedt = ref(true);
const fehler = ref<string | null>(null);
const aktionsfehler = ref<string | null>(null);
const qualifikationen = ref<Qualifikationszeile[]>([]);
const aktionsmeldung = ref<string | null>(null);
const abzulehnendeBewerbung = ref<Qualifikationszeile | null>(null);
const zuEntziehendeQualifikation = ref<Qualifikationszeile | null>(null);

async function laden() {
  laedt.value = true;
  fehler.value = aktionsfehler.value = null;
  try {
    schulung.value = await fetchSchulung(props.id);
  } catch (ursache) {
    fehler.value = ursache instanceof Error ? ursache.message : "Unbekannter Fehler";
    laedt.value = false;
    return;
  }
  if (istAdministrator.value) {
    try {
      qualifikationen.value = await fetchQualifikationenDerSchulung(props.id);
    } catch (ursache) {
      qualifikationen.value = [];
      aktionsfehler.value = ursache instanceof Error
        ? ursache.message : "Die Qualifikationen konnten nicht geladen werden.";
    }
  } else {
    qualifikationen.value = [];
  }
  laedt.value = false;
}

async function qualifikationsvorgang(aktion: () => Promise<void>, meldung: string) {
  aktionsfehler.value = aktionsmeldung.value = null;
  try {
    await aktion();
    aktionsmeldung.value = meldung;
  } catch (ursache) {
    aktionsfehler.value = ursache instanceof Error ? ursache.message : "Der Vorgang ist fehlgeschlagen.";
    return;
  }
  try {
    qualifikationen.value = await fetchQualifikationenDerSchulung(props.id);
  } catch {
    aktionsfehler.value = `${meldung} Der aktuelle Stand konnte aber nicht geladen werden.`;
  }
}

function ablehnen(begruendung?: string) {
  const zeile = abzulehnendeBewerbung.value;
  abzulehnendeBewerbung.value = null;
  if (!begruendung || zeile?.bewerbungId == null) return;
  return qualifikationsvorgang(
    () => qualifikationsbewerbungAblehnen(zeile.bewerbungId!, begruendung),
    "Die Bewerbung wurde abgelehnt.",
  );
}

function entziehen() {
  const zeile = zuEntziehendeQualifikation.value;
  zuEntziehendeQualifikation.value = null;
  if (!zeile) return;
  return qualifikationsvorgang(
    () => qualifikationEntziehen(props.id, zeile.trainerId),
    "Die Qualifikation wurde entzogen.",
  );
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
        <p v-if="aktionsmeldung" class="success" role="status">{{ aktionsmeldung }}</p>
        <p v-if="aktionsfehler" class="form-error" role="alert">{{ aktionsfehler }}</p>

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

        <template v-if="istAdministrator">
          <h2>Qualifikationen</h2>
          <table class="verwaltungstabelle">
            <caption class="sr-only">Qualifikationen und Bewerbungen für diese Schulung</caption>
            <thead><tr><th scope="col">Trainer</th><th scope="col">Status</th><th scope="col">Vorgänge</th></tr></thead>
            <tbody>
              <tr v-for="zeile in qualifikationen" :key="zeile.trainerId">
                <td>{{ zeile.trainerName }}</td><td>{{ zeile.status }}</td>
                <td class="zeilen-aktionen">
                  <template v-if="zeile.status === 'OFFEN' && zeile.bewerbungId != null">
                    <button type="button" :aria-label="`Bewerbung von ${zeile.trainerName} genehmigen`" @click="qualifikationsvorgang(
                      () => qualifikationsbewerbungGenehmigen(zeile.bewerbungId!),
                      'Die Bewerbung wurde genehmigt.')">Genehmigen</button>
                    <button type="button" :aria-label="`Bewerbung von ${zeile.trainerName} ablehnen`" @click="abzulehnendeBewerbung = zeile">Ablehnen</button>
                  </template>
                  <button v-else-if="zeile.status === 'QUALIFIZIERT'" type="button" class="gefahr"
                    :aria-label="`Qualifikation von ${zeile.trainerName} entziehen`"
                    @click="zuEntziehendeQualifikation = zeile">Entziehen</button>
                  <button v-else type="button" :aria-label="`${zeile.trainerName} direkt qualifizieren`" @click="qualifikationsvorgang(
                    () => qualifikationDirektErteilen(props.id, zeile.trainerId),
                    'Die Qualifikation wurde erteilt.')">Direkt qualifizieren</button>
                </td>
              </tr>
            </tbody>
          </table>
        </template>

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
    <RueckfrageDialog
      v-if="abzulehnendeBewerbung"
      :titel="`Bewerbung von ${abzulehnendeBewerbung.trainerName} ablehnen?`"
      text="Die Ablehnung wird dem Trainer mit der Begründung zugestellt."
      bestaetigung="Bewerbung ablehnen"
      eingabe-label="Begründung"
      :eingabe-maxlength="1000"
      @bestaetigt="ablehnen"
      @abgebrochen="abzulehnendeBewerbung = null"
    />
    <RueckfrageDialog
      v-if="zuEntziehendeQualifikation"
      :titel="`Qualifikation von ${zuEntziehendeQualifikation.trainerName} entziehen?`"
      text="Künftige Zuweisungen dieser Schulung werden aufgehoben."
      bestaetigung="Qualifikation entziehen"
      @bestaetigt="entziehen"
      @abgebrochen="zuEntziehendeQualifikation = null"
    />
  </section>
</template>
