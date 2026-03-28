import { useState, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { motion, AnimatePresence } from "framer-motion";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Textarea } from "@/components/ui/textarea";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Progress } from "@/components/ui/progress";
import { 
  ArrowRight, 
  ArrowLeft, 
  Check, 
  Info,
  Loader2
} from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { onboardingApi } from "@/services/onboardingService";
import {
  UserGoal,
  Gender,
  CurrentSituation,
  SubstanceType,
  RecoveryStage,
  OnboardingTrack,
  OnboardingData,
} from "@/types/onboarding";

const Onboarding = () => {
  const navigate = useNavigate();
  const { t } = useTranslation();
  const { user, refreshUser } = useAuth();
  const [currentStep, setCurrentStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const directionRef = useRef(1); // 1 = forward, -1 = backward

  // Steg 1 - Spårval
  const [selectedTrack, setSelectedTrack] = useState<OnboardingTrack | undefined>(undefined);

  const stepVariants = {
    initial: (direction: number) => ({ opacity: 0, x: direction > 0 ? 60 : -60 }),
    animate: { opacity: 1, x: 0 },
    exit: (direction: number) => ({ opacity: 0, x: direction > 0 ? -60 : 60 }),
  };

  // Steg 2 - Mål
  const [selectedGoals, setSelectedGoals] = useState<UserGoal[]>([]);
  const [otherGoalText, setOtherGoalText] = useState("");

  // Steg 3 - Bakgrund (bara vid RECOVERY-spår)
  const [age, setAge] = useState<number | undefined>(undefined);
  const [gender, setGender] = useState<Gender | undefined>(undefined);
  const [currentSituation, setCurrentSituation] = useState<CurrentSituation | undefined>(undefined);
  const [substanceHistory, setSubstanceHistory] = useState<SubstanceType[]>([]);

  // Steg 4 - Recovery Stage (bara vid RECOVERY-spår)
  const [recoveryStage, setRecoveryStage] = useState<RecoveryStage | undefined>(undefined);

  // Samtycke till AI-beteendeanalys — visas på sista steget
  const [aiAnalysisConsent, setAiAnalysisConsent] = useState(false);

  // Antal steg beror på vilket spår användaren väljer
  const isRecoveryTrack = selectedTrack === OnboardingTrack.RECOVERY;
  const totalSteps = isRecoveryTrack ? 4 : 2;
  const progress = (currentStep / totalSteps) * 100;

  const goalOptions = [
    { value: UserGoal.DAILY_STABILITY, label: t('onboarding.goals.dailyStability') },
    { value: UserGoal.BETTER_ROUTINES, label: t('onboarding.goals.betterRoutines') },
    { value: UserGoal.REDUCE_SUBSTANCES, label: t('onboarding.goals.reduceSubstances') },
    { value: UserGoal.MENTAL_HEALTH, label: t('onboarding.goals.mentalHealth') },
    { value: UserGoal.STRUCTURE_MOTIVATION, label: t('onboarding.goals.structureMotivation') },
    { value: UserGoal.STAY_CLEAN, label: t('onboarding.goals.stayClean') },
    { value: UserGoal.OTHER, label: t('onboarding.goals.other') },
  ];

  const genderOptions = [
    { value: Gender.MALE, label: t('onboarding.gender.male') },
    { value: Gender.FEMALE, label: t('onboarding.gender.female') },
    { value: Gender.NON_BINARY, label: t('onboarding.gender.nonBinary') },
    { value: Gender.PREFER_NOT_TO_SAY, label: t('onboarding.gender.preferNotToSay') },
  ];

  const situationOptions = [
    { value: CurrentSituation.IN_TREATMENT, label: t('onboarding.situation.inTreatment') },
    { value: CurrentSituation.RECENTLY_COMPLETED, label: t('onboarding.situation.recentlyCompleted') },
    { value: CurrentSituation.STRUGGLING_NOT_IN_TREATMENT, label: t('onboarding.situation.struggling') },
    { value: CurrentSituation.SUPPORTING_SOMEONE, label: t('onboarding.situation.supporting') },
    { value: CurrentSituation.PREFER_NOT_TO_SAY, label: t('onboarding.situation.preferNotToSay') },
  ];

  const substanceOptions = [
    { value: SubstanceType.ALCOHOL, label: t('onboarding.substance.alcohol') },
    { value: SubstanceType.CANNABIS, label: t('onboarding.substance.cannabis') },
    { value: SubstanceType.STIMULANTS, label: t('onboarding.substance.stimulants') },
    { value: SubstanceType.OPIOIDS, label: t('onboarding.substance.opioids') },
    { value: SubstanceType.PRESCRIPTION_MEDS, label: t('onboarding.substance.prescriptionMeds') },
    { value: SubstanceType.GAMBLING, label: t('onboarding.substance.gambling') },
    { value: SubstanceType.OTHER, label: t('onboarding.substance.other') },
    { value: SubstanceType.PREFER_NOT_TO_SAY, label: t('onboarding.substance.preferNotToSay') },
  ];

  const recoveryStageOptions = [
    { value: RecoveryStage.ACTIVE_USE, label: t('onboarding.recovery.activeUse') },
    { value: RecoveryStage.TRYING_TO_QUIT, label: t('onboarding.recovery.tryingToQuit') },
    { value: RecoveryStage.ONE_TO_SEVEN_DAYS, label: t('onboarding.recovery.oneToSevenDays') },
    { value: RecoveryStage.ONE_TO_FOUR_WEEKS, label: t('onboarding.recovery.oneToFourWeeks') },
    { value: RecoveryStage.ONE_TO_SIX_MONTHS, label: t('onboarding.recovery.oneToSixMonths') },
    { value: RecoveryStage.SIX_PLUS_MONTHS, label: t('onboarding.recovery.sixPlusMonths') },
    { value: RecoveryStage.LONG_TERM_RECOVERY, label: t('onboarding.recovery.longTermRecovery') },
  ];

  const toggleGoal = (goal: UserGoal) => {
    setSelectedGoals((prev) =>
      prev.includes(goal)
        ? prev.filter((g) => g !== goal)
        : [...prev, goal]
    );
  };

  const toggleSubstance = (substance: SubstanceType) => {
    setSubstanceHistory((prev) =>
      prev.includes(substance)
        ? prev.filter((s) => s !== substance)
        : [...prev, substance]
    );
  };

  const canProceedFromStep1 = selectedTrack !== undefined;
  const canProceedFromStep2 = selectedGoals.length > 0;

  const handleNext = () => {
    if (currentStep === 1 && !canProceedFromStep1) {
      setError(t('onboarding.selectTrackError'));
      return;
    }
    if (currentStep === 2 && !canProceedFromStep2) {
      setError(t('onboarding.selectGoalError'));
      return;
    }
    setError(null);
    directionRef.current = 1;
    setCurrentStep((prev) => Math.min(prev + 1, totalSteps));
  };

  const handleBack = () => {
    setError(null);
    directionRef.current = -1;
    setCurrentStep((prev) => Math.max(prev - 1, 1));
  };

  const handleSkipStep = () => {
    setError(null);
    if (currentStep < totalSteps) {
      setCurrentStep((prev) => prev + 1);
    } else {
      handleComplete();
    }
  };

  const handleComplete = async () => {
    if (!user?.email && !user?.id) {
      setError(t('onboarding.noUserError'));
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const onboardingData: OnboardingData = {
        onboardingTrack: selectedTrack ?? OnboardingTrack.CONSUMER,
        userGoals: selectedGoals,
        otherGoal: selectedGoals.includes(UserGoal.OTHER) ? otherGoalText : undefined,
        // Bakgrundsinformation skickas bara vid RECOVERY-spåret
        backgroundInfo: isRecoveryTrack ? {
          age,
          gender,
          currentSituation,
          substanceHistory: substanceHistory.length > 0 ? substanceHistory : undefined,
        } : undefined,
        recoveryStage: isRecoveryTrack ? recoveryStage : undefined,
        aiAnalysisConsent,
      };

      await onboardingApi.completeOnboarding(user.email || user.id, onboardingData);
      await refreshUser(); // Uppdatera användaren så onboardingCompleted = true
      navigate("/dashboard");
    } catch (err) {
      setError(t('onboarding.completeError'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-full flex items-start justify-center px-4 py-6 md:py-10">
      <Card className="w-full max-w-2xl p-5 md:p-8 shadow-[var(--shadow-elevated)] animate-fade-in-up">
        {/* Progress Bar */}
        <div className="mb-4">
          <div className="flex justify-between items-center mb-2">
            <span className="text-sm text-muted-foreground">
              {t('onboarding.stepOf', { current: currentStep, total: totalSteps })}
            </span>
            <span className="text-sm font-medium">{Math.round(progress)}%</span>
          </div>
          <Progress value={progress} className="h-2" />
        </div>

        {/* Info Alert */}
        {currentStep > 1 && (
          <Alert className="mb-4 bg-blue-500/10 border-blue-500/30">
            <Info className="h-4 w-4 text-blue-500" />
            <AlertDescription className="text-sm">
              {t('onboarding.infoAlert')}
            </AlertDescription>
          </Alert>
        )}

        {/* Error Alert */}
        {error && (
          <Alert variant="destructive" className="mb-4">
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {/* Step Content */}
        <AnimatePresence mode="wait" custom={directionRef.current}>

        {/* STEG 1 - Spårval */}
        {currentStep === 1 && (
          <motion.div
            key="step0"
            custom={directionRef.current}
            variants={stepVariants}
            initial="initial"
            animate="animate"
            exit="exit"
            transition={{ duration: 0.3, ease: [0.23, 1, 0.32, 1] }}
            className="space-y-6"
          >
            <div>
              <h1 className="text-2xl md:text-3xl font-bold text-foreground mb-2">
                {t('onboarding.trackTitle')}
              </h1>
              <p className="text-muted-foreground">
                {t('onboarding.trackSubtitle')}
              </p>
            </div>

            <div className="space-y-4">
              <Card
                role="button"
                tabIndex={0}
                onClick={() => setSelectedTrack(OnboardingTrack.CONSUMER)}
                onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); setSelectedTrack(OnboardingTrack.CONSUMER); } }}
                className={`p-5 cursor-pointer transition-all ${
                  selectedTrack === OnboardingTrack.CONSUMER
                    ? "bg-primary/10 border-primary"
                    : "hover:bg-muted/50"
                }`}
              >
                <div className="flex items-start gap-4">
                  <span className="text-3xl">🌱</span>
                  <div>
                    <p className="font-semibold text-base">{t('onboarding.trackConsumerTitle')}</p>
                    <p className="text-sm text-muted-foreground mt-1">
                      {t('onboarding.trackConsumerDesc')}
                    </p>
                  </div>
                </div>
              </Card>

              <Card
                role="button"
                tabIndex={0}
                onClick={() => setSelectedTrack(OnboardingTrack.RECOVERY)}
                onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); setSelectedTrack(OnboardingTrack.RECOVERY); } }}
                className={`p-5 cursor-pointer transition-all ${
                  selectedTrack === OnboardingTrack.RECOVERY
                    ? "bg-primary/10 border-primary"
                    : "hover:bg-muted/50"
                }`}
              >
                <div className="flex items-start gap-4">
                  <span className="text-3xl">💪</span>
                  <div>
                    <p className="font-semibold text-base">{t('onboarding.trackRecoveryTitle')}</p>
                    <p className="text-sm text-muted-foreground mt-1">
                      {t('onboarding.trackRecoveryDesc')}
                    </p>
                  </div>
                </div>
              </Card>
            </div>
          </motion.div>
        )}

        {/* STEG 2 - Mål */}
        {currentStep === 2 && (
          <motion.div
            key="step1"
            custom={directionRef.current}
            variants={stepVariants}
            initial="initial"
            animate="animate"
            exit="exit"
            transition={{ duration: 0.3, ease: [0.23, 1, 0.32, 1] }}
            className="space-y-4"
          >
            <div>
              <h1 className="text-2xl md:text-3xl font-bold text-foreground mb-2">
                {t('onboarding.step1Title')}
              </h1>
              <p className="text-muted-foreground">
                {t('onboarding.step1Subtitle')}
              </p>
            </div>

            <div className="space-y-3">
              {goalOptions.map((option) => (
                <Card
                  key={option.value}
                  role="button"
                  tabIndex={0}
                  onClick={() => toggleGoal(option.value)}
                  onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); toggleGoal(option.value); } }}
                  className={`p-4 cursor-pointer transition-all ${ selectedGoals.includes(option.value)
                      ? "bg-primary/10 border-primary"
                      : "hover:bg-muted/50"
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <Checkbox
                      checked={selectedGoals.includes(option.value)}
                      onCheckedChange={() => toggleGoal(option.value)}
                    />
                    <Label className="flex-1 cursor-pointer font-normal">
                      {option.label}
                    </Label>
                    {selectedGoals.includes(option.value) && (
                      <Check className="w-5 h-5 text-primary" />
                    )}
                  </div>
                </Card>
              ))}
            </div>

            {selectedGoals.includes(UserGoal.OTHER) && (
              <div className="space-y-2">
                <Label htmlFor="other-goal">{t('onboarding.describeGoal')}</Label>
                <Textarea
                  id="other-goal"
                  placeholder={t('onboarding.describeGoalPlaceholder')}
                  value={otherGoalText}
                  onChange={(e) => setOtherGoalText(e.target.value)}
                  rows={3}
                />
              </div>
            )}
          </motion.div>
        )}

        {/* STEG 3 - Bakgrund (bara RECOVERY-spåret) */}
        {currentStep === 3 && isRecoveryTrack && (
          <motion.div
            key="step2"
            custom={directionRef.current}
            variants={stepVariants}
            initial="initial"
            animate="animate"
            exit="exit"
            transition={{ duration: 0.3, ease: [0.23, 1, 0.32, 1] }}
            className="space-y-4"
          >
            <div>
              <h1 className="text-2xl md:text-3xl font-bold text-foreground mb-2">
                {t('onboarding.step2Title')}
              </h1>
              <p className="text-muted-foreground">
                {t('onboarding.step2Subtitle')}
              </p>
            </div>

            {/* Ålder */}
            <div className="space-y-2">
              <Label htmlFor="age">{t('onboarding.background.ageLabel')}</Label>
              <Input
                id="age"
                type="number"
                placeholder={t('onboarding.background.agePlaceholder')}
                value={age || ""}
                onChange={(e) => setAge(e.target.value ? parseInt(e.target.value) : undefined)}
                min={13}
                max={120}
              />
            </div>

            {/* Kön */}
            <div className="space-y-3">
              <Label>{t('onboarding.background.genderLabel')}</Label>
              <RadioGroup value={gender} onValueChange={(v) => setGender(v as Gender)}>
                {genderOptions.map((option) => (
                  <div key={option.value} className="flex items-center space-x-2">
                    <RadioGroupItem value={option.value} id={`gender-${option.value}`} />
                    <Label htmlFor={`gender-${option.value}`} className="font-normal cursor-pointer">
                      {option.label}
                    </Label>
                  </div>
                ))}
              </RadioGroup>
            </div>

            {/* Nuvarande situation */}
            <div className="space-y-3">
              <Label>{t('onboarding.background.situationLabel')}</Label>
              <RadioGroup
                value={currentSituation}
                onValueChange={(v) => setCurrentSituation(v as CurrentSituation)}
              >
                {situationOptions.map((option) => (
                  <div key={option.value} className="flex items-center space-x-2">
                    <RadioGroupItem value={option.value} id={`situation-${option.value}`} />
                    <Label
                      htmlFor={`situation-${option.value}`}
                      className="font-normal cursor-pointer"
                    >
                      {option.label}
                    </Label>
                  </div>
                ))}
              </RadioGroup>
            </div>

            {/* Substanshistorik */}
            <div className="space-y-3">
              <Label>{t('onboarding.background.substanceLabel')}</Label>
              <div className="space-y-2">
                {substanceOptions.map((option) => (
                  <Card
                    key={option.value}
                    role="button"
                    tabIndex={0}
                    onClick={() => toggleSubstance(option.value)}
                    onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); toggleSubstance(option.value); } }}
                    className={`p-3 cursor-pointer transition-all ${
                      substanceHistory.includes(option.value)
                        ? "bg-primary/10 border-primary"
                        : "hover:bg-muted/50"
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <Checkbox
                        checked={substanceHistory.includes(option.value)}
                        onCheckedChange={() => toggleSubstance(option.value)}
                      />
                      <Label className="flex-1 cursor-pointer font-normal text-sm">
                        {option.label}
                      </Label>
                    </div>
                  </Card>
                ))}
              </div>
            </div>
          </motion.div>
        )}

        {/* STEG 4 - Recovery Stage (bara RECOVERY-spåret) */}
        {currentStep === 4 && isRecoveryTrack && (
          <motion.div
            key="step3"
            custom={directionRef.current}
            variants={stepVariants}
            initial="initial"
            animate="animate"
            exit="exit"
            transition={{ duration: 0.3, ease: [0.23, 1, 0.32, 1] }}
            className="space-y-4"
          >
            <div>
              <h1 className="text-2xl md:text-3xl font-bold text-foreground mb-2">
                {t('onboarding.step3Title')}
              </h1>
              <p className="text-muted-foreground">
                {t('onboarding.step3Subtitle')}
              </p>
            </div>

            <RadioGroup
              value={recoveryStage}
              onValueChange={(v) => setRecoveryStage(v as RecoveryStage)}
            >
              <div className="space-y-2">
                {recoveryStageOptions.map((option) => (
                  <Card
                    key={option.value}
                    role="button"
                    tabIndex={0}
                    onClick={() => setRecoveryStage(option.value)}
                    onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); setRecoveryStage(option.value); } }}
                    className={`p-4 cursor-pointer transition-all ${
                      recoveryStage === option.value
                        ? "bg-primary/10 border-primary"
                        : "hover:bg-muted/50"
                    }`}
                  >
                    <div className="flex items-center space-x-3">
                      <RadioGroupItem value={option.value} id={`stage-${option.value}`} />
                      <Label
                        htmlFor={`stage-${option.value}`}
                        className="flex-1 cursor-pointer font-normal"
                      >
                        {option.label}
                      </Label>
                      {recoveryStage === option.value && (
                        <Check className="w-5 h-5 text-primary" />
                      )}
                    </div>
                  </Card>
                ))}
              </div>
            </RadioGroup>
          </motion.div>
        )}
        </AnimatePresence>

        {/* Samtycke till AI-analys — visas på sista steget */}
        {currentStep === totalSteps && (
          <div className="mt-5 p-4 rounded-xl border border-border bg-muted/40 space-y-3">
            <p className="text-sm font-semibold text-foreground">{t('onboarding.consentTitle')}</p>
            <div className="flex items-start gap-3">
              <Checkbox
                id="ai-analysis-consent"
                checked={aiAnalysisConsent}
                onCheckedChange={(checked) => setAiAnalysisConsent(checked === true)}
                className="mt-0.5 shrink-0"
              />
              <Label htmlFor="ai-analysis-consent" className="text-sm cursor-pointer font-normal leading-relaxed text-muted-foreground">
                {t('onboarding.consentText')}
              </Label>
            </div>
            <p className="text-xs text-muted-foreground">{t('onboarding.consentRequired')}</p>
          </div>
        )}

        {/* Navigation Buttons */}
        <div className="flex gap-3 mt-5">
          {currentStep > 1 && (
            <Button onClick={handleBack} variant="outline" className="gap-2">
              <ArrowLeft className="w-4 h-4" />
              {t('common.back')}
            </Button>
          )}

          {currentStep > 1 && currentStep < totalSteps && (
            <Button onClick={handleSkipStep} variant="ghost" className="ml-auto">
              {t('common.skip')}
            </Button>
          )}

          {currentStep < totalSteps ? (
            <Button
              onClick={handleNext}
              className={`gap-2 ${currentStep === 1 ? "ml-auto" : ""}`}
              disabled={
                (currentStep === 1 && !canProceedFromStep1) ||
                (currentStep === 2 && !canProceedFromStep2)
              }
            >
              {t('common.next')}
              <ArrowRight className="w-4 h-4" />
            </Button>
          ) : (
            <>
              {currentStep === totalSteps && (
                <Button onClick={handleSkipStep} variant="ghost" className="ml-auto">
                  {t('common.skip')}
                </Button>
              )}
              <Button
                onClick={handleComplete}
                disabled={loading}
                className="gap-2"
              >
                {loading ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    {t('onboarding.completing')}
                  </>
                ) : (
                  <>
                    <Check className="w-4 h-4" />
                    {t('onboarding.complete')}
                  </>
                )}
              </Button>
            </>
          )}
        </div>
      </Card>
    </div>
  );
};

export default Onboarding;
