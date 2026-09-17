import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { defineConfig } from '@playwright/test'
import { KATALOG_VERZEICHNIS, katalogZuruecksetzen } from './tests/e2e/katalog-umgebung'

const frontendDir = path.dirname(fileURLToPath(import.meta.url))
const backendDir = path.resolve(frontendDir, '../backend')
const mavenCommand =
  process.platform === 'win32'
    ? 'mvnw.cmd -Dspring-boot.run.profiles=e2e -Dspring-boot.run.arguments=--server.port=18082 spring-boot:run'
    : './mvnw -Dspring-boot.run.profiles=e2e -Dspring-boot.run.arguments=--server.port=18082 spring-boot:run'

katalogZuruecksetzen()

export default defineConfig({
  testDir: './tests/e2e',
  fullyParallel: false,
  // Ein Arbeiter, nicht nur eine Datei nach der anderen: Alle Tests teilen
  // sich ein Backend und damit einen Katalog. Die Verwaltungstests legen
  // Schulungen an und löschen sie wieder -- liefe daneben ein Test, der
  // Schulungen zählt, zählte er einen Katalog im Umbau.
  workers: 1,
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
      env: { SCHULUNGSPLANER_KATALOG_PFAD: KATALOG_VERZEICHNIS },
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
