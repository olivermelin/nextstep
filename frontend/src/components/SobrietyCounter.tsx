import { useState, useEffect, useMemo } from "react";
import { Card } from "@/components/ui/card";
import { useTranslation } from "react-i18next";
import { streakService, StreakData } from "@/services/streakService";
import { Timer } from "lucide-react";

interface SobrietyCounterProps {
  userId: string;
  onMilestone?: (days: number) => void;
}

// Milstolpar som triggar celebration
const MILESTONES = [1, 7, 14, 30, 60, 90, 180, 365, 730, 1095];

const SobrietyCounter = ({ userId, onMilestone }: SobrietyCounterProps) => {
  const { t } = useTranslation();
  const [streakData, setStreakData] = useState<StreakData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStreak = async () => {
      try {
        const data = await streakService.getStreak(userId);
        setStreakData(data);

        // Kontrollera milstolpar
        if (onMilestone && data.currentStreak > 0 && MILESTONES.includes(data.currentStreak)) {
          onMilestone(data.currentStreak);
        }
      } catch {
        // Streak-data kunde inte hämtas
      } finally {
        setLoading(false);
      }
    };

    fetchStreak();
  }, [userId, onMilestone]);

  // Beräkna tid sedan streakStartDate
  const timeSince = useMemo(() => {
    if (!streakData?.streakStartDate) return null;

    const start = new Date(streakData.streakStartDate);
    const now = new Date();
    const diffMs = now.getTime() - start.getTime();
    const totalDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
    const years = Math.floor(totalDays / 365);
    const months = Math.floor((totalDays % 365) / 30);
    const days = totalDays % 30;

    return { years, months, days, totalDays };
  }, [streakData?.streakStartDate]);

  if (loading) {
    return (
      <Card className="p-4 animate-pulse">
        <div className="h-16 bg-muted rounded" />
      </Card>
    );
  }

  // Ingen aktiv streak — visa uppmuntran
  if (!streakData || streakData.currentStreak === 0 || !streakData.streakStartDate) {
    return (
      <Card className="p-5 text-center bg-gradient-to-br from-card to-muted/20 shadow-[var(--shadow-card)]">
        <Timer className="w-8 h-8 mx-auto mb-2 text-muted-foreground" />
        <p className="text-sm font-medium text-foreground">{t("sobriety.title")}</p>
        <p className="text-xs text-muted-foreground mt-1">{t("sobriety.setupDesc")}</p>
      </Card>
    );
  }

  return (
    <Card className="p-5 bg-gradient-to-br from-green-50 to-emerald-50 dark:from-green-950/30 dark:to-emerald-950/20 border-green-200 dark:border-green-800 shadow-[var(--shadow-card)]">
      <div className="flex items-center gap-4">
        <div className="w-12 h-12 rounded-full bg-green-100 dark:bg-green-900/40 flex items-center justify-center flex-shrink-0">
          <Timer className="w-6 h-6 text-green-600 dark:text-green-400" />
        </div>
        <div className="flex-1">
          <p className="text-sm text-muted-foreground font-medium">{t("sobriety.title")}</p>
          <div className="flex items-baseline gap-2 mt-1">
            {timeSince && timeSince.years > 0 && (
              <span className="text-2xl font-bold text-green-700 dark:text-green-300">
                {timeSince.years} {t("sobriety.years", { count: timeSince.years })}
              </span>
            )}
            {timeSince && timeSince.months > 0 && (
              <span className="text-2xl font-bold text-green-700 dark:text-green-300">
                {timeSince.months} {t("sobriety.months", { count: timeSince.months })}
              </span>
            )}
            <span className={`font-bold text-green-700 dark:text-green-300 ${
              !timeSince?.years && !timeSince?.months ? "text-3xl" : "text-2xl"
            }`}>
              {streakData.currentStreak} {t("common.days", { defaultValue: "dagar" })}
            </span>
          </div>
          <p className="text-xs text-muted-foreground mt-1">
            {t("sobriety.since")} {new Date(streakData.streakStartDate).toLocaleDateString("sv-SE")}
          </p>
        </div>
      </div>
    </Card>
  );
};

export default SobrietyCounter;
