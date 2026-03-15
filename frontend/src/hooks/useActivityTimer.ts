import { useState, useEffect, useRef } from "react";

interface UseActivityTimerReturn {
  remainingSeconds: number;
  totalSeconds: number;
  isComplete: boolean;
  progressPercent: number;
  formattedTime: string;
}

function getStorageKey(challengeId: number | null): string {
  return `nextstep_timer_${challengeId}`;
}

/**
 * Countdown timer for challenge activities.
 * Persists start time in sessionStorage so it survives navigation.
 * Each challenge gets its own timer keyed by challengeId.
 */
export function useActivityTimer(
  durationMinutes: number,
  active: boolean,
  challengeId: number | null = null
): UseActivityTimerReturn {
  const totalSeconds = durationMinutes * 60;
  const [remainingSeconds, setRemainingSeconds] = useState(totalSeconds);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    if (!active || challengeId == null) {
      return;
    }

    const storageKey = getStorageKey(challengeId);

    // Check if a start time already exists in sessionStorage
    let startTime: number;
    const stored = sessionStorage.getItem(storageKey);

    if (stored) {
      startTime = parseInt(stored, 10);
    } else {
      startTime = Date.now();
      sessionStorage.setItem(storageKey, startTime.toString());
    }

    const updateRemaining = () => {
      const elapsed = Math.floor((Date.now() - startTime) / 1000);
      const remaining = Math.max(totalSeconds - elapsed, 0);
      setRemainingSeconds(remaining);

      if (remaining <= 0 && intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };

    // Set initial value immediately
    updateRemaining();

    // Then update every second
    intervalRef.current = setInterval(updateRemaining, 1000);

    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [active, totalSeconds, challengeId]);

  const isComplete = remainingSeconds <= 0;
  const elapsed = totalSeconds - remainingSeconds;
  const progressPercent = totalSeconds > 0 ? Math.min((elapsed / totalSeconds) * 100, 100) : 100;

  const minutes = Math.floor(remainingSeconds / 60);
  const seconds = remainingSeconds % 60;
  const formattedTime = `${minutes.toString().padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`;

  return { remainingSeconds, totalSeconds, isComplete, progressPercent, formattedTime };
}

/**
 * Clear the stored timer for a specific challenge (call after completing).
 */
export function clearChallengeTimer(challengeId: number): void {
  sessionStorage.removeItem(getStorageKey(challengeId));
}

