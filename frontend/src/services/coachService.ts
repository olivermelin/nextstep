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
      throw new Error("quota_exceeded");
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

// --- Kvot ---

export interface QuotaInfo {
  used: number;
  remaining: number;
  dailyLimit: number;
  isPremium: boolean;
  resetAt: string;
}

/**
 * Hämta kvotstatus för användaren (använda/kvar/gräns).
 */
export async function getQuota(userId: string): Promise<QuotaInfo> {
  return fetchWithCredentials(API_ENDPOINTS.COACH.QUOTA(userId));
}

// --- Multi-konversation ---

export interface SessionSummary {
  sessionId: string;
  status: "ACTIVE" | "CLOSED";
  preview: string;
  createdAt: string;
  lastMessageAt: string;
  messageCount: number;
}

export interface SessionMessage {
  role: "user" | "assistant";
  content: string;
  crisisLevel: string;
  timestamp: string;
}

export interface SessionMessages {
  sessionId: string;
  status: string;
  createdAt: string;
  messages: SessionMessage[];
}

/**
 * Hämta alla konversationer för en användare.
 */
export async function getCoachSessions(userId: string): Promise<SessionSummary[]> {
  return fetchWithCredentials(API_ENDPOINTS.COACH.SESSIONS(userId));
}

/**
 * Hämta alla meddelanden för en specifik konversation.
 */
export async function getSessionMessages(userId: string, sessionId: string): Promise<SessionMessages> {
  return fetchWithCredentials(API_ENDPOINTS.COACH.SESSION_MESSAGES(userId, sessionId));
}

/**
 * Hämta ett dagligt motivationstips utan att spara till konversationshistoriken.
 * Använder /coach/personalized som är stateless och påverkar inte AI Coach-sessioner.
 */
export async function getDailyCoachTip(userId: string, message: string): Promise<string> {
  const data = await fetchWithCredentials(API_ENDPOINTS.COACH.PERSONALIZED(userId), {
    method: "POST",
    body: JSON.stringify({ message }),
  }) as { response: string };
  return data.response;
}

/**
 * Ta bort en konversation och alla dess meddelanden permanent.
 */
export async function deleteCoachSession(userId: string, sessionId: string): Promise<void> {
  await fetchWithCredentials(API_ENDPOINTS.COACH.DELETE_SESSION(sessionId, userId), {
    method: "DELETE",
  });
}

/**
 * Skapa en ny konversation (stänger aktiv).
 */
export async function createNewSession(userId: string): Promise<SessionSummary> {
  return fetchWithCredentials(API_ENDPOINTS.COACH.NEW_SESSION(userId), {
    method: "POST",
  });
}
