import { useState, useEffect, useRef, useCallback } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { useToast } from "@/hooks/use-toast";
import { Bell, User, Palette, LogOut, Settings as SettingsIcon, Bot, Trash2, AlertTriangle, Download } from "lucide-react";
import { SettingsPageSkeleton } from "@/components/skeletons/SettingsSkeleton";
import { useAuth } from "@/context/AuthContext";
import { API_ENDPOINTS, fetchWithCredentials } from "@/config/api";
import { useTranslation, Trans } from "react-i18next";
import CoachPersonaSelector, { type CoachPersonality } from "@/components/CoachPersonaSelector";

type FeedItemType = "CHALLENGE_COMPLETED" | "STREAK_MILESTONE" | "LEVEL_UP" | "DUEL_WON";

interface UserSettings {
  name: string;
  phone: string;
  email: string;
  notificationsEnabled: boolean;
  aiNotificationsEnabled: boolean;
  darkModeEnabled: boolean;
  language: "sv" | "en";
  coachPersonality: CoachPersonality;
  feedSharingEnabled: boolean;
  feedSharedTypes: FeedItemType[];
}

const Settings = () => {
  const { toast } = useToast();
  const { user } = useAuth();
  const { t, i18n } = useTranslation();
  const [settings, setSettings] = useState<UserSettings | null>(null);
  const [loading, setLoading] = useState(true);
  const hasFetched = useRef(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [exportLoading, setExportLoading] = useState(false);
  const [deleteConfirmText, setDeleteConfirmText] = useState("");
  const [deleting, setDeleting] = useState(false);

  // Applicera tema
  const applyTheme = useCallback((isDark: boolean) => {
    const root = document.documentElement;
    if (isDark) {
      root.classList.add("dark");
      localStorage.setItem("theme", "dark");
    } else {
      root.classList.remove("dark");
      localStorage.setItem("theme", "light");
    }
  }, []);

  // Hantera mörkt läge
  const handleDarkModeChange = (checked: boolean) => {
    if (!settings) return;
    setSettings({ ...settings, darkModeEnabled: checked });
    applyTheme(checked);
    toast({
      title: checked ? t('settings.darkModeEnabled') : t('settings.lightModeEnabled'),
      description: t('settings.themeChanged', { mode: checked ? t('settings.themeDark') : t('settings.themeLight') }),
    });
  };

  // Ladda settings från backend — körs en gång per userId
  const userId = user?.email || user?.id || null;

  useEffect(() => {
    if (!userId || hasFetched.current) return;
    hasFetched.current = true;

    const loadSettings = async () => {
      try {
        const res = await fetch(API_ENDPOINTS.SETTINGS.GET_USER_SETTINGS(userId), {
          method: "GET",
          credentials: "include",
        });

        if (res.ok) {
          const data = await res.json();
          const darkMode = data.darkModeEnabled ?? false;
          const lang = data.language === "en" ? "en" : "sv";
          setSettings({
            name: data.name || user?.name || "",
            email: data.email || user?.email || "",
            phone: data.phone || "",
            notificationsEnabled: data.notificationsEnabled ?? true,
            aiNotificationsEnabled: data.aiNotificationsEnabled ?? false,
            darkModeEnabled: darkMode,
            language: lang,
            coachPersonality: data.coachPersonality ?? "SUPPORTIVE",
            feedSharingEnabled: data.feedSharingEnabled ?? false,
            feedSharedTypes: data.feedSharedTypes ?? [],
          });
          applyTheme(darkMode);
          // Synka i18n-språk med backend-värdet (single source of truth)
          if (i18n.language !== lang) {
            i18n.changeLanguage(lang);
            localStorage.setItem("i18nLanguage", lang);
          }
        } else {
          // Om inställningar inte finns, skapa defaults
          const isDark = localStorage.getItem("theme") === "dark";
          setSettings({
            name: user?.name || "",
            email: user?.email || "",
            phone: "",
            notificationsEnabled: true,
            aiNotificationsEnabled: false,
            darkModeEnabled: isDark,
            language: "sv" as const,
            coachPersonality: "SUPPORTIVE",
            feedSharingEnabled: false,
            feedSharedTypes: [],
          });
        }
      } catch {
        toast({
          title: t('common.error'),
          description: t('settings.loadError'),
          variant: "destructive",
        });
      } finally {
        setLoading(false);
      }
    };

    loadSettings();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId]);

  // Spara inställningar
  const handleSave = async () => {
    if ((!user?.email && !user?.id) || !settings) return;
    const userId = user.email || user.id;

    try {
      await fetchWithCredentials(API_ENDPOINTS.SETTINGS.UPDATE_USER_SETTINGS(userId), {
        method: "POST",
        body: JSON.stringify({
          userId: user.id,
          name: settings.name,
          email: settings.email,
          phone: settings.phone,
          notificationsEnabled: settings.notificationsEnabled,
          aiNotificationsEnabled: settings.aiNotificationsEnabled,
          darkModeEnabled: settings.darkModeEnabled,
          language: settings.language,
          coachPersonality: settings.coachPersonality,
          feedSharingEnabled: settings.feedSharingEnabled,
          feedSharedTypes: settings.feedSharedTypes,
        }),
      });

      // Applicera språkbyte efter lyckad sparning
      if (i18n.language !== settings.language) {
        i18n.changeLanguage(settings.language);
        localStorage.setItem("i18nLanguage", settings.language);
      }

      toast({
        title: t('settings.saved'),
        description: t('settings.savedDesc'),
      });
    } catch {
      toast({
        title: t('settings.saveFailed'),
        description: t('settings.saveFailedDesc'),
        variant: "destructive",
      });
    }
  };

  // Logga ut
  const handleLogout = async () => {
    try {
      await fetchWithCredentials(API_ENDPOINTS.AUTH.LOGOUT, { method: "POST" });
      localStorage.removeItem("userPoints");
      localStorage.removeItem("userLevel");
      localStorage.removeItem("theme");
      toast({
        title: t('auth.loggedOut'),
        description: t('auth.loggedOutDesc'),
      });
      window.location.href = "/";
    } catch {
      toast({
        title: t('auth.logoutError'),
        description: t('auth.logoutErrorDesc'),
        variant: "destructive",
      });
    }
  };

  // Radera konto (GDPR artikel 17)
  const handleDeleteAccount = async () => {
    if (!user || deleting) return;
    setDeleting(true);
    try {
      await fetchWithCredentials(API_ENDPOINTS.AUTH.DELETE_ACCOUNT, { method: "DELETE" });
      localStorage.clear();
      window.location.href = "/login";
    } catch {
      toast({
        title: t('settings.deleteError'),
        description: t('settings.deleteErrorDesc'),
        variant: "destructive",
      });
    } finally {
      setDeleting(false);
    }
  };

  // Visa loading
  if (loading) {
    return <SettingsPageSkeleton />;
  }

  if (!settings) {
    return (
      <div className="flex items-center justify-center min-h-full bg-gradient-to-br from-background via-background to-primary/5">
        <p className="text-red-500">{t('settings.loadErrorGeneric')}</p>
      </div>
    );
  }

  return (
    <div className="min-h-full bg-gradient-to-br from-background via-background to-primary/5 relative pb-28">
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

      <div className="relative z-10 max-w-2xl mx-auto p-4 pt-4 space-y-4 animate-in fade-in slide-in-from-bottom-4 duration-700">
        {/* Page Header — compact */}
        <div className="flex items-center gap-3 pb-1">
          <div className="inline-flex items-center justify-center w-11 h-11 rounded-xl bg-gradient-to-br from-primary to-primary/60 shadow-lg">
            <SettingsIcon className="w-5 h-5 text-primary-foreground" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-foreground">
              {t('settings.title')}
            </h1>
            <p className="text-xs text-muted-foreground">
              {t('settings.subtitle')}
            </p>
          </div>
        </div>

        {/* Profilinställningar */}
        <div className="bg-card/80 backdrop-blur-xl border border-border/50 rounded-2xl shadow-lg p-5 space-y-4 text-left">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0">
              <User className="w-4 h-4 text-primary" />
            </div>
            <h2 className="text-base font-semibold text-foreground">{t('settings.profile')}</h2>
          </div>

          <div className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="name" className="text-sm font-medium text-foreground/80">{t('settings.name')}</Label>
              <Input
                id="name"
                value={settings.name}
                onChange={(e) =>
                  setSettings({ ...settings, name: e.target.value })
                }
                className="rounded-xl border-border/50 bg-background/50 focus:bg-background transition-colors"
              />
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="phone" className="text-sm font-medium text-foreground/80">{t('settings.phone')}</Label>
              <Input
                id="phone"
                type="tel"
                value={settings.phone}
                onChange={(e) =>
                  setSettings({ ...settings, phone: e.target.value })
                }
                placeholder={t('settings.phonePlaceholder')}
                className="rounded-xl border-border/50 bg-background/50 focus:bg-background transition-colors"
              />
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="email" className="text-sm font-medium text-foreground/80">{t('settings.emailLabel')}</Label>
              <Input
                id="email"
                type="email"
                value={settings.email}
                disabled
                className="rounded-xl border-border/50 bg-muted/50 text-muted-foreground cursor-not-allowed"
              />
              <p className="text-xs text-muted-foreground/70">
                {t('settings.emailLocked')}
              </p>
            </div>
          </div>
        </div>

        {/* Notifieringar */}
        <div className="bg-card/80 backdrop-blur-xl border border-border/50 rounded-2xl shadow-lg p-5 space-y-4 text-left">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0">
              <Bell className="w-4 h-4 text-primary" />
            </div>
            <h2 className="text-base font-semibold text-foreground">{t('settings.notifications')}</h2>
          </div>

          <div className="space-y-1">
            <div className="flex items-center justify-between py-3">
              <div>
                <p className="text-sm font-medium text-foreground">{t('settings.pushNotifications')}</p>
                <p className="text-xs text-muted-foreground mt-0.5">
                  {t('settings.pushDesc')}
                </p>
              </div>
              <Switch
                checked={settings.notificationsEnabled}
                onCheckedChange={(checked) =>
                  setSettings({ ...settings, notificationsEnabled: checked })
                }
              />
            </div>

            <div className="border-t border-border/30" />

            <div className="flex items-center justify-between py-3">
              <div>
                <p className="text-sm font-medium text-foreground">{t('settings.emailNotifications')}</p>
                <p className="text-xs text-muted-foreground mt-0.5">
                  {t('settings.emailNotificationsDesc')}
                </p>
              </div>
              <Switch
                checked={settings.aiNotificationsEnabled}
                onCheckedChange={(checked) =>
                  setSettings({ ...settings, aiNotificationsEnabled: checked })
                }
              />
            </div>
          </div>
        </div>

        {/* Utseende */}
        <div className="bg-card/80 backdrop-blur-xl border border-border/50 rounded-2xl shadow-lg p-5 space-y-4 text-left">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0">
              <Palette className="w-4 h-4 text-primary" />
            </div>
            <h2 className="text-base font-semibold text-foreground">{t('settings.appearance')}</h2>
          </div>

          <div className="space-y-1">
            <div className="flex items-center justify-between py-3">
              <div>
                <p className="text-sm font-medium text-foreground">{t('settings.darkMode')}</p>
                <p className="text-xs text-muted-foreground mt-0.5">
                  {t('settings.darkModeDesc')}
                </p>
              </div>
              <Switch
                checked={settings.darkModeEnabled}
                onCheckedChange={handleDarkModeChange}
              />
            </div>

            <div className="border-t border-border/30" />

            <div className="py-3 space-y-2">
              <Label htmlFor="language" className="text-sm font-medium text-foreground">{t('settings.language')}</Label>
              <select
                id="language"
                value={settings.language}
                onChange={(e) => {
                  const newLang = e.target.value as "sv" | "en";
                  setSettings({
                    ...settings,
                    language: newLang,
                  });
                }}
                className="w-full px-4 py-2.5 rounded-xl border border-border/50 bg-background/50 text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary/50 transition-all cursor-pointer"
              >
                <option value="sv">{t('settings.languageSv')}</option>
                <option value="en">{t('settings.languageEn')}</option>
              </select>
            </div>
          </div>
        </div>

        {/* Coach-personlighet */}
        <div className="bg-card/80 backdrop-blur-xl border border-border/50 rounded-2xl shadow-lg p-5 space-y-3 text-left">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0">
              <Bot className="w-4 h-4 text-primary" />
            </div>
            <div>
              <h2 className="text-base font-semibold text-foreground">{t('settings.coachTitle')}</h2>
              <p className="text-xs text-muted-foreground">{t('settings.coachSubtitle')}</p>
            </div>
          </div>
          <CoachPersonaSelector
            value={settings.coachPersonality}
            onChange={(p) => setSettings({ ...settings, coachPersonality: p })}
          />
        </div>

        {/* Sparaknapp & Logout */}
        <div className="space-y-3 pt-1">
          <Button
            onClick={handleSave}
            className="h-auto w-full py-3.5 rounded-2xl bg-gradient-to-r from-primary to-primary/80 text-primary-foreground font-semibold text-sm shadow-lg hover:shadow-xl hover:scale-[1.01] active:scale-[0.99] transition-all duration-200"
          >
            {t('common.save')}
          </Button>
          <Button
            variant="outline"
            onClick={handleLogout}
            className="h-auto w-full flex items-center justify-center gap-2 py-3.5 rounded-2xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 font-semibold text-sm hover:bg-red-500/20 hover:border-red-500/30 active:scale-[0.99] transition-all duration-200"
          >
            <LogOut className="w-4 h-4" />
            {t('auth.logout')}
          </Button>
        </div>

        {/* Feed-delningsinställningar */}
        <div className="bg-card border border-border/50 rounded-2xl p-5 space-y-4">
          <div className="flex items-start gap-3">
            <div className="w-9 h-9 rounded-xl bg-primary/10 flex items-center justify-center flex-shrink-0 mt-0.5">
              <Bell className="w-4 h-4 text-primary" />
            </div>
            <div className="flex-1">
              <h2 className="text-base font-semibold text-foreground">{t("feed.sharingSettings")}</h2>
              <p className="text-xs text-muted-foreground mt-0.5">{t("feed.enableSharingDesc")}</p>
            </div>
          </div>

          {/* Master-toggle */}
          <div className="flex items-center justify-between">
            <Label htmlFor="feed-sharing" className="text-sm">{t("feed.enableSharing")}</Label>
            <Switch
              id="feed-sharing"
              checked={settings.feedSharingEnabled}
              onCheckedChange={(checked) =>
                setSettings({ ...settings, feedSharingEnabled: checked })
              }
            />
          </div>

          {/* Per-typ val (visas bara när master-toggle är på) */}
          {settings.feedSharingEnabled && (
            <div className="space-y-2 pl-1 border-l-2 border-primary/20 ml-1">
              {(["CHALLENGE_COMPLETED", "STREAK_MILESTONE", "LEVEL_UP", "DUEL_WON"] as FeedItemType[]).map((type) => {
                const checked = settings.feedSharedTypes.includes(type);
                return (
                  <div key={type} className="flex items-center gap-3">
                    <input
                      type="checkbox"
                      id={`feed-type-${type}`}
                      checked={checked}
                      onChange={(e) => {
                        const next = e.target.checked
                          ? [...settings.feedSharedTypes, type]
                          : settings.feedSharedTypes.filter((t) => t !== type);
                        setSettings({ ...settings, feedSharedTypes: next });
                      }}
                      className="w-4 h-4 accent-primary rounded"
                    />
                    <Label htmlFor={`feed-type-${type}`} className="text-sm font-normal cursor-pointer">
                      {t(`feed.shareTypes.${type}`)}
                    </Label>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* GDPR — Exportera min data */}
        <div className="bg-card border border-border/50 rounded-2xl p-5 space-y-3 text-left">
          <div className="flex items-start gap-3">
            <div className="w-9 h-9 rounded-xl bg-primary/10 flex items-center justify-center flex-shrink-0 mt-0.5">
              <Download className="w-4 h-4 text-primary" />
            </div>
            <div>
              <h2 className="text-base font-semibold text-foreground">{t('settings.exportData', { defaultValue: 'Exportera mina uppgifter' })}</h2>
              <p className="text-xs text-muted-foreground mt-0.5">
                {t('settings.exportDataDesc', { defaultValue: 'Ladda ner all din data som en JSON-fil (GDPR Artikel 20).' })}
              </p>
            </div>
          </div>
          <Button
            variant="outline"
            size="sm"
            disabled={exportLoading}
            onClick={async () => {
              if (!user?.email && !user?.id) return;
              const userId = user.email || user.id;
              setExportLoading(true);
              try {
                const res = await fetch(`/api/settings/${encodeURIComponent(userId)}/export`, { credentials: "include" });
                const blob = await res.blob();
                const url = URL.createObjectURL(blob);
                const a = document.createElement("a");
                a.href = url;
                a.download = "nextstep-data.json";
                a.click();
                URL.revokeObjectURL(url);
              } catch {
                toast({ title: t('settings.exportError', { defaultValue: 'Kunde inte exportera data' }), variant: "destructive" });
              } finally {
                setExportLoading(false);
              }
            }}
            className="gap-2"
          >
            <Download className="w-3.5 h-3.5" />
            {exportLoading
              ? t('common.loading')
              : t('settings.exportDataButton', { defaultValue: 'Ladda ner min data' })}
          </Button>
        </div>

        {/* Radera konto */}
        <div className="bg-red-50/50 dark:bg-red-950/20 border border-red-200/50 dark:border-red-800/30 rounded-2xl p-5 space-y-3 text-left">
          <div className="flex items-start gap-3">
            <div className="w-9 h-9 rounded-xl bg-red-100 dark:bg-red-900/40 flex items-center justify-center flex-shrink-0 mt-0.5">
              <Trash2 className="w-4 h-4 text-red-600 dark:text-red-400" />
            </div>
            <div>
              <h2 className="text-base font-semibold text-red-700 dark:text-red-400">{t('settings.deleteAccount')}</h2>
              <p className="text-xs text-red-600/70 dark:text-red-400/70 mt-0.5">
                {t('settings.deleteAccountDesc')}
              </p>
            </div>
          </div>

          {!showDeleteConfirm ? (
            <Button
              variant="outline"
              onClick={() => setShowDeleteConfirm(true)}
              className="h-auto w-full py-3 rounded-xl border-red-300/60 dark:border-red-700/40 text-red-600 dark:text-red-400 text-sm font-medium hover:bg-red-100/60 dark:hover:bg-red-900/30 transition-all duration-200"
            >
              {t('settings.deleteAccountButton')}
            </Button>
          ) : (
            <div className="space-y-3 animate-in fade-in duration-200">
              <div className="flex items-center gap-2 text-xs text-red-700 dark:text-red-400 bg-red-100/60 dark:bg-red-900/30 rounded-xl p-3">
                <AlertTriangle className="w-4 h-4 flex-shrink-0" />
                <Trans i18nKey="settings.deleteConfirmPrompt" components={{ strong: <strong /> }} />
              </div>
              <Input
                type="text"
                autoFocus
                aria-label={t('settings.deleteConfirmPlaceholder')}
                value={deleteConfirmText}
                onChange={(e) => setDeleteConfirmText(e.target.value)}
                placeholder={t('settings.deleteConfirmPlaceholder')}
                className="rounded-xl border-red-300/60 dark:border-red-700/40 bg-background/50 focus:ring-red-400/40"
              />
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  onClick={() => { setShowDeleteConfirm(false); setDeleteConfirmText(""); }}
                  className="h-auto flex-1 py-2.5 rounded-xl text-sm font-medium"
                >
                  {t('settings.deleteCancelButton')}
                </Button>
                <Button
                  onClick={handleDeleteAccount}
                  disabled={deleteConfirmText.toLowerCase() !== t('settings.deleteConfirmPlaceholder').toLowerCase() || deleting}
                  className="h-auto flex-1 py-2.5 rounded-xl bg-red-600 text-white text-sm font-semibold hover:bg-red-700 active:scale-[0.99] transition-all duration-200"
                >
                  {deleting ? t('settings.deleting') : t('settings.deleteConfirmButton')}
                </Button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Settings;