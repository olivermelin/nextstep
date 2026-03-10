package se.sobriety.nextstep.dto;

/**
 * DTO för total framsteg response
 */
public record ProgressResponseDto(
        String userId,
        int totalPoints,
        int level,
        String lastUpdated
) {}

