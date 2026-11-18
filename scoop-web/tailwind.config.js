/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        cream: '#17141f',
        surface: '#24202f',
        'surface-raised': '#302a3d',
        berry: '#efa8c8',
        mint: '#a8e3d1',
        ink: '#f4edf7',
      },
      fontFamily: { display: ['Nunito', 'ui-rounded', 'sans-serif'] },
      boxShadow: { soft: '0 12px 35px rgba(0, 0, 0, .22)' },
    },
  },
  plugins: [],
}
