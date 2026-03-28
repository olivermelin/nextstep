# Design & UX Production Readiness Review

**Reviewer:** Designer Agent
**Date:** 2026-03-26
**Scope:** All frontend pages, key components, styling, responsiveness, mobile-readiness

---

## 1. Design & UX

### 1.1 Overall Visual Identity

**Strengths:**
- Cohesive green-based color system (HSL 142) carried through light and dark modes with proper CSS custom properties (`frontend/src/index.css` lines 10-96)
- Professional glassmorphism aesthetic: `backdrop-blur-xl`, `bg-card/80`, semi-transparent borders used consistently across Settings, AICoach, and Login pages
- Time-aware greeting on Dashboard (`frontend/src/pages/Dashboard.tsx` lines 171-185) with matching gradient backgrounds is a thoughtful personal touch
- Custom animation library in `frontend/src/App.css` (fadeInUp, scaleIn, celebratePop) with proper `prefers-reduced-motion` respect (lines 120-144)
- Dark mode fully defined in CSS variables with appropriate adjusted luminosity values

**Issues:**

| ID | Severity | Finding | Location |
|----|----------|---------|----------|
| D-01 | HIGH | MilestoneCelebration is a null stub: `const MilestoneCelebration = (_props) => null`. Users hitting milestones (1, 7, 30, 90 days etc.) get zero feedback. This is a core gamification moment completely missing. | `frontend/src/components/MilestoneCelebration.tsx` line 7 |
| D-02 | HIGH | DailyRewardBox is a stub showing "Kommer snart" at 50% opacity. This dead placeholder sits prominently on the Dashboard sidebar, signaling an unfinished product. | `frontend/src/components/DailyRewardBox.tsx` lines 9-18 |
| D-03 | HIGH | SOSButton text is entirely hardcoded Swedish: `"SOS — Ring hjalp nu (90101)"`. No i18n. For a crisis-critical button, this is a localization blocker. The aria-label is also hardcoded. | `frontend/src/components/SOSButton.tsx` lines 9, 12 |
| D-04 | HIGH | DailyCheckIn has all labels hardcoded in Swedish: mood labels ("Mycket daligt", "Bra", etc.), toast messages ("Incheckning klar!"), placeholder text, button text. None use `t()`. | `frontend/src/components/DailyCheckIn.tsx` lines 17-22, 52-53, 57-58, 89, 125, 147 |
| D-05 | HIGH | Onboarding Step 1 (track selection) has fully hardcoded Swedish text: heading, descriptions, card labels. Other steps use `t()` correctly, making Step 1 inconsistent. | `frontend/src/pages/Onboarding.tsx` lines 245-288 |
| D-06 | MEDIUM | CoachPersonaSelector has all persona names, taglines, and descriptions hardcoded in Swedish. English-speaking users see Swedish persona descriptions in Settings. | `frontend/src/components/CoachPersonaSelector.tsx` lines 15-47 |
| D-07 | MEDIUM | AIChat quota messages are hardcoded Swedish: "Daglig grans uppnadd", "meddelanden kvar idag", "Uppgradera till Premium". | `frontend/src/components/AIChat.tsx` lines 467-468, 624 |
| D-08 | MEDIUM | ConversationList panel has hardcoded text: "Tidigare konversationer", "Ny konversation", "Inga tidigare konversationer", "Konversation" fallback, "meddelanden". | `frontend/src/components/ConversationList.tsx` lines 48, 66, 78, 95, 100 |
| D-09 | MEDIUM | ErrorBoundary has hardcoded Swedish: "Nagot gick fel", "Ett ovantat fel uppstod", "Ladda om". Since this is a class component, using hooks is not straightforward, but a workaround exists. | `frontend/src/components/ErrorBoundary.tsx` lines 32-44 |
| D-10 | LOW | AIChat difficulty labels are hardcoded: "Latt", "Medel", "Svar" in `getDifficultyLabel` and `getDifficultyColor` functions. | `frontend/src/components/AIChat.tsx` lines 124-131 |

### 1.2 User Flow & Navigation

**Strengths:**
- Clean bottom navigation with 5 items (Home, Progress, Challenges, Social, AI Coach) using animated active indicator via framer-motion layoutId (`frontend/src/components/Navigation.tsx` lines 48-54)
- Proper `aria-label`, `aria-current`, and `role="navigation"` attributes on nav bar
- Settings accessible via header icon, keeping the bottom bar focused on core features
- Challenge flow is well-structured: Categories -> Challenge List -> Activity View with clear back navigation at each level

**Issues:**

| ID | Severity | Finding | Location |
|----|----------|---------|----------|
| D-11 | HIGH | All unauthenticated routes redirect to `/login` including true 404 paths. Users who mistype a URL get silently redirected to login with no indication of what went wrong. The NotFound page is unreachable for logged-out users. | `frontend/src/App.tsx` line 106 |
| D-12 | MEDIUM | The header always shows "NextStep" title even on authenticated pages. There is no per-page title or breadcrumb to orient the user, especially in deep challenge flows. | `frontend/src/App.tsx` line 49 |
| D-13 | MEDIUM | The Settings gear icon in the header has `p-2` padding giving roughly a 36x36px touch target (20px icon + 2*8px padding). Below the 44x44px minimum per Apple HIG. | `frontend/src/App.tsx` lines 66-72 |
| D-14 | LOW | No visible loading/skeleton state for the Dashboard when `!user` (pre-auth check). Just a plain "Laddar..." text without the branded skeleton pattern used elsewhere. | `frontend/src/pages/Dashboard.tsx` lines 188-193 |

### 1.3 Empty States & Error Handling

**Strengths:**
- Dashboard shows a proper empty state for no active challenges with icon, message, and CTA button to browse challenges (`Dashboard.tsx` lines 243-251)
- Social page has empty states for all three tabs (friends, duels, league) with relevant icons
- Error alerts use the destructive variant consistently
- Skeleton components exist for Dashboard, Progress, Settings, and Challenges

**Issues:**

| ID | Severity | Finding | Location |
|----|----------|---------|----------|
| D-15 | MEDIUM | Silent error swallowing throughout: at least 12 `catch {}` blocks across Dashboard, Challenges, Social, Progress pages that fail silently with no user feedback. If the API is down, users see empty content with no explanation. | Multiple files, e.g., `Dashboard.tsx` lines 89-91, `Social.tsx` lines 80-84 |
| D-16 | MEDIUM | AIChat shows an empty conversation state with "Start a conversation" but if the backend is unreachable, the status indicator just shows a spinning loader indefinitely with "Ansluter..." text. No timeout or retry mechanism. | `frontend/src/components/AIChat.tsx` lines 431-436 |
| D-17 | LOW | The coach message fallback on Dashboard randomly picks from an array of messages, which could show the same message repeatedly. | `frontend/src/pages/Dashboard.tsx` lines 134-135 |

### 1.4 Gamification & Motivation

**Strengths:**
- XP system with levels and progress bar clearly displayed on Dashboard sidebar
- Challenge completion has a thoughtful time-picker with XP preview showing how duration affects rewards
- Streak system with badges and calendar visualization on Progress page
- Animated counters (framer-motion `useMotionValue`) for stats on Progress page
- Achievement cards with share functionality via Web Share API
- Sobriety counter with milestone detection for key day counts (1, 7, 14, 30, 60, 90, 180, 365)

**Issues:**

| ID | Severity | Finding | Location |
|----|----------|---------|----------|
| D-18 | HIGH | No celebration or feedback when milestones are reached because MilestoneCelebration is a null stub. The detection logic exists but the UI does not. | `frontend/src/components/MilestoneCelebration.tsx` |
| D-19 | MEDIUM | The DailyRewardBox "mystery box" placeholder creates expectation that is never fulfilled. Better to remove it entirely than show a permanently disabled feature. | `frontend/src/components/DailyRewardBox.tsx` |
| D-20 | LOW | Achievement share button only renders when `navigator.share` is available. No fallback for desktop browsers that lack the Web Share API (e.g., Firefox, older Chrome). | `frontend/src/pages/Progress.tsx` lines 367-385 |

### 1.5 Crisis UX (Critical for this domain)

**Strengths:**
- SOS button uses `variant="destructive"` with phone icon for high visibility
- AIChat has a persistent CrisisBanner at CRITICAL level with direct call links to Mind (90101) and SOS (112)
- Crisis bubble styling differentiates ELEVATED (orange border) and CRITICAL (red border) messages
- The crisis banner links use `<a href="tel:...">` for direct phone dialing

**Issues:**

| ID | Severity | Finding | Location |
|----|----------|---------|----------|
| D-21 | HIGH | SOSButton uses `window.open("tel:90101")` which may be blocked by popup blockers. Should use `window.location.href = "tel:90101"` or an `<a>` element instead. A crisis user being blocked from calling for help is a critical UX failure. | `frontend/src/components/SOSButton.tsx` line 8 |
| D-22 | HIGH | SOSButton is positioned `fixed bottom-20 right-4`. On mobile, this overlaps with content and can be accidentally tapped, or worse, be obscured by content. It also competes visually with the bottom navigation bar at `bottom-0`. The 16px gap between them is tight. | `frontend/src/components/SOSButton.tsx` line 7 |
| D-23 | MEDIUM | SOS button is ONLY shown on the Dashboard page. Users in crisis may be on any page (AI Coach, Settings, etc.). The SOS button should be globally accessible. | `frontend/src/pages/Dashboard.tsx` line 316 |
| D-24 | MEDIUM | The CrisisBanner phone links have small touch targets: `px-3 py-1.5 text-xs` gives approximately 60x28px. While width is fine, the 28px height is below the 44px minimum for a crisis-critical element. | `frontend/src/components/AIChat.tsx` lines 47-54 |

---

## 2. Responsiveness (Mobile Adaptation)

### 2.1 Test Results by Viewport

**375px (iPhone SE):**
- Login page: Renders well, all buttons fully visible without scrolling, card has appropriate padding
- Signup form: All three inputs and submit button visible above fold on 812px height
- Google button: 58px height (confirmed via inspection), well above 44px minimum
- Form inputs: 54px height, 16px font-size (confirmed) - no iOS zoom trigger (good)
- Reset Password page: Clean layout, all elements visible

**390px (iPhone 14):**
- Login page: Slightly more breathing room, proportionally identical to 375px
- No layout breaks observed

**768px (Tablet):**
- Login page: Card centered, good use of max-w-md constraint
- Large amount of vertical whitespace above card - could feel empty on tablet

**1280px (Desktop):**
- Login page: Well centered with footer visible, clean layout
- Dashboard: 3-column grid (lg:grid-cols-3) activates properly
- AICoach: Side panel visible at lg breakpoint with quick-action buttons
- Progress: 2-column layout (lg:grid-cols-2) for categories + achievements

### 2.2 Layout & Spacing Issues

| ID | Severity | Finding | Location |
|----|----------|---------|----------|
| R-01 | HIGH | Navigation bar `h-16` (64px) is fixed at bottom. Main content has `pb-24` (96px). This creates 32px of dead space below all page content. On small screens this wastes valuable vertical space. | `frontend/src/App.tsx` line 79, `Navigation.tsx` line 30 |
| R-02 | HIGH | AICoach page uses `h-full` for the main container and `min-h-[500px]` on the chat container. On short mobile screens (e.g., iPhone SE in landscape, or with keyboard open), the 500px minimum forces vertical overflow and the input may be pushed offscreen. | `frontend/src/pages/AICoach.tsx` lines 25, 124 |
| R-03 | MEDIUM | The Social page tab bar at 375px fits all 3 tabs but the text becomes cramped. The pending request count badge adds to the width. With longer translated tab labels, this could overflow. | `frontend/src/pages/Social.tsx` lines 226-249 |
| R-04 | MEDIUM | Progress page stats grid uses `grid-cols-3` unconditionally. On 375px, each stat card is approximately 107px wide. With `text-2xl` (24px) numbers and `text-xs` labels, this is tight but readable. However, long translated labels (e.g., German) would overflow. | `frontend/src/pages/Progress.tsx` line 219 |
| R-05 | MEDIUM | The Onboarding page uses `items-start` which pushes the card to the top. On desktop, this looks awkward with large empty space below. Should use `items-center` at md+ breakpoints. | `frontend/src/pages/Onboarding.tsx` line 198 |
| R-06 | LOW | AICoach mobile quick-start buttons use `overflow-x-auto` horizontal scrolling. There is no visual affordance (gradient fade, scroll indicator) to hint that more buttons exist off-screen. | `frontend/src/pages/AICoach.tsx` line 110 |

### 2.3 Touch Target Analysis

| Element | Measured Size | Minimum (Apple HIG) | Status |
|---------|--------------|---------------------|--------|
| Google login button | 58px height | 44px | PASS |
| Email/password inputs | 54px height | 44px | PASS |
| Submit buttons (login/signup) | 56px height | 44px | PASS |
| Bottom nav items | ~48x64px (px-3, h-16 container) | 44x44px | PASS |
| Settings gear (header) | ~36x36px (p-2 + 20px icon) | 44x44px | FAIL |
| SOS button | ~48px height (py-3 + text) | 44px | PASS |
| CrisisBanner phone links | ~60x28px | 44x44px | FAIL (height) |
| Social tab buttons | full width / ~30px height area | 44px | BORDERLINE |
| DailyCheckIn mood buttons | flex-1, ~60x60px | 44x44px | PASS |
| Friend accept/decline circles | 32x32px (h-8 w-8) | 44x44px | FAIL |
| Conversation list close button | ~28x28px (p-1 + 16px icon) | 44x44px | FAIL |
| Challenge time preset buttons | ~100x60px | 44x44px | PASS |

### 2.4 Typography at Mobile Sizes

- Body text consistently uses `text-sm` (14px) which is readable on mobile
- Page headings use `text-3xl` (30px) on mobile - slightly large, but acceptable
- The `text-[10px]` used on nav labels and some meta text is below the recommended 11px minimum for mobile readability
- Form labels use `text-sm` (14px) which is appropriate
- Input font-size is 16px, preventing iOS auto-zoom (correct)

---

## 3. Future Mobile App Perspective

### 3.1 Patterns That Need Adaptation

| ID | Pattern | Issue for Native | Recommendation |
|----|---------|-----------------|----------------|
| M-01 | `card-hover` CSS class with `translateY(-2px)` on hover | No hover state on mobile - these interactions are invisible on touch devices | Replace with `whileTap` scale animation (already used in some places via framer-motion) |
| M-02 | `hover:bg-primary/10` and similar hover-only color changes on quick-prompt buttons, friend cards, conversation items | States only visible on desktop | Add active/pressed states with `:active` pseudo-class or framer-motion `whileTap` |
| M-03 | `overflow-x-auto` horizontal scrolling on AICoach quick prompts | Horizontal scroll lists need snap points and haptic feedback on native | Use `scroll-snap-type: x mandatory` and consider a paginated carousel for React Native |
| M-04 | `window.open("tel:90101")` for SOS | Works on mobile web but needs deep linking in React Native | Use `Linking.openURL()` in React Native with fallback |
| M-05 | `navigator.share` API for achievement sharing | Not available in React Native WebView | Use React Native Share API instead |
| M-06 | `backdrop-blur-xl` glassmorphism effect | Performance-intensive on older Android devices | Use solid backgrounds as fallback, test on low-end devices |
| M-07 | `sessionStorage` for caching coach messages | Not persistent across app restarts on native | Use AsyncStorage or MMKV in React Native |
| M-08 | YouTube iframe embeds in challenges | iframes don't work in React Native | Use `react-native-youtube-iframe` or similar native player |
| M-09 | DrawingCanvas component (HTMLCanvasElement) | Canvas API differs in React Native | Use `react-native-svg` or `react-native-canvas` |
| M-10 | Fixed-position SOS button and bottom nav | Fixed positioning behaves differently with native keyboards and safe areas | Use React Native SafeAreaView and keyboard-aware positioning |

### 3.2 Positive Patterns for Migration

- Component architecture is clean and modular - each page is self-contained
- All API calls are centralized in `/src/services/` - easy to swap transport layer
- i18n via react-i18next maps directly to `react-native-i18next` or `expo-localization`
- framer-motion animations can be replaced with `react-native-reanimated` (similar API concepts)
- Tailwind classes map well to NativeWind for React Native
- State management via `@tanstack/react-query` works identically in React Native

---

## 4. Summary of Critical Findings

### Blockers (must fix before production)

1. **Hardcoded Swedish in crisis-critical components** (D-03, D-04): SOSButton and DailyCheckIn are completely un-internationalized. For a bilingual app, crisis text must work in both languages.
2. **SOS button uses `window.open` which can be popup-blocked** (D-21): A user in crisis unable to reach help is an unacceptable failure mode.
3. **SOS button only on Dashboard** (D-23): Crisis can happen on any page.
4. **MilestoneCelebration is a null stub** (D-01, D-18): Core gamification feature with detection logic wired but zero UI. This is a broken promise to the user.
5. **DailyRewardBox placeholder visible to users** (D-02, D-19): "Kommer snart" at 50% opacity on the main dashboard signals an unfinished product.
6. **Onboarding Step 1 hardcoded Swedish** (D-05): First impression for new English-speaking users is entirely in Swedish.

### High Priority (should fix before production)

7. **Touch targets below 44px minimum** on several interactive elements (D-13, D-24, friend accept/decline buttons, conversation list close button)
8. **All unauthenticated 404s redirect silently to login** (D-11)
9. **AICoach min-height causes overflow on small screens** (R-02)
10. **Silent error handling in 12+ API calls** (D-15)

### Medium Priority (fix soon after launch)

11. CoachPersonaSelector, ConversationList, AIChat quota strings need i18n (D-06, D-07, D-08)
12. ErrorBoundary hardcoded text (D-09)
13. Navigation dead space from pb-24 vs h-16 nav (R-01)
14. No breadcrumb or per-page title in header (D-12)

---

## 5. GO / NO-GO Vote

### Vote: NO-GO

**Argumentation:**

The visual design quality is high - the color system, glassmorphism aesthetic, animations, and overall layout are polished and appropriate for the target audience. The component architecture is sound and well-prepared for a future React Native migration.

However, there are six specific blockers that prevent a production launch:

1. **Safety-critical UX failure:** The SOS button using `window.open` can be blocked by browsers, and it only appears on the Dashboard. For an app serving users with substance dependency and crisis potential, this is unacceptable. A user on the AI Coach page experiencing a crisis has no SOS access.

2. **Broken i18n contract:** The app officially supports Swedish and English (settings page has a language switcher, locale files exist for both). But SOSButton, DailyCheckIn, Onboarding Step 1, CoachPersonaSelector, ConversationList, and ErrorBoundary all have hardcoded Swedish text. An English-speaking user encounters Swedish crisis text, mood labels, onboarding questions, and error messages. This breaks the bilingual promise.

3. **Visible stub features:** Two placeholder components (MilestoneCelebration returning null, DailyRewardBox showing "Kommer snart") are wired into the production Dashboard. The milestone detection runs but produces no output. The reward box takes up sidebar space while being disabled. These erode user trust.

4. **Touch target failures:** Four interactive elements fall below the 44px Apple HIG minimum, including the header settings button and crisis banner phone links. These are particularly problematic given the target audience may be in stressed states.

**Estimated effort to unblock:** 3-5 developer days to fix all six blockers. The i18n fixes are mostly mechanical (wrapping strings in `t()` calls and adding translation keys). The SOS and milestone issues require focused but bounded work.

**Recommendation:** Fix the six blockers, then the app is ready for a controlled beta launch. The design foundation is strong.
