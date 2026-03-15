package se.sobriety.nextstep.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import se.sobriety.nextstep.dto.ForgotPasswordRequestDto;
import se.sobriety.nextstep.dto.LoginRequestDto;
import se.sobriety.nextstep.dto.ResetPasswordRequestDto;
import se.sobriety.nextstep.dto.SignUpRequestDto;
import se.sobriety.nextstep.dto.SignUpResponseDto;
import se.sobriety.nextstep.entity.User;
import se.sobriety.nextstep.repository.UserRepository;
import se.sobriety.nextstep.service.OnboardingService;
import se.sobriety.nextstep.service.PasswordResetService;
import se.sobriety.nextstep.service.SignUpService;
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

    public AuthController(UserProgressService progressService,
                         UserInitializationService initializationService,
                         OnboardingService onboardingService,
                         SignUpService signUpService,
                         PasswordResetService passwordResetService,
                         UserRepository userRepository) {
        this.initializationService = initializationService;
        this.progressService = progressService;
        this.onboardingService = onboardingService;
        this.signUpService = signUpService;
        this.passwordResetService = passwordResetService;
        this.userRepository = userRepository;
    }

    /**
     * GET /api/auth/me
     * OAuth2-autentisering - hämta inloggad användare
     * Användare skapas AUTOMATISKT vid första OAuth2-inloggningen (ingen signup behövs)
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(OAuth2AuthenticationToken auth) {
        if (auth == null) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }

        String email = (String) auth.getPrincipal().getAttribute("email");
        String name = (String) auth.getPrincipal().getAttribute("name");

        // Använd email som konsekvent userId genom hela systemet
        String resolvedUserId = email;

        // OAuth2: Skapa användare automatiskt vid första inloggningen
        initializationService.ensureUserExistsAndUpdateFromAuth(resolvedUserId, name, email);

        // Hämta onboarding status
        boolean onboardingCompleted = onboardingService.isOnboardingCompleted(resolvedUserId);

        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "id", resolvedUserId,
                "name", name != null ? name : "",
                "email", email != null ? email : "",
                "picture", auth.getPrincipal().getAttribute("picture") != null ? auth.getPrincipal().getAttribute("picture") : "",
                "onboardingCompleted", onboardingCompleted
        ));
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
     * Email/Password-inloggning - validera credentials
     * Används ENDAST för email/password-flow, INTE för OAuth2
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequestDto request) {
        String email = request.email();
        String password = request.password();

        try {
            // Validera lösenord
            boolean isValid = signUpService.validatePassword(email, password);

            if (!isValid) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Invalid credentials"));
            }

            // Hämta användare
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Säkerställ att UserProgress och UserSettings finns
            initializationService.ensureUserExistsAndUpdateFromAuth(email, user.getName(), email);

            // Hämta onboarding status
            boolean onboardingCompleted = onboardingService.isOnboardingCompleted(email);

            return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "id", email,
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "onboardingCompleted", onboardingCompleted
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid credentials"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "An error occurred during login"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            OAuth2AuthenticationToken authentication) {

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

    @GetMapping("/success")
    public String success() {
        return "Login OK!";
    }
}
