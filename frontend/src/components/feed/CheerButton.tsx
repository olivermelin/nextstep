import { useState } from "react";
import { HandHeart } from "lucide-react";
import { useTranslation } from "react-i18next";
import { feedService, FeedItemDto } from "@/services/feedService";
import { useAuth } from "@/context/AuthContext";
import { cn } from "@/lib/utils";

interface CheerButtonProps {
  item: FeedItemDto;
  onUpdate: (itemId: number, cheerCount: number, iCheered: boolean) => void;
}

const CheerButton = ({ item, onUpdate }: CheerButtonProps) => {
  const { t } = useTranslation();
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const userId = user?.email || user?.id || "";

  const handleCheer = async () => {
    if (!userId || loading) return;

    // Optimistisk UI
    const optimisticCount = item.iCheered ? item.cheerCount - 1 : item.cheerCount + 1;
    const optimisticCheered = !item.iCheered;
    onUpdate(item.id, optimisticCount, optimisticCheered);

    setLoading(true);
    try {
      const result = await feedService.toggleCheer(userId, item.id);
      onUpdate(item.id, result.cheerCount, result.iCheered);
    } catch {
      // Återställ vid fel
      onUpdate(item.id, item.cheerCount, item.iCheered);
    } finally {
      setLoading(false);
    }
  };

  return (
    <button
      onClick={handleCheer}
      disabled={loading}
      className={cn(
        "flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium transition-all",
        item.iCheered
          ? "bg-rose-100 text-rose-600 dark:bg-rose-900/30 dark:text-rose-400"
          : "bg-muted text-muted-foreground hover:bg-rose-50 hover:text-rose-500 dark:hover:bg-rose-900/20"
      )}
    >
      <HandHeart className={cn("w-3.5 h-3.5", loading && "opacity-50")} />
      <span>
        {item.iCheered ? t("feed.cheered") : t("feed.cheer")}
        {item.cheerCount > 0 && ` · ${item.cheerCount}`}
      </span>
    </button>
  );
};

export default CheerButton;
