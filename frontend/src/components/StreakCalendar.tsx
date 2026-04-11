import { Card } from "@/components/ui/card";
import { StreakData } from "@/services/streakService";
import { Flame, TrendingUp } from "lucide-react";
import { useTranslation } from 'react-i18next';

interface StreakCalendarProps {
  streakData?: StreakData | null;
}

const StreakCalendar = ({ streakData }: StreakCalendarProps) => {
  const { t } = useTranslation();

  if (!streakData) return null;

  const { currentStreak, longestStreak, streakActive, activityRegisteredToday } = streakData;

  return (
    <Card className="p-4">
      <div className="flex items-center gap-2 mb-3">
        <Flame className={`w-5 h-5 ${streakActive ? 'text-orange-500' : 'text-muted-foreground'}`} />
        <p className="text-sm font-semibold text-foreground">{t('streak.title')}</p>
        {activityRegisteredToday && (
          <span className="ml-auto text-xs text-primary font-medium">✓ {t('streak.todayDone')}</span>
        )}
      </div>

      <div className="grid grid-cols-2 gap-4">
        {/* Nuvarande streak */}
        <div>
          <p className={`text-4xl font-bold ${streakActive ? 'text-orange-500' : 'text-foreground'}`}>
            {currentStreak}
          </p>
          <p className="text-xs text-muted-foreground mt-0.5">{t('streak.currentDays')}</p>
        </div>

        {/* Längsta streak */}
        <div className="border-l border-border pl-4">
          <div className="flex items-center gap-1 mb-0.5">
            <TrendingUp className="w-3.5 h-3.5 text-primary" />
          </div>
          <p className="text-4xl font-bold text-primary">{longestStreak}</p>
          <p className="text-xs text-muted-foreground mt-0.5">{t('streak.longestDays')}</p>
        </div>
      </div>
    </Card>
  );
};

export default StreakCalendar;
