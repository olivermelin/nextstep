import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  CheckCircle2,
  Clock,
  Target,
  Trophy,
} from "lucide-react";
import {
  ChallengeOutDto,
  UserChallengeOutDto,
  ChallengeCategory,
} from "@/types/challenge";
import {
  categoryConfigs,
  categoryToSlug,
  getDifficultyLabel,
  getDifficultyColor,
  getCategoryKey,
} from "./challengeUtils";

interface CategoriesViewProps {
  activeTab: "available" | "active" | "completed";
  onTabChange: (tab: "available" | "active" | "completed") => void;
  activeChallenges: UserChallengeOutDto[];
  completedChallenges: UserChallengeOutDto[];
  completedTodayChallenges: ChallengeOutDto[];
  loading: boolean;
  onSelectCategory: (category: ChallengeCategory) => void;
}

const CategoriesView = ({
  activeTab,
  onTabChange,
  activeChallenges,
  completedChallenges,
  completedTodayChallenges,
  loading,
  onSelectCategory,
}: CategoriesViewProps) => {
  const { t } = useTranslation();
  const navigate = useNavigate();

  return (
    <Tabs value={activeTab} onValueChange={(v) => onTabChange(v as "available" | "active" | "completed")} className="w-full">
      <TabsList className="grid w-full grid-cols-3">
        <TabsTrigger value="available">{t('challenges.available')}</TabsTrigger>
        <TabsTrigger value="active">
          {t('challenges.active')}
          {activeChallenges.length > 0 && (
            <Badge className="ml-2" variant="secondary">
              {activeChallenges.length}
            </Badge>
          )}
        </TabsTrigger>
        <TabsTrigger value="completed">
          {t('challenges.completed')}
          {(completedChallenges.length > 0 || completedTodayChallenges.length > 0) && (
            <Badge className="ml-2" variant="secondary">
              {completedTodayChallenges.length || completedChallenges.length}
            </Badge>
          )}
        </TabsTrigger>
      </TabsList>

      <TabsContent value="available" className="mt-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {categoryConfigs.map((category) => {
            const Icon = category.icon;
            return (
              <Card
                key={category.id}
                onClick={() => onSelectCategory(category.id)}
                className={`p-6 bg-gradient-to-br ${category.bgColor} border ${category.borderColor} hover:border-opacity-100 cursor-pointer transition-all hover:shadow-md ${loading ? "opacity-50 pointer-events-none" : ""}`}
              >
                <div className="flex items-start justify-between mb-4">
                  <div className="w-12 h-12 rounded-xl bg-muted flex items-center justify-center">
                    <Icon className={`w-6 h-6 ${category.color}`} />
                  </div>
                </div>
                <h3 className="font-semibold text-foreground text-lg mb-2">{t(category.nameKey)}</h3>
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
        {activeChallenges.length === 0 ? (
          <Card className="p-8 text-center">
            <Target className="w-12 h-12 mx-auto mb-4 text-muted-foreground" />
            <p className="text-muted-foreground">{t('challenges.noActive')}</p>
            <p className="text-sm text-muted-foreground mt-2">
              {t('challenges.noActiveDesc')}
            </p>
          </Card>
        ) : (
          <div className="space-y-3">
            {activeChallenges.map((uc) => {
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
                        {getDifficultyLabel(uc.difficulty, t)}
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
                        {getDifficultyLabel(uc.difficulty, t)}
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
        {completedTodayChallenges.length === 0 && completedChallenges.length === 0 ? (
          <Card className="p-8 text-center">
            <Trophy className="w-12 h-12 mx-auto mb-4 text-muted-foreground" />
            <p className="text-muted-foreground">{t('challenges.noCompleted')}</p>
            <p className="text-sm text-muted-foreground mt-2">
              {t('challenges.noCompletedDesc')}
            </p>
          </Card>
        ) : (
          <div className="space-y-3">
            {completedTodayChallenges.length > 0 && (
              <>
                <p className="text-sm font-medium text-muted-foreground">{t('challenges.completedTodaySection')}</p>
                {completedTodayChallenges.map((challenge) => (
                  <Card key={`today-${challenge.id}`} className="p-4 bg-green-500/5 border-green-500/20">
                    <div className="flex items-start justify-between mb-2">
                      <div>
                        <div className="flex items-center gap-2">
                          <CheckCircle2 className="w-4 h-4 text-green-600" />
                          <h3 className="font-semibold text-foreground">{challenge.title}</h3>
                        </div>
                        <p className="text-sm text-muted-foreground">
                          {t(`challenges.categories.${getCategoryKey(challenge.category)}`)}{" · "}
                          {getDifficultyLabel(challenge.difficulty, t)}
                          {challenge.durationMinutes ? ` · ${challenge.durationMinutes} min` : ""}
                        </p>
                      </div>
                      <Badge className={getDifficultyColor(challenge.difficulty)}>
                        {getDifficultyLabel(challenge.difficulty, t)}
                      </Badge>
                    </div>
                  </Card>
                ))}
              </>
            )}

            {completedChallenges.length > 0 && (
              <>
                {completedTodayChallenges.length > 0 && (
                  <p className="text-sm font-medium text-muted-foreground mt-4">{t('challenges.previouslyCompleted')}</p>
                )}
                {completedChallenges.map((uc) => (
                  <Card key={uc.id} className="p-4 bg-green-500/5 border-green-500/20">
                    <div className="flex items-start justify-between mb-2">
                      <div>
                        <div className="flex items-center gap-2">
                          <CheckCircle2 className="w-4 h-4 text-green-600" />
                          <h3 className="font-semibold text-foreground">{uc.challengeName}</h3>
                        </div>
                        <p className="text-sm text-muted-foreground">
                          {t(`challenges.categories.${getCategoryKey(uc.category)}`)}{" · "}
                          {getDifficultyLabel(uc.difficulty, t)}
                          {uc.durationMinutes ? ` · ${uc.durationMinutes} min` : ""}
                        </p>
                      </div>
                      <Badge className={getDifficultyColor(uc.difficulty)}>
                        {getDifficultyLabel(uc.difficulty, t)}
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
  );
};

export default CategoriesView;
