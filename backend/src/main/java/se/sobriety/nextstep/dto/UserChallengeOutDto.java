package se.sobriety.nextstep.dto;

import java.time.LocalDateTime;

public record UserChallengeOutDto(
        Long id,
        String userId,
        ChallengeOutDto challenge,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        boolean completed,
        int pointsEarned
) {}

