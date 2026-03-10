import { API_ENDPOINTS } from "@/config/api";

// --- Typer ---

export type CrisisLevel = "NONE" | "ELEVATED" | "CRITICAL";

export interface CoachMessageRequest {
  message: string;
  sessionId: string | null;
}

export interface CoachMessageResponse {
  response: string;
  crisisLevel: CrisisLevel;
  sessionId: string;
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
  const response = await fetch(API_ENDPOINTS.COACH.MESSAGE(userId), {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
    },
    body: JSON.stringify({
      message,
      sessionId,
    } satisfies CoachMessageRequest),
  });

  if (response.status === 429) {
    throw new Error("insufficient_quota");
  }

  if (response.status === 500) {
    throw new Error("server_error");
  }

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }

  return response.json();
}

/**
 * Hämta status för AI Coach (vilka modeller som är online).
 */
export async function getCoachStatus(): Promise<CoachStatusResponse> {
  const response = await fetch(API_ENDPOINTS.COACH.STATUS, {
    method: "GET",
    credentials: "include",
    headers: {
      Accept: "application/json",
    },
  });

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }

  return response.json();
}
