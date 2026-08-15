import { createContext, useContext, useEffect, useCallback, useState } from 'react';

const ThemeContext = createContext(null);

const THEME_KEY = 'theme';

function resolveInitialTheme() {
  try {
    const stored = localStorage.getItem(THEME_KEY);
    if (stored === 'dark' || stored === 'light') return stored;
  } catch { /* */ }

  const tg = typeof window !== 'undefined' ? window.Telegram?.WebApp : null;
  const inTelegram = !!(tg && tg.initData);
  if (inTelegram && tg.colorScheme) return tg.colorScheme;

  if (typeof window !== 'undefined' && window.matchMedia) {
    if (window.matchMedia('(prefers-color-scheme: dark)').matches) return 'dark';
  }

  return 'light';
}

export function ThemeProvider({ children }) {
  const [theme, setThemeInternal] = useState(resolveInitialTheme);

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    try { localStorage.setItem(THEME_KEY, theme); } catch { /* */ }
  }, [theme]);

  // Listen for Telegram theme changes — only override if user hasn't set a manual preference
  useEffect(() => {
    const tg = typeof window !== 'undefined' ? window.Telegram?.WebApp : null;
    if (!tg || !tg.initData) return;

    const checkUserOverride = () => {
      try {
        const stored = localStorage.getItem(THEME_KEY);
        return stored === 'dark' || stored === 'light';
      } catch { return false; }
    };

    const onTelegramThemeChange = () => {
      if (!checkUserOverride()) {
        setThemeInternal(tg.colorScheme || 'light');
      }
    };

    if (typeof tg.onEvent === 'function') {
      tg.onEvent('themeChanged', onTelegramThemeChange);
    }

    // Listen for OS preference changes outside Telegram
    if (typeof window.matchMedia === 'function') {
      const mq = window.matchMedia('(prefers-color-scheme: dark)');
      const onOsChange = () => {
        if (!checkUserOverride()) {
          const newTheme = mq.matches ? 'dark' : 'light';
          setThemeInternal(newTheme);
        }
      };
      if (mq.addEventListener) mq.addEventListener('change', onOsChange);
      else if (mq.addListener) mq.addListener(onOsChange);
    }
  }, []);

  const toggle = useCallback(() => {
    setThemeInternal(prev => prev === 'dark' ? 'light' : 'dark');
  }, []);

  return (
    <ThemeContext.Provider value={{ theme, toggle, isDark: theme === 'dark' }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  const ctx = useContext(ThemeContext);
  if (!ctx) throw new Error('useTheme must be used within ThemeProvider');
  return ctx;
}
