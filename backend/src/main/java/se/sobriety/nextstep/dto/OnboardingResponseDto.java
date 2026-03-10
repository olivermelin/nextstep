package se.sobriety.nextstep.dto;

/**
 * Response för onboarding completion
 */
public record OnboardingResponseDto(
        boolean success,
        String message
) {}

