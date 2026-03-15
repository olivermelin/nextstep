import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { fetchWithCredentials, ApiError, API_ENDPOINTS } from '@/config/api';

describe('ApiError', () => {
  it('has correct name and status', () => {
    const error = new ApiError('Not found', 404);
    expect(error.name).toBe('ApiError');
    expect(error.message).toBe('Not found');
    expect(error.status).toBe(404);
    expect(error).toBeInstanceOf(Error);
  });
});

describe('fetchWithCredentials', () => {
  const originalFetch = globalThis.fetch;
  const originalLocation = window.location;

  beforeEach(() => {
    globalThis.fetch = vi.fn();
    delete (window as unknown as Record<string, unknown>).location;
    window.location = { ...originalLocation, href: '', pathname: '/dashboard' } as Location;
  });

  afterEach(() => {
    globalThis.fetch = originalFetch;
    window.location = originalLocation;
  });

  it('sends request with credentials and JSON headers', async () => {
    (globalThis.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ data: 'test' }),
    });

    await fetchWithCredentials('http://example.com/api');

    expect(globalThis.fetch).toHaveBeenCalledWith('http://example.com/api', {
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
    });
  });

  it('returns JSON on success', async () => {
    (globalThis.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ id: 1, name: 'test' }),
    });

    const result = await fetchWithCredentials('http://example.com/api');
    expect(result).toEqual({ id: 1, name: 'test' });
  });

  it('redirects to login on 401', async () => {
    (globalThis.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
      ok: false,
      status: 401,
      text: () => Promise.resolve('Unauthorized'),
    });

    await expect(fetchWithCredentials('http://example.com/api'))
      .rejects.toThrow(ApiError);
    expect(window.location.href).toBe('/login');
  });

  it('redirects to login on 403', async () => {
    (globalThis.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
      ok: false,
      status: 403,
      text: () => Promise.resolve('Forbidden'),
    });

    await expect(fetchWithCredentials('http://example.com/api'))
      .rejects.toThrow('Session expired');
    expect(window.location.href).toBe('/login');
  });

  it('does NOT redirect when already on login page', async () => {
    window.location = { ...originalLocation, href: '', pathname: '/login' } as Location;

    (globalThis.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
      ok: false,
      status: 401,
      text: () => Promise.resolve('Unauthorized'),
    });

    await expect(fetchWithCredentials('http://example.com/api'))
      .rejects.toThrow(ApiError);
    expect(window.location.href).toBe('');
  });

  it('does NOT redirect when on reset-password page', async () => {
    window.location = { ...originalLocation, href: '', pathname: '/reset-password/abc123' } as Location;

    (globalThis.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
      ok: false,
      status: 401,
      text: () => Promise.resolve('Unauthorized'),
    });

    await expect(fetchWithCredentials('http://example.com/api'))
      .rejects.toThrow(ApiError);
    expect(window.location.href).toBe('');
  });

  it('throws ApiError with status on 500', async () => {
    (globalThis.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
      ok: false,
      status: 500,
      text: () => Promise.resolve('Internal Server Error'),
    });

    try {
      await fetchWithCredentials('http://example.com/api');
      expect.fail('Should have thrown');
    } catch (e) {
      expect(e).toBeInstanceOf(ApiError);
      expect((e as ApiError).status).toBe(500);
    }
  });

  it('parses JSON error response', async () => {
    (globalThis.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
      ok: false,
      status: 400,
      text: () => Promise.resolve(JSON.stringify({ error: 'Invalid input' })),
    });

    try {
      await fetchWithCredentials('http://example.com/api');
      expect.fail('Should have thrown');
    } catch (e) {
      expect((e as ApiError).message).toBe('Invalid input');
      expect((e as ApiError).status).toBe(400);
    }
  });

  it('handles non-JSON error response', async () => {
    (globalThis.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
      ok: false,
      status: 502,
      text: () => Promise.resolve('Bad Gateway'),
    });

    try {
      await fetchWithCredentials('http://example.com/api');
      expect.fail('Should have thrown');
    } catch (e) {
      expect((e as ApiError).message).toBe('Bad Gateway');
    }
  });

  it('merges custom headers with defaults', async () => {
    (globalThis.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({}),
    });

    await fetchWithCredentials('http://example.com/api', {
      headers: { 'X-Custom': 'value' },
    });

    expect(globalThis.fetch).toHaveBeenCalledWith('http://example.com/api', {
      credentials: 'include',
      headers: { 'Content-Type': 'application/json', 'X-Custom': 'value' },
    });
  });
});

describe('API_ENDPOINTS', () => {
  it('has auth endpoints', () => {
    expect(API_ENDPOINTS.AUTH.ME).toContain('/auth/me');
    expect(API_ENDPOINTS.AUTH.LOGIN).toContain('/auth/login');
    expect(API_ENDPOINTS.AUTH.FORGOT_PASSWORD).toContain('/auth/forgot-password');
    expect(API_ENDPOINTS.AUTH.RESET_PASSWORD).toContain('/auth/reset-password');
  });

  it('encodes userId in progress endpoints', () => {
    const url = API_ENDPOINTS.PROGRESS.GET_USER_PROGRESS('user@test.com');
    expect(url).toContain('user%40test.com');
  });

  it('encodes userId in challenge endpoints', () => {
    const url = API_ENDPOINTS.USER_CHALLENGES.START('user@test.com', 5);
    expect(url).toContain('user%40test.com');
    expect(url).toContain('/5/start');
  });
});
