import assert from 'node:assert/strict';
import test from 'node:test';
import { coachTelegram } from '../src/utils/coachTelegram.js';

test('coach contact resolves existing API Markdown and plain usernames to a Telegram chat', () => {
  for (const value of ['[@test_coach](https://t.me/test_coach)', '@test_coach', 'test_coach', 'https://t.me/test_coach/']) {
    assert.deepEqual(coachTelegram(value), { username: 'test_coach', url: 'https://t.me/test_coach' });
  }
});
test('missing or invalid Telegram contact does not turn into a phone or arbitrary URL', () => {
  for (const value of [null, undefined, '', '+79991234567', 'tel:+79991234567', 'javascript:alert(1)', 'https://example.com/test_coach', '[@test_coach](https://t.me/another_coach)', 'a/b', {}]) {
    assert.equal(coachTelegram(value), null);
  }
});
