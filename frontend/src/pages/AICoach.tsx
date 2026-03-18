import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { AIChat } from "../components/AIChat";
import {
  MessageCircle,
  Lightbulb,
  Heart,
  Target,
  Zap,
  Info,
} from "lucide-react";

const AICoach: React.FC = () => {
  const { t } = useTranslation();
  const [quickPrompt, setQuickPrompt] = useState<string | null>(null);

  const quickPrompts = [
    { icon: Heart, label: t('aiCoach.quickPrompts.howAmI'), prompt: t('aiCoach.quickPrompts.howAmIPrompt') },
    { icon: Target, label: t('aiCoach.quickPrompts.setGoals'), prompt: t('aiCoach.quickPrompts.setGoalsPrompt') },
    { icon: Zap, label: t('aiCoach.quickPrompts.motivation'), prompt: t('aiCoach.quickPrompts.motivationPrompt') },
    { icon: Lightbulb, label: t('aiCoach.quickPrompts.dailyTip'), prompt: t('aiCoach.quickPrompts.dailyTipPrompt') },
  ];

  return (
    <div className="flex h-full bg-gradient-to-br from-background via-background to-primary/5 relative overflow-hidden">
      {/* Animated background elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div
          className="absolute top-1/4 -left-48 w-96 h-96 bg-primary/10 rounded-full blur-3xl animate-pulse"
          style={{ animationDuration: "8s" }}
        />
        <div
          className="absolute bottom-1/4 -right-48 w-96 h-96 bg-accent/10 rounded-full blur-3xl animate-pulse"
          style={{ animationDuration: "10s", animationDelay: "2s" }}
        />
      </div>

      {/* Main content area */}
      <div className="flex-1 flex relative z-10 p-4 gap-4 max-w-[1400px] mx-auto w-full h-full animate-in fade-in slide-in-from-bottom-4 duration-700">

        {/* Sidopanel - vänster (gömd på mobil) */}
        <aside className="hidden lg:flex flex-col w-72 flex-shrink-0 space-y-4">
          {/* Header-kort */}
          <div className="bg-card/80 backdrop-blur-xl border border-border/50 rounded-2xl shadow-lg p-5 text-center space-y-3">
            <div className="inline-flex items-center justify-center w-12 h-12 rounded-2xl bg-gradient-to-br from-primary to-primary/60 shadow-lg">
              <MessageCircle className="w-6 h-6 text-primary-foreground" />
            </div>
            <h1 className="text-xl font-bold bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent">
              {t('aiCoach.title')}
            </h1>
            <p className="text-xs text-muted-foreground leading-relaxed">
              {t('aiCoach.subtitle')}
            </p>
          </div>

          {/* Snabbåtgärder */}
          <div className="bg-card/80 backdrop-blur-xl border border-border/50 rounded-2xl shadow-lg p-4 space-y-3">
            <h2 className="text-xs font-semibold text-muted-foreground uppercase tracking-wider px-1">
              {t('aiCoach.quickStart')}
            </h2>
            <div className="space-y-1.5">
              {quickPrompts.map((item) => (
                <button
                  key={item.label}
                  onClick={() => setQuickPrompt(item.prompt)}
                  className="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-left text-sm font-medium text-foreground/80 hover:bg-primary/10 hover:text-foreground transition-all duration-200 group"
                >
                  <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0 group-hover:bg-primary/20 transition-colors">
                    <item.icon className="w-4 h-4 text-primary" />
                  </div>
                  <span>{item.label}</span>
                </button>
              ))}
            </div>
          </div>

          {/* Info-kort */}
          <div className="bg-card/80 backdrop-blur-xl border border-border/50 rounded-2xl shadow-lg p-4 space-y-3">
            <div className="flex items-center gap-2 text-xs font-semibold text-muted-foreground uppercase tracking-wider px-1">
              <Info className="w-3 h-3" />
              {t('aiCoach.aboutCoach')}
            </div>
            <div className="space-y-2 text-xs text-muted-foreground leading-relaxed px-1">
              <p>
                {t('aiCoach.aboutDesc1')}
              </p>
              <p>
                {t('aiCoach.aboutDesc2')}
              </p>
              <p className="text-primary/80 font-medium">
                {t('aiCoach.aboutDesc3')}
              </p>
            </div>
          </div>
        </aside>

        {/* Chat - huvudyta */}
        <div className="flex-1 flex flex-col min-w-0">
          {/* Mobil-header (visas bara på sm/md) */}
          <div className="lg:hidden text-center space-y-2 mb-4">
            <div className="inline-flex items-center justify-center w-11 h-11 rounded-xl bg-gradient-to-br from-primary to-primary/60 shadow-lg">
              <MessageCircle className="w-5 h-5 text-primary-foreground" />
            </div>
            <h1 className="text-2xl font-bold bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent">
              {t('aiCoach.title')}
            </h1>
          </div>

          {/* Mobil snabbstart-knappar */}
          <div className="lg:hidden flex gap-2 mb-3 overflow-x-auto pb-1 scrollbar-hide">
            {quickPrompts.map((item) => (
              <button
                key={item.label}
                onClick={() => setQuickPrompt(item.prompt)}
                className="flex-shrink-0 flex items-center gap-1.5 px-3 py-2 rounded-xl bg-card/80 backdrop-blur-sm border border-border/50 text-xs font-medium text-foreground/80 hover:bg-primary/10 transition-colors"
              >
                <item.icon className="w-3.5 h-3.5 text-primary" />
                {item.label}
              </button>
            ))}
          </div>

          {/* Chat-container */}
          <div className="bg-card/80 backdrop-blur-xl border border-border/50 rounded-3xl shadow-2xl flex-1 flex flex-col overflow-hidden min-h-[500px]">
            <AIChat quickPrompt={quickPrompt} onQuickPromptConsumed={() => setQuickPrompt(null)} />
          </div>
        </div>
      </div>
    </div>
  );
};

export default AICoach;
