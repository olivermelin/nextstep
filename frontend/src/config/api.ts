/**
 * Centraliserad API-konfiguration
 * Alla API-anrop ska använda denna konfiguration istället för hårdkodade URLs
 */

const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

export const API_ENDPOINTS = {
  // Auth endpoints
  AUTH: {
    ME: `${API_BASE_URL}/auth/me`,
    LOGIN: `${API_BASE_URL}/auth/login`,
    SIGNUP: `${API_BASE_URL}/auth/signup`,
    LOGOUT: `${API_BASE_URL}/auth/logout`,
    FORGOT_PASSWORD: `${API_BASE_URL}/auth/forgot-password`,
    RESET_PASSWORD: `${API_BASE_URL}/auth/reset-password`,
    OAUTH_GOOGLE: `${API_BASE_URL.replace('/api', '')}/oauth2/authorization/google`,
  },
  
  // Progress endpoints
  PROGRESS: {
    GET_USER_PROGRESS: (userId: string) => `${API_BASE_URL}/progress/${encodeURIComponent(userId)}`,
    GET_CATEGORY_PROGRESS: (userId: string, days?: number) => 
      `${API_BASE_URL}/progress/${encodeURIComponent(userId)}/categories${days ? `?days=${days}` : ''}`,
    GET_ACHIEVEMENTS: (userId: string) => `${API_BASE_URL}/progress/${encodeURIComponent(userId)}/achievements`,
    ADD_POINTS: (userId: string, points: number) => `${API_BASE_URL}/progress/${encodeURIComponent(userId)}/add?points=${points}`,
  },
  
  // Challenge endpoints
  CHALLENGES: {
    BASE: `${API_BASE_URL}/challenges`,
    GET_BY_ID: (id: number) => `${API_BASE_URL}/challenges/${id}`,
    GET_BY_CATEGORY: (category: string) => `${API_BASE_URL}/challenges/category/${category}`,
    GET_BY_DIFFICULTY: (difficulty: string) => `${API_BASE_URL}/challenges/difficulty/${difficulty}`,
    GET_BY_CATEGORY_AND_DIFFICULTY: (category: string, difficulty: string) => 
      `${API_BASE_URL}/challenges/category/${category}/difficulty/${difficulty}`,
  },
  
  // User Challenge endpoints
  USER_CHALLENGES: {
    BASE: `${API_BASE_URL}/user-challenges`,
    GET_BY_USER: (userId: string) => `${API_BASE_URL}/user-challenges/user/${encodeURIComponent(userId)}`,
    GET_COMPLETED: (userId: string) => `${API_BASE_URL}/user-challenges/user/${encodeURIComponent(userId)}/completed`,
    GET_ACTIVE: (userId: string) => `${API_BASE_URL}/user-challenges/user/${encodeURIComponent(userId)}/active`,
    START: (userId: string, challengeId: number) => `${API_BASE_URL}/user-challenges/user/${encodeURIComponent(userId)}/challenge/${challengeId}/start`,
    COMPLETE: (userId: string, challengeId: number) => `${API_BASE_URL}/user-challenges/user/${encodeURIComponent(userId)}/complete/${challengeId}`,
  },
  
  // Onboarding endpoints
  ONBOARDING: {
    COMPLETE: (userId: string) => `${API_BASE_URL}/onboarding/complete/${encodeURIComponent(userId)}`,
    STATUS: (userId: string) => `${API_BASE_URL}/onboarding/status/${encodeURIComponent(userId)}`,
  },
  
  // Settings endpoints
  SETTINGS: {
    GET_USER_SETTINGS: (userId: string) => `${API_BASE_URL}/settings/${encodeURIComponent(userId)}`,
    UPDATE_USER_SETTINGS: (userId: string) => `${API_BASE_URL}/settings/${encodeURIComponent(userId)}`,
  },
  
  // AI Coach endpoints
  COACH: {
    // Ny Claude AI Coach-endpoint (primär)
    MESSAGE: (userId: string) => `${API_BASE_URL}/coach/message?userId=${encodeURIComponent(userId)}`,
    STATUS: `${API_BASE_URL}/coach/status`,
    // Bakåtkompatibla endpoints (kan fasas ut)
    MOTIVATE: (message: string) => `${API_BASE_URL}/coach/motivate?message=${encodeURIComponent(message)}`,
  },
};

/**
 * Standard fetch-konfiguration med credentials
 */
export const fetchWithCredentials = async (url: string, options?: RequestInit) => {
  const response = await fetch(url, {
    ...options,
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...options?.headers,
    },
  });

  if (!response.ok) {
    const error = await response.text();
    throw new Error(error || `HTTP error! status: ${response.status}`);
  }

  return response.json();
};

export { API_BASE_URL };
