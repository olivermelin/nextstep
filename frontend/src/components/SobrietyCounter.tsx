import { Card } from "@/components/ui/card";

interface SobrietyCounterProps {
  userId: string;
}

const SobrietyCounter = ({ userId: _userId }: SobrietyCounterProps) => {
  return (
    <Card className="p-4 text-center">
      <p className="text-sm text-muted-foreground">Nykterhetsmätare</p>
      <p className="text-xs text-muted-foreground mt-1">Kommer snart</p>
    </Card>
  );
};

export default SobrietyCounter;
