import { useTheme } from '../context/ThemeContext';
import { Page, PageHeader, IconButton, Hero, SectionLabel, Tiles, Tile } from '../ui';

const Home = () => {
  const { toggle } = useTheme();

  return (
    <>
      <PageHeader
        eyebrow="Воронеж"
        eyebrowIcon="map-pin"
        title="Привет, чем займёмся?"
        action={<IconButton icon="settings" label="Настройки" onClick={toggle} />}
      />
      <Page>
        <Hero
          to="/events"
          title="Найди тренировку рядом"
          subtitle="События, площадки и партнёры для занятий спортом в одном месте."
          cta="Смотреть события"
        />

        <SectionLabel>Быстрый доступ</SectionLabel>
        <Tiles>
          <Tile to="/events" icon="calendar" title="События" subtitle="Ближайшие спорт-события" />
          <Tile to="/places" icon="map-pin" accent title="Места" subtitle="Площадки и залы" />
          <Tile to="/joint-trainings" icon="users" title="Тренировки" subtitle="Найти партнёров" />
          <Tile to="/coaches" icon="graduation-cap" accent title="Тренеры" subtitle="Инструкторы рядом с тобой" />
        </Tiles>
      </Page>
    </>
  );
};

export default Home;
