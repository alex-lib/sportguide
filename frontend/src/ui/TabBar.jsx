import { Link } from 'react-router-dom';
import Icon from './Icon.jsx';

/** TabBar - floating Liquid-Glass bottom navigation (max 5 items). */
const TabBar = ({ items, pathname }) => (
  <nav className="tabbar">
    {items.map((item) => (
      <Link
        key={item.path}
        to={item.path}
        className={`tab ${pathname === item.path ? 'active' : ''}`.trim()}
        data-tour={`nav-${item.path}`}
      >
        <span className="ti">
          <Icon name={item.icon} size={21} />
        </span>
        {item.label}
      </Link>
    ))}
  </nav>
);

export default TabBar;
