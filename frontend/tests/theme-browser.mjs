// Production UI with synthetic SDK launch data and mocked API; no real bot calls.
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
const { chromium } = await import(process.env.PLAYWRIGHT_MODULE || 'playwright');
const base = process.env.SMOKE_BASE_URL || 'http://127.0.0.1:4175';
const sdk = await readFile(new URL('../node_modules/@twa-dev/sdk/dist/telegram-web-apps.js', import.meta.url), 'utf8');
const browser = await chromium.launch({ channel: 'chrome', headless: true });
const errors = [];
try {
  const ctx = await browser.newContext({ viewport: { width: 390, height: 844 }, colorScheme: 'light', timezoneId: 'Europe/Moscow' });
  await ctx.route('https://telegram.org/js/telegram-web-app.js*', r => r.fulfill({ contentType: 'application/javascript', body: sdk }));
  await ctx.route('https://fonts.googleapis.com/**', r => r.fulfill({ contentType: 'text/css', body: '' }));
  await ctx.route('https://api.sportguide.online/**', r => {
    const url = r.request().url();
    if (url.endsWith('/api/auth/telegram')) return r.fulfill({ json: { token: 'synthetic-test-token' } });
    if (url.endsWith('/shown')) return r.fulfill({ json: true });
    return r.fulfill({ json: { weather: false, events: false, places: false, coaches: false, jointTrainings: false, list: [], canEdit: false } });
  });
  const page = await ctx.newPage();
  page.on('pageerror', error => errors.push(error.message));
  const launch = new URLSearchParams({ tgWebAppData: new URLSearchParams({ auth_date: '1806060000', hash: 'a'.repeat(64), user: JSON.stringify({ id: 123, first_name: 'Test' }) }).toString(), tgWebAppVersion: '8.0', tgWebAppPlatform: 'android', tgWebAppThemeParams: JSON.stringify({ bg_color: '#ffffff' }) });
  await page.goto(`${base}/#${launch}`);
  await page.waitForFunction(() => document.querySelector('[data-theme]'));
  await page.evaluate(() => { location.hash = '/settings'; });
  await page.getByRole('radio', { name: 'Системная', exact: true }).waitFor();
  assert.equal(await page.getByRole('radio', { name: 'Системная', exact: true }).isChecked(), true);
  assert.equal(await page.evaluate(() => localStorage.getItem('theme')), null);
  const current = () => page.locator('html').getAttribute('data-theme');
  async function telegramTheme(color) {
    await page.evaluate(bg_color => window.Telegram.WebView.receiveEvent('theme_changed', { theme_params: { bg_color } }), color);
  }
  await telegramTheme('#000000');
  await page.waitForFunction(() => document.documentElement.dataset.theme === 'dark');
  assert.equal(await page.evaluate(() => localStorage.getItem('theme')), null);
  await page.getByRole('radio', { name: 'Светлая', exact: true }).check();
  assert.equal(await current(), 'light');
  await telegramTheme('#ffffff'); await telegramTheme('#000000');
  assert.equal(await current(), 'light');
  await page.reload(); await page.getByRole('radio', { name: 'Светлая', exact: true }).waitFor();
  assert.equal(await page.getByRole('radio', { name: 'Светлая', exact: true }).isChecked(), true);
  await telegramTheme('#000000');
  await page.getByRole('radio', { name: 'Системная', exact: true }).check();
  await page.waitForFunction(() => document.documentElement.dataset.theme === 'dark');
  assert.equal(await page.evaluate(() => localStorage.getItem('theme')), 'system');
  await telegramTheme('#ffffff'); await page.waitForFunction(() => document.documentElement.dataset.theme === 'light');
  await page.reload(); await page.getByRole('radio', { name: 'Системная', exact: true }).waitFor();
  assert.equal(await page.getByRole('radio', { name: 'Системная', exact: true }).isChecked(), true);
  await page.getByRole('radio', { name: 'Тёмная', exact: true }).check();
  await telegramTheme('#ffffff'); assert.equal(await current(), 'dark');
  assert.equal(await page.evaluate(() => document.documentElement.scrollWidth > innerWidth), false);
  await page.screenshot({ path: '/tmp/sportguide-theme-dark.png', fullPage: true });
  await page.getByRole('radio', { name: 'Светлая', exact: true }).check();
  await page.screenshot({ path: '/tmp/sportguide-theme-light.png', fullPage: true });
  assert.equal(await page.getByRole('radio').count(), 3);
  assert.equal(await page.getByRole('radio', { name: 'По времени', exact: true }).count(), 0);
  // Users who selected the removed mode now follow the system theme on reload.
  await page.evaluate(() => localStorage.setItem('theme', 'scheduled'));
  await page.reload();
  const system = page.getByRole('radio', { name: 'Системная', exact: true });
  await system.waitFor();
  assert.equal(await system.isChecked(), true);
  await telegramTheme('#000000');
  await page.waitForFunction(() => document.documentElement.dataset.theme === 'dark');
  await telegramTheme('#ffffff');
  await page.waitForFunction(() => document.documentElement.dataset.theme === 'light');
  await page.getByRole('radio', { name: 'Тёмная', exact: true }).check();
  assert.equal(await page.evaluate(() => localStorage.getItem('theme')), 'dark');
  assert.equal(await page.evaluate(() => document.documentElement.scrollWidth > innerWidth), false);
  assert.deepEqual(errors, []);
  console.log('PASS: three system/manual modes; removed scheduled preference falls back to system; persistence and Telegram updates; mobile controls fit; no page errors');
} finally { await browser.close(); }
