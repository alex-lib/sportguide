import { Link } from 'react-router-dom';
import '../App.css';

const Home = () => {
  const quickActions = [
    { path: '/events', icon: '📅', title: 'События', description: 'Ближайшие спортивные события' },
    { path: '/places', icon: '📍', title: 'Места', description: 'Спортивные площадки и залы' },
    { path: '/joint-trainings', icon: '👥', title: 'Совместные тренировки', description: 'Найдите партнеров для тренировок' },
    { path: '/training-programs', icon: '📋', title: 'Программы тренировок', description: 'Готовые программы для занятий' },
    { path: '/coaches', icon: '👨‍🏫', title: 'Тренеры', description: 'Профессиональные инструкторы' },
  ];

  return (
    <div className="page-container">
      <h1 className="page-title">SportGuide</h1>
      <p className="page-subtitle">Ваш помощник в мире спорта</p>

      <div className="grid">
        {quickActions.map((action) => (
          <Link key={action.path} to={action.path} className="card" style={{ textDecoration: 'none', display: 'block' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
              <span style={{ fontSize: '32px' }}>{action.icon}</span>
              <div style={{ flex: 1 }}>
                <h3 className="card-title">{action.title}</h3>
                <p className="card-description">{action.description}</p>
              </div>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
};

export default Home;

