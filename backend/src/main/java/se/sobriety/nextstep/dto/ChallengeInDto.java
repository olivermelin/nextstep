package se.sobriety.nextstep.dto;

public record ChallengeInDto(
        String title,
        String description,
        int durationMinutes,
        String difficulty,
        String category,
        String youtubeUrl,

        String instructions
) {}

