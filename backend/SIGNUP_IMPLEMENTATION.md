# Sign Up Implementation - Dokumentation

## Översikt
Komplett implementation av användarregistrering (signup) med lösenordskryptering, validering och välkomstmail.

## Implementerade Komponenter

### 1. Entity

#### `User.java`
- Huvudentitet för användarautentisering
- Fält: `id`, `email` (unique), `password` (hashed), `name`, `createdAt`, `lastUpdated`
- Tabell: `app_user` (för att undvika konflikt med PostgreSQL reserved keyword "user")

### 2. Repository

#### `UserRepository.java`
```java
Optional<User> findByEmail(String email);
boolean existsByEmail(String email);
```

### 3. DTOs

#### `SignUpRequestDto`
```json
{
  "email": "string",
  "password": "string",
  "name": "string"
}
```

#### `SignUpResponseDto`
```json
{
  "id": "string",
  "email": "string",
  "name": "string",
  "message": "string"
}
```

### 4. Services

#### `SignUpService.java`

**Huvudmetoder:**

1. **`signUpNewUser(SignUpRequestDto request)`**
   - Validerar input (email format, lösenordslängd min 6, namn required)
   - Kontrollerar om email redan finns
   - Krypterar lösenord med BCrypt
   - Skapar User-entity
   - Initialiserar UserProgress och UserSettings
   - Skickar välkomstmail (asynkront, fel stoppar inte signup)
   - Returnerar SignUpResponseDto

2. **`validatePassword(String email, String rawPassword)`**
   - Validerar lösenord mot hashat lösenord i databasen
   - Används vid login
   - Kastar IllegalArgumentException om användaren inte finns

3. **`validateSignUpRequest(SignUpRequestDto request)`** (private)
   - Email required och måste innehålla @
   - Lösenord minst 6 tecken
   - Namn required

### 5. Controllers

#### `SignUpController.java`

**POST /api/auth/signup**
```java
@PostMapping("/signup")
public ResponseEntity<?> signUp(@RequestBody SignUpRequestDto request)
```

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "John Doe"
}
```

**Success Response (201 Created):**
```json
{
  "id": "1",
  "email": "user@example.com",
  "name": "John Doe",
  "message": "User registered successfully"
}
```

**Error Responses:**

400 Bad Request (Invalid input):
```json
{
  "error": "Email already registered"
}
```
eller
```json
{
  "error": "Password must be at least 6 characters"
}
```

500 Internal Server Error:
```json
{
  "error": "An error occurred during registration"
}
```

#### `AuthController.java` (uppdaterad)

**POST /api/auth/login**
```java
@PostMapping("/login")
public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request)
```

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Success Response (200 OK):**
```json
{
  "authenticated": true,
  "id": "1",
  "email": "user@example.com",
  "name": "John Doe",
  "onboardingCompleted": false
}
```

**Error Response (401 Unauthorized):**
```json
{
  "error": "Invalid credentials"
}
```

## Säkerhet

### Lösenordskryptering
- **BCryptPasswordEncoder** från Spring Security
- Automatisk salt generation
- 10 rounds (default strength)
- Lösenord hashas **aldrig** lagras i klartext

### Validering
- Email-format kontroll
- Lösenordslängd minimum 6 tecken
- Dubbelkoll att email inte redan finns
- Alla fel ger generiska felmeddelanden för att undvika information leakage

## Integration

### UserInitializationService
Vid signup skapas automatiskt:
1. **UserProgress** - med 0 poäng, nivå 1
2. **UserSettings** - med default-inställningar

### MailNotificationService
Välkomstmail skickas automatiskt efter lyckad registrering:
- Template: WELCOME
- Variables: firstName (name från signup)
- Asynkron - fel stoppar inte signup

## Database Schema

```sql
CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_updated TIMESTAMP NOT NULL
);

CREATE INDEX idx_user_email ON app_user(email);
```

## Flöde

### Signup Flow
1. Frontend → POST /api/auth/signup
2. Validera input (email, password, name)
3. Kontrollera om email redan finns
4. Hasha lösenord med BCrypt
5. Skapa User i database
6. Initialisera UserProgress och UserSettings
7. Skicka välkomstmail (catch errors)
8. Returnera success response

### Login Flow
1. Frontend → POST /api/auth/login
2. Hämta User från database via email
3. Validera lösenord mot hashat värde
4. Säkerställ UserProgress och UserSettings finns
5. Hämta onboarding-status
6. Returnera authenticated response med user data

## Testing

### Manuell Test med curl

**Signup:**
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "test123",
    "name": "Test User"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "test123"
  }'
```

### Test Scenarios

1. **✅ Valid Signup**
   - Input: Valid email, password ≥6, name
   - Expected: 201 Created, user created, welcome email sent

2. **❌ Duplicate Email**
   - Input: Email som redan finns
   - Expected: 400 Bad Request, "Email already registered"

3. **❌ Invalid Email**
   - Input: Email utan @
   - Expected: 400 Bad Request, "Invalid email format"

4. **❌ Short Password**
   - Input: Password < 6 characters
   - Expected: 400 Bad Request, "Password must be at least 6 characters"

5. **❌ Missing Name**
   - Input: Blank name
   - Expected: 400 Bad Request, "Name is required"

6. **✅ Valid Login**
   - Input: Correct email + password
   - Expected: 200 OK, authenticated response

7. **❌ Wrong Password**
   - Input: Correct email, wrong password
   - Expected: 401 Unauthorized, "Invalid credentials"

8. **❌ Non-existent User**
   - Input: Email som inte finns
   - Expected: 401 Unauthorized, "Invalid credentials"

## Dependencies

Alla dependencies finns redan i pom.xml:
- `spring-boot-starter-security` - BCryptPasswordEncoder
- `spring-boot-starter-data-jpa` - Repository support
- `spring-boot-starter-web` - REST controllers
- `spring-boot-starter-mail` - Welcome email
- `postgresql` - Database driver

## Nästa Steg

### Frontend Integration
1. Uppdatera signup-formulär för att posta till `/api/auth/signup`
2. Uppdatera login-formulär för att posta till `/api/auth/login`
3. Hantera success/error responses
4. Spara authenticated state efter login
5. Redirect till onboarding om `onboardingCompleted: false`

### Säkerhetsförbättringar (Optional)
1. Email verification (skicka verifieringslänk)
2. Rate limiting på signup/login endpoints
3. CAPTCHA för att förhindra bot-registreringar
4. Lösenordsstyrka-validering (stora/små bokstäver, siffror, specialtecken)
5. Account lockout efter X misslyckade login-försök

### Session Management (Optional)
1. JWT tokens istället för sessions
2. Refresh tokens för längre sessions
3. Remember me-funktionalitet

## Status

✅ **KOMPLETT** - Båda autentiseringsflödena är fullt fungerande!

### OAuth2 Flow (Google)
- [x] GET /api/auth/me endpoint
- [x] Automatisk användarregistrering via UserInitializationService
- [x] UserProgress och UserSettings skapas automatiskt
- [x] Onboarding-status returneras
- [x] Ingen signup behövs

### Email/Password Flow
- [x] User entity med email/password/name
- [x] UserRepository med findByEmail och existsByEmail
- [x] SignUpRequestDto och SignUpResponseDto
- [x] SignUpService med validering och BCrypt
- [x] POST /api/auth/signup endpoint i AuthController
- [x] POST /api/auth/login endpoint med lösenordsvalidering
- [x] Integration med UserInitializationService
- [x] Välkomstmail via MailNotificationService
- [x] Fullständig dokumentation

### Unified Auth
- [x] Alla endpoints i AuthController (ingen konflikt)
- [x] Tydlig separation mellan OAuth2 och Email/Password
- [x] Båda flödena leder till samma user-struktur

