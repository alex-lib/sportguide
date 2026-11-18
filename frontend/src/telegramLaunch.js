// Navigation hint only. The API must still verify the initData signature and JWT.
// Do not infer Telegram from window.Telegram, user agent, platform or URL flags.
export function hasTelegramLaunchData(webApp) {
  if (typeof webApp?.initData !== 'string' || !webApp.initData) return false;

  const data = new URLSearchParams(webApp.initData);
  return /^\d+$/.test(data.get('auth_date') || '')
    && /^[a-f\d]{64}$/i.test(data.get('hash') || '');
}
