/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        cream: '#fff8ed',
        berry: '#da5d82',
        mint: '#b9ead7',
        ink: '#372b3b',
      },
      fontFamily: { display: ['Nunito', 'ui-rounded', 'sans-serif'] },
      boxShadow: { soft: '0 12px 35px rgba(89, 49, 72, .09)' },
    },
  },
  plugins: [],
}
