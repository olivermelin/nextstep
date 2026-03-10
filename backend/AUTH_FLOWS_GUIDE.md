# Authentication Flows - Quick Reference

## 🔐 Två Autentiseringsflöden

NextStep stöder två helt separata sätt att logga in:

---

## 1️⃣ OAuth2 (Google) - INGEN SIGNUP BEHÖVS

### Flow
```
┌─────────────┐
│   Användare │
│  klickar    │
│ "Login with │
│   Google"   │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│ Redirect till   │
│ Google OAuth2   │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│ Google validerar│
│ och redirectar  │
└──────┬──────────┘
       │
       ▼
┌─────────────────────┐
│ GET /api/auth/me    │
└──────┬──────────────┘
       │
       ▼
┌──────────────────────────┐
│ UserInitializationService│
│ skapar AUTOMATISKT:      │
│ • UserProgress           │
│ • UserSettings           │
└──────┬───────────────────┘
       │
       ▼
┌─────────────────┐
│ Användare är    │
│ inloggad! ✅     │
└─────────────────┘
```

### Endpoints
- **Login**: `GET /api/auth/me` (sker automatiskt efter OAuth2 redirect)
- **Signup**: ❌ INGEN - användare skapas automatiskt

### Frontend
```javascript
<button onClick={() => window.location.href = '/oauth2/authorization/google'}>
  <img src="google-logo.svg" /> Login with Google
</button>
```

---

## 2️⃣ Email/Password - SIGNUP KRÄVS

### Flow

#### A. SIGNUP (första gången)
```
┌─────────────┐
│  Användare  │
│  fyller i   │
│  formulär   │
│ (email,     │
│  password,  │
│  name)      │
└──────┬──────┘
       │
       ▼
┌──────────────────────┐
│ POST /api/auth/signup│
└──────┬───────────────┘
       │
       ▼
┌─────────────────────┐
│ Validera input:     │
│ • Email format      │
│ • Password ≥ 6 char │
│ • Name required     │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│ Kontrollera email   │
│ redan finns?        │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│ Kryptera password   │
│ med BCrypt          │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│ Skapa User entity   │
│ i database          │
└──────┬──────────────┘
       │
       ▼
┌──────────────────────────┐
│ UserInitializationService│
│ skapar:                  │
│ • UserProgress           │
│ • UserSettings           │
└──────┬───────────────────┘
       │
       ▼
┌─────────────────────┐
│ Skicka välkomstmail │
│ (asynkront)         │
└──────┬──────────────┘
       │
       ▼
┌─────────────────┐
│ Användare kan   │
│ nu logga in! ✅  │
└─────────────────┘
```

#### B. LOGIN (efterföljande gånger)
```
┌─────────────┐
│  Användare  │
│  fyller i   │
│  login      │
│ (email,     │
│  password)  │
└──────┬──────┘
       │
       ▼
┌──────────────────────┐
│ POST /api/auth/login │
└──────┬───────────────┘
       │
       ▼
┌─────────────────────┐
│ Hämta User från DB  │
│ via email           │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│ Validera password   │
│ med BCrypt.matches()│
└──────┬──────────────┘
       │
       ├─── ❌ Fel lösenord
       │    └─> 401 Unauthorized
       │
       └─── ✅ Rätt lösenord
            │
            ▼
       ┌─────────────────┐
       │ Användare är    │
       │ inloggad! ✅     │
       └─────────────────┘
```

### Endpoints
- **Signup**: `POST /api/auth/signup`
- **Login**: `POST /api/auth/login`

### Frontend
```javascript
// Signup Form
<form onSubmit={async (e) => {
  e.preventDefault();
  const res = await fetch('/api/auth/signup', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      email: e.target.email.value,
      password: e.target.password.value,
      name: e.target.name.value
    })
  });
  if (res.status === 201) {
    // Redirect to login
    navigate('/login');
  }
}}>
  <input name="email" type="email" required />
  <input name="password" type="password" minLength={6} required />
  <input name="name" type="text" required />
  <button type="submit">Sign Up</button>
</form>

// Login Form
<form onSubmit={async (e) => {
  e.preventDefault();
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      email: e.target.email.value,
      password: e.target.password.value
    })
  });
  if (res.ok) {
    const data = await res.json();
    // Save user data and redirect
    setUser(data);
    navigate(data.onboardingCompleted ? '/dashboard' : '/onboarding');
  }
}}>
  <input name="email" type="email" required />
  <input name="password" type="password" required />
  <button type="submit">Login</button>
</form>
```

---

## 📊 Jämförelse

| Feature | OAuth2 (Google) | Email/Password |
|---------|----------------|----------------|
| **Signup behövs?** | ❌ NEJ | ✅ JA |
| **Endpoint signup** | - | POST /api/auth/signup |
| **Endpoint login** | GET /api/auth/me | POST /api/auth/login |
| **Lösenord?** | Nej (Google hanterar) | Ja (BCrypt-krypterat) |
| **User skapas** | Automatiskt vid första login | Manuellt via signup |
| **Email-verifiering** | Hanteras av Google | Välkomstmail (ingen verifiering) |
| **Onboarding** | Samma som email/password | Samma som OAuth2 |

---

## 🎯 Rekommendationer

### Frontend
1. **Ha båda alternativen** på login-sidan:
   ```
   ┌────────────────────────┐
   │  Login with Google     │  ← OAuth2
   └────────────────────────┘
   
   ─────── or ───────
   
   ┌────────────────────────┐
   │  Email: ___________    │
   │  Password: ________    │  ← Email/Password
   │  [Login] [Sign Up]     │
   └────────────────────────┘
   ```

2. **Signup-sidan** (endast för email/password):
   ```
   ┌────────────────────────┐
   │  Name: ____________    │
   │  Email: ___________    │
   │  Password: ________    │
   │  [Sign Up]             │
   │                        │
   │  Already have account? │
   │  [Login]               │
   └────────────────────────┘
   ```

### Backend
- ✅ Båda flödena leder till samma datastruktur (UserProgress, UserSettings)
- ✅ Onboarding fungerar likadant för båda
- ✅ Ingen duplicerad kod tack vare `UserInitializationService`

---

## 🔒 Säkerhet

### OAuth2
- Google validerar användaren
- Ingen risk för svaga lösenord
- Ingen risk för phishing av lösenord
- MFA hanteras av Google

### Email/Password
- BCrypt med auto-salt (10 rounds)
- Lösenord minst 6 tecken
- Email-format validering
- Generiska felmeddelanden (ingen info leakage)

---

## ✅ Sammanfattning

- **OAuth2 (Google)**: Enklast för användaren, ingen signup behövs
- **Email/Password**: Mer kontroll, signup krävs, lösenord krypteras säkert
- **Båda flödena**: Leder till samma användarupplevelse efter inloggning
- **Onboarding**: Samma process oavsett inloggningsmetod

