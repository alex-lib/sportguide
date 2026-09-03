import { useContext, useRef, useEffect, useState } from 'react';
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
  const [shouldRun, setShouldRun] = useState(false);

  const route = location.pathname === '/' ? '/' : location.pathname;

  useEffect(() => {
    runRef.current = false;
    setTourSteps([]);
    setShouldRun(false);

    Promise.all([
      apiService.isTourShown(route),
      apiService.getTourSteps(route),
    ])
      .then(([isShown, steps]) => {
        if (!isShown && steps?.length > 0) {
          const formatted = steps.map(step => ({
            target: step.target,
            content: step.content,
            placement: step.placement || 'bottom',
            primary: step.isPrimary || false,
          }));
          setTourSteps(formatted);
          setShouldRun(true);
        }
      })
      .catch((e) => {
        console.error('Failed to load tour:', e);
        setTourSteps([]);
        setShouldRun(false);
      });
  }, [route]);

  return (
    <Joyride
      steps={shouldRun ? tourSteps : []}
      callback={(data) => {
        if (data.status === 'finished') {
          apiService.syncTourShown(route).catch(e => {
            console.error('Failed to sync tour to server:', e);
          });
        }
      }}
      run={shouldRun && !runRef.current}
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
