package se.sobriety.nextstep.dto;

/**
 * DTO för signup response
 */
public record SignUpResponseDto(
        String id,
        String email,
        String name,
        String message
) {}