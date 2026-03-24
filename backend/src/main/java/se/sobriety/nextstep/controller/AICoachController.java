package se.sobriety.nextstep.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import se.sobriety.nextstep.dto.*;
import se.sobriety.nextstep.entity.CoachSession;
import se.sobriety.nextstep.exception.QuotaExceededException;
import se.sobriety.nextstep.service.AICoachService;
import se.sobriety.nextstep.service.QuotaService;
import se.sobriety.nextstep.service.ai.ClaudeApiService;
import se.sobriety.nextstep.service.ai.ConversationService;

import java.util.List;
import java.util.Map;

import static se.sobriety.nextstep.util.SecurityUtils.verifyUserAccess;

@RestController
@RequestMapping("/api/coach")
@Validated
public class AICoachController {

    private final AICoachService aiCoachService;
    private final ClaudeApiService claudeApiService;
    private final ConversationService conversationService;
    private final QuotaService quotaService;

    public AICoachController(AICoachService aiCoachService,
                             ClaudeApiService claudeApiService,
                             ConversationService conversationService,
                             QuotaService quotaService) {
        this.aiCoachService = aiCoachService;
        this.claudeApiService = claudeApiService;
        this.conversationService = conversationService;
        this.quotaService = quotaService;
    }

    /**
     * Simple motivation endpoint - no user context
     * GET /api/coach/motivate?message=Ge mig motivation
     */
    @GetMapping("/motivate")
    public String motivate(@RequestParam(defaultValue = "Ge mig lite motivation!") @Size(max = 2000) String message) {
        return aiCoachService.getMotivation(message);
    }

    /**
     * Personalized chat endpoint with user context
     * POST /api/coach/chat/{userId}
     * Body: { "message": "Jag känner mig svag idag", "includeContext": true }
     */
    @PostMapping("/chat/{userId}")
    public AICoachResponseDto chat(
            @PathVariable String userId,
            @Valid @RequestBody AICoachRequestDto request
    ) {
        verifyUserAccess(userId);
        // Create new request with userId from path
        AICoachRequestDto requestWithUserId = new AICoachRequestDto(
                request.message(),
                userId,
                request.includeContext()
        );
        return aiCoachService.chat(requestWithUserId);
    }

    /**
     * Get personalized coaching based on user's profile and progress
     * POST /api/coach/personalized/{userId}
     * Body: { "message": "Hur ska jag hantera stress?" }
     */
    @PostMapping("/personalized/{userId}")
    public AICoachResponseDto getPersonalizedCoaching(
            @PathVariable String userId,
            @RequestBody Map<String, String> body
    ) {
        verifyUserAccess(userId);
        String message = body.getOrDefault("message", "Ge mig lite motivation!");
        if (message.length() > 2000) {
            throw new IllegalArgumentException("Message must be at most 2000 characters");
        }
        return aiCoachService.getPersonalizedCoaching(userId, message);
    }

    /**
     * Quick motivation for specific user
     * GET /api/coach/quick/{userId}?message=Jag behöver stöd
     */
    @GetMapping("/quick/{userId}")
    public AICoachResponseDto quickMotivation(
            @PathVariable String userId,
            @RequestParam(defaultValue = "Ge mig lite motivation!") @Size(max = 2000) String message
    ) {
        verifyUserAccess(userId);
        return aiCoachService.getPersonalizedCoaching(userId, message);
    }

    /**
     * Claude AI Coach - personalized message endpoint with crisis detection
     * POST /api/coach/message
     * Body: { "message": "Jag behöver stöd idag", "sessionId": null }
     * Returns: { "response": "...", "crisisLevel": "NONE", "sessionId": "uuid" }
     */
    @PostMapping("/message")
    public CoachMessageResponse sendMessage(
            @RequestParam String userId,
            @Valid @RequestBody CoachMessageRequest request
    ) {
        verifyUserAccess(userId);
        return claudeApiService.sendMessage(userId, request.message(), request.sessionId());
    }

    /**
     * Hanterar kvot-undantag — returnerar 429 med kvotstatus
     */
    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<Map<String, Object>> handleQuotaExceeded(QuotaExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "error", "quota_exceeded",
                "message", "Du har nått din dagliga gräns på " + ex.getLimit() + " AI-meddelanden. Uppgradera till Premium för obegränsad access.",
                "used", ex.getUsed(),
                "limit", ex.getLimit()
        ));
    }

    /**
     * GET /api/coach/quota/{userId}
     * Hämtar kvotstatus för användaren (använda/kvar/gräns)
     */
    @GetMapping("/quota/{userId}")
    public QuotaResponseDto getQuota(@PathVariable String userId) {
        verifyUserAccess(userId);
        return quotaService.getQuota(userId);
    }

    // ── Multi-konversation endpoints ──────────────────────────────────

    /**
     * GET /api/coach/sessions?userId=...
     * Lista alla konversationer för en användare
     */
    @GetMapping("/sessions")
    public List<CoachSessionSummaryDto> getSessions(@RequestParam String userId) {
        verifyUserAccess(userId);
        return conversationService.getUserSessions(userId);
    }

    /**
     * GET /api/coach/sessions/{sessionId}/messages?userId=...
     * Hämta alla meddelanden för en specifik konversation
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public CoachSessionMessagesDto getSessionMessages(
            @PathVariable String sessionId,
            @RequestParam String userId) {
        verifyUserAccess(userId);
        return conversationService.getSessionMessages(sessionId, userId);
    }

    /**
     * POST /api/coach/sessions/new?userId=...
     * Skapa en ny konversation (stänger aktiv)
     */
    @PostMapping("/sessions/new")
    public CoachSessionSummaryDto createNewSession(@RequestParam String userId) {
        verifyUserAccess(userId);
        CoachSession session = conversationService.createNewSession(userId);
        return new CoachSessionSummaryDto(
                session.getSessionId(),
                session.getStatus().name(),
                "",
                session.getCreatedAt().toString(),
                session.getLastMessageAt().toString(),
                0
        );
    }

    /**
     * DELETE /api/coach/sessions/{sessionId}?userId=...
     * Tar bort en konversation och alla dess meddelanden permanent.
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable String sessionId,
            @RequestParam String userId) {
        verifyUserAccess(userId);
        conversationService.deleteSession(sessionId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if AI service is available
     * GET /api/coach/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        boolean openAiAvailable = aiCoachService.isAIAvailable();
        boolean claudeAvailable = claudeApiService.isAvailable();
        String activeProvider = claudeAvailable ? "claude/groq" : (openAiAvailable ? "openai" : "none");
        return ResponseEntity.ok(Map.of(
                "aiAvailable", openAiAvailable || claudeAvailable,
                "activeProvider", activeProvider,
                "service", "AI Coach",
                "status", claudeAvailable ? "online" : (openAiAvailable ? "online (openai)" : "fallback mode")
        ));
    }
}
