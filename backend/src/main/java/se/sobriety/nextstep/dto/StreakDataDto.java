package se.sobriety.nextstep.dto;

import java.util.List;

public record StreakDataDto(
        int currentStreak,
        int longestStreak,
        int streakFreezes,
        List<CalendarDayDto> calendarDays
) {
    public record CalendarDayDto(
            String date,
            int activityCount
    ) {}
}
