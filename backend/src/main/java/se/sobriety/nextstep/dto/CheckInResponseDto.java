package se.sobriety.nextstep.dto;

import java.time.LocalDate;

/**
 * Svar-DTO för daglig incheckning
 */
public record CheckInResponseDto(
        Long id,
        String userId,
        LocalDate checkInDate,
        int moodScore,
        String note,
        boolean alreadyCheckedInToday
) {}
