# SportGuide Frontend

Telegram Web App frontend for SportGuide application.

## Features

- 🔐 Telegram Web App authentication
- 📅 Events listing and viewing
- 📍 Sports places with photos and details
- 👥 Joint trainings (create, edit, delete)
- 📋 Training programs
- 👨‍🏫 Coaches directory
- 📱 Responsive design following Telegram design guidelines

## Tech Stack

- **React 18** with JavaScript
- **Vite** for build tooling
- **React Router** for navigation
- **Axios** for API calls
- **@twa-dev/sdk** for Telegram Web App integration

## Setup

### Option 1: Docker (optional local alternative)

Production uses host Nginx and GitHub Actions (`.github/workflows/deploy.yml`).
The frontend service in `docker-compose.yml` remains commented out. Use the
standalone Docker commands below only for local testing.

### Option 2: Local Development

1. Install dependencies:
```bash
npm install
```

2. Configure API URL (optional - defaults to `http://localhost:8081`):
   - For local development: Create `.env.local` file:
     ```env
     VITE_API_BASE_URL=http://localhost:8081
     ```
   - For production: Use `https://api.sportguide.online`

3. **Start the backend** (if not already running):
   ```bash
   # In the project root directory
   ./gradlew bootRun
   # Backend will run on http://localhost:8081
   ```

4. **Start the frontend development server** (in a separate terminal):
   ```bash
   cd frontend
   npm run dev
   ```
   Frontend will run on `http://localhost:3001`

5. Build for production:
```bash
npm run build
```

## Project Structure

```
frontend/
├── src/
│   ├── components/      # Reusable components
│   ├── pages/          # Page components
│   ├── services/       # API and auth services
│   ├── config/         # Configuration files
│   ├── App.jsx         # Main app component
│   └── main.jsx        # Entry point
├── public/             # Static assets
├── index.html          # HTML template
├── Dockerfile          # Docker configuration
├── nginx.conf          # Nginx configuration for production
└── package.json        # Dependencies
```

## Docker

Run these commands from the repository root. `frontend/Dockerfile.dockerignore`
selects only frontend and landing sources; the backend `.dockerignore` is unchanged.

### Build Docker Image
```bash
docker build -f frontend/Dockerfile -t sportguide-frontend .
```

### Run Docker Container
```bash
docker run --rm -p 127.0.0.1:3001:80 sportguide-frontend
```

### Build with Custom API URL
```bash
docker build -f frontend/Dockerfile --build-arg VITE_API_BASE_URL=https://api.sportguide.online -t sportguide-frontend .
```

## Telegram Web App Integration

The app uses Telegram Web App SDK to:
- Authenticate users via Telegram initData
- Access Telegram theme colors
- Show native alerts and confirmations
- Expand to full screen

## API Endpoints

The frontend communicates with the backend API:
- `POST /api/auth/telegram` - Telegram authentication
- `GET /api/events` - Get events
- `GET /api/places` - Get places
- `GET /api/joint-trainings` - Get joint trainings
- `POST /api/joint-trainings` - Create joint training
- `PUT /api/joint-trainings/:id` - Update joint training
- `DELETE /api/joint-trainings/:id` - Delete joint training
- `GET /api/training-programs` - Get training programs
- `GET /api/coaches` - Get coaches

## Development

The app runs on `http://localhost:3001` by default (to avoid conflict with Grafana on port 3000). Make sure the backend API is accessible and CORS is properly configured.

## Deployment

Build the app and serve the `dist` folder. The app should be accessible via HTTPS for Telegram Web App to work properly.

## Ports

- **Development**: Port 3001 (to avoid conflict with Grafana on 3000)
- **Docker**: Port 80 inside container, mapped to 3001 on host

## Public landing and launch navigation

`npm run build` also packages `../landing/` into `dist/landing/`. Assets are copied
byte-for-byte into `assets/<content-hash>/`; only asset URLs in the published
`index.html` are updated. Relative JS imports and CSS URLs stay inside that version,
so CDN caching cannot mix releases. The source landing remains buildless, with no API.
Serve it at `/landing/`; Nginx redirects `/landing` to `/landing/` with 308 and
returns actual 404 responses for missing landing files.

Production checks signed-data **structure** from the initialized `@twa-dev/sdk`
before importing React/App. Without launch data it uses
`location.replace('/landing/')`. SDK session recovery preserves hash routes on
reload, including Telegram Web. This is a navigation hint, not authentication: the
backend still verifies Telegram initData and protects API access with JWT.
Launch modes without initData cannot authenticate this application (as before).

`npm run dev` keeps the existing browser development workflow. It skips only the
navigation gate and does not create a token or bypass API authentication. Production
builds have no URL, localStorage or hostname switch to disable the gate. To preview
the standalone landing during development, run `python3 -m http.server 18083` from
`landing/`. For the integrated production behavior use Nginx or the Docker image.

`npm test` checks the launch gate. `tests/browser-smoke.mjs` exercises a production
Nginx site using Playwright and Chrome, synthetic launch data and mocked API replies.
Set `SMOKE_BASE_URL`; optionally set `SMOKE_DEV_URL` and `PLAYWRIGHT_MODULE` to an
installed Playwright module. This does not verify real Telegram signatures.
