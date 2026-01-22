import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.jsx'
import './index.css'

// Apply Telegram theme colors if available (with error handling)
try {
  if (typeof window !== 'undefined' && window.Telegram?.WebApp) {
    const tg = window.Telegram.WebApp;
    
    // Only apply if themeParams exists and is accessible
    if (tg.themeParams) {
      document.documentElement.style.setProperty('--tg-theme-bg-color', tg.themeParams.bg_color || '#ffffff');
      document.documentElement.style.setProperty('--tg-theme-text-color', tg.themeParams.text_color || '#1a1a1a');
      document.documentElement.style.setProperty('--tg-theme-hint-color', tg.themeParams.hint_color || '#6b7280');
      document.documentElement.style.setProperty('--tg-theme-link-color', tg.themeParams.link_color || '#1a9b8e');
      document.documentElement.style.setProperty('--tg-theme-button-color', tg.themeParams.button_color || '#1a9b8e');
      document.documentElement.style.setProperty('--tg-theme-button-text-color', tg.themeParams.button_text_color || '#ffffff');
      document.documentElement.style.setProperty('--tg-theme-secondary-bg-color', tg.themeParams.secondary_bg_color || '#f0f9f8');
    }
  }
} catch (error) {
  // Silently fail if Telegram WebApp is not available
  console.warn('Telegram WebApp theme not available:', error);
}

// Render the app
const rootElement = document.getElementById('root');
if (!rootElement) {
  throw new Error('Root element not found');
}

ReactDOM.createRoot(rootElement).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)

