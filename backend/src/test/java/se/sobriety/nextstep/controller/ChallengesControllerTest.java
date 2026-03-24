package se.sobriety.nextstep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.sobriety.nextstep.dto.ChallengeInDto;
import se.sobriety.nextstep.dto.ChallengeOutDto;
import se.sobriety.nextstep.entity.ChallengeCategory;
import se.sobriety.nextstep.entity.ChallengeDifficulty;
import se.sobriety.nextstep.exception.GlobalExceptionHandler;
import se.sobriety.nextstep.service.ChallengeService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ChallengesControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private ChallengeService challengeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ChallengesController controller = new ChallengesController(challengeService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        SecurityContextHolder.clearContext();
    }

    // ---- Helper methods ----

    private ChallengeOutDto sampleChallenge(Long id, String title, String category, String difficulty) {
        return new ChallengeOutDto(
                id, title, "A sample challenge description", 15,
                difficulty, category,
                "https://youtube.com/watch?v=abc123",
                "Follow the steps carefully",
                LocalDateTime.of(2025, 1, 15, 10, 0),
                false
        );
    }

    private void setUpSecurityContext() {
        var auth = new UsernamePasswordAuthenticationToken("test@example.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ---- GET /api/challenges ----

    @Test
    void getAllChallenges_returnsList() throws Exception {
        List<ChallengeOutDto> challenges = List.of(
                sampleChallenge(1L, "Mindful Breathing", "MENTAL_HEALTH", "EASY"),
                sampleChallenge(2L, "Morning Run", "PHYSICAL_ACTIVITY", "MEDIUM")
        );
        when(challengeService.getAllChallenges()).thenReturn(challenges);

        mockMvc.perform(get("/api/challenges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Mindful Breathing"))
                .andExpect(jsonPath("$[1].title").value("Morning Run"));
    }

    // ---- GET /api/challenges/{id} ----

    @Test
    void getChallengeById_returnsChallenge() throws Exception {
        ChallengeOutDto challenge = sampleChallenge(1L, "Mindful Breathing", "MENTAL_HEALTH", "EASY");
        when(challengeService.getChallengeById(1L)).thenReturn(challenge);

        mockMvc.perform(get("/api/challenges/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Mindful Breathing"))
                .andExpect(jsonPath("$.category").value("MENTAL_HEALTH"));
    }

    @Test
    void getChallengeById_notFound_returns400() throws Exception {
        when(challengeService.getChallengeById(999L))
                .thenThrow(new IllegalArgumentException("Challenge not found with id: 999"));

        mockMvc.perform(get("/api/challenges/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Challenge not found with id: 999"));
    }

    // ---- GET /api/challenges/category/{category} ----

    @Test
    void getChallengesByCategory_returnsList() throws Exception {
        List<ChallengeOutDto> challenges = List.of(
                sampleChallenge(1L, "Mindful Breathing", "MENTAL_HEALTH", "EASY")
        );
        when(challengeService.getChallengesByCategory(ChallengeCategory.MENTAL_HEALTH))
                .thenReturn(challenges);

        mockMvc.perform(get("/api/challenges/category/MENTAL_HEALTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("MENTAL_HEALTH"));
    }

    // ---- GET /api/challenges/difficulty/{difficulty} ----

    @Test
    void getChallengesByDifficulty_returnsList() throws Exception {
        List<ChallengeOutDto> challenges = List.of(
                sampleChallenge(1L, "Hard Workout", "PHYSICAL_ACTIVITY", "HARD")
        );
        when(challengeService.getChallengesByDifficulty(ChallengeDifficulty.HARD))
                .thenReturn(challenges);

        mockMvc.perform(get("/api/challenges/difficulty/HARD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].difficulty").value("HARD"));
    }

    // ---- GET /api/challenges/category/{category}/difficulty/{difficulty} ----

    @Test
    void getChallengesByCategoryAndDifficulty_returnsList() throws Exception {
        List<ChallengeOutDto> challenges = List.of(
                sampleChallenge(1L, "Creative Sketch", "FOCUS_DISCIPLINE", "MEDIUM")
        );
        when(challengeService.getChallengesByCategoryAndDifficulty(
                ChallengeCategory.FOCUS_DISCIPLINE,
                ChallengeDifficulty.MEDIUM))
                .thenReturn(challenges);

        mockMvc.perform(get("/api/challenges/category/FOCUS_DISCIPLINE/difficulty/MEDIUM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("FOCUS_DISCIPLINE"))
                .andExpect(jsonPath("$[0].difficulty").value("MEDIUM"));
    }

    // ---- GET /api/challenges/user/{userId} ----

    @Test
    void getAllChallengesForUser_withAuth_returnsList() throws Exception {
        setUpSecurityContext();
        List<ChallengeOutDto> challenges = List.of(
                sampleChallenge(1L, "Mindful Breathing", "MENTAL_HEALTH", "EASY")
        );
        when(challengeService.getAllChallengesForUser("test@example.com")).thenReturn(challenges);

        mockMvc.perform(get("/api/challenges/user/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Mindful Breathing"));
    }

    // ---- GET /api/challenges/user/{userId}/category/{category} ----

    @Test
    void getChallengesByCategoryForUser_withAuth_returnsList() throws Exception {
        setUpSecurityContext();
        List<ChallengeOutDto> challenges = List.of(
                sampleChallenge(2L, "Morning Run", "PHYSICAL_ACTIVITY", "MEDIUM")
        );
        when(challengeService.getChallengesByCategoryForUser(
                ChallengeCategory.PHYSICAL_ACTIVITY, "test@example.com"))
                .thenReturn(challenges);

        mockMvc.perform(get("/api/challenges/user/test@example.com/category/PHYSICAL_ACTIVITY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("PHYSICAL_ACTIVITY"));
    }

    // ---- POST /api/challenges ----

    @Test
    void createChallenge_validRequest_returns201() throws Exception {
        ChallengeInDto request = new ChallengeInDto(
                "New Challenge", "A new challenge", 30,
                "EASY", "MENTAL_HEALTH", null, null
        );
        ChallengeOutDto response = sampleChallenge(10L, "New Challenge", "MENTAL_HEALTH", "EASY");
        when(challengeService.createChallenge(any(ChallengeInDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/challenges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("New Challenge"));
    }

    @Test
    void createChallenge_missingTitle_returns400() throws Exception {
        ChallengeInDto request = new ChallengeInDto(
                "", "A description", 30,
                "EASY", "MENTAL_HEALTH", null, null
        );

        mockMvc.perform(post("/api/challenges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ---- DELETE /api/challenges/{id} ----

    @Test
    void deleteChallenge_returns204() throws Exception {
        doNothing().when(challengeService).deleteChallenge(1L);

        mockMvc.perform(delete("/api/challenges/1"))
                .andExpect(status().isNoContent());

        verify(challengeService).deleteChallenge(1L);
    }
}
