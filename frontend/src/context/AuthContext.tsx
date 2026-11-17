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

  const handleAuthSuccess = async (response: AuthResponse, fallbackUser?: Partial<User>) => {
    setAuthToken(response.accessToken);
    if (response.refreshToken) {
      setRefreshToken(response.refreshToken);
    }

    let resolvedUser: User | null = response.user ?? null;
    if (!resolvedUser) {
      try {
        resolvedUser = await authApi.getCurrentUser();
      } catch {
        if (fallbackUser?.email) {
          resolvedUser = {
            id: fallbackUser.id ?? '',
            email: fallbackUser.email,
            fullName: fallbackUser.fullName ?? fallbackUser.email,
            role: fallbackUser.role ?? 'USER',
          };
        }
      }
    }

    if (resolvedUser) {
      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(resolvedUser));
      setUser(resolvedUser);
    }
    setAccessTokenState(response.accessToken);
  };

  const login = async (credentials: LoginRequest): Promise<AuthResponse> => {
    setIsLoading(true);
    try {
      const response = await authApi.login(credentials);
      await handleAuthSuccess(response, { email: credentials.email });
      return response;
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (data: RegisterRequest): Promise<AuthResponse> => {
    setIsLoading(true);
    try {
      const registerResponse = await authApi.register(data);
      if (registerResponse && registerResponse.accessToken) {
        await handleAuthSuccess(registerResponse, {
          email: data.email,
          fullName: data.fullName,
        });
        return registerResponse;
      }

      // Backend POST /api/v1/auth/register returns 201 UserProfileResponse;
      // seamlessly sign in to obtain JWT access and refresh tokens.
      const registeredProfile = registerResponse as unknown as Partial<User>;
      const loginResponse = await authApi.login({
        email: data.email,
        password: data.password,
      });
      await handleAuthSuccess(loginResponse, {
        id: registeredProfile?.id,
        email: registeredProfile?.email ?? data.email,
        fullName: registeredProfile?.fullName ?? data.fullName,
        role: 'USER',
      });
      return loginResponse;
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

