import { useEffect, useState, useCallback } from 'react';
import { apiService } from '../services/api.js';
import FilterPanel from '../components/FilterPanel.jsx';
import { SPORT_TYPES } from '../constants/filters.js';
import '../App.css';

const TrainingPrograms = () => {
  const [programs, setPrograms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState({
    sportTypes: [],
  });

  const loadPrograms = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const filterParams = {};
      if (filter.sportTypes && filter.sportTypes.length > 0) {
        filterParams.sportTypes = filter.sportTypes;
      }

      const response = await apiService.getTrainingPrograms(filterParams);
      setPrograms(response?.list || []);
    } catch (error) {
      console.error('Failed to load training programs:', error);
      setError(error.message || 'Не удалось загрузить программы');
      setPrograms([]);
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => {
    loadPrograms();
  }, [loadPrograms]);

  const handleFilterChange = (key, value) => {
    setFilter((prev) => ({
      ...prev,
      [key]: value,
    }));
  };

  const handleResetFilters = () => {
    setFilter({
      sportTypes: [],
    });
  };

  const filterConfig = [
    {
      type: 'multiselect',
      key: 'sportTypes',
      title: 'Вид спорта',
      options: SPORT_TYPES,
      value: filter.sportTypes,
    },
  ];

  if (loading) {
    return (
      <div className="page-container">
        <h1 className="page-title">Программы тренировок</h1>
        <div className="empty-state">
          <div className="empty-state-icon">⏳</div>
          <p>Загрузка...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <h1 className="page-title">Программы тренировок</h1>

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

      {!error && programs.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">📋</div>
          <p>Программы не найдены</p>
          <p style={{ fontSize: '14px', marginTop: '8px', opacity: 0.7 }}>
            Попробуйте изменить фильтры
          </p>
        </div>
      ) : (
        <div>
          {programs.map((program) => (
            <div key={program.id} className="card">
              <h3 className="card-title">{program.name}</h3>
              {program.description && <p className="card-description">{program.description}</p>}

              <div className="card-meta">
                {program.duration && <span className="meta-item">⏱️ {program.duration}</span>}
                {program.difficulty && <span className="meta-item">💪 {program.difficulty}</span>}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default TrainingPrograms;

