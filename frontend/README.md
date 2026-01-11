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

### Option 1: Docker Compose (Recommended)

The frontend is included in the main `docker-compose.yml` file. To start everything:

```bash
# From project root
docker-compose up --build
```

The frontend will be available at `http://localhost:3001`

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
   - For production: Use `http://api.sportguide.online`

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

### Build Docker Image
```bash
docker build -t sportguide-frontend ./frontend
```

### Run Docker Container
```bash
docker run -p 3001:80 sportguide-frontend
```

### Build with Custom API URL
```bash
docker build --build-arg VITE_API_BASE_URL=http://api.sportguide.online -t sportguide-frontend ./frontend
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
