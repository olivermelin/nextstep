package se.sobriety.nextstep.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.sobriety.nextstep.entity.PasswordResetToken;
import se.sobriety.nextstep.entity.User;
import se.sobriety.nextstep.repository.PasswordResetTokenRepository;
import se.sobriety.nextstep.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private MailNotificationService mailNotificationService;

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordResetService = new PasswordResetService(
                userRepository, tokenRepository, mailNotificationService, "http://localhost:8082");
    }

    // --- requestReset ---

    @Test
    void requestReset_existingUser_generatesTokenAndSendsMail() {
        User user = new User("test@example.com", "hashedPwd", "Test User");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));

        passwordResetService.requestReset("test@example.com");

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken savedToken = tokenCaptor.getValue();

        assertNotNull(savedToken.getToken());
        assertFalse(savedToken.isUsed());
        assertTrue(savedToken.getExpiresAt().isAfter(LocalDateTime.now()));

        verify(mailNotificationService).sendPasswordResetEmail(
                eq("test@example.com"),
                contains("/reset-password/"));
    }

    @Test
    void requestReset_nonexistentEmail_doesNotThrow() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> passwordResetService.requestReset("unknown@example.com"));

        verify(tokenRepository, never()).save(any());
        verify(mailNotificationService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void requestReset_mailFailure_doesNotThrow() {
        User user = new User("test@example.com", "hashedPwd", "Test User");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));
        doThrow(new RuntimeException("Mail server down"))
                .when(mailNotificationService).sendPasswordResetEmail(anyString(), anyString());

        assertDoesNotThrow(() -> passwordResetService.requestReset("test@example.com"));
    }

    @Test
    void requestReset_generatesResetLinkWithCorrectFormat() {
        User user = new User("test@example.com", "hashedPwd", "Test User");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));

        passwordResetService.requestReset("test@example.com");

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailNotificationService).sendPasswordResetEmail(anyString(), linkCaptor.capture());

        String resetLink = linkCaptor.getValue();
        assertTrue(resetLink.startsWith("http://localhost:8082/reset-password/"));
        // UUID format after the last slash
        String token = resetLink.substring(resetLink.lastIndexOf('/') + 1);
        assertEquals(36, token.length()); // UUID length
    }

    // --- resetPassword ---

    @Test
    void resetPassword_validToken_updatesPasswordAndMarksUsed() {
        User user = new User("test@example.com", "oldHashedPwd", "Test User");
        PasswordResetToken token = new PasswordResetToken(
                "valid-token", user, LocalDateTime.now().plusHours(1));

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));

        passwordResetService.resetPassword("valid-token", "newPassword123");

        // Password should be updated (BCrypt hash, not plain text)
        assertNotEquals("oldHashedPwd", user.getPassword());
        assertTrue(user.getPassword().startsWith("$2a$"));
        verify(userRepository).save(user);

        // Token should be marked as used
        assertTrue(token.isUsed());
        verify(tokenRepository).save(token);
    }

    @Test
    void resetPassword_invalidToken_throwsException() {
        when(tokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.resetPassword("invalid-token", "newPassword123"));

        assertEquals("Invalid or expired token", ex.getMessage());
    }

    @Test
    void resetPassword_usedToken_throwsException() {
        User user = new User("test@example.com", "hashedPwd", "Test User");
        PasswordResetToken token = new PasswordResetToken(
                "used-token", user, LocalDateTime.now().plusHours(1));
        token.markUsed();

        when(tokenRepository.findByToken("used-token")).thenReturn(Optional.of(token));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.resetPassword("used-token", "newPassword123"));

        assertEquals("Token has already been used", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_expiredToken_throwsException() {
        User user = new User("test@example.com", "hashedPwd", "Test User");
        PasswordResetToken token = new PasswordResetToken(
                "expired-token", user, LocalDateTime.now().minusHours(1));

        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.resetPassword("expired-token", "newPassword123"));

        assertEquals("Token has expired", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void requestReset_tokenExpiresInOneHour() {
        User user = new User("test@example.com", "hashedPwd", "Test User");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));

        passwordResetService.requestReset("test@example.com");

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());

        LocalDateTime expiresAt = tokenCaptor.getValue().getExpiresAt();
        LocalDateTime expectedMin = LocalDateTime.now().plusMinutes(59);
        LocalDateTime expectedMax = LocalDateTime.now().plusMinutes(61);
        assertTrue(expiresAt.isAfter(expectedMin) && expiresAt.isBefore(expectedMax),
                "Token should expire in approximately 1 hour");
    }
}
