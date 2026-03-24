// Reward-service — backend-endpoints implementeras i Sprint 2
// Dessa typer och stubs håller bygget grönt under tiden

export interface CollectibleData {
  id: string;
  name: string;
  emoji: string;
  unlockedAt: string;
}

export interface DailyRewardData {
  available: boolean;
  claimed: boolean;
  reward?: CollectibleData;
}

export const rewardService = {
  getToday: async (_userId: string): Promise<DailyRewardData> => {
    return { available: false, claimed: false };
  },
  claim: async (_userId: string): Promise<DailyRewardData> => {
    return { available: false, claimed: true };
  },
  getCollection: async (_userId: string): Promise<CollectibleData[]> => {
    return [];
  },
};
