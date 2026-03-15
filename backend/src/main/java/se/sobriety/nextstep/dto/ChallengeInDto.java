package se.sobriety.nextstep.dto;

import jakarta.validation.constraints.*;

public record ChallengeInDto(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must be at most 200 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        @Min(value = 1, message = "Duration must be at least 1 minute")
        @Max(value = 480, message = "Duration must be at most 480 minutes")
        int durationMinutes,

        @NotBlank(message = "Difficulty is required")
        String difficulty,

        @NotBlank(message = "Category is required")
        String category,

        @Size(max = 500, message = "YouTube URL must be at most 500 characters")
        String youtubeUrl,

        @Size(max = 5000, message = "Instructions must be at most 5000 characters")
        String instructions
) {}
