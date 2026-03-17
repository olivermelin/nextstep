package se.sobriety.nextstep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserProgressService progressService;

    @Mock
    private UserInitializationService initializationService;

    @Mock
    private OnboardingService onboardingService;

    @Mock
    private SignUpService signUpService;

    @Mock
    private PasswordResetService passwordResetService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        AuthController controller = new AuthController(
                progressService,
                initializationService,
                onboardingService,
                signUpService,
                passwordResetService,
                userRepository,
                authenticationManager
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ---- POST /api/auth/signup ----

    @Test
    void signup_validRequest_returns201() throws Exception {
        SignUpRequestDto request = new SignUpRequestDto("test@example.com", "password123", "Test User");
        SignUpResponseDto response = new SignUpResponseDto("test@example.com", "test@example.com", "Test User", "Account created");

        when(signUpService.signUpNewUser(any(SignUpRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void signup_invalidEmail_returns400() throws Exception {
        // With standalone MockMvc, Bean Validation must be triggered by the controller.
        // @Valid on the controller method triggers validation when the resolver is configured.
        // standaloneSetup does register a default validator if JSR-380 is on classpath.
        SignUpRequestDto request = new SignUpRequestDto("not-an-email", "password123", "Test User");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_duplicateEmail_returns400() throws Exception {
        SignUpRequestDto request = new SignUpRequestDto("existing@example.com", "password123", "Test User");

        when(signUpService.signUpNewUser(any(SignUpRequestDto.class)))
                .thenThrow(new IllegalArgumentException("Email already in use"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already in use"));
    }

    @Test
    void signup_missingName_returns400() throws Exception {
        SignUpRequestDto request = new SignUpRequestDto("test@example.com", "password123", "");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ---- POST /api/auth/login ----

    @Test
    void login_validCredentials_returns200() throws Exception {
        LoginRequestDto request = new LoginRequestDto("test@example.com", "password123");

        Authentication mockAuth = new UsernamePasswordAuthenticationToken(
                "test@example.com", null, List.of());
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);

        User user = new User("test@example.com", "hashedPwd", "Test User");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(onboardingService.isOnboardingCompleted("test@example.com")).thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.onboardingCompleted").value(false));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        LoginRequestDto request = new LoginRequestDto("test@example.com", "wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void login_nonexistentUser_returns401() throws Exception {
        LoginRequestDto request = new LoginRequestDto("unknown@example.com", "password123");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void login_invalidEmailFormat_returns400() throws Exception {
        LoginRequestDto request = new LoginRequestDto("not-an-email", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ---- GET /api/auth/me ----

    @Test
    void me_notAuthenticated_returnsAuthenticatedFalse() throws Exception {
        // Without an OAuth2AuthenticationToken, the auth parameter is null
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false));
    }

    // ---- POST /api/auth/forgot-password ----

    @Test
    void forgotPassword_validEmail_returns200() throws Exception {
        ForgotPasswordRequestDto request = new ForgotPasswordRequestDto("test@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If the email exists, a reset link has been sent"));

        verify(passwordResetService).requestReset("test@example.com");
    }

    @Test
    void forgotPassword_nonexistentEmail_stillReturns200() throws Exception {
        ForgotPasswordRequestDto request = new ForgotPasswordRequestDto("nonexistent@example.com");

        doNothing().when(passwordResetService).requestReset(anyString());

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If the email exists, a reset link has been sent"));
    }

    @Test
    void forgotPassword_invalidEmail_returns400() throws Exception {
        ForgotPasswordRequestDto request = new ForgotPasswordRequestDto("not-an-email");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ---- POST /api/auth/reset-password ----

    @Test
    void resetPassword_validToken_returns200() throws Exception {
        ResetPasswordRequestDto request = new ResetPasswordRequestDto("valid-token", "newPassword123");

        doNothing().when(passwordResetService).resetPassword("valid-token", "newPassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password has been reset successfully"));
    }

    @Test
    void resetPassword_invalidToken_returns400() throws Exception {
        ResetPasswordRequestDto request = new ResetPasswordRequestDto("invalid-token", "newPassword123");

        doThrow(new IllegalArgumentException("Invalid or expired token"))
                .when(passwordResetService).resetPassword("invalid-token", "newPassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid or expired token"));
    }

    @Test
    void resetPassword_shortPassword_returns400() throws Exception {
        ResetPasswordRequestDto request = new ResetPasswordRequestDto("valid-token", "short");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
