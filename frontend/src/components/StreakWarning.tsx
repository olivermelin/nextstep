import { motion, AnimatePresence } from "framer-motion";
import { Flame, X, ArrowRight } from "lucide-react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { StreakData } from "@/services/streakService";

interface StreakWarningProps {
  streakData: StreakData;
  checkedInToday: boolean;
}

/**
 * Visar en diskret varningsbanner om användaren har en aktiv streak
 * men inte registrerat aktivitet idag.
 * Visas bara efter kl 18 (kvällsvarning).
 */
const StreakWarning = ({ streakData, checkedInToday }: StreakWarningProps) => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const hour = new Date().getHours();

  // Visa bara om: streak aktiv, ej incheckat idag, och det är kväll (efter 18)
  const shouldShow =
    streakData.streakActive &&
    streakData.currentStreak >= 2 &&
    !streakData.activityRegisteredToday &&
    !checkedInToday &&
    hour >= 18;

  if (!shouldShow) return null;

  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0, y: -8 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, y: -8 }}
        transition={{ duration: 0.3 }}
        className="rounded-2xl border border-orange-400/40 bg-gradient-to-r from-orange-500/15 to-amber-500/10 p-4 flex items-center gap-3"
      >
        <div className="w-9 h-9 rounded-xl bg-orange-500/20 flex items-center justify-center flex-shrink-0">
          <Flame className="w-5 h-5 text-orange-500" />
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-sm font-semibold text-foreground">
            {t("streakWarning.title", {
              days: streakData.currentStreak,
              defaultValue: `Din ${streakData.currentStreak}-dagarsstreak riskerar att brytas!`,
            })}
          </p>
          <p className="text-xs text-muted-foreground mt-0.5">
            {t("streakWarning.subtitle", {
              defaultValue: "Checka in eller slutför en utmaning för att hålla den vid liv.",
            })}
          </p>
        </div>
        <button
          onClick={() => navigate("/challenges")}
          className="flex items-center gap-1 text-xs font-semibold text-orange-600 dark:text-orange-400 hover:underline flex-shrink-0"
        >
          {t("streakWarning.cta", { defaultValue: "Gör något nu" })}
          <ArrowRight className="w-3.5 h-3.5" />
        </button>
      </motion.div>
    </AnimatePresence>
  );
};

export default StreakWarning;
