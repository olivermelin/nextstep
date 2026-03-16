import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { ArrowLeft } from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { challengeApi, userChallengeApi } from "@/services/challengeService";
import { useToast } from "@/components/ui/use-toast";
import { clearChallengeTimer } from "@/hooks/useActivityTimer";
import {
  ChallengeOutDto,
  UserChallengeOutDto,
  ChallengeCategory,
} from "@/types/challenge";
import {
  categorySlugMap,
  categoryToSlug,
} from "@/components/challenges/challengeUtils";
import { CategoriesView } from "@/components/challenges";
import { ChallengeListView } from "@/components/challenges";
import { ChallengeActivityView } from "@/components/challenges";

const Challenges = () => {
  const { t } = useTranslation();
  const { user } = useAuth();
  const { toast } = useToast();
  const { categorySlug, challengeId } = useParams<{ categorySlug?: string; challengeId?: string }>();
  const navigate = useNavigate();

  // Derived viewMode from URL params
  const selectedCategory = categorySlug ? categorySlugMap[categorySlug] || null : null;
  const viewMode: "categories" | "challenges" | "activity" = challengeId ? "activity" : categorySlug ? "challenges" : "categories";

  const [selectedChallenge, setSelectedChallenge] = useState<ChallengeOutDto | null>(null);
  const [challenges, setChallenges] = useState<ChallengeOutDto[]>([]);
  const [allChallenges, setAllChallenges] = useState<ChallengeOutDto[]>([]);
  const [userChallenges, setUserChallenges] = useState<UserChallengeOutDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<"available" | "active" | "completed">("available");

  // Load user challenges on mount
  useEffect(() => {
    if (user?.id || user?.email) {
      loadUserChallenges();
      loadAllChallenges();
    }
  }, [user?.id, user?.email]);

  // Load challenges for selected category
  useEffect(() => {
    if (selectedCategory) {
      loadChallengesByCategory(selectedCategory);
    }
  }, [selectedCategory]);

  // Load specific challenge when challengeId is in URL
  useEffect(() => {
    if (challengeId) {
      const openChallenge = async () => {
        try {
          const fullChallenge = await challengeApi.getChallengeById(Number(challengeId));
          setSelectedChallenge(fullChallenge);

          const userId = user?.email || user?.id;
          if (userId && !fullChallenge.completedToday) {
            try {
              await userChallengeApi.startChallenge(userId, fullChallenge.id);
              await loadUserChallenges();
            } catch {
              // Ignore auto-start errors
            }
          }
        } catch {
          navigate("/challenges", { replace: true });
        }
      };
      openChallenge();
    } else {
      setSelectedChallenge(null);
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
    } catch {
      // User challenges fetch failed silently
    }
  };

  const loadAllChallenges = async () => {
    if (!user?.email && !user?.id) return;
    const userId = user.email || user.id;

    try {
      const data = await challengeApi.getUserChallenges(userId);
      setAllChallenges(data);
    } catch {
      // All challenges fetch failed silently
    }
  };

  const loadChallengesByCategory = async (category: ChallengeCategory) => {
    setLoading(true);
    setError(null);

    try {
      const userId = user?.email || user?.id;
      let data: ChallengeOutDto[];
      if (userId) {
        const allUserChallenges = await challengeApi.getUserChallenges(userId);
        data = allUserChallenges.filter(c => c.category === category);
      } else {
        data = await challengeApi.getChallengesByCategory(category);
      }
      setChallenges(data);
    } catch {
      setError(t('challenges.couldNotLoadChallenges'));
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

      const slug = categoryToSlug[challenge.category];
      navigate(`/challenges/${slug}/${challenge.id}`);
    } catch (err: any) {
      const msg = err?.message || '';
      if (msg.includes('already completed today') || msg.includes('Challenge already completed')) {
        toast({
          title: t('challenges.alreadyCompletedTodayTitle'),
          description: t('challenges.alreadyCompletedTodayDesc'),
        });
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

      clearChallengeTimer(selectedChallenge.id);

      setChallenges(prev => prev.map(c => c.id === selectedChallenge.id ? { ...c, completedToday: true } : c));
      setAllChallenges(prev => prev.map(c => c.id === selectedChallenge.id ? { ...c, completedToday: true } : c));

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
          <CategoriesView
            activeTab={activeTab}
            onTabChange={setActiveTab}
            activeChallenges={getActiveChallenges()}
            completedChallenges={getCompletedChallenges()}
            completedTodayChallenges={getCompletedTodayChallenges()}
            loading={loading}
            onSelectCategory={handleSelectCategory}
          />
        )}

        {/* CHALLENGES LIST VIEW */}
        {viewMode === "challenges" && (
          <ChallengeListView
            challenges={challenges}
            selectedCategory={selectedCategory}
            loading={loading}
            onStartChallenge={handleStartChallenge}
            isChallengeStarted={isChallengeStarted}
          />
        )}

        {/* ACTIVITY EXECUTION VIEW */}
        {viewMode === "activity" && selectedChallenge && (
          <ChallengeActivityView
            challenge={selectedChallenge}
            loading={loading}
            onComplete={handleCompleteChallenge}
          />
        )}
      </div>
    </div>
  );
};

export default Challenges;
