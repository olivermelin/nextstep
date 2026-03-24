import { Skeleton } from "@/components/ui/skeleton";
import { Card } from "@/components/ui/card";

export const ProgressPageSkeleton = () => (
  <div className="space-y-4 p-4">
    <Card className="p-4 space-y-3">
      <Skeleton className="h-6 w-1/3" />
      <Skeleton className="h-10 w-1/4" />
      <Skeleton className="h-3 w-full rounded-full" />
    </Card>
    {[1, 2, 3].map((i) => (
      <Card key={i} className="p-4 space-y-2">
        <Skeleton className="h-4 w-1/2" />
        <Skeleton className="h-3 w-full" />
      </Card>
    ))}
  </div>
);
