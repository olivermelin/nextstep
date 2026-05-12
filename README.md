# NextStep — Plattform för personlig återhämtning och utveckling

NextStep är en AI-driven plattform som stöder människor i återhämtning och personlig utveckling. Plattformen kombinerar AI-coaching, gamification, krishantering och sociala funktioner för att skapa en helhetslösning anpassad för känsliga användarbehov.

---

## Innehåll

- [Funktioner](#funktioner)
- [Tech Stack](#tech-stack)
- [Kom igång](#kom-igång)
- [Miljövariabler](#miljövariabler)
- [API-översikt](#api-översikt)
- [Arkitektur](#arkitektur)
- [Säkerhet & GDPR](#säkerhet--gdpr)
- [Testning](#testning)
- [Deployment](#deployment)
- [Agent Team (AI-assisterad utveckling)](#agent-team-ai-assisterad-utveckling)

---

## Funktioner

### AI-coaching
- Personlig coach driven av **Claude (Anthropic)** med Groq som fallback
- Valbara coachpersonligheter anpassade till användarens behov
- Automatisk krisdetektering — kritiska meddelanden hanteras utan LLM-anrop
- Nödknapp (SOS) med direktlänk till Mind 90101 och nödkontakter

### Gamification & Motivation
- Streak-tracking med visuell kalender och varningssystem
- Dagliga belöningar, XP och collectibles
- Achievement-system med upplåsbara badges
- Ligor och dueller mot andra användare

### Dagliga verktyg
- Daglig incheckning (stämningslogg, välmående)
- Dagliga ritualer och progressspårning
- Personliga mål och kategoribaserade utmaningar
- Humörtrend-visualisering med Recharts

### Socialt
- Aktivitetsflöde med reaktioner (cheers)
- Vänner och vänskapsförfrågningar
- Anonyma dueller och ligasystem

### Teknik & Tillgänglighet
- Fullständigt flerspråkigt: **svenska & engelska** (i18next)
- **PWA-stöd** — installeras som app, fungerar offline
- Dark mode i alla komponenter
- WCAG 2.1 AA-tillgänglighet

---

## Tech Stack

| Lager | Teknologi |
|---|---|
| Backend | Java 25, Spring Boot 4.0, Spring Security, JPA/Hibernate |
| Databas | PostgreSQL 17, Flyway (migrationer) |
| AI | Anthropic Claude Haiku (primär), Groq (dev-fallback) |
| Frontend | React 18, TypeScript, Vite 7 |
| UI | Tailwind CSS 3, shadcn/ui, Framer Motion |
| State | TanStack React Query 5, React Router 6 |
| Säkerhet | Bucket4j (rate limiting), BCrypt, OAuth2 (Google) |
| PWA | Vite PWA Plugin, service worker med offline-cache |

---

## Kom igång

### Krav

- Java 25+
- Node.js 20+
- PostgreSQL 17+
- Maven 3.9+

### 1. Klona repot

```bash
git clone https://github.com/olivermelin/nextstep.git
cd nextstep
```

### 2. Sätt upp databasen

```sql
CREATE DATABASE sobrietydb;
```

### 3. Konfigurera miljövariabler

Kopiera exempelfilen och fyll i dina värden:

```bash
cp .env.production.example .env
```

Se [Miljövariabler](#miljövariabler) för fullständig beskrivning.

### 4. Starta backend

```bash
cd backend
mvn spring-boot:run
```

Servern startar på `http://localhost:8080`.

### 5. Starta frontend

```bash
cd frontend
npm install
npm run dev
```

Appen öppnas på `http://localhost:8082`.

---

## Miljövariabler

### Backend (`backend/src/main/resources/application.yaml` + miljö)

| Variabel | Beskrivning | Standardvärde |
|---|---|---|
| `DB_URL` | JDBC-URL till PostgreSQL | `jdbc:postgresql://localhost:5432/sobrietydb` |
| `DB_USERNAME` | Databasanvändare | — |
| `DB_PASSWORD` | Databaslösenord | — |
| `ANTHROPIC_API_KEY` | API-nyckel för Claude | — |
| `CLAUDE_MODEL` | Modell att använda | `claude-haiku-4-5` |
| `USE_GROQ` | Använd Groq som AI-backend | `false` |
| `MAIL_HOST` | SMTP-värd | — |
| `MAIL_USERNAME` | SMTP-användare | — |
| `MAIL_PASSWORD` | SMTP-lösenord | — |
| `GOOGLE_CLIENT_ID` | OAuth2 Google Client ID | — (valfritt) |
| `GOOGLE_CLIENT_SECRET` | OAuth2 Google Client Secret | — (valfritt) |
| `JPA_DDL_AUTO` | Hibernate DDL-strategi | `update` (dev), `validate` (prod) |

> **OBS:** `ANTHROPIC_API_KEY` får aldrig committas eller loggas.

### Frontend

| Variabel | Beskrivning |
|---|---|
| `VITE_API_URL` | Backend API-bas-URL, t.ex. `http://localhost:8080/api` |

---

## API-översikt

Alla endpoints kräver autentisering om inget annat anges. Autentisering sker via session-cookie efter inloggning.

| Prefix | Ansvarig controller | Beskrivning |
|---|---|---|
| `/api/auth` | `AuthController` | Registrering, inloggning, lösenordsåterställning |
| `/api/coach` | `AICoachController` | AI-coachsamtal, snabbprompts |
| `/api/challenges` | `ChallengesController` | Utmaningar och kategorier |
| `/api/checkin` | `DailyCheckInController` | Daglig incheckning |
| `/api/progress` | `ProgressController` | Framstegsdata och statistik |
| `/api/streaks` | `StreakController` | Streak-hantering |
| `/api/rewards` | `RewardController` | Dagliga belöningar och collectibles |
| `/api/social` | `SocialController` | Aktivitetsflöde, vänner, dueller |
| `/api/settings` | `UserSettingsController` | Användarinställningar |
| `/api/onboarding` | `OnboardingController` | Onboarding-flöde |

---

## Arkitektur

```
nextstep/
├── backend/                        # Java 25 + Spring Boot 4.0
│   └── src/main/java/se/sobriety/nextstep/
│       ├── config/                 # SecurityConfig, AI-config, rate limiting
│       ├── controller/             # 11 REST-controllers
│       ├── service/                # 22 tjänster (AI, kris, socialt, mail...)
│       │   └── ai/                 # CrisisDetectionService, ClaudeApiService
│       ├── entity/                 # 37 JPA-entiteter (UUID-primärnycklar)
│       ├── repository/             # 20 Spring Data-repositories
│       ├── dto/                    # 41 request/response-objekt
│       └── exception/              # GlobalExceptionHandler
│
└── frontend/                       # React 18 + TypeScript
    └── src/
        ├── pages/                  # 10 sidor (Dashboard, AICoach, Social...)
        ├── components/             # 20+ komponenter + shadcn/ui
        │   ├── SOSButton.tsx       # Nödknapp med krisresurser
        │   ├── feed/               # Aktivitetsflöde
        │   └── skeletons/          # Loading states
        ├── services/               # API-anropsfunktioner
        ├── hooks/                  # Custom React hooks
        ├── context/                # AuthContext, CrisisContext
        └── i18n/locales/           # sv.json, en.json
```

### Kritiskt flöde — AI-anrop

```
Användarmeddelande
       ↓
CrisisDetectionService.detectCrisis()
       ↓
  CRITICAL? ──► Returnera fast säkerhetssvar (Mind 90101) — INGEN LLM
       ↓
  ELEVATED? ──► Lägg till kriskontext i prompt
       ↓
  ClaudeApiService.sendMessage()
       ↓
  Svar till användare
```

`CrisisDetectionService` körs **alltid** och **före** varje LLM-anrop — inga undantag.

---

## Säkerhet & GDPR

### Säkerhetsåtgärder

- **Krisdetektering** — Meddelanden på CRITICAL-nivå når aldrig Claude API
- **Rate limiting** — Bucket4j skyddar känsliga endpoints
- **BCrypt** — Alla lösenord hashas, aldrig klartext
- **OAuth2** — Google-inloggning som alternativ
- **OWASP-scanning** — `mvn dependency-check:check` vid bygge (failar vid CVSS ≥ 7)
- **TLS** — All trafik krypterad i transit

### GDPR

| Rätt | Implementering |
|---|---|
| Radering | `UserDataDeletionService` — fullständig borttagning |
| Export | `GdprExportService` — komplett dataexport |
| Samtycke | Explicit godkännande vid registrering |

> Känslig hälsodata (stämning, krismeddelanden, beroendehistorik) lagras krypterat och loggas aldrig i klartext.

---

## Testning

### Backend

```bash
cd backend

# Kör alla tester
mvn test

# OWASP-sårbarhetsscanning
mvn dependency-check:check

# Bygge utan tester
mvn package -DskipTests
```

### Frontend

```bash
cd frontend

# Kör tester
npm test

# Watch mode
npm run test:watch

# Lint-kontroll
npm run lint
```

### Krisdetektering — testkrav

`CrisisDetectionService` har striktast krav av alla tjänster:

| Metric | Gräns |
|---|---|
| Falsk positiv | < 5 % |
| Falsk negativ (CRITICAL) | **0 %** |

Testset inkluderar direkta uttryck, eufemismer, omvändningar och blandspråk.

---

## Deployment

### Docker

```bash
# Bygga backend-image
cd backend
docker build -t nextstep-backend .

# Kör
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host:5432/sobrietydb \
  -e DB_USERNAME=user \
  -e DB_PASSWORD=secret \
  -e ANTHROPIC_API_KEY=sk-ant-... \
  nextstep-backend
```

Backend-imagen kör som icke-root-användare (`nextstep`) med JVM-flaggor optimerade för containerbruk:
`-Xms256m -Xmx512m -XX:+UseContainerSupport`

### Frontend (produktion)

```bash
cd frontend
npm run build
# Statiska filer hamnar i dist/ — serveras via valfri webbserver
```

### GitHub Actions

| Workflow | Trigger | Syfte |
|---|---|---|
| `security.yml` | Push / PR | OWASP-scanning, dependency-audit |
| Dependabot | Schema | Automatiska dependency-uppdateringar |

---

## Agent Team (AI-assisterad utveckling)

NextStep använder ett strukturerat **agent-team-arbetsflöde** med Claude Code. Alla större beslut och features går genom en sekvens av specialiserade roller innan implementation. Konfigurationen finns i [`CLAUDE.md`](./CLAUDE.md).

### Rollerna

| Agent | Ansvar | Frågar alltid |
|---|---|---|
| 🏗️ `architect` | Tekniska beslut, hållbarhet, skalbarhet | "Är detta hållbart på sikt?" |
| 🔒 `security` | GDPR, dataintegritet, krishantering (**vetorätt**) | "Vad händer om denna data läcker?" |
| 👨‍💻 `developer` | Implementation enligt befintliga mönster | "Följer detta projektets befintliga mönster?" |
| 🧪 `tester` | Testkvalitet, krisdetekteringsprecision | "Vad händer om detta fallerar i produktion?" |
| 🎨 `ui` | Användarupplevelse, tillgänglighet, SOS-flöde | "Hur känns detta för en användare i ett svårt ögonblick?" |
| 📋 `team-lead` | Orkestrerar teamet, sammanfattar konsensus | — |

### Standardflöde vid nya features

```
architect → security → developer → tester → ui → team-lead
```

Inga features implementeras utan godkännande från `security` och `tester`. Vid oenighet lyfts beslutet till Oliver.

### Snabbkommandon

```
@architect + @security: Granskar ni [feature/beslut]?
@tester: Skriv tester för [komponent/service]
@ui: Granska detta flöde ur ett krisanvändarperspektiv
Hela teamet: Diskutera och rekommendera approach för [fråga]
```

### Kritiska regler som ALLA agenter följer

1. `CrisisDetectionService` körs **alltid** före varje Claude API-anrop
2. Inga råa API-nycklar i kod, commits eller loggar
3. Känslig hälsodata krypteras — aldrig i klartext
4. All användartext och systempromptar på svenska
5. Coachen diagnosticerar aldrig, föreskriver aldrig — hänvisar alltid till proffs
6. Vid CRITICAL-nivå: fördefinierat säkerhetssvar med Mind 90101 — inget LLM-anrop
7. GDPR: alla endpoints med persondata stödjer radering och export

> Se [`CLAUDE.md`](./CLAUDE.md) för fullständig konfiguration, projektstruktur och öppna beslutspunkter.

---

## Licens

Privat projekt — © Oliver Melin. All rights reserved. Ej för distribution, kopiering eller modifiering utan skriftligt tillstånd.
