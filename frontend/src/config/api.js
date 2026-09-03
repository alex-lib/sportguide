// API Configuration
// For local development, use: http://localhost:8081
// For production, use: https://api.sportguide.online
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081';

export const API_ENDPOINTS = {
  AUTH: {
    TELEGRAM: `${API_BASE_URL}/api/auth/telegram`,
  },
  EVENTS: `${API_BASE_URL}/api/events`,
  PLACES: `${API_BASE_URL}/api/places`,
  PLACES_PHOTO: `${API_BASE_URL}/api/places/photo`,
  JOINT_TRAININGS: `${API_BASE_URL}/api/joint-trainings`,
  TRAINING_PROGRAMS: `${API_BASE_URL}/api/training-programs`,
  COACHES: `${API_BASE_URL}/api/coaches`,
  TOURS: `${API_BASE_URL}/api/tours`,
};
