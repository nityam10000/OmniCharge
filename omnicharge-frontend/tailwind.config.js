/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      fontFamily: {
        sans: ['Helvetica Now', 'Helvetica', 'Arial', 'sans-serif'],
        display: ['Helvetica Now', 'Helvetica', 'Arial', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      colors: {
        brand: {
          DEFAULT: '#0EA5E9',
          dark: '#0284C7',
          light: '#38BDF8',
          glow: 'rgba(14, 165, 233, 0.3)',
        },
        surface: {
          DEFAULT: '#0F172A',
          card: '#1E293B',
          elevated: '#334155',
          border: '#334155',
          muted: '#475569',
        },
        accent: {
          purple: '#6366F1',
          green: '#10B981',
          amber: '#F59E0B',
          red: '#EF4444',
        },
      },
      backgroundImage: {
        'gradient-brand': 'linear-gradient(135deg, #0EA5E9 0%, #6366F1 100%)',
        'mesh-bg': 'radial-gradient(at 40% 20%, hsla(210,100%,56%,0.12) 0px, transparent 50%), radial-gradient(at 80% 0%, hsla(263,100%,60%,0.08) 0px, transparent 50%), radial-gradient(at 0% 50%, hsla(198,100%,46%,0.08) 0px, transparent 50%)',
      },
      boxShadow: {
        glow: 'none',
        'glow-lg': 'none',
        card: 'none',
        'card-hover': 'none',
      },
      animation: {
        'fade-in': 'fadeIn 0.5s ease-out',
        'slide-up': 'slideUp 0.4s ease-out',
        'slide-in-right': 'slideInRight 0.3s ease-out',
        shimmer: 'shimmer 2s infinite',
      },
      keyframes: {
        fadeIn: { from: { opacity: '0' }, to: { opacity: '1' } },
        slideUp: { from: { opacity: '0', transform: 'translateY(16px)' }, to: { opacity: '1', transform: 'translateY(0)' } },
        slideInRight: { from: { opacity: '0', transform: 'translateX(16px)' }, to: { opacity: '1', transform: 'translateX(0)' } },
        shimmer: { '0%': { backgroundPosition: '-200% 0' }, '100%': { backgroundPosition: '200% 0' } },
      },
    },
  },
  plugins: [],
};

