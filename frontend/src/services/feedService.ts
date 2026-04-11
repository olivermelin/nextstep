import { API_ENDPOINTS, fetchWithCredentials } from "@/config/api";

export interface FeedReactionDto {
  id: number;
  displayName: string;
}

export interface FeedItemDto {
  id: number;
  userId: string;
  displayName: string;
  type: "CHALLENGE_COMPLETED" | "STREAK_MILESTONE" | "LEVEL_UP" | "DUEL_WON" | "ACHIEVEMENT_UNLOCKED";
  message: string;
  metadata: string | null;
  createdAt: string;
  cheerCount: number;
  iCheered: boolean;
  recentCheers: FeedReactionDto[];
}

export interface FeedPageDto {
  items: FeedItemDto[];
  nextCursor: number | null;
  hasMore: boolean;
}

export interface CheerResponse {
  cheerCount: number;
  iCheered: boolean;
}

export const feedService = {
  getFeed: (userId: string, cursor?: number): Promise<FeedPageDto> =>
    fetchWithCredentials(API_ENDPOINTS.SOCIAL.FEED(userId, cursor)),

  toggleCheer: (userId: string, itemId: number): Promise<CheerResponse> =>
    fetchWithCredentials(API_ENDPOINTS.SOCIAL.FEED_CHEER(itemId, userId), {
      method: "POST",
    }),

  deleteFeedItem: (userId: string, itemId: number): Promise<void> =>
    fetchWithCredentials(API_ENDPOINTS.SOCIAL.FEED_DELETE(itemId, userId), {
      method: "DELETE",
    }),
};
