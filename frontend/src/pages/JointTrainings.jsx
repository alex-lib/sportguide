import { useEffect, useState, useCallback, useRef } from 'react';
import { apiService } from '../services/api.js';
import FilterPanel from '../components/FilterPanel.jsx';
import { DISTRICTS, SPORT_TYPES } from '../constants/filters.js';
import { toLocalISODate } from '../utils/date.js';
import WebApp from '@twa-dev/sdk';
import {
  Page,
  PageHeader,
  // IconButton,
  Card,
  CardTitle,
  CardText,
  Thumb,
  Pills,
  Pill,
  MetaLine,
  Button,
  CardActions,
  Divider,
  Field,
  FieldRow,
  Input,
  Textarea,
  Select,
  Fab,
  Modal,
  Icon,
  EmptyState,
  ErrorBanner,
  SkeletonList,
  sportIconName,
} from '../ui';
import { pluralRu } from '../utils/plural.js';

const EMPTY_FORM = {
  title: '',
  description: '',
  date: '',
  time: '',
  sportType: '',
  placeName: '',
  district: '',
  address: '',
  phoneNumber: '',
};

const formatDateTime = (date, time) => {
  try {
    const d = new Date((date || '').split(' ')[0].replace(/-/g, '/'));
    const base = d.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' });
    return time ? `${base} · ${time}` : base;
  } catch {
    return `${date} ${time}`;
  }
};

const JointTrainings = () => {
  const [trainings, setTrainings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [filter, setFilter] = useState({ date: null, sportType: [], district: null });
  const [formData, setFormData] = useState(EMPTY_FORM);
  const [searchText, setSearchText] = useState('');

  const latestRequest = useRef(0);

  const loadTrainings = useCallback(async () => {
    const requestId = ++latestRequest.current;
    try {
      setLoading(true);
      setError(null);
      const filterParams = {};
      if (searchText) filterParams.search = searchText;
      if (filter.date) filterParams.date = filter.date;
      if (filter.sportType && filter.sportType.length > 0) filterParams.sportType = filter.sportType;
      if (filter.district) filterParams.district = filter.district;

      const response = await apiService.getJointTrainings(filterParams);
      if (requestId !== latestRequest.current) return;
      setTrainings(response?.list || []);
    } catch (error) {
      if (requestId !== latestRequest.current) return;
      console.error('Failed to load joint trainings:', error);
      setError(error.message || 'Не удалось загрузить тренировки');
      setTrainings([]);
    } finally {
      if (requestId === latestRequest.current) setLoading(false);
    }
  }, [filter, searchText]);

  useEffect(() => {
    loadTrainings();
  }, [loadTrainings]);

  const handleFilterChange = (key, value) => {
    setFilter((prev) => ({ ...prev, [key]: value }));
  };

  const handleResetFilters = () => {
    setFilter({ date: null, sportType: [], district: null });
  };

  const filterConfig = [
    { type: 'multiselect', key: 'sportType', title: 'Вид спорта', options: SPORT_TYPES, value: filter.sportType },
    { type: 'chip', key: 'district', title: 'Район', options: DISTRICTS, value: filter.district },
  ];

  const openCreate = () => {
    setFormData(EMPTY_FORM);
    setEditingId(null);
    setShowForm(true);
  };

  const closeForm = () => {
    setShowForm(false);
    setEditingId(null);
    setFormData(EMPTY_FORM);
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
      closeForm();
      loadTrainings();
    } catch (error) {
      console.error('Failed to save training:', error);
      WebApp.showAlert('Ошибка при сохранении');
    }
  };

  const handleDelete = (id) => {
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
    let formattedDate = training.date;
    let formattedTime = training.time;

    if (training.date) {
      if (typeof training.date === 'string' && training.date.includes('T')) {
        formattedDate = training.date.split('T')[0];
      } else if (typeof training.date === 'string') {
        formattedDate = training.date;
      } else {
        formattedDate = toLocalISODate(training.date);
      }
    }
    if (training.time && typeof training.time === 'string' && training.time.includes(':')) {
      formattedTime = training.time.substring(0, 5);
    }

    setFormData({
      title: training.title || '',
      description: training.description || '',
      date: formattedDate || '',
      time: formattedTime || '',
      sportType: training.sportType || '',
      placeName: training.placeName || '',
      district: training.district || '',
      address: training.address || '',
      phoneNumber: training.phoneNumber || '',
    });
    setEditingId(training.id || null);
    setShowForm(true);
  };

  const setField = (key) => (e) => setFormData((prev) => ({ ...prev, [key]: e.target.value }));

  const eyebrow =
    !loading && trainings.length > 0
      ? `${trainings.length} ${pluralRu(trainings.length, ['тренировка', 'тренировки', 'тренировок'])} ${
          pluralRu(trainings.length, ['ищет', 'ищут', 'ищут'])
        } участников`
      : undefined;

  return (
    <>
      <PageHeader
        eyebrow={eyebrow}
        title="Тренировки"
        // action={<IconButton icon="arrow-up-down" label="Сортировка" />}
      />
      <Page>
        <FilterPanel
          filters={filterConfig}
          onFilterChange={handleFilterChange}
          onReset={handleResetFilters}
          searchPlaceholder="Поиск тренировок"
          hideQuickChips
          search={searchText}
          onSearch={(e) => setSearchText(e.target.value)}
        />

        {error && <ErrorBanner>{error}</ErrorBanner>}

        {loading ? (
          <SkeletonList />
        ) : !error && trainings.length === 0 ? (
          <EmptyState
            icon="users"
            title="Пока нет тренировок"
            message="Создайте первую совместную тренировку или измените фильтры."
            action={<Button onClick={openCreate}>Создать тренировку</Button>}
          />
        ) : (
          <div>
            {trainings.map((training, index) => (
              <Card key={training.id || index} first={index === 0}>
                <div style={{ display: 'flex', alignItems: 'flex-start', gap: '12px' }}>
                  <Thumb icon={sportIconName(training.sportType)} size={22} />
                  <div style={{ flex: 1 }}>
                    <CardTitle>{training.title}</CardTitle>
                    {training.description && <CardText>{training.description}</CardText>}
                  </div>
                </div>

                <Pills>
                  <Pill tone="brand" icon="calendar">
                    {formatDateTime(training.date, training.time)}
                  </Pill>
                  {training.placeName && <Pill icon="map-pin">{training.placeName}</Pill>}
                  {training.district && <Pill>{training.district}</Pill>}
                </Pills>

                {training.address && <MetaLine>{training.address}</MetaLine>}

                <Divider />
                <CardActions>
                  {training.linkToChatWithCreator && (
                    <Button
                      href={training.linkToChatWithCreator}
                      target="_blank"
                      rel="noopener noreferrer"
                      size="sm"
                      style={{ flex: 1 }}
                    >
                      <Icon name="message-circle" size={15} />
                      Написать
                    </Button>
                  )}
                  {training.id && (
                    <>
                      <Button variant="tint" size="sm" aria-label="Редактировать" onClick={() => handleEdit(training)}>
                        <Icon name="pencil" size={15} />
                      </Button>
                      <Button variant="danger" size="sm" aria-label="Удалить" onClick={() => handleDelete(training.id)}>
                        <Icon name="trash-2" size={15} />
                      </Button>
                    </>
                  )}
                </CardActions>
              </Card>
            ))}
          </div>
        )}
      </Page>

      {!showForm && <Fab label="Создать тренировку" onClick={openCreate} />}

      <Modal
        open={showForm}
        title={editingId ? 'Редактировать' : 'Новая тренировка'}
        onCancel={closeForm}
        footer={
          <Button type="submit" form="jt-form" fullWidth>
            {editingId ? 'Сохранить' : 'Создать'}
          </Button>
        }
      >
        <form id="jt-form" onSubmit={handleSubmit}>
          <Field label="Название">
            <Input type="text" value={formData.title} onChange={setField('title')} required />
          </Field>
          <Field label="Описание">
            <Textarea value={formData.description} onChange={setField('description')} required />
          </Field>
          <FieldRow>
            <Field label="Дата">
              <Input type="date" value={formData.date} onChange={setField('date')} required />
            </Field>
            <Field label="Время">
              <Input type="time" value={formData.time} onChange={setField('time')} required />
            </Field>
          </FieldRow>
          <Field label="Вид спорта">
            <Select value={formData.sportType} onChange={setField('sportType')} required>
              <option value="">Выберите вид спорта</option>
              {SPORT_TYPES.map((sport) => (
                <option key={sport.value} value={sport.value}>
                  {sport.label}
                </option>
              ))}
            </Select>
          </Field>
          <Field label="Место">
            <Input type="text" value={formData.placeName} onChange={setField('placeName')} required />
          </Field>
          <Field label="Район">
            <Select value={formData.district} onChange={setField('district')} required>
              <option value="">Выберите район</option>
              {DISTRICTS.map((district) => (
                <option key={district.value} value={district.value}>
                  {district.label}
                </option>
              ))}
            </Select>
          </Field>
          <Field label="Адрес">
            <Input type="text" value={formData.address} onChange={setField('address')} required />
          </Field>
          <Field label="Телефон">
            <div style={{ display: 'flex', alignItems: 'center', width: '100%' }}>
              <span style={{ fontSize: 'var(--text-md)', color: 'var(--label-3)', padding: '0 10px 0 14px', border: '1px solid var(--fill-2)', borderRight: 'none', borderRadius: 'var(--radius-input) 0 0 var(--radius-input)', height: '48px', lineHeight: '48px', userSelect: 'none', background: 'var(--fill-2)', display: 'flex', alignItems: 'center' }}>
                +7
              </span>
              <Input
                type="tel"
                placeholder="XXXXXXXXXX"
                value={formData.phoneNumber}
                onChange={(e) => {
                  const input = e.target.value.replace(/\D/g, '');
                  if (input.length <= 10) {
                    setFormData(prev => ({ ...prev, phoneNumber: input }));
                  }
                }}
                style={{ borderLeft: 'none', borderRadius: '0 var(--radius-input) var(--radius-input) 0' }}
                required
              />
            </div>
          </Field>
        </form>
      </Modal>
    </>
  );
};

export default JointTrainings;
