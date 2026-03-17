package se.sobriety.nextstep.dto;

import java.time.LocalDateTime;

public record UserChallengeOutDto(
        Long id,
        Long challengeId,
        String challengeName,
        String category,
        String difficulty,
        int durationMinutes,
        String status,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        int pointsEarned,
        int actualMinutes
) {}

