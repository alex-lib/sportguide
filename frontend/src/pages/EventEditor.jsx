import { useEffect, useState } from 'react';
import { Navigate, useNavigate, useParams } from 'react-router-dom';
import { apiService } from '../services/api.js';
import { DISTRICTS } from '../constants/filters.js';
import { Page, PageHeader, Button, Field, FieldRow, Input, Textarea, Select, ErrorBanner, Loading } from '../ui';

const toForm = (event) => ({
  ...event,
  ...Object.fromEntries(['name', 'description', 'date', 'time', 'district', 'address', 'placeName', 'link', 'coordinates']
    .map((key) => [key, event[key] ?? ''])),
});

const EventEditor = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [allowed, setAllowed] = useState(null);
  const [form, setForm] = useState(null);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [conflict, setConflict] = useState(false);
  const [reload, setReload] = useState(0);

  useEffect(() => {
    let active = true;
    setError('');
    setForm(null);
    setAllowed(null);
    (async () => {
      try {
        const permissions = await apiService.getEventPermissions();
        if (!active) return;
        if (permissions.canEdit !== true) { setAllowed(false); return; }
        setAllowed(true);
        const event = await apiService.getEventForEditing(id);
        if (!active) return;
        setForm(toForm(event));
        setConflict(false);
      } catch (failure) {
        if (!active) return;
        if (failure.response?.status === 403) setAllowed(false);
        else setError(failure.response?.status === 404 ? 'Событие не найдено.' : 'Не удалось загрузить событие. Попробуйте ещё раз.');
      }
    })();
    return () => { active = false; };
  }, [id, reload]);

  const change = (key) => (event) => {
    setForm((current) => ({ ...current, [key]: event.target.value }));
    setSaved(false);
  };

  const save = async (event) => {
    event.preventDefault();
    if (saving || conflict) return;
    setSaving(true);
    setSaved(false);
    setError('');
    try {
      const updated = await apiService.updateEvent(id, { ...form, time: form.time || null });
      setForm(toForm(updated));
      setSaved(true);
    } catch (failure) {
      const status = failure.response?.status;
      if (status === 403) { setAllowed(false); return; }
      setConflict(status === 409);
      setError(status === 409
        ? 'Событие изменилось после открытия формы. Загрузите актуальные данные и повторите изменения.'
        : status === 404 ? 'Событие уже удалено.'
          : status === 400 ? 'Проверьте заполнение полей, ссылку и координаты.'
            : 'Не удалось сохранить изменения. Введённые данные остались в форме — попробуйте ещё раз.');
    } finally { setSaving(false); }
  };

  // Never render the form or request editable data until the server confirms the admin role.
  if (allowed === false) return <Navigate to="/events" replace />;
  if (!error && (allowed === null || !form)) return <Loading />;

  return (
    <>
      <PageHeader eyebrow="Редактирование" title="Событие" />
      <Page>
        {error && <div role="alert"><ErrorBanner>{error}</ErrorBanner></div>}
        {(!form || conflict) && (
          <Button type="button" variant="tint" disabled={saving} onClick={() => setReload((value) => value + 1)}>
            Загрузить актуальные данные
          </Button>
        )}
        {allowed === true && form && (
          <form onSubmit={save} className="event-editor">
            <Field label="Название"><Input aria-label="Название" value={form.name} onChange={change('name')} required maxLength={255} disabled={saving} /></Field>
            <Field label="Описание"><Textarea aria-label="Описание" value={form.description} onChange={change('description')} required maxLength={10000} rows={5} disabled={saving} /></Field>
            <FieldRow>
              <Field label="Дата"><Input aria-label="Дата" type="date" value={form.date} onChange={change('date')} required disabled={saving} /></Field>
              <Field label="Время (если известно)"><Input aria-label="Время" type="time" step="1" value={form.time} onChange={change('time')} disabled={saving} /></Field>
            </FieldRow>
            <Field label="Район"><Select aria-label="Район" value={form.district} onChange={change('district')} required disabled={saving}>
              <option value="" disabled>Выберите район</option>
              {DISTRICTS.filter((district) => district.value !== 'ALL_DISTRICTS').map((district) => (
                <option key={district.value} value={district.value}>{district.label}</option>
              ))}
            </Select></Field>
            <Field label="Адрес"><Input aria-label="Адрес" value={form.address} onChange={change('address')} required maxLength={255} disabled={saving} /></Field>
            <Field label="Место"><Input aria-label="Место" value={form.placeName} onChange={change('placeName')} required maxLength={255} disabled={saving} /></Field>
            <Field label="Ссылка"><Input aria-label="Ссылка" type="url" value={form.link} onChange={change('link')} maxLength={2000} placeholder="https://…" disabled={saving} /></Field>
            <Field label="Координаты (необязательно)"><Input aria-label="Координаты" value={form.coordinates} onChange={change('coordinates')} maxLength={100} placeholder="51.65, 39.15" disabled={saving} /></Field>
            <div role="status" aria-live="polite">{saved ? 'Изменения сохранены' : ''}</div>
            <Button type="submit" fullWidth disabled={saving || conflict}>{saving ? 'Сохраняем…' : 'Сохранить изменения'}</Button>
          </form>
        )}
        <Button type="button" variant="ghost" fullWidth disabled={saving} onClick={() => navigate('/events')}>К событиям</Button>
      </Page>
    </>
  );
};

export default EventEditor;
