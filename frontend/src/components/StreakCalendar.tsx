import { Card } from "@/components/ui/card";
import { StreakData } from "@/services/streakService";

interface StreakCalendarProps {
  streakData?: StreakData | null;
}

// Platshållare — implementeras i Sprint 2
const StreakCalendar = ({ streakData }: StreakCalendarProps) => (
  <Card className="p-4">
    <p className="text-sm font-medium mb-1">Streak-kalender</p>
    {streakData ? (
      <p className="text-2xl font-bold">{streakData.currentStreak} <span className="text-sm font-normal text-muted-foreground">dagar</span></p>
    ) : (
      <p className="text-xs text-muted-foreground">Ingen streak ännu — slutför en utmaning!</p>
    )}
  </Card>
);

export default StreakCalendar;
