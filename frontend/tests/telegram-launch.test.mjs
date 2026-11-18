import assert from 'node:assert/strict';
import test from 'node:test';
import { hasTelegramLaunchData } from '../src/telegramLaunch.js';

const initData = new URLSearchParams({
  auth_date: '1805536800', hash: 'a'.repeat(64),
  user: JSON.stringify({ id: 123, first_name: 'Test' }), start_param: 'event_42',
}).toString();

test('ordinary browser, SDK object alone and arbitrary launch values are not a launch', () => {
  for (const sdk of [undefined, {}, { platform: 'android' }, { initData: '' },
    { initData: 'true' }, { initData: 'startapp=event_42' },
    { initData: 'auth_date=1&hash=invalid' }, { initData: 'hash=' + 'a'.repeat(64) }]) {
    assert.equal(hasTelegramLaunchData(sdk), false);
  }
});

test('SDK launch data works for native and web clients without changing parameters', () => {
  for (const platform of ['android', 'ios', 'tdesktop', 'macos', 'weba', 'webk', 'unknown']) {
    const sdk = { initData, platform };
    assert.equal(hasTelegramLaunchData(sdk), true);
    assert.equal(sdk.initData, initData);
  }
});

test('client gate deliberately does not verify signatures: API validation is required', () => {
  assert.equal(hasTelegramLaunchData({ initData }), true);
  // This synthetic hash passes navigation only. It must never grant API access.
});
