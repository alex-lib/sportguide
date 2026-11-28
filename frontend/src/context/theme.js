export const THEME_KEY = 'theme';
export const THEME_MODES = ['system', 'light', 'dark'];

export function readThemeMode(storage) {
  try {
    const stored = storage.getItem(THEME_KEY);
    if (THEME_MODES.includes(stored)) return stored;
  } catch { /* Storage may be unavailable in a WebView. */ }
  return 'system';
}

export function resolveSystemTheme(telegram, mediaQuery) {
  if (telegram?.initData && ['light', 'dark'].includes(telegram.colorScheme)) {
    return telegram.colorScheme;
  }
  return mediaQuery?.matches ? 'dark' : 'light';
}
