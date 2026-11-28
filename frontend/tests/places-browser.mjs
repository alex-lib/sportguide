// Production UI with synthetic Telegram launch data and mocked API; no real bot calls.
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
const { chromium } = await import(process.env.PLAYWRIGHT_MODULE || 'playwright');
const base = process.env.SMOKE_BASE_URL || 'http://127.0.0.1:4175';
const sdk = await readFile(new URL('../node_modules/@twa-dev/sdk/dist/telegram-web-apps.js', import.meta.url), 'utf8');
const browser = await chromium.launch({ channel: 'chrome', headless: true });
const places = Array.from({ length: 45 }, (_, i) => ({ id: i + 1, name: `Место ${i + 1}`, address: 'Воронеж', coordinates: `https://maps.google.com/?q=${51.65 + i / 1000},39.2` }));
const requests = [];
const errors = [];
let failMap = false;
let delayPage = false;
const tile = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQIHWP4z8DwHwAFgAI/ScLbtAAAAABJRU5ErkJggg==', 'base64');
try {
  const ctx = await browser.newContext({ viewport: { width: 390, height: 844 } });
  await ctx.route('https://telegram.org/js/telegram-web-app.js*', r => r.fulfill({ contentType: 'application/javascript', body: sdk }));
  await ctx.route('https://fonts.googleapis.com/**', r => r.fulfill({ contentType: 'text/css', body: '' }));
  await ctx.route('https://*.tile.openstreetmap.org/**', r => r.fulfill({ contentType: 'image/png', body: tile }));
  await ctx.route('https://unpkg.com/**', r => r.fulfill({ contentType: 'image/png', body: tile }));
  await ctx.route('https://api.sportguide.online/**', async r => {
    const u = new URL(r.request().url());
    if (u.pathname === '/api/auth/telegram') return r.fulfill({ json: { token: 'synthetic-test-token' } });
    if (u.pathname.endsWith('/shown')) return r.fulfill({ json: true });
    if (u.pathname === '/api/places') {
      const params = Object.fromEntries(u.searchParams); requests.push(params);
      const page = Number(params.page || 0), size = Number(params.size || 20);
      let selected = params.placeType ? places.slice(0, 8) : places;
      if (params.search === 'ничего') selected = [];
      if (delayPage && params.page === '1') await new Promise(resolve => setTimeout(resolve, 500));
      if (params.page === undefined && failMap) return r.fulfill({ status: 503, json: { error: 'Временная ошибка' } });
      return r.fulfill({ json: params.page === undefined ? { list: selected } : {
        list: selected.slice(page * size, (page + 1) * size), page, size,
        totalElements: selected.length, totalPages: Math.ceil(selected.length / size),
      } });
    }
    if (u.pathname === '/api/events') return r.fulfill({ json: { list: [{ id: 1, name: 'Тренировка', date: '2026-11-24' }], canEdit: false } });
    return r.fulfill({ json: { list: [], canManage: false } });
  });
  const page = await ctx.newPage();
  page.on('pageerror', e => errors.push(e.message));
  const launch = new URLSearchParams({ tgWebAppData: new URLSearchParams({ auth_date: '1806060000', hash: 'a'.repeat(64), user: JSON.stringify({ id: 123, first_name: 'Test' }) }).toString(), tgWebAppVersion: '8.0', tgWebAppPlatform: 'android', tgWebAppThemeParams: JSON.stringify({ bg_color: '#ffffff' }) });
  await page.goto(`${base}/#${launch}`);
  await page.waitForSelector('[data-theme]');
  await page.evaluate(() => { location.hash = '/places'; });
  const cards = page.locator('[data-tour="place-card"]');
  await cards.first().waitFor();
  assert.equal(await cards.count(), 20);
  assert.equal(await page.locator('.page-header .eyebrow').count(), 0);
  assert.equal(requests.every(r => r.page === '0' && r.size === '20'), true);
  await page.getByRole('button', { name: 'Далее', exact: true }).click();
  await page.getByText('Место 21', { exact: true }).waitFor();
  assert.equal(await cards.count(), 20);
  await page.getByRole('button', { name: 'Далее', exact: true }).click();
  await page.getByText('Место 41', { exact: true }).waitFor();
  assert.equal(await cards.count(), 5);
  assert.equal(await page.getByRole('button', { name: 'Далее', exact: true }).isDisabled(), true);
  await page.getByRole('button', { name: 'Карта', exact: true }).click();
  await page.waitForFunction(() => document.querySelectorAll('.leaflet-marker-icon').length === 45);
  assert.equal(requests.at(-1).page, undefined);
  for (const viewport of [{ width: 390, height: 844 }, { width: 390, height: 600 }, { width: 844, height: 390 }, { width: 1440, height: 900 }]) {
    await page.setViewportSize(viewport);
    await page.waitForTimeout(400);
    const rect = await page.evaluate(() => {
      const body = document.querySelector('.map-modal-body');
      const map = document.querySelector('.map-container');
      const b = body.getBoundingClientRect(), m = map.getBoundingClientRect();
      const css = getComputedStyle(body);
      return { gap: b.bottom - m.bottom, padding: parseFloat(css.paddingBottom), height: m.height, overflow: body.scrollHeight > body.clientHeight, widthOverflow: document.documentElement.scrollWidth > innerWidth };
    });
    assert.ok(Math.abs(rect.gap - rect.padding) < 2, JSON.stringify(rect));
    assert.ok(rect.height > viewport.height * .65, JSON.stringify(rect));
    assert.equal(rect.overflow, false); assert.equal(rect.widthOverflow, false);
  }
  await page.setViewportSize({ width: 390, height: 844 });
  await page.screenshot({ path: '/tmp/sportguide-map-expanded.png' });
  await page.getByRole('button', { name: 'Отмена', exact: true }).click();
  await page.getByRole('button', { name: /Фильтры/ }).click();
  const sheet = page.getByRole('dialog', { name: 'Фильтры', exact: true });
  assert.equal(await page.evaluate(() => document.body.style.overflow), 'hidden');
  await page.keyboard.press('Shift+Tab');
  assert.equal(await sheet.getByRole('button', { name: 'Применить', exact: true }).evaluate(e => e === document.activeElement), true);
  await page.keyboard.press('Escape');
  assert.equal(await sheet.count(), 0);
  assert.equal(await page.getByRole('button', { name: /Фильтры/ }).evaluate(e => e === document.activeElement), true);
  await page.getByRole('button', { name: /Фильтры/ }).click();
  await page.getByRole('button', { name: 'Тренажерный зал', exact: true }).click();
  await page.getByRole('button', { name: 'Применить', exact: true }).click();
  await page.waitForFunction(() => document.querySelectorAll('[data-tour="place-card"]').length === 8);
  assert.equal(requests.at(-1).page, '0'); assert.equal(requests.at(-1).placeType, 'GYM');
  await page.getByRole('button', { name: 'Убрать фильтр: Тип места: Тренажерный зал', exact: true }).click();
  await page.waitForFunction(() => document.querySelectorAll('[data-tour="place-card"]').length === 20);
  assert.equal(requests.at(-1).placeType, undefined);
  await page.getByRole('button', { name: /Фильтры/ }).click();
  await page.getByRole('button', { name: 'Тренажерный зал', exact: true }).click();
  await page.getByRole('button', { name: 'Применить', exact: true }).click();
  await page.waitForFunction(() => document.querySelectorAll('[data-tour="place-card"]').length === 8);
  assert.equal(await page.getByRole('navigation', { name: 'Страницы мест' }).count(), 0);
  failMap = true;
  await page.getByRole('button', { name: 'Карта', exact: true }).click();
  await page.getByRole('alert').waitFor(); failMap = false;
  await page.getByRole('button', { name: 'Повторить', exact: true }).click();
  await page.waitForFunction(() => document.querySelectorAll('.leaflet-marker-icon').length === 8);
  assert.equal(requests.at(-1).placeType, 'GYM'); assert.equal(requests.at(-1).page, undefined);
  await page.getByRole('button', { name: 'Отмена', exact: true }).click();
  await page.getByPlaceholder('Поиск мест').fill('ничего');
  await page.getByText('Мест не найдено', { exact: true }).waitFor();
  await page.getByRole('button', { name: 'Карта', exact: true }).click();
  await page.getByText('По выбранным фильтрам нет мест с координатами.', { exact: true }).waitFor();
  await page.getByRole('button', { name: 'Отмена', exact: true }).click();
  await page.getByPlaceholder('Поиск мест').fill('');
  await page.getByRole('button', { name: /Фильтры/ }).click();
  await page.getByRole('button', { name: 'Сбросить', exact: true }).click();
  await page.getByRole('button', { name: 'Применить', exact: true }).click();
  await page.getByText('Страница 1 из 3', { exact: true }).waitFor();
  delayPage = true;
  await page.getByRole('button', { name: 'Далее', exact: true }).click();
  await page.getByPlaceholder('Поиск мест').fill('ничего');
  await page.getByText('Мест не найдено', { exact: true }).waitFor();
  await page.waitForTimeout(700);
  assert.equal(await cards.count(), 0);
  await page.evaluate(() => { location.hash = '/events'; });
  await page.getByText('Тренировка', { exact: true }).waitFor();
  assert.equal(await page.locator('.page-header .eyebrow').count(), 0);
  assert.deepEqual(errors, []);
  console.log('PASS: database page requests, last page, filter/search reset, stale responses, all filtered map markers, map retry/empty states, full map height at four sizes, no catalog counters or page errors');
} finally { await browser.close(); }
