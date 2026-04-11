import { useState, useEffect, useRef, useCallback } from "react";
import { AnimatePresence } from "framer-motion";
import { useTranslation } from "react-i18next";
import { feedService, FeedItemDto, FeedPageDto } from "@/services/feedService";
import FeedCard from "./FeedCard";
import FeedSkeleton from "./FeedSkeleton";
import FeedEmptyState from "./FeedEmptyState";

interface FeedListProps {
  userId: string;
  hasFriends: boolean;
}

const FeedList = ({ userId, hasFriends }: FeedListProps) => {
  const { t } = useTranslation();
  const [items, setItems] = useState<FeedItemDto[]>([]);
  const [nextCursor, setNextCursor] = useState<number | undefined>(undefined);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const sentinelRef = useRef<HTMLDivElement>(null);

  const loadPage = useCallback(async (cursor?: number) => {
    if (!userId) return;
    const isFirst = cursor === undefined;
    if (isFirst) setLoading(true); else setLoadingMore(true);

    try {
      const page: FeedPageDto = await feedService.getFeed(userId, cursor);
      setItems(prev => isFirst ? page.items : [...prev, ...page.items]);
      setNextCursor(page.nextCursor ?? undefined);
      setHasMore(page.hasMore);
    } catch {
      // Tyst fel — flödet visas tomt
    } finally {
      if (isFirst) setLoading(false); else setLoadingMore(false);
    }
  }, [userId]);

  // Initial laddning
  useEffect(() => {
    loadPage();
  }, [loadPage]);

  // IntersectionObserver för infinite scroll
  useEffect(() => {
    if (!sentinelRef.current || !hasMore || loadingMore) return;
    const observer = new IntersectionObserver(entries => {
      if (entries[0].isIntersecting && hasMore && !loadingMore && nextCursor !== undefined) {
        loadPage(nextCursor);
      }
    }, { threshold: 0.1 });
    observer.observe(sentinelRef.current);
    return () => observer.disconnect();
  }, [hasMore, loadingMore, nextCursor, loadPage]);

  // Uppdatera heja-data optimistiskt
  const handleCheerUpdate = (itemId: number, cheerCount: number, iCheered: boolean) => {
    setItems(prev =>
      prev.map(item =>
        item.id === itemId ? { ...item, cheerCount, iCheered } : item
      )
    );
  };

  // Ta bort soft-raderat inlägg från listan
  const handleDelete = (itemId: number) => {
    setItems(prev => prev.filter(item => item.id !== itemId));
  };

  if (loading) return <FeedSkeleton />;

  if (!loading && items.length === 0) {
    return <FeedEmptyState hasFriends={hasFriends} />;
  }

  return (
    <div className="space-y-3">
      <AnimatePresence>
        {items.map(item => (
          <FeedCard
            key={item.id}
            item={item}
            onUpdate={handleCheerUpdate}
            onDelete={handleDelete}
          />
        ))}
      </AnimatePresence>

      {/* Sentinel för infinite scroll */}
      <div ref={sentinelRef} className="h-4" />

      {loadingMore && (
        <p className="text-center text-xs text-muted-foreground py-2">
          {t("feed.loading")}
        </p>
      )}
    </div>
  );
};

export default FeedList;
