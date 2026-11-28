import { useEffect, useState, useCallback, useRef } from 'react';
import CoachMiniCard from '../components/CoachMiniCard.jsx';
import './Coaches.css';
import { apiService } from '../services/api.js';
import { useCatalogPermissions } from '../components/CatalogAdmin.jsx';
import FilterPanel from '../components/FilterPanel.jsx';
import { SPORT_TYPES, SEX_OPTIONS } from '../constants/filters.js';
import {
  Page,
  PageHeader,
  Button,
  EmptyState,
  ErrorBanner,
  SkeletonList,
} from '../ui';
import { pluralRu } from '../utils/plural.js';

const Coaches = () => {
  const canManage = useCatalogPermissions('coaches');
  const [coaches, setCoaches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState({
    sportTypes: [],
    age: null,
    sex: null,
    yearsOfExperience: null,
  });
  const [searchText, setSearchText] = useState('');

  const latestRequest = useRef(0);

  const loadCoaches = useCallback(async () => {
    const requestId = ++latestRequest.current;
    setLoading(true);
    setError(null);
    try {
      const filterParams = {};
      if (searchText) filterParams.search = searchText;
      if (filter.sportTypes && filter.sportTypes.length > 0) filterParams.sportTypes = filter.sportTypes;
      if (filter.age) filterParams.age = filter.age;
      if (filter.sex) filterParams.sex = filter.sex;
      if (filter.yearsOfExperience) filterParams.yearsOfExperience = filter.yearsOfExperience;

      const response = await apiService.getCoaches(filterParams);
      if (requestId !== latestRequest.current) return;
      setCoaches(response?.list || []);
    } catch (error) {
      if (requestId !== latestRequest.current) return;
      console.error('Failed to load coaches:', error);
      setError(error.message || 'Не удалось загрузить тренеров');
    } finally {
      if (requestId === latestRequest.current) setLoading(false);
    }
  }, [filter, searchText]);

  useEffect(() => {
    loadCoaches();
  }, [loadCoaches]);

  const handleFilterChange = (key, value) => {
    setFilter((prev) => ({ ...prev, [key]: value }));
  };

  const handleResetFilters = () => {
    setFilter({ sportTypes: [], age: null, sex: null, yearsOfExperience: null });
  };

  const filterConfig = [
    { type: 'multiselect', key: 'sportTypes', title: 'Вид спорта', options: SPORT_TYPES, value: filter.sportTypes },
    { type: 'chip', key: 'sex', title: 'Пол', options: SEX_OPTIONS, value: filter.sex },
    { type: 'number', key: 'age', title: 'Возраст', value: filter.age, min: 18, max: 80 },
    { type: 'number', key: 'yearsOfExperience', title: 'Опыт работы', value: filter.yearsOfExperience, min: 0, max: 50 },
  ];

  const eyebrow =
    !loading && coaches.length > 0
      ? `${coaches.length} ${pluralRu(coaches.length, ['тренер', 'тренера', 'тренеров'])}`
      : undefined;

  return (
    <>
      <PageHeader
        eyebrow={eyebrow}
        title="Тренеры"
        // action={<IconButton icon="arrow-up-down" label="Сортировка" />}
      />
      <Page>
        {canManage && <Button to="/coaches/new" fullWidth className="catalog-create">Добавить тренера</Button>}
        <FilterPanel
          filters={filterConfig}
          onFilterChange={handleFilterChange}
          onReset={handleResetFilters}
          searchPlaceholder="Поиск тренеров"
          hideQuickChips
          search={searchText}
          onSearch={(e) => setSearchText(e.target.value)}
        />

        {error && <ErrorBanner>{error}</ErrorBanner>}

        {loading ? (
          <SkeletonList />
        ) : !error && coaches.length === 0 ? (
          <EmptyState
            icon="graduation-cap"
            title="Тренеров не найдено"
            message="По выбранным фильтрам никого нет."
            data-tour="coaches-empty"
            action={
              <Button variant="ghost" onClick={handleResetFilters} data-tour="coaches-reset-filters">
                Сбросить фильтры
              </Button>
            }
          />
        ) : (
          <div className="catalog-grid" data-tour="coaches-list">
            {coaches.map((coach) => (
              <CoachMiniCard key={coach.id} coach={coach} canManage={canManage} onDeleted={loadCoaches} />
            ))}
          </div>
        )}
      </Page>
    </>
  );
};

export default Coaches;
