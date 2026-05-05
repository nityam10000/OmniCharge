import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {
    minify: 'terser',
    sourcemap: false,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules')) {
            if (id.includes('react')) return 'vendor-react';
            if (id.includes('react-router')) return 'vendor-router';
            if (id.includes('lucide-react') || id.includes('react-hot-toast') || id.includes('zod') || id.includes('react-hook-form')) return 'vendor-ui';
            if (id.includes('axios')) return 'vendor-api';
          }
        },
      },
    },
  },
})
