/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/main/resources/templates/**/*.html'
  ],

  theme: {
    extend: {
      colors: {
        'primary': '#3B82F6',
        'secondary': '#6366F1',
        'surface': '#F3F4F6',
      },
    },
  },

  plugins: [],
}