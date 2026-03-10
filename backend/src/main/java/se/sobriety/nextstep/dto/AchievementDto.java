package se.sobriety.nextstep.dto;

/**
 * DTO för achievement response
 */
public record AchievementDto(
        int id,
        String title,
        String description,
        boolean unlocked,
        int requiredPoints,
        int requiredLevel
) {}

