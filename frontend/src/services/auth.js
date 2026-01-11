import { apiService } from './api.js';
import WebApp from '@twa-dev/sdk';

export class AuthService {
  async authenticate() {
    try {

    const isTelegram =
            window.Telegram &&
            window.Telegram.WebApp &&
            WebApp.initData;

      // Get initData from Telegram WebApp
      const initData = WebApp.initData;
//      const initData = isTelegram
//              ? WebApp.initData
//              : 'DEV_MODE';
      
      if (!initData) {
        throw new Error('Telegram initData not available');
      }

      // Authenticate with backend
//      await apiService.authenticateTelegram(initData);

const response = await apiService.authenticateTelegram(initData);


      localStorage.setItem('auth_token', response.token);

      if (isTelegram) {
        WebApp.expand();
        WebApp.ready();
      }
      
      // Expand WebApp to full height
//      WebApp.expand();
//      WebApp.ready();
    } catch (error) {
      console.error('Authentication failed:', error);
      if (window.Telegram?.WebApp) {
              WebApp.showAlert('Ошибка аутентификации');
            }
//      WebApp.showAlert('Ошибка аутентификации. Пожалуйста, попробуйте снова.');
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

