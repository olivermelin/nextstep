# NextStep Production Readiness Analysis — Architect Report

**Date:** 2026-03-26
**Analyst:** Architect Agent
**Scope:** Frontend (React/TypeScript), Backend (Spring Boot/Java), Overall Architecture

---

## 1. Features & Functionality

### 1.1 Feature Map

| Feature | Page/Component | Status | Notes |
|---|---|---|---|
| **Google OAuth Login** | Login.tsx, AuthContext.tsx | COMPLETE | Redirects to Spring Security OAuth2 |
| **Email/Password Auth** | Login.tsx, AuthContext.tsx | COMPLETE | Signup, login, forgot/reset password |
| **Onboarding Flow** | Onboarding.tsx | COMPLETE | 2-track (Consumer/Recovery), 2-4 steps, saves to backend |
| **Dashboard** | Dashboard.tsx | COMPLETE | Greeting, level/XP, daily check-in, coach tip, active challenges |
| **AI Coach Chat** | AIChat.tsx, AICoach.tsx | COMPLETE | Multi-conversation, crisis detection, quota system, suggested challenges |
| **Crisis Detection** | CrisisDetectionService (backend) | COMPLETE | NONE/ELEVATED/CRITICAL flow; CRITICAL blocks LLM call |
| **SOS Button** | SOSButton.tsx | COMPLETE | Fixed button, links to Mind 90101 |
| **Challenge System** | Challenges.tsx + 3 sub-views | COMPLETE | Categories, start/complete, timed activities, YouTube embeds, drawing canvas |
| **Progress Tracking** | Progress.tsx | COMPLETE | Points, level, category progress, achievements, animated counters |
| **Streaks** | StreakCalendar.tsx, StreakBadges.tsx, streakService.ts | COMPLETE | Backend endpoints exist; calendar + badge UI |
| **Daily Check-In** | DailyCheckIn.tsx, checkInService.ts | COMPLETE | 5-point mood scale + optional note |
| **Social: Friends** | Social.tsx | COMPLETE | Add/accept/decline/remove, pending requests |
| **Social: Duels** | Social.tsx | COMPLETE | Create/accept/decline/complete, status tracking |
| **Social: League** | Social.tsx | COMPLETE | Leaderboard with tiers (Bronze/Silver/Gold/Diamond) |
| **Settings** | Settings.tsx | COMPLETE | Profile, notifications, dark mode, language, coach personality, delete account |
| **Daily Rewards** | DailyRewardBox.tsx, rewardService.ts | **STUB** | UI is a placeholder ("Kommer snart"). Service returns hardcoded empty data. |
| **Reward Collection** | RewardCollection.tsx, rewardService.ts | **STUB** | Service stubs only; backend endpoints defined in api.ts but service fakes responses |
| **Milestone Celebration** | MilestoneCelebration.tsx | COMPLETE | Confetti animation at streak milestones |
| **Conversation History** | ConversationList.tsx | COMPLETE | List/select/delete sessions |
| **Coach Persona Selector** | CoachPersonaSelector.tsx | COMPLETE | Multiple personality types saved to backend |
| **Sobriety Counter** | SobrietyCounter.tsx | COMPLETE | Days since streak start, i18n |
| **Error Boundary** | ErrorBoundary.tsx | COMPLETE | Catches React errors, shows reload option |
| **i18n** | sv.json, en.json, react-i18next | MOSTLY COMPLETE | See issues below |
| **Password Reset** | ResetPassword.tsx | COMPLETE | Token-based via email |

### 1.2 Half-Finished Features & Stubs

**CRITICAL: Reward System is a Stub**
- File: `frontend/src/services/rewardService.ts` (line 1-2): Comment reads "backend-endpoints implementeras i Sprint 2 / Dessa typer och stubs haller bygget gront under tiden"
- All methods (`getToday`, `getTodayReward`, `claim`, `generateReward`, `getCollection`) return hardcoded empty/null responses
- `DailyRewardBox.tsx` renders a static placeholder card with "Daglig beloning / Kommer snart"
- The Dashboard calls `rewardService.generateReward()` and `rewardService.getTodayReward()` on every load — these silently do nothing
- Backend endpoints are defined in `api.ts` (REWARDS section) but the frontend never calls them

**Hardcoded Swedish strings bypassing i18n:**
- `Onboarding.tsx` line 136: `"Valj ett alternativ for att fortsatta"` (hardcoded error)
- `Onboarding.tsx` lines 264, 265, 283-285: Track selection labels are hardcoded Swedish
- `DailyCheckIn.tsx`: All visible text is hardcoded Swedish (lines 17-22 mood labels, 52-53 toast, 74, 88-89, 125, 144, 147)
- `ErrorBoundary.tsx` lines 32-45: All error text is hardcoded Swedish
- `AIChat.tsx` lines 124-129, 391, 467-468, 624: Quota-related text is hardcoded Swedish
- `SOSButton.tsx` line 9: Label is hardcoded Swedish

### 1.3 Critical User Flows Assessment

| Flow | Verdict | Risk |
|---|---|---|
| Login -> Onboarding -> Dashboard | Works end-to-end | LOW |
| Dashboard -> Challenge -> Complete -> XP earned | Works end-to-end | LOW |
| AI Coach chat -> Crisis detected -> Crisis response | Backend flow is sound | LOW |
| SOS button -> phone call | Works (uses tel: protocol) | LOW |
| Settings -> Save -> Persist | Works end-to-end | LOW |
| Daily Check-In -> Streak update | Works (backend triggers) | LOW |
| Daily Reward -> Claim | **BROKEN** — stub service, nothing happens | HIGH |

### 1.4 MVP Sufficiency

The app has a solid feature set for MVP **except** the reward system is non-functional. All core flows (auth, onboarding, challenges, AI coach, progress, social, crisis handling) are implemented end-to-end. The reward system should either be completed or entirely hidden from the UI before launch.

---

## 2. Performance & Loading

### 2.1 Bundle Size

Build output (Vite 7.3.1 production build):

```
dist/index.html           1.23 kB  (gzip: 0.52 kB)
dist/assets/index.css    73.96 kB  (gzip: 12.28 kB)
dist/assets/index.js    713.90 kB  (gzip: 219.66 kB)
```

**The JS bundle is 714 KB (220 KB gzipped) — exceeds the 500 KB Vite warning threshold.** This is a single monolithic chunk with no code splitting.

### 2.2 No Lazy Loading / Code Splitting

`App.tsx` imports all pages eagerly (lines 12-22):
```typescript
import Onboarding from "./pages/Onboarding";
import Dashboard from "./pages/Dashboard";
import Progress from "./pages/Progress";
// ... all pages imported statically
```

**No `React.lazy()` or dynamic `import()` is used anywhere.** This means every page (AICoach, Social, Challenges, Settings, etc.) is loaded even if the user never visits them.

**Estimated savings from code splitting:**
- `recharts` (~150 KB) — only used by Progress page
- `framer-motion` (~100 KB) — used broadly but heaviest on Progress/AICoach
- `canvas-confetti` (~15 KB) — only used by Confetti/MilestoneCelebration
- AI Coach + ConversationList — only needed on /ai-coach route

### 2.3 Unnecessary Dependencies

No clearly unnecessary dependencies found. All Radix UI primitives correspond to used shadcn/ui components. However:
- `@radix-ui/react-toggle-group` — the `toggle-group.tsx` component was **deleted** (shown in git status as `D frontend/src/components/ui/toggle-group.tsx`) but the dependency remains in `package.json`
- Multiple other deleted UI components (accordion, calendar, carousel, chart, etc.) still have their Radix dependencies — but those were already removed from `package.json` since only used components remain

### 2.4 Memoization

Memoization usage is **inconsistent**:
- **Good:** `Login.tsx` uses `useMemo` for field errors and password strength
- **Good:** `Social.tsx` uses `useCallback` for load functions
- **Good:** `Dashboard.tsx` uses `useCallback` for handlers
- **Missing:** `Progress.tsx` — heavy page with animated counters, category mapping functions, and achievement icon/text lookup — none memoized
- **Missing:** `Challenges.tsx` — category mapping functions and filter functions are recreated every render
- **Missing:** `Settings.tsx` — `handleSave`, `handleLogout`, `handleDeleteAccount` are not wrapped in `useCallback`
- **No React.memo used anywhere** on exported page components or child components like DuelCard, LeagueRow

### 2.5 API Call Patterns

**Positive patterns:**
- Dashboard caches daily coach message in `sessionStorage` (line 113-118) to avoid redundant AI API calls
- Progress page uses `Promise.all` for parallel data fetching (progressService.ts line 11)
- Social page loads data lazily per tab

**Problematic patterns:**
- `Dashboard.tsx` fires 4 separate API calls on mount: `fetchUserProgress`, `fetchActiveChallenges`, `rewardService.getTodayReward`, and the coach message fetch — these are NOT parallelized (`useEffect` dependencies trigger sequentially)
- `Challenges.tsx` makes 3+ API calls on mount (`loadUserChallenges`, `loadAllChallenges`, then `loadChallengesByCategory` when a category is selected), and `loadChallengesByCategory` fetches ALL user challenges and filters client-side (line 111-112) instead of using the backend category endpoint
- **No react-query usage for data fetching** — despite `@tanstack/react-query` being in dependencies and `QueryClientProvider` being in `App.tsx`, ALL data fetching uses raw `fetch`/`useEffect`/`useState` patterns. The entire react-query library is bundled but unused.
- `AIChat.tsx` fetches sessions, then status, then quota on mount — three sequential waterfall calls

### 2.6 QueryClient Configuration

```typescript
const queryClient = new QueryClient(); // Line 25 of App.tsx
```

Default configuration with no `staleTime`, `cacheTime`, or retry configuration. But as noted, react-query is not actually used for any queries — it's dead weight in the bundle.

---

## 3. Security & Authentication

### 3.1 Authentication Architecture

- **Session-based auth** via Spring Security (cookies with `credentials: "include"`)
- **OAuth2** (Google) + email/password dual support
- **SecurityUtils.verifyUserAccess()** is called in every controller method — this is good IDOR protection
- Password encoding: `BCryptPasswordEncoder` via `PasswordEncoderConfig.java`
- No JWT tokens — pure session cookies

### 3.2 CSRF Protection

**MAJOR ISSUE:** CSRF protection is effectively disabled for most of the application.

`SecurityConfig.java` lines 58-75 — CSRF is ignored for:
- `/api/coach/**`, `/api/onboarding/**`, `/api/user-challenges/**`, `/api/settings/**`, `/api/progress/**`, `/api/checkins/**`, `/api/streaks/**`, `/api/rewards/**`, `/api/social/**`

This means CSRF is only active for auth endpoints. The frontend does **not** read or send the XSRF-TOKEN cookie (grep confirms zero references to XSRF/csrf in frontend code). Combined with session-based auth using cookies, this makes all state-changing operations (completing challenges, sending messages, modifying settings, social actions) vulnerable to CSRF attacks.

### 3.3 Route Protection

Frontend route guarding is implemented via `AppContent` in `App.tsx`:
- Unauthenticated users see only Login and ResetPassword routes (lines 102-107)
- Authenticated users are redirected to onboarding if not completed (lines 83-84)
- Authenticated users cannot access `/login` (redirected to dashboard, line 96)
- **Good:** No ProtectedRoute wrapper needed — the bifurcation in `AppContent` handles it cleanly

### 3.4 API Key Security

- **Good:** All API keys (Anthropic, Groq, Google OAuth, mail) are loaded from environment variables via `${VARIABLE:default}`
- **Good:** No API keys or secrets in source code
- **Good:** No `console.log` statements in production code (only in test files)
- `application.yaml` uses `${ANTHROPIC_API_KEY:}` with empty default — safe

### 3.5 Rate Limiting

`RateLimitingFilter.java` implements IP-based rate limiting with Bucket4j:
- Coach endpoints: 20 req/min
- Auth endpoints: 10 req/min
- General API: 100 req/min
- LRU cache with 10,000 max entries per bucket type to prevent memory leaks
- **Good:** This protects against basic abuse

### 3.6 npm audit Results

```
4 vulnerabilities (2 moderate, 2 high)
- ajv <6.14.0: ReDoS (moderate) — dev dependency
- flatted <=3.4.1: DoS + prototype pollution (high) — dev dependency
- picomatch <=2.3.1: ReDoS + method injection (high) — dev dependency (vite/vitest)
- yaml 2.0.0-2.8.2: stack overflow (moderate) — dev dependency
```

All vulnerabilities are in **dev dependencies** (build/test tools). None affect the production bundle. All are fixable via `npm audit fix`.

### 3.7 CORS Configuration

```java
config.setAllowedOrigins(List.of(frontendBaseUrl)); // Single origin
config.setAllowCredentials(true);
```

**Good:** CORS is restricted to the configured frontend URL only. Not using wildcards.

### 3.8 Security Headers

- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `X-Content-Type-Options` configured
- Cache-Control configured

### 3.9 Sensitive Data Exposure

- `dangerouslySetInnerHTML` used in `Settings.tsx` line 469 for the delete confirmation prompt. The value comes from i18n translations, not user input, so the XSS risk is minimal but the pattern is concerning.
- User email is passed as `userId` in URL query parameters throughout the app (e.g., `/api/social/friends?userId=user@email.com`). This exposes PII in server access logs, browser history, and referrer headers.

### 3.10 Database Security

- `spring.jpa.hibernate.ddl-auto=update` — **NOT suitable for production.** Schema migrations should use Flyway or Liquibase. `update` can cause data loss or unexpected schema changes.

---

## 4. Future Mobile App Perspective (React Native Migration)

### 4.1 Business Logic Separation

**Mixed — needs refactoring for mobile.**

Services layer (`/src/services/`) is cleanly separated from UI — this is the strongest point for migration. All API calls are centralized in service files with proper TypeScript interfaces.

However:
- `fetchWithCredentials` in `api.ts` uses `window.location.href` for auth redirect (line 157) — not portable
- `api.ts` uses `import.meta.env.VITE_API_URL` — Vite-specific

### 4.2 Web-Specific API Usage

| File | Web API | Migration Impact |
|---|---|---|
| `api.ts` | `window.location.href/pathname` | Must replace with RN navigation |
| `AuthContext.tsx` | `window.location.href` (login redirect, line 93), `window.location.search` (OAuth check, line 40) | Must replace with RN deep linking |
| `Settings.tsx` | `document.documentElement`, `localStorage` (theme) | Must replace with AsyncStorage |
| `animations.ts` | `window.matchMedia` | Must use RN Appearance API |
| `useActivityTimer.ts` | `sessionStorage` | Must replace with AsyncStorage |
| `useYouTubeProgress.ts` | `window.YT`, `document.createElement("script")`, `document.head.appendChild` | Cannot port — needs WebView or RN YouTube library |
| `DrawingCanvas.tsx` | HTML5 `<canvas>` element | Must replace with react-native-canvas or react-native-sketch |
| `i18n/index.ts` | `localStorage`, `navigator` (language detection) | Must replace with RN i18n detector |
| `SobrietyCounter.tsx` | `Date.toLocaleDateString("sv-SE")` | Works in RN, but should verify |
| `ErrorBoundary.tsx` | `window.location.href` | Must replace with RN navigation |
| `challengeUtils.ts` | `window.location.origin` | Must replace |
| `Progress.tsx` (line 367) | `navigator.share` | Must use RN Share API |

### 4.3 Component Architecture

**Good patterns for migration:**
- Components are function-based (no class components except ErrorBoundary)
- Consistent use of TypeScript interfaces
- shadcn/ui components are thin wrappers — easy to swap for RN equivalents
- i18n is implemented project-wide via react-i18next (which supports RN)
- State management is Context-based (works in RN)

**Problematic patterns:**
- Tailwind CSS classes are embedded in every component — every single component needs restyled for RN (NativeWind is an option but still requires work)
- `framer-motion` is used heavily (Progress, AIChat, Navigation, Challenges, Onboarding, DailyCheckIn) — must replace with `react-native-reanimated`
- No shared business logic layer — hooks and services would need to be extracted into a shared package
- Drawing canvas is deeply web-specific

### 4.4 Recommended Migration Strategy

1. **Extract services + types into a shared package** (already well-structured)
2. **Replace `fetchWithCredentials`** with a platform-agnostic HTTP client (axios/ky)
3. **Use NativeWind** for Tailwind-compatible styling in RN
4. **Replace framer-motion** with react-native-reanimated (animation patterns are simple enough)
5. **Web-specific hooks** (useYouTubeProgress, useActivityTimer) need RN equivalents
6. **DrawingCanvas** needs a complete rewrite with react-native-skia or similar

### 4.5 Migration Effort Estimate

- **Services/types layer:** LOW effort (mostly portable)
- **Context/state:** LOW effort (React context works in RN)
- **Pages/components:** HIGH effort (every component needs restyling)
- **Web-specific features (Canvas, YouTube):** HIGH effort (complete rewrites)
- **Overall:** Estimated 40-60% code reuse for logic, near-zero for UI components

---

## 5. Summary of Critical Issues

### Blockers (Must fix before production)

| # | Issue | Severity | Location |
|---|---|---|---|
| B1 | **CSRF effectively disabled** for all API endpoints except auth | CRITICAL | SecurityConfig.java:58-75 |
| B2 | **`ddl-auto=update`** in production config | CRITICAL | application.yaml:11 |
| B3 | **Reward system is a stub** — UI calls non-functional service | HIGH | rewardService.ts, DailyRewardBox.tsx |
| B4 | **No code splitting** — 714 KB single JS bundle | HIGH | App.tsx (all eager imports) |
| B5 | **react-query bundled but unused** — dead weight (~40 KB gzipped) | MEDIUM | package.json, App.tsx |

### Important (Should fix before production)

| # | Issue | Severity | Location |
|---|---|---|---|
| I1 | Hardcoded Swedish strings bypassing i18n in ~6 components | MEDIUM | DailyCheckIn, Onboarding, ErrorBoundary, AIChat, SOSButton |
| I2 | User email exposed in URL query parameters across all API calls | MEDIUM | api.ts, all services |
| I3 | Frontend does not send XSRF-TOKEN cookie even where CSRF is enabled | MEDIUM | No CSRF header in fetchWithCredentials |
| I4 | Missing memoization on Progress, Challenges, Settings pages | LOW | See section 2.4 |
| I5 | Dashboard API calls are not parallelized | LOW | Dashboard.tsx |
| I6 | npm audit shows 4 vulnerabilities in dev dependencies | LOW | package-lock.json |
| I7 | `dangerouslySetInnerHTML` usage in Settings.tsx | LOW | Settings.tsx:469 |
| I8 | Browserslist data 9 months out of date | LOW | Build warning |

---

## 6. GO / NO-GO Vote

### **VOTE: CONDITIONAL GO**

**Argumentation:**

The application is architecturally sound and feature-rich for an MVP. The core user flows (authentication, onboarding, challenges, AI coaching, crisis handling, social features) are fully implemented end-to-end. Backend security is well-structured with consistent `verifyUserAccess` calls on every endpoint, proper rate limiting, environment-variable-based secrets, and restricted CORS.

However, three issues must be resolved before production:

1. **CSRF must be properly implemented or the exemptions must be justified.** Since this is a session-cookie-based app, disabling CSRF on all data-modification endpoints is a genuine security vulnerability. Either enable CSRF with proper token handling in the frontend, or switch to token-based (JWT) auth where CSRF is less relevant.

2. **`ddl-auto=update` must be replaced** with a proper migration tool (Flyway/Liquibase) before any production deployment. This is a data safety issue.

3. **The reward system stub must either be completed or fully hidden from the UI.** Currently, the Dashboard calls dead service methods and displays a "Kommer snart" placeholder, which is not production-ready.

After those three items are addressed, the bundle-size issue (code splitting) should be tackled for acceptable load times on mobile networks. The remaining issues are quality-of-life improvements that can be addressed post-launch.

The React Native migration path is viable but will require significant effort on the UI layer. The services and business logic layer is well-positioned for sharing.

---

*Report generated by Architect Agent, 2026-03-26*
