import { useRef, useEffect, useState, useCallback } from 'react';
import { useLocation } from 'react-router-dom';
import Joyride from 'react-joyride';
import { apiService } from '../services/api.js';

export const TourContext = () => null;

export const TourProvider = ({ children }) => {
  return children;
};

export const TourPage = () => {
  const location = useLocation();
  const [steps, setSteps] = useState([]);
  const [skip, setSkip] = useState(false);
  const [stepCount, setStepCount] = useState(-1);
  const [runTour, setRunTour] = useState(false);
  const tourFinishedRef = useRef(false);

  const route = location.pathname === '/' ? '' : location.pathname.substring(1);

  useEffect(() => {
    setSkip(false);
    setSteps([]);
    setStepCount(-1);
    setRunTour(false);
    tourFinishedRef.current = false;
  }, [route]);

  useEffect(() => {
    let cancelled = false;

    apiService.isTourShown(route)
      .then((isShown) => {
        if (cancelled) return;
        if (isShown) {
          console.log('Tour: already shown for', route);
          setSkip(true);
          return;
        }
        return apiService.getTourSteps(route);
      })
      .then((data) => {
        if (cancelled) return;
        if (data && data.length > 0) {
          const formatted = data.map(step => ({
            target: step.target,
            content: step.content,
            placement: step.placement || 'bottom',
            primary: step.isPrimary || false,
          }));
          console.log('Tour: loaded', formatted.length, 'steps for', route);
          formatted.forEach((step, i) => {
            const el = document.querySelector(step.target);
            console.log(`Tour: step ${i} "${step.target}" found:`, !!el);
          });
          setSteps(formatted);
          setStepCount(formatted.length);
          setRunTour(true);
        } else {
          console.log('Tour: no steps for', route);
          setSkip(true);
          setStepCount(0);
        }
      })
      .catch((e) => {
        console.error('Tour error:', e);
        setSkip(true);
        setStepCount(0);
      });

    return () => { cancelled = true; };
  }, [route]);

  const callback = useCallback((data) => {
    console.log('Joyride:', data.status, 'index:', data.index);
    if (data.status === 'finished') {
      apiService.syncTourShown(route).catch((e) => {
        console.error('Tour: failed to sync:', e);
      });
      tourFinishedRef.current = true;
    }
    if (data.status === 'stalled') {
      const step = steps[data.index];
      console.warn('Stalled at step', data.index, step?.target);
      if (step) {
        const el = document.querySelector(step.target);
        console.warn('Element found:', !!el, el);
      }
    }
    if (data.status === 'error') {
      console.error('Tour error:', data);
    }
  }, [route, steps]);

  const shouldRun = runTour && stepCount > 0 && !tourFinishedRef.current && !skip;
  const joyrideKey = stepCount > 0 ? `${route}-${stepCount}` : `${route}-idle`;

  return (
    <Joyride
      key={joyrideKey}
      steps={shouldRun ? steps : []}
      callback={callback}
      run={shouldRun}
      continuous
      showSkipButton
      scrollToFirstStepDuringMount
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
