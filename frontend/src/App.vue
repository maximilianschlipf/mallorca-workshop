<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchSchulungen } from './api'
import type { Schulung } from './types'

const schulungen = ref<Schulung[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

onMounted(async () => {
  try {
    schulungen.value = await fetchSchulungen()
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unbekannter Fehler'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <main class="page">
    <header class="hero">
      <p class="eyebrow">Nordwind Academy</p>
      <h1>Schulungskatalog</h1>
      <p class="subtitle">MVP Iteration 1: Katalog-Read aus Seed-Daten</p>
    </header>

    <section class="panel" aria-live="polite">
      <p v-if="loading">Lade Schulungen...</p>
      <p v-else-if="error" class="error">Fehler beim Laden: {{ error }}</p>

      <ul v-else class="catalog-list">
        <li v-for="schulung in schulungen" :key="schulung.id" class="card">
          <h2>{{ schulung.titel }}</h2>
          <p class="meta">{{ schulung.kategorie }} · {{ schulung.dauerInTagen }} Tage</p>
          <p>{{ schulung.kurzbeschreibung }}</p>
          <p class="meta">Öffentliche Termine: {{ schulung.oeffentlicheTermine.length }}</p>
        </li>
      </ul>
    </section>
  </main>
</template>
