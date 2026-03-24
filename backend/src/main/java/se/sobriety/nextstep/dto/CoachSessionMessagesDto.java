package se.sobriety.nextstep.dto;

import java.util.List;

public record CoachSessionMessagesDto(
        String sessionId,
        String status,
        String createdAt,
        List<CoachMessageDto> messages
) {}
