import { useEffect, useState } from 'react';
import { apiService } from '../services/api.js';
import '../App.css';

const TrainingPrograms = () => {
  const [programs, setPrograms] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadPrograms();
  }, []);

  const loadPrograms = async () => {
    try {
      setLoading(true);
      const response = await apiService.getTrainingPrograms();
      setPrograms(response.list || []);
    } catch (error) {
      console.error('Failed to load training programs:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="page-container">
        <h1 className="page-title">Программы тренировок</h1>
        <div className="empty-state">Загрузка...</div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <h1 className="page-title">Программы тренировок</h1>

      {programs.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">📋</div>
          <p>Программы не найдены</p>
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

