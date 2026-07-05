import { useEffect, useState, useCallback } from 'react';
import { apiService } from '../services/api.js';
import FilterPanel from '../components/FilterPanel.jsx';
import { SPORT_TYPES } from '../constants/filters.js';
import {
  Page,
  PageHeader,
  Card,
  CardRow,
  CardTitle,
  CardText,
  CardChevron,
  Thumb,
  Pills,
  Pill,
  Button,
  EmptyState,
  ErrorBanner,
  SkeletonList,
  sportIconName,
} from '../ui';

// Map a difficulty label to a tone + dot count.
const difficultyTone = (d = '') => {
  const s = d.toLowerCase();
  if (s.includes('нач') || s.includes('лёг') || s.includes('лег')) return { tone: 'success', dots: '●' };
  if (s.includes('сред')) return { tone: 'warning', dots: '●●' };
  if (s.includes('слож') || s.includes('продв') || s.includes('выс')) return { tone: 'danger', dots: '●●●' };
  return { tone: 'neutral', dots: '' };
};

const TrainingPrograms = () => {
  const [programs, setPrograms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState({ sportTypes: [] });

  const loadPrograms = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const filterParams = {};
      if (filter.sportTypes && filter.sportTypes.length > 0) filterParams.sportTypes = filter.sportTypes;

      const response = await apiService.getTrainingPrograms(filterParams);
      setPrograms(response?.list || []);
    } catch (error) {
      console.error('Failed to load training programs:', error);
      setError(error.message || 'Не удалось загрузить программы');
      setPrograms([]);
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => {
    loadPrograms();
  }, [loadPrograms]);

  const handleFilterChange = (key, value) => {
    setFilter((prev) => ({ ...prev, [key]: value }));
  };

  const handleResetFilters = () => {
    setFilter({ sportTypes: [] });
  };

  const filterConfig = [
    { type: 'multiselect', key: 'sportTypes', title: 'Вид спорта', options: SPORT_TYPES || [], value: filter.sportTypes },
  ];

  return (
    <>
      <PageHeader title="Программы" />
      <Page>
        <FilterPanel
          filters={filterConfig}
          onFilterChange={handleFilterChange}
          onReset={handleResetFilters}
          searchPlaceholder="Поиск программ"
        />

        {error && <ErrorBanner>{error}</ErrorBanner>}

        {loading ? (
          <SkeletonList />
        ) : !error && programs.length === 0 ? (
          <EmptyState
            icon="clipboard-list"
            accent
            title="Программ не найдено"
            message="По выбранным фильтрам ничего нет."
            action={
              <Button variant="ghost" onClick={handleResetFilters}>
                Сбросить фильтры
              </Button>
            }
          />
        ) : (
          <div>
            {programs.map((program, index) => {
              const diff = difficultyTone(program.difficulty);
              return (
                <Card key={program.id || index} first={index === 0}>
                  <CardRow>
                    <Thumb icon={sportIconName(program.sportType || program.name)} accent={index % 2 === 1} size={23} />
                    <div style={{ flex: 1 }}>
                      <CardTitle>{program.name}</CardTitle>
                      {program.description && <CardText>{program.description}</CardText>}
                    </div>
                    <CardChevron />
                  </CardRow>

                  {(program.duration || program.difficulty) && (
                    <Pills>
                      {program.duration && <Pill icon="clock">{program.duration}</Pill>}
                      {program.difficulty && (
                        <Pill tone={diff.tone}>
                          {diff.dots && `${diff.dots} `}
                          {program.difficulty}
                        </Pill>
                      )}
                    </Pills>
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

export default TrainingPrograms;
