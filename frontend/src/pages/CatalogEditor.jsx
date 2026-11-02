import { useEffect, useRef, useState } from 'react';
import { Navigate, useNavigate, useParams } from 'react-router-dom';
import { apiService } from '../services/api.js';
import { DISTRICTS, SUBDISTRICTS, PLACE_TYPES, SPORT_TYPES, SEX_OPTIONS, OUTDOOR_OPTIONS } from '../constants/filters.js';
import { Page, PageHeader, Button, Field, Input, Textarea, Select, ErrorBanner, Loading } from '../ui';

const emptyPlace = { name: '', description: '', district: '', subdistrict: '', placeType: '', outdoor: 'true', address: '', webSite: '', coordinates: '' };
const emptyCoach = { name: '', description: '', subscriberUsername: '', sportTypes: [], age: '', sex: '', yearsOfExperience: 0, education: '', phoneNumber: '', workPlacesNames: [], trainingProgramsTitles: [], monthsForSubscriptionToBeCoach: 1, showInWeb: true };

const CatalogEditor = ({ catalog }) => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isPlace = catalog === 'places';
  const savedId = useRef(id);
  const [allowed, setAllowed] = useState(null);
  const [form, setForm] = useState(null);
  const [places, setPlaces] = useState([]);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [reload, setReload] = useState(0);
  const [photo, setPhoto] = useState(null);
  const [removePhoto, setRemovePhoto] = useState(false);
  const [preview, setPreview] = useState('');
  const [photoError, setPhotoError] = useState('');

  useEffect(() => {
    let active = true;
    savedId.current = id;
    setAllowed(null); setForm(null); setError(''); setPhoto(null); setRemovePhoto(false);
    (async () => {
      try {
        const permissions = await apiService.getCatalogPermissions(catalog);
        if (!active) return;
        if (!permissions.canManage) { setAllowed(false); return; }
        setAllowed(true);
        const [item, locations] = await Promise.all([
          id ? apiService.getCatalogItem(catalog, id) : Promise.resolve(isPlace ? emptyPlace : emptyCoach),
          isPlace ? Promise.resolve({ list: [] }) : apiService.getPlaces({}),
        ]);
        if (!active) return;
        setForm({ ...(isPlace ? emptyPlace : emptyCoach), ...item,
          ...(isPlace ? { outdoor: String(item.outdoor), subdistrict: item.subdistrict || '' } : {}),
        });
        setPlaces(locations.list || []);
      } catch (failure) {
        if (!active) return;
        if (failure.response?.status === 403) setAllowed(false);
        else setError(failure.response?.status === 404 ? 'Запись не найдена.' : 'Не удалось загрузить форму. Попробуйте ещё раз.');
      }
    })();
    return () => { active = false; };
  }, [catalog, id, isPlace, reload]);

  useEffect(() => {
    let active = true;
    let url;
    setPreview(''); setPhotoError('');
    if (removePhoto) return;
    (async () => {
      try {
        const blob = photo || (form?.photoUrl ? await apiService.getCatalogPhoto(catalog, form.photoUrl) : null);
        if (!active || !blob) return;
        url = URL.createObjectURL(blob);
        setPreview(url);
      } catch { if (active) setPhotoError('Не удалось загрузить текущее фото. Оно сохранится при редактировании.'); }
    })();
    return () => { active = false; if (url) URL.revokeObjectURL(url); };
  }, [catalog, form?.photoUrl, photo, removePhoto]);

  const change = (key) => (event) => setForm((current) => ({ ...current, [key]: event.target.value }));
  const toggle = (key, value) => setForm((current) => ({ ...current,
    [key]: current[key].includes(value) ? current[key].filter((item) => item !== value) : [...current[key], value],
  }));
  const save = async (event) => {
    event.preventDefault();
    if (saving) return;
    setSaving(true); setError('');
    let recordSaved = false;
    try {
      const data = isPlace ? {
        ...form, outdoor: form.outdoor === 'true', subdistrict: form.subdistrict || null,
        coordinates: form.coordinates?.trim() || null, photoUrl: undefined, photo: undefined,
      } : {
        ...form, age: Number(form.age), yearsOfExperience: Number(form.yearsOfExperience),
        monthsForSubscriptionToBeCoach: Number(form.monthsForSubscriptionToBeCoach), photoUrl: undefined,
      };
      const result = await apiService.saveCatalogItem(catalog, savedId.current, data, isPlace ? null : photo);
      savedId.current = savedId.current || result.id;
      recordSaved = true;
      if (isPlace && photo) await apiService.uploadCatalogPhoto(catalog, savedId.current, photo);
      if (removePhoto && form.photoUrl) await apiService.deleteCatalogPhoto(catalog, savedId.current);
      navigate(`/${catalog}`, { replace: true });
    } catch (failure) {
      const status = failure.response?.status;
      if (status === 403) { setAllowed(false); return; }
      const detail = failure.response?.data?.error;
      setError(recordSaved ? 'Данные сохранены, но фото не удалось обновить. Повторите сохранение — новая запись повторно не создастся.'
        : status === 404 ? 'Запись уже удалена.'
          : status === 400 ? (detail || 'Проверьте заполнение обязательных полей и формат данных.')
            : 'Не удалось сохранить. Введённые данные остались в форме — попробуйте ещё раз.');
    } finally { setSaving(false); }
  };

  if (allowed === false) return <Navigate to={`/${catalog}`} replace />;
  if (!error && !form) return <Loading />;
  const textField = (key, label, props = {}) => (
    <Field label={label}><Input aria-label={label} value={form[key] ?? ''} onChange={change(key)} {...props} /></Field>
  );
  const selectField = (key, label, options, required = true) => (
    <Field label={label}><Select aria-label={label} value={form[key] ?? ''} onChange={change(key)} required={required}>
      <option value="">{required ? 'Выберите' : 'Не указан'}</option>
      {options.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
    </Select></Field>
  );
  const workPlaces = [...new Set([...places.map((place) => place.name), ...(form?.workPlacesNames || [])])];
  return (
    <>
      <PageHeader eyebrow={id ? 'Редактирование' : 'Создание'} title={isPlace ? 'Место' : 'Тренер'} />
      <Page>
        {error && <div role="alert"><ErrorBanner>{error}</ErrorBanner></div>}
        {!form && <Button onClick={() => setReload((value) => value + 1)}>Повторить загрузку</Button>}
        {allowed && form && (
          <form onSubmit={save} className="catalog-editor">
            <fieldset disabled={saving}>
              {textField('name', isPlace ? 'Название' : 'Имя тренера', { required: true, maxLength: 255 })}
              <Field label="Описание"><Textarea aria-label="Описание" value={form.description || ''} onChange={change('description')} required={!isPlace} maxLength={10000} rows={5} /></Field>
              {isPlace ? <>
                {selectField('district', 'Район', DISTRICTS.filter((option) => option.value !== 'ALL_DISTRICTS'))}
                {selectField('subdistrict', 'Подрайон', SUBDISTRICTS.filter((option) => option.value !== 'ALL_SUBDISTRICTS'), false)}
                {selectField('placeType', 'Тип места', PLACE_TYPES)}
                {selectField('outdoor', 'Расположение', OUTDOOR_OPTIONS)}
                {textField('address', 'Адрес', { required: true, maxLength: 255 })}
                {textField('webSite', 'Сайт', { maxLength: 2000, placeholder: 'https://…' })}
                {textField('coordinates', 'Координаты', { maxLength: 100, placeholder: '51.65, 39.15' })}
              </> : <>
                {textField('subscriberUsername', 'Telegram тренера', { required: !form.subscriberId, readOnly: Boolean(form.subscriberId), placeholder: '@username' })}
                {!id && <p className="catalog-hint">Тренер должен сначала открыть бота SportGuide.</p>}
                <fieldset className="catalog-options"><legend>Виды спорта</legend>
                  {SPORT_TYPES.map((sport) => <label key={sport.value}><input type="checkbox" checked={form.sportTypes.includes(sport.label)} onChange={() => toggle('sportTypes', sport.label)} />{sport.label}</label>)}
                </fieldset>
                {textField('age', 'Возраст', { type: 'number', required: true, min: 18, max: 120 })}
                {selectField('sex', 'Пол', SEX_OPTIONS.map((option) => ({ value: option.label, label: option.label })))}
                {textField('yearsOfExperience', 'Опыт работы, лет', { type: 'number', required: true, min: 0, max: 100 })}
                {textField('education', 'Образование', { required: true, maxLength: 1000 })}
                {textField('phoneNumber', 'Телефон', { type: 'tel', required: true, placeholder: '+79991234567' })}
                <fieldset className="catalog-options"><legend>Места работы</legend>
                  {workPlaces.length ? workPlaces.map((name) => <label key={name}><input type="checkbox" checked={form.workPlacesNames.includes(name)} onChange={() => toggle('workPlacesNames', name)} />{name}</label>) : <p>Мест пока нет. Их можно добавить в разделе «Места».</p>}
                </fieldset>
                {textField('monthsForSubscriptionToBeCoach', 'Срок публикации, месяцев', { type: 'number', required: true, min: 1, max: 120 })}
                {id && <p className="catalog-hint">При изменении срока он отсчитывается от сегодняшнего дня.</p>}
                <label className="catalog-check"><input type="checkbox" checked={form.showInWeb} onChange={(event) => setForm((current) => ({ ...current, showInWeb: event.target.checked }))} />Показывать анкету в приложении</label>
              </>}
              <Field label="Фото"><Input aria-label="Фото" type="file" accept="image/jpeg,image/png,image/webp" onChange={(event) => { setPhoto(event.target.files?.[0] || null); setRemovePhoto(false); }} /></Field>
              {preview && <img className="catalog-photo-preview" src={preview} alt="Фото записи" />}
              {photoError && <p className="catalog-hint">{photoError}</p>}
              {form.photoUrl && <label className="catalog-check"><input type="checkbox" checked={removePhoto} onChange={(event) => { setRemovePhoto(event.target.checked); setPhoto(null); }} />Удалить текущее фото</label>}
              <Button type="submit" fullWidth disabled={saving || (!isPlace && form.sportTypes.length === 0)}>{saving ? 'Сохраняем…' : 'Сохранить'}</Button>
            </fieldset>
          </form>
        )}
        <Button variant="ghost" fullWidth disabled={saving} onClick={() => navigate(`/${catalog}`)}>Отмена</Button>
      </Page>
    </>
  );
};
export default CatalogEditor;
