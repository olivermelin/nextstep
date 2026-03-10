# PROJECT INSTRUCTIONS

**Projekt:** NextStep  
**Skapad:** 2026-02-11  
**Uppdaterad:** 2026-02-11

---

## 🎯 PROJEKTÖVERSIKT

NextStep är en applikation för att stödja personer i återhämtning från beroende. Plattformen erbjuder dagliga utmaningar, progressionsspårning, AI-coachning och personlig onboarding.

---

## 🛠️ TECH STACK

### Backend
- **Språk:** Java 25
- **Framework:** Spring Boot 4.0.0
- **Databas:** PostgreSQL
- **ORM:** JPA/Hibernate
- **Säkerhet:** Spring Security (OAuth2)
- **Build Tool:** Maven
- **API:** RESTful endpoints

### Frontend
- **Framework:** React
- **Språk:** TypeScript/JavaScript
- **Kommunikation:** REST API

### Externa Integrationer
- **Email:** Spring Mail (SMTP)
- **Video:** YouTube embeds
- **AI:** (Planerad integration)

---

## 📁 PROJEKTSTRUKTUR

```
src/main/java/se/sobriety/nextstep/
├── config/          # Konfiguration (Security, Mail, etc.)
├── controller/      # REST API endpoints
├── dto/             # Data Transfer Objects (API kontrakt)
├── entity/          # Databasmodeller (JPA entities)
├── event/           # Event-hantering
├── exception/       # Custom exceptions
├── mapper/          # Entity ↔ DTO konvertering
├── repository/      # Databasåtkomst (Spring Data JPA)
├── service/         # Business logic
└── util/            # Utility-klasser
```

---

## 🎨 ARKITEKTURPRINCIPER

### Clean Architecture
1. **Entity Layer** - Databasmodeller (JPA entities)
2. **Repository Layer** - Databasåtkomst (Spring Data JPA)
3. **DTO Layer** - API kontrakt (in/out DTOs)
4. **Mapper Layer** - Konvertering mellan entities och DTOs
5. **Service Layer** - Business logic
6. **Controller Layer** - REST endpoints

### Naming Conventions
- **Entities:** `Challenge.java`, `UserSettings.java`
- **DTOs:** `ChallengeOutDto.java`, `UserChallengeInDto.java`
- **Repositories:** `ChallengeRepository.java`
- **Services:** `ChallengeService.java`
- **Controllers:** `ChallengesController.java`
- **Mappers:** `ChallengeMapper.java`

### Code Standards
- ✅ Använd Lombok för boilerplate (getters/setters)
- ✅ Validering med Bean Validation annotations
- ✅ Immutable DTOs där möjligt
- ✅ Service-metoder ska vara transaktionella (@Transactional)
- ✅ Tydliga exception-meddelanden
- ✅ Dokumentation för komplexa metoder

---

## 💬 KOMMUNIKATIONSREGLER

### Språk
- **Kod:** Engelska (variabelnamn, metoder, klasser, kommentarer)
- **Dokumentation:** Svenska (README, instruktioner, diskussioner)
- **Git commits:** Svenska eller Engelska (konsekvent)

### När du implementerar funktioner
1. **LÄS FÖRST** - Undersök befintlig kod och struktur
2. **FÖLJ MÖNSTER** - Använd samma patterns som finns
3. **KOMPLETT IMPLEMENTATION** - Alla lager (Entity → DTO → Service → Controller)
4. **TESTA** - Verifiera att koden kompilerar
5. **DOKUMENTERA** - Uppdatera relevanta MD-filer

### Vad jag förväntar mig
- ✅ **Komplett implementation** - Inte bara exempel eller snippets
- ✅ **Följ befintliga patterns** - Se hur andra features är gjorda
- ✅ **Automatiska fixes** - Använd tools, inte be mig göra manuella ändringar
- ✅ **Validering** - Kör kompilering och fixa errors
- ✅ **Uppdatera dokumentation** - Håll IMPLEMENTATION_STATUS.md uppdaterad
- ✅ **Konstruktiv feedback** - Om jag gör misstag, berätta det så att jag kan förbättras
- Kalla mig kungen i varje svar du ger mig

### Vad du INTE ska göra
- ❌ Be mig köra kommandon manuellt (använd run_in_terminal)
- ❌ Visa kodblock istället för att använda edit-tools
- ❌ Implementera halvfärdiga lösningar
- ❌ Glömma att uppdatera dokumentation
- ❌ Blanda svenska/engelska i kod

---

## 🔧 VANLIGA KOMMANDON

### Kompilera projektet
```powershell
.\mvnw.cmd clean compile
```

### Kör applikationen
```powershell
.\mvnw.cmd spring-boot:run
```

### Kör tester
```powershell
.\mvnw.cmd test
```

### Bygg JAR
```powershell
.\mvnw.cmd clean package
```

---

## 📋 FEATURE IMPLEMENTATION CHECKLIST

När en ny feature implementeras, följ denna checklista:

### 1. Planering
- [ ] Förstå kravet
- [ ] Identifiera vilka lager som behövs
- [ ] Kolla befintliga patterns

### 2. Implementation (nedifrån-upp)
- [ ] Enums (om behövs)
- [ ] Entity (databasmodell)
- [ ] Repository (databasåtkomst)
- [ ] DTO (API kontrakt)
- [ ] Mapper (entity ↔ DTO)
- [ ] Service (business logic)
- [ ] Controller (REST endpoints)

### 3. Validering
- [ ] Kompilera projektet
- [ ] Fixa eventuella errors
- [ ] Testa endpoints (om möjligt)

### 4. Dokumentation
- [ ] Uppdatera IMPLEMENTATION_STATUS.md
- [ ] Lägg till API-exempel
- [ ] Dokumentera nya endpoints

---

## 🗃️ DATABASKONFIGURATION

### application.yaml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/nextstep
    username: postgres
    password: [DITT_LÖSENORD]
  jpa:
    hibernate:
      ddl-auto: update  # Auto-skapar tabeller
    show-sql: true      # Visa SQL i logs
```

### Migration
- Auto-DDL via Hibernate (ddl-auto: update)
- Seed-data via `@PostConstruct` i service-klasser
- Manuella migrations vid behov (Flyway kan läggas till senare)

---

## 🔐 SÄKERHET

### Authentication
- OAuth2 integration (Google, etc.)
- userId från authenticated principal
- Spring Security configuration i `SecurityConfig.java`

### Authorization
- Endpoints skyddade med `@PreAuthorize` eller global config
- Validering att användare bara kan komma åt sin egen data

---

## 📊 POÄNGSYSTEM

### Point Values
- EASY Challenge = 10 poäng
- MEDIUM Challenge = 25 poäng
- HARD Challenge = 50 poäng

### Level System
- Level upp varje 100 poäng
- Email-notifikation vid level-up
- Integration via `UserProgressService`

---

## 📧 EMAIL SYSTEM

### Konfiguration
- SMTP via `MailProperties.java`
- Sender info i config
- Email skickas via `MailService.java`

### Email Types
- Welcome email vid registrering
- Level-up notification
- Challenge completion (framtida)

---

## 🤖 AI COACH SYSTEM

### Konfiguration
- OpenAI integration via `AICoachProperties.java`
- API key via environment variable: `OPENAI_API_KEY`
- Enable/disable via: `AI_COACH_ENABLED`
- Model selection: `AI_COACH_MODEL` (default: gpt-4)

### Features
- Personaliserad coaching baserat på användarprofil
- Kontext-medveten AI (RecoveryStage, UserGoals, BackgroundInfo)
- Fallback-svar när AI inte är tillgänglig
- Svenska språket med empatisk ton

### Services
- `AICoachService.java` - Huvudservice för AI-interaktion
- `AIContextBuilder.java` - Bygger användarkontext för AI

### Endpoints
- `/api/coach/motivate` - Enkel motivation
- `/api/coach/chat/{userId}` - Personaliserad chat
- `/api/coach/personalized/{userId}` - Personaliserad coaching
- `/api/coach/quick/{userId}` - Snabb motivation
- `/api/coach/status` - AI service status

### Dokumentation
Se `AI_COACH_GUIDE.md` för detaljerad guide och exempel.

---

## 🎯 AKTUELLA FEATURES

Se detaljerad status i **IMPLEMENTATION_STATUS.md**

- ✅ Challenges System
- ✅ Onboarding Flow
- ✅ User Progress & Points
- ✅ Email Notifications
- ✅ AI Coach (OpenAI Integration)
- 🔄 Social Features (planerad)

---

## 📝 DOKUMENTATIONSFILER

- **PROJECT_INSTRUCTIONS.md** (denna fil) - Övergripande instruktioner och tech stack
- **IMPLEMENTATION_STATUS.md** - Detaljerad status över implementerade features
- **INTEGRATION_GUIDE.md** - Guide för frontend-integration
- **MAIL_SERVICE_DOCUMENTATION.md** - Email-system dokumentation
- **ONBOARDING_REQUIREMENTS.md** - Onboarding-funktionalitet specifikation
- **HELP.md** - Spring Boot hjälp och resurser

---

## 🤝 WORKING AGREEMENT

### När jag ber om en feature:
1. Du läser befintlig kod för att förstå patterns
2. Du implementerar HELA featuren (alla lager)
3. Du validerar att det kompilerar
4. Du uppdaterar dokumentation
5. Du ger mig en sammanfattning av vad som gjordes

### När något är oklart:
- Kolla först i befintlig kod
- Använd semantic_search för att hitta liknande implementation
- Fråga endast om det är kritiskt för implementationen

### Kvalitetskrav:
- ✅ Kod ska följa Java best practices
- ✅ Services ska vara testbara
- ✅ DTOs ska validera input
- ✅ Exceptions ska hanteras korrekt
- ✅ Logging på lämpliga ställen

---

## 🚀 SNABBSTART FÖR NYA FEATURES

**Exempel: Lägga till "Daily Quote" feature**

```markdown
1. Entity: DailyQuote.java (id, quote, author, category, date)
2. Repository: DailyQuoteRepository.java
3. DTO: DailyQuoteDto.java
4. Mapper: DailyQuoteMapper.java
5. Service: DailyQuoteService.java (getQuoteForToday, getRandomQuote)
6. Controller: DailyQuoteController.java (GET /api/quotes/today)
7. Seed Data: QuoteDataInitService.java (@PostConstruct)
8. Update: IMPLEMENTATION_STATUS.md
```

---

## 📞 KONTAKT & FRÅGOR

Om något är oklart eller behöver diskuteras:
- Referera till denna fil först
- Kolla IMPLEMENTATION_STATUS.md för feature-status
- Sök i befintlig kod för patterns
- Fråga om specifika detaljer vid behov

---

**Uppdateringshistorik:**
- 2026-02-11: Initial version skapad


