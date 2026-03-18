import { useTranslation } from "react-i18next";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  CheckCircle2,
  Clock,
  Play,
  BookOpen,
  Youtube,
  Loader2,
  Pencil,
  Eye,
  ArrowRight,
} from "lucide-react";
import { useNavigate } from "react-router-dom";
import {
  ChallengeOutDto,
  ChallengeCategory,
} from "@/types/challenge";
import {
  categoryConfigs,
  getDifficultyLabel,
  getDifficultyColor,
  isDrawingCategory,
} from "./challengeUtils";

interface ChallengeListViewProps {
  challenges: ChallengeOutDto[];
  selectedCategory: ChallengeCategory | null;
  loading: boolean;
  onStartChallenge: (challenge: ChallengeOutDto) => void;
  isChallengeStarted: (challengeId: number) => boolean;
  onViewChallenge?: (challenge: ChallengeOutDto) => void;
}

const ChallengeListView = ({
  challenges,
  selectedCategory,
  loading,
  onStartChallenge,
  isChallengeStarted,
  onViewChallenge,
}: ChallengeListViewProps) => {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const currentCategory = categoryConfigs.find(c => c.id === selectedCategory);

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3 mb-6">
        <div className="w-10 h-10 rounded-full bg-muted flex items-center justify-center">
          {currentCategory && <currentCategory.icon className={`w-5 h-5 ${currentCategory.color}`} />}
        </div>
        <div>
          <h2 className="text-2xl font-bold text-foreground">{currentCategory ? t(currentCategory.nameKey) : ""}</h2>
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
                  {getDifficultyLabel(challenge.difficulty, t)}
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
                isChallengeStarted(challenge.id) ? (
                  <Button
                    className="w-full gap-2"
                    onClick={() => onViewChallenge?.(challenge)}
                    variant="outline"
                  >
                    <ArrowRight className="w-4 h-4" />
                    {t('challenges.continueActivity', { defaultValue: 'Fortsätt aktivitet' })}
                  </Button>
                ) : (
                  <Button
                    className="w-full gap-2"
                    onClick={() => onViewChallenge?.(challenge)}
                    variant="outline"
                    disabled={loading}
                  >
                    <Eye className="w-4 h-4" />
                    {t('challenges.viewDetails', { defaultValue: 'Visa detaljer' })}
                  </Button>
                )
              )}
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};

export default ChallengeListView;
