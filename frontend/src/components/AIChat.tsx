import React, { useState, useRef, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { Button } from "./ui/button";
import { Loader2, Send, AlertCircle, RotateCcw, Phone, ShieldAlert, Bot, User, Target, ArrowRight, Clock } from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import {
  sendCoachMessage,
  getCoachStatus,
  type CrisisLevel,
  type CoachStatusResponse,
  type SuggestedChallenge,
} from "@/services/coachService";

interface Message {
  id: string;
  text: string;
  sender: "user" | "ai";
  timestamp: Date;
  error?: boolean;
  crisisLevel?: CrisisLevel;
  suggestedChallenges?: SuggestedChallenge[];
}

// --- Krishantering-komponenter ---

const CrisisBanner: React.FC = () => {
  const { t } = useTranslation();
  return (
  <div className="mx-4 mt-3 bg-gradient-to-r from-red-600 to-red-500 text-white p-4 rounded-2xl flex items-center gap-3 animate-in fade-in shadow-lg">
    <ShieldAlert className="w-5 h-5 flex-shrink-0" />
    <div className="flex-1 text-sm font-medium">
      {t('aiCoach.crisisBanner')}
    </div>
    <div className="flex gap-2 flex-shrink-0">
      <a
        href="tel:90101"
        className="inline-flex items-center gap-1 bg-white/90 text-red-600 rounded-lg px-3 py-1.5 text-xs font-bold hover:bg-white transition-colors shadow-sm"
      >
        <Phone className="w-3 h-3" /> Mind: 90101
      </a>
      <a
        href="tel:112"
        className="inline-flex items-center gap-1 bg-white/90 text-red-600 rounded-lg px-3 py-1.5 text-xs font-bold hover:bg-white transition-colors shadow-sm"
      >
        <Phone className="w-3 h-3" /> SOS: 112
      </a>
    </div>
  </div>
  );
};

function crisisBubbleClasses(crisisLevel?: CrisisLevel): string {
  switch (crisisLevel) {
    case "ELEVATED":
      return "bg-orange-50 border-2 border-orange-300 text-orange-900 dark:bg-orange-950/40 dark:border-orange-500 dark:text-orange-100";
    case "CRITICAL":
      return "bg-red-50 border-2 border-red-400 text-red-900 dark:bg-red-950/40 dark:border-red-500 dark:text-red-100";
    default:
      return "bg-blue-50 border border-blue-200/60 text-blue-950 dark:bg-blue-950/40 dark:border-blue-800/50 dark:text-blue-100";
  }
}

// --- Markdown-formatering ---

export function formatMarkdown(text: string): React.ReactNode[] {
  // Split by newlines first to handle paragraphs
  const lines = text.split(/\n/);
  const result: React.ReactNode[] = [];

  lines.forEach((line, lineIndex) => {
    if (lineIndex > 0) {
      result.push(<br key={`br-${lineIndex}`} />);
    }

    // Strip markdown headers (## etc.)
    const strippedLine = line.replace(/^#{1,6}\s+/, "");

    // Split by **bold** and *italic* patterns
    const parts = strippedLine.split(/(\*\*[^*]+\*\*|\*[^*]+\*)/g);
    parts.forEach((part, i) => {
      if (part.startsWith("**") && part.endsWith("**")) {
        result.push(<strong key={`${lineIndex}-${i}`}>{part.slice(2, -2)}</strong>);
      } else if (part.startsWith("*") && part.endsWith("*")) {
        result.push(<em key={`${lineIndex}-${i}`}>{part.slice(1, -1)}</em>);
      } else if (part) {
        result.push(<span key={`${lineIndex}-${i}`}>{part}</span>);
      }
    });
  });

  return result;
}

// --- Challenge-rekommendationer ---

function getDifficultyLabel(difficulty: string): string {
  switch (difficulty) {
    case "EASY": return "Lätt";
    case "MEDIUM": return "Medel";
    case "HARD": return "Svår";
    default: return difficulty;
  }
}

function getDifficultyColor(difficulty: string): string {
  switch (difficulty) {
    case "EASY": return "text-green-600 bg-green-100 dark:text-green-400 dark:bg-green-900/30";
    case "MEDIUM": return "text-amber-600 bg-amber-100 dark:text-amber-400 dark:bg-amber-900/30";
    case "HARD": return "text-red-600 bg-red-100 dark:text-red-400 dark:bg-red-900/30";
    default: return "text-muted-foreground bg-muted";
  }
}

const ChallengeCards: React.FC<{ challenges: SuggestedChallenge[] }> = ({ challenges }) => {
  const navigate = useNavigate();

  if (!challenges || challenges.length === 0) return null;

  return (
    <div className="mt-3 space-y-2">
      {challenges.map((challenge) => (
        <button
          key={challenge.id}
          onClick={() => navigate(challenge.url)}
          className="w-full text-left bg-white/80 dark:bg-white/5 border border-primary/20 rounded-xl p-3
                     hover:border-primary/50 hover:bg-primary/5 dark:hover:bg-primary/10
                     transition-all duration-200 group cursor-pointer"
        >
          <div className="flex items-start gap-3">
            <div className="flex-shrink-0 w-9 h-9 rounded-lg bg-primary/10 flex items-center justify-center mt-0.5">
              <Target className="w-4 h-4 text-primary" />
            </div>
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2 mb-1">
                <span className="font-semibold text-sm text-foreground group-hover:text-primary transition-colors">
                  {challenge.title}
                </span>
                <ArrowRight className="w-3.5 h-3.5 text-primary opacity-0 group-hover:opacity-100 transition-opacity" />
              </div>
              <p className="text-xs text-muted-foreground line-clamp-2 mb-1.5">{challenge.description}</p>
              <div className="flex items-center gap-2">
                <span className={`text-xs font-medium px-1.5 py-0.5 rounded ${getDifficultyColor(challenge.difficulty)}`}>
                  {getDifficultyLabel(challenge.difficulty)}
                </span>
                <span className="text-xs text-muted-foreground flex items-center gap-1">
                  <Clock className="w-3 h-3" />
                  {challenge.durationMinutes} min
                </span>
              </div>
            </div>
          </div>
        </button>
      ))}
    </div>
  );
};

// --- Huvudkomponent ---

interface AIChatProps {
  quickPrompt?: string | null;
  onQuickPromptConsumed?: () => void;
}

export const AIChat: React.FC<AIChatProps> = ({ quickPrompt, onQuickPromptConsumed }) => {
  const { t } = useTranslation();
  const { user } = useAuth();
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [showCrisisBanner, setShowCrisisBanner] = useState(false);
  const [coachStatus, setCoachStatus] = useState<CoachStatusResponse | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    getCoachStatus()
      .then(setCoachStatus)
      .catch((err) => console.warn("Kunde inte hämta coach-status:", err));
  }, []);

  // Hantera snabbåtgärd från sidopanelen
  useEffect(() => {
    if (quickPrompt) {
      setInput(quickPrompt);
      onQuickPromptConsumed?.();
    }
  }, [quickPrompt]);

  const handleNewConversation = () => {
    setSessionId(null);
    setMessages([]);
    setError(null);
    setShowCrisisBanner(false);
  };

  const handleSendMessage = async () => {
    if (!input.trim() || !user) return;

    setError(null);

    const userMessage: Message = {
      id: Date.now().toString(),
      text: input,
      sender: "user",
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMessage]);
    const messageText = input;
    setInput("");
    setLoading(true);

    try {
      const data = await sendCoachMessage(user.email || user.id, messageText, sessionId);

      if (data.sessionId) {
        setSessionId(data.sessionId);
      }

      if (data.crisisLevel === "CRITICAL") {
        setShowCrisisBanner(true);
      }

      const aiMessage: Message = {
        id: (Date.now() + 1).toString(),
        text: data.response || t('aiCoach.noResponse'),
        sender: "ai",
        timestamp: new Date(),
        crisisLevel: data.crisisLevel,
        suggestedChallenges: data.suggestedChallenges,
      };

      setMessages((prev) => [...prev, aiMessage]);
    } catch (err) {
      console.error("Fel vid kommunikation med backend:", err);

      let errorText = t('aiCoach.couldNotConnect');

      if (err instanceof Error) {
        if (err.message === "insufficient_quota") {
          errorText = t('aiCoach.aiUnavailable');
        } else if (err.message === "server_error") {
          errorText = t('aiCoach.serverError');
        }
        setError(errorText);
      }

      const errorMessage: Message = {
        id: (Date.now() + 1).toString(),
        text: errorText,
        sender: "ai",
        timestamp: new Date(),
        error: true,
      };

      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col h-full">
      {/* Status bar - glassmorphism */}
      <div className="flex items-center justify-between px-5 py-3 border-b border-border/30 bg-background/50 backdrop-blur-sm">
        <div className="flex items-center gap-2 text-xs text-muted-foreground">
          {coachStatus ? (
            <>
              <span
                className={`inline-block w-2 h-2 rounded-full shadow-sm ${
                  coachStatus.aiAvailable
                    ? "bg-green-500 shadow-green-500/50"
                    : "bg-orange-400 shadow-orange-400/50"
                }`}
              />
              <span className="font-medium">{coachStatus.status}</span>
            </>
          ) : (
            <>
              <Loader2 className="w-3 h-3 animate-spin" />
              <span>{t('aiCoach.connecting')}</span>
            </>
          )}
        </div>
        <button
          onClick={handleNewConversation}
          className="flex items-center gap-1.5 text-xs text-muted-foreground hover:text-foreground font-medium transition-colors"
        >
          <RotateCcw className="w-3 h-3" />
          {t('aiCoach.newConversation')}
        </button>
      </div>

      {/* Persistent kris-banner vid CRITICAL */}
      {showCrisisBanner && <CrisisBanner />}

      {/* Meddelandelista */}
      <div className="flex-1 overflow-y-auto p-4 bg-gradient-to-b from-background/30 to-background/10">
        <div className="w-full max-w-3xl mx-auto space-y-4">
          {messages.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-full py-16 gap-4 animate-in fade-in duration-500">
              <div className="w-12 h-12 rounded-2xl bg-primary/10 flex items-center justify-center">
                <Bot className="w-6 h-6 text-primary" />
              </div>
              <div className="text-center space-y-1">
                <p className="text-sm font-medium text-foreground/80">
                  {t('aiCoach.startConversation')}
                </p>
                <p className="text-xs text-muted-foreground">
                  {t('aiCoach.startConversationDesc')}
                </p>
              </div>
            </div>
          ) : (
            <>
              {messages.map((msg) => (
                <div
                  key={msg.id}
                  className={`flex items-end gap-2 animate-in fade-in slide-in-from-bottom-2 duration-300 ${
                    msg.sender === "user" ? "justify-end" : "justify-start"
                  }`}
                >
                  {/* AI avatar */}
                  {msg.sender === "ai" && (
                    <div className="flex-shrink-0 w-7 h-7 rounded-full bg-primary/10 flex items-center justify-center mb-1">
                      <Bot className="w-4 h-4 text-primary" />
                    </div>
                  )}

                  <div
                    className={`max-w-[75%] px-4 py-3 shadow-sm ${
                      msg.error
                        ? "bg-destructive/10 border border-destructive/30 rounded-2xl rounded-bl-md text-destructive dark:bg-red-950/30 dark:border-red-700/50 dark:text-red-300"
                        : msg.sender === "user"
                        ? "bg-gradient-to-r from-primary to-primary/80 text-primary-foreground rounded-2xl rounded-br-md shadow-md"
                        : `${crisisBubbleClasses(msg.crisisLevel)} rounded-2xl rounded-bl-md`
                    }`}
                  >
                    {msg.error && (
                      <div className="flex items-center gap-1.5 mb-1.5">
                        <AlertCircle className="w-3.5 h-3.5" />
                        <span className="text-xs font-medium">{t('aiCoach.errorLabel')}</span>
                      </div>
                    )}
                    {msg.crisisLevel === "ELEVATED" && (
                      <div className="flex items-center gap-1.5 mb-1.5 text-orange-600 dark:text-orange-400">
                        <AlertCircle className="w-3.5 h-3.5" />
                        <span className="text-xs font-medium">{t('aiCoach.supportAvailable')}</span>
                      </div>
                    )}
                    {msg.crisisLevel === "CRITICAL" && (
                      <div className="flex items-center gap-1.5 mb-1.5 text-red-600 dark:text-red-400">
                        <ShieldAlert className="w-3.5 h-3.5" />
                        <span className="text-xs font-medium">{t('aiCoach.crisisSupport')}</span>
                      </div>
                    )}
                    <p className="text-sm leading-relaxed whitespace-pre-wrap text-left">{formatMarkdown(msg.text)}</p>
                    {msg.suggestedChallenges && msg.suggestedChallenges.length > 0 && (
                      <ChallengeCards challenges={msg.suggestedChallenges} />
                    )}
                  </div>

                  {/* User avatar */}
                  {msg.sender === "user" && (
                    <div className="flex-shrink-0 w-7 h-7 rounded-full bg-primary/20 flex items-center justify-center mb-1">
                      <User className="w-4 h-4 text-primary" />
                    </div>
                  )}
                </div>
              ))}
              {loading && (
                <div className="flex items-end gap-2 animate-in fade-in">
                  <div className="flex-shrink-0 w-7 h-7 rounded-full bg-primary/10 flex items-center justify-center mb-1">
                    <Bot className="w-4 h-4 text-primary" />
                  </div>
                  <div className="bg-blue-50 border border-blue-200/60 dark:bg-blue-950/40 dark:border-blue-800/50 rounded-2xl rounded-bl-md px-4 py-3 shadow-sm">
                    <div className="flex gap-1.5">
                      <span className="w-2 h-2 rounded-full bg-muted-foreground/40 animate-bounce" style={{ animationDelay: "0ms" }} />
                      <span className="w-2 h-2 rounded-full bg-muted-foreground/40 animate-bounce" style={{ animationDelay: "150ms" }} />
                      <span className="w-2 h-2 rounded-full bg-muted-foreground/40 animate-bounce" style={{ animationDelay: "300ms" }} />
                    </div>
                  </div>
                </div>
              )}
              <div ref={messagesEndRef} />
            </>
          )}
        </div>
      </div>

      {/* Input-area */}
      <div className="border-t border-border/30 p-4 bg-background/50 backdrop-blur-sm">
        <div className="w-full max-w-3xl mx-auto space-y-2">
          {error && (
            <div className="bg-destructive/10 border border-destructive/30 rounded-xl p-3 text-sm text-destructive dark:bg-red-950/30 dark:border-red-700/50 dark:text-red-300 animate-in fade-in">
              <div className="flex items-center gap-2">
                <AlertCircle className="w-4 h-4 flex-shrink-0" />
                <span>{error}</span>
              </div>
            </div>
          )}
          <div className="flex gap-2">
            <input
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && !loading && handleSendMessage()}
              placeholder={t('aiCoach.inputPlaceholder')}
              disabled={loading}
              className="flex-1 px-4 py-3.5 bg-background/50 border border-border/50 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all text-foreground placeholder:text-muted-foreground disabled:opacity-50"
            />
            <button
              onClick={handleSendMessage}
              disabled={loading || !input.trim()}
              className="flex items-center justify-center w-12 h-12 bg-gradient-to-r from-primary to-primary/80 text-primary-foreground rounded-xl hover:shadow-lg transition-all duration-300 hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100 disabled:hover:shadow-none"
            >
              {loading ? (
                <Loader2 className="w-5 h-5 animate-spin" />
              ) : (
                <Send className="w-5 h-5" />
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
export default AIChat;