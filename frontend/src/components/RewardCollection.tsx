import { Card } from "@/components/ui/card";
import { CollectibleData } from "@/services/rewardService";

interface RewardCollectionProps {
  collectibles?: CollectibleData[];
}

// Platshållare — implementeras i Sprint 2
const RewardCollection = ({ collectibles }: RewardCollectionProps) => (
  <Card className="p-4">
    <p className="text-sm font-medium mb-2">Samling</p>
    {collectibles && collectibles.length > 0 ? (
      <div className="flex gap-2 flex-wrap">
        {collectibles.map((c) => (
          <span key={c.id} className="text-2xl" title={c.name}>{c.emoji}</span>
        ))}
      </div>
    ) : (
      <p className="text-xs text-muted-foreground">Slutför utmaningar för att samla belöningar!</p>
    )}
  </Card>
);

export default RewardCollection;
