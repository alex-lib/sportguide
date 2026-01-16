import { useEffect, useState, useCallback } from 'react';
import { apiService } from '../services/api.js';
import FilterPanel from '../components/FilterPanel.jsx';
import { DISTRICTS } from '../constants/filters.js';
import '../App.css';

const Events = () => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState({
    district: null,
    date: null,
  });

  const loadEvents = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const filterParams = {};
      if (filter.district) filterParams.district = filter.district;
      if (filter.date) filterParams.date = filter.date;

      const response = await apiService.getEvents(filterParams);
      setEvents(response?.list || []);
    } catch (error) {
      console.error('Failed to load events:', error);
      setError(error.message || 'Не удалось загрузить события');
      setEvents([]);
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => {
    loadEvents();
  }, [loadEvents]);

  const handleFilterChange = (key, value) => {
    setFilter((prev) => ({
      ...prev,
      [key]: value,
    }));
  };

  const handleResetFilters = () => {
    setFilter({
      district: null,
      date: null,
    });
  };

  const today = new Date().toISOString().split('T')[0];
  const tomorrow = new Date(Date.now() + 86400000).toISOString().split('T')[0];

  const filterConfig = [
    {
      type: 'chip',
      key: 'district',
      title: 'Район',
      options: DISTRICTS,
      value: filter.district,
    },
    {
      type: 'chip',
      key: 'date',
      title: 'Дата',
      options: [
        { value: today, label: 'Сегодня' },
        { value: tomorrow, label: 'Завтра' },
      ],
      value: filter.date,
    },
  ];

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
        <div className="empty-state">
          <div className="empty-state-icon">⏳</div>
          <p>Загрузка...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <h1 className="page-title">События</h1>

      <FilterPanel
        filters={filterConfig}
        onFilterChange={handleFilterChange}
        onReset={handleResetFilters}
      />

      {error && (
        <div className="card" style={{ background: '#fef2f2', borderColor: 'var(--error-color)' }}>
          <p style={{ color: 'var(--error-color)', margin: 0 }}>⚠️ {error}</p>
        </div>
      )}

      {!error && events.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">📅</div>
          <p>События не найдены</p>
          <p style={{ fontSize: '14px', marginTop: '8px', opacity: 0.7 }}>
            Попробуйте изменить фильтры
          </p>
        </div>
      ) : (
        <div>
          {events.map((event, index) => (
            <div key={event.id || index} className="card">
              <h3 className="card-title">{event.name}</h3>
              {event.description && <p className="card-description">{event.description}</p>}

              <div className="card-meta">
                <span className="meta-item">📍 {event.placeName}</span>
                <span className="meta-item">🏘️ {event.district}</span>
                <span className="meta-item">📅 {formatDate(event.date, event.time)}</span>
              </div>

              {event.address && (
                <p
                  style={{
                    marginTop: '12px',
                    fontSize: '14px',
                    color: 'var(--text-secondary)',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '6px',
                  }}
                >
                  <span>📍</span>
                  <span>{event.address}</span>
                </p>
              )}

              {event.link && (
                <a
                  href={event.link}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="btn btn-primary btn-small"
                  style={{ marginTop: '16px' }}
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

