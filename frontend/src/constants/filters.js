export const DISTRICTS = [
  { value: 'ZHELEZNODOROZHNYY', label: 'Железнодорожный' },
  { value: 'LEVOBEREZHNYY', label: 'Левобережный' },
  { value: 'CENTRALNYY', label: 'Центральный' },
  { value: 'SOVETSKYY', label: 'Советский' },
  { value: 'KOMINTERNOVSKYY', label: 'Коминтерновский' },
  { value: 'LENINSKYY', label: 'Ленинский' },
  { value: 'BEHIND_OF_CITY', label: 'За городом' },
  { value: 'ALL_DISTRICTS', label: 'Все районы' },
];

export const SUBDISTRICTS = [
  { value: 'PROCESSOR', label: 'Процессор' },
  { value: 'FROM_OSTUZHEVO_RING_TO_CHERNAVSKAAY_DAM', label: 'От Остужевского к-а до Чернавской дамбы' },
  { value: 'FROM_OSTUZHEVO_RING_TO_RAILWAY_BRIDGE', label: 'От ЖД м-а до Остужевского к-а' },
  { value: 'ELECTRONIKA', label: 'Электроника' },
  { value: 'OTROZHKA_BOROVOE_SOMOVO', label: 'Отрожка/Боровое/Сомово' },
  { value: 'DIMITROVA_STREET', label: 'Димитрова/Мостозавод/Частный сектор' },
  { value: 'FROM_CHERNAVSKYY_BRIDGE_TO_VOGRESOVSKYY_BRIDGE', label: 'От Чернавского м-а до Вогресовского м-а' },
  { value: 'VAI_KRASNYY_OKTYABR', label: 'ВАИ/Красный октябрь' },
  { value: 'PESCHANKA_OZERKI_SHINNIK_1_STARYY_MASHMET_BAM_MASHMET', label: 'Машмет/БАМ/Озерки/Шинник-1/Песчанка' },
  { value: 'FROM_NOVYY_BOMBEY_TO_ARMADA_TO_DEPUTATKA', label: 'м.Новый Бомбей/ТЦ Армада/м.Депутатка' },
  { value: 'PERVOE_MAAY_UGO_ZAPADNYY_ZAPADNYY_POSELOK', label: 'Первое мая/Юго-западный/Западный поселок' },
  { value: 'PRIDONSKOYY_PODKLETNOE', label: 'Придонской/Подкетное' },
  { value: 'TENNISTYY_ZAYYMISHE', label: 'Теннистый/Займище' },
  { value: 'SHILOVO', label: 'Шилово' },
  { value: 'REST_OF_CENTRALNYY_DISTRICT', label: 'Ломоносова/Московский пр./Крынина' },
  { value: 'FROM_VGU_TO_SEVERNYY_BRIDGE', label: 'От ВГУ до Северного моста' },
  { value: 'FROM_45_DIVISII_STREET_TO_9_YANVARY_STREET_AND_TO_MP', label: '45 Стрел-ой див./9 января/Московский пр.' },
  { value: 'IPPODROM_FROM_MP_TO_ROTONDA_TO_URITSKOGO_STREET', label: 'кв.Ипподром/Московский пр./Урицкого' },
  { value: 'PODGORNOE_HVOINYY_ZADONIE', label: 'Подгорное/Хвойный/Задонье(Грин парк)' },
  { value: 'SEVERNYY', label: 'Северный' },
  { value: 'ALL_SUBDISTRICTS', label: 'Все подрайоны' },
];

export const PLACE_TYPES = [
  { value: 'SPORT_GROUND', label: 'Уличная площадка' },
  { value: 'FOOTBALL_FIELD', label: 'Футбольное поле' },
  { value: 'BASKETBALL_FIELD', label: 'Баскетбольное поле' },
  { value: 'VOLLEYBALL_FIELD', label: 'Волейбольное поле' },
  { value: 'TENNIS_COURT', label: 'Теннисный корт' },
  { value: 'PINGPONG_TABLE', label: 'Пинг-понг стол' },
  { value: 'PADEL_COURT', label: 'Падел корт' },
  { value: 'ICE_RING', label: 'Ледовая арена' },
  { value: 'SWIMMING_POOL', label: 'Бассейн' },
  { value: 'RUNNING_PLACE', label: 'Беговая зона' },
  { value: 'MARTIAL_ARTS_HALL', label: 'Зал единоборств' },
  { value: 'GYM', label: 'Тренажерный зал' },
];

export const OUTDOOR_OPTIONS = [
  { value: 'true', label: 'На улице' },
  { value: 'false', label: 'В помещении' },
];

export const SPORT_TYPES = [
  { value: 'FOOTBALL', label: 'Футбол' },
  { value: 'VOLLEYBALL', label: 'Волейбол' },
  { value: 'HOCKEY', label: 'Хоккей' },
  { value: 'MMA', label: 'ММА' },
  { value: 'BOXING', label: 'Бокс' },
  { value: 'BASKETBALL', label: 'Баскетбол' },
  { value: 'PADEL', label: 'Падел' },
  { value: 'PING_PONG', label: 'Пинг-понг' },
  { value: 'TENNIS', label: 'Теннис' },
  { value: 'JIU_JITSU', label: 'Джиу-Джитсу' },
  { value: 'WRESTLING', label: 'Борьба' },
  { value: 'FITNESS', label: 'Фитнес' },
  { value: 'SWIMMING', label: 'Плаванье' },
  { value: 'NUTRITIONOLOGY', label: 'Нутрициология' },
  { value: 'RUNNING', label: 'Бег' },
  { value: 'WORKOUT', label: 'Воркаут' },
  { value: 'SKIING', label: 'Лыжи' },
  { value: 'SCATING', label: 'Фигурное катание' },
  { value: 'GYMNASTICS', label: 'Гимнастика' },
  { value: 'STRETCHING', label: 'Растяжка' },
  { value: 'YOGA', label: 'Йога' },
  { value: 'GROUP_TRAININGS', label: 'Групповые тренировки' },
  { value: 'ATHLETICS', label: 'Легкая атлетика' },
  { value: 'POWERLIFTING', label: 'Тяжелая атлетика' },
  { value: 'RECOVERY', label: 'Восстановление' },
  { value: 'DANCING', label: 'Танцы' },
  { value: 'CYCLING', label: 'Велосипед' },
];

export const SEX_OPTIONS = [
  { value: 'MALE', label: 'Мужчина' },
  { value: 'FEMALE', label: 'Женщина' },
];
