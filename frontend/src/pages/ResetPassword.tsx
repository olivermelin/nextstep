import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useToast } from '@/hooks/use-toast';
import { Loader2, CheckCircle2 } from 'lucide-react';
import { useTranslation } from 'react-i18next';

const ResetPassword = () => {
  const { token } = useParams<{ token: string }>();
  const navigate = useNavigate();
  const { toast } = useToast();
  const { t } = useTranslation();
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();

    if (password.length < 8) {
      toast({
        title: t('common.error'),
        description: t('resetPassword.minLength'),
        variant: 'destructive',
      });
      return;
    }

    if (password !== confirmPassword) {
      toast({
        title: t('common.error'),
        description: t('resetPassword.mismatch'),
        variant: 'destructive',
      });
      return;
    }

    setLoading(true);
    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080/api'}/auth/reset-password`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ token, newPassword: password }),
      });

      if (response.ok) {
        setSuccess(true);
        toast({
          title: t('resetPassword.success'),
          description: t('resetPassword.successDesc'),
        });
        
        setTimeout(() => {
          navigate('/login');
        }, 3000);
      } else {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || t('resetPassword.errorResetting'));
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
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-background via-background to-primary/5 p-4 relative overflow-hidden">
      {/* Animated background elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/4 -left-20 w-72 h-72 bg-primary/10 rounded-full blur-3xl animate-pulse" />
        <div className="absolute bottom-1/4 -right-20 w-96 h-96 bg-primary/5 rounded-full blur-3xl animate-pulse delay-1000" />
      </div>

      <div className="w-full max-w-md space-y-8 relative z-10">
        {/* Logo/Header */}
        <div className="text-center animate-in fade-in slide-in-from-bottom-4 duration-1000">
          <div className="inline-block p-4 rounded-2xl bg-gradient-to-br from-primary/10 to-primary/5 mb-4 backdrop-blur-sm">
            <div className="text-5xl">🔑</div>
          </div>
          <h1 className="text-4xl font-bold tracking-tight bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent">
            {t('resetPassword.title')}
          </h1>
          <p className="text-base text-muted-foreground mt-2">
            {t('resetPassword.subtitle')}
          </p>
        </div>

        {/* Main Card */}
        <div className="bg-card/50 backdrop-blur-xl border border-border/50 rounded-3xl shadow-2xl p-8 space-y-6 animate-in fade-in slide-in-from-bottom-8 duration-1000 delay-200">
          {success ? (
            <div className="text-center py-8 space-y-4 animate-in fade-in zoom-in duration-500">
              <CheckCircle2 className="w-20 h-20 text-green-500 mx-auto" />
              <h2 className="text-2xl font-semibold text-foreground">{t('resetPassword.done')}</h2>
              <p className="text-muted-foreground">
                {t('resetPassword.redirecting')} <br />
                {t('resetPassword.redirectingDesc')}
              </p>
            </div>
          ) : (
            <form onSubmit={handleResetPassword} className="space-y-5 animate-in fade-in duration-500">
              <div className="space-y-2">
                <label htmlFor="password" className="block text-sm font-medium text-foreground/90">
                  {t('resetPassword.newPassword')}
                </label>
                <input
                  id="password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder={t('auth.passwordPlaceholder')}
                  className="w-full px-4 py-3.5 bg-background/50 border border-border/50 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all text-foreground placeholder:text-muted-foreground"
                  disabled={loading}
                  required
                />
              </div>

              <div className="space-y-2">
                <label htmlFor="confirmPassword" className="block text-sm font-medium text-foreground/90">
                  {t('resetPassword.confirmPassword')}
                </label>
                <input
                  id="confirmPassword"
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder={t('resetPassword.confirmPlaceholder')}
                  className="w-full px-4 py-3.5 bg-background/50 border border-border/50 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all text-foreground placeholder:text-muted-foreground"
                  disabled={loading}
                  required
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
                    <span>{t('resetPassword.submitting')}</span>
                  </>
                ) : (
                  <span>{t('resetPassword.submit')}</span>
                )}
              </button>

              <button
                type="button"
                onClick={() => navigate('/login')}
                className="w-full text-sm text-muted-foreground hover:text-foreground transition-colors"
              >
                                {t('auth.backToLogin')}
              </button>
            </form>
          )}
        </div>

        {/* Footer */}
        <p className="text-center text-sm text-muted-foreground animate-in fade-in duration-1000 delay-500">
          {t('common.copyright')}
        </p>
      </div>
    </div>
  );
};

export default ResetPassword;
