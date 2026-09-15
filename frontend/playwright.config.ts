import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { defineConfig } from '@playwright/test'

const frontendDir = path.dirname(fileURLToPath(import.meta.url))
const backendDir = path.resolve(frontendDir, '../backend')
const mavenCommand =
  process.platform === 'win32'
    ? 'mvnw.cmd -Dspring-boot.run.profiles=e2e -Dspring-boot.run.arguments=--server.port=18082 spring-boot:run'
    : './mvnw -Dspring-boot.run.profiles=e2e -Dspring-boot.run.arguments=--server.port=18082 spring-boot:run'

export default defineConfig({
  testDir: './tests/e2e',
  fullyParallel: false,
  use: {
    baseURL: 'http://localhost:15174',
    trace: 'on-first-retry',
  },
  webServer: [
    {
      name: 'backend',
      command: mavenCommand,
      cwd: backendDir,
      url: 'http://localhost:18082/api/health',
      reuseExistingServer: false,
      timeout: 120000,
      gracefulShutdown: { signal: 'SIGTERM', timeout: 5000 },
    },
    {
      name: 'frontend',
      command: 'npm run dev -- --port 15174',
      cwd: frontendDir,
      env: { VITE_BACKEND_URL: 'http://localhost:18082' },
      url: 'http://localhost:15174',
      reuseExistingServer: false,
      timeout: 120000,
      gracefulShutdown: { signal: 'SIGTERM', timeout: 5000 },
    },
  ],
})
