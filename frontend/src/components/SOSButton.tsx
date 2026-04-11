import { useState } from "react";
import { useTranslation } from "react-i18next";
import { Phone, X } from "lucide-react";

const SOSButton = () => {
  const { t } = useTranslation();
  const [showConfirm, setShowConfirm] = useState(false);

  if (showConfirm) {
    return (
      <div
        className="fixed bottom-20 right-4 z-50 bg-card border border-border/50 rounded-2xl shadow-xl p-4 space-y-3 w-64 animate-in fade-in slide-in-from-bottom-2 duration-200"
        role="dialog"
        aria-modal="true"
        aria-label={t("sos.title")}
      >
        <div className="flex items-center justify-between">
          <p className="text-sm font-semibold">{t("sos.confirmPrompt")}</p>
          <button
            onClick={() => setShowConfirm(false)}
            className="p-1 rounded-lg hover:bg-muted transition-colors text-muted-foreground"
            aria-label={t("sos.confirmCancel")}
          >
            <X className="w-4 h-4" />
          </button>
        </div>
        <a
          href="tel:112"
          className="flex items-center gap-2 w-full bg-destructive text-destructive-foreground font-bold rounded-xl px-4 py-3 text-sm hover:bg-destructive/90 transition-colors"
        >
          <Phone className="w-4 h-4" />
          {t("sos.confirmCall112")}
        </a>
        <a
          href="tel:90101"
          className="flex items-center gap-2 w-full bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-300 font-bold rounded-xl px-4 py-3 text-sm hover:bg-red-200 dark:hover:bg-red-900/50 transition-colors"
        >
          <Phone className="w-4 h-4" />
          {t("sos.confirmCallMind")}
        </a>
        <button
          onClick={() => setShowConfirm(false)}
          className="w-full py-2 text-sm text-muted-foreground hover:text-foreground transition-colors"
        >
          {t("sos.confirmCancel")}
        </button>
      </div>
    );
  }

  return (
    <button
      onClick={() => setShowConfirm(true)}
      className="fixed bottom-20 right-4 z-50 inline-flex items-center gap-2 font-bold rounded-full shadow-lg px-4 py-3 bg-destructive text-destructive-foreground hover:bg-destructive/90 transition-colors"
      aria-label={t("sos.buttonLabel")}
    >
      <Phone className="w-5 h-5" />
      {t("sos.callNow")}
    </button>
  );
};

export default SOSButton;
