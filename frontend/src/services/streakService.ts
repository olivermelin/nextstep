import { API_ENDPOINTS, fetchWithCredentials } from "@/config/api";

export interface StreakData {
  userId: string;
  currentStreak: number;
  longestStreak: number;
  lastActivityDate: string | null;
  streakStartDate: string | null;
  streakActive: boolean;
  activityRegisteredToday: boolean;
}

export const streakService = {
  /**
   * Hämta streak-data för användaren
   */
  getStreak: async (userId: string): Promise<StreakData> => {
    return fetchWithCredentials(API_ENDPOINTS.STREAKS.GET_DATA(userId));
  },

  /**
   * Registrera aktivitet manuellt (triggas normalt automatiskt vid slutförd challenge)
   */
  registerActivity: async (userId: string): Promise<StreakData> => {
    return fetchWithCredentials(API_ENDPOINTS.STREAKS.LOG_ACTIVITY(userId), {
      method: "POST",
    });
  },
};
