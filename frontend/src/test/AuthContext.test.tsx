import React from 'react';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, act } from '@testing-library/react';
import { AuthProvider, useAuth } from '../context/AuthContext';
import { authApi } from '../api/authApi';
import { AuthResponse } from '../types/auth';

vi.mock('../api/authApi');

const TestConsumer: React.FC = () => {
  const { user, isAuthenticated, login, logout } = useAuth();
  return (
    <div>
      <div data-testid="auth-status">{isAuthenticated ? 'authenticated' : 'unauthenticated'}</div>
      <div data-testid="user-email">{user?.email || 'no-email'}</div>
      <button
        onClick={() =>
          login({ email: 'test@example.com', password: 'Password123!' })
        }
      >
        Login
      </button>
      <button onClick={logout}>Logout</button>
    </div>
  );
};

describe('AuthContext and AuthProvider', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  it('should initialize with unauthenticated state when localStorage is empty', () => {
    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    expect(screen.getByTestId('auth-status').textContent).toBe('unauthenticated');
    expect(screen.getByTestId('user-email').textContent).toBe('no-email');
  });

  it('should restore session from localStorage when user and token exist', () => {
    const mockUser = { id: 'u-1', email: 'restored@example.com', fullName: 'Restored User', role: 'ROLE_USER' };
    localStorage.setItem('tms_user', JSON.stringify(mockUser));
    localStorage.setItem('tms_access_token', 'valid-token');

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    expect(screen.getByTestId('auth-status').textContent).toBe('authenticated');
    expect(screen.getByTestId('user-email').textContent).toBe('restored@example.com');
  });

  it('should log in successfully and update state', async () => {
    const mockResponse: AuthResponse = {
      accessToken: 'access-123',
      refreshToken: 'refresh-123',
      tokenType: 'Bearer',
      expiresIn: 3600,
      user: { id: 'u-2', email: 'test@example.com', fullName: 'Test User', role: 'ROLE_USER' },
    };

    vi.mocked(authApi.login).mockResolvedValue(mockResponse);

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    await act(async () => {
      screen.getByText('Login').click();
    });

    expect(screen.getByTestId('auth-status').textContent).toBe('authenticated');
    expect(screen.getByTestId('user-email').textContent).toBe('test@example.com');
  });

  it('should clear state on logout', async () => {
    const mockUser = { id: 'u-1', email: 'user@example.com', fullName: 'User', role: 'ROLE_USER' };
    localStorage.setItem('tms_user', JSON.stringify(mockUser));
    localStorage.setItem('tms_access_token', 'token');

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    expect(screen.getByTestId('auth-status').textContent).toBe('authenticated');

    await act(async () => {
      screen.getByText('Logout').click();
    });

    expect(screen.getByTestId('auth-status').textContent).toBe('unauthenticated');
    expect(localStorage.getItem('tms_user')).toBeNull();
  });
});

