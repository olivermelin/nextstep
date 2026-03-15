import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { Badge } from "@/components/ui/badge";
import { Sparkles, ArrowRight, Target, Loader2 } from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { API_ENDPOINTS } from "@/config/api";
import { useTranslation } from "react-i18next";
import { sendCoachMessage } from "@/services/coachService";
import { userChallengeApi } from "@/services/challengeService";
import { formatMarkdown } from "@/components/AIChat";
import { UserChallengeOutDto } from "@/types/challenge";

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

  // Härledd userId från AuthContext (email prioriterat)
  const userId = user?.email || user?.id || null;

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
    } catch (error) {
      console.error("Fel vid hämtning av framsteg:", error);
    }
  };

  // Hämta aktiva challenges för Today's Activities
  const fetchActiveChallenges = async () => {
    if (!userId) return;
    setChallengesLoading(true);
    try {
      const data = await userChallengeApi.getUserActiveChallenges(userId);
      setActiveChallenges(data);
    } catch (error) {
      console.error("Fel vid hämtning av aktiva challenges:", error);
    } finally {
      setChallengesLoading(false);
    }
  };

  // Hämta personligt AI-coachmeddelande baserat på framsteg
  useEffect(() => {
    if (userId && userProgress) {
      fetchCoachMessage();
    }
  }, [userId, userProgress.points, userProgress.level]);

  const fetchCoachMessage = async () => {
    if (!userId) return;
    setCoachLoading(true);
    try {
      const contextMessage = t('dashboard.coachPrompt', {
        level: userProgress.level,
        points: userProgress.points,
        activeChallenges: activeChallenges.length,
      });
      const response = await sendCoachMessage(userId, contextMessage, null);
      setDailyCoachMessage(response.response);
    } catch (error) {
      console.error("Fel vid hämtning av coachmeddelande:", error);
      // Fallback till en slumpmässig statisk fras
      const messages = t('dashboard.coachMessages', { returnObjects: true }) as string[];
      const randomMessage = messages[Math.floor(Math.random() * messages.length)];
      setDailyCoachMessage(randomMessage);
    } finally {
      setCoachLoading(false);
    }
  };

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

  if (!user) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p>{t('dashboard.loadingUser')}</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary/5 via-background to-secondary/5 p-4 pb-20 pt-4">
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Debug info */}
        {!userId && (
          <Card className="p-4 bg-red-50 border-red-200">
            <p className="text-red-700 text-sm">{t('dashboard.debugWarning')}</p>
          </Card>
        )}

        {/* Header */}
        <div className="pt-2">
          <h1 className="text-3xl font-bold text-foreground mb-2">{t('dashboard.title')}</h1>
          <p className="text-muted-foreground">{t('dashboard.subtitle')}</p>
        </div>

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
        <Card className="p-6 bg-gradient-to-br from-primary/10 to-secondary/10 border-primary/20 shadow-[var(--shadow-glow)] animate-fade-in-up stagger-2 card-hover">
          <div className="flex items-start gap-4">
            <div className="w-12 h-12 rounded-2xl bg-primary/20 flex items-center justify-center flex-shrink-0">
              <Sparkles className="w-6 h-6 text-primary" />
            </div>
            <div className="flex-1">
              <h3 className="font-semibold text-lg mb-2 text-foreground">{t('dashboard.aiCoachSays')}</h3>
              {coachLoading ? (
                <div className="flex items-center gap-2 text-muted-foreground">
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>{t('dashboard.coachThinking')}</span>
                </div>
              ) : (
                <p className="text-foreground/90">{formatMarkdown(dailyCoachMessage)}</p>
              )}
            </div>
          </div>
        </Card>

        {/* Today's Active Challenges */}
        <div className="space-y-4">
          <h2 className="text-xl font-semibold text-foreground">{t('dashboard.todayActivities')}</h2>
          <div className="grid gap-4">
            {challengesLoading ? (
              <Card className="p-6 flex justify-center">
                <Loader2 className="w-6 h-6 animate-spin text-primary" />
              </Card>
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
    </div>
  );
};

export default Dashboard;
