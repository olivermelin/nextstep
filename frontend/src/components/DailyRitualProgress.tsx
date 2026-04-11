import { motion } from "framer-motion";
import { CheckCircle2, ClipboardCheck, Target, Gift } from "lucide-react";
import { Card } from "@/components/ui/card";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";

interface DailyRitualProgressProps {
  checkedIn: boolean;
  challengeCompleted: boolean;
  rewardClaimed: boolean;
}

const DailyRitualProgress = ({ checkedIn, challengeCompleted, rewardClaimed }: DailyRitualProgressProps) => {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const steps = [
    { key: "checkin", label: t("dashboard.ritualCheckIn"), icon: ClipboardCheck, done: checkedIn, action: () => window.scrollTo({ top: 0, behavior: "smooth" }) },
    { key: "challenge", label: t("dashboard.ritualChallenge"), icon: Target, done: challengeCompleted, action: () => navigate("/challenges") },
    { key: "reward", label: t("dashboard.ritualReward"), icon: Gift, done: rewardClaimed, action: () => document.getElementById("daily-reward")?.scrollIntoView({ behavior: "smooth" }) },
  ];

  const completedCount = steps.filter(s => s.done).length;
  const allDone = completedCount === 3;

  return (
    <Card className="p-4">
      <div className="flex items-center justify-between mb-3">
        <p className="text-sm font-semibold text-foreground">{t("dashboard.dailyRitual")}</p>
        <span className="text-xs text-muted-foreground">
          {completedCount}/3
        </span>
      </div>

      <div className="flex items-center gap-1">
        {steps.map((step, i) => (
          <div key={step.key} className="flex items-center flex-1">
            {/* Steg-ikon — klickbar om ej slutfört */}
            <motion.button
              onClick={!step.done ? step.action : undefined}
              className={`flex flex-col items-center gap-1 flex-1 transition-colors ${
                step.done ? "text-primary" : "text-muted-foreground hover:text-foreground cursor-pointer"
              }`}
              initial={false}
              animate={step.done ? { scale: [1, 1.15, 1] } : {}}
              transition={{ duration: 0.3 }}
            >
              <div className={`w-9 h-9 rounded-full flex items-center justify-center transition-colors ${
                step.done
                  ? "bg-primary/15"
                  : "bg-muted/50 group-hover:bg-primary/10"
              }`}>
                {step.done ? (
                  <CheckCircle2 className="w-5 h-5 text-primary" />
                ) : (
                  <step.icon className="w-4 h-4" />
                )}
              </div>
              <span className="text-[10px] leading-tight text-center">{step.label}</span>
            </motion.button>

            {/* Linje mellan steg */}
            {i < steps.length - 1 && (
              <div className={`h-0.5 w-4 mx-0.5 mt-[-14px] rounded transition-colors ${
                steps[i + 1].done ? "bg-primary" : "bg-border"
              }`} />
            )}
          </div>
        ))}
      </div>

      {allDone && (
        <motion.p
          className="text-xs text-primary font-medium text-center mt-3"
          initial={{ opacity: 0, y: 4 }}
          animate={{ opacity: 1, y: 0 }}
        >
          {t("dashboard.ritualComplete")}
        </motion.p>
      )}
    </Card>
  );
};

export default DailyRitualProgress;
