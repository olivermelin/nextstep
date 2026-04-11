import { Card } from "@/components/ui/card";
import { CollectibleData, RewardType } from "@/services/rewardService";
import { useTranslation } from 'react-i18next';

interface RewardCollectionProps {
  collectibles?: CollectibleData[];
}

const typeEmoji: Record<RewardType, string> = {
  QUOTE_CARD:    '📜',
  AVATAR_FRAME:  '🌟',
  BONUS_XP:      '⚡',
  STREAK_FREEZE: '🧊',
};

const RewardCollection = ({ collectibles }: RewardCollectionProps) => {
  const { t } = useTranslation();

  return (
    <Card className="p-4">
      <p className="text-sm font-semibold text-foreground mb-3">{t('reward.collection')}</p>
      {collectibles && collectibles.length > 0 ? (
        <div className="flex gap-2 flex-wrap">
          {collectibles.map((c) => (
            <div
              key={c.collectibleKey}
              className="w-12 h-12 rounded-xl bg-muted flex items-center justify-center text-2xl hover:scale-110 transition-transform cursor-default"
              title={`${c.collectibleType} · ${new Date(c.unlockedAt).toLocaleDateString('sv-SE')}`}
            >
              {typeEmoji[c.collectibleType] ?? '🎁'}
            </div>
          ))}
        </div>
      ) : (
        <p className="text-xs text-muted-foreground">{t('reward.emptyCollection')}</p>
      )}
    </Card>
  );
};

export default RewardCollection;
