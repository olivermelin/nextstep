import { useState, useEffect, useRef, useCallback } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { useToast } from "@/components/ui/use-toast";
import { Bell, User, Palette, LogOut, Settings as SettingsIcon } from "lucide-react";
import { SettingsPageSkeleton } from "@/components/skeletons/SettingsSkeleton";
import { useAuth } from "@/context/AuthContext";
import { API_ENDPOINTS } from "@/config/api";
import { useTranslation } from "react-i18next";

interface UserSettings {
  name: string;
  phone: string;
  email: string;
  notificationsEnabled: boolean;
  aiNotificationsEnabled: boolean;
  darkModeEnabled: boolean;
  language: "sv" | "en";
}

const Settings = () => {
  const { toast } = useToast();
  const { user } = useAuth();
  const { t, i18n } = useTranslation();
  const [settings, setSettings] = useState<UserSettings | null>(null);
  const [loading, setLoading] = useState(true);
  const hasFetched = useRef(false);

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
      const res = await fetch(API_ENDPOINTS.SETTINGS.UPDATE_USER_SETTINGS(userId), {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          userId: user.id,
          name: settings.name,
          email: settings.email,
          phone: settings.phone,
          notificationsEnabled: settings.notificationsEnabled,
          aiNotificationsEnabled: settings.aiNotificationsEnabled,
          darkModeEnabled: settings.darkModeEnabled,
          language: settings.language,
        }),
      });

      if (!res.ok) {
        throw new Error(`HTTP error! status: ${res.status}`);
      }

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
      const res = await fetch(API_ENDPOINTS.AUTH.LOGOUT, {
        method: "POST",
        credentials: "include",
      });

      if (res.ok) {
        localStorage.removeItem("userPoints");
        localStorage.removeItem("userLevel");
        localStorage.removeItem("theme");
        toast({
          title: t('auth.loggedOut'),
          description: t('auth.loggedOutDesc'),
        });
        window.location.href = "/";
      } else {
        throw new Error("Logout failed");
      }
    } catch {
      toast({
        title: t('auth.logoutError'),
        description: t('auth.logoutErrorDesc'),
        variant: "destructive",
      });
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
    <div className="min-h-full bg-gradient-to-br from-background via-background to-primary/5 relative overflow-hidden">
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

      <div className="relative z-10 max-w-2xl mx-auto p-4 pt-6 space-y-5 animate-in fade-in slide-in-from-bottom-4 duration-700">
        {/* Page Header */}
        <div className="text-center space-y-2 pb-2">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-gradient-to-br from-primary to-primary/60 shadow-lg mb-1">
            <SettingsIcon className="w-7 h-7 text-primary-foreground" />
          </div>
          <h1 className="text-3xl font-bold bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent">
            {t('settings.title')}
          </h1>
          <p className="text-sm text-muted-foreground">
            {t('settings.subtitle')}
          </p>
        </div>

        {/* Profilinställningar */}
        <div className="bg-card/80 backdrop-blur-xl border border-border/50 rounded-2xl shadow-lg p-6 space-y-5 text-left">
          <div className="flex items-center gap-3 mb-1">
            <div className="w-9 h-9 rounded-xl bg-primary/10 flex items-center justify-center flex-shrink-0">
              <User className="w-4.5 h-4.5 text-primary" />
            </div>
            <h2 className="text-lg font-semibold text-foreground">{t('settings.profile')}</h2>
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
        <div className="bg-card/80 backdrop-blur-xl border border-border/50 rounded-2xl shadow-lg p-6 space-y-5 text-left">
          <div className="flex items-center gap-3 mb-1">
            <div className="w-9 h-9 rounded-xl bg-primary/10 flex items-center justify-center flex-shrink-0">
              <Bell className="w-4.5 h-4.5 text-primary" />
            </div>
            <h2 className="text-lg font-semibold text-foreground">{t('settings.notifications')}</h2>
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
        <div className="bg-card/80 backdrop-blur-xl border border-border/50 rounded-2xl shadow-lg p-6 space-y-5 text-left">
          <div className="flex items-center gap-3 mb-1">
            <div className="w-9 h-9 rounded-xl bg-primary/10 flex items-center justify-center flex-shrink-0">
              <Palette className="w-4.5 h-4.5 text-primary" />
            </div>
            <h2 className="text-lg font-semibold text-foreground">{t('settings.appearance')}</h2>
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

        {/* Sparaknapp & Logout */}
        <div className="space-y-3 pt-1">
          <button
            onClick={handleSave}
            className="w-full py-3.5 rounded-2xl bg-gradient-to-r from-primary to-primary/80 text-primary-foreground font-semibold text-sm shadow-lg hover:shadow-xl hover:scale-[1.01] active:scale-[0.99] transition-all duration-200"
          >
            {t('common.save')}
          </button>
          <button
            onClick={handleLogout}
            className="w-full flex items-center justify-center gap-2 py-3.5 rounded-2xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 font-semibold text-sm hover:bg-red-500/20 hover:border-red-500/30 active:scale-[0.99] transition-all duration-200"
          >
            <LogOut className="w-4 h-4" />
            {t('auth.logout')}
          </button>
        </div>
      </div>
    </div>
  );
};

export default Settings;