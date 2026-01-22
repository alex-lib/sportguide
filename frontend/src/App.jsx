import { useEffect, useState } from 'react';
import { HashRouter as Router, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import WebApp from '@twa-dev/sdk';
import { authService } from './services/auth.js';
import Layout from './components/Layout.jsx';
import Home from './pages/Home.jsx';
import Events from './pages/Events.jsx';
import Places from './pages/Places.jsx';
import JointTrainings from './pages/JointTrainings.jsx';
import TrainingPrograms from './pages/TrainingPrograms.jsx';
import Coaches from './pages/Coaches.jsx';
import Loading from './components/Loading.jsx';
import './App.css';

// Component to handle Telegram back button
const TelegramBackButtonHandler = () => {
  const navigate = useNavigate();

  useEffect(() => {
    // Handle Telegram back button
    const handleBackButton = () => {
      if (window.history.length > 1) {
        navigate(-1);
      } else {
        WebApp.close();
      }
    };

    // Enable back button in Telegram
    WebApp.BackButton.onClick(handleBackButton);
    
    // Show back button if not on home page
    const updateBackButton = () => {
      if (window.location.hash !== '#/' && window.location.hash !== '') {
        WebApp.BackButton.show();
      } else {
        WebApp.BackButton.hide();
      }
    };

    updateBackButton();
    window.addEventListener('hashchange', updateBackButton);

    return () => {
      window.removeEventListener('hashchange', updateBackButton);
      WebApp.BackButton.offClick(handleBackButton);
    };
  }, [navigate]);

  return null;
};

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const init = async () => {
      try {
        // Check if running in Telegram
        const isTelegram = typeof window !== 'undefined' && window.Telegram?.WebApp;
        
        if (isTelegram) {
          // Initialize Telegram WebApp
          WebApp.ready();
          
          // Expand the app to full height
          WebApp.expand();
          
          // Disable pull-to-refresh
          WebApp.enableClosingConfirmation();
          
          // Set theme colors - modern green/teal scheme
          WebApp.setHeaderColor('#1a9b8e');
          WebApp.setBackgroundColor('#f0f9f8');
          
          // Set main button color to match theme
          WebApp.MainButton.setParams({
            color: '#1a9b8e',
            text_color: '#ffffff',
          });
        }

        // Check if already authenticated
        if (authService.isAuthenticated()) {
          setIsAuthenticated(true);
          setIsLoading(false);
          return;
        }

        // Try to authenticate (only works in Telegram)
        if (isTelegram) {
          await authService.authenticate();
          setIsAuthenticated(true);
        } else {
          // For development/testing outside Telegram
          console.warn('Not running in Telegram - authentication skipped');
          // You can set a test token here for development
          // localStorage.setItem('auth_token', 'test-token');
          setIsAuthenticated(false);
        }
      } catch (error) {
        console.error('Initialization error:', error);
        const isTelegram = typeof window !== 'undefined' && window.Telegram?.WebApp;
        if (isTelegram && WebApp.showAlert) {
          WebApp.showAlert('Не удалось инициализировать приложение: ' + (error.message || 'Неизвестная ошибка'));
        }
        // Don't block the app - show error but allow user to see what's wrong
        setIsAuthenticated(false);
      } finally {
        setIsLoading(false);
      }
    };

    init();
  }, []);

  if (isLoading) {
    return <Loading />;
  }

  if (!isAuthenticated) {
    return (
      <div className="page-container" style={{ padding: '24px', textAlign: 'center' }}>
        <div className="empty-state">
          <div className="empty-state-icon">⚠️</div>
          <h2 style={{ color: 'var(--error-color)', marginBottom: '16px' }}>Ошибка аутентификации</h2>
          <p style={{ marginBottom: '16px' }}>
            Не удалось войти в приложение. Убедитесь, что вы открыли приложение из Telegram.
          </p>
          <button
            className="btn btn-primary"
            onClick={() => window.location.reload()}
          >
            Перезагрузить
          </button>
        </div>
      </div>
    );
  }

  return (
    <Router>
      <TelegramBackButtonHandler />
      <Layout>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/events" element={<Events />} />
          <Route path="/places" element={<Places />} />
          <Route path="/joint-trainings" element={<JointTrainings />} />
          <Route path="/training-programs" element={<TrainingPrograms />} />
          <Route path="/coaches" element={<Coaches />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Layout>
    </Router>
  );
}

export default App;

