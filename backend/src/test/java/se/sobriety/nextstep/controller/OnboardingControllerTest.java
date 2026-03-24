package se.sobriety.nextstep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.sobriety.nextstep.dto.OnboardingDataDto;
import se.sobriety.nextstep.entity.RecoveryStage;
import se.sobriety.nextstep.entity.UserGoal;
import se.sobriety.nextstep.exception.GlobalExceptionHandler;
import se.sobriety.nextstep.service.OnboardingService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OnboardingControllerTest {

    private static final String USER_ID = "test@example.com";

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OnboardingService onboardingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        OnboardingController controller = new OnboardingController(onboardingService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // Set up SecurityContext so SecurityUtils.verifyUserAccess succeeds
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(USER_ID, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void completeOnboarding_validData_returns200() throws Exception {
        OnboardingDataDto data = new OnboardingDataDto(
                List.of(UserGoal.DAILY_STABILITY, UserGoal.MENTAL_HEALTH),
                null,
                null,
                RecoveryStage.TRYING_TO_QUIT
        );

        doNothing().when(onboardingService).completeOnboarding(eq(USER_ID), any(OnboardingDataDto.class));

        mockMvc.perform(post("/api/onboarding/complete/{userId}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Onboarding completed successfully"));
    }

    @Test
    void completeOnboarding_missingGoals_returns400() throws Exception {
        // userGoals is null, which violates @NotNull
        OnboardingDataDto data = new OnboardingDataDto(
                null,
                null,
                null,
                RecoveryStage.ACTIVE_USE
        );

        mockMvc.perform(post("/api/onboarding/complete/{userId}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completeOnboarding_missingRecoveryStage_returns200() throws Exception {
        // recoveryStage is optional — user can skip step 3
        OnboardingDataDto data = new OnboardingDataDto(
                List.of(UserGoal.REDUCE_SUBSTANCES),
                null,
                null,
                null
        );

        doNothing().when(onboardingService).completeOnboarding(eq(USER_ID), any(OnboardingDataDto.class));

        mockMvc.perform(post("/api/onboarding/complete/{userId}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getOnboardingStatus_completed_returnsTrue() throws Exception {
        when(onboardingService.isOnboardingCompleted(USER_ID)).thenReturn(true);

        mockMvc.perform(get("/api/onboarding/status/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void getOnboardingStatus_notCompleted_returnsFalse() throws Exception {
        when(onboardingService.isOnboardingCompleted(USER_ID)).thenReturn(false);

        mockMvc.perform(get("/api/onboarding/status/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(false));
    }
}
