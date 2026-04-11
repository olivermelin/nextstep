package se.sobriety.nextstep.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import se.sobriety.nextstep.dto.UserChallengeOutDto;
import se.sobriety.nextstep.exception.GlobalExceptionHandler;
import se.sobriety.nextstep.service.UserChallengeService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserChallengesControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserChallengeService userChallengeService;

    private static final String USER_ID = "test@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        UserChallengesController controller = new UserChallengesController(userChallengeService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        setUpSecurityContext(USER_ID);
    }

    private void setUpSecurityContext(String email) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(email, null, List.of()));
        SecurityContextHolder.setContext(context);
    }

    private UserChallengeOutDto sampleChallenge(String status) {
        return new UserChallengeOutDto(
                1L, 10L, "Morning Walk", "Physical", "Easy",
                30, status,
                LocalDateTime.of(2026, 1, 1, 8, 0),
                "COMPLETED".equals(status) ? LocalDateTime.of(2026, 1, 1, 8, 30) : null,
                "COMPLETED".equals(status) ? 50 : 0,
                30
        );
    }

    @Test
    void getUserChallenges_returns200WithList() throws Exception {
        List<UserChallengeOutDto> challenges = List.of(
                sampleChallenge("ACTIVE"),
                sampleChallenge("COMPLETED")
        );
        when(userChallengeService.getUserChallenges(USER_ID)).thenReturn(challenges);

        mockMvc.perform(get("/api/user-challenges/user/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].challengeName").value("Morning Walk"));
    }

    @Test
    void getUserCompletedChallenges_returns200WithCompletedOnly() throws Exception {
        List<UserChallengeOutDto> completed = List.of(sampleChallenge("COMPLETED"));
        when(userChallengeService.getUserCompletedChallenges(USER_ID)).thenReturn(completed);

        mockMvc.perform(get("/api/user-challenges/user/{userId}/completed", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[0].pointsEarned").value(50));
    }

    @Test
    void getUserActiveChallenges_returns200WithActiveOnly() throws Exception {
        List<UserChallengeOutDto> active = List.of(sampleChallenge("ACTIVE"));
        when(userChallengeService.getUserActiveChallenges(USER_ID)).thenReturn(active);

        mockMvc.perform(get("/api/user-challenges/user/{userId}/active", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void startChallenge_returns200WithStartedChallenge() throws Exception {
        UserChallengeOutDto started = sampleChallenge("ACTIVE");
        when(userChallengeService.startChallenge(USER_ID, 10L)).thenReturn(started);

        mockMvc.perform(post("/api/user-challenges/user/{userId}/challenge/{challengeId}/start", USER_ID, 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.challengeId").value(10))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void completeChallenge_returns200WithCompletedChallenge() throws Exception {
        UserChallengeOutDto completed = sampleChallenge("COMPLETED");
        when(userChallengeService.completeChallenge(eq(USER_ID), eq(10L), anyInt(), anyBoolean())).thenReturn(completed);

        mockMvc.perform(post("/api/user-challenges/user/{userId}/complete/{challengeId}", USER_ID, 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.pointsEarned").value(50));
    }

    @Test
    void getUserChallenges_returnsEmptyList() throws Exception {
        when(userChallengeService.getUserChallenges(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/user-challenges/user/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
