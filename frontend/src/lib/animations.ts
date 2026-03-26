// Framer Motion-varianter för animationer
import { Variants } from "framer-motion";

// Detect user preference for reduced motion
const prefersReducedMotion =
  typeof window !== "undefined" &&
  window.matchMedia("(prefers-reduced-motion: reduce)").matches;

// Helper: returns instant transitions when user prefers reduced motion
const safeTransition = (t: object) =>
  prefersReducedMotion ? { duration: 0 } : t;

export const staggerContainer: Variants = {
  hidden: { opacity: 0 },
  show: {
    opacity: 1,
    transition: {
      staggerChildren: prefersReducedMotion ? 0 : 0.08,
    },
  },
};

export const staggerItem: Variants = {
  hidden: { opacity: 0, y: prefersReducedMotion ? 0 : 16 },
  show: {
    opacity: 1,
    y: 0,
    transition: safeTransition({ duration: 0.3, ease: [0.23, 1, 0.32, 1] }),
  },
};

export const fadeIn: Variants = {
  hidden: { opacity: 0 },
  show: { opacity: 1, transition: safeTransition({ duration: 0.2 }) },
};

export const slideUp: Variants = {
  hidden: { opacity: 0, y: prefersReducedMotion ? 0 : 20 },
  show: {
    opacity: 1,
    y: 0,
    transition: safeTransition({ duration: 0.4, ease: [0.23, 1, 0.32, 1] }),
  },
};

export const scaleIn: Variants = {
  hidden: { opacity: 0, scale: prefersReducedMotion ? 1 : 0.92 },
  show: {
    opacity: 1,
    scale: 1,
    transition: safeTransition({ duration: 0.3, ease: [0.23, 1, 0.32, 1] }),
  },
};

// Micro-interactions
export const hoverLift = prefersReducedMotion
  ? {}
  : { y: -2, transition: { duration: 0.2 } };

export const tapScale = prefersReducedMotion
  ? {}
  : { scale: 0.97 };
