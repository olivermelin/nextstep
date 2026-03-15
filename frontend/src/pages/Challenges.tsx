import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Brain,
  Activity,
  Focus,
  Lightbulb,
  Pencil,
  ArrowLeft,
  CheckCircle2,
  Clock,
  Play,
  BookOpen,
  Youtube,
  Loader2,
  Trophy,
  Target
} from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { challengeApi, userChallengeApi } from "@/services/challengeService";
import { useToast } from "@/components/ui/use-toast";
import DrawingCanvas from "@/components/DrawingCanvas";
import { useActivityTimer, clearChallengeTimer } from "@/hooks/useActivityTimer";
import { useYouTubeProgress, extractYouTubeVideoId } from "@/hooks/useYouTubeProgress";
import { Progress } from "@/components/ui/progress";
import {
  ChallengeOutDto,
  UserChallengeOutDto,
  ChallengeCategory,
  ChallengeDifficulty,
} from "@/types/challenge";

// Mappning mellan URL-slugs och backend-kategorier
const categorySlugMap: Record<string, ChallengeCategory> = {
  "mental-health": ChallengeCategory.MENTAL_HEALTH,
  "physical-activity": ChallengeCategory.PHYSICAL_ACTIVITY,
  "focus-discipline": ChallengeCategory.FOCUS_DISCIPLINE,
  "personal-development": ChallengeCategory.PERSONAL_DEVELOPMENT,
  "drawing-exercises": ChallengeCategory.DRAWING_EXERCISES,
};

const categoryToSlug: Record<ChallengeCategory, string> = {
  [ChallengeCategory.MENTAL_HEALTH]: "mental-health",
  [ChallengeCategory.PHYSICAL_ACTIVITY]: "physical-activity",
  [ChallengeCategory.FOCUS_DISCIPLINE]: "focus-discipline",
  [ChallengeCategory.PERSONAL_DEVELOPMENT]: "personal-development",
  [ChallengeCategory.DRAWING_EXERCISES]: "drawing-exercises",
};

const Challenges = () => {
  const { t } = useTranslation();
  const { user } = useAuth();
  const { toast } = useToast();
  const { categorySlug, challengeId } = useParams<{ categorySlug?: string; challengeId?: string }>();
  const navigate = useNavigate();

  // Härledd viewMode från URL-params
  const selectedCategory = categorySlug ? categorySlugMap[categorySlug] || null : null;
  const viewMode: "categories" | "challenges" | "activity" = challengeId ? "activity" : categorySlug ? "challenges" : "categories";

  const [selectedChallenge, setSelectedChallenge] = useState<ChallengeOutDto | null>(null);
  const [challenges, setChallenges] = useState<ChallengeOutDto[]>([]);
  const [allChallenges, setAllChallenges] = useState<ChallengeOutDto[]>([]);
  const [userChallenges, setUserChallenges] = useState<UserChallengeOutDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<"available" | "active" | "completed">("available");
  const [alreadyDone, setAlreadyDone] = useState(false);

  const categories = [
    { 
      id: ChallengeCategory.MENTAL_HEALTH, 
      name: t('challenges.categories.mentalHealth'), 
      icon: Brain, 
      color: "text-blue-500", 
      borderColor: "border-blue-500/30", 
      bgColor: "from-blue-500/10 to-blue-500/5" 
    },
    { 
      id: ChallengeCategory.PHYSICAL_ACTIVITY, 
      name: t('challenges.categories.physicalActivity'), 
      icon: Activity, 
      color: "text-red-500", 
      borderColor: "border-red-500/30", 
      bgColor: "from-red-500/10 to-red-500/5" 
    },
    { 
      id: ChallengeCategory.FOCUS_DISCIPLINE, 
      name: t('challenges.categories.focusDiscipline'), 
      icon: Focus, 
      color: "text-purple-500", 
      borderColor: "border-purple-500/30", 
      bgColor: "from-purple-500/10 to-purple-500/5" 
    },
    {
      id: ChallengeCategory.PERSONAL_DEVELOPMENT,
      name: t('challenges.categories.personalDevelopment'),
      icon: Lightbulb,
      color: "text-amber-500",
      borderColor: "border-amber-500/30",
      bgColor: "from-amber-500/10 to-amber-500/5"
    },
    {
      id: ChallengeCategory.DRAWING_EXERCISES,
      name: t('challenges.categories.drawingExercises'),
      icon: Pencil,
      color: "text-teal-500",
      borderColor: "border-teal-500/30",
      bgColor: "from-teal-500/10 to-teal-500/5"
    },
  ];

  // Ladda användarens challenges vid mount
  useEffect(() => {
    if (user?.id || user?.email) {
      loadUserChallenges();
      loadAllChallenges();
    }
  }, [user?.id, user?.email]);

  // Ladda challenges för vald kategori när categorySlug ändras
  useEffect(() => {
    if (selectedCategory) {
      loadChallengesByCategory(selectedCategory);
    }
  }, [selectedCategory]);

  // Ladda specifik challenge när challengeId finns i URL:en
  useEffect(() => {
    setAlreadyDone(false);
    if (challengeId) {
      const openChallenge = async () => {
        try {
          const fullChallenge = await challengeApi.getChallengeById(Number(challengeId));
          setSelectedChallenge(fullChallenge);

          // Auto-starta challenge (backend returnerar befintlig om redan startad)
          const userId = user?.email || user?.id;
          if (userId && !fullChallenge.completedToday) {
            try {
              await userChallengeApi.startChallenge(userId, fullChallenge.id);
              await loadUserChallenges();
            } catch (startErr: any) {
              const msg = startErr?.message || '';
              if (!msg.includes('already completed')) {
                console.warn("Kunde inte auto-starta challenge:", startErr);
              }
            }
          }
        } catch (err) {
          console.error("Kunde inte ladda challenge:", err);
          navigate("/challenges", { replace: true });
        }
      };
      openChallenge();
    } else {
      setSelectedChallenge(null);
      // Ladda om användarens challenges när man navigerar tillbaka
      if (user?.id || user?.email) {
        loadUserChallenges();
      }
    }
  }, [challengeId]);

  const loadUserChallenges = async () => {
    if (!user?.email && !user?.id) return;
    const userId = user.email || user.id;
    
    try {
      const data = await userChallengeApi.getUserChallenges(userId);
      setUserChallenges(data);
    } catch (err) {
      console.error("Kunde inte ladda användarens challenges:", err);
    }
  };

  const loadAllChallenges = async () => {
    if (!user?.email && !user?.id) return;
    const userId = user.email || user.id;
    
    try {
      const data = await challengeApi.getUserChallenges(userId);
      setAllChallenges(data);
    } catch (err) {
      console.error("Kunde inte ladda alla challenges:", err);
    }
  };

  const loadChallengesByCategory = async (category: ChallengeCategory) => {
    setLoading(true);
    setError(null);

    try {
      const userId = user?.email || user?.id;
      let data: ChallengeOutDto[];
      if (userId) {
        // Använd user-endpoint för att få completedToday-status
        const allUserChallenges = await challengeApi.getUserChallenges(userId);
        data = allUserChallenges.filter(c => c.category === category);
      } else {
        data = await challengeApi.getChallengesByCategory(category);
      }
      setChallenges(data);
    } catch (err) {
      setError(t('challenges.couldNotLoadChallenges'));
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSelectCategory = (category: ChallengeCategory) => {
    navigate(`/challenges/${categoryToSlug[category]}`);
  };

  const handleStartChallenge = async (challenge: ChallengeOutDto) => {
    if (!user?.email && !user?.id) {
      setError(t('challenges.mustBeLoggedIn'));
      return;
    }

    // Blockera om en annan challenge redan pågår
    const activeChallenge = getActiveChallenges().find(uc => uc.challengeId !== challenge.id);
    if (activeChallenge) {
      toast({
        title: "Du har redan en pågående aktivitet",
        description: `Slutför "${activeChallenge.challengeName}" först innan du startar en ny.`,
      });
      return;
    }

    setLoading(true);
    setError(null);

    try {
      await userChallengeApi.startChallenge(user.email || user.id, challenge.id);
      await loadUserChallenges();

      // Navigera till aktivitets-URL:en
      const slug = categoryToSlug[challenge.category];
      navigate(`/challenges/${slug}/${challenge.id}`);
    } catch (err: any) {
      const msg = err?.message || '';
      if (msg.includes('already completed today') || msg.includes('Challenge already completed')) {
        toast({
          title: t('challenges.alreadyCompletedTodayTitle'),
          description: t('challenges.alreadyCompletedTodayDesc'),
        });
        // Markera denna challenge som completedToday lokalt
        setChallenges(prev => prev.map(c => c.id === challenge.id ? { ...c, completedToday: true } : c));
        setAllChallenges(prev => prev.map(c => c.id === challenge.id ? { ...c, completedToday: true } : c));
      } else if (msg.includes('pågående aktivitet')) {
        toast({
          title: "Du har redan en pågående aktivitet",
          description: "Slutför din pågående aktivitet innan du startar en ny.",
        });
      } else {
        setError(t('challenges.couldNotStart'));
      }
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleCompleteChallenge = async () => {
    if ((!user?.email && !user?.id) || !selectedChallenge) return;
    const userId = user.email || user.id;

    setLoading(true);
    setError(null);

    try {
      await userChallengeApi.completeChallenge(userId, selectedChallenge.id);
      await loadUserChallenges();

      // Rensa sparad timer
      clearChallengeTimer(selectedChallenge.id);

      // Markera denna challenge som completedToday lokalt
      setChallenges(prev => prev.map(c => c.id === selectedChallenge.id ? { ...c, completedToday: true } : c));
      setAllChallenges(prev => prev.map(c => c.id === selectedChallenge.id ? { ...c, completedToday: true } : c));

      // Gå tillbaka till kategori-vyn
      if (categorySlug) {
        navigate(`/challenges/${categorySlug}`);
      } else {
        navigate("/challenges");
      }
    } catch (err: any) {
      const msg = err?.message || '';
      if (msg.includes('already completed today') || msg.includes('Challenge already completed')) {
        toast({
          title: t('challenges.alreadyCompletedTodayTitle'),
          description: t('challenges.alreadyCompletedTodayDesc'),
        });
        setChallenges(prev => prev.map(c => c.id === selectedChallenge.id ? { ...c, completedToday: true } : c));
        setAllChallenges(prev => prev.map(c => c.id === selectedChallenge.id ? { ...c, completedToday: true } : c));
        if (categorySlug) {
          navigate(`/challenges/${categorySlug}`);
        } else {
          navigate("/challenges");
        }
      } else {
        setError(t('challenges.couldNotComplete'));
      }
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleBackToCategories = () => {
    navigate("/challenges");
  };

  const handleBackToChallenges = () => {
    if (categorySlug) {
      navigate(`/challenges/${categorySlug}`);
    } else {
      navigate("/challenges");
    }
  };

  const getDifficultyLabel = (difficulty: ChallengeDifficulty): string => {
    switch (difficulty) {
      case ChallengeDifficulty.EASY:
        return t('challenges.difficulty.easy');
      case ChallengeDifficulty.MEDIUM:
        return t('challenges.difficulty.medium');
      case ChallengeDifficulty.HARD:
        return t('challenges.difficulty.hard');
      default:
        return difficulty;
    }
  };

  const getDifficultyColor = (difficulty: ChallengeDifficulty) => {
    switch (difficulty) {
      case ChallengeDifficulty.EASY:
        return "bg-green-500/20 text-green-700 border-green-500/30";
      case ChallengeDifficulty.MEDIUM:
        return "bg-amber-500/20 text-amber-700 border-amber-500/30";
      case ChallengeDifficulty.HARD:
        return "bg-red-500/20 text-red-700 border-red-500/30";
      default:
        return "";
    }
  };

  const getCategoryCount = (category: ChallengeCategory) => {
    return challenges.filter(c => c.category === category).length;
  };

  const currentCategory = categories.find(c => c.id === selectedCategory);

  // Mappa backend-kategori till i18n-nyckel
  const getCategoryKey = (category: string): string => {
    const map: Record<string, string> = {
      MENTAL_HEALTH: "mentalHealth",
      PHYSICAL_ACTIVITY: "physicalActivity",
      FOCUS_DISCIPLINE: "focusDiscipline",
      PERSONAL_DEVELOPMENT: "personalDevelopment",
      DRAWING_EXERCISES: "drawingExercises",
    };
    return map[category] || category;
  };

  const isDrawingCategory = (category: ChallengeCategory): boolean => {
    return category === ChallengeCategory.DRAWING_EXERCISES;
  };

  const getActiveChallenges = () => {
    return userChallenges.filter(uc => uc.status === "IN_PROGRESS");
  };

  const getCompletedTodayChallenges = () => {
    return allChallenges.filter(c => c.completedToday);
  };

  const getCompletedChallenges = () => {
    return userChallenges.filter(uc => uc.status === "COMPLETED");
  };

  const isChallengeStarted = (challengeId: number) => {
    return userChallenges.some(
      uc => uc.challengeId === challengeId && uc.status !== "COMPLETED"
    );
  };



  const getYouTubeEmbedUrl = (url: string | null | undefined) => {
    if (!url) return null;
    
    // Konvertera YouTube URL till embed format (med enablejsapi för progress-tracking)
    const videoIdMatch = url.match(/(?:youtube\.com\/watch\?v=|youtu\.be\/)([^&]+)/);
    if (videoIdMatch) {
      return `https://www.youtube.com/embed/${videoIdMatch[1]}?enablejsapi=1&origin=${window.location.origin}`;
    }
    return url;
  };

  // --- Completion gate: timer + YouTube tracking ---
  const isActivityView = viewMode === "activity" && !!selectedChallenge;
  const hasVideo = !!(selectedChallenge?.youtubeUrl);
  const hasInstructions = !!(selectedChallenge?.instructions);
  const isDrawing = selectedChallenge ? isDrawingCategory(selectedChallenge.category) : false;
  const showTimer = isActivityView && !isDrawing && !hasVideo;

  const videoId = extractYouTubeVideoId(selectedChallenge?.youtubeUrl);
  const ytProgress = useYouTubeProgress(videoId, isActivityView && hasVideo);
  const timer = useActivityTimer(selectedChallenge?.durationMinutes || 1, showTimer, selectedChallenge?.id ?? null);

  // Determine unlock condition:
  // - Drawing: always unlocked
  // - Already done (user confirmed): unlocked
  // - Video activities: must watch >= 80% of video
  // - Non-video activities: timer must complete
  const isUnlocked = (() => {
    if (isDrawing) return true;
    if (alreadyDone) return true;
    if (hasVideo) return ytProgress.isComplete;
    return timer.isComplete;
  })();

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary/5 via-background to-secondary/5 p-4 pb-24 pt-4">
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Header */}
        <div className="pt-2">
          <h1 className="text-3xl font-bold text-foreground mb-2">{t('challenges.title')}</h1>
          <p className="text-muted-foreground">
            {viewMode === "categories" && t('challenges.categoriesSubtitle')}
            {viewMode === "challenges" && t('challenges.challengesSubtitle')}
            {viewMode === "activity" && t('challenges.activitySubtitle')}
          </p>
        </div>

        {/* Error Alert */}
        {error && (
          <Alert variant="destructive">
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {/* Back Button */}
        {viewMode === "challenges" && (
          <Button 
            onClick={handleBackToCategories}
            variant="outline"
            className="gap-2"
          >
            <ArrowLeft className="w-4 h-4" />
            {t('challenges.backToCategories')}
          </Button>
        )}

        {viewMode === "activity" && (
          <Button 
            onClick={handleBackToChallenges}
            variant="outline"
            className="gap-2"
          >
            <ArrowLeft className="w-4 h-4" />
            {t('challenges.backToChallenges')}
          </Button>
        )}

        {/* CATEGORIES VIEW */}
        {viewMode === "categories" && (
          <Tabs value={activeTab} onValueChange={(v) => setActiveTab(v as any)} className="w-full">
            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="available">{t('challenges.available')}</TabsTrigger>
              <TabsTrigger value="active">
                {t('challenges.active')}
                {getActiveChallenges().length > 0 && (
                  <Badge className="ml-2" variant="secondary">
                    {getActiveChallenges().length}
                  </Badge>
                )}
              </TabsTrigger>
              <TabsTrigger value="completed">
                {t('challenges.completed')}
                {(getCompletedChallenges().length > 0 || getCompletedTodayChallenges().length > 0) && (
                  <Badge className="ml-2" variant="secondary">
                    {getCompletedTodayChallenges().length || getCompletedChallenges().length}
                  </Badge>
                )}
              </TabsTrigger>
            </TabsList>

            <TabsContent value="available" className="mt-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {categories.map((category, index) => {
                  const Icon = category.icon;
                  return (
                    <Card 
                      key={category.id} 
                      onClick={() => handleSelectCategory(category.id)}
                      className={`p-6 bg-gradient-to-br ${category.bgColor} border ${category.borderColor} hover:border-opacity-100 cursor-pointer transition-all hover:shadow-md ${loading ? "opacity-50 pointer-events-none" : ""}`}
                    >
                      <div className="flex items-start justify-between mb-4">
                        <div className="w-12 h-12 rounded-xl bg-muted flex items-center justify-center">
                          <Icon className={`w-6 h-6 ${category.color}`} />
                        </div>
                      </div>
                      <h3 className="font-semibold text-foreground text-lg mb-2">{category.name}</h3>
                      <p className="text-sm text-muted-foreground mb-4">
                        {t('challenges.clickToSee')}
                      </p>
                      <Button variant="outline" className="w-full">
                        {t('challenges.explore')}
                      </Button>
                    </Card>
                  );
                })}
              </div>
            </TabsContent>

            <TabsContent value="active" className="mt-6">
              {getActiveChallenges().length === 0 ? (
                <Card className="p-8 text-center">
                  <Target className="w-12 h-12 mx-auto mb-4 text-muted-foreground" />
                  <p className="text-muted-foreground">{t('challenges.noActive')}</p>
                  <p className="text-sm text-muted-foreground mt-2">
                    {t('challenges.noActiveDesc')}
                  </p>
                </Card>
              ) : (
                <div className="space-y-3">
                  {getActiveChallenges().map((uc) => {
                    // Check if there's a persisted timer for this challenge
                    const storageKey = `nextstep_timer_${uc.challengeId}`;
                    const storedStart = sessionStorage.getItem(storageKey);
                    let timerText: string | null = null;

                    if (storedStart) {
                      const startTime = parseInt(storedStart, 10);
                      const totalSec = (uc.durationMinutes || 0) * 60;
                      const elapsed = Math.floor((Date.now() - startTime) / 1000);
                      const remaining = Math.max(totalSec - elapsed, 0);

                      if (remaining > 0) {
                        const m = Math.floor(remaining / 60);
                        const s = remaining % 60;
                        timerText = `${m.toString().padStart(2, "0")}:${s.toString().padStart(2, "0")} kvar`;
                      } else {
                        timerText = "Klar att slutföra!";
                      }
                    }

                    return (
                      <Card
                        key={uc.id}
                        className="p-4 cursor-pointer hover:shadow-md transition-all border-primary/30 bg-primary/5"
                        onClick={() => navigate(`/challenges/${categoryToSlug[uc.category]}/${uc.challengeId}`)}
                      >
                        <div className="flex items-start justify-between mb-2">
                          <div className="flex-1">
                            <div className="flex items-center gap-2 mb-1">
                              <span className="relative flex h-2.5 w-2.5">
                                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                                <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-green-500"></span>
                              </span>
                              <h3 className="font-semibold text-foreground">{uc.challengeName}</h3>
                            </div>
                            <p className="text-sm text-muted-foreground">
                              {t(`challenges.categories.${getCategoryKey(uc.category)}`)}{" · "}
                              {getDifficultyLabel(uc.difficulty)}
                              {uc.durationMinutes ? ` · ${uc.durationMinutes} min` : ""}
                            </p>
                            {timerText && (
                              <p className="text-xs font-medium text-primary mt-1 flex items-center gap-1">
                                <Clock className="w-3 h-3" />
                                {timerText}
                              </p>
                            )}
                          </div>
                          <div className="flex flex-col items-end gap-1">
                            <Badge className={getDifficultyColor(uc.difficulty)}>
                              {getDifficultyLabel(uc.difficulty)}
                            </Badge>
                            <span className="text-xs text-primary font-medium">Fortsätt →</span>
                          </div>
                        </div>
                      </Card>
                    );
                  })}
                </div>
              )}
            </TabsContent>

            <TabsContent value="completed" className="mt-6">
              {getCompletedTodayChallenges().length === 0 && getCompletedChallenges().length === 0 ? (
                <Card className="p-8 text-center">
                  <Trophy className="w-12 h-12 mx-auto mb-4 text-muted-foreground" />
                  <p className="text-muted-foreground">{t('challenges.noCompleted')}</p>
                  <p className="text-sm text-muted-foreground mt-2">
                    {t('challenges.noCompletedDesc')}
                  </p>
                </Card>
              ) : (
                <div className="space-y-3">
                  {/* Idag genomförda */}
                  {getCompletedTodayChallenges().length > 0 && (
                    <>
                      <p className="text-sm font-medium text-muted-foreground">{t('challenges.completedTodaySection')}</p>
                      {getCompletedTodayChallenges().map((challenge) => (
                        <Card key={`today-${challenge.id}`} className="p-4 bg-green-500/5 border-green-500/20">
                          <div className="flex items-start justify-between mb-2">
                            <div>
                              <div className="flex items-center gap-2">
                                <CheckCircle2 className="w-4 h-4 text-green-600" />
                                <h3 className="font-semibold text-foreground">{challenge.title}</h3>
                              </div>
                              <p className="text-sm text-muted-foreground">
                                {t(`challenges.categories.${getCategoryKey(challenge.category)}`)}{" · "}
                                {getDifficultyLabel(challenge.difficulty)}
                                {challenge.durationMinutes ? ` · ${challenge.durationMinutes} min` : ""}
                              </p>
                            </div>
                            <Badge className={getDifficultyColor(challenge.difficulty)}>
                              {getDifficultyLabel(challenge.difficulty)}
                            </Badge>
                          </div>
                        </Card>
                      ))}
                    </>
                  )}

                  {/* Tidigare slutförda */}
                  {getCompletedChallenges().length > 0 && (
                    <>
                      {getCompletedTodayChallenges().length > 0 && (
                        <p className="text-sm font-medium text-muted-foreground mt-4">{t('challenges.previouslyCompleted')}</p>
                      )}
                      {getCompletedChallenges().map((uc) => (
                        <Card key={uc.id} className="p-4 bg-green-500/5 border-green-500/20">
                          <div className="flex items-start justify-between mb-2">
                            <div>
                              <div className="flex items-center gap-2">
                                <CheckCircle2 className="w-4 h-4 text-green-600" />
                                <h3 className="font-semibold text-foreground">{uc.challengeName}</h3>
                              </div>
                              <p className="text-sm text-muted-foreground">
                                {t(`challenges.categories.${getCategoryKey(uc.category)}`)}{" · "}
                                {getDifficultyLabel(uc.difficulty)}
                                {uc.durationMinutes ? ` · ${uc.durationMinutes} min` : ""}
                              </p>
                            </div>
                            <Badge className={getDifficultyColor(uc.difficulty)}>
                              {getDifficultyLabel(uc.difficulty)}
                            </Badge>
                          </div>
                        </Card>
                      ))}
                    </>
                  )}
                </div>
              )}
            </TabsContent>
          </Tabs>
        )}

        {/* CHALLENGES LIST VIEW */}
        {viewMode === "challenges" && (
          <div className="space-y-4">
            <div className="flex items-center gap-3 mb-6">
              <div className="w-10 h-10 rounded-full bg-muted flex items-center justify-center">
                {currentCategory && <currentCategory.icon className={`w-5 h-5 ${currentCategory.color}`} />}
              </div>
              <div>
                <h2 className="text-2xl font-bold text-foreground">{currentCategory?.name}</h2>
              </div>
            </div>

            {loading ? (
              <div className="flex justify-center items-center py-12">
                <Loader2 className="w-8 h-8 animate-spin text-primary" />
              </div>
            ) : challenges.length === 0 ? (
              <Card className="p-8 text-center">
                <p className="text-muted-foreground">{t('challenges.noChallenges')}</p>
              </Card>
            ) : (
              <div className="space-y-3">
                {challenges.map((challenge) => (
                  <Card key={challenge.id} className={`p-4 ${challenge.completedToday ? 'opacity-60' : 'card-hover'}`}>
                    <div className="flex items-start justify-between mb-3">
                      <div className="flex-1">
                        <h3 className={`font-semibold mb-1 ${challenge.completedToday ? 'text-muted-foreground' : 'text-foreground'}`}>{challenge.title}</h3>
                        <p className="text-sm text-muted-foreground">{challenge.description}</p>
                      </div>
                      {challenge.completedToday && (
                        <div className="flex flex-col items-end gap-0.5 flex-shrink-0 ml-3">
                          <div className="flex items-center gap-1.5 text-green-500">
                            <CheckCircle2 className="w-4 h-4" />
                            <span className="text-xs font-medium">{t('challenges.completedToday')}</span>
                          </div>
                          <span className="text-[10px] text-muted-foreground">{t('challenges.availableTomorrow')}</span>
                        </div>
                      )}
                    </div>
                    
                    <div className="flex items-center gap-2 mb-4">
                      <div className="flex items-center gap-1 text-xs text-muted-foreground">
                        <Clock className="w-3 h-3" />
                        {challenge.durationMinutes} min
                      </div>
                      <Badge className={getDifficultyColor(challenge.difficulty)}>
                        {getDifficultyLabel(challenge.difficulty)}
                      </Badge>
                      {challenge.youtubeUrl && (
                        <Badge variant="outline" className="text-xs flex items-center gap-1">
                          <Youtube className="w-3 h-3" />
                          Video
                        </Badge>
                      )}
                      {challenge.instructions && (
                        <Badge variant="outline" className="text-xs flex items-center gap-1">
                          <BookOpen className="w-3 h-3" />
                          Guide
                        </Badge>
                      )}
                      {isDrawingCategory(challenge.category) && (
                        <Badge variant="outline" className="text-xs flex items-center gap-1">
                          <Pencil className="w-3 h-3" />
                          {t('challenges.drawing.badge')}
                        </Badge>
                      )}
                    </div>

                    {!challenge.completedToday && (
                      <Button 
                        className="w-full gap-2"
                        onClick={() => handleStartChallenge(challenge)}
                        disabled={loading || isChallengeStarted(challenge.id)}
                      >
                        {isChallengeStarted(challenge.id) ? (
                          <>
                            <CheckCircle2 className="w-4 h-4" />
                            {t('challenges.alreadyStarted')}
                          </>
                        ) : (
                          <>
                            <Play className="w-4 h-4" />
                            {t('challenges.startActivity')}
                          </>
                        )}
                      </Button>
                    )}
                  </Card>
                ))}
              </div>
            )}
          </div>
        )}

        {/* ACTIVITY EXECUTION VIEW */}
        {viewMode === "activity" && selectedChallenge && (
          <div className="space-y-6">
            <Card className="p-6">
              <div className="flex items-start justify-between mb-4">
                <div className="flex-1">
                  <h2 className="text-2xl font-bold text-foreground mb-2">
                    {selectedChallenge.title}
                  </h2>
                  <p className="text-muted-foreground mb-4">
                    {selectedChallenge.description}
                  </p>
                  <div className="flex items-center gap-2">
                    <div className="flex items-center gap-1 text-sm text-muted-foreground">
                      <Clock className="w-4 h-4" />
                      {selectedChallenge.durationMinutes} {t('common.minutes')}
                    </div>
                    <Badge className={getDifficultyColor(selectedChallenge.difficulty)}>
                      {getDifficultyLabel(selectedChallenge.difficulty)}
                    </Badge>
                  </div>
                </div>
              </div>

              {/* YouTube Video */}
              {selectedChallenge.youtubeUrl && (
                <div className="mb-6">
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center gap-2">
                      <Youtube className="w-5 h-5 text-red-500" />
                      <h3 className="font-semibold">{t('challenges.videoGuide')}</h3>
                    </div>
                    {/* Video progress indicator */}
                    <div className="flex items-center gap-2 text-xs text-muted-foreground">
                      {ytProgress.isComplete ? (
                        <span className="flex items-center gap-1 text-green-600 font-medium">
                          <CheckCircle2 className="w-3.5 h-3.5" />
                          {t('challenges.videoWatched', { fallbackLng: 'sv', defaultValue: 'Video sedd' })}
                        </span>
                      ) : (
                        <span>{t('challenges.videoProgress', { percent: ytProgress.watchedPercent, fallbackLng: 'sv', defaultValue: `${ytProgress.watchedPercent}% sett` })}</span>
                      )}
                    </div>
                  </div>
                  <div className="relative w-full" style={{ paddingBottom: "56.25%" }}>
                    <iframe
                      ref={ytProgress.iframeRef}
                      className="absolute top-0 left-0 w-full h-full rounded-lg"
                      src={getYouTubeEmbedUrl(selectedChallenge.youtubeUrl) || ""}
                      title={selectedChallenge.title}
                      allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                      allowFullScreen
                    />
                  </div>
                  {/* Video progress bar */}
                  <div className="mt-2">
                    <Progress value={ytProgress.watchedPercent} className="h-1.5" />
                  </div>
                </div>
              )}

              {/* Guidance Text */}
              {selectedChallenge.instructions && (
                <div className="mb-6">
                  <div className="flex items-center gap-2 mb-3">
                    <BookOpen className="w-5 h-5 text-blue-500" />
                    <h3 className="font-semibold">{t('challenges.instructions')}</h3>
                  </div>
                  <Card className="p-4 bg-blue-500/5 border-blue-500/20">
                    <p className="text-sm text-foreground whitespace-pre-wrap text-left">
                      {selectedChallenge.instructions}
                    </p>
                  </Card>
                </div>
              )}

              {/* Drawing Canvas - only for drawing categories */}
              {isDrawingCategory(selectedChallenge.category) && (
                <div className="mb-6">
                  <DrawingCanvas />
                </div>
              )}

              {/* Activity Timer - only for activities WITHOUT video */}
              {showTimer && (
                <div className="mb-6">
                  <Card className={`p-5 text-center border ${timer.isComplete ? 'bg-green-500/5 border-green-500/30' : 'bg-primary/5 border-primary/20'} transition-colors duration-500`}>
                    <p className="text-xs text-muted-foreground uppercase tracking-wide mb-2">
                      {timer.isComplete
                        ? (t('challenges.timerComplete', { fallbackLng: 'sv', defaultValue: 'Aktivitetstid klar!' }))
                        : (t('challenges.timerRemaining', { fallbackLng: 'sv', defaultValue: 'Tid kvar för aktiviteten' }))}
                    </p>
                    <p className={`text-4xl font-mono font-bold ${timer.isComplete ? 'text-green-600' : 'text-foreground'}`}>
                      {timer.isComplete ? '✓' : timer.formattedTime}
                    </p>
                    <div className="mt-3">
                      <Progress value={timer.progressPercent} className="h-2" />
                    </div>
                  </Card>
                </div>
              )}

              {/* Complete Button - gated */}
              <div className="flex flex-col gap-3 mt-4">
                {!isUnlocked && !isDrawing && (
                  <>
                    <Card className="p-4 bg-muted/50 border-dashed border-2 border-muted-foreground/20 text-center">
                      <p className="text-sm text-muted-foreground font-medium">
                        🔒 {hasVideo
                          ? (t('challenges.unlockHintVideo', { fallbackLng: 'sv', defaultValue: 'Titta klart på videon (minst 80%) för att kunna slutföra utmaningen' }))
                          : (t('challenges.unlockHintTimer', { fallbackLng: 'sv', defaultValue: 'Genomför aktiviteten och vänta tills timern är klar' }))}
                      </p>
                    </Card>
                    <button
                      onClick={() => setAlreadyDone(true)}
                      className="text-sm text-muted-foreground hover:text-foreground underline underline-offset-4 transition-colors mx-auto py-1"
                    >
                      Jag har redan gjort denna aktivitet
                    </button>
                  </>
                )}
                <Button
                  onClick={handleCompleteChallenge}
                  disabled={loading || !isUnlocked}
                  className={`w-full gap-2 text-lg py-7 transition-all duration-300 ${
                    isUnlocked
                      ? 'bg-green-600 hover:bg-green-700 text-white shadow-lg hover:shadow-xl'
                      : 'bg-muted text-muted-foreground cursor-not-allowed border-2 border-dashed border-muted-foreground/20 hover:bg-muted'
                  }`}
                  size="lg"
                  variant={isUnlocked ? "default" : "outline"}
                >
                  {loading ? (
                    <>
                      <Loader2 className="w-5 h-5 animate-spin" />
                      {t('challenges.completing')}
                    </>
                  ) : !isUnlocked ? (
                    <>
                      🔒 {t('challenges.completeChallenge')}
                    </>
                  ) : (
                    <>
                      <CheckCircle2 className="w-5 h-5" />
                      {t('challenges.completeChallenge')}
                    </>
                  )}
                </Button>
              </div>
            </Card>
          </div>
        )}
      </div>
    </div>
  );
};

export default Challenges;
