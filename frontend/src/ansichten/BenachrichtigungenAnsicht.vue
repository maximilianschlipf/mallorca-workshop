<script setup lang="ts">
import { nextTick, onMounted, ref } from "vue";
import { fetchBenachrichtigungen, markiereAlleBenachrichtigungenGelesen } from "../api";
import type { Benachrichtigung } from "../types";

const eintraege = ref<Benachrichtigung[]>([]);
const fehler = ref("");
const meldung = ref("");
const statusmeldung = ref<HTMLElement | null>(null);
const ladenAktiv = ref(true);
const inArbeit = ref(false);

function ziel(eintrag: Benachrichtigung) {
  if (!eintrag.bezugVorhanden || !eintrag.bezugId) return null;
  if (eintrag.bezugArt === "SCHULUNG") return `/katalog/${eintrag.bezugId}`;
  if (eintrag.bezugArt === "TERMIN") return `/planer?termin=${encodeURIComponent(eintrag.bezugId)}#kalender`;
  return "/";
}

function datum(iso: string) {
  return new Intl.DateTimeFormat("de-DE", { dateStyle: "medium", timeStyle: "short" }).format(new Date(iso));
}

async function laden() {
  eintraege.value = await fetchBenachrichtigungen();
  if (eintraege.value.some(eintrag => !eintrag.gelesen)) {
    await markiereAlleBenachrichtigungenGelesen();
  }
  window.dispatchEvent(new Event("benachrichtigungen-gelesen"));
}

async function allesGelesen() {
  if (inArbeit.value) return;
  inArbeit.value = true;
  fehler.value = meldung.value = "";
  try {
    await markiereAlleBenachrichtigungenGelesen();
    eintraege.value = eintraege.value.map(eintrag => ({ ...eintrag, gelesen: true }));
    meldung.value = "Alle Benachrichtigungen sind als gelesen markiert.";
    window.dispatchEvent(new Event("benachrichtigungen-gelesen"));
    await nextTick();
    statusmeldung.value?.focus();
  } catch (error) {
    fehler.value = error instanceof Error ? error.message : "Benachrichtigungen konnten nicht aktualisiert werden.";
  } finally {
    inArbeit.value = false;
  }
}

onMounted(async () => {
  try {
    await laden();
  } catch (error) {
    fehler.value = error instanceof Error ? error.message : "Benachrichtigungen konnten nicht geladen werden.";
  } finally {
    ladenAktiv.value = false;
  }
});
</script>

<template>
  <main class="content-page notifications-page">
    <header class="section-heading compact-heading">
      <h1>Benachrichtigungen</h1>
      <p>Mitteilungen über Entscheidungen und Änderungen, neueste zuerst.</p>
      <button v-if="eintraege.some(e => !e.gelesen)" class="sekundaer-aktion" type="button" :disabled="inArbeit" @click="allesGelesen">{{ inArbeit ? "Wird aktualisiert …" : "Alles als gelesen" }}</button>
    </header>
    <p v-if="meldung" ref="statusmeldung" class="success" role="status" tabindex="-1">{{ meldung }}</p>
    <p v-if="fehler" class="form-error" role="alert">{{ fehler }}</p>
    <div v-if="ladenAktiv" class="state" aria-busy="true">Benachrichtigungen werden geladen.</div>
    <ol v-else-if="eintraege.length" class="notification-list">
      <li v-for="eintrag in eintraege" :key="eintrag.id" :class="{ unread: !eintrag.gelesen }">
        <span class="notification-state">{{ eintrag.gelesen ? "Gelesen" : "Ungelesen" }}</span>
        <p>{{ eintrag.anlass }}</p>
        <time :datetime="eintrag.erstelltAm">{{ datum(eintrag.erstelltAm) }}</time>
        <RouterLink v-if="ziel(eintrag)" :to="ziel(eintrag)!">Gegenstand öffnen</RouterLink>
        <span v-else-if="eintrag.bezugId" class="muted">Gegenstand nicht mehr vorhanden</span>
      </li>
    </ol>
    <div v-else-if="!fehler" class="state">Keine Benachrichtigungen.</div>
  </main>
</template>
