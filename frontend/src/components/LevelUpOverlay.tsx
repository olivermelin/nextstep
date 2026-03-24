import { useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Shield, Zap } from "lucide-react";
import { useConfetti } from "./Confetti";
import { useTranslation } from "react-i18next";

interface LevelUpOverlayProps {
  level: number | null;
  onClose: () => void;
}

export default function LevelUpOverlay({ level, onClose }: LevelUpOverlayProps) {
  const { t } = useTranslation();
  const { fire } = useConfetti();

  useEffect(() => {
    if (level !== null) {
      fire({ particleCount: 60, spread: 60 });
      const timer = setTimeout(onClose, 3500);
      return () => clearTimeout(timer);
    }
  }, [level]);

  return (
    <AnimatePresence>
      {level !== null && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.2 }}
          className="fixed inset-0 z-[10000] flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm"
          onClick={onClose}
        >
          <motion.div
            initial={{ scale: 0.3, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            exit={{ scale: 0.8, opacity: 0 }}
            transition={{ type: "spring", stiffness: 250, damping: 15 }}
            onClick={(e) => e.stopPropagation()}
            className="bg-gradient-to-br from-primary/20 to-accent/20 bg-card border border-primary/30 rounded-3xl p-8 max-w-xs w-full text-center shadow-2xl"
          >
            {/* Animated badge */}
            <motion.div
              initial={{ scale: 0, rotate: -20 }}
              animate={{ scale: 1, rotate: 0 }}
              transition={{ type: "spring", stiffness: 300, damping: 12, delay: 0.15 }}
              className="mx-auto mb-4"
            >
              <motion.div
                className="w-20 h-20 rounded-full bg-gradient-to-br from-primary to-primary/60 flex items-center justify-center mx-auto shadow-lg"
                animate={{
                  boxShadow: [
                    "0 0 0 0px rgba(34, 197, 94, 0.4)",
                    "0 0 0 16px rgba(34, 197, 94, 0)",
                  ],
                }}
                transition={{ duration: 1.5, repeat: Infinity }}
              >
                <Shield className="w-10 h-10 text-primary-foreground" />
              </motion.div>
            </motion.div>

            {/* Zap icons */}
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.3 }}
              className="flex justify-center gap-1 mb-2"
            >
              <Zap className="w-4 h-4 text-accent fill-accent" />
              <Zap className="w-5 h-5 text-accent fill-accent" />
              <Zap className="w-4 h-4 text-accent fill-accent" />
            </motion.div>

            {/* Level number */}
            <motion.div
              initial={{ opacity: 0, scale: 0.5 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ type: "spring", stiffness: 200, damping: 12, delay: 0.25 }}
            >
              <p className="text-sm text-muted-foreground font-medium uppercase tracking-wider">
                {t('levelUp.title', { defaultValue: 'Niv\u00e5 upp!' })}
              </p>
              <p className="text-5xl font-bold text-primary mt-1">
                {level}
              </p>
            </motion.div>

            <motion.p
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.5 }}
              className="text-sm text-muted-foreground mt-3"
            >
              {t('levelUp.subtitle', { defaultValue: 'Bra jobbat! Forts\u00e4tt s\u00e5!' })}
            </motion.p>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
