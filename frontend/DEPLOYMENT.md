# Deployment Guide

## Prerequisites

- Node.js 18+ and npm
- Backend API running and accessible
- HTTPS domain (required for Telegram Web App)

## Setup Steps

1. **Install Dependencies**
   ```bash
   cd frontend
   npm install
   ```

2. **Configure Environment**
   Create `.env` file:
   ```env
   VITE_API_BASE_URL=http://api.sportguide.online
   ```

3. **Build for Production**
   ```bash
   npm run build
   ```

4. **Deploy**
   - The `dist` folder contains the built files
   - Serve the `dist` folder using a web server (nginx, Apache, etc.)
   - Ensure HTTPS is enabled
   - Configure CORS on the backend to allow your frontend domain

5. **Telegram Bot Configuration**
   - Set the Web App URL in your Telegram bot settings
   - The URL should point to your deployed frontend (e.g., `https://yourdomain.com`)

## Development

Run the development server:
```bash
npm run dev
```

The app will be available at `http://localhost:3000`

## Backend Requirements

The backend API must:
- Accept CORS requests from your frontend domain
- Have the `/api/auth/telegram` endpoint configured
- Return proper JWT tokens for authentication
- Include `id` field in `JointTrainingResponse` (already updated)

## Notes

- The frontend uses Telegram Web App SDK for authentication
- Users authenticate via Telegram's `initData`
- JWT tokens are stored in localStorage
- The app automatically expands to full screen in Telegram

