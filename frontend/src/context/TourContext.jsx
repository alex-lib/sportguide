import { useRef, useEffect, useState, createContext } from 'react';
import { useLocation } from 'react-router-dom';
import Joyride from 'react-joyride';
import { apiService } from '../services/api.js';

export const TourContext = createContext(null);

export const TourProvider = ({ children }) => {
  return (
    <TourContext.Provider value={{}}>
      {children}
    </TourContext.Provider>
  );
};

export const TourPage = () => {
  const location = useLocation();
  const runRef = useRef(false);
  const [tourSteps, setTourSteps] = useState([]);
  const [skipTour, setSkipTour] = useState(false);

  const route = location.pathname === '/' ? '/' : location.pathname;

  useEffect(() => {
    runRef.current = false;
    setSkipTour(false);

    let cancelled = false;

    apiService.isTourShown(route)
      .then((isShown) => {
        if (cancelled) return;
        if (isShown) {
          setSkipTour(true);
          return;
        }

        return apiService.getTourSteps(route);
      })
      .then((steps) => {
        if (cancelled) return;
        if (steps && steps.length > 0) {
          const formatted = steps.map(step => ({
            target: step.target,
            content: step.content,
            placement: step.placement || 'bottom',
            primary: step.isPrimary || false,
          }));
          setTourSteps(formatted);
        } else {
          setSkipTour(true);
        }
      })
      .catch((e) => {
        console.error('TourPage error:', e);
        setSkipTour(true);
      });

    return () => {
      cancelled = true;
    };
  }, [route]);

  return (
    <Joyride
      steps={skipTour ? [] : tourSteps}
      callback={(data) => {
        if (data.status === 'finished') {
          apiService.syncTourShown(route).catch(() => {});
        }
      }}
      run={tourSteps.length > 0 && !runRef.current}
      continuous
      showSkipButton
      styles={{
        overlay: { backgroundColor: 'rgba(0, 0, 0, 0.5)' },
        tooltip: { maxWidth: 'calc(100vw - 32px)', padding: '16px' },
        tooltipContent: { fontSize: '14px' },
        buttonClose: { right: '-10px', top: '-10px' },
        buttonNext: { backgroundColor: '#0f9d8f' },
        buttonSkip: { color: '#666' },
        buttonBack: { marginRight: '8px' },
        spotlight: { backgroundColor: 'transparent' },
        spotlightPadding: 0,
      }}
    />
  );
};
