package se.sobriety.nextstep.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import se.sobriety.nextstep.config.AICoachProperties;
import se.sobriety.nextstep.dto.AICoachRequestDto;
import se.sobriety.nextstep.dto.AICoachResponseDto;
import se.sobriety.nextstep.entity.BackgroundInfo;
import se.sobriety.nextstep.entity.RecoveryStage;
import se.sobriety.nextstep.entity.UserGoal;
import se.sobriety.nextstep.entity.UserProgress;
import se.sobriety.nextstep.entity.UserSettings;
import se.sobriety.nextstep.repository.UserChallengeRepository;
import se.sobriety.nextstep.service.ai.AIContextBuilder;

import java.util.List;

@Service
public class AICoachService {

    private static final Logger log = LoggerFactory.getLogger(AICoachService.class);

    private final AICoachProperties properties;
    private final AIContextBuilder contextBuilder;
    private final UserSettingsService userSettingsService;
    private final UserProgressService userProgressService;
    private final UserChallengeRepository userChallengeRepository;
    private final ChatClient chatClient;

    public AICoachService(
            AICoachProperties properties,
            AIContextBuilder contextBuilder,
            UserSettingsService userSettingsService,
            UserProgressService userProgressService,
            UserChallengeRepository userChallengeRepository,
            ChatClient.Builder chatClientBuilder
    ) {
        this.properties = properties;
        this.contextBuilder = contextBuilder;
        this.userSettingsService = userSettingsService;
        this.userProgressService = userProgressService;
        this.userChallengeRepository = userChallengeRepository;
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Get motivation message - simple endpoint with default message
     */
    public String getMotivation(String message) {
        AICoachRequestDto request = new AICoachRequestDto(message, null, false);
        AICoachResponseDto response = chat(request);
        return response.message();
    }

    /**
     * Get personalized coaching response with full context
     */
    public AICoachResponseDto getPersonalizedCoaching(String userId, String message) {
        AICoachRequestDto request = new AICoachRequestDto(message, userId, true);
        return chat(request);
    }

    /**
     * Main chat method that handles AI interaction
     */
    public AICoachResponseDto chat(AICoachRequestDto request) {
        long startTime = System.currentTimeMillis();

        try {
            // If AI is disabled, use fallback
            if (!properties.isEnabled()) {
                return createFallbackResponse(request, startTime);
            }

            // Build context if userId is provided and context requested
            String systemPrompt;
            boolean contextUsed = false;

            if (request.userId() != null && request.includeContext()) {
                systemPrompt = buildContextualSystemPrompt(request.userId());
                contextUsed = true;
            } else {
                systemPrompt = buildBasicSystemPrompt();
            }

            // Call OpenAI via Spring AI ChatClient
            String responseMessage = chatClient.prompt()
                    .system(systemPrompt)
                    .user(request.message())
                    .call()
                    .content();

            long responseTime = System.currentTimeMillis() - startTime;

            return new AICoachResponseDto(
                    responseMessage != null ? responseMessage.trim() : "",
                    request.userId(),
                    contextUsed,
                    responseTime
            );

        } catch (Exception e) {
            log.error("Error calling OpenAI API: {}", e.getMessage(), e);
            return createFallbackResponse(request, startTime);
        }
    }

    /**
     * Build contextual system prompt with user data
     */
    private String buildContextualSystemPrompt(String userId) {
        try {
            UserSettings settings = userSettingsService.getOrCreateSettings(userId);
            UserProgress progress = userProgressService.getOrCreateUser(userId);

            RecoveryStage recoveryStage = settings.getRecoveryStage();
            List<UserGoal> goals = settings.getUserGoals();
            BackgroundInfo backgroundInfo = settings.getBackgroundInfo();

            int completedChallenges = (int) userChallengeRepository
                    .findByUserIdAndCompleted(userId, true)
                    .stream()
                    .count();

            String systemPrompt = contextBuilder.buildSystemPrompt(settings, progress, recoveryStage);
            String userContext = contextBuilder.buildUserContext(settings, goals, backgroundInfo, completedChallenges);

            return systemPrompt + "\n" + userContext;

        } catch (Exception e) {
            log.warn("Failed to build contextual prompt for user {}: {}", userId, e.getMessage());
            return buildBasicSystemPrompt();
        }
    }

    /**
     * Build basic system prompt without user context
     */
    private String buildBasicSystemPrompt() {
        return """
                Du är en empatisk och professionell återhämtningscoach för NextStep-appen.
                Din roll är att stödja användare i deras återhämtning från beroende.
                Du ska vara uppmuntrande, icke-dömande och fokusera på små steg framåt.
                
                RIKTLINJER:
                - Var alltid empatisk och stödjande
                - Fokusera på små, konkreta steg
                - Uppmuntra självreflektion
                - Var icke-dömande och respektfull
                - Använd svenska språket
                - Håll svaren koncisa (max 2-3 meningar om inte användaren frågar mer)
                """;
    }

    /**
     * Create fallback response when AI is unavailable
     */
    private AICoachResponseDto createFallbackResponse(AICoachRequestDto request, long startTime) {
        String userName = null;
        if (request.userId() != null) {
            try {
                UserSettings settings = userSettingsService.getOrCreateSettings(request.userId());
                userName = settings.getName();
            } catch (Exception e) {
                log.debug("Could not fetch user name for fallback: {}", e.getMessage());
            }
        }

        String fallbackMessage = contextBuilder.buildFallbackResponse(userName);
        long responseTime = System.currentTimeMillis() - startTime;

        return new AICoachResponseDto(
                fallbackMessage,
                request.userId(),
                false,
                responseTime
        );
    }

    /**
     * Check if AI is currently available
     */
    public boolean isAIAvailable() {
        return properties.isEnabled();
    }
}
