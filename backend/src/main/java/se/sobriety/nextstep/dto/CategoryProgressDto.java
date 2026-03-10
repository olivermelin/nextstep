package se.sobriety.nextstep.dto;

/**
 * DTO för kategori-framsteg
 */
public record CategoryProgressDto(
        String category,
        int points
) {}

