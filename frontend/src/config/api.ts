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
    DELETE_ACCOUNT: `${API_BASE_URL}/auth/account`,
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
    GET_USER_CHALLENGES: (userId: string) => `${API_BASE_URL}/challenges/user/${encodeURIComponent(userId)}`,
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
  
  // Streak endpoints
  STREAKS: {
    GET_DATA: (userId: string, weeks?: number) =>
      `${API_BASE_URL}/streaks/${encodeURIComponent(userId)}${weeks ? `?weeks=${weeks}` : ''}`,
    LOG_ACTIVITY: (userId: string) =>
      `${API_BASE_URL}/streaks/${encodeURIComponent(userId)}/activity`,
  },

  // Reward endpoints
  REWARDS: {
    GET_TODAY: (userId: string) =>
      `${API_BASE_URL}/rewards/${encodeURIComponent(userId)}/today`,
    GENERATE: (userId: string) =>
      `${API_BASE_URL}/rewards/${encodeURIComponent(userId)}/generate`,
    CLAIM: (userId: string) =>
      `${API_BASE_URL}/rewards/${encodeURIComponent(userId)}/claim`,
    GET_COLLECTION: (userId: string) =>
      `${API_BASE_URL}/rewards/${encodeURIComponent(userId)}/collection`,
  },

  // Check-in endpoints
  CHECKINS: {
    CHECK_IN: (userId: string) => `${API_BASE_URL}/checkins/${encodeURIComponent(userId)}`,
    GET_TODAY: (userId: string) => `${API_BASE_URL}/checkins/${encodeURIComponent(userId)}/today`,
    GET_HISTORY: (userId: string, days?: number) =>
      `${API_BASE_URL}/checkins/${encodeURIComponent(userId)}/history${days ? `?days=${days}` : ''}`,
  },

  // Social endpoints (vänner, dueller, liga)
  SOCIAL: {
    FRIENDS: (userId: string) => `${API_BASE_URL}/social/friends?userId=${encodeURIComponent(userId)}`,
    FRIENDS_PENDING: (userId: string) => `${API_BASE_URL}/social/friends/pending?userId=${encodeURIComponent(userId)}`,
    FRIEND_REQUEST: (userId: string) => `${API_BASE_URL}/social/friends/request?userId=${encodeURIComponent(userId)}`,
    FRIEND_ACCEPT: (friendshipId: number, userId: string) => `${API_BASE_URL}/social/friends/${friendshipId}/accept?userId=${encodeURIComponent(userId)}`,
    FRIEND_DECLINE: (friendshipId: number, userId: string) => `${API_BASE_URL}/social/friends/${friendshipId}/decline?userId=${encodeURIComponent(userId)}`,
    FRIEND_REMOVE: (friendshipId: number, userId: string) => `${API_BASE_URL}/social/friends/${friendshipId}?userId=${encodeURIComponent(userId)}`,
    DUELS: (userId: string) => `${API_BASE_URL}/social/duels?userId=${encodeURIComponent(userId)}`,
    DUEL_CREATE: (userId: string) => `${API_BASE_URL}/social/duels?userId=${encodeURIComponent(userId)}`,
    DUEL_ACCEPT: (duelId: number, userId: string) => `${API_BASE_URL}/social/duels/${duelId}/accept?userId=${encodeURIComponent(userId)}`,
    DUEL_DECLINE: (duelId: number, userId: string) => `${API_BASE_URL}/social/duels/${duelId}/decline?userId=${encodeURIComponent(userId)}`,
    DUEL_COMPLETE: (duelId: number, userId: string) => `${API_BASE_URL}/social/duels/${duelId}/complete?userId=${encodeURIComponent(userId)}`,
    LEAGUE: (userId: string) => `${API_BASE_URL}/social/league?userId=${encodeURIComponent(userId)}`,
    LEAGUE_ME: (userId: string) => `${API_BASE_URL}/social/league/me?userId=${encodeURIComponent(userId)}`,
  },

  // AI Coach endpoints
  COACH: {
    // Ny Claude AI Coach-endpoint (primär)
    MESSAGE: (userId: string) => `${API_BASE_URL}/coach/message?userId=${encodeURIComponent(userId)}`,
    PERSONALIZED: (userId: string) => `${API_BASE_URL}/coach/personalized/${encodeURIComponent(userId)}`,
    STATUS: `${API_BASE_URL}/coach/status`,
    // Kvotstatus
    QUOTA: (userId: string) => `${API_BASE_URL}/coach/quota/${encodeURIComponent(userId)}`,
    // Multi-konversation
    SESSIONS: (userId: string) => `${API_BASE_URL}/coach/sessions?userId=${encodeURIComponent(userId)}`,
    SESSION_MESSAGES: (userId: string, sessionId: string) =>
      `${API_BASE_URL}/coach/sessions/${sessionId}/messages?userId=${encodeURIComponent(userId)}`,
    NEW_SESSION: (userId: string) => `${API_BASE_URL}/coach/sessions/new?userId=${encodeURIComponent(userId)}`,
    DELETE_SESSION: (sessionId: string, userId: string) => `${API_BASE_URL}/coach/sessions/${encodeURIComponent(sessionId)}?userId=${encodeURIComponent(userId)}`,
  },
};

/**
 * Custom API error med statuskod för bättre felhantering i komponenter.
 */
export class ApiError extends Error {
  status: number;
  constructor(message: string, status: number) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

/**
 * Läser CSRF-token från XSRF-TOKEN-cookien som Spring sätter.
 * Cookien är httpOnly: false så att JavaScript kan läsa den.
 */
function getCsrfToken(): string | null {
  const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
  return match ? decodeURIComponent(match[1]) : null;
}

/**
 * Standard fetch-konfiguration med credentials och CSRF-token.
 * Hanterar 401/403 automatiskt genom att redirecta till login.
 * Kastar ApiError med statuskod vid övriga fel.
 * Inkluderar X-XSRF-TOKEN-header automatiskt för alla mutationsanrop (POST, PUT, DELETE).
 */
export const fetchWithCredentials = async (url: string, options?: RequestInit) => {
  const csrfHeaders: Record<string, string> = {};
  const method = (options?.method || 'GET').toUpperCase();
  if (method !== 'GET' && method !== 'HEAD' && method !== 'OPTIONS') {
    const token = getCsrfToken();
    if (token) {
      csrfHeaders['X-XSRF-TOKEN'] = token;
    }
  }

  const response = await fetch(url, {
    ...options,
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...csrfHeaders,
      ...options?.headers,
    },
  });

  if (!response.ok) {
    // Session utgången → redirecta till login (bara vid 401, inte 403)
    if (response.status === 401) {
      const path = window.location.pathname;
      if (!path.startsWith('/login') && !path.startsWith('/reset-password')) {
        window.location.href = '/login';
      }
      throw new ApiError('Session expired', response.status);
    }

    const errorText = await response.text().catch(() => '');
    let message = errorText;
    try {
      const errorJson = JSON.parse(errorText);
      message = errorJson.error || errorJson.message || errorText;
    } catch {
      // errorText was not JSON, use as-is
    }

    throw new ApiError(message || `HTTP error! status: ${response.status}`, response.status);
  }

  // 204 No Content eller tom body — returnera null istället för att försöka parsa JSON
  if (response.status === 204 || response.headers.get("Content-Length") === "0") {
    return null;
  }
  const contentType = response.headers.get("Content-Type");
  if (!contentType || !contentType.includes("application/json")) {
    return null;
  }
  return response.json();
};

export { API_BASE_URL };
