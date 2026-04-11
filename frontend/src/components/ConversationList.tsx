import { useTranslation } from "react-i18next";
import { motion, AnimatePresence } from "framer-motion";
import { Button } from "@/components/ui/button";
import { X, Plus, MessageSquare, Loader2, Trash2 } from "lucide-react";
import { SessionSummary } from "@/services/coachService";

interface ConversationListProps {
  sessions: SessionSummary[];
  activeSessionId: string | null;
  onSelectSession: (sessionId: string) => void;
  onNewConversation: () => void;
  onDeleteSession: (sessionId: string) => void;
  loading: boolean;
  open: boolean;
  onClose: () => void;
}

const ConversationList = ({
  sessions,
  activeSessionId,
  onSelectSession,
  onNewConversation,
  onDeleteSession,
  loading,
  open,
  onClose,
}: ConversationListProps) => {
  const { t } = useTranslation();

  return (
    <AnimatePresence>
      {open && (
        <>
          {/* Bakgrundsöverdrag */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/40 z-40"
            onClick={onClose}
          />

          {/* Sidopanel */}
          <motion.div
            initial={{ x: "100%" }}
            animate={{ x: 0 }}
            exit={{ x: "100%" }}
            transition={{ type: "spring", damping: 25, stiffness: 300 }}
            className="fixed right-0 top-0 h-full w-80 bg-card border-l border-border z-50 flex flex-col shadow-xl"
          >
            {/* Header */}
            <div className="flex items-center justify-between p-4 border-b border-border">
              <h2 className="font-semibold text-sm">{t("aiCoach.conversationHistory")}</h2>
              <button
                onClick={onClose}
                className="p-1 rounded-lg hover:bg-muted transition-colors"
                aria-label={t("common.close")}
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Ny konversation */}
            <div className="p-3 border-b border-border">
              <Button
                onClick={() => { onNewConversation(); onClose(); }}
                className="w-full gap-2"
                size="sm"
              >
                <Plus className="w-4 h-4" />
                {t("aiCoach.newConversation")}
              </Button>
            </div>

            {/* Lista */}
            <div className="flex-1 overflow-y-auto p-2">
              {loading ? (
                <div className="flex justify-center py-8">
                  <Loader2 className="w-5 h-5 animate-spin text-muted-foreground" />
                </div>
              ) : sessions.length === 0 ? (
                <p className="text-xs text-muted-foreground text-center py-8">
                  {t("aiCoach.noConversations")}
                </p>
              ) : (
                sessions.map((session) => (
                  <div
                    key={session.sessionId}
                    className={`group flex items-center gap-1 rounded-xl mb-1 transition-all ${
                      session.sessionId === activeSessionId
                        ? "bg-primary/10 border border-primary/20"
                        : "hover:bg-muted"
                    }`}
                  >
                    <button
                      onClick={() => { onSelectSession(session.sessionId); onClose(); }}
                      className="flex-1 text-left p-3 min-w-0"
                    >
                      <div className="flex items-start gap-2">
                        <MessageSquare className="w-3.5 h-3.5 mt-0.5 text-muted-foreground shrink-0" />
                        <div className="overflow-hidden">
                          <p className="text-xs font-medium truncate">
                            {session.preview || t("aiCoach.conversation")}
                          </p>
                          <p className="text-[10px] text-muted-foreground mt-0.5">
                            {new Date(session.lastMessageAt).toLocaleDateString("sv-SE")}
                            {" · "}
                            {session.messageCount} {t("aiCoach.messages")}
                          </p>
                        </div>
                      </div>
                    </button>
                    <button
                      onClick={(e) => { e.stopPropagation(); onDeleteSession(session.sessionId); }}
                      className="p-2 mr-1 rounded-lg opacity-0 group-hover:opacity-100 hover:bg-destructive/10 hover:text-destructive transition-all shrink-0"
                      aria-label={t("common.delete")}
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                ))
              )}
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
};

export default ConversationList;
