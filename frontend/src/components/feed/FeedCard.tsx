import { useState } from "react";
import { motion } from "framer-motion";
import { Card } from "@/components/ui/card";
import { Target, Flame, Star, Trophy, Award, MoreHorizontal, Trash2 } from "lucide-react";
import { useTranslation } from "react-i18next";
import { FeedItemDto, feedService } from "@/services/feedService";
import { useAuth } from "@/context/AuthContext";
import CheerButton from "./CheerButton";

interface FeedCardProps {
  item: FeedItemDto;
  onUpdate: (itemId: number, cheerCount: number, iCheered: boolean) => void;
  onDelete: (itemId: number) => void;
}

const typeIcon = (type: FeedItemDto["type"]) => {
  const cls = "w-4 h-4";
  switch (type) {
    case "CHALLENGE_COMPLETED":  return <Target className={`${cls} text-primary`} />;
    case "STREAK_MILESTONE":     return <Flame  className={`${cls} text-orange-500`} />;
    case "LEVEL_UP":             return <Star   className={`${cls} text-amber-500`} />;
    case "DUEL_WON":             return <Trophy className={`${cls} text-yellow-500`} />;
    case "ACHIEVEMENT_UNLOCKED": return <Award  className={`${cls} text-purple-500`} />;
  }
};

const typeBg = (type: FeedItemDto["type"]) => {
  switch (type) {
    case "CHALLENGE_COMPLETED":  return "bg-primary/10";
    case "STREAK_MILESTONE":     return "bg-orange-100 dark:bg-orange-900/20";
    case "LEVEL_UP":             return "bg-amber-100 dark:bg-amber-900/20";
    case "DUEL_WON":             return "bg-yellow-100 dark:bg-yellow-900/20";
    case "ACHIEVEMENT_UNLOCKED": return "bg-purple-100 dark:bg-purple-900/20";
  }
};

/** Relativ tid på svenska/engelska utan bibliotek */
const relativeTime = (iso: string, t: (k: string, opts?: object) => string): string => {
  const diff = (Date.now() - new Date(iso).getTime()) / 1000;
  if (diff < 60)   return t("feed.ago.justNow");
  if (diff < 3600) return t("feed.ago.minutesAgo", { n: Math.floor(diff / 60) });
  if (diff < 86400) return t("feed.ago.hoursAgo", { n: Math.floor(diff / 3600) });
  if (diff < 172800) return t("feed.ago.yesterday");
  return t("feed.ago.daysAgo", { n: Math.floor(diff / 86400) });
};

const FeedCard = ({ item, onUpdate, onDelete }: FeedCardProps) => {
  const { t } = useTranslation();
  const { user } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const isOwn = item.userId === (user?.email || user?.id);
  const initials = item.displayName?.slice(0, 2).toUpperCase() || "??";

  const handleDelete = async () => {
    const userId = user?.email || user?.id || "";
    if (!userId) return;
    setDeleting(true);
    try {
      await feedService.deleteFeedItem(userId, item.id);
      onDelete(item.id);
    } catch {
      setDeleting(false);
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 4 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, scale: 0.97 }}
      layout
    >
      <Card className="p-4 hover:border-primary/20 transition-colors">
        <div className="flex items-start gap-3">
          {/* Avatar */}
          <div className="w-9 h-9 rounded-full bg-primary/15 flex items-center justify-center flex-shrink-0 text-xs font-semibold text-primary">
            {initials}
          </div>

          {/* Innehåll */}
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-1.5 mb-1 flex-wrap">
              <div className={`p-1 rounded-md ${typeBg(item.type)}`}>
                {typeIcon(item.type)}
              </div>
              <p className="text-sm text-foreground leading-snug">{item.message}</p>
            </div>
            <p className="text-xs text-muted-foreground mb-2">
              {relativeTime(item.createdAt, t)}
              {item.recentCheers.length > 0 && (
                <span className="ml-2 text-muted-foreground/70">
                  · {item.recentCheers.map(r => r.displayName.split(" ")[0]).join(", ")} hejar
                </span>
              )}
            </p>

            {/* Heja-knapp */}
            <CheerButton item={item} onUpdate={onUpdate} />
          </div>

          {/* Meny för egna inlägg */}
          {isOwn && (
            <div className="relative flex-shrink-0">
              <button
                onClick={() => setMenuOpen(o => !o)}
                className="p-1 rounded-md text-muted-foreground hover:text-foreground hover:bg-muted/60 transition-colors"
              >
                <MoreHorizontal className="w-4 h-4" />
              </button>
              {menuOpen && (
                <div className="absolute right-0 top-7 z-10 bg-popover border border-border rounded-lg shadow-md py-1 min-w-[150px]">
                  <button
                    onClick={handleDelete}
                    disabled={deleting}
                    className="flex items-center gap-2 w-full px-3 py-2 text-sm text-destructive hover:bg-muted/60 transition-colors"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                    {t("feed.deleteItem")}
                  </button>
                </div>
              )}
            </div>
          )}
        </div>
      </Card>
    </motion.div>
  );
};

export default FeedCard;
