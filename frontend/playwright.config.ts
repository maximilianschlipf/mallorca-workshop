import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './tests/e2e',
  fullyParallel: false,
  use: {
    baseURL: 'http://localhost:15173',
    trace: 'on-first-retry',
  },
  webServer: {
    command: 'npm run dev -- --port 15173',
    url: 'http://localhost:15173',
    reuseExistingServer: true,
    timeout: 120000,
  },
})
