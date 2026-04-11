import { useEffect, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Trophy, X, Flame } from "lucide-react";
import { useTranslation } from "react-i18next";
import { useConfetti } from "@/components/Confetti";
import { StreakData } from "@/services/streakService";

const MILESTONES = [7, 14, 30, 60, 90, 180, 365];

interface MilestoneCelebrationProps {
  streakData: StreakData | null;
}

/**
 * Visar en milstolpefirning när användaren når dag 7, 14, 30, 60, 90 etc.
 * Visas en gång per milstolpe (lagras i localStorage).
 */
const MilestoneCelebration = ({ streakData }: MilestoneCelebrationProps) => {
  const { t } = useTranslation();
  const { fireBurst } = useConfetti();
  const [milestone, setMilestone] = useState<number | null>(null);

  useEffect(() => {
    if (!streakData?.streakActive) return;
    const streak = streakData.currentStreak;
    const hit = MILESTONES.find(m => m === streak);
    if (!hit) return;

    const key = `milestone_celebrated_${streakData.userId}_${hit}`;
    if (localStorage.getItem(key)) return;

    localStorage.setItem(key, "1");
    setMilestone(hit);
    setTimeout(() => fireBurst(), 300);
  }, [streakData]);

  if (!milestone) return null;

  const getMessage = (days: number) => {
    if (days >= 365) return t("milestone.year", { defaultValue: "Ett helt år! Du är en inspiration för alla runt dig." });
    if (days >= 180) return t("milestone.halfYear", { defaultValue: "Halvt år — det här är otroligt starkt." });
    if (days >= 90) return t("milestone.ninetyDays", { defaultValue: "90 dagar! Du har byggt ett helt nytt liv." });
    if (days >= 60) return t("milestone.sixtyDays", { defaultValue: "60 dagar av styrka och mod — imponerande." });
    if (days >= 30) return t("milestone.thirtyDays", { defaultValue: "En hel månad! De nya vanorna sitter nu." });
    if (days >= 14) return t("milestone.twoWeeks", { defaultValue: "Två veckor i rad — du är på väg!" });
    return t("milestone.sevenDays", { defaultValue: "En hel vecka — det här är en riktig vändpunkt!" });
  };

  return (
    <AnimatePresence>
      <motion.div
        key={milestone}
        initial={{ opacity: 0, scale: 0.9, y: -12 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.9 }}
        transition={{ type: "spring", stiffness: 300, damping: 22 }}
        className="rounded-2xl border border-yellow-400/50 bg-gradient-to-r from-yellow-500/15 via-amber-500/10 to-orange-500/10 p-5 relative overflow-hidden"
      >
        <div className="absolute top-0 right-0 w-32 h-32 bg-yellow-400/10 rounded-full -translate-y-1/2 translate-x-1/2 pointer-events-none" />

        <div className="flex items-start gap-4">
          <div className="w-12 h-12 rounded-2xl bg-yellow-500/20 flex items-center justify-center flex-shrink-0">
            <Trophy className="w-6 h-6 text-yellow-500" />
          </div>
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-1">
              <Flame className="w-4 h-4 text-orange-500" />
              <p className="text-xs font-semibold text-orange-500 uppercase tracking-wide">
                {t("milestone.label", { days: milestone, defaultValue: `${milestone} dagar i rad!` })}
              </p>
            </div>
            <p className="text-base font-bold text-foreground mb-1">
              {t("milestone.congrats", { defaultValue: "Grattis — det är en milstolpe!" })}
            </p>
            <p className="text-sm text-muted-foreground">{getMessage(milestone)}</p>
          </div>
          <button
            onClick={() => setMilestone(null)}
            className="text-muted-foreground hover:text-foreground transition-colors flex-shrink-0 mt-0.5"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      </motion.div>
    </AnimatePresence>
  );
};

export default MilestoneCelebration;
