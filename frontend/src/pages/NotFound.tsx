import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Home } from "lucide-react";
import { Button } from "@/components/ui/button";

const NotFound = () => {
  const { t } = useTranslation();

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-primary/5 via-background to-secondary/5 p-4">
      <div className="text-center space-y-6">
        <div className="inline-block p-4 rounded-2xl bg-gradient-to-br from-primary/10 to-primary/5 mb-2">
          <span className="text-6xl">🔍</span>
        </div>
        <h1 className="text-6xl font-bold text-foreground">{t('notFound.title')}</h1>
        <p className="text-xl text-muted-foreground">{t('notFound.message')}</p>
        <Button asChild variant="default" className="gap-2">
          <Link to="/" aria-label={t('notFound.returnHome')}>
            <Home className="w-4 h-4" />
            {t('notFound.returnHome')}
          </Link>
        </Button>
      </div>
    </div>
  );
};

export default NotFound;
