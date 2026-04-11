import { useState, useEffect } from 'react';
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Star, Quote, Snowflake, Frame } from "lucide-react";
import { rewardService, DailyRewardDto, RewardType } from "@/services/rewardService";
import { motion, AnimatePresence } from "framer-motion";
import { useTranslation } from 'react-i18next';

interface DailyRewardBoxProps {
  userId: string;
  onClaimStatusChange?: (claimed: boolean) => void;
}

const rewardConfig: Record<RewardType, { icon: React.ReactNode; color: string; bg: string }> = {
  BONUS_XP:      { icon: <Star className="w-5 h-5" />,      color: "text-yellow-600 dark:text-yellow-400", bg: "bg-yellow-100 dark:bg-yellow-500/20" },
  QUOTE_CARD:    { icon: <Quote className="w-5 h-5" />,     color: "text-blue-600 dark:text-blue-400",   bg: "bg-blue-100 dark:bg-blue-500/20" },
  STREAK_FREEZE: { icon: <Snowflake className="w-5 h-5" />, color: "text-cyan-600 dark:text-cyan-400",   bg: "bg-cyan-100 dark:bg-cyan-500/20" },
  AVATAR_FRAME:  { icon: <Frame className="w-5 h-5" />,     color: "text-orange-600 dark:text-orange-400", bg: "bg-orange-100 dark:bg-orange-500/20" },
};

const DailyRewardBox = ({ userId, onClaimStatusChange }: DailyRewardBoxProps) => {
  const { t } = useTranslation();
  const [reward, setReward] = useState<DailyRewardDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [claiming, setClaiming] = useState(false);
  const [justClaimed, setJustClaimed] = useState(false);

  useEffect(() => {
    const loadReward = async () => {
      try {
        const today = await rewardService.getToday(userId);
        setReward(today);
      } catch {
        // Tyst fel — visa ingenting vid API-problem
      } finally {
        setLoading(false);
      }
    };
    loadReward();
  }, [userId]);

  const handleClaim = async () => {
    if (!reward || reward.claimed) return;
    setClaiming(true);
    try {
      const claimed = await rewardService.claim(userId);
      setReward(claimed);
      setJustClaimed(true);
      onClaimStatusChange?.(true);
    } catch {
      // Tyst fel
    } finally {
      setClaiming(false);
    }
  };

  // Visa ingenting medan vi laddar eller om det inte finns någon belöning
  if (loading || !reward) return null;

  // Visa inte om redan hämtad från en tidigare session (inte precis hämtad nu)
  if (reward.claimed && !justClaimed) return null;

  const parsed = rewardService.parseRewardValue(reward.rewardValue);
  const config = rewardConfig[reward.rewardType];

  const getRewardDescription = () => {
    switch (reward.rewardType) {
      case 'BONUS_XP':
        return t('reward.bonusXp', { xp: parsed.xp ?? 10 });
      case 'QUOTE_CARD':
        return parsed.quote
          ? `"${parsed.quote.length > 70 ? parsed.quote.substring(0, 70) + '...' : parsed.quote}"`
          : t('reward.quoteCard');
      case 'STREAK_FREEZE':
        return t('reward.streakFreeze');
      case 'AVATAR_FRAME':
        return t('reward.avatarFrame', { xp: parsed.xp ?? 100 });
    }
  };

  return (
    <motion.div
      id="daily-reward"
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3, ease: [0.23, 1, 0.32, 1] }}
    >
      <Card className={`p-4 ${reward.rare
        ? 'border-yellow-400/60 bg-gradient-to-br from-yellow-50 to-amber-50 dark:from-yellow-500/10 dark:to-amber-500/10'
        : 'bg-gradient-to-br from-card to-muted/20'
      }`}>
        <div className="flex items-center gap-3">
          {/* Ikon */}
          <div className={`w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0 ${config.bg}`}>
            <span className={config.color}>{config.icon}</span>
          </div>

          {/* Text */}
          <div className="flex-1 min-w-0">
            <p className="text-sm font-semibold text-foreground flex items-center gap-1.5">
              {t('reward.dailyReward')}
              {reward.rare && (
                <span className="text-xs bg-yellow-400/20 text-yellow-600 dark:text-yellow-400 px-1.5 py-0.5 rounded-full font-medium">
                  {t('reward.rare')}
                </span>
              )}
            </p>
            <p className="text-xs text-muted-foreground truncate">{getRewardDescription()}</p>
          </div>

          {/* Knapp / Bekräftelse */}
          <AnimatePresence mode="wait">
            {!reward.claimed ? (
              <motion.div key="claim" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}>
                <Button
                  size="sm"
                  onClick={handleClaim}
                  disabled={claiming}
                  className="flex-shrink-0"
                >
                  {claiming ? '...' : t('reward.claim')}
                </Button>
              </motion.div>
            ) : (
              <motion.span
                key="done"
                initial={{ scale: 0 }}
                animate={{ scale: 1 }}
                transition={{ type: "spring", stiffness: 300, damping: 15 }}
                className="text-2xl flex-shrink-0"
              >
                ✅
              </motion.span>
            )}
          </AnimatePresence>
        </div>
      </Card>
    </motion.div>
  );
};

export default DailyRewardBox;
