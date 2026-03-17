import { API_ENDPOINTS, fetchWithCredentials, ApiError } from "@/config/api";

// --- Typer ---

export type CrisisLevel = "NONE" | "ELEVATED" | "CRITICAL";

export interface CoachMessageRequest {
  message: string;
  sessionId: string | null;
}

export interface SuggestedChallenge {
  id: number;
  title: string;
  description: string;
  durationMinutes: number;
  difficulty: string;
  category: string;
  url: string;
}

export interface CoachMessageResponse {
  response: string;
  crisisLevel: CrisisLevel;
  sessionId: string;
  suggestedChallenges: SuggestedChallenge[];
}

export interface CoachStatusResponse {
  aiAvailable: boolean;
  openai: "online" | "offline";
  claude: "online" | "offline";
  service: string;
  status: string;
}

// --- API-funktioner ---

/**
 * Skicka meddelande till Claude AI Coach.
 * Skickar med sessionId för att bevara konversationshistorik (20-meddelandefönster).
 */
export async function sendCoachMessage(
  userId: string,
  message: string,
  sessionId: string | null
): Promise<CoachMessageResponse> {
  try {
    return await fetchWithCredentials(API_ENDPOINTS.COACH.MESSAGE(userId), {
      method: "POST",
      body: JSON.stringify({
        message,
        sessionId,
      } satisfies CoachMessageRequest),
    });
  } catch (error) {
    if (error instanceof ApiError && error.status === 429) {
      throw new Error("insufficient_quota");
    }
    throw error;
  }
}

/**
 * Hämta status för AI Coach (vilka modeller som är online).
 */
export async function getCoachStatus(): Promise<CoachStatusResponse> {
  return fetchWithCredentials(API_ENDPOINTS.COACH.STATUS);
}
