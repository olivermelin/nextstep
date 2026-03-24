import { Button } from "@/components/ui/button";
import { Phone } from "lucide-react";

const SOSButton = () => (
  <Button
    variant="destructive"
    className="w-full gap-2 font-bold"
    onClick={() => window.open("tel:90101")}
    aria-label="SOS — Ring Mind Självmordslinjen 90101"
  >
    <Phone className="w-5 h-5" />
    SOS — Ring hjälp nu (90101)
  </Button>
);

export default SOSButton;
