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
import DrawingCanvas from "@/components/DrawingCanvas";
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
  const { categorySlug, challengeId } = useParams<{ categorySlug?: string; challengeId?: string }>();
  const navigate = useNavigate();

  // Härledd viewMode från URL-params
  const selectedCategory = categorySlug ? categorySlugMap[categorySlug] || null : null;
  const viewMode: "categories" | "challenges" | "activity" = challengeId ? "activity" : categorySlug ? "challenges" : "categories";

  const [selectedChallenge, setSelectedChallenge] = useState<ChallengeOutDto | null>(null);
  const [challenges, setChallenges] = useState<ChallengeOutDto[]>([]);
  const [userChallenges, setUserChallenges] = useState<UserChallengeOutDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<"available" | "active" | "completed">("available");

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
    if (user?.id) {
      loadUserChallenges();
    }
  }, [user?.id]);

  // Ladda challenges för vald kategori när categorySlug ändras
  useEffect(() => {
    if (selectedCategory) {
      loadChallengesByCategory(selectedCategory);
    }
  }, [selectedCategory]);

  // Ladda specifik challenge när challengeId finns i URL:en
  useEffect(() => {
    if (challengeId) {
      const openChallenge = async () => {
        try {
          const fullChallenge = await challengeApi.getChallengeById(Number(challengeId));
          setSelectedChallenge(fullChallenge);
        } catch (err) {
          console.error("Kunde inte ladda challenge:", err);
          navigate("/challenges", { replace: true });
        }
      };
      openChallenge();
    } else {
      setSelectedChallenge(null);
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

  const loadChallengesByCategory = async (category: ChallengeCategory) => {
    setLoading(true);
    setError(null);

    try {
      const data = await challengeApi.getChallengesByCategory(category);
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

    setLoading(true);
    setError(null);

    try {
      await userChallengeApi.startChallenge(user.email || user.id, challenge.id);
      await loadUserChallenges();

      // Navigera till aktivitets-URL:en
      const slug = categoryToSlug[challenge.category];
      navigate(`/challenges/${slug}/${challenge.id}`);
    } catch (err) {
      setError(t('challenges.couldNotStart'));
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

      // Gå tillbaka till kategori-vyn
      if (categorySlug) {
        navigate(`/challenges/${categorySlug}`);
      } else {
        navigate("/challenges");
      }
    } catch (err) {
      setError(t('challenges.couldNotComplete'));
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
    
    // Konvertera YouTube URL till embed format
    const videoIdMatch = url.match(/(?:youtube\.com\/watch\?v=|youtu\.be\/)([^&]+)/);
    if (videoIdMatch) {
      return `https://www.youtube.com/embed/${videoIdMatch[1]}`;
    }
    return url;
  };

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
                {getCompletedChallenges().length > 0 && (
                  <Badge className="ml-2" variant="secondary">
                    {getCompletedChallenges().length}
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
                  {getActiveChallenges().map((uc) => (
                    <Card
                      key={uc.id}
                      className="p-4 cursor-pointer hover:shadow-md transition-all"
                      onClick={() => navigate(`/challenges/${categoryToSlug[uc.category]}/${uc.challengeId}`)}
                    >
                      <div className="flex items-start justify-between mb-2">
                        <div>
                          <h3 className="font-semibold text-foreground">{uc.challengeName}</h3>
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
                </div>
              )}
            </TabsContent>

            <TabsContent value="completed" className="mt-6">
              {getCompletedChallenges().length === 0 ? (
                <Card className="p-8 text-center">
                  <Trophy className="w-12 h-12 mx-auto mb-4 text-muted-foreground" />
                  <p className="text-muted-foreground">{t('challenges.noCompleted')}</p>
                  <p className="text-sm text-muted-foreground mt-2">
                    {t('challenges.noCompletedDesc')}
                  </p>
                </Card>
              ) : (
                <div className="space-y-3">
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
                  <Card key={challenge.id} className="p-4 card-hover">
                    <div className="flex items-start justify-between mb-3">
                      <div className="flex-1">
                        <h3 className="font-semibold text-foreground mb-1">{challenge.title}</h3>
                        <p className="text-sm text-muted-foreground">{challenge.description}</p>
                      </div>
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
                  <div className="flex items-center gap-2 mb-3">
                    <Youtube className="w-5 h-5 text-red-500" />
                    <h3 className="font-semibold">{t('challenges.videoGuide')}</h3>
                  </div>
                  <div className="relative w-full" style={{ paddingBottom: "56.25%" }}>
                    <iframe
                      className="absolute top-0 left-0 w-full h-full rounded-lg"
                      src={getYouTubeEmbedUrl(selectedChallenge.youtubeUrl) || ""}
                      title={selectedChallenge.title}
                      allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                      allowFullScreen
                    />
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

              {/* Complete Button */}
              <div className="flex gap-3">
                <Button 
                  onClick={handleCompleteChallenge}
                  disabled={loading}
                  className="flex-1 gap-2"
                  size="lg"
                >
                  {loading ? (
                    <>
                      <Loader2 className="w-4 h-4 animate-spin" />
                      {t('challenges.completing')}
                    </>
                  ) : (
                    <>
                      <CheckCircle2 className="w-4 h-4" />
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
