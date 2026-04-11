// Onboarding-relaterade typer och enums

export enum UserGoal {
  DAILY_STABILITY = "DAILY_STABILITY",
  BETTER_ROUTINES = "BETTER_ROUTINES",
  REDUCE_SUBSTANCES = "REDUCE_SUBSTANCES",
  MENTAL_HEALTH = "MENTAL_HEALTH",
  STRUCTURE_MOTIVATION = "STRUCTURE_MOTIVATION",
  STAY_CLEAN = "STAY_CLEAN",
  OTHER = "OTHER"
}

export enum Gender {
  MALE = "MALE",
  FEMALE = "FEMALE",
  NON_BINARY = "NON_BINARY",
  PREFER_NOT_TO_SAY = "PREFER_NOT_TO_SAY"
}

export enum CurrentSituation {
  IN_TREATMENT = "IN_TREATMENT",
  RECENTLY_COMPLETED = "RECENTLY_COMPLETED",
  STRUGGLING_NOT_IN_TREATMENT = "STRUGGLING_NOT_IN_TREATMENT",
  SUPPORTING_SOMEONE = "SUPPORTING_SOMEONE",
  PREFER_NOT_TO_SAY = "PREFER_NOT_TO_SAY"
}

export enum SubstanceType {
  ALCOHOL = "ALCOHOL",
  CANNABIS = "CANNABIS",
  STIMULANTS = "STIMULANTS",
  OPIOIDS = "OPIOIDS",
  PRESCRIPTION_MEDS = "PRESCRIPTION_MEDS",
  GAMBLING = "GAMBLING",
  OTHER = "OTHER",
  PREFER_NOT_TO_SAY = "PREFER_NOT_TO_SAY"
}

export enum RecoveryStage {
  ACTIVE_USE = "ACTIVE_USE",
  TRYING_TO_QUIT = "TRYING_TO_QUIT",
  ONE_TO_SEVEN_DAYS = "ONE_TO_SEVEN_DAYS",
  ONE_TO_FOUR_WEEKS = "ONE_TO_FOUR_WEEKS",
  ONE_TO_SIX_MONTHS = "ONE_TO_SIX_MONTHS",
  SIX_PLUS_MONTHS = "SIX_PLUS_MONTHS",
  LONG_TERM_RECOVERY = "LONG_TERM_RECOVERY"
}

export interface BackgroundInfo {
  age?: number;
  gender?: Gender;
  currentSituation?: CurrentSituation;
  substanceHistory?: SubstanceType[];
}

export enum OnboardingTrack {
  CONSUMER = "CONSUMER",
  RECOVERY = "RECOVERY"
}

export interface OnboardingData {
  onboardingTrack: OnboardingTrack;
  userGoals: UserGoal[];
  otherGoal?: string;
  backgroundInfo?: BackgroundInfo;
  recoveryStage?: RecoveryStage;
  /** Samtycke till att AI-coachen analyserar beteendemönster */
  aiAnalysisConsent?: boolean;
  /** Vald coach-personlighet */
  coachPersonality?: "SUPPORTIVE" | "MOTIVATIONAL" | "REFLECTIVE" | "DIRECT";
}

export interface OnboardingResponse {
  success: boolean;
  message?: string;
}
