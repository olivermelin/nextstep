import { useTranslation } from "react-i18next";
import { Phone } from "lucide-react";

const SOSButton = () => {
  const { t } = useTranslation();

  return (
    <a
      href="tel:90101"
      className="fixed bottom-20 right-4 z-50 inline-flex items-center gap-2 font-bold rounded-full shadow-lg px-4 py-3 bg-destructive text-destructive-foreground hover:bg-destructive/90 transition-colors"
      aria-label={t("sos.buttonLabel")}
    >
      <Phone className="w-5 h-5" />
      {t("sos.callNow")}
    </a>
  );
};

export default SOSButton;
