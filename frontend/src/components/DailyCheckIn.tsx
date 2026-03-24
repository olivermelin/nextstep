import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { CheckCircle2, Loader2 } from "lucide-react";
import { checkInService, CheckInData } from "@/services/checkInService";
import { useToast } from "@/hooks/use-toast";

interface DailyCheckInProps {
  userId: string;
  existingCheckIn?: CheckInData | null;
  onCheckInComplete?: (checkIn: CheckInData) => void;
}

const MOOD_OPTIONS = [
  { score: 1, emoji: "😔", label: "Mycket dåligt" },
  { score: 2, emoji: "😕", label: "Dåligt" },
  { score: 3, emoji: "😐", label: "Okej" },
  { score: 4, emoji: "🙂", label: "Bra" },
  { score: 5, emoji: "😄", label: "Mycket bra" },
];

/**
 * Daglig incheckningskomponent.
 * Visar 5 humöralternativ och ett valfritt anteckningsfält.
 * Om användaren redan checkat in idag visas en summering istället.
 */
const DailyCheckIn = ({ userId, existingCheckIn, onCheckInComplete }: DailyCheckInProps) => {
  const { toast } = useToast();
  const [selectedMood, setSelectedMood] = useState<number | null>(
    existingCheckIn?.moodScore ?? null
  );
  const [note, setNote] = useState(existingCheckIn?.note ?? "");
  const [loading, setLoading] = useState(false);
  const [completed, setCompleted] = useState(!!existingCheckIn);

  const alreadyDone = !!existingCheckIn && !completed;
  const canSubmit = selectedMood !== null;

  const handleSubmit = async () => {
    if (!canSubmit) return;
    setLoading(true);
    try {
      const result = await checkInService.checkIn(userId, {
        moodScore: selectedMood!,
        note: note.trim() || undefined,
      });
      setCompleted(true);
      onCheckInComplete?.(result);
      toast({
        title: "Incheckning klar! ✓",
        description: `Du har registrerat ditt mående för idag.`,
      });
    } catch {
      toast({
        title: "Något gick fel",
        description: "Kunde inte spara incheckning. Försök igen.",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  // Redan incheckad och inte redigering
  if (completed && existingCheckIn) {
    const mood = MOOD_OPTIONS.find((m) => m.score === existingCheckIn.moodScore);
    return (
      <Card className="p-4">
        <div className="flex items-center gap-3">
          <CheckCircle2 className="w-5 h-5 text-green-500 shrink-0" />
          <div>
            <p className="text-sm font-medium">Incheckning klar idag</p>
            <p className="text-sm text-muted-foreground">
              {mood?.emoji} {mood?.label}
              {existingCheckIn.note && ` — "${existingCheckIn.note}"`}
            </p>
          </div>
        </div>
      </Card>
    );
  }

  return (
    <Card className="p-5 space-y-4">
      <div>
        <h3 className="font-semibold text-base">Hur mår du idag?</h3>
        <p className="text-sm text-muted-foreground">En snabb check-in håller din streak vid liv</p>
      </div>

      {/* Humörval */}
      <div className="flex justify-between gap-2">
        {MOOD_OPTIONS.map((option) => (
          <motion.button
            key={option.score}
            whileTap={{ scale: 0.9 }}
            onClick={() => setSelectedMood(option.score)}
            className={`flex-1 flex flex-col items-center gap-1 p-2 rounded-xl transition-all border-2 ${
              selectedMood === option.score
                ? "border-primary bg-primary/10"
                : "border-transparent hover:border-muted hover:bg-muted/50"
            }`}
            aria-label={option.label}
            aria-pressed={selectedMood === option.score}
          >
            <span className="text-2xl">{option.emoji}</span>
            <span className="text-[10px] text-muted-foreground leading-tight text-center">
              {option.label}
            </span>
          </motion.button>
        ))}
      </div>

      {/* Anteckning */}
      <AnimatePresence>
        {selectedMood !== null && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            exit={{ opacity: 0, height: 0 }}
            transition={{ duration: 0.2 }}
          >
            <Textarea
              placeholder="Valfri anteckning om hur du mår... (max 500 tecken)"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              maxLength={500}
              rows={2}
              className="resize-none text-sm"
            />
          </motion.div>
        )}
      </AnimatePresence>

      <Button
        onClick={handleSubmit}
        disabled={!canSubmit || loading}
        className="w-full"
      >
        {loading ? (
          <>
            <Loader2 className="w-4 h-4 mr-2 animate-spin" />
            Sparar...
          </>
        ) : (
          "Checka in"
        )}
      </Button>
    </Card>
  );
};

export default DailyCheckIn;
