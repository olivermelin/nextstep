package se.sobriety.nextstep.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import se.sobriety.nextstep.entity.PasswordResetToken;
import se.sobriety.nextstep.entity.User;
import se.sobriety.nextstep.repository.PasswordResetTokenRepository;
import se.sobriety.nextstep.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final MailNotificationService mailNotificationService;
    private final PasswordEncoder passwordEncoder;
    private final String frontendBaseUrl;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                MailNotificationService mailNotificationService,
                                PasswordEncoder passwordEncoder,
                                @Value("${app.frontend-base-url:http://localhost:8082}") String frontendBaseUrl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.mailNotificationService = mailNotificationService;
        this.passwordEncoder = passwordEncoder;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    /**
     * Begär lösenordsåterställning. Genererar token och skickar mail.
     * Returnerar alltid utan fel oavsett om emailen finns - förhindrar email enumeration.
     */
    @Transactional
    public void requestReset(String email) {
        var userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            log.debug("Password reset requested for non-existent email");
            return;
        }

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

        PasswordResetToken resetToken = new PasswordResetToken(token, user, expiresAt);
        tokenRepository.save(resetToken);

        String resetLink = frontendBaseUrl + "/reset-password/" + token;

        try {
            mailNotificationService.sendPasswordResetEmail(user.getEmail(), resetLink);
        } catch (Exception e) {
            log.error("Failed to send password reset email: {}", e.getMessage());
        }
    }

    /**
     * Återställ lösenord med token.
     * Validerar att token finns, inte är använd och inte har utgått.
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        // Validera lösenordskomplexitet
        validatePassword(newPassword);

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("Token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.markUsed();
        tokenRepository.save(resetToken);

        log.info("Password reset successful for user ID: {}", user.getId());
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
    }
}
