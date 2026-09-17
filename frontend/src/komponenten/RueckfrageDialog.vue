<script setup lang="ts">
defineProps<{
  titel: string;
  text: string;
  bestaetigung: string;
}>();

const emit = defineEmits<{ bestaetigt: []; abgebrochen: [] }>();
</script>

<template>
  <!--
    Eine Rückfrage vor dem Löschen. Bewusst kein window.confirm: Der native
    Dialog lässt sich weder gestalten noch von Hilfsmitteln so ankündigen wie
    der Rest der Seite.
  -->
  <div class="dialog-hintergrund" @click.self="emit('abgebrochen')">
    <div
      class="dialog card"
      role="dialog"
      aria-modal="true"
      :aria-label="titel"
    >
      <h3>{{ titel }}</h3>
      <p>{{ text }}</p>
      <div class="dialog-aktionen">
        <button type="button" @click="emit('abgebrochen')">Abbrechen</button>
        <button type="button" class="gefahr" @click="emit('bestaetigt')">
          {{ bestaetigung }}
        </button>
      </div>
    </div>
  </div>
</template>
