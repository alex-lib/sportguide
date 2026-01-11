import { useEffect, useState } from 'react';
import { apiService } from '../services/api.js';
import '../App.css';

const Events = () => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState({});

  useEffect(() => {
    loadEvents();
  }, [filter]);

  const loadEvents = async () => {
    try {
      setLoading(true);
      const response = await apiService.getEvents(filter);
      setEvents(response.list || []);
    } catch (error) {
      console.error('Failed to load events:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (date, time) => {
    try {
      const dateObj = new Date(date);
      return dateObj.toLocaleDateString('ru-RU', {
        day: 'numeric',
        month: 'long',
        year: 'numeric',
      }) + (time ? ` в ${time}` : '');
    } catch {
      return `${date} ${time}`;
    }
  };

  if (loading) {
    return (
      <div className="page-container">
        <h1 className="page-title">События</h1>
        <div className="empty-state">Загрузка...</div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <h1 className="page-title">События</h1>

      {events.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">📅</div>
          <p>События не найдены</p>
        </div>
      ) : (
        <div>
          {events.map((event, index) => (
            <div key={index} className="card">
              <h3 className="card-title">{event.name}</h3>
              {event.description && <p className="card-description">{event.description}</p>}
              
              <div className="card-meta">
                <span className="meta-item">📍 {event.placeName}</span>
                <span className="meta-item">🏘️ {event.district}</span>
                <span className="meta-item">📅 {formatDate(event.date, event.time)}</span>
              </div>

              {event.address && (
                <p style={{ marginTop: '8px', fontSize: '14px', color: 'var(--tg-theme-hint-color)' }}>
                  📍 {event.address}
                </p>
              )}

              {event.link && (
                <a
                  href={event.link}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="btn btn-primary btn-small"
                  style={{ marginTop: '12px' }}
                >
                  Подробнее →
                </a>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Events;

