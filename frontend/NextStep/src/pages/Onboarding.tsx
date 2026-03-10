import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
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
  OnboardingData,
} from "@/types/onboarding";

const Onboarding = () => {
  const navigate = useNavigate();
  const { t } = useTranslation();
  const { user, refreshUser } = useAuth();
  const [currentStep, setCurrentStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Steg 1 - Mål
  const [selectedGoals, setSelectedGoals] = useState<UserGoal[]>([]);
  const [otherGoalText, setOtherGoalText] = useState("");

  // Steg 2 - Bakgrund
  const [age, setAge] = useState<number | undefined>(undefined);
  const [gender, setGender] = useState<Gender | undefined>(undefined);
  const [currentSituation, setCurrentSituation] = useState<CurrentSituation | undefined>(undefined);
  const [substanceHistory, setSubstanceHistory] = useState<SubstanceType[]>([]);

  // Steg 3 - Recovery Stage
  const [recoveryStage, setRecoveryStage] = useState<RecoveryStage | undefined>(undefined);

  const totalSteps = 3;
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

  const canProceedFromStep1 = selectedGoals.length > 0;

  const handleNext = () => {
    if (currentStep === 1 && !canProceedFromStep1) {
      setError(t('onboarding.selectGoalError'));
      return;
    }
    setError(null);
    setCurrentStep((prev) => Math.min(prev + 1, totalSteps));
  };

  const handleBack = () => {
    setError(null);
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
        userGoals: selectedGoals,
        otherGoal: selectedGoals.includes(UserGoal.OTHER) ? otherGoalText : undefined,
        backgroundInfo: {
          age,
          gender,
          currentSituation,
          substanceHistory: substanceHistory.length > 0 ? substanceHistory : undefined,
        },
        recoveryStage,
      };

      await onboardingApi.completeOnboarding(user.email || user.id, onboardingData);
      await refreshUser(); // Uppdatera användaren så onboardingCompleted = true
      navigate("/dashboard");
    } catch (err) {
      setError(t('onboarding.completeError'));
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary/5 via-background to-secondary/5 flex items-center justify-center p-4">
      <Card className="w-full max-w-2xl p-6 md:p-8 shadow-[var(--shadow-elevated)] animate-fade-in-up">
        {/* Progress Bar */}
        <div className="mb-6">
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
          <Alert className="mb-6 bg-blue-500/10 border-blue-500/30">
            <Info className="h-4 w-4 text-blue-500" />
            <AlertDescription className="text-sm">
              {t('onboarding.infoAlert')}
            </AlertDescription>
          </Alert>
        )}

        {/* Error Alert */}
        {error && (
          <Alert variant="destructive" className="mb-6">
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {/* STEG 1 - Mål */}
        {currentStep === 1 && (
          <div className="space-y-6 animate-fade-in-up">
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
                  onClick={() => toggleGoal(option.value)}
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
          </div>
        )}

        {/* STEG 2 - Bakgrund */}
        {currentStep === 2 && (
          <div className="space-y-6 animate-fade-in-up">
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
                    onClick={() => toggleSubstance(option.value)}
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
          </div>
        )}

        {/* STEG 3 - Recovery Stage */}
        {currentStep === 3 && (
          <div className="space-y-6 animate-fade-in-up">
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
                    onClick={() => setRecoveryStage(option.value)}
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
          </div>
        )}

        {/* Navigation Buttons */}
        <div className="flex gap-3 mt-8">
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
              disabled={currentStep === 1 && !canProceedFromStep1}
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
