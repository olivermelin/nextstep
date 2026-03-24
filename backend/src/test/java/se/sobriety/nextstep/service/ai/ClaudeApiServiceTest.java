package se.sobriety.nextstep.service.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.sobriety.nextstep.config.AICoachProperties;
import se.sobriety.nextstep.dto.CoachMessageResponse;
import se.sobriety.nextstep.dto.SuggestedChallengeDto;
import se.sobriety.nextstep.entity.*;
import se.sobriety.nextstep.repository.UserChallengeRepository;
import se.sobriety.nextstep.service.UserProgressService;
import se.sobriety.nextstep.service.UserSettingsService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ClaudeApiServiceTest {

    @Mock
    private AICoachProperties properties;
    @Mock
    private ConversationService conversationService;
    @Mock
    private CrisisDetectionService crisisDetectionService;
    @Mock
    private SystemPromptBuilder systemPromptBuilder;
    @Mock
    private UserSettingsService userSettingsService;
    @Mock
    private UserProgressService userProgressService;
    @Mock
    private UserChallengeRepository userChallengeRepository;
    @Mock
    private ChallengeRecommendationParser challengeRecommendationParser;

    private ClaudeApiService claudeApiService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        claudeApiService = new ClaudeApiService(
                properties,
                conversationService,
                crisisDetectionService,
                systemPromptBuilder,
                userSettingsService,
                userProgressService,
                userChallengeRepository,
                challengeRecommendationParser
        );
    }

    // ---- sendMessage: CRITICAL crisis ----

    @Test
    void sendMessage_criticalCrisis_returnsCrisisResponseWithoutApiCall() {
        String userId = "user-1";
        CoachSession session = new CoachSession(userId);
        when(conversationService.getOrCreateActiveSession(userId)).thenReturn(session);
        when(crisisDetectionService.analyze("I want to end it")).thenReturn(CrisisLevel.CRITICAL);
        when(crisisDetectionService.getCrisisResponse()).thenReturn("Ring 112 om du är i fara.");

        CoachMessageResponse response = claudeApiService.sendMessage(userId, "I want to end it");

        assertEquals(CrisisLevel.CRITICAL, response.crisisLevel());
        assertEquals("Ring 112 om du är i fara.", response.response());
        assertEquals(session.getSessionId(), response.sessionId());
        // Verify crisis message was saved
        verify(conversationService).saveMessageWithCrisis(session, "user", "I want to end it", CrisisLevel.CRITICAL);
        verify(conversationService).saveMessage(session, "assistant", "Ring 112 om du är i fara.");
    }

    // ---- sendMessage: ELEVATED crisis (still calls API) ----

    @Test
    void sendMessage_elevatedCrisis_savesWithCrisisFlag() {
        String userId = "user-1";
        CoachSession session = new CoachSession(userId);
        when(conversationService.getOrCreateActiveSession(userId)).thenReturn(session);
        when(crisisDetectionService.analyze("I feel really bad")).thenReturn(CrisisLevel.ELEVATED);

        // Mock the system prompt build chain
        UserSettings settings = new UserSettings(userId);
        UserProgress progress = new UserProgress(userId);
        when(userSettingsService.getOrCreateSettings(userId)).thenReturn(settings);
        when(userProgressService.getOrCreateUser(userId)).thenReturn(progress);
        when(userChallengeRepository.findByUserIdAndCompleted(userId, true)).thenReturn(List.of());
        when(systemPromptBuilder.build(any(), any(), anyInt(), any(), any())).thenReturn("system prompt");
        when(conversationService.buildMessageHistory(session.getSessionId())).thenReturn(List.of());

        // The API call will fail (no real API key), but fallback is tested
        when(userSettingsService.getOrCreateSettings(userId)).thenReturn(settings);
        when(challengeRecommendationParser.parse(anyString()))
                .thenReturn(new ChallengeRecommendationParser.ParseResult("fallback", List.of()));

        CoachMessageResponse response = claudeApiService.sendMessage(userId, "I feel really bad");

        // Should have saved with ELEVATED crisis
        verify(conversationService).saveMessageWithCrisis(session, "user", "I feel really bad", CrisisLevel.ELEVATED);
        assertEquals(CrisisLevel.ELEVATED, response.crisisLevel());
    }

    // ---- sendMessage: NONE crisis (normal flow, API fails → fallback) ----

    @Test
    void sendMessage_noCrisis_apiFailure_returnsFallback() {
        String userId = "user-1";
        CoachSession session = new CoachSession(userId);
        when(conversationService.getOrCreateActiveSession(userId)).thenReturn(session);
        when(crisisDetectionService.analyze("Hello")).thenReturn(CrisisLevel.NONE);

        UserSettings settings = new UserSettings(userId);
        UserProgress progress = new UserProgress(userId);
        when(userSettingsService.getOrCreateSettings(userId)).thenReturn(settings);
        when(userProgressService.getOrCreateUser(userId)).thenReturn(progress);
        when(userChallengeRepository.findByUserIdAndCompleted(userId, true)).thenReturn(List.of());
        when(systemPromptBuilder.build(any(), any(), anyInt(), any(), any())).thenReturn("system prompt");
        when(conversationService.buildMessageHistory(session.getSessionId())).thenReturn(List.of());
        when(challengeRecommendationParser.parse(anyString()))
                .thenReturn(new ChallengeRecommendationParser.ParseResult("fallback response", List.of()));

        CoachMessageResponse response = claudeApiService.sendMessage(userId, "Hello");

        assertEquals(CrisisLevel.NONE, response.crisisLevel());
        assertNotNull(response.response());
        assertEquals(session.getSessionId(), response.sessionId());
        // Normal message saved (no crisis flag)
        verify(conversationService).saveMessage(session, "user", "Hello");
    }

    // ---- isAvailable ----

    @Test
    void isAvailable_disabledConfig_returnsFalse() {
        when(properties.isEnabled()).thenReturn(false);

        assertFalse(claudeApiService.isAvailable());
    }

    @Test
    void isAvailable_enabledWithAnthropicKey_returnsTrue() {
        when(properties.isEnabled()).thenReturn(true);
        when(properties.isUseGroq()).thenReturn(false);
        when(properties.getAnthropicApiKey()).thenReturn("sk-test-key");

        assertTrue(claudeApiService.isAvailable());
    }

    @Test
    void isAvailable_enabledWithGroqKey_returnsTrue() {
        when(properties.isEnabled()).thenReturn(true);
        when(properties.isUseGroq()).thenReturn(true);
        when(properties.getGroqApiKey()).thenReturn("gsk-test-key");

        assertTrue(claudeApiService.isAvailable());
    }

    @Test
    void isAvailable_enabledNoKey_returnsFalse() {
        when(properties.isEnabled()).thenReturn(true);
        when(properties.isUseGroq()).thenReturn(false);
        when(properties.getAnthropicApiKey()).thenReturn(null);

        assertFalse(claudeApiService.isAvailable());
    }

    @Test
    void isAvailable_enabledBlankKey_returnsFalse() {
        when(properties.isEnabled()).thenReturn(true);
        when(properties.isUseGroq()).thenReturn(false);
        when(properties.getAnthropicApiKey()).thenReturn("  ");

        assertFalse(claudeApiService.isAvailable());
    }
}
