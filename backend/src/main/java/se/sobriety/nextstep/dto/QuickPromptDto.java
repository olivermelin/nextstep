package se.sobriety.nextstep.dto;

/**
 * Representerar en snabbfråga som visas i AI-coachens sidopanel.
 */
public record QuickPromptDto(
        String icon,
        String label,
        String prompt
) {}
