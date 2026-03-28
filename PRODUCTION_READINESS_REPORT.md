# nextstep — Production Readiness Report

**Datum:** 2026-03-27
**Genomford av:** Arkitekt, Designer, Developer, Testare

---

## Teamvote

| Agent      | Rost           | Kortmotivering                                                                 |
|------------|----------------|--------------------------------------------------------------------------------|
| Arkitekt   | CONDITIONAL GO | Arkitektoniskt solid MVP — CSRF, ddl-auto och reward-stub maste fixas forst    |
| Designer   | NO-GO          | SOS-brister, i18n-brott i kriskritiska komponenter och synliga stubs blockerar  |
| Developer  | CONDITIONAL GO | Kodbasen ar valstrukturerad, 9 avgrnsade blockers maste atgardas              |
| Testare    | NO-GO          | Onboarding otillgangligt for tangentbord/skarmlasar, kris-UX bristfallligt     |

**Samlat beslut: NO-GO** ❌

Tva agenter rostar NO-GO och de tva som rostar CONDITIONAL GO kraver samma kritiska atgarder som forutsattning. Konsensus ar tydlig: **appen ar nara produktion men har avgrnsade, fixbara blockers**. Inga arkitekturomskrivningar kravs — alla problem ar avgransade och kan sprintas pa direkt.

---

## Sammanlagd atgardslista

### Kritiska blockers (maste atgardas innan lansering)

| # | Atgard | Ansvarig | Estimat |
|---|--------|----------|---------|
| 1 | **CSRF-skydd:** Aktivera CSRF med token-hantering i frontend eller motivera dispens per endpoint | Arkitekt + Developer | 1-2 dagar |
| 2 | **ddl-auto=update → Flyway/Liquibase:** Implementera databasmigrering | Arkitekt + Developer | 1 dag |
| 3 | **SOS-knappen: window.open → tel:-lank:** Byt till `<a href="tel:90101">` eller `window.location.href` for att undvika popup-blockering | Developer | 10 minuter |
| 4 | **SOS-knappen globalt:** Flytta fran Dashboard till App.tsx (authenticated-blocket) | Developer | 15 minuter |
| 5 | **i18n — kriskritiska komponenter:** Internationalisera SOSButton, DailyCheckIn, Onboarding steg 1 | Developer | 3-4 timmar |
| 6 | **i18n — ovriga komponenter:** ConversationList, ErrorBoundary, CoachPersonaSelector, AIChat kvottexter, AuthContext | Developer | 2-3 timmar |
| 7 | **Ta bort/implementera stubs:** DailyRewardBox (ta bort fran Dashboard) och MilestoneCelebration (implementera eller ta bort) | Developer | 15 min (ta bort) / 4-6h (implementera) |
| 8 | **Tangentbordstillganglighet:** Lagg till `role="button"`, `tabIndex={0}`, `onKeyDown` pa alla klickbara Card-element (Onboarding, CategoriesView) | Developer | 3-4 timmar |
| 9 | **ConversationList fokushantering:** Focus trap, `role="dialog"`, `aria-modal`, Escape-stangning | Developer | 2-3 timmar |

**Estimerad total for kritiska atgarder: 4-5 utvecklardagar** (om stubs tas bort istallet for implementeras)

---

### Hog prioritet (bor atgardas innan lansering)

| # | Atgard | Ansvarig | Estimat |
|---|--------|----------|---------|
| 10 | **Touch-targets:** Oka till minst 44x44px pa header settings-knapp, kris-bannerns telefonlankar, van-knappar, konversationslist-stang | Developer | 1-2 timmar |
| 11 | **Felhantering:** Ersatt 32 tomma `catch {}` med anvandarvandiga felmeddelanden | Developer | 1-2 dagar |
| 12 | **ARIA-labels:** AIChat skicka-knapp, chatinput, Progress dela-knapp, Social van-knappar | Developer | 1 timme |
| 13 | **`<html lang>` dynamisk:** Uppdatera baserat pa valt sprak | Developer | 30 minuter |
| 14 | **Koddelning:** `React.lazy` + `Suspense` pa route-niva (714 KB bundle) | Developer | 0.5-1 dag |
| 15 | **Frontend-tester:** Grundlaggande tester for krisfloden och auth-flodet | Testare | 2-3 dagar |
| 16 | **ConversationList delete-bugg:** Prop `onDeleteSession` deklareras inte i interface — session-radering ar bruten | Developer | 30 minuter |

**Estimerad total for hoga: 5-7 utvecklardagar**

---

### Medium prioritet (kan atgardas post-launch)

| # | Atgard | Ansvarig | Estimat |
|---|--------|----------|---------|
| 17 | TypeScript strict mode (`strict: true`) — avsloja dolda buggar | Developer | 2-3 dagar |
| 18 | Ta bort oanvand react-query (~40 KB gzipped dead weight) eller borja anvand den | Arkitekt | 2-4 timmar |
| 19 | Memoization (useMemo/useCallback) pa Progress, Challenges, Settings | Developer | 1 dag |
| 20 | AbortController pa alla fetch-effekter for att undvika minneslackor | Developer | 1 dag |
| 21 | Parallellisera Dashboard API-anrop | Developer | 2-3 timmar |
| 22 | `aria-live="polite"` pa AIChat-meddelandelistan | Developer | 1 timme |
| 23 | Touch-events pa DrawingCanvas (onTouchStart/Move/End) | Developer | 1-2 timmar |
| 24 | npm audit — 4 sarbarheter i dev-dependencies | Developer | 1-2 timmar |
| 25 | Uppdatera browserslist-data | Developer | 5 minuter |

---

## Mobilapp-forberedelse — sammanfattning

### Redan val forberett
- **Services-lager** ar rent separerat fran UI — alla API-anrop centraliserade i `/src/services/`
- **TypeScript-interfaces** ar konsekvent definierade — kan delas direkt
- **i18n via react-i18next** mappar direkt till React Native
- **Context-baserad state** fungerar identiskt i React Native
- **Komponentarkitekturen** ar moduler — varje sida ar sjalvstandig

### Arkitektoniska justeringar att gora nu (eller infir migration)
1. **Extrahera services + types till ett delat paket** — redan valstrukturerat for detta
2. **Byt `fetchWithCredentials`** fran `window.location`-baserad redirect till en plattformsoberoende HTTP-klient
3. **Isolera web-specifika beroenden:** `window.open`, `navigator.share`, Canvas API, YouTube iframes, `sessionStorage`, `localStorage` finns inbakade i delade hooks/komponenter

### UX-monster att adressera
- **Hover-states som enda feedback** (`card-hover`, `hover:bg-primary/10`) — behover active/pressed-states for touch
- **`backdrop-blur-xl`** glassmorphism — prestandakravande pa aldre Android, behover solid fallback
- **Fixed positioning** (SOS-knapp, bottom nav) — beter sig annorlunda med nativa tangentbord och safe areas
- **Horisontell scroll** pa AI Coach quick prompts — behover scroll-snap eller paginerad karusell

### Migrationsinsats (uppskattning)
- **Services/types:** LAG insats (mestadels portabelt)
- **Context/state:** LAG insats
- **Sidor/komponenter:** HOG insats (varje komponent behover ny styling — NativeWind rekommenderas)
- **Web-specifika features (Canvas, YouTube):** HOG insats (kompletta omskrivningar)
- **Totalt:** Uppskattningsvis 40-60% kodaterbruk for logik, nara noll for UI-komponenter

---

## Slutsats

**Appen ar 85% redo for produktion.** Arkitekturen ar solid, feature-setet ar komplett for MVP, krisdetekterings-pipelinen fungerar korrekt end-to-end, och den visuella designen ar polerad.

De 9 kritiska blockerna ar avgransade och valmotiverade — 4-5 utvecklardagar racker for att na GO. Rekommenderad ordning:

1. **Dag 1-2:** CSRF + Flyway (backend-sakerhet)
2. **Dag 2-3:** SOS-fix + i18n (kriskritiskt)
3. **Dag 3-4:** Tangentbordstillganglighet + stubs bort (a11y + polish)
4. **Dag 4-5:** Felhantering + tester (stabilitet)

Efter dessa atgarder ar appen redo for en **kontrollerad beta-lansering**.

---

*Rapport sammanstelld av Team Lead, 2026-03-27*
