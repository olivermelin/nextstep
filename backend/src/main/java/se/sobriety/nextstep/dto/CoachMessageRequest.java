package se.sobriety.nextstep.dto;

/**
 * Request DTO för coach-meddelanden
 */
public record CoachMessageRequest(
        String message,
        String sessionId // valfritt – om null skapas/hämtas aktiv session
) {
}

