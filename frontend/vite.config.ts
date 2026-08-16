import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    // IntelliJ 本機啟動 Spring Boot 時，讓 React 的 /api 請求轉送到 8082。
    proxy: {
      '/api': 'http://localhost:8082',
    },
  },
});
