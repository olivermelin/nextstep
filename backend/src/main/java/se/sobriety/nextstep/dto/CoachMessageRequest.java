package se.sobriety.nextstep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO för coach-meddelanden
 */
public record CoachMessageRequest(
        @NotBlank(message = "Message is required")
        @Size(max = 2000, message = "Message must be at most 2000 characters")
        String message,

        String sessionId // valfritt – om null skapas/hämtas aktiv session
) {
}
