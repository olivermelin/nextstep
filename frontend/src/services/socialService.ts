import { API_ENDPOINTS, fetchWithCredentials } from "@/config/api";

// --- Typer ---

export interface FriendDto {
  friendshipId: number;
  displayName: string;
  totalPoints: number;
  status: "PENDING" | "ACCEPTED" | "DECLINED";
  iRequested: boolean;
  friendshipSince: string | null;
}

export interface DuelDto {
  id: number;
  challengerName: string;
  challengedName: string;
  challengeId: number;
  challengeName: string;
  status: "PENDING" | "ACTIVE" | "COMPLETED" | "DECLINED" | "EXPIRED";
  winnerId: string | null;
  winnerName: string | null;
  iAmChallenger: boolean;
  iCompleted: boolean;
  opponentCompleted: boolean;
  createdAt: string;
  expiresAt: string;
  completedAt: string | null;
}

export interface LeagueEntryDto {
  rank: number;
  displayName: string;
  totalPoints: number;
  tier: "BRONZE" | "SILVER" | "GOLD" | "DIAMOND";
  isMe: boolean;
}

// --- Vänner ---

export async function getFriends(userId: string): Promise<FriendDto[]> {
  return fetchWithCredentials(API_ENDPOINTS.SOCIAL.FRIENDS(userId));
}

export async function getPendingRequests(userId: string): Promise<FriendDto[]> {
  return fetchWithCredentials(API_ENDPOINTS.SOCIAL.FRIENDS_PENDING(userId));
}

export async function sendFriendRequest(userId: string, targetEmail: string): Promise<FriendDto> {
  return fetchWithCredentials(API_ENDPOINTS.SOCIAL.FRIEND_REQUEST(userId), {
    method: "POST",
    body: JSON.stringify({ targetEmail }),
  });
}

export async function acceptFriendRequest(userId: string, friendshipId: number): Promise<FriendDto> {
  return fetchWithCredentials(API_ENDPOINTS.SOCIAL.FRIEND_ACCEPT(friendshipId, userId), {
    method: "POST",
  });
}

export async function declineFriendRequest(userId: string, friendshipId: number): Promise<FriendDto> {
  return fetchWithCredentials(API_ENDPOINTS.SOCIAL.FRIEND_DECLINE(friendshipId, userId), {
    method: "POST",
  });
}

export async function removeFriend(userId: string, friendshipId: number): Promise<void> {
  return fetchWithCredentials(API_ENDPOINTS.SOCIAL.FRIEND_REMOVE(friendshipId, userId), {
    method: "DELETE",
  });
}

// --- Dueller ---

export async function getDuels(userId: string): Promise<DuelDto[]> {
  return fetchWithCredentials(API_ENDPOINTS.SOCIAL.DUELS(userId));
}

export async function createDuel(
  userId: string,
  challengedEmail: string,
  challengeId: number,
  challengeName: string
): Promise<DuelDto> {
  return fetchWithCredentials(API_ENDPOINTS.SOCIAL.DUEL_CREATE(userId), {
    method: "POST",
    body: JSON.stringify({ challengedEmail, challengeId, challengeName }),
  });
}

export async function acceptDuel(userId: string, duelId: number): Promise<DuelDto> {
  return fetchWithCredentials(API_ENDPOINTS.SOCIAL.DUEL_ACCEPT(duelId, userId), {
    method: "POST",
  });
}

export async function declineDuel(userId: string, duelId: number): Promise<DuelDto> {
  return fetchWithCredentials(API_ENDPOINTS.SOCIAL.DUEL_DECLINE(duelId, userId), {
    method: "POST",
  });
}

export async function completeDuel(userId: string, duelId: number): Promise<DuelDto> {
  return fetchWithCredentials(API_ENDPOINTS.SOCIAL.DUEL_COMPLETE(duelId, userId), {
    method: "POST",
  });
}

// --- Liga ---

export async function getLeaderboard(userId: string): Promise<LeagueEntryDto[]> {
  return fetchWithCredentials(API_ENDPOINTS.SOCIAL.LEAGUE(userId));
}

export async function getMyLeagueEntry(userId: string): Promise<LeagueEntryDto> {
  return fetchWithCredentials(API_ENDPOINTS.SOCIAL.LEAGUE_ME(userId));
}
