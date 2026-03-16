import { useState } from "react";
import { useTranslation } from "react-i18next";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  CheckCircle2,
  Clock,
  BookOpen,
  Youtube,
  Loader2,
} from "lucide-react";
import { Progress } from "@/components/ui/progress";
import DrawingCanvas from "@/components/DrawingCanvas";
import { useActivityTimer, clearChallengeTimer } from "@/hooks/useActivityTimer";
import { useYouTubeProgress, extractYouTubeVideoId } from "@/hooks/useYouTubeProgress";
import { ChallengeOutDto } from "@/types/challenge";
import {
  getDifficultyLabel,
  getDifficultyColor,
  isDrawingCategory,
  getYouTubeEmbedUrl,
} from "./challengeUtils";

interface ChallengeActivityViewProps {
  challenge: ChallengeOutDto;
  loading: boolean;
  onComplete: () => void;
}

const ChallengeActivityView = ({
  challenge,
  loading,
  onComplete,
}: ChallengeActivityViewProps) => {
  const { t } = useTranslation();
  const [alreadyDone, setAlreadyDone] = useState(false);

  const hasVideo = !!challenge.youtubeUrl;
  const isDrawing = isDrawingCategory(challenge.category);
  const showTimer = !isDrawing && !hasVideo;

  const videoId = extractYouTubeVideoId(challenge.youtubeUrl);
  const ytProgress = useYouTubeProgress(videoId, hasVideo);
  const timer = useActivityTimer(challenge.durationMinutes || 1, showTimer, challenge.id ?? null);

  const isUnlocked = (() => {
    if (isDrawing) return true;
    if (alreadyDone) return true;
    if (hasVideo) return ytProgress.isComplete;
    return timer.isComplete;
  })();

  return (
    <div className="space-y-6">
      <Card className="p-6">
        <div className="flex items-start justify-between mb-4">
          <div className="flex-1">
            <h2 className="text-2xl font-bold text-foreground mb-2">
              {challenge.title}
            </h2>
            <p className="text-muted-foreground mb-4">
              {challenge.description}
            </p>
            <div className="flex items-center gap-2">
              <div className="flex items-center gap-1 text-sm text-muted-foreground">
                <Clock className="w-4 h-4" />
                {challenge.durationMinutes} {t('common.minutes')}
              </div>
              <Badge className={getDifficultyColor(challenge.difficulty)}>
                {getDifficultyLabel(challenge.difficulty, t)}
              </Badge>
            </div>
          </div>
        </div>

        {/* YouTube Video */}
        {challenge.youtubeUrl && (
          <div className="mb-6">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <Youtube className="w-5 h-5 text-red-500" />
                <h3 className="font-semibold">{t('challenges.videoGuide')}</h3>
              </div>
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
                src={getYouTubeEmbedUrl(challenge.youtubeUrl) || ""}
                title={challenge.title}
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowFullScreen
              />
            </div>
            <div className="mt-2">
              <Progress value={ytProgress.watchedPercent} className="h-1.5" />
            </div>
          </div>
        )}

        {/* Guidance Text */}
        {challenge.instructions && (
          <div className="mb-6">
            <div className="flex items-center gap-2 mb-3">
              <BookOpen className="w-5 h-5 text-blue-500" />
              <h3 className="font-semibold">{t('challenges.instructions')}</h3>
            </div>
            <Card className="p-4 bg-blue-500/5 border-blue-500/20">
              <p className="text-sm text-foreground whitespace-pre-wrap text-left">
                {challenge.instructions}
              </p>
            </Card>
          </div>
        )}

        {/* Drawing Canvas */}
        {isDrawing && (
          <div className="mb-6">
            <DrawingCanvas />
          </div>
        )}

        {/* Activity Timer */}
        {showTimer && (
          <div className="mb-6">
            <Card className={`p-5 text-center border ${timer.isComplete ? 'bg-green-500/5 border-green-500/30' : 'bg-primary/5 border-primary/20'} transition-colors duration-500`}>
              <p className="text-xs text-muted-foreground uppercase tracking-wide mb-2">
                {timer.isComplete
                  ? (t('challenges.timerComplete', { fallbackLng: 'sv', defaultValue: 'Aktivitetstid klar!' }))
                  : (t('challenges.timerRemaining', { fallbackLng: 'sv', defaultValue: 'Tid kvar för aktiviteten' }))}
              </p>
              <p className={`text-4xl font-mono font-bold ${timer.isComplete ? 'text-green-600' : 'text-foreground'}`}>
                {timer.isComplete ? '\u2713' : timer.formattedTime}
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
                  {hasVideo
                    ? (t('challenges.unlockHintVideo', { fallbackLng: 'sv', defaultValue: 'Titta klart p\u00e5 videon (minst 80%) f\u00f6r att kunna slutf\u00f6ra utmaningen' }))
                    : (t('challenges.unlockHintTimer', { fallbackLng: 'sv', defaultValue: 'Genomf\u00f6r aktiviteten och v\u00e4nta tills timern \u00e4r klar' }))}
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
            onClick={onComplete}
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
                {t('challenges.completeChallenge')}
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
  );
};

export default ChallengeActivityView;
