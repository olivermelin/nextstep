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
      console.log("AuthContext: Detekterade OAuth-redirect, väntar på session...");
      setTimeout(() => {
        loadUser();
      }, 500);
    }
  }, []);

  const loadUser = async () => {
    setLoading(true);
    try {
      console.log("AuthContext: Hämtar användarinfo från /api/auth/me...");
      
      const response = await fetch(API_ENDPOINTS.AUTH.ME, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json',
        },
      });

      console.log("AuthContext: HTTP Status:", response.status);

      if (response.ok) {
        const data = await response.json();
        console.log("AuthContext: Användardata mottagen:", data);
        console.log("AuthContext: onboardingCompleted värde från backend:", data.onboardingCompleted);
        
        // Verifiera att vi har nödvändig data
        if (data && (data.id || data.email)) {
          const userData: User = {
            id: data.id || data.sub || data.email || '',
            name: data.name || data.given_name || 'Användare',
            email: data.email || '',
            picture: data.picture,
            onboardingCompleted: data.onboardingCompleted || false,
          };
          console.log("AuthContext: Användare satt:", userData.email);
          console.log("AuthContext: onboardingCompleted satt till:", userData.onboardingCompleted);
          setUser(userData);
        } else {
          console.log("AuthContext: Data saknar id/email, sätter user till null");
          setUser(null);
        }
      } else if (response.status === 401 || response.status === 403) {
        console.log("AuthContext: Inte autentiserad (401/403)");
        setUser(null);
      } else {
        console.warn("AuthContext: Oväntat HTTP-status:", response.status);
        setUser(null);
      }
    } catch (error) {
      console.error("AuthContext: Nätverksfel:", error);
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  const refreshUser = async () => {
    await loadUser();
  };

  const login = () => {
    console.log("AuthContext: Startar Google OAuth...");
    // Backend bör redirecta tillbaka till http://localhost:3000/ efter inloggning
    window.location.href = API_ENDPOINTS.AUTH.OAUTH_GOOGLE;
  };

  const loginWithEmail = async (email: string, password: string) => {
    try {
      console.log("AuthContext: Loggar in med email...");
      
      const response = await fetch(API_ENDPOINTS.AUTH.LOGIN, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
      });

      console.log("AuthContext: Inloggnings-svar status:", response.status);

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
        console.log("AuthContext: Användare inloggad:", userData.email);
        setUser(userData);
        setLoading(false);
      } else {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Felaktigt lösenord eller e-postadress');
      }
    } catch (error) {
      console.error("AuthContext: Fel vid email-inloggning:", error);
      throw error;
    }
  };

  const signupWithEmail = async (email: string, password: string, name: string) => {
    try {
      console.log("AuthContext: Skapar konto med email...");
      
      const response = await fetch(API_ENDPOINTS.AUTH.SIGNUP, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password, name }),
      });

      console.log("AuthContext: Signup-svar status:", response.status);

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
        console.log("AuthContext: Användare skapad och inloggad:", userData.email);
        setUser(userData);
        setLoading(false);
      } else {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Kunde inte skapa konto');
      }
    } catch (error) {
      console.error("AuthContext: Fel vid registrering:", error);
      throw error;
    }
  };

  const logout = async () => {
    try {
      console.log("AuthContext: Loggar ut...");
      await fetch(API_ENDPOINTS.AUTH.LOGOUT, {
        method: 'POST',
        credentials: 'include',
      });
      setUser(null);
      window.location.href = '/login';
    } catch (error) {
      console.error('AuthContext: Fel vid utloggning:', error);
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
