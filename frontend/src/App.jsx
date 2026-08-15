import { useEffect, useState } from 'react';
import { HashRouter as Router, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import WebApp from '@twa-dev/sdk';
import { useTheme } from './context/ThemeContext';
import { authService } from './services/auth.js';
import Layout from './components/Layout.jsx';
import Home from './pages/Home.jsx';
import Events from './pages/Events.jsx';
import Places from './pages/Places.jsx';
import JointTrainings from './pages/JointTrainings.jsx';
import TrainingPrograms from './pages/TrainingPrograms.jsx';
import Coaches from './pages/Coaches.jsx';
import { Page, EmptyState, Button, Loading } from './ui';

const THEME_COLORS = {
  light: { header: '#f2f4f7', background: '#f2f4f7' },
  dark: { header: '#0c0c0f', background: '#0c0c0f' }
};

const TelegramChromSync = () => {
  const { isDark } = useTheme();
  useEffect(() => {
    const colors = isDark ? THEME_COLORS.dark : THEME_COLORS.light;
    if (WebApp.setHeaderColor) WebApp.setHeaderColor(colors.header);
    if (WebApp.setBackgroundColor) WebApp.setBackgroundColor(colors.background);
  }, [isDark]);
  return null;
};

// Component to handle Telegram back button
const TelegramBackButtonHandler = () => {
  const navigate = useNavigate();

  useEffect(() => {
    const isTelegram = typeof window !== 'undefined' && window.Telegram?.WebApp;
    if (!isTelegram || !WebApp?.BackButton) {
      return;
    }

    try {
      const handleBackButton = () => {
        if (window.history.length > 1) {
          navigate(-1);
        } else {
          if (WebApp.close) WebApp.close();
        }
      };

      WebApp.BackButton.onClick(handleBackButton);
      
      const updateBackButton = () => {
        try {
          if (window.location.hash !== '#/' && window.location.hash !== '') {
            WebApp.BackButton.show();
          } else {
            WebApp.BackButton.hide();
          }
        } catch (e) {
          console.warn('Back button update failed:', e);
        }
      };

      updateBackButton();
      window.addEventListener('hashchange', updateBackButton);

      return () => {
        window.removeEventListener('hashchange', updateBackButton);
        try {
          if (WebApp.BackButton) {
            WebApp.BackButton.offClick(handleBackButton);
          }
        } catch (e) {
          console.warn('Back button cleanup failed:', e);
        }
      };
    } catch (error) {
      console.warn('Telegram back button setup failed:', error);
    }
  }, [navigate]);

  return null;
};

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const init = async () => {
      try {
        const isTelegram = typeof window !== 'undefined' && window.Telegram?.WebApp;
        
        if (isTelegram) {
          try {
            if (WebApp.ready) WebApp.ready();
            if (WebApp.expand) WebApp.expand();
            if (WebApp.enableClosingConfirmation) WebApp.enableClosingConfirmation();

            if (WebApp.MainButton?.setParams) {
              WebApp.MainButton.setParams({ color: '#0f9d8f', text_color: '#ffffff' });
            }
          } catch (tgError) {
            console.warn('Telegram WebApp initialization error:', tgError);
          }
        }

        if (authService.isAuthenticated()) {
          setIsAuthenticated(true);
          setIsLoading(false);
          return;
        }

        if (isTelegram) {
          try {
            await authService.authenticate();
            setIsAuthenticated(true);
          } catch (authError) {
            console.error('Authentication error:', authError);
            setIsAuthenticated(false);
          }
        } else {
          console.warn('Not running in Telegram - authentication skipped');
          setIsAuthenticated(false);
        }
      } catch (error) {
        console.error('Initialization error:', error);
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
      <Page>
        <EmptyState
          icon="triangle-alert"
          accent
          title="Не удалось войти"
          message="Откройте приложение из Telegram и попробуйте снова."
          action={
            <Button fullWidth onClick={() => window.location.reload()}>
              Перезагрузить
            </Button>
          }
        />
      </Page>
    );
  }

  return (
    <Router>
      <TelegramChromSync />
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
