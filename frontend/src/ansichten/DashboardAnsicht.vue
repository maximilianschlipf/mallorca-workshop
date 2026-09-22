<script setup lang="ts">
import { onMounted, ref } from "vue";
import { fetchTerminDashboard } from "../api";
import type { DashboardTermin } from "../types";

const eintraege = ref<DashboardTermin[]>([]);
const laden = ref(true);
const fehler = ref("");

function zeitraum(termin: DashboardTermin) {
  const format = new Intl.DateTimeFormat("de-DE", {
    day: "2-digit", month: "2-digit", year: "numeric",
  });
  const datum = (iso: string) => format.format(new Date(`${iso}T12:00:00`));
  return termin.startdatum === termin.enddatum
    ? datum(termin.startdatum)
    : `${datum(termin.startdatum)} - ${datum(termin.enddatum)}`;
}

onMounted(async () => {
  try {
    const antwort = await fetchTerminDashboard();
    eintraege.value = Array.isArray(antwort) ? antwort : [];
  } catch (error) {
    fehler.value = error instanceof Error ? error.message : "Das Dashboard konnte nicht geladen werden.";
  } finally {
    laden.value = false;
  }
});
</script>

<template>
  <main class="dashboard-section" aria-labelledby="dashboard-heading">
    <div class="section-heading compact-heading">
      <h1 id="dashboard-heading">Was liegt bei mir?</h1>
      <p>Überfällige Termine zuerst, danach die nächsten Planungslücken.</p>
    </div>

    <p v-if="laden" class="state" aria-busy="true">Dashboard wird geladen.</p>
    <p v-else-if="fehler" class="state state-error" role="alert">{{ fehler }}</p>
    <ul v-else-if="eintraege.length" class="dashboard-list">
      <li v-for="eintrag in eintraege" :key="eintrag.terminId"
          :class="{ dringend: eintrag.dringend || eintrag.ueberfaellig }">
        <strong>{{ eintrag.schulungTitel }}</strong>
        <span>{{ zeitraum(eintrag) }}</span>
        <span v-if="eintrag.ueberfaellig">Durchführungsbestätigung ausstehend</span>
        <span v-else-if="eintrag.ohneTrainer">Ohne Trainer</span>
        <span v-if="eintrag.mindestteilnehmerUnterschritten">Mindestteilnehmerzahl nicht erreicht</span>
        <RouterLink class="sekundaer-aktion" to="/planer#kalender">Zum Planer</RouterLink>
      </li>
    </ul>
    <p v-else class="state">Aktuell liegt nichts an.</p>
  </main>
</template>
