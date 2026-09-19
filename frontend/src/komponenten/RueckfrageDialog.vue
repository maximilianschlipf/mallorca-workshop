<script setup lang="ts">
import { ref } from "vue";

defineProps<{
  titel: string;
  text: string;
  bestaetigung: string;
  eingabeLabel?: string;
  eingabeMaxlength?: number;
}>();

const emit = defineEmits<{ bestaetigt: [eingabe?: string]; abgebrochen: [] }>();
const eingabe = ref("");
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
      <div v-if="eingabeLabel" class="form-field">
        <label for="rueckfrage-eingabe">{{ eingabeLabel }}</label>
        <textarea
          id="rueckfrage-eingabe"
          v-model="eingabe"
          :maxlength="eingabeMaxlength"
          required
          autofocus
        />
      </div>
      <div class="dialog-aktionen">
        <button type="button" @click="emit('abgebrochen')">Abbrechen</button>
        <button
          type="button"
          class="gefahr"
          :disabled="Boolean(eingabeLabel) && !eingabe.trim()"
          @click="emit('bestaetigt', eingabe.trim() || undefined)"
        >
          {{ bestaetigung }}
        </button>
      </div>
    </div>
  </div>
</template>
