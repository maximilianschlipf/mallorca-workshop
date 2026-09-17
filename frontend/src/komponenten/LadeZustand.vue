<script setup lang="ts">
defineProps<{
  laedt: boolean;
  fehler: string | null;
  /** Was nicht geladen werden konnte, für die Fehlermeldung. */
  gegenstand: string;
}>();
</script>

<template>
  <div v-if="laedt" class="state" aria-busy="true">
    <p>{{ gegenstand }} wird geladen.</p>
  </div>
  <div v-else-if="fehler" class="state state-error" role="alert">
    <p>{{ gegenstand }} konnte nicht geladen werden.</p>
    <small>{{ fehler }}</small>
  </div>
  <slot v-else />
</template>
