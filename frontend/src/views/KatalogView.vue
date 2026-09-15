<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import {
  aufAssistenzplatzBewerben,
  aufQualifikationBewerben,
  fetchKategorien,
  fetchSchulungen,
  fetchVerfuegbareTrainer,
  trainerZuweisen,
} from "../api";
import { aktuellesKonto } from "../auth";
import type { Schulung, Termin, Trainer } from "../types";

interface KalenderTermin {
  schulungId: string;
  schulungTitel: string;
  termin: Termin;
}

const schulungen = ref<Schulung[]>([]);
const kategorien = ref<string[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);
const kalenderSchulungen = ref<Schulung[]>([]);
const kalenderLoading = ref(true);
const kalenderError = ref<string | null>(null);
const kalenderMonat = ref(
  new Date(new Date().getFullYear(), new Date().getMonth(), 1),
);
const ausgewaehlterTermin = ref<KalenderTermin | null>(null);
const aktionsmeldung = ref("");
const aktionsfehler = ref("");
const qualifikationsbewerbungen = ref(new Set<string>());
const assistenzbewerbungen = ref(new Set<string>());
const istTrainer = computed(() => aktuellesKonto.value?.rollen.includes("TRAINER"));
const istAdministrator = computed(() => aktuellesKonto.value?.rollen.includes("ADMINISTRATOR"));

const suche = ref("");
const kategorie = ref("");

const trainer = ref<Trainer[]>([]);
const trainerLoading = ref(false);
const trainerError = ref<string | null>(null);
const trainerGesucht = ref(false);
const trainerSchulungId = ref("");
const trainerVon = ref("");
const trainerBis = ref("");

const filterAktiv = computed(
  () => suche.value.trim() !== "" || kategorie.value !== "",
);

const cleanText = (text: string) => text.replace(/[—–]/g, "-");

const wochentage = [
  "Montag",
  "Dienstag",
  "Mittwoch",
  "Donnerstag",
  "Freitag",
  "Samstag",
  "Sonntag",
];
const monatsTitel = computed(() =>
  new Intl.DateTimeFormat("de-DE", {
    month: "long",
    year: "numeric",
  }).format(kalenderMonat.value),
);

const kalenderTermine = computed<KalenderTermin[]>(() =>
  kalenderSchulungen.value
    .flatMap((schulung) =>
      schulung.oeffentlicheTermine.map((termin) => ({
        schulungId: schulung.id,
        schulungTitel: schulung.titel,
        termin,
      })),
    )
    .sort((a, b) => a.termin.startdatum.localeCompare(b.termin.startdatum)),
);

const heuteIso = () => datumZuIso(new Date());

function datumZuIso(datum: Date) {
  const jahr = datum.getFullYear();
  const monat = String(datum.getMonth() + 1).padStart(2, "0");
  const tag = String(datum.getDate()).padStart(2, "0");
  return `${jahr}-${monat}-${tag}`;
}

function isoZuDatum(iso: string) {
  const [jahr, monat, tag] = iso.split("-").map(Number);
  return new Date(jahr, monat - 1, tag);
}

const kalenderTage = computed(() => {
  const monat = kalenderMonat.value;
  const ersterWochentag = (monat.getDay() + 6) % 7;
  const ersterTag = new Date(
    monat.getFullYear(),
    monat.getMonth(),
    1 - ersterWochentag,
  );

  return Array.from({ length: 42 }, (_, index) => {
    const datum = new Date(
      ersterTag.getFullYear(),
      ersterTag.getMonth(),
      ersterTag.getDate() + index,
    );
    const iso = datumZuIso(datum);
    return {
      iso,
      tag: datum.getDate(),
      imMonat: datum.getMonth() === monat.getMonth(),
      heute: iso === heuteIso(),
      termine: kalenderTermine.value.filter(
        ({ termin }) => termin.startdatum <= iso && termin.enddatum >= iso,
      ),
    };
  });
});

const termineImMonat = computed(() => {
  const monat = kalenderMonat.value;
  const von = datumZuIso(new Date(monat.getFullYear(), monat.getMonth(), 1));
  const bis = datumZuIso(
    new Date(monat.getFullYear(), monat.getMonth() + 1, 0),
  );
  return kalenderTermine.value.filter(
    ({ termin }) => termin.startdatum <= bis && termin.enddatum >= von,
  );
});

function monatWechseln(schritte: number) {
  kalenderMonat.value = new Date(
    kalenderMonat.value.getFullYear(),
    kalenderMonat.value.getMonth() + schritte,
    1,
  );
  ausgewaehlterTermin.value = null;
}

function zumAktuellenMonat() {
  const heute = new Date();
  kalenderMonat.value = new Date(heute.getFullYear(), heute.getMonth(), 1);
  ausgewaehlterTermin.value = null;
}

function datumText(iso: string) {
  return new Intl.DateTimeFormat("de-DE", { dateStyle: "long" }).format(
    isoZuDatum(iso),
  );
}

function zeitraumText(termin: Termin) {
  const format = new Intl.DateTimeFormat("de-DE", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
  const von = format.format(isoZuDatum(termin.startdatum));
  if (termin.startdatum === termin.enddatum) return von;
  return `${von} - ${format.format(isoZuDatum(termin.enddatum))}`;
}

function statusText(termin: Termin) {
  const status: Record<string, string> = {
    geplant: "Geplant",
    ausgebucht: "Ausgebucht",
    abgesagt: "Abgesagt",
  };
  const text = status[termin.status] ?? termin.status;
  return termin.enddatum < heuteIso() ? `${text}, vergangen` : text;
}

function terminAuswaehlen(eintrag: KalenderTermin) {
  ausgewaehlterTermin.value = eintrag;
  trainerSchulungId.value = eintrag.schulungId;
  trainerVon.value = eintrag.termin.startdatum;
  trainerBis.value = eintrag.termin.enddatum;
  trainer.value = [];
  trainerGesucht.value = false;
}

async function aktion(ausfuehren: () => Promise<void>, erfolg: string) {
  aktionsmeldung.value = aktionsfehler.value = "";
  try {
    await ausfuehren();
    aktionsmeldung.value = erfolg;
  } catch (err) {
    aktionsfehler.value = err instanceof Error ? err.message : "Die Aktion ist fehlgeschlagen.";
  }
}

async function qualifikationBewerben(schulungId: string) {
  await aktion(async () => {
    await aufQualifikationBewerben(schulungId);
    qualifikationsbewerbungen.value.add(schulungId);
  }, "Die Bewerbung auf die Qualifikation wurde eingereicht.");
}

async function assistenzBewerben(terminId: string) {
  await aktion(async () => {
    await aufAssistenzplatzBewerben(terminId);
    assistenzbewerbungen.value.add(terminId);
  }, "Die Bewerbung auf den Assistenzplatz wurde eingereicht.");
}

async function trainerEinsetzen(person: Trainer) {
  if (!ausgewaehlterTermin.value) return;
  await aktion(async () => {
    await trainerZuweisen(ausgewaehlterTermin.value!.termin.terminId, person.id);
    ausgewaehlterTermin.value!.termin.trainerId = person.id;
    ausgewaehlterTermin.value!.termin.trainerName = person.name;
  }, `${person.name} wurde dem Termin als Trainer zugewiesen.`);
}

async function ladeSchulungen(kalenderInitialisieren = false) {
  loading.value = true;
  error.value = null;
  if (kalenderInitialisieren) {
    kalenderLoading.value = true;
    kalenderError.value = null;
  }
  try {
    const ergebnis = await fetchSchulungen({
      suche: suche.value,
      kategorie: kategorie.value,
    });
    schulungen.value = ergebnis;
    if (kalenderInitialisieren) kalenderSchulungen.value = ergebnis;
  } catch (err) {
    error.value = err instanceof Error ? err.message : "Unbekannter Fehler";
    if (kalenderInitialisieren) kalenderError.value = error.value;
  } finally {
    loading.value = false;
    if (kalenderInitialisieren) kalenderLoading.value = false;
  }
}

function filterZuruecksetzen() {
  suche.value = "";
  kategorie.value = "";
}

async function trainerSuchen() {
  trainerGesucht.value = true;
  trainer.value = [];
  trainerError.value = null;

  if (trainerVon.value > trainerBis.value) {
    trainerError.value =
      "Das Anfangsdatum darf nicht nach dem Enddatum liegen.";
    return;
  }

  trainerLoading.value = true;
  try {
    trainer.value = await fetchVerfuegbareTrainer(
      trainerSchulungId.value,
      trainerVon.value,
      trainerBis.value,
    );
  } catch (err) {
    trainerError.value =
      err instanceof Error ? err.message : "Unbekannter Fehler";
  } finally {
    trainerLoading.value = false;
  }
}

let debounceTimer: ReturnType<typeof setTimeout> | undefined;
watch([suche, kategorie], () => {
  clearTimeout(debounceTimer);
  debounceTimer = setTimeout(ladeSchulungen, 250);
});

onMounted(async () => {
  try {
    kategorien.value = await fetchKategorien();
  } catch {
    kategorien.value = [];
  }
  await ladeSchulungen(true);
});
</script>

<template>
  <div>

    <section
      id="kalender"
      class="calendar-section"
      aria-labelledby="calendar-heading"
    >
      <div class="section-heading compact-heading">
        <h1 id="calendar-heading">Schulungskalender</h1>
        <p>
          Alle öffentlichen Schulungen mit Zeitraum, Ort und aktuellem Status.
        </p>
      </div>

      <div v-if="kalenderLoading" class="state" aria-busy="true">
        <p>Kalender wird geladen.</p>
      </div>

      <div v-else-if="kalenderError" class="state state-error" role="alert">
        <p>Der Kalender konnte nicht geladen werden.</p>
        <small>{{ cleanText(kalenderError) }}</small>
      </div>

      <template v-else>
        <div class="calendar-toolbar">
          <h3 aria-live="polite">{{ monatsTitel }}</h3>
          <div class="calendar-actions" aria-label="Monat auswählen">
            <button type="button" @click="monatWechseln(-1)">Zurück</button>
            <button type="button" @click="zumAktuellenMonat">Heute</button>
            <button type="button" @click="monatWechseln(1)">Weiter</button>
          </div>
        </div>

        <p
          v-if="termineImMonat.length === 0"
          class="calendar-empty"
          role="status"
        >
          In diesem Monat finden keine Schulungen statt.
        </p>

        <div
          class="calendar-desktop"
          role="region"
          :aria-label="`Schulungskalender ${monatsTitel}`"
        >
          <div
            v-for="wochentag in wochentage"
            :key="wochentag"
            class="calendar-weekday"
          >
            {{ wochentag }}
          </div>
          <div
            v-for="tag in kalenderTage"
            :key="tag.iso"
            class="calendar-day"
            :class="{ 'is-outside': !tag.imMonat, 'is-today': tag.heute }"
          >
            <time :datetime="tag.iso">{{ tag.tag }}</time>
            <button
              v-for="eintrag in tag.termine"
              :key="eintrag.termin.terminId"
              type="button"
              class="calendar-event"
              :class="[
                `status-${eintrag.termin.status}`,
                { 'is-past': eintrag.termin.enddatum < heuteIso() },
              ]"
              :aria-label="`${cleanText(eintrag.schulungTitel)}, ${datumText(tag.iso)}, ${statusText(eintrag.termin)}`"
              @click="terminAuswaehlen(eintrag)"
            >
              <span>{{ cleanText(eintrag.schulungTitel) }}</span>
              <small>{{ statusText(eintrag.termin) }}</small>
            </button>
          </div>
        </div>

        <ul v-if="termineImMonat.length" class="calendar-mobile">
          <li v-for="eintrag in termineImMonat" :key="eintrag.termin.terminId">
            <button type="button" @click="terminAuswaehlen(eintrag)">
              <time :datetime="eintrag.termin.startdatum">
                {{ zeitraumText(eintrag.termin) }}
              </time>
              <strong>{{ cleanText(eintrag.schulungTitel) }}</strong>
              <span>{{ statusText(eintrag.termin) }}</span>
            </button>
          </li>
        </ul>

        <section
          v-if="ausgewaehlterTermin"
          class="calendar-detail"
          aria-labelledby="calendar-detail-heading"
          aria-live="polite"
        >
          <div>
            <p>Termindetails</p>
            <h3 id="calendar-detail-heading">
              {{ cleanText(ausgewaehlterTermin.schulungTitel) }}
            </h3>
          </div>
          <dl>
            <div>
              <dt>Zeitraum</dt>
              <dd>{{ zeitraumText(ausgewaehlterTermin.termin) }}</dd>
            </div>
            <div>
              <dt>Ort</dt>
              <dd>{{ cleanText(ausgewaehlterTermin.termin.ort) }}</dd>
            </div>
            <div>
              <dt>Format</dt>
              <dd>
                {{
                  cleanText(
                    ausgewaehlterTermin.termin.format || "Nicht angegeben",
                  )
                }}
              </dd>
            </div>
            <div>
              <dt>Status</dt>
              <dd>{{ statusText(ausgewaehlterTermin.termin) }}</dd>
            </div>
            <div v-if="ausgewaehlterTermin.termin.trainerName">
              <dt>Trainer</dt>
              <dd>{{ cleanText(ausgewaehlterTermin.termin.trainerName) }}</dd>
            </div>
            <div v-if="ausgewaehlterTermin.termin.assistenten?.length">
              <dt>Assistenz</dt>
              <dd>{{ ausgewaehlterTermin.termin.assistenten.map(cleanText).join(", ") }}</dd>
            </div>
          </dl>
          <div v-if="istTrainer || istAdministrator" class="calendar-detail-actions">
            <button
              v-if="istTrainer && ausgewaehlterTermin.termin.enddatum >= heuteIso()"
              class="filter-reset"
              type="button"
              :disabled="assistenzbewerbungen.has(ausgewaehlterTermin.termin.terminId)"
              @click="assistenzBewerben(ausgewaehlterTermin.termin.terminId)"
            >
              {{ assistenzbewerbungen.has(ausgewaehlterTermin.termin.terminId)
                ? "Als Assistenz beworben" : "Als Assistenz bewerben" }}
            </button>
            <button
              v-if="istAdministrator"
              class="primary-action"
              type="button"
              @click="trainerSuchen"
            >Verfügbare Trainer laden</button>
          </div>
        </section>
      </template>
    </section>

    <p v-if="aktionsmeldung" class="success planer-rueckmeldung" role="status">{{ aktionsmeldung }}</p>
    <p v-if="aktionsfehler" class="form-error planer-rueckmeldung" role="alert">{{ aktionsfehler }}</p>

    <section
      id="katalog"
      class="catalog-section"
      aria-labelledby="catalog-heading"
    >
      <div class="section-heading">
        <h2 id="catalog-heading">Schulungskatalog</h2>
        <p>
          Von Cloud-Infrastruktur bis Teamkommunikation. Finden Sie das passende
          Format für Ihr Team.
        </p>
      </div>

      <form class="filter-bar" role="search" @submit.prevent>
        <div class="filter-field">
          <label for="filter-suche">Titel suchen</label>
          <input
            id="filter-suche"
            v-model="suche"
            type="search"
            autocomplete="off"
            placeholder="z. B. Scrum, Azure, Kommunikation"
          />
        </div>
        <div class="filter-field">
          <label for="filter-kategorie">Kategorie</label>
          <select id="filter-kategorie" v-model="kategorie">
            <option value="">Alle Kategorien</option>
            <option v-for="kat in kategorien" :key="kat" :value="kat">
              {{ cleanText(kat) }}
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

      <div
        v-if="loading"
        class="catalog-grid"
        aria-label="Schulungen werden geladen"
        aria-busy="true"
      >
        <div
          v-for="index in 4"
          :key="index"
          class="course-card skeleton"
          aria-hidden="true"
        >
          <span></span><span></span><span></span>
        </div>
      </div>

      <div v-else-if="error" class="state state-error" role="alert">
        <p>Der Katalog konnte nicht geladen werden.</p>
        <small>{{ cleanText(error) }}</small>
      </div>

      <div v-else-if="schulungen.length === 0 && filterAktiv" class="state">
        <p>Keine Schulungen entsprechen den gewählten Filtern.</p>
        <small
          >Passen Sie Suchbegriff oder Kategorie an oder setzen Sie die Filter
          zurück.</small
        >
        <button type="button" class="filter-reset" @click="filterZuruecksetzen">
          Filter zurücksetzen
        </button>
      </div>

      <div v-else-if="schulungen.length === 0" class="state">
        <p>Aktuell sind keine Schulungen veröffentlicht.</p>
        <small>Neue Termine erscheinen hier, sobald sie verfügbar sind.</small>
      </div>

      <template v-else>
        <p class="filter-result" aria-live="polite">
          {{ schulungen.length }}
          {{ schulungen.length === 1 ? "Schulung" : "Schulungen" }} gefunden
        </p>
        <ul class="catalog-grid">
          <li
            v-for="schulung in schulungen"
            :key="schulung.id"
            class="card course-card"
          >
            <div class="course-topline">
              <span class="category">{{ cleanText(schulung.kategorie) }}</span>
              <span
                >{{ schulung.dauerInTagen }}
                {{ schulung.dauerInTagen === 1 ? "Tag" : "Tage" }}</span
              >
            </div>
            <h3>{{ cleanText(schulung.titel) }}</h3>
            <p>{{ cleanText(schulung.kurzbeschreibung) }}</p>
            <div class="course-footer">
              <span>
                {{ schulung.oeffentlicheTermine.length }}
                {{
                  schulung.oeffentlicheTermine.length === 1
                    ? "öffentlicher Termin"
                    : "öffentliche Termine"
                }}
              </span>
              <button
                v-if="istTrainer"
                class="filter-reset course-action"
                type="button"
                :disabled="qualifikationsbewerbungen.has(schulung.id)"
                @click="qualifikationBewerben(schulung.id)"
              >
                {{ qualifikationsbewerbungen.has(schulung.id)
                  ? "Beworben" : "Auf Qualifikation bewerben" }}
              </button>
            </div>
          </li>
        </ul>
      </template>
    </section>

    <section
      id="trainer"
      class="trainer-section"
      aria-labelledby="trainer-heading"
    >
      <div class="section-heading compact-heading">
        <h2 id="trainer-heading">Verfügbare Trainer finden</h2>
        <p>
          Wählen Sie Schulung und Zeitraum. Angezeigt werden passende Trainer
          ohne überschneidende Abwesenheit.
        </p>
      </div>

      <form class="trainer-form" @submit.prevent="trainerSuchen">
        <div class="filter-field trainer-course-field">
          <label for="trainer-schulung">Schulung</label>
          <select id="trainer-schulung" v-model="trainerSchulungId" required>
            <option value="" disabled>Schulung auswählen</option>
            <option
              v-for="schulung in schulungen"
              :key="schulung.id"
              :value="schulung.id"
            >
              {{ cleanText(schulung.titel) }} ({{ schulung.id }})
            </option>
          </select>
        </div>
        <div class="filter-field">
          <label for="trainer-von">Anfangsdatum</label>
          <input id="trainer-von" v-model="trainerVon" type="date" required />
        </div>
        <div class="filter-field">
          <label for="trainer-bis">Enddatum</label>
          <input id="trainer-bis" v-model="trainerBis" type="date" required />
        </div>
        <button class="primary-action trainer-submit" type="submit">
          Trainer anzeigen
        </button>
      </form>

      <div v-if="trainerLoading" class="trainer-results" aria-busy="true">
        <div v-for="index in 2" :key="index" class="trainer-row skeleton-row">
          <span></span><span></span>
        </div>
      </div>

      <div v-else-if="trainerError" class="state state-error" role="alert">
        <p>Die Trainersuche konnte nicht ausgeführt werden.</p>
        <small>{{ cleanText(trainerError) }}</small>
      </div>

      <div v-else-if="trainerGesucht && trainer.length === 0" class="state">
        <p>Keine verfügbaren Trainer</p>
        <small>
          Für diese Schulung und diesen Zeitraum wurde keine passende Person
          gefunden. Wählen Sie einen anderen Zeitraum.
        </small>
      </div>

      <div v-else-if="trainer.length" class="trainer-results">
        <p class="filter-result" aria-live="polite">
          {{ trainer.length }} Trainer verfügbar
        </p>
        <ul>
          <li v-for="person in trainer" :key="person.id" class="trainer-row">
            <div>
              <strong>{{ person.name }}</strong>
              <span>{{ person.id }}</span>
            </div>
            <div class="trainer-row-actions">
              <a :href="`mailto:${person.email}`">{{ person.email }}</a>
              <button
                v-if="istAdministrator && ausgewaehlterTermin"
                class="filter-reset"
                type="button"
                @click="trainerEinsetzen(person)"
              >Diesem Termin zuweisen</button>
            </div>
          </li>
        </ul>
      </div>
    </section>

    <footer class="footer">
      <div class="footer-main">
        <a
          class="footer-brand"
          href="#top"
          aria-label="Zurück zum Seitenanfang"
        >
          <span class="footer-brand-copy">
            <span class="brand-lockup" aria-hidden="true">
              <img src="/simplytest-wordmark-white.svg" alt="" />
              <strong>Academy</strong>
            </span>
          </span>
        </a>
        <nav class="footer-links" aria-label="Fußnavigation">
          <a href="#kalender">Kalender</a>
          <a href="#katalog">Katalog</a>
          <a href="#trainer">Trainer finden</a>
        </nav>
      </div>
      <p class="footer-meta">Ein Produkt von SimplyTest</p>
    </footer>
  </div>
</template>
