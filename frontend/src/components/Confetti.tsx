import { useEffect, useCallback } from "react";
import confetti from "canvas-confetti";

interface ConfettiProps {
  trigger: boolean;
  colors?: string[];
  particleCount?: number;
  duration?: number;
}

const defaultColors = ["#22c55e", "#eab308", "#3b82f6", "#a855f7"];

export function useConfetti() {
  const fire = useCallback(
    ({
      colors = defaultColors,
      particleCount = 80,
      spread = 70,
      origin,
    }: {
      colors?: string[];
      particleCount?: number;
      spread?: number;
      origin?: { x: number; y: number };
    } = {}) => {
      confetti({
        particleCount,
        spread,
        colors,
        origin: origin || { y: 0.6, x: 0.5 },
        disableForReducedMotion: true,
      });
    },
    []
  );

  const fireBurst = useCallback(
    (colors: string[] = defaultColors) => {
      const duration = 2000;
      const end = Date.now() + duration;

      const frame = () => {
        confetti({
          particleCount: 3,
          angle: 60,
          spread: 55,
          origin: { x: 0, y: 0.6 },
          colors,
          disableForReducedMotion: true,
        });
        confetti({
          particleCount: 3,
          angle: 120,
          spread: 55,
          origin: { x: 1, y: 0.6 },
          colors,
          disableForReducedMotion: true,
        });

        if (Date.now() < end) {
          requestAnimationFrame(frame);
        }
      };

      frame();
    },
    []
  );

  return { fire, fireBurst };
}

export default function Confetti({
  trigger,
  colors = defaultColors,
  particleCount = 80,
  duration = 2000,
}: ConfettiProps) {
  const { fireBurst } = useConfetti();

  useEffect(() => {
    if (trigger) {
      fireBurst(colors);
    }
  }, [trigger]);

  return null;
}
