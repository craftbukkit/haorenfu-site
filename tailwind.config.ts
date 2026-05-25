import type { Config } from 'tailwindcss';

export default {
  content: ['./src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        bg0: '#05070A', bg1: '#090E12', brass: '#D6A85C', verdigris: '#47D6B4', arc: '#4AA8FF', ember: '#FF8A3D', mech: '#A7B0BA'
      },
      fontFamily: { sans: ['Noto Sans SC', 'system-ui', 'sans-serif'], mono: ['JetBrains Mono', 'ui-monospace', 'monospace'] }
    }
  },
  plugins: []
} satisfies Config;
