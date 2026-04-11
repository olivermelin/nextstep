import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { useNavigate } from "react-router-dom";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Sparkles, Target, MessageCircle, X } from "lucide-react";
import { useTranslation } from "react-i18next";

interface WelcomeCardProps {
  userId: string;
  userName?: string;
}

const WelcomeCard = ({ userId, userName }: WelcomeCardProps) => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [dismissed, setDismissed] = useState(false);

  const storageKey = `welcome_shown_${userId}`;

  const handleDismiss = () => {
    localStorage.setItem(storageKey, "true");
    setDismissed(true);
  };

  const handleAction = (path: string) => {
    handleDismiss();
    navigate(path);
  };

  const actions = [
    {
      icon: <Sparkles className="w-5 h-5 text-amber-500" />,
      bg: "bg-amber-50 dark:bg-amber-900/20",
      label: t("welcome.actionCheckIn"),
      desc: t("welcome.actionCheckInDesc"),
      onClick: () => {
        handleDismiss();
        window.scrollTo({ top: 400, behavior: "smooth" });
      },
    },
    {
      icon: <Target className="w-5 h-5 text-primary" />,
      bg: "bg-primary/10",
      label: t("welcome.actionChallenge"),
      desc: t("welcome.actionChallengeDesc"),
      onClick: () => handleAction("/challenges"),
    },
    {
      icon: <MessageCircle className="w-5 h-5 text-emerald-500" />,
      bg: "bg-emerald-50 dark:bg-emerald-900/20",
      label: t("welcome.actionCoach"),
      desc: t("welcome.actionCoachDesc"),
      onClick: () => handleAction("/ai-coach"),
    },
  ];

  return (
    <AnimatePresence>
      {!dismissed && (
        <motion.div
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: -10, height: 0, marginBottom: 0 }}
          transition={{ duration: 0.35, ease: "easeOut" }}
        >
          <Card className="relative p-6 bg-gradient-to-br from-primary/10 via-background to-secondary/10 border-primary/25 overflow-hidden">
            {/* Bakgrundsdekor */}
            <div className="absolute top-0 right-0 w-40 h-40 bg-primary/5 rounded-full -translate-y-1/2 translate-x-1/2 pointer-events-none" />

            {/* Stäng-knapp */}
            <button
              onClick={handleDismiss}
              className="absolute top-3 right-3 p-1.5 rounded-full text-muted-foreground hover:text-foreground hover:bg-muted/60 transition-colors"
              aria-label={t("common.close")}
            >
              <X className="w-4 h-4" />
            </button>

            {/* Rubrik */}
            <div className="mb-5">
              <h2 className="text-xl font-bold text-foreground mb-1">
                {userName
                  ? `${t("welcome.title").replace("! 🎉", `, ${userName.split(" ")[0]}! 🎉`)}`
                  : t("welcome.title")}
              </h2>
              <p className="text-sm text-muted-foreground">
                {t("welcome.subtitle")}
              </p>
            </div>

            {/* Val-knappar */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              {actions.map((action) => (
                <button
                  key={action.label}
                  onClick={action.onClick}
                  className="flex flex-col items-start gap-2 p-4 rounded-xl border border-border/60 bg-card/80 hover:bg-card hover:shadow-sm hover:border-primary/30 transition-all text-left group"
                >
                  <div className={`p-2 rounded-lg ${action.bg} group-hover:scale-105 transition-transform`}>
                    {action.icon}
                  </div>
                  <div>
                    <p className="text-sm font-semibold text-foreground">{action.label}</p>
                    <p className="text-xs text-muted-foreground mt-0.5">{action.desc}</p>
                  </div>
                </button>
              ))}
            </div>

            {/* Hoppa över */}
            <div className="mt-4 text-center">
              <Button variant="ghost" size="sm" className="text-xs text-muted-foreground" onClick={handleDismiss}>
                {t("welcome.dismiss")}
              </Button>
            </div>
          </Card>
        </motion.div>
      )}
    </AnimatePresence>
  );
};

export default WelcomeCard;
