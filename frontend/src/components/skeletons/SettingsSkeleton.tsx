import { Skeleton } from "@/components/ui/skeleton";
import { Card } from "@/components/ui/card";

export const SettingsPageSkeleton = () => (
  <div className="space-y-4 p-4">
    {[1, 2, 3].map((i) => (
      <Card key={i} className="p-4 space-y-3">
        <Skeleton className="h-4 w-1/3" />
        <Skeleton className="h-10 w-full" />
      </Card>
    ))}
  </div>
);
