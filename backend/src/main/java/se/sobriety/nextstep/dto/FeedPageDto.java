package se.sobriety.nextstep.dto;

import java.util.List;

public record FeedPageDto(
        List<FeedItemDto> items,
        Long nextCursor,
        boolean hasMore
) {}
