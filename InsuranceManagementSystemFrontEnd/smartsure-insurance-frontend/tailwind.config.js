/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#eff7ff',
          100: '#d9ecff',
          200: '#b9dcff',
          300: '#8cc7ff',
          400: '#57a9f8',
          500: '#2d89e5',
          600: '#0f6cbd',
          700: '#0b4f8a',
          800: '#083c68',
          900: '#062d4f',
        },
        brand: {
          navy: '#102542',
          gold: '#f3b53f',
          teal: '#0fa3b1',
          coral: '#f4845f',
          mist: '#eef4fb',
        }
      },
      fontFamily: {
        sans: ['Manrope', 'sans-serif'],
        display: ['Outfit', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      animation: {
        'fade-in': 'fadeIn 0.5s ease-out',
        'slide-up': 'slideUp 0.4s ease-out',
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { transform: 'translateY(20px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
      },
    },
  },
  plugins: [],
}
