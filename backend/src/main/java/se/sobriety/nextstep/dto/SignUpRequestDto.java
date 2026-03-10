package se.sobriety.nextstep.dto;

/**
 * DTO för signup request
 */
public record SignUpRequestDto(
        String email,
        String password,
        String name
) {}
