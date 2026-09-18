<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from "vue";
import {
  aendereTermin,
  aufAssistenzplatzBewerben,
  aufQualifikationBewerben,
  bestaetigeTermin,
  fetchKategorien,
  fetchSchulungen,
  fetchTermin,
  fetchTerminDashboard,
  fetchTrainerOptionen,
  fetchTrainerOptionenFuerPlanung,
  fetchVerfuegbareTrainer,
  legeTerminAn,
  loescheTermin,
  sageTerminAb,
  schlageEnddatumVor,
  trainerZuweisen,
  zieheTrainerAb,
} from "../api";
import { aktuellesKonto } from "../auth";
import type { DashboardTermin, Schulung, Termin, TerminDetail, TerminEingabe, Trainer } from "../types";

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
const terminDetail = ref<TerminDetail | null>(null);
const dashboard = ref<DashboardTermin[]>([]);
const terminDialog = ref(false);
const terminDialogElement = ref<HTMLElement | null>(null);
const terminDetailDialog = ref(false);
const terminDetailDialogElement = ref<HTMLElement | null>(null);
const terminTrainer = ref<Trainer[]>([]);
const bearbeiteterTermin = ref<string | null>(null);
const terminForm = ref<TerminEingabe>({
  schulungId: "", startdatum: "", enddatum: "", zugangsart: null,
  durchfuehrungsart: null, ort: null, kundenfirma: null, onlineZugang: null,
});
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

const cleanText = (text: string | null | undefined) => (text ?? "Nicht angegeben").replace(/[—–]/g, "-");

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

const aktiveSchulungen = computed(() => schulungen.value.filter((s) => s.zustand !== "ARCHIVIERT"));

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
        ({ termin }) => datum.getDay() !== 0 && datum.getDay() !== 6
          && termin.startdatum <= iso && termin.enddatum >= iso,
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

function zeitraumText(termin: Pick<Termin, "startdatum" | "enddatum">) {
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
    abgeschlossen: "Abgeschlossen",
    abgesagt: "Abgesagt",
  };
  const text = status[termin.status] ?? termin.status;
  return termin.enddatum < heuteIso() ? `${text}, vergangen` : text;
}

async function terminAuswaehlen(eintrag: KalenderTermin) {
  ausgewaehlterTermin.value = eintrag;
  terminDetail.value = null;
  terminDetailDialog.value = true;
  trainerSchulungId.value = eintrag.schulungId;
  trainerVon.value = eintrag.termin.startdatum;
  trainerBis.value = eintrag.termin.enddatum;
  trainer.value = [];
  trainerGesucht.value = false;
  const angefragterTermin = eintrag.termin.terminId;
  try {
    const detail = await fetchTermin(eintrag.termin.terminId);
    if (detail?.terminId && ausgewaehlterTermin.value?.termin.terminId === angefragterTermin) {
      terminDetail.value = detail;
    }
  } catch {
    // Die kompakte Kalenderansicht bleibt auch bei einem Detailfehler nutzbar.
  }
}

function neuerTermin() {
  terminDetailDialog.value = false;
  bearbeiteterTermin.value = null;
  terminForm.value = { schulungId: "", startdatum: "", enddatum: "", zugangsart: null,
    durchfuehrungsart: null, ort: null, kundenfirma: null, onlineZugang: null, trainerId: null };
  terminTrainer.value = [];
  terminDialog.value = true;
}

function terminBearbeiten() {
  if (!terminDetail.value) return;
  const t = terminDetail.value;
  bearbeiteterTermin.value = t.terminId;
  terminForm.value = { schulungId: t.schulungId, startdatum: t.startdatum, enddatum: t.enddatum,
    zugangsart: t.zugangsart ?? null, durchfuehrungsart: t.durchfuehrungsart ?? null,
    ort: t.ort || null, kundenfirma: t.kundenfirma ?? null, onlineZugang: t.onlineZugang ?? null };
  terminDetailDialog.value = false;
  terminDialog.value = true;
}

async function enddatumVorschlagen() {
  if (bearbeiteterTermin.value || !terminForm.value.schulungId || !terminForm.value.startdatum) return;
  const schulungId = terminForm.value.schulungId;
  const startdatum = terminForm.value.startdatum;
  try {
    const { enddatum } = await schlageEnddatumVor(schulungId, startdatum);
    if (terminForm.value.schulungId !== schulungId || terminForm.value.startdatum !== startdatum) return;
    terminForm.value.enddatum = enddatum;
    await trainerFuerNeuanlageLaden();
  } catch (err) {
    if (terminForm.value.schulungId === schulungId && terminForm.value.startdatum === startdatum) {
      aktionsfehler.value = err instanceof Error ? err.message : "Enddatum konnte nicht vorgeschlagen werden.";
    }
  }
}

async function terminSpeichern() {
  await aktion(async () => {
    const eingabe: TerminEingabe = {
      ...terminForm.value,
      ort: ["vor_ort", "beim_kunden", "hybrid"].includes(terminForm.value.durchfuehrungsart || "")
        ? terminForm.value.ort : null,
      kundenfirma: terminForm.value.zugangsart === "exklusiv" ? terminForm.value.kundenfirma : null,
      onlineZugang: ["remote", "hybrid"].includes(terminForm.value.durchfuehrungsart || "")
        ? terminForm.value.onlineZugang : null,
    };
    let gespeichert: TerminDetail;
    try {
      gespeichert = bearbeiteterTermin.value
        ? await aendereTermin(bearbeiteterTermin.value, eingabe)
        : await legeTerminAn(eingabe);
    } catch (err: unknown) {
      if (err instanceof Error && err.message.includes("bestätigt") && window.confirm(err.message)) {
        gespeichert = bearbeiteterTermin.value
          ? await aendereTermin(bearbeiteterTermin.value, { ...eingabe, entfernenBestaetigt: true })
          : await legeTerminAn(eingabe);
      } else throw err;
    }
    terminDialog.value = false;
    terminDetail.value = gespeichert;
    await ladeSchulungen(true);
    kalenderMonat.value = new Date(Number(gespeichert.startdatum.slice(0, 4)), Number(gespeichert.startdatum.slice(5, 7)) - 1, 1);
    const schulung = kalenderSchulungen.value.find((s) => s.id === gespeichert.schulungId);
    const termin = schulung?.oeffentlicheTermine.find((t) => t.terminId === gespeichert.terminId);
    if (schulung && termin) ausgewaehlterTermin.value = { schulungId: schulung.id, schulungTitel: schulung.titel, termin };
    terminDetailDialog.value = true;
  }, bearbeiteterTermin.value ? "Termin geändert." : "Termin angelegt.");
}

async function trainerFuerNeuanlageLaden() {
  if (bearbeiteterTermin.value || !terminForm.value.schulungId
      || !terminForm.value.startdatum || !terminForm.value.enddatum) return;
  const { schulungId, startdatum, enddatum } = terminForm.value;
  terminTrainer.value = [];
  terminForm.value.trainerId = null;
  try {
    const ergebnis = await fetchTrainerOptionenFuerPlanung(schulungId, startdatum, enddatum);
    if (terminForm.value.schulungId === schulungId && terminForm.value.startdatum === startdatum
        && terminForm.value.enddatum === enddatum) terminTrainer.value = ergebnis;
  } catch {
    if (terminForm.value.schulungId === schulungId && terminForm.value.startdatum === startdatum
        && terminForm.value.enddatum === enddatum) terminTrainer.value = [];
  }
}

let fokusVorDialog: HTMLElement | null = null;
watch(terminDialog, async (offen) => {
  if (offen) {
    fokusVorDialog = document.activeElement as HTMLElement;
    await nextTick();
    terminDialogElement.value?.querySelector<HTMLElement>(
      "select:not([disabled]), input:not([disabled]), button:not([disabled])",
    )?.focus();
  } else {
    fokusVorDialog?.focus();
  }
});

function dialogTaste(event: KeyboardEvent) {
  modalTaste(event, terminDialogElement.value, () => { terminDialog.value = false; });
}

let fokusVorTermindetail: HTMLElement | null = null;
watch(terminDetailDialog, async (offen) => {
  if (offen) {
    fokusVorTermindetail = document.activeElement as HTMLElement;
    await nextTick();
    terminDetailDialogElement.value?.querySelector<HTMLElement>("button:not([disabled])")?.focus();
  } else {
    fokusVorTermindetail?.focus();
  }
});

function terminDetailSchliessen() {
  terminDetailDialog.value = false;
}

function terminDetailDialogTaste(event: KeyboardEvent) {
  modalTaste(event, terminDetailDialogElement.value, terminDetailSchliessen);
}

function modalTaste(event: KeyboardEvent, element: HTMLElement | null, schliessen: () => void) {
  if (event.key === "Escape") {
    schliessen();
    return;
  }
  if (event.key !== "Tab" || !element) return;
  const elemente = [...element.querySelectorAll<HTMLElement>(
    'button:not([disabled]), input:not([disabled]), select:not([disabled]), a[href], [tabindex="0"]',
  )];
  if (!elemente.length) return;
  const erstes = elemente[0];
  const letztes = elemente.at(-1)!;
  if (event.shiftKey && document.activeElement === erstes) {
    event.preventDefault();
    letztes.focus();
  } else if (!event.shiftKey && document.activeElement === letztes) {
    event.preventDefault();
    erstes.focus();
  }
}

async function terminBestaetigen() {
  if (!terminDetail.value || !window.confirm("Durchführung endgültig bestätigen?")) return;
  await aktion(async () => { aktualisiereTermin(await bestaetigeTermin(terminDetail.value!.terminId)); await ladeSchulungen(true); }, "Durchführung bestätigt.");
}

async function terminAbsagen() {
  if (!terminDetail.value || !window.confirm(`${warntext(terminDetail.value)} absagen?`)) return;
  const grund = window.prompt("Optionaler Absagegrund") || null;
  await aktion(async () => { aktualisiereTermin(await sageTerminAb(terminDetail.value!.terminId, grund)); await ladeSchulungen(true); }, "Termin abgesagt.");
}

async function terminLoeschen() {
  if (!terminDetail.value || !window.confirm(`${warntext(terminDetail.value)} löschen?`)) return;
  await aktion(async () => { await loescheTermin(terminDetail.value!.terminId); terminDetailSchliessen(); terminDetail.value = null; ausgewaehlterTermin.value = null; await ladeSchulungen(true); }, "Termin gelöscht.");
}

function warntext(termin: TerminDetail) {
  const beteiligte = [termin.trainerName, ...(termin.assistenten ?? []).map((a) => a.name)].filter(Boolean);
  return `Termin mit ${termin.anzahlBuchungen} Buchungen${beteiligte.length ? ` und ${beteiligte.join(", ")}` : ""}`;
}

async function trainerAbziehen() {
  if (!terminDetail.value) return;
  await aktion(async () => { await zieheTrainerAb(terminDetail.value!.terminId); aktualisiereTermin(await fetchTermin(terminDetail.value!.terminId)); await ladeSchulungen(true); }, "Trainerzuweisung aufgehoben.");
}

function aktualisiereTermin(detail: TerminDetail) {
  terminDetail.value = detail;
  if (!ausgewaehlterTermin.value) return;
  ausgewaehlterTermin.value.termin.status = detail.status;
  ausgewaehlterTermin.value.termin.trainerId = detail.trainerId;
  ausgewaehlterTermin.value.termin.trainerName = detail.trainerName;
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
    const terminId = ausgewaehlterTermin.value!.termin.terminId;
    try {
      await trainerZuweisen(terminId, person.id);
    } catch (err) {
      if (!(err instanceof Error) || !err.message.includes("bestätigt") || !window.confirm(err.message)) throw err;
      await trainerZuweisen(terminId, person.id, true);
    }
    aktualisiereTermin(await fetchTermin(terminId));
    await ladeSchulungen(true);
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
    const [ergebnis, ungefilterterKalender] = await Promise.all([
      fetchSchulungen({ suche: suche.value, kategorie: kategorie.value }),
      kalenderInitialisieren && filterAktiv.value ? fetchSchulungen() : Promise.resolve(null),
    ]);
    schulungen.value = ergebnis;
    if (kalenderInitialisieren) {
      kalenderSchulungen.value = ungefilterterKalender ?? ergebnis;
      await ladeDashboard();
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : "Unbekannter Fehler";
    if (kalenderInitialisieren) kalenderError.value = error.value;
  } finally {
    loading.value = false;
    if (kalenderInitialisieren) kalenderLoading.value = false;
  }
}

async function ladeDashboard() {
  try {
    const eintraege = await fetchTerminDashboard();
    dashboard.value = Array.isArray(eintraege)
      ? eintraege.filter((eintrag) => typeof eintrag?.terminId === "string") : [];
  } catch {
    dashboard.value = [];
  }
}

function filterZuruecksetzen() {
  suche.value = "";
  kategorie.value = "";
}

async function trainerSuchen(fuerTermin = false) {
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
    trainer.value = fuerTermin && terminDetail.value
      ? await fetchTrainerOptionen(terminDetail.value.terminId)
      : await fetchVerfuegbareTrainer(trainerSchulungId.value, trainerVon.value, trainerBis.value);
  } catch (err) {
    trainerError.value =
      err instanceof Error ? err.message : "Unbekannter Fehler";
  } finally {
    trainerLoading.value = false;
    if (fuerTermin) {
      terminDetailSchliessen();
      await nextTick();
      document.querySelector<HTMLElement>("#trainer .trainer-results, #trainer .state")?.focus();
    }
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

    <section v-if="dashboard.length" class="dashboard-section" aria-labelledby="dashboard-heading">
      <div class="section-heading compact-heading">
        <h1 id="dashboard-heading">Offene Aufgaben</h1>
        <p>Überfällige Termine zuerst, danach die nächsten Planungslücken.</p>
      </div>
      <ul class="dashboard-list">
        <li v-for="eintrag in dashboard" :key="eintrag.terminId" :class="{ dringend: eintrag.dringend || eintrag.ueberfaellig }">
          <strong>{{ cleanText(eintrag.schulungTitel) }}</strong>
          <span>{{ zeitraumText(eintrag) }}</span>
          <span v-if="eintrag.ueberfaellig">Durchführungsbestätigung ausstehend</span>
          <span v-else-if="eintrag.ohneTrainer">Ohne Trainer</span>
          <span v-if="eintrag.mindestteilnehmerUnterschritten">Mindestteilnehmerzahl nicht erreicht</span>
        </li>
      </ul>
    </section>

    <section
      id="kalender"
      class="calendar-section"
      aria-labelledby="calendar-heading"
    >
      <div class="section-heading compact-heading">
        <h1 id="calendar-heading">Schulungskalender</h1>
        <p>
          Alle Termine mit Zeitraum, Ort und aktuellem Status.
        </p>
        <button v-if="istAdministrator" class="primary-action" type="button" @click="neuerTermin">Neuer Termin</button>
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

        <div
          v-if="ausgewaehlterTermin && terminDetailDialog"
          class="dialog-hintergrund"
          role="presentation"
          @click.self="terminDetailSchliessen"
        >
          <section
            ref="terminDetailDialogElement"
            class="dialog termin-detail-dialog calendar-detail"
            role="dialog"
            aria-modal="true"
            aria-labelledby="calendar-detail-heading"
            @keydown="terminDetailDialogTaste"
          >
          <button class="termin-detail-schliessen" type="button" aria-label="Termindetails schließen" @click="terminDetailSchliessen">
            Schließen
          </button>
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
            <template v-if="terminDetail">
              <div><dt>Schulungstage</dt><dd>{{ terminDetail.schulungsTage }}</dd></div>
              <div><dt>Zugangsart</dt><dd>{{ terminDetail.zugangsart || "Nicht angegeben" }}</dd></div>
              <div><dt>Durchführungsart</dt><dd>{{ terminDetail.durchfuehrungsart || "Nicht angegeben" }}</dd></div>
              <div v-if="terminDetail.kundenfirma"><dt>Kundenfirma</dt><dd>{{ terminDetail.kundenfirma }}</dd></div>
              <div v-if="terminDetail.onlineZugang"><dt>Online-Zugang</dt><dd><a :href="terminDetail.onlineZugang">{{ terminDetail.onlineZugang }}</a></dd></div>
              <div><dt>Buchungen</dt><dd>{{ terminDetail.anzahlBuchungen }}</dd></div>
              <div v-if="terminDetail.abschlussart"><dt>Abschluss</dt><dd>{{ terminDetail.abschlussart === "automatisch" ? "Automatisch, ungeprüft" : "Manuell bestätigt" }}</dd></div>
              <div v-if="terminDetail.absagegrund"><dt>Absagegrund</dt><dd>{{ terminDetail.absagegrund }}</dd></div>
            </template>
          </dl>
          <div v-if="terminDetail?.teilnehmer.length" class="teilnehmerliste">
            <h4>Teilnehmer</h4>
            <ul>
              <li v-for="buchung in terminDetail.teilnehmer" :key="buchung.id">
                <strong>{{ buchung.name || "Anonymisiert" }}</strong>
                <span>{{ buchung.firma }}</span>
                <span v-if="buchung.bemerkung">{{ buchung.bemerkung }}</span>
              </li>
            </ul>
          </div>
          <ul v-if="terminDetail?.warnungen?.length" class="termin-warnungen">
            <li v-for="warnung in terminDetail.warnungen" :key="warnung">{{ warnung }}</li>
          </ul>
          <div v-if="istTrainer || istAdministrator" class="calendar-detail-actions">
            <button
              v-if="istTrainer && terminDetail?.status === 'geplant' && ausgewaehlterTermin.termin.enddatum >= heuteIso()"
              class="filter-reset"
              type="button"
              :disabled="assistenzbewerbungen.has(ausgewaehlterTermin.termin.terminId)"
              @click="assistenzBewerben(ausgewaehlterTermin.termin.terminId)"
            >
              {{ assistenzbewerbungen.has(ausgewaehlterTermin.termin.terminId)
                ? "Als Assistenz beworben" : "Als Assistenz bewerben" }}
            </button>
            <button
              v-if="istAdministrator && terminDetail?.status === 'geplant'"
              class="primary-action"
              type="button"
              @click="trainerSuchen(true)"
            >Verfügbare Trainer laden</button>
            <button v-if="istAdministrator && terminDetail?.status === 'geplant'" type="button" @click="terminBearbeiten">Bearbeiten</button>
            <button v-if="istAdministrator && terminDetail?.status === 'geplant' && terminDetail.trainerId" type="button" @click="trainerAbziehen">Trainer abziehen</button>
            <button
              v-if="terminDetail?.status === 'geplant' && terminDetail.trainerId && terminDetail.enddatum <= heuteIso()
                && (istAdministrator || terminDetail.trainerId === aktuellesKonto?.id)"
              type="button" @click="terminBestaetigen"
            >Durchführung bestätigen</button>
            <button v-if="istAdministrator && terminDetail?.status === 'geplant'" class="gefahr" type="button" @click="terminAbsagen">Absagen</button>
            <button
              v-if="istAdministrator && terminDetail && terminDetail.status !== 'abgeschlossen' && terminDetail.startdatum > heuteIso()"
              class="gefahr" type="button" @click="terminLoeschen"
            >Löschen</button>
          </div>
          </section>
        </div>
      </template>
    </section>

    <div v-if="terminDialog" class="dialog-hintergrund" role="presentation" @click.self="terminDialog = false">
      <section ref="terminDialogElement" class="dialog termin-dialog" role="dialog" aria-modal="true"
        aria-labelledby="termin-dialog-heading" @keydown="dialogTaste">
        <h2 id="termin-dialog-heading">{{ bearbeiteterTermin ? "Termin bearbeiten" : "Neuer Termin" }}</h2>
        <form class="schulungsformular" @submit.prevent="terminSpeichern">
          <label>Schulung
            <select v-model="terminForm.schulungId" :disabled="!!bearbeiteterTermin" required @change="enddatumVorschlagen">
              <option value="" disabled>Schulung auswählen</option>
              <option v-for="schulung in aktiveSchulungen" :key="schulung.id" :value="schulung.id">{{ schulung.titel }}</option>
            </select>
          </label>
          <label>Startdatum
            <input v-model="terminForm.startdatum" type="date" required @change="enddatumVorschlagen" />
          </label>
          <label>Enddatum
            <input v-model="terminForm.enddatum" type="date" required @change="trainerFuerNeuanlageLaden" />
          </label>
          <label v-if="!bearbeiteterTermin">Trainer (optional)
            <select v-model="terminForm.trainerId">
              <option :value="null">Noch nicht zugewiesen</option>
              <option v-for="person in terminTrainer" :key="person.id" :value="person.id"
                :disabled="person.verfuegbar === false">
                {{ person.name }}{{ person.grund ? ` - ${person.grund}` : "" }}
              </option>
            </select>
          </label>
          <div v-if="!bearbeiteterTermin && terminTrainer.length" class="trainer-planung">
            <table v-for="person in terminTrainer" :key="person.id" class="trainer-calendar">
              <caption>{{ person.name }} - {{ person.verfuegbar === false ? person.grund : "verfügbar" }}</caption>
              <thead><tr><th>Status</th><th>Von</th><th>Bis</th></tr></thead>
              <tbody>
                <tr v-for="belegung in person.kalender" :key="`${belegung.art}-${belegung.von}-${belegung.bis}`">
                  <td>{{ belegung.art }}</td><td><time :datetime="belegung.von">{{ belegung.von }}</time></td>
                  <td><time :datetime="belegung.bis">{{ belegung.bis }}</time></td>
                </tr>
                <tr v-if="!person.kalender?.length"><td colspan="3">Keine Belegung im Planungsumfeld</td></tr>
              </tbody>
            </table>
          </div>
          <label>Zugangsart
            <select v-model="terminForm.zugangsart">
              <option :value="null">Noch offen</option>
              <option value="oeffentlich">Öffentlich</option>
              <option value="exklusiv">Exklusiv</option>
            </select>
          </label>
          <label>Durchführungsart
            <select v-model="terminForm.durchfuehrungsart">
              <option :value="null">Noch offen</option>
              <option value="remote">Remote</option>
              <option value="vor_ort">Vor Ort</option>
              <option value="beim_kunden">Beim Kunden</option>
              <option value="hybrid">Hybrid</option>
            </select>
          </label>
          <label v-if="['vor_ort', 'beim_kunden', 'hybrid'].includes(terminForm.durchfuehrungsart || '')">Ort
            <input v-model="terminForm.ort" type="text" required />
          </label>
          <label v-if="terminForm.zugangsart === 'exklusiv'">Kundenfirma
            <input v-model="terminForm.kundenfirma" type="text" required />
          </label>
          <label v-if="['remote', 'hybrid'].includes(terminForm.durchfuehrungsart || '')">Online-Zugang
            <input v-model="terminForm.onlineZugang" type="url" placeholder="https://…" />
          </label>
          <div class="formular-aktionen">
            <button class="primary-action" type="submit">Speichern</button>
            <button type="button" @click="terminDialog = false">Abbrechen</button>
          </div>
        </form>
      </section>
    </div>

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

      <form class="trainer-form" @submit.prevent="trainerSuchen()">
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

      <div v-else-if="trainerError" class="state state-error" role="alert" tabindex="-1">
        <p>Die Trainersuche konnte nicht ausgeführt werden.</p>
        <small>{{ cleanText(trainerError) }}</small>
      </div>

      <div v-else-if="trainerGesucht && trainer.length === 0" class="state" role="status" tabindex="-1">
        <p>Keine verfügbaren Trainer</p>
        <small>
          Für diese Schulung und diesen Zeitraum wurde keine passende Person
          gefunden. Wählen Sie einen anderen Zeitraum.
        </small>
      </div>

      <div v-else-if="trainer.length" class="trainer-results" tabindex="-1">
        <p class="filter-result" aria-live="polite">
          {{ trainer.length }} qualifizierte Trainer
        </p>
        <ul>
          <li v-for="person in trainer" :key="person.id" class="trainer-row">
            <div>
              <strong>{{ person.name }}</strong>
              <span>{{ person.id }}</span>
              <span v-if="person.grund">{{ person.grund }}</span>
              <table v-if="person.kalender?.length" class="trainer-calendar">
                <caption>Kalender von {{ person.name }}</caption>
                <thead><tr><th>Status</th><th>Von</th><th>Bis</th></tr></thead>
                <tbody><tr v-for="belegung in person.kalender" :key="`${belegung.art}-${belegung.von}-${belegung.bis}`">
                  <td>{{ belegung.art }}</td><td><time :datetime="belegung.von">{{ belegung.von }}</time></td>
                  <td><time :datetime="belegung.bis">{{ belegung.bis }}</time></td>
                </tr></tbody>
              </table>
            </div>
            <div class="trainer-row-actions">
              <a v-if="person.email" :href="`mailto:${person.email}`">{{ person.email }}</a>
              <button
                v-if="istAdministrator && ausgewaehlterTermin"
                class="filter-reset"
                type="button"
                :disabled="person.verfuegbar === false"
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
