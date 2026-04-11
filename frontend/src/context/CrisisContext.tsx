import { createContext, useContext, useState, type ReactNode } from "react";

interface CrisisContextType {
  showCrisisBanner: boolean;
  setShowCrisisBanner: (show: boolean) => void;
}

const CrisisContext = createContext<CrisisContextType>({
  showCrisisBanner: false,
  setShowCrisisBanner: () => {},
});

export function CrisisProvider({ children }: { children: ReactNode }) {
  const [showCrisisBanner, setShowCrisisBanner] = useState(false);
  return (
    <CrisisContext.Provider value={{ showCrisisBanner, setShowCrisisBanner }}>
      {children}
    </CrisisContext.Provider>
  );
}

export const useCrisis = () => useContext(CrisisContext);
