import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Navigation from '@/components/Navigation';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

const renderNav = (path: string) => {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Navigation />
    </MemoryRouter>
  );
};

describe('Navigation', () => {
  beforeEach(() => {
    mockNavigate.mockClear();
  });

  it('renders all 5 nav items', () => {
    renderNav('/dashboard');
    expect(screen.getAllByRole('button')).toHaveLength(5);
  });

  it('renders nav item labels', () => {
    renderNav('/dashboard');
    expect(screen.getByText('navigation.home')).toBeInTheDocument();
    expect(screen.getByText('navigation.progress')).toBeInTheDocument();
    expect(screen.getByText('navigation.challenges')).toBeInTheDocument();
    expect(screen.getByText('navigation.aiCoach')).toBeInTheDocument();
    expect(screen.getByText('navigation.settings')).toBeInTheDocument();
  });

  it('navigates when clicking a nav item', () => {
    renderNav('/dashboard');
    fireEvent.click(screen.getByText('navigation.challenges'));
    expect(mockNavigate).toHaveBeenCalledWith('/challenges');
  });

  it('hides navigation on root path', () => {
    const { container } = renderNav('/');
    expect(container.querySelector('nav')).toBeNull();
  });

  it('shows navigation on dashboard', () => {
    const { container } = renderNav('/dashboard');
    expect(container.querySelector('nav')).not.toBeNull();
  });
});
