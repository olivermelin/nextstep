import {
  Brain,
  Activity,
  Focus,
  Lightbulb,
  Pencil,
} from "lucide-react";
import {
  ChallengeCategory,
  ChallengeDifficulty,
} from "@/types/challenge";
import type { LucideIcon } from "lucide-react";

// Mapping between URL slugs and backend categories
export const categorySlugMap: Record<string, ChallengeCategory> = {
  "mental-health": ChallengeCategory.MENTAL_HEALTH,
  "physical-activity": ChallengeCategory.PHYSICAL_ACTIVITY,
  "focus-discipline": ChallengeCategory.FOCUS_DISCIPLINE,
  "personal-development": ChallengeCategory.PERSONAL_DEVELOPMENT,
  "drawing-exercises": ChallengeCategory.DRAWING_EXERCISES,
};

export const categoryToSlug: Record<ChallengeCategory, string> = {
  [ChallengeCategory.MENTAL_HEALTH]: "mental-health",
  [ChallengeCategory.PHYSICAL_ACTIVITY]: "physical-activity",
  [ChallengeCategory.FOCUS_DISCIPLINE]: "focus-discipline",
  [ChallengeCategory.PERSONAL_DEVELOPMENT]: "personal-development",
  [ChallengeCategory.DRAWING_EXERCISES]: "drawing-exercises",
};

export interface CategoryConfig {
  id: ChallengeCategory;
  nameKey: string;
  icon: LucideIcon;
  color: string;
  borderColor: string;
  bgColor: string;
}

export const categoryConfigs: CategoryConfig[] = [
  {
    id: ChallengeCategory.MENTAL_HEALTH,
    nameKey: "challenges.categories.mentalHealth",
    icon: Brain,
    color: "text-blue-500",
    borderColor: "border-blue-500/30",
    bgColor: "from-blue-500/10 to-blue-500/5",
  },
  {
    id: ChallengeCategory.PHYSICAL_ACTIVITY,
    nameKey: "challenges.categories.physicalActivity",
    icon: Activity,
    color: "text-red-500",
    borderColor: "border-red-500/30",
    bgColor: "from-red-500/10 to-red-500/5",
  },
  {
    id: ChallengeCategory.FOCUS_DISCIPLINE,
    nameKey: "challenges.categories.focusDiscipline",
    icon: Focus,
    color: "text-purple-500",
    borderColor: "border-purple-500/30",
    bgColor: "from-purple-500/10 to-purple-500/5",
  },
  {
    id: ChallengeCategory.PERSONAL_DEVELOPMENT,
    nameKey: "challenges.categories.personalDevelopment",
    icon: Lightbulb,
    color: "text-amber-500",
    borderColor: "border-amber-500/30",
    bgColor: "from-amber-500/10 to-amber-500/5",
  },
  {
    id: ChallengeCategory.DRAWING_EXERCISES,
    nameKey: "challenges.categories.drawingExercises",
    icon: Pencil,
    color: "text-teal-500",
    borderColor: "border-teal-500/30",
    bgColor: "from-teal-500/10 to-teal-500/5",
  },
];

export const getDifficultyLabel = (difficulty: ChallengeDifficulty, t: (key: string) => string): string => {
  switch (difficulty) {
    case ChallengeDifficulty.EASY:
      return t("challenges.difficulty.easy");
    case ChallengeDifficulty.MEDIUM:
      return t("challenges.difficulty.medium");
    case ChallengeDifficulty.HARD:
      return t("challenges.difficulty.hard");
    default:
      return difficulty;
  }
};

export const getDifficultyColor = (difficulty: ChallengeDifficulty): string => {
  switch (difficulty) {
    case ChallengeDifficulty.EASY:
      return "bg-green-500/20 text-green-700 border-green-500/30";
    case ChallengeDifficulty.MEDIUM:
      return "bg-amber-500/20 text-amber-700 border-amber-500/30";
    case ChallengeDifficulty.HARD:
      return "bg-red-500/20 text-red-700 border-red-500/30";
    default:
      return "";
  }
};

export const getCategoryKey = (category: string): string => {
  const map: Record<string, string> = {
    MENTAL_HEALTH: "mentalHealth",
    PHYSICAL_ACTIVITY: "physicalActivity",
    FOCUS_DISCIPLINE: "focusDiscipline",
    PERSONAL_DEVELOPMENT: "personalDevelopment",
    DRAWING_EXERCISES: "drawingExercises",
  };
  return map[category] || category;
};

export const isDrawingCategory = (category: ChallengeCategory): boolean => {
  return category === ChallengeCategory.DRAWING_EXERCISES;
};

export const getYouTubeEmbedUrl = (url: string | null | undefined): string | null => {
  if (!url) return null;
  const videoIdMatch = url.match(/(?:youtube\.com\/watch\?v=|youtu\.be\/)([^&]+)/);
  if (videoIdMatch) {
    return `https://www.youtube.com/embed/${videoIdMatch[1]}?enablejsapi=1&origin=${window.location.origin}`;
  }
  return url;
};
