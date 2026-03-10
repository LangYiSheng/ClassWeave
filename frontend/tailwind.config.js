/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: 'rgb(var(--cw-ink) / <alpha-value>)',
        muted: 'rgb(var(--cw-muted) / <alpha-value>)',
        paper: 'rgb(var(--cw-paper) / <alpha-value>)',
        line: 'rgb(var(--cw-line) / <alpha-value>)',
        blue: 'rgb(var(--cw-blue) / <alpha-value>)',
        teal: 'rgb(var(--cw-teal) / <alpha-value>)',
        coral: 'rgb(var(--cw-coral) / <alpha-value>)',
        gold: 'rgb(var(--cw-gold) / <alpha-value>)',
      },
      fontFamily: {
        heading: ['Georgia', '"Times New Roman"', '"Songti SC"', 'serif'],
        body: ['"Trebuchet MS"', '"Segoe UI Variable"', '"PingFang SC"', 'sans-serif'],
      },
      boxShadow: {
        glow: '0 24px 60px rgba(31, 34, 48, 0.12)',
      },
      borderRadius: {
        shell: '28px',
      },
      backgroundImage: {
        shell:
          'radial-gradient(circle at top left, rgba(223, 93, 67, 0.18), transparent 28%), radial-gradient(circle at 80% 10%, rgba(31, 111, 235, 0.16), transparent 24%), linear-gradient(180deg, #f6f1e8 0%, #efe8dc 100%)',
      },
    },
  },
  plugins: [],
}
