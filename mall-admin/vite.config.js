import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5175,
    host: '0.0.0.0',
    proxy: {
      '/admin-api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/admin-api/, '')
      }
    }
  },
  build: { outDir: 'dist', sourcemap: false }
})
