import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Trophy, Target, Zap, Star, Award, Crown, Brain, Activity, Focus, Lightbulb, Share2, LucideIcon } from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { progressService } from "@/services/progressService";
import { CategoryProgress, Achievement } from "@/types/progress";
import { ProgressPageSkeleton } from "@/components/skeletons/ProgressSkeleton";
import { motion, useMotionValue, useTransform, animate as motionAnimate } from "framer-motion";
import { staggerContainer, staggerItem } from "@/lib/animations";
import StreakCalendar from "@/components/StreakCalendar";
import StreakBadges from "@/components/StreakBadges";
import RewardCollection from "@/components/RewardCollection";
import { streakService, StreakData } from "@/services/streakService";
import { rewardService, CollectibleData } from "@/services/rewardService";

function AnimatedCounter({ value, className }: { value: number; className?: string }) {
  const motionValue = useMotionValue(0);
  const rounded = useTransform(motionValue, (v) => Math.round(v));
  const [display, setDisplay] = useState(0);

  useEffect(() => {
    const controls = motionAnimate(motionValue, value, {
      duration: 1,
      ease: [0.23, 1, 0.32, 1],
    });
    const unsubscribe = rounded.on("change", (v) => setDisplay(v));
    return () => { controls.stop(); unsubscribe(); };
  }, [value]);

  return <span className={className}>{display}</span>;
}

const Progress = () => {
  const { t } = useTranslation();
  const { user } = useAuth();
  const [points, setPoints] = useState(0);
  const [level, setLevel] = useState(1);
  const [showAchievements, setShowAchievements] = useState(false);
  const [categoryProgress, setCategoryProgress] = useState<CategoryProgress[]>([]);
  const [achievements, setAchievements] = useState<Achievement[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [streakData, setStreakData] = useState<StreakData | null>(null);
  const [collectibles, setCollectibles] = useState<CollectibleData[]>([]);

  // Mappning av kategori-ID till ikoner och färger
  const getCategoryStyle = (categoryId: string) => {
    const styles: Record<string, { icon: LucideIcon; color: string; borderColor: string; bgColor: string }> = {
      mental: { icon: Brain, color: "text-blue-500", borderColor: "border-blue-500/30", bgColor: "from-blue-500/10 to-blue-500/5" },
      physical: { icon: Activity, color: "text-red-500", borderColor: "border-red-500/30", bgColor: "from-red-500/10 to-red-500/5" },
      focus: { icon: Focus, color: "text-purple-500", borderColor: "border-purple-500/30", bgColor: "from-purple-500/10 to-purple-500/5" },
      growth: { icon: Lightbulb, color: "text-amber-500", borderColor: "border-amber-500/30", bgColor: "from-amber-500/10 to-amber-500/5" },
    };
    return styles[categoryId] || styles.mental;
  };

  // Mappning av kategori-ID till svenska namn
  const getCategoryName = (categoryId: string): string => {
    const names: Record<string, string> = {
      mental: t('progress.categories.mental'),
      physical: t('progress.categories.physical'),
      focus: t('progress.categories.focus'),
      growth: t('progress.categories.growth'),
    };
    return names[categoryId] || categoryId;
  };

  // Mappning av achievement-ID till ikoner
  const getAchievementIcon = (achievementId: number): LucideIcon => {
    const icons: Record<number, LucideIcon> = {
      1: Target,
      2: Zap,
      3: Trophy,
      4: Star,
      5: Award,
      6: Crown,
    };
    return icons[achievementId] || Trophy;
  };

  // Mappning av achievement-ID till svenska texter
  const getAchievementText = (achievementId: number): { title: string; description: string } => {
    const textData = t(`progress.achievementTexts.${achievementId}`, { returnObjects: true }) as { title: string; description: string } | string;
    if (typeof textData === 'object' && textData.title) {
      return textData;
    }
    return { title: `Achievement ${achievementId}`, description: "" };
  };

  useEffect(() => {
    if (user?.id) {
      fetchUserProgress();
      fetchStreakAndCollection();
    }
    // Trigger animationen efter en kort delay för att låta komponenten rendera
    const timer = setTimeout(() => {
      setShowAchievements(true);
    }, 100);
    return () => clearTimeout(timer);
  }, [user?.id]);

  const fetchStreakAndCollection = async () => {
    if (!user?.email && !user?.id) return;
    const userId = user.email || user.id;
    try {
      const [streak, collection] = await Promise.all([
        streakService.getStreakData(userId, 12).catch(() => null),
        rewardService.getCollection(userId).catch(() => []),
      ]);
      if (streak) setStreakData(streak);
      setCollectibles(collection);
    } catch {
      // Silently ignore
    }
  };

  const fetchUserProgress = async () => {
    if (!user?.email && !user?.id) return;
    const userId = user.email || user.id;
    
    setIsLoading(true);
    try {
      // Hämta användarens totala framsteg
      const progressData = await progressService.getUserProgress(userId);
      
      setPoints(progressData.totalPoints || 0);
      setLevel(progressData.level || 1);

      // Konvertera backend kategoriformat till frontend format
      // Backend: {category: 'mental', points: 10}
      // Frontend: {id: 'mental', name: 'Mental hälsa', completedDays: X, totalDays: 7}
      const knownCategories = ['mental', 'physical', 'focus', 'growth'];
      const categoriesWithStyles: CategoryProgress[] = (progressData.categoryProgress || [])
        .filter((cat: any) => knownCategories.includes(cat.category || cat.id))
        .map((cat: any) => {
        // Beräkna completedDays baserat på poäng (exempel: 10 poäng = 1 dag, 70 = 7 dagar)
        const completedDays = Math.min(Math.floor((cat.points || 0) / 10), 7);
        
        return {
          id: cat.category || cat.id || "",
          name: getCategoryName(cat.category || cat.id),
          completedDays: completedDays,
          totalDays: 7,
          ...getCategoryStyle(cat.category || cat.id),
        };
      });
      
      setCategoryProgress(categoriesWithStyles);

      // Mappa achievements med ikoner och svenska texter
      const achievementsWithIcons: Achievement[] = (progressData.achievements || []).map((ach: any) => {
        const texts = getAchievementText(ach.id);
        return {
          id: ach.id,
          title: texts.title,
          description: texts.description,
          unlocked: ach.unlocked || false,
          points: ach.requiredPoints || ach.points || 0,
          icon: getAchievementIcon(ach.id),
        };
      });
      setAchievements(achievementsWithIcons);

    } catch {
      // Vid fel, sätt tom data istället för att låta komponenten krascha
      setCategoryProgress([]);
      setAchievements([]);
    } finally {
      setIsLoading(false);
    }
  };

  const stats = [
    { label: t('progress.totalPoints'), value: points, color: "text-accent" },
    { label: t('progress.currentLevel'), value: level, color: "text-primary" },
    { label: t('progress.unlockedAchievements'), value: achievements.filter(a => a.unlocked).length, color: "text-secondary" },
  ];

  if (isLoading) {
    return <ProgressPageSkeleton />;
  }

  return (
    <div className="min-h-full bg-gradient-to-br from-primary/5 via-background to-secondary/5 p-4 pt-4">
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Header */}
        <div className="pt-2">
          <h1 className="text-3xl font-bold text-foreground mb-2">{t('progress.title')}</h1>
          <p className="text-muted-foreground">{t('progress.subtitle')}</p>
        </div>

        {/* Avatar Card */}
        <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5, ease: [0.23, 1, 0.32, 1] }}>
        <Card className="p-8 bg-gradient-to-br from-card to-muted/20 shadow-[var(--shadow-card)]">
          <div className="flex flex-col items-center space-y-4">
            <motion.div
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{ type: "spring", stiffness: 200, damping: 12, delay: 0.2 }}
              className="w-28 h-28 rounded-full bg-gradient-to-br from-primary to-primary-glow flex items-center justify-center shadow-[var(--shadow-glow)]"
            >
              <Trophy className="w-14 h-14 text-primary-foreground" />
            </motion.div>
            <Badge className="bg-primary text-primary-foreground text-xl px-6 py-2">
              {t('common.level')} {level}
            </Badge>
          </div>
        </Card>
        </motion.div>

        {/* Stats Grid */}
        <motion.div
          variants={staggerContainer}
          initial="initial"
          animate="animate"
          className="grid grid-cols-1 md:grid-cols-3 gap-4"
        >
          {stats.map((stat, index) => (
            <motion.div key={index} variants={staggerItem} whileHover={{ y: -2 }} whileTap={{ scale: 0.98 }}>
              <Card className="p-5 text-center">
                <AnimatedCounter value={stat.value} className={`text-3xl font-bold ${stat.color}`} />
                <p className="text-sm text-muted-foreground mt-1">{stat.label}</p>
              </Card>
            </motion.div>
          ))}
        </motion.div>

        {/* Streak Badges + Calendar */}
        {streakData && (
          <motion.div
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.2, ease: [0.23, 1, 0.32, 1] }}
            className="space-y-3"
          >
            <StreakBadges
              currentStreak={streakData.currentStreak}
              longestStreak={streakData.longestStreak}
              streakFreezes={streakData.streakFreezes}
            />
            <StreakCalendar calendarDays={streakData.calendarDays} />
          </motion.div>
        )}

        {/* Framsteg per kategori */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.3, ease: [0.23, 1, 0.32, 1] }}
          className="space-y-4"
        >
          <div>
            <h2 className="text-xl font-semibold text-foreground">{t('progress.categoryProgress')}</h2>
            <p className="text-sm text-muted-foreground mt-1">{t('progress.last7Days')}</p>
          </div>

          {/* Totalt framsteg */}
          {categoryProgress.length > 0 && (
            <Card className="p-5 bg-gradient-to-br from-green-500/10 to-green-500/5 border border-green-500/30 card-hover">
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <h3 className="font-semibold text-foreground">{t('progress.totalProgress')}</h3>
                  <span className="text-lg font-bold text-green-600">
                    {categoryProgress.length > 0 
                      ? Math.round((categoryProgress.reduce((sum, cat) => sum + (cat.completedDays || 0), 0) / (categoryProgress.length * 7)) * 100)
                      : 0}%
                  </span>
                </div>
                <div className="relative h-3 w-full overflow-hidden rounded-full bg-muted">
                  <div 
                    className="h-full bg-green-500 rounded-full progress-fill"
                    style={{ 
                      width: `${categoryProgress.length > 0 
                        ? Math.round((categoryProgress.reduce((sum, cat) => sum + (cat.completedDays || 0), 0) / (categoryProgress.length * 7)) * 100)
                        : 0}%`
                    }}
                  />
                </div>
                <p className="text-xs text-muted-foreground">
                  {t('progress.completedDaysOf', { completed: categoryProgress.reduce((sum, cat) => sum + (cat.completedDays || 0), 0), total: categoryProgress.length * 7 })}
                </p>
              </div>
            </Card>
          )}

          {/* Individuella kategorier */}
          <div className="space-y-3">
            {categoryProgress.length > 0 ? (
              categoryProgress.map((category) => {
                const completedDays = category.completedDays || 0;
                const totalDays = category.totalDays || 7;
                const progressPercentage = totalDays > 0 ? (completedDays / totalDays) * 100 : 0;
                const Icon = category.icon;
                
                return (
                  <Card key={category.id} className={`p-5 bg-gradient-to-br ${category.bgColor} border ${category.borderColor} card-hover`}>
                    <div className="space-y-3">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-xl bg-muted flex items-center justify-center">
                            <Icon className={`w-5 h-5 ${category.color}`} />
                          </div>
                          <h3 className="font-semibold text-foreground">{category.name}</h3>
                        </div>
                        <span className="text-sm text-muted-foreground font-medium">
                          {completedDays}/{totalDays} {t('common.days')}
                        </span>
                      </div>
                      <div className="relative h-2 w-full overflow-hidden rounded-full bg-muted">
                        <div 
                          className="h-full bg-green-500 rounded-full progress-fill"
                          style={{ width: `${Math.min(progressPercentage, 100)}%` }}
                        />
                      </div>
                    </div>
                  </Card>
                );
              })
            ) : (
              <Card className="p-5">
                <p className="text-center text-muted-foreground">{t('progress.noCategoryData')}</p>
              </Card>
            )}
          </div>
        </motion.div>

        {/* Achievements */}
        <div className="space-y-4">
          <h2 className="text-xl font-semibold text-foreground">{t('progress.achievements')}</h2>
          <div className="grid gap-3">
            {achievements.map((achievement, index) => {
              const Icon = achievement.icon;
              return (
                <motion.div
                  key={achievement.id}
                  initial={{ opacity: 0, y: 12 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.4 + index * 0.08, duration: 0.4, ease: [0.23, 1, 0.32, 1] }}
                  whileHover={{ y: -2 }}
                  whileTap={{ scale: 0.98 }}
                >
                <Card
                  className={`p-4 ${
                    achievement.unlocked
                      ? "bg-gradient-to-br from-primary/15 to-accent/15 border-primary/50 shadow-[var(--shadow-card)]"
                      : "opacity-50 bg-muted/30"
                  }`}
                >
                  <div className="flex items-center gap-4">
                    <div className={`w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0 ${
                      achievement.unlocked 
                        ? "bg-gradient-to-br from-primary to-primary-glow shadow-[var(--shadow-glow)]" 
                        : "bg-muted"
                    }`}>
                      <Icon className={`w-6 h-6 ${achievement.unlocked ? "text-primary-foreground" : "text-muted-foreground"}`} />
                    </div>
                    <div className="flex-1 min-w-0 text-left">
                      <h3 className="font-semibold text-foreground text-sm mb-0.5 truncate">{achievement.title}</h3>
                      <p className="text-xs text-muted-foreground line-clamp-1">{achievement.description}</p>
                    </div>
                    {achievement.unlocked && (
                      <div className="flex items-center gap-2 flex-shrink-0">
                        <Badge className="bg-accent text-accent-foreground text-xs">
                          {t('progress.unlocked')}
                        </Badge>
                        {typeof navigator.share === "function" && (
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              navigator.share({
                                title: achievement.title,
                                text: t('progress.shareAchievementText', {
                                  title: achievement.title,
                                  defaultValue: `Jag låste upp "${achievement.title}" på NextStep! 🏆`,
                                }),
                                url: window.location.origin,
                              }).catch(() => {});
                            }}
                            className="p-1.5 rounded-lg hover:bg-primary/10 text-muted-foreground hover:text-primary transition-colors"
                            title={t('progress.shareAchievement', { defaultValue: 'Dela' })}
                          >
                            <Share2 className="w-3.5 h-3.5" />
                          </button>
                        )}
                      </div>
                    )}
                  </div>
                </Card>
                </motion.div>
              );
            })}
          </div>
        </div>

        {/* Reward Collection */}
        {collectibles.length > 0 && (
          <motion.div
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.5, ease: [0.23, 1, 0.32, 1] }}
          >
            <RewardCollection collectibles={collectibles} />
          </motion.div>
        )}
      </div>
    </div>
  );
};

export default Progress;
