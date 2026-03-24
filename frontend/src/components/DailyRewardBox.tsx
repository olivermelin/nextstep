import { Card } from "@/components/ui/card";
import { Gift } from "lucide-react";

interface DailyRewardBoxProps {
  userId?: string;
}

// Platshållare — implementeras i Sprint 2
const DailyRewardBox = (_props: DailyRewardBoxProps) => (
  <Card className="p-4 flex items-center gap-3 opacity-50">
    <Gift className="w-5 h-5 text-muted-foreground" />
    <div>
      <p className="text-sm font-medium">Daglig belöning</p>
      <p className="text-xs text-muted-foreground">Kommer snart</p>
    </div>
  </Card>
);

export default DailyRewardBox;
