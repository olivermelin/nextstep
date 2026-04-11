import { useState, useMemo } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/hooks/use-toast';
import { Loader2, ArrowRight, Sparkles, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { useTranslation } from 'react-i18next';

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

type FieldErrors = {
  name?: string;
  email?: string;
  password?: string;
};

function getPasswordStrength(password: string): 'weak' | 'medium' | 'strong' {
  let score = 0;
  if (password.length >= 8) score++;
  if (password.length >= 12) score++;
  if (/[0-9]/.test(password)) score++;
  if (/[^a-zA-Z0-9]/.test(password)) score++;
  if (/[A-Z]/.test(password) && /[a-z]/.test(password)) score++;
  if (score <= 2) return 'weak';
  if (score <= 3) return 'medium';
  return 'strong';
}

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
  const [touched, setTouched] = useState<Record<string, boolean>>({});

  const fieldErrors = useMemo((): FieldErrors => {
    const errors: FieldErrors = {};

    // Email validation
    if (!email.trim()) {
      errors.email = t('auth.validationEmailRequired');
    } else if (!EMAIL_REGEX.test(email.trim())) {
      errors.email = t('auth.validationEmailInvalid');
    }

    // Password validation
    if (!password) {
      errors.password = t('auth.validationPasswordRequired');
    } else if (isSignup && password.length < 8) {
      errors.password = t('auth.validationPasswordMinLength');
    }

    // Name validation (signup only)
    if (isSignup) {
      if (!name.trim()) {
        errors.name = t('auth.validationNameRequired');
      } else if (name.trim().length < 2) {
        errors.name = t('auth.validationNameMinLength');
      }
    }

    return errors;
  }, [email, password, name, isSignup, t]);

  const passwordStrength = useMemo(() => {
    if (!password || password.length < 8) return null;
    return getPasswordStrength(password);
  }, [password]);

  const isFormValid = Object.keys(fieldErrors).length === 0;

  const handleBlur = (field: string) => {
    setTouched((prev) => ({ ...prev, [field]: true }));
  };

  const handleEmailSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Mark all fields as touched to show errors
    setTouched({ email: true, password: true, ...(isSignup ? { name: true } : {}) });

    if (!isFormValid) {
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
    setTouched({});
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

  const strengthColor = passwordStrength === 'strong'
    ? 'text-green-600 dark:text-green-400'
    : passwordStrength === 'medium'
      ? 'text-yellow-600 dark:text-yellow-400'
      : 'text-red-500 dark:text-red-400';

  const strengthBarColor = passwordStrength === 'strong'
    ? 'bg-green-500'
    : passwordStrength === 'medium'
      ? 'bg-yellow-500'
      : 'bg-red-500';

  const strengthBarWidth = passwordStrength === 'strong'
    ? 'w-full'
    : passwordStrength === 'medium'
      ? 'w-2/3'
      : 'w-1/3';

  const inputErrorClass = 'border-red-400 focus:ring-red-400/50 focus:border-red-400';
  const baseInputClass = 'w-full px-4 py-3.5 bg-background/50 border rounded-xl focus:outline-none focus:ring-2 transition-all text-foreground placeholder:text-muted-foreground';
  const normalInputClass = 'border-border/50 focus:ring-primary/50 focus:border-primary';

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center overflow-hidden relative">
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
              <Button
                onClick={login}
                className="group h-auto w-full flex items-center justify-center gap-3 bg-white dark:bg-card text-foreground px-6 py-4 rounded-2xl border border-border/50 hover:border-primary/50 hover:shadow-lg font-medium transition-all duration-300 hover:scale-[1.02] active:scale-[0.98]"
              >
                <svg className="w-5 h-5" viewBox="0 0 24 24" aria-hidden="true">
                  <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                  <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                  <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                  <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                </svg>
                <span>{t('auth.loginWithGoogle')}</span>
              </Button>

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
                <Button
                  onClick={() => {
                    setShowEmailForm(true);
                    setIsSignup(false);
                  }}
                  className="h-auto w-full flex items-center justify-between bg-primary/5 dark:bg-primary/10 text-foreground px-6 py-4 rounded-2xl border border-primary/20 hover:border-primary/40 hover:bg-primary/10 dark:hover:bg-primary/20 font-medium transition-all duration-300 group"
                >
                  <span>{t('auth.loginWithEmail')}</span>
                  <ArrowRight className="w-5 h-5 text-primary group-hover:translate-x-1 transition-transform" />
                </Button>

                <Button
                  onClick={() => {
                    setShowEmailForm(true);
                    setIsSignup(true);
                  }}
                  className="h-auto w-full flex items-center justify-between bg-accent/5 dark:bg-accent/10 text-foreground px-6 py-4 rounded-2xl border border-accent/20 hover:border-accent/40 hover:bg-accent/10 dark:hover:bg-accent/20 font-medium transition-all duration-300 group"
                >
                  <span>{t('auth.createAccount')}</span>
                  <ArrowRight className="w-5 h-5 text-accent group-hover:translate-x-1 transition-transform" />
                </Button>
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
                  className={`${baseInputClass} ${normalInputClass}`}
                  disabled={loading}
                />
              </div>

              <p className="text-sm text-muted-foreground">
                {t('forgotPassword.instructions')}
              </p>

              <Button
                type="submit"
                disabled={loading}
                className="h-auto w-full flex items-center justify-center gap-2 bg-gradient-to-r from-primary to-primary/80 text-primary-foreground px-6 py-4 rounded-xl hover:shadow-lg font-semibold transition-all duration-300 hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100"
              >
                {loading ? (
                  <>
                    <Loader2 className="w-5 h-5 animate-spin" />
                    <span>{t('forgotPassword.sending')}</span>
                  </>
                ) : (
                  <span>{t('forgotPassword.sendLink')}</span>
                )}
              </Button>

              <Button
                type="button"
                variant="ghost"
                onClick={resetForm}
                className="h-auto w-full text-sm text-muted-foreground hover:text-foreground"
              >
                {t('auth.backToLogin')}
              </Button>
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
                    onBlur={() => handleBlur('name')}
                    placeholder={t('auth.namePlaceholder')}
                    aria-describedby={touched.name && fieldErrors.name ? "name-error" : undefined}
                    aria-invalid={touched.name && !!fieldErrors.name}
                    className={`${baseInputClass} ${touched.name && fieldErrors.name ? inputErrorClass : normalInputClass}`}
                    disabled={loading}
                  />
                  {touched.name && fieldErrors.name && (
                    <p id="name-error" role="alert" className="text-sm text-red-500 dark:text-red-400 animate-in fade-in slide-in-from-top-1 duration-200">
                      {fieldErrors.name}
                    </p>
                  )}
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
                  onBlur={() => handleBlur('email')}
                  placeholder={t('auth.emailPlaceholder')}
                  aria-describedby={touched.email && fieldErrors.email ? "email-error" : undefined}
                  aria-invalid={touched.email && !!fieldErrors.email}
                  className={`${baseInputClass} ${touched.email && fieldErrors.email ? inputErrorClass : normalInputClass}`}
                  disabled={loading}
                />
                {touched.email && fieldErrors.email && (
                  <p id="email-error" role="alert" className="text-sm text-red-500 dark:text-red-400 animate-in fade-in slide-in-from-top-1 duration-200">
                    {fieldErrors.email}
                  </p>
                )}
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
                  onBlur={() => handleBlur('password')}
                  placeholder={t('auth.passwordPlaceholder')}
                  aria-describedby={touched.password && fieldErrors.password ? "password-error" : undefined}
                  aria-invalid={touched.password && !!fieldErrors.password}
                  className={`${baseInputClass} ${touched.password && fieldErrors.password ? inputErrorClass : normalInputClass}`}
                  disabled={loading}
                />
                {touched.password && fieldErrors.password && (
                  <p id="password-error" role="alert" className="text-sm text-red-500 dark:text-red-400 animate-in fade-in slide-in-from-top-1 duration-200">
                    {fieldErrors.password}
                  </p>
                )}
                {isSignup && password.length > 0 && !fieldErrors.password && (
                  <div className="space-y-1.5 animate-in fade-in duration-200">
                    <div className="flex items-center gap-2">
                      <div className="flex-1 h-1.5 bg-muted rounded-full overflow-hidden">
                        <div className={`h-full rounded-full transition-all duration-300 ${strengthBarColor} ${strengthBarWidth}`} />
                      </div>
                      <span className={`text-xs font-medium ${strengthColor}`}>
                        {passwordStrength === 'strong'
                          ? t('auth.validationPasswordStrengthStrong')
                          : passwordStrength === 'medium'
                            ? t('auth.validationPasswordStrengthMedium')
                            : t('auth.validationPasswordStrengthWeak')}
                      </span>
                    </div>
                    <p className="text-xs text-muted-foreground">
                      {t('auth.validationPasswordHint')}
                    </p>
                  </div>
                )}
              </div>

              <Button
                type="submit"
                disabled={loading || !isFormValid}
                className="h-auto w-full flex items-center justify-center gap-2 bg-gradient-to-r from-primary to-primary/80 text-primary-foreground px-6 py-4 rounded-xl hover:shadow-lg font-semibold transition-all duration-300 hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100"
              >
                {loading ? (
                  <>
                    <Loader2 className="w-5 h-5 animate-spin" />
                    <span>{isSignup ? t('auth.creatingAccount') : t('auth.loggingIn')}</span>
                  </>
                ) : (
                  <span>{isSignup ? t('auth.createAccount') : t('auth.login')}</span>
                )}
              </Button>

              <div className="flex items-center justify-between pt-2">
                <Button
                  type="button"
                  variant="ghost"
                  onClick={resetForm}
                  className="h-auto text-sm text-muted-foreground hover:text-foreground px-0"
                >
                  {t('auth.backButton')}
                </Button>

                {!isSignup ? (
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() => {
                      setShowEmailForm(false);
                      setShowForgotPassword(true);
                    }}
                    className="h-auto text-sm text-primary hover:text-primary/80 font-medium px-0"
                  >
                    {t('auth.forgotPassword')}
                  </Button>
                ) : (
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() => setIsSignup(false)}
                    className="h-auto text-sm text-primary hover:text-primary/80 font-medium px-0"
                  >
                    {t('auth.hasAccount')}
                  </Button>
                )}
              </div>

              {!isSignup && (
                <div className="text-center pt-2">
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() => setIsSignup(true)}
                    className="h-auto text-sm text-muted-foreground hover:text-foreground px-0"
                  >
                    {t('auth.noAccount')} <span className="text-primary font-medium">{t('auth.createOne')}</span>
                  </Button>
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
