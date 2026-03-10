package se.sobriety.nextstep.dto;

import se.sobriety.nextstep.entity.CrisisLevel;

/**
 * Response DTO för coach-meddelanden via Claude API
 */
public record CoachMessageResponse(
        String response,
        CrisisLevel crisisLevel,
        String sessionId
) {
}

