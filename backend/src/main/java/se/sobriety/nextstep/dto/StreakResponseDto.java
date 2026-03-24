package se.sobriety.nextstep.dto;

import java.time.LocalDate;

/**
 * Svar-DTO för streak-data
 */
public record StreakResponseDto(
        String userId,
        int currentStreak,
        int longestStreak,
        LocalDate lastActivityDate,
        LocalDate streakStartDate,
        boolean streakActive,
        boolean activityRegisteredToday
) {}
