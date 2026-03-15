package se.sobriety.nextstep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO för AI Coach
 */
public record AICoachRequestDto(
        @NotBlank(message = "Message is required")
        @Size(max = 2000, message = "Message must be at most 2000 characters")
        String message,

        String userId,

        boolean includeContext
) {
    public AICoachRequestDto(String message, String userId) {
        this(message, userId, true);
    }
}
