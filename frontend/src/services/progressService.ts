import { UserProgress } from "@/types/progress";
import { API_ENDPOINTS, fetchWithCredentials } from "@/config/api";

export const progressService = {
  /**
   * Hämta användarens framsteg (kombinerar data från flera endpoints)
   */
  getUserProgress: async (userId: string): Promise<UserProgress> => {
    try {
      // Hämta all data parallellt
      const [progressData, categoryData, achievementsData] = await Promise.all([
        fetchWithCredentials(API_ENDPOINTS.PROGRESS.GET_USER_PROGRESS(userId)),
        fetchWithCredentials(API_ENDPOINTS.PROGRESS.GET_CATEGORY_PROGRESS(userId, 7)).catch(() => ({ categories: [] })),
        fetchWithCredentials(API_ENDPOINTS.PROGRESS.GET_ACHIEVEMENTS(userId)).catch(() => []),
      ]);

      // Kombinera resultaten
      return {
        totalPoints: progressData.totalPoints || 0,
        level: progressData.level || 1,
        categoryProgress: categoryData.categories || categoryData || [],
        achievements: achievementsData || [],
      };
    } catch (error) {
      throw error;
    }
  },

  /**
   * Hämta kategoriframsteg för en viss period
   */
  getCategoryProgress: async (userId: string, days: number = 7): Promise<UserProgress['categoryProgress']> => {
    try {
      const data = await fetchWithCredentials(API_ENDPOINTS.PROGRESS.GET_CATEGORY_PROGRESS(userId, days));
      return data.categories || data || [];
    } catch (error) {
      throw error;
    }
  },

  /**
   * Hämta användarens achievements
   */
  getAchievements: async (userId: string): Promise<UserProgress['achievements']> => {
    try {
      return await fetchWithCredentials(API_ENDPOINTS.PROGRESS.GET_ACHIEVEMENTS(userId));
    } catch (error) {
      throw error;
    }
  },
};
