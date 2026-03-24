import { useState, useEffect, useCallback } from "react";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { useToast } from "@/components/ui/use-toast";
import {
  Users, Swords, Trophy, UserPlus, Check, X, Trash2,
  Crown, Medal, Star, CircleDot, Target, ChevronRight
} from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { useTranslation } from "react-i18next";
import {
  FriendDto, DuelDto, LeagueEntryDto,
  getFriends, getPendingRequests, sendFriendRequest,
  acceptFriendRequest, declineFriendRequest, removeFriend,
  getDuels, createDuel, acceptDuel, declineDuel, completeDuel,
  getLeaderboard
} from "@/services/socialService";

type Tab = "friends" | "duels" | "league";

const TIER_COLORS: Record<string, string> = {
  DIAMOND: "text-cyan-400",
  GOLD: "text-yellow-400",
  SILVER: "text-slate-300",
  BRONZE: "text-amber-600",
};

const TIER_BG: Record<string, string> = {
  DIAMOND: "bg-cyan-400/10 border-cyan-400/30",
  GOLD: "bg-yellow-400/10 border-yellow-400/30",
  SILVER: "bg-slate-300/10 border-slate-300/30",
  BRONZE: "bg-amber-600/10 border-amber-600/30",
};

const TIER_ICON = {
  DIAMOND: Crown,
  GOLD: Trophy,
  SILVER: Medal,
  BRONZE: Star,
};

const Social = () => {
  const { user } = useAuth();
  const { t } = useTranslation();
  const { toast } = useToast();
  const [activeTab, setActiveTab] = useState<Tab>("friends");

  const userId = user?.email || user?.id || null;

  // --- Friends state ---
  const [friends, setFriends] = useState<FriendDto[]>([]);
  const [pendingRequests, setPendingRequests] = useState<FriendDto[]>([]);
  const [friendsLoading, setFriendsLoading] = useState(false);
  const [addEmail, setAddEmail] = useState("");
  const [addLoading, setAddLoading] = useState(false);

  // --- Duels state ---
  const [duels, setDuels] = useState<DuelDto[]>([]);
  const [duelsLoading, setDuelsLoading] = useState(false);
  const [newDuelEmail, setNewDuelEmail] = useState("");
  const [newDuelChallengeName, setNewDuelChallengeName] = useState("");
  const [newDuelChallengeId, setNewDuelChallengeId] = useState("");
  const [duelFormOpen, setDuelFormOpen] = useState(false);
  const [duelCreateLoading, setDuelCreateLoading] = useState(false);

  // --- League state ---
  const [leaderboard, setLeaderboard] = useState<LeagueEntryDto[]>([]);
  const [leagueLoading, setLeagueLoading] = useState(false);

  // --- Load data ---
  const loadFriends = useCallback(async () => {
    if (!userId) return;
    setFriendsLoading(true);
    try {
      const [f, p] = await Promise.all([getFriends(userId), getPendingRequests(userId)]);
      setFriends(f);
      setPendingRequests(p);
    } catch {
      // silent
    } finally {
      setFriendsLoading(false);
    }
  }, [userId]);

  const loadDuels = useCallback(async () => {
    if (!userId) return;
    setDuelsLoading(true);
    try {
      setDuels(await getDuels(userId));
    } catch {
      // silent
    } finally {
      setDuelsLoading(false);
    }
  }, [userId]);

  const loadLeague = useCallback(async () => {
    if (!userId) return;
    setLeagueLoading(true);
    try {
      setLeaderboard(await getLeaderboard(userId));
    } catch {
      // silent
    } finally {
      setLeagueLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    if (activeTab === "friends") loadFriends();
    if (activeTab === "duels") loadDuels();
    if (activeTab === "league") loadLeague();
  }, [activeTab, loadFriends, loadDuels, loadLeague]);

  // --- Friend actions ---
  const handleSendRequest = async () => {
    if (!userId || !addEmail.trim()) return;
    setAddLoading(true);
    try {
      await sendFriendRequest(userId, addEmail.trim());
      setAddEmail("");
      toast({ title: t("social.requestSent"), description: addEmail });
      loadFriends();
    } catch (e: unknown) {
      toast({
        title: t("common.error"),
        description: e instanceof Error ? e.message : t("social.requestFailed"),
        variant: "destructive",
      });
    } finally {
      setAddLoading(false);
    }
  };

  const handleAcceptRequest = async (friendshipId: number) => {
    if (!userId) return;
    try {
      await acceptFriendRequest(userId, friendshipId);
      loadFriends();
    } catch {
      toast({ title: t("common.error"), variant: "destructive" });
    }
  };

  const handleDeclineRequest = async (friendshipId: number) => {
    if (!userId) return;
    try {
      await declineFriendRequest(userId, friendshipId);
      loadFriends();
    } catch {
      toast({ title: t("common.error"), variant: "destructive" });
    }
  };

  const handleRemoveFriend = async (friendshipId: number) => {
    if (!userId) return;
    try {
      await removeFriend(userId, friendshipId);
      loadFriends();
    } catch {
      toast({ title: t("common.error"), variant: "destructive" });
    }
  };

  // --- Duel actions ---
  const handleCreateDuel = async () => {
    if (!userId || !newDuelEmail.trim() || !newDuelChallengeName.trim()) return;
    setDuelCreateLoading(true);
    try {
      const challengeIdNum = parseInt(newDuelChallengeId) || 1;
      await createDuel(userId, newDuelEmail.trim(), challengeIdNum, newDuelChallengeName.trim());
      setNewDuelEmail("");
      setNewDuelChallengeName("");
      setNewDuelChallengeId("");
      setDuelFormOpen(false);
      toast({ title: t("social.duelSent") });
      loadDuels();
    } catch (e: unknown) {
      toast({
        title: t("common.error"),
        description: e instanceof Error ? e.message : t("social.duelFailed"),
        variant: "destructive",
      });
    } finally {
      setDuelCreateLoading(false);
    }
  };

  const handleDuelAction = async (
    duelId: number,
    action: "accept" | "decline" | "complete"
  ) => {
    if (!userId) return;
    try {
      if (action === "accept") await acceptDuel(userId, duelId);
      else if (action === "decline") await declineDuel(userId, duelId);
      else await completeDuel(userId, duelId);
      loadDuels();
    } catch (e: unknown) {
      toast({
        title: t("common.error"),
        description: e instanceof Error ? e.message : "",
        variant: "destructive",
      });
    }
  };

  const tabs: { id: Tab; label: string; icon: typeof Users; count?: number }[] = [
    { id: "friends", label: t("social.friends"), icon: Users, count: pendingRequests.length || undefined },
    { id: "duels", label: t("social.duels"), icon: Swords },
    { id: "league", label: t("social.league"), icon: Trophy },
  ];

  return (
    <div className="min-h-full bg-gradient-to-br from-background via-background to-primary/5 p-4 pt-4">
      <div className="max-w-4xl mx-auto space-y-5">
        {/* Header */}
        <div className="pt-2">
          <h1 className="text-3xl font-bold text-foreground mb-1">{t("social.title")}</h1>
          <p className="text-muted-foreground text-sm">{t("social.subtitle")}</p>
        </div>

        {/* Tab bar */}
        <div className="flex gap-2 bg-muted/40 p-1 rounded-2xl">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 ${
                  activeTab === tab.id
                    ? "bg-card text-foreground shadow-sm"
                    : "text-muted-foreground hover:text-foreground"
                }`}
              >
                <Icon className="w-4 h-4" />
                {tab.label}
                {tab.count ? (
                  <span className="w-5 h-5 rounded-full bg-primary text-primary-foreground text-[10px] font-bold flex items-center justify-center">
                    {tab.count}
                  </span>
                ) : null}
              </button>
            );
          })}
        </div>

        {/* ===== VÄNNER ===== */}
        {activeTab === "friends" && (
          <div className="space-y-4">
            {/* Lägg till vän */}
            <Card className="p-4">
              <h3 className="font-semibold text-sm mb-3 flex items-center gap-2">
                <UserPlus className="w-4 h-4 text-primary" />
                {t("social.addFriend")}
              </h3>
              <div className="flex gap-2">
                <Input
                  placeholder={t("social.friendEmailPlaceholder")}
                  value={addEmail}
                  onChange={(e) => setAddEmail(e.target.value)}
                  onKeyDown={(e) => e.key === "Enter" && handleSendRequest()}
                  className="flex-1"
                />
                <Button onClick={handleSendRequest} disabled={addLoading || !addEmail.trim()} size="sm">
                  {addLoading ? "..." : t("social.send")}
                </Button>
              </div>
            </Card>

            {/* Inkommande förfrågningar */}
            {pendingRequests.length > 0 && (
              <div className="space-y-2">
                <h3 className="text-sm font-semibold text-muted-foreground px-1">
                  {t("social.incomingRequests")} ({pendingRequests.length})
                </h3>
                {pendingRequests.map((req) => (
                  <Card key={req.friendshipId} className="p-4 border-primary/20 bg-primary/5">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="font-medium text-sm">{req.displayName}</p>
                        <p className="text-xs text-muted-foreground">{req.totalPoints} {t("common.points")}</p>
                      </div>
                      <div className="flex gap-2">
                        <Button
                          size="sm"
                          className="h-8 w-8 p-0 rounded-full"
                          onClick={() => handleAcceptRequest(req.friendshipId)}
                        >
                          <Check className="w-4 h-4" />
                        </Button>
                        <Button
                          size="sm"
                          variant="outline"
                          className="h-8 w-8 p-0 rounded-full"
                          onClick={() => handleDeclineRequest(req.friendshipId)}
                        >
                          <X className="w-4 h-4" />
                        </Button>
                      </div>
                    </div>
                  </Card>
                ))}
              </div>
            )}

            {/* Väner */}
            <div className="space-y-2">
              <h3 className="text-sm font-semibold text-muted-foreground px-1">
                {t("social.myFriends")} ({friends.length})
              </h3>
              {friendsLoading ? (
                <div className="space-y-2">
                  {[1, 2, 3].map((i) => (
                    <div key={i} className="h-16 rounded-xl bg-muted/40 animate-pulse" />
                  ))}
                </div>
              ) : friends.length === 0 ? (
                <Card className="p-8 text-center">
                  <Users className="w-10 h-10 mx-auto mb-3 text-muted-foreground/50" />
                  <p className="text-muted-foreground text-sm">{t("social.noFriends")}</p>
                </Card>
              ) : (
                friends.map((friend) => (
                  <Card key={friend.friendshipId} className="p-4">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center font-bold text-primary text-sm">
                          {friend.displayName.charAt(0).toUpperCase()}
                        </div>
                        <div>
                          <p className="font-medium text-sm">{friend.displayName}</p>
                          <p className="text-xs text-muted-foreground">{friend.totalPoints} {t("common.points")}</p>
                        </div>
                      </div>
                      <button
                        onClick={() => handleRemoveFriend(friend.friendshipId)}
                        className="p-2 text-muted-foreground hover:text-red-500 transition-colors"
                        title={t("social.removeFriend")}
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </Card>
                ))
              )}
            </div>
          </div>
        )}

        {/* ===== DUELLER ===== */}
        {activeTab === "duels" && (
          <div className="space-y-4">
            {/* Skapa ny duell */}
            <Card className="p-4">
              <button
                onClick={() => setDuelFormOpen(!duelFormOpen)}
                className="w-full flex items-center justify-between text-left"
              >
                <span className="font-semibold text-sm flex items-center gap-2">
                  <Swords className="w-4 h-4 text-primary" />
                  {t("social.createDuel")}
                </span>
                <ChevronRight className={`w-4 h-4 text-muted-foreground transition-transform ${duelFormOpen ? "rotate-90" : ""}`} />
              </button>
              {duelFormOpen && (
                <div className="mt-4 space-y-3 animate-in fade-in duration-200">
                  <Input
                    placeholder={t("social.duelOpponentEmail")}
                    value={newDuelEmail}
                    onChange={(e) => setNewDuelEmail(e.target.value)}
                  />
                  <Input
                    placeholder={t("social.duelChallengeName")}
                    value={newDuelChallengeName}
                    onChange={(e) => setNewDuelChallengeName(e.target.value)}
                  />
                  <Button
                    className="w-full"
                    onClick={handleCreateDuel}
                    disabled={duelCreateLoading || !newDuelEmail.trim() || !newDuelChallengeName.trim()}
                  >
                    {duelCreateLoading ? "..." : t("social.sendDuel")}
                  </Button>
                </div>
              )}
            </Card>

            {/* Duell-lista */}
            {duelsLoading ? (
              <div className="space-y-2">
                {[1, 2].map((i) => (
                  <div key={i} className="h-24 rounded-xl bg-muted/40 animate-pulse" />
                ))}
              </div>
            ) : duels.length === 0 ? (
              <Card className="p-8 text-center">
                <Swords className="w-10 h-10 mx-auto mb-3 text-muted-foreground/50" />
                <p className="text-muted-foreground text-sm">{t("social.noDuels")}</p>
              </Card>
            ) : (
              duels.map((duel) => <DuelCard key={duel.id} duel={duel} onAction={handleDuelAction} t={t} />)
            )}
          </div>
        )}

        {/* ===== LIGA ===== */}
        {activeTab === "league" && (
          <div className="space-y-3">
            <p className="text-xs text-muted-foreground px-1">{t("social.leagueDesc")}</p>

            {leagueLoading ? (
              <div className="space-y-2">
                {[1, 2, 3, 4, 5].map((i) => (
                  <div key={i} className="h-14 rounded-xl bg-muted/40 animate-pulse" />
                ))}
              </div>
            ) : leaderboard.length === 0 ? (
              <Card className="p-8 text-center">
                <Trophy className="w-10 h-10 mx-auto mb-3 text-muted-foreground/50" />
                <p className="text-muted-foreground text-sm">{t("social.leagueEmpty")}</p>
              </Card>
            ) : (
              leaderboard.map((entry) => <LeagueRow key={entry.rank} entry={entry} t={t} />)
            )}
          </div>
        )}
      </div>
    </div>
  );
};

// --- Duell-kort ---
const DuelCard = ({
  duel,
  onAction,
  t,
}: {
  duel: DuelDto;
  onAction: (id: number, action: "accept" | "decline" | "complete") => void;
  t: (key: string) => string;
}) => {
  const statusColors: Record<string, string> = {
    PENDING: "bg-amber-500/10 text-amber-600 border-amber-500/20",
    ACTIVE: "bg-primary/10 text-primary border-primary/20",
    COMPLETED: "bg-green-500/10 text-green-600 border-green-500/20",
    DECLINED: "bg-red-500/10 text-red-500 border-red-500/20",
    EXPIRED: "bg-muted text-muted-foreground border-border",
  };

  const statusLabel: Record<string, string> = {
    PENDING: t("social.duelPending"),
    ACTIVE: t("social.duelActive"),
    COMPLETED: t("social.duelCompleted"),
    DECLINED: t("social.duelDeclined"),
    EXPIRED: t("social.duelExpired"),
  };

  return (
    <Card className="p-4 space-y-3">
      <div className="flex items-start justify-between">
        <div className="space-y-0.5">
          <p className="text-xs text-muted-foreground">
            {duel.challengerName} vs {duel.challengedName}
          </p>
          <div className="flex items-center gap-2">
            <Target className="w-4 h-4 text-primary" />
            <p className="font-medium text-sm">{duel.challengeName}</p>
          </div>
        </div>
        <Badge className={`text-[10px] border ${statusColors[duel.status] || ""}`}>
          {statusLabel[duel.status] || duel.status}
        </Badge>
      </div>

      {duel.status === "COMPLETED" && duel.winnerName && (
        <div className="flex items-center gap-1.5 text-xs text-yellow-600 font-medium">
          <Crown className="w-3.5 h-3.5" />
          {duel.winnerName} {t("social.wins")}
        </div>
      )}

      {duel.status === "ACTIVE" && (
        <div className="flex items-center gap-3 text-xs text-muted-foreground">
          <span className={`flex items-center gap-1 ${duel.iCompleted ? "text-green-600" : ""}`}>
            <CircleDot className="w-3.5 h-3.5" />
            {duel.iAmChallenger ? duel.challengerName : duel.challengedName}
            {duel.iCompleted ? ` ✓` : ""}
          </span>
          <span className={`flex items-center gap-1 ${duel.opponentCompleted ? "text-green-600" : ""}`}>
            <CircleDot className="w-3.5 h-3.5" />
            {duel.iAmChallenger ? duel.challengedName : duel.challengerName}
            {duel.opponentCompleted ? ` ✓` : ""}
          </span>
        </div>
      )}

      {/* Actions */}
      {duel.status === "PENDING" && !duel.iAmChallenger && (
        <div className="flex gap-2">
          <Button size="sm" className="flex-1 h-8" onClick={() => onAction(duel.id, "accept")}>
            <Check className="w-3.5 h-3.5 mr-1" /> {t("social.accept")}
          </Button>
          <Button size="sm" variant="outline" className="flex-1 h-8" onClick={() => onAction(duel.id, "decline")}>
            <X className="w-3.5 h-3.5 mr-1" /> {t("social.decline")}
          </Button>
        </div>
      )}

      {duel.status === "ACTIVE" && !duel.iCompleted && (
        <Button size="sm" className="w-full h-8" onClick={() => onAction(duel.id, "complete")}>
          {t("social.markComplete")}
        </Button>
      )}
    </Card>
  );
};

// --- Liga-rad ---
const LeagueRow = ({ entry, t }: { entry: LeagueEntryDto; t: (key: string) => string }) => {
  const TierIcon = TIER_ICON[entry.tier] || Star;

  return (
    <div
      className={`flex items-center gap-3 p-3 rounded-xl border transition-all ${
        entry.isMe
          ? "bg-primary/10 border-primary/30 ring-1 ring-primary/20"
          : "bg-card border-border/50"
      }`}
    >
      {/* Rank */}
      <div
        className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0 ${
          entry.rank === 1 ? "bg-yellow-400 text-yellow-900" :
          entry.rank === 2 ? "bg-slate-300 text-slate-800" :
          entry.rank === 3 ? "bg-amber-600 text-white" :
          "bg-muted text-muted-foreground"
        }`}
      >
        {entry.rank}
      </div>

      {/* Tier icon */}
      <TierIcon className={`w-5 h-5 flex-shrink-0 ${TIER_COLORS[entry.tier] || ""}`} />

      {/* Name */}
      <div className="flex-1 min-w-0">
        <p className={`text-sm font-medium truncate ${entry.isMe ? "text-primary" : "text-foreground"}`}>
          {entry.displayName}
          {entry.isMe && <span className="ml-1.5 text-xs text-primary/70">{t("social.you")}</span>}
        </p>
      </div>

      {/* Points + tier badge */}
      <div className="flex items-center gap-2 flex-shrink-0">
        <span className="text-sm font-semibold text-foreground">{entry.totalPoints}</span>
        <Badge className={`text-[10px] border ${TIER_BG[entry.tier] || ""} ${TIER_COLORS[entry.tier] || ""}`}>
          {t(`social.tier${entry.tier}`)}
        </Badge>
      </div>
    </div>
  );
};

export default Social;
