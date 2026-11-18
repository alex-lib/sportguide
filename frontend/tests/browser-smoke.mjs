// Run against a production build served by Nginx. API replies and Telegram launch
// data are synthetic: real Telegram clients/signatures need a separate smoke test.
import assert from 'node:assert/strict';
import { readFile, readdir } from 'node:fs/promises';
const { chromium } = await import(process.env.PLAYWRIGHT_MODULE || 'playwright');
const base = process.env.SMOKE_BASE_URL || 'https://127.0.0.1:18443';
const sdk = await readFile(new URL('../node_modules/@twa-dev/sdk/dist/telegram-web-apps.js', import.meta.url), 'utf8');
const browser = await chromium.launch({ channel: 'chrome', headless: true });
const initData = new URLSearchParams({
  auth_date: '1805536800', hash: 'a'.repeat(64),
  user: JSON.stringify({ id: 123, first_name: 'Test' }), start_param: 'event_42',
}).toString();
function launchHash(platform = 'android') {
  return new URLSearchParams({ tgWebAppData: initData, tgWebAppVersion: '8.0',
    tgWebAppPlatform: platform, tgWebAppStartParam: 'event_42' }).toString();
}
async function listFiles(directory) {
  const files = [];
  for (const entry of await readdir(directory, { withFileTypes: true })) {
    if (entry.isDirectory()) {
      const children = await listFiles(new URL(entry.name + '/', directory));
      files.push(...children.map(file => entry.name + '/' + file));
    } else {
      files.push(entry.name);
    }
  }
  return files;
}
async function context({ blockSdk = false, rejectAuth = false } = {}) {
  const ctx = await browser.newContext({ ignoreHTTPSErrors: true });
  const auth = [];
  await ctx.route('https://telegram.org/js/telegram-web-app.js*', r => blockSdk
    ? r.abort() : r.fulfill({ contentType: 'application/javascript', body: sdk }));
  await ctx.route('https://fonts.googleapis.com/**', r => r.fulfill({ contentType: 'text/css', body: '' }));
  await ctx.route('https://api.sportguide.online/**', async r => {
    if (r.request().url().endsWith('/api/auth/telegram')) {
      auth.push(r.request().postDataJSON());
      return r.fulfill({ status: rejectAuth ? 400 : 200, json: rejectAuth ? {} : { token: 'synthetic-test-token' } });
    }
    return r.fulfill({ json: r.request().url().endsWith('/shown') ? true : { list: [], canEdit: false } });
  });
  return { ctx, auth };
}
try {
  const { ctx } = await context();
  // Test the exact served bytes and MIME types, not a Python/Vite approximation.
  const canonical = await ctx.request.get(base + '/landing?source=test', { maxRedirects: 0 });
  assert.equal(canonical.status(), 308);
  assert.equal(canonical.headers().location, '/landing/?source=test');
  for (const file of await listFiles(new URL('../../landing/', import.meta.url))) {
    const response = await ctx.request.get(base + '/landing/' + file);
    assert.equal(response.status(), 200, file);
    assert.deepEqual(await response.body(), await readFile(new URL('../../landing/' + file, import.meta.url)), file);
    if (file.endsWith('.js')) assert.match(response.headers()['content-type'], /javascript/);
  }
  for (const missing of ['missing.js', 'assets/missing.js', 'nested/missing']) {
    const response = await ctx.request.get(base + '/landing/' + missing);
    assert.equal(response.status(), 404);
    assert.ok(!(await response.text()).includes('id="root"'));
  }
  console.log('PASS: landing bytes, all assets, module MIME, slash/query redirect, true 404');
  await ctx.close();

  for (const suffix of ['/', '/?telegram=true&startapp=event_42', '/?dev=true&mock=true', '/#/events', '/events/42', '/#tgWebAppData=garbage']) {
    const { ctx, auth } = await context();
    const page = await ctx.newPage();
    const bootstrapRequests = [];
    page.on('request', r => { if (/\/assets\/bootstrap-/.test(r.url())) bootstrapRequests.push(r.url()); });
    await page.addInitScript(() => {
      localStorage.setItem('auth_token', 'old-token');
      document.addEventListener('DOMContentLoaded', () => {
        const root = document.getElementById('root');
        if (root) new MutationObserver(() => {
          if (root.childNodes.length) sessionStorage.setItem('app-flashed', 'yes');
        }).observe(root, { childList: true });
      });
    });
    await page.goto(base + suffix);
    await page.waitForURL(base + '/landing/');
    assert.equal(await page.evaluate(() => sessionStorage.getItem('app-flashed')), null);
    assert.deepEqual(bootstrapRequests, []);
    assert.deepEqual(auth, []);
    await ctx.close();
  }
  console.log('PASS: browser redirects, stored JWT and URL flags do not bypass, no app flash/API calls');

  {
    const { ctx } = await context();
    const page = await ctx.newPage();
    await page.goto(base + '/landing/?before=1');
    await page.goto(base + '/');
    await page.waitForURL(base + '/landing/');
    await page.goBack();
    await page.waitForURL(base + '/landing/?before=1');
    await page.goForward();
    await page.waitForURL(base + '/landing/');
    for (const width of [390, 1440]) {
      await page.setViewportSize({ width, height: 900 });
      await page.locator('img').evaluateAll(imgs => imgs.forEach(img => img.loading = 'eager'));
      await page.waitForFunction(() => [...document.images].every(i => i.complete && i.naturalWidth > 0));
      assert.equal(await page.evaluate(() => document.documentElement.scrollWidth > innerWidth), false);
    }
    await ctx.close();
  }
  console.log('PASS: Back/Forward without loops; landing images and widths 390/1440');

  for (const platform of ['android', 'ios', 'tdesktop', 'macos', 'weba', 'webk']) {
    const { ctx, auth } = await context({ blockSdk: platform === 'macos' });
    const page = await ctx.newPage();
    await page.goto(base + '/?campaign=kept#/events?' + launchHash(platform));
    await page.waitForFunction(() => document.getElementById('root')?.textContent.includes('События'));
    assert.ok(page.url().includes('/?campaign=kept#/events?'));
    assert.ok(auth.length > 0);
    assert.equal(auth[0].initData, initData);
    assert.equal(await page.evaluate(() => window.Telegram.WebApp.initDataUnsafe.start_param), 'event_42');
    await page.evaluate(() => { location.hash = '/places'; });
    await page.reload();
    await page.waitForFunction(() => document.getElementById('root')?.textContent.includes('Места'));
    assert.equal(await page.evaluate(() => window.Telegram.WebApp.initData), initData);
    assert.ok(page.url().endsWith('#/places'));
    await ctx.close();
  }
  console.log('PASS: SDK native/web launch parameters, deep route, reload/session recovery, external SDK failure fallback');

  {
    const { ctx, auth } = await context();
    // A public parent cannot frame loopback in current Chrome (Local Network Access).
    // Use a synthetic public origin, proxying the actual local Nginx bytes/headers.
    await ctx.route('https://sportguide.test/**', async r => {
      const url = new URL(r.request().url());
      const response = await ctx.request.get(base + url.pathname + url.search);
      await r.fulfill({ status: response.status(), headers: response.headers(), body: await response.body() });
    });
    await ctx.route('https://web.telegram.org/smoke', r => r.fulfill({ contentType: 'text/html',
      body: `<iframe src="https://sportguide.test/#/events?${launchHash('webk')}"></iframe>` }));
    const page = await ctx.newPage();
    await page.goto('https://web.telegram.org/smoke');
    await page.frameLocator('iframe').locator('#root').getByText('События', { exact: true }).first().waitFor();
    assert.equal(auth[0].initData, initData);
    assert.equal(page.frames().filter(f => f.url().includes('/landing/')).length, 0);
    await ctx.close();
  }
  console.log('PASS: cross-origin Telegram Web iframe model');

  {
    const { ctx } = await context({ rejectAuth: true });
    const page = await ctx.newPage();
    await page.goto(base + '/#' + launchHash());
    await page.getByText('Не удалось войти', { exact: true }).waitFor();
    assert.equal(await page.evaluate(() => localStorage.getItem('auth_token')), null);
    await ctx.close();
  }
  console.log('PASS: API rejection still blocks authentication');

  if (process.env.SMOKE_DEV_URL) {
    const { ctx } = await context();
    const page = await ctx.newPage();
    await page.goto(process.env.SMOKE_DEV_URL);
    await page.getByText('Не удалось войти', { exact: true }).waitFor();
    assert.ok(!page.url().includes('/landing/'));
    assert.equal(await page.evaluate(() => localStorage.getItem('auth_token')), null);
    await ctx.close();
    console.log('PASS: Vite development stays accessible without granting authentication');
  }
} finally {
  await browser.close();
}
