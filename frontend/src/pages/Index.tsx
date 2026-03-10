import { useState } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/components/ui/use-toast';
import { Loader2 } from 'lucide-react';
import { useTranslation } from 'react-i18next';

const Index = () => {
  const { login, loginWithEmail } = useAuth();
  const { toast } = useToast();
  const { t } = useTranslation();
  const [showEmailForm, setShowEmailForm] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleEmailLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email || !password) {
      toast({
        title: t('common.error'),
        description: t('auth.fillEmailPassword'),
        variant: 'destructive',
      });
      return;
    }

    setLoading(true);
    try {
      await loginWithEmail(email, password);
      toast({
        title: t('auth.success'),
        description: t('auth.loggedIn'),
      });
    } catch (error) {
      toast({
        title: t('auth.loginFailed'),
        description: error instanceof Error ? error.message : t('auth.tryAgainLater'),
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-primary/5 via-background to-secondary/5">
      <div className="w-full max-w-md px-4">
        <div className="text-center space-y-6">
          {/* Header */}
          <div>
            <h1 className="mb-2 text-5xl font-bold">{t('common.appName')}</h1>
            <p className="text-xl text-muted-foreground">{t('auth.subtitle')}</p>
          </div>
          
          <p className="text-muted-foreground">
            {t('auth.description')}
          </p>

          {/* Login Options */}
          {!showEmailForm ? (
            <div className="space-y-4">
              {/* Google OAuth Button */}
              <button
                onClick={login}
                className="w-full inline-flex items-center justify-center gap-2 bg-primary text-primary-foreground px-8 py-3 rounded-lg hover:bg-primary/90 font-semibold transition"
              >
                🔐 {t('auth.loginWithGoogle')}
              </button>

              {/* Divider */}
              <div className="relative">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-border"></div>
                </div>
                <div className="relative flex justify-center text-sm">
                  <span className="px-2 bg-background text-muted-foreground">{t('common.or')}</span>
                </div>
              </div>

              {/* Email Login Button */}
              <button
                onClick={() => setShowEmailForm(true)}
                className="w-full inline-flex items-center justify-center gap-2 bg-secondary text-secondary-foreground px-8 py-3 rounded-lg hover:bg-secondary/90 font-semibold transition"
              >
                📧 {t('auth.loginWithEmail')}
              </button>

              <p className="text-sm text-muted-foreground pt-4">
                {t('auth.secureLoginGoogle')}
              </p>
            </div>
          ) : (
            /* Email Login Form */
            <form onSubmit={handleEmailLogin} className="space-y-4">
              <div>
                <label htmlFor="email" className="block text-sm font-medium mb-2">
                  {t('auth.email')}
                </label>
                <input
                  id="email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder={t('auth.emailPlaceholder')}
                  className="w-full px-4 py-2 border border-input rounded-lg bg-background text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  disabled={loading}
                />
              </div>

              <div>
                <label htmlFor="password" className="block text-sm font-medium mb-2">
                  {t('auth.password')}
                </label>
                <input
                  id="password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full px-4 py-2 border border-input rounded-lg bg-background text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  disabled={loading}
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full bg-primary text-primary-foreground px-8 py-3 rounded-lg hover:bg-primary/90 font-semibold transition disabled:opacity-50 disabled:cursor-not-allowed inline-flex items-center justify-center gap-2"
              >
                {loading ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    Loggar in...
                  </>
                ) : (
                  t('auth.login')
                )}
              </button>

              <button
                type="button"
                onClick={() => setShowEmailForm(false)}
                className="w-full text-sm text-muted-foreground hover:text-foreground transition"
              >
                {t('common.back')}
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default Index;
