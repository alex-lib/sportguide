# SportGuide

SportGuide is a sport guide for the city of Voronezh, delivered as a **Telegram Mini App** with a Telegram bot and notifications. It helps people discover sport events, places to train, partners for joint workouts, ready-made training programs and coaches - and sends weather and event notifications.

The product is two parts:

- **Backend** - a Spring Boot service (Java + Kotlin) that exposes the REST API, runs the Telegram bot, handles authentication, scheduling, weather and AI assistant features.
- **Frontend** - a Telegram Web App (Vite + React) rendered inside Telegram, talking to the backend API.

## Navigation (app sections)

The Mini App is a 6-tab experience. Routes use a `HashRouter` (`frontend/src/App.jsx`); the bottom tab bar is defined in `frontend/src/components/Layout.jsx`.

| Tab | Route | Page | What it does |
| --- | --- | --- | --- |
| Главная (Home) | `/` | `Home.jsx` | Entry screen with links into each section |
| События (Events) | `/events` | `Events.jsx` | Upcoming sport events, filterable by district and date |
| Места (Places) | `/places` | `Places.jsx` | Courts, gyms and grounds, filterable by district / type / indoor |
| Тренировки (Joint trainings) | `/joint-trainings` | `JointTrainings.jsx` | Find partners and create/edit/delete training meetups |
| Программы (Programs) | `/training-programs` | `TrainingPrograms.jsx` | Ready-made workout programs, filterable by sport |
| Тренеры (Coaches) | `/coaches` | `Coaches.jsx` | Coach roster, filterable by sport / sex / age / experience |

Shared pieces: `Layout.jsx` (tab bar), `FilterPanel.jsx` (collapsible filters), `Loading.jsx`, and auth in `services/auth.js`.

## Design mockups

Interactive HTML mockups of every screen live in [`mockups/index.html`](mockups/index.html). Open the file in a browser (it is fully self-contained - no build, no network needed).

It includes:

- A **Design: Old / New** toggle - "Old" reproduces the current production UI faithfully; "New" is a refactored design aligned with Apple HIG 2026.
- A **Light / Dark** theme switch (the New design adapts to the theme; the page background follows it).
- Every screen rendered inside an iPhone 17 Pro frame, grouped by feature and tab.

The chosen design variant and theme are remembered across reloads.

## Tech stack

**Backend** (`src/`, `build.gradle.kts`)
- Spring Boot 3.5, Java + Kotlin
- Spring Security (OAuth2 resource server / client, JWT), Keycloak as the identity provider
- Spring AI (sport assistant), Thymeleaf templates for messages
- PostgreSQL (data), Redis (sessions / notifications)
- Telegram bot integration, scheduled weather and event notifications
- Runs on port `8081`

**Frontend** (`frontend/`)
- Vite + React 18
- `react-router-dom` (HashRouter)
- `@twa-dev/sdk` (Telegram Web App SDK)
- `axios` for API calls

## Running locally

### Backend

```bash
# build and run the service (needs PostgreSQL + Redis, see docker-compose.yml)
./gradlew bootRun
```

Required infrastructure (PostgreSQL, Redis, Keycloak) is described in `docker-compose.yml`. Configuration lives in `src/main/resources/application.yml` and is driven by environment variables (bot token, DB credentials, etc.).

### Frontend

```bash
cd frontend
npm install
npm run dev        # start the Vite dev server
npm run build      # production build
npm run lint       # lint
```

### Docker

```bash
docker compose up        # postgres, redis, app, keycloak
```

## Telegram authentication

The Web App sends Telegram `initData` to the backend, which validates it and issues a session:

```js
const initData = Telegram.WebApp.initData;
fetch("https://app/auth/telegram", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ initData })
});
```

## Repository layout

```
.
├── src/                  # backend (Spring Boot, Java/Kotlin)
│   └── main/resources/   # application.yml, init.sql, message templates, AI prompts
├── frontend/             # Telegram Web App (Vite + React)
│   └── src/{pages,components,services,config,constants}
├── mockups/              # self-contained HTML screen mockups (Old/New, Light/Dark)
├── config/               # prometheus / grafana / alertmanager configs
├── keycloak/             # Keycloak realm export
├── docker-compose.yml
└── build.gradle.kts
```
