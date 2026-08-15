import { useEffect, useState, useCallback, useRef } from 'react';
import { apiService } from '../services/api.js';
import FilterPanel from '../components/FilterPanel.jsx';
import { DISTRICTS } from '../constants/filters.js';
import { toLocalISODate } from '../utils/date.js';
import {
  Page,
  PageHeader,
  // IconButton,
  Card,
  CardRow,
  CardTitle,
  CardText,
  CardActions,
  Divider,
  Pills,
  Pill,
  MetaLine,
  DateBadge,
  Button,
  EmptyState,
  ErrorBanner,
  SkeletonList,
} from '../ui';
import { pluralRu } from '../utils/plural.js';

const Events = () => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState({
    district: null,
    date: null,
  });

  const latestRequest = useRef(0);

  const loadEvents = useCallback(async () => {
    const requestId = ++latestRequest.current;
    try {
      setLoading(true);
      setError(null);
      const filterParams = {};
      if (filter.district) filterParams.district = filter.district;
      if (filter.date) filterParams.date = filter.date;

      const response = await apiService.getEvents(filterParams);
      if (requestId !== latestRequest.current) return;
      setEvents(response?.list || []);
    } catch (error) {
      if (requestId !== latestRequest.current) return;
      console.error('Failed to load events:', error);
      setError(error.message || 'Не удалось загрузить события');
      setEvents([]);
    } finally {
      if (requestId === latestRequest.current) setLoading(false);
    }
  }, [filter]);

  useEffect(() => {
    loadEvents();
  }, [loadEvents]);

  const handleFilterChange = (key, value) => {
    setFilter((prev) => ({ ...prev, [key]: value }));
  };

  const handleResetFilters = () => {
    setFilter({ district: null, date: null });
  };

  const dateOptions = [
    { value: toLocalISODate(new Date()), label: 'Сегодня' },
    { value: toLocalISODate(new Date(Date.now() + 86400000)), label: 'Завтра' },
  ];

  const filterConfig = [
    { type: 'chip', key: 'date', title: 'Дата', options: dateOptions, value: filter.date },
    { type: 'chip', key: 'district', title: 'Район', options: DISTRICTS || [], value: filter.district },
  ];

  const eyebrow =
    !loading && events.length > 0
      ? `${events.length} ${pluralRu(events.length, ['событие', 'события', 'событий'])} рядом`
      : undefined;

  return (
    <>
      <PageHeader
        eyebrow={eyebrow}
        title="События"
        // action={<IconButton icon="arrow-up-down" label="Сортировка" />}
      />
      <Page>
        <FilterPanel
          filters={filterConfig}
          onFilterChange={handleFilterChange}
          onReset={handleResetFilters}
          searchPlaceholder="Поиск событий"
        />

        {error && <ErrorBanner>{error}</ErrorBanner>}

        {loading ? (
          <SkeletonList />
        ) : !error && events.length === 0 ? (
          <EmptyState
            icon="calendar"
            title="Пока ничего нет"
            message="По выбранным фильтрам событий не нашлось."
            action={
              <Button variant="ghost" onClick={handleResetFilters}>
                Сбросить фильтры
              </Button>
            }
          />
        ) : (
          <div>
            {events.map((event, index) => (
              <Card key={event.id || index} first={index === 0}>
                <CardRow>
                  <DateBadge date={event.date} />
                  <div style={{ flex: 1 }}>
                    <CardTitle>{event.name}</CardTitle>
                    {event.description && <CardText>{event.description}</CardText>}
                  </div>
                </CardRow>

                <Pills>
                  {event.time && (
                    <Pill tone="brand" icon="clock">
                      {event.time}
                    </Pill>
                  )}
                  {event.placeName && <Pill icon="map-pin">{event.placeName}</Pill>}
                  {event.district && <Pill>{event.district}</Pill>}
                </Pills>

                {event.address && <MetaLine>{event.address}</MetaLine>}

                {event.link && (
                  <>
                    <Divider />
                    <CardActions>
                      <Button href={event.link} target="_blank" rel="noopener noreferrer" size="sm" fullWidth>
                        Подробнее
                      </Button>
                    </CardActions>
                  </>
                )}
              </Card>
            ))}
          </div>
        )}
      </Page>
    </>
  );
};

export default Events;
