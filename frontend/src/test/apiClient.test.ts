import { describe, it, expect, beforeEach, vi } from 'vitest';
import { apiClient, setAuthToken, getAuthToken, clearAuthToken } from '../api/apiClient';

describe('Centralized apiClient', () => {
  beforeEach(() => {
    clearAuthToken();
    vi.restoreAllMocks();
  });

  it('should have configured default headers and base URL', () => {
    expect(apiClient.defaults.headers['Content-Type']).toBe('application/json');
    expect(apiClient.defaults.timeout).toBe(10000);
  });

  it('should inject stored token and correlation ID in request interceptor', async () => {
    setAuthToken('mock-jwt-token-123');

    // Test request interceptor directly
    // @ts-expect-error accessing internal interceptor
    const requestInterceptor = apiClient.interceptors.request.handlers[0];
    const config = { headers: {} } as any;
    const modifiedConfig = await requestInterceptor.fulfilled(config);

    expect(modifiedConfig.headers['Authorization']).toBe('Bearer mock-jwt-token-123');
    expect(modifiedConfig.headers['X-Correlation-ID']).toBeDefined();
    expect(modifiedConfig.headers['X-Correlation-ID']).toMatch(/^[a-z0-9-]+$/i);
  });

  it('should not attach Authorization header when token is not present', async () => {
    clearAuthToken();
    expect(getAuthToken()).toBeNull();

    // @ts-expect-error accessing internal interceptor
    const requestInterceptor = apiClient.interceptors.request.handlers[0];
    const config = { headers: {} } as any;
    const modifiedConfig = await requestInterceptor.fulfilled(config);

    expect(modifiedConfig.headers['Authorization']).toBeUndefined();
    expect(modifiedConfig.headers['X-Correlation-ID']).toBeDefined();
  });

  it('should handle 401 error response by clearing auth token', async () => {
    setAuthToken('token-to-be-cleared');

    // @ts-expect-error accessing internal interceptor
    const responseInterceptor = apiClient.interceptors.response.handlers[0];
    const error401 = {
      response: {
        status: 401,
        data: { errorCode: 'UNAUTHORIZED', message: 'Token expired' },
      },
    };

    await expect(responseInterceptor?.rejected?.(error401)).rejects.toEqual(error401);
    expect(getAuthToken()).toBeNull();
  });
});
