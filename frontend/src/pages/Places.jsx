import { useEffect, useState, useCallback, useRef } from 'react';
import { apiService } from '../services/api.js';
import { CatalogActions, useCatalogPermissions } from '../components/CatalogAdmin.jsx';
import CatalogPhoto from '../components/CatalogPhoto.jsx';
import FilterPanel from '../components/FilterPanel.jsx';
import { DISTRICTS, SUBDISTRICTS, PLACE_TYPES, OUTDOOR_OPTIONS } from '../constants/filters.js';
import {
  Page,
  PageHeader,
  IconButton,
  Card,
  CardTitle,
  CardText,
  PhotoPlaceholder,
  Pill,
  MetaLine,
  Button,
  CardActions,
  Divider,
  EmptyState,
  ErrorBanner,
  SkeletonList,
  MapView,
} from '../ui';

const isOutdoor = (place) => place.outdoor === 'true' || /улиц/i.test(place.outdoor || '');

const Places = () => {
  const canManage = useCatalogPermissions('places');
  const [places, setPlaces] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [mapOpen, setMapOpen] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [mapPlaces, setMapPlaces] = useState([]);
  const [mapLoading, setMapLoading] = useState(false);
  const [mapError, setMapError] = useState(null);
  const [mapAttempt, setMapAttempt] = useState(0);
  const [filter, setFilter] = useState({
    district: null,
    subDistrict: null,
    outdoor: null,
    placeType: null,
  });
  const [searchText, setSearchText] = useState('');

  const latestRequest = useRef(0);

  const getFilterParams = useCallback(() => {
    const params = {};
    if (searchText) params.search = searchText;
    for (const [key, value] of Object.entries(filter)) {
      if (value) params[key] = value;
    }
    return params;
  }, [filter, searchText]);

  const loadPlaces = useCallback(async () => {
    const requestId = ++latestRequest.current;
    setLoading(true);
    setError(null);
    try {
      const response = await apiService.getPlaces({ ...getFilterParams(), page, size: 20 });
      if (requestId !== latestRequest.current) return;
      const pages = response?.totalPages ?? 0;
      if (page > 0 && page >= pages) {
        setPage(Math.max(0, pages - 1));
        return;
      }
      const items = response?.list || [];
      setPlaces(items);
      setTotalPages(pages);
    } catch (error) {
      if (requestId !== latestRequest.current) return;
      console.error('Failed to load places:', error);
      setError(error.message || 'Не удалось загрузить места');
    } finally {
      if (requestId === latestRequest.current) setLoading(false);
    }
  }, [getFilterParams, page]);

  useEffect(() => {
    loadPlaces();
    return () => { latestRequest.current += 1; };
  }, [loadPlaces]);

  useEffect(() => {
    if (!mapOpen) return;
    let active = true;
    setMapLoading(true);
    setMapError(null);
    setMapPlaces([]);
    // Fetch all matching places only when opening the map, without loading their photos.
    apiService.getPlaces(getFilterParams())
      .then(response => { if (active) setMapPlaces(response?.list || []); })
      .catch(error => { if (active) setMapError(error.message || 'Не удалось загрузить карту'); })
      .finally(() => { if (active) setMapLoading(false); });
    return () => { active = false; };
  }, [mapOpen, getFilterParams, mapAttempt]);

  const handleFilterChange = (key, value) => {
    setPage(0);
    setFilter((prev) => ({ ...prev, [key]: value }));
  };

  const handleResetFilters = () => {
    setPage(0);
    setFilter({ district: null, subDistrict: null, outdoor: null, placeType: null });
  };

  const filterConfig = [
    { type: 'chip', key: 'placeType', title: 'Тип места', options: PLACE_TYPES, value: filter.placeType },
    { type: 'chip', key: 'district', title: 'Район', options: DISTRICTS, value: filter.district },
    { type: 'chip', key: 'subDistrict', title: 'Подрайон', options: SUBDISTRICTS, value: filter.subDistrict },
    { type: 'chip', key: 'outdoor', title: 'Расположение', options: OUTDOOR_OPTIONS, value: filter.outdoor },
  ];

  return (
    <>
      <PageHeader
        title="Места"
        action={<IconButton icon="map" label="Карта" onClick={() => setMapOpen(true)} data-tour="places-map-btn" />}
      />
      <Page>
        {canManage && <Button to="/places/new" fullWidth className="catalog-create">Добавить место</Button>}
        <FilterPanel
          filters={filterConfig}
          onFilterChange={handleFilterChange}
          onReset={handleResetFilters}
          searchPlaceholder="Поиск мест"
          hideQuickChips
          search={searchText}
          onSearch={(e) => { setPage(0); setSearchText(e.target.value); }}
        />

        {error && <><ErrorBanner>{error}</ErrorBanner><Button variant="ghost" onClick={loadPlaces}>Повторить</Button></>}

        {loading ? (
          <SkeletonList />
        ) : !error && places.length === 0 ? (
          <EmptyState
            icon="map-pin"
            accent
            title="Мест не найдено"
            message="По выбранным фильтрам ничего нет."
            data-tour="places-empty"
            action={
              <Button variant="ghost" onClick={handleResetFilters} data-tour="places-reset-filters">
                Сбросить фильтры
              </Button>
            }
          />
        ) : !error && (
          <div className="catalog-grid" data-tour="places-list">
            {places.map((place, index) => (
              <Card key={place.id || index} first={index === 0} data-tour="place-card">
                <CatalogPhoto catalog="places" photoUrl={place.photoUrl} className="card-photo" alt={place.name} fallback={<PhotoPlaceholder icon="building-2" />} />

                <div style={{ display: 'flex', alignItems: 'flex-start', gap: '8px' }}>
                  <div style={{ flex: 1 }}>
                    {place.placeType && <p className="place-category">{place.placeType}</p>}
                    <CardTitle>{place.name}</CardTitle>
                  </div>
                  <Pill tone="brand" icon={isOutdoor(place) ? 'land-plot' : 'building'}>
                    {isOutdoor(place) ? 'Улица' : 'Зал'}
                  </Pill>
                </div>

                {(place.address || place.district) && (
                  <MetaLine>{[place.address, place.district].filter(Boolean).join(' · ')}</MetaLine>
                )}

                {place.description && <CardText className="place-summary">{place.description}</CardText>}

                <Divider />
                <CardActions>
                  <Button href={place.coordinates} target="_blank" rel="noopener noreferrer" size="sm" fullWidth data-tour="place-map">
                    На карте
                  </Button>
                  {place.webSite && place.webSite !== '-' && (
                    <Button href={place.webSite} target="_blank" rel="noopener noreferrer" variant="tint" size="sm" fullWidth data-tour="place-website">
                      Сайт
                    </Button>
                  )}
                </CardActions>
                {canManage && <CatalogActions catalog="places" item={place} onDeleted={loadPlaces} />}
              </Card>
            ))}
          </div>
        )}
        {totalPages > 1 && (
          <nav className="catalog-pagination" aria-label="Страницы мест">
            <Button variant="tint" size="sm" disabled={loading || page === 0}
              onClick={() => { setPage(value => value - 1); window.scrollTo(0, 0); }}>Назад</Button>
            <span aria-live="polite">Страница {page + 1} из {totalPages}</span>
            <Button variant="tint" size="sm" disabled={loading || page + 1 >= totalPages}
              onClick={() => { setPage(value => value + 1); window.scrollTo(0, 0); }}>Далее</Button>
          </nav>
        )}
      </Page>
      <MapView places={mapPlaces} open={mapOpen} onClose={() => setMapOpen(false)}
        loading={mapLoading} error={mapError} onRetry={() => setMapAttempt(value => value + 1)} />
    </>
  );
};

export default Places;
