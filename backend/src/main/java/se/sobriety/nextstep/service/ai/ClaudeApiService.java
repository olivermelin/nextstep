package se.sobriety.nextstep.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import se.sobriety.nextstep.config.AICoachProperties;
import se.sobriety.nextstep.dto.CoachMessageResponse;
import se.sobriety.nextstep.entity.*;
import se.sobriety.nextstep.repository.UserChallengeRepository;
import se.sobriety.nextstep.service.UserProgressService;
import se.sobriety.nextstep.service.UserSettingsService;

import java.util.List;
import java.util.Map;

/**
 * Huvudingångspunkt för Claude AI-coach integration.
 * Hanterar hela flödet: session → krisdetektering → systemprompt → API-anrop → persistens.
 */
@Service
public class ClaudeApiService {

    private static final Logger log = LoggerFactory.getLogger(ClaudeApiService.class);

    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final String DEFAULT_MODEL = "claude-haiku-4-5";
    private static final int MAX_TOKENS = 1024;

    private final AICoachProperties properties;
    private final ConversationService conversationService;
    private final CrisisDetectionService crisisDetectionService;
    private final SystemPromptBuilder systemPromptBuilder;
    private final UserSettingsService userSettingsService;
    private final UserProgressService userProgressService;
    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeRecommendationParser challengeRecommendationParser;
    private final RestClient restClient;

    public ClaudeApiService(AICoachProperties properties,
                            ConversationService conversationService,
                            CrisisDetectionService crisisDetectionService,
                            SystemPromptBuilder systemPromptBuilder,
                            UserSettingsService userSettingsService,
                            UserProgressService userProgressService,
                            UserChallengeRepository userChallengeRepository,
                            ChallengeRecommendationParser challengeRecommendationParser) {
        this.properties = properties;
        this.conversationService = conversationService;
        this.crisisDetectionService = crisisDetectionService;
        this.systemPromptBuilder = systemPromptBuilder;
        this.userSettingsService = userSettingsService;
        this.userProgressService = userProgressService;
        this.userChallengeRepository = userChallengeRepository;
        this.challengeRecommendationParser = challengeRecommendationParser;
        this.restClient = RestClient.builder()
                .baseUrl(ANTHROPIC_API_URL)
                .build();
    }

    /**
     * Huvudmetod: skickar ett meddelande och returnerar coach-svar.
     *
     * 1. Hämtar/skapar aktiv session
     * 2. Kör krisdetektering – vid CRITICAL returneras krishanteringssvar direkt
     * 3. Bygger systemprompt via SystemPromptBuilder
     * 4. Hämtar de senaste 20 meddelandena som kontextfönster
     * 5. Sparar användarens meddelande
     * 6. Anropar Claude API
     * 7. Sparar och returnerar svaret
     */
    public CoachMessageResponse sendMessage(String userId, String userMessage) {
        // 1. Hämta eller skapa aktiv session
        CoachSession session = conversationService.getOrCreateActiveSession(userId);

        // 2. Krisdetektering
        CrisisLevel crisisLevel = crisisDetectionService.analyze(userMessage);

        if (crisisLevel == CrisisLevel.CRITICAL) {
            log.warn("CRITICAL crisis detected for user {}. Returning crisis response without API call.", userId);

            // Spara användarens meddelande med krisflagga
            conversationService.saveMessageWithCrisis(session, "user", userMessage, CrisisLevel.CRITICAL);

            // Spara krishanteringssvar
            String crisisResponse = crisisDetectionService.getCrisisResponse();
            conversationService.saveMessage(session, "assistant", crisisResponse);

            return new CoachMessageResponse(
                    crisisResponse,
                    CrisisLevel.CRITICAL,
                    session.getSessionId()
            );
        }

        // 3. Bygg systemprompt
        String systemPrompt = buildSystemPrompt(userId, crisisLevel);

        // 4. Hämta meddelandehistorik (senaste 20)
        List<Map<String, String>> messageHistory = conversationService.buildMessageHistory(session.getSessionId());

        // 5. Spara användarens meddelande
        if (crisisLevel == CrisisLevel.ELEVATED) {
            conversationService.saveMessageWithCrisis(session, "user", userMessage, CrisisLevel.ELEVATED);
        } else {
            conversationService.saveMessage(session, "user", userMessage);
        }

        // Lägg till det nya meddelandet i historiken för API-anropet
        messageHistory = new java.util.ArrayList<>(messageHistory);
        messageHistory.add(Map.of("role", "user", "content", userMessage));

        // 6. Anropa AI API (Groq eller Claude)
        String assistantResponse;
        try {
            assistantResponse = callApi(systemPrompt, messageHistory);
        } catch (Exception e) {
            log.error("Failed to call AI API for user {}: {}", userId, e.getMessage(), e);
            assistantResponse = buildFallbackResponse(userId);
        }

        // 7. Parsa challenge-rekommendationer ur svaret
        ChallengeRecommendationParser.ParseResult parseResult = challengeRecommendationParser.parse(assistantResponse);
        String cleanedResponse = parseResult.cleanedResponse();

        // 8. Spara och returnera assistentens svar (spara rensat svar utan taggar)
        conversationService.saveMessage(session, "assistant", cleanedResponse);

        return new CoachMessageResponse(
                cleanedResponse,
                crisisLevel,
                session.getSessionId(),
                parseResult.suggestedChallenges()
        );
    }

    /**
     * Bygger systemprompt baserat på användarens kontext.
     */
    private String buildSystemPrompt(String userId, CrisisLevel crisisLevel) {
        try {
            UserSettings settings = userSettingsService.getOrCreateSettings(userId);
            UserProgress progress = userProgressService.getOrCreateUser(userId);

            int completedChallenges = (int) userChallengeRepository
                    .findByUserIdAndCompleted(userId, true)
                    .stream()
                    .count();

            // Använd CoachPersonality – default SUPPORTIVE (kan utökas med UserSettings-fält)
            CoachPersonality personality = CoachPersonality.SUPPORTIVE;

            return systemPromptBuilder.build(settings, progress, completedChallenges, personality, crisisLevel);
        } catch (Exception e) {
            log.warn("Failed to build contextual system prompt for user {}: {}", userId, e.getMessage());
            return buildDefaultSystemPrompt();
        }
    }

    /**
     * Routar API-anropet till Groq (dev) eller Claude (prod) baserat på konfiguration.
     */
    private String callApi(String systemPrompt, List<Map<String, String>> messages) {
        if (properties.isUseGroq()) {
            return callGroqApi(systemPrompt, messages);
        }
        return callClaudeApi(systemPrompt, messages);
    }

    /**
     * Anropar Groq API (OpenAI-kompatibelt format) – gratis under utveckling.
     */
    @SuppressWarnings("unchecked")
    private String callGroqApi(String systemPrompt, List<Map<String, String>> messages) {
        String apiKey = properties.getGroqApiKey();

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GROQ_API_KEY is not configured");
        }

        String model = properties.getGroqModel() != null ? properties.getGroqModel() : "llama-3.1-8b-instant";

        // Groq använder OpenAI-format: system som första meddelande
        List<Map<String, String>> allMessages = new java.util.ArrayList<>();
        allMessages.add(Map.of("role", "system", "content", systemPrompt));
        allMessages.addAll(messages);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", MAX_TOKENS,
                "messages", allMessages
        );

        RestClient groqClient = RestClient.builder()
                .baseUrl("https://api.groq.com/openai/v1/chat/completions")
                .build();

        Map<String, Object> response = groqClient.post()
                .header("Authorization", "Bearer " + apiKey)
                .header("content-type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new RuntimeException("Empty response from Groq API");
        }

        // OpenAI-format: { "choices": [{ "message": { "content": "..." } }] }
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("No choices in Groq API response");
        }

        Map<String, Object> message = (Map<String, Object>) choices.getFirst().get("message");
        return (String) message.get("content");
    }

    /**
     * Anropar Anthropic Claude API via RestClient.
     */
    @SuppressWarnings("unchecked")
    private String callClaudeApi(String systemPrompt, List<Map<String, String>> messages) {
        String apiKey = properties.getAnthropicApiKey();

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("ANTHROPIC_API_KEY is not configured");
        }

        String model = properties.getClaudeModel() != null ? properties.getClaudeModel() : DEFAULT_MODEL;

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", MAX_TOKENS,
                "system", systemPrompt,
                "messages", messages
        );

        Map<String, Object> response = restClient.post()
                .header("x-api-key", apiKey)
                .header("anthropic-version", ANTHROPIC_VERSION)
                .header("content-type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new RuntimeException("Empty response from Claude API");
        }

        // Parse response: { "content": [{ "type": "text", "text": "..." }] }
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
        if (content == null || content.isEmpty()) {
            throw new RuntimeException("No content in Claude API response");
        }

        return (String) content.getFirst().get("text");
    }

    /**
     * Fallback-svar när API inte är tillgängligt.
     */
    private String buildFallbackResponse(String userId) {
        try {
            UserSettings settings = userSettingsService.getOrCreateSettings(userId);
            String name = settings.getName();

            if (name != null && !name.isBlank() && !name.equals("Default Name")) {
                return name + ", just nu kan jag inte nå min AI-tjänst, men jag vill att du vet att du gör ett bra jobb. " +
                        "Fortsätt ta en dag i taget!";
            }
        } catch (Exception ignored) {
        }

        return "Just nu kan jag inte nå min AI-tjänst, men jag vill att du vet att du gör ett bra jobb. " +
                "Fortsätt ta en dag i taget! Om du behöver prata med någon, ring Mind Självmordslinjen: 90101.";
    }

    /**
     * Default systemprompt utan användarkontext.
     */
    private String buildDefaultSystemPrompt() {
        return """
                Du är en återhämtningscoach i NextStep-appen – en svensk app som hjälper människor i återhämtning från beroende.
                Du är en stödjande, icke-dömande återhämtningskompis som pratar med användaren på svenska.
                
                GRUNDREGLER:
                - Svara ALLTID på svenska
                - Var icke-dömande och respektfull
                - Fokusera på små, konkreta steg framåt
                - Uppmuntra självreflektion
                - Håll svaren koncisa (2-4 meningar)
                - Du är INTE läkare eller terapeut – hänvisa till professionell hjälp vid behov
                
                KRISHJÄLP:
                - Mind Självmordslinjen: 90101
                - Jourhavande medmänniska: 08-702 16 80
                - Vid akut fara: ring 112
                """;
    }

    /**
     * Kontrollerar om AI API är konfigurerat och tillgängligt (Claude eller Groq).
     */
    public boolean isAvailable() {
        if (!properties.isEnabled()) return false;

        if (properties.isUseGroq()) {
            return properties.getGroqApiKey() != null && !properties.getGroqApiKey().isBlank();
        }

        return properties.getAnthropicApiKey() != null && !properties.getAnthropicApiKey().isBlank();
    }
}

