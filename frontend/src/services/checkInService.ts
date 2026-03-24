import { API_ENDPOINTS, fetchWithCredentials } from "@/config/api";

export interface CheckInData {
  id: number;
  userId: string;
  checkInDate: string;
  moodScore: number;
  note: string | null;
  alreadyCheckedInToday: boolean;
}

export interface CheckInRequest {
  moodScore: number;
  note?: string;
}

export const checkInService = {
  /**
   * Checka in idag med humörpoäng (1–5) och valfri anteckning
   */
  checkIn: async (userId: string, request: CheckInRequest): Promise<CheckInData> => {
    return fetchWithCredentials(API_ENDPOINTS.CHECKINS.CHECK_IN(userId), {
      method: "POST",
      body: JSON.stringify(request),
    });
  },

  /**
   * Hämta dagens incheckning (null om inte gjord än idag)
   */
  getToday: async (userId: string): Promise<CheckInData | null> => {
    try {
      return await fetchWithCredentials(API_ENDPOINTS.CHECKINS.GET_TODAY(userId));
    } catch {
      return null;
    }
  },

  /**
   * Hämta incheckningshistorik för de senaste N dagarna
   */
  getHistory: async (userId: string, days = 30): Promise<CheckInData[]> => {
    return fetchWithCredentials(API_ENDPOINTS.CHECKINS.GET_HISTORY(userId, days));
  },
};
