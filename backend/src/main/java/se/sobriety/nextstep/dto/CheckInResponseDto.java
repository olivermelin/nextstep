package se.sobriety.nextstep.dto;

import java.time.LocalDate;

/**
 * Svar-DTO för daglig incheckning.
 * Inkluderar streak-data och ev. daglig belöning för att ge omedelbar feedback.
 */
public record CheckInResponseDto(
        Long id,
        String userId,
        LocalDate checkInDate,
        int moodScore,
        String note,
        boolean alreadyCheckedInToday,
        // Streak-data (fylls i vid ny incheckning)
        int currentStreak,
        boolean streakIncremented,
        // Daglig belöning (genereras vid ny incheckning, null vid uppdatering)
        DailyRewardDto dailyReward
) {
    /**
     * Bakåtkompatibel factory-metod för enkel konvertering utan streak/reward-data.
     */
    public static CheckInResponseDto simple(Long id, String userId, LocalDate checkInDate,
                                            int moodScore, String note, boolean alreadyCheckedIn) {
        return new CheckInResponseDto(id, userId, checkInDate, moodScore, note,
                alreadyCheckedIn, 0, false, null);
    }
}
