import axios from 'axios';
import { auth } from '../config/firebase';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

api.interceptors.request.use(
  async (config) => {
    const token = localStorage.getItem('idbi_firebase_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    config.headers['X-Request-ID'] = crypto.randomUUID();
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        if (auth.currentUser) {
          const newToken = await auth.currentUser.getIdToken(true);
          localStorage.setItem('idbi_firebase_token', newToken);
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          return api(originalRequest);
        }
      } catch (refreshError) {
        localStorage.removeItem('idbi_firebase_token');
        if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
          window.location.href = '/login?expired=true';
        }
        return Promise.reject(refreshError);
      }
    }

    if (error.response) {
      if (error.response.status === 403) {
        console.error('Access denied:', error.response.data);
      } else if (error.response.status >= 500) {
        console.error('Server error:', error.response.data);
      } else if (error.response.status === 429) {
        console.error('Rate limited. Please slow down.');
      }
    } else if (error.code === 'ECONNABORTED') {
      console.error('Request timeout');
    } else if (!window.navigator.onLine) {
      console.error('Network offline');
    }
    return Promise.reject(error);
  }
);

export default api;
