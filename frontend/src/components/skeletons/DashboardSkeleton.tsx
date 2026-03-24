import { Skeleton } from "@/components/ui/skeleton";
import { Card } from "@/components/ui/card";

export const CoachMessageSkeleton = () => (
  <Card className="p-4 space-y-2">
    <Skeleton className="h-4 w-1/3" />
    <Skeleton className="h-4 w-full" />
    <Skeleton className="h-4 w-4/5" />
  </Card>
);

export const ActiveChallengesSkeleton = () => (
  <div className="space-y-3">
    {[1, 2].map((i) => (
      <Card key={i} className="p-4 space-y-2">
        <Skeleton className="h-4 w-1/2" />
        <Skeleton className="h-3 w-full" />
      </Card>
    ))}
  </div>
);

export const DashboardSkeleton = () => (
  <div className="space-y-4 p-4">
    <CoachMessageSkeleton />
    <ActiveChallengesSkeleton />
  </div>
);
