package se.sobriety.nextstep.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import se.sobriety.nextstep.dto.ForgotPasswordRequestDto;
import se.sobriety.nextstep.dto.LoginRequestDto;
import se.sobriety.nextstep.dto.ResetPasswordRequestDto;
import se.sobriety.nextstep.dto.SignUpRequestDto;
import se.sobriety.nextstep.dto.SignUpResponseDto;
import se.sobriety.nextstep.entity.User;
import se.sobriety.nextstep.repository.UserRepository;
import se.sobriety.nextstep.service.MailNotificationService;
import se.sobriety.nextstep.service.OnboardingService;
import se.sobriety.nextstep.service.PasswordResetService;
import se.sobriety.nextstep.service.SignUpService;
import se.sobriety.nextstep.service.UserDataDeletionService;
import se.sobriety.nextstep.service.UserInitializationService;
import se.sobriety.nextstep.service.UserProgressService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserProgressService progressService;
    private final UserInitializationService initializationService;
    private final OnboardingService onboardingService;
    private final SignUpService signUpService;
    private final PasswordResetService passwordResetService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final UserDataDeletionService userDataDeletionService;
    private final MailNotificationService mailNotificationService;

    public AuthController(UserProgressService progressService,
                         UserInitializationService initializationService,
                         OnboardingService onboardingService,
                         SignUpService signUpService,
                         PasswordResetService passwordResetService,
                         UserRepository userRepository,
                         AuthenticationManager authenticationManager,
                         UserDataDeletionService userDataDeletionService) {
                         MailNotificationService mailNotificationService) {
        this.initializationService = initializationService;
        this.progressService = progressService;
        this.onboardingService = onboardingService;
        this.signUpService = signUpService;
        this.passwordResetService = passwordResetService;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.userDataDeletionService = userDataDeletionService;
        this.mailNotificationService = mailNotificationService;
    }

    /**
     * GET /api/auth/me
     * Hämta inloggad användare — stödjer både OAuth2 och email/password-sessioner.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // OAuth2-inloggning
        if (auth instanceof OAuth2AuthenticationToken oauthToken) {
            String email = (String) oauthToken.getPrincipal().getAttribute("email");
            String name = (String) oauthToken.getPrincipal().getAttribute("name");

            boolean isNewUser = initializationService.ensureUserExistsAndUpdateFromAuth(email, name, email);
            if (isNewUser) {
                try {
                    String firstName = name != null && name.contains(" ") ? name.split(" ")[0] : name;
                    mailNotificationService.sendWelcomeEmail(email, firstName != null ? firstName : "");
                } catch (Exception e) {
                    // Mailfel ska inte stoppa inloggningen
                }
            }
            boolean onboardingCompleted = onboardingService.isOnboardingCompleted(email);

            return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "id", email,
                    "name", name != null ? name : "",
                    "email", email != null ? email : "",
                    "picture", oauthToken.getPrincipal().getAttribute("picture") != null ? oauthToken.getPrincipal().getAttribute("picture") : "",
                    "onboardingCompleted", onboardingCompleted
            ));
        }

        // Email/password-inloggning
        if (auth instanceof UsernamePasswordAuthenticationToken && auth.isAuthenticated()) {
            String email = auth.getName();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                boolean onboardingCompleted = onboardingService.isOnboardingCompleted(email);
                return ResponseEntity.ok(Map.of(
                        "authenticated", true,
                        "id", email,
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "picture", "",
                        "onboardingCompleted", onboardingCompleted
                ));
            }
        }

        return ResponseEntity.ok(Map.of("authenticated", false));
    }

    /**
     * POST /api/auth/signup
     * Email/Password-registrering - skapa ny användare
     * Används ENDAST för email/password-flow, INTE för OAuth2
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequestDto request) {
        try {
            SignUpResponseDto response = signUpService.signUpNewUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred during registration"));
        }
    }

    /**
     * POST /api/auth/login
     * Email/Password-inloggning — autentiserar via Spring Security AuthenticationManager
     * och skapar en HTTP-session så att efterföljande API-anrop fungerar.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletRequest httpRequest) {
        try {
            // Autentisera via Spring Security — skapar en riktig Authentication
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            // Spara i SecurityContext och knyt till HTTP-sessionen
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            httpRequest.getSession(true)
                    .setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            // Hämta användardata
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            initializationService.ensureUserExistsAndUpdateFromAuth(email, user.getName(), email);
            boolean onboardingCompleted = onboardingService.isOnboardingCompleted(email);

            return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "id", email,
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "onboardingCompleted", onboardingCompleted
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {

        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        if (request.getSession(false) != null) {
            request.getSession().invalidate();
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok("Logged out successfully");
    }

    /**
     * POST /api/auth/forgot-password
     * Begär lösenordsåterställning - skickar mail med reset-länk.
     * Returnerar alltid 200 OK oavsett om emailen finns (förhindrar enumeration).
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        passwordResetService.requestReset(request.email());
        return ResponseEntity.ok(Map.of("message", "If the email exists, a reset link has been sent"));
    }

    /**
     * POST /api/auth/reset-password
     * Återställ lösenord med token från reset-mailet.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        try {
            passwordResetService.resetPassword(request.token(), request.newPassword());
            return ResponseEntity.ok(Map.of("message", "Password has been reset successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/auth/account
     * GDPR artikel 17 — Rätt till radering.
     * Raderar ALLA personuppgifter för inloggad användare och loggar ut.
     */
    @DeleteMapping("/account")
    public ResponseEntity<Map<String, String>> deleteAccount(
            HttpServletRequest request,
            HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String userId = null;
        if (auth instanceof OAuth2AuthenticationToken oauthToken) {
            userId = (String) oauthToken.getPrincipal().getAttribute("email");
        } else if (auth instanceof UsernamePasswordAuthenticationToken && auth.isAuthenticated()) {
            userId = auth.getName();
        }

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Inte inloggad"));
        }

        // Radera all data
        userDataDeletionService.deleteAllUserData(userId);

        // Logga ut sessionen
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) {
            request.getSession().invalidate();
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }

        return ResponseEntity.ok(Map.of("message", "Kontot och all tillhörande data har raderats"));
    }

    @GetMapping("/success")
    public String success() {
        return "Login OK!";
    }
}
