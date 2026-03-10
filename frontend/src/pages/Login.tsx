import { useState } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/components/ui/use-toast';
import { Loader2, ArrowRight, Sparkles, AlertCircle } from 'lucide-react';
import { useTranslation } from 'react-i18next';

const Login = () => {
  const { login, loginWithEmail, signupWithEmail } = useAuth();
  const { toast } = useToast();
  const { t } = useTranslation();
  const [isSignup, setIsSignup] = useState(false);
  const [showEmailForm, setShowEmailForm] = useState(false);
  const [showForgotPassword, setShowForgotPassword] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [loading, setLoading] = useState(false);
  const [loginError, setLoginError] = useState<string | null>(null);

  const handleEmailSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email || !password || (isSignup && !name)) {
      toast({
        title: t('common.error'),
        description: isSignup ? t('auth.fillAllFields') : t('auth.fillEmailPassword'),
        variant: 'destructive',
      });
      return;
    }

    setLoading(true);
    setLoginError(null);
    try {
      if (isSignup) {
        await signupWithEmail(email, password, name);
        toast({
          title: t('auth.welcomeNew'),
          description: t('auth.accountCreated'),
        });
      } else {
        await loginWithEmail(email, password);
        toast({
          title: t('auth.welcomeBack'),
          description: t('auth.loggedIn'),
        });
      }
    } catch (error) {
      const errorMsg = error instanceof Error ? error.message : t('auth.tryAgainLater');
      setLoginError(errorMsg);
      toast({
        title: isSignup ? t('auth.signupFailed') : t('auth.loginFailed'),
        description: errorMsg,
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setShowEmailForm(false);
    setIsSignup(false);
    setShowForgotPassword(false);
    setEmail('');
    setPassword('');
    setName('');
    setLoginError(null);
  };

  const handleForgotPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email) {
      toast({
        title: t('common.error'),
        description: t('auth.enterEmail'),
        variant: 'destructive',
      });
      return;
    }

    setLoading(true);
    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080/api'}/auth/forgot-password`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email }),
      });

      if (response.ok) {
        toast({
          title: t('forgotPassword.emailSent'),
          description: t('forgotPassword.emailSentDesc'),
        });
        setShowForgotPassword(false);
        setShowEmailForm(false);
        setEmail('');
      } else {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || t('forgotPassword.errorSending'));
      }
    } catch (error) {
      toast({
        title: t('auth.logoutError'),
        description: error instanceof Error ? error.message : t('auth.tryAgainLater'),
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-background via-background to-primary/5 overflow-hidden relative">
      {/* Animated background elements - Apple style */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/4 -left-48 w-96 h-96 bg-primary/10 rounded-full blur-3xl animate-pulse" 
             style={{ animationDuration: '8s' }} />
        <div className="absolute bottom-1/4 -right-48 w-96 h-96 bg-accent/10 rounded-full blur-3xl animate-pulse" 
             style={{ animationDuration: '10s', animationDelay: '2s' }} />
      </div>

      <div className="w-full max-w-md px-6 relative z-10">
        {/* Card container with glassmorphism effect */}
        <div className="bg-card/80 backdrop-blur-xl border border-border/50 rounded-3xl shadow-2xl p-8 space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
          
          {/* Header */}
          <div className="text-center space-y-3">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-primary to-primary/60 mb-2 shadow-lg">
              <Sparkles className="w-8 h-8 text-primary-foreground" />
            </div>
            <h1 className="text-4xl font-bold bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent">
              NextStep
            </h1>
            <p className="text-base text-muted-foreground">
              {showForgotPassword 
                ? t('forgotPassword.title')
                : showEmailForm 
                  ? (isSignup ? t('auth.createAccount') : t('auth.welcomeBack'))
                  : t('auth.subtitle')
              }
            </p>
          </div>

          {!showEmailForm && !showForgotPassword ? (
            /* Initial login options */
            <div className="space-y-4 animate-in fade-in duration-500">
              {/* Google OAuth Button */}
              <button
                onClick={login}
                className="group w-full flex items-center justify-center gap-3 bg-white dark:bg-card text-foreground px-6 py-4 rounded-2xl border border-border/50 hover:border-primary/50 hover:shadow-lg font-medium transition-all duration-300 hover:scale-[1.02] active:scale-[0.98]"
              >
                <svg className="w-5 h-5" viewBox="0 0 24 24">
                  <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                  <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                  <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                  <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                </svg>
                <span>{t('auth.loginWithGoogle')}</span>
              </button>

              {/* Divider */}
              <div className="relative py-2">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-border/30"></div>
                </div>
                <div className="relative flex justify-center text-xs uppercase">
                  <span className="px-3 bg-card/80 text-muted-foreground font-medium">{t('common.or')}</span>
                </div>
              </div>

              {/* Email options */}
              <div className="space-y-3">
                <button
                  onClick={() => {
                    setShowEmailForm(true);
                    setIsSignup(false);
                  }}
                  className="w-full flex items-center justify-between bg-primary/5 dark:bg-primary/10 text-foreground px-6 py-4 rounded-2xl border border-primary/20 hover:border-primary/40 hover:bg-primary/10 dark:hover:bg-primary/20 font-medium transition-all duration-300 group"
                >
                  <span>{t('auth.loginWithEmail')}</span>
                  <ArrowRight className="w-5 h-5 text-primary group-hover:translate-x-1 transition-transform" />
                </button>
                
                <button
                  onClick={() => {
                    setShowEmailForm(true);
                    setIsSignup(true);
                  }}
                  className="w-full flex items-center justify-between bg-accent/5 dark:bg-accent/10 text-foreground px-6 py-4 rounded-2xl border border-accent/20 hover:border-accent/40 hover:bg-accent/10 dark:hover:bg-accent/20 font-medium transition-all duration-300 group"
                >
                  <span>{t('auth.createAccount')}</span>
                  <ArrowRight className="w-5 h-5 text-accent group-hover:translate-x-1 transition-transform" />
                </button>
              </div>

              <p className="text-xs text-center text-muted-foreground pt-4">
                {t('auth.secureLogin')}
              </p>
            </div>
          ) : showForgotPassword ? (
            /* Forgot Password Form */
            <form onSubmit={handleForgotPassword} className="space-y-5 animate-in fade-in duration-500">
              <div className="space-y-2">
                <label htmlFor="email" className="block text-sm font-medium text-foreground/90">
                  {t('auth.email')}
                </label>
                <input
                  id="email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder={t('auth.emailPlaceholder')}
                  className="w-full px-4 py-3.5 bg-background/50 border border-border/50 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all text-foreground placeholder:text-muted-foreground"
                  disabled={loading}
                />
              </div>

              <p className="text-sm text-muted-foreground">
                {t('forgotPassword.instructions')}
              </p>

              <button
                type="submit"
                disabled={loading}
                className="w-full flex items-center justify-center gap-2 bg-gradient-to-r from-primary to-primary/80 text-primary-foreground px-6 py-4 rounded-xl hover:shadow-lg font-semibold transition-all duration-300 hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100"
              >
                {loading ? (
                  <>
                    <Loader2 className="w-5 h-5 animate-spin" />
                    <span>{t('forgotPassword.sending')}</span>
                  </>
                ) : (
                  <span>{t('forgotPassword.sendLink')}</span>
                )}
              </button>

              <button
                type="button"
                onClick={resetForm}
                className="w-full text-sm text-muted-foreground hover:text-foreground transition-colors"
              >
                {t('auth.backToLogin')}
              </button>
            </form>
          ) : (
            /* Email Form */
            <form onSubmit={handleEmailSubmit} className="space-y-5 animate-in fade-in duration-500">
              {loginError && (
                <div className="flex items-center gap-2 bg-destructive/10 border border-destructive/30 text-destructive rounded-xl px-4 py-3 animate-in fade-in slide-in-from-top-2 duration-300">
                  <AlertCircle className="w-4 h-4 flex-shrink-0" />
                  <span className="text-sm font-medium">{loginError}</span>
                </div>
              )}
              {isSignup && (
                <div className="space-y-2">
                  <label htmlFor="name" className="block text-sm font-medium text-foreground/90">
                    {t('auth.name')}
                  </label>
                  <input
                    id="name"
                    type="text"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder={t('auth.namePlaceholder')}
                    className="w-full px-4 py-3.5 bg-background/50 border border-border/50 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all text-foreground placeholder:text-muted-foreground"
                    disabled={loading}
                  />
                </div>
              )}

              <div className="space-y-2">
                <label htmlFor="email" className="block text-sm font-medium text-foreground/90">
                  {t('auth.email')}
                </label>
                <input
                  id="email"
                  type="email"
                  value={email}
                  onChange={(e) => { setEmail(e.target.value); setLoginError(null); }}
                  placeholder={t('auth.emailPlaceholder')}
                  className="w-full px-4 py-3.5 bg-background/50 border border-border/50 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all text-foreground placeholder:text-muted-foreground"
                  disabled={loading}
                />
              </div>

              <div className="space-y-2">
                <label htmlFor="password" className="block text-sm font-medium text-foreground/90">
                  {t('auth.password')}
                </label>
                <input
                  id="password"
                  type="password"
                  value={password}
                  onChange={(e) => { setPassword(e.target.value); setLoginError(null); }}
                  placeholder={t('auth.passwordPlaceholder')}
                  className="w-full px-4 py-3.5 bg-background/50 border border-border/50 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all text-foreground placeholder:text-muted-foreground"
                  disabled={loading}
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full flex items-center justify-center gap-2 bg-gradient-to-r from-primary to-primary/80 text-primary-foreground px-6 py-4 rounded-xl hover:shadow-lg font-semibold transition-all duration-300 hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100"
              >
                {loading ? (
                  <>
                    <Loader2 className="w-5 h-5 animate-spin" />
                    <span>{isSignup ? t('auth.creatingAccount') : t('auth.loggingIn')}</span>
                  </>
                ) : (
                  <span>{isSignup ? t('auth.createAccount') : t('auth.login')}</span>
                )}
              </button>

              <div className="flex items-center justify-between pt-2">
                <button
                  type="button"
                  onClick={resetForm}
                  className="text-sm text-muted-foreground hover:text-foreground transition-colors"
                >
                  {t('auth.backButton')}
                 </button>
                
                {!isSignup ? (
                  <button
                    type="button"
                    onClick={() => {
                      setShowEmailForm(false);
                      setShowForgotPassword(true);
                    }}
                    className="text-sm text-primary hover:text-primary/80 transition-colors font-medium"
                  >
                    {t('auth.forgotPassword')}
                  </button>
                ) : (
                  <button
                    type="button"
                    onClick={() => setIsSignup(false)}
                    className="text-sm text-primary hover:text-primary/80 transition-colors font-medium"
                  >
                    {t('auth.hasAccount')}
                  </button>
                )}
              </div>
              
              {!isSignup && (
                <div className="text-center pt-2">
                  <button
                    type="button"
                    onClick={() => setIsSignup(true)}
                    className="text-sm text-muted-foreground hover:text-foreground transition-colors"
                  >
                    {t('auth.noAccount')} <span className="text-primary font-medium">{t('auth.createOne')}</span>
                  </button>
                </div>
              )}
            </form>
          )}
        </div>

        {/* Footer text */}
        <p className="text-center text-sm text-muted-foreground mt-8 animate-in fade-in duration-1000 delay-500">
          {t('common.copyright')}
        </p>
      </div>
    </div>
  );
};

export default Login;
