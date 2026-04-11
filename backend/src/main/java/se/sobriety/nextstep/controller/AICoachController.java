package se.sobriety.nextstep.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import se.sobriety.nextstep.dto.*;
import se.sobriety.nextstep.entity.CoachSession;
import se.sobriety.nextstep.exception.QuotaExceededException;
import se.sobriety.nextstep.service.QuotaService;
import se.sobriety.nextstep.service.ai.ClaudeApiService;
import se.sobriety.nextstep.service.ai.ConversationService;
import se.sobriety.nextstep.service.ai.QuickPromptService;

import java.util.List;
import java.util.Map;

import static se.sobriety.nextstep.util.SecurityUtils.verifyUserAccess;

@RestController
@RequestMapping("/api/coach")
@Validated
public class AICoachController {

    private final ClaudeApiService claudeApiService;
    private final ConversationService conversationService;
    private final QuotaService quotaService;
    private final QuickPromptService quickPromptService;

    public AICoachController(ClaudeApiService claudeApiService,
                             ConversationService conversationService,
                             QuotaService quotaService,
                             QuickPromptService quickPromptService) {
        this.claudeApiService = claudeApiService;
        this.conversationService = conversationService;
        this.quotaService = quotaService;
        this.quickPromptService = quickPromptService;
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
     * GET /api/coach/quick-prompts/{userId}
     * Returnerar personaliserade snabbfrågor baserat på användarens kontext.
     */
    @GetMapping("/quick-prompts/{userId}")
    public List<QuickPromptDto> getQuickPrompts(@PathVariable String userId) {
        verifyUserAccess(userId);
        return quickPromptService.getQuickPrompts(userId);
    }

    /**
     * Check if AI service is available
     * GET /api/coach/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        boolean available = claudeApiService.isAvailable();
        return ResponseEntity.ok(Map.of(
                "aiAvailable", available,
                "activeProvider", available ? "claude/groq" : "none",
                "service", "AI Coach",
                "status", available ? "online" : "fallback mode"
        ));
    }

    /**
     * POST /api/coach/personalized/{userId}
     * Hämtar ett personligt meddelande från AI-coachen baserat på användarens kontext.
     * Sparas inte i konversationshistoriken (stateless).
     */
    @PostMapping("/personalized/{userId}")
    public CoachMessageResponse getPersonalizedMessage(
            @PathVariable String userId,
            @Valid @RequestBody CoachMessageRequest request
    ) {
        verifyUserAccess(userId);
        return claudeApiService.sendStateless(userId, request.message());
    }
}
