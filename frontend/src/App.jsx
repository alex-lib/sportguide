import { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
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

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const init = async () => {
      try {
        // Initialize Telegram WebApp
        WebApp.ready();
        
        // Set theme colors
        WebApp.setHeaderColor('#3390ec');
        WebApp.setBackgroundColor('#ffffff');

        // Check if already authenticated
        if (authService.isAuthenticated()) {
          setIsAuthenticated(true);
          setIsLoading(false);
          return;
        }

        // Try to authenticate
        await authService.authenticate();
        setIsAuthenticated(true);
      } catch (error) {
        console.error('Initialization error:', error);
        WebApp.showAlert('Не удалось инициализировать приложение');
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
    return <Loading message="Аутентификация..." />;
  }

  return (
    <Router>
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

