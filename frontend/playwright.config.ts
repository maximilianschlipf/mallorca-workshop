import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { defineConfig } from '@playwright/test'

const frontendDir = path.dirname(fileURLToPath(import.meta.url))
const backendDir = path.resolve(frontendDir, '../backend')
const mavenCommand =
  process.platform === 'win32'
    ? 'mvnw.cmd spring-boot:run'
    : './mvnw spring-boot:run'

export default defineConfig({
  testDir: './tests/e2e',
  fullyParallel: false,
  use: {
    baseURL: 'http://localhost:15173',
    trace: 'on-first-retry',
  },
  webServer: [
    {
      name: 'backend',
      command: mavenCommand,
      cwd: backendDir,
      url: 'http://localhost:18081/api/health',
      reuseExistingServer: !process.env.CI,
      timeout: 120000,
      gracefulShutdown: { signal: 'SIGTERM', timeout: 5000 },
    },
    {
      name: 'frontend',
      command: 'npm run dev',
      cwd: frontendDir,
      url: 'http://localhost:15173',
      reuseExistingServer: !process.env.CI,
      timeout: 120000,
      gracefulShutdown: { signal: 'SIGTERM', timeout: 5000 },
    },
  ],
})
