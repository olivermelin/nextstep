import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { Badge } from "@/components/ui/badge";
import { Sparkles, ArrowRight, Target } from "lucide-react";
import { CoachMessageSkeleton, ActiveChallengesSkeleton } from "@/components/skeletons/DashboardSkeleton";
import { useAuth } from "@/context/AuthContext";
import { API_ENDPOINTS } from "@/config/api";
import { useTranslation } from "react-i18next";
import { getDailyCoachTip } from "@/services/coachService";
import { userChallengeApi } from "@/services/challengeService";
import { formatMarkdown } from "@/components/AIChat";
import { UserChallengeOutDto } from "@/types/challenge";
import SobrietyCounter from "@/components/SobrietyCounter";
import DailyCheckIn from "@/components/DailyCheckIn";
import SOSButton from "@/components/SOSButton";
import MilestoneCelebration from "@/components/MilestoneCelebration";
import DailyRewardBox from "@/components/DailyRewardBox";
import { rewardService, DailyRewardData } from "@/services/rewardService";
import { useCallback } from "react";

interface UserProgress {
  points: number;
  level: number;
}

const Dashboard = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { t } = useTranslation();
  const [userProgress, setUserProgress] = useState<UserProgress>({
    points: 0,
    level: 1,
  });
  const [dailyCoachMessage, setDailyCoachMessage] = useState("");
  const [coachLoading, setCoachLoading] = useState(false);
  const [activeChallenges, setActiveChallenges] = useState<UserChallengeOutDto[]>([]);
  const [challengesLoading, setChallengesLoading] = useState(false);
  const [milestoneDays, setMilestoneDays] = useState<number | null>(null);
  const [dailyReward, setDailyReward] = useState<DailyRewardData | null>(null);

  // Härledd userId från AuthContext (email prioriterat)
  const userId = user?.email || user?.id || null;

  const handleMilestone = useCallback((days: number) => {
    setMilestoneDays(days);
  }, []);

  // Generera daily reward efter check-in
  const handleCheckIn = useCallback(async () => {
    if (!userId) return;
    try {
      const reward = await rewardService.generateReward(userId);
      setDailyReward(reward);
    } catch {
      // Reward generation failed silently
    }
  }, [userId]);

  // Hämta ev. redan genererad reward
  useEffect(() => {
    if (!userId) return;
    rewardService.getTodayReward(userId).then((reward) => {
      if (reward) setDailyReward(reward);
    });
  }, [userId]);

  // Hämta användarens framsteg
  useEffect(() => {
    if (userId) {
      fetchUserProgress();
      fetchActiveChallenges();
    }
  }, [userId]);

  const fetchUserProgress = async () => {
    if (!userId) return;
    try {
      const res = await fetch(API_ENDPOINTS.PROGRESS.GET_USER_PROGRESS(userId), {
        credentials: "include",
      });
      const data = await res.json();
      setUserProgress({
        points: data.totalPoints || 0,
        level: data.level || 1,
      });
    } catch {
      // Progress fetch failed silently; default values are used
    }
  };

  // Hämta aktiva challenges för Today's Activities
  const fetchActiveChallenges = async () => {
    if (!userId) return;
    setChallengesLoading(true);
    try {
      const data = await userChallengeApi.getUserActiveChallenges(userId);
      setActiveChallenges(data);
    } catch {
      // Active challenges fetch failed silently
    } finally {
      setChallengesLoading(false);
    }
  };

  // Hämta personligt AI-coachmeddelande EN gång per session (cachad)
  const [hasFetchedCoach, setHasFetchedCoach] = useState(false);

  useEffect(() => {
    if (!userId || hasFetchedCoach) return;

    // Kolla sessionStorage-cache först
    const cached = sessionStorage.getItem(`coach_message_${userId}`);
    if (cached) {
      setDailyCoachMessage(cached);
      setHasFetchedCoach(true);
      return;
    }

    const fetchCoachMessage = async () => {
      setCoachLoading(true);
      try {
        const contextMessage = t('dashboard.coachPrompt', {
          level: userProgress.level,
          points: userProgress.points,
          activeChallenges: activeChallenges.length,
        });
        const tipText = await getDailyCoachTip(userId, contextMessage);
        setDailyCoachMessage(tipText);
        sessionStorage.setItem(`coach_message_${userId}`, response.response);
      } catch {
        const messages = t('dashboard.coachMessages', { returnObjects: true }) as string[];
        const randomMessage = messages[Math.floor(Math.random() * messages.length)];
        setDailyCoachMessage(randomMessage);
      } finally {
        setCoachLoading(false);
        setHasFetchedCoach(true);
      }
    };

    fetchCoachMessage();
  }, [userId, hasFetchedCoach]);

  const progressToNextLevel = userProgress.points % 100;

  // Mappa backend-kategori till i18n-nyckel
  const getCategoryKey = (category: string): string => {
    const map: Record<string, string> = {
      MENTAL_HEALTH: "mentalHealth",
      PHYSICAL_ACTIVITY: "physicalActivity",
      FOCUS_DISCIPLINE: "focusDiscipline",
      PERSONAL_DEVELOPMENT: "personalDevelopment",
    };
    return map[category] || category;
  };

  // Mappa backend-kategori till URL-slug
  const getCategorySlug = (category: string): string => {
    const map: Record<string, string> = {
      MENTAL_HEALTH: "mental-health",
      PHYSICAL_ACTIVITY: "physical-activity",
      FOCUS_DISCIPLINE: "focus-discipline",
      PERSONAL_DEVELOPMENT: "personal-development",
    };
    return map[category] || category.toLowerCase();
  };

  // Time-aware greeting
  const getTimeGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 6) return t('dashboard.greetingNight', { defaultValue: 'God natt' });
    if (hour < 12) return t('dashboard.greetingMorning', { defaultValue: 'God morgon' });
    if (hour < 17) return t('dashboard.greetingAfternoon', { defaultValue: 'God eftermiddag' });
    return t('dashboard.greetingEvening', { defaultValue: 'God kv\u00e4ll' });
  };

  const getTimeGradient = () => {
    const hour = new Date().getHours();
    if (hour < 6) return "from-indigo-500/8 via-background to-blue-500/5";
    if (hour < 12) return "from-amber-500/8 via-background to-orange-500/5";
    if (hour < 17) return "from-primary/5 via-background to-secondary/5";
    return "from-blue-500/8 via-background to-indigo-500/5";
  };

  if (!user) {
    return (
      <div className="flex items-center justify-center min-h-full">
        <p>{t('dashboard.loadingUser')}</p>
      </div>
    );
  }

  return (
    <div className={`min-h-full bg-gradient-to-br ${getTimeGradient()} p-4 pt-4`}>
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Debug info */}
        {!userId && (
          <Card className="p-4 bg-red-50 border-red-200">
            <p className="text-red-700 text-sm">{t('dashboard.debugWarning')}</p>
          </Card>
        )}

        {/* Header with time-aware greeting */}
        <div className="pt-2">
          <h1 className="text-3xl font-bold text-foreground mb-2">
            {getTimeGreeting()}{user.name ? `, ${user.name.split(' ')[0]}` : ''} {'\u{1F44B}'}
          </h1>
          <p className="text-muted-foreground">{t('dashboard.subtitle')}</p>
        </div>

        {/* Daily Check-In */}
        {userId && <DailyCheckIn userId={userId} onCheckIn={handleCheckIn} />}

        {/* Daily Reward (Mystery Box) — visas efter check-in */}
        {userId && dailyReward && !dailyReward.claimed && (
          <DailyRewardBox
            userId={userId}
            reward={dailyReward}
            onClaimed={() => {
              setDailyReward((prev) => prev ? { ...prev, claimed: true } : null);
              fetchUserProgress(); // Uppdatera XP
            }}
          />
        )}

        {/* Sobriety Counter */}
        {userId && <SobrietyCounter userId={userId} onMilestone={handleMilestone} />}

        {/* Level & Points Card */}
        <Card className="p-6 bg-gradient-to-br from-card to-muted/20 shadow-[var(--shadow-card)] animate-fade-in-up stagger-1 card-hover">
          <div className="flex items-center justify-between mb-4">
            <div>
              <p className="text-sm text-muted-foreground">{t('dashboard.level')}</p>
              <p className="text-4xl font-bold text-primary">{userProgress.level}</p>
            </div>
            <Badge className="bg-accent text-accent-foreground text-lg px-4 py-2">
              {userProgress.points} {t('common.points')}
            </Badge>
          </div>
          <div className="space-y-2">
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">{t('dashboard.progressToNext')}</span>
              <span className="text-foreground font-medium">{progressToNextLevel}/100</span>
            </div>
            <div className="relative">
              <Progress value={progressToNextLevel} className="h-3" />
            </div>
          </div>
        </Card>

        {/* Daily Coach Message */}
        {coachLoading ? (
          <CoachMessageSkeleton />
        ) : (
          <Card className="p-6 bg-gradient-to-br from-primary/10 to-secondary/10 border-primary/20 shadow-[var(--shadow-glow)] animate-fade-in-up stagger-2 card-hover">
            <div className="flex items-start gap-4">
              <div className="w-12 h-12 rounded-2xl bg-primary/20 flex items-center justify-center flex-shrink-0">
                <Sparkles className="w-6 h-6 text-primary" />
              </div>
              <div className="flex-1">
                <h3 className="font-semibold text-lg mb-2 text-foreground">{t('dashboard.aiCoachSays')}</h3>
                <p className="text-foreground/90">{formatMarkdown(dailyCoachMessage)}</p>
              </div>
            </div>
          </Card>
        )}

        {/* Today's Active Challenges */}
        <div className="space-y-4">
          <h2 className="text-xl font-semibold text-foreground">{t('dashboard.todayActivities')}</h2>
          <div className="grid gap-4">
            {challengesLoading ? (
              <ActiveChallengesSkeleton />
            ) : activeChallenges.length === 0 ? (
              <Card className="p-6 text-center">
                <Target className="w-10 h-10 mx-auto mb-3 text-muted-foreground" />
                <p className="text-muted-foreground mb-4">{t('dashboard.noActiveChallenges')}</p>
                <Button onClick={() => navigate("/challenges")} className="gap-2">
                  {t('dashboard.browseChallenges')}
                  <ArrowRight className="w-4 h-4" />
                </Button>
              </Card>
            ) : (
              activeChallenges.map((challenge) => (
                <Card key={challenge.id} className="p-4 hover:shadow-lg transition-all">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-4">
                      <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
                        <Target className="w-5 h-5 text-primary" />
                      </div>
                      <div>
                        <p className="font-medium text-foreground">{challenge.challengeName}</p>
                        <p className="text-sm text-muted-foreground">
                          {t(`challenges.categories.${getCategoryKey(challenge.category)}`)}{challenge.difficulty && ` · ${t(`challenges.difficulty.${challenge.difficulty.toLowerCase()}`)}`}{challenge.durationMinutes ? ` · ${challenge.durationMinutes} min` : ""}
                        </p>
                      </div>
                    </div>
                    <Button
                      onClick={() => navigate(`/challenges/${getCategorySlug(challenge.category)}/${challenge.challengeId}`)}
                      variant="outline"
                      className="gap-2"
                    >
                      {t('dashboard.goToChallenge')}
                      <ArrowRight className="w-4 h-4" />
                    </Button>
                  </div>
                </Card>
              ))
            )}
          </div>
        </div>
      </div>

      {/* SOS Button */}
      <SOSButton />

      {/* Milestone Celebration */}
      <MilestoneCelebration
        days={milestoneDays}
        onClose={() => setMilestoneDays(null)}
      />
    </div>
  );
};

export default Dashboard;
