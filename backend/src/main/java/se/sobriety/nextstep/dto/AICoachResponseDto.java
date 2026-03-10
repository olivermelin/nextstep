package se.sobriety.nextstep.dto;

/**
 * Response DTO för AI Coach
 */
public record AICoachResponseDto(
        String message,
        String userId,
        boolean contextUsed,
        long responseTimeMs
) {}

