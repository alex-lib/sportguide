import { createContext, useContext, useState, useCallback, useEffect } from 'react';

const VISITED_KEY = 'sportguide_visited_pages';

function getVisited() {
  try {
    const raw = localStorage.getItem(VISITED_KEY);
    return raw ? new Set(JSON.parse(raw)) : new Set();
  } catch {
    return new Set();
  }
}

const PageVisitContext = createContext(null);

export const PageVisitProvider = ({ children }) => {
  const [visited, setVisited] = useState(() => getVisited());

  const visit = useCallback((page) => {
    setVisited((prev) => {
      const next = new Set(prev);
      next.add(page);
      localStorage.setItem(VISITED_KEY, JSON.stringify([...next]));
      return next;
    });
  }, []);

  const isVisited = useCallback((page) => visited.has(page), [visited]);

  const reset = useCallback(() => {
    localStorage.removeItem(VISITED_KEY);
    setVisited(new Set());
  }, []);

  return (
    <PageVisitContext.Provider value={{ visited, visit, isVisited, reset }}>
      {children}
    </PageVisitContext.Provider>
  );
};

export const usePageVisit = () => useContext(PageVisitContext);
