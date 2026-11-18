# SportGuide

SportGuide — сервис для поиска спортивных мероприятий, мест для тренировок, партнёров для совместных занятий, готовых тренировочных программ и тренеров в городе Воронеж. Доступен через Telegram Mini App (Web App) и Telegram бот с уведомлениями.

Продукт состоит из двух частей:

- **Backend** — Spring Boot сервис (Java), реализующий REST API, Telegram бот, аутентификацию, планировщик уведомлений, интеграцию с погодным API, AI-ассистентом и объектным хранилищем MinIO.
- **Frontend** — Telegram Web App (Vite + React), встроенная в Telegram, общающаяся с backend API.

## Навигация (разделы приложения)

Mini App — это SPA с 6 вкладками. Роутинг через `HashRouter` (`frontend/src/App.jsx`); нижняя панель вкладок определена в `frontend/src/components/Layout.jsx`.

| Вкладка | Маршрут | Страница | Описание |
| --- | --- | --- | --- |
| Главная (Home) | `/` | `Home.jsx` | Входной экран со ссылками во все разделы |
| События (Events) | `/events` | `Events.jsx` | Ближайшие спортивные события с фильтрами по району и дате |
| Места (Places) | `/places` | `Places.jsx` | Спортплощадки, залы и стадионы с фильтрами по району / типу / помещению |
| Тренировки (Joint trainings) | `/joint-trainings` | `JointTrainings.jsx` | Поиск партнёров, создание / редактирование / удаление совместных тренировок |
| Программы (Programs) | `/training-programs` | `TrainingPrograms.jsx` | Готовые тренировочные программы с фильтрацией по виду спорта |
| Тренеры (Coaches) | `/coaches` | `Coaches.jsx` | Каталог тренеров с фильтрацией по виду спорта |

Общие компоненты: `Layout.jsx` (нижняя панель), `FilterPanel.jsx` (сворачиваемые фильтры), набор UI-компонентов в `ui/` (`Card`, `Button`, `Chip`, `Modal`, `TabBar`, `MapView` и др.), аутентификация в `services/auth.js`.

При первом посещении каждой вкладки показывается онбординг-тур с тултипами (`react-joyride`): шаги приходят с backend (`TourController`), состояние — в `TourContext.jsx` / `PageVisitContext.jsx`, кастомный тултип — `ui/TourTooltip.jsx`.

## API

| Controller | Описание |
| --- | --- |
| `AuthController` | Telegram-аутентификация с выдачей JWT |
| `EventController` | CRUD мероприятий, фильтрация |
| `PlaceController` | CRUD мест, фильтрация |
| `CoachController` | CRUD тренеров, фильтрация |
| `JointTrainingController` | Совместные тренировки, запросы на присоединение |
| `TrainingProgramController` | Тренировочные программы |
| `AiController` | AI-ассистент через Spring AI (OpenAI) |
| `AlertController` | Системные алерты |
| `TourController` | Онбординг-туры: шаги тултипов по маршруту, отметка о показе тура пользователю (без аутентификации) |

## Telegram бот

Бот (пакет `bot`) поддерживает текстовые команды, callback-кнопки и фото-ввод через интерфейсы `TextProcessable`, `CallbackProcessable`, `PhotoProcessable`.

**Команды пользователей:** `StartCommand`, `MenuCommand`, `GetUpcomingEventsCommand`, `GetPlaceCommand`, `GetNotificationsCommand`, `StopNotificationsCommand`, `CreateTrainingProgramCommand`, `ContactAdminCommand`, `SupportProjectCommand`.

**Команды администраторов:** `CreateEventCommand`, `DeleteEventCommand`, `CreatePlaceCommand`, `DeletePlaceCommand`, `SendMessageToAllUsersCommand`, `GetUsersCountCommand`, `GetSubscriptionsCountCommand`.

**Меню:** `SubscriberMenu`, `AdminMenu`, `ChoosingPlaceOptionsMenu`.

Состояния команд хранятся в Redis (session-store), конфигурация в `RedisBotCommandSessionConfiguration`.

## Tech stack

**Backend** (`src/`, `build.gradle.kts`)

- Spring Boot 3.5.4, Java 21
- Spring Data JPA, Flyway (миграции БД), Spring Data Redis, Spring Cache
- Spring Security: проверка собственных JWT после входа через Telegram
- Spring AI (Spring AI OpenAI Starter, sport assistant)
- OpenFeign (HTTP-clients), Spring Cloud
- Spring AOP (аннотация `@AdminOnly`)
- MinIO (объектное хранилище для фото)
- Lombok, MapStruct (mapper-компоненты)
- Telegram Bots API 6.9.7.1
- Prometheus + Micrometer (actuator metrics)
- PostgreSQL (данные), Redis (сессии, уведомления, кеш)
- Запуск на порту `8081`

**Frontend** (`frontend/`)

- Vite 6 + React 18
- `react-router-dom` (HashRouter)
- `@twa-dev/sdk` (Telegram Web App SDK)
- Leaflet + react-leaflet (карты)
- Lucide React (иконки)
- `react-joyride` (онбординг-туры с тултипами)
- `axios` для вызовов API

## Локальный запуск

### Backend

```bash
# Запустить инфраструктуру (PostgreSQL, Redis, MinIO)
docker compose up postgres redis minio

# Собрать и запустить бэкенд
./gradlew bootRun
```

Инфраструктура описана в `docker-compose.yml`. Конфигурация — `src/main/resources/application.yml`, управляется через переменные окружения (bot token, DB credentials и т.д.).

### Frontend

```bash
cd frontend
npm install
npm run dev        # запустить Vite dev server
npm run build      # продакшн-сборка
npm run lint       # линтер
```

### Docker

```bash
docker compose up              # postgres, redis, minio, app
```

## Telegram аутентификация

Web App отправляет Telegram `initData` на backend, который валидирует и выдаёт JWT-сессию:

```js
const initData = Telegram.WebApp.initData;
fetch("https://app/auth/telegram", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ initData })
});
```

## Структура репозитория

```
.
├── src/                              # backend (Spring Boot, Java 21)
│   ├── main/
│   │   ├── java/com/sport/service/
│   │   │   ├── ServiceApplication.java
│   │   │   ├── annotations/          # @AdminOnly, @PhoneNumberValid
│   │   │   ├── aop/                  # @AdminOnly aspect
│   │   │   ├── bot/                  # Telegram bot commands, menus, sessions
│   │   │   ├── components/           # Schedulers, health checks
│   │   │   ├── configurations/       # Bean configs (Redis, MinIO, Security, AI, etc.)
│   │   │   ├── constants/            # Constants
│   │   │   ├── dto/                  # Data transfer objects
│   │   │   ├── entities/             # JPA entities + enums
│   │   │   ├── exceptions/           # Global exception handling
│   │   │   ├── mappers/              # MapStruct mappers + string mappers
│   │   │   ├── repositories/         # JPA + Document repositories
│   │   │   ├── security/             # JwtService, TelegramJwtFilter
│   │   │   ├── services/             # Business logic services
│   │   │   ├── store/                # Redis session-store (commands, notifications)
│   │   │   ├── utils/                # Utility classes
│   │   │   └── web/controllers/      # REST API controllers
│   │   └── resources/                # application.yml, templates, prompts, migrations
│   └── test/                         # Tests
├── frontend/                         # Telegram Web App (Vite + React 18)
│   └── src/
│       ├── pages/                    # Home, Events, Places, JointTrainings, TrainingPrograms, Coaches
│       ├── components/               # Layout, FilterPanel
│       ├── ui/                       # Reusable UI: Card, Button, Modal, TabBar, MapView, TourTooltip, etc.
│       ├── services/                 # api.js, auth.js
│       ├── context/                  # ThemeContext, TourContext, PageVisitContext
│       ├── config/                   # API config
│       ├── constants/                # Filter constants, etc.
│       └── utils/                    # Date formatting, pluralization
├── config/                           # Prometheus / Grafana configs (commented out in compose)
├── design/                           # Design resources
├── docker-compose.yml
├── Dockerfile
├── build.gradle.kts
└── README.md
```

## AI provider routing

Training generation uses `app.ai.primary` (Groq `openai/gpt-oss-120b`), then
`app.ai.fallback` (OpenRouter `openrouter/free`) on HTTP 429, then the existing
Spring AI OpenAI client (`gpt-4o`) if OpenRouter also returns HTTP 429.
Other errors propagate and release the generation quota; they do not silently
trigger paid OpenAI requests.

Set `GROQ_API_KEY` and `OPENROUTER_API_KEY` in the server's private `.env` alongside
its existing `OPENAI_API_KEY`. Compose already passes this file into `app` and
maps the OpenAI key to `SPRING_AI_OPENAI_API_KEY`. Never commit this file.
Recreate the app container after changing environment values.

Groq `Retry-After` supports seconds and HTTP dates. During that pause, requests
start at OpenRouter; at expiry they start at Groq again. Missing/invalid headers
only affect the current request. The thread-safe cooldown belongs to each app
process and resets on restart; multiple replicas do not share it.

The same prompt is passed to every provider. Generated HTML is restricted to
`b` and `i`; empty, truncated (for Groq/OpenRouter), or oversized answers fail
rather than consuming a user's quota with an unusable Telegram message.
Free OpenRouter models vary between requests: successful HTTP responses do not
guarantee accurate exercise instructions or duration calculations.

Routing and output checks: `./gradlew test --tests '*AiProviderRouterTest' --tests '*AiAssistantServiceTest'`.
