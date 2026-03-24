import React, { useState } from "react";
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
  Zap,
  Play,
  Timer,
} from "lucide-react";
import { motion } from "framer-motion";
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
  onComplete: (actualMinutes: number) => void;
  /** Om true: challenge ej startad – visa info + startknapp istället för timer/slutför */
  previewOnly?: boolean;
  /** Callback to start challenge with a chosen duration */
  onStart?: (challenge: ChallengeOutDto, chosenMinutes: number) => void;
}

// Baspoäng per svårighetsgrad (speglar backend ChallengeDifficulty)
const BASE_POINTS: Record<string, number> = { EASY: 10, MEDIUM: 25, HARD: 50 };

function calculateXP(difficulty: string, actualMinutes: number, defaultMinutes: number): number {
  const base = BASE_POINTS[difficulty] ?? 10;
  const ratio = actualMinutes / Math.max(1, defaultMinutes);
  const clamped = Math.max(0.5, Math.min(3.0, ratio));
  return Math.round(base * clamped);
}

function roundToFive(n: number): number {
  return Math.max(1, Math.round(n / 5) * 5);
}

function buildPresets(defaultMinutes: number): number[] {
  const half = roundToFive(defaultMinutes * 0.5);
  const oneAndHalf = roundToFive(defaultMinutes * 1.5);
  const double = roundToFive(defaultMinutes * 2);
  const presets = [half, defaultMinutes, oneAndHalf, double];
  // Deduplicate and sort
  return [...new Set(presets)].sort((a, b) => a - b);
}

// --- Tidsväljar-komponent ---

interface TimePickerProps {
  challenge: ChallengeOutDto;
  onConfirm: (minutes: number) => void;
  loading: boolean;
}

const TimePicker: React.FC<TimePickerProps> = ({ challenge, onConfirm, loading }) => {
  const { t } = useTranslation();
  const defaultMinutes = challenge.durationMinutes || 1;
  const [selected, setSelected] = useState<number>(defaultMinutes);
  const [customInput, setCustomInput] = useState<string>("");
  const [useCustom, setUseCustom] = useState(false);

  const presets = buildPresets(defaultMinutes);
  const effectiveMinutes = useCustom ? (parseInt(customInput) || defaultMinutes) : selected;
  const xp = calculateXP(challenge.difficulty, effectiveMinutes, defaultMinutes);

  return (
    <Card className="p-5 bg-green-100 border-green-300 dark:bg-green-500/15 dark:border-green-500/40 animate-in fade-in slide-in-from-bottom-2 duration-300">
      <div className="flex items-center gap-2 mb-4">
        <Clock className="w-4 h-4 text-green-600" />
        <h3 className="font-semibold text-sm">
          {t('challenges.timePicker.title', { defaultValue: 'Hur länge höll du på?' })}
        </h3>
      </div>

      {/* Förvalda tidsknappar */}
      <div className="flex flex-wrap gap-2 mb-3">
        {presets.map((min, index) => (
          <motion.button
            key={min}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.05, duration: 0.25 }}
            whileTap={{ scale: 0.93 }}
            onClick={() => { setSelected(min); setUseCustom(false); }}
            className={`px-3 py-1.5 rounded-lg text-sm font-medium border transition-all duration-200 ${
              !useCustom && selected === min
                ? 'bg-green-600 text-white border-green-600 shadow-sm'
                : 'bg-background border-border hover:border-green-500 hover:text-green-700'
            }`}
          >
            {min === defaultMinutes
              ? `${min} min \u2713`
              : `${min} min`}
          </motion.button>
        ))}
        <button
          onClick={() => setUseCustom(true)}
          className={`px-3 py-1.5 rounded-lg text-sm font-medium border transition-all duration-200 ${
            useCustom
              ? 'bg-green-600 text-white border-green-600 shadow-sm'
              : 'bg-background border-border hover:border-green-500 hover:text-green-700'
          }`}
        >
          {t('challenges.timePicker.custom', { defaultValue: 'Annat' })}
        </button>
      </div>

      {/* Eget minutfält */}
      {useCustom && (
        <div className="flex items-center gap-2 mb-3">
          <input
            type="number"
            min={1}
            max={999}
            value={customInput}
            onChange={(e) => setCustomInput(e.target.value)}
            placeholder={t('challenges.timePicker.minutesPlaceholder', { defaultValue: 'Ange minuter' })}
            className="w-32 px-3 py-1.5 text-sm border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500/40 focus:border-green-500 bg-background"
            autoFocus
          />
          <span className="text-sm text-muted-foreground">
            {t('common.minutes')}
          </span>
        </div>
      )}

      {/* XP-förhandsvisning */}
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-1.5 text-sm text-muted-foreground">
          <Zap className="w-4 h-4 text-amber-500" />
          <span>{t('challenges.timePicker.xpPreview', { defaultValue: 'Du tjänar' })}</span>
          <span className="font-bold text-amber-600">{xp} XP</span>
          {effectiveMinutes !== defaultMinutes && (
            <span className="text-xs text-muted-foreground/70">
              ({effectiveMinutes >= defaultMinutes * 2 ? '3× max' : effectiveMinutes > defaultMinutes ? `+${Math.round((effectiveMinutes / defaultMinutes - 1) * 100)}%` : `-${Math.round((1 - effectiveMinutes / defaultMinutes) * 100)}%`})
            </span>
          )}
        </div>
      </div>

      <Button
        onClick={() => onConfirm(effectiveMinutes)}
        disabled={loading || (useCustom && (!customInput || parseInt(customInput) < 1))}
        className="w-full bg-green-600 hover:bg-green-700 text-white shadow-md hover:shadow-lg gap-2"
        size="lg"
      >
        {loading ? (
          <>
            <Loader2 className="w-5 h-5 animate-spin" />
            {t('challenges.completing')}
          </>
        ) : (
          <>
            <CheckCircle2 className="w-5 h-5" />
            {t('challenges.timePicker.confirm', { defaultValue: 'Slutför med {{xp}} XP', xp })}
          </>
        )}
      </Button>
    </Card>
  );
};

// --- Huvudkomponent ---

// Duration presets for starting a challenge
const DURATION_PRESETS = [
  { key: "easy", minutes: 15, labelKey: "challenges.durationEasy", defaultLabel: "Lätt", color: "bg-green-500/15 border-green-500/30 hover:bg-green-500/25 text-green-700 dark:text-green-400" },
  { key: "medium", minutes: 30, labelKey: "challenges.durationMedium", defaultLabel: "Medel", color: "bg-amber-500/15 border-amber-500/30 hover:bg-amber-500/25 text-amber-700 dark:text-amber-400" },
  { key: "hard", minutes: 45, labelKey: "challenges.durationHard", defaultLabel: "Svår", color: "bg-red-500/15 border-red-500/30 hover:bg-red-500/25 text-red-700 dark:text-red-400" },
];

const ChallengeActivityView = ({
  challenge,
  loading,
  onComplete,
  previewOnly = false,
  onStart,
}: ChallengeActivityViewProps) => {
  const { t } = useTranslation();
  const [alreadyDone, setAlreadyDone] = useState(false);
  const [showTimePicker, setShowTimePicker] = useState(false);
  const [selectedDuration, setSelectedDuration] = useState<number | null>(null);

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
              <div className="flex items-center gap-1 text-xs text-amber-600 font-medium">
                <Zap className="w-3 h-3" />
                {BASE_POINTS[challenge.difficulty] ?? 10} XP
              </div>
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
            <Card className="p-4 bg-blue-100 border-blue-300 dark:bg-blue-500/15 dark:border-blue-500/40">
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

        {/* Preview-läge: visa tidsval + startknapp */}
        {previewOnly ? (
          <div className="space-y-4">
            {/* Duration Picker – visas bara om aktiviteten INTE har en video eller rityta */}
            {!hasVideo && !isDrawing && (
              <Card className="p-5 bg-primary/10 border-primary/35">
                <div className="flex items-center gap-2 mb-4">
                  <Timer className="w-5 h-5 text-primary" />
                  <h3 className="font-semibold">
                    {t('challenges.chooseDuration', { defaultValue: 'Välj hur länge du vill hålla på' })}
                  </h3>
                </div>
                <div className="grid grid-cols-3 gap-3 mb-4">
                  {DURATION_PRESETS.map((preset, index) => (
                    <motion.button
                      key={preset.key}
                      initial={{ opacity: 0, y: 8 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: index * 0.07, duration: 0.3 }}
                      whileTap={{ scale: 0.95 }}
                      onClick={() => setSelectedDuration(preset.minutes)}
                      className={`flex flex-col items-center gap-1.5 p-4 rounded-xl border-2 transition-all duration-200 ${
                        selectedDuration === preset.minutes
                          ? `${preset.color} border-current shadow-sm ring-2 ring-current/20`
                          : 'bg-card border-border hover:border-primary/30'
                      }`}
                    >
                      <span className="text-2xl font-bold">{preset.minutes}</span>
                      <span className="text-xs font-medium">{t('common.minutes', { defaultValue: 'min' })}</span>
                      <span className={`text-xs font-medium ${selectedDuration === preset.minutes ? '' : 'text-muted-foreground'}`}>
                        {t(preset.labelKey, { defaultValue: preset.defaultLabel })}
                      </span>
                    </motion.button>
                  ))}
                </div>

                {/* XP preview */}
                {selectedDuration && (
                  <motion.div
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: "auto" }}
                    className="flex items-center justify-center gap-1.5 text-sm text-muted-foreground mb-1"
                  >
                    <Zap className="w-4 h-4 text-amber-500" />
                    <span>{t('challenges.timePicker.xpPreview', { defaultValue: 'Du tjänar' })}</span>
                    <span className="font-bold text-amber-600">
                      {calculateXP(challenge.difficulty, selectedDuration, challenge.durationMinutes || 1)} XP
                    </span>
                  </motion.div>
                )}
              </Card>
            )}

            {/* Start Button */}
            <Button
              onClick={() => {
                const duration = hasVideo || isDrawing
                  ? challenge.durationMinutes || 1
                  : selectedDuration || 0;
                if (onStart && duration > 0) {
                  onStart(challenge, duration);
                } else if (!onStart && duration > 0) {
                  onComplete(duration);
                }
              }}
              disabled={loading || (!hasVideo && !isDrawing && !selectedDuration)}
              className={`w-full gap-2 text-lg py-7 transition-all duration-300 ${
                (hasVideo || isDrawing || selectedDuration)
                  ? 'bg-primary hover:bg-primary/90 text-primary-foreground shadow-lg hover:shadow-xl'
                  : 'bg-muted text-muted-foreground'
              }`}
              size="lg"
            >
              {loading ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  {t('challenges.starting', { defaultValue: 'Startar...' })}
                </>
              ) : (
                <>
                  <Play className="w-5 h-5" />
                  {hasVideo || isDrawing
                    ? t('challenges.startActivity', { defaultValue: 'Starta aktivitet' })
                    : selectedDuration
                      ? t('challenges.startWithDuration', { minutes: selectedDuration, defaultValue: `Starta aktivitet (${selectedDuration} min)` })
                      : t('challenges.selectDurationFirst', { defaultValue: 'Välj tid ovan för att starta' })
                  }
                </>
              )}
            </Button>
          </div>
        ) : (
          <>
            {/* Activity Timer */}
            {showTimer && (
              <div className="mb-6">
                <Card className={`p-5 text-center border ${timer.isComplete ? 'bg-green-100 border-green-300 dark:bg-green-500/15 dark:border-green-500/40' : 'bg-primary/10 border-primary/35'} transition-colors duration-500`}>
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
                    {t('challenges.alreadyDone', { fallbackLng: 'sv', defaultValue: 'Jag har redan gjort denna aktivitet' })}
                  </button>
                </>
              )}

              {/* Tidsväljar-kort (visas när unlocked + knapp klickad) */}
              {showTimePicker && isUnlocked ? (
                <TimePicker
                  challenge={challenge}
                  onConfirm={(minutes) => onComplete(minutes)}
                  loading={loading}
                />
              ) : (
                <Button
                  onClick={() => {
                    if (isUnlocked) setShowTimePicker(true);
                  }}
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
                    <>{t('challenges.completeChallenge')}</>
                  ) : (
                    <>
                      <CheckCircle2 className="w-5 h-5" />
                      {t('challenges.completeChallenge')}
                    </>
                  )}
                </Button>
              )}
            </div>
          </>
        )}
      </Card>
    </div>
  );
};

export default ChallengeActivityView;
