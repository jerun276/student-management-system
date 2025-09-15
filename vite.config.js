import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
    plugins: [react()],
    build: {
        manifest: true,
        rollupOptions: {
            input: './src/main/react/main.jsx',
            output: {
                dir: './src/main/resources/static/react-build',
                format: 'es',
                // --- START OF THE FIX ---
                // By controlling the filenames, we can predict the output.
                // This tells Vite to name the entry file exactly 'main.js'
                // and other chunks (if any) as '[name].js'.
                entryFileNames: `[name].js`,
                chunkFileNames: `[name].js`,
                assetFileNames: `[name].[ext]`
                // --- END OF THE FIX ---
            }
        }
    }
});