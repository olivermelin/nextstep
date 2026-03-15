import {
  ChallengeOutDto,
  UserChallengeOutDto,
  ChallengeCategory,
  ChallengeDifficulty,
} from "@/types/challenge";
import { API_ENDPOINTS, fetchWithCredentials } from "@/config/api";

/* ===================== CHALLENGE API ===================== */

export const challengeApi = {
  // Hämta alla challenges
  getAllChallenges: async (): Promise<ChallengeOutDto[]> => {
    return fetchWithCredentials(API_ENDPOINTS.CHALLENGES.BASE);
  },

  // Hämta specifik challenge
  getChallengeById: async (id: number): Promise<ChallengeOutDto> => {
    return fetchWithCredentials(API_ENDPOINTS.CHALLENGES.GET_BY_ID(id));
  },

  // Hämta challenges per kategori
  getChallengesByCategory: async (
    category: ChallengeCategory
  ): Promise<ChallengeOutDto[]> => {
    return fetchWithCredentials(API_ENDPOINTS.CHALLENGES.GET_BY_CATEGORY(category));
  },

  // Hämta challenges per svårighetsgrad
  getChallengesByDifficulty: async (
    difficulty: ChallengeDifficulty
  ): Promise<ChallengeOutDto[]> => {
    return fetchWithCredentials(API_ENDPOINTS.CHALLENGES.GET_BY_DIFFICULTY(difficulty));
  },

  // Hämta challenges per kategori och svårighetsgrad
  getChallengesByCategoryAndDifficulty: async (
    category: ChallengeCategory,
    difficulty: ChallengeDifficulty
  ): Promise<ChallengeOutDto[]> => {
    return fetchWithCredentials(
      API_ENDPOINTS.CHALLENGES.GET_BY_CATEGORY_AND_DIFFICULTY(category, difficulty)
    );
  },

  // Hämta challenges med completedToday-status för en specifik användare
  getUserChallenges: async (userId: string): Promise<ChallengeOutDto[]> => {
    return fetchWithCredentials(API_ENDPOINTS.CHALLENGES.GET_USER_CHALLENGES(userId));
  },
};

/* ===================== USER CHALLENGE API ===================== */

export const userChallengeApi = {
  // Hämta alla användarens challenges
  getUserChallenges: async (userId: string): Promise<UserChallengeOutDto[]> => {
    return fetchWithCredentials(API_ENDPOINTS.USER_CHALLENGES.GET_BY_USER(userId));
  },

  // Hämta slutförda challenges
  getUserCompletedChallenges: async (
    userId: string
  ): Promise<UserChallengeOutDto[]> => {
    return fetchWithCredentials(API_ENDPOINTS.USER_CHALLENGES.GET_COMPLETED(userId));
  },

  // Hämta pågående challenges
  getUserActiveChallenges: async (
    userId: string
  ): Promise<UserChallengeOutDto[]> => {
    return fetchWithCredentials(API_ENDPOINTS.USER_CHALLENGES.GET_ACTIVE(userId));
  },

  // Starta en challenge
  startChallenge: async (
    userId: string,
    challengeId: number
  ): Promise<UserChallengeOutDto> => {
    return fetchWithCredentials(
      API_ENDPOINTS.USER_CHALLENGES.START(userId, challengeId),
      {
        method: "POST",
      }
    );
  },

  // Slutför en challenge
  completeChallenge: async (
    userId: string,
    challengeId: number
  ): Promise<UserChallengeOutDto> => {
    return fetchWithCredentials(
      API_ENDPOINTS.USER_CHALLENGES.COMPLETE(userId, challengeId),
      {
        method: "POST",
      }
    );
  },
};
