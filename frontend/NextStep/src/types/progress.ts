import { LucideIcon } from "lucide-react";

export interface CategoryProgress {
  id: string;
  name: string;
  completedDays: number;
  totalDays: number;
  icon: LucideIcon;
  color: string;
  borderColor: string;
  bgColor: string;
}

export interface Achievement {
  id: number;
  title: string;
  description: string;
  icon: LucideIcon;
  unlocked: boolean;
  points: number;
}

export interface UserProgress {
  totalPoints: number;
  level: number;
  categoryProgress: Omit<CategoryProgress, 'icon' | 'color' | 'borderColor' | 'bgColor'>[];
  achievements: Omit<Achievement, 'icon'>[];
}

export interface ProgressStats {
  label: string;
  value: number;
  color: string;
}
