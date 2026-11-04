import axios, { AxiosError } from 'axios';

const TOKEN_KEY = 'tms_access_token';
const REFRESH_TOKEN_KEY = 'tms_refresh_token';

export const getAuthToken = (): string | null => {
  return localStorage.getItem(TOKEN_KEY);
};

export const setAuthToken = (token: string): void => {
  localStorage.setItem(TOKEN_KEY, token);
};

export const getRefreshToken = (): string | null => {
  return localStorage.getItem(REFRESHRESH_TOKEN_KEY());
};

const REFRESHRESH_TOKEN_KEY = () => REFRESH_TOKEN_KEY;

export const setRefreshToken = (token: string): void => {
  localStorage.setItem(REFRESH_TOKEN_KEY, token);
};

export const clearAuthToken = (): void => {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
};

const generateCorrelationId = (): string => {
  return 'corr-' + Math.random().toString(36).substring(2, 11) + '-' + Date.now();
};

const apiGatewayUrl = import.meta.env.VITE_API_GATEWAY_URL;

export const apiClient = axios.create({
  baseURL: apiGatewayUrl !== undefined && apiGatewayUrl !== '' ? apiGatewayUrl : '',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Attach Bearer JWT and X-Correlation-ID
apiClient.interceptors.request.use(
  (config) => {
    const token = getAuthToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    if (!config.headers['X-Correlation-ID']) {
      config.headers['X-Correlation-ID'] = generateCorrelationId();
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Centralize error handling & handle 401
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      clearAuthToken();
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('tms:auth-expired'));
      }
    }
    return Promise.reject(error);
  }
);

