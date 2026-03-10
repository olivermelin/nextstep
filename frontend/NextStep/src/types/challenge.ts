// Enums matchar backend exakt
export enum ChallengeCategory {
  MENTAL_HEALTH = "MENTAL_HEALTH",
  PHYSICAL_ACTIVITY = "PHYSICAL_ACTIVITY",
  FOCUS_DISCIPLINE = "FOCUS_DISCIPLINE",
  PERSONAL_DEVELOPMENT = "PERSONAL_DEVELOPMENT",
  DRAWING_EXERCISES = "DRAWING_EXERCISES"
}

export enum ChallengeDifficulty {
  EASY = "EASY",
  MEDIUM = "MEDIUM",
  HARD = "HARD"
}

export enum ChallengeStatus {
  NOT_STARTED = "NOT_STARTED",
  IN_PROGRESS = "IN_PROGRESS",
  COMPLETED = "COMPLETED"
}

// DTO för Challenge från backend - matchar exakt backend response
export interface ChallengeOutDto {
  id: number;
  title: string;
  description: string;
  category: ChallengeCategory;
  difficulty: ChallengeDifficulty;
  durationMinutes: number;
  youtubeUrl?: string | null;
  instructions?: string | null;
  createdAt?: string;
}

// DTO för UserChallenge från backend
export interface UserChallengeOutDto {
  id: number;
  challengeId: number;
  challengeName: string;
  category: ChallengeCategory;
  difficulty: ChallengeDifficulty;
  durationMinutes: number;
  status: ChallengeStatus;
  startedAt?: string;
  completedAt?: string;
  progress: number;
}
