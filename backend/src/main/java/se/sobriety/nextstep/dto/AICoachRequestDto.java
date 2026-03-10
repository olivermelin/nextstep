package se.sobriety.nextstep.dto;

/**
 * Request DTO för AI Coach
 */
public record AICoachRequestDto(
        String message,
        String userId,
        boolean includeContext
) {
    public AICoachRequestDto(String message, String userId) {
        this(message, userId, true);
    }
}

