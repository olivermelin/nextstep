# Backend-implementering för Onboarding

## Översikt
Frontend är nu färdig och redo att konsumera onboarding-API:er från backend.

## API-Endpoints som behövs

### 1. POST /api/onboarding/complete/{userId}
Slutför onboarding och spara användardata.

**Request Body:**
```json
{
  "userGoals": ["DAILY_STABILITY", "MENTAL_HEALTH", "OTHER"],
  "otherGoal": "Min egen målbeskrivning",
  "backgroundInfo": {
    "age": 28,
    "gender": "MALE",
    "currentSituation": "IN_TREATMENT",
    "substanceHistory": ["ALCOHOL", "CANNABIS"]
  },
  "recoveryStage": "ONE_TO_FOUR_WEEKS"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Onboarding completed successfully"
}
```

### 2. GET /api/onboarding/status/{userId}
Kontrollera om användaren har slutfört onboarding.

**Response:**
```json
{
  "completed": true
}
```

### 3. GET /api/auth/me (Uppdatera befintlig endpoint)
Inkludera `onboardingCompleted` i användarresponsen.

**Response:**
```json
{
  "id": "123",
  "name": "John Doe",
  "email": "john@example.com",
  "picture": "https://...",
  "onboardingCompleted": false
}
```

## Database-modell

Utöka User-entiteten med följande fält:

```java
@Entity
public class User {
    @Id
    private String id;
    
    private String name;
    private String email;
    private String picture;
    
    // Nya onboarding-relaterade fält
    private Boolean onboardingCompleted = false;
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<UserGoal> userGoals;
    
    private String otherGoal;
    
    @Enumerated(EnumType.STRING)
    private RecoveryStage recoveryStage;
    
    // Background info som embedded object
    @Embedded
    private BackgroundInfo backgroundInfo;
}

@Embeddable
public class BackgroundInfo {
    private Integer age;
    
    @Enumerated(EnumType.STRING)
    private Gender gender;
    
    @Enumerated(EnumType.STRING)
    private CurrentSituation currentSituation;
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<SubstanceType> substanceHistory;
}
```

## Enums som behövs

```java
public enum UserGoal {
    DAILY_STABILITY,
    BETTER_ROUTINES,
    REDUCE_SUBSTANCES,
    MENTAL_HEALTH,
    STRUCTURE_MOTIVATION,
    STAY_CLEAN,
    OTHER
}

public enum Gender {
    MALE,
    FEMALE,
    NON_BINARY,
    PREFER_NOT_TO_SAY
}

public enum CurrentSituation {
    IN_TREATMENT,
    RECENTLY_COMPLETED,
    STRUGGLING_NOT_IN_TREATMENT,
    SUPPORTING_SOMEONE,
    PREFER_NOT_TO_SAY
}

public enum SubstanceType {
    ALCOHOL,
    CANNABIS,
    STIMULANTS,
    OPIOIDS,
    PRESCRIPTION_MEDS,
    GAMBLING,
    OTHER,
    PREFER_NOT_TO_SAY
}

public enum RecoveryStage {
    ACTIVE_USE,
    TRYING_TO_QUIT,
    ONE_TO_SEVEN_DAYS,
    ONE_TO_FOUR_WEEKS,
    ONE_TO_SIX_MONTHS,
    SIX_PLUS_MONTHS,
    LONG_TERM_RECOVERY
}
```

## Controller-exempel

```java
@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping("/complete/{userId}")
    public ResponseEntity<OnboardingResponse> completeOnboarding(
            @PathVariable String userId,
            @RequestBody OnboardingData data) {
        
        onboardingService.completeOnboarding(userId, data);
        
        return ResponseEntity.ok(new OnboardingResponse(true, "Onboarding completed successfully"));
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<OnboardingStatusResponse> getOnboardingStatus(@PathVariable String userId) {
        boolean completed = onboardingService.isOnboardingCompleted(userId);
        return ResponseEntity.ok(new OnboardingStatusResponse(completed));
    }
}
```

## Service-exempel

```java
@Service
public class OnboardingService {

    private final UserRepository userRepository;

    public void completeOnboarding(String userId, OnboardingData data) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        
        user.setUserGoals(data.getUserGoals());
        user.setOtherGoal(data.getOtherGoal());
        user.setRecoveryStage(data.getRecoveryStage());
        user.setBackgroundInfo(data.getBackgroundInfo());
        user.setOnboardingCompleted(true);
        
        userRepository.save(user);
    }

    public boolean isOnboardingCompleted(String userId) {
        return userRepository.findById(userId)
            .map(User::getOnboardingCompleted)
            .orElse(false);
    }
}
```

## Migration

Skapa en Liquibase/Flyway-migration för att lägga till de nya kolumnerna:

```sql
ALTER TABLE users ADD COLUMN onboarding_completed BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN other_goal VARCHAR(500);
ALTER TABLE users ADD COLUMN recovery_stage VARCHAR(50);
ALTER TABLE users ADD COLUMN age INTEGER;
ALTER TABLE users ADD COLUMN gender VARCHAR(50);
ALTER TABLE users ADD COLUMN current_situation VARCHAR(100);

CREATE TABLE user_goals (
    user_id VARCHAR(255),
    goal VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE user_substance_history (
    user_id VARCHAR(255),
    substance_type VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## Personalisering (Framtida förbättringar)

Använd onboarding-datan för att:

1. **Anpassa challenges**: Filtrera eller rekommendera challenges baserat på `userGoals` och `recoveryStage`

2. **Anpassa AI-coachen**: Låt AI-coachen ta hänsyn till användarens situation när den ger råd

3. **Anpassa dashboard**: Visa relevant information baserat på `recoveryStage`

4. **Notifieringar**: Anpassa timing och innehåll i notifieringar baserat på användarens resa

5. **Progress tracking**: Använd recoveryStage som baseline för att mäta framsteg

## Nästa steg

1. Implementera backend-endpoints enligt specifikationen ovan
2. Skapa database-migration
3. Testa API:erna med frontend
4. Implementera personalisering baserat på onboarding-data
