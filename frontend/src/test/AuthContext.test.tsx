import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, act } from '@testing-library/react';
import { AuthProvider, useAuth } from '@/context/AuthContext';
import { BrowserRouter } from 'react-router-dom';

const TestConsumer = () => {
  const { user, loading } = useAuth();
  if (loading) return <div>Loading...</div>;
  if (user) return <div>Logged in as {user.email}</div>;
  return <div>Not logged in</div>;
};

const renderWithAuth = () => {
  return render(
    <BrowserRouter>
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    </BrowserRouter>
  );
};

describe('AuthContext', () => {
  const originalFetch = globalThis.fetch;

  beforeEach(() => {
    globalThis.fetch = vi.fn();
  });

  afterEach(() => {
    globalThis.fetch = originalFetch;
  });

  it('shows loading initially', async () => {
    // Never resolve fetch to keep loading state
    (globalThis.fetch as ReturnType<typeof vi.fn>).mockReturnValue(new Promise(() => {}));

    renderWithAuth();
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('sets user when /auth/me returns authenticated user', async () => {
    (globalThis.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({
        authenticated: true,
        id: 'user@test.com',
        name: 'Test User',
        email: 'user@test.com',
        onboardingCompleted: true,
      }),
    });

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByText('Logged in as user@test.com')).toBeInTheDocument();
    });
  });

  it('sets user to null when /auth/me returns 401', async () => {
    (globalThis.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
      ok: false,
      status: 401,
    });

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByText('Not logged in')).toBeInTheDocument();
    });
  });

  it('sets user to null on network error', async () => {
    (globalThis.fetch as ReturnType<typeof vi.fn>).mockRejectedValue(new Error('Network error'));

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByText('Not logged in')).toBeInTheDocument();
    });
  });

  it('sets user to null when response lacks id/email', async () => {
    (globalThis.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({
        authenticated: false,
      }),
    });

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByText('Not logged in')).toBeInTheDocument();
    });
  });

  it('throws error when useAuth is used outside AuthProvider', () => {
    // Suppress console.error for this test
    const spy = vi.spyOn(console, 'error').mockImplementation(() => {});

    const BadComponent = () => {
      useAuth();
      return null;
    };

    expect(() => render(<BadComponent />)).toThrow('useAuth måste användas inom AuthProvider');
    spy.mockRestore();
  });
});
