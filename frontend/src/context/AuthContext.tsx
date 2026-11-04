import React, { createContext, useContext, useState, useEffect } from 'react';
import { AuthResponse, LoginRequest, RegisterRequest, User } from '../types/auth';
import { authApi } from '../api/authApi';
import { clearAuthToken, getAuthToken, setAuthToken, setRefreshToken } from '../api/apiClient';

interface AuthContextType {
  user: User | null;
  accessToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<AuthResponse>;
  register: (data: RegisterRequest) => Promise<AuthResponse>;
  logout: () => void;
}

const USER_STORAGE_KEY = 'tms_user';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const storedUser = localStorage.getItem(USER_STORAGE_KEY);
    if (storedUser) {
      try {
        return JSON.parse(storedUser);
      } catch {
        localStorage.removeItem(USER_STORAGE_KEY);
      }
    }
    return null;
  });

  const [accessToken, setAccessTokenState] = useState<string | null>(() => getAuthToken());
  const [isLoading, setIsLoading] = useState<boolean>(false);

  const logout = (): void => {
    clearAuthToken();
    localStorage.removeItem(USER_STORAGE_KEY);
    setUser(null);
    setAccessTokenState(null);
  };

  useEffect(() => {
    const handleAuthExpired = () => {
      logout();
    };

    window.addEventListener('tms:auth-expired', handleAuthExpired);
    return () => {
      window.removeEventListener('tms:auth-expired', handleAuthExpired);
    };
  }, []);

  const handleAuthSuccess = (response: AuthResponse) => {
    setAuthToken(response.accessToken);
    setRefreshToken(response.refreshToken);
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(response.user));
    setUser(response.user);
    setAccessTokenState(response.accessToken);
  };

  const login = async (credentials: LoginRequest): Promise<AuthResponse> => {
    setIsLoading(true);
    try {
      const response = await authApi.login(credentials);
      handleAuthSuccess(response);
      return response;
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (data: RegisterRequest): Promise<AuthResponse> => {
    setIsLoading(true);
    try {
      const response = await authApi.register(data);
      handleAuthSuccess(response);
      return response;
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        accessToken,
        isAuthenticated: !!user && !!accessToken,
        isLoading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

