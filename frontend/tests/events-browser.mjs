// Production build, synthetic Telegram launch and API fixtures; no production writes.
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
const { chromium } = await import(process.env.PLAYWRIGHT_MODULE || 'playwright');
const sdk = await readFile(new URL('../node_modules/@twa-dev/sdk/dist/telegram-web-apps.js', import.meta.url), 'utf8');
const base = process.env.SMOKE_BASE_URL || 'http://127.0.0.1:4175';
const description = 'Открытая тренировка для участников с любым уровнем подготовки. '.repeat(5);
const events = [
  { id: 1, name: 'Открытая тренировка по функциональному тренингу', date: '2026-11-29', time: '12:00', placeName: 'HYDRA GYM', address: 'ул. Бакунина, 45А', district: 'Ленинский', description, link: 'https://example.com/event' },
  { id: 2, name: 'Тренировка без ссылки', date: '2027-01-15', description, link: '-' },
  { id: 3, name: 'Дата уточняется', date: null, description: 'Короткое описание' },
];
const errors = [];
let canEdit = false;
const browser = await chromium.launch({ channel: 'chrome', headless: true });
try {
  const ctx = await browser.newContext({ viewport: { width: 390, height: 844 }, timezoneId: 'America/Los_Angeles' });
  await ctx.route('https://telegram.org/js/telegram-web-app.js*', r => r.fulfill({ contentType: 'application/javascript', body: sdk }));
  await ctx.route('https://fonts.googleapis.com/**', r => r.fulfill({ contentType: 'text/css', body: '' }));
  await ctx.route('https://api.sportguide.online/**', r => {
    const url = new URL(r.request().url());
    if (url.pathname === '/api/auth/telegram') return r.fulfill({ json: { token: 'synthetic-test-token' } });
    if (url.pathname.endsWith('/shown')) return r.fulfill({ json: true });
    if (url.pathname === '/api/events') return r.fulfill({ json: { list: events } });
    return r.fulfill({ json: { list: [], canEdit } });
  });
  const page = await ctx.newPage();
  page.on('pageerror', e => errors.push(e.message));
  const launch = new URLSearchParams({ tgWebAppData: new URLSearchParams({ auth_date: '1806060000', hash: 'a'.repeat(64), user: JSON.stringify({ id: 123, first_name: 'Test' }) }).toString(), tgWebAppVersion: '8.0', tgWebAppPlatform: 'android', tgWebAppThemeParams: '{}' });
  await page.goto(`${base}/#${launch}`);
  await page.waitForSelector('.tabbar');
  await page.evaluate(() => { location.hash = '/events'; });
  const cards = page.locator('[data-tour="event-card"]');
  await cards.first().waitFor();
  assert.equal(await cards.count(), 3);
  const first = cards.first();
  assert.equal(await first.locator('.date-badge .d').innerText(), '29');
  await first.getByText('воскресенье', { exact: true }).waitFor();
  await first.getByText('2026', { exact: true }).waitFor();
  await first.getByText('12:00', { exact: true }).waitFor();
  await first.getByText('ул. Бакунина, 45А · Ленинский', { exact: true }).waitFor();
  const more = first.getByRole('link', { name: 'Подробнее', exact: true });
  assert.equal(await more.getAttribute('href'), events[0].link);
  assert.equal(await more.getAttribute('target'), '_blank');
  assert.equal(await page.getByRole('link', { name: 'Редактировать событие', exact: true }).count(), 0);
  assert.equal(await cards.nth(1).getByRole('link', { name: 'Подробнее', exact: true }).count(), 0);
  await cards.nth(1).getByText('2027', { exact: true }).waitFor();
  await cards.nth(1).getByText('Время уточняется', { exact: true }).waitFor();
  assert.equal(await cards.nth(2).locator('.date-badge .d').innerText(), '—');
  const text = first.locator('.event-card-description-text');
  assert.ok(await text.evaluate(e => e.scrollHeight > e.clientHeight));
  await first.getByText('Развернуть описание', { exact: true }).click();
  assert.equal(await text.innerText(), description.trim());
  assert.ok(await text.evaluate(e => e.clientHeight >= e.scrollHeight));
  await first.getByText('Свернуть описание', { exact: true }).click();
  await cards.nth(1).locator('summary').focus();
  await page.keyboard.press('Enter');
  assert.equal(await cards.nth(1).locator('details').getAttribute('open'), '');
  for (const width of [320, 390, 1440]) {
    await page.setViewportSize({ width, height: 844 });
    for (const theme of ['light', 'dark']) {
      await page.evaluate(theme => { document.documentElement.dataset.theme = theme; window.scrollTo(0, 0); }, theme);
      await page.waitForTimeout(200);
      assert.equal(await page.evaluate(() => document.documentElement.scrollWidth > innerWidth), false);
      await page.screenshot({ path: `/tmp/sportguide-event-cards-${theme}-${width}.png` });
    }
  }
  canEdit = true;
  await page.reload();
  await page.getByRole('link', { name: 'Редактировать событие', exact: true }).first().waitFor();
  assert.equal(await first.getByRole('link', { name: 'Редактировать событие', exact: true }).getAttribute('href'), '#/events/1/edit');
  assert.deepEqual(errors, []);
  console.log('PASS: event date/weekday/year across timezones, time/location, expandable full description without external link, keyboard, permission-aware editing, 320/390/1440 layouts in both themes');
} finally { await browser.close(); }
