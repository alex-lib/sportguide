import test from 'node:test';
import assert from 'node:assert/strict';
import { readThemeMode, resolveSystemTheme } from '../src/context/theme.js';

test('new users use system mode; existing manual preferences are retained', () => {
  for (const value of [null, 'unknown', 'system', 'scheduled']) {
    assert.equal(readThemeMode({ getItem: () => value }), 'system');
  }
  for (const value of ['light', 'dark']) {
    assert.equal(readThemeMode({ getItem: () => value }), value);
  }
  assert.equal(readThemeMode({ getItem: () => { throw new Error('blocked'); } }), 'system');
});

test('system mode follows Telegram launch theme or OS outside Telegram', () => {
  assert.equal(resolveSystemTheme({ initData: 'launch', colorScheme: 'dark' }, { matches: false }), 'dark');
  assert.equal(resolveSystemTheme({ initData: 'launch', colorScheme: 'light' }, { matches: true }), 'light');
  assert.equal(resolveSystemTheme({ initData: '', colorScheme: 'light' }, { matches: true }), 'dark');
  assert.equal(resolveSystemTheme(null, { matches: false }), 'light');
  assert.equal(resolveSystemTheme(null, undefined), 'light');
});
