import { ref } from "vue";
import { ApiError, fetchIch, logout } from "./api";
import type { Benutzerkonto } from "./types";

export const aktuellesKonto = ref<Benutzerkonto | null>(null);
let geladen = false;

export async function ladeKonto(neu = false) {
  if (geladen && !neu) return aktuellesKonto.value;
  try {
    aktuellesKonto.value = await fetchIch();
  } catch (error) {
    if (!(error instanceof ApiError) || error.status !== 401) throw error;
    aktuellesKonto.value = null;
  }
  geladen = true;
  return aktuellesKonto.value;
}

export function setzeKonto(konto: Benutzerkonto) {
  aktuellesKonto.value = konto;
  geladen = true;
}

export async function abmelden() {
  await logout();
  aktuellesKonto.value = null;
  geladen = true;
}
