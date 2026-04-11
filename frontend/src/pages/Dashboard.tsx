import { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { Badge } from "@/components/ui/badge";
import { Sparkles, ArrowRight, Target, Clock, Play, ChevronRight, RefreshCw } from "lucide-react";
import { CoachMessageSkeleton } from "@/components/skeletons/DashboardSkeleton";
import { useAuth } from "@/context/AuthContext";
import { API_ENDPOINTS } from "@/config/api";
import { useTranslation } from "react-i18next";
import { getDailyCoachTip } from "@/services/coachService";
import { challengeApi, userChallengeApi } from "@/services/challengeService";
import { checkInService, CheckInData } from "@/services/checkInService";
import { formatMarkdown } from "@/components/AIChat";
import { ChallengeOutDto } from "@/types/challenge";
import SobrietyCounter from "@/components/SobrietyCounter";
import DailyCheckIn from "@/components/DailyCheckIn";
import DailyRewardBox from "@/components/DailyRewardBox";
import { rewardService } from "@/services/rewardService";
import DailyRitualProgress from "@/components/DailyRitualProgress";
import StreakCalendar from "@/components/StreakCalendar";
import StreakWarning from "@/components/StreakWarning";
import MilestoneCelebration from "@/components/MilestoneCelebration";
import WelcomeCard from "@/components/WelcomeCard";
import { streakService, StreakData } from "@/services/streakService";

interface UserProgress {
  points: number;
  level: number;
}

const SUGGESTION_COUNT = 3;

const Dashboard = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { t } = useTranslation();

  const [userProgress, setUserProgress] = useState<UserProgress>({ points: 0, level: 1 });
  const [dailyCoachMessage, setDailyCoachMessage] = useState("");
  const [coachLoading, setCoachLoading] = useState(false);
  const [todayCheckIn, setTodayCheckIn] = useState<CheckInData | null | undefined>(undefined);
  const [streakData, setStreakData] = useState<StreakData | null>(null);
  const [rewardClaimed, setRewardClaimed] = useState(false);
  const [challengeCompletedToday, setChallengeCompletedToday] = useState(false);

  // Föreslagna challenges
  const [suggestedChallenges, setSuggestedChallenges] = useState<ChallengeOutDto[]>([]);
  const [suggestionsLoading, setSuggestionsLoading] = useState(false);

  const userId = user?.email || user?.id || null;

  const handleCheckIn = useCallback((checkIn: CheckInData) => {
    setTodayCheckIn(checkIn);
  }, []);

  // Mappa backend-kategori till i18n-nyckel
  const getCategoryKey = (category: string): string => {
    const map: Record<string, string> = {
      MENTAL_HEALTH: "mentalHealth",
      PHYSICAL_ACTIVITY: "physicalActivity",
      FOCUS_DISCIPLINE: "focusDiscipline",
      PERSONAL_DEVELOPMENT: "personalDevelopment",
      DRAWING_EXERCISES: "drawingExercises",
      HEALTHY_HABITS: "healthyHabits",
      SOCIAL_SKILLS: "socialSkills",
      EMOTIONAL_AWARENESS: "emotionalAwareness",
    };
    return map[category] || category;
  };

  const getCategorySlug = (category: string): string => {
    const map: Record<string, string> = {
      MENTAL_HEALTH: "mental-health",
      PHYSICAL_ACTIVITY: "physical-activity",
      FOCUS_DISCIPLINE: "focus-discipline",
      PERSONAL_DEVELOPMENT: "personal-development",
      DRAWING_EXERCISES: "drawing-exercises",
      HEALTHY_HABITS: "healthy-habits",
      SOCIAL_SKILLS: "social-skills",
      EMOTIONAL_AWARENESS: "emotional-awareness",
    };
    return map[category] || category.toLowerCase().replace(/_/g, "-");
  };

  // Hämta föreslagna challenges — slumpa bland ej slutförda idag
  const fetchSuggestions = useCallback(async () => {
    if (!userId) return;
    setSuggestionsLoading(true);
    try {
      const allChallenges = await challengeApi.getUserChallenges(userId);
      const available = allChallenges.filter(c => !c.completedToday);
      // Slumpa
      const shuffled = [...available].sort(() => Math.random() - 0.5);
      setSuggestedChallenges(shuffled.slice(0, SUGGESTION_COUNT));
    } catch {
      // Tyst fel
    } finally {
      setSuggestionsLoading(false);
    }
  }, [userId]);

  // Hämta all data vid mount
  useEffect(() => {
    if (!userId) return;

    // Progress
    fetch(API_ENDPOINTS.PROGRESS.GET_USER_PROGRESS(userId), { credentials: "include" })
      .then(r => r.json())
      .then(d => setUserProgress({ points: d.totalPoints || 0, level: d.level || 1 }))
      .catch(() => {});

    // Dagens incheckning
    checkInService.getToday(userId).then(setTodayCheckIn);

    // Streak
    streakService.getStreak(userId).then(setStreakData).catch(() => {});

    // Slutförda challenges idag
    userChallengeApi.getUserCompletedChallenges(userId).then(completed => {
      const today = new Date().toISOString().slice(0, 10);
      setChallengeCompletedToday(completed.some(c => c.completedAt?.startsWith(today)));
    }).catch(() => {});

    // Belöningsstatus
    rewardService.getToday(userId).then(reward => {
      if (reward?.claimed) setRewardClaimed(true);
    }).catch(() => {});

    // Föreslagna challenges
    fetchSuggestions();
  }, [userId, fetchSuggestions]);

  // Coach-meddelande (cachad per dag)
  const [hasFetchedCoach, setHasFetchedCoach] = useState(false);
  useEffect(() => {
    if (!userId || hasFetchedCoach) return;
    const today = new Date().toISOString().slice(0, 10);
    const cacheKey = `coach_message_${userId}_${today}`;
    const cached = sessionStorage.getItem(cacheKey);
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
          activeChallenges: suggestedChallenges.length,
        });
        const tipText = await getDailyCoachTip(userId, contextMessage);
        setDailyCoachMessage(tipText);
        sessionStorage.setItem(cacheKey, tipText);
      } catch {
        const messages = t('dashboard.coachMessages', { returnObjects: true }) as string[];
        setDailyCoachMessage(messages[Math.floor(Math.random() * messages.length)]);
      } finally {
        setCoachLoading(false);
        setHasFetchedCoach(true);
      }
    };
    fetchCoachMessage();
  }, [userId, hasFetchedCoach]);

  const progressToNextLevel = userProgress.points % 100;

  // Ny användare: level 1 och 0 poäng och välkomstkort inte visats
  const isNewUser =
    userId !== null &&
    userProgress.level === 1 &&
    userProgress.points === 0 &&
    !localStorage.getItem(`welcome_shown_${userId}`);

  // Tidsbaserad hälsning
  const getTimeGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 6) return t('dashboard.greetingNight', { defaultValue: 'God natt' });
    if (hour < 12) return t('dashboard.greetingMorning', { defaultValue: 'God morgon' });
    if (hour < 17) return t('dashboard.greetingAfternoon', { defaultValue: 'God eftermiddag' });
    return t('dashboard.greetingEvening', { defaultValue: 'God kväll' });
  };

  const getTimeGradient = () => {
    const hour = new Date().getHours();
    if (hour < 6) return "from-indigo-500/8 via-background to-blue-500/5";
    if (hour < 12) return "from-amber-500/8 via-background to-orange-500/5";
    if (hour < 17) return "from-primary/5 via-background to-secondary/5";
    return "from-blue-500/8 via-background to-indigo-500/5";
  };

  const getDifficultyColor = (difficulty: string) => {
    switch (difficulty) {
      case "EASY": return "text-green-600 bg-green-100 dark:text-green-400 dark:bg-green-900/30";
      case "MEDIUM": return "text-amber-600 bg-amber-100 dark:text-amber-400 dark:bg-amber-900/30";
      case "HARD": return "text-red-600 bg-red-100 dark:text-red-400 dark:bg-red-900/30";
      default: return "text-muted-foreground bg-muted";
    }
  };

  if (!user) {
    return (
      <div className="flex items-center justify-center min-h-full">
        <p>{t('dashboard.loadingUser')}</p>
      </div>
    );
  }

  return (
    <div className={`min-h-full bg-gradient-to-br ${getTimeGradient()} p-4 pt-4 pb-28`}>
      <div className="max-w-5xl mx-auto space-y-5">

        {/* ── Hälsning ── */}
        <div className="pt-2">
          <h1 className="text-3xl font-bold text-foreground mb-1">
            {getTimeGreeting()}{user.name ? `, ${user.name.split(' ')[0]}` : ''} {'\u{1F44B}'}
          </h1>
          <p className="text-muted-foreground">{t('dashboard.subtitle')}</p>
        </div>

        {/* ── Välkomstkort för nya användare ── */}
        {isNewUser && userId && (
          <WelcomeCard userId={userId} userName={user.name} />
        )}

        {/* ── Milstolpe-firande ── */}
        <MilestoneCelebration streakData={streakData} />

        {/* ── Streak-varning (kväll) ── */}
        {streakData && (
          <StreakWarning streakData={streakData} checkedInToday={!!todayCheckIn} />
        )}

        {/* ── Dagens rutin ── */}
        {userId && todayCheckIn !== undefined && (
          <DailyRitualProgress
            checkedIn={!!todayCheckIn}
            challengeCompleted={challengeCompletedToday}
            rewardClaimed={rewardClaimed}
          />
        )}

        {/* ── Tvåkolumns-layout ── */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">

          {/* ── Vänsterkolumn: huvudinnehåll ── */}
          <div className="lg:col-span-2 space-y-5">

            {/* Incheckning */}
            {userId && todayCheckIn !== undefined && (
              <DailyCheckIn
                userId={userId}
                existingCheckIn={todayCheckIn}
                onCheckInComplete={handleCheckIn}
              />
            )}

            {/* Coach-meddelande — direkt efter incheckning */}
            {coachLoading ? (
              <CoachMessageSkeleton />
            ) : dailyCoachMessage && (
              <Card className="p-5 bg-gradient-to-br from-primary/10 to-secondary/10 border-primary/20">
                <div className="flex items-start gap-4">
                  <div className="w-11 h-11 rounded-2xl bg-primary/20 flex items-center justify-center flex-shrink-0">
                    <Sparkles className="w-5 h-5 text-primary" />
                  </div>
                  <div className="flex-1">
                    <h3 className="font-semibold text-base mb-1.5 text-foreground">{t('dashboard.aiCoachSays')}</h3>
                    <div className="text-foreground/90 text-sm leading-relaxed">{formatMarkdown(dailyCoachMessage)}</div>
                  </div>
                </div>
              </Card>
            )}

            {/* Föreslagna utmaningar */}
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <h2 className="text-lg font-semibold text-foreground">
                  {t('dashboard.suggestedChallenges', { defaultValue: 'Förslag för idag' })}
                </h2>
                <button
                  onClick={fetchSuggestions}
                  className="flex items-center gap-1.5 text-xs text-muted-foreground hover:text-foreground transition-colors"
                  title={t('dashboard.refreshSuggestions', { defaultValue: 'Nya förslag' })}
                >
                  <RefreshCw className="w-3.5 h-3.5" />
                  {t('dashboard.newSuggestions', { defaultValue: 'Nya förslag' })}
                </button>
              </div>

              {suggestionsLoading ? (
                <div className="grid gap-3">
                  {[1, 2, 3].map(i => (
                    <Card key={i} className="p-4 animate-pulse">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-xl bg-muted" />
                        <div className="flex-1 space-y-2">
                          <div className="h-4 bg-muted rounded w-3/4" />
                          <div className="h-3 bg-muted rounded w-1/2" />
                        </div>
                      </div>
                    </Card>
                  ))}
                </div>
              ) : suggestedChallenges.length === 0 ? (
                <Card className="p-5 text-center">
                  <Target className="w-9 h-9 mx-auto mb-2 text-primary/50" />
                  <p className="text-sm font-medium text-foreground mb-1">
                    {t('dashboard.allChallengesDone', { defaultValue: 'Alla utmaningar klara idag!' })}
                  </p>
                  <p className="text-xs text-muted-foreground">
                    {t('dashboard.comeBackTomorrow', { defaultValue: 'Bra jobbat. Kom tillbaka imorgon för nya.' })}
                  </p>
                </Card>
              ) : (
                <div className="grid gap-3">
                  {suggestedChallenges.map((challenge) => (
                    <Card
                      key={challenge.id}
                      className="p-4 hover:border-primary/30 transition-colors cursor-pointer group"
                      onClick={() => navigate(`/challenges/${getCategorySlug(challenge.category)}/${challenge.id}`)}
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center flex-shrink-0 group-hover:bg-primary/20 transition-colors">
                          <Target className="w-5 h-5 text-primary" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="font-medium text-sm text-foreground group-hover:text-primary transition-colors">
                            {challenge.title}
                          </p>
                          <div className="flex items-center gap-2 mt-0.5">
                            <span className={`text-[10px] font-medium px-1.5 py-0.5 rounded ${getDifficultyColor(challenge.difficulty)}`}>
                              {t(`challenges.difficulty.${challenge.difficulty.toLowerCase()}`)}
                            </span>
                            <span className="text-xs text-muted-foreground flex items-center gap-0.5">
                              <Clock className="w-3 h-3" />
                              {challenge.durationMinutes} min
                            </span>
                            <span className="text-xs text-muted-foreground">
                              {t(`challenges.categories.${getCategoryKey(challenge.category)}`)}
                            </span>
                          </div>
                        </div>
                        <ChevronRight className="w-4 h-4 text-muted-foreground group-hover:text-primary transition-colors flex-shrink-0" />
                      </div>
                    </Card>
                  ))}
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => navigate("/challenges")}
                    className="w-full text-muted-foreground hover:text-foreground gap-1.5"
                  >
                    {t('dashboard.seeAllChallenges', { defaultValue: 'Se alla utmaningar' })}
                    <ArrowRight className="w-3.5 h-3.5" />
                  </Button>
                </div>
              )}
            </div>

          </div>

          {/* ── Högerkolumn: widgets ── */}
          <div className="space-y-5">

            {/* Level & poäng */}
            <Card className="p-5 bg-gradient-to-br from-card to-muted/20">
              <div className="flex items-center justify-between mb-3">
                <div>
                  <p className="text-xs text-muted-foreground">{t('dashboard.level')}</p>
                  <p className="text-3xl font-bold text-primary">{userProgress.level}</p>
                </div>
                <Badge className="bg-accent text-accent-foreground text-sm px-3 py-1.5">
                  {userProgress.points} {t('common.points')}
                </Badge>
              </div>
              <div className="space-y-1.5">
                <div className="flex justify-between text-xs">
                  <span className="text-muted-foreground">{t('dashboard.progressToNext')}</span>
                  <span className="text-foreground font-medium">{progressToNextLevel}/100</span>
                </div>
                <Progress value={progressToNextLevel} className="h-2.5" />
              </div>
            </Card>

            {/* Streak-kalender */}
            {streakData && <StreakCalendar streakData={streakData} />}

            {/* Nykterhetstimer */}
            {userId && user.onboardingTrack === 'RECOVERY' && (
              <SobrietyCounter userId={userId} />
            )}

            {/* Daglig belöning */}
            {userId && <DailyRewardBox userId={userId} onClaimStatusChange={setRewardClaimed} />}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
