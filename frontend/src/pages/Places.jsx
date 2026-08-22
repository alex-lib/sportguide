import { useEffect, useState, useCallback, useRef } from 'react';
import { apiService } from '../services/api.js';
import FilterPanel from '../components/FilterPanel.jsx';
import { DISTRICTS, SUBDISTRICTS, PLACE_TYPES, OUTDOOR_OPTIONS } from '../constants/filters.js';
import {
  Page,
  PageHeader,
  IconButton,
  Card,
  CardTitle,
  CardText,
  Photo,
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
import { pluralRu } from '../utils/plural.js';

const isOutdoor = (place) => place.outdoor === 'true' || /улиц/i.test(place.outdoor || '');

const Places = () => {
  const [places, setPlaces] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [mapOpen, setMapOpen] = useState(false);
  const [filter, setFilter] = useState({
    district: null,
    subDistrict: null,
    outdoor: null,
    placeType: null,
  });
  const [searchText, setSearchText] = useState('');

  const latestRequest = useRef(0);

  const loadPlaces = useCallback(async () => {
    const requestId = ++latestRequest.current;
    setLoading(true);
    setError(null);
    try {
      const filterParams = {};
      if (searchText) filterParams.search = searchText;
      if (filter.district) filterParams.district = filter.district;
      if (filter.subDistrict) filterParams.subDistrict = filter.subDistrict;
      if (filter.outdoor) filterParams.outdoor = filter.outdoor;
      if (filter.placeType) filterParams.placeType = filter.placeType;

      const response = await apiService.getPlaces(filterParams);
      if (requestId !== latestRequest.current) return;
      setPlaces(response?.list || []);
    } catch (error) {
      if (requestId !== latestRequest.current) return;
      console.error('Failed to load places:', error);
      setError(error.message || 'Не удалось загрузить места');
    } finally {
      if (requestId === latestRequest.current) setLoading(false);
    }
  }, [filter, searchText]);

  useEffect(() => {
    loadPlaces();
  }, [loadPlaces]);

  const handleFilterChange = (key, value) => {
    setFilter((prev) => ({ ...prev, [key]: value }));
  };

  const handleResetFilters = () => {
    setFilter({ district: null, subDistrict: null, outdoor: null, placeType: null });
  };

  const filterConfig = [
    { type: 'chip', key: 'placeType', title: 'Тип места', options: PLACE_TYPES, value: filter.placeType },
    { type: 'chip', key: 'district', title: 'Район', options: DISTRICTS, value: filter.district },
    { type: 'chip', key: 'subDistrict', title: 'Подрайон', options: SUBDISTRICTS, value: filter.subDistrict },
    { type: 'chip', key: 'outdoor', title: 'Расположение', options: OUTDOOR_OPTIONS, value: filter.outdoor },
  ];

  const eyebrow =
    !loading && places.length > 0
      ? `${places.length} ${pluralRu(places.length, ['место', 'места', 'мест'])}`
      : undefined;

  return (
    <>
      <PageHeader
        eyebrow={eyebrow}
        title="Места"
        action={<IconButton icon="map" label="Карта" onClick={() => setMapOpen(true)} />}
      />
      <Page>
        <FilterPanel
          filters={filterConfig}
          onFilterChange={handleFilterChange}
          onReset={handleResetFilters}
          searchPlaceholder="Поиск мест"
          hideQuickChips
          search={searchText}
          onSearch={(e) => setSearchText(e.target.value)}
        />

        {error && <ErrorBanner>{error}</ErrorBanner>}

        {loading ? (
          <SkeletonList />
        ) : !error && places.length === 0 ? (
          <EmptyState
            icon="map-pin"
            accent
            title="Мест не найдено"
            message="По выбранным фильтрам ничего нет."
            action={
              <Button variant="ghost" onClick={handleResetFilters}>
                Сбросить фильтры
              </Button>
            }
          />
        ) : (
          <div>
            {places.map((place, index) => (
              <Card key={place.id || index} first={index === 0}>
                {place.photoUrl ? (
                  <Photo src={place.photoUrl} alt={place.name} />
                ) : place.id ? (
                  <Photo src={`/api/places/${place.id}/photo`} alt={place.name} onError={(e) => { e.currentTarget.style.display = 'none'; e.currentTarget.nextElementSibling?.removeAttribute('hidden'); }} />
                ) : null}
                {(!place.photoUrl && !place.id) && <PhotoPlaceholder icon="building-2" />}

                <div style={{ display: 'flex', alignItems: 'flex-start', gap: '8px' }}>
                  <div style={{ flex: 1 }}>
                    <CardTitle>{place.name}</CardTitle>
                    {place.description && <CardText>{place.description}</CardText>}
                  </div>
                  <Pill tone="brand" icon={isOutdoor(place) ? 'land-plot' : 'building'}>
                    {isOutdoor(place) ? 'Улица' : 'Зал'}
                  </Pill>
                </div>

                {(place.address || place.district) && (
                  <MetaLine>{[place.address, place.district].filter(Boolean).join(' · ')}</MetaLine>
                )}

                <Divider />
                <CardActions>
                  <Button href={place.coordinates} target="_blank" rel="noopener noreferrer" size="sm" fullWidth>
                    На карте
                  </Button>
                  {place.webSite && place.webSite !== '-' && (
                    <Button href={place.webSite} target="_blank" rel="noopener noreferrer" variant="tint" size="sm" fullWidth>
                      Сайт
                    </Button>
                  )}
                </CardActions>
              </Card>
            ))}
          </div>
        )}
      </Page>
      <MapView places={places} open={mapOpen} onClose={() => setMapOpen(false)} />
    </>
  );
};

export default Places;
