import { useEffect, useState } from 'react';
import { apiService } from '../services/api.js';
import '../App.css';

const Coaches = () => {
  const [coaches, setCoaches] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadCoaches();
  }, []);

  const loadCoaches = async () => {
    try {
      setLoading(true);
      const response = await apiService.getCoaches();
      setCoaches(response.list || []);
    } catch (error) {
      console.error('Failed to load coaches:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="page-container">
        <h1 className="page-title">Тренеры</h1>
        <div className="empty-state">Загрузка...</div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <h1 className="page-title">Тренеры</h1>

      {coaches.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">👨‍🏫</div>
          <p>Тренеры не найдены</p>
        </div>
      ) : (
        <div>
          {coaches.map((coach) => (
            <div key={coach.id} className="card">
              <div style={{ display: 'flex', gap: '16px', alignItems: 'flex-start' }}>
                {coach.photo && (
                  <img
                    src={`data:image/jpeg;base64,${coach.photo}`}
                    alt={coach.name}
                    style={{
                      width: '80px',
                      height: '80px',
                      borderRadius: '50%',
                      objectFit: 'cover',
                      flexShrink: 0,
                    }}
                  />
                )}
                <div style={{ flex: 1 }}>
                  <h3 className="card-title">{coach.name}</h3>
                  {coach.specialization && (
                    <p style={{ fontSize: '14px', color: 'var(--tg-theme-hint-color)', marginBottom: '8px' }}>
                      {coach.specialization}
                    </p>
                  )}
                  {coach.description && <p className="card-description">{coach.description}</p>}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Coaches;

