import { apiService } from './api.js';
import WebApp from '@twa-dev/sdk';

export class AuthService {
  async authenticate() {
    try {
      const isTelegram = window.Telegram && window.Telegram.WebApp;

      let initData;
      if (isTelegram) {
        initData = window.Telegram.WebApp.initData;
      }

      if (!initData) {
        throw new Error('Telegram initData not available');
      }

      const response = await apiService.authenticateTelegram(initData);

      localStorage.setItem('auth_token', response.token);

      if (isTelegram) {
        window.Telegram.WebApp.expand();
        window.Telegram.WebApp.ready();
      }

      return response;
    } catch (error) {
      if (window.Telegram?.WebApp?.showAlert) {
        window.Telegram.WebApp.showAlert('Ошибка аутентификации');
      }
      throw error;
    }
  }

  isAuthenticated() {
    return !!localStorage.getItem('auth_token');
  }

  logout() {
    apiService.clearAuth();
     localStorage.removeItem('auth_token');
//    WebApp.close();
if (window.Telegram?.WebApp) {
      WebApp.close();
    }
  }
}

export const authService = new AuthService();

