import type { Schulung } from './types'

export async function fetchSchulungen(): Promise<Schulung[]> {
  const response = await fetch('/api/schulungen')
  if (!response.ok) {
    throw new Error(`API-Fehler: ${response.status}`)
  }
  return (await response.json()) as Schulung[]
}
