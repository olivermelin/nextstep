import { useAuth } from '@/context/AuthContext';

/**
 * Hook som returnerar det korrekta userId:t oavsett inloggningsmetod.
 * Prioriterar email (som backend använder som primär identifierare),
 * med fallback till id.
 */
export const useUserId = (): string | null => {
  const { user } = useAuth();
  if (!user) return null;
  return user.email || user.id || null;
};
