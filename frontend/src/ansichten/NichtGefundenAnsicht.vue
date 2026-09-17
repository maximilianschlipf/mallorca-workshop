<script setup lang="ts">
import { useRoute } from "vue-router";

const route = useRoute();
</script>

<template>
  <section class="page-section">
    <div class="section-heading">
      <h1>Seite nicht gefunden</h1>
      <p>Unter <code>{{ route.fullPath }}</code> liegt nichts.</p>
    </div>
    <RouterLink class="primary-action" to="/">Zur Startseite</RouterLink>
  </section>
</template>
