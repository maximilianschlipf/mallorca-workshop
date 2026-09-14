<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import {
  aendereSchulung,
  ApiFehler,
  fetchKategorien,
  fetchKennungsschema,
  fetchSchulung,
  legeSchulungAn,
} from "../api";
import type { Feldfehler, Kennungsschema, Warnung } from "../types";
import LadeZustand from "../komponenten/LadeZustand.vue";

const props = defineProps<{ id?: string }>();
const router = useRouter();

const bearbeitet = computed(() => props.id !== undefined);

const kennung = ref("");
const titel = ref("");
const kategorie = ref("");
const kurzbeschreibung = ref("");
/** Eine Voraussetzung je Zeile -- das ist die Schreibweise, die Menschen erwarten. */
const voraussetzungen = ref("");
// Vue wandelt v-model bei type="number" selbsttaetig in eine Zahl um, laesst
// ein leeres Feld aber als leere Zeichenkette stehen. Beide Formen kommen hier
// also an, und beide muessen durch.
const dauerInTagen = ref<string | number>("");
const mindestteilnehmerExklusiv = ref<string | number>("");
const maxTeilnehmerOeffentlich = ref<string | number>("");

const kategorien = ref<string[]>([]);
const schema = ref<Kennungsschema | null>(null);
const laedt = ref(true);
const ladefehler = ref<string | null>(null);
const speichert = ref(false);
const fehler = ref<Feldfehler[]>([]);
const warnungen = ref<Warnung[]>([]);

function fehlerAn(feld: string) {
  return fehler.value.find((f) => f.feld === feld)?.meldung;
}

const allgemeineFehler = computed(() =>
  fehler.value.filter((f) => f.feld === null),
);

async function speichern() {
  speichert.value = true;
  fehler.value = [];
  warnungen.value = [];
  try {
    const eingabe = {
      id: kennung.value,
      titel: titel.value,
      kategorie: kategorie.value,
      kurzbeschreibung: kurzbeschreibung.value,
      voraussetzungen: voraussetzungen.value
        .split("\n")
        .map((zeile) => zeile.trim())
        .filter((zeile) => zeile !== ""),
      dauerInTagen: alsZahl(dauerInTagen.value),
      mindestteilnehmerExklusiv: alsZahl(mindestteilnehmerExklusiv.value),
      maxTeilnehmerOeffentlich: alsZahl(maxTeilnehmerOeffentlich.value),
    };

    const antwort = bearbeitet.value
      ? await aendereSchulung(props.id!, eingabe)
      : await legeSchulungAn(eingabe);

    if (antwort.warnungen.length > 0) {
      // REQ_KAT_PFLEG_04: Die Warnung verhindert nichts -- die Änderung ist
      // bereits geschehen. Deshalb bleibt die Ansicht stehen und zeigt sie,
      // statt weiterzuspringen.
      warnungen.value = antwort.warnungen;
      return;
    }
    await router.push(`/katalog/${antwort.schulung.id}`);
  } catch (ursache) {
    if (ursache instanceof ApiFehler && ursache.fehler.length > 0) {
      fehler.value = ursache.fehler;
    } else {
      fehler.value = [
        {
          feld: null,
          code: null,
          meldung:
            ursache instanceof Error ? ursache.message : "Unbekannter Fehler",
        },
      ];
    }
  } finally {
    speichert.value = false;
  }
}

/** Leer bleibt leer: Das Backend unterscheidet "fehlt" von "ist 0". */
function alsZahl(wert: string | number): number | null {
  if (typeof wert === "number") return Number.isNaN(wert) ? null : wert;
  return wert.trim() === "" ? null : Number(wert);
}

onMounted(async () => {
  try {
    kategorien.value = await fetchKategorien();
    if (bearbeitet.value) {
      const schulung = await fetchSchulung(props.id!);
      kennung.value = schulung.id;
      titel.value = schulung.titel;
      kategorie.value = schulung.kategorie;
      kurzbeschreibung.value = schulung.kurzbeschreibung;
      voraussetzungen.value = schulung.voraussetzungen.join("\n");
      dauerInTagen.value = String(schulung.dauerInTagen);
      mindestteilnehmerExklusiv.value = String(schulung.mindestteilnehmerExklusiv);
      maxTeilnehmerOeffentlich.value = schulung.maxTeilnehmerOeffentlich
        ? String(schulung.maxTeilnehmerOeffentlich)
        : "";
    } else {
      schema.value = await fetchKennungsschema();
    }
  } catch (ursache) {
    ladefehler.value =
      ursache instanceof Error ? ursache.message : "Unbekannter Fehler";
  } finally {
    laedt.value = false;
  }
});
</script>

<template>
  <section class="page-section">
    <div class="section-heading">
      <h1>{{ bearbeitet ? "Schulung bearbeiten" : "Schulung anlegen" }}</h1>
      <p>
        Die Beschreibung landet als JSON-Datei im Katalog und wird als Commit
        gesichert.
      </p>
    </div>

    <LadeZustand :laedt="laedt" :fehler="ladefehler" gegenstand="Das Formular">
      <div v-if="warnungen.length" class="state state-warnung" data-testid="warnung" role="status">
        <p>Gespeichert, mit Hinweis:</p>
        <p v-for="warnung in warnungen" :key="warnung.code">{{ warnung.meldung }}</p>
        <RouterLink class="primary-action" :to="`/katalog/${kennung}`"
          >Weiter zur Schulung</RouterLink
        >
      </div>

      <div v-if="allgemeineFehler.length" class="state state-error" role="alert">
        <p v-for="(eintrag, index) in allgemeineFehler" :key="index">
          {{ eintrag.meldung }}
        </p>
      </div>

      <form class="schulungsformular" @submit.prevent="speichern">
        <div class="filter-field">
          <label for="feld-id">Kennung</label>
          <input
            id="feld-id"
            v-model="kennung"
            type="text"
            autocomplete="off"
            :disabled="bearbeitet"
            :aria-describedby="bearbeitet ? undefined : 'kennung-hilfe'"
          />
          <!--
            REQ_KAT_ID_01: Das Schema der bestehenden Kennungen wird gezeigt,
            die Kennung selbst aber weder vorbelegt noch vorgeschlagen.
          -->
          <small v-if="!bearbeitet && schema?.muster" id="kennung-hilfe" data-testid="kennungsschema">
            Bestehende Kennungen folgen dem Muster {{ schema.muster }}, zum
            Beispiel {{ schema.beispiele.join(", ") }}. Großbuchstaben, Ziffern
            und Bindestriche.
          </small>
          <small v-else-if="bearbeitet">
            Die Kennung benennt die Katalogdatei und bindet die Termine an. Sie
            lässt sich nach dem Anlegen nicht mehr ändern.
          </small>
          <p v-if="fehlerAn('id')" class="feldfehler" data-testid="fehler-id">
            {{ fehlerAn("id") }}
          </p>
        </div>

        <div class="filter-field">
          <label for="feld-titel">Titel</label>
          <input id="feld-titel" v-model="titel" type="text" />
          <p v-if="fehlerAn('titel')" class="feldfehler" data-testid="fehler-titel">
            {{ fehlerAn("titel") }}
          </p>
        </div>

        <div class="filter-field">
          <label for="feld-kategorie">Kategorie</label>
          <!--
            REQ_KAT_KATG_02: ausgewählt, nicht eingetippt. Eine neue Kategorie
            entsteht nur über die Kategorienpflege.
          -->
          <select id="feld-kategorie" v-model="kategorie">
            <option value="" disabled>Kategorie auswählen</option>
            <option v-for="kat in kategorien" :key="kat" :value="kat">{{ kat }}</option>
          </select>
          <small>
            Fehlt eine Kategorie, lege sie zuerst unter
            <RouterLink to="/kategorien">Kategorien pflegen</RouterLink> an.
          </small>
          <p v-if="fehlerAn('kategorie')" class="feldfehler" data-testid="fehler-kategorie">
            {{ fehlerAn("kategorie") }}
          </p>
        </div>

        <div class="filter-field breit">
          <label for="feld-kurzbeschreibung">Kurzbeschreibung</label>
          <textarea id="feld-kurzbeschreibung" v-model="kurzbeschreibung" rows="3"></textarea>
          <p
            v-if="fehlerAn('kurzbeschreibung')"
            class="feldfehler"
            data-testid="fehler-kurzbeschreibung"
          >
            {{ fehlerAn("kurzbeschreibung") }}
          </p>
        </div>

        <div class="filter-field breit">
          <label for="feld-voraussetzungen">Voraussetzungen</label>
          <textarea id="feld-voraussetzungen" v-model="voraussetzungen" rows="3"></textarea>
          <small>
            Eine je Zeile, als freier Text. Sie verweisen nicht auf andere
            Schulungen und werden nicht geprüft. Darf leer bleiben.
          </small>
          <p
            v-if="fehlerAn('voraussetzungen')"
            class="feldfehler"
            data-testid="fehler-voraussetzungen"
          >
            {{ fehlerAn("voraussetzungen") }}
          </p>
        </div>

        <div class="filter-field">
          <label for="feld-dauer">Dauer in Tagen</label>
          <input id="feld-dauer" v-model="dauerInTagen" type="number" min="1" />
          <p v-if="fehlerAn('dauerInTagen')" class="feldfehler" data-testid="fehler-dauerInTagen">
            {{ fehlerAn("dauerInTagen") }}
          </p>
        </div>

        <div class="filter-field">
          <label for="feld-mindest">Mindestteilnehmerzahl</label>
          <input id="feld-mindest" v-model="mindestteilnehmerExklusiv" type="number" min="0" />
          <small>Gilt für exklusive Termine.</small>
          <p
            v-if="fehlerAn('mindestteilnehmerExklusiv')"
            class="feldfehler"
            data-testid="fehler-mindestteilnehmerExklusiv"
          >
            {{ fehlerAn("mindestteilnehmerExklusiv") }}
          </p>
        </div>

        <div class="filter-field">
          <label for="feld-hoechst">Höchstteilnehmerzahl</label>
          <input id="feld-hoechst" v-model="maxTeilnehmerOeffentlich" type="number" min="0" />
          <small>
            Gilt für öffentliche Termine. Leer oder 0 bedeutet: keine Obergrenze.
          </small>
          <p
            v-if="fehlerAn('maxTeilnehmerOeffentlich')"
            class="feldfehler"
            data-testid="fehler-maxTeilnehmerOeffentlich"
          >
            {{ fehlerAn("maxTeilnehmerOeffentlich") }}
          </p>
        </div>

        <div class="formular-aktionen">
          <button class="primary-action" type="submit" :disabled="speichert">
            Speichern
          </button>
          <RouterLink class="sekundaer-action" :to="bearbeitet ? `/katalog/${props.id}` : '/katalog'"
            >Abbrechen</RouterLink
          >
        </div>
      </form>
    </LadeZustand>
  </section>
</template>
