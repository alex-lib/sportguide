import { useEffect, useState, useCallback, useRef } from 'react';
import { apiService } from '../services/api.js';
import FilterPanel from '../components/FilterPanel.jsx';
import { SPORT_TYPES, SEX_OPTIONS } from '../constants/filters.js';
import {
  Page,
  PageHeader,
  // IconButton,
  Card,
  CardTitle,
  CardText,
  Icon,
  Pills,
  Pill,
  Button,
  CardActions,
  Divider,
  EmptyState,
  ErrorBanner,
  SkeletonList,
} from '../ui';
import { pluralRu } from '../utils/plural.js';

// Contact link for the card's single primary action, when data allows.
const coachContact = (coach) => {
  if (coach.phoneNumber) return `tel:${coach.phoneNumber}`;
  return coach.link || coach.telegram || coach.linkToChat || null;
};

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
            action={
              <Button variant="ghost" onClick={handleResetFilters}>
                Сбросить фильтры
              </Button>
            }
          />
        ) : (
          <div>
            {coaches.map((coach, index) => {
              const contact = coachContact(coach);
              return (
                <Card key={coach.id || index} first={index === 0}>
                  <div className="coach">
                    {coach.photo ? (
                      <img className="avatar-img" src={`data:image/jpeg;base64,${coach.photo}`} alt={coach.name} />
                    ) : (
                      <div className={`ava ${index % 2 === 1 ? 'accent' : ''}`.trim()}>
                        <Icon name="user" size={28} />
                      </div>
                    )}
                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <CardTitle style={{ margin: 0 }}>{coach.name}</CardTitle>
                        {coach.rating != null && (
                          <span className="rate">
                            <Icon name="star" size={13} />
                            {coach.rating}
                          </span>
                        )}
                      </div>
                      {coach.specialization && (
                        <p className="card-text" style={{ color: 'var(--brand)', fontWeight: 600, margin: '3px 0 0' }}>
                          {coach.specialization}
                        </p>
                      )}
                    </div>
                  </div>

                  {coach.description && <CardText style={{ marginTop: '12px' }}>{coach.description}</CardText>}

                  {(coach.yearsOfExperience || coach.district) && (
                    <Pills>
                      {coach.yearsOfExperience && <Pill icon="award">{coach.yearsOfExperience} {pluralRu(coach.yearsOfExperience, ['год', 'года', 'лет'])} опыта</Pill>}
                      {coach.district && <Pill icon="map-pin">{coach.district}</Pill>}
                    </Pills>
                  )}

                  {contact && (
                    <>
                      <Divider />
                      <CardActions>
                        <Button href={contact} target="_blank" rel="noopener noreferrer" fullWidth>
                          Связаться с тренером
                        </Button>
                      </CardActions>
                    </>
                  )}
                </Card>
              );
            })}
          </div>
        )}
      </Page>
    </>
  );
};

export default Coaches;
