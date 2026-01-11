import { useEffect, useState } from 'react';
import { apiService } from '../services/api.js';
import '../App.css';
import WebApp from '@twa-dev/sdk';

const JointTrainings = () => {
  const [trainings, setTrainings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    date: '',
    time: '',
    sportType: '',
    placeName: '',
    district: '',
    address: '',
    phoneNumber: '',
  });

  useEffect(() => {
    loadTrainings();
  }, []);

  const loadTrainings = async () => {
    try {
      setLoading(true);
      const response = await apiService.getJointTrainings();
      setTrainings(response.list || []);
    } catch (error) {
      console.error('Failed to load joint trainings:', error);
      WebApp.showAlert('Не удалось загрузить тренировки');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingId) {
        await apiService.updateJointTraining(editingId, formData);
        WebApp.showAlert('Тренировка обновлена');
      } else {
        await apiService.createJointTraining(formData);
        WebApp.showAlert('Тренировка создана');
      }
      setShowForm(false);
      setEditingId(null);
      resetForm();
      loadTrainings();
    } catch (error) {
      console.error('Failed to save training:', error);
      WebApp.showAlert('Ошибка при сохранении');
    }
  };

  const handleDelete = async (id) => {
    WebApp.showConfirm('Удалить эту тренировку?', async (confirmed) => {
      if (!confirmed) return;
      
      try {
        await apiService.deleteJointTraining(id);
        WebApp.showAlert('Тренировка удалена');
        loadTrainings();
      } catch (error) {
        console.error('Failed to delete training:', error);
        WebApp.showAlert('Ошибка при удалении');
      }
    });
  };

  const handleEdit = (training) => {
    setFormData({
      title: training.title,
      description: training.description,
      date: training.date,
      time: training.time,
      sportType: training.sportType,
      placeName: training.placeName,
      district: training.district,
      address: training.address,
      phoneNumber: training.phoneNumber,
    });
    setEditingId(training.id || null);
    setShowForm(true);
  };

  const resetForm = () => {
    setFormData({
      title: '',
      description: '',
      date: '',
      time: '',
      sportType: '',
      placeName: '',
      district: '',
      address: '',
      phoneNumber: '',
    });
  };

  const formatDateTime = (date, time) => {
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
        <h1 className="page-title">Совместные тренировки</h1>
        <div className="empty-state">Загрузка...</div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
        <h1 className="page-title" style={{ marginBottom: 0 }}>Совместные тренировки</h1>
        <button
          className="btn btn-primary btn-small"
          onClick={() => {
            resetForm();
            setEditingId(null);
            setShowForm(true);
          }}
        >
          + Создать
        </button>
      </div>

      {showForm && (
        <div className="card" style={{ marginBottom: '16px' }}>
          <h3 className="card-title">{editingId ? 'Редактировать' : 'Создать тренировку'}</h3>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Название</label>
              <input
                type="text"
                className="form-input"
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Описание</label>
              <textarea
                className="form-textarea"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Дата</label>
              <input
                type="date"
                className="form-input"
                value={formData.date}
                onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Время</label>
              <input
                type="time"
                className="form-input"
                value={formData.time}
                onChange={(e) => setFormData({ ...formData, time: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Вид спорта</label>
              <input
                type="text"
                className="form-input"
                value={formData.sportType}
                onChange={(e) => setFormData({ ...formData, sportType: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Место</label>
              <input
                type="text"
                className="form-input"
                value={formData.placeName}
                onChange={(e) => setFormData({ ...formData, placeName: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Район</label>
              <input
                type="text"
                className="form-input"
                value={formData.district}
                onChange={(e) => setFormData({ ...formData, district: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Адрес</label>
              <input
                type="text"
                className="form-input"
                value={formData.address}
                onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Телефон</label>
              <input
                type="tel"
                className="form-input"
                value={formData.phoneNumber}
                onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                required
              />
            </div>
            <div style={{ display: 'flex', gap: '8px' }}>
              <button type="submit" className="btn btn-primary btn-full">
                {editingId ? 'Сохранить' : 'Создать'}
              </button>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => {
                  setShowForm(false);
                  resetForm();
                  setEditingId(null);
                }}
              >
                Отмена
              </button>
            </div>
          </form>
        </div>
      )}

      {trainings.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">👥</div>
          <p>Тренировки не найдены</p>
        </div>
      ) : (
        <div>
          {trainings.map((training, index) => (
            <div key={index} className="card">
              <h3 className="card-title">{training.title}</h3>
              <p className="card-description">{training.description}</p>
              
              <div className="card-meta">
                <span className="meta-item">🏃 {training.sportType}</span>
                <span className="meta-item">📍 {training.placeName}</span>
                <span className="meta-item">🏘️ {training.district}</span>
                <span className="meta-item">📅 {formatDateTime(training.date, training.time)}</span>
              </div>

              {training.address && (
                <p style={{ marginTop: '8px', fontSize: '14px', color: 'var(--tg-theme-hint-color)' }}>
                  📍 {training.address}
                </p>
              )}

              <div style={{ marginTop: '12px', display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                {training.linkToChatWithCreator && (
                  <a
                    href={training.linkToChatWithCreator}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="btn btn-primary btn-small"
                  >
                    Написать организатору
                  </a>
                )}
                {training.id && (
                  <>
                    <button
                      className="btn btn-secondary btn-small"
                      onClick={() => handleEdit(training)}
                    >
                      Редактировать
                    </button>
                    <button
                      className="btn btn-danger btn-small"
                      onClick={() => training.id && handleDelete(training.id)}
                    >
                      Удалить
                    </button>
                  </>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default JointTrainings;

