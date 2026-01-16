import { useEffect, useState, useCallback } from 'react';
import { apiService } from '../services/api.js';
import FilterPanel from '../components/FilterPanel.jsx';
import { DISTRICTS, PLACE_TYPES, OUTDOOR_OPTIONS } from '../constants/filters.js';
import '../App.css';

const Places = () => {
  const [places, setPlaces] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState({
    district: null,
    subDistrict: null,
    outdoor: null,
    placeType: null,
  });

  const loadPlaces = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      // Build filter object, only including non-null values
      const filterParams = {};
      if (filter.district) filterParams.district = filter.district;
      if (filter.subDistrict) filterParams.subDistrict = filter.subDistrict;
      if (filter.outdoor) filterParams.outdoor = filter.outdoor;
      if (filter.placeType) filterParams.placeType = filter.placeType;

      const response = await apiService.getPlaces(filterParams);
      console.log('Places response:', response);
      setPlaces(response?.list || []);
    } catch (error) {
      console.error('Failed to load places:', error);
      setError(error.message || 'Не удалось загрузить места');
      setPlaces([]);
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => {
    loadPlaces();
  }, [loadPlaces]);

  const handleFilterChange = (key, value) => {
    setFilter((prev) => ({
      ...prev,
      [key]: value,
    }));
  };

  const handleResetFilters = () => {
    setFilter({
      district: null,
      subDistrict: null,
      outdoor: null,
      placeType: null,
    });
  };

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
      key: 'placeType',
      title: 'Тип места',
      options: PLACE_TYPES,
      value: filter.placeType,
    },
    {
      type: 'chip',
      key: 'outdoor',
      title: 'Расположение',
      options: OUTDOOR_OPTIONS,
      value: filter.outdoor,
    },
  ];

  if (loading) {
    return (
      <div className="page-container">
        <h1 className="page-title">Спортивные места</h1>
        <div className="empty-state">
          <div className="empty-state-icon">⏳</div>
          <p>Загрузка...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <h1 className="page-title">Спортивные места</h1>

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

      {!error && places.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">📍</div>
          <p>Места не найдены</p>
          <p style={{ fontSize: '14px', marginTop: '8px', opacity: 0.7 }}>
            Попробуйте изменить фильтры
          </p>
        </div>
      ) : (
        <div>
          {places.map((place, index) => (
            <div key={place.id || index} className="card">
              {place.photo && (
                <img
                  src={`data:image/jpeg;base64,${place.photo}`}
                  alt={place.name}
                  style={{
                    width: '100%',
                    height: '200px',
                    objectFit: 'cover',
                    borderRadius: '12px',
                    marginBottom: '16px',
                    border: '1px solid var(--border-color)',
                  }}
                />
              )}

              <h3 className="card-title">{place.name}</h3>
              {place.description && <p className="card-description">{place.description}</p>}

              <div className="card-meta">
                <span className="meta-item">🏘️ {place.district}</span>
                {place.subDistrict && <span className="meta-item">{place.subDistrict}</span>}
                <span className="meta-item">{place.placeType}</span>
                <span className="meta-item">
                  {place.outdoor === 'true' || place.outdoor === 'Улица 🏜'
                    ? '🌳 На улице'
                    : '🏢 В помещении'}
                </span>
              </div>

              {place.address && (
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
                  <span>{place.address}</span>
                </p>
              )}

              {place.webSite && (
                <a
                  href={place.webSite}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="btn btn-primary btn-small"
                  style={{ marginTop: '16px' }}
                >
                  Сайт →
                </a>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Places;

