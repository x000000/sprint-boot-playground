import { fileURLToPath, URL } from 'url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const srcDir = process.env.VUE_SRC || './src/main/resources/assets';
const outDir = process.env.VUE_OUT || '../public';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL(srcDir, import.meta.url))
    }
  },
  build: {
    outDir: outDir,
  },
  root: srcDir,
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        ws: true,
      },
    },
  },
})
