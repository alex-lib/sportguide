import { useLocation } from 'react-router-dom';
import { TabBar } from '../ui';

// Five primary tabs (HIG: max 5). Programs is reachable from the Home grid.
const NAV_ITEMS = [
  { path: '/', label: 'Главная', icon: 'home' },
  { path: '/events', label: 'События', icon: 'calendar' },
  { path: '/places', label: 'Места', icon: 'map-pin' },
  { path: '/joint-trainings', label: 'Тренировки', icon: 'users' },
  { path: '/coaches', label: 'Тренеры', icon: 'graduation-cap' },
];

const Layout = ({ children }) => {
  const location = useLocation();
  return (
    <div className="app-shell">
      <main>{children}</main>
      <TabBar items={NAV_ITEMS} pathname={location.pathname} />
    </div>
  );
};

export default Layout;
