import { API_ENDPOINTS, fetchWithCredentials } from "@/config/api";

export type RewardType = 'BONUS_XP' | 'QUOTE_CARD' | 'STREAK_FREEZE' | 'AVATAR_FRAME';

export interface DailyRewardDto {
  id: number;
  rewardDate: string;
  rewardType: RewardType;
  rewardValue: string; // JSON-sträng, t.ex. {"xp":25} eller {"quoteId":"q3","quote":"...","xp":25}
  claimed: boolean;
  consecutiveDays: number;
  rare: boolean;
}

export interface RewardValue {
  xp?: number;
  quoteId?: string;
  quote?: string;
}

export interface CollectibleData {
  collectibleKey: string;
  collectibleType: RewardType;
  unlockedAt: string;
}

export const rewardService = {
  /** Hämta dagens belöning (null om ingen genererats ännu) */
  getToday: async (userId: string): Promise<DailyRewardDto | null> => {
    return fetchWithCredentials(API_ENDPOINTS.REWARDS.GET_TODAY(userId));
  },

  /** Generera dagens belöning (idempotent) */
  generate: async (userId: string): Promise<DailyRewardDto> => {
    return fetchWithCredentials(API_ENDPOINTS.REWARDS.GENERATE(userId), {
      method: 'POST',
    });
  },

  /** Hämta belöningen — ger XP och sparar collectible */
  claim: async (userId: string): Promise<DailyRewardDto> => {
    return fetchWithCredentials(API_ENDPOINTS.REWARDS.CLAIM(userId), {
      method: 'POST',
    });
  },

  /** Hämta användarens samling av collectibles */
  getCollection: async (userId: string): Promise<CollectibleData[]> => {
    return fetchWithCredentials(API_ENDPOINTS.REWARDS.GET_COLLECTION(userId));
  },

  /** Parsa rewardValue JSON-strängen säkert */
  parseRewardValue: (rewardValue: string): RewardValue => {
    try {
      return JSON.parse(rewardValue);
    } catch {
      return {};
    }
  },
};
