package se.sobriety.nextstep.dto;

public record CoachSessionSummaryDto(
        String sessionId,
        String status,
        String preview,
        String createdAt,
        String lastMessageAt,
        int messageCount
) {}
