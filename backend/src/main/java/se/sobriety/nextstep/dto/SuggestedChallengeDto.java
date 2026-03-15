package se.sobriety.nextstep.dto;

/**
 * Lightweight DTO for challenge recommendations from the AI coach.
 * Includes a pre-computed frontend URL for direct linking.
 */
public record SuggestedChallengeDto(
        Long id,
        String title,
        String description,
        int durationMinutes,
        String difficulty,
        String category,
        String url
) {}

