package se.sobriety.nextstep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.sobriety.nextstep.dto.CoachMessageRequest;
import se.sobriety.nextstep.dto.CoachMessageResponse;
import se.sobriety.nextstep.entity.CrisisLevel;
import se.sobriety.nextstep.exception.GlobalExceptionHandler;
import se.sobriety.nextstep.service.QuotaService;
import se.sobriety.nextstep.service.ai.ClaudeApiService;
import se.sobriety.nextstep.service.ai.ConversationService;
import se.sobriety.nextstep.service.ai.QuickPromptService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AICoachControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ClaudeApiService claudeApiService;

    @Mock
    private ConversationService conversationService;

    @Mock
    private QuotaService quotaService;

    @Mock
    private QuickPromptService quickPromptService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        AICoachController controller = new AICoachController(claudeApiService, conversationService, quotaService, quickPromptService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        SecurityContextHolder.clearContext();
    }

    private void setUpSecurityContext() {
        var auth = new UsernamePasswordAuthenticationToken("test@example.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ---- POST /api/coach/message ----

    @Test
    void sendMessage_validRequest_returnsResponse() throws Exception {
        setUpSecurityContext();
        CoachMessageResponse response = new CoachMessageResponse("I hear you", CrisisLevel.NONE, "session-123");
        when(claudeApiService.sendMessage(eq("test@example.com"), eq("I need help"), any()))
                .thenReturn(response);

        CoachMessageRequest request = new CoachMessageRequest("I need help", null);

        mockMvc.perform(post("/api/coach/message")
                        .param("userId", "test@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("I hear you"))
                .andExpect(jsonPath("$.crisisLevel").value("NONE"))
                .andExpect(jsonPath("$.sessionId").value("session-123"));
    }

    // ---- GET /api/coach/status ----

    @Test
    void getStatus_available_returnsOnline() throws Exception {
        when(claudeApiService.isAvailable()).thenReturn(true);

        mockMvc.perform(get("/api/coach/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiAvailable").value(true))
                .andExpect(jsonPath("$.activeProvider").value("claude/groq"))
                .andExpect(jsonPath("$.status").value("online"));
    }

    @Test
    void getStatus_noneAvailable_returnsFallback() throws Exception {
        when(claudeApiService.isAvailable()).thenReturn(false);

        mockMvc.perform(get("/api/coach/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiAvailable").value(false))
                .andExpect(jsonPath("$.activeProvider").value("none"))
                .andExpect(jsonPath("$.status").value("fallback mode"));
    }
}
