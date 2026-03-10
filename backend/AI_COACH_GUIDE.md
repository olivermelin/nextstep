# AI Coach Feature

## Översikt

AI Coach-featuren ger personaliserad coaching och motivation till användare baserat på deras profil, återhämtningsstadium, och framsteg i appen.

## Komponenter

### 1. AICoachService
Huvudservice som hanterar all AI-interaktion.

**Funktioner:**
- Personaliserad coaching baserat på användarens profil
- Kontext-medveten AI som förstår användarens situation
- Fallback-svar när AI inte är tillgänglig
- Integration med OpenAI GPT-4

### 2. AIContextBuilder
Helper-klass som bygger kontextuell information för AI:n.

**Funktioner:**
- Bygger system prompts med användardata
- Översätter enums till svenska
- Genererar fallback-svar

### 3. AICoachProperties
Konfigurationsklass för AI Coach.

## API Endpoints

### 1. Enkel Motivation
```http
GET /api/coach/motivate?message=Ge mig motivation
```

**Response:**
```
"Fokusera på ett steg i taget – du klarar det!"
```

---

### 2. Personaliserad Chat (med kontext)
```http
POST /api/coach/chat/{userId}
Content-Type: application/json

{
  "message": "Jag känner mig svag idag",
  "includeContext": true
}
```

**Response:**
```json
{
  "message": "Johan, jag förstår att det känns tufft just nu. Du har redan kommit så långt - du är på nivå 3 och har slutfört 5 utmaningar. Det visar verklig styrka. Försök ta en utmaning från Mental Hälsa-kategorin idag, till exempel andningsövningen. Små steg framåt.",
  "userId": "google-oauth2|123456",
  "contextUsed": true,
  "responseTimeMs": 1250
}
```

---

### 3. Snabb Motivation
```http
GET /api/coach/quick/{userId}?message=Jag behöver stöd
```

**Response:**
```json
{
  "message": "Du är på rätt väg! Med 1-4 veckor nykter bakom dig visar du verklig kraft. Varje dag är en seger.",
  "userId": "google-oauth2|123456",
  "contextUsed": true,
  "responseTimeMs": 980
}
```

---

### 4. Status Check
```http
GET /api/coach/status
```

**Response:**
```json
{
  "aiAvailable": true,
  "service": "AI Coach",
  "status": "online"
}
```

---

## Konfiguration

### application.yaml
```yaml
ai:
  coach:
    enabled: ${AI_COACH_ENABLED:false}
    api-key: ${OPENAI_API_KEY:}
    model: ${AI_COACH_MODEL:gpt-4}
    max-tokens: ${AI_COACH_MAX_TOKENS:500}
    temperature: ${AI_COACH_TEMPERATURE:0.7}
```

### Environment Variables

| Variable | Default | Beskrivning |
|----------|---------|-------------|
| `OPENAI_API_KEY` | - | Din OpenAI API-nyckel (krävs för AI) |
| `AI_COACH_ENABLED` | `false` | Aktivera/inaktivera AI Coach |
| `AI_COACH_MODEL` | `gpt-4` | OpenAI model (gpt-4, gpt-3.5-turbo, etc.) |
| `AI_COACH_MAX_TOKENS` | `500` | Max antal tokens i svar |
| `AI_COACH_TEMPERATURE` | `0.7` | Kreativitetsnivå (0.0-1.0) |

### Starta med AI aktiverat

**Windows (PowerShell):**
```powershell
$env:OPENAI_API_KEY="sk-..."
$env:AI_COACH_ENABLED="true"
.\mvnw.cmd spring-boot:run
```

**Linux/Mac:**
```bash
export OPENAI_API_KEY="sk-..."
export AI_COACH_ENABLED="true"
./mvnw spring-boot:run
```

---

## Användarkontext

När `includeContext: true` används, inkluderar AI:n följande information:

### Från UserSettings:
- Namn
- Användarens mål (UserGoals)
- Återhämtningsstadium (RecoveryStage)

### Från BackgroundInfo:
- Ålder
- Kön
- Nuvarande situation (CurrentSituation)
- Substanshistorik (SubstanceType)

### Från UserProgress:
- Nuvarande nivå
- Totala poäng

### Från UserChallenges:
- Antal genomförda utmaningar

---

## Fallback-läge

Om AI inte är tillgänglig (ingen API-nyckel, nätverksfel, etc.), använder tjänsten automatiskt fördefinierade motiverande meddelanden:

**Exempel:**
- "Kom ihåg att varje liten framsteg räknas. Du tar ett steg i taget!"
- "Bra jobbat att du är här idag. Det visar styrka och engagemang."
- "Fokusera på nuet. Du klarar av det här, ett ögonblick i taget."

---

## System Prompt

AI:n använder följande riktlinjer:

```
Du är en empatisk och professionell återhämtningscoach för NextStep-appen.
Din roll är att stödja användare i deras återhämtning från beroende.

RIKTLINJER:
- Var alltid empatisk och stödjande
- Fokusera på små, konkreta steg
- Uppmuntra självreflektion
- Var icke-dömande och respektfull
- Använd svenska språket
- Håll svaren koncisa (max 2-3 meningar om inte användaren frågar mer)
- Referera till deras framsteg när relevant
```

---

## Exempel på användning i frontend

### React Example (TypeScript)

```typescript
// Hämta personaliserad coaching
const getCoaching = async (userId: string, message: string) => {
  const response = await fetch(`/api/coach/chat/${userId}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      message: message,
      includeContext: true
    })
  });
  
  const data = await response.json();
  return data.message;
};

// Snabb motivation
const getQuickMotivation = async (userId: string) => {
  const response = await fetch(
    `/api/coach/quick/${userId}?message=Ge mig motivation`,
    { method: 'GET' }
  );
  
  const data = await response.json();
  return data.message;
};

// Kolla AI status
const checkAIStatus = async () => {
  const response = await fetch('/api/coach/status');
  const data = await response.json();
  return data.aiAvailable;
};
```

---

## Testing

### Testa utan AI (Fallback)
```bash
# AI disabled by default
curl http://localhost:8080/api/coach/motivate
```

### Testa med AI
```bash
# Set environment variable
export OPENAI_API_KEY="sk-your-key-here"
export AI_COACH_ENABLED="true"

# Start application
./mvnw spring-boot:run

# Test endpoint
curl -X POST http://localhost:8080/api/coach/chat/user123 \
  -H "Content-Type: application/json" \
  -d '{"message":"Jag behöver stöd","includeContext":true}'
```

---

## Dependencies

```xml
<!-- OpenAI Java Client -->
<dependency>
    <groupId>com.theokanning.openai-gpt3-java</groupId>
    <artifactId>service</artifactId>
    <version>0.18.2</version>
</dependency>
```

---

## Säkerhet

- API-nyckeln lagras aldrig i kod eller version control
- Använd alltid environment variables för API-nyckel
- Överväg rate limiting för produktionsmiljö
- Användardata skickas endast till OpenAI när AI är aktiverad

---

## Kostnader

OpenAI GPT-4 pricing (ca):
- Input: ~$0.03 per 1K tokens
- Output: ~$0.06 per 1K tokens

Med `maxTokens: 500` blir typisk kostnad per request ~$0.01-0.03.

För kostnadsbesparingar:
- Använd `gpt-3.5-turbo` istället (10x billigare)
- Minska `maxTokens`
- Implementera caching av vanliga frågor

---

## Framtida Förbättringar

- [ ] Konversationshistorik (minne av tidigare chattar)
- [ ] Specialiserade prompts för olika situationer (kris, fira framsteg, etc.)
- [ ] Voice input/output integration
- [ ] Multi-språkstöd
- [ ] A/B testing av olika prompts
- [ ] Analytics för AI-användning
- [ ] Custom fine-tuned model för återhämtningscoaching

