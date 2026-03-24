import { Skeleton } from "@/components/ui/skeleton";
import { Card } from "@/components/ui/card";

export function CategoriesGridSkeleton() {
  return (
    <div className="space-y-6">
      {/* Tabs */}
      <div className="flex gap-2">
        {[1, 2, 3].map((i) => (
          <Skeleton key={i} className="h-9 w-24 rounded-lg" />
        ))}
      </div>
      {/* Category Cards */}
      <div className="grid grid-cols-2 gap-4">
        {[1, 2, 3, 4].map((i) => (
          <Card key={i} className="p-6 space-y-3">
            <Skeleton className="w-12 h-12 rounded-xl" />
            <Skeleton className="h-5 w-24" />
            <Skeleton className="h-3 w-full" />
            <Skeleton className="h-3 w-3/4" />
          </Card>
        ))}
      </div>
    </div>
  );
}

export function ChallengeListSkeleton() {
  return (
    <div className="space-y-4">
      {[1, 2, 3, 4].map((i) => (
        <Card key={i} className="p-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Skeleton className="w-10 h-10 rounded-xl" />
              <div className="space-y-2">
                <Skeleton className="h-4 w-40" />
                <Skeleton className="h-3 w-56" />
              </div>
            </div>
            <Skeleton className="h-9 w-20 rounded-md" />
          </div>
        </Card>
      ))}
    </div>
  );
}
