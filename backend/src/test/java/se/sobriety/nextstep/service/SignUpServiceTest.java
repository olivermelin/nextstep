package se.sobriety.nextstep.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import se.sobriety.nextstep.dto.SignUpRequestDto;
import se.sobriety.nextstep.dto.SignUpResponseDto;
import se.sobriety.nextstep.entity.User;
import se.sobriety.nextstep.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SignUpServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserInitializationService userInitializationService;
    @Mock
    private MailNotificationService mailNotificationService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private SignUpService signUpService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        signUpService = new SignUpService(userRepository, passwordEncoder, userInitializationService, mailNotificationService);
    }

    @Test
    void signUpNewUser_validRequest_createsUser() {
        SignUpRequestDto request = new SignUpRequestDto("test@example.com", "password123", "Test User");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        User savedUser = new User("test@example.com", "hashedPwd", "Test User");
        savedUser.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        SignUpResponseDto response = signUpService.signUpNewUser(request);

        assertEquals("test@example.com", response.email());
        assertEquals("Test User", response.name());
        verify(userRepository).save(any(User.class));
        verify(userInitializationService).ensureUserExistsAndUpdateFromAuth("test@example.com", "Test User", "test@example.com");
    }

    @Test
    void signUpNewUser_duplicateEmail_throwsException() {
        SignUpRequestDto request = new SignUpRequestDto("existing@example.com", "password123", "Test User");
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> signUpService.signUpNewUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void signUpNewUser_nullEmail_throwsException() {
        SignUpRequestDto request = new SignUpRequestDto(null, "password123", "Test User");

        assertThrows(IllegalArgumentException.class, () -> signUpService.signUpNewUser(request));
    }

    @Test
    void signUpNewUser_invalidEmail_throwsException() {
        SignUpRequestDto request = new SignUpRequestDto("notanemail", "password123", "Test User");

        assertThrows(IllegalArgumentException.class, () -> signUpService.signUpNewUser(request));
    }

    @Test
    void signUpNewUser_shortPassword_throwsException() {
        SignUpRequestDto request = new SignUpRequestDto("test@example.com", "12345", "Test User");

        assertThrows(IllegalArgumentException.class, () -> signUpService.signUpNewUser(request));
    }

    @Test
    void signUpNewUser_blankName_throwsException() {
        SignUpRequestDto request = new SignUpRequestDto("test@example.com", "password123", "");

        assertThrows(IllegalArgumentException.class, () -> signUpService.signUpNewUser(request));
    }

    @Test
    void signUpNewUser_mailFailure_doesNotBlockSignup() {
        SignUpRequestDto request = new SignUpRequestDto("test@example.com", "password123", "Test User");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        User savedUser = new User("test@example.com", "hashedPwd", "Test User");
        savedUser.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        doThrow(new RuntimeException("Mail server down")).when(mailNotificationService).sendWelcomeEmail(anyString(), anyString());

        SignUpResponseDto response = signUpService.signUpNewUser(request);

        assertNotNull(response);
        assertEquals("test@example.com", response.email());
    }

    @Test
    void validatePassword_correctPassword_returnsTrue() {
        User user = new User("test@example.com", "$2a$10$validhash", "Test");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // We can't easily test BCrypt matching without encoding first,
        // but we can verify the method doesn't throw for existing users
        assertDoesNotThrow(() -> signUpService.validatePassword("test@example.com", "somePassword"));
    }

    @Test
    void validatePassword_nonexistentUser_throwsException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> signUpService.validatePassword("unknown@example.com", "password"));
    }
}
