import { mount, flushPromises } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import App from './App.vue'

describe('App smoke', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('renders at least one training entry from API data', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => [
        {
          id: 'SCH-001',
          titel: 'Scrum Master Zertifizierung',
          kategorie: 'Agile',
          kurzbeschreibung: 'Kurzbeschreibung',
          voraussetzungen: [],
          dauerInTagen: 2,
          mindestteilnehmerExklusiv: 6,
          maxTeilnehmerOeffentlich: 12,
          oeffentlicheTermine: [],
        },
      ],
    } as Response)

    const wrapper = mount(App)
    await flushPromises()

    expect(wrapper.text()).toContain('Schulungskatalog')
    expect(wrapper.text()).toContain('Scrum Master Zertifizierung')
  })
})
