import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 15173,
    proxy: {
      '/api': {
        target: process.env.VITE_BACKEND_URL ?? 'http://localhost:18081',
        changeOrigin: true,
      },
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    include: ['src/**/*.spec.ts'],
    exclude: ['tests/**'],
  },
})
