import { StreakData } from "@/services/streakService";

interface StreakBadgesProps {
  streakData?: StreakData | null;
}

const MILESTONES = [3, 7, 14, 30, 60, 100];

// Platshållare — implementeras i Sprint 2 med animerade badges
const StreakBadges = ({ streakData }: StreakBadgesProps) => {
  const current = streakData?.longestStreak ?? 0;
  return (
    <div className="flex gap-2 flex-wrap">
      {MILESTONES.map((m) => (
        <span
          key={m}
          className={`text-xs px-2 py-1 rounded-full border ${
            current >= m
              ? "bg-primary/10 border-primary text-primary"
              : "bg-muted border-transparent text-muted-foreground opacity-40"
          }`}
        >
          {m} dagar
        </span>
      ))}
    </div>
  );
};

export default StreakBadges;
