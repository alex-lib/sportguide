import WebApp from '@twa-dev/sdk';
import { hasTelegramLaunchData } from './telegramLaunch.js';

// The SDK synchronously reads Telegram launch data (including its session cache).
// Decide before importing/mounting the application or starting API requests.
// DEV only preserves the existing local workflow; it never grants API access.
if (import.meta.env.DEV || hasTelegramLaunchData(WebApp)) {
  import('./bootstrap.jsx');
} else {
  window.location.replace('/landing/');
}
