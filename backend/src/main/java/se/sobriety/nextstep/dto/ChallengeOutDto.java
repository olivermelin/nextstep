package se.sobriety.nextstep.dto;

import java.time.LocalDateTime;

public record ChallengeOutDto(
        Long id,
        String title,
        String description,
        int durationMinutes,
        String difficulty,
        String category,
        String youtubeUrl,
        String instructions,
        LocalDateTime createdAt
) {}

