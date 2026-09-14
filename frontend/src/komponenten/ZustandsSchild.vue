<script setup lang="ts">
import type { Schulungszustand } from "../types";

defineProps<{ zustand: Schulungszustand }>();
</script>

<template>
  <!--
    REQ_KAT_SICHT_02: Archivierte Schulungen verschwinden nicht, werden aber
    als archiviert gekennzeichnet.
  -->
  <span class="zustand" :class="`zustand-${zustand.toLowerCase()}`">
    {{ zustand === "AKTIV" ? "Aktiv" : "Archiviert" }}
  </span>
</template>
