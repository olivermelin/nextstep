package se.sobriety.nextstep.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.sobriety.nextstep.dto.AchievementDto;
import se.sobriety.nextstep.dto.CategoryProgressResponseDto;
import se.sobriety.nextstep.dto.ProgressResponseDto;
import se.sobriety.nextstep.entity.UserProgress;
import se.sobriety.nextstep.exception.GlobalExceptionHandler;
import se.sobriety.nextstep.service.UserProgressService;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProgressControllerTest {

    private static final String USER_ID = "test@example.com";

    private MockMvc mockMvc;

    @Mock
    private UserProgressService userProgressService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ProgressController controller = new ProgressController(userProgressService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(USER_ID, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getProgress_returns200() throws Exception {
        ProgressResponseDto dto = new ProgressResponseDto(USER_ID, 150, 3, "2026-01-15T10:00:00");
        when(userProgressService.getTotalProgress(USER_ID)).thenReturn(dto);

        mockMvc.perform(get("/api/progress/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.totalPoints").value(150))
                .andExpect(jsonPath("$.level").value(3))
                .andExpect(jsonPath("$.lastUpdated").value("2026-01-15T10:00:00"));
    }

    @Test
    void getCategoryProgress_validDays_returns200() throws Exception {
        CategoryProgressResponseDto dto = new CategoryProgressResponseDto(USER_ID, 7, List.of());
        when(userProgressService.getCategoryProgress(USER_ID, 7)).thenReturn(dto);

        mockMvc.perform(get("/api/progress/{userId}/categories", USER_ID)
                        .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.days").value(7))
                .andExpect(jsonPath("$.categories").isArray());
    }

    @Test
    void getCategoryProgress_invalidDays_returns400() throws Exception {
        mockMvc.perform(get("/api/progress/{userId}/categories", USER_ID)
                        .param("days", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Days must be between 1 and 365"));

        mockMvc.perform(get("/api/progress/{userId}/categories", USER_ID)
                        .param("days", "400"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Days must be between 1 and 365"));
    }

    @Test
    void getAchievements_returns200() throws Exception {
        List<AchievementDto> achievements = List.of(
                new AchievementDto(1, "First Steps", "Complete your first challenge", true, 10, 1),
                new AchievementDto(2, "Rising Star", "Reach level 5", false, 500, 5)
        );
        when(userProgressService.getUserAchievements(USER_ID)).thenReturn(achievements);

        mockMvc.perform(get("/api/progress/{userId}/achievements", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("First Steps"))
                .andExpect(jsonPath("$[0].unlocked").value(true))
                .andExpect(jsonPath("$[1].title").value("Rising Star"))
                .andExpect(jsonPath("$[1].unlocked").value(false));
    }

    @Test
    void addPoints_validPoints_returns200() throws Exception {
        UserProgress updated = new UserProgress(USER_ID);
        when(userProgressService.addPoints(USER_ID, 10)).thenReturn(updated);

        mockMvc.perform(post("/api/progress/{userId}/add", USER_ID)
                        .param("points", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.totalPoints").value(0))
                .andExpect(jsonPath("$.level").value(1));
    }

    @Test
    void addPoints_invalidPoints_returns400() throws Exception {
        mockMvc.perform(post("/api/progress/{userId}/add", USER_ID)
                        .param("points", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid points: must be between 1 and 100"));

        mockMvc.perform(post("/api/progress/{userId}/add", USER_ID)
                        .param("points", "200"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid points: must be between 1 and 100"));
    }

    @Test
    void deleteUser_returns200() throws Exception {
        mockMvc.perform(delete("/api/progress/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().string("User " + USER_ID + " deleted (if existed)"));

        verify(userProgressService).deleteUser(USER_ID);
    }
}
