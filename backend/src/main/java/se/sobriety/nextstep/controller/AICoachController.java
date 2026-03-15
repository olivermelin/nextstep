package se.sobriety.nextstep.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import se.sobriety.nextstep.dto.AICoachRequestDto;
import se.sobriety.nextstep.dto.AICoachResponseDto;
import se.sobriety.nextstep.dto.CoachMessageRequest;
import se.sobriety.nextstep.dto.CoachMessageResponse;
import se.sobriety.nextstep.service.AICoachService;
import se.sobriety.nextstep.service.ai.ClaudeApiService;

import java.util.Map;

@RestController
@RequestMapping("/api/coach")
@Validated
public class AICoachController {

    private final AICoachService aiCoachService;
    private final ClaudeApiService claudeApiService;

    public AICoachController(AICoachService aiCoachService, ClaudeApiService claudeApiService) {
        this.aiCoachService = aiCoachService;
        this.claudeApiService = claudeApiService;
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
        return claudeApiService.sendMessage(userId, request.message());
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
