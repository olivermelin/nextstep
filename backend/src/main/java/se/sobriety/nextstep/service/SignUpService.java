package se.sobriety.nextstep.service;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import se.sobriety.nextstep.dto.SignUpRequestDto;
import se.sobriety.nextstep.dto.SignUpResponseDto;
import se.sobriety.nextstep.entity.User;
import se.sobriety.nextstep.repository.UserRepository;

@Service
@Transactional
public class SignUpService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserInitializationService userInitializationService;
    private final MailNotificationService mailNotificationService;

    public SignUpService(
            UserRepository userRepository,
            UserInitializationService userInitializationService,
            MailNotificationService mailNotificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.userInitializationService = userInitializationService;
        this.mailNotificationService = mailNotificationService;
    }

    /**
     * Registrera en ny användare
     */
    public SignUpResponseDto signUpNewUser(SignUpRequestDto request) {
        // Validera input
        validateSignUpRequest(request);

        // Kontrollera om email redan finns
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Kryptera lösenord
        String hashedPassword = passwordEncoder.encode(request.password());

        // Skapa user
        User user = new User(request.email(), hashedPassword, request.name());
        user = userRepository.save(user);

        // Initialisera UserProgress och UserSettings
        userInitializationService.ensureUserExistsAndUpdateFromAuth(
                request.email(),
                request.name(),
                request.email()
        );

        // Skicka välkomstmail (asynkront, fel ska inte stoppa signup)
        try {
            mailNotificationService.sendWelcomeEmail(request.email(), request.name());
        } catch (Exception e) {
            System.err.println("Warning: Could not send welcome email to " + request.email() + ": " + e.getMessage());
        }

        return new SignUpResponseDto(
                user.getId().toString(),
                user.getEmail(),
                user.getName(),
                "User registered successfully"
        );
    }

    /**
     * Validera lösenord vid login
     */
    public boolean validatePassword(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    /**
     * Validera signup request
     */
    private void validateSignUpRequest(SignUpRequestDto request) {
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!request.email().contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (request.password() == null || request.password().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
    }
}
