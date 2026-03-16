package se.sobriety.nextstep.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.client.ChatClient;
import se.sobriety.nextstep.config.AICoachProperties;
import se.sobriety.nextstep.dto.AICoachRequestDto;
import se.sobriety.nextstep.dto.AICoachResponseDto;
import se.sobriety.nextstep.entity.*;
import se.sobriety.nextstep.repository.UserChallengeRepository;
import se.sobriety.nextstep.service.ai.AIContextBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AICoachServiceTest {

    @Mock private AICoachProperties properties;
    @Mock private AIContextBuilder contextBuilder;
    @Mock private UserSettingsService userSettingsService;
    @Mock private UserProgressService userProgressService;
    @Mock private UserChallengeRepository userChallengeRepository;
    @Mock private ChatClient.Builder chatClientBuilder;
    @Mock private ChatClient chatClient;
    @Mock private ChatClient.ChatClientRequestSpec requestSpec;
    @Mock private ChatClient.CallResponseSpec callResponseSpec;

    private AICoachService service;

    private static final String USER_ID = "user@test.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        service = new AICoachService(
                properties, contextBuilder, userSettingsService,
                userProgressService, userChallengeRepository, chatClientBuilder
        );
    }

    // --- Helper to set up ChatClient fluent chain ---

    private void stubChatClientResponse(String response) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(response);
    }

    private void stubChatClientThrows(RuntimeException ex) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenThrow(ex);
    }

    // --- AI disabled: fallback ---

    @Test
    void chat_aiDisabled_returnsFallbackResponse() {
        when(properties.isEnabled()).thenReturn(false);
        when(contextBuilder.buildFallbackResponse(isNull())).thenReturn("Fallback message");

        AICoachRequestDto request = new AICoachRequestDto("Hello", null, false);
        AICoachResponseDto response = service.chat(request);

        assertEquals("Fallback message", response.message());
        assertNull(response.userId());
        assertFalse(response.contextUsed());
        verify(chatClient, never()).prompt();
    }

    @Test
    void chat_aiDisabled_withUserId_includesUserNameInFallback() {
        when(properties.isEnabled()).thenReturn(false);

        UserSettings settings = new UserSettings(USER_ID);
        settings.setName("Anna");
        when(userSettingsService.getOrCreateSettings(USER_ID)).thenReturn(settings);
        when(contextBuilder.buildFallbackResponse("Anna")).thenReturn("Anna, keep going!");

        AICoachRequestDto request = new AICoachRequestDto("Hello", USER_ID, false);
        AICoachResponseDto response = service.chat(request);

        assertEquals("Anna, keep going!", response.message());
        assertEquals(USER_ID, response.userId());
        assertFalse(response.contextUsed());
    }

    // --- AI call fails: fallback ---

    @Test
    void chat_aiCallFails_returnsFallbackResponse() {
        when(properties.isEnabled()).thenReturn(true);
        stubChatClientThrows(new RuntimeException("API error"));
        when(contextBuilder.buildFallbackResponse(isNull())).thenReturn("Fallback on error");

        AICoachRequestDto request = new AICoachRequestDto("Hello", null, false);
        AICoachResponseDto response = service.chat(request);

        assertEquals("Fallback on error", response.message());
        assertFalse(response.contextUsed());
    }

    @Test
    void chat_aiCallFails_withUserId_fetchesNameForFallback() {
        when(properties.isEnabled()).thenReturn(true);
        stubChatClientThrows(new RuntimeException("API error"));

        UserSettings settings = new UserSettings(USER_ID);
        settings.setName("Erik");
        when(userSettingsService.getOrCreateSettings(USER_ID)).thenReturn(settings);
        when(contextBuilder.buildFallbackResponse("Erik")).thenReturn("Erik, stay strong!");

        AICoachRequestDto request = new AICoachRequestDto("Help me", USER_ID, true);
        AICoachResponseDto response = service.chat(request);

        assertEquals("Erik, stay strong!", response.message());
    }

    // --- Chat without context ---

    @Test
    void chat_noContext_usesBasicPrompt() {
        when(properties.isEnabled()).thenReturn(true);
        stubChatClientResponse("AI response");

        AICoachRequestDto request = new AICoachRequestDto("How are you?", null, false);
        AICoachResponseDto response = service.chat(request);

        assertEquals("AI response", response.message());
        assertFalse(response.contextUsed());
        assertNull(response.userId());
        verify(requestSpec).system(contains("empatisk"));
        verify(requestSpec).user("How are you?");
    }

    @Test
    void chat_withUserIdButNoContext_usesBasicPrompt() {
        when(properties.isEnabled()).thenReturn(true);
        stubChatClientResponse("Basic response");

        AICoachRequestDto request = new AICoachRequestDto("Hello", USER_ID, false);
        AICoachResponseDto response = service.chat(request);

        assertEquals("Basic response", response.message());
        assertFalse(response.contextUsed());
        assertEquals(USER_ID, response.userId());
        // Should NOT call userSettingsService for context building
        verify(userSettingsService, never()).getOrCreateSettings(anyString());
    }

    // --- Chat with context ---

    @Test
    void chat_withContext_buildsContextualPrompt() {
        when(properties.isEnabled()).thenReturn(true);
        stubChatClientResponse("Personalized response");

        UserSettings settings = new UserSettings(USER_ID);
        settings.setName("Anna");
        settings.setRecoveryStage(RecoveryStage.ONE_TO_FOUR_WEEKS);
        settings.setUserGoals(List.of(UserGoal.STAY_CLEAN));
        UserProgress progress = new UserProgress(USER_ID);

        when(userSettingsService.getOrCreateSettings(USER_ID)).thenReturn(settings);
        when(userProgressService.getOrCreateUser(USER_ID)).thenReturn(progress);
        when(userChallengeRepository.findByUserIdAndCompleted(USER_ID, true))
                .thenReturn(List.of());
        when(contextBuilder.buildSystemPrompt(eq(settings), eq(progress), eq(RecoveryStage.ONE_TO_FOUR_WEEKS)))
                .thenReturn("System prompt");
        when(contextBuilder.buildUserContext(eq(settings), eq(List.of(UserGoal.STAY_CLEAN)), isNull(), eq(0)))
                .thenReturn("User context");

        AICoachRequestDto request = new AICoachRequestDto("I need help", USER_ID, true);
        AICoachResponseDto response = service.chat(request);

        assertEquals("Personalized response", response.message());
        assertTrue(response.contextUsed());
        assertEquals(USER_ID, response.userId());
        verify(requestSpec).system("System prompt\nUser context");
    }

    @Test
    void chat_contextBuildFails_fallsBackToBasicPrompt() {
        when(properties.isEnabled()).thenReturn(true);
        stubChatClientResponse("Basic fallback response");

        when(userSettingsService.getOrCreateSettings(USER_ID))
                .thenThrow(new RuntimeException("DB error"));

        AICoachRequestDto request = new AICoachRequestDto("Hello", USER_ID, true);
        AICoachResponseDto response = service.chat(request);

        assertEquals("Basic fallback response", response.message());
        // Even though context building failed, the AI call still succeeds with basic prompt
        verify(requestSpec).system(contains("empatisk"));
    }

    // --- Motivation method ---

    @Test
    void getMotivation_delegatesToChat() {
        when(properties.isEnabled()).thenReturn(true);
        stubChatClientResponse("Stay strong!");

        String result = service.getMotivation("Give me motivation");

        assertEquals("Stay strong!", result);
        verify(requestSpec).user("Give me motivation");
    }

    // --- Personalized coaching ---

    @Test
    void getPersonalizedCoaching_setsUserIdAndContext() {
        when(properties.isEnabled()).thenReturn(true);
        stubChatClientResponse("Personal advice");

        UserSettings settings = new UserSettings(USER_ID);
        UserProgress progress = new UserProgress(USER_ID);

        when(userSettingsService.getOrCreateSettings(USER_ID)).thenReturn(settings);
        when(userProgressService.getOrCreateUser(USER_ID)).thenReturn(progress);
        when(userChallengeRepository.findByUserIdAndCompleted(USER_ID, true))
                .thenReturn(List.of());
        when(contextBuilder.buildSystemPrompt(any(), any(), any())).thenReturn("System");
        when(contextBuilder.buildUserContext(any(), any(), any(), anyInt())).thenReturn("Context");

        AICoachResponseDto response = service.getPersonalizedCoaching(USER_ID, "Help");

        assertEquals("Personal advice", response.message());
        assertTrue(response.contextUsed());
        assertEquals(USER_ID, response.userId());
    }

    // --- Null/empty input handling ---

    @Test
    void chat_nullMessage_stillCallsAI() {
        when(properties.isEnabled()).thenReturn(true);
        // Need to stub user(null) explicitly since anyString() does not match null
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user((String) null)).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Response to null");

        AICoachRequestDto request = new AICoachRequestDto(null, null, false);
        AICoachResponseDto response = service.chat(request);

        assertEquals("Response to null", response.message());
        verify(requestSpec).user((String) null);
    }

    @Test
    void chat_emptyMessage_stillCallsAI() {
        when(properties.isEnabled()).thenReturn(true);
        stubChatClientResponse("Response to empty");

        AICoachRequestDto request = new AICoachRequestDto("", null, false);
        AICoachResponseDto response = service.chat(request);

        assertEquals("Response to empty", response.message());
    }

    @Test
    void chat_aiReturnsNull_returnsEmptyString() {
        when(properties.isEnabled()).thenReturn(true);
        stubChatClientResponse(null);

        AICoachRequestDto request = new AICoachRequestDto("Hello", null, false);
        AICoachResponseDto response = service.chat(request);

        assertEquals("", response.message());
    }

    @Test
    void chat_aiReturnsWhitespace_returnsTrimmedResponse() {
        when(properties.isEnabled()).thenReturn(true);
        stubChatClientResponse("  Hello world  ");

        AICoachRequestDto request = new AICoachRequestDto("Hi", null, false);
        AICoachResponseDto response = service.chat(request);

        assertEquals("Hello world", response.message());
    }

    // --- isAIAvailable ---

    @Test
    void isAIAvailable_enabled_returnsTrue() {
        when(properties.isEnabled()).thenReturn(true);
        assertTrue(service.isAIAvailable());
    }

    @Test
    void isAIAvailable_disabled_returnsFalse() {
        when(properties.isEnabled()).thenReturn(false);
        assertFalse(service.isAIAvailable());
    }

    // --- Response time tracking ---

    @Test
    void chat_responseIncludesResponseTime() {
        when(properties.isEnabled()).thenReturn(true);
        stubChatClientResponse("Quick response");

        AICoachRequestDto request = new AICoachRequestDto("Hi", null, false);
        AICoachResponseDto response = service.chat(request);

        assertTrue(response.responseTimeMs() >= 0);
    }

    // --- Fallback when user settings lookup fails ---

    @Test
    void chat_aiDisabled_userSettingsLookupFails_fallbackStillWorks() {
        when(properties.isEnabled()).thenReturn(false);
        when(userSettingsService.getOrCreateSettings(USER_ID))
                .thenThrow(new RuntimeException("DB down"));
        when(contextBuilder.buildFallbackResponse(isNull())).thenReturn("Generic fallback");

        AICoachRequestDto request = new AICoachRequestDto("Help", USER_ID, false);
        AICoachResponseDto response = service.chat(request);

        assertEquals("Generic fallback", response.message());
        assertFalse(response.contextUsed());
    }

    // --- Completed challenges counted correctly ---

    @Test
    void chat_withContext_countsCompletedChallenges() {
        when(properties.isEnabled()).thenReturn(true);
        stubChatClientResponse("Great progress!");

        UserSettings settings = new UserSettings(USER_ID);
        settings.setUserGoals(List.of());
        UserProgress progress = new UserProgress(USER_ID);

        when(userSettingsService.getOrCreateSettings(USER_ID)).thenReturn(settings);
        when(userProgressService.getOrCreateUser(USER_ID)).thenReturn(progress);

        UserChallenge c1 = mock(UserChallenge.class);
        UserChallenge c2 = mock(UserChallenge.class);
        UserChallenge c3 = mock(UserChallenge.class);
        when(userChallengeRepository.findByUserIdAndCompleted(USER_ID, true))
                .thenReturn(List.of(c1, c2, c3));

        when(contextBuilder.buildSystemPrompt(any(), any(), any())).thenReturn("System");
        when(contextBuilder.buildUserContext(eq(settings), any(), any(), eq(3)))
                .thenReturn("Context with 3 challenges");

        AICoachRequestDto request = new AICoachRequestDto("How am I doing?", USER_ID, true);
        service.chat(request);

        verify(contextBuilder).buildUserContext(eq(settings), any(), any(), eq(3));
    }
}
