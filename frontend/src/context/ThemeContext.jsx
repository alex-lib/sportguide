import { createContext, useContext, useEffect, useLayoutEffect, useCallback, useState } from 'react';
import WebApp from '@twa-dev/sdk';
import { THEME_KEY, THEME_MODES, readThemeMode, resolveSystemTheme, resolveScheduledTheme } from './theme.js';

const ThemeContext = createContext(null);
const mediaQuery = () => window.matchMedia?.('(prefers-color-scheme: dark)');
const systemTheme = () => resolveSystemTheme(WebApp, mediaQuery());
const initialMode = () => {
  try { return readThemeMode(window.localStorage); } catch { return 'system'; }
};

export function ThemeProvider({ children }) {
  const [mode, setModeInternal] = useState(initialMode);
  const [automaticTheme, setAutomaticTheme] = useState(systemTheme);
  const [scheduledTheme, setScheduledTheme] = useState(resolveScheduledTheme);
  const theme = mode === 'system' ? automaticTheme : mode === 'scheduled' ? scheduledTheme : mode;

  useLayoutEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    document.documentElement.style.colorScheme = theme;
  }, [theme]);

  useLayoutEffect(() => {
    if (mode !== 'system') return;
    const mq = mediaQuery();
    const update = () => setAutomaticTheme(resolveSystemTheme(WebApp, mq));
    update();
    WebApp.onEvent?.('themeChanged', update);
    if (mq?.addEventListener) mq.addEventListener('change', update);
    else mq?.addListener?.(update);
    window.addEventListener('focus', update);
    document.addEventListener('visibilitychange', update);
    return () => {
      WebApp.offEvent?.('themeChanged', update);
      if (mq?.removeEventListener) mq.removeEventListener('change', update);
      else mq?.removeListener?.(update);
      window.removeEventListener('focus', update);
      document.removeEventListener('visibilitychange', update);
    };
  }, [mode]);

  useLayoutEffect(() => {
    if (mode !== 'scheduled') return;
    let timer;
    const update = () => {
      window.clearTimeout(timer);
      setScheduledTheme(resolveScheduledTheme());
      // Align to the next minute, including 07:00/19:00, and pick up clock/timezone changes.
      timer = window.setTimeout(update, 60000 - Date.now() % 60000);
    };
    update();
    window.addEventListener('focus', update);
    document.addEventListener('visibilitychange', update);
    return () => {
      window.clearTimeout(timer);
      window.removeEventListener('focus', update);
      document.removeEventListener('visibilitychange', update);
    };
  }, [mode]);

  useEffect(() => {
    const onStorage = event => {
      if (event.key === THEME_KEY || event.key === null) setModeInternal(initialMode());
    };
    window.addEventListener('storage', onStorage);
    return () => window.removeEventListener('storage', onStorage);
  }, []);

  const setMode = useCallback(next => {
    if (!THEME_MODES.includes(next)) return;
    setModeInternal(next);
    // Store the chosen mode, never the automatically resolved light/dark value.
    try { localStorage.setItem(THEME_KEY, next); } catch { /* Keep working without storage. */ }
  }, []);

  return (
    <ThemeContext.Provider value={{ theme, mode, setMode, isDark: theme === 'dark' }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  const ctx = useContext(ThemeContext);
  if (!ctx) throw new Error('useTheme must be used within ThemeProvider');
  return ctx;
}
