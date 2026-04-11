package se.sobriety.nextstep.dto;

import java.util.List;

public record FeedItemDto(
        Long id,
        String userId,
        String displayName,
        String type,
        String message,
        String metadata,
        String createdAt,
        int cheerCount,
        boolean iCheered,
        List<FeedReactionDto> recentCheers
) {}
