import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { API_ENDPOINTS } from '@/config/api';

interface User {
  id: string;
  name: string;
  email: string;
  picture?: string;
  onboardingCompleted?: boolean;
}

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: () => void;
  logout: () => Promise<void>;
  loginWithEmail: (email: string, password: string) => Promise<void>;
  signupWithEmail: (email: string, password: string, name: string) => Promise<void>;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Applicera sparad temainställning på initiering
    const savedTheme = localStorage.getItem("theme");
    if (savedTheme === "dark") {
      document.documentElement.classList.add("dark");
    } else {
      document.documentElement.classList.remove("dark");
    }

    loadUser();

    // Kontrollera om vi kommer från OAuth-redirect
    const params = new URLSearchParams(window.location.search);
    if (params.get("code") || window.location.pathname.includes("oauth")) {
      setTimeout(() => {
        loadUser();
      }, 500);
    }
  }, []);

  const loadUser = async () => {
    setLoading(true);
    try {
      const response = await fetch(API_ENDPOINTS.AUTH.ME, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json',
        },
      });

      if (response.ok) {
        const data = await response.json();

        // Verifiera att vi har nödvändig data
        if (data && (data.id || data.email)) {
          const userData: User = {
            id: data.id || data.sub || data.email || '',
            name: data.name || data.given_name || 'Användare',
            email: data.email || '',
            picture: data.picture,
            onboardingCompleted: data.onboardingCompleted || false,
          };
          setUser(userData);
        } else {
          setUser(null);
        }
      } else if (response.status === 401 || response.status === 403) {
        setUser(null);
      } else {
        setUser(null);
      }
    } catch {
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  const refreshUser = async () => {
    await loadUser();
  };

  const login = () => {
    window.location.href = API_ENDPOINTS.AUTH.OAUTH_GOOGLE;
  };

  const loginWithEmail = async (email: string, password: string) => {
    const response = await fetch(API_ENDPOINTS.AUTH.LOGIN, {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, password }),
    });

    if (response.ok) {
      setLoading(true);
      const data = await response.json();
      const userData: User = {
        id: data.id || data.email || '',
        name: data.name || 'Användare',
        email: data.email || '',
        picture: data.picture,
        onboardingCompleted: data.onboardingCompleted || false,
      };
      setUser(userData);
      setLoading(false);
    } else {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Felaktigt lösenord eller e-postadress');
    }
  };

  const signupWithEmail = async (email: string, password: string, name: string) => {
    const response = await fetch(API_ENDPOINTS.AUTH.SIGNUP, {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, password, name }),
    });

    if (response.ok) {
      setLoading(true);
      const data = await response.json();
      const userData: User = {
        id: data.id || data.email || '',
        name: data.name || name,
        email: data.email || '',
        picture: data.picture,
        onboardingCompleted: data.onboardingCompleted || false,
      };
      setUser(userData);
      setLoading(false);
    } else {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Kunde inte skapa konto');
    }
  };

  const logout = async () => {
    // Chatten hanteras nu helt av backend — ingen localStorage-arkivering behövs
    try {
      await fetch(API_ENDPOINTS.AUTH.LOGOUT, {
        method: 'POST',
        credentials: 'include',
      });
      setUser(null);
      window.location.href = '/login';
    } catch {
      setUser(null);
    }
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, loginWithEmail, signupWithEmail, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth måste användas inom AuthProvider');
  }
  return context;
};
