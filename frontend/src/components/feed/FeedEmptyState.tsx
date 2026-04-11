import { Users } from "lucide-react";
import { useTranslation } from "react-i18next";

interface FeedEmptyStateProps {
  hasFriends: boolean;
}

const FeedEmptyState = ({ hasFriends }: FeedEmptyStateProps) => {
  const { t } = useTranslation();
  return (
    <div className="flex flex-col items-center justify-center py-14 text-center px-4">
      <div className="w-14 h-14 rounded-full bg-muted flex items-center justify-center mb-4">
        <Users className="w-6 h-6 text-muted-foreground" />
      </div>
      <p className="text-muted-foreground text-sm max-w-xs">
        {hasFriends ? t("feed.empty") : t("feed.emptyNoFriends")}
      </p>
    </div>
  );
};

export default FeedEmptyState;
