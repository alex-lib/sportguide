// Synthetic launch and API fixtures: no production authentication or bot calls.
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
const { chromium } = await import(process.env.PLAYWRIGHT_MODULE || 'playwright');
const base = process.env.SMOKE_BASE_URL || 'http://127.0.0.1:4175';
const sdk = await readFile(new URL('../node_modules/@twa-dev/sdk/dist/telegram-web-apps.js', import.meta.url), 'utf8');
const browser = await chromium.launch({ channel: 'chrome', headless: true });
const tile = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQIHWP4z8DwHwAFgAI/ScLbtAAAAABJRU5ErkJggg==', 'base64');
const places = [
  { id: 173, name: 'HYDRA GYM', address: 'ул. Бакунина, 45А', coordinates: 'https://maps.google.com/?q=51.671105,39.185911' },
  { id: 174, name: 'Второй зал', address: 'Другой адрес', coordinates: 'https://maps.google.com/?q=51.7,39.3' },
  { id: 175, name: 'Без координат', coordinates: 'Координаты места не указаны' },
];
const portrait = process.env.COACH_TEST_PHOTO ? await readFile(process.env.COACH_TEST_PHOTO) : tile;
const errors = [], placeRequests = [];
let failProfile = false;
const profile = { id: 1, name: 'Тренер с местами', telegramUsername: '[@test_coach](https://t.me/test_coach)',
  sportTypes: ['Фитнес'], age: 33, sex: 'Мужчина', yearsOfExperience: 8,
  education: 'Инструктор групповых программ по направлению «кроссфит»; академия спорта; специалист центра тестирования ГТО.',
  phoneNumber: '+79991234567', description: 'Персональные и групповые занятия, составление тренировочных программ.',
  photoUrl: 'coaches/test.jpg', workPlaces: places, workPlacesNames: places.map(p => p.name) };
try {
  const ctx = await browser.newContext({ viewport: { width: 390, height: 844 } });
  await ctx.addInitScript(() => {
    window.nativeEvents = [];
    window.TelegramWebviewProxy = { postEvent: (type, data) => window.nativeEvents.push({ type, data }) };
  });
  await ctx.route('https://telegram.org/js/telegram-web-app.js*', r => r.fulfill({ contentType: 'application/javascript', body: sdk }));
  await ctx.route('https://fonts.googleapis.com/**', r => r.fulfill({ contentType: 'text/css', body: '' }));
  await ctx.route('https://*.tile.openstreetmap.org/**', r => r.fulfill({ contentType: 'image/png', body: tile }));
  await ctx.route('https://unpkg.com/**', r => r.fulfill({ contentType: 'image/png', body: tile }));
  await ctx.route('https://api.sportguide.online/**', r => {
    const u = new URL(r.request().url());
    if (u.pathname === '/api/auth/telegram') return r.fulfill({ json: { token: 'synthetic-test-token' } });
    if (u.pathname.endsWith('/shown')) return r.fulfill({ json: true });
    if (u.pathname === '/api/places') placeRequests.push(u.href);
    if (u.pathname === '/api/coaches/photo') return r.fulfill({ contentType: process.env.COACH_TEST_PHOTO ? 'image/jpeg' : 'image/png', body: portrait });
    if (u.pathname === '/api/coaches/1/profile') return r.fulfill(failProfile ? { status: 503, json: {} } : { json: profile });
    if (u.pathname === '/api/coaches/2/profile') return r.fulfill({ json: { id: 2, name: 'Тренер без мест', yearsOfExperience: 0 } });
    if (u.pathname.endsWith('/profile')) return r.fulfill({ status: 404, json: {} });
    if (u.pathname === '/api/coaches') return r.fulfill({ json: { list: [
      { id: 1, name: 'Тренер с местами', workPlaces: places, workPlacesNames: places.map(p => p.name) },
      { id: 2, name: 'Тренер без мест', workPlaces: [], workPlacesNames: [] },
      { id: 3, name: 'Старая версия API', workPlacesNames: ['Название зала из старого API'] },
    ] } });
    return r.fulfill({ json: { list: [], canManage: false } });
  });
  const page = await ctx.newPage();
  page.on('pageerror', e => errors.push(e.message));
  const launch = new URLSearchParams({ tgWebAppData: new URLSearchParams({ auth_date: '1806060000', hash: 'a'.repeat(64), user: JSON.stringify({ id: 123, first_name: 'Test' }) }).toString(), tgWebAppVersion: '8.0', tgWebAppPlatform: 'android', tgWebAppThemeParams: '{}' });
  await page.goto(`${base}/#${launch}`);
  await page.waitForSelector('[data-theme]');
  await page.evaluate(() => { location.hash = '/coaches'; });
  await page.getByText('Тренер с местами', { exact: true }).waitFor();
  assert.equal(await page.locator('[data-tour="coach-card"]').count(), 3);
  await page.getByText('Название зала из старого API', { exact: true }).waitFor();
  for (const place of places.slice(0, 2)) {
    await page.getByRole('button', { name: `Показать ${place.name} на карте`, exact: true }).click();
    await page.locator('.leaflet-marker-icon').waitFor();
    assert.equal(await page.locator('.leaflet-marker-icon').count(), 1);
    await page.locator('.leaflet-marker-icon').click();
    assert.equal(await page.locator('.leaflet-popup-content strong').innerText(), place.name);
    assert.equal(await page.locator('.leaflet-popup-content a').getAttribute('href'), place.coordinates);
    assert.ok((await page.locator('.leaflet-popup-content').innerText()).includes(place.address));
    assert.equal(await page.evaluate(() => document.documentElement.scrollWidth > innerWidth), false);
    await page.getByRole('button', { name: 'Отмена', exact: true }).click();
    assert.equal(await page.locator('.leaflet-container').count(), 0);
  }
  await page.getByRole('button', { name: 'Показать Без координат на карте', exact: true }).click();
  await page.getByRole('status').filter({ hasText: 'нет мест с координатами' }).waitFor();
  await page.getByRole('button', { name: 'Отмена', exact: true }).click();
  assert.equal(await page.getByRole('link', { name: 'Связаться с тренером', exact: true }).count(), 0);
  await page.getByRole('link', { name: 'Открыть карточку', exact: true }).first().click();
  const detail = page.locator('[data-tour="coach-profile"]');
  await detail.waitFor();
  assert.equal(new URL(page.url()).hash, '#/coaches/1');
  for (const text of ['@test_coach', 'Фитнес', '33 года', 'Мужской', '8 лет', ...profile.education.split('; ').map(part => part.trim()), profile.description, profile.phoneNumber]) {
    await detail.getByText(text, { exact: true }).waitFor();
  }
  await detail.locator('img.coach-profile-photo').waitFor();
  const contact = detail.getByRole('link', { name: 'Связаться с тренером', exact: true });
  assert.equal(await contact.getAttribute('href'), 'https://t.me/test_coach');
  await contact.click();
  assert.ok(await page.evaluate(() => window.nativeEvents.some(e => e.type === 'web_app_open_tg_link' && JSON.parse(e.data).path_full === '/test_coach')));
  assert.equal(await detail.locator('.coach-profile-phone').getAttribute('href'), `tel:${profile.phoneNumber}`);
  await detail.getByRole('button', { name: 'Показать HYDRA GYM на карте', exact: true }).click();
  await page.locator('.leaflet-marker-icon').waitFor();
  assert.equal(await page.locator('.leaflet-marker-icon').count(), 1);
  await page.locator('.leaflet-marker-icon').click();
  assert.equal(await page.locator('.leaflet-popup-content a').getAttribute('href'), places[0].coordinates);
  await page.getByRole('button', { name: 'Отмена', exact: true }).click();
  for (const width of [320, 390, 1440]) {
    await page.setViewportSize({ width, height: 844 });
    assert.equal(await page.evaluate(() => document.documentElement.scrollWidth > innerWidth), false);
  }
  await page.setViewportSize({ width: 390, height: 844 });
  await page.evaluate(() => window.scrollTo(0, 0));
  await page.waitForTimeout(250);
  await page.screenshot({ path: '/tmp/sportguide-coach-profile.png' });
  await page.evaluate(() => document.documentElement.dataset.theme = 'dark');
  await page.waitForTimeout(250);
  await page.screenshot({ path: '/tmp/sportguide-coach-profile-dark.png' });
  await page.evaluate(() => document.documentElement.dataset.theme = 'light');
  await page.reload();
  await detail.waitFor();
  assert.equal(await page.getByRole('button', { name: 'К тренерам', exact: true }).count(), 0);
  await page.locator('.tabbar').getByRole('link', { name: 'Тренеры', exact: true }).click();
  await page.locator('[data-tour="coach-card"]').first().waitFor();
  // The photo/name link also opens the profile, and browser Back restores the list.
  await page.locator('.coach-profile-link').first().click();
  await detail.waitFor();
  await page.goBack();
  await page.getByRole('link', { name: 'Открыть карточку', exact: true }).first().waitFor();
  failProfile = true;
  await page.getByRole('link', { name: 'Открыть карточку', exact: true }).first().click();
  await page.getByRole('alert').waitFor();
  failProfile = false;
  await page.getByRole('button', { name: 'Повторить', exact: true }).click();
  await detail.waitFor();
  await page.evaluate(() => { location.hash = '/coaches/2'; });
  await detail.getByText('Тренер без мест', { exact: true }).waitFor();
  await detail.getByText('0 лет', { exact: true }).waitFor();
  await detail.getByText('Контакт Telegram не указан.', { exact: true }).waitFor();
  assert.equal(await detail.getByRole('link', { name: 'Связаться с тренером', exact: true }).count(), 0);
  await page.evaluate(() => { location.hash = '/coaches/999'; });
  await page.getByText('Тренер не найден', { exact: true }).waitFor();
  assert.equal(await page.getByRole('button', { name: 'К тренерам', exact: true }).count(), 0);
  await page.locator('.tabbar').getByRole('link', { name: 'Тренеры', exact: true }).click();
  await page.getByRole('link', { name: 'Открыть карточку', exact: true }).first().waitFor();
  assert.deepEqual(placeRequests, []);
  assert.deepEqual(errors, []);
  console.log('PASS: list workplace maps preserved; full profile, photo, all fields, Telegram SDK chat command, phone link, profile map, reload/back, mobile/desktop, retry/404/missing fields; no entire places catalog load');
} finally { await browser.close(); }
