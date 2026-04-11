import { Skeleton } from "@/components/ui/skeleton";
import { Card } from "@/components/ui/card";

const FeedSkeleton = () => (
  <div className="space-y-3">
    {[1, 2, 3].map((i) => (
      <Card key={i} className="p-4">
        <div className="flex items-start gap-3">
          <Skeleton className="w-10 h-10 rounded-full flex-shrink-0" />
          <div className="flex-1 space-y-2">
            <Skeleton className="h-4 w-3/4" />
            <Skeleton className="h-3 w-1/2" />
          </div>
          <Skeleton className="h-8 w-16 rounded-full" />
        </div>
      </Card>
    ))}
  </div>
);

export default FeedSkeleton;
