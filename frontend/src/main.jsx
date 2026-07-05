import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.jsx'
import './index.css'

// Resolve and apply the active color theme (light/dark) on <html>.
// Priority: Telegram colorScheme -> OS preference. Re-applied on change.
function applyTheme() {
  try {
    const tg = typeof window !== 'undefined' ? window.Telegram?.WebApp : null;
    // Trust Telegram's colorScheme only when truly launched from Telegram
    // (initData present); the SDK otherwise defaults to 'light' in a browser.
    const inTelegram = !!(tg && tg.initData);
    let dark;
    if (inTelegram && tg.colorScheme) {
      dark = tg.colorScheme === 'dark';
    } else if (typeof window !== 'undefined' && window.matchMedia) {
      dark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    } else {
      dark = false;
    }
    document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light');
  } catch {
    document.documentElement.setAttribute('data-theme', 'light');
  }
}

applyTheme();

try {
  if (typeof window !== 'undefined' && window.Telegram?.WebApp) {
    const tg = window.Telegram.WebApp;

    // Map Telegram theme params onto our passthrough tokens when present.
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

    // Follow Telegram theme changes live.
    if (typeof tg.onEvent === 'function') {
      tg.onEvent('themeChanged', applyTheme);
    }
  }

  // Follow OS theme changes when not inside Telegram.
  if (typeof window !== 'undefined' && window.matchMedia) {
    const mq = window.matchMedia('(prefers-color-scheme: dark)');
    const onChange = () => {
      // Only follow the OS when not driven by a real Telegram launch.
      if (!window.Telegram?.WebApp?.initData) applyTheme();
    };
    if (mq.addEventListener) mq.addEventListener('change', onChange);
    else if (mq.addListener) mq.addListener(onChange);
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
    <App />
  </React.StrictMode>,
)
