import { Link, useLocation } from 'react-router-dom';
import './Layout.css';

const Layout = ({ children }) => {
  const location = useLocation();

  const navItems = [
    { path: '/', label: 'Главная', icon: '🏠' },
    { path: '/events', label: 'События', icon: '📅' },
    { path: '/places', label: 'Места', icon: '📍' },
    { path: '/joint-trainings', label: 'Тренировки', icon: '👥' },
    { path: '/training-programs', label: 'Программы', icon: '📋' },
    { path: '/coaches', label: 'Тренеры', icon: '👨‍🏫' },
  ];

  return (
    <div className="layout">
      <main className="main-content">{children}</main>
      <nav className="bottom-nav">
        {navItems.map((item) => (
          <Link
            key={item.path}
            to={item.path}
            className={`nav-item ${location.pathname === item.path ? 'active' : ''}`}
          >
            <span className="nav-icon">{item.icon}</span>
            <span className="nav-label">{item.label}</span>
          </Link>
        ))}
      </nav>
    </div>
  );
};

export default Layout;

