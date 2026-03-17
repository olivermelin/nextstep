import { Home, TrendingUp, Zap, MessageCircle, Settings } from "lucide-react";
import { useLocation, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

const Navigation = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { t } = useTranslation();

  const navItems = [
    { path: "/dashboard", icon: Home, label: t('navigation.home') },
    { path: "/progress", icon: TrendingUp, label: t('navigation.progress') },
    { path: "/challenges", icon: Zap, label: t('navigation.challenges') },
    { path: "/ai-coach", icon: MessageCircle, label: t('navigation.aiCoach') },
    { path: "/settings", icon: Settings, label: t('navigation.settings') }
  ];

  // Visa inte navigation på onboarding-sidan
  if (location.pathname === "/") {
    return null;
  }

  return (
    <nav
      className="fixed bottom-0 left-0 right-0 bg-card/70 backdrop-blur-xl border-t border-border/50 shadow-lg z-50"
      role="navigation"
      aria-label={t('navigation.mainNav', 'Huvudnavigation')}
    >
      <div className="max-w-4xl mx-auto flex justify-around items-center h-16 px-4">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = location.pathname === item.path ||
            (item.path !== "/dashboard" && location.pathname.startsWith(item.path));
          return (
            <button
              key={item.path}
              onClick={() => navigate(item.path)}
              aria-label={item.label}
              aria-current={isActive ? "page" : undefined}
              className={`relative flex flex-col items-center justify-center gap-0.5 py-1 px-3 rounded-xl transition-all duration-300 ${
                isActive
                  ? "text-primary"
                  : "text-muted-foreground hover:text-foreground active:scale-95"
              }`}
            >
              {isActive && (
                <span className="absolute -top-0.5 left-0 right-0 mx-auto w-5 h-0.5 rounded-full bg-primary animate-scale-in" aria-hidden="true" />
              )}
              <Icon className={`w-5 h-5 transition-transform duration-300 ${isActive ? "scale-110" : ""}`} aria-hidden="true" />
              <span className={`text-[10px] font-medium transition-opacity duration-200 ${isActive ? "opacity-100" : "opacity-70"}`}>
                {item.label}
              </span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};

export default Navigation;
