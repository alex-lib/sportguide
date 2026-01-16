import { useEffect, useState, useCallback } from 'react';
import { apiService } from '../services/api.js';
import FilterPanel from '../components/FilterPanel.jsx';
import { SPORT_TYPES, SEX_OPTIONS } from '../constants/filters.js';
import '../App.css';

const Coaches = () => {
  const [coaches, setCoaches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState({
    sportTypes: [],
    age: null,
    sex: null,
    yearsOfExperience: null,
  });

  const loadCoaches = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const filterParams = {};
      if (filter.sportTypes && filter.sportTypes.length > 0) {
        filterParams.sportTypes = filter.sportTypes;
      }
      if (filter.age) filterParams.age = filter.age;
      if (filter.sex) filterParams.sex = filter.sex;
      if (filter.yearsOfExperience) filterParams.yearsOfExperience = filter.yearsOfExperience;

      const response = await apiService.getCoaches(filterParams);
      setCoaches(response?.list || []);
    } catch (error) {
      console.error('Failed to load coaches:', error);
      setError(error.message || 'Не удалось загрузить тренеров');
      setCoaches([]);
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => {
    loadCoaches();
  }, [loadCoaches]);

  const handleFilterChange = (key, value) => {
    setFilter((prev) => ({
      ...prev,
      [key]: value,
    }));
  };

  const handleResetFilters = () => {
    setFilter({
      sportTypes: [],
      age: null,
      sex: null,
      yearsOfExperience: null,
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
    {
      type: 'chip',
      key: 'sex',
      title: 'Пол',
      options: SEX_OPTIONS,
      value: filter.sex,
    },
    {
      type: 'number',
      key: 'age',
      title: 'Возраст',
      label: 'Возраст',
      value: filter.age,
      min: 18,
      max: 80,
    },
    {
      type: 'number',
      key: 'yearsOfExperience',
      title: 'Опыт работы',
      label: 'Лет опыта',
      value: filter.yearsOfExperience,
      min: 0,
      max: 50,
    },
  ];

  if (loading) {
    return (
      <div className="page-container">
        <h1 className="page-title">Тренеры</h1>
        <div className="empty-state">
          <div className="empty-state-icon">⏳</div>
          <p>Загрузка...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <h1 className="page-title">Тренеры</h1>

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

      {!error && coaches.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">👨‍🏫</div>
          <p>Тренеры не найдены</p>
          <p style={{ fontSize: '14px', marginTop: '8px', opacity: 0.7 }}>
            Попробуйте изменить фильтры
          </p>
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
                      border: '2px solid var(--border-color)',
                    }}
                  />
                )}
                <div style={{ flex: 1 }}>
                  <h3 className="card-title">{coach.name}</h3>
                  {coach.specialization && (
                    <p
                      style={{
                        fontSize: '14px',
                        color: 'var(--primary-color)',
                        marginBottom: '8px',
                        fontWeight: '500',
                      }}
                    >
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

