import { useEffect, useState } from 'react';
import { apiService } from '../services/api.js';
import '../App.css';

const Places = () => {
  const [places, setPlaces] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState({});

  useEffect(() => {
    loadPlaces();
  }, [filter]);

  const loadPlaces = async () => {
    try {
      setLoading(true);
      const response = await apiService.getPlaces(filter);
      setPlaces(response.list || []);
    } catch (error) {
      console.error('Failed to load places:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="page-container">
        <h1 className="page-title">Места</h1>
        <div className="empty-state">Загрузка...</div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <h1 className="page-title">Спортивные места</h1>

      {places.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">📍</div>
          <p>Места не найдены</p>
        </div>
      ) : (
        <div>
          {places.map((place, index) => (
            <div key={index} className="card">
              {place.photo && (
                <img
                  src={`data:image/jpeg;base64,${place.photo}`}
                  alt={place.name}
                  style={{
                    width: '100%',
                    height: '200px',
                    objectFit: 'cover',
                    borderRadius: '8px',
                    marginBottom: '12px',
                  }}
                />
              )}
              
              <h3 className="card-title">{place.name}</h3>
              {place.description && <p className="card-description">{place.description}</p>}
              
              <div className="card-meta">
                <span className="meta-item">🏘️ {place.district}</span>
                {place.subDistrict && <span className="meta-item">{place.subDistrict}</span>}
                <span className="meta-item">{place.placeType}</span>
                <span className="meta-item">{place.outdoor === 'true' ? '🌳 На улице' : '🏢 В помещении'}</span>
              </div>

              {place.address && (
                <p style={{ marginTop: '8px', fontSize: '14px', color: 'var(--tg-theme-hint-color)' }}>
                  📍 {place.address}
                </p>
              )}

              {place.webSite && (
                <a
                  href={place.webSite}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="btn btn-primary btn-small"
                  style={{ marginTop: '12px' }}
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

