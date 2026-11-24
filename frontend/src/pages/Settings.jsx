import { useEffect, useState } from 'react';
import { useTheme } from '../context/ThemeContext';
import { apiService } from '../services/api';
import { Page, PageHeader, Icon, Button, ErrorBanner } from '../ui';

const NOTIFICATIONS = [
  { key: 'weather', title: 'Погода', description: 'Утренний прогноз для планирования тренировок', icon: 'sun' },
  { key: 'events', title: 'Новые события', description: 'Спортивные события, которые появились в приложении', icon: 'calendar' },
  { key: 'places', title: 'Новые места', description: 'Новые площадки и залы для занятий', icon: 'map-pin' },
  { key: 'coaches', title: 'Новые тренеры', description: 'Новые анкеты тренеров и инструкторов', icon: 'graduation-cap' },
  { key: 'jointTrainings', title: 'Совместные тренировки', description: 'Новые тренировки с другими участниками после одобрения', icon: 'users' },
];

function ToggleRow({ title, description, icon, checked, disabled, onChange }) {
  return (
    <button type="button" role="switch" aria-checked={checked} aria-label={title}
      className="settings-row" disabled={disabled} onClick={onChange}>
      <span className="settings-icon"><Icon name={icon} size={20} /></span>
      <span className="settings-copy">
        <span className="settings-title">{title}</span>
        <span className="settings-description">{description}</span>
      </span>
      <span className="settings-switch" aria-hidden="true"><span /></span>
    </button>
  );
}

export default function Settings() {
  const { mode, setMode } = useTheme();
  const [preferences, setPreferences] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [status, setStatus] = useState('');
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');
    apiService.getNotificationPreferences()
      .then(data => { if (active) setPreferences(data); })
      .catch(() => { if (active) setError('Не удалось загрузить настройки уведомлений. Попробуйте ещё раз.'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [attempt]);

  const changePreference = async (key) => {
    if (saving || !preferences) return;
    const previous = preferences;
    const next = { ...previous, [key]: !previous[key] };
    setPreferences(next);
    setSaving(true);
    setError('');
    setStatus('Сохраняем…');
    try {
      const saved = await apiService.updateNotificationPreferences(next);
      setPreferences(saved);
      setStatus('Настройки сохранены');
    } catch {
      setPreferences(previous);
      setError('Не удалось сохранить изменение. Попробуйте ещё раз.');
      setStatus('');
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <PageHeader eyebrow="Под себя" title="Настройки" />
      <Page className="settings-page">
        <section aria-labelledby="appearance-heading">
          <h2 className="settings-section-title" id="appearance-heading">Оформление</h2>
          <p className="settings-intro">Системная тема следует оформлению Telegram или устройства, в том числе его автоматическому расписанию.</p>
          <div className="settings-card" role="group" aria-label="Тема оформления">
            {[
              { value: 'system', title: 'Системная', description: 'Меняется автоматически вместе с Telegram или устройством', icon: 'settings' },
              { value: 'scheduled', title: 'По времени', description: 'Светлая 07:00–19:00, тёмная 19:00–07:00 по времени устройства', icon: 'clock' },
              { value: 'light', title: 'Светлая', description: 'Всегда светлое оформление', icon: 'sun' },
              { value: 'dark', title: 'Тёмная', description: 'Всегда тёмное оформление', icon: 'moon' },
            ].map(option => (
              <label className="settings-row settings-theme-option" key={option.value}>
                <span className="settings-icon"><Icon name={option.icon} size={20} /></span>
                <span className="settings-copy">
                  <span className="settings-title">{option.title}</span>
                  <span className="settings-description">{option.description}</span>
                </span>
                <input type="radio" name="theme" value={option.value} checked={mode === option.value}
                  aria-label={option.title} onChange={() => setMode(option.value)} />
              </label>
            ))}
          </div>
        </section>

        <section aria-labelledby="notifications-heading" aria-busy={loading || saving}>
          <h2 className="settings-section-title" id="notifications-heading">Уведомления в Telegram</h2>
          <p className="settings-intro">Выберите, о чём вам рассказывать.</p>
          {loading ? <p className="settings-note" role="status">Загружаем настройки…</p> : preferences && (
            <div className="settings-card">
              {NOTIFICATIONS.map(item => <ToggleRow key={item.key} {...item}
                checked={Boolean(preferences[item.key])} disabled={saving}
                onChange={() => changePreference(item.key)} />)}
            </div>
          )}
          {error && <div className="settings-error" role="alert"><ErrorBanner>{error}</ErrorBanner>
            {!preferences && <Button variant="ghost" onClick={() => setAttempt(value => value + 1)}>Повторить</Button>}
          </div>}
          <p className="settings-save-status" role="status" aria-live="polite">{status}</p>
        </section>
      </Page>
    </>
  );
}
