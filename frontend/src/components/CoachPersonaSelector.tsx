import { CheckCircle2 } from "lucide-react";

export type CoachPersonality = "SUPPORTIVE" | "MOTIVATIONAL" | "REFLECTIVE" | "DIRECT";

interface Persona {
  id: CoachPersonality;
  name: string;
  emoji: string;
  tagline: string;
  description: string;
  color: string;
}

const PERSONAS: Persona[] = [
  {
    id: "SUPPORTIVE",
    name: "Maja",
    emoji: "🤗",
    tagline: "Varm & stödjande",
    description: "Lyssnar, validerar och ger omtänksamt stöd utan att döma.",
    color: "border-rose-200 hover:border-rose-400 dark:border-rose-800 dark:hover:border-rose-500",
  },
  {
    id: "MOTIVATIONAL",
    name: "Erik",
    emoji: "⚡",
    tagline: "Energisk & peppande",
    description: "Fokuserar på dina styrkor och peppar dig att ta nästa steg.",
    color: "border-amber-200 hover:border-amber-400 dark:border-amber-800 dark:hover:border-amber-500",
  },
  {
    id: "REFLECTIVE",
    name: "Lena",
    emoji: "🌿",
    tagline: "Lugn & reflekterande",
    description: "Hjälper dig att förstå dig själv på djupet med tankeväckande frågor.",
    color: "border-emerald-200 hover:border-emerald-400 dark:border-emerald-800 dark:hover:border-emerald-500",
  },
  {
    id: "DIRECT",
    name: "Alex",
    emoji: "🎯",
    tagline: "Rakt på sak",
    description: "Ärlig, konkret och fokuserad på lösningar — ingen omväg.",
    color: "border-blue-200 hover:border-blue-400 dark:border-blue-800 dark:hover:border-blue-500",
  },
];

interface CoachPersonaSelectorProps {
  value: CoachPersonality;
  onChange: (personality: CoachPersonality) => void;
}

const CoachPersonaSelector = ({ value, onChange }: CoachPersonaSelectorProps) => {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
      {PERSONAS.map((persona) => {
        const isSelected = value === persona.id;
        return (
          <button
            key={persona.id}
            type="button"
            onClick={() => onChange(persona.id)}
            className={`relative text-left p-4 rounded-2xl border-2 transition-all duration-200 ${persona.color} ${
              isSelected
                ? "bg-primary/5 dark:bg-primary/10 ring-2 ring-primary/40"
                : "bg-card hover:bg-muted/50"
            }`}
          >
            {isSelected && (
              <CheckCircle2 className="absolute top-3 right-3 w-4 h-4 text-primary" />
            )}
            <span className="text-2xl block mb-2">{persona.emoji}</span>
            <p className="font-semibold text-sm text-foreground">{persona.name}</p>
            <p className="text-xs font-medium text-primary/80 mb-1">{persona.tagline}</p>
            <p className="text-xs text-muted-foreground leading-relaxed">{persona.description}</p>
          </button>
        );
      })}
    </div>
  );
};

export default CoachPersonaSelector;
