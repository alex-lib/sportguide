import axios from 'axios';
import { API_ENDPOINTS } from '../config/api.js';

class ApiService {
  constructor() {
    this.api = axios.create({
      baseURL: API_ENDPOINTS.AUTH.TELEGRAM.split('/api')[0],
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Load token from localStorage on initialization
    this.token = localStorage.getItem('auth_token');
    if (this.token) {
      this.setAuthToken(this.token);
    }

    // Add request interceptor to include token
    this.api.interceptors.request.use(
      (config) => {
        if (this.token) {
          config.headers.Authorization = `Bearer ${this.token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

   this.api.interceptors.response.use(
     (response) => response,
     (error) => {
        if (error.response?.status === 401) {
            this.clearAuth();
            console.warn('Unauthorized, redirecting to auth flow');
        }
        return Promise.reject(error);
      }
    );
}

    // Add response interceptor to handle errors
//    this.api.interceptors.response.use(
//      (response) => response,
//      (error) => {
//        if (error.response?.status === 401) {
//          // Token expired or invalid
//          this.clearAuth();
//          window.location.reload();
//        }
//        return Promise.reject(error);
//      }
//    );
//  }

  setAuthToken(token) {
    this.token = token;
    localStorage.setItem('auth_token', token);
    this.api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  }

  clearAuth() {
    this.token = null;
    localStorage.removeItem('auth_token');
    delete this.api.defaults.headers.common['Authorization'];
  }

  // Authentication
  async authenticateTelegram(initData) {
    const response = await this.api.post(
      API_ENDPOINTS.AUTH.TELEGRAM,
      { initData }
    );
    this.setAuthToken(response.data.token);
    return response.data;
  }

  // Events
  async getEvents(filter) {
    const response = await this.api.get(API_ENDPOINTS.EVENTS, {
      params: filter,
    });
    return response.data;
  }

  // Places
  async getPlaces(filter) {
    const response = await this.api.get(API_ENDPOINTS.PLACES, {
      params: filter,
    });
    return response.data;
  }

  // Joint Trainings
  async getJointTrainings(filter = {}) {
    const response = await this.api.get(
      API_ENDPOINTS.JOINT_TRAININGS,
      { params: filter }
    );
    return response.data;
  }

  async createJointTraining(data) {
    await this.api.post(API_ENDPOINTS.JOINT_TRAININGS, data);
  }

  async updateJointTraining(id, data) {
    await this.api.put(`${API_ENDPOINTS.JOINT_TRAININGS}/${id}`, data);
  }

  async deleteJointTraining(id) {
    await this.api.delete(`${API_ENDPOINTS.JOINT_TRAININGS}/${id}`);
  }

  // Training Programs
  async getTrainingPrograms(filter = {}) {
    const response = await this.api.get(
      API_ENDPOINTS.TRAINING_PROGRAMS,
      { params: filter }
    );
    return response.data;
  }

  // Coaches
  async getCoaches(filter = {}) {
    const response = await this.api.get(API_ENDPOINTS.COACHES, {
      params: filter,
    });
    return response.data;
  }
}

export const apiService = new ApiService();