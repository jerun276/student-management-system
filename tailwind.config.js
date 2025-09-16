/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/main/resources/templates/**/*.html',
    './src/main/react/**/*.{js,jsx}'
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

  plugins: [require("daisyui")],

  daisyui: {
    themes: [
      {
        mytheme: {
          "primary": "#3B82F6",
          "secondary": "#6366F1",
          "accent": "#F472B6",
          "neutral": "#374151",
          "base-100": "#FFFFFF",
          "info": "#0CA5E9",

          ".btn": {
            "text-transform": "none",
          }
        },
      },
    ],
  },
}