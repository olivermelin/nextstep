package se.sobriety.nextstep.dto;

import se.sobriety.nextstep.entity.CrisisLevel;

import java.util.List;

/**
 * Response DTO för coach-meddelanden via Claude API
 */
public record CoachMessageResponse(
        String response,
        CrisisLevel crisisLevel,
        String sessionId,
        List<SuggestedChallengeDto> suggestedChallenges
) {
    /**
     * Convenience constructor without suggested challenges (backwards compatible).
     */
    public CoachMessageResponse(String response, CrisisLevel crisisLevel, String sessionId) {
        this(response, crisisLevel, sessionId, List.of());
    }
}

