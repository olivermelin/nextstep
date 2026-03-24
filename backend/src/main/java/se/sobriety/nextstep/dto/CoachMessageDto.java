package se.sobriety.nextstep.dto;

public record CoachMessageDto(
        String role,
        String content,
        String crisisLevel,
        String timestamp
) {}
