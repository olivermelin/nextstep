import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate, useLocation, useNavigate } from "react-router-dom";
import { AnimatePresence } from "framer-motion";
import PageTransition from "@/components/PageTransition";
import { AuthProvider, useAuth } from "@/context/AuthContext";
import { Settings as SettingsIcon } from "lucide-react";
import ErrorBoundary from "@/components/ErrorBoundary";
import { useTranslation } from "react-i18next";
import Onboarding from "./pages/Onboarding";
import Dashboard from "./pages/Dashboard";
import Progress from "./pages/Progress";
import Challenges from "./pages/Challenges";
import Settings from "./pages/Settings";
import AICoach from "./pages/AICoach";
import Social from "./pages/Social";
import Navigation from "./components/Navigation";
import NotFound from "./pages/NotFound";
import Login from "./pages/Login";
import ResetPassword from "./pages/ResetPassword";
import SOSButton from "./components/SOSButton";
import "./App.css";

const queryClient = new QueryClient();

function AppContent() {
  const { user, loading, login, logout } = useAuth();
  const { t } = useTranslation();
  const location = useLocation();
  const navigate = useNavigate();

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen bg-background">
        <div className="text-center">
          <div className="w-12 h-12 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-muted-foreground">{t('common.loading')}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-screen">
      {/* Header */}
      <header className="bg-card/70 backdrop-blur-xl border-b border-border/50 px-4 py-3 sticky top-0 z-40">
        <div className="max-w-4xl mx-auto flex justify-between items-center">
          <h1 className="text-2xl font-bold">{t('common.appName')}</h1>
          
          {user && (
            <div className="flex items-center gap-3">
              <div className="flex items-center gap-3">
                {user.picture && (
                  <img
                    src={user.picture}
                    alt={user.name}
                    className="w-9 h-9 rounded-full border-2 border-primary/20 shadow-sm"
                  />
                )}
                <div className="text-right hidden sm:block">
                  <p className="font-semibold text-sm">{user.name}</p>
                  <p className="text-xs text-muted-foreground">{user.email}</p>
                </div>
              </div>
              <button
                onClick={() => navigate('/settings')}
                className="p-2 rounded-xl hover:bg-muted text-muted-foreground hover:text-foreground transition-all duration-200"
                aria-label={t('navigation.settings')}
              >
                <SettingsIcon className="w-5 h-5" />
              </button>
            </div>
          )}
        </div>
      </header>

      {/* Huvudinnehål */}
      <main className="flex-1 overflow-y-auto pb-24">
        <AnimatePresence mode="wait">
        {user ? (
          // Redirecta till onboarding om inte slutförd
          !user.onboardingCompleted && location.pathname !== '/onboarding' ? (
            <Navigate to="/onboarding" replace />
          ) : (
          <Routes location={location} key={location.pathname}>
            <Route path="/dashboard" element={<PageTransition><Dashboard /></PageTransition>} />
            <Route path="/progress" element={<PageTransition><Progress /></PageTransition>} />
            <Route path="/challenges" element={<PageTransition><Challenges /></PageTransition>} />
            <Route path="/challenges/:categorySlug" element={<PageTransition><Challenges /></PageTransition>} />
            <Route path="/challenges/:categorySlug/:challengeId" element={<PageTransition><Challenges /></PageTransition>} />
            <Route path="/social" element={<PageTransition><Social /></PageTransition>} />
            <Route path="/ai-coach" element={<PageTransition><AICoach /></PageTransition>} />
            <Route path="/settings" element={<PageTransition><Settings /></PageTransition>} />
            <Route path="/onboarding" element={<PageTransition><Onboarding /></PageTransition>} />
            <Route path="/login" element={<Navigate to="/dashboard" replace />} />
            <Route path="/" element={<Navigate to="/dashboard" />} />
            <Route path="*" element={<PageTransition><NotFound /></PageTransition>} />
          </Routes>
          )
        ) : (
          <Routes location={location} key={location.pathname}>
            <Route path="/login" element={<PageTransition><Login /></PageTransition>} />
            <Route path="/reset-password/:token" element={<PageTransition><ResetPassword /></PageTransition>} />
            <Route path="/" element={<Navigate to="/login" />} />
            <Route path="*" element={<Navigate to="/login" />} />
          </Routes>
        )}
        </AnimatePresence>
      </main>

      {user && <Navigation />}
      {user && <SOSButton />}
    </div>
  );
}

function App() {
  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <TooltipProvider>
          <BrowserRouter>
            <AuthProvider>
              <AppContent />
            </AuthProvider>
          </BrowserRouter>
          <Toaster />
          <Sonner />
        </TooltipProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  );
}

export default App;
