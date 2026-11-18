import React from 'react'
import ReactDOM from 'react-dom/client'
import { ThemeProvider } from './context/ThemeContext'
import App from './App.jsx'
import './index.css'

try {
  if (typeof window !== 'undefined' && window.Telegram?.WebApp) {
    const tg = window.Telegram.WebApp;

    if (tg.themeParams) {
      const p = tg.themeParams;
      const root = document.documentElement.style;
      if (p.bg_color) root.setProperty('--tg-theme-bg-color', p.bg_color);
      if (p.text_color) root.setProperty('--tg-theme-text-color', p.text_color);
      if (p.hint_color) root.setProperty('--tg-theme-hint-color', p.hint_color);
      if (p.link_color) root.setProperty('--tg-theme-link-color', p.link_color);
      if (p.button_color) root.setProperty('--tg-theme-button-color', p.button_color);
      if (p.button_text_color) root.setProperty('--tg-theme-button-text-color', p.button_text_color);
      if (p.secondary_bg_color) root.setProperty('--tg-theme-secondary-bg-color', p.secondary_bg_color);
    }
  }
} catch (error) {
  console.warn('Theme setup issue:', error);
}

const rootElement = document.getElementById('root');
if (!rootElement) {
  throw new Error('Root element not found');
}

ReactDOM.createRoot(rootElement).render(
  <React.StrictMode>
    <ThemeProvider>
      <App />
    </ThemeProvider>
  </React.StrictMode>,
)
